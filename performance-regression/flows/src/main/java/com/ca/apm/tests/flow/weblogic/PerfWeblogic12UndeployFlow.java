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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class PerfWeblogic12UndeployFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerfWeblogic12UndeployFlow.class);
    @FlowContext
    private PerfWeblogicUndeployFlowContext context;

    public PerfWeblogic12UndeployFlow() {
    }

    protected File createSilentUninstallFile(File sourcesLocation, File beaHome) {
        File outputFile = FileUtils.getFile(sourcesLocation, "uninstall.rsp");
        Path outputPath = Paths.get(outputFile.getAbsolutePath());
        try {
            Files.write(outputPath, "[ENGINE]\n".getBytes(), TRUNCATE_EXISTING, WRITE, CREATE);
            Files.write(outputPath, "Response File Version=1.0.0.0.0\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "[GENERIC]\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "SELECTED_DISTRIBUTION=WebLogic Server~12.1.3.0.0\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, ("ORACLE_HOME=" + beaHome + "\n").getBytes(), WRITE, APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    public void run() throws IOException {
        try {
            File beaHome = FileUtils.getFile(context.getParentDir());
            File sourcesLocation = FileUtils.getFile(context.getSourcesLocation());
            File uninstallResponseFile = createSilentUninstallFile(sourcesLocation, beaHome);
            // Uninstall
            uninstallWeblogic(uninstallResponseFile);
            rmDir();
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void uninstallWeblogic(File uninstallResponseFile) throws InterruptedException, IOException {
        File uninstall = FileUtils.getFile(this.context.getParentDir(), "oui/bin/deinstall.cmd");
        if (uninstall.exists()) {
            int responseCode = this.getExecutionBuilder(LOGGER, uninstall.getAbsolutePath())
                    .args(new String[]{"-silent", "-nowait", "-responseFile", uninstallResponseFile.getAbsolutePath()}).build().go();
            switch (responseCode) {
                case 0:
                    LOGGER.info("Weblogic uninstallation completed SUCCESSFULLY! Congratulations!");
                    return;
                default:
                    throw new IllegalStateException(String.format("Weblogic uninstallation failed (%d)", new Object[]{responseCode}));
            }
        } else {
            LOGGER.info("Uninstall batch script '" + uninstall + "'does not exist. Skipping WLS uninstallation.");
        }
    }

    protected void rmDir() throws InterruptedException, IOException {
        File installDir = FileUtils.getFile(this.context.getInstallDir());
        if (installDir.exists()) {
            FileUtils.deleteDirectory(installDir);
            LOGGER.info("Directory '" + installDir.getAbsolutePath() + "' deleted.");
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
