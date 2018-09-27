package com.ca.apm.tests.testbed;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class F5UmaAgentLinuxStandaloneTestBed implements ITestbedFactory {
    
    public static final String CONFIG_FILES_LOC = TasBuilder.LINUX_SOFTWARE_LOC;
    
    public static final String UMA_ROLE_ID = "umaRole";
    public static final String DATA_POWER_EXTENSION_ROLE_ID = "dataPowerMonitorRole";
    

     @Override
     public ITestbed create(ITasResolver tasResolver) {
         
         // create EM role
         EmRole emRole =
             new EmRole.LinuxBuilder(AgentControllabilityConstants.EM_ROLE_ID, tasResolver).nostartEM().nostartWV().build();

         // create QAApp role for Tomcat
         WebAppRole<TomcatRole> qaAppTomcatRole =
             new QaAppTomcatRole.Builder(AgentControllabilityConstants.QA_APP_TOMCAT_ROLE_ID, tasResolver).cargoDeploy().contextName("qa-app").build();

         // create Tomcat role
         TomcatRole tomcatRole =
             new TomcatRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_ROLE_ID, tasResolver).tomcatVersion(TomcatVersion.v70).tomcatCatalinaPort(9091).webApp(qaAppTomcatRole)
                 .build();

         // create Tomcat Agent role
         IRole tomcatAgentRole =
             new AgentRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_AGENT_ROLE_ID, tasResolver)
                 .webAppServer(tomcatRole).intrumentationLevel(AgentInstrumentationLevel.FULL)
                 .emRole(emRole).build();
         
         //UMA Agent download
         GenericRole downloadUMARole =
                 new GenericRole.Builder(UMA_ROLE_ID, tasResolver).unpack(
                     new DefaultArtifact("com.ca.apm.agent.UMA", "APM-Infrastructure-Agent", "unix", "tar.gz", "10.7.0_dev"), CONFIG_FILES_LOC).build();
                 
         //DataPower extension download
       /*  GenericRole downloadExtensionFileRole =
             new GenericRole.Builder(DATA_POWER_EXTENSION_ROLE_ID, tasResolver).unpack(
                 new DefaultArtifact("com.ca.apm.coda.testdata.extension", "DataPowerMonitor", "zip", "3.0"), CONFIG_FILES_LOC+"/extensions/DataPower").build();
         */
         // map roles to machines
         ITestbedMachine emMachine =
             TestBedUtils.createLinuxMachine(AgentControllabilityConstants.EM_MACHINE_ID,
                 AgentControllabilityConstants.CO66_TEMPLATE_ID);
         emMachine.addRole(emRole);
         ITestbedMachine agentMachine =
             TestBedUtils.createLinuxMachine(AgentControllabilityConstants.TOMCAT_MACHINE_ID,
                 AgentControllabilityConstants.CO66_TEMPLATE_ID);
         
         agentMachine.addRole(tomcatRole,  qaAppTomcatRole, tomcatAgentRole, downloadUMARole);
         
         emMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", emRole.getDeployEmFlowContext().getInstallDir()+ "/logs"));
         agentMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcatRole.getTomcatFlowContext().getTomcatInstallDir()+ "/wily/logs"));
         
         
         return new Testbed(getClass().getSimpleName()).addMachine(emMachine, agentMachine);
                                                               
    }
 
}
