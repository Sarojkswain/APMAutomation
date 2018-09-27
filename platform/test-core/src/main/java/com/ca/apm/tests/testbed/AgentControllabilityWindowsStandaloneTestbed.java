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
 * Author : JAMSA07/ JAMMI SANTOSH
 * Date : 20/11/2015
 */

package com.ca.apm.tests.testbed;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
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
public class AgentControllabilityWindowsStandaloneTestbed implements ITestbedFactory {


    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // create EM role
        EmRole emRole =
            new EmRole.Builder(AgentControllabilityConstants.EM_ROLE_ID, tasResolver).nostartEM().nostartWV()
                .build();

        // create QAApp role for Tomcat
        WebAppRole<TomcatRole> qaAppTomcatRole =
            new QaAppTomcatRole.Builder(AgentControllabilityConstants.QA_APP_TOMCAT_ROLE_ID, tasResolver)
                .cargoDeploy().contextName("qa-app").build();

        // create Tomcat role
        TomcatRole tomcatRole =
            new TomcatRole.Builder(AgentControllabilityConstants.TOMCAT_ROLE_ID, tasResolver)
                .tomcatVersion(TomcatVersion.v70).tomcatCatalinaPort(9091).webApp(qaAppTomcatRole)
                .build();

        // create Tomcat Agent role
        IRole tomcatAgentRole =
            new AgentRole.Builder(AgentControllabilityConstants.TOMCAT_AGENT_ROLE_ID, tasResolver)
                .webAppServer(tomcatRole).intrumentationLevel(AgentInstrumentationLevel.FULL)
                .emRole(emRole).build();

        // map roles to machines
        ITestbedMachine emMachine =
            TestBedUtils.createWindowsMachine(AgentControllabilityConstants.EM_MACHINE_ID,
                AgentControllabilityConstants.WINDOWS_TEMPLATE_ID);
        emMachine.addRole(emRole);
        ITestbedMachine agentMachine =
            TestBedUtils.createWindowsMachine(AgentControllabilityConstants.TOMCAT_MACHINE_ID,
                AgentControllabilityConstants.WINDOWS_TEMPLATE_ID);
        agentMachine.addRole(tomcatRole, qaAppTomcatRole, tomcatAgentRole);
        
        emMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", emRole.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        agentMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcatRole.getTomcatFlowContext().getTomcatInstallDir()+ "\\wily\\logs"));

        
        return new Testbed(getClass().getSimpleName()).addMachine(emMachine, agentMachine);
    }
}
