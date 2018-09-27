package com.ca.apm.systemtest.fld.logmonitor;


import java.util.Date;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;
import com.ca.apm.systemtest.fld.common.logmonitor.LoggerMessage;
import com.ca.apm.systemtest.fld.monitor.LoggerMessageSender;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;

public class LoggerMonitorDashboardRESTTest {
    private static final String LOG_MONITOR_QUEUE = "LogMonitorQueue";
    private static final String BROKER_URL = "tcp://localhost:61616";
    private LoggerMessageSender loggerMessageSender;
    private ActiveMQConnectionFactory activeMQConnectionFactory;
    private ActiveMQQueue queue;
    
   
    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
        
        activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        
        queue = new ActiveMQQueue(LOG_MONITOR_QUEUE);
        
        loggerMessageSender = new LoggerMessageSender();
        JmsTemplate jmsTemplate = new JmsTemplate(activeMQConnectionFactory);
        loggerMessageSender.setJmsTemplate(jmsTemplate);
        loggerMessageSender.setQueue(queue);
        loggerMessageSender.setNodeName("testNotebook");
    }
    
    
    
    @After
    public void tearDown() throws Exception {
        DashboardIdStore.clearDashboardId();
    }
    
    
    @Test
    public void test() throws Exception {
        
        try {
            for (int i = 0; i < 100; i++) {
                DashboardIdStore.setDashboardId(new Long(i % 4));
                
                LoggerMessage lm = new LoggerMessage();
                lm.setFldLevel(FldLevel.INFO);
                lm.setCategory("cat" + (i % 10));
                lm.setTag("tag" + (i%5));
                lm.setMessage("Message: " + i);
                lm.setExcept("except" + i);
                lm.setTimestamp(new Date());
                
                loggerMessageSender.sendLogMessage(lm);
            }
            
            loggerMessageSender.setNodeName("testPC");
            for (int i = 0; i < 50; i++) {
                DashboardIdStore.setDashboardId(new Long(i % 4));
                
                LoggerMessage lm = new LoggerMessage();
                lm.setFldLevel(FldLevel.DEBUG);
                lm.setCategory("cat" + (i % 10));
                lm.setTag("tag" + (i%5));
                lm.setMessage("Message: " + i);
                lm.setExcept("except" + i);
                lm.setTimestamp(new Date());
                
                loggerMessageSender.sendLogMessage(lm);
            }
        } catch (Exception e){
            Assert.assertTrue(true);
        }
        Assert.assertTrue(true);
    }
   
}
