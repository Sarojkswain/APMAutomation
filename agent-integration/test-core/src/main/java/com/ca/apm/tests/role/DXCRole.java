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
 * DXC Role - copy of DXC role used for DXC automation
 */

package com.ca.apm.tests.role;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;

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
 * @author banra06@ca.com katra03@ca.com, gupra04@ca.com
 */

public class DXCRole extends AbstractRole {

	private String installDir;
	private ITasResolver tasResolver;
	public static final String DXC_START = "dxcStart";
	public static final String DXC_STOP = "dxcStop";
	public static final String GET_JSON = "getJson";
	public static final String UPLOAD_BA = "uploadBA";
	public static final String DXC_STATUS = "dxcStatus";
	public static final String DXC_VERSION = "master-SNAPSHOT"; //"99.99.feature-SNAPSHOT"
    public static final String BA_VERSION = "master-SNAPSHOT"; //"badev-SNAPSHOT"
	private String kafkaHost;
	private int kafkaPort;
	private String zookeeperHost;
	private int zookeeperPort;
	private String host;
	protected boolean apmEnabled;

	// private String elasticHost;

    protected DXCRole(Builder builder) {

        super(builder.roleId, builder.getEnvProperties());
        this.tasResolver = builder.tasResolver;
        this.installDir = builder.installDir;
        this.kafkaHost = builder.kafkaHost;
        this.kafkaPort = builder.kafkaPort;
        this.zookeeperHost = builder.zookeeperHost;
        this.zookeeperPort = builder.zookeeperPort;
        this.apmEnabled = builder.apmEnabled;
        // this.elasticHost = builder.elasticHost;
        host = tasResolver.getHostnameById(builder.roleId);

        String baProfileFileFullPath =
            installDir + "browserAgent/wa/ba/profile/default-tenant/default-app/profile.json";

        getEnvProperties().put("dxcHome", installDir);
        getEnvProperties().put("baProfileFileFullPath", baProfileFileFullPath);

    }

	@Override
	public void deploy(IAutomationAgentClient aaClient) {
		getArtifacts(aaClient);
		deployDXC(aaClient);
		updateKafkaZookeeper(aaClient);
		configureDXC(aaClient);
		unJarJsons(aaClient);
		configureBAprofile(aaClient);
		uploadBA(aaClient);
		uploadBA_IncorrectKafka(aaClient);
		getWilyAgent(aaClient);
		dxcStatus(aaClient);
		moveBAExtJsFile(aaClient);

	}

	private void deployDXC(IAutomationAgentClient aaClient) {

		// Shell script commands for extracting required files for dxc
		Collection<String> data =
            Arrays.asList("(tar -xvf " + installDir + "DigitalExperienceCollector* -C " + installDir + ") &&" 
            			+ " ( mv -f " + installDir + "DigitalExperienceCollector*tar.gz /tmp) &&" 
            			+ " ( mv " + installDir + "DigitalExperienceCollector*/* " + installDir + ") &&" 
            			+ " ( rm -rf " + installDir + "DigitalExperienceCollector* )");

		FileModifierFlowContext makeFileModification = new FileModifierFlowContext.Builder()
				.create("/opt/moveDxcFiles.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, makeFileModification);
		RunCommandFlowContext command = new RunCommandFlowContext.Builder(
				"moveDxcFiles.sh").workDir("/opt").build();
		runCommandFlow(aaClient, command);
	}

	private void updateKafkaZookeeper(IAutomationAgentClient aaClient) {
		Map<String, String> updateArgs = new HashMap<String, String>();
		updateArgs.put(
				"bootstrap.servers.hostandport=(.*)",
				"bootstrap.servers.hostandport=" + kafkaHost + ":"
						+ Integer.toString(kafkaPort));
		updateArgs.put("zookeeper.servers.hostandport=(.*)",
				"zookeeper.servers.hostandport=" + zookeeperHost + ":"
						+ Integer.toString(zookeeperPort));
		if (apmEnabled) {
			updateArgs.put("is.apm.only.deployment=false",
					"is.apm.only.deployment=true");
		}

		FileModifierFlowContext updateProps = new FileModifierFlowContext.Builder()
				.replace(installDir + "conf/dxc.properties", updateArgs)
				.build();
		runFlow(aaClient, FileModifierFlow.class, updateProps);
	}

	private void getWilyAgent(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.delivery", "agent-noinstaller-default-unix", "tar",
				tasResolver.getDefaultVersion()));
		GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
				.artifactUrl(url).destination(installDir).build();
		runFlow(aaClient, GenericFlow.class, getAgentContext);
	}

	private void moveBAExtJsFile(IAutomationAgentClient aaClient) {

		FileModifierFlowContext updateDirContext = new FileModifierFlowContext.Builder()
				.move(installDir + "browserAgent/wa/ba/default/BAExt.js",
						installDir
								+ "browserAgent/wa/ba/profile/default-tenant/default-app/BAExt.js")
				.build();
		runFlow(aaClient, FileModifierFlow.class, updateDirContext);
	}

	private void configureDXC(IAutomationAgentClient aaClient) {
		// InetAddress ipAddr = InetAddress.getByName(elasticHost);

		Collection<String> data = Arrays
				.asList("export JAVA_HOME=/opt/jdk1.8 && cd " + installDir
						+ "bin && sh dxc.sh start");

		FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder()
				.create(installDir + "startDXC.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, createFileFlow);

		Collection<String> data1 = Arrays
				.asList("export JAVA_HOME=/opt/jdk1.8 && cd " + installDir
						+ "bin && sh dxc.sh stop");

		FileModifierFlowContext createFileFlowForStop = new FileModifierFlowContext.Builder()
				.create(installDir + "stopDXC.sh", data1).build();
		runFlow(aaClient, FileModifierFlow.class, createFileFlowForStop);
	}

	private void getArtifacts(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm",
				"ExperienceCollectorBundle", "", "tar.gz", DXC_VERSION));

		GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
				.artifactUrl(url).destination(installDir).build();
		runFlow(aaClient, GenericFlow.class, getAgentContext);
	}

	private void unJarJsons(IAutomationAgentClient aaClient) {
		Collection<String> data = Arrays
				.asList("unzip /tmp/tests/com/ca/tas/dxc/dxc-testing-core/"
						+ BA_VERSION + "/dxc-testing-core-" + BA_VERSION
						+ "-jar-with-dependencies.jar -d /opt/unjar");
		FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder()
				.create("/tmp/unjar.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, createFileFlow);
	}

    private void configureBAprofile(IAutomationAgentClient aaClient) {
        InetAddress ipAddr;
        try {
            ipAddr = InetAddress.getByName(host);
            Map<String, String> updateArgs = new HashMap<String, String>();
            updateArgs.put("\\{DXC_IP\\}", ipAddr.getHostAddress());
            updateArgs.put("\\{DXC_PORT\\}", "8080");
            updateArgs.put("\\{APP_ID\\}", "default-app");
            updateArgs.put("\\{TENANT_ID\\}", "default-tenant");
            updateArgs.put("\"pageLoadMetricsThreshold\" : (.*)",
                "\"pageLoadMetricsThreshold\" : 0,");
            updateArgs.put("\"ajaxMetricsThreshold\" : (.*)", "\"ajaxMetricsThreshold\" : 0,");
            updateArgs.put("\"jsFunctionMetricsThreshold\" : (.*)",
                "\"jsFunctionMetricsThreshold\" : 0,");
            updateArgs.put("\"browserLoggingEnabled\" : (.*)", "\"browserLoggingEnabled\" : true,");
            FileModifierFlowContext updateProps =
                new FileModifierFlowContext.Builder().replace(
                    installDir
                        + "/browserAgent/wa/ba/profile/default-tenant/default-app/profile.json",
                    updateArgs).build();
            runFlow(aaClient, FileModifierFlow.class, updateProps);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	private void uploadBA(IAutomationAgentClient aaClient) {
		Collection<String> data = Arrays
				.asList("export JAVA_HOME=/opt/jdk1.8 &&  cd " + installDir
						+ "bin && ./uploadBA.sh -baDir " + installDir
						+ "browserAgent/wa/");

		FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder()
				.create(installDir + "runUploadBA.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, createFileFlow);
		// Run uploadBA.sh
		RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
				"runUploadBA.sh").workDir(installDir).build();
		runCommandFlow(aaClient, runCmdFlowContext);

	}

	private void dxcStatus(IAutomationAgentClient aaClient) {
		Collection<String> data = Arrays
				.asList("export JAVA_HOME=/opt/jdk1.8 &&  cd " + installDir
						+ "bin && ./dxc.sh status >>dxcStatus.txt");

		FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder()
				.create(installDir + "dxcStatus.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, createFileFlow);

		// Run dxcStatus.sh
		RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
				"dxcStatus.sh").workDir(installDir).build();
		runCommandFlow(aaClient, runCmdFlowContext);

	}

	private void uploadBA_IncorrectKafka(IAutomationAgentClient aaClient) {
		Collection<String> data = Arrays
				.asList("export JAVA_HOME=/opt/jdk1.8 &&  cd " + installDir
						+ " && ./runUploadBA.sh 2>uploadBAError.txt");

		FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder()
				.create(installDir + "runUploadBA_Incorrectkafka.sh", data)
				.build();
		runFlow(aaClient, FileModifierFlow.class, createFileFlow);

	}

	public static class LinuxBuilder extends Builder {

		public LinuxBuilder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
		}
	}

	public static class Builder extends BuilderBase<Builder, DXCRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		private String installDir = "/opt/dxc/";
		private String kafkaHost = "localhost";
		private int kafkaPort = 9092;
		private String zookeeperHost = "localhost";
		private int zookeeperPort = 2181;
		private boolean apmEnabled = false;

		// private String elasticHost = "localhost";

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public DXCRole build() {
			// TODO Auto-generated method stub
			startDXC();
			stopDXC();
			getJson();
			uploadBAFiles();
			dxcStatusFile();
			return getInstance();
		}

		@Override
		protected DXCRole getInstance() {
			// TODO Auto-generated method stub
			return new DXCRole(this);
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

		public Builder kafkaHost(String kafkaHost) {
			this.kafkaHost = kafkaHost;
			return builder();
		}

		public Builder kafkaPort(int kafkaPort) {
			this.kafkaPort = kafkaPort;
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

		public Builder apmEnabled(Boolean apmEnabled) {
			this.apmEnabled = apmEnabled;
			return builder();
		}

		private void startDXC() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"startDXC.sh").workDir(installDir).build();
			getEnvProperties().add(DXC_START, runCmdFlowContext);
		}

		private void stopDXC() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"stopDXC.sh").workDir(installDir).build();
			getEnvProperties().add(DXC_STOP, runCmdFlowContext);
		}

		private void getJson() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"unjar.sh").workDir("/tmp").build();
			getEnvProperties().add(GET_JSON, runCmdFlowContext);
		}

		private void uploadBAFiles() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"runUploadBA.sh").workDir(installDir).build();
			getEnvProperties().add(UPLOAD_BA, runCmdFlowContext);
		}

		private void dxcStatusFile() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"dxcStatus.sh").workDir(installDir).build();
			getEnvProperties().add(DXC_STATUS, runCmdFlowContext);
		}

	}
}
