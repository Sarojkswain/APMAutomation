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
package com.ca.apm.tests.flow.agent;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * DeployJavaAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public abstract class DeployAgentFlowAbs extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployAgentFlowAbs.class);
    @FlowContext
    private DeployAgentFlowContext context;

    public DeployAgentFlowAbs() {
    }

    public void run() throws IOException {
        try {
            if (context.isUndeployExistingBeforeInstall()) {
                rmDir();
            }
            this.archiveFactory.createArchive(this.context.getDeployPackageUrl()).unpack(new File(this.context.getDeploySourcesLocation()));
            configureAgent();
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    public abstract void configureAgent() throws IOException;

    protected void setAgentName(File configFile, String agentName) throws IOException {
        setProperty(configFile, "introscope.agent.agentAutoNamingEnabled", "false");
        uncommentProperty(configFile, "introscope.agent.agentName");
        setProperty(configFile, "introscope.agent.agentName", agentName);
    }

    protected void setEmLocation(File configFile, String emLocation) throws IOException {
        setProperty(configFile, "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", emLocation); // pre-10.2
        setProperty(configFile, "agentManager.url.1", emLocation); // 10.2

    }

    protected void uncommentProperty(File configFile, String property) throws IOException {
        Path path = Paths.get(configFile.toString());
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll("\\#(\\s*)" + property + "(\\s*)=(\\s*)", property + "=");
        Files.write(path, content.getBytes(charset));
    }

    protected void setProperty(File configFile, String property, String value) throws IOException {
        Path path = Paths.get(configFile.toString());
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll(property + "(\\s*)=.*", property + "=" + value);
        Files.write(path, content.getBytes(charset));
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }

    protected void rmDir() throws InterruptedException, IOException {
        File installDir = FileUtils.getFile(this.context.getDeploySourcesLocation());
        if (installDir.exists()) {
            LOGGER.info("installDir to be deleted: '" + installDir);
            try {
                FileUtils.deleteDirectory(installDir);
            } catch (IOException e) {
                LOGGER.warn("installDir could not be deleted (will try again after 60 seconds): '" + e, e);
                try {
                    Thread.sleep(60000L);
                } catch (InterruptedException e2) {}
                FileUtils.deleteDirectory(installDir);
            }
            LOGGER.info("Directory '" + installDir.getAbsolutePath() + "' deleted.");
        } else {
            LOGGER.info("Directory '" + installDir.getAbsolutePath() + "' does not exist. Skipping deletion.");
        }
    }

}
