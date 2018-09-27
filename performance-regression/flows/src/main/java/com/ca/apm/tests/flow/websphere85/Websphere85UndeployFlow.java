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
package com.ca.apm.tests.flow.websphere85;

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
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class Websphere85UndeployFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(Websphere85UndeployFlow.class);
    @FlowContext
    private Websphere85UndeployFlowContext context;

    public Websphere85UndeployFlow() {
    }

    public void run() throws IOException {
        try {
            uninstallWebsphereAndJava();
            rmDir();
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void uninstallWebsphereAndJava() throws InterruptedException, IOException {
        File imcl = FileUtils.getFile(this.context.getInstallManagerDir(), "eclipse/tools/imcl.exe");
        File installationDir = FileUtils.getFile(this.context.getInstallLocation());
        if (imcl.exists() && installationDir.exists()) {
            int responseCode = this.getExecutionBuilder(LOGGER, imcl.getAbsolutePath())
                    .args(new String[]{"uninstallAll", "-installationDirectory", installationDir.getAbsolutePath()}).build().go();
            switch (responseCode) {
                case 0:
                    LOGGER.info("WAS Uninstallation completed SUCCESSFULLY! Congratulations!");
                    return;
                default:
                    throw new IllegalStateException(String.format("WAS Uninstallation failed (%d)", new Object[]{responseCode}));
            }
        } else {
            LOGGER.info("Install manager does not exist. Skipping WAS uninstallation.");
        }
    }

    protected void rmDir() throws InterruptedException, IOException {
        File installDir = FileUtils.getFile(this.context.getInstallLocation());
        if (installDir.exists()) {
            FileUtils.deleteDirectory(installDir);
        } else {
            LOGGER.info("Directory '" + installDir.getAbsolutePath() + "' does not exist. Skipping deletion.");
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
