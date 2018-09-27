/*
* Copyright (c) 2015 CA.  All rights reserved.
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author julro02
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using=ExternalIdSerializer.class)
@JsonDeserialize(using=ExternalIdDeserializer.class)
public class ExternalId implements Comparable<ExternalId> {
    private final Layer layer;
    private final String justExternalId;
    
    public ExternalId(Layer layer, String justExternalId) {
        this.layer = layer;
        this.justExternalId = justExternalId;
    }
    
    public Layer getLayer() {
        return layer;
    }
    
    public String getJustExternalId() {
        return justExternalId;
    }

    @Override
    public String toString() {
        return layer.getValue() + ":" + justExternalId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((justExternalId == null) ? 0 : justExternalId.hashCode());
        result = prime * result + ((layer == null) ? 0 : layer.hashCode());
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
        ExternalId other = (ExternalId) obj;
        if (justExternalId == null) {
            if (other.justExternalId != null) {
                return false;
            }
        } else if (!justExternalId.equals(other.justExternalId)) {
            return false;
        }
        if (layer == null) {
            if (other.layer != null) {
                return false;
            }
        } else if (!layer.equals(other.layer)) {
            return false;
        }
        return true;
    }

    public int compareTo(ExternalId externalId) {
        if (externalId == null) {
            return -1;
        }
        int result = this.justExternalId.compareTo(externalId.justExternalId);
        if (result != 0) {
            return result;
        }
        result = this.layer.compareTo(externalId.layer);
        return result;
    }

    public static ExternalId fromString(String externalId) {
        if (externalId == null) {
            return null;
        }
        int ndx =  externalId.indexOf(":");
        Layer layer;
        String justExternalId;
        if (ndx < 1 || ndx >= externalId.length() -1) {
            throw new IllegalArgumentException("Invalid external id: " + externalId);
        } else {
            layer = Layer.fromValue(externalId.substring(0, ndx));
            justExternalId = externalId.substring(ndx + 1);
        }
        return new ExternalId(layer, justExternalId);
    }

}
