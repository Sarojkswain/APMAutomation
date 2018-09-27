/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.powerpack.sysview.tools.smfgenerator;

/**
 * Transaction SMF record mock, holding the key transaction identifiers.
 */
public class SmfSnip implements SmfSnippet {

    /** transaction lifetime [ns] */
    private final long lifetime;
    private final Signature base;

    public SmfSnip(Signature base, long lifetime) {
        this.base = base;
        this.lifetime = lifetime;
    }

    @Override
    public long getLifetime() {
        return lifetime;
    }

    @Override
    public Type getType() {
        return base.getType();
    }

    @Override
    public String getJobName() {
        return base.getJobName();
    }

    @Override
    public String getDb2Ssid() {
        return base.getDb2Ssid();
    }

    @Override
    public String toString() {
        return base.toString() + "(" + lifetime + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        result = prime * result + (int) (lifetime ^ (lifetime >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SmfSnip other = (SmfSnip) obj;
        if (base == null) {
            if (other.base != null) return false;
        } else if (!base.equals(other.base)) return false;
        if (lifetime != other.lifetime) return false;
        return true;
    }
}