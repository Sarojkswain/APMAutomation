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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteOptions;

import com.wily.introscope.server.enterprise.entity.rocksdb.RocksDBUtils;

public abstract class ObjectTable<K, V> {

    private final ByteBufferCache bufferCache;

    private Map<K, V> cache;
    
    protected final RocksDB db;

    protected final int tableId;

    private WriteOptions options;
    
    public static interface Callback<K, V> {
        
        /**
         * @param key
         * @param value
         * @return true to continue iteration
         */
        boolean row(K key, V value); 
        
    }

    public ObjectTable(ByteBufferCache bc, RocksDB db, int tableId, boolean doCache) {
        this.db = db;
        this.bufferCache = bc;
        if (doCache) {
            cache = new ConcurrentHashMap<>();
        }
        this.tableId = tableId;
        this.options = new WriteOptions();
        this.options.setDisableWAL(true);
    }

    public V get(K k) throws RocksDBException {
        V result = cache == null ? null : cache.get(k);
        if (result != null) {
            return result;
        }

        byte[] key = writeTableKey(k);
        byte[] value = db.get(key);
        if (value != null) {
            result = readValue(createBuffer(value));
            if (cache != null) {
                cache.put(k, result);
            }
            return result;
        }
        
        return null;
    }

    public void put(K k, V v) throws RocksDBException {
        byte[] key = writeTableKey(k);
        byte[] value = writeTableValue(v);
        
        db.put(options, key, value);
        if (cache != null) {
            cache.remove(key);
        }
    }

    private byte[] writeTableKey(K key) {
        ByteBuffer buffer = bufferCache.get();
        try {
            buffer.clear();
            buffer.putInt(tableId);
            writeKey(buffer, key);
            buffer.flip();
            return toArray(buffer);
        } finally {
            bufferCache.put(buffer);
        }
    }

    private byte[] writeTableValue(V value) throws RocksDBException {
        ByteBuffer buffer = bufferCache.get();
        try {
            buffer.clear();
            writeValue(buffer, value);
            buffer.flip();
            return toArray(buffer);
        } finally {
            bufferCache.put(buffer);
        }
    }
    

    protected abstract void writeKey(ByteBuffer buffer, K key);

    protected abstract void writeValue(ByteBuffer buffer, V value) throws RocksDBException;

    protected abstract K readKey(ByteBuffer buffer) throws RocksDBException;

    protected abstract V readValue(ByteBuffer buffer) throws RocksDBException;
    
    public void iterate(byte [] keyPrefix, Callback<K, V> callback) throws RocksDBException {
        RocksIterator it = db.newIterator();
        ByteBuffer buffer = bufferCache.get();
        ByteBuffer buffer2 = bufferCache.get();
        try {
            for (it.seek(keyPrefix); it.isValid(); it.next()) {
                buffer = readKey(it, buffer);
                if (!RocksDBUtils.startsWith(buffer, keyPrefix)) {
                    break;
                }
                buffer2 = readValue(it, buffer2);
                if (!callback.row(readTableKey(buffer), readValue(buffer2))) {
                    break;
                }
            }
        } finally {
            bufferCache.put(buffer);
            bufferCache.put(buffer2);
            it.dispose();
        }
    }
    
    private K readTableKey(ByteBuffer buffer) throws RocksDBException {
        buffer.getInt();
        return readKey(buffer);
    }

    private static ByteBuffer readKey(RocksIterator it, ByteBuffer bufferKey) {
        int size = it.key(bufferKey.array());
        if (size > bufferKey.capacity()) {
            bufferKey = ByteBuffer.allocate(size);
            it.key(bufferKey.array());
        }
        bufferKey.limit(size);
        bufferKey.position(0);
        return bufferKey;
    }

    private static ByteBuffer readValue(RocksIterator it, ByteBuffer bufferValue) {
        int size = it.value(bufferValue.array());
        if (size > bufferValue.capacity()) {
            bufferValue = ByteBuffer.allocate(size);
            it.value(bufferValue.array());
        }
        bufferValue.limit(size);
        bufferValue.position(0);
        return bufferValue;
    }
    
    private static ByteBuffer createBuffer(byte[] value) {
        ByteBuffer result = ByteBuffer.wrap(value);
        result.order(ByteOrder.BIG_ENDIAN);
        return result;
    }
    
    public static byte[] toArray(ByteBuffer b) {
        byte [] result = new byte[b.remaining()];
        System.arraycopy(b.array(), b.position(), result, 0, b.remaining());
        return result;
    }    
    
    public static byte[] getLastKeyOrValueInPrefix(RocksDB db, byte [] prefix, boolean returnKey) {
        RocksIterator it = db.newIterator();        
        try {
            int i = prefix.length - 1;
            for (; i > 0; i--) {
                if ((prefix[i] & 0xff) != 0xff) {
                    break;
                }                
            }
            
            if ((prefix[i] & 0xff) == 0xff) {
                throw new IllegalArgumentException();
            }
            
            // Right after prefix
            prefix[i]++;
            for (int o = i + 1; o < prefix.length; o++) {
                prefix[o] = 0;
            }
            
            it.seek(prefix);
            
            // Switch back
            prefix[i]--;
            for (int o = i + 1; o < prefix.length; o++) {
                prefix[o] = (byte) 0xff;
            }
            
            if (it.isValid()) {
                it.prev();
            } else {
                it.seekToLast();                
            }
            
            if (!it.isValid()) {
                return null;
            }
            
            byte[] bufferKey = it.key();            
            if (! RocksDBUtils.startsWith(bufferKey, prefix) ) {
                return null;
            }
            
            if (returnKey) {
                return bufferKey;
            }
            
            byte[] bufferValue = it.value();            
            return bufferValue;
        } finally {
            it.dispose();
        }
    }
    
}
