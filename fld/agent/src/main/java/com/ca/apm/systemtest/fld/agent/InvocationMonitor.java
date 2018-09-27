/**
 * 
 */
package com.ca.apm.systemtest.fld.agent;

import java.util.HashSet;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.proxy.InvocationStallListener;

/**
 * @author KEYJA01
 *
 */
@Component
public class InvocationMonitor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(InvocationMonitor.class);
    private JmsTemplate jmsTemplate;
    private Set<String> invocationIds = new HashSet<>();
    private boolean done = false;

    /**
     * 
     */
    @Autowired
    public InvocationMonitor(@Qualifier("connectionFactory") ConnectionFactory connFactory) {
        jmsTemplate = new JmsTemplate(connFactory);
        Thread th = new Thread(this);
        th.setDaemon(true);
        th.start();
    }
    
    
    @Override
    public void run() {
       while (!done) {
           HashSet<String> toSend = new HashSet<>();
           synchronized (invocationIds) {
               toSend.addAll(invocationIds);
           }
           for (String id: toSend) {
               try {
                   sendPing(id);
               } catch (Exception e) {
                   log.warn("Unable to send ping from InvocationMonitor: " + e.getMessage());
                   log.debug("Exception while sending ping", e);
               }
           }
           try {
               Thread.sleep(10000L);
           } catch (Exception e) {
               // don't care
           }
       }
    }
    
    
    private void sendPing(final String id) {
        jmsTemplate.send(InvocationStallListener.AGENT_CONTROL, new MessageCreator() {
            
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(id);
            }
        });
    }


    public void registerInvocationId(String id) {
        synchronized (invocationIds) {
            invocationIds.add(id);
        }
    }
    
    
    public void unregisterInvocationId(String id) {
        synchronized (invocationIds) {
            invocationIds.remove(id);
        }
    }

}
