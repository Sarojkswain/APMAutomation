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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.wily.introscope.spec.server.transactiontrace.TransactionTraceData;

public class TraceDataTable extends ObjectTable<TraceDataKey, TransactionTraceData> {

    public TraceDataTable(ByteBufferCache bc, RocksDB db) {
        super(bc, db, HammondTables.TABLE_TRACE_DATA, false);
    }

    @Override
    protected void writeKey(ByteBuffer buffer, TraceDataKey key) {
        buffer.putLong(key.getTime());
        buffer.putInt(key.getAgentId());
        buffer.putInt(key.getId());
    }
    
    @Override
    protected TraceDataKey readKey(ByteBuffer buffer) throws RocksDBException {
        long time = buffer.getLong();
        int agentId = buffer.getInt();
        int id = buffer.getInt();
        return new TraceDataKey(time, agentId, id);
    }

    @Override
    protected void writeValue(ByteBuffer buffer, TransactionTraceData value) throws RocksDBException {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new ByteBufferOutputStream(buffer)));
            out.writeObject(value);
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RocksDBException(e.getMessage());
        }
    }

    @Override
    protected TransactionTraceData readValue(ByteBuffer buffer) throws RocksDBException {
        try {
            ObjectInputStream out = new ObjectInputStream(new GZIPInputStream(new ByteBufferInputStream(buffer)));
            TransactionTraceData result = (TransactionTraceData) out.readObject();
            out.close();
            return result;
        } catch (IOException | ClassNotFoundException e) {
            throw new RocksDBException(e.getMessage());
        }
    }

    public Integer getMaxId(int agent, long time) {
        byte[] key = getLastKeyOrValueInPrefix(db, createTracePrefixKey(time, agent), true);
        if (key == null) {  
            return null;
        }
        ByteBuffer b = ByteBuffer.wrap(key);
        return b.getInt(16);
    }

    public byte[] createTracePrefixKey(long time, int agent) {
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putInt(tableId);
        buf.putLong(time);
        buf.putInt(agent);
        return buf.array();
    }


}
