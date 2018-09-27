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
 * Author : GAMSA03/ SANTOSH KUMAR GAMPA
 * Date : 30/08/2016
 */
package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;

import static com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants.*;

import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.QaAppJbossRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * The testbed is for deployment of EM Cluster with two Collectors, one Tomcat and one JBoss Server
 * EM Database is of Postgres on MOM machine and Tomcat is on Collector1 Machine while JBoss on Collector2.
 * This is on CentOS and to serves as Common Testbed for APM Testing *
 */

@TestBedDefinition
public class TwoCollectorsOneTomcatOneJBossCentOSTestbed implements ITestbedFactory {


    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Features to be installed for collector
        List<String> collectorChosenFeatures = Arrays.asList("Enterprise Manager", "WebView");

        // Collector1 role
        EmRole collector1Role =
            new EmRole.LinuxBuilder(COLLECTOR1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID)).nostartEM()
                .nostartWV().build();

        // Collector1 role
        EmRole collector2Role =
            new EmRole.LinuxBuilder(COLLECTOR2_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID)).nostartEM()
                .nostartWV().build();

        // MOM role
        EmRole momRole =
            new EmRole.LinuxBuilder(MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).emCollector(collector1Role)
                .emCollector(collector2Role).nostartEM().nostartWV().build();

        // QAAppRole for Tomcat
        WebAppRole<TomcatRole> qaAppTomcatRole =
            new QaAppTomcatRole.Builder(QA_APP_TOMCAT_ROLE_ID, tasResolver)
                .cargoDeploy().contextName("qa-app").build();

        // Tomcat Role
        TomcatRole tomcatRole =
            new TomcatRole.LinuxBuilder(TOMCAT_ROLE_ID, tasResolver)
                .tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(9091).webApp(qaAppTomcatRole)
                .build();

        // Tomcat Agent Role
        IRole tomcatAgentRole =
            new AgentRole.LinuxBuilder(TOMCAT_AGENT_ROLE_ID, tasResolver)
                .webAppServer(tomcatRole).intrumentationLevel(AgentInstrumentationLevel.FULL)
                .emRole(momRole).build();

        // QAAppRole for JBoss
        WebAppRole<JbossRole> qaAppJbossRole =
            new QaAppJbossRole.Builder(QA_APP_JBOSS_ROLE_ID, tasResolver)
                .cargoDeploy().contextName("qa-app")
                .build();
        
        // Jboss Role
        JbossRole jbossRole =
            new JbossRole.LinuxBuilder(JBOSS_ROLE_ID, tasResolver).version(JBossVersion.JBOSS711)
                .addWebAppRole(qaAppJbossRole).build();

        // Jboss Agent Role
        IRole jbossAgentRole =
            new AgentRole.LinuxBuilder(JBOSS_AGENT_ROLE_ID, tasResolver).webAppServer(jbossRole)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole)
                .build();
        
        // Configuration of Collector1 and Tomcat machine
        TestbedMachine collector1Machine =
            TestBedUtils.createLinuxMachine(COLLECTOR1_MACHINE_ID,
            		CO66_TEMPLATE_ID, collector1Role, tomcatRole, tomcatAgentRole, qaAppTomcatRole);

        // Configuration of Collector2 and Tomcat machine
        TestbedMachine collector2Machine =
            TestBedUtils.createLinuxMachine(COLLECTOR2_MACHINE_ID,
            		CO66_TEMPLATE_ID, collector2Role, jbossRole, jbossAgentRole, qaAppJbossRole);
        
        // Configuration of mom machine
        TestbedMachine momMachine =
            TestBedUtils.createLinuxMachine(MOM_MACHINE_ID,
            		CO66_TEMPLATE_ID, momRole);

        TestbedMachine jbossMachine = collector2Machine;
        TestbedMachine tomcactMachine = collector1Machine;

        momMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", momRole.getDeployEmFlowContext().getInstallDir()+ "/logs"));
        collector1Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector1Role.getDeployEmFlowContext().getInstallDir()+ "/logs"));
        collector2Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector2Role.getDeployEmFlowContext().getInstallDir()+ "/logs"));
        tomcactMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcatRole.getTomcatFlowContext().getTomcatInstallDir()+ "/wily/logs"));
        jbossMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", jbossRole.getInstallDir() + "/wily/logs"));
        
        momMachine.addRemoteResource(RemoteResource.createFromRegExp(".*_(\\d+)", momRole.getDeployEmFlowContext().getInstallDir()+ "/config"));
        collector1Machine.addRemoteResource(RemoteResource.createFromRegExp(".*_(\\d+)", collector1Role.getDeployEmFlowContext().getInstallDir()+ "/config"));
        collector2Machine.addRemoteResource(RemoteResource.createFromRegExp(".*_(\\d+)", collector2Role.getDeployEmFlowContext().getInstallDir()+ "/config"));
        tomcactMachine.addRemoteResource(RemoteResource.createFromRegExp(".*_(\\d+)", tomcatRole.getTomcatFlowContext().getTomcatInstallDir()+ "/wily/core/config"));
        jbossMachine.addRemoteResource(RemoteResource.createFromRegExp(".*_(\\d+)", jbossRole.getInstallDir() + "/wily/core/config"));
        
        return Testbed.create(this, collector1Machine, momMachine, collector2Machine);

    }
}
