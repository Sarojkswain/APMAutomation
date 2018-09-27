/*
 * Copyright (c) 2014 CA. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the <b>Metric values</b> for metric.
 * 
 * @author Petr Kachlik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricValues implements Cloneable {

    private String id;
    // ids of all metrics that were aggregated
    private Set<String> ids;
    private int type;
    private long interval;
    private List<DataChunk> dataChunks;
    private Set<String> mom = new HashSet<String>();

    public MetricValues() {
    }

    public MetricValues(String id, Set<String> ids, int type, long interval, List<DataChunk> dataChunks) {
        super();
        this.id = id;
        this.ids = ids;
        this.type = type;
        this.interval = interval;
        this.dataChunks = dataChunks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public List<DataChunk> getDataChunks() {
        return dataChunks;
    }

    public void setDataChunks(List<DataChunk> dataChunks) {
        this.dataChunks = dataChunks;
    }

    public Set<String> getMom() {
        return mom;
    }

    public void setMom(Set<String> mom) {
        this.mom = mom;
    }

    public void addMom(String mom) {
        this.mom.add(mom);
    }

    @Override
    public String toString() {
        return "[MetricValues|id=" + id + ", type=" + type + ", interval=" + interval
            + ", dataChunks=" + dataChunks + ", mom=" + mom + "]";
    }
    
    @Override
    public Object clone() {
        MetricValues cloned = new MetricValues(); 
        cloned.id = id;
        cloned.ids = new HashSet<String>(ids);
        cloned.type = type;
        cloned.interval = interval;
        cloned.dataChunks = new ArrayList<DataChunk>();
        for (DataChunk chunk : dataChunks) {
            cloned.dataChunks.add((DataChunk) chunk.clone());    
        }
        cloned.mom = new HashSet<String>(mom);
        return cloned;
    }

    @JsonIgnore
    public long getEndTime() {
        if (dataChunks.isEmpty()) {
            return Long.MIN_VALUE;
        }
        return dataChunks.get(dataChunks.size() - 1).getLastEndTimestamp(interval);
    }
    
}
