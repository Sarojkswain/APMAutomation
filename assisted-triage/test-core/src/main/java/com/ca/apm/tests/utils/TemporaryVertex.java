/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without * the express written
 * permission of CA. All authorized reproductions must be
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
package com.ca.apm.tests.utils;

import java.util.List;
import java.util.Map;

import com.ca.apm.classes.from.appmap.plugin.Layer;
import com.ca.apm.classes.from.appmap.plugin.TopologicalChangeEvent.TopologicalChangeType;
import com.ca.apm.classes.from.appmap.plugin.VertexStatus;

/*
 * Placeholder for getGraph output.
 */
public class TemporaryVertex {

    private String vertexId;
    private Map<String, List<String>> attributes;
    private VertexStatus status;
    private long liveCacheVersion;
    private TopologicalChangeType topologicalChange;
    private boolean gAttributesChanged;
    private String worstAlertState;
    private long endTime;
    private Layer layer;

    public TemporaryVertex() {}

    public String getVertexId() {
        return vertexId;
    }

    public void setVertexId(String vertexId) {
        this.vertexId = vertexId;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    public VertexStatus getStatus() {
        return status;
    }

    public void setStatus(VertexStatus status) {
        this.status = status;
    }

    public long getLiveCacheVersion() {
        return liveCacheVersion;
    }

    public void setLiveCacheVersion(long liveCacheVersion) {
        this.liveCacheVersion = liveCacheVersion;
    }

    public TopologicalChangeType getTopologicalChange() {
        return topologicalChange;
    }

    public void setTopologicalChange(TopologicalChangeType topologicalChange) {
        this.topologicalChange = topologicalChange;
    }

    public boolean isgAttributesChanged() {
        return gAttributesChanged;
    }

    public void setgAttributesChanged(boolean gAttributesChanged) {
        this.gAttributesChanged = gAttributesChanged;
    }

    public String getWorstAlertState() {
        return worstAlertState;
    }

    public void setWorstAlertState(String worstAlertState) {
        this.worstAlertState = worstAlertState;
    }
    
    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }
}
