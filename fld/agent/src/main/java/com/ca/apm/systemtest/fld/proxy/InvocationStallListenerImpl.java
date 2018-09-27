package com.ca.apm.systemtest.fld.proxy;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.vo.RemoteCallResult;

/**
 * @author KEYJA01
 *
 */
@Component
public class InvocationStallListenerImpl implements InvocationStallListener, Runnable {
    private static final Logger log = LoggerFactory.getLogger(InvocationStallListenerImpl.class);
    private long nextId = 0;
    private Map<String, Registration> registrationMap = new ConcurrentHashMap<>(10);
    private Map<String, String> invocationToRegMap = new ConcurrentHashMap<>(10);
    private JmsTemplate jmsTemplate;
    private JmsListenerThread listenerThread = null;
    
    private ThreadLocal<ObjectMapper> objectMapperTL = new ThreadLocal<ObjectMapper>() {
        protected ObjectMapper initialValue() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return mapper;
        }
    };
    
    private static class Registration {
        String invocationId;
        long stallMs = 120000L;
        long lastPing = 0L;
        boolean fired = false;
    }
    
    
    private class JmsListenerThread implements Runnable {
        boolean done = false;
        
        public void shutdown() {
            done = true;
        }
        
        @Override
        public void run() {
            while (!done) {
                boolean shouldWait = false;
                Message m = null;
                try {
                    m = jmsTemplate.receive(AGENT_CONTROL);
                    if (m != null && m instanceof TextMessage) {
                        TextMessage tm = (TextMessage) m;
                        String txt = tm.getText();
                        log.trace("Got ping {}", txt);
                        String regId = invocationToRegMap.get(txt);
                        Registration r = null;
                        if (regId != null) {
                            r = registrationMap.get(regId);
                        }
                        if (r != null) {
                            r.lastPing = System.currentTimeMillis();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    shouldWait = true;
                }
                if (shouldWait) {
                    try {
                        Thread.sleep(5000L);
                    } catch (Exception e) {
                        // don't care
                    }
                }
            }
        }
    }
    

    /**
     * Standard constructor to be used by spring
     * @param connFactory
     */
    @Autowired
    public InvocationStallListenerImpl(ConnectionFactory connFactory) {
        this(connFactory, 5000L);
    }
    
    
    /**
     * Constructor for testing
     * @param connFactory
     * @param receiveTimeout
     */
    public InvocationStallListenerImpl(ConnectionFactory connFactory, long receiveTimeout) {
        jmsTemplate = new JmsTemplate(connFactory);
        jmsTemplate.setReceiveTimeout(receiveTimeout);
        Thread th = new Thread(this);
        th.setDaemon(true);
        th.start();
        
        listenerThread = new JmsListenerThread();
        Thread th2 = new Thread(listenerThread);
        th2.setDaemon(true);
        th2.start();
    }
    
    
    
    protected void shutdown() {
        listenerThread.shutdown();
    }
    
    
    public void setReceiveTimeout(long ms) {
        jmsTemplate.setReceiveTimeout(ms);
    }
    

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.proxy.InvocationStallListener#listenForStalls(java.lang.String, long)
     */
    @Override
    public String listenForStalls(String invocationId, long stallMs) {
        String regId = "inv-" + nextId++;
        
        Registration r = new Registration();
        r.invocationId = invocationId;
        r.stallMs = stallMs;
        r.lastPing = System.currentTimeMillis();
        
        registrationMap.put(regId, r);
        invocationToRegMap.put(invocationId, regId);
        
        return regId;
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.proxy.InvocationStallListener#unregister(java.lang.String)
     */
    @Override
    public void unregister(String registrationId) {
        registrationMap.remove(registrationId);
    }
    
    public long registeredCount() {
        return registrationMap.size();
    }
    
    /* 
     * Main processing loop for invocation stall detection
     * 
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        boolean done = false;
        while (!done) {
            Set<String> toRemove = new HashSet<>();
            try {
                // XXX: This TreeMap wrapping here is there so that we get stable ordering of stall
                // response callbacks in tests.
                for (Entry<String, Registration> entry: new TreeMap<>(registrationMap).entrySet()) {
                    Registration r = entry.getValue();
                    long now = System.currentTimeMillis();
                    long elapsed = now - r.lastPing;
                    log.trace("Elapsed time since last ping: {}", elapsed);
                    // automatically remove registrations that have already been fired but
                    // not removed after 1 minute
                    if (r.fired && elapsed > 60000L) {
                        toRemove.add(entry.getKey());
                    }
                    if (!r.fired && elapsed > r.stallMs) {
                        log.info("Invocation is stalled: {} > {}", elapsed, r.stallMs);
                        sendStallResponse(r.invocationId, r.stallMs);
                        r.fired = true;
                    }
                }
            } catch (Exception e) {
                log.warn("Exception while checking for stalled invocations", e);
            }
            for (String key: toRemove) {
                try {
                    log.debug("Removing invocation for key {}", key);
                    registrationMap.remove(key);
                } catch (Exception e) {
                    log.warn("Exception while removing stalled invocations from map for key {}",
                        key, e);
                }
            }
            
            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
                // don't care
            }
        }
        log.error("Exiting invocation stall detector loop");
    }
    
    
    private void sendStallResponse(final String invocationId, long stallMs) {
        RemoteCallResult result = new RemoteCallResult();
        result.setSuccess(false);
        result.setErrorCode(ERR_INVOCATION_STALLED);
        result.setErrorMessage("Invocation " + invocationId + " stalled after " + stallMs + " ms without an active ping");
        ObjectMapper mapper = objectMapperTL.get();
        
        try {
            final String json;
            json = mapper.writeValueAsString(result);
            
            jmsTemplate.send(AgentPluginProxyFactory.AGENT_RESPONSE_QUEUE, new MessageCreator() {
                
                @Override
                public Message createMessage(Session session) throws JMSException {
                    TextMessage msg = session.createTextMessage(json);
                    msg.setStringProperty(RemoteCallResult.REMOTE_INVOCATION_ID_HEADER, invocationId);
                    return msg;
                }
            });
        } catch (Exception e) {
            log.warn("Unable to send stall response", e);
        }
    }
}
