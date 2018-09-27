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

package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.annotation.TasDocFlow;
import com.ca.tas.role.utility.ExecutionRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Flow
@TasDocFlow(description = "Logging with fluentD system")
public class FluentdFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(FluentdFlow.class);

    @FlowContext
    private FluentdFlowContext context;

    @Override
    public void run() throws InterruptedException {
        installFluentdAgentOnLinux();

        createFoldersForOutputs();

        //TODO - DM - maybe write config file to new file and just run td-agent with parameter -c
        replaceTdConfigFile(context.getConfigFileContent());

        //TODO - DM - security/tracking - use special user for all of this ?
        //su - logging
        //chown logging logging <filename>
    }

    private void createFoldersForOutputs() {
        for (String logFile : context.getOutputFolers()) {
            File folder = new File(logFile).getParentFile();

            LOGGER.info("Creating folder {} for log output", folder);
            if (!folder.mkdir()){
                LOGGER.warn("Folder for logs {} was not created", folder);
            }

            try {
                new Execution.Builder("chmod", logger)
                        .args(new String[]{"o+w", folder.getAbsolutePath()})
                        .build()
                        .go();
            } catch (InterruptedException ex) {
                LOGGER.error("Error changing +w permissions of folder" + logFile, ex);
            }
        }
    }

    private void installFluentdAgentOnLinux() throws InterruptedException {
        final String scriptFolder = "/tmp";
        final String scriptName = "executescript.sh";

        createIInstallScript(scriptFolder + "/" + scriptName);

        new Execution.Builder("chmod", logger)
                .args(new String[]{"u+x", scriptName})
                .workDir(new File(scriptFolder))
                .build()
                .go();

        new Execution.Builder("./" + scriptName, logger)
                .workDir(new File(scriptFolder))
                .build()
                .go();
    }

    private void createIInstallScript(String pathToWrite) {
        String udpateScript = "sh <<SCRIPT\n" +
                "rpm --import https://packages.treasuredata.com/GPG-KEY-td-agent\n" +
                "cat >/etc/yum.repos.d/td.repo <<'EOF';\n" +
                "[treasuredata]\n" +
                "name=TreasureData\n" +
                "baseurl=http://packages.treasuredata.com/2/redhat/\\$releasever/\\$basearch\n" +
                "gpgcheck=1\n" +
                "gpgkey=https://packages.treasuredata.com/GPG-KEY-td-agent\n" +
                "EOF\n" +
                "\n" +
                "yum check-update\n" +
                "yes | yum install -y td-agent\n" +
                "\n" +
                "SCRIPT";

        try {
            FileWriter fw = new FileWriter(pathToWrite);
            fw.write(udpateScript);
            fw.close();
        } catch (IOException ex) {
            LOGGER.error("Cannot find file for update script", ex);
        }
    }

    private void replaceTdConfigFile(String configFileContents) {
        LOGGER.info("About to use config file:\n" + configFileContents);
        String tdAgentConfFileLocation = "/etc/td-agent/td-agent.conf";

        //TODO - DM - catch here or elsewhere
        try {
            FileWriter fw = new FileWriter(tdAgentConfFileLocation);
            fw.write(configFileContents);
            fw.close();
        } catch (IOException ex) {
            LOGGER.error("Error while creating td-agent.conf file ", ex);
        }
    }

    public static ExecutionRole StartTdAgentRole(String roleName){
        return executeAgentAction(roleName, "start");
    }

    public static ExecutionRole StopTdAgentRole(String roleName){
        return executeAgentAction(roleName, "stop");
    }

    private static ExecutionRole executeAgentAction(String roleName, String action) {
        RunCommandFlowContext fluentdStartContext
                = new RunCommandFlowContext.Builder("/etc/init.d/td-agent")
                .args(Collections.singletonList(action))
                .build();

        ExecutionRole role =new ExecutionRole.Builder(roleName).flow(RunCommandFlow.class, fluentdStartContext).build();
        return role;
    }

    public static ExecutionRole InstallFluentdPlugin(String roleName, String action) {
        RunCommandFlowContext fluentdStartContext
                = new RunCommandFlowContext.Builder("td-agent-gem")
                .args(Arrays.asList("install", action))
                .build();

        ExecutionRole role =new ExecutionRole.Builder(roleName).flow(RunCommandFlow.class, fluentdStartContext).build();
        return role;
    }


}
