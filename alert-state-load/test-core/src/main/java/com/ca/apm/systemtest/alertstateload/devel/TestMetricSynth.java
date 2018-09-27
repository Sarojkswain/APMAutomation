package com.ca.apm.systemtest.alertstateload.devel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ca.apm.systemtest.alertstateload.util.ASLMetricFactory;
import com.ca.apm.testing.metricsynth.AgentConnection;
import com.ca.apm.testing.metricsynth.ConnectionGroup;
import com.ca.apm.testing.metricsynth.ChainElement;
import com.ca.apm.testing.metricsynth.MetricFactory;
import com.ca.apm.testing.metricsynth.TransactionTraceScheduler;

public class TestMetricSynth {

    private static final int NUMBER_OF_CONNECTION_GROUPS = 1 /* 23 */;
    private static final int NUMBER_OF_HOSTS = 1 /* 20 */;
    private static final int NUMBER_OF_AGENTS = 1 /* 10 */;

    public static void main(String[] args) throws Exception {
//        (new TestMetricSynth()).test2();
        test2();
    }

    @SuppressWarnings("unused")
    private static void test1() throws Exception {
        String domain = "SuperDomain";
        String processName = "FakeAgent3";
        String agent = "tomcat01";
        String emHost = "tas-czfld-n84";
        int emPort = 5001;
        String agentHostname = "superhost";
        String webappBaseName = "tesstest";

        ConnectionGroup connectionGroup = new ASLConnectionGroup();

        MetricFactory metricFactory = new ASLMetricFactory(domain, agentHostname, processName, agent, webappBaseName, 1, 0, 200);
        AgentConnection agentConnection = new AgentConnection(emHost, emPort, domain, agentHostname, processName, agent, metricFactory);

        agentConnection.start();
        connectionGroup.addAgentConnection(agentConnection, null, new String[]{ webappBaseName /*+ "0"*/ });
        pause(1500L);

        TransactionTraceScheduler ttScheduler = new TransactionTraceScheduler();
        connectionGroup.createTTChains(1, 1);
        for (ChainElement chain : connectionGroup.getChains()) {
            while (chain != null) {
                ttScheduler.addAgentChain(chain.getAgentConnection(), chain);
                chain = chain.next();
            }
        }
        ttScheduler.start();

        System.out.println("TestMetricSynth.test1():: before sleep");
        Thread.sleep(600000L);
        agentConnection.shutdown();
        System.out.println("TestMetricSynth.test1():: XXXXXXXXXX exit");
    }

    private static void test2() throws Exception {
        String domain = "SuperDomain";
        String processName = "FakeAgent";
//        String agent = "tomcat01";
        String emHost = "tas-czfld-n84";
        int emPort = 5001;
        String hostname = "superhost";
//        String webappBaseName = "tesstest";
        String agentFormat = "appsrv-%02d";

        ConnectionGroup[] connectionGroups = new ConnectionGroup[NUMBER_OF_CONNECTION_GROUPS];
        for (int i = 0; i < NUMBER_OF_CONNECTION_GROUPS; i++) {
            connectionGroups[i] = new ASLConnectionGroup();
        }

        List<AgentConnection> agentConnections = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            String agentHostname = String.format("%s-%02d", hostname, i);
            for (int j = 0; j < NUMBER_OF_AGENTS; j++) {
                String agent = String.format(agentFormat, j);
                String webappBaseName = "webapp" + i + "_" + j + "_";
                String ejbBaseName = "baseejb" + i + "_" + j + "_";

                MetricFactory metricFactory = new ASLMetricFactory(domain, agentHostname, processName, agent, webappBaseName, 1, 0, 200);
                AgentConnection agentConnection = new AgentConnection(emHost, emPort, domain, agentHostname, processName, agent, metricFactory);

                agentConnection.start();
                agentConnections.add(agentConnection);

                int index = agentConnections.size() % NUMBER_OF_CONNECTION_GROUPS;
                connectionGroups[index].addAgentConnection(agentConnection, generateCandidateList(ejbBaseName, 0), generateCandidateList(webappBaseName, 1));
                pause(1500L);
            }
        }

        TransactionTraceScheduler ttScheduler = new TransactionTraceScheduler();
        for (ConnectionGroup connectionGroup : connectionGroups) {
            connectionGroup.createTTChains(1, 1);
            for (ChainElement chain : connectionGroup.getChains()) {
                while (chain != null) {
                    ttScheduler.addAgentChain(chain.getAgentConnection(), chain);
                    chain = chain.next();
                }
            }
        }
        ttScheduler.start();

        System.out.println("TestMetricSynth.test2():: XXXXXXXXXX [" + Thread.currentThread().getId() + "] - before sleep");
        Thread.sleep(600000L);
        System.out.println("TestMetricSynth.test2():: XXXXXXXXXX [" + Thread.currentThread().getId() + "] - after sleep");
        for (AgentConnection agentConnection : agentConnections) {
            agentConnection.shutdown();
        }
        System.out.println("TestMetricSynth.test2():: XXXXXXXXXX exit");
    }

//    private synchronized void pause(long ms) {
//        try {
//            System.out.println("TestMetricSynth.pause():: XXXXXXXXXX " + Thread.currentThread().getId() + " -       waiting for " + ms + " ms");
//            wait(ms);
//            System.out.println("TestMetricSynth.pause():: XXXXXXXXXX " + Thread.currentThread().getId() + " - after waiting for " + ms + " ms");
//        } catch (Exception e) {
//            // ignore
//        }
//    }
    private static void pause(long ms) {
        try {
            System.out.println("TestMetricSynth.pause():: XXXXXXXXXX [" + Thread.currentThread().getId() + "] -     sleep for " + ms + " ms");
            Thread.sleep(ms);
            System.out.println("TestMetricSynth.pause():: XXXXXXXXXX [" + Thread.currentThread().getId() + "] - wake up after " + ms + " ms");
        } catch (InterruptedException e) {}
    }

    private static String[] generateCandidateList(String base, int num) {
        String[] arr = new String[num];
        for (int i = 0; i < num; i++) {
            arr[i] = base + i;
        }
        System.out.println("TestMetricSynth.generateCandidateList():: XXXXXXXXXX " + Arrays.toString(arr));
        return arr;
    }

    private static class ASLConnectionGroup extends ConnectionGroup {
        @Override
        public void addAgentConnection(AgentConnection agentConnection, String[] ejbs, String[] webapps) {
            super.addAgentConnection(agentConnection, ejbs == null ? new String[0] : ejbs, webapps == null ? new String[0] : webapps);
        }
    }

}
