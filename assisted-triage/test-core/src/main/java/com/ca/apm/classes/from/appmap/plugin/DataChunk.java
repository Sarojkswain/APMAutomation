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
package com.ca.apm.classes.from.appmap.plugin;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataChunk implements Cloneable {

    private long firstEndTimestamp;
    private double [] values;
    private double [] mins;
    private double [] maxes;
    private long [] counts;

    public DataChunk() {
        
    }
    
    public DataChunk(long firstEndTimestamp, double [] values, double [] mins,
                     double [] maxes, long [] counts) {
         this.firstEndTimestamp = firstEndTimestamp;
         this.values = values;
         this.mins = mins;
         this.maxes = maxes;
         this.counts = counts;
     }
    
    public long getFirstEndTimestamp() {
        return firstEndTimestamp;
    }

    public long getLastEndTimestamp(long interval) {
        if (size() > 1) {
            return firstEndTimestamp + (size() - 1) * interval;
        } else {
            return firstEndTimestamp;
        }
    }

    @JsonIgnore
    public int size() {
        if (values == null) {
            return 0;
        }
        return values.length;
    }

    public void setFirstEndTimestamp(long firstEndTimestamp) {
        this.firstEndTimestamp = firstEndTimestamp;
    }
    @Override
    protected Object clone() {
        DataChunk cloned = new DataChunk(firstEndTimestamp, Arrays.copyOf(values, values.length), 
            Arrays.copyOf(mins, mins.length), Arrays.copyOf(maxes, maxes.length), Arrays.copyOf(counts, counts.length));
        return cloned;
    }

    public int getIndex(long time, long interval) {
        int result = (int) ((time - firstEndTimestamp) / interval);
        if (result < 0 || result >= values.length) {
            return -1;
        }
        return result;
    }

    public double getValue(int index) {
        return values[index];
    }

    public double getMin(int index) {
        return mins[index];
    }

    public double getMax(int index) {
        return maxes[index];
    }

    public long getCount(int index) {
        return counts[index];
    }

    public boolean isNull(int index) {
        return index < 0 || index >= values.length || counts[index] == 0;
    }
    
    public void replaceChunk(DataChunk chunk) {
        this.firstEndTimestamp = chunk.firstEndTimestamp;
        this.values = Arrays.copyOf(chunk.values, chunk.values.length);
        this.mins = Arrays.copyOf(chunk.mins, chunk.mins.length);
        this.maxes = Arrays.copyOf(chunk.maxes, chunk.maxes.length);
        this.counts = Arrays.copyOf(chunk.counts, chunk.counts.length);
    }

    public double[] getValues() {
        return values;
    }

    public double[] getMins() {
        return mins;
    }

    public double[] getMaxes() {
        return maxes;
    }

    public long[] getCounts() {
        return counts;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    public void setMins(double[] mins) {
        this.mins = mins;
    }

    public void setMaxes(double[] maxes) {
        this.maxes = maxes;
    }

    public void setCounts(long[] counts) {
        this.counts = counts;
    }


}

