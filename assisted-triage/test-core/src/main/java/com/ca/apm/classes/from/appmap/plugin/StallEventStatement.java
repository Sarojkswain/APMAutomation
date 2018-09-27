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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Evidence statement created for stall type events.
 *
 */
public class StallEventStatement extends EvidenceStatement {

    private Set<String> stalledComponents;

    private StallEventStatement(StallEventStatement incoming) {

        setSuspect(incoming.getSuspect());
        setFirstOccurrence(incoming.getFirstOccurrence());
        setLastOccurence(incoming.getLastOccurrence());
        setStalledComponents(new HashSet<String>(incoming.getStalledComponents()));
    }

    public StallEventStatement() {
        stalledComponents = new HashSet<String>();
    }

    public void setStalledComponents(Set<String> stalledComponents) {
        this.stalledComponents = stalledComponents;
    }

    public void addStalledComponent(String stalledComponent) {
        stalledComponents.add(stalledComponent);
    }

    public Set<String> getStalledComponents() {
        return this.stalledComponents;
    }

    @JsonProperty("componentsCount")
    private int getAlertCount() {
        return stalledComponents.size();
    }

    @Override
    public int hashCode() {

        Actor suspect = getSuspect();

        final int prime = 31;
        int result = 1;
        result = prime * result + getClass().getSimpleName().hashCode();
        result = prime * result + ((suspect == null) ? 0 : suspect.hashCode());
        result = (int) (prime * result + getFirstOccurrence());
        result = (int) (prime * result + getLastOccurrence());
        result = prime * result + ((stalledComponents == null) ? 0 : stalledComponents.hashCode());
        return result;
    }

    /**
     * Find equality between two statements.
     * Two statements of this type are equal if the all of
     * suspect, occurrence times, count and error messages
     * are equal
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof StallEventStatement) {

            StallEventStatement stmnt = (StallEventStatement) obj;
            Actor suspect = getSuspect();
            if ((suspect == stmnt.getSuspect() || (suspect != null && suspect.equals(stmnt
                .getSuspect())))) {

                if ((stalledComponents == stmnt.getStalledComponents() || (stalledComponents != null && stalledComponents
                    .equals(stmnt.getStalledComponents())))) {

                    if (getFirstOccurrence() == stmnt.getFirstOccurrence()) {

                        if (getLastOccurrence() == stmnt.getLastOccurrence()) {

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void mergeWith(AnalystStatement data) {
        if (data instanceof StallEventStatement) {

            StallEventStatement incoming = (StallEventStatement) data;

            if (getFirstOccurrence() > incoming.getFirstOccurrence()) {

                setFirstOccurrence(incoming.getFirstOccurrence());
            }

            if (getLastOccurrence() < incoming.getLastOccurrence()) {

                setLastOccurence(incoming.getLastOccurrence());
            }
            for (String stalledComponent : incoming.getStalledComponents()) {
                addStalledComponent(stalledComponent);
            }
        }
    }

    @Override
    public EvidenceStatement safeClone() {

        return new StallEventStatement(this);
    }

	@Override
	public TriageEventType getEventType() {
		return TriageEventType.STALL;
	}

    @Override
    public Collection<String> evidenceObjects() {
        return stalledComponents;
    }
}
