package com.ca.apm.testing.metricsynth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wily.EDU.oswego.cs.dl.util.concurrent.Latch;
import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.introscope.spec.server.KIntroscopeConstants;
import com.wily.introscope.spec.server.beans.agent.IAgentBridgeService;
import com.wily.introscope.spec.server.beans.agent.IAgentManager;
import com.wily.introscope.spec.server.beans.agent.ICompressedTimesliceData;
import com.wily.introscope.spec.server.beans.agent.ISyncAgentControlChannel;
import com.wily.introscope.spec.server.beans.loadbalancing.ILoadBalancer;
import com.wily.introscope.spec.server.transactiontrace.KTransactionTraceConstants;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;
import com.wily.isengard.IsengardException;
import com.wily.isengard.api.IIsengardClient;
import com.wily.isengard.api.IsengardClient;
import com.wily.isengard.messageprimitives.ConnectionException;
import com.wily.isengard.messageprimitives.InvalidIsengardInterface;
import com.wily.isengard.messageprimitives.service.MessageService;
import com.wily.isengard.messageprimitives.service.MessageServiceFactory;
import com.wily.isengard.messageprimitives.service.MessageServiceInfo;
import com.wily.isengard.messageprimitives.service.ServiceException;
import com.wily.isengard.postoffice.Address;
import com.wily.isengard.postoffice.PostOffice;
import com.wily.util.exception.UnexpectedExceptionError;
import com.wily.util.feedback.IModuleFeedbackChannel;
import com.wily.util.feedback.SeverityLevel;
import com.wily.util.feedback.SystemOutFeedbackChannel;

public class AgentConnection implements Comparable<AgentConnection> {
    private String agentSpecifier = "";
    private IsengardClient isengardClient;
    private IModuleFeedbackChannel feedback;
    private IIsengardClient client;
    private boolean done = false;
    private boolean shouldDisconnect = false;
    // TODO implement load balancer support
    private ILoadBalancer loadBalancer;
    private AgentName agentName;
    private IAgentBridgeService bridgeService;
    private MetricFactory metricFactory;
    private int emPort;
    private String emHost;
    private String hostname;
    private final int instanceId = nextId++;
    private final List<TransactionComponentData> ttQueue = new ArrayList<>();
    
    private static int nextId = 0;
    private String processName;
    public class DefaultSyncAgentControlChannel  extends MessageService implements ISyncAgentControlChannel {

        /**
         * @param po
         */
        public DefaultSyncAgentControlChannel(PostOffice po) {
            super(po);
            
            try {
                initialize(ISyncAgentControlChannel.class, this);
            } catch (InvalidIsengardInterface e) {
                throw new UnexpectedExceptionError(e);
            }
        }

        /* (non-Javadoc)
         * @see com.wily.introscope.spec.server.beans.agent.ISyncAgentControlChannel#disconnect()
         */
        @Override
        public void disconnect() throws ConnectionException {
            feedback.info("EM requested agent disconnect");
            shouldDisconnect = true;
        }

        /* (non-Javadoc)
         * @see com.wily.introscope.spec.server.beans.agent.ISyncAgentControlChannel#ping()
         */
        @Override
        public void ping() throws ConnectionException {
            // ignored in this implementation
        }
    }
    

    public AgentConnection(String emHost, int emPort, String domain, String hostname, String processName, 
                           String agent, MetricFactory metricFactory) throws BadlyFormedNameException {
        this.agentSpecifier = hostname + "|" + processName + "|" + agent;
        this.emHost = emHost;
        this.emPort = emPort;
        this.hostname = hostname;
        this.processName = processName;
        feedback = new SystemOutFeedbackChannel("FakeAgent", SeverityLevel.INFO);
        this.metricFactory = metricFactory;
        client = new IIsengardClient() {
            
            @Override
            public void lostConnectionToIsengardServer() {
                System.out.println("Connection to isengard lost");
            }
            
            @Override
            public void connectedToIsengardServer() {
                System.out.println("Connection to isengard successful!");
            }
        };
        
        agentName = AgentName.getAgentName(domain, hostname, processName, agent);
    }
    
    private synchronized void pause(long ms) {
        try {
            wait(ms);
        } catch (Exception e) {
            // ignore
        }
    }
    
    public void start() {
        Thread t = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while (!done) {
                    try {
                        connect();
                        sendData();
                    } catch (Exception e) {
                        disconnect();
                        pause(5000L);
                    }
                }
            }
        });
        t.start();
    }
    
    public void shutdown() {
        done = true;
        try {
            isengardClient.close();
        } catch (Exception e) {
            
        }
    }
    
    
    /**
     * Set the profile to be used by the underlying {@link MetricFactory}
     * @param profileName
     */
    public void setActiveProfile(String profileName) {
        this.metricFactory.setActiveProfile(profileName);
    }
    
    
    private void sendData() throws ConnectionException {
        while (!done) {
            if (shouldDisconnect) {
                disconnect();
                shouldDisconnect = false;
                throw new ConnectionException();
            }
            
            long startTime = System.currentTimeMillis();

            TransactionComponentData[] tt = null;
            
            synchronized (ttQueue) {
                if (ttQueue.size() > 0) {
                    tt = ttQueue.toArray(new TransactionComponentData[ttQueue.size()]);
                    ttQueue.clear();
                }
            }
            
            if (tt != null) {
                try {
                    feedback.debug("About to send transaction traces: " + tt.length + " traces");
                    bridgeService.handleTransactions(tt);
                    feedback.debug("Sent " + tt.length + " transaction traces");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            
            try {
                feedback.debug("About to generate metric data");
                ICompressedTimesliceData data = metricFactory.generateCompressedMetricData();
                feedback.debug("Metric data generated");
                feedback.debug("Sending data to bridge service");
                bridgeService.recordTimesliceBindingList(data);
                feedback.debug("Sent data to bridge service");
                feedback.debug("Metric data sent");
            } catch (BadlyFormedNameException e) {
                e.printStackTrace();
            }
            
            long elapsed = System.currentTimeMillis() - startTime;
            long delay = 7500L - elapsed;
            if (delay < 0L) {
                delay = 7500L;
            }
            feedback.debug("send metric loop: delaying " + delay + " ms");
            pause(delay);
            
        }
    }

    private void connect() throws IsengardException, ServiceException, BadlyFormedNameException, IOException {
        AgentMetricData[] data = metricFactory.generateMetricData();
        
        isengardClient = new IsengardClient(feedback, emHost, emPort, KIntroscopeConstants.kAgentGroup, 
            KIntroscopeConstants.kAgentPassword, client);
        isengardClient.connect();
        
        IAgentManager agentManager = (IAgentManager) MessageServiceFactory.getService(isengardClient.getMainPO(), IAgentManager.class);
        
        Latch latch = new Latch();
        DefaultAsyncControlChannel dacc = new DefaultAsyncControlChannel(latch, isengardClient.getMainPO());
        Address addr = dacc.getAddress();
        DefaultSyncAgentControlChannel dsacc = new DefaultSyncAgentControlChannel(isengardClient.getMainPO());
        MessageServiceInfo msgSvc = dsacc.getServiceInfo();
        
        MessageServiceInfo bridgeInfo = agentManager.createNewAgentBridgeService(addr, msgSvc);
        bridgeService = (IAgentBridgeService) MessageServiceFactory.getService(isengardClient.getMainPO(), IAgentBridgeService.class, bridgeInfo);
        
        String ipAddress = "127.0.0.1";
        bridgeService.handleRegisterAgent(hostname, ipAddress, agentName.getProcess(), agentName.getAgentName(), false, false, "default", KTransactionTraceConstants.kAgentTransactionTraceFilterTypes);
        bridgeService.handleClientReadyToSendData();
        bridgeService.recordTimesliceBindingList(data);
        
        
//        loadBalancer = (ILoadBalancer) MessageServiceFactory.getService(isengardClient.getMainPO(), ILoadBalancer.class);
//        while (true) {
//            
//            loadBalancer.getCollectorToReconnectTo(arg0, arg1, arg2, arg3, arg4)
//        }
    }
    
    
    private void disconnect() {
        if (isengardClient != null) {
            isengardClient.close();
            isengardClient = null;
        }
            
    }
    
    @Override
    public String toString() {
        return "AgentConnection(" + hostname + "," + agentName + ")";
    }

    @Override
    public int hashCode() {
        return instanceId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AgentConnection)) {
            return false;
        }
        
        AgentConnection ac2 = (AgentConnection) obj;
        return instanceId == ac2.instanceId;
    }

    @Override
    public int compareTo(AgentConnection o) {
        return instanceId - o.instanceId;
    }

    public String getHostname() {
        return hostname;
    }
    
    public String getProcessName() {
        return processName;
    }

    public void queueTransactionTrace(TransactionComponentData tcd) {
        synchronized (ttQueue) {
            if (ttQueue.size() > 50) {
                return;
            }
            ttQueue.add(tcd);
        }
    }

    public String getAgentSpecifier() {
        return agentSpecifier;
    }
}
