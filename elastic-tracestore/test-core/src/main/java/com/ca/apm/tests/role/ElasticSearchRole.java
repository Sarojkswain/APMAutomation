package com.ca.apm.tests.role;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
 * @author banra06@ca.com
 */

public class ElasticSearchRole extends AbstractRole {

	private String installDir;
	private ITasResolver tasResolver;
	public static final String ELASTICSEARCH_START = "elasticSearchStart";
	public static final String ELASTICSEARCH_STOP = "elasticSearchStop";
	public String host;

	protected ElasticSearchRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.installDir = builder.installDir;
		host = tasResolver.getHostnameById(builder.roleId);

	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

		getArtifacts(aaClient);
		installYumPackages(aaClient);
		installElasticSearch(aaClient);
		updateYAML(aaClient);
		createStartFile(aaClient);
	}

	private void updateYAML(IAutomationAgentClient aaClient) {

		try {
			InetAddress ipAddr = InetAddress.getByName(host);
			FileModifierFlowContext context = null;
			Map<String, String> replacePairs = new HashMap<String, String>();

			replacePairs.put("#network.host: 192.168.0.1", "network.host: "
					+ ipAddr.getHostAddress());

			String fileName = installDir
					+ "/elasticsearch-5.3.0/config/elasticsearch.yml";

			context = new FileModifierFlowContext.Builder().replace(fileName,
					replacePairs).build();
			runFlow(aaClient, FileModifierFlow.class, context);
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}
		Collection<String> data = Arrays
				.asList("elastic  	-  	nofile  	65536",
						"elastic  	-  	nproc  	    65536");
		String fileName = "/etc/security/limits.conf";

		FileModifierFlowContext appendFileFlow = new FileModifierFlowContext.Builder()
		.append(fileName, data).build();
		runFlow(aaClient, FileModifierFlow.class, appendFileFlow);
	}

	private void installElasticSearch(IAutomationAgentClient aaClient) {
		Collection<String> data = Arrays
				.asList("adduser -m elastic", "runuser -l elastic -c 'unzip /tmp/elastic5.3.zip -d "
						+ installDir + "'","sysctl -w vm.max_map_count=262144");
		FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder()
				.create("/tmp/installelastic.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, createFileFlow);
		RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
				"installelastic.sh").workDir("/tmp").build();
		runCommandFlow(aaClient, runCmdFlowContext);
	}

	private void createStartFile(IAutomationAgentClient aaClient) {

		Collection<String> data = Arrays.asList("runuser -l elastic -c '"
				+ installDir + "/elasticsearch-5.3.0/bin/elasticsearch'");
		FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder()
				.create("/tmp/startelastic.sh", data).build();
		runFlow(aaClient, FileModifierFlow.class, createFileFlow);

	}

	private void getArtifacts(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.binaries", "dxc", "elastic5.3", "zip", "1.0"));
		GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
				.artifactUrl(url).destination("/tmp/elastic5.3.zip")
				.notArchive().build();
		runFlow(aaClient, GenericFlow.class, getAgentContext);
	}

	private void installYumPackages(IAutomationAgentClient aaClient) {
		String COMMAND = "yum";
		List<String> arguments = new ArrayList<>();
		arguments.add("install");
		arguments.add("-y");
		arguments.add("unzip");
		RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
				COMMAND).args(arguments).build();
		runCommandFlow(aaClient, runCmdFlowContext);
	}

	public static class LinuxBuilder extends Builder {
		public LinuxBuilder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
		}
	}

	public static class Builder extends BuilderBase<Builder, ElasticSearchRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		private String installDir = "/home/elastic/";

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public ElasticSearchRole build() {
			// TODO Auto-generated method stub
			startElasticSearch();
			stopElasticSearch();
			return getInstance();
		}

		@Override
		protected ElasticSearchRole getInstance() {
			// TODO Auto-generated method stub
			return new ElasticSearchRole(this);
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

		private void startElasticSearch() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"startelastic.sh").workDir("/tmp")
					.terminateOnMatch("indices into cluster_state").build();
			getEnvProperties().add(ELASTICSEARCH_START, runCmdFlowContext);
		}

		private void stopElasticSearch() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"stopelastic.sh").workDir(installDir).build();
			getEnvProperties().add(ELASTICSEARCH_STOP, runCmdFlowContext);
		}
	}
}
