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
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
//import com.ca.apm.tests.artifact.CollectorAgentArtifact;
import com.ca.apm.tests.artifact.HelloWorldAppArtifact;
import com.ca.apm.tests.artifact.NodeJSProbeArtifact;
import com.ca.apm.tests.artifact.NodeJSRuntimeVersionArtifact;
//import com.ca.apm.tests.role.CollectorAgentRole;
import com.ca.apm.tests.role.HelloWorldAppRole;
import com.ca.apm.tests.role.MongoDBRole;
import com.ca.apm.tests.role.NodeJSAppRole;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.apm.tests.role.TixChangeRole;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.built.tixchange.TixChangeNodeWarArtifact;
import com.ca.tas.artifact.built.tixchange.TixChangeRestWarArtifact;
import com.ca.apm.tests.artifact.UMAgentArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.MysqlRole;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.role.RedisRole;
import com.ca.tas.role.linux.YumInstallPackageRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.apm.tests.flow.DeployUMAgentFlow;
import com.ca.apm.tests.flow.DeployUMAgentFlowContext;
import com.ca.apm.tests.role.UMAgentRole;


/**
 * NodeJS Agent automation testbed for recommended LTS nodejs runtime
 *
 * @author Jan Pojer (pojja01@ca.com)
 * @author Karmjit Singh (sinka08@ca.com)
 * @author zheji01@ca.com
 */
@TestBedDefinition
public class NodeJSAgentTestbed implements ITestbedFactory {

    protected static final String TEMPLATE_ID = ITestbedMachine.TEMPLATE_CO_LATEST;
    protected static final String TIXCHANGE_VERSION = "0.1-SNAPSHOT";
    protected static final String NODEJS_PROBE_VERSION = "leo_node-SNAPSHOT";
    public static final String NODEJS_MACHINE = "nodejsMachine";
    protected static final String NODEJS_ROLE_ID = "nodejsRole";
    public static final String TIXCHANGE_PROBE_ROLE_ID = "tixChangeProbeRole";
    public static final String HELLOWORLD_PROBE_ROLE_ID = "helloWorldAppProbeRole";
    protected static final String MYSQL_ROLE_ID = "mysqlRole";
    protected static final String REDIS_ROLE_ID = "redisRole";
    protected static final String TOMCAT_ROLE_ID = "tomcatRole";
    protected static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    protected static final String CONFIG_ROLE_ID = "configRole";
    protected static final String DB_CONFIG_ROLE_ID = "restAppDBConfigRole";
    protected static final String TESTDATA_ROLE_ID = "testDataRole";
    public static final String TIXCHANGE_ROLE_ID = "tixchangeRole";
    public static final String HELLOWORLD_APP_ROLE_ID = "helloWorldAppRole";
//    public static final String COLLECTOR_AGENT_ROLE_ID = "collectorAgentRole";
    protected static final String MONGODB_ROLE_ID = "mongodbRole";
    public static final String UMAGENT_ROLE_ID = "umAgentRole";

    protected static final String TIXCHANGE_NODE_CONTEXT = "tixchangeNode";
    public static final String TESTDATA_DEPLOY_DIR = BuilderBase.LINUX_SOFTWARE_LOC + "testdata";
    
    // testbed properties
    public static final String MOCKEM_HOST_KEY = "mockEmHost";
    public static final String MOCKEM_PORT_KEY = "mockEmPort";
    public static final String NODEJS_PROBE_ARTIFACT_NAME = "nodeJsProbeArtifact";
    public static final String PUBLISHED_PROBE_PACKAGE_NAME = "ca-apm-probe";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        // mysql role
        MysqlRole mysqlRole = createMySqlRole();

        // nodejs role
        NodeJsRole nodeJsRole = createNodeJsRole(tasResolver);

        TixChangeRole tixChangeRole = createTixChangeRole(tasResolver, mysqlRole, nodeJsRole);

        // redis role
        RedisRole redisRole = creatRedisRole(tasResolver);
        
          // mongodb role
        MongoDBRole mongodbRole = createMongoDBRole();
        
        // tomcat role, don't auto start here bcz we want to deploy java agent
        // on it
        TomcatRole tomcatRole = createTomcatRole(tasResolver);

        // role for configuring tixchangeNode app on tomcat
        ExecutionRole indexConfigRole = createIndexFileModificationRole(tasResolver,
                TIXCHANGE_NODE_CONTEXT, tomcatRole);
        
        // role for updating db credentials for tixChangeRest app
        ExecutionRole dbConfigRole = createDBCredentialsModificationRole(tasResolver, tomcatRole, mysqlRole);
        
        
        HelloWorldAppRole helloWorldAppRole = createHelloWorldAppRole(tasResolver, nodeJsRole);
        
        // fetch artifact containing test data 
        GenericRole testDataDeployRole = createTestDataDeployRole(tasResolver);

        String mockEMHost = getMockEMHostAndPort(tasResolver, tixChangeRole)[0];
        int mockEMPort = Integer.parseInt(getMockEMHostAndPort(tasResolver, tixChangeRole)[1]);

        // java agent on tomcat, start tomcat using webAppAutoStart() if needed
        AgentRole tomcatAgentRole = new AgentRole.LinuxBuilder(TOMCAT_AGENT_ROLE_ID, tasResolver)
            .webAppServer(tomcatRole).webAppAutoStart().overrideEM(mockEMHost, mockEMPort).build();

//        CollectorAgentRole collectorAgentRole = createCollectorAgentRole(tasResolver, mockEMHost,
//                mockEMPort);

        UMAgentRole umAgentRole = createUMAgentRole(tasResolver, mockEMHost, mockEMPort);
        
        NodeJSProbeRole tixChangeProbeRole = createNodeJsProbeRole(tasResolver, umAgentRole,
                tixChangeRole, nodeJsRole, TIXCHANGE_PROBE_ROLE_ID);
        
        NodeJSProbeRole helloWorldAppProbeRole = createNodeJsProbeRole(tasResolver,
            umAgentRole, helloWorldAppRole, nodeJsRole, HELLOWORLD_PROBE_ROLE_ID);
        
        IRole prerequisitesRole = new YumInstallPackageRole.Builder("yumInstallPrerequisites")
            .addPackage("gcc-c++")
            .build();

        // set ordering of role execution
        tomcatRole.before(indexConfigRole, dbConfigRole);
        tixChangeRole.before(nodeJsRole, mysqlRole, redisRole, mongodbRole);
        tixChangeProbeRole.after(tixChangeRole, nodeJsRole);
        helloWorldAppProbeRole.after(helloWorldAppRole, nodeJsRole);
        nodeJsRole.before(helloWorldAppRole);
        prerequisitesRole.before(nodeJsRole);
        
        ITestbedMachine nodeMachine = TestBedUtils.createLinuxMachine(NODEJS_MACHINE, TEMPLATE_ID,
                redisRole, mongodbRole, mysqlRole, nodeJsRole, tixChangeRole, tixChangeProbeRole, tomcatRole, indexConfigRole, dbConfigRole,
                tomcatAgentRole, helloWorldAppRole, helloWorldAppProbeRole, testDataDeployRole,
                prerequisitesRole, umAgentRole);

        ITestbed testbed = new Testbed(getClass().getSimpleName());
        testbed.addProperty(MOCKEM_HOST_KEY, mockEMHost);
        testbed.addProperty(MOCKEM_PORT_KEY, String.valueOf(mockEMPort));
        testbed.addMachine(nodeMachine);

        return testbed;
    }
    
    public String[] getMockEMHostAndPort(ITasResolver tasResolver, TixChangeRole tixChangeRole)
    {
        String mockEMHost = tasResolver.getHostnameById(tixChangeRole.getRoleId());
        int mockEMPort = DeployEMFlowContext.EM_PORT;
        
        return new String[] {mockEMHost, String.valueOf(mockEMPort)};
    }

//    protected CollectorAgentRole createCollectorAgentRole(ITasResolver tasResolver, EmRole emRole) {
//        return createCollectorAgentRole(tasResolver,
//                tasResolver.getHostnameById(emRole.getRoleId()), emRole.getEmPort());
//    }
//
//    protected CollectorAgentRole createCollectorAgentRole(ITasResolver tasResolver, String emHost,
//            int emPort) {
//        return createBasicCollectorAgentRoleBuilder(tasResolver, emHost, emPort).build();
//    }
//
//    protected CollectorAgentRole.Builder createBasicCollectorAgentRoleBuilder(
//            ITasResolver tasResolver, String emHost, int emPort) {
//        return new CollectorAgentRole.LinuxBuilder(COLLECTOR_AGENT_ROLE_ID, tasResolver)
//                .version(
//                        new CollectorAgentArtifact(ArtifactPlatform.UNIX, tasResolver,
//                                CollectorAgentArtifact.Runtime.NODEJS))
//                .version(getCollectorAgentVersion(tasResolver)).overrideEM(emHost, emPort)
//                .setTcpLocalMode(true);
//    }
    
    protected NodeJsRole createNodeJsRole(ITasResolver tasResolver) {
        return new NodeJsRole.LinuxBuilder(NODEJS_ROLE_ID, tasResolver)
		        .versionNodeJs(NodeJSRuntimeVersionArtifact.LINUXx64v6_8_1.getArtifact())
                .install("forever", Collections.singletonList("-g")).build();
    }

    protected NodeJSProbeRole createNodeJsProbeRole(ITasResolver tasResolver,
            UMAgentRole umAgentRole, NodeJSAppRole nodejsAppRole,
            NodeJsRole nodeJsRole, String roleId) {

        Artifact artifact = new NodeJSProbeArtifact(tasResolver).createArtifact(
                getNodeJsProbeVersion(tasResolver)).getArtifact();
        NodeJSProbeRole role = new NodeJSProbeRole.LinuxBuilder(roleId, tasResolver)
                .version(artifact).UMAgentRole(umAgentRole)
                .nodeJSAppRole(nodejsAppRole).nodeJSRole(nodeJsRole).build();

        String artifactName = tasResolver.getArtifactUrl(artifact).getFile();
        if (artifactName != null) {
            role.addProperty(NODEJS_PROBE_ARTIFACT_NAME, artifactName);
        }
        return role;
    }

    protected TomcatRole createTomcatRole(ITasResolver tasResolver) {
        TomcatRole tomcatRole = new TomcatRole.LinuxBuilder(TOMCAT_ROLE_ID, tasResolver)
                .webApplication(
                        new TixChangeNodeWarArtifact(tasResolver).createArtifact(TIXCHANGE_VERSION),
                        TIXCHANGE_NODE_CONTEXT)
                .webApplication(
                        new TixChangeRestWarArtifact(tasResolver).createArtifact(TIXCHANGE_VERSION),
                        "tixchangeRest").tomcatVersion(TomcatVersion.v70).build();

        return tomcatRole;
    }

    protected TixChangeRole createTixChangeRole(ITasResolver tasResolver, MysqlRole mysqlRole,
            NodeJsRole nodeJsRole) {
        TixChangeRole tixChangeRole = createBasicTixChangeRoleBuilder(tasResolver, mysqlRole,
                nodeJsRole).build();
        return tixChangeRole;
    }

    protected TixChangeRole.Builder createBasicTixChangeRoleBuilder(ITasResolver tasResolver,
            MysqlRole mysqlRole, NodeJsRole nodeJsRole) {

        return new TixChangeRole.LinuxBuilder(TIXCHANGE_ROLE_ID, tasResolver)
                .version(TIXCHANGE_VERSION)
                .destination(BuilderBase.LINUX_SOFTWARE_LOC + TIXCHANGE_NODE_CONTEXT)
                .mysqlCreds(mysqlRole.getEnvPropertyById(MysqlRole.ENV_MYSQL_USERNAME),
                        mysqlRole.getEnvPropertyById(MysqlRole.ENV_MYSQL_PASSWORD))
                .node(nodeJsRole).configDatasources("nodetix.host", "localhost")
                .configConfig("useClickstream", "true");  //-- new add --//
    }

    protected ExecutionRole createIndexFileModificationRole(ITasResolver tasResolver,
            String tixchangeNodeContext, TomcatRole tomcatRole) {
        // modify datasources
        String configFile = tomcatRole.getWebappsDirectory() + tixchangeNodeContext
                + BuilderBase.LINUX_SEPARATOR + "index.html";
        Map<String, String> configData = Collections.singletonMap("localhost",
                tasResolver.getHostnameById(TOMCAT_ROLE_ID));

        return new ExecutionRole.Builder(CONFIG_ROLE_ID).flow(FileModifierFlow.class,
                new FileModifierFlowContext.Builder().replace(configFile, configData).build())
                .build();
    }
    
    protected ExecutionRole createDBCredentialsModificationRole(ITasResolver tasResolver,
            TomcatRole tomcatRole, MysqlRole mysqlRole) {
        // update db credentials
        String configFile = tomcatRole.getWebappsDirectory() + "tixchangeRest"
                + BuilderBase.LINUX_SEPARATOR + "WEB-INF" + BuilderBase.LINUX_SEPARATOR + "classes"
                + BuilderBase.LINUX_SEPARATOR + "db.config";
        Map<String, String> configData = new HashMap<>();
        configData.put("dbuser=root",
                "dbuser=" + mysqlRole.getEnvPropertyById(MysqlRole.ENV_MYSQL_USERNAME));
        configData.put("dbpwd=",
                "dbpwd=" + mysqlRole.getEnvPropertyById(MysqlRole.ENV_MYSQL_PASSWORD));

        return new ExecutionRole.Builder(DB_CONFIG_ROLE_ID).flow(FileModifierFlow.class,
                new FileModifierFlowContext.Builder().replace(configFile, configData).build())
                .build();
    }
    
    protected HelloWorldAppRole createHelloWorldAppRole(ITasResolver tasResolver,
            NodeJsRole nodeJsRole) {
        String installDir = BuilderBase.LINUX_SOFTWARE_LOC + "nodejs-apps";
        return new HelloWorldAppRole.LinuxBuilder(HELLOWORLD_APP_ROLE_ID, tasResolver)
                .artifact(new HelloWorldAppArtifact(tasResolver).createArtifact().getArtifact())
                .installDir(installDir).nodeJSRole(nodeJsRole).build();
    }
    
    protected GenericRole createTestDataDeployRole(ITasResolver tasResolver) {
        String unpackDest = TESTDATA_DEPLOY_DIR;
        DefaultArtifact artifact = new DefaultArtifact("com.ca.apm.tests", "nodeagent-tests-core",
                "testdata", TasExtension.ZIP.toString(), tasResolver.getDefaultVersion());

        GenericRole role = new GenericRole.Builder(TESTDATA_ROLE_ID, tasResolver).unpack(artifact,
                unpackDest).build();
        return role;
    }

    protected RedisRole creatRedisRole(ITasResolver tasResolver) {
        return new RedisRole.LinuxBuilder(REDIS_ROLE_ID, tasResolver).autoStart().build();
    }

    protected MysqlRole createMySqlRole() {
        String unpackDest = BuilderBase.LINUX_SOFTWARE_LOC + TIXCHANGE_NODE_CONTEXT;

        // mysql role
        String sqlFile = getSqlScriptLocation(unpackDest);
        return new MysqlRole.LinuxBuilder(MYSQL_ROLE_ID).importSql(sqlFile).autoStart().build();
    }
    
    protected MongoDBRole createMongoDBRole() {
        return new MongoDBRole.LinuxBuilder(MONGODB_ROLE_ID).autoStart().build();
    }
    
    protected UMAgentRole createUMAgentRole(ITasResolver tasResolver, String emHost, int emPort)
    {
        return new UMAgentRole.LinuxBuilder(UMAGENT_ROLE_ID, tasResolver)
        .version(new UMAgentArtifact(ArtifactPlatform.UNIX, tasResolver))
        .version(getUMAgentVersion(tasResolver))
        .overrideEM(emHost, emPort)
        .setTcpLocalMode(true)
        .build();
    }

    @NotNull
    private String getSqlScriptLocation(String unpackDest) {
        return unpackDest + BuilderBase.LINUX_SEPARATOR + "nodetix.sql";
    }
    
    protected String getNodeJsProbeVersion(ITasResolver tasResolver) {
        return NODEJS_PROBE_VERSION;
    }
    
    protected String getUMAgentVersion(ITasResolver tasResolver) {
        return tasResolver.getDefaultVersion();
    }

//    protected String getCollectorAgentVersion(ITasResolver tasResolver) {
//        //return tasResolver.getDefaultVersion();
//        return "10.3.0-SNAPSHOT";
//    }
}
