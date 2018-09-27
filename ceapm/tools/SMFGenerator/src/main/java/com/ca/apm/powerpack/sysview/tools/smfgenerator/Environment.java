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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.ca.apm.powerpack.sysview.tools.smfgenerator.SmfSnippet.Type;

/**
 * Set of unique signatures representing nodes that are present in the "customer environment".
 */
public class Environment {
    final ArrayList<Signature> uniqs;

    /**
     * Environment constructor.
     *
     * @param cics Number of CICS nodes.
     * @param ims Number of IMS nodes.
     * @param db2 Number of DB2 nodes.
     * @param seed RNG seed for consistent randomness.
     */
    public Environment(final int cics, final int ims, final int db2, long seed) {
        int uniqNum = cics + ims;
        int imsCount = 0;
        int cicsCount = 0;

        uniqs = new ArrayList<>(uniqNum);
        Random rand = new Random(seed);

        for (int i = 0; i < uniqNum; i++) {
            // Generate subsystem type randomly so that eventually their counts will match.
            final SmfSnippet.Type type;
            if (rand.nextInt(uniqNum - imsCount - cicsCount) < cics - cicsCount) {
                type = Type.CICS;
                cicsCount++;
            } else {
                type = Type.IMS;
                imsCount++;
            }

            /*
             * Generated jobname is starting with type, then padding characters so that maximal
             * jobname length is reached for longest index, then index without padding.
             * This way shorter than maximal length identifiers are also sometimes produced.
             */
            //int prefixlen = MAX_JOB_NAME - Integer.toString(uniqNum - 1).length();
            int prefixlen = (type == Type.CICS ? SmfSnippetGenerator.MAX_JOB_NAME : 4) - 1;
            //String jobname = StringUtils.rightPad(type.toString(), prefixlen, "_") + i;
            String jobname = type.toString().substring(0,  1) + StringUtils.leftPad(Integer.toString(i), prefixlen, "0");
            // ~half of CICS nodes are connected to single DB2
            String db2name =
                (type == Type.CICS ? rand.nextBoolean() ? String.format("D%03d",
                    rand.nextInt(db2)) : null : null);
            uniqs.add(new Signature(type, jobname, db2name, rand));
        }
    }

    /**
     * Create transaction snippet for given node. Represents customer transaction that ran on
     * given node.
     *
     * @param i
     * @return
     */
    public SmfSnippet generateTransactionSnip(int i) {
        SmfSnip snip = uniqs.get(i).createSnip();
        return snip;
    }

    /**
     * Returns number of nodes.
     *
     * @return Total number of nodes.
     */
    public int getCount() {
        return uniqs.size();
    }

    public Collection<Signature> getSignatures() {
        return uniqs;
    }
}