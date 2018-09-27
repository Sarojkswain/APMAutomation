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
 * Author : JAMSA07/ JAMMI SANTOSH
 * Date : 20/11/2015
 */

package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * SampleTestbed class.
 *
 * Testbed description.
 */
@TestBedDefinition
public class StandaloneOnlyEMWindowsTestbed implements ITestbedFactory {

	public static final String MY_APP_ROLE = "myApp1Role";
	public static final String MY_APP_HELLOWORLD_ROLE = "helloworld";
	public static final String CONFIG_FILES_ARTIFACT_VERSION = "1.0";
	public static final String SAMPLE_APP_LOC = TasBuilder.WIN_SOFTWARE_LOC
			+ "sampleApps/";
	public static final String EM_ROLE_ID = "emRole";
	private static final String EM_MACHINE_TEMPLATE_ID = TEMPLATE_W64;
	public static final String EM_MACHINE_ID = "emMachine";

	@Override
	public ITestbed create(ITasResolver tasResolver) {
		// create EM role
		EmRole emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver).nostartEM()
				.nostartWV().build();

		// creates Generic roles to download artifacts
		GenericRole downloadMyApp1Role = new GenericRole.Builder(MY_APP_ROLE,
				tasResolver).download(
				new DefaultArtifact("com.ca.apm.coda.testdata.em.sampleapps",
						"myapp1", "war", CONFIG_FILES_ARTIFACT_VERSION),
				SAMPLE_APP_LOC + "myapp1.war").build();

		// creates Generic roles to download artifacts
		GenericRole downloadhelloWorldAppRole = new GenericRole.Builder(
				MY_APP_HELLOWORLD_ROLE, tasResolver).download(
				new DefaultArtifact("com.ca.apm.coda.testdata.em.sampleapps",
						"helloworld", "war", CONFIG_FILES_ARTIFACT_VERSION),
				SAMPLE_APP_LOC + "helloworld.war").build();

		// map roles to machines
		ITestbedMachine emMachine = TestBedUtils.createWindowsMachine(
				EM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID);
		emMachine
				.addRole(emRole, downloadMyApp1Role, downloadhelloWorldAppRole);

		emMachine.addRemoteResource(RemoteResource.createFromRegExp(".*",
				emRole.getDeployEmFlowContext().getInstallDir() + "\\logs"));

		return new Testbed(getClass().getSimpleName()).addMachine(emMachine);
	}
}
