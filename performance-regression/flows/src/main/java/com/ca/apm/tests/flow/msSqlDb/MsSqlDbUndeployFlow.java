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
package com.ca.apm.tests.flow.msSqlDb;

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
 * UndeployMsSqlDbFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class MsSqlDbUndeployFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MsSqlDbUndeployFlow.class);
    @FlowContext
    private MsSqlDbUndeployFlowContext context;

    public MsSqlDbUndeployFlow() {
    }

    public void run() throws IOException {
        try {
            this.runUninstallationProcess();
            rmDir();
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void runUninstallationProcess() throws InterruptedException {
        File installExecutableFilePath = FileUtils.getFile(this.context.getInstallSourcesLocation(),
                this.context.getUnpackDirName(), this.context.getInstallerFileName());
        File responseFile = FileUtils.getFile(this.context.getInstallSourcesLocation(), this.context.getUnpackDirName(),
                this.context.getResponseFileName());
        File dataDir = FileUtils.getFile(this.context.getInstallPath(), "MSSQL10.MSSQLSERVER/MSSQL/DATA");
        if (installExecutableFilePath.exists() && responseFile.exists() && dataDir.exists()) {
            int responseCode = this.getExecutionBuilder(LOGGER, installExecutableFilePath.toString())
                    .args(new String[]{"/ConfigurationFile=\"" + responseFile.toString() + "\""})
                    .build().go();
            switch (responseCode) {
                case 0:
                    LOGGER.info("MSSQL DB uninstallation completed SUCCESSFULLY! Congratulations!");
                    return;
                default:
                    throw new IllegalStateException(String.format("Launching silent uninstallation of MSSQL DB failed (%d)", new Object[]{responseCode}));
            }
        } else {
            LOGGER.info("DB Installer or response file does not exist, or the DB has been already uninstalled. Skipping MSSQL DB uninstallation.");
        }
    }

    protected void rmDir() throws InterruptedException, IOException {
        if (this.context.isDeleteSources()) {
            LOGGER.info("Deleting sources at '" + this.context.getInstallSourcesLocation() + "'");
            File installSourcesDir = FileUtils.getFile(this.context.getInstallSourcesLocation());
            if (installSourcesDir.exists()) {
                FileUtils.deleteDirectory(installSourcesDir);
            }
        }
        LOGGER.info("Deleting installation dir at '" + this.context.getInstallPath() + "'");
        File installDir = FileUtils.getFile(this.context.getInstallPath());
        if (installDir.exists()) {
            FileUtils.deleteDirectory(installDir);
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
