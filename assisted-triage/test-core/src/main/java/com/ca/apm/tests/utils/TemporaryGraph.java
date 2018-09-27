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
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS * EXPRESSLY ADVISED OF SUCH
 * LOSS OR DAMAGE.
 */
package com.ca.apm.tests.utils;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * Placeholder for getGraph output.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemporaryGraph {

    private Collection<TemporaryVertex> vertices;
    private Collection<TemporaryEdge> edges;
    private String liveModeVersion;
    private boolean incrementalResult;
    private Collection<TemporaryVertex> removedVertices;
    private Collection<TemporaryEdge> removedEdges;
    private boolean resultClamped = false;
    private long resultVerticesSize = 0;
    private long resultVerticesLimit = 0;
    private long graphTime;

    public TemporaryGraph() {}

    public Collection<TemporaryVertex> getVertices() {
        return vertices;
    }

    public void setVertices(Collection<TemporaryVertex> vertices) {
        this.vertices = vertices;
    }

    public Collection<TemporaryEdge> getEdges() {
        return edges;
    }

    public void setEdges(Collection<TemporaryEdge> edges) {
        this.edges = edges;
    }

    public String getLiveModeVersion() {
        return liveModeVersion;
    }

    public void setLiveModeVersion(String liveModeVersion) {
        this.liveModeVersion = liveModeVersion;
    }

    public boolean isIncrementalResult() {
        return incrementalResult;
    }

    public void setIncrementalResult(boolean incrementalResult) {
        this.incrementalResult = incrementalResult;
    }

    public Collection<TemporaryVertex> getRemovedVertices() {
        return removedVertices;
    }

    public void setRemovedVertices(Collection<TemporaryVertex> removedVertices) {
        this.removedVertices = removedVertices;
    }

    public Collection<TemporaryEdge> getRemovedEdges() {
        return removedEdges;
    }

    public void setRemovedEdges(Collection<TemporaryEdge> removedEdges) {
        this.removedEdges = removedEdges;
    }

    public boolean isResultClamped() {
        return resultClamped;
    }

    public void setResultClamped(boolean resultClamped) {
        this.resultClamped = resultClamped;
    }

    public long getResultVerticesSize() {
        return resultVerticesSize;
    }

    public void setResultVerticesSize(long resultVerticesSize) {
        this.resultVerticesSize = resultVerticesSize;
    }

    public long getResultVerticesLimit() {
        return resultVerticesLimit;
    }

    public void setResultVerticesLimit(long resultVerticesLimit) {
        this.resultVerticesLimit = resultVerticesLimit;
    }

    public long getGraphTime() {
        return graphTime;
    }

    public void setGraphTime(long graphTime) {
        this.graphTime = graphTime;
    }
    
    public long getLiveModeVersionAsLong() {
        if (liveModeVersion == null) {
            return 0;
        }
        return Long.parseLong(liveModeVersion);
    }
}
