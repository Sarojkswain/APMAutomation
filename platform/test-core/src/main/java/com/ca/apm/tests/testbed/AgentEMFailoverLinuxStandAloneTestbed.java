/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 * 
 * Author : TUUJA01/ JAYARAM PRASAD TADIMETI
 * Date : 13/04/2016
 */
package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO65;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * The testbed is for deployment of Cluster with a collectors
 * in Windows for execution of all A cluster test cases. *
 */

@TestBedDefinition
public class AgentEMFailoverLinuxStandAloneTestbed implements ITestbedFactory {

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String AGENT_MACHINE_ID = "agentMachine";
    public static final String EM_ROLE_ID = "emRole";
    public static final String freeRole_ID="freeRole";

    protected static String EM_TEMPLATE_ID = TEMPLATE_CO65;
    private static final String AGENT_MACHINE_TEMPLATE_ID = TEMPLATE_CO65;


    public static final String TOMCAT_ROLE_ID = "tomcatRole";
    public static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";
    public static final String freeMachineID= "deployFreeMachine";


    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        // EM role
        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver).build();
        
        // QAAppRole for Tomcat
        WebAppRole<TomcatRole> qaAppTomcatRole =
            new QaAppTomcatRole.Builder(QA_APP_TOMCAT_ROLE_ID, tasResolver)
                .cargoDeploy().contextName("qa-app")
                .build();
       
        // Tomcat Role
        TomcatRole tomcatRole =
            new TomcatRole.LinuxBuilder(TOMCAT_ROLE_ID, tasResolver).tomcatVersion(TomcatVersion.v70)
                .tomcatCatalinaPort(9091).webApp(qaAppTomcatRole).build();

        // Tomcat Agent Role
        IRole tomcatAgentRole =
            new AgentRole.LinuxBuilder(TOMCAT_AGENT_ROLE_ID, tasResolver).webAppServer(tomcatRole)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(emRole)
                .build();

            
        // Configuration of mom machine
        TestbedMachine emMachine =
            TestBedUtils.createLinuxMachine(EM_MACHINE_ID, EM_TEMPLATE_ID, emRole);
        
        TestbedMachine freeMachine =
            TestBedUtils.createLinuxMachine(freeMachineID, EM_TEMPLATE_ID, new DeployFreeRole(freeRole_ID) );
        
     // Configuration of Agent Machine
        TestbedMachine agentMachine =
            TestBedUtils.createLinuxMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID,
                tomcatRole, qaAppTomcatRole, tomcatAgentRole);
        
    
        return Testbed.create(this, emMachine, freeMachine,agentMachine);

    }
    public static String getEM_TEMPLATE_ID() {
		return EM_TEMPLATE_ID;
	}

	public static void setEM_TEMPLATE_ID(String eM_TEMPLATE_ID) {
		EM_TEMPLATE_ID = eM_TEMPLATE_ID;
	}
}
