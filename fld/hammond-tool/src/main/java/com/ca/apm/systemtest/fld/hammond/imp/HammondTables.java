/*
 * Copyright (c) 2017 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.systemtest.fld.hammond.imp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;
import com.wily.introscope.spec.server.transactiontrace.TransactionTraceData;
import org.rocksdb.RocksIterator;

public class HammondTables {

    public static final int TABLE_SLICE_DATA = 0;
    public static final int TABLE_AGENT_NAME_TO_ID = 1;
    public static final int TABLE_ID_TO_AGENT_NAME = 2;
    public static final int TABLE_AGENT_METRIC_TO_ID = 3;
    public static final int TABLE_ID_TO_AGENT_METRIC = 4;
    public static final int TABLE_TRACE_DATA = 5;

    private AgentNameTables agentNameTables;
    private AgentMetricTables agentMetricTables;
    private SliceDataTable sliceDataTable;
    private TraceDataTable traceDataTable;

    private RocksDB db;

    public HammondTables(RocksDB db) {
        this.db = db;
        ByteBufferCache bc = new ByteBufferCache();

        agentNameTables = new AgentNameTables(bc, db);
        agentMetricTables = new AgentMetricTables(bc, db);
        sliceDataTable = new SliceDataTable(bc, db);
        traceDataTable = new TraceDataTable(bc, db);
    }

    public int getAgentId(AgentName agentName) throws RocksDBException {
        return agentNameTables.getAgentId(agentName);
    }

    public  void store(AgentName agentName, long time, List<SliceDataValues.SliceDataValue> value) throws RocksDBException {
        int agentId = agentNameTables.getAgentId(agentName);
        sliceDataTable.put(new SliceDataKey(time, agentId), new SliceDataValues(value));
    }

    public  int getAttributeId(AgentMetric attribute) throws RocksDBException {
        return agentMetricTables.getAttributeId(attribute);
    }

    public  void storeTrace(AgentName agent, long timestamp, TransactionTraceData trace) throws RocksDBException {
        int agentId = agentNameTables.getAgentId(agent);
        Integer id = traceDataTable.getMaxId(agentId, timestamp);
        if (id == null) {
            id = 0;
        }
        id++;
        traceDataTable.put(new TraceDataKey(timestamp, agentId, id), trace);
    }

    public  List<TransactionComponentData> getTraceSlice(long hammondTime, AgentName origAgentName) throws RocksDBException {
        final List<TransactionComponentData> result = new ArrayList<>();
        int agentId = agentNameTables.getAgentId(origAgentName);
        traceDataTable.iterate(traceDataTable.createTracePrefixKey(hammondTime, agentId),
            new ObjectTable.Callback<TraceDataKey, TransactionTraceData>() {
                @Override
                public boolean row(TraceDataKey key, TransactionTraceData value) {
                    result.add(value.getRootComponent());
                    return true;
                }});
        
        return result;
    }

    public  SliceDataValues getMetricSlice(long hammondTime, AgentName origAgentName) throws RocksDBException {
        SliceDataValues result;

        int id = agentNameTables.getAgentId(origAgentName);
        result = sliceDataTable.get(new SliceDataKey(hammondTime, id));

        if (result == null) {
            result = new SliceDataValues(Collections.EMPTY_LIST);
        }

        return result;
    }

    public  AgentMetric getAttributeById(int attribute) throws RocksDBException {
        return agentMetricTables.getAttributeById(attribute);
    }

    public  List<AgentName> getAgents() {
        return agentNameTables.getAgents();
    }

    public  Long getRangeFrom(int tableId) {
        try {
            RocksIterator it = db.newIterator();
            it.seek(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(tableId).array());
            return ByteBuffer.wrap(it.key()).getLong(4);
        } catch (Exception e) {
            return null;
        }

    }

    public  Long getRangeTo(int tableId) {
        try {
            RocksIterator it = db.newIterator();
            byte[] b = TraceDataTable.getLastKeyOrValueInPrefix(db, ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(tableId).array(), true);
            return ByteBuffer.wrap(b).getLong(4);
        } catch (Exception e) {
            return null;
        }
    }
}
