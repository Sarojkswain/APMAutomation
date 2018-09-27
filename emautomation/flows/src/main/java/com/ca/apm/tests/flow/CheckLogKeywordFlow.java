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
package com.ca.apm.tests.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;

/**
 * Flow that checks if some keywords appear in chosen logs.
 * Configuration in context
 * 
 * @author sobar03
 *
 */
public class CheckLogKeywordFlow implements IAutomationFlow {



    private static final Logger LOGGER = LoggerFactory.getLogger(RunCommandFlow.class);


    @FlowContext
    private CheckLogKeywordFlowContext context;

    public CheckLogKeywordFlow(CheckLogKeywordFlowContext context) {
        this.context = context;
    }

    public CheckLogKeywordFlow() {

    }

    /*
     * (non-Javadoc)
     * Will fail the test by throwing exception if keyword is not found
     * 
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {

        if (EmBatLocalUtils.isKeywordInLog(context.getLogPath(), context.getTextToMatch())) {
            LOGGER.info("Keyword found in log, everything's fine.");
        } else {
            throw new IllegalStateException("Required keyword not in log.");
        }
    }


}
