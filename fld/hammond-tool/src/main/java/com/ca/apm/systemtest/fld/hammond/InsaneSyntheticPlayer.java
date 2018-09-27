/*
 * Copyright (c) 2016 CA. All rights reserved.
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
package com.ca.apm.systemtest.fld.hammond;

import com.wily.introscope.em.internal.Activator;
import com.wily.introscope.util.Log;

public class InsaneSyntheticPlayer {

    InsaneSyntheticAgentOrchestrator agentOrchestrator;

    public InsaneSyntheticPlayer() {
        agentOrchestrator = new InsaneSyntheticAgentOrchestrator();
    }

    public void setupPlayback(int agents, int offset) throws Exception {
        agentOrchestrator.setCollectorHost("localhost");
        agentOrchestrator.createAgents(agents, offset);
    }

    public boolean startPlayback() {
        return agentOrchestrator.startPlayback();
    }

    public static void main(String[] args) throws Exception {
        new Activator();
        Log.out = Configuration.instance().createFeedback("Synthetic Player");

        final InsaneSyntheticPlayer sp = new InsaneSyntheticPlayer();
        if (args.length > 1) {
            sp.setupPlayback(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } else {
            sp.setupPlayback(1, 1);
        }
        if (sp.startPlayback()) {
            if (args.length > 2) {
                Thread.sleep(Integer.parseInt(args[2]));
            } else {
                Thread.sleep(Long.MAX_VALUE);
            }
        }
        Runtime.getRuntime().exit(0);
    }

}
