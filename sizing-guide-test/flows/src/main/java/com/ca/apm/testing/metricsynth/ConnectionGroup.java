/**
 * 
 */
package com.ca.apm.testing.metricsynth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author keyja01
 *
 */
public class ConnectionGroup {
    private List<ConnectionMetaData> connList = new ArrayList<>(); 
    private Set<ChainElement> chains = new TreeSet<>();
    static int nextId = 0;
    
    private class ConnectionMetaData {
        private AgentConnection conn;
        private String[] ejbs;
        private String[] webapps;
    }
    
    /**
     * Creates chains of agent connections:component for creating cross-process transaction traces.  The chains are added
     * to the {@link AgentConnection} which will be sending the TTs
     * @param numChains
     * @param depth
     */
    public void createTTChains(int numChains, int depth) {
        Random rand = new Random(1010101L);
        
        int max = connList.size();
        if (max == 0) {
            return;
        }
        for (int chainId = 0; chainId < numChains; chainId++) {
            int required = depth;
            ChainElement root = null;
            ChainElement current = null;
            while (required > 0) {
                int idx = rand.nextInt(max);
                ConnectionMetaData meta = connList.get(idx);
                if (root != null && root.contains(meta.conn)) {
                    continue;
                }
                ChainElement ce = new ChainElement();
                ce.conn = meta.conn;
                // TODO pick random from the app servers
                ce.frontEndWeb = meta.webapps[rand.nextInt(meta.webapps.length)];
                ArrayList<String> ejbList = new ArrayList<>();
                int numEjbs = meta.ejbs.length / 6;
                if (numEjbs > 0) {
                    for (int i = 0; i < numEjbs; i++) {
                        ejbList.add(meta.ejbs[rand.nextInt(meta.ejbs.length)]);
                    }
                }
                ce.calledEjbs = ejbList.toArray(new String[0]);
                if (root == null) {
                    root = ce;
                }
                if (current != null) {
                    current.next = ce;
                }
                current = ce;
                required--;
            }
            chains.add(root);
        }
    }
    
    public void addAgentConnection(AgentConnection conn, String[] ejbs, String[] webapps) {
        ConnectionMetaData meta = new ConnectionMetaData();
        meta.conn = conn;
        meta.ejbs = ejbs;
        meta.webapps = webapps;
        connList.add(meta);
    }
    
    public Set<ChainElement> getChains() {
        return new HashSet<>(chains);
    }
}
