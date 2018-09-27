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

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public abstract class Integer2ObjectTable<V> extends ObjectTable<Integer, V> {

    public Integer2ObjectTable(ByteBufferCache bc, RocksDB db, int tableId, boolean doCache) {
        super(bc, db, tableId, doCache);
    }

    @Override
    protected void writeKey(ByteBuffer buffer, Integer key) {
        buffer.putInt(key);
    }
    
    @Override
    protected Integer readKey(ByteBuffer buffer) throws RocksDBException {
        return buffer.getInt();
    }

    public Integer getMaxId() {
        byte[] key = getLastKeyOrValueInPrefix(db, createTablePrefixKey(tableId), true);
        if (key == null) {  
            return null;
        }
        ByteBuffer b = ByteBuffer.wrap(key);
        return b.getInt(4);
    }

    private static byte[] createTablePrefixKey(int table) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.order(ByteOrder.BIG_ENDIAN);
        b.putInt(table);
        b.flip();
        return toArray(b);
    }
    
}
