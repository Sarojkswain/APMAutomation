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
 * Represents distribution of events over time. Uses time relative to abstract start position.
 */
interface TimeDistribution {
    /**
     * Hint how long should the caller wait before requesting next transaction [nanoseconds] at
     * given time instant.
     * This is to be used for reducing CPU usage for low transactions per second (below 1000).
     *
     * @return nanoseconds until next transaction
     */
    long getSleepHint(long startTime);

    /**
     * Calculates number of events that happen in given time period after specific time instant.
     *
     * @param start Starting instant relative to internal beginning. [nanoseconds]
     * @param duration Time period for which to accumulate events. [nanoseconds]
     * @return Accumulated event count.
     */
    TimeDistribution.ReturnCount getEventCount(long start, long duration);

    /**
     * Return value helper for {@link TimeDistribution#getEventCount()} of whole count of events
     * and the remainder of time left after those events in given period.
     */
    class ReturnCount {
        final int count;
        final long remainder;

        ReturnCount(int count, long remainder) {
            this.count = count;
            this.remainder = remainder;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + count;
            result = prime * result + (int) (remainder ^ (remainder >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            TimeDistribution.ReturnCount other = (TimeDistribution.ReturnCount) obj;
            if (count != other.count) return false;
            if (remainder != other.remainder) return false;
            return true;
        }

        @Override
        public String toString() {
            return "[" + count + "," + remainder + "]";
        }
    }
}