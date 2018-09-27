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
 * Evidence statement created for alert events.
 *
 */
public class AlertEventStatement extends EvidenceStatement {

    private Set<String> alerts;

    private AlertEventStatement(AlertEventStatement incoming) {

        setSuspect(incoming.getSuspect());
        setFirstOccurrence(incoming.getFirstOccurrence());
        setLastOccurence(incoming.getLastOccurrence());
        setAlerts(new HashSet<String>(incoming.getAlerts()));
    }

    public AlertEventStatement() {
        alerts = new HashSet<String>();
    }

    public void setAlerts(Set<String> alerts) {

        this.alerts = alerts;
    }

    public void addAlert(String alert) {
        alerts.add(alert);
    }

    public Set<String> getAlerts() {

        return alerts;
    }

    @JsonProperty("alertsCount")
    private int getAlertCount() {
        return alerts.size();
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
        result = prime * result + ((alerts == null) ? 0 : alerts.hashCode());
        return result;
    }

    /**
     * Find equality between two statements.
     * Two statements of this type are equal if the all of
     * suspect, occurrence times, count and alerts are equal
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof AlertEventStatement) {

            AlertEventStatement stmnt = (AlertEventStatement) obj;
            Actor suspect = getSuspect();
            if ((suspect == stmnt.getSuspect() || (suspect != null && suspect.equals(stmnt
                .getSuspect())))) {

                if ((alerts == stmnt.getAlerts() || (alerts != null && alerts.equals(stmnt
                    .getAlerts())))) {

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

        if (data instanceof AlertEventStatement) {

            AlertEventStatement incoming = (AlertEventStatement) data;

            if (getFirstOccurrence() > incoming.getFirstOccurrence()) {

                setFirstOccurrence(incoming.getFirstOccurrence());
            }

            if (getLastOccurrence() < incoming.getLastOccurrence()) {

                setLastOccurence(incoming.getLastOccurrence());
            }
            for (String alert : incoming.getAlerts()) {
                addAlert(alert);
            }
        }
    }

    @Override
    public EvidenceStatement safeClone() {
        return new AlertEventStatement(this);
    }

    @Override
    public Collection<String> evidenceObjects() {
        return alerts;
    }

	@Override
	public TriageEventType getEventType() {
		return TriageEventType.ALERT;
	}
}
