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
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * SampleTestbed class.
 * <p>
 * Testbed description.
 */
@TestBedDefinition
public class AgentControllability3Collectors3TomcatAgentLinuxTestbed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        List<String> collectorChosenFeatures = Arrays.asList("Enterprise Manager", "WebView");

        EmRole collector1Role =
            new EmRole.LinuxBuilder(AgentControllabilityConstants.COLLECTOR1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(AgentControllabilityConstants.MOM_ROLE_ID))
                .nostartEM().nostartWV().build();

        EmRole collector2Role =
            new EmRole.LinuxBuilder(AgentControllabilityConstants.COLLECTOR2_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(AgentControllabilityConstants.MOM_ROLE_ID))
                .nostartEM().nostartWV().build();

        EmRole collector3Role =
            new EmRole.LinuxBuilder(AgentControllabilityConstants.COLLECTOR3_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(AgentControllabilityConstants.MOM_ROLE_ID))
                .nostartEM().nostartWV().build();

        // MOM role
        EmRole momRole =
            new EmRole.LinuxBuilder(AgentControllabilityConstants.MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).emCollector(collector1Role)
                .emCollector(collector2Role).emCollector(collector3Role).nostartEM().nostartWV()
                .build();


        // create QAApp role for Tomcat
        WebAppRole<TomcatRole> qaAppTomcatRole1 =
            new QaAppTomcatRole.Builder(AgentControllabilityConstants.QA_APP_TOMCAT_ROLE1_ID,
                tasResolver).cargoDeploy().contextName("qa-app").build();

        WebAppRole<TomcatRole> qaAppTomcatRole2 =
            new QaAppTomcatRole.Builder(AgentControllabilityConstants.QA_APP_TOMCAT_ROLE2_ID,
                tasResolver).cargoDeploy().contextName("qa-app").build();

        WebAppRole<TomcatRole> qaAppTomcatRole3 =
            new QaAppTomcatRole.Builder(AgentControllabilityConstants.QA_APP_TOMCAT_ROLE3_ID,
                tasResolver).cargoDeploy().contextName("qa-app").build();


        // create Tomcat role
        TomcatRole tomcatRole1 =
            new TomcatRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_ROLE1_ID, tasResolver)
                .tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(9091).webApp(qaAppTomcatRole1)
                .build();


        // create Tomcat Agent role
        IRole tomcatAgentRole1 =
            new AgentRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_AGENT1_ROLE_ID,
                tasResolver).webAppServer(tomcatRole1).customName("Tomcat1")
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole).build();


        TomcatRole tomcatRole2 =
            new TomcatRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_ROLE2_ID, tasResolver)
                .tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(9092).webApp(qaAppTomcatRole2)
                .build();


        // create Tomcat Agent role
        IRole tomcatAgentRole2 =
            new AgentRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_AGENT2_ROLE_ID,
                tasResolver).webAppServer(tomcatRole2).customName("Tomcat2")
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole).build();


        TomcatRole tomcatRole3 =
            new TomcatRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_ROLE3_ID, tasResolver)
                .tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(9093).webApp(qaAppTomcatRole3)
                .build();


        // create Tomcat Agent role
        IRole tomcatAgentRole3 =
            new AgentRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_AGENT3_ROLE_ID,
                tasResolver).webAppServer(tomcatRole3).customName("Tomcat3")
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole).build();

        // map roles to machines
        ITestbedMachine momMachine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.MOM_MACHINE_ID,
                AgentControllabilityConstants.CO66_TEMPLATE_ID);
        ITestbedMachine collector1Machine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.COLLECTOR1_MACHINE_ID,
                AgentControllabilityConstants.CO66_TEMPLATE_ID);
        ITestbedMachine collector2Machine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.COLLECTOR2_MACHINE_ID,
                AgentControllabilityConstants.CO66_TEMPLATE_ID);
        ITestbedMachine collector3Machine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.COLLECTOR3_MACHINE_ID,
                AgentControllabilityConstants.CO66_TEMPLATE_ID);
        momMachine.addRole(momRole);
        collector1Machine.addRole(collector1Role);
        collector2Machine.addRole(collector2Role);
        collector3Machine.addRole(collector3Role);
        ITestbedMachine tomcatMachine1 =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.TOMCAT_MACHINE_ID1,
                AgentControllabilityConstants.CO66_TEMPLATE_ID);
        ITestbedMachine tomcatMachine2 =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.TOMCAT_MACHINE_ID2,
                AgentControllabilityConstants.CO66_TEMPLATE_ID);
        ITestbedMachine tomcatMachine3 =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.TOMCAT_MACHINE_ID3,
                AgentControllabilityConstants.CO66_TEMPLATE_ID);
        tomcatMachine1.addRole(tomcatRole1, qaAppTomcatRole1, tomcatAgentRole1);
        tomcatMachine2.addRole(tomcatRole2, qaAppTomcatRole2, tomcatAgentRole2);
        tomcatMachine3.addRole(tomcatRole3, qaAppTomcatRole3, tomcatAgentRole3);

        momMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", momRole
            .getDeployEmFlowContext().getInstallDir() + "/logs"));
        collector1Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector1Role
            .getDeployEmFlowContext().getInstallDir() + "/logs"));
        collector2Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector2Role
            .getDeployEmFlowContext().getInstallDir() + "/logs"));
        collector3Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector3Role
            .getDeployEmFlowContext().getInstallDir() + "/logs"));
        tomcatMachine1.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcatRole1
            .getTomcatFlowContext().getTomcatInstallDir() + "/wily/logs"));
        tomcatMachine2.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcatRole2
            .getTomcatFlowContext().getTomcatInstallDir() + "/wily/logs"));
        tomcatMachine3.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcatRole3
            .getTomcatFlowContext().getTomcatInstallDir() + "/wily/logs"));

        return new Testbed(getClass().getSimpleName()).addMachine(momMachine, collector1Machine,
            collector2Machine, collector3Machine, tomcatMachine1, tomcatMachine2, tomcatMachine3);
    }
}
