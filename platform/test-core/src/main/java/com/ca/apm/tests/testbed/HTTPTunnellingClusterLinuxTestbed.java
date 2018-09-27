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
 * Author : JAMSA07/ SANTOSH JAMMI
 * Author : TUUJA01/ JAYARAM PRASAD
 * Date : 25/03/2016
 */
package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
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
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.apm.role.ApacheRole;


/**
 * The testbed is for deployment of Cluster with two collectors
 * in Windows for execution of all Virtual Agents cluster test cases. *
 */

@TestBedDefinition
public class HTTPTunnellingClusterLinuxTestbed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Features to be installed for collector
        List<String> collectorChosenFeatures = Arrays.asList("Enterprise Manager", "WebView");

        // Collector1 role
        EmRole collector1Role =
            new EmRole.LinuxBuilder(AgentControllabilityConstants.COLLECTOR1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(AgentControllabilityConstants.MOM_ROLE_ID))
                .nostartEM().nostartWV().build();

        // MOM role
        EmRole momRole =
            new EmRole.LinuxBuilder(AgentControllabilityConstants.MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).emCollector(collector1Role)
                .nostartEM().nostartWV().build();

        ApacheRole apacheRole = new ApacheRole.LinuxBuilder(AgentControllabilityConstants.APACHE_ROLE_ID).build();


        // creates Generic roles to download artifacts
        // QAAppRole for Tomcat
        WebAppRole<TomcatRole> qaAppTomcatRole =
            new QaAppTomcatRole.Builder(AgentControllabilityConstants.QA_APP_TOMCAT_ROLE_ID,
                tasResolver).cargoDeploy().contextName("qa-app").build();

        // Tomcat Role
        TomcatRole tomcatRole =
            new TomcatRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_ROLE_ID, tasResolver)
                .tomcatVersion(TomcatVersion.v70).tomcatCatalinaPort(9091).webApp(qaAppTomcatRole)
                .build();

        // Tomcat Agent Role
        IRole tomcatAgentRole =
            new AgentRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_AGENT_ROLE_ID,
                tasResolver).webAppServer(tomcatRole)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole).build();

        // Configuration of Collector1 machine
        TestbedMachine collector1Machine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.COLLECTOR1_MACHINE_ID,
                AgentControllabilityConstants.CO66_TEMPLATE_ID, collector1Role);

        // Configuration of mom machine
        TestbedMachine momMachine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.MOM_MACHINE_ID,
                AgentControllabilityConstants.CO66_TEMPLATE_ID, momRole);

        // Configuration of Agent Machine
        TestbedMachine agentMachine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.TOMCAT_MACHINE_ID,
                AgentControllabilityConstants.CO66_TEMPLATE_ID, tomcatRole, qaAppTomcatRole,
                tomcatAgentRole);

        // Configuration of Apache Machine
        TestbedMachine apacheMachine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.APACHE_MACHINE_ID,
                AgentControllabilityConstants.CO66_TEMPLATE_ID,apacheRole);

        momMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", momRole
            .getDeployEmFlowContext().getInstallDir() + "/logs"));
        collector1Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector1Role
            .getDeployEmFlowContext().getInstallDir() + "/logs"));
        agentMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcatRole
            .getTomcatFlowContext().getTomcatInstallDir() + "/wily/logs"));

        return Testbed.create(this, collector1Machine, momMachine, agentMachine, apacheMachine);

    }
}
