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
import com.ca.apm.automation.action.flow.utility.VarSubstitutionFilter;
import com.ca.apm.automation.utils.file.FileOperation;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * OracleTradeDbScriptFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class Trade6Flow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(Trade6Flow.class);
    @FlowContext
    private Trade6FlowContext context;

    public void run() throws IOException {
        this.archiveFactory.createArchive(this.context.getDeployPackageUrl()).unpack(new File(this.context.getDeploySourcesLocation()));

        String sources = this.context.getDeploySourcesLocation();
        String destination = this.context.getWebsphereProfileBinPath();

        File applicationEarFile = FileUtils.getFile(sources, this.context.getApplicationEarFileName());
        File applicationEarDestFile = FileUtils.getFile(destination, this.context.getApplicationEarFileName());

        File installScriptFile = FileUtils.getFile(sources, this.context.getInstallScriptFileName());
        File installScriptDestFile = FileUtils.getFile(destination, this.context.getInstallScriptFileName());

        File resourcesScriptFile = FileUtils.getFile(sources, this.context.getResourcesScriptFileName());
        File resourcesScriptDestFile = FileUtils.getFile(destination, this.context.getResourcesScriptFileName());

        File ojdbcFile = FileUtils.getFile(this.context.getOjdbcPath());
        File ojdbcDestFile = FileUtils.getFile(this.context.getWebsphereInstallPath(), "lib", ojdbcFile.getName());
        this.context.getPropertiesFileOptions().put("OJDBC_PATH", ojdbcDestFile.toString().replaceAll("\\\\", "/"));

        try {
            // PREPARE SCRIPT FILES
            this.prepareScriptFile(installScriptFile, StandardCharsets.UTF_8.name(), this.context.getPropertiesFileOptions());
            this.prepareScriptFile(resourcesScriptFile, StandardCharsets.UTF_8.name(), this.context.getPropertiesFileOptions());
            // COPY FILES
            FileUtils.copyFile(applicationEarFile, applicationEarDestFile);
            FileUtils.copyFile(installScriptFile, installScriptDestFile);
            FileUtils.copyFile(resourcesScriptFile, resourcesScriptDestFile);
            FileUtils.copyFile(ojdbcFile, ojdbcDestFile);
            // INSTALL WEBAPP
            this.runScript(installScriptFile);
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void prepareScriptFile(File scriptFile, String scriptFileEncoding, Map<String, String> scriptFileOptions) {
        LOGGER.info("Preparing response file");
        File installResponseFilePath = scriptFile.getAbsoluteFile();
        FileOperation replaceable = this.fileOperationFactory.createReplaceable(installResponseFilePath);
        VarSubstitutionFilter varSubstitutionFilter = VarSubstitutionFilter
                .withCharsetAndPlaceholder(scriptFileEncoding, "\\[%s\\]");
        varSubstitutionFilter.add(scriptFileOptions);
        replaceable.perform(Collections.singleton(varSubstitutionFilter));
    }

    protected void runScript(File scriptFile) throws InterruptedException {
        File wasBinDir = FileUtils.getFile(this.context.getWebsphereProfileBinPath());
        int responseCode = this.getExecutionBuilder(LOGGER, "wsadmin.bat").workDir(wasBinDir)
                .args(new String[]{"-f", scriptFile.toString(), "all"}).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Script Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Script Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
