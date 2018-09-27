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
package com.ca.apm.tests.flow.agent;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * StopNetAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class StopNetAgentFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopNetAgentFlow.class);

    public StopNetAgentFlow() {
    }

    public void run() throws IOException {
        try {
            this.stopAgent();
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
    }

    protected void stopAgent() throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, "net")
                .args(new String[]{"stop", "CA APM PerfMon Collector Service"}).build().go();
//        switch (responseCode) { // todo ignoring for now - resolve response codes http://ss64.com/nt/net_service.html
//            case 0:
//                LOGGER.info("NET Execution completed SUCCESSFULLY! Congratulations!");
//                return;
//            default:
//                throw new IllegalStateException(String.format("NET Execution failed (%d)", new Object[]{responseCode}));
//        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
