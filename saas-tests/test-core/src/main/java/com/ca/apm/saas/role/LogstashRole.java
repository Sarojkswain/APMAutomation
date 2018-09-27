/*
 * Copyright (c) 2017 CA. All rights reserved.
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

/*
 * LogstashRole - copy of Logstash role used for DXC automation
 */

package com.ca.apm.saas.role;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 *
 * @author banra06@ca.com, gupra04@ca.com
 * 
 */

public class LogstashRole extends AbstractRole {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LogstashRole.class);

	private String installDir;
	private String dxcInstallDir;
	private ITasResolver tasResolver;
	public static final String LOGSTASH_START = "logstashStart";
	public static final String LOGSTASH_STOP = "logstashStop";
	public static final String LOGSTASH_APM_START = "logstashAPMStart";
	private String zookeeperHost;
	private int zookeeperPort;
	private String elasticHost;
	private int elasticPort;
	private String emHost;
	private int emPort;

	protected LogstashRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.installDir = builder.installDir;
		this.zookeeperHost = builder.zookeeperHost;
		this.zookeeperPort = builder.zookeeperPort;
		this.elasticHost = builder.elasticHost;
		this.elasticPort = builder.elasticPort;
		this.emHost = builder.emHost;
		this.emPort = builder.emPort;

	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {
		getArtifacts(aaClient);
		logstashDeploy(aaClient);
		configureLogstash(aaClient);
		installAPMLogstashPlugin(aaClient);
		configureAPMLogstash(aaClient);
	}

	private void configureLogstash(IAutomationAgentClient aaClient) {

		// create dxc logstash file
		Collection<String> data1 = Arrays.asList("input {", "kafka {",
				"zk_connect => '" + zookeeperHost + ":" + zookeeperPort + "'",
				"topic_id => 'analytics'", "}", "}", "output {", "stdout {",
				"codec => rubydebug ", "}", "elasticsearch { hosts => [\""
						+ elasticHost + ":" + elasticPort + "\"]",
				"index => \"dxi_dxc_%{+MM_YYYY}\"", "}", "}");

		Collection<String> data = Arrays.asList("cd " + installDir
				+ "bin && ./logstash -f dxc-logstash.conf >> logstashrun.txt");

		Collection<String> data2 = Arrays
				.asList("kill -9 `ps -ef|grep logstash|grep -v grep|awk '{print $2}'`");

		FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder()
				.create(installDir + "/bin/dxc-logstash.conf", data1)
				.create(installDir + "/startLogstash.sh", data)
				.create(installDir + "/stopLogstash.sh", data2).build();

		runFlow(aaClient, FileModifierFlow.class, createFileFlow);
	}

	private void configureAPMLogstash(IAutomationAgentClient aaClient) {

		Collection<String> data = Arrays.asList("cd " + installDir
				+ "bin && ./logstash -f dxc-logstash-apm-template.conf");

		FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder()
				.create(installDir + "/startAPMLogstash.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, createFileFlow);

		Map<String, String> updateArgs = new HashMap<String, String>();
		updateArgs.put("agentManager.url.1=(.*)", "agentManager.url.1="
				+ emHost + ":" + Integer.toString(emPort));

		FileModifierFlowContext updateProps = new FileModifierFlowContext.Builder()
				.replace(installDir + "/bin/agent.properties", updateArgs)
				.build();
		runFlow(aaClient, FileModifierFlow.class, updateProps);
	}

	private void installAPMLogstashPlugin(IAutomationAgentClient aaClient) {

		// Install plugin

		Collection<String> data = Arrays
				.asList("(PATH=$JAVA_HOME\bin:$PATH) &&" + "(export PATH) &&"
						+ installDir + "/bin/logstash-plugin install "
						+ installDir + "/bin/logstashplugin.gem");

		FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder()
				.create("/tmp/installLogstashPlugin.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, createFileFlow);

		RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
				"installLogstashPlugin.sh").workDir("/tmp").build();
		runCommandFlow(aaClient, runCmdFlowContext);
	}

	private void getArtifacts(IAutomationAgentClient aaClient) {
		URL url = tasResolver
				.getArtifactUrl(new DefaultArtifact("com.ca.apm",
						"ExperienceCollectorBundle", "", "tar.gz",
						DXCRole.DXC_VERSION));

		GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
				.artifactUrl(url).destination(installDir).build();
		runFlow(aaClient, GenericFlow.class, getAgentContext);

	}

	private void logstashDeploy(IAutomationAgentClient aaClient) {

		// Shell script commands for extracting files required for logstash &
		// Copy apm-logstash-plugin conf & gem file to bin
		Collection<String> data = Arrays.asList("(tar -xvf " + installDir + "DigitalExperienceCollector* -C " + installDir + ") &&" 
                + " ( mv -f " + installDir + "DigitalExperienceCollector*tar.gz /tmp) &&" 
                + " ( mv " + installDir + "DigitalExperienceCollector*/logstash/* " + installDir + ") &&" 
                + " ( rm -rf " + installDir + "DigitalExperienceCollector* ) &&" 
                + " ( tar -xvf " + installDir + "logstash*tgz -C " + installDir + ") &&" 
                + " ( mv -v " + installDir + "logstash*/* " + installDir + ") &&" 
                + " ( rm -rf " + installDir + "logstash*) &&" 
                + " ( mv " + installDir + "apm-logstash-plugin/agent.properties "  + installDir + "apm-logstash-plugin/dxc-logstash-apm-template.conf " + installDir + "bin) &&" 
                + " ( mv " + installDir + "apm-logstash-plugin/logstash-output*gem "  + installDir + "bin/logstashplugin.gem)");
        
		LOGGER.info(" moveLogstashFiles content: " + data.toString());
		FileModifierFlowContext makeFileModification = new FileModifierFlowContext.Builder()
				.create("/opt/moveLogstashFiles.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, makeFileModification);
		RunCommandFlowContext command = new RunCommandFlowContext.Builder(
				"moveLogstashFiles.sh").workDir("/opt").build();
		runCommandFlow(aaClient, command);
	}

	public static class LinuxBuilder extends Builder {
		public LinuxBuilder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
		}
	}

	public static class Builder extends BuilderBase<Builder, LogstashRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		private String installDir = "/opt/logstash/";
		private String dxcInstallDir = "/opt/dxc/";
		private String zookeeperHost = "localhost";
		private int zookeeperPort = 2181;
		private String elasticHost = "localhost";
		private int elasticPort = 9200;
		private String emHost = "localhost";
		private int emPort = 5001;

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public LogstashRole build() {
			// TODO Auto-generated method stub
			startLogstash();
			startAPMLogstash();
			stopLogstash();
			return getInstance();
		}

		@Override
		protected LogstashRole getInstance() {
			// TODO Auto-generated method stub
			return new LogstashRole(this);
		}

		@Override
		protected Builder builder() {
			// TODO Auto-generated method stub
			return this;
		}

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		public Builder elasticPort(int elasticPort) {
			this.elasticPort = elasticPort;
			return builder();
		}

		public Builder elasticHost(String elasticHost) {
			this.elasticHost = elasticHost;
			return builder();
		}

		public Builder emPort(int emPort) {
			this.emPort = emPort;
			return builder();
		}

		public Builder emHost(String emHost) {
			this.emHost = emHost;
			return builder();
		}

		public Builder zookeeperHost(String zookeeperHost) {
			this.zookeeperHost = zookeeperHost;
			return builder();
		}

		public Builder zookeeperPort(int zookeeperPort) {
			this.zookeeperPort = zookeeperPort;
			return builder();
		}

		private void startLogstash() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"startLogstash.sh").workDir(installDir)
					.terminateOnMatch("Pipeline main started").build();
			getEnvProperties().add(LOGSTASH_START, runCmdFlowContext);

		}

		private void stopLogstash() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"stopLogstash.sh").workDir(installDir)
					.terminateOnMatch("Pipeline main started").build();
			getEnvProperties().add(LOGSTASH_STOP, runCmdFlowContext);

		}

		private void startAPMLogstash() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"startAPMLogstash.sh").workDir(installDir)
					.terminateOnMatch("Pipeline main started").build();
			getEnvProperties().add(LOGSTASH_APM_START, runCmdFlowContext);

		}
	}
}