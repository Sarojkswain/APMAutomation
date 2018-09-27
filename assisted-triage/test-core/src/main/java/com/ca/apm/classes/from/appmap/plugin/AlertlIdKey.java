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
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.classes.from.appmap.plugin;


public class AlertlIdKey {

    private final ExternalId externalIdVertex;
    
    private final String externalIdAlert;
    private final String externalIdMetric;
    
    public AlertlIdKey(ExternalId externalIdVertex, String externalIdAlert, String externalIdMetric) {
        super();
        this.externalIdVertex = externalIdVertex;
        this.externalIdAlert = externalIdAlert;
        this.externalIdMetric = externalIdMetric;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((externalIdAlert == null) ? 0 : externalIdAlert.hashCode());
        result = prime * result + ((externalIdMetric == null) ? 0 : externalIdMetric.hashCode());
        result = prime * result + ((externalIdVertex == null) ? 0 : externalIdVertex.hashCode());
        return result;
    }


    public ExternalId getExternalIdVertex() {
        return externalIdVertex;
    }

    public String getExternalIdAlert() {
        return externalIdAlert;
    }

    @Override
    public String toString() {
        return externalIdVertex + ";" + externalIdAlert;
    }

    public String getExternalIdMetric() {
        return externalIdMetric;
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
        AlertlIdKey other = (AlertlIdKey) obj;
        if (externalIdAlert == null) {
            if (other.externalIdAlert != null) {
                return false;
            }
        } else if (!externalIdAlert.equals(other.externalIdAlert)) {
            return false;
        }
        if (externalIdMetric == null) {
            if (other.externalIdMetric != null) {
                return false;
            }
        } else if (!externalIdMetric.equals(other.externalIdMetric)) {
            return false;
        }
        if (externalIdVertex == null) {
            if (other.externalIdVertex != null) {
                return false;
            }
        } else if (!externalIdVertex.equals(other.externalIdVertex)) {
            return false;
        }
        return true;
    }
    
}
