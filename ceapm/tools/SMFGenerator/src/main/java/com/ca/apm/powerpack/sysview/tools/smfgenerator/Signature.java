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

import java.util.Random;

import com.ca.apm.powerpack.sysview.tools.smfgenerator.SmfSnippet.Type;

/**
 * Unique transaction signature, i.e. what subsystem(s) it touches. If supplied with RNG it can
 * be used to generate transactions.
 */
public class Signature {
    private final Type type;
    private final String jobName;
    private final String db2name;

    /** Base transaction duration. */
    private final long baseLifetime;
    /** Maximal increase of transaction lifetime. */
    private final int maxLifetimeIncreasePct;
    /** RNG for lifetime */
    private final Random rand;
    /** Number of times this signature was used to generate a transaction */
    int generated = 0;
    /** Sum of lifetime of all transactions generated from this signature */
    long sumLifetime = 0;

    /**
     * Constructor.
     *
     * @param type Transaction type.
     * @param jobName Node jobname.
     * @param db2name DB2 name or <code>null</code> if none.
     * @param rand RNG for deterministic randomness, or <code>null</code> when not used for
     *        transaction generation.
     */
    public Signature(Type type, String jobName, String db2name, Random rand) {
        this.type = type;
        this.jobName = jobName;
        this.db2name = db2name;
        this.rand = rand;

        if (rand == null) {
            baseLifetime = 0;
            maxLifetimeIncreasePct = 0;
        } else {
            // 0,01ms-1s [ns], exponential
            baseLifetime = (long) Math.pow(10, 4 + rand.nextInt(6));
            // increased up to +100%
            maxLifetimeIncreasePct = rand.nextInt(100) + 1;
        }
    }

    public SmfSnip createSnip() {
        if (rand == null) {
            throw new IllegalStateException("no RNG provided");
        }
        generated++;
        long lifetime = ((rand.nextInt(maxLifetimeIncreasePct) + 100) * baseLifetime) / 100;
        sumLifetime += lifetime;
        return new SmfSnip(this, lifetime);
    }

    public Type getType() {
        return type;
    }

    public String getJobName() {
        return jobName;
    }

    public String getDb2Ssid() {
        return db2name;
    }

    public int getGenerated() {
        return generated;
    }

    @Override
    public String toString() {
        return "(" + type.toString() + ") " + jobName + ((db2name == null) ? "" : ("|" + db2name));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((db2name == null) ? 0 : db2name.hashCode());
        result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Signature other = (Signature) obj;
        if (db2name == null) {
            if (other.db2name != null) return false;
        } else if (!db2name.equals(other.db2name)) return false;
        if (jobName == null) {
            if (other.jobName != null) return false;
        } else if (!jobName.equals(other.jobName)) return false;
        if (type != other.type) return false;
        return true;
    }
}