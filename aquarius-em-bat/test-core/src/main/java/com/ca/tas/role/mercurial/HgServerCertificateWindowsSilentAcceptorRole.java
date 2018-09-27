package com.ca.tas.role.mercurial;

import java.util.Arrays;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * Role to make sure to accept and cache Hg server certificate silently if it hasn't been yet.
 * This role requires plink.exe to be available on the system PATH environment variable of the target machine. 
 * 
 * @author sinal04
 *
 */
public class HgServerCertificateWindowsSilentAcceptorRole extends AbstractRole {

	private static final int ASYNC_DELAY = 180;

	public HgServerCertificateWindowsSilentAcceptorRole(String roleId) {
		super(roleId);

	}

	@Override
	public void deploy(IAutomationAgentClient automationClient) {
		RunCommandFlowContext commandFlowContext = new RunCommandFlowContext.Builder(
				"cmd").args(Arrays.asList("/C", "echo y | plink.exe hg@oerth"))
				.name(getRoleId()).build();

		automationClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(
				RunCommandFlow.class, commandFlowContext, getHostingMachine()
						.getHostnameWithPort()).delay(ASYNC_DELAY).async());

	}

}
