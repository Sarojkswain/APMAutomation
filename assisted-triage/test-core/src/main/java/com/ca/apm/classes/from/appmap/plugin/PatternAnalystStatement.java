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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Generic class representing an pattern summary for a
 * particular actor. Each pattern analyst could identify
 * multiple such actors and create one of these for each.
 * A collection of these will go into storage as part of story
 * summary.
 *
 */
public abstract class PatternAnalystStatement implements AnalystStatement {

    private LogicalActor culprit;
    private Collection<String> culpritVertexIds = new HashSet<String>();

    public Collection<String> getCulpritVertexIds() {
        return culpritVertexIds;
    }

    public void setCulpritVertexIds(Collection<String> culpritVertexIds) {
        this.culpritVertexIds = culpritVertexIds;
    }

    public void addCulpritVertexIds(Collection<String> culpritVertexIds) {
        this.culpritVertexIds.addAll(culpritVertexIds);
    }
    
    public LogicalActor getCulprit() {
        return culprit;
    }

    public void setCulprit(LogicalActor logicalCulprit) {
        this.culprit = logicalCulprit;
    }

    /*
     * Force users to override these. This helps
     * us compare summaries over time and reduce
     * creating multiple records. If implementors
     * put wrong logic in there; worst thing that
     * could happen is multiple rows in database
     * instead of single.
     * TODO: any other better way than this?
     */
    @JsonIgnore
    @Override
    public abstract int hashCode();

    @JsonIgnore
    @Override
    public abstract boolean equals(Object obj);

    /*
     * Used to determine hash of this statement without involving
     * physical vertices reference. This is useful in determining
     * if a statement is logically the same across two deployments
     * and hence in deployment merging. In cases no physical references
     * exist; this probably might equal hashCode.
     */
    @JsonIgnore
    public abstract int logicalHash();
    
    @JsonIgnore
    public abstract int physicalHash();

    @JsonIgnore
    public abstract boolean logicalEquals(PatternAnalystStatement stmnt);


    /*
     * Merge only if its logically equal.
     * If not equal, return the current object without modification.
     */
    @JsonIgnore
    public PatternAnalystStatement mergeWith(PatternAnalystStatement incoming) {
    	if (logicalEquals(incoming)) {
    		merge(incoming);
    	}
    	return this;
    }

    @Override
	public void mergeWith(AnalystStatement data) {
		if(data instanceof PatternAnalystStatement) {
			mergeWith((PatternAnalystStatement) data);
		}
		
	}

	/*
     * Merge fields that needs to be merged.
     * Ignore rest of the fields from incoming
     */
    @JsonIgnore
    abstract void merge(PatternAnalystStatement incoming);
}
