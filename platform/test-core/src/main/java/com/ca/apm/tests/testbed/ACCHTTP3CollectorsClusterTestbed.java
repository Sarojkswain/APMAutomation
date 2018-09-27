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
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;

import java.util.Arrays;
import java.util.List;

/**
 * SampleTestbed class.
 * <p>
 * Testbed description.
 */
@TestBedDefinition
public class ACCHTTP3CollectorsClusterTestbed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        List<String> collectorChosenFeatures = Arrays.asList("Enterprise Manager", "WebView");

        EmRole collector1Role =
            new EmRole.LinuxBuilder(AgentControllabilityConstants.COLLECTOR1_ROLE_ID, tasResolver).emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).installDir("/opt/automation/deployed/collector1")
                //                    .version(IBuiltArtifact.Version.SNAPSHOT_DEV_99_99)
                .silentInstallChosenFeatures(collectorChosenFeatures).dbhost(tasResolver.getHostnameById(AgentControllabilityConstants.MOM_ROLE_ID)).nostartEM().nostartWV().build();

        EmRole collector2Role =
            new EmRole.LinuxBuilder(AgentControllabilityConstants.COLLECTOR2_ROLE_ID, tasResolver).emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).installDir("/opt/automation/deployed/collector2")
                //                    .version(IBuiltArtifact.Version.SNAPSHOT_DEV_99_99)
                .silentInstallChosenFeatures(collectorChosenFeatures).dbhost(tasResolver.getHostnameById(AgentControllabilityConstants.MOM_ROLE_ID)).nostartEM().nostartWV().build();

        EmRole collector3Role =
            new EmRole.LinuxBuilder(AgentControllabilityConstants.COLLECTOR3_ROLE_ID, tasResolver).emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).installDir("/opt/automation/deployed/collector3")
                //                    .version(IBuiltArtifact.Version.SNAPSHOT_DEV_99_99)
                .silentInstallChosenFeatures(collectorChosenFeatures).dbhost(tasResolver.getHostnameById(AgentControllabilityConstants.MOM_ROLE_ID)).nostartEM().nostartWV().build();

        // MOM role
        EmRole momRole =
            new EmRole.LinuxBuilder(AgentControllabilityConstants.MOM_ROLE_ID, tasResolver).emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                //                    .version(IBuiltArtifact.Version.SNAPSHOT_DEV_99_99)
                .emCollector(collector1Role).emCollector(collector2Role).emCollector(collector3Role).nostartEM().nostartWV().build();


        // create QAApp role for Tomcat
        WebAppRole<TomcatRole> qaAppTomcatRole1 =
            new QaAppTomcatRole.Builder(AgentControllabilityConstants.QA_APP_TOMCAT_ROLE1_ID, tasResolver).cargoDeploy().contextName("qa-app").build();

        WebAppRole<TomcatRole> qaAppTomcatRole2 =
            new QaAppTomcatRole.Builder(AgentControllabilityConstants.QA_APP_TOMCAT_ROLE2_ID, tasResolver).cargoDeploy().contextName("qa-app").build();

        WebAppRole<TomcatRole> qaAppTomcatRole3 =
            new QaAppTomcatRole.Builder(AgentControllabilityConstants.QA_APP_TOMCAT_ROLE3_ID, tasResolver).cargoDeploy().contextName("qa-app").build();


        // create Tomcat role
        TomcatRole tomcatRole1 =
            new TomcatRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_ROLE1_ID, tasResolver).tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(9091).webApp(qaAppTomcatRole1)
                //                .installDir("/opt/automation/deployed/tomcat1")
                .build();


        // create Tomcat Agent role
        IRole tomcatAgentRole1 =
            new AgentRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_AGENT1_ROLE_ID, tasResolver).webAppRole(tomcatRole1).customName("Tomcat1").intrumentationLevel(AgentInstrumentationLevel.FULL)
                //                .installDir("/opt/automation/deployed/tomcat1/wily")
                //                    .version(IBuiltArtifact.Version.SNAPSHOT_DEV_99_99)
                .emRole(momRole).build();


        TomcatRole tomcatRole2 =
            new TomcatRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_ROLE2_ID, tasResolver).tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(9092).webApp(qaAppTomcatRole2)
                //                .installDir("/opt/automation/deployed/tomcat2")
                .build();


        // create Tomcat Agent role
        IRole tomcatAgentRole2 =
            new AgentRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_AGENT2_ROLE_ID, tasResolver).webAppRole(tomcatRole2).customName("Tomcat2").intrumentationLevel(AgentInstrumentationLevel.FULL)
                //                .installDir("/opt/automation/deployed/tomcat2/wily")
                //                    .version(IBuiltArtifact.Version.SNAPSHOT_DEV_99_99)
                .emRole(momRole).build();


        TomcatRole tomcatRole3 =
            new TomcatRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_ROLE3_ID, tasResolver).tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(9093).webApp(qaAppTomcatRole3)
                //                .installDir("/opt/automation/deployed/tomcat3")
                .build();


        // create Tomcat Agent role
        IRole tomcatAgentRole3 =
            new AgentRole.LinuxBuilder(AgentControllabilityConstants.TOMCAT_AGENT3_ROLE_ID, tasResolver).webAppRole(tomcatRole3).customName("Tomcat3").intrumentationLevel(AgentInstrumentationLevel.FULL)
                //                .installDir("/opt/automation/deployed/tomcat3/wily")
                //                    .version(IBuiltArtifact.Version.SNAPSHOT_DEV_99_99)
                .emRole(momRole).build();

        /*qaAppTomcatRole1.before(tomcatAgentRole1);
        tomcatAgentRole1.after(tomcatRole1);

        qaAppTomcatRole2.before(tomcatAgentRole2);
        tomcatAgentRole2.after(tomcatRole2);

        qaAppTomcatRole3.before(tomcatAgentRole3);
        tomcatAgentRole3.after(tomcatRole3);
*/
        // map roles to machines
        ITestbedMachine momMachine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.MOM_MACHINE_ID, AgentControllabilityConstants.CO66_TEMPLATE_ID);
        ITestbedMachine collector1Machine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.COLLECTOR1_MACHINE_ID, AgentControllabilityConstants.CO66_TEMPLATE_ID);
        ITestbedMachine collector2Machine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.COLLECTOR2_MACHINE_ID, AgentControllabilityConstants.CO66_TEMPLATE_ID);
        ITestbedMachine collector3Machine =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.COLLECTOR3_MACHINE_ID, AgentControllabilityConstants.CO66_TEMPLATE_ID);
        momMachine.addRole(momRole);
        collector1Machine.addRole(collector1Role);
        collector2Machine.addRole(collector2Role);
        collector3Machine.addRole(collector3Role);
        ITestbedMachine tomcatMachine1 =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.TOMCAT_MACHINE_ID1, AgentControllabilityConstants.CO65_TEMPLATE_ID);
        ITestbedMachine tomcatMachine2 =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.TOMCAT_MACHINE_ID2, AgentControllabilityConstants.CO65_TEMPLATE_ID);
        ITestbedMachine tomcatMachine3 =
            TestBedUtils.createLinuxMachine(AgentControllabilityConstants.TOMCAT_MACHINE_ID3, AgentControllabilityConstants.CO65_TEMPLATE_ID);
        tomcatMachine1.addRole(tomcatRole1, qaAppTomcatRole1, tomcatAgentRole1);
        tomcatMachine2.addRole(tomcatRole2, qaAppTomcatRole2, tomcatAgentRole2);
        tomcatMachine3.addRole(tomcatRole3, qaAppTomcatRole3, tomcatAgentRole3);

        return new Testbed(getClass().getSimpleName()).addMachine(momMachine, collector1Machine, collector2Machine, collector3Machine, tomcatMachine1, tomcatMachine2, tomcatMachine3);
    }
}
