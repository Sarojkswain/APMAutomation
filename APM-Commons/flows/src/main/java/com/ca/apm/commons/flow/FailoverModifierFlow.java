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
package com.ca.apm.commons.flow;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.commons.coda.common.ApmbaseUtil;

/**
 * Runs the given commands in remote machine and displays output to console
 */
@Flow
public class FailoverModifierFlow extends FlowBase {
    @FlowContext
    private FailoverModifierFlowContext context;

    private static final Logger LOGGER = LoggerFactory.getLogger(FailoverModifierFlow.class);

    @Override
    public void run() throws Exception {
        LOGGER.info("Display the command :{} here {}", context.getcommand(),
            context.getdir());

        sharedDrive();

        LOGGER.info("Task completed.");
    }


    protected void sharedDrive() {
        try {

            List<String> stringsTobeVerified = new ArrayList<String>();

            stringsTobeVerified.add("The command completed successfully.");

            ApmbaseUtil.invokeProcessBuilder(context.getcommand());
        } catch (Exception e) {
            LOGGER.equals("Unable to Execute the givne command ");
        }


    }
}
