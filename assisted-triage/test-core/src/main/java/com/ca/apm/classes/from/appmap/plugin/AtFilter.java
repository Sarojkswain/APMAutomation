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

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Filter to be used by consumers of story REST API.
 * Contains commonly used filter for APM like ATC
 * universe and also specific filters related to story
 * engine as well.
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtFilter {

    public static final String PROJ_SUMMARY = "summary";
    public static final String PROJ_DETAILED = "detailed";

    private String startTime;

    private String endTime;

    private Set<String> storyIds;

    private Set<ExternalId> vertexIds;

    private String projection;

    private Boolean mergeStories;

    private Boolean translateExternalIds;

    private int limit;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Set<String> getStoryIds() {
        return storyIds;
    }

    public void setStoryIds(Set<String> storyIds) {
        this.storyIds = storyIds;
    }

    public Set<ExternalId> getVertexIds() {
        return vertexIds;
    }

    public void setVertexIds(Set<ExternalId> vertexIds) {
        this.vertexIds = vertexIds;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public Boolean getMergeStories() {
        return mergeStories;
    }

    public void setMergeStories(Boolean mergeStories) {
        this.mergeStories = mergeStories;
    }

    public Boolean getTranslateExternalIds() {
        return translateExternalIds;
    }

    public void setTranslateExternalIds(Boolean translateExternalIds) {
        this.translateExternalIds = translateExternalIds;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
