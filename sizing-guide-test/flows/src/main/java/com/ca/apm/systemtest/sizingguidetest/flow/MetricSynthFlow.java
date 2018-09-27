package com.ca.apm.systemtest.sizingguidetest.flow;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.sizingguidetest.util.SGTConnectionGroup;
import com.ca.apm.systemtest.sizingguidetest.util.SGTMetricFactory;
import com.ca.apm.testing.metricsynth.AgentConnection;
import com.ca.apm.testing.metricsynth.ConnectionGroup;
import com.ca.apm.testing.metricsynth.ChainElement;
import com.ca.apm.testing.metricsynth.MetricFactory;
import com.ca.apm.testing.metricsynth.TransactionTraceScheduler;
import com.ca.apm.testing.synth.defs.WebFrontend;

@Flow
public class MetricSynthFlow extends FlowBase implements IAutomationFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricSynthFlow.class);

    @FlowContext
    private MetricSynthFlowContext ctx;

    private String emHost;

    private int numberOfConnectionGroups;
    private int numberOfHosts;
    private int numberOfAgents;
    private Long duration;

    private String domain;
    private String processName;
    private int emPort;
    private String hostname;
    private String agentFormat;

    private String webappBaseNamePrefix;
    private String ejbBaseNamePrefix;

    private String agentHostnameFormat;

    private int minValueAverageResponseTime;
    private int maxValueAverageResponseTime;

    private int numWars;
    private int numEjbs;

    private int createTTChainsNumChains;
    private int createTTChainsDepth;

    @Override
    public void run() throws Exception {
        init();
        perform();
    }

    private void init() {
        init(this.ctx);
    }

    private void init(MetricSynthFlowContext ctx) {
        emHost = ctx.getEmHost();
        numberOfConnectionGroups = ctx.getNumberOfConnectionGroups();
        numberOfHosts = ctx.getNumberOfHosts();
        numberOfAgents = ctx.getNumberOfAgents();
        duration = ctx.getDuration();

        domain = ctx.getDomain();
        processName = ctx.getProcessName();
        emPort = ctx.getEmPort();
        hostname = ctx.getHostname();
        agentFormat = ctx.getAgentFormat();

        webappBaseNamePrefix = ctx.getWebappBaseName();
        ejbBaseNamePrefix = ctx.getEjbBaseName();

        agentHostnameFormat = ctx.getAgentHostnameFormat();

        minValueAverageResponseTime = ctx.getMinValueAverageResponseTime();
        maxValueAverageResponseTime = ctx.getMaxValueAverageResponseTime();

        numWars = ctx.getNumWars();
        numEjbs = ctx.getNumEjbs();

        createTTChainsNumChains = ctx.getCreateTTChainsNumChains();
        createTTChainsDepth = ctx.getCreateTTChainsDepth();
    }

    private void perform() throws Exception {
        try {
            ConnectionGroup[] connectionGroups = new ConnectionGroup[numberOfConnectionGroups];
            for (int i = 0; i < numberOfConnectionGroups; i++) {
                connectionGroups[i] = new SGTConnectionGroup();
            }

            List<AgentConnection> agentConnections = new ArrayList<>();
            for (int i = 0; i < numberOfHosts; i++) {
                String agentHostname = String.format(agentHostnameFormat, hostname, i);
                for (int j = 0; j < numberOfAgents; j++) {
                    String agent = String.format(agentFormat, j);
                    String webappBaseName = webappBaseNamePrefix + i + "_" + j + "_";
                    String ejbBaseName = ejbBaseNamePrefix + i + "_" + j + "_";
                    List<WebFrontend> webFrontends = new ArrayList<>();

                    MetricFactory metricFactory =
                        new SGTMetricFactory(domain, agentHostname, processName, agent,
                            webappBaseName, numWars, ejbBaseName, numEjbs, webFrontends,
                            minValueAverageResponseTime, maxValueAverageResponseTime);

                    AgentConnection agentConnection =
                        new AgentConnection(emHost, emPort, domain, agentHostname, processName,
                            agent, metricFactory);

                    agentConnection.start();
                    agentConnections.add(agentConnection);

                    int index = agentConnections.size() % numberOfConnectionGroups;
                    connectionGroups[index].addAgentConnection(agentConnection,
                        generateCandidateList(ejbBaseName, 0),
                        generateCandidateList(webappBaseName, 1));
                    // pause(1500L);
                }
            }

            TransactionTraceScheduler ttScheduler = new TransactionTraceScheduler();
            for (ConnectionGroup connectionGroup : connectionGroups) {
                connectionGroup.createTTChains(createTTChainsNumChains, createTTChainsDepth);
                for (ChainElement chain : connectionGroup.getChains()) {
                    while (chain != null) {
                        ttScheduler.addAgentChain(chain.getAgentConnection(), chain);
                        chain = chain.next();
                    }
                }
            }
            ttScheduler.start();

            if (duration != null && duration.longValue() > 0) {
                LOGGER.info("MetricSynthFlow.run():: now waiting for {} ms", duration);
                pause(duration);
                for (AgentConnection agentConnection : agentConnections) {
                    agentConnection.shutdown();
                }
            }
        } catch (Exception e) {
            LOGGER.error("MetricSynthFlow.run():: exception while running MetricSynth: " + e, e);
        } finally {
            LOGGER.info("MetricSynthFlow.run():: exit");
        }
    }

    private String[] generateCandidateList(String base, int num) {
        String[] arr = new String[num];
        for (int i = 0; i < num; i++) {
            arr[i] = base + i;
        }
        return arr;
    }

    private void pause(long duration) {
        try {
            synchronized (this) {
                LOGGER.info("MetricSynthFlow.sleep():: sleeping for {} ms", duration);
                wait(duration);
            }
        } catch (InterruptedException e) {}
    }

    public static void main(String[] args) throws Exception {
        MetricSynthFlow metricSynthFlow = new MetricSynthFlow();
        MetricSynthFlowContext ctx =
            (new MetricSynthFlowContext.Builder()).emHost("tas-czfld-n88")
                .numberOfConnectionGroups(1).numberOfHosts(1).numberOfAgents(1).duration(60000L)
                .minValueAverageResponseTime(200).maxValueAverageResponseTime(300).build();
        metricSynthFlow.init(ctx);
        metricSynthFlow.perform();
    }

}
