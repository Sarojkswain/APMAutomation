/**
 * 
 */
package com.ca.apm.systemtest.fld.server;

import java.text.MessageFormat;
import java.util.Date;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.agentdownload.AgentDownloadPlugin;
import com.ca.apm.systemtest.fld.plugin.vo.HeartbeatRequest;
import com.ca.apm.systemtest.fld.plugin.vo.HeartbeatResponse;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;
import com.ca.apm.systemtest.fld.server.dao.AgentDistributionDao;
import com.ca.apm.systemtest.fld.server.dao.NodeDao;
import com.ca.apm.systemtest.fld.server.model.Node;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Node manager implementation.
 * 
 * @author keyja01
 *
 */
public class NodeManagerImpl implements InitializingBean, NodeManager, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(NodeManagerImpl.class);

    @Autowired
    private AgentDistributionDao agentDistroDao;

    @Autowired
    AgentProxyFactory agentProxyFactory;

    private NodeDao nodeDao;

    private ConnectionFactory connFactory;
    private Destination sendDestination;
    private JmsTemplate jmsTemplate;
    @SuppressWarnings("unused")
    private AgentProxyFactory proxyFactory;
    private FldLogger logger;

    // internal objects used to synchronize and shutdown the thread
    private boolean done = false;
    private Object lock = new Object();


    private ThreadLocal<ObjectMapper> objectMapperTL = new ThreadLocal<ObjectMapper>() {
        protected ObjectMapper initialValue() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return (mapper);
        }
    };

    public NodeManagerImpl() {
        Runnable discoveryThread = discoveryThread();
        Thread th = new Thread(discoveryThread);
        th.setDaemon(true);
        th.start();
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public boolean checkNodeAvailable(String nodeName) {
        Node node = nodeDao.findByNodeName(nodeName);
        if (node == null) {
            if (logger != null) {
                logger.info("NodeManager", "Check node", "Node " + nodeName + " not found");
            }
            return false;
        }

        Long lastHeartbeat = node.getLastHeartbeat();
        if (lastHeartbeat == null) {
            if (logger != null) {
                logger.info("NodeManager", "Check node", "No heartbeat found for node " + nodeName);
            }
            return false;
        }

        // if more than 5 minutes have passed since heartbeat
        long now = System.currentTimeMillis();
        long elapsed = now - lastHeartbeat;
        if (elapsed > 300000L) {
            if (logger != null) {
                logger.info("NodeManager", "Check node",
                    "Last heartbeat over 5 minutes old for node " + nodeName);
            }
            return false;
        }

        return true;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void receiveHeartbeat(String heartbeatJson) throws Exception {
        ObjectMapper mapper = objectMapperTL.get();
        HeartbeatResponse response = null;
        synchronized (mapper) {
            response = mapper.readValue(heartbeatJson, HeartbeatResponse.class);
        }
        
        boolean createNode = false;
        Node node = nodeDao.findByNodeName(response.getNodeName());
        if (node == null) {
            node = new Node();
            node.setName(response.getNodeName());
            createNode = true;
        }
        node.setLastHeartbeatRequest(response.getHeartbeatRequestTimestamp());
        node.setLastHeartbeat(System.currentTimeMillis());
        node.setVersion(response.getVersion());
        node.setHostName(response.getHostName());
        node.setIp4(response.getIp4());
        node.setNtpTimeOffset(response.getNtpTimeOffset());

        node.setIsAgentUpdating(false);

        Long latestAgentVersion = agentDistroDao.getLatestVersion();
        if (latestAgentVersion != null) {
            if (node.getVersion() == null || latestAgentVersion.intValue() > node.getVersion()) {
                AgentProxy nodeProxy = agentProxyFactory.createProxy(node.getName());
                try {
                    nodeProxy.getPlugin(AgentDownloadPlugin.class).downloadNewVersion();
                    log.info("Node {} is using old version {} (was notified about new version)",
                        node.getName(), node.getVersion());
                    node.setIsAgentUpdating(true);
                } catch (Exception e) {
                    log.warn("Unable to notify " + node.getName() + " of new version", e);
                }
            } 
        }
        
        if (createNode) {
            nodeDao.create(node);
        } else {
            nodeDao.update(node);
        }

        System.out.println("Set heartbeat for " + node.getName() + " (hostname = "
            + node.getHostName() + ", IP4 = " + node.getIp4() + ") to "
            + new Date(node.getLastHeartbeat()) + " (ver:" + node.getVersion() + "),"
            + " NTP time offset = " + node.getNtpTimeOffset() + " ms");

    }

    /**
     * This runnable periodically sends out a hearbeat request, requesting that all active nodes
     * respond with
     * a status message.
     * 
     * @return
     */
    private Runnable discoveryThread() {
        Runnable r = new Runnable() {
            public void run() {
                shortWait(10000L);
                while (!done) {
                    // send heartbeat request
                    final HeartbeatRequest heartbeat =
                        new HeartbeatRequest(System.currentTimeMillis());
                    ObjectMapper mapper = objectMapperTL.get();
                    try {
                        final String json;
                        synchronized (mapper) {
                            json = mapper.writeValueAsString(heartbeat);
                        }
                        jmsTemplate.send(sendDestination, new MessageCreator() {
                            public Message createMessage(Session session) throws JMSException {
                                TextMessage msg = session.createTextMessage(json);
                                return (msg);
                            }
                        });
                    } catch (JsonProcessingException e1) {
                        final String msg =
                            MessageFormat.format("JSON processing failure. Exception: {0}",
                                e1.getMessage());
                        log.error(msg, e1);
                    }
                    // TODO consider automatically removing old nodes from the server's list
                    shortWait(60000L);
                }
            }

            private void shortWait(long ms) {
                try {
                    synchronized (lock) {
                        lock.wait(ms);
                    }
                } catch (Exception e) {
                    final String msg =
                        MessageFormat.format("wait() failure. Exception: {0}", e.getMessage());
                    log.error(msg, e);
                }
            }
        };
        return (r);
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    @Autowired(required = true)
    public void setConnectionFactory(ConnectionFactory connFactory) {
        this.connFactory = connFactory;
        jmsTemplate = new JmsTemplate(connFactory);
        jmsTemplate.setDefaultDestinationName("fld.admin");
    }

    @Autowired
    public void setAgentProxyFactory(AgentProxyFactory fact) {
        this.proxyFactory = fact;
    }

    public void afterPropertiesSet() throws Exception {
        if (connFactory == null) {
            throw new IllegalArgumentException(
                "You must set a connection factory for the NodeManagerImpl");
        }
    }

    public void setSendDestination(Destination sendDestination) {
        this.sendDestination = sendDestination;
    }


    public FldLogger getLogger() {
        return logger;
    }


    @Autowired(required = false)
    public void setLogger(FldLogger logger) {
        this.logger = logger;
    }


    @Override
    public void destroy() throws Exception {
        try {
            done = true;
            synchronized (lock) {
                lock.notifyAll();
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
