/**
 *
 */
package com.ca.apm.systemtest.fld.proxy;

import javax.jms.TextMessage;

import java.util.Map;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author KEYJA01
 */
public class InvocationStallListenerTest {
    private static final Logger log = LoggerFactory.getLogger(InvocationStallListenerTest.class);

    private BrokerService broker;
    private ActiveMQConnectionFactory connFactory;
    private InvocationStallListenerImpl listener;
    

    @Before
    public void setup() throws Exception {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.setPersistenceAdapter(new MemoryPersistenceAdapter());
        broker.addConnector("tcp://localhost:0");
        broker.start();
        Map<String, String> transConnectors = broker.getTransportConnectorURIsAsMap();
        assertFalse(transConnectors.isEmpty());
        log.info("transport connectors: {}", transConnectors);
        String brokerUrl = null;
        assertFalse(isEmpty(brokerUrl = transConnectors.values().iterator().next()));
        connFactory = new ActiveMQConnectionFactory(brokerUrl);
        listener = new InvocationStallListenerImpl(connFactory, 100L);
    }


    @After
    public void teardown() throws Exception {
        listener.shutdown();
        Thread.sleep(1000L);

        try {
            broker.stop();
        } catch (Exception e) {
            // do nothing
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testInvocationStallListener() throws Exception {
        String id1 = listener.listenForStalls("invocation-1", 100L);
        log.info("invocation-1 ID: {}", id1);
        String id2 = listener.listenForStalls("invocation-2", 250L);
        log.info("invocation-1 ID: {}", id2);
        String id3 = listener.listenForStalls("invocation-3", 120000L);
        log.info("invocation-1 ID: {}", id3);

        JmsTemplate jmsTemplate = new JmsTemplate(connFactory);
        jmsTemplate.setReceiveTimeout(5000L);
        // remove first stall
        TextMessage msg = (TextMessage) jmsTemplate
            .receive(AgentPluginProxyFactory.AGENT_RESPONSE_QUEUE);
        assertNotNull(msg);
        log.info("testInvocationStallListener msg: >{}<", msg.getText());
        assertTrue(msg.getText().contains("ERR_INVOCATION_STALLED"));
        assertTrue(msg.getText().contains("invocation-1"));

        // remove second stall
        msg = (TextMessage) jmsTemplate.receive(AgentPluginProxyFactory.AGENT_RESPONSE_QUEUE);
        assertNotNull(msg);
        log.info("testInvocationStallListener msg 2: >{}<", msg.getText());
        assertTrue(msg.getText().contains("ERR_INVOCATION_STALLED"));
        assertTrue(msg.getText().contains("invocation-2"));

        listener.unregister(id1);
        listener.unregister(id2);

        // we should only have one registration left
        long count = listener.registeredCount();
        assertEquals(1, count);

    }
}
