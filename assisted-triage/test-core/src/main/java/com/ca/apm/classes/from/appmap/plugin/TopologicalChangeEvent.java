/*
* Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE 
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR 
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST 
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS 
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.  
 */
package com.ca.apm.classes.from.appmap.plugin;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TopologicalChangeEvent extends ChangeEvent {
    public static enum TopologicalChangeType {
        NOT_CHANGED,
        ADDED,
        DELETED
    }
    private TopologicalChangeType topologicalChangeType;
    
    
    public TopologicalChangeEvent(Timestamp timestamp, int vertexId, String vertexName,
            TopologicalChangeType topologicalChangeType) {
        super(timestamp, vertexId, vertexName);
        this.topologicalChangeType = topologicalChangeType;
    }
    
    public TopologicalChangeType getTopologicalChangeType() {
        return topologicalChangeType;
    }
    
    // methods for JSON
    public TopologicalChangeEvent() {
    }
    
    public void setTopologicalChangeType(TopologicalChangeType topologicalChangeType) {
        this.topologicalChangeType = topologicalChangeType;
    }
    
    @Override
    public TopologicalChangeEvent clone() {
        TopologicalChangeEvent result = new TopologicalChangeEvent();
        copy(result);
        return result;
    }
    
    @Override
    protected <E extends ChangeEvent> void copy(E target) {
        super.copy(target);
        if (target instanceof TopologicalChangeEvent) {
            TopologicalChangeEvent topoTarget = (TopologicalChangeEvent) target;
            topoTarget.setTopologicalChangeType(topologicalChangeType);
        }
    }
}
