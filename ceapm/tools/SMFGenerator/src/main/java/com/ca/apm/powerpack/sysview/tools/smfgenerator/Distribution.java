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

import java.util.List;

import com.ca.apm.powerpack.sysview.tools.smfgenerator.TimeDistribution.ReturnCount;

class Distribution {
    private final ConstantTransactionRate timeDist;
    private final ItemDistribution sigDist;
    final long duration;

    public Distribution(int duration, ConstantTransactionRate timeDist,
        WeightedSignatureDistribution sigDist) {
        this.timeDist = timeDist;
        this.sigDist = sigDist;
        this.duration = duration * 1_000_000L;
    }

    /**
     * @param start
     * @param duration
     * @param out
     * @return
     */
    public long getEventIndexes(long start, long duration, List<Integer> out) {
        ReturnCount events = timeDist.getEventCount(start, duration);
        for (int i = events.count; i > 0; i--) {
            int idx = sigDist.getRandomIndex();
            out.add(idx);
        }
        return events.remainder;
    }

    /**
     * @see TimeDistribution#getSleepHint(long)
     */
    public long getSleepHint(long startTime) {
        return timeDist.getSleepHint(startTime);
    }
}