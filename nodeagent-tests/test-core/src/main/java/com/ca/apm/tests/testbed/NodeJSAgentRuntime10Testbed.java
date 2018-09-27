/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

import com.ca.tas.artifact.thirdParty.NodeJsVersionArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.*;
import com.ca.tas.tests.annotations.TestBedDefinition;

import java.util.Collections;

/**
 * NodeJS10 Agent automation testbed
 *
 */
@TestBedDefinition
public class NodeJSAgentRuntime10Testbed extends NodeJSAgentTestbed {

	protected NodeJsRole createNodeJsRole(ITasResolver tasResolver) {
		return new NodeJsRole.LinuxBuilder(NODEJS_ROLE_ID, tasResolver).versionNodeJs(NodeJsVersionArtifact.LINUXx64v0_10_28.getArtifact()).install("forever",
		        Collections.singletonList("-g")).build();
	}
}
