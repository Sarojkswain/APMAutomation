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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a story summary which is for a single or merged result
 * of similar patterns. Impacts and potential impacts are merged
 * across different stories as well if there is more than one. This is one or
 * more StorySnapshot from database and exists separately to easily support
 * JSON/UI
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtStory {

    private String templateKey;

    private Set<String> storyIds;

    private long startTime;

    private boolean active;

    private long endTime;

    private Set<String> contextIds;

    private PatternAnalystStatement pattern;

    private Collection<String> culpritEventTypes;

    private Collection<String> suspectVertexIds;

    private Collection<String> impactVertexIds;

    private Collection<AnalystStatement> evidences;

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String type) {
        this.templateKey = type;
    }

    public Set<String> getStoryIds() {
        return this.storyIds;
    }

    public void setStoryIds(Set<String> storyIds) {
        this.storyIds = storyIds;
    }

    @JsonIgnore
    public void addStoryId(String storyIds) {
        if (this.storyIds == null) {
            this.storyIds = new HashSet<String>();
        }
        this.storyIds.add(storyIds);
    }

    @JsonIgnore
    public void addContextId(String contextId) {
        if (this.contextIds == null) {
            this.contextIds = new HashSet<String>();
        }
        this.contextIds.add(contextId);
    }

    public Set<String> getContextIds() {
        return contextIds;
    }

    public void setContextIds(Set<String> contextIds) {
        this.contextIds = contextIds;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @JsonIgnore
    public void addStoryIds(Set<String> storyIds) {
        if (this.storyIds == null) {
            this.storyIds = new HashSet<String>();
        }
        this.storyIds.addAll(storyIds);
    }

    @JsonIgnore
    public void addContextIds(Set<String> contextIds) {
        if (this.contextIds == null) {
            this.contextIds = new HashSet<String>();
        }
        this.contextIds.addAll(contextIds);
    }

    public PatternAnalystStatement getPattern() {
        return pattern;
    }

    public void setPattern(PatternAnalystStatement pattern) {
        this.pattern = pattern;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Collection<String> getImpactVertexIds() {
        return impactVertexIds;
    }

    public void setImpactVertexIds(Collection<String> impacts) {
        this.impactVertexIds = impacts;
    }

    public Collection<String> getSuspectVertexIds() {
        return suspectVertexIds;
    }

    public void setSuspectVertexIds(Collection<String> suspects) {
        this.suspectVertexIds = suspects;
    }

    public Collection<String> getCulpritEventTypes() {
        return this.culpritEventTypes;
    }

    public void setCulpritEventTypes(Collection<String> events) {
        this.culpritEventTypes = events;
    }

    public void addCulpritEventTypes(Collection<String> events) {
        if (this.culpritEventTypes == null) {
            this.culpritEventTypes = new HashSet<String>();
        }
        this.culpritEventTypes.addAll(events);
    }

    public Collection<AnalystStatement> getEvidences() {
        return evidences;
    }

    public void setEvidences(Collection<AnalystStatement> evidences) {
        this.evidences = evidences;
    }

}
