package com.ca.apm.tests.testbed;

import com.ca.apm.tests.role.DXCRole;
import com.ca.apm.tests.role.DotNetAgentDeployRole;
import com.ca.apm.tests.role.DotNetAppsRole;
import com.ca.apm.tests.role.KafkaZookeeperRole;
import com.ca.apm.tests.role.LogstashRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * .NET Browser Agent Test Bed 
 *
 * @author kurma05
 */
@TestBedDefinition
public class DotNetDxcTestBed extends DotNetBaseTestBed {
    
    public static final String DXC_ROLE_ID        = "dxcrole";
    public static final String KAFKA_ROLE_ID      = "kafka";;
    public static final String LOGSTASH_ROLE_ID   = "logstash";
    private static final String LINUX_TEMPLATE_ID = ITestbedMachine.TEMPLATE_RH7;
    
    @Override
    protected void initMachines(ITasResolver tasResolver) {

        TestbedMachine machine1 = 
            new TestbedMachine.Builder(MACHINE1_ID).templateId(TEMPLATE_W64_JASS).build();
        TestbedMachine machine2 = 
            new TestbedMachine.LinuxBuilder(MACHINE2_ID).templateId(LINUX_TEMPLATE_ID).build();  
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
        
        //add DxC machine                 
        KafkaZookeeperRole kafka =
            new KafkaZookeeperRole.LinuxBuilder(KAFKA_ROLE_ID, tasResolver).build();
        LogstashRole logstash =
            new LogstashRole.LinuxBuilder(LOGSTASH_ROLE_ID, tasResolver)
                .emHost(tasResolver.getHostnameById(EM_ROLE_ID)).emPort(5001).build();
        DXCRole dxc = new DXCRole.LinuxBuilder(DXC_ROLE_ID, tasResolver).apmEnabled(true).build(); 

        machine2.addRole(kafka, dxc, logstash);         
        testbed.addMachine(machine1, machine2);
    }
}