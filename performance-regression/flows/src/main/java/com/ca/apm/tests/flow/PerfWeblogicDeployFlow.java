package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.webapp.weblogic.DeployWebLogicFlow;
import com.ca.apm.automation.action.flow.webapp.weblogic.DeployWebLogicFlowContext;
import com.ca.apm.automation.action.responsefile.OracleResponseFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@Deprecated
public class PerfWeblogicDeployFlow extends DeployWebLogicFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerfWeblogicDeployFlow.class);
    @FlowContext
    private DeployWebLogicFlowContext context;

    public void run() throws Exception {
        URL webLogicInstallUrl = this.context.getWebLogicInstallerUrl();
        File webLogicInstallerDir = new File(this.context.getWebLogicInstallerDir());
        File installerFile = new File(webLogicInstallerDir, this.context.getWebLogicInstallerFilename());
        if (!installerFile.exists()) {
            LOGGER.info("Downloading WebLogic artefact");
            this.archiveFactory.createArtifact(webLogicInstallUrl).download(installerFile);
        } else {
            LOGGER.info("Weblogic installation files already exist in '" + installerFile.getAbsolutePath() + "'. Skipping download.");
        }
        installerFile.setExecutable(true);
        LOGGER.info("Building response file");
        File responseFileDir = new File(this.context.getResponseFileDir());
        File installResponseFile = new File(responseFileDir, "weblogic_install_response_file.xml");
        OracleResponseFile oracleResponseFile = new OracleResponseFile(this.context.getInstallResponseFileData());
        oracleResponseFile.create(installResponseFile);
        LOGGER.info("Installing");
        File installationLogFile = new File(this.context.getInstallLogFile());
        this.runAndMonitorInstallationProcess(installerFile, installationLogFile, installResponseFile);
    }
}
