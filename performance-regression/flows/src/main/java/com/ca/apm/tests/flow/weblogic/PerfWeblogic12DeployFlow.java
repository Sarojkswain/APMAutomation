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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class PerfWeblogic12DeployFlow extends PerfWeblogicDeployFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerfWeblogic12DeployFlow.class);
    @FlowContext
    private PerfWeblogicDeployFlowContext context;

    protected File createSilentInstallFile(File sourcesLocation, File beaHome) {
        File outputFile = FileUtils.getFile(sourcesLocation, "install.rsp");
        Path outputPath = Paths.get(outputFile.getAbsolutePath());
        try {
            Files.write(outputPath, "[ENGINE]\n".getBytes(), TRUNCATE_EXISTING, WRITE, CREATE);
            Files.write(outputPath, "Response File Version=1.0.0.0.0\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "[GENERIC]\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, ("ORACLE_HOME=" + beaHome + "\n").getBytes(), WRITE, APPEND);
            Files.write(outputPath, "INSTALL_TYPE=Complete with Examples\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "MYORACLESUPPORT_USERNAME=\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "MYORACLESUPPORT_PASSWORD=\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "DECLINE_SECURITY_UPDATES=true\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "SECURITY_UPDATES_VIA_MYORACLESUPPORT=false\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "PROXY_HOST=\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "PROXY_PORT=\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "PROXY_USER=\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "PROXY_PWD=\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "COLLECTOR_SUPPORTHUB_URL=\n".getBytes(), WRITE, APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    protected File createDomainSilentInstallFile(File sourcesLocation, File installDir) {
        File outputFile = FileUtils.getFile(sourcesLocation, "create_domain.py");
        Path outputPath = Paths.get(outputFile.getAbsolutePath());
        try {
            Files.write(outputPath, ("readTemplate('" + installDir + "/common/templates/wls/wls.jar')\n").getBytes(), TRUNCATE_EXISTING, WRITE, CREATE);
            Files.write(outputPath, "cd('Servers/AdminServer')\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "set('ListenAddress','')\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "set('ListenPort', 7001)\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "create('AdminServer','SSL')\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "cd('SSL/AdminServer')\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "set('Enabled', 'True')\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "set('ListenPort', 7002)\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "cd('/')\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "cd('Security/base_domain/User/weblogic')\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "cmo.setPassword('weblogic1')\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "setOption('OverwriteDomain', 'true')\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, ("writeDomain('" + installDir + "/samples/domains/wl_server')\n").getBytes(), WRITE, APPEND);
            Files.write(outputPath, "closeTemplate()\n".getBytes(), WRITE, APPEND);
            Files.write(outputPath, "exit()".getBytes(), WRITE, APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    public void run() throws Exception {
        File sourcesLocation = FileUtils.getFile(context.getSourcesLocation());
        File beaHome = FileUtils.getFile(context.getBeaHome());
        File installDir = FileUtils.getFile(context.getInstallDir());
        // Download Artifact
        URL webLogicInstallUrl = this.context.getInstallerUrl();
        File installerFile = new File(sourcesLocation, this.context.getInstallerFileName());
        if (!installerFile.exists()) {
            LOGGER.info("Downloading WebLogic artefact");
            this.archiveFactory.createArtifact(webLogicInstallUrl).download(installerFile);
        } else {
            LOGGER.info("Weblogic installation files already exist in '" + installerFile.getAbsolutePath() + "'. Skipping download.");
        }
        // Install WLS
        installerFile.setExecutable(true);
        LOGGER.info("Building WLS response file");
        File installResponseFile = createSilentInstallFile(sourcesLocation, beaHome);
        LOGGER.info("Installing WLS");
        runInstallationProcess(installerFile, installResponseFile);
        // Create Domain
        File createDomainResponseFile = createDomainSilentInstallFile(sourcesLocation, installDir);
        runCreateDomainProcess(createDomainResponseFile);
        LOGGER.info("Flow has finished.");
    }

    protected void runInstallationProcess(File installerFile, File installResponseFile) throws InterruptedException {
        List<String> args = new ArrayList<>();
        String command = context.getCustomJvm() != null ? context.getCustomJvm() + "/bin/java" : "java";
        args.add("-jar");
        args.add(installerFile.toString());
        args.add("-silent");
        args.add("-nowait");
        args.add("-responseFile");
        args.add(installResponseFile.getAbsolutePath());
        int responseCode = getExecutionBuilder(LOGGER, command)
                .args(args)
                .build()
                .go();
        switch (responseCode) {
            case 0:
                LOGGER.info("WLS installation completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Launching silent installation of WLS failed (%d)", new Object[]{responseCode}));
        }
    }

    protected void runCreateDomainProcess(File createDomainResponseFile) throws InterruptedException {
        List<String> args = new ArrayList<>();
        String command = context.getInstallDir() + "/common/bin/wlst.cmd";
        args.add(createDomainResponseFile.getAbsolutePath());
        int responseCode = getExecutionBuilder(LOGGER, command)
                .args(args)
                .build()
                .go();
        switch (responseCode) {
            case 0:
                LOGGER.info("WLS domain creation completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Creating of WLS domain failed (%d)", new Object[]{responseCode}));
        }
    }
}
