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

import com.ca.apm.tests.artifact.NodeJSRuntimeVersionArtifact;
import com.ca.apm.tests.role.CollectorAgentRole;
import com.ca.apm.tests.role.NodeJSAppRole;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.apm.tests.role.UMAgentRole;

@TestBedDefinition
public class NodeJSNativeLessLoadTestbed extends NodeJSLoadTestbed {

	protected NodeJSProbeRole createNodeJsProbeRole(ITasResolver tasResolver,
        UMAgentRole umAgentRole, NodeJSAppRole tixChangeRole,
	        NodeJsRole nodeJsRole, String roleId) {

		return NodeJSAgentNativeLessTestbed.createNodeJsProbeRole(tasResolver, umAgentRole,
		        tixChangeRole, nodeJsRole, roleId, getNodeJsProbeVersion(tasResolver));
	}

	protected String getNodeJsProbeVersion(ITasResolver tasResolver) {
		return "1.0-SNAPSHOT";
	}

	protected String getCollectorAgentVersion(ITasResolver tasResolver) {
		return "99.99.dev-SNAPSHOT";
	}
	
	protected NodeJsRole createNodeJsRole(ITasResolver tasResolver) {
		return new NodeJsRole.LinuxBuilder(NODEJS_ROLE_ID, tasResolver)
		        .versionNodeJs(NodeJSRuntimeVersionArtifact.LINUXx64v0_12_2.getArtifact())
		        .install("forever", Collections.singletonList("-g")).build();
	}

}
