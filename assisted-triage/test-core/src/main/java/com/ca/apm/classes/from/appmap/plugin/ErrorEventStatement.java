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
 * Evidence statement created for error type events.
 *
 */
public class ErrorEventStatement extends EvidenceStatement {

    public static final int MAX_ERROR_MESSAGES_PER_STATEMENT = 10;

    private Set<String> errorMessages;

    private boolean messagesTruncated = false;

    private ErrorEventStatement(ErrorEventStatement incoming) {

        setSuspect(incoming.getSuspect());
        setFirstOccurrence(incoming.getFirstOccurrence());
        setLastOccurence(incoming.getLastOccurrence());
        setErrorMessages(new HashSet<String>(incoming.getErrorMessages()));
        setMessagesTruncated(incoming.isMessagesTruncated());
    }

    public ErrorEventStatement() {
        errorMessages = new HashSet<String>();
    }

    public void setErrorMessages(Set<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public void addErrorMessage(String errorMessage) {
        // limit the error messages contained by each statement, it's ok to ignore
        // additional messages as it anyway keeps the occurrence count and time ranges
        // are intact
        // Error message explosion is very rare and generally may happen due to SQL
        // errors.
        if (getErrorCount() < MAX_ERROR_MESSAGES_PER_STATEMENT) {
            errorMessages.add(errorMessage);
        } else {
            setMessagesTruncated(true);
        }
    }

    public Set<String> getErrorMessages() {
        return this.errorMessages;
    }

    public boolean isMessagesTruncated() {
        return messagesTruncated;
    }

    public void setMessagesTruncated(boolean messagesTruncated) {
        this.messagesTruncated = messagesTruncated;
    }

    @JsonProperty("errorsCount")
    private int getErrorCount() {
        return errorMessages.size();
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
        result = prime * result + ((errorMessages == null) ? 0 : errorMessages.hashCode());
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

        if (obj instanceof ErrorEventStatement) {

            ErrorEventStatement stmnt = (ErrorEventStatement) obj;
            Actor suspect = getSuspect();
            if ((suspect == stmnt.getSuspect() || (suspect != null && suspect.equals(stmnt
                .getSuspect())))) {

                if ((errorMessages == stmnt.getErrorMessages() || (errorMessages != null && errorMessages
                    .equals(stmnt.getErrorMessages())))) {

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
        if (data instanceof ErrorEventStatement) {

            ErrorEventStatement incoming = (ErrorEventStatement) data;

            if (getFirstOccurrence() > incoming.getFirstOccurrence()) {

                setFirstOccurrence(incoming.getFirstOccurrence());
            }

            if (getLastOccurrence() < incoming.getLastOccurrence()) {

                setLastOccurence(incoming.getLastOccurrence());
            }
            for (String errorMessage : incoming.getErrorMessages()) {
                addErrorMessage(errorMessage);
            }
        }
    }

    @Override
    public EvidenceStatement safeClone() {
        return new ErrorEventStatement(this);
    }

	@Override
	public TriageEventType getEventType() {
		return TriageEventType.ERROR;
	}

    @Override
    public Collection<String> evidenceObjects() {
        return errorMessages;
    }

}
