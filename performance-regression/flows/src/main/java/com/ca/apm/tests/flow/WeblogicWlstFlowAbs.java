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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic Flow for running a WLST Command in Oracle DB
 * <p/>
 * Method prepareAndExecuteWlst() has to be implemented for actual running of a specific script from the archive.
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public abstract class WeblogicWlstFlowAbs extends FlowBase {

    public static final String DEFAULT_WLS_MODULES_PATH = "C:\\AUTOMA~1\\deployed\\Oracle\\install\\modules\\features\\weblogic.server.modules_10.3.6.0.jar";

    private static final Logger LOGGER = LoggerFactory.getLogger(WeblogicWlstFlowAbs.class);
    @FlowContext
    private WeblogicWlstFlowContext context;

    public WeblogicWlstFlowAbs() {
    }

    public void run() throws IOException {
        prepareAndExecuteWlst();
    }

    public abstract void prepareAndExecuteWlst() throws IOException;

    @NotNull
    protected File createFilePath(String fileDirName, String fileName) {
        File file;
        if (fileDirName != null) {
            File fileDir = FileUtils.getFile(fileDirName);
            file = new File(fileDir, fileName);
        } else {
            file = new File(fileName);
        }
        if (file.exists() && file.canRead()) {
            LOGGER.info("Installation script file located at: {}", file);
            return file;
        } else {
            throw new IllegalStateException("Installation script file(\'" + file.getAbsolutePath() +
                    "\') is either missing or can\'t be read.");
        }
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
        int responseCode = this.getExecutionBuilder(LOGGER, this.context.getWeblogicInstallPath() + WeblogicWlstFlowContext.WLST_PATH_REL)
                .args(new String[]{scriptFile.toString()}).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Script Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Script Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected void deployLibrary(File archiveFile, int wlsPort, String wlsUser,
                                 String wlsPassword) throws InterruptedException {
        deploy(archiveFile, null, true, wlsPort, wlsUser, wlsPassword);
    }

    protected void deployApplication(File archiveFile, String appName, int wlsPort, String wlsUser,
                                     String wlsPassword) throws InterruptedException {
        deploy(archiveFile, appName, false, wlsPort, wlsUser, wlsPassword);
    }

    private void deploy(File archiveFile, String appName, boolean asLibrary, int wlsPort, String wlsUser,
                        String wlsPassword) throws InterruptedException {
        // Deploy arguments
        String[] args = new String[]{"weblogic.Deployer", "-adminurl", "t3://localhost:" + wlsPort, "-user", wlsUser,
                "-password", wlsPassword, "-deploy", appName != null ? "-name" : "", appName != null ? appName : "",
                asLibrary ? "-library" : "", archiveFile.toString()};
        //
        // Classpath definition // TODO parametrize
        Map<String, String> environment = new HashMap<>();
        environment.put("CLASSPATH", this.context.getWeblogicInstallPath() + WeblogicWlstFlowContext.WEBLOGIC_JAR_PATH_REL +
                ";" + DEFAULT_WLS_MODULES_PATH + ";");
        //
        int responseCode = this.getExecutionBuilder(LOGGER, "java")
                .args(args).environment(environment).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Deploy Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Deploy Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
