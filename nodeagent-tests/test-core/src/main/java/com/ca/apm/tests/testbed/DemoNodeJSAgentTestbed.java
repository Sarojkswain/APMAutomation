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

import com.ca.apm.tests.role.CollectorAgentRole;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.apm.tests.role.TixChangeRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.MysqlRole;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.role.RedisRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.apm.tests.role.UMAgentRole;

/**
 * NodeJS Agent testbed for Demo purpose
 *
 * @author Karmjit Singh (sinka08@ca.com)
 */
@TestBedDefinition
public class DemoNodeJSAgentTestbed extends NodeJSAgentTestbed {

	public static final String NODEJS_DEMO_MACHINE = "nodejsDemoMachine";
	public static final String EM_MACHINE = "emMachine";
	private static final String EM_ROLE_ID = "emRole";

	@Override
	public ITestbed create(ITasResolver tasResolver) {

		// mysql role
		MysqlRole mysqlRole = createMySqlRole();

		// nodejs role
		NodeJsRole nodeJsRole = createNodeJsRole(tasResolver);

		TixChangeRole tixChangeRole = createBasicTixChangeRoleBuilder(tasResolver, mysqlRole,
		        nodeJsRole).autoStart().build();

		// redis role
		RedisRole redisRole = creatRedisRole(tasResolver);

		TomcatRole tomcatRole = createTomcatRole(tasResolver);

		// role for configuring tixchangeNode app on tomcat
		ExecutionRole configRole = createIndexFileModificationRole(tasResolver,
		        TIXCHANGE_NODE_CONTEXT, tomcatRole);

		// em should auto start by default
		EmRole emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver).build();

		// java agent on tomcat
		// FIXME weird cyclic redundancy issue is noticed when adding
		// tomcatAgentRole and setting its execution order after tomcat Role.
		// commenting for now.
		// AgentRole tomcatAgentRole = new
		// AgentRole.LinuxBuilder(TOMCAT_AGENT_ROLE_ID, tasResolver)
		// .webAppRole(tomcatRole).emRole(emRole).build();
		
		String mockEMHost = getMockEMHostAndPort(tasResolver, tixChangeRole)[0];
        int mockEMPort = Integer.parseInt(getMockEMHostAndPort(tasResolver, tixChangeRole)[1]);

		
		UMAgentRole umAgentRole = createUMAgentRole(tasResolver, mockEMHost, mockEMPort);
		 
//		CollectorAgentRole collectorAgentRole = createBasicCollectorAgentRoleBuilder(tasResolver,
//		        tasResolver.getHostnameById(emRole.getRoleId()), emRole.getEmPort()).autoStart()
//		        .build();

		NodeJSProbeRole probeRole = createNodeJsProbeRole(tasResolver, umAgentRole,
		    tixChangeRole, nodeJsRole, NODEJS_ROLE_ID + "-probe");
		
		// set ordering of role execution
		tomcatRole.before(configRole);
		// tomcatAgentRole.after(tomcatRole);
		nodeJsRole.before(tixChangeRole);
		tixChangeRole.before(mysqlRole, redisRole, probeRole);

		ITestbedMachine nodeMachine = TestBedUtils.createLinuxMachine(NODEJS_DEMO_MACHINE,
		        TEMPLATE_ID, redisRole, mysqlRole, nodeJsRole, tixChangeRole, probeRole,
		        tomcatRole, configRole, umAgentRole);

		ITestbedMachine emMachine = TestBedUtils.createWindowsMachine(EM_MACHINE,
		        ITestbedMachine.TEMPLATE_W64, emRole);

		ITestbed testbed = new Testbed(getClass().getSimpleName());
		testbed.addMachine(emMachine);
		testbed.addMachine(nodeMachine);
		return testbed;
	}

}
