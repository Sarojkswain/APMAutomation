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

import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlow;
import com.ca.apm.automation.utils.file.FileModifierOperation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class TomcatDeployFlow extends DeployTomcatFlow {

    @FlowContext
    protected TomcatDeployFlowContext context;

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatDeployFlow.class);

    @Override
    public void run() throws Exception {
        File tomcatInstallDir = new File(this.context.getOriginalContext().getTomcatInstallDir());
        try {
            if (!this.context.getOriginalContext().isNoCleanUp()) {
                LOGGER.info("DeployTomcatFlow isNoCleanUp = false");
                this.clearTargetInstallationFolder(tomcatInstallDir);
            }

            this.deployTomcat(tomcatInstallDir);
            this.configure(tomcatInstallDir);
        } catch (IOException var3) {
            throw new IllegalStateException("Unable to deploy Apache Tomcat", var3);
        }
        LOGGER.info("Apache Tomcat has been deployed into {}", tomcatInstallDir);
        LOGGER.info("Flow has finished.");
    }

    protected void deployTomcat(File installDir) throws IOException {
        if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_UNIX) {
            throw new IllegalStateException("Unable to deployTomcat: OS not supported!");
        } else {
            File installerTgdirFile;
            if (SystemUtils.IS_OS_UNIX) {
                installerTgdirFile = new File("/opt/automation/deployed/tomcat");
            } else {
                installerTgdirFile = new File("C:\\automation\\deployed\\installers\\tomcat");
            }

            FileUtils.forceMkdir(installerTgdirFile);
            this.archiveFactory.createArchive(context.getOriginalContext().getTomcatBinariesArtifactURL()).unpack(installerTgdirFile);
            LOGGER.info("deployTomcat Creating folder {}", installDir.getAbsolutePath());
            if (context.getOriginalContext().isNoCleanUp()) {
                FileUtils.forceMkdir(installDir);
            }

            File unpackedDir = FileUtils.getFile(installerTgdirFile, context.getUnpackDir());
            LOGGER.info("deployTomcat Copying files from {} to {}", unpackedDir.getAbsolutePath(), installDir.getAbsolutePath());
            FileUtils.copyDirectory(unpackedDir, installDir);
            FileUtils.forceDelete(installerTgdirFile);
        }
    }

    protected void configureServerFile(File destinationFolder) {
        File configFolder = new File(destinationFolder, "conf");
        File serverConfigFile = new File(configFolder, "server.xml");
        String textTemplate = "Connector port=\"%s\"";
        String from = String.format(textTemplate, new Object[]{"8080"});
        String to = String.format(textTemplate, new Object[]{String.valueOf(context.getOriginalContext().getTomcatCatalinaPort())});
        this.fileOperationFactory.create(serverConfigFile).replace(Collections.singletonMap(from, to));
    }

    protected void createEnvVariableScript(File setEnvScriptFile) {
        HashSet envVariables = new HashSet(2);
        envVariables.add(this.formatEnvVariable("JAVA_HOME", context.getOriginalContext().getJdkHomeDir()));
        envVariables.add(this.formatEnvVariable("JRE_HOME", context.getOriginalContext().getJdkHomeDir()));
        this.fileOperationFactory.createByType(setEnvScriptFile, FileModifierOperation.CREATE).perform(envVariables);
        LOGGER.info("Created an environment variables script @ {}.", setEnvScriptFile);
    }

}
