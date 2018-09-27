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
 */

package com.ca.apm.transactiontrace.appmap.testbed;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.transactiontrace.appmap.role.DeferredInitiateTransactionTraceSessionRole;
import com.ca.apm.transactiontrace.appmap.role.NodeJsAgentRole;
import com.ca.tas.artifact.built.tixchange.TixChangeNodeWarArtifact;
import com.ca.tas.artifact.built.tixchange.TixChangeRestWarArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.MysqlRole;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.role.RedisRole;
import com.ca.tas.role.TixChangeRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Test bed that installs
 *
 * on a CentOS machine
 * 1. Stand Alone EM
 * 2. TixChange test application (+ NodeJS, MySQL, Redis, Tomcat)
 * 3. NodeJS agent (= Collector Agent + NodeJS probe)
 * 
 * on a Windows machine
 * 1. Selenium web driver for Chrome
 * 
 * You might need to download artifacts from Artifactory in Santa Clara to your local repository prior to deploying the testbed:
 * 
 * mvn dependency:get -Dartifact=com.jtixchange:tixchangeNode:0.1-SNAPSHOT:zip -DremoteRepositories=http://oerth-scx.ca.com:8081/artifactory/repo -U
 * mvn dependency:get -Dartifact=com.jtixchange:tixchangeNode:0.1-SNAPSHOT:war -DremoteRepositories=http://oerth-scx.ca.com:8081/artifactory/repo -U
 * mvn dependency:get -Dartifact=com.jtixchange:tixchangeRest:0.1-SNAPSHOT:war -DremoteRepositories=http://oerth-scx.ca.com:8081/artifactory/repo -U
 *
 * mvn dependency:get -Dartifact=com.ca.apm.agent.CollectorAgent:CollectorAgent-dist:99.99.leo_node-SNAPSHOT:tar.gz:unix -DremoteRepositories=http://oerth-scx.ca.com:8081/artifactory/repo -U
 * mvn dependency:get -Dartifact=com.ca.apm.nodejs:nodejs-probe:1.0-SNAPSHOT:tgz -DremoteRepositories=http://oerth-scx.ca.com:8081/artifactory/repo -U
 */
@TestBedDefinition
public class NodeJsAgentStandAloneTestbed implements ITestbedFactory {

    private static final String TIXCHANGE_TEMPLATE_ID = ITestbedMachine.TEMPLATE_CO66;
    public static final String TIXCHANGE_MACHINE = "tixChangeMachine";
    private static final String NODEJS_ROLE_ID = "nodejs";
    private static final String MYSQL_ROLE_ID = "mysqlRole";
    private static final String REDIS_ROLE_ID = "redisRole";
    private static final String TIXCHANGE_VERSION = "0.1-SNAPSHOT";
    public static final String TOMCAT_ROLE_ID = "tomcatRole";
    private static final String CONFIG_ROLE = "configRole";
    public static final String TIXCHANGE_ROLE = "tixchangeRole";
    public static final String TIXCHANGE_NODE_CONTEXT = "tixchangeNode";

    // ---

    public static final String EM_ROLE_ID = "emRole";
    public static final String NODEJS_AGENT_ROLE_ID = "nodeJsAgentRole";
    public static final String INITIATE_TT_SESSION_ROLE_ID = "inititateTTSessionRole";

    private static final String COLLECTOR_AGENT_VERSION = "99.99.leo_node-SNAPSHOT";
    private static final String NODEJS_PROBE_VERSION = "1.0-SNAPSHOT";

    private static final String EM_CONF_PROP_TT_ARRIVAL_BUFFER_INCUB_TIME_FAST =
        "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        String tixchangeNodeUnpackDest = TasBuilder.LINUX_SOFTWARE_LOC + TIXCHANGE_NODE_CONTEXT;

        // mysql role
        String sqlFile = getSqlScriptLocation(tixchangeNodeUnpackDest);
        MysqlRole mysqlRole =
            new MysqlRole.LinuxBuilder(MYSQL_ROLE_ID).importSql(sqlFile).autoStart().build();

        // nodejs role
        NodeJsRole nodeJsRole =
            new NodeJsRole.LinuxBuilder(NODEJS_ROLE_ID, tasResolver).install("forever",
                Collections.singletonList("-g")).build();

        // tixchange role
        TixChangeRole tixChangeRole =
            new TixChangeRole.LinuxBuilder(TIXCHANGE_ROLE, tasResolver)
                .version(TIXCHANGE_VERSION)
                .destination(tixchangeNodeUnpackDest)
                .mysqlCreds("username", "password")
                .node(nodeJsRole)
                .mysqlCreds(mysqlRole.getEnvPropertyById(MysqlRole.ENV_MYSQL_USERNAME),
                    mysqlRole.getEnvPropertyById(MysqlRole.ENV_MYSQL_PASSWORD)).node(nodeJsRole)
                // .configDatasources("nodetix.host",
                // tasResolver.getHostnameById(mysqlRole.getRoleId()))
                .configDatasources("nodetix.host", "localhost").build();

        // redis role
        RedisRole redisRole =
            new RedisRole.LinuxBuilder(REDIS_ROLE_ID, tasResolver).autoStart().build();

        // tomcat role
        TomcatRole tomcatRole =
            new TomcatRole.LinuxBuilder(TOMCAT_ROLE_ID, tasResolver)
                .webApplication(
                    new TixChangeNodeWarArtifact(tasResolver).createArtifact(TIXCHANGE_VERSION),
                    TIXCHANGE_NODE_CONTEXT)
                .webApplication(
                    new TixChangeRestWarArtifact(tasResolver).createArtifact(TIXCHANGE_VERSION),
                    "tixchangeRest").tomcatVersion(TomcatVersion.v70).autoStart().build();

        ExecutionRole executionRole =
            modifyIndexFile(tasResolver, TIXCHANGE_NODE_CONTEXT, tomcatRole);

        // EM role
        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver).configProperty(
                EM_CONF_PROP_TT_ARRIVAL_BUFFER_INCUB_TIME_FAST, "30").build();

        // NodeJs agent role
        NodeJsAgentRole nodeJsAgentRole =
            new NodeJsAgentRole.LinuxBuilder(NODEJS_AGENT_ROLE_ID, tasResolver)
                .collectorAgentVersion(COLLECTOR_AGENT_VERSION).collectorAgentAutoStart()
                .nodeJsProbeVersion(NODEJS_PROBE_VERSION).nodeJsRole(nodeJsRole)
                .tixChangeRole(tixChangeRole).build();

        // initiate TT session role
        DeferredInitiateTransactionTraceSessionRole traceSessionRole =
            new DeferredInitiateTransactionTraceSessionRole.LinuxBuilder(
                INITIATE_TT_SESSION_ROLE_ID).emRole(emRole).build();

        ITestbedMachine tixChangeMachine =
            TestBedUtils.createLinuxMachine(TIXCHANGE_MACHINE, TIXCHANGE_TEMPLATE_ID, redisRole,
                mysqlRole, nodeJsRole, tixChangeRole, tomcatRole, executionRole, emRole,
                nodeJsAgentRole, traceSessionRole);

        emRole.before(tixChangeRole);

        tixChangeRole.before(nodeJsRole, mysqlRole, redisRole);

        nodeJsAgentRole.after(nodeJsRole, tixChangeRole);

        traceSessionRole.after(nodeJsAgentRole);

        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver);

        return new Testbed(getClass().getSimpleName())
            .addMachine(tixChangeMachine)
            .addMachines(seleniumGridMachines);
    }

    private ExecutionRole modifyIndexFile(ITasResolver tasResolver, String tixchangeNodeContext,
        TomcatRole tomcatRole) {
        // modify datasources
        String configFile =
            tomcatRole.getWebappsDirectory() + tixchangeNodeContext + TasBuilder.LINUX_SEPARATOR
                + "index.html";
        Map<String, String> configData =
            Collections.singletonMap("localhost", tasResolver.getHostnameById(TOMCAT_ROLE_ID));

        return new ExecutionRole.Builder(CONFIG_ROLE).flow(FileModifierFlow.class,
            new FileModifierFlowContext.Builder().replace(configFile, configData).build()).build();
    }

    @NotNull
    private String getSqlScriptLocation(String unpackDest) {
        return unpackDest + TasBuilder.LINUX_SEPARATOR + "nodetix.sql";
    }
}
