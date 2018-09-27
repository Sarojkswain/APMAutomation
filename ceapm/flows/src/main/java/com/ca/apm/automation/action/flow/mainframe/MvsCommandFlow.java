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

package com.ca.apm.automation.action.flow.mainframe;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.mainframe.Mvs;

/**
 * Flow that allows execution of a MVS command on a z/OS system.
 */
@Flow
public class MvsCommandFlow implements IAutomationFlow {
    private static final Logger logger = LoggerFactory.getLogger(MvsCommandFlow.class);

    @FlowContext
    private MvsCommandFlowContext context;

    /**
     * Executes the flow - runs the configured MVS command.
     */
    @Override
    public void run() throws Exception {
        logger.info("Running the '" + context.getCommand() + "' MVS command");
        try (Mvs mvs = new Mvs()) {
            int rc = mvs.execute(context.getCommand());
            if (rc != 0) {
                try (BufferedReader output =
                    new BufferedReader(new InputStreamReader(mvs.getStdoutStream()))) {
                    String line;
                    while ((line = output.readLine()) != null) {
                        logger.error(line);
                    }
                }
                throw new IllegalStateException("Failed to execute the '" + context.getCommand()
                    + "' MVS command, rc=" + String.valueOf(rc));
            }
        }
    }
}
