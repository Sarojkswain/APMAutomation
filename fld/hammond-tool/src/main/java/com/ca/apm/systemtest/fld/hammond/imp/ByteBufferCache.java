/*
 * Copyright (c) 2016 CA. All rights reserved.
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
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicReference;

public class ByteBufferCache {

    private static final int CACHE_SIZE = 32;
    private static final int INITIAL_SIZE = 10 * 1024 * 1024;
    
    private AtomicReference<ByteBuffer> single;
    
    private ArrayDeque<ByteBuffer> cache;

    public ByteBufferCache() {
        this(INITIAL_SIZE, 1);
    }
    
    public ByteBufferCache(int initialSize, int cacheSize) {
        cache = new ArrayDeque<>(cacheSize);
        single = new AtomicReference<>();
        single.set(ByteBuffer.allocate(initialSize));
        single.get().order(ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < cacheSize; i++) {
            ByteBuffer buffer = ByteBuffer.allocate(initialSize);
            buffer.order(ByteOrder.BIG_ENDIAN);
            cache.add(buffer);
        }
    }
    
    public ByteBuffer get() {
        ByteBuffer buffer = single.get();
        if (buffer != null && single.compareAndSet(buffer, null)) {
            buffer.clear();
            return buffer;
        }
        
        synchronized(cache) {
            buffer = cache.pollLast();
        }
        
        if (buffer == null) {
            buffer = ByteBuffer.allocate(INITIAL_SIZE);
            buffer.order(ByteOrder.BIG_ENDIAN);
        }
        
        buffer.clear();
        return buffer;
    }
    
    public void put(ByteBuffer buffer) {
        if (single.compareAndSet(null, buffer)) {
            return;
        }
        
        synchronized(cache) {
            if (cache.size() < CACHE_SIZE) {
                cache.add(buffer);
            }
        }
    }
    
}
