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
package com.ca.apm.tests.flow.weblogic;

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
public class PerfWeblogicUndeployFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerfWeblogicUndeployFlow.class);
    @FlowContext
    private PerfWeblogicUndeployFlowContext context;

    public PerfWeblogicUndeployFlow() {
    }

    public void run() throws IOException {
        try {
            uninstallWeblogic();
            rmDir();
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void uninstallWeblogic() throws InterruptedException, IOException {
        File uninstall = FileUtils.getFile(this.context.getInstallDir(), "utils/uninstall/uninstall.cmd");
        if (uninstall.exists()) {
            int responseCode = this.getExecutionBuilder(LOGGER, uninstall.getAbsolutePath())
                    .args(new String[]{"-mode=silent"}).build().go();
            switch (responseCode) {
                case 0:
                    LOGGER.info("Weblogic uninstallation completed SUCCESSFULLY! Congratulations!");
                    return;
                default:
                    throw new IllegalStateException(String.format("Weblogic uninstallation failed (%d)", new Object[]{responseCode}));
            }
        } else {
            LOGGER.info("Uninstall batch script does not exist. Skipping WLS uninstallation.");
        }
    }

    protected void rmDir() throws InterruptedException, IOException {
        File installDir = FileUtils.getFile(this.context.getInstallDir());
        if (installDir.exists()) {
            FileUtils.deleteDirectory(installDir);
            LOGGER.info("Directory '" + installDir + "' deleted.");
        } else {
            LOGGER.info("Directory '" + installDir.getAbsolutePath() + "' does not exist. Skipping deletion.");
        }
        File parentDir = FileUtils.getFile(this.context.getParentDir());
        if (parentDir.exists()) {
            FileUtils.deleteDirectory(parentDir);
            LOGGER.info("Directory '" + parentDir + "' deleted.");
        } else {
            LOGGER.info("Directory '" + parentDir.getAbsolutePath() + "' does not exist. Skipping deletion.");
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
