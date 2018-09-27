package com.ca.apm.tests.testbed;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * SampleTestbed class.
 *
 * Testbed description.
 */
@TestBedDefinition

public class WindowsStandaloneTestbed implements ITestbedFactory
{

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_ROLE_ID = "emRole";
    private static final String EM_MACHINE_TEMPLATE_ID =TEMPLATE_W64;
    
    public static final String AGENT_MACHINE_ID = "agentMachine";
    private static final String AGENT_MACHINE_TEMPLATE_ID = TEMPLATE_W64;
    public static final String TOMCAT_ROLE_ID="tomcatRole";
    
    public static final String TOMCAT_AGENT_ROLE_ID="tomcatAgentRole";
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";
   
        
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
      //create EM role
        EmRole emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver)
            .nostartEM().nostartWV()
            .build();

        
      //create QAApp role for Tomcat   
        /*WebAppRole<TomcatRole> qaAppTomcatRole = new QaAppTomcatRole.Builder(QA_APP_TOMCAT_ROLE_ID, tasResolver)
        .cargoDeploy()
        .contextName("qa-app")
        .build();*/
      
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
        
 
                
        //map roles to machines
        ITestbedMachine emMachine = TestBedUtils.createWindowsMachine(EM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID);
        
        emMachine.addRole(emRole);
        ITestbedMachine agentMachine = TestBedUtils.createWindowsMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID);
        
        //agentMachine.addRole(tomcatRole, qaAppTomcatRole,tomcatAgentRole);
        agentMachine.addRole(tomcatRole,tomcatAgentRole);
        
        return new Testbed(getClass().getSimpleName()).addMachine(emMachine,agentMachine);
    }
}




