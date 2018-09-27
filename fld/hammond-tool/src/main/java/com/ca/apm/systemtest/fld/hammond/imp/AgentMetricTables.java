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

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.wily.introscope.server.enterprise.entity.rocksdb.RocksDBUtils;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.BadlyFormedNameException;

public class AgentMetricTables {

    private static class AgentMetric2Id extends Object2IntegerTable<AgentMetric> {

        public AgentMetric2Id(ByteBufferCache bc, RocksDB db) {
            super(bc, db, HammondTables.TABLE_AGENT_METRIC_TO_ID, true);
        }

        @Override
        protected void writeKey(ByteBuffer buffer, AgentMetric key) {
            buffer.putInt(key.getAttributeType());
            String name = key.getAttributeURL();
            byte[] bytes = name.getBytes(RocksDBUtils.UTF8);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }
        
        @Override
        protected AgentMetric readKey(ByteBuffer buffer) throws RocksDBException {
            int type = buffer.getInt();
            byte[] buf = new byte[buffer.getInt()];
            buffer.get(buf);
            try {
                return AgentMetric.getAgentMetric(new String(buf, RocksDBUtils.UTF8), type);
            } catch (BadlyFormedNameException e) {
                throw new RocksDBException(e.getMessage());
            }
        }
        
    };

    private static class Id2AgentMetric extends Integer2ObjectTable<AgentMetric> {

        public Id2AgentMetric(ByteBufferCache bc, RocksDB db) {
            super(bc, db, HammondTables.TABLE_ID_TO_AGENT_METRIC, true);
        }

        @Override
        protected void writeValue(ByteBuffer buffer, AgentMetric value) {
            buffer.putInt(value.getAttributeType());
            String name = value.getAttributeURL();
            byte[] bytes = name.getBytes(RocksDBUtils.UTF8);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }

        @Override
        protected AgentMetric readValue(ByteBuffer buffer) throws RocksDBException {
            int type = buffer.getInt();
            byte[] buf = new byte[buffer.getInt()];
            buffer.get(buf);
            try {
                return AgentMetric.getAgentMetric(new String(buf, RocksDBUtils.UTF8), type);
            } catch (BadlyFormedNameException e) {
                throw new RocksDBException(e.getMessage());
            }
        }
        
    };
    
    private AgentMetric2Id agentMetric2Id;
    private Id2AgentMetric id2agentMetric;
    
    public AgentMetricTables(ByteBufferCache bc, RocksDB db) {
        agentMetric2Id = new AgentMetric2Id(bc, db);
        id2agentMetric = new Id2AgentMetric(bc, db);
    }

    public int getAttributeId(AgentMetric agentAttribute) throws RocksDBException {
        Integer id = agentMetric2Id.get(agentAttribute);
        if (id != null) {
            return id;
        }

        id = id2agentMetric.getMaxId();
        if (id == null) {
            id = 0;
        }
        id++;
        
        agentMetric2Id.put(agentAttribute, id);
        id2agentMetric.put(id, agentAttribute);
        
        return id;
    }

    public AgentMetric getAttributeById(int id) throws RocksDBException {
        return id2agentMetric.get(id);
    }

}
