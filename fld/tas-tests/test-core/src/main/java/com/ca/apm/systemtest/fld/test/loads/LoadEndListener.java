/**
 * 
 */
package com.ca.apm.systemtest.fld.test.loads;

import java.util.Collections;
import java.util.Queue;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import com.ca.apm.systemtest.fld.flow.controller.LoadEvent;
import com.google.gson.Gson;

/**
 * @author keyja01
 *
 */
public class LoadEndListener {
    private static final Logger logger = LoggerFactory.getLogger(LoadEndListener.class);

    private JmsTemplate jt;

    /**
     * 
     */
    public LoadEndListener(JmsTemplate jt) {
        this.jt = jt;
        jt.setMessageConverter(gsonMessageConverter());
    }

    
    public static Thread startListenerThread(final Object lock, final JmsTemplate jt, 
                                             final Queue<LoadEvent> eventQueue, final String loadName) {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean done = false;
                LoadEndListener listener = new LoadEndListener(jt);
                while (!done) {
                    LoadEvent event = listener.waitForLoad(Collections.singleton(loadName));
                    synchronized (lock) {
                        eventQueue.add(event);
                        lock.notifyAll();
                    }
                }
            }
        });
        th.setDaemon(true);
        th.start();
        return th;
    }
    
    
    public LoadEvent waitForLoad(Set<String> loadNames) {
        boolean done = false;
        int wait = 30000;
        while (!done) {
            try {
                LoadEvent event = (LoadEvent) jt.receiveAndConvert();
                if (loadNames.contains(event.loadId)) {
                    logger.info("[" + event.loadId + "] Recieved event: " + event);
                    return event;
                }
            } catch (Exception e) {
                e.printStackTrace();
                shortWait(wait);
            }
        }
        return null;
        
    }
    
    
    private synchronized void shortWait(long ms) {
        try {
            wait(ms);
        } catch (Exception e) {
            // ignore
        }
    }


    private MessageConverter gsonMessageConverter() {
        return new MessageConverter() {
            
            @Override
            public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
                Gson gson = new Gson();
                String json = gson.toJson(object);
                return session.createTextMessage(json);
            }
            
            @Override
            public Object fromMessage(Message message) throws JMSException, MessageConversionException {
                if (!(message instanceof TextMessage)) {
                    throw new MessageConversionException("Unsupported message type: " + message.getJMSType());
                }
                TextMessage txtMessage = (TextMessage) message;
                Gson gson = new Gson();
                LoadEvent event = gson.fromJson(txtMessage.getText(), LoadEvent.class);
                
                return event;
            }
        };
    }
}
