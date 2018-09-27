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
 * Author : KETSW01/ KETHIREDDY SWETHA 
 * Author : BALRA06/ RADHA BALASUBRAMANIAM
 */
package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.tests.agentcontrollability.AccConstants;
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
 * The testbed is for deployment of standalone , Cluster with three collectors and 4 agents
 * in Windows for execution of all Virtual Agents cluster test cases. *
 */

@TestBedDefinition
public class ACCWindowsTestbed implements ITestbedFactory {
  
    public static final String MOM_MACHINE_ID = AccConstants.MOM_MACHINE_ID;
    public static final String COLLECTOR1_MACHINE_ID = AccConstants.COLLECTOR1_MACHINE_ID;
    public static final String COLLECTOR2_MACHINE_ID = AccConstants.COLLECTOR2_MACHINE_ID;
    public static final String COLLECTOR3_MACHINE_ID = AccConstants.COLLECTOR3_MACHINE_ID;
    public static final String STANDALONE_MACHINE_ID = AccConstants.STANDALONE_MACHINE_ID;

    public static final String MOM_ROLE_ID = AccConstants.MOM_ROLE_ID;
    public static final String COLLECTOR1_ROLE_ID = AccConstants.COLLECTOR1_ROLE_ID;
    public static final String COLLECTOR2_ROLE_ID = AccConstants.COLLECTOR2_ROLE_ID;
    public static final String COLLECTOR3_ROLE_ID = AccConstants.COLLECTOR3_ROLE_ID;
    public static final String STANDALONE_ROLE_ID = AccConstants.STANDALONE_ROLE_ID;

    protected static String EM_TEMPLATE_ID = AccConstants.EM_TEMPLATE_ID_WIN;

    public static final String AGENT_MACHINE_ID = AccConstants.AGENT_MACHINE_ID;
    private static final String AGENT_MACHINE_TEMPLATE_ID = AccConstants.AGENT_MACHINE_TEMPLATE_ID_WIN;    
    
    public static final String TOMCAT_ROLE_ID = AccConstants.TOMCAT_ROLE_ID;
    public static final String TOMCAT_AGENT_ROLE_ID = AccConstants.TOMCAT_AGENT_ROLE_ID;
    public static final String QA_APP_TOMCAT_ROLE_ID = AccConstants.QA_APP_TOMCAT_ROLE_ID;

    public static final String JBOSS_ROLE_ID = AccConstants.JBOSS_ROLE_ID;
    public static final String JBOSS_AGENT_ROLE_ID = AccConstants.JBOSS_AGENT_ROLE_ID;
    public static final String QA_APP_JBOSS_ROLE_ID = AccConstants.QA_APP_JBOSS_ROLE_ID; 
    
    public static final String TOMCAT1_ROLE_ID = AccConstants.TOMCAT1_ROLE_ID;
    public static final String TOMCAT1_AGENT_ROLE_ID = AccConstants.TOMCAT1_AGENT_ROLE_ID;
    public static final String QA_APP_TOMCAT1_ROLE_ID = AccConstants.QA_APP_TOMCAT1_ROLE_ID;
    public static final String TOMCAT1_AGENT_NAME = AccConstants.TOMCAT1_AGENT_NAME;

    public static final String JBOSS1_ROLE_ID = AccConstants.JBOSS1_ROLE_ID;
    public static final String JBOSS1_AGENT_ROLE_ID = AccConstants.JBOSS1_AGENT_ROLE_ID;
    public static final String QA_APP_JBOSS1_ROLE_ID = AccConstants.QA_APP_JBOSS1_ROLE_ID;
    public static final String JBOSS1_AGENT_NAME = AccConstants.JBOSS1_AGENT_NAME;
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Features to be installed for collector
        List<String> collectorChosenFeatures = Arrays.asList("Enterprise Manager", "WebView");
        
        // Collector1 role
        EmRole collector1Role =
            new EmRole.Builder(COLLECTOR1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID))
                .nostartEM().nostartWV()
                .build();

        // Collector2 role
        EmRole collector2Role =
            new EmRole.Builder(COLLECTOR2_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID))
                .nostartEM().nostartWV()
                .build();
        
        // Collector3 role
        EmRole collector3Role =
            new EmRole.Builder(COLLECTOR3_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID))
                .nostartEM().nostartWV()
                .build();

        // MOM role
        EmRole momRole =
            new EmRole.Builder(MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .emCollector(collector1Role)
                .emCollector(collector2Role)
                .emCollector(collector3Role).nostartEM().nostartWV().build();
        
     // Standalone role
        EmRole standaloneRole =
            new EmRole.Builder(STANDALONE_ROLE_ID, tasResolver)        	
                .nostartEM().nostartWV().build();    
        

        // QAAppRole for JBoss
        WebAppRole<JbossRole> qaAppJbossRole =
            new QaAppJbossRole.Builder(QA_APP_JBOSS_ROLE_ID, tasResolver)
                .cargoDeploy().contextName("qa-app")
                .build();

        // QAAppRole for Tomcat
        WebAppRole<TomcatRole> qaAppTomcatRole =
            new QaAppTomcatRole.Builder(QA_APP_TOMCAT_ROLE_ID, tasResolver)
                .cargoDeploy().contextName("qa-app")
                .build();      

        // Jboss Role
        JbossRole jbossRole =
            new JbossRole.Builder(JBOSS_ROLE_ID, tasResolver).version(JBossVersion.JBOSS711)
                .addWebAppRole(qaAppJbossRole).build();

        // Jboss Agent Role
        IRole jbossAgentRole =
            new AgentRole.Builder(JBOSS_AGENT_ROLE_ID, tasResolver).webAppServer(jbossRole)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole)
                .build();

        // Tomcat Role
        TomcatRole tomcatRole =
            new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver).tomcatVersion(TomcatVersion.v70)
                .tomcatCatalinaPort(9091).webApp(qaAppTomcatRole).build();

        // Tomcat Agent Role
        IRole tomcatAgentRole =
            new AgentRole.Builder(TOMCAT_AGENT_ROLE_ID, tasResolver).webAppServer(tomcatRole)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole)
                .build();  
       
        // QAAppRole for JBoss1
        WebAppRole<JbossRole> qaAppJboss1Role =
            new QaAppJbossRole.Builder(QA_APP_JBOSS1_ROLE_ID, tasResolver)
                .cargoDeploy().contextName("qa-app")
                .build();

        // QAAppRole for Tomcat1
        WebAppRole<TomcatRole> qaAppTomcat1Role =
            new QaAppTomcatRole.Builder(QA_APP_TOMCAT1_ROLE_ID, tasResolver)
                .cargoDeploy().contextName("qa-app")
                .build();      

        // Jboss1 Role
        JbossRole jboss1Role =
            new JbossRole.Builder(JBOSS1_ROLE_ID, tasResolver).version(JBossVersion.JBOSS711)
                .addWebAppRole(qaAppJboss1Role).build();

        // Jboss1 Agent Role
        IRole jboss1AgentRole =
            new AgentRole.Builder(JBOSS1_AGENT_ROLE_ID, tasResolver).webAppServer(jboss1Role)
                .customName(JBOSS1_AGENT_NAME).intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole)
                .build();

        // Tomcat1 Role
        TomcatRole tomcat1Role =
            new TomcatRole.Builder(TOMCAT1_ROLE_ID, tasResolver).tomcatVersion(TomcatVersion.v70)
                .tomcatCatalinaPort(9091).webApp(qaAppTomcat1Role).build();

        // Tomcat1 Agent Role
        IRole tomcat1AgentRole =
            new AgentRole.Builder(TOMCAT1_AGENT_ROLE_ID, tasResolver).webAppServer(tomcat1Role)
                .customName(TOMCAT1_AGENT_NAME).intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole)
                .build();    
        
        // Configuration of Collector1 machine
        TestbedMachine collector1Machine =
            TestBedUtils
                .createWindowsMachine(COLLECTOR1_MACHINE_ID, EM_TEMPLATE_ID, collector1Role);

        // Configuration of Collector2 machine
        TestbedMachine collector2Machine =
            TestBedUtils
                .createWindowsMachine(COLLECTOR2_MACHINE_ID, EM_TEMPLATE_ID, collector2Role);
        
        // Configuration of Collector3 machine
        TestbedMachine collector3Machine =
            TestBedUtils
                .createWindowsMachine(COLLECTOR3_MACHINE_ID, EM_TEMPLATE_ID, collector3Role);

        // Configuration of mom machine
        TestbedMachine momMachine =
            TestBedUtils.createWindowsMachine(MOM_MACHINE_ID, EM_TEMPLATE_ID, momRole);
        
        // Configuration of standalone machine
        TestbedMachine standaloneMachine =
            TestBedUtils.createWindowsMachine(STANDALONE_MACHINE_ID, EM_TEMPLATE_ID, standaloneRole,
            		tomcat1Role, qaAppTomcat1Role, tomcat1AgentRole, jboss1Role, qaAppJboss1Role,
                    jboss1AgentRole);

        // Configuration of Agent Machine
        TestbedMachine agentMachine =
            TestBedUtils.createWindowsMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID,
                tomcatRole, qaAppTomcatRole, tomcatAgentRole, jbossRole, qaAppJbossRole,
                jbossAgentRole); 
        
        momMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", momRole.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        collector1Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector1Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        collector2Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector2Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        collector3Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector3Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        agentMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcatRole.getTomcatFlowContext().getTomcatInstallDir()+ "\\wily\\logs"));
        agentMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", jbossRole.getDeployJbossFlowContext().getJbossInstallDirectory()+ "\\wily\\logs"));
        standaloneMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", standaloneRole.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        standaloneMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcat1Role.getTomcatFlowContext().getTomcatInstallDir()+ "\\wily\\logs"));
        standaloneMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", jboss1Role.getDeployJbossFlowContext().getJbossInstallDirectory()+ "\\wily\\logs"));
        
        return Testbed.create(this, momMachine, collector1Machine, collector2Machine, collector3Machine, agentMachine, standaloneMachine);

    }

}
