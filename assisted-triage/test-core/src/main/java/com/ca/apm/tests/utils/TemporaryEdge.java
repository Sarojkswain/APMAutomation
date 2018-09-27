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
package com.ca.apm.tests.utils;

import java.util.List;
import java.util.Map;

import com.ca.apm.classes.from.appmap.plugin.TopologicalChangeEvent.TopologicalChangeType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/*
 * Placeholder for getGraph output.
 */
public class TemporaryEdge {

    private TopologicalChangeType topologicalChange;
    private String sourceId;
    private String targetId;
    private String businessTransactionId;
    private long liveCacheVersion;
    private Map<String, String> attributes;
    private String backendId;
    

    public TemporaryEdge() {

    }
    
    @JsonInclude(Include.NON_NULL)
    public TopologicalChangeType getTopologicalChange() {
        return topologicalChange;
    }

    public void setTopologicalChange(TopologicalChangeType topologicalChange) {
        this.topologicalChange = topologicalChange;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getBusinessTransactionId() {
        return businessTransactionId;
    }

    public void setBusinessTransactionId(String businessTransactionId) {
        this.businessTransactionId = businessTransactionId;
    }

    public long getLiveCacheVersion() {
        return liveCacheVersion;
    }

    public void setLiveCacheVersion(long liveCacheVersion) {
        this.liveCacheVersion = liveCacheVersion;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    public String getBackendId() {
        return backendId;
    }
    
    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

}
