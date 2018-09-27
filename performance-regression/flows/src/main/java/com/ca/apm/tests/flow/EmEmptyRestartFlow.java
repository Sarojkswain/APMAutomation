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
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * EmEmptyRestartFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class EmEmptyRestartFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmEmptyRestartFlow.class);
    @FlowContext
    private EmEmptyDeployFlowContext context;

    public static final String EM_WV_PROCESS_IDENT = "launcher.jar";

    public void run() throws IOException {
        File startEmFile = FileUtils.getFile(this.context.getInstallLocation() + "/Introscope_Enterprise_Manager.exe");
        File startWvFile = FileUtils.getFile(this.context.getInstallLocation() + "/Introscope_WebView.exe");
        try {
            // Kill EM + WV
            LOGGER.info("Shutting down Enterprise Manager and WebView");
            this.getExecutionBuilder(LOGGER, "wmic")
                    .args(new String[]{"PROCESS", "where", "name like '%java%' and CommandLine like '%" +
                            EM_WV_PROCESS_IDENT + "%'", "Call", "Terminate"}).build().go();
            // Start EM + WV
            LOGGER.info("Starting Enterprise Manager");
            int responseCode = this.getExecutionBuilder(LOGGER, startEmFile.getAbsolutePath())
                    .textToMatch("Introscope Enterprise Manager started").build().go();
            if (responseCode == 0) {
                LOGGER.info("Introscope Enterprise Manager started SUCCESSFULLY! Congratulations!");
            } else {
                throw new IllegalStateException(String.format("Sarting of Introscope Enterprise Manager failed (%d)", new Object[]{responseCode}));
            }
            LOGGER.info("Starting WebView");
            responseCode = this.getExecutionBuilder(LOGGER, startWvFile.getAbsolutePath())
                    .textToMatch("Web Application Server started").build().go();
            if (responseCode == 0) {
                LOGGER.info("WebView started SUCCESSFULLY! Congratulations!");
            } else {
                throw new IllegalStateException(String.format("Sarting of WebView failed (%d)", new Object[]{responseCode}));
            }
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
