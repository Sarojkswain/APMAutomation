package com.ca.apm.saas.standalone;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author banra06
 */
public class JMeterRole extends AbstractRole {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JMeterRole.class);
	private ITasResolver tasResolver;
	private String jmxFile;
	private boolean installJmeter;
	public static final String START_LOAD="startLoad";
	private String targetHost;
	protected JMeterRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.jmxFile=builder.jmxFile;
		this.targetHost=builder.targetHost;
		this.installJmeter=builder.installJmeter;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {
		if(installJmeter){	
		deployJmeter(aaClient);
		}
		if (jmxFile != null && !jmxFile.isEmpty()){
		createBatchFile(aaClient);
		}
	}
	

	private void createBatchFile(IAutomationAgentClient aaClient) {
		FileCreatorFlowContext flow= new FileCreatorFlowContext.Builder()
        .fromResource("/jmeterscripts/"+jmxFile)
        .destinationPath("C:/automation/deployed/jmeterscripts/"+jmxFile)
        .build();
		runFlow(aaClient,FileCreatorFlow.class,flow);
		
		Collection<String> createBatch = Arrays.asList("cd "
				+ TasBuilder.WIN_SOFTWARE_LOC
				+ "/jmeter/bin && jmeter -n -t "
				+ TasBuilder.WIN_SOFTWARE_LOC + "/jmeterscripts/"+jmxFile+" -Jhost="+targetHost);
		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/"+jmxFile+".bat",
						createBatch).build();
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
	}

	
	private void deployJmeter(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.binaries", "apache-jmeter", "", "zip", "3.1"));
		LOGGER.info("Downloading artifact " + url.toString());

		GenericFlowContext context = new GenericFlowContext.Builder()
				.artifactUrl(url)
				.destination(
						TasBuilder.WIN_SOFTWARE_LOC
								+ "/jmeter").build();
		runFlow(aaClient, GenericFlow.class, context);		
	}

	public static class Builder extends BuilderBase<Builder, JMeterRole> {

		private final String roleId;
		private final ITasResolver tasResolver;		
		private String jmxFile;		
		private boolean installJmeter=false;
		private String targetHost="localhost";

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;			
		}

		@Override
		public JMeterRole build() {		
			startLoad();
			return getInstance();
		}

		@Override
		protected JMeterRole getInstance() {
			return new JMeterRole(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}
		 public Builder installJmeter(Boolean installJmeter) {
	            this.installJmeter = installJmeter;
	            return builder();
	     }
		 public Builder jmxFile(String jmxFile) {
	            this.jmxFile = jmxFile;
	            return builder();
	     }
		 public Builder targetHost(String targetHost) {
	            this.targetHost = targetHost;
	            return builder();
	     }
		 
		private void startLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					jmxFile+".bat").workDir(TasBuilder.WIN_SOFTWARE_LOC)
					.terminateOnMatch("Up and Running.").build();
			getEnvProperties().add(START_LOAD, runCmdFlowContext);
		}		
	}
}