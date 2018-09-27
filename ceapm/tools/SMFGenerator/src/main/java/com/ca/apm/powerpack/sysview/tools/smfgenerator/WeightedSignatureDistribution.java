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

import java.util.stream.IntStream;

/**
 * Weighted distribution of N unique transaction signatures. Initial M signatures use specified
 * probability, and remaining signatures have equal probability among themselves.
 */
class WeightedSignatureDistribution implements ItemDistribution {
    /** Total number of transaction signatures (N). */
    final int uniqs;
    /** First M signatures with specified probability. */
    final int[] ppm;
    /** Sum of probabilities for signatures with explicitly specified probability. */
    final int specialPpmSum;
    /** 100% in ppm */
    static final int ALL = 1_000_000;

    /**
     * Constructor.
     *
     * @param total total number of unique transaction signatures.
     * @param ppms (optional) Probabilities of exceptional signatures specified in parts per
     *        million [ppm]
     */
    WeightedSignatureDistribution(int total, int... ppms) {
        uniqs = total;
        ppm = ppms;
        if (ppm.length > uniqs) {
            throw new IllegalArgumentException(
                "there are more exceptional transactions than total");
        }
        specialPpmSum = IntStream.of(ppm).sum();
        if (specialPpmSum > ALL) {
            throw new IllegalArgumentException(
                "exceptional transactions are more than 100% likely to occur");
        }
        if (ppm.length == uniqs && specialPpmSum != ALL) {
            throw new IllegalArgumentException(
                "ppm for each signature is specified but they don't add to 100%");
        }
    }

    @Override
    public int getRandomIndex() {
        int random = SmfSnippetGenerator.masterRand.nextInt(ALL);
        // this hit something in the "equally probable" range
        if (random >= specialPpmSum) {
            // integer is too small for the calculation
            return (int) (ppm.length + (random - specialPpmSum) * (long) (uniqs - ppm.length)
                / (ALL - specialPpmSum));
        }
        // this hit some of the "odd probabilities"
        int sum = 0;
        for (int i = 0; i < ppm.length; i++) {
            sum += ppm[i];
            if (random < sum) {
                return i;
            }
        }
        // should be unreachable
        throw new IllegalStateException("matching index not found for " + random);
    }

    @Override
    public int getMaxIndex() {
        return uniqs - 1;
    }
}