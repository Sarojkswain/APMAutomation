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

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.utils.configuration.ConfigurationFile;
import com.ca.apm.automation.utils.configuration.ConfigurationFileFactory;

/**
 * AbstractDeployAgentFlow is abstract base class which encapsulates common
 * behavior for agent deployment flow.
 *
 * @author turyu01
 * @author pojja01
 */
@Flow
public abstract class AbstractDeployAgentFlow extends FlowBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDeployAgentFlow.class);

	protected abstract AbstractDeployAgentFlowContext getContext();

	protected ConfigurationFile loadConfigFile(File coreAgentConfig) {
		File agentConfigFile = FileUtils.getFile(coreAgentConfig, "config",
		        "IntroscopeAgent.profile");

		LOGGER.info("Reading config file {}", agentConfigFile.getAbsolutePath());
		return new ConfigurationFileFactory().create(agentConfigFile);
	}

	protected void clearOldAgent(File agentInstallDir) throws IOException {
		LOGGER.info("Deleting folder {}", agentInstallDir.getAbsolutePath());
		FileUtils.deleteDirectory(agentInstallDir);
	}

	protected void deployAgent(File agentInstallDir) throws IOException {
		File installerTgdirFile = new File(getContext().getInstallerTgdir());
		archiveFactory.createArchive(getContext().getInstallerUrl()).unpack(installerTgdirFile);

		LOGGER.info("Creating folder {}", agentInstallDir.getAbsolutePath());
		FileUtils.forceMkdir(agentInstallDir);

		File agentUnpackedDir = new File(installerTgdirFile, "wily");
		LOGGER.info("Copying files from {} to {}", agentUnpackedDir.getAbsolutePath(),
		        agentInstallDir.getAbsolutePath());
		FileUtils.copyDirectory(agentUnpackedDir, agentInstallDir);

		// check that agent was installed
		File agentJarFile = new File(agentInstallDir, "Agent.jar");
		if (!agentJarFile.exists()) {
			throw new IllegalStateException(format("Didn't find %s as expected",
			        agentJarFile.getAbsolutePath()));
		}
		LOGGER.info("Found {}", agentJarFile.getAbsolutePath());
		FileUtils.forceDelete(installerTgdirFile);
	}

	protected void configureEm(ConfigurationFile agentConfig) {
		if (getContext().getEmHost() == null) {
			return;
		}
		// configure EM port
		String propertyKey = "introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT";
		String src = agentConfig.getString(propertyKey);
		String dst = String.valueOf(getContext().getEmPort());
		LOGGER.info("Changing value of {} from {} to {}", propertyKey, src, dst);
		agentConfig.setProperty(propertyKey, dst);

		// configure EM host
		propertyKey = "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT";
		src = agentConfig.getString(propertyKey);
		dst = getContext().getEmHost();
		LOGGER.info("Changing value of {} from {} to {}", propertyKey, src, dst);
		agentConfig.setProperty(propertyKey, dst);
	}

	protected void configureInstrumentation(ConfigurationFile agentConfig) {
		Collection<String> fileNames = getContext().getDirectiveFilenames();
		
		if (fileNames != null && !fileNames.isEmpty()) {
			// configure directive files for AutoProbe instrumentation type
			String propertyKey = "introscope.autoprobe.directivesFile";
			String[] src = agentConfig.getStringArray(propertyKey);
			LOGGER.info("Changing value of {} from {} to {}", propertyKey,
			        StringUtils.join(src, ','),
			        StringUtils.join(getContext().getDirectiveFilenames(), ','));
			agentConfig.setProperty(propertyKey, getContext().getDirectiveFilenames());
		}
	}

	protected void configureProperties(ConfigurationFile agentConfig) {
		for (Map.Entry<String, String> propertyEntry : getContext().getAdditionalProperties()
		        .entrySet()) {
			String key = propertyEntry.getKey();
			String value = propertyEntry.getValue();

			if (agentConfig.containsKey(key)) {
				LOGGER.debug("Changing value of {} from {} to {}", key,
				        agentConfig.getProperty(key), value);
				agentConfig.setProperty(key, value);
			} else {
				LOGGER.debug("Adding new property {} with value {}", key, value);
				agentConfig.addProperty(key, value);
			}
		}
	}

}
