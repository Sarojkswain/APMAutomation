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

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 
 * @author David Nemcok
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ChangeEvent {
    public static enum ChangeEventType {
        TOPOLOGICAL_CHANGE, GATHERED_ATTRIBUTES_CHANGE, STATUS_CHANGE
    }

    private Timestamp time;
    private int vertexId;
    private String vertexName;

    public ChangeEvent(Timestamp timestamp, int vertexId, String vertexName) {
        super();
        this.time = timestamp;
        this.vertexId = vertexId;
        this.vertexName = vertexName;
    }

    @JsonIgnore
    public int getVertexIdAsInt() {
        return this.vertexId;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public int getVertexId() {
        return vertexId;
    }

    public String getVertexName() {
        return vertexName;
    }

    public Timestamp getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "ChangeEvent [name=" + vertexName + ", timestamp=" + time + "]";
    }

    // methods for JSON
    public ChangeEvent() {}

    public void setVertexId(int vertexId) {
        this.vertexId = vertexId;
    }

    public void setVertexName(String vertexName) {
        this.vertexName = vertexName;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        result = prime * result + vertexId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChangeEvent other = (ChangeEvent) obj;
        if (time == null) {
            if (other.time != null) {
                return false;
            }
        } else if (!time.equals(other.time)) {
            return false;
        }
        if (vertexId != other.vertexId) {
            return false;
        }
        return true;
    }

    @Override
    public abstract ChangeEvent clone();

    protected <E extends ChangeEvent> void copy(E target) {
        target.setTime(time);
        target.setVertexId(vertexId);
        target.setVertexName(vertexName);
    }
}
