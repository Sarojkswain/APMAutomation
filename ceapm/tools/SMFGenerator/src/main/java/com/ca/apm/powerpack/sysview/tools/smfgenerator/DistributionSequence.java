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

import java.util.LinkedList;
import java.util.List;

/**
 * Sequence of periodically repeating item rate/distribution changes.
 */
class DistributionSequence {
    /** Sequence of distributions. */
    private final Distribution[] distributions;
    /** Last instant when we produced any items [ns] */
    private long lastInstant = 0;
    /** Index of currently used distribution. */
    private int distIndex = 0;
    /** Last instant within current distribution when we produced any items [ms] */
    private long lastInDist = 0;

    /**
     * Constructor.
     *
     * @param distributions Partial distributions in order they appear within period.
     */
    public DistributionSequence(Distribution... distributions) {
        if (distributions.length == 0) {
            throw new IllegalArgumentException("no defined distributions");
        }
        for (Distribution dist : distributions) {
            if (dist == null) {
                throw new IllegalArgumentException("a null distribution is used");
            }
        }
        this.distributions = distributions;
    }

    /**
     * Generate new transactions from last instant when they were generated until specified
     * instant.
     *
     * @param until Instant up to which the transactions should be generated.
     * @param out List that will be modified by adding the generated transactions.
     */
    public List<Integer> getNewTransactions(long until) {
        List<Integer> out = new LinkedList<>();
        if (until - lastInstant < 0) {
            SmfSnippetGenerator.log.error("going back by {}", until - lastInstant);
            return out;
            // throw new IllegalArgumentException("going back in time");
        }
        long deltaTime = until - lastInstant;
        long remainder = 0;
        while (deltaTime > remainder) {
            SmfSnippetGenerator.log.trace("delta {}", deltaTime);
            Distribution dist = distributions[distIndex];
            if (lastInDist + deltaTime <= dist.duration) {
                // we can fulfill whole period from single distribution
                remainder = dist.getEventIndexes(lastInDist, deltaTime, out);
                until -= remainder;
                lastInDist += deltaTime - remainder;
                break;
            }
            // use up rest of current distribution, loop to next
            long rest = dist.duration - lastInDist;
            remainder = dist.getEventIndexes(lastInDist, rest, out);
            deltaTime -= rest;
            distIndex++;
            // move the unused time to next distribution
            lastInDist = -remainder;
            until -= remainder;
            // after last wrap to beginning
            if (distIndex >= distributions.length) {
                distIndex = 0;
            }
            continue;
        }
        lastInstant = until;
        return out;
    }

    /**
     * Hint how long can the caller wait before he's likely to get next transaction. For >1k TPS
     * this returns 0.
     *
     * @return Suggested sleep in milliseconds.
     */
    public long getSleepHint() {
        assert distIndex >= 0 && distIndex < distributions.length;
        Distribution dist = distributions[distIndex];
        return dist.getSleepHint(lastInDist) / 1_000_000L;
    }
}