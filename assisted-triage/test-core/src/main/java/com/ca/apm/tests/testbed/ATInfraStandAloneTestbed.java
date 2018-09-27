/*
 * Copyright (c) 2017 CA. All rights reserved.
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

package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.AgentCapable;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Deploys a single EM with test apps and does all our automation
 * testing.
 */
@TestBedDefinition
public class ATInfraStandAloneTestbed implements ITestbedFactory {

    public static final String SA_MASTER = "saMaster";
    public static final String TOMCAT_AGENT_ONLY = "tomcatAgentOnly";

    public static final String SA_MASTER_EM_ROLE = SA_MASTER + "-emRole";
    public static final String TOMCAT_AGENT_ONLY_ROLE = TOMCAT_AGENT_ONLY + "-tomcatRole";

    public static final String DEPLOY_DIR = "C:\\sw";
    public static final String EM_INSTALL_DIR = DEPLOY_DIR + "\\em";
    public static final String CLW_LOCATION = EM_INSTALL_DIR + "\\lib";
    public static final String TOMCAT_INSTALL_DIR = DEPLOY_DIR + "\\apache-tomcat-7.0.57";
    public static final String H2_LOCATION = DEPLOY_DIR + "\\H2";
    public static final String CPU_APP_LOCATION = "C:\\SW\\LoadGeneratorApp\\";
    
    public static final String ADMIN_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String ADMIN_TOKEN_CMDLINE = "-Dappmap.token=" + ADMIN_TOKEN
        + " -Dappmap.user=admin";

    public static final String PIPEORGAN_CONTEXT = "pipeorgan";
    public static final String H2_VERSION = "1.4.191";
    public static final String DB_PASSWORD = "quality";

    public static final String BAT_FILE = "run.bat";

    public static final int EMPORT = 8081;
    public static final int WVPORT = 8082;
    public static final int EM_SERVER_PORT = 5001;

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed(this.getClass().getSimpleName());
        
        ITestbedMachine saMasterMachine =
                TestBedUtils.createWindowsMachine(SA_MASTER, TEMPLATE_W64);
        EmRole saMaster =
            new EmRole.Builder(SA_MASTER_EM_ROLE, tasResolver)
                .installDir(EM_INSTALL_DIR)
                .emWebPort(EMPORT)
                .wvPort(WVPORT)
                .dbpassword(DB_PASSWORD)
                .emLaxNlJavaOption(Arrays.asList(ADMIN_TOKEN_CMDLINE))
                .configProperty("introscope.apmserver.teamcenter.master", "true")
                .configProperty(
                    "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast",
                    "30")
                .configProperty(
                    "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.slow",
                    "120")
                .configProperty("introscope.triage.uvb.relay.interval", "60000")
                .configProperty("introscope.triage.uvb.clear.interval", "90000")
                .configProperty("introscope.triage.alert.relay.interval", "60000")
                .configProperty("introscope.triage.alert.clear.interval", "90000")
                .configProperty("introscope.triage.error.relay.interval", "60000")
                .configProperty("introscope.triage.error.clear.interval", "90000")
                .configProperty("introscope.triage.suspect.events.expiration.time", "240000")
                .configProperty("log4j.logger.Manager.AT",
                    "TRACE#com.wily.util.feedback.Log4JSeverityLevel,console,logfile")
                .configProperty("enable.default.BusinessTransaction", "false")
                .configProperty("introscope.public.restapi.enabled", "true")
                .configProperty("introscope.enterprisemanager.appmap.em.topologyPoller", "true")
            .configProperty("introscope.apm.appmap.vertexCleaner.networkAttributePropagation", "true").build();
        IRole mmRoleSaMaster = getMMRole(EM_INSTALL_DIR, tasResolver, SA_MASTER + "-mmRole");
        mmRoleSaMaster.after(saMaster);
        saMasterMachine.addRole(saMaster, mmRoleSaMaster);
        
        ExecutionRole addCPUAlertMappingRole = addCpuAlertMappingtoTeamCenter(SA_MASTER + "-addAlertMappingRole", EM_INSTALL_DIR);
        addCPUAlertMappingRole.after(saMaster); 
        saMasterMachine.addRole(addCPUAlertMappingRole);

        ITestbedMachine tomcatAgentOnlyMachine =
            TestBedUtils.createWindowsMachine(TOMCAT_AGENT_ONLY, TEMPLATE_W64);
        IRole lastTestAppRoletomcatAgentOnly =
            addTestAppDeploymentRoles(TOMCAT_AGENT_ONLY, tasResolver, tomcatAgentOnlyMachine,
                                      saMaster);

        testbed.addMachine(saMasterMachine, tomcatAgentOnlyMachine);

        final String saMasterName = tasResolver.getHostnameById(SA_MASTER_EM_ROLE);

        IRole runCLWTraceRoleMomProvider = runCLWTrace(tasResolver, saMasterName);
        runCLWTraceRoleMomProvider.after(lastTestAppRoletomcatAgentOnly);
        saMasterMachine.addRole(runCLWTraceRoleMomProvider);

        IRole sleepRole = getSleepRole(SA_MASTER + "-sleepRole", tasResolver, 10 * 30);
        addCPUAlertMappingRole.before(sleepRole);
        sleepRole.after(lastTestAppRoletomcatAgentOnly,addCPUAlertMappingRole);
        saMasterMachine.addRole(sleepRole);

        return testbed;
    }

    private IRole addTestAppDeploymentRoles(String prefix, ITasResolver tasResolver,
        ITestbedMachine machine, EmRole emRole) {

        AgentCapable tomcatRole =
            getTomcatRole(prefix + "-tomcatRole", tasResolver, TOMCAT_INSTALL_DIR);
        IRole agentRole = getAgentRole(prefix + "-agentRole", tasResolver, tomcatRole, emRole);
        IRole h2Role = getH2DeployRole(tasResolver, prefix + "-copyH2ArtifactRole");
        IRole copyH2SchemaFileRole =
            getDeployDBScriptsRole(H2_LOCATION, tasResolver, prefix + "-copyH2SchemaFileRole");
        IRole startH2ServerRole =
            getStartH2ServerRole(H2_LOCATION, tasResolver, prefix + "-startH2ServerRole");
        IRole createH2SchemaRole =
            getCreateH2DBRole(H2_LOCATION, tasResolver, prefix + "-createH2SchemaRole");
        h2Role.after(agentRole);
        h2Role.before(startH2ServerRole);
        startH2ServerRole.before(createH2SchemaRole);
        createH2SchemaRole.after(copyH2SchemaFileRole);

        IRole pipeScriptsRole =
            getPipeScriptsDeployRole(TOMCAT_INSTALL_DIR + "\\webapps\\pipeorgan\\WEB-INF\\lib\\",
                tasResolver, prefix + "-pipeScriptsDeployRole");

        String normalScenarioScriptName = "";
        if (prefix.equalsIgnoreCase(TOMCAT_AGENT_ONLY)) {
            normalScenarioScriptName = "NormalScenario.xml";
        }

        IRole startNormalLoadRole =
            startNormalLoadRole(TOMCAT_INSTALL_DIR + "\\webapps\\pipeorgan\\WEB-INF\\lib\\",
                tasResolver, prefix + "-startNormalLoadRole", normalScenarioScriptName);

        pipeScriptsRole.after(createH2SchemaRole);
        startNormalLoadRole.after(pipeScriptsRole);
        

        IRole cpuAppDeployRole =
                deployCPURole(CPU_APP_LOCATION,
                    tasResolver, prefix + "-cpuAppDeployRole");

        // add only roles created here
        machine.addRole(startNormalLoadRole, pipeScriptsRole, createH2SchemaRole, startH2ServerRole, copyH2SchemaFileRole, h2Role,
            agentRole, tomcatRole, cpuAppDeployRole);

        // return the last role in the chain created here
        return startNormalLoadRole;
    }

    private IRole getAgentRole(String roleId, ITasResolver tasResolver, AgentCapable tomcatRole,
        EmRole emRole) {

        Map<String, String> agentProperties = new HashMap<String, String>();

        agentProperties.put("introscope.agent.ttClamp", "2000");
        agentProperties.put("introscope.agent.transactiontracer.antifloodthreshold", "2000");

        return (new AgentRole.Builder(roleId, tasResolver))
            .intrumentationLevel(AgentInstrumentationLevel.FULL).webAppServer(tomcatRole)
            .additionalProperties(agentProperties).emRole(emRole).webAppAutoStart().build();
    }

    private AgentCapable getTomcatRole(String roleId, ITasResolver tasResolver,
        String installLocation) {

        return new TomcatRole.Builder(roleId, tasResolver).tomcatVersion(TomcatVersion.v70)
            .installDir(installLocation)
            .webApplication(getPipeOrganArtifact(tasResolver), PIPEORGAN_CONTEXT).build();
    }

    private IRole runCLWTrace(ITasResolver tasResolver, String machineName) {

        String fileName = "CLWTraceFile.bat";
        String runCLWBatLocation = DEPLOY_DIR + "\\" + fileName;
        Collection<String> data =
            Arrays
                .asList("cd "
                    + CLW_LOCATION
                    + " && java -Xmx256M -Duser=admin -Dpassword= -Dhost="
                    + machineName
                    + " -Dport=5001 -jar \""
                    + CLW_LOCATION
                    + "\\CLWorkstation.jar\" trace transactions exceeding 1 ms in agents matching .* for 60 seconds");

        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create(runCLWBatLocation, data).build();

        String roleName = fileName + "Role";
        ExecutionRole execRole =
            new ExecutionRole.Builder(roleName).flow(FileModifierFlow.class, createFileFlow)
                .syncCommand(new RunCommandFlowContext.Builder(runCLWBatLocation).build()).build();

        return execRole;
    }

    private IRole startNormalLoadRole(String location, ITasResolver tasResolver, String roleName,
        String fileName) {
        String command =
            "start /d " + location + " " + BAT_FILE + " \"" + location + "*\" ./scenarios/"
                + fileName;

        RunCommandFlowContext runCommandFlowContext =
            new RunCommandFlowContext.Builder("").args(Arrays.asList(command)).build();

        ExecutionRole execRole =
            new ExecutionRole.Builder(roleName).asyncCommand(runCommandFlowContext).build();

        return execRole;
    }

    private IRole getMMRole(String emLocation, ITasResolver tasResolver, String roleName) {

        DefaultArtifact mmJar =
            new DefaultArtifact("com.ca.apm.tests", "assisted-triage-core", "alertsMM", "jar",
                tasResolver.getDefaultVersion());

        String deployLocation = emLocation + "\\deploy\\alertMM.jar";
        GenericRole mmRole =
            new GenericRole.Builder(roleName, tasResolver).download(mmJar, deployLocation).build();

        return mmRole;
    }

    private IRole getPipeScriptsDeployRole(String location, ITasResolver tasResolver,
        String roleName) {

        DefaultArtifact zip =
            new DefaultArtifact("com.ca.apm.tests", "assisted-triage-core", "pipescripts", "zip",
                tasResolver.getDefaultVersion());

        GenericRole mmRole =
            new GenericRole.Builder(roleName, tasResolver).unpack(zip, location).build();

        return mmRole;
    }

    private ITasArtifact getPipeOrganArtifact(ITasResolver tasResolver) {

        return new TasArtifact.Builder("pipeorgan_web").version(tasResolver.getDefaultVersion())
            .extension(TasExtension.WAR).groupId("com.ca.apm.coda-projects.test-tools.pipeorgan")
            .build();
    }

    private IRole getH2DeployRole(ITasResolver tasResolver, String roleName) {
        DefaultArtifact h2Artifact =
            new DefaultArtifact("com.h2database", "h2", "", "jar", H2_VERSION);
        GenericRole h2Role =
            new GenericRole.Builder(roleName, tasResolver).download(h2Artifact,
                H2_LOCATION + "\\h2.jar").build();

        return h2Role;
    }

    private IRole getDeployDBScriptsRole(String location, ITasResolver tasResolver, String roleName) {

        ClassLoader classLoader = getClass().getClassLoader();
        ExecutionRole pipeorganDBScriptRole = null;
        try {
            ArrayList<String> scripts = new ArrayList<String>(Arrays.asList("PipeOrganDB.sql"));

            for (String fileName : scripts) {
                String result =
                    IOUtils.toString(classLoader.getResourceAsStream("mysql/" + fileName));
                Collection<String> data = Arrays.asList(result);
                FileModifierFlowContext createFileFlow =
                    new FileModifierFlowContext.Builder().create(location + "\\" + fileName, data)
                        .build();
                pipeorganDBScriptRole =
                    new ExecutionRole.Builder(roleName)
                        .flow(FileModifierFlow.class, createFileFlow).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pipeorganDBScriptRole;
    }

    private IRole getStartH2ServerRole(String location, ITasResolver tasResolver, String roleName) {
        String h2JarLocation = location + "\\h2.jar";
        String startH2BatLocation = location + "\\startH2.bat";

        Collection<String> data =
            Arrays.asList("java -cp " + h2JarLocation + " org.h2.tools.Server -webPort 8085");
        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create(startH2BatLocation, data).build();

        ExecutionRole execRole =
            new ExecutionRole.Builder(roleName)
                .flow(FileModifierFlow.class, createFileFlow)
                .asyncCommand(
                    new RunCommandFlowContext.Builder("").args(
                        Arrays.asList("START /D " + location + " startH2.bat")).build()).build();
        return execRole;
    }

    private IRole getCreateH2DBRole(String h2location, ITasResolver tasResolver, String roleName) {

        String h2URL = "jdbc:h2:tcp://localhost/~/test";
        String h2JarLocation = h2location + "\\h2.jar";
        String dbScriptLocation = h2location + "\\PipeOrganDB.sql";

        String createDBBatLocation = h2location + "\\createDBSchema.bat";
        String command =
            "java -classpath " + h2JarLocation + " org.h2.tools.RunScript" + " -url " + h2URL
                + " -script " + dbScriptLocation;

        Collection<String> data = Arrays.asList(command);
        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create(createDBBatLocation, data).build();

        ExecutionRole execRole =
            new ExecutionRole.Builder(roleName)
                .flow(FileModifierFlow.class, createFileFlow)
                .asyncCommand(
                    new RunCommandFlowContext.Builder("").args(
                        Arrays.asList("START /D " + h2location + " createDBSchema.bat")).build())
                .build();
        return execRole;
    }

    private ExecutionRole getSleepRole(String roleName, ITasResolver tasResolver,
        long durationInSeconds) {

        String timeoutBatFile = DEPLOY_DIR + "\\sleep.bat";
        Collection<String> data =
            Arrays.asList("echo Sleeping for " + durationInSeconds, "ping 127.0.0.1 -n "
                + durationInSeconds + " > NUL");

        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create(timeoutBatFile, data).build();

        ExecutionRole execRole =
            new ExecutionRole.Builder(roleName).flow(FileModifierFlow.class, createFileFlow)
                .syncCommand(new RunCommandFlowContext.Builder(timeoutBatFile).build()).build();

        return execRole;
    }
    
    private IRole deployCPURole(String cpuAppLocation, ITasResolver tasResolver, String roleName) {
        DefaultArtifact loadJar =
            new DefaultArtifact("com.ca.apm.tests", "assisted-triage-core", "cpuLoadGenerator", "zip",
                tasResolver.getDefaultVersion());

        GenericRole cpuRole =
            new GenericRole.Builder(roleName, tasResolver).unpack(loadJar, cpuAppLocation).build();

        return cpuRole;
    }

    
    //role to add processor mapping to team-center-mappings file
    protected ExecutionRole addCpuAlertMappingtoTeamCenter(String roleName, String location) 
    {
     //location = EM_INSTALL_DIR + "\\config\\teamcenter-status-mapping.properties";
   
        Set<String> configData = new HashSet<String>();
   
        String configFile =   location + "\\config\\teamcenter-status-mapping.properties";
        configData.add("AGENT.3=CPU|Processor 0");
        configData.add("AGENT.4=CPU|Processor 1");

        return  new ExecutionRole.Builder(roleName).flow(FileModifierFlow.class,
                         new FileModifierFlowContext.Builder().append(configFile, configData).build())
                          .build();
    }

}
