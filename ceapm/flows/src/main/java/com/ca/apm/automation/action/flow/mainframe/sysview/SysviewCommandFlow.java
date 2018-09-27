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

package com.ca.apm.automation.action.flow.mainframe.sysview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.Rc;

/**
 * Flow that allows execution of SYSVIEW command.
 *
 * <p>Through the context ({@link SysviewCommandFlowContext}) you can specify
 * against which SYSVIEW the command should be executed, and what return values
 * are acceptable.</p>
 */
@Flow
public class SysviewCommandFlow implements IAutomationFlow {
    private static final Logger logger = LoggerFactory.getLogger(SysviewCommandFlow.class);

    @FlowContext
    private SysviewCommandFlowContext context;

    /**
     * Executes the flow - runs the configured SYSVIEW command.
     */
    @Override
    public void run() throws Exception {
        assert context.getCommand() != null && !context.getCommand().isEmpty();
        assert context.getAcceptableRCs().size() > 0;

        try (Sysview sysv = new Sysview(context.getLoadlib())) {
            logger.info("Running the '" + context.getCommand() + "' SYSVIEW command");

            Rc rc = sysv.execute(context.getCommand()).getRc();

            if (!context.getAcceptableRCs().contains(rc)) {
                throw new IllegalStateException("Failed to execute SYSVIEW command '"
                    + context.getCommand() + "', rc=" + rc);
            }
        }
    }
}
