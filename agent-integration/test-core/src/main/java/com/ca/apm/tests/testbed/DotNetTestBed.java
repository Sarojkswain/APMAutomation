package com.ca.apm.tests.testbed;

import com.ca.apm.tests.role.DotNetAgentDeployRole;
import com.ca.apm.tests.role.DotNetAppsRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * .Net test bed 
 *
 * @author kurma05
 */
@TestBedDefinition
public class DotNetTestBed extends DotNetBaseTestBed {
      
    @Override
    protected void initMachines(ITasResolver tasResolver) {

        TestbedMachine machine1 = new TestbedMachine.Builder(MACHINE1_ID).templateId(TEMPLATE_W64_JASS).build();
        addClientRoles(tasResolver, machine1);
        
        //iis, perfmon & tns roles       
        addEnableWebServerRole(tasResolver, machine1);
        addPerfmonRebuildRole(tasResolver, machine1);
        addTnsRole(tasResolver, machine1); 
        
        //.net apps
        DotNetAppsRole dotNetAppsRole = new DotNetAppsRole.Builder(DOTNET_APPS_ROLE_ID, tasResolver)
            .installDir(WIN_DEPLOY_BASE + "testapps")
            .shouldDeploySystemApps(true)
            .shouldDisableHttpLogging(true)
            .shouldInstallSQLServer(true)
            .build();

        //deploy agent
        DotNetAgentDeployRole dotNetAgentRole = new DotNetAgentDeployRole.Builder(DOTNET_AGENT_ROLE_ID, tasResolver)
            .installDir(DOTNET_AGENT_HOME)
            .build();  
        
        IRole registerIISRole = machine1.getRoleById(IIS_REGISTER_ROLE_ID);
        IRole updatePortRole = machine1.getRoleById(IIS_UPDATE_PORT_ROLE_ID);
        dotNetAppsRole.after(updatePortRole, registerIISRole); 
        dotNetAgentRole.after(dotNetAppsRole);       
        machine1.addRole(dotNetAppsRole, dotNetAgentRole);
        
        // deploy em
        setupJarvis = true;
        addEMWinRole(tasResolver, machine1);
         
        testbed.addMachine(machine1);
    }
}