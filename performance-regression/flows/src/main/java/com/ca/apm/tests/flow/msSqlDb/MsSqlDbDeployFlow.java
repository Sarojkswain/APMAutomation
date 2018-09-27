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
import com.ca.apm.automation.action.flow.utility.VarSubstitutionFilter;
import com.ca.apm.automation.utils.file.FileOperation;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * DeployMsSqlDbFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class MsSqlDbDeployFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MsSqlDbDeployFlow.class);
    @FlowContext
    private MsSqlDbDeployFlowContext context;

    public MsSqlDbDeployFlow() {
    }

    public void run() throws IOException {
        File installSourcesDir = FileUtils.getFile(this.context.getInstallSourcesLocation());
        if (!installSourcesDir.exists()) {
            this.archiveFactory.createArchive(this.context.getInstallPackageUrl()).unpack(new File(this.context.getInstallSourcesLocation()));
        } else {
            LOGGER.info("MSSQL DB installation files already exist in '" + this.context.getInstallSourcesLocation() + "'. Skipping download.");
        }
        File installResponseFile = this.createResponseFilePath();
        this.prepareResponseFile(installResponseFile);

        try {
            this.runInstallationProcess(installResponseFile);
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    @NotNull
    protected File createResponseFilePath() throws IOException {
        File installResponseFile = FileUtils.getFile(this.context.getInstallSourcesLocation(), this.context.getUnpackDirName(), this.context.getResponseFileName());
        FileUtils.copyFileToDirectory(installResponseFile, FileUtils.getTempDirectory());
        File installResponseFileTmp = FileUtils.getFile(FileUtils.getTempDirectory(), this.context.getResponseFileName());
        if (installResponseFile.exists() && installResponseFileTmp.exists() && installResponseFileTmp.canRead()) {
            LOGGER.info("Installation response file located at: {}", installResponseFileTmp);
            return installResponseFileTmp;
        } else {
            throw new IllegalStateException("Installation response file '" + installResponseFile.getAbsolutePath() + "' or '"
                    + installResponseFileTmp.getAbsolutePath() + "' are either missing or can\'t be read.");
        }
    }

    protected void prepareResponseFile(File installResponseFile) {
        LOGGER.info("Preparing response file");
        File installResponseFilePath = installResponseFile.getAbsoluteFile();
        FileOperation replaceable = this.fileOperationFactory.createReplaceable(installResponseFilePath);
        VarSubstitutionFilter varSubstitutionFilter = VarSubstitutionFilter.withCharsetAndPlaceholder(this.context.getEncoding(), "\\[%s\\]");
        varSubstitutionFilter.add(this.context.getResponseFileOptions());
        replaceable.perform(Collections.singleton(varSubstitutionFilter));
    }

    protected void runInstallationProcess(File installResponseFile) throws InterruptedException {
        File installExecutableDirectory = FileUtils.getFile(this.context.getInstallSourcesLocation(),
                this.context.getUnpackDirName(), this.context.getInstallerFileName());
        int responseCode = this.getExecutionBuilder(LOGGER, installExecutableDirectory.toString())
                .args(new String[]{"/ConfigurationFile=\"" + installResponseFile.toString() + "\""})
                .build()
                .go();
        switch (responseCode) {
            case 0:
                LOGGER.info("MSSQL DB installation completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Launching silent installation of MSSQL DB failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
