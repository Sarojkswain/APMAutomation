package com.ca.apm.systemtest.fld.logmonitor;


import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import com.ca.apm.systemtest.fld.common.LoggerMonitorUtils;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;
import com.ca.apm.systemtest.fld.common.logmonitor.LoggerMessage;
import com.ca.apm.systemtest.fld.monitor.LoggerMessageSender;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;

public class LoggerMonitorTest {
    private static final String LOG_MONITOR_QUEUE = "LogMonitorQueue";
    private static final String BROKER_URL = "vm://localhost";
    private LoggerMessageSender loggerMessageSender;
    private BrokerService broker;
    private ActiveMQConnectionFactory activeMQConnectionFactory;
    private ActiveMQQueue queue;
    
   
    @Before
    public void setUp() throws Exception {
        broker = new BrokerService();
        broker.setBrokerName("localhost");
        broker.addConnector(BROKER_URL);
        broker.setPersistenceAdapter(new MemoryPersistenceAdapter());
        broker.setPersistent(false);
        broker.start();
        
        activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        
        queue = new ActiveMQQueue(LOG_MONITOR_QUEUE);
        
        DashboardIdStore.setDashboardId(new Long(1));
        
        loggerMessageSender = new LoggerMessageSender();
        JmsTemplate jmsTemplate = new JmsTemplate(activeMQConnectionFactory);
        loggerMessageSender.setJmsTemplate(jmsTemplate);
        loggerMessageSender.setQueue(queue);
        loggerMessageSender.setNodeName("testNotebook");
        
    }
    
    
    
    @After
    public void tearDown() throws Exception {
        broker.stop();
        broker = null;
        DashboardIdStore.clearDashboardId();
    }
    
    
    @Test
    public void test() throws Exception {
        //ctx = new ClassPathXmlApplicationContext("fldagent-test-context.xml");
        LoggerMessage loggerMessage;
        
        FldLevel fldLevel = FldLevel.INFO;
        String category = "cat1"; 
        String tag = "tag1";
        String message = "message1";
        String exception = "exception";

        loggerMessage = new LoggerMessage();
        loggerMessage.setFldLevel(fldLevel);
        loggerMessage.setCategory(category);
        loggerMessage.setTag(tag);
        loggerMessage.setMessage(message);
        loggerMessage.setExcept(exception);
        
        loggerMessageSender.sendLogMessage(loggerMessage);
        
        Connection conn = null;
        Session session = null;
        MessageConsumer consumer = null;
        try {
            conn = activeMQConnectionFactory.createConnection();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(queue);
            conn.start();
            Message msg = consumer.receive(100L);
            
            Assert.assertTrue(msg instanceof TextMessage);
            TextMessage tmsg = (TextMessage) msg;
            String json = tmsg.getText();
            LoggerMessage lm2 = LoggerMonitorUtils.convertJSONtoLog(json);
            Assert.assertEquals(loggerMessage, lm2);
        } finally {
            if (session != null) {
                session.close();
            }
            if (conn != null) {
                conn.stop();
                conn.close();
            }
        }
    }
   
}
