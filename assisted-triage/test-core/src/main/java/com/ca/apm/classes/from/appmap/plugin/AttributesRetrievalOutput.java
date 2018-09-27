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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributesRetrievalOutput {

    private String timestamp;

    private long timestampEpoch;

    private Set<String> notFoundIds;

    private Map<String, Collection<Attribute>> attributes;
    private Map<String, Collection<AlertState>> alerts;

    public AttributesRetrievalOutput() {
    }
    
    public AttributesRetrievalOutput(String timestamp, long timestampEpoch,
        Map<String, Collection<Attribute>> attributes,
        Map<String, Collection<AlertState>> alerts, Set<String> notFoundIds) {

        this.timestamp = timestamp;
        this.timestampEpoch = timestampEpoch;
        this.attributes = attributes;
        this.alerts = alerts;
        this.notFoundIds = notFoundIds;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getTimestampEpoch() {
        return timestampEpoch;
    }

    public Map<String, Collection<Attribute>> getAttributes() {
        return attributes == null
            ? Collections.<String, Collection<Attribute>>emptyMap()
            : attributes;
    }

    public Map<String, Collection<AlertState>> getAlerts() {
        return alerts == null ? Collections.<String, Collection<AlertState>>emptyMap() : alerts;
    }

    public Collection<String> getNotFoundIds() {
        return notFoundIds == null ? Collections.<String>emptySet() : notFoundIds;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestampEpoch(long timestampEpoch) {
        this.timestampEpoch = timestampEpoch;
    }

    public void setNotFoundIds(Set<String> notFoundIds) {
        this.notFoundIds = notFoundIds;
    }

    public void setAttributes(Map<String, Collection<Attribute>> attributes) {
        this.attributes = attributes;
    }

    public void setAlerts(Map<String, Collection<AlertState>> alerts) {
        this.alerts = alerts;
    }
    
    
    
}
