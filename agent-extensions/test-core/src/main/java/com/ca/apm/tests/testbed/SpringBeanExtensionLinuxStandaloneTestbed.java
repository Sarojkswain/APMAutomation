/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 *
 * Author: MARSA22
 * Date: 5/8/2017
 */

package com.ca.apm.tests.testbed;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * 
 *
 * Testbed description.
 */
@TestBedDefinition
public class SpringBeanExtensionLinuxStandaloneTestbed implements ITestbedFactory {

	public static final String SPRING_EXTENSION_ROLE_ID = "SpringExtensionRole";
	public static final String SPRING_EXTENSION_TESTAPP_ROLE_ID = "SpringExtensionTestAppRole";
	public static final String CONFIG_FILES_LOC = TasBuilder.LINUX_SOFTWARE_LOC
			+ "extension/";
	public static final String TESTAPP_FILES_LOC = CONFIG_FILES_LOC + "webapps";

	public static final String CLIENT_MACHINE_ID = "clientMachine";

	public static final String RH7_TEMPLATE_ID = "rh7";

	@Override
	public ITestbed create(ITasResolver tasResolver) {

		// create EM role
		EmRole emRole = new EmRole.LinuxBuilder(
				AgentControllabilityConstants.EM_ROLE_ID, tasResolver)
				.nostartEM().nostartWV().build();

		// create QAApp role for Tomcat
		WebAppRole<TomcatRole> qaAppTomcatRole = new QaAppTomcatRole.Builder(
				AgentControllabilityConstants.QA_APP_TOMCAT_ROLE_ID,
				tasResolver).cargoDeploy().contextName("qa-app").build();

		// create Tomcat role
		TomcatRole tomcatRole = new TomcatRole.LinuxBuilder(
				AgentControllabilityConstants.TOMCAT_ROLE_ID, tasResolver)
				.tomcatVersion(TomcatVersion.v70).tomcatCatalinaPort(9091)
				.webApp(qaAppTomcatRole).build();

		// create Tomcat Agent role
		IRole tomcatAgentRole = new AgentRole.LinuxBuilder(
				AgentControllabilityConstants.TOMCAT_AGENT_ROLE_ID, tasResolver)
				.webAppServer(tomcatRole)
				.intrumentationLevel(AgentInstrumentationLevel.FULL)
				.emRole(emRole).build();

		// creates Generic roles to download artifacts

		GenericRole downloadExtensionFileRole = new GenericRole.Builder(
				SPRING_EXTENSION_ROLE_ID, tasResolver).unpack(
				new DefaultArtifact("com.ca.apm.coda.testdata.extension",
						"SpringBean", "zip", "1.0"), CONFIG_FILES_LOC).build();

		// for testapplications
		GenericRole downloadTestAppsRole = new GenericRole.Builder(
				SPRING_EXTENSION_TESTAPP_ROLE_ID, tasResolver).unpack(
				new DefaultArtifact(
						"com/ca/apm/coda/testdata/extension/testapplication",
						"SpringTestApps", "zip", "1.0"), TESTAPP_FILES_LOC)
				.build();

		// map roles to machines
		ITestbedMachine emMachine = TestBedUtils.createLinuxMachine(
				AgentControllabilityConstants.EM_MACHINE_ID,
				AgentControllabilityConstants.CO66_TEMPLATE_ID);
		emMachine.addRole(emRole);
		ITestbedMachine agentMachine = TestBedUtils.createLinuxMachine(
				AgentControllabilityConstants.TOMCAT_MACHINE_ID,
				AgentControllabilityConstants.CO66_TEMPLATE_ID);
		agentMachine.addRole(tomcatRole, downloadExtensionFileRole,
				qaAppTomcatRole, tomcatAgentRole, downloadTestAppsRole);

		ITestbedMachine clientMachine = TestBedUtils.createWindowsMachine(
				CLIENT_MACHINE_ID,
				AgentControllabilityConstants.WINDOWS_TEMPLATE_ID);

		emMachine.addRemoteResource(RemoteResource.createFromRegExp(".*",
				emRole.getDeployEmFlowContext().getInstallDir() + "/logs"));
		agentMachine.addRemoteResource(RemoteResource.createFromRegExp(".*",
				tomcatRole.getTomcatFlowContext().getTomcatInstallDir()
						+ "/wily/logs"));

		return new Testbed(getClass().getSimpleName()).addMachine(emMachine,
				agentMachine, clientMachine);
	}
}
