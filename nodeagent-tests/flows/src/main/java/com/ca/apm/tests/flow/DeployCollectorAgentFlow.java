/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
package com.ca.apm.tests.flow;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.configuration.ConfigurationFile;
import com.ca.apm.automation.utils.configuration.ConfigurationFileFactory;

/**
 * DeployCollectorAgentFlow class provides deployment logic for Collector Agent
 *
 */
public class DeployCollectorAgentFlow extends AbstractDeployAgentFlow {
	@FlowContext
	private DeployCollectorAgentFlowContext context;

	private static final Logger LOGGER = LoggerFactory.getLogger(DeployCollectorAgentFlow.class);

	@Override
	public void run() throws Exception {
		LOGGER.info("Starting Collector Agent deployment");

		File agentInstallDir = new File(context.getInstallDir());

		// un-install previous collector agent deployment
		clearOldCollectorAgent(agentInstallDir);
		deployAgent(agentInstallDir);
		configureAgent(agentInstallDir);

		LOGGER.info("Task completed.");
	}

	private void clearOldCollectorAgent(File agentInstallDir) throws IOException {
		super.clearOldAgent(agentInstallDir);
	}

	@Override
	protected void deployAgent(File agentInstallDir) throws IOException {
		File installerTgdirDir = new File(context.getInstallerTgdir());
		archiveFactory.createArchive(context.getInstallerUrl()).unpack(installerTgdirDir);

		LOGGER.info("Creating folder {}", agentInstallDir.getAbsolutePath());
		FileUtils.forceMkdir(agentInstallDir);

		File agentUnpackedDir = installerTgdirDir;
		LOGGER.info("Copying files from {} to {}", agentUnpackedDir.getAbsolutePath(),
		        agentInstallDir.getAbsolutePath());
		FileUtils.copyDirectory(agentUnpackedDir, agentInstallDir);

		// check that agent was installed
		File agentJarFile = new File(agentInstallDir.getAbsolutePath() + "/lib/Agent.jar");
		if (!agentJarFile.exists()) {
			throw new IllegalStateException(String.format("Didn't find %s as expected",
			        agentJarFile.getAbsolutePath()));
		}
		updatePermissions();

		LOGGER.info("Found {}", agentJarFile.getAbsolutePath());
		FileUtils.forceDelete(installerTgdirDir);
	}
	
	protected ConfigurationFile loadConfigFile(File coreAgentConfig) {
		File agentConfigFile = FileUtils.getFile(coreAgentConfig, "config",
		        "IntroscopeCollectorAgent.profile");

		LOGGER.info("Reading config file {}", agentConfigFile.getAbsolutePath());
		return new ConfigurationFileFactory().create(agentConfigFile);
	}

	protected void configureAgent(File agentInstallDir) {
		File coreAgentConfig = new File(agentInstallDir, "core");
		ConfigurationFile agentProfile = loadConfigFile(coreAgentConfig);
		configureEm(agentProfile);
		configureInstrumentation(agentProfile);
		configureProperties(agentProfile);
	}

	private void updatePermissions() {
		String installDir = context.getInstallDir();
		// update permission for collectorAgent.sh
		String fileName = installDir + "bin" + File.separator + context.getCollAgentExecutable();

		File file = new File(fileName);
		file.setExecutable(true);

		// update permission for java executable
		fileName = installDir + "jre" + File.separator + "bin" + File.separator + "java";
		file = new File(fileName);
		file.setExecutable(true);
	}

	@Override
	protected AbstractDeployAgentFlowContext getContext() {
		return context;
	}
}
