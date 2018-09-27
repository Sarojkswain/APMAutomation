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

package com.ca.apm.tests.testbed;

import java.util.Collections;

import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.tests.artifact.NodeJSProbeArtifact;
import com.ca.apm.tests.artifact.NodeJSRuntimeVersionArtifact;
import com.ca.apm.tests.role.NodeJSAppRole;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.apm.tests.role.UMAgentRole;

/**
 * Another version of NodeJS Agent automation testbed which differs for node
 * probe deployment. Node probe installation will cause failure while building
 * native modules for this deployment, thus probe won't report app health data
 * for event loop, gc etc. activities.
 *
 * @author Jan Pojer (pojja01@ca.com)
 * @author Karmjit Singh (sinka08@ca.com)
 */
@TestBedDefinition
public class NodeJSAgentNativeLessTestbed extends NodeJSAgentTestbed implements ITestbedFactory {

	protected NodeJSProbeRole createNodeJsProbeRole(ITasResolver tasResolver,
        UMAgentRole umAgentRole, NodeJSAppRole tixChangeRole,
	        NodeJsRole nodeJsRole, String roleId) {
		return createNodeJsProbeRole(tasResolver, umAgentRole, tixChangeRole, nodeJsRole,
		        roleId, getNodeJsProbeVersion(tasResolver));
	}

	public static NodeJSProbeRole createNodeJsProbeRole(ITasResolver tasResolver,
        UMAgentRole umAgentRole, NodeJSAppRole tixChangeRole,
	        NodeJsRole nodeJsRole, String roleId, String probeVersion) {

		Artifact artifact = new NodeJSProbeArtifact(tasResolver).createArtifact(probeVersion)
		        .getArtifact();
		NodeJSProbeRole role = new NodeJSProbeRole.LinuxBuilder(roleId, tasResolver)
		        .version(artifact).UMAgentRole(umAgentRole)
		        .nodeJSAppRole(tixChangeRole).nodeJSRole(nodeJsRole).setShouldNativeBuildFail(true)
		        .build();

		String artifactName = tasResolver.getArtifactUrl(artifact).getFile();
		if (artifactName != null) {
			role.addProperty(NODEJS_PROBE_ARTIFACT_NAME, artifactName);
		}
		return role;
	}
	
	protected NodeJsRole createNodeJsRole(ITasResolver tasResolver) {
		return new NodeJsRole.LinuxBuilder(NODEJS_ROLE_ID, tasResolver)
		        .versionNodeJs(NodeJSRuntimeVersionArtifact.LINUXx64v0_12_2.getArtifact())
		        .install("forever", Collections.singletonList("-g")).build();
	}
}
