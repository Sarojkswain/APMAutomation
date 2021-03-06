/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;
import com.ca.apm.tests.utils.emutils.EmConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stops EM
 */
@Flow
public class StopEmFlow implements IAutomationFlow {

    private static final Logger log = LoggerFactory.getLogger(StopEmFlow.class);

    @FlowContext
    private StopEmFlowContext ctx;

    @Override
    public void run() throws Exception {

        log.info("About to stop EM from folder " + ctx.getInstallDir() + " port " + ctx.getPort());

        final EmConfiguration emc = new EmConfiguration(ctx.getInstallDir(), ctx.getPort());
        EmBatLocalUtils.stopLocalEm(emc);

        log.info("Flow has finished.");
    }
}
