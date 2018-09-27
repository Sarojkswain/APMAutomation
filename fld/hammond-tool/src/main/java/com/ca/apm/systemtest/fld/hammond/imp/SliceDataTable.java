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

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.ca.apm.systemtest.fld.hammond.imp.SliceDataValues.SliceDataValue;
import com.wily.introscope.spec.server.beans.metricdata.IMetricDataValue;
import com.wily.introscope.stat.timeslice.ANumericalTimeslicedValue;
import com.wily.introscope.stat.timeslice.IntegerTimeslicedValue;
import com.wily.introscope.stat.timeslice.LongTimeslicedValue;

public class SliceDataTable extends ObjectTable<SliceDataKey, SliceDataValues> {

    public SliceDataTable(ByteBufferCache bc, RocksDB db) {
        super(bc, db, HammondTables.TABLE_SLICE_DATA, false);
    }

    @Override
    protected void writeKey(ByteBuffer buffer, SliceDataKey key) {
        buffer.putLong(key.getTime());
        buffer.putInt(key.getAgentId());
    }

    @Override
    protected SliceDataKey readKey(ByteBuffer buffer) throws RocksDBException {
        long time = buffer.getLong();
        int agentId = buffer.getInt();
        return new SliceDataKey(time, agentId);
    }
    
    @Override
    protected void writeValue(ByteBuffer buffer, SliceDataValues value) {
        buffer.putInt(value.getValues().size());
        buffer.putLong(value.getValues().get(0).getValue().getStopTimestampInMillis());
        for (SliceDataValue h : value.getValues()) {
            ANumericalTimeslicedValue v = (ANumericalTimeslicedValue) h.getValue();
            buffer.putInt((v instanceof LongTimeslicedValue) ? 1 : 0);
            buffer.putInt(h.getAttribute());
            buffer.putInt(v.getType());
            buffer.putLong(v.getMinimumAsLong());
            buffer.putLong(v.getMaximumAsLong());
            buffer.putLong(v.getValueAsLong());
            buffer.putLong(v.getDataPointCount());
        }
    }

    @Override
    protected SliceDataValues readValue(ByteBuffer buffer) throws RocksDBException {
        SliceDataValues result = new SliceDataValues(new ArrayList<SliceDataValue>());
        List<SliceDataValue> values = result.getValues();
        int count = buffer.getInt();
        long time = buffer.getLong();
        for (int i = 0; i < count; i++) {
            boolean isLong = buffer.getInt() == 1;
            int attribute, type;
            long min, max, value, dataCount;
            attribute = buffer.getInt();
            type = buffer.getInt();
            min = buffer.getLong();
            max = buffer.getLong();
            value = buffer.getLong();
            dataCount = buffer.getLong();
            
            IMetricDataValue dv = isLong ?
                    new LongTimeslicedValue(type, time - 15000, time, null, dataCount, false, value, min, max)
                    : new IntegerTimeslicedValue(type, time - 15000, time, null, dataCount, false, (int)value, (int)min, (int)max);
            
            values.add(new SliceDataValue(dv, attribute));
        }
        return result;
    }


}
