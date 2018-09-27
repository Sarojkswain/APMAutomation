/*
 * Copyright (c) 2016 CA.  All rights reserved.
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

package com.ca.tas.test.em.appmap;

import java.util.Arrays;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.PhantomJSRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

@TestBedDefinition
public class GartnerDemoTestbed implements ITestbedFactory {
    
    static public final String MACHINE_ID = "em";
    static public final String EM_ROLE_ID = "introscope";


    static public String CONFIG_VALUE_0 =
        "[{"
            + "\"metricSpecifier\": {"
            + "\"format\": \"Servlets|<servletClassName>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"section\": \"Application Metrics\","
            + "\"metricNames\": [\"Average Response Time (ms)\", \"Errors Per Interval\", \"Responses Per Interval\"]"
            + "}, {"
            + "\"metricSpecifier\": {"
            + "\"format\": \"(GC Monitor|CPU)\","
            + "\"type\": \"REGEX\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"section\": \"Resource Metrics\","
            + "\"metricNames\": [\"Percentage of Java Heap Used\", \"Utilization % (process)\",\"Idle %\"]"
            + "}]";
    static public String CONFIG_VALUE_1 =
        "[{"
            + "\"metricSpecifier\": {"
            + "\"format\": \"Backends|<name>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"section\": \"Application Metrics\","
            + "\"metricNames\": [\"Average Response Time (ms)\", \"Errors Per Interval\", \"Responses Per Interval\"]"
            + "}, {"
            + "\"metricSpecifier\": {"
            + "\"format\": \"DB2 z/OS Subsystems\\\\\\\\|<SSID>\\\\\\\\|[^|]*\","
            + "\"type\": \"REGEX\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {\"databaseType\": \"DB2\"},"
            + "\"section\": \"DB2 z/OS Subsystem Metrics\","
            + "\"metricNames\": [\"Active Log Space Available (%)\", \"EDM Pool Full Failures\", \"DBD Pool Full Failures\", \"Statement Pool Full Failures\", \"Queued Create Thread Requests\", \"Maximum Users (%)\", \"Maximum Remote Users (%)\"]"
            + "}, {"
            + "\"metricSpecifier\": {"
            + "\"format\": \"(GC Monitor|CPU)\","
            + "\"type\": \"REGEX\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"section\": \"Resource Metrics\","
            + "\"metricNames\": [\"Percentage of Java Heap Used\", \"Utilization % (process)\",\"Idle %\"]"
            + "}]";
    static public String CONFIG_VALUE_2 =
        "[{"
            + "\"metricSpecifier\": {"
            + "\"format\": \"WebServices|Client|<wsNamespace>|<wsOperation>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {\"backendName\": null},"
            + "\"section\": \"Application Metrics\","
            + "\"metricNames\": [\"Average Response Time (ms)\", \"Errors Per Interval\", \"Responses Per Interval\"]"
            + "}, {"
            + "\"metricSpecifier\": {"
            + "\"format\": \"Backends|<backendName>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {\"backendName\": \".*\"},"
            + "\"section\": \"Application Metrics\","
            + "\"metricNames\": [\"Average Response Time (ms)\", \"Errors Per Interval\", \"Responses Per Interval\"]"
            + "}, {"
            + "\"metricSpecifier\": {"
            + "\"format\": \"(GC Monitor|CPU)\","
            + "\"type\": \"REGEX\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"section\": \"Resource Metrics\","
            + "\"metricNames\": [\"Percentage of Java Heap Used\", \"Utilization % (process)\",\"Idle %\"]"
            + "}]";
    static public String CONFIG_VALUE_3 =
        "[{"
            + "\"metricSpecifier\": {"
            + "\"format\": \"Frontends|<name>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"section\": \"Application Metrics\","
            + "\"metricNames\": [\"Average Response Time (ms)\", \"Errors Per Interval\", \"Responses Per Interval\"]"
            + "}, {"
            + "\"metricSpecifier\": {"
            + "\"format\": \"(GC Monitor|CPU)\","
            + "\"type\": \"REGEX\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"section\": \"Resource Metrics\","
            + "\"metricNames\": [\"Percentage of Java Heap Used\", \"Utilization % (process)\",\"Idle %\"]"
            + "}]";
    static public String CONFIG_VALUE_4 =
        "[{"
            + "\"metricSpecifier\": {"
            + "\"format\": \"<name>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"section\": \"Application Metrics\","
            + "\"metricNames\": [\"Average Response Time (ms)\", \"Errors Per Interval\", \"Responses Per Interval\"]"
            + "}, {"
            + "\"metricSpecifier\": {"
            + "\"format\": \"(GC Monitor|CPU)\","
            + "\"type\": \"REGEX\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"<agent>\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"section\": \"Resource Metrics\","
            + "\"metricNames\": [\"Percentage of Java Heap Used\", \"Utilization % (process)\",\"Idle %\"]"
            + "}]";
    static public String CONFIG_VALUE_5 =
        "[{"
            + "\"metricSpecifier\": {"
            + "\"format\": \"By Frontend|<applicationName>|Health\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"agentSpecifier\": {"
            + "\"format\": \"Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Business Application Agent (Virtual)\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"section\": \"Application Metrics\","
            + "\"metricNames\": [\"Average Response Time (ms)\", \"Errors Per Interval\", \"Responses Per Interval\", \"Stall Count\", \"Concurrent Invocations\"]"
            + "}]";
    static public String CONFIG_VALUE_6 =
        "[{"
            + "\"agentSpecifier\": {"
            + "\"format\": \"Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Business Application Agent (Virtual)\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"metricNames\": [\"Average Response Time (ms)\",\"Average Response Time 95th Percentile (ms)\",\"Errors Per Interval\",\"Responses Per Interval\"],"
            + "\"metricSpecifier\": {"
            + "\"format\": \"By Business Service\\\\\\\\|<serviceId>\\\\\\\\|<name>( via [^\\|]+)?\\\\\\\\|[^\\|]*\","
            + "\"type\": \"REGEX\""
            + "},"
            + "\"section\": \"Application Metrics\""
            + "},"
            + "{"
            + "\"agentSpecifier\": {"
            + "\"format\": \"Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Business Application Agent (Virtual)\","
            + "\"type\": \"EXACT\""
            + "},"
            + "\"filter\": {},"
            + "\"metricNames\": [\"Average Browser Render Time (ms)\",\"Average Page Load Complete Time (ms)\",\"Average Time to First Byte (ms)\",\"Responses Per Interval\"],"
            + "\"metricSpecifier\": {"
            + "\"format\": \"By Business Service\\\\\\\\|<serviceId>\\\\\\\\|<name>( via [^\\|]+)?\\\\\\\\|[^\\|]*\\\\\\\\|Browser\","
            + "\"type\": \"REGEX\"" + "}," + "\"section\": \"Browser Metrics\"" + "}]";

    static public List<String> envList = Arrays.asList(
        "export JAVA_HOME=/opt/jdk1.8",
        "export APM_HOME=/opt/automation/deployed/em",
        "export DB_HOME=/opt/automation/deployed/database",
        "export DB_NAME=cemdb",
        "export DB_USER=postgres",
        "export DB_USER_PWD=Lister@123",
        "export DB_PORT=5432",
        "export DB_ADMIN_USER=admin",
        "export DB_ADMIN_USER_PWD=quality",
        "export DB_APM_VERSION=10.4",
        "export SETUP_HOME=/opt/automation/deployed/installers/em");

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("Introscope/AppMap/GartnerDemo");

        ITestbedMachine machine =
            new TestbedMachine.LinuxBuilder(MACHINE_ID).platform(Platform.LINUX)
                .templateId("rh66").bitness(Bitness.b64).build();

        Artifact gartnerdemo = new DefaultArtifact("com.ca.apm.binaries", "gartnerdemo", "tar", "1.0");
        GenericRole gartnerdemoRole =
            new GenericRole.Builder("gartnerdemo", tasResolver).unpack(gartnerdemo, "/opt/gartnerdemo").build();
        machine.addRole(gartnerdemoRole);

        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver).dbpassword("quality")
                .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001))
                .configProperty("introscope.public.restapi.enabled", "true")
                .configProperty("introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast", "30")
                .configProperty("introscope.apmserver.ui.configuration.name.0", "PERFORMANCE_SPECIFIER_SERVLET")
                .configProperty("introscope.apmserver.ui.configuration.value.0", CONFIG_VALUE_0)
                .configProperty("introscope.apmserver.ui.configuration.name.1", "PERFORMANCE_SPECIFIER_DATABASE")
                .configProperty("introscope.apmserver.ui.configuration.value.1", CONFIG_VALUE_1)
                .configProperty("introscope.apmserver.ui.configuration.name.2", "PERFORMANCE_SPECIFIER_WEBSERVICE")
                .configProperty("introscope.apmserver.ui.configuration.value.2", CONFIG_VALUE_2)
                .configProperty("introscope.apmserver.ui.configuration.name.3", "PERFORMANCE_SPECIFIER_GENERICFRONTEND")
                .configProperty("introscope.apmserver.ui.configuration.value.3", CONFIG_VALUE_3)
                .configProperty("introscope.apmserver.ui.configuration.name.4", "PERFORMANCE_SPECIFIER_GENERICBACKEND")
                .configProperty("introscope.apmserver.ui.configuration.value.4", CONFIG_VALUE_4)
                .configProperty("introscope.apmserver.ui.configuration.name.5", "PERFORMANCE_SPECIFIER_GROUP_APPLICATIONNAME")
                .configProperty("introscope.apmserver.ui.configuration.value.5", CONFIG_VALUE_5)
                .configProperty("introscope.apmserver.ui.configuration.name.6", "PERFORMANCE_SPECIFIER_BUSINESSTRANSACTION")
                .configProperty("introscope.apmserver.ui.configuration.value.6", CONFIG_VALUE_6)
                .nostartEM()
                .nostartWV().build();
        machine.addRole(emRole);
        emRole.after(gartnerdemoRole);
        
        RunCommandFlowContext mkdir = new RunCommandFlowContext.Builder("mkdir").args(Arrays.asList("/opt/setup")).build();
        IRole mkdirRole = new UniversalRole.Builder("mkdir", tasResolver).syncCommand(mkdir).build();
        machine.addRole(mkdirRole);
        FileCreatorFlowContext envContext = new FileCreatorFlowContext.Builder().fromData(envList).destinationPath("/opt/setup/env.sh").build();
        IRole createEnvRole = new UniversalRole.Builder("create_env", tasResolver)
                        .runFlow(FileCreatorFlow.class, envContext).build();
        machine.addRole(createEnvRole);
        createEnvRole.after(mkdirRole);
        
        FileModifierFlowContext deleteContext = new FileModifierFlowContext.Builder().delete(emRole.getDeployEmFlowContext().getInstallDir()
                        + "/config/modules/DefaultMM.jar").build();
        IRole deleteRole = new UniversalRole.Builder("delete_mm", tasResolver).runFlow(FileModifierFlow.class, deleteContext).build();
        deleteRole.after(emRole);
        machine.addRole(deleteRole);
        
        RoleUtility.addMmRole(machine, emRole.getRoleId() + "_mm", emRole, "GartnerDemoDefaultMM");

        FileCreatorFlowContext jsContext = new FileCreatorFlowContext.Builder()
                .fromResource("/MauiAgent-AggregatedTree.js")
                .destinationPath(emRole.getDeployEmFlowContext().getInstallDir()
                        + "/scripts/MauiAgent-AggregatedTree.js").build();
        IRole jsRole = new UniversalRole.Builder("create_js", tasResolver).runFlow(FileCreatorFlow.class, jsContext).build();
        jsRole.after(emRole);
        machine.addRole(jsRole);

        FileCreatorFlowContext mappingContext = new FileCreatorFlowContext.Builder()
                .fromResource("/gartnerdemo-teamcenter-status-mapping.properties")
                .destinationPath(emRole.getDeployEmFlowContext().getInstallDir()
                        + "/config/teamcenter-status-mapping.properties").build();
        IRole mappingRole = new UniversalRole.Builder("create_mapping", tasResolver)
                .runFlow(FileCreatorFlow.class, mappingContext).build();
        mappingRole.after(emRole, deleteRole);
        machine.addRole(mappingRole);
        
        RoleUtility.addStartEmRole(machine, emRole, true, mappingRole);
        
        /*RunCommandFlowContext cmd2 = new RunCommandFlowContext.Builder("2-add_perspectives.sh").workDir("/opt/gartnerdemo").build();
        IRole runCmdRole2 = new UniversalRole.Builder("run_cmd2", tasResolver).syncCommand(cmd2).build();
        machine.addRole(runCmdRole2);
        runCmdRole2.after(emRole, createEnvRole);*/

        PhantomJSRole phantomjsRole = new PhantomJSRole.LinuxBuilder("phantomjs", tasResolver).build();
        machine.addRole(phantomjsRole);

        testbed.addMachine(machine);

        return testbed;
    }
}
