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
 * Statement created by ProblemZoneIdentifier analyst representing
 * the deepest component having the problem.
 *
 */
public class ProblemZoneStatement extends PatternAnalystStatement {

    private String zone;

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
    
    @Override
    public int physicalHash() {

        final int prime = 31;
        int result = 1;
        result = prime * result + getClass().getSimpleName().hashCode();
        result = prime * result + (getCulprit() == null ? 0 : getCulprit().hashCode());
        result =
            prime * result
                + ((getCulpritVertexIds().size() == 0) ? 0 : getCulpritVertexIds().hashCode());
        result = prime * result + ((zone == null) ? 0 : zone.hashCode());

        return result;
    }
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + getClass().getSimpleName().hashCode();
        result = prime * result + (getCulprit() == null ? 0 : getCulprit().hashCode());
        result =
            prime * result
                + ((getCulpritVertexIds().size() == 0) ? 0 : getCulpritVertexIds().hashCode());
         result = prime * result + ((zone == null) ? 0 : zone.hashCode());

        return result;
    }

    /**
     * Finds equality between two statement.
     * Two statements of this type are equal when both culprit and zone
     * they point to are equal.
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ProblemZoneStatement) {

            ProblemZoneStatement stmnt = (ProblemZoneStatement) obj;
            LogicalActor culprit = getCulprit();
            Collection<String> culpritVertexIds = getCulpritVertexIds();
            if ((culprit == stmnt.getCulprit() || (culprit != null && culprit.equals(stmnt
                .getCulprit())))) {
                if ((culpritVertexIds == stmnt.getCulpritVertexIds() || (culpritVertexIds != null && culpritVertexIds
                    .equals(stmnt.getCulpritVertexIds())))) {

                    if ((zone == stmnt.getZone() || (zone != null && zone.equals(stmnt.getZone())))) {

                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean logicalEquals(PatternAnalystStatement obj) {

        if (obj instanceof ProblemZoneStatement) {

            ProblemZoneStatement stmnt = (ProblemZoneStatement) obj;
            LogicalActor culprit = getCulprit();
            if ((culprit == stmnt.getCulprit() || (culprit != null && culprit.equals(stmnt
                .getCulprit())))) {

                if ((zone == stmnt.getZone() || (zone != null && zone.equals(stmnt.getZone())))) {

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int logicalHash() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getClass().getSimpleName().hashCode();
        result = prime * result + (getCulprit() == null ? 0 : getCulprit().hashCode());
        result = prime * result + ((zone == null) ? 0 : zone.hashCode());

        return result;
    }

    /*
     * we merge fields that needs to be merged; rest ignore for
     * incoming. For example; we merge only if its logically equal.
     * If not equal return the current object without modification.
     */
    @Override
    public PatternAnalystStatement mergeWith(PatternAnalystStatement incoming) {

        // TODO: can we avoid this?
        if (logicalEquals(incoming)) {
            ProblemZoneStatement stmnt = (ProblemZoneStatement) incoming;
            addCulpritVertexIds(stmnt.getCulpritVertexIds());
        }
        return this;
    }

	@Override
	void merge(PatternAnalystStatement incoming) {
		 // nothing to merge in pattern
	}
}
