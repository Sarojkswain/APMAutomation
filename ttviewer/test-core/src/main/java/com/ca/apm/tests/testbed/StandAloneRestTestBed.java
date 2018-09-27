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
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.role.WLSAgentAppDeployRole;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.AgentCapable;
import com.ca.tas.role.webapp.JavaRole;
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
public class StandAloneRestTestBed implements ITestbedFactory {

    public static final String SA_MASTER = "saMaster";
    public static final String TOMCAT_AGENT_ONLY = "tomcatAgentOnly";
    public static final String WLS_AGENT_ONLY = "wlsAgentOnly";
    public static final String SA_MASTER_EM_ROLE = SA_MASTER + "-emRole";
    public static final String TOMCAT_AGENT_ONLY_EM_ROLE = TOMCAT_AGENT_ONLY + "-emRole";

    public static final String DEPLOY_DIR = "C:\\sw";
    public static final String EM_INSTALL_DIR = DEPLOY_DIR + "\\em";
    public static final String CLW_LOCATION = EM_INSTALL_DIR + "\\lib";
    public static final String TOMCAT_INSTALL_DIR = DEPLOY_DIR + "\\apache-tomcat-7.0.57";
    public static final String H2_LOCATION = DEPLOY_DIR + "\\H2";
    public static final String ADMIN_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String ADMIN_TOKEN_CMDLINE = "-Dappmap.token=" + ADMIN_TOKEN
        + " -Dappmap.user=admin";

    public static final String VERSION = "10.5.0-SNAPSHOT";

    public static final String PIPEORGAN_CONTEXT = "pipeorgan";
    public static final String H2_VERSION = "1.4.191";
    public static final String DB_PASSWORD = "quality";

    public static final String BAT_FILE = "run.bat";

    public static final int EMPORT = 8081;
    public static final int WVPORT = 8082;

    private JavaRole javaRole;
    protected boolean isJassEnabled = false;
    protected boolean isLegacyMode = false;
    protected static final String DEPLOY_BASE = TasBuilder.WIN_SOFTWARE_LOC;
    protected static final String WLS12C_INSTALL_HOME = DEPLOY_BASE + "Oracle/Middleware12.1.3";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed(this.getClass().getSimpleName());

        // Standalone EM Machine
        ITestbedMachine saMasterMachine =
            TestBedUtils.createWindowsMachine(SA_MASTER, TEMPLATE_W64);
        EmRole saMaster =
            new EmRole.Builder(SA_MASTER_EM_ROLE, tasResolver)
                .installDir(EM_INSTALL_DIR)
                .emWebPort(EMPORT)
                .wvPort(WVPORT)
                // .version(VERSION)
                .dbpassword(DB_PASSWORD)
                .emLaxNlJavaOption(Arrays.asList(ADMIN_TOKEN_CMDLINE))
                .configProperty("introscope.apmserver.teamcenter.master", "true")
                .configProperty(
                    "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast",
                    "10")
                .configProperty(
                    "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.slow",
                    "60").configProperty("enable.default.BusinessTransaction", "true").build();
        IRole mmRoleSaMaster = getMMRole(EM_INSTALL_DIR, tasResolver, SA_MASTER + "-mmRole");
        mmRoleSaMaster.after(saMaster);
        saMasterMachine.addRole(saMaster, mmRoleSaMaster);
        IRole lastTestAppRoleColToMomProv =
            addTestAppDeploymentRoles(SA_MASTER, tasResolver, saMasterMachine, saMaster);

        // Tomcat Agent Machine
        ITestbedMachine tomcatAgentOnlyMachine =
            TestBedUtils.createWindowsMachine(TOMCAT_AGENT_ONLY, TEMPLATE_W64);
        IRole lastTestAppRoletomcatAgentOnly =
            addTestAppDeploymentRoles(TOMCAT_AGENT_ONLY, tasResolver, tomcatAgentOnlyMachine,
                saMaster);
        mmRoleSaMaster.after(lastTestAppRoletomcatAgentOnly);


        // WebSphere Agent Machine
        ITestbedMachine wlsAgentOnlyMachine =
            TestBedUtils.createWindowsMachine(WLS_AGENT_ONLY, TEMPLATE_W64);
        // IRole wlsAppRoleAgentOnly =
        addWeblogicRoles(WLS_AGENT_ONLY, tasResolver, wlsAgentOnlyMachine);

        // wlsAgentOnlyMachine.addRole(wlsAppRoleAgentOnly);


        final String saMasterName = tasResolver.getHostnameById(SA_MASTER_EM_ROLE);

        IRole runCLWTraceRoleMaster = runCLWTrace(tasResolver, saMasterName);
        runCLWTraceRoleMaster.after(lastTestAppRoleColToMomProv, saMaster);
        saMasterMachine.addRole(runCLWTraceRoleMaster);

        testbed.addMachine(saMasterMachine, tomcatAgentOnlyMachine, wlsAgentOnlyMachine);
        return testbed;
    }

    // Adds all roles to WLS agent machine
    protected void addWeblogicRoles(String wlsRoleId, ITasResolver tasResolver,
        ITestbedMachine machine) {

        String agentVersion = tasResolver.getDefaultVersion();

        // install wls
        javaRole =
            new JavaRole.Builder(wlsRoleId + "_" + "java8Role", tasResolver).version(
                JavaBinary.WINDOWS_64BIT_JDK_18_0_51).build();

        GenericRole wlsRole = getwls12cRole(tasResolver, wlsRoleId);

        WLSAgentAppDeployRole wlsAgentPORole =
            new WLSAgentAppDeployRole.Builder(machine.getMachineId() + "_" + wlsRoleId, tasResolver)
                .agentVersion(agentVersion).classifier("jvm7-genericnodb")
                .isLegacyMode(isLegacyMode).isJassEnabled(isJassEnabled).javaRole(javaRole)
                .serverPort("7001").wlsRole(wlsRoleId)
                .emHost(tasResolver.getHostnameById(SA_MASTER_EM_ROLE)).build();

        // start web logic server
        IRole startWlsServerRole =
            startWlsServerRole(tasResolver, wlsRoleId + "-startWlsServerRole");

        // copy wls pipeorgan scripts
        IRole wlsScriptsRole =
            getWlsScriptsDeployRole("C:\\automation\\deployed", tasResolver, wlsRoleId
                + "-wlsScriptsDeployRole");

        // change hostname for scripts and start load
        startWlsScripts(wlsRoleId, tasResolver, machine, wlsScriptsRole);

        javaRole.before(wlsRole);
        wlsRole.before(wlsAgentPORole);
        startWlsServerRole.after(wlsAgentPORole);
        wlsScriptsRole.after(startWlsServerRole);
        machine.addRole(javaRole, wlsRole, wlsAgentPORole, startWlsServerRole, wlsScriptsRole);
    }

    private IRole addTestAppDeploymentRoles(String prefix, ITasResolver tasResolver,
        ITestbedMachine machine, EmRole emRole) {

        final String hostname = tasResolver.getHostnameById(SA_MASTER_EM_ROLE) + ".ca.com";
        final String crossMachineName =
            tasResolver.getHostnameById(TOMCAT_AGENT_ONLY + "-agentRole") + ".ca.com";

        String libLocation = TOMCAT_INSTALL_DIR + "\\webapps\\pipeorgan\\WEB-INF\\lib\\";

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
        copyH2SchemaFileRole.before(startH2ServerRole, createH2SchemaRole);
        startH2ServerRole.before(createH2SchemaRole);

        IRole pipeScriptsRole =
            getPipeScriptsDeployRole(libLocation, tasResolver, prefix + "-pipeScriptsDeployRole");

        // String normalScenarioScriptName = "NormalScenarioDemo";
        String normalScenarioScriptName = "";
        if (prefix.equalsIgnoreCase(TOMCAT_AGENT_ONLY)) {
            normalScenarioScriptName = "NormalScenario.xml";

        } else if (prefix.equalsIgnoreCase(SA_MASTER)) {
            normalScenarioScriptName = "NormalScenarioDemo.xml";
        }

        ExecutionRole changeHostName =
            changeMachineHost(prefix, hostname, crossMachineName, libLocation + "\\scenarios",
                normalScenarioScriptName);

        IRole startNormalLoadRole =
            startNormalLoadRole(libLocation, tasResolver, prefix + "-startNormalLoadRole",
                libLocation, "scenarios", normalScenarioScriptName);

        ExecutionRole changeProblemScenarioHostName =
            changeMachineHost(prefix + "ProblemScenario", hostname, crossMachineName,
                TOMCAT_INSTALL_DIR + "\\webapps\\pipeorgan\\WEB-INF\\lib\\scenarios",
                "Problem-CrossProcess.xml");

        pipeScriptsRole.after(createH2SchemaRole);
        changeHostName.after(pipeScriptsRole);
        changeProblemScenarioHostName.after(changeHostName);
        startNormalLoadRole.after(pipeScriptsRole, changeHostName);

        // add only roles created here
        machine.addRole(startNormalLoadRole, copyH2SchemaFileRole, changeHostName, pipeScriptsRole,
            createH2SchemaRole, startH2ServerRole, h2Role, agentRole, tomcatRole);// changeProblemScenarioHostName,

        // return the last role in the chain created here
        return startNormalLoadRole;
    }

    private IRole getAgentRole(String roleId, ITasResolver tasResolver, AgentCapable tomcatRole,
        EmRole emRole) {

        return (new AgentRole.Builder(roleId, tasResolver))
            .intrumentationLevel(AgentInstrumentationLevel.FULL).webAppServer(tomcatRole)
            .emRole(emRole).webAppAutoStart().build();// .version(VERSION)
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
        String libLocation, String folderName, String fileName) {
        String command =
            "start /d " + location + " " + BAT_FILE + " \"" + libLocation + "*\" ./" + folderName
                + "/" + fileName;

        RunCommandFlowContext runCommandFlowContext =
            new RunCommandFlowContext.Builder("").args(Arrays.asList(command)).build();

        ExecutionRole execRole =
            new ExecutionRole.Builder(roleName).asyncCommand(runCommandFlowContext).build();

        return execRole;
    }


    private IRole getMMRole(String emLocation, ITasResolver tasResolver, String roleName) {

        DefaultArtifact mmJar =
            new DefaultArtifact("com.ca.apm.tests", "ttviewer-core", "alertsMM", "jar",
                tasResolver.getDefaultVersion());

        String deployLocation = emLocation + "\\deploy\\alertMM.jar";
        GenericRole mmRole =
            new GenericRole.Builder(roleName, tasResolver).download(mmJar, deployLocation).build();

        return mmRole;
    }

    private IRole getPipeScriptsDeployRole(String location, ITasResolver tasResolver,
        String roleName) {

        DefaultArtifact zip =
            new DefaultArtifact("com.ca.apm.tests", "ttviewer-core", "pipescripts", "zip",
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

    private ExecutionRole changeMachineHost(String prefix, String hostMachineName,
        String crossMachineName, String location, String fileName) {

        Map<String, String> hostData = new HashMap<String, String>();
        hostData.put("localMachine", hostMachineName);
        hostData.put("crossMachine", crossMachineName);

        String filePath = location + "\\" + fileName;
        String roleName = prefix + fileName + "-changeHostNameRole";

        return new ExecutionRole.Builder(roleName).flow(FileModifierFlow.class,
            new FileModifierFlowContext.Builder().replace(filePath, hostData).build()).build();
    }


    @NotNull
    protected GenericRole getwls12cRole(ITasResolver tasResolver, String wlsRole) {

        ArrayList<String> args = new ArrayList<String>();
        args.add("-silent");

        RunCommandFlowContext installWlc12cCommand =
            new RunCommandFlowContext.Builder("configure.cmd").workDir(WLS12C_INSTALL_HOME)
                .args(args).build();

        GenericRole wls12cInstallRole =
            new GenericRole.Builder(wlsRole, tasResolver)
                .unpack(
                    new DefaultArtifact("com.ca.apm.binaries", "weblogic", "dev", "zip", "12.1.3"),
                    codifyPath(WLS12C_INSTALL_HOME)).runCommand(installWlc12cCommand).build();

        return wls12cInstallRole;
    }

    @NotNull
    protected String codifyPath(String path) {
        return FilenameUtils.separatorsToUnix(path);
    }

    // starts wls
    private IRole startWlsServerRole(ITasResolver tasResolver, String roleName) {
        String location = "C:\\automation\\deployed\\webapp\\pipeorgandomain\\bin";

        ExecutionRole execRole =
            new ExecutionRole.Builder(roleName).asyncCommand(
                new RunCommandFlowContext.Builder("").args(
                    Arrays.asList("START /D " + location + " startWebLogic.cmd")).build()).build();
        return execRole;
    }

    // deploys wls pipeorgan scripts
    private IRole getWlsScriptsDeployRole(String location, ITasResolver tasResolver, String roleName) {

        DefaultArtifact zip =
            new DefaultArtifact("com.ca.apm.tests", "ttviewer-core", "wlsScripts", "zip",
                tasResolver.getDefaultVersion());

        GenericRole mmRole =
            new GenericRole.Builder(roleName, tasResolver).unpack(zip, location).build();

        return mmRole;
    }

    // modifies hostname for pipeorgan scripts and starts load
    protected void startWlsScripts(String prefix, ITasResolver tasResolver,
        ITestbedMachine machine, IRole beforeRole) {

        final String hostname = tasResolver.getHostnameById(prefix + "_" + "java8Role") + ".ca.com";
        final String dummyMachineName = tasResolver.getHostnameById(SA_MASTER_EM_ROLE) + ".ca.com";

        String rootLocation = "C:\\automation\\deployed\\";
        String wlsPipeScriptLocation = rootLocation + "wlsScripts";
        String libLocation = "C:\\automation\\deployed\\webapp\\pipeorgandomain\\pipeorgan\\";

        ArrayList<String> scripts =
            new ArrayList<String>(Arrays.asList("Deep480.xml", "Deep550.xml",
                "LargeTTEJB2_480.xml", "LargeTTEJB2_550.xml"));

        for (String fileName : scripts) {

            // changes hostname in scripts
            ExecutionRole changeHostName =
                changeMachineHost(prefix, hostname, dummyMachineName, wlsPipeScriptLocation,
                    fileName);

            // start individual role scripts
            IRole startNormalLoadRole =
                startNormalLoadRole(rootLocation, tasResolver, prefix + fileName + "-start"
                    + fileName + "LoadRole", libLocation, "wlsScripts", fileName);

            changeHostName.after(beforeRole);
            changeHostName.before(startNormalLoadRole);
            machine.addRole(changeHostName, startNormalLoadRole);
        }
    }

}
