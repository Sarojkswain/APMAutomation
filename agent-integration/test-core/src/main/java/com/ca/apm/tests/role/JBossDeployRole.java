package com.ca.apm.tests.role;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author Abhishek Sinha
 */
public class JBossDeployRole extends AbstractRole {

	private static final Logger LOGGER = LoggerFactory.getLogger(JBossDeployRole.class);
	public static final String ALL_AGENTS_LOC = TasBuilder.WIN_SOFTWARE_LOC + "agents\\";
	private String installDir;
	protected JBossDeployRole(Builder builder) {
		super(builder.roleId, builder.getEnvProperties());		
		this.installDir = builder.installDir();
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {
		
		// create jboss start script
		createJBossScript(aaClient);
				
	}
	
	private void createJBossScript(IAutomationAgentClient aaClient){
		String jBossBin = installDir+"/bin";
		//String command = "start standalone.bat -b "+ tasResolver.getHostnameById(SaasUITestbed.JBOSS_ROLE_ID);
		String command = "start standalone.bat";
		ArrayList<String> commandList = new ArrayList<String>();
		commandList.add(command);
		
		FileModifierFlowContext jBossStartFile = new FileModifierFlowContext.Builder()
							.create(jBossBin + "/runJBoss.bat",commandList).build();
		
		runFlow(aaClient, FileModifierFlow.class, jBossStartFile);
	}

	
	
	public static class Builder extends BuilderBase<Builder, JBossDeployRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		private String installDir;		
		

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public JBossDeployRole build() {
			linkJBossStartFile();
			return getInstance();
		}

		@Override
		protected JBossDeployRole getInstance() {
			return new JBossDeployRole(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder installDir(String installDir) {
		    
            this.installDir = installDir;           
            return builder();
        }
	
		private String installDir(){
			return installDir;
		}
		
	
		private void linkJBossStartFile(){
			String jBossBin = installDir+"/bin";
			String jBossStartKey = "jBossStartKey";
			//testbed.addProperty();
			
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"runJBoss.bat").workDir(jBossBin)
					//.terminateOnMatch("Up and Running.")
					.build();
			getEnvProperties().add(jBossStartKey, runCmdFlowContext);
		}
	}
}
