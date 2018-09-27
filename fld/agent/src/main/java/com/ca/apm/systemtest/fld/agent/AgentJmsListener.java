/**
 *
 */
package com.ca.apm.systemtest.fld.agent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.NetworkUtils;
import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.plugin.RemoteCallException;
import com.ca.apm.systemtest.fld.plugin.agentdownload.AgentDownloadPlugin;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.plugin.vo.HeartbeatRequest;
import com.ca.apm.systemtest.fld.plugin.vo.HeartbeatResponse;
import com.ca.apm.systemtest.fld.plugin.vo.ProcessInstanceIdStore;
import com.ca.apm.systemtest.fld.plugin.vo.RemoteCall;
import com.ca.apm.systemtest.fld.plugin.vo.RemoteCallResult;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/**
 * @author keyja01
 */
public class AgentJmsListener {
    public static Logger log = LoggerFactory.getLogger(AgentJmsListener.class);

    @Autowired
    private PluginRepository pluginRepository;

    @Autowired
    private AgentDownloadPlugin agentDownloadPlugin;
    
    @Autowired
    private InvocationMonitor invocationMonitor;

    @Autowired
    private AgentSpringConfiguration.TimeMonitorBean timeMonitorBean;

    private ThreadLocal<ObjectMapper> objectMapperTL = new ThreadLocal<ObjectMapper>() {
        protected ObjectMapper initialValue() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return mapper;
        }
    };
    private String nodeName;

    /**
     *
     */
    public AgentJmsListener() {
    }


    /**
     * Receives and responds to {@link HeartbeatRequest} messages.
     *
     * @param message
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public String receiveHeartbeat(
        String message) throws JsonParseException, JsonMappingException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Receiving heartbeat request: {}", message);
        }
        ObjectMapper mapper = objectMapperTL.get();
        HeartbeatRequest heartbeatRequest = mapper.readValue(message, HeartbeatRequest.class);
        HeartbeatResponse resp = new HeartbeatResponse();
        InetAddress inetAddress = NetworkUtils.getCurrentIndetAddress();
        if (inetAddress != null) {
            resp.setHostName(inetAddress.getHostName());
            resp.setIp4(inetAddress.getHostAddress());
        }
        resp.setHeartbeatRequestTimestamp(heartbeatRequest.getTimestamp());
        resp.setNodeName(nodeName);
        resp.setVersion((int) agentDownloadPlugin.getCurrentVersion());
        resp.setNtpTimeOffset(timeMonitorBean.getOffset());
        return mapper.writeValueAsString(resp);
    }

    /**
     * Expects a json message encapsulating a method call.
     *
     * @param message
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public Message<String> handleMessage(
        String message) throws JsonParseException, JsonMappingException, IOException {
        
        log.info("-- Receiving message ------------------");
        log.info(message);
        log.info("--------------------");
        
        
        ObjectMapper mapper = objectMapperTL.get();
        RemoteCall call;
        try {
            call = mapper.readValue(message, RemoteCall.class);
        } catch (IOException e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Failed to parse remote call JSON: {1}. Exception: {0}", message);
            throw e;
        }

        DashboardIdStore.setDashboardId(call.getDashboardId());
        ProcessInstanceIdStore.setProcessInstanceId(call.getProcessInstanceId());
        try {
            invocationMonitor.registerInvocationId(call.getCallReferenceId());
            Plugin plugin = pluginRepository.findPlugin(call.getPlugin());
            if (plugin == null) {
                final String msg = MessageFormat.format(
                    "The plugin {0} was not found in the agent's configured plugins",
                    call.getPlugin());
                log.error(msg);
                RemoteCallResult faultResult = RemoteCallResult.createErrorResult("PLUGIN_NOT_FOUND", msg,
                    call.getCallReferenceId(), call.getProcessInstanceId());
                return MessageBuilder
                    .withPayload(toJson(faultResult))
                    .setHeaderIfAbsent(RemoteCallResult.REMOTE_INVOCATION_ID_HEADER, call.getCallReferenceId())
                    .build();
            }
            Method m = findMethod(plugin.getClass(), call.getOperation(), call.getParameters());
            if (m == null) {
                final String msg = MessageFormat.format(
                    "The operation {0} was not found in the {1} plugin",
                    call.getOperation(), call.getPlugin());
                log.error(msg);

                RemoteCallResult faultResult = RemoteCallResult.createErrorResult("OP_NOT_FOUND", msg, 
                    call.getCallReferenceId(), call.getProcessInstanceId());
                return MessageBuilder
                    .withPayload(toJson(faultResult))
                    .setHeaderIfAbsent(RemoteCallResult.REMOTE_INVOCATION_ID_HEADER, call.getCallReferenceId())
                    .build();
            }

            Object[] args = null;

            RemoteCallResult rcResult = new RemoteCallResult();
            rcResult.setProcessInstanceId(call.getProcessInstanceId());
            rcResult.setCallReferenceId(call.getCallReferenceId());
            Throwable throwable = null;
            try {
                args = prepareArguments(m, call.getParameters());
                Object result = m.invoke(plugin, args);
                rcResult.setResult(result);
                rcResult.setSuccess(true);
            } catch (Exception ex) {
                throwable = ex;
                ErrorUtils.logExceptionFmt(log, ex,
                    "Got exception when trying to call {1} on {2}. Exception: {0}",
                    call.getOperation(), call.getPlugin());
            }

            if (throwable != null) {
                rcResult.setSuccess(false);
                if (throwable instanceof InvocationTargetException) {
                    InvocationTargetException ite = (InvocationTargetException) throwable;
                    throwable = ite.getTargetException();
                }
                if (throwable instanceof RemoteCallException) {
                    RemoteCallException rce = (RemoteCallException) throwable;
                    rcResult.setErrorCode(rce.getErrorCode());
                    rcResult.setErrorMessage(rce.getMessage());
                } else {
                    rcResult.setErrorCode(throwable.getClass().getName());
                    rcResult.setErrorMessage(throwable.getMessage());
                }
            }

            String resultJson = mapper.writeValueAsString(rcResult);
            Map<String, Object> map = new HashMap<>(1);
            map.put(RemoteCallResult.REMOTE_INVOCATION_ID_HEADER, call.getCallReferenceId());
            MessageHeaders messageHeaders = new MessageHeaders(map);
            Message<String> msg = MessageBuilder.createMessage(resultJson, messageHeaders);
            
            log.info("-- Sending message ------------------");
            log.info(resultJson);
            log.info("--------------------");

            return msg;
        } finally {
            invocationMonitor.unregisterInvocationId(call.getCallReferenceId());
            DashboardIdStore.clearDashboardId();
        }
    }


    private Object[] prepareArguments(Method m, Object[] parameters) {
        if (parameters == null) {
            parameters = ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        Object[] args = new Object[parameters.length];
        Class<?>[] ptypes = m.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            Object param = parameters[i];
            if (param == null) {
                args[i] = null;
                continue;
            }
            // otherwise attempt conversion if necessary
            args[i] = convertArgument(param, ptypes[i]);
        }
        return args;
    }


    private IllegalArgumentException conversionError(Object param, Class<?> expectedType) {
        final String msg = MessageFormat.format(
            "Parameter of type {0} cannot be converted to expected type {1}",
            param.getClass().getName(), expectedType.getSimpleName());
        log.error(msg);
        throw new IllegalArgumentException(msg);
    }


    private Object convertArgument(Object param, Class<?> requiredType) {
        if (param == null) {
            return null;
        }

        Class<?> ptype = param.getClass();
        if (requiredType.equals(ptype) || requiredType.isAssignableFrom(ptype)) {
            // found the correct type
            return param;
        }

        // Expand simple example to handle array types as well
        if (requiredType.equals(String.class)) {
            return param.toString();
        } else if (requiredType.equals(Byte.class) || requiredType.equals(Byte.TYPE)) {
            if (param instanceof Number) {
                return ((Number) param).byteValue();
            } else {
                throw conversionError(param, Byte.class);
            }
        } else if (requiredType.equals(Short.class) || requiredType.equals(Short.TYPE)) {
            if (param instanceof Number) {
                return ((Number) param).shortValue();
            } else {
                throw conversionError(param, Short.class);
            }
        } else if (requiredType.equals(Integer.class) || requiredType.equals(Integer.TYPE)) {
            if (param instanceof Number) {
                return ((Number) param).intValue();
            } else {
                throw conversionError(param, Integer.class);
            }
        } else if (requiredType.equals(Long.class) || requiredType.equals(Long.TYPE)) {
            if (param instanceof Number) {
                return ((Number) param).longValue();
            } else {
                throw conversionError(param, Long.class);
            }
        } else if (requiredType.equals(Double.class) || requiredType.equals(Double.TYPE)) {
            if (param instanceof Number) {
                return ((Number) param).doubleValue();
            } else {
                throw conversionError(param, Double.class);
            }
        } else if (requiredType.equals(Float.class) || requiredType.equals(Float.TYPE)) {
            if (param instanceof Number) {
                return ((Number) param).floatValue();
            } else {
                throw conversionError(param, Float.class);
            }
        } else if (requiredType.equals(Boolean.class) || requiredType.equals(Boolean.TYPE)) {
            if (param instanceof Boolean) {
                return param;
            } else if (param instanceof String) {
                return Boolean.valueOf((String) param);
            } else {
                throw conversionError(param, Boolean.class);
            }
        } else {
            // we have an object type which we should try to deserialize from json
            throw conversionError(param, requiredType);
        }
    }


    private String toJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = objectMapperTL.get();

        return mapper.writeValueAsString(obj);
    }


    /**
     * Attempts to find an appropriate {@link Method} for the given name and number and types of
     * parameters
     *
     * @param pluginClass
     * @param methodName
     * @param parameters
     * @return
     */
    private Method findMethod(Class<? extends Plugin> pluginClass, String methodName,
        Object[] parameters) {
        if (parameters == null) {
            parameters = ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        Method[] methods = pluginClass.getMethods();
        List<Method> candidates = new ArrayList<Method>(parameters.length);
        for (Method m : methods) {
            if (!m.getName().equals(methodName)
                || m.getParameterTypes().length != parameters.length) {
                continue;
            }
            candidates.add(m);
        }

        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        return null;
    }


    public String getNodeName() {
        return nodeName;
    }


    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}
