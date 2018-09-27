/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * Author : GAMSA03/ SANTOSH GAMPA
 * Date : 19/04/2016
 */
package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
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
 * The testbed is for deployment of Cluster with a MOM , one Collector and Agent. 
 * Three freemachines to share the filesystem for failover tests
 */

@TestBedDefinition
public class AgentEMFailoverLinuxClusterTestbed implements ITestbedFactory {

    public static final String MOM_MACHINE_ID = "emMachine";
    public static final String COLLECTOR_MACHINE_ID = "emMachine";
    public static final String FREE_MACHINE1_ID = "freeMachine1";
    public static final String FREE_MACHINE2_ID = "freeMachine2";
    public static final String FREE_MACHINE3_ID = "freeMachine3";

    public static final String MOM_ROLE_ID = "momRole";
    public static final String COLLECTOR_ROLE_ID = "collector1Role";
    public static final String FREE_ROLE1_ID = "freeRole1";
    public static final String FREE_ROLE2_ID = "freeRole2";
    public static final String FREE_ROLE3_ID = "freeRole3";

    protected static String EM_TEMPLATE_ID = TEMPLATE_CO66;
      
    public static final String AGENT_MACHINE_ID = "agentMachine";
    private static final String AGENT_MACHINE_TEMPLATE_ID = TEMPLATE_CO66;
   

    public static final String TOMCAT_ROLE_ID = "tomcatRole";
    public static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";


    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Features to be installed for collector
        List<String> collectorChosenFeatures = Arrays.asList("Enterprise Manager");

        // Collector role
        EmRole collectorRole =
            new EmRole.LinuxBuilder(COLLECTOR_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).installDir("/opt/automation/deployed_Collector/collem").emPort(5002).emWebPort(8085)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID))
                .nostartEM().nostartWV()
                .build();

        // MOM role
        EmRole momRole =
            new EmRole.LinuxBuilder(MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .emCollector(collectorRole)
                .nostartEM().nostartWV().build();


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
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole)
                .build();

        
        // Configuration of mom machine and Collector
        TestbedMachine emMachine =
            TestBedUtils.createLinuxMachine(MOM_MACHINE_ID, EM_TEMPLATE_ID, momRole,collectorRole
                );

        // Configuration of free machines
        TestbedMachine freeMachine1 =
            TestBedUtils.createLinuxMachine(FREE_MACHINE1_ID, EM_TEMPLATE_ID, new DeployFreeRole(FREE_ROLE1_ID) );
        
        TestbedMachine freeMachine2 =
                TestBedUtils.createLinuxMachine(FREE_MACHINE2_ID, EM_TEMPLATE_ID, new DeployFreeRole(FREE_ROLE2_ID) );
        
        TestbedMachine freeMachine3 =
                TestBedUtils.createLinuxMachine(FREE_MACHINE3_ID, EM_TEMPLATE_ID, new DeployFreeRole(FREE_ROLE3_ID) );
        
        // Configuration of Agent Machine
        TestbedMachine agentMachine =
            TestBedUtils.createLinuxMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID,
                tomcatRole, qaAppTomcatRole, tomcatAgentRole);

        return Testbed.create(this,  emMachine, agentMachine, freeMachine1, freeMachine2, freeMachine3);
        
        

    }
    public static String getEM_TEMPLATE_ID() {
		return EM_TEMPLATE_ID;
	}

	public static void setEM_TEMPLATE_ID(String eM_TEMPLATE_ID) {
		EM_TEMPLATE_ID = eM_TEMPLATE_ID;
	}

}
