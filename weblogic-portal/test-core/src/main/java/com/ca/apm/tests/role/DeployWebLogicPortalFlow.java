/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.role;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.apm.automation.action.responsefile.IResponseFile;
import com.ca.apm.automation.action.utils.monitor.TasFileWatchMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;


@Flow
public class DeployWebLogicPortalFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployWebLogicPortalFlow.class);
    @FlowContext
    private DeployWebLogicFlowContext context;

    @Override
    public void run() throws Exception {
        /**
         * From where do we fetch the installer
         */
        URL webLogicInstallUrl = context.getWebLogicInstallerUrl();
        /**
         * Where the installer gets stored
         */
        File webLogicInstallerDir = new File(context.getWebLogicInstallerDir());

        /**
         * The actual name of the installer file
         */
        File installerFile = new File(webLogicInstallerDir, context.getWebLogicInstallerFilename());

        LOGGER.info("Downloading WebLogic artefact");
        archiveFactory.createArtifact(webLogicInstallUrl).download(installerFile);
        installerFile.setExecutable(true);

        LOGGER.info("Building response file");
        //response file
        File responseFileDir = new File(context.getResponseFileDir());
        File installResponseFile = new File(responseFileDir, "weblogic_install_response_file.xml");
        IResponseFile oracleResponseFile = new OracleResponseFile(context.getInstallResponseFileData());
        oracleResponseFile.create(installResponseFile);

        LOGGER.info("Installing");
        //install & monitor (of course)
        File installationLogFile = new File(context.getInstallLogFile());
        runAndMonitorInstallationProcess(installerFile, installationLogFile, installResponseFile);
    }


    protected void runAndMonitorInstallationProcess(File installerFile, final File installationLogFile, File installResponseFile)
        throws Exception {

        String[] args = {"-mode=silent", "-silent_xml=" + installResponseFile.toPath().toAbsolutePath(),
                         "-log=" + installationLogFile.toPath()};

        Execution.Builder
            executionBuilder =
            context.isGenericJavaInstaller() ? genericJavaInstaller(installerFile.getPath()) : binaryInstaller(installerFile.getPath());
        executionBuilder.args(args);

        try (TasFileWatchMonitor watchMonitor = monitorFactory.createWatchMonitor()) {
            //setup monitoring
            watchMonitor.watchFileChanged(installationLogFile).watchFileCreated(installationLogFile).monitor();
            //execute
            int responseCode = executionBuilder.build().go();
            //evaluate
            switch (responseCode) {
                case 0:
                    LOGGER.info("Installation completed SUCCESSFULLY. Congrats!");
                    break;
                case -2:
                    LOGGER.info("Installation failed due to an internal XML parsing error.");
                    break;
                default:
                    throw new IllegalStateException(String.format("Installation failed due to a fatal error (%d)", responseCode));
            }
        }
    }

    protected Execution.Builder binaryInstaller(final String installerFile) {
        return new Execution.Builder(installerFile, LOGGER);
    }

    protected Execution.Builder genericJavaInstaller(final String installerFile) {
        Execution.Builder builder = new Execution.Builder("java", LOGGER);

        String[] args = new String[2];
        args[0] = "-jar";
        args[1] = installerFile;

        builder.args(args);

        return builder;
    }
}
