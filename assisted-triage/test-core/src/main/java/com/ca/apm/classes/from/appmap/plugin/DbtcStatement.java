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
 * Statement created by DbtcAnalyst representing a high
 * ratio caller problem.
 *
 */
public class DbtcStatement extends PatternAnalystStatement {

    private int ratio;
    private LogicalActor calledComp;
    private Collection<String> calledCompPaths;
    private Collection<String> calledCompVertexIds;

    public LogicalActor getCalledComp() {
        return calledComp;
    }

    public void setCalledComp(LogicalActor calledComp) {
        this.calledComp = calledComp;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public Collection<String> getCalledCompPaths() {
        return calledCompPaths;
    }

    public void setCalledCompPaths(Collection<String> calledCompPaths) {
        this.calledCompPaths = calledCompPaths;
    }

    public void addCalledCompPaths(Collection<String> calledCompPaths) {
        this.calledCompPaths.addAll(calledCompPaths);
    }

    public Collection<String> getCalledCompVertexIds() {
        return calledCompVertexIds;
    }

    public void setCalledCompVertexIds(Collection<String> calledCompVertexIds) {
        this.calledCompVertexIds = calledCompVertexIds;
    }

    public void addCalledCompVertexIds(Collection<String> calledCompVertexIds) {
        this.calledCompVertexIds.addAll(calledCompVertexIds);
    }

 
    /**
     * Find equality between two statements.
     * Two statements of this type are equal if the culprit
     * and called component they refer to are same.
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof DbtcStatement) {

            DbtcStatement stmnt = (DbtcStatement) obj;
            LogicalActor culprit = getCulprit();
            Collection<String> culpritVertexIds = getCulpritVertexIds();

            if ((culprit == stmnt.getCulprit() || (culprit != null && culprit.equals(stmnt
                .getCulprit())))) {

                if ((culpritVertexIds == stmnt.getCulpritVertexIds() || (culpritVertexIds != null && culpritVertexIds
                    .equals(stmnt.getCulpritVertexIds())))) {

                    if ((calledComp == stmnt.getCalledComp() || (calledComp != null && calledComp
                        .equals(stmnt.getCalledComp())))) {

                        if ((calledCompVertexIds == stmnt.getCalledCompVertexIds() || (calledCompVertexIds != null && calledCompVertexIds
                            .equals(stmnt.getCalledCompVertexIds())))) {

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // When merging stories check that statement problems
    // match but dont consider the culprit here.
    @Override
    public boolean logicalEquals(PatternAnalystStatement obj) {

        if (obj instanceof DbtcStatement) {
            DbtcStatement stmnt = (DbtcStatement) obj;
            LogicalActor culprit = getCulprit();

            if ((culprit == stmnt.getCulprit() || (culprit != null && culprit.equals(stmnt
                .getCulprit())))) {

                if ((calledComp == stmnt.getCalledComp() || (calledComp != null && calledComp
                    .equals(stmnt.getCalledComp())))) {
                    return true;
                }
            }
        }
        return false;
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
        result = prime * result + ((calledComp == null) ? 0 : calledComp.hashCode());
        result =
            prime
                * result
                + ((getCalledCompVertexIds().size() == 0) ? 0 : getCalledCompVertexIds().hashCode());
        return result;
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
        result = prime * result + ((calledComp == null) ? 0 : calledComp.hashCode());
        result =
            prime
                * result
                + ((getCalledCompVertexIds().size() == 0) ? 0 : getCalledCompVertexIds().hashCode());
        return result;
    }
    
    @Override
    public int logicalHash() {

        final int prime = 31;
        int result = 1;
        result = prime * result + getClass().getSimpleName().hashCode();
        result = prime * result + (getCulprit() == null ? 0 : getCulprit().hashCode());
        result = prime * result + ((calledComp == null) ? 0 : calledComp.hashCode());

        return result;
    }

    @Override
    void merge(PatternAnalystStatement incoming) {
        DbtcStatement stmnt = (DbtcStatement) incoming;
        addCulpritVertexIds(stmnt.getCulpritVertexIds());
        addCalledCompPaths(stmnt.getCalledCompPaths());
        addCalledCompVertexIds(stmnt.getCalledCompVertexIds());
    }

}
