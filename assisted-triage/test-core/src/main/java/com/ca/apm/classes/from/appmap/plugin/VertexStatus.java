/*
* Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE 
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR 
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST 
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS 
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.  
 */
package com.ca.apm.classes.from.appmap.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author julro02
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class VertexStatus {
    public static final int NULL_STATUS = 127 & AlertState.STATUS_MASK;
    
    private final List<AlertState> alertStates;

    public VertexStatus() {
        this.alertStates = new ArrayList<AlertState>();
    }

    public VertexStatus(List<AlertState> alertStates) {
        this.alertStates = alertStates;
    }
        
    public void addAlertStatus(AlertState state) {
        this.alertStates.add(state);
    }


    public void addAlertStatuses(Collection<AlertState> states) {
        this.alertStates.addAll(states);
    }

    public void setAlertStatuses(Collection<AlertState> states) {
        this.alertStates.clear();
        this.alertStates.addAll(states);
    }
    
    public List<AlertState> getAlertStates() {
        return this.alertStates;
    }
    
    public VertexStatus clone() {
        return new VertexStatus(new ArrayList<AlertState>(alertStates));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alertStates == null) ? 0 : alertStates.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VertexStatus other = (VertexStatus) obj;
        if (alertStates == null) {
            if (other.alertStates != null) {
                return false;
            }
        } else if (!alertStates.equals(other.alertStates)) {
            return false;
        }
        return true;
    }

}
