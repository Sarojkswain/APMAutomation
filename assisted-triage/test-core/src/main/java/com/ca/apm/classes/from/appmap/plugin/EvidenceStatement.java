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

/**
 * Generic class representing an event summary for a
 * particular actor. Some of generic fields could include
 * first occurrence, last occurrence, count etc.
 *
 */
public abstract class EvidenceStatement implements AnalystStatement {

    private Actor suspect;
    private long firstOccurrence = Long.MAX_VALUE;
    private long lastOccurrence = Long.MIN_VALUE;

    public void setSuspect(Actor suspect) {
        this.suspect = suspect;
    }

    public void setFirstOccurrence(long firstOccurrence) {

        this.firstOccurrence = firstOccurrence;
    }

    public void setLastOccurence(long lastOccurrence) {

        this.lastOccurrence = lastOccurrence;
    }

    public Actor getSuspect() {
        return suspect;
    }

    public long getFirstOccurrence() {
        return firstOccurrence;
    }

    public long getLastOccurrence() {
        return lastOccurrence;
    }

    /**
     * @return object this evidence is about, e.g. alert names, metric names, error messages etc.
     */
    public abstract Collection<String> evidenceObjects();

    public abstract EvidenceStatement safeClone();

    public abstract TriageEventType getEventType();

    /*
     * Force users to override these. This helps
     * us compare evidences over time and reduce
     * creating multiple records. If implementors
     * put wrong logic in there; worst thing that
     * could happen is multiple rows in output for
     * merged stories/evidences instead of single.
     * TODO: any other better way than this?
     */
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
