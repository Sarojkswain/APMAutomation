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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
//import com.ca.apm.tests.artifact.CollectorAgentArtifact;
import com.ca.apm.tests.artifact.NodeJSProbeArtifact;
//import com.ca.apm.tests.role.CollectorAgentRole;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.apm.tests.role.TixChangeRole;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.built.tixchange.TixChangeNodeWarArtifact;
import com.ca.tas.artifact.built.tixchange.TixChangeRestWarArtifact;
import com.ca.tas.artifact.thirdParty.RedisVersionArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
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
 * NodeJS Agent automation testbed
 *
 * @author Jan Pojer (pojja01@ca.com)
 * @author Karmjit Singh (sinka08@ca.com)
 * @author zheji01@ca.com
 */
@TestBedDefinition
public class NodeJSAgentWindowsTestbed extends NodeJSAgentTestbed {

	protected static final String TEMPLATE_ID = ITestbedMachine.TEMPLATE_W64;

	@Override
	public ITestbed create(ITasResolver tasResolver) {

		// nodejs role
		NodeJsRole nodeJsRole = createNodeJsRole(tasResolver);

		TixChangeRole tixChangeRole = createTixChangeRole(tasResolver, nodeJsRole);

		// redis role
		RedisRole redisRole = creatRedisRole(tasResolver);
		
		// tomcat role, don't auto start here bcz we want to deploy java agent
		// on it
		TomcatRole tomcatRole = createTomcatRole(tasResolver);

		// role for configuring tixchangeNode app on tomcat
		ExecutionRole configRole = createIndexFileModificationRole(tasResolver,
		        TIXCHANGE_NODE_CONTEXT, tomcatRole);
		
		// role for updating db credentials for tixChangeRest app
		ExecutionRole dbConfigRole = createDBCredentialsModificationRole(tasResolver, tomcatRole);

		String mockEMHost = tasResolver.getHostnameById(tixChangeRole.getRoleId());
		int mockEMPort = DeployEMFlowContext.EM_PORT;

		// java agent on tomcat, start tomcat using webAppAutoStart() if needed
		AgentRole tomcatAgentRole = new AgentRole.Builder(TOMCAT_AGENT_ROLE_ID, tasResolver)
		        .webAppRole(tomcatRole).webAppAutoStart().overrideEM(mockEMHost, mockEMPort).build();

//		CollectorAgentRole collectorAgentRole = createCollectorAgentRole(tasResolver, mockEMHost,
//		        mockEMPort);
		
		//TODO update createUMAgentRole for windows
		UMAgentRole umAgentRole = createUMAgentRole(tasResolver, mockEMHost, mockEMPort);

		NodeJSProbeRole probeRole = createNodeJsProbeRole(tasResolver, umAgentRole,
		        tixChangeRole, nodeJsRole);

		// set ordering of role execution
		tixChangeRole.before(nodeJsRole, redisRole);
		probeRole.after(tixChangeRole, nodeJsRole);
		configRole.after(tomcatRole);

		ITestbedMachine nodeMachine = TestBedUtils.createWindowsMachine(NODEJS_MACHINE, TEMPLATE_ID,
				redisRole, nodeJsRole, tixChangeRole, probeRole, tomcatRole, configRole, dbConfigRole,
				tomcatAgentRole, umAgentRole);

		ITestbed testbed = new Testbed(getClass().getSimpleName());
		testbed.addProperty(MOCKEM_HOST_KEY, mockEMHost);
		testbed.addProperty(MOCKEM_PORT_KEY, String.valueOf(mockEMPort));
		testbed.addMachine(nodeMachine);

		return testbed;
	}

//	protected CollectorAgentRole createCollectorAgentRole(ITasResolver tasResolver, EmRole emRole) {
//		return createCollectorAgentRole(tasResolver,
//		        tasResolver.getHostnameById(emRole.getRoleId()), emRole.getEmPort());
//	}
//
//	protected CollectorAgentRole createCollectorAgentRole(ITasResolver tasResolver, String emHost,
//	        int emPort) {
//		return createBasicCollectorAgentRoleBuilder(tasResolver, emHost, emPort).build();
//	}
//
//	protected CollectorAgentRole.Builder createBasicCollectorAgentRoleBuilder(
//	        ITasResolver tasResolver, String emHost, int emPort) {
//		return new CollectorAgentRole.Builder(COLLECTOR_AGENT_ROLE_ID, tasResolver)
//		        .version(
//		                new CollectorAgentArtifact(ArtifactPlatform.WINDOWS, tasResolver,
//		                        CollectorAgentArtifact.Runtime.NODEJS))
//		        .overrideEM(emHost, emPort).setTcpLocalMode(true);
//	}

	protected NodeJsRole createNodeJsRole(ITasResolver tasResolver) {
		return new NodeJsRole.Builder(NODEJS_ROLE_ID, tasResolver).install("forever",
				Collections.singletonList("-g")).build();
	}

	protected NodeJSProbeRole createNodeJsProbeRole(ITasResolver tasResolver,
                                                    UMAgentRole umAgentRole, TixChangeRole tixChangeRole,
                                                    NodeJsRole nodeJsRole) {
                                                
        Artifact artifact = new NodeJSProbeArtifact(tasResolver).createArtifact(NODEJS_PROBE_VERSION)
                            .getArtifact();
        NodeJSProbeRole role = new NodeJSProbeRole.Builder(TIXCHANGE_PROBE_ROLE_ID, tasResolver)
                            .version(artifact)
                            .UMAgentRole(umAgentRole)
                            .nodeJSAppRole(tixChangeRole)
                            .nodeJSRole(nodeJsRole).build();
        
        String artifactName = tasResolver.getArtifactUrl(artifact).getFile();
        if (artifactName != null) {
            role.addProperty(NODEJS_PROBE_ARTIFACT_NAME, artifactName);
        }
        return role;
    }

	protected TomcatRole createTomcatRole(ITasResolver tasResolver) {
		TomcatRole tomcatRole = new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver)
		        .webApplication(
		                new TixChangeNodeWarArtifact(tasResolver).createArtifact(TIXCHANGE_VERSION),
		                TIXCHANGE_NODE_CONTEXT)
		        .webApplication(
		                new TixChangeRestWarArtifact(tasResolver).createArtifact(TIXCHANGE_VERSION),
		                "tixchangeRest").tomcatVersion(TomcatVersion.v70).build();

		return tomcatRole;
	}

	protected TixChangeRole createTixChangeRole(ITasResolver tasResolver,
	        NodeJsRole nodeJsRole) {
		TixChangeRole tixChangeRole = createBasicTixChangeRoleBuilder(tasResolver,
		        nodeJsRole).build();
		return tixChangeRole;
	}

	protected TixChangeRole.Builder createBasicTixChangeRoleBuilder(ITasResolver tasResolver, NodeJsRole nodeJsRole) {

		return new TixChangeRole.Builder(TIXCHANGE_ROLE_ID, tasResolver)
		        .version(TIXCHANGE_VERSION)
		        .destination(BuilderBase.WIN_SOFTWARE_LOC + TIXCHANGE_NODE_CONTEXT)
		        .mysqlCreds("root", "root")
		        .node(nodeJsRole).configDatasources("nodetix.host", "localhost");
	}

	protected ExecutionRole createIndexFileModificationRole(ITasResolver tasResolver,
	        String tixchangeNodeContext, TomcatRole tomcatRole) {
		// modify datasources
		String configFile = tomcatRole.getWebappsDirectory() + tixchangeNodeContext
		        + BuilderBase.WIN_SEPARATOR + "index.html";
		Map<String, String> configData = Collections.singletonMap("localhost",
		        tasResolver.getHostnameById(TOMCAT_ROLE_ID));

		return new ExecutionRole.Builder(CONFIG_ROLE_ID).flow(FileModifierFlow.class,
		        new FileModifierFlowContext.Builder().replace(configFile, configData).build())
		        .build();
	}
	
	protected ExecutionRole createDBCredentialsModificationRole(ITasResolver tasResolver,
	        TomcatRole tomcatRole) {
		// update db credentials
		String configFile = tomcatRole.getWebappsDirectory() + "tixchangeRest"
		        + BuilderBase.WIN_SEPARATOR + "WEB-INF" + BuilderBase.WIN_SEPARATOR + "classes"
		        + BuilderBase.WIN_SEPARATOR + "db.config";
		Map<String, String> configData = new HashMap<String, String>();
		configData.put("dbuser=root",
		        "dbuser=" + "root");
		configData.put("dbpwd=",
		        "dbpwd=" + "root");

		return new ExecutionRole.Builder(DB_CONFIG_ROLE_ID).flow(FileModifierFlow.class,
		        new FileModifierFlowContext.Builder().replace(configFile, configData).build())
		        .build();
	}

	protected RedisRole creatRedisRole(ITasResolver tasResolver) {
		return new RedisRole.Builder(REDIS_ROLE_ID, tasResolver).artifact(RedisVersionArtifact.WINv2_8_21.getArtifact()).autoStart().build();
	}

	protected MysqlRole createMySqlRole() {
		String unpackDest = BuilderBase.WIN_SOFTWARE_LOC + TIXCHANGE_NODE_CONTEXT;

		// mysql role
		String sqlFile = getSqlScriptLocation(unpackDest);
		return new MysqlRole.Builder(MYSQL_ROLE_ID).importSql(sqlFile).build();
	}

	@NotNull
	private String getSqlScriptLocation(String unpackDest) {
		return unpackDest + BuilderBase.WIN_SEPARATOR + "nodetix.sql";
	}
}
