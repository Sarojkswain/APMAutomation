/**
 *
 */
package com.ca.apm.systemtest.fld.proxy;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.plugin.RemoteCallException;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.plugin.vo.RemoteCall;
import com.ca.apm.systemtest.fld.plugin.vo.RemoteCallResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Factory class to create client JDK proxies for accessing configured plugins on a particular
 * target node
 *
 * @author keyja01
 */
public class AgentPluginProxyFactory {
    public static final String AGENT_RESPONSE_QUEUE = "fld.agent.response";
    public static final String AGENT_COMMAND_QUEUE = "fld.agent.command";
    public static final String JMS_TRANSPORT_LOG_CATEGORY = "JMS Transport";
    
    private static final Logger logger = LoggerFactory.getLogger(AgentPluginProxyFactory.class);
    private static long nextInvocationId = 0;

    private JmsTemplate jmsTemplate;
    private String target;
    private String processInstanceId;
    
    private InvocationStallListener invocationStallListener;
    private long stallMs;

    protected FldLogger fldLogger;

    private ThreadLocal<ObjectMapper> objectMapperTL = new ThreadLocal<ObjectMapper>() {
        protected ObjectMapper initialValue() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return mapper;
        }
    };

    /**
     * Constructor.
     *
     * @param jmsTemplate
     * @param target
     * @param processInstanceId Activiti process instance id
     */
    public AgentPluginProxyFactory(JmsTemplate jmsTemplate, String target,
        String processInstanceId, InvocationStallListener stallListener, long stallMs) {
        this.jmsTemplate = jmsTemplate;
        this.target = target;
        this.processInstanceId = processInstanceId;
        this.invocationStallListener = stallListener;
        this.stallMs = stallMs;
    }

    public AgentPluginProxyFactory(JmsTemplate jmsTemplate, String target,
                                   String processInstanceId, FldLogger fldLogger,
                                   InvocationStallListener stallListener, long stallMs) {
        this.jmsTemplate = jmsTemplate;
        this.target = target;
        this.processInstanceId = processInstanceId;
        this.fldLogger = fldLogger;
        this.invocationStallListener = stallListener;
        this.stallMs = stallMs;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Plugin> T createJdkProxyWithTimeout(Class<T> pluginClass,
        final String pluginId, Long timeout) {
        AgentPluginProxyInvocationHandler<T> handler = new AgentPluginProxyInvocationHandler<T>(
            pluginClass, pluginId, timeout, stallMs);
        T proxy = (T) Proxy
            .newProxyInstance(pluginClass.getClassLoader(), new Class<?>[]{pluginClass}, handler);

        return proxy;
    }


    public <T extends Plugin> T createJdkProxy(Class<T> pluginClass, final String pluginId) {
        return createJdkProxyWithTimeout(pluginClass, pluginId, null);
    }

    public void setFldLogger(FldLogger fldLogger) {
        this.fldLogger = fldLogger;
    }
    
    
    public InvocationStallListener getInvocationStallListener() {
        return invocationStallListener;
    }

    @Autowired
    public void setInvocationStallListener(InvocationStallListener invocationStallListener) {
        this.invocationStallListener = invocationStallListener;
    }
    
    
    //----------------------------- DEBUG -----------------------------

    protected void logDebug(String category, String tag, String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
        if (fldLogger != null) {
            fldLogger.debug(category, tag, message);
        }
    }

    protected void logDebug(String category, String tag, String pattern, Object...arguments) {
        String msg = null;
        if (logger.isDebugEnabled()) {
            msg = MessageFormat.format(pattern, arguments);
            logger.debug(msg);
        }
        if (fldLogger != null) {
            if (msg == null) {
                msg = MessageFormat.format(pattern, arguments);
            }
            fldLogger.debug(category, tag, msg);
        }
    }

    //----------------------------- ERROR -----------------------------
    
    protected void logError(String category, String tag, String message) {
        if (logger.isErrorEnabled()) {
            logger.error(message);
        }
        if (fldLogger != null) {
            fldLogger.error(category, tag, message);
        }
    }

    protected void logError(String category, String tag, String pattern, Object...arguments) {
        logError(null, category, tag, pattern, arguments);
    }
    
    protected void logError(Throwable ex, String category, String tag, String pattern, Object...arguments) {
        String msg = null;
        if (logger.isErrorEnabled()) {
            msg = MessageFormat.format(pattern, arguments);
            logger.error(msg, ex);
        }
        if (fldLogger != null) {
            if (msg == null) {
                msg = MessageFormat.format(pattern, arguments);
            }
            fldLogger.error(category, tag, msg, ex);
        }
    }

    //----------------------------- INFO -----------------------------
    
    protected void logInfo(String category, String tag, String message) {
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
        if (fldLogger != null) {
            fldLogger.info(category, tag, message);
        }
    }

    protected void logInfo(String category, String tag, String pattern, Object...arguments) {
        logInfo(null, category, tag, pattern, arguments);
    }
    
    protected void logInfo(Throwable ex, String category, String tag, String pattern, Object...arguments) {
        String msg = null;
        if (logger.isInfoEnabled()) {
            msg = MessageFormat.format(pattern, arguments);
            logger.info(msg, ex);
        }
        if (fldLogger != null) {
            if (msg == null) {
                msg = MessageFormat.format(pattern, arguments);
            }
            fldLogger.info(category, tag, msg, ex);
        }
    }
    
    public class AgentPluginProxyInvocationHandler<T extends Plugin> implements InvocationHandler {
        private Class<T> pluginClass;
        private String pluginId;
        private Long timeout;
        
        /**
         * When active, the remote agent should be sending an active ping back with the invocation's 
         * ID. If the proxy has not received a ping on the fld.agent.control topic with the invocation
         * ID within stallMs, the method invocation will be considered as stalled and will be 
         * terminated with an exception.
         */
        private long stallMs = 35000L;

        public AgentPluginProxyInvocationHandler(Class<T> pluginClass, String pluginId,
            Long timeout, long stallMs) {
            this.pluginClass = pluginClass;
            this.pluginId = pluginId;
            this.timeout = timeout;
            this.stallMs = stallMs;
            logger.debug("Creating agent plugin proxy for class {}", this.pluginClass.getName());
        }

        private String _toString() {
            return "FLDProxy:" + pluginClass.getName() + ":" + pluginId;
        }

        private int _hashCode() {
            return pluginId.hashCode();
        }

        private boolean _equals(Object obj) {
            return obj == this;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString")) {
                return _toString();
            } else if (method.getName().equals("hashCode")) {
                return _hashCode();
            } else if (method.getName().equals("equals")) {
                return _equals(args[0]);
            }

            final String invocationid = Long.toHexString(++nextInvocationId);

            // Create a RemoteCall object
            RemoteCall call = new RemoteCall();
            call.setOperation(method.getName());
            call.setCallReferenceId(invocationid);
            call.setPlugin(pluginId);
            call.setTarget(target);
            call.setParameters(args);
            call.setDashboardId(DashboardIdStore.getDashboardId());
            call.setProcessInstanceId(processInstanceId);

            // Serialize to JSON
            ObjectMapper mapper = objectMapperTL.get();
            final String json = mapper.writeValueAsString(call);
            
            logger.info("-- Sending message ------------------");
            logger.info(json);
            logger.info("--------------------");
            
            final String stallListenerRegId = invocationStallListener.listenForStalls(invocationid, stallMs);
            Message m = null;
            try {
                // Send via JMS with targetNode=xxx property set
                jmsTemplate.send(AGENT_COMMAND_QUEUE, new MessageCreator() {

                    public Message createMessage(Session session) throws JMSException {
                        Message msg = session.createTextMessage(json);
                        msg.setStringProperty("fldTarget", target);
                        return msg;
                    }
                });

                // create listener with selector for "remoteInvocationid=xxx"
                jmsTemplate.setReceiveTimeout(timeout == null ? 1200000L : timeout);
                m = jmsTemplate.receiveSelected(AGENT_RESPONSE_QUEUE,
                    RemoteCallResult.REMOTE_INVOCATION_ID_HEADER + "='" + invocationid + "'");
                
            } finally {
                invocationStallListener.unregister(stallListenerRegId);
            }

            if (m == null) {
                final String msg = MessageFormat.format(
                    "Did not receive a response to {0} call"
                        + " before timing out after {1} milliseconds.",
                    method.getName(), jmsTemplate.getReceiveTimeout());
                System.out.println(msg);
                logError(JMS_TRANSPORT_LOG_CATEGORY, "Timeout", 
                    "Did not receive a response to {0} call before timing out after {1} milliseconds.", 
                    method.getName(), jmsTemplate.getReceiveTimeout());
                throw new RemoteCallException(msg);
            }

            // deserialize response and return
            TextMessage tm = (TextMessage) m;
            String responseJson = tm.getText();
            logger.info("-- Receiving message ------------------");
            logger.info(responseJson);
            logger.info("--------------------");

            RemoteCallResult result;
            try {
                result = mapper.readValue(responseJson, RemoteCallResult.class);
            } catch (IOException e) {
                String msg = "Failed to parse result JSON: {0}. Exception message: {1}"; 
                logError(e, JMS_TRANSPORT_LOG_CATEGORY, "Response Json Read Failure", 
                    msg, responseJson, e.getMessage());
                throw new RemoteCallException(e, "ERR_JSON_RECEIVED_MESSAGE_PARSING_FAILED", 
                    msg, responseJson, e.getMessage());
            }

            // TODO handle error conditions and throw an exception
            if (!result.isSuccess()) {
                logError(getClass().getName(), 
                    result.getErrorCode(), 
                    "Remote call failed. Error code: {0}. Error message: {1}", 
                    result.getErrorCode(), result.getErrorMessage());
                
                throw new RemoteCallException(result.getErrorMessage(), 
                    result.getErrorCode());
            }

            return result.getResult();
        }

    }
}
