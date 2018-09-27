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
 * Evidence statement created for UVB type events.
 *
 */
public class UvbEventStatement extends EvidenceStatement {

    private Set<String> metrics;

    public UvbEventStatement(UvbEventStatement incoming) {

        setSuspect(incoming.getSuspect());
        setFirstOccurrence(incoming.getFirstOccurrence());
        setLastOccurence(incoming.getLastOccurrence());
        setMetrics(new HashSet<String>(incoming.getMetrics()));
    }

    public UvbEventStatement() {
        metrics = new HashSet<String>();
    }

    public void setMetrics(Set<String> metrics) {

        this.metrics = metrics;
    }

    public void addMetric(String metric) {
        metrics.add(metric);
    }

    public Set<String> getMetrics() {

        return metrics;
    }

    @JsonProperty("metricsCount")
    private int getAlertCount() {
        return metrics.size();
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
        result = prime * result + ((metrics == null) ? 0 : metrics.hashCode());
        return result;
    }

    /**
     * Find equality between two statements.
     * Two statements of this type are equal if all of
     * suspect, occurrence times, count and metrics are equal
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof UvbEventStatement) {

            UvbEventStatement stmnt = (UvbEventStatement) obj;
            Actor suspect = getSuspect();
            if ((suspect == stmnt.getSuspect() || (suspect != null && suspect.equals(stmnt
                .getSuspect())))) {

                if ((metrics == stmnt.getMetrics() || (metrics != null && metrics.equals(stmnt
                    .getMetrics())))) {

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
        if (data instanceof UvbEventStatement) {

            UvbEventStatement incoming = (UvbEventStatement) data;

            if (getFirstOccurrence() > incoming.getFirstOccurrence()) {

                setFirstOccurrence(incoming.getFirstOccurrence());
            }

            if (getLastOccurrence() < incoming.getLastOccurrence()) {

                setLastOccurence(incoming.getLastOccurrence());
            }
            for (String metric : incoming.getMetrics()) {
                addMetric(metric);
            }
        }
    }

    @Override
    public EvidenceStatement safeClone() {

        return new UvbEventStatement(this);
    }

	@Override
	public TriageEventType getEventType() {
		return TriageEventType.UVB;
	}
    
    @Override
    public Collection<String> evidenceObjects() {
        return metrics;
    }
}
