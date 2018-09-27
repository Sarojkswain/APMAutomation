/**
 * 
 */
package com.ca.apm.testing.metricsynth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;

/**
 * @author keyja01
 *
 */
public class TransactionTraceScheduler {
    private final Map<AgentConnection, List<ChainElement>> agentChainMap = new HashMap<>();
    /** The rate at which transaction traces should be sent.  Defaults to 1 TT every 2 minutes */
    private double ttRate = 1.0 / 120.0;
    private double counter = 0;
    private boolean shouldStop = false;
    private Thread thread = null;
    
    private class SchedulerThread implements Runnable {
        private long lastCheck;
        
        @Override
        public void run() {
            lastCheck = System.currentTimeMillis();
            while (!shouldStop) {
                long now = System.currentTimeMillis();
                long elapsed = now - lastCheck;
                
                double elapsedSec = ((double) elapsed) / 1000.0;
                double dx = ttRate * elapsedSec;
                counter += dx;
                
                int ttToSend = (int) counter;
                if (ttToSend > 0) {
                    counter -= ttToSend;
                    queueTTs(ttToSend);
                }
                
                lastCheck = now;
                pause(1000L);
            }
        }
        
        private void pause(long ms) {
            synchronized (this) {
                try {
                    wait(ms);
                } catch (InterruptedException e) {
                }
            }
        }
        
    }
    
    
    private void queueTTs(int ttToSend) {
        Random r = new Random();
        synchronized (agentChainMap) {
            for (Entry<AgentConnection, List<ChainElement>> entry: agentChainMap.entrySet()) {
                List<ChainElement> list = entry.getValue();
                for (int i = 0; i < ttToSend; i++) {
                    int idx = r.nextInt(list.size());
                    ChainElement chain = list.get(idx);
                    Map<AgentConnection, List<TransactionComponentData>> ttMap = new HashMap<>();
                    chain.generateTTs(ttMap);
                    for (Entry<AgentConnection, List<TransactionComponentData>> e2: ttMap.entrySet()) {
                        AgentConnection conn = e2.getKey();
                        for (TransactionComponentData tcd: e2.getValue()) {
                            conn.queueTransactionTrace(tcd);
                        }
                    }
                }
            }
        }
    }
    
    
    public void start() {
        thread = new Thread(new SchedulerThread());
        thread.setDaemon(true);
        thread.start();
    }
    
    
    public void addAgentChain(AgentConnection conn, ChainElement chain) {
        synchronized (agentChainMap) {
            List<ChainElement> list = agentChainMap.get(conn);
            if (list == null) {
                list = new ArrayList<>();
                agentChainMap.put(conn, list);
            }
            list.add(chain);
        }
    }
    
    
    /**
     * Sets the rate at which transaction traces should be created.  Calculates the rate by
     * dividing number of traces by the period
     * @param numTraces Number of traces that should be 
     * @param period Period for generating traces in seconds
     */
    public void setTransactionTraceRate(long numTraces, long period) {
        ttRate = (double) numTraces / (double) period;
    }
}
