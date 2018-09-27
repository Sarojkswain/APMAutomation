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

package com.ca.apm.tests.testbed.dotnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.DotNetAgentDeployRole;
import com.ca.apm.tests.role.DotNetAppsDeployRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * .Net Agent Automation 
 *
 * @author kurma05@ca.com
 */
@TestBedDefinition
public class DotNetAgentTestBed extends AgentRegressionBaseTestBed {

    public static final String DOTNET_AGENT_ROLE_ID      = "dotNetAgentRole";
    public static final String DOTNET_APPS_ROLE_ID       = "dotNetAppsRole";
    public static final String IIS_ENABLE_ROLE_ID        = "iisEnableRole";
    public static final String IIS_UPDATE_PORT_ROLE_ID   = "iisUpdatePortRole";
    public static final String IIS_REGISTER_ROLE_ID      = "iisRegisterRole";
    public static final String ODP_NET_SCRIPTS_ROLE_ID   = "odpNetScriptsRole";
    public static final String COPY_TNS_FILE_ROLE_ID     = "copyTnsFileRole";
   
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        ITestbedMachine machine1 = initMachine(tasResolver);
        ITestbed testBed = new Testbed(getTestBedName());
        testBed.addMachine(machine1);
        initSystemProperties(tasResolver, testBed, new HashMap<String,String>());
        return testBed;
    }

    protected void initSystemProperties(ITasResolver tasResolver, 
                                        ITestbed testBed, 
                                        HashMap<String,String> props) {
     
        String host = tasResolver.getHostnameById(DOTNET_AGENT_ROLE_ID);
        initGenericSystemProperties(tasResolver, testBed, props);
        initDotNetSystemProperties(tasResolver, host, props);
        setTestngCustomJvmArgs(props, testBed);
    }
    
    /**
     * Deploys EM & .net agent
     */
    @NotNull
    protected ITestbedMachine initMachine(ITasResolver tasResolver) {

        TestbedMachine machine1 = new TestbedMachine.Builder(MACHINE_1).templateId(TEMPLATE_W64_JASS).build();

        //add em, agent & misc roles
        addCygwinRole(tasResolver, machine1); 
        addQCUploadRole(tasResolver, machine1);
        addEnableWebServerRole(tasResolver, machine1);
        addPerfmonRebuildRole(tasResolver, machine1);
        addTnsRole(tasResolver, machine1); 
        addDotNetAgentRole(tasResolver, machine1);
        addEmRole(tasResolver, machine1);
        
        //add testng client
        machine1.addRole(new ClientDeployRole.Builder("dotnet_client01", tasResolver)
            .jvmVersion("7")
            .shouldDeployConsoleApps(false)
            .shouldDeployJassApps(isJassEnabled)
            .build());
        
        //.net apps
        DotNetAppsDeployRole dotNetAppsRole = new DotNetAppsDeployRole.Builder(DOTNET_APPS_ROLE_ID, tasResolver)
            .installDir(DEPLOY_BASE + "testapps")
            .shouldDeploySystemApps(isJassEnabled)
            .shouldDisableHttpLogging(isJassEnabled)
            .shouldInstallSQLServer(true) // needed by Nerd Dinner
            .build();

        IRole registerIISRole = machine1.getRoleById(IIS_REGISTER_ROLE_ID);
        IRole updatePortRole = machine1.getRoleById(IIS_UPDATE_PORT_ROLE_ID);
        IRole dotNetAgentRole = machine1.getRoleById(DOTNET_AGENT_ROLE_ID);
        dotNetAppsRole.before(dotNetAgentRole);
        updatePortRole.before(dotNetAppsRole);
        registerIISRole.before(dotNetAppsRole, dotNetAgentRole);  
        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(
            machine1.getMachineId() + "_" + "fetchAgentLogs", 
            codifyPath(DEPLOY_BASE + "/" + RESULTS_DIR));
        
        return machine1.addRole(dotNetAppsRole, fetchAgentLogs);
    }
  
    private void addDotNetAgentRole(ITasResolver tasResolver, TestbedMachine machine) {
       
        //deploy agent
        DotNetAgentDeployRole dotNetAgentRole = new DotNetAgentDeployRole.Builder(DOTNET_AGENT_ROLE_ID, tasResolver)
            .installDir(DEPLOY_BASE + "dotnet")
            .isLegacyMode(isLegacyMode)
            .build();
            
        //set version  
        String artifact = "dotnet-agent-installer";
        DefaultArtifact agentArtifact = new DefaultArtifact("com.ca.apm.delivery", 
            artifact, "64", "zip", getAgentArtifactVersion(tasResolver));
        setAgentVersion(artifact, tasResolver.getArtifactUrl(agentArtifact));
        
        machine.addRole(dotNetAgentRole);
    }
    
    private void addEnableWebServerRole(ITasResolver tasResolver, TestbedMachine machine) {
        
        //enable iis components
        ArrayList<String> args = new ArrayList<String>();
        args.add("/online");
        args.add("/enable-feature");
        args.add("/featurename:IIS-WebServerRole");
        args.add("/featurename:IIS-WebServer");
        args.add("/featurename:IIS-CommonHttpFeatures");
        args.add("/featurename:IIS-StaticContent");
        args.add("/featurename:MSMQ-Server");
        args.add("/featurename:IIS-CGI");
        args.add("/featurename:IIS-ISAPIExtensions");
        args.add("/featurename:IIS-ISAPIFilter");
        
        RunCommandFlowContext enableIISCommand = new RunCommandFlowContext.Builder("C:\\Windows\\System32\\Dism.exe")
            .args(args)
            .build();
        ExecutionRole enableIISRole =
            new ExecutionRole.Builder(IIS_ENABLE_ROLE_ID)
            .flow(RunCommandFlow.class, enableIISCommand)
            .build();
        
        //update port for default site
        RunCommandFlowContext updatePortCommand = new RunCommandFlowContext.Builder("C:\\Windows\\system32\\inetsrv\\appcmd")
            .args(Arrays.asList("set", "site", "\"Default Web Site\"", "/bindings:\"http/*:85:\""))
            .build();        
        ExecutionRole updatePortRole =
            new ExecutionRole.Builder(IIS_UPDATE_PORT_ROLE_ID)
            .flow(RunCommandFlow.class, updatePortCommand)
            .build();
    
        //register iis for .net 4
        RunCommandFlowContext registerIISCommand = new RunCommandFlowContext.Builder("C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\aspnet_regiis")
            .args(Arrays.asList("-i"))
            .build();
        ExecutionRole registerIISRole =
            new ExecutionRole.Builder(IIS_REGISTER_ROLE_ID)
            .flow(RunCommandFlow.class, registerIISCommand)
            .build();
        
        enableIISRole.before(updatePortRole, registerIISRole);
        machine.addRole(enableIISRole);
        machine.addRole(registerIISRole);
        machine.addRole(updatePortRole);
    }
 
    protected void addTnsRole(ITasResolver tasResolver, TestbedMachine machine) {
        
        String installBaseDir = DEPLOY_BASE + "oracle\\";
        
        //get dist package
        DefaultArtifact artifact = new DefaultArtifact("com.ca.apm.tests",
            "agent-tests-core",
            "dist_dotnet",
            "zip",
            tasResolver.getDefaultVersion());
        GenericRole distRole = new GenericRole.Builder(ODP_NET_SCRIPTS_ROLE_ID, tasResolver)
            .unpack(artifact, installBaseDir)
            .build();
   
        //copy tns file
        String source = installBaseDir + "tnsnames.sc.oracle.ora";
        String dest = "C:\\SW\\oracle\\product\\12.1.0\\client_1\\Network\\Admin\\tnsnames.ora";
        
        ExecutionRole tnsRole = new ExecutionRole.Builder(COPY_TNS_FILE_ROLE_ID).flow(FileModifierFlow.class,
            new FileModifierFlowContext.Builder().copy(source, dest).build())
            .build();
        
        distRole.before(tnsRole);
        machine.addRole(distRole);
        machine.addRole(tnsRole);
    }
}
