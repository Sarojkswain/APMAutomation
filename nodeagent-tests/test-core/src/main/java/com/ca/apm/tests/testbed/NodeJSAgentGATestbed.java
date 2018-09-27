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

//import com.ca.apm.tests.role.CollectorAgentRole;
import com.ca.apm.tests.role.NodeJSAppRole;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.apm.tests.role.UMAgentRole;

/**
 * Testbed for testing nodejs probe published in npmjs repository (GA)
 * 
 * @author sinka08
 *
 */
@TestBedDefinition
public class NodeJSAgentGATestbed extends NodeJSAgentTestbed {

	protected NodeJSProbeRole createNodeJsProbeRole(ITasResolver tasResolver,
	        UMAgentRole umAgentRole, NodeJSAppRole nodejsAppRole,
	        NodeJsRole nodeJsRole, String roleId) {
		NodeJSProbeRole role = new NodeJSProbeRole.LinuxBuilder(roleId, tasResolver)
		        .packageName(PUBLISHED_PROBE_PACKAGE_NAME).UMAgentRole(umAgentRole)
		        .nodeJSAppRole(nodejsAppRole).nodeJSRole(nodeJsRole).build();

		role.addProperty(NODEJS_PROBE_ARTIFACT_NAME, PUBLISHED_PROBE_PACKAGE_NAME);
		return role;
	}
	
	protected String getUMAgentVersion(ITasResolver tasResolver) {
		return "99.99.dev-SNAPSHOT";
	}
}
