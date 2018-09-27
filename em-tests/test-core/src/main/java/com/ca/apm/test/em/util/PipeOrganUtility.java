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
package com.ca.apm.test.em.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.AgentCapable;
import com.ca.tas.testbed.ITestbedMachine;

public class PipeOrganUtility {

    public static final String DEPLOY_DIR = "C:\\sw";
    public static final String PIPEORGAN_CONTEXT = "pipeorgan";
    
    public static final String H2_LOCATION = DEPLOY_DIR + "\\H2";
    public static final String H2_VERSION = "1.4.191";

    public static final String BAT_FILE = "run.bat";

    static public ITasArtifact getPipeOrganArtifact(ITasResolver tasResolver) {

        return new TasArtifact.Builder("pipeorgan_web").version(tasResolver.getDefaultVersion())
            .extension(TasExtension.WAR).groupId("com.ca.apm.coda-projects.test-tools.pipeorgan")
            .build();
    }
    
    static public IRole addPipeOrganRoles(ITestbedMachine machine, String prefix, String emHostname,
                                          AgentCapable tomcatRole, ITasResolver tasResolver) {

        final String crossMachineName =
            tasResolver.getHostnameById(tomcatRole.getRoleId()) + ".ca.com";

        String libLocation = tomcatRole.getInstallDir() + "\\webapps\\pipeorgan\\WEB-INF\\lib\\";

        IRole h2Role = getH2DeployRole(tasResolver, prefix + "-copyH2ArtifactRole");
        IRole copyH2SchemaFileRole =
            getDeployDBScriptsRole(H2_LOCATION, tasResolver, prefix + "-copyH2SchemaFileRole");
        IRole startH2ServerRole =
            getStartH2ServerRole(H2_LOCATION, tasResolver, prefix + "-startH2ServerRole");
        IRole createH2SchemaRole =
            getCreateH2DBRole(H2_LOCATION, tasResolver, prefix + "-createH2SchemaRole");
        h2Role.after(tomcatRole);
        h2Role.before(startH2ServerRole);
        copyH2SchemaFileRole.before(startH2ServerRole, createH2SchemaRole);
        startH2ServerRole.before(createH2SchemaRole);

        IRole pipeScriptsRole =
            getPipeScriptsDeployRole(libLocation, tasResolver, prefix + "-pipeScriptsDeployRole");

        String normalScenarioScriptName = "NormalScenarioDemo.xml";

        ExecutionRole changeHostName =
            changeMachineHost(prefix, emHostname, crossMachineName, libLocation + "scenarios",
                normalScenarioScriptName);
        IRole startNormalLoadRole =
            startLoadRole(libLocation, tasResolver, prefix + "-startNormalLoadRole",
                libLocation, "scenarios", normalScenarioScriptName);

        ExecutionRole changeProblemScenarioHostName =
            changeMachineHost(prefix + "ProblemScenario", emHostname, crossMachineName,
                libLocation + "scenarios", "Problem-BTAlert.xml");
        IRole startProblemLoadRole =
            startLoadRole(libLocation, tasResolver, prefix + "-startProblemLoadRole",
                libLocation, "scenarios", "Problem-BTAlert.xml");

        pipeScriptsRole.after(createH2SchemaRole);
        changeHostName.after(pipeScriptsRole);
        changeProblemScenarioHostName.after(changeHostName);
        startNormalLoadRole.after(changeHostName);
        startProblemLoadRole.after(startNormalLoadRole);

        // add only roles created here
        machine.addRole(startProblemLoadRole, startNormalLoadRole, copyH2SchemaFileRole, changeHostName,
            changeProblemScenarioHostName, pipeScriptsRole, createH2SchemaRole, startH2ServerRole, h2Role);

        // return the last role in the chain created here
        return startProblemLoadRole;
    }

    static private IRole startLoadRole(String location, ITasResolver tasResolver, String roleName,
        String libLocation, String folderName, String fileName) {

        RunCommandFlowContext runCommandFlowContext = new RunCommandFlowContext.Builder(BAT_FILE)
                                    .workDir(location)
                                    .args(Arrays.asList(libLocation + "*", folderName + "/" + fileName))
                                    .terminateOnMatch("Pipe organ running")
                                    .build();
        ExecutionRole execRole = new ExecutionRole.Builder(roleName)
                                    .syncCommand(runCommandFlowContext)
                                    .build();
        return execRole;
    }

    static private IRole getPipeScriptsDeployRole(String location, ITasResolver tasResolver,
        String roleName) {

        DefaultArtifact zip =
            new DefaultArtifact("com.ca.apm.test", "em-tests-core", "pipescripts", "zip",
                tasResolver.getDefaultVersion());

        GenericRole mmRole =
            new GenericRole.Builder(roleName, tasResolver).unpack(zip, location).build();

        return mmRole;
    }

    static private IRole getH2DeployRole(ITasResolver tasResolver, String roleName) {
        DefaultArtifact h2Artifact =
            new DefaultArtifact("com.h2database", "h2", "", "jar", H2_VERSION);
        GenericRole h2Role =
            new GenericRole.Builder(roleName, tasResolver).download(h2Artifact,
                H2_LOCATION + "\\h2.jar").build();

        return h2Role;
    }

    static private IRole getDeployDBScriptsRole(String location, ITasResolver tasResolver, String roleName) {

        ClassLoader classLoader = PipeOrganUtility.class.getClassLoader();
        ExecutionRole pipeorganDBScriptRole = null;
        try {
            ArrayList<String> scripts = new ArrayList<String>(Arrays.asList("PipeOrganDB.sql"));

            for (String fileName : scripts) {
                String result =
                    IOUtils.toString(classLoader.getResourceAsStream("mysql/" + fileName), StandardCharsets.UTF_8);
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

    static private IRole getStartH2ServerRole(String location, ITasResolver tasResolver, String roleName) {
        String h2JarLocation = location + "\\h2.jar";
        String startH2BatLocation = location + "\\startH2.bat";

        Collection<String> data =
            Arrays.asList("java -cp " + h2JarLocation + " org.h2.tools.Server -webPort 8085");
        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create(startH2BatLocation, data).build();

        ExecutionRole execRole =
            new ExecutionRole.Builder(roleName)
                .flow(FileModifierFlow.class, createFileFlow)
                .syncCommand(new RunCommandFlowContext.Builder("startH2.bat")
                    .workDir(location)
                    .terminateOnMatch("Web Console server running")
                    .build())
                .build();
        return execRole;
    }

    static private IRole getCreateH2DBRole(String h2location, ITasResolver tasResolver, String roleName) {

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
                .asyncCommand(new RunCommandFlowContext.Builder("")
                        .args(Arrays.asList("START /D " + h2location + " createDBSchema.bat"))
                        .build())
                .build();
        return execRole;
    }

    static private ExecutionRole changeMachineHost(String prefix, String hostMachineName,
        String crossMachineName, String location, String fileName) {

        Map<String, String> hostData = new HashMap<String, String>();
        hostData.put("localMachine", hostMachineName);
        hostData.put("crossMachine", crossMachineName);

        String filePath = location + "\\" + fileName;
        String roleName = prefix + fileName + "-changeHostNameRole";

        return new ExecutionRole.Builder(roleName).flow(FileModifierFlow.class,
            new FileModifierFlowContext.Builder().replace(filePath, hostData).build()).build();
    }

}
