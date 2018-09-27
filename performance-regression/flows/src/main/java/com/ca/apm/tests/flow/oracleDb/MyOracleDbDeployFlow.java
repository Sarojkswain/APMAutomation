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
package com.ca.apm.tests.flow.oracleDb;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.apm.automation.action.flow.oracle.DeployOracleDbFlow;
import com.ca.apm.automation.action.flow.oracle.DeployOracleDbFlowContext;
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
 * Copied from com.ca.apm.automation.action.flow.oracle.DeployOracleDbFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class MyOracleDbDeployFlow extends FlowBase {
    public static final String DEFAULT_PARENT_DIR_NAME = "database";
    public static final String DEFAULT_RESPONSE_DIR_NAME = "response";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployOracleDbFlow.class);
    @FlowContext
    private DeployOracleDbFlowContext context;

    public MyOracleDbDeployFlow() {
    }

    public void run() throws IOException {
        File installSources = FileUtils.getFile(this.context.getInstallSourcesLocation());
        if (!installSources.exists()) {
            this.archiveFactory.createArchive(this.context.getInstallPackageUrl()).unpack(installSources);
        } else {
            LOGGER.info("Oracle DB installation files already exist in '" + this.context.getInstallSourcesLocation() + "'. Skipping download.");
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

    protected void prepareResponseFile(File installResponseFile) {
        LOGGER.info("Preparing response file");
        File installResponseFilePath = installResponseFile.getAbsoluteFile();
        FileOperation replaceable = this.fileOperationFactory.createReplaceable(installResponseFilePath);
        VarSubstitutionFilter varSubstitutionFilter = VarSubstitutionFilter.withCharsetAndPlaceholder(this.context.getEncoding(), "\\[%s\\]");
        varSubstitutionFilter.add(this.context.getResponseFileOptions());
        replaceable.perform(Collections.singleton(varSubstitutionFilter));
    }

    @NotNull
    protected File createResponseFilePath() throws IOException {
        File responseFileDir = this.context.getResponseFileDir() == null ? FileUtils.getFile(new String[]{this.context.getInstallSourcesLocation(), DEFAULT_PARENT_DIR_NAME, DEFAULT_RESPONSE_DIR_NAME}) : new File(this.context.getResponseFileDir());
        File installResponseFile = new File(responseFileDir, this.context.getResponseFileName());
        FileUtils.copyFileToDirectory(installResponseFile, FileUtils.getTempDirectory());
        File installResponseFileTmp = FileUtils.getFile(FileUtils.getTempDirectory(), this.context.getResponseFileName());
        if (installResponseFileTmp.exists() && installResponseFile.exists() && installResponseFile.canRead()) {
            LOGGER.info("Installation response file located at: {}", installResponseFileTmp);
            return installResponseFileTmp;
        } else {
            throw new IllegalStateException("Installation response file '" + installResponseFile.getAbsolutePath() + "' or '"
                    + installResponseFileTmp.getAbsolutePath() + "' are either missing or can\'t be read.");
        }
    }

    protected void runInstallationProcess(File installResponseFile) throws InterruptedException {
        File installExecutableDirectory = FileUtils.getFile(new String[]{this.context.getInstallSourcesLocation(), DEFAULT_PARENT_DIR_NAME, "setup.exe"});
        int responseCode = this.getExecutionBuilder(LOGGER, installExecutableDirectory.toString()).args(new String[]{"-silent", "-force", "-waitforcompletion", "-nowait", "-ignorePrereq", "-ignoreSysPrereqs", "-responseFile", installResponseFile.toString()}).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Oracle DB installation completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Launching silent installation of Oracle DB failed (%d)", new Object[]{Integer.valueOf(responseCode)}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}