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
import java.util.ArrayList;
import java.util.List;

import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;
import com.wily.introscope.spec.server.transactiontrace.TransactionTraceData;
import org.eclipse.equinox.weaving.internal.caching.Log;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.wily.introscope.server.enterprise.entity.rocksdb.RocksDBUtils;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import org.rocksdb.RocksIterator;

public class AgentNameTables {

    private static class AgentName2Id extends Object2IntegerTable<AgentName> {

        public AgentName2Id(ByteBufferCache bc, RocksDB db) {
            super(bc, db, HammondTables.TABLE_AGENT_NAME_TO_ID, true);
        }

        @Override
        protected void writeKey(ByteBuffer buffer, AgentName key) {
            String name = key.getProcessURLWithoutDomain();
            byte[] bytes = name.getBytes(RocksDBUtils.UTF8);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }
        
        @Override
        protected AgentName readKey(ByteBuffer buffer) throws RocksDBException {
            byte[] buf = new byte[buffer.getInt()];
            buffer.get(buf);
            try {
                return AgentName.getAgentName("SuperDomain|" + new String(buf, RocksDBUtils.UTF8));
            } catch (BadlyFormedNameException e) {
                throw new RocksDBException(e.getMessage());
            }
        }
        
    };

    private static class Id2AgentName extends Integer2ObjectTable<AgentName> {

        public Id2AgentName(ByteBufferCache bc, RocksDB db) {
            super(bc, db, HammondTables.TABLE_ID_TO_AGENT_NAME, true);
        }

        @Override
        protected void writeValue(ByteBuffer buffer, AgentName value) {
            String name = value.getProcessURLWithoutDomain();
            byte[] bytes = name.getBytes(RocksDBUtils.UTF8);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }

        @Override
        protected AgentName readValue(ByteBuffer buffer) throws RocksDBException {
            byte[] buf = new byte[buffer.getInt()];
            buffer.get(buf);
            try {
                return AgentName.getAgentName("SuperDomain|" + new String(buf, RocksDBUtils.UTF8));
            } catch (BadlyFormedNameException e) {
                throw new RocksDBException(e.getMessage());
            }
        }
        
    };
    
    private AgentName2Id agentName2Id;
    private Id2AgentName id2agentName;
    
    public AgentNameTables(ByteBufferCache bc, RocksDB db) {
        agentName2Id = new AgentName2Id(bc, db);
        id2agentName = new Id2AgentName(bc, db);
    }

    public int getAgentId(AgentName agentName) throws RocksDBException {
        Integer id = agentName2Id.get(agentName);
        if (id != null) {
            return id;
        }

        id = id2agentName.getMaxId();
        if (id == null) {
            id = 0;
        }
        id++;
        
        agentName2Id.put(agentName, id);
        id2agentName.put(id, agentName);
        
        return id;
    }

    public List<AgentName> getAgents() {

        final List<AgentName> result = new ArrayList<>();

        try {
            agentName2Id.iterate(ByteBuffer.allocate(4).putInt(agentName2Id.tableId).array(),
                    new ObjectTable.Callback<AgentName, Integer>() {
                        @Override
                        public boolean row(AgentName key, Integer value) {
                            result.add(key);
                            return true;
                        }});
        } catch (RocksDBException e) {
            Log.error("Cannot get agent names from RocksDB. ", e);
        }

        return result;
    }
    

}
