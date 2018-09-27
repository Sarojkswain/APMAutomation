package com.ca.apm.tests.testbed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.QaAppJbossRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.TomcatVersion;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.builder.TasBuilder;
import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.google.common.collect.Sets;

/**
 * SampleTestbed class.
 *
 * Testbed description.
 */
@TestBedDefinition

public class AgentsWindowsStandaloneTestbed implements ITestbedFactory
{

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_ROLE_ID = "emRole";
    private static final String EM_MACHINE_TEMPLATE_ID =TEMPLATE_W64;
    
    public static final String AGENT_MACHINE_ID = "agentMachine";
    private static final String AGENT_MACHINE_TEMPLATE_ID = TEMPLATE_W64;
    
    public static final String AGENT_MACHINE_ID1 = "agentMachine1";
    private static final String AGENT_MACHINE_TEMPLATE_ID1 = TEMPLATE_W64;
    
    public static final String AGENT_MACHINE_ID2 = "agentMachine2";
    private static final String AGENT_MACHINE_TEMPLATE_ID2 = TEMPLATE_W64;
    
    public static final String TOMCAT_ROLE_ID="tomcatRole";
    public static final String WEBLOGIC_ROLE_ID = "Weblogic103Role";
    public static final String WEBLOGIC_AGENT_ROLE_ID="weblogic103AgentRole";
    public static final String TOMCAT_AGENT_ROLE_ID="tomcatAgentRole";
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";
   
    
    public static final String JBOSS_ROLE_ID="JBossRole";    
    public static final String JBOSS_AGENT_ROLE_ID="JbossAgentRole";
    public static final String QA_APP_JBOSS_ROLE_ID = "qaAppJBossRole";
    
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
      //create EM role
        EmRole emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver)
            .nostartEM().nostartWV()
            .build();
        
        WebLogicRole weblogic01 =
                new WebLogicRole.Builder(WEBLOGIC_ROLE_ID, tasResolver)
                        .customComponentPaths(
                                Sets.newHashSet(
                                        "WebLogic Server/Core Application Server",
                                        "WebLogic Server/Administration Console",
                                        "WebLogic Server/Configuration Wizard and Upgrade Framework",
                                        "WebLogic Server/Web 2.0 HTTP Pub-Sub Server",
                                        "WebLogic Server/WebLogic JDBC Drivers",
                                        "WebLogic Server/Third Party JDBC Drivers",
                                        "WebLogic Server/WebLogic Server Clients",
                                        "WebLogic Server/WebLogic Web Server Plugins",
                                        "WebLogic Server/UDDI and Xquery Support",
                                        "WebLogic Server/Server Examples"))
                        .build();
      
      
      //create QAApp role for Jboss
        WebAppRole<JbossRole> qaAppJbossRole = new QaAppJbossRole.Builder(QA_APP_JBOSS_ROLE_ID, tasResolver)
        .cargoDeploy()
        .contextName("qa-app")
        .build();
      
      //create QAApp role for Tomcat   
        /*WebAppRole<TomcatRole> qaAppTomcatRole = new QaAppTomcatRole.Builder(QA_APP_TOMCAT_ROLE_ID, tasResolver)
        .cargoDeploy()
        .contextName("qa-app")
        .build();*/
      
        //create Jboss role  
        JbossRole jbossRole = new JbossRole.Builder(JBOSS_ROLE_ID, tasResolver)
        .version(JBossVersion.JBOSS711)
        .addWebAppRole(qaAppJbossRole)
        .build();
        
        //create Jboss agent role
        IRole jbossAgentRole = new AgentRole.Builder(JBOSS_AGENT_ROLE_ID, tasResolver)
        .webAppRole(jbossRole)
        .intrumentationLevel(AgentInstrumentationLevel.FULL)
        .emRole(emRole)
        .build();
           
        //create Tomcat role
        TomcatRole tomcatRole = new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver)
        .tomcatVersion(TomcatVersion.v70)
        .tomcatCatalinaPort(9091)
        //.webApp(qaAppTomcatRole)
        .build();
        
  

        //create Tomcat Agent role
        IRole tomcatAgentRole = new AgentRole.Builder(TOMCAT_AGENT_ROLE_ID, tasResolver)
        .webAppRole(tomcatRole)
        .intrumentationLevel(AgentInstrumentationLevel.FULL)
        .emRole(emRole)
        .build();  
        
 //Create WebLogic Agent role
        
        IRole weblogicAgentRole = new AgentRole.Builder(WEBLOGIC_AGENT_ROLE_ID, tasResolver).webAppRole(weblogic01)
                .intrumentationLevel(AgentInstrumentationLevel.FULL)
                .emRole(emRole)
                .build();  
                
        //map roles to machines
        ITestbedMachine emMachine = TestBedUtils.createWindowsMachine(EM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID);
        
        emMachine.addRole(emRole);
        ITestbedMachine agentMachine = TestBedUtils.createWindowsMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID);
        ITestbedMachine agentMachine1 = TestBedUtils.createWindowsMachine(AGENT_MACHINE_ID1, AGENT_MACHINE_TEMPLATE_ID1);
        ITestbedMachine agentMachine2 = TestBedUtils.createWindowsMachine(AGENT_MACHINE_ID2, AGENT_MACHINE_TEMPLATE_ID2);
        //agentMachine.addRole(tomcatRole, qaAppTomcatRole,tomcatAgentRole);
        agentMachine.addRole(tomcatRole,tomcatAgentRole);
        agentMachine1.addRole(jbossRole, qaAppJbossRole, jbossAgentRole);
        agentMachine2.addRole(weblogic01, weblogicAgentRole);
               
        return new Testbed(getClass().getSimpleName()).addMachine(emMachine,agentMachine,agentMachine1,agentMachine2);
    }
}




