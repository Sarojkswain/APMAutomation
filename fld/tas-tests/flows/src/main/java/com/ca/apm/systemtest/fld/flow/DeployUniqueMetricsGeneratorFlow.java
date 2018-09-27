package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.utils.configuration.ConfigurationFile;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

/**
 * This flow downloads and unpacks Unique MEtrics Generator tool.
 *
 * @author haiva01
 */
@Flow
public class DeployUniqueMetricsGeneratorFlow extends FlowBase {
    private static final Logger log = LoggerFactory
        .getLogger(DeployUniqueMetricsGeneratorFlow.class);

    @FlowContext
    private DeployUniqueMetricsGeneratorFlowContext flowContext;

    @Override
    public void run() throws Exception {
        File destDir = new File(flowContext.getDir());
        log.info("Deploying Unique MEtrics Generator tool into {}.", destDir.getAbsolutePath());
        FileUtils.forceMkdir(destDir);

        this.archiveFactory.createArchive(new URL(flowContext.getUmegArtifactUrl()))
            .unpack(destDir);
        ConfigurationFile agentConfig = this.configFileFactory.create(
            Paths
                .get(destDir.getAbsolutePath(), "wily", "core", "config", "IntroscopeAgent.profile")
                .toFile());
        agentConfig.addOrUpdate("introscope.agent.metricClamp", "50000000");
    }
}
