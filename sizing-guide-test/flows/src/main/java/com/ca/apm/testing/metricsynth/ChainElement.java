package com.ca.apm.testing.metricsynth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;

public class ChainElement implements Comparable<ChainElement> {
    AgentConnection conn;
    String frontEndWeb;
    String[] calledEjbs;
    ChainElement next;
    private final int id = ConnectionGroup.nextId++;
    
    @Override
    public int compareTo(ChainElement o) {
        if (o == null) {
            return 1;
        }
        return id - o.id;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ChainElement)) {
            return false;
        }
        ChainElement o = (ChainElement) obj;
        return id == o.id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }

    public boolean contains(AgentConnection conn) {
        if (this.next == null) {
            return this.conn.equals(conn);
        }
        return this.conn.equals(conn) || next.contains(conn);
    }
    
    public void generateTTs(Map<AgentConnection, List<TransactionComponentData>> ttMap) {
        String corrId = TTUtil.correlationId();
        generateTTInternal(this, System.currentTimeMillis(), corrId, ttMap, null);
    }
    
    public AgentConnection getAgentConnection() {
        return conn;
    }
    
    public ChainElement next() {
        return next;
    }
    
    private long generateTTInternal(ChainElement ce, long startTime, String corrId, Map<AgentConnection, List<TransactionComponentData>> ttMap, String callerTxnTraceId) {
        List<TransactionComponentData> list = new ArrayList<>();
        ttMap.put(ce.conn, list);
        
        String txnTraceId = TTUtil.correlationId();
        
        long duration = 100L;
        
        String nextFrontEnd = null;
        Integer nextFrontEndPort = null;
        String backendWebAppName = null;
        
        if (ce.next != null) {
            long subDuration = generateTTInternal(ce.next, startTime + 3L, corrId, ttMap, txnTraceId);
            duration = duration + 3L + subDuration;
            nextFrontEnd = ce.next.conn.getHostname();
            nextFrontEndPort = 8080;
            backendWebAppName = ce.next.frontEndWeb;
        }
        System.out.println("For agent connection: " + ce.getAgentConnection());
        
        TransactionComponentData tcd = TTUtil.frontEndTT(ce.frontEndWeb, startTime, duration, corrId, txnTraceId, callerTxnTraceId, ce.conn.getHostname(), nextFrontEnd, nextFrontEndPort, backendWebAppName);
        list.add(tcd);
        
        return duration;
    }
}