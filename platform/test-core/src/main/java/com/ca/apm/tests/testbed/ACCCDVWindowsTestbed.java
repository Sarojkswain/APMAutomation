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
 * Author : GAMSA03/ SANTOSH JAMMI
 * Author : KETSW01/ KETHIREDDY SWETHA
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 20/11/2015
 */
package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.tests.cdv.CDVConstants;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
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
 * The testbed is for deployment of CDV , a MOM and a Collector
 *  
 */

@TestBedDefinition
public class ACCCDVWindowsTestbed implements ITestbedFactory {

   public static final String CDV_MACHINE_ID = CDVConstants.CDV_MACHINE_ID;
   public static final String MOM1_MACHINE_ID = CDVConstants.MOM1_MACHINE_ID;
   public static final String MOM2_MACHINE_ID = CDVConstants.MOM2_MACHINE_ID;
   
   public static final String CDV_ROLE_ID = CDVConstants.CDV_ROLE_ID;
   public static final String MOM1_ROLE_ID = CDVConstants.MOM1_ROLE_ID;
   public static final String MOM2_ROLE_ID = CDVConstants.MOM2_ROLE_ID;
   public static final String MOM1_COL1_ROLE_ID = CDVConstants.MOM1_COL1_ROLE_ID;
   public static final String MOM1_COL2_ROLE_ID = CDVConstants.MOM1_COL2_ROLE_ID;
   public static final String MOM2_COL1_ROLE_ID = CDVConstants.MOM2_COL1_ROLE_ID;
   public static final String MOM2_COL2_ROLE_ID = CDVConstants.MOM2_COL2_ROLE_ID;

   public static final String EM_TEMPLATE_ID = CDVConstants.EM_TEMPLATE_ID_WIN;

   public static final String AGENT_MACHINE_ID = CDVConstants.AGENT_MACHINE_ID;
   public static final String AGENT_MACHINE_TEMPLATE_ID = CDVConstants.AGENT_MACHINE_TEMPLATE_ID_WIN;
       
   public static final String TOMCAT_ROLE_ID = CDVConstants.TOMCAT_ROLE_ID;
   public static final String TOMCAT_AGENT_ROLE_ID = CDVConstants.TOMCAT_AGENT_ROLE_ID;
   public static final String QA_APP_TOMCAT_ROLE_ID = CDVConstants.QA_APP_TOMCAT_ROLE_ID;

   public static final String JBOSS_ROLE_ID = CDVConstants.JBOSS_ROLE_ID;
   public static final String JBOSS_AGENT_ROLE_ID = CDVConstants.JBOSS_AGENT_ROLE_ID;
   public static final String QA_APP_JBOSS_ROLE_ID = CDVConstants.QA_APP_JBOSS_ROLE_ID;
   
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Features to be installed for collector
        List<String> emChosenFeatures = Arrays.asList("Enterprise Manager", "WebView");

        
        // Mom1Collector1 role
        EmRole mom1Col1Role =
            new EmRole.Builder(MOM1_COL1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(emChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM1_ROLE_ID))
                .installDir(TasBuilder.WIN_SOFTWARE_LOC+"em_col1")
                .emPort(5005).emWebPort(8085).wvEmPort(8090)
                .nostartEM().nostartWV()
                .build();
        // Mom1Collector2 role
        EmRole mom1Col2Role =
            new EmRole.Builder(MOM1_COL2_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(emChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM1_ROLE_ID))
                .installDir(TasBuilder.WIN_SOFTWARE_LOC+"em_col2")
                .emPort(5006).emWebPort(8086).wvEmPort(8091)
                .nostartEM().nostartWV()
                .build();
        // MOM1 role
        EmRole mom1Role =
            new EmRole.Builder(MOM1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .emCollector(mom1Col1Role)
                .emCollector(mom1Col2Role)
                .nostartEM().nostartWV().build();
        
        // Mom2Collector1 role
        EmRole mom2Col1Role =
            new EmRole.Builder(MOM2_COL1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(emChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM2_ROLE_ID))
                .installDir(TasBuilder.WIN_SOFTWARE_LOC+"em_col1")
                .emPort(5005).emWebPort(8085).wvEmPort(8090)
                .nostartEM().nostartWV()
                .build();
        // Mom2Collector2 role
        EmRole mom2Col2Role =
            new EmRole.Builder(MOM2_COL2_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(emChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM2_ROLE_ID))
                .installDir(TasBuilder.WIN_SOFTWARE_LOC+"em_col2")
                .emPort(5006).emWebPort(8086).wvEmPort(8091)
                .nostartEM().nostartWV()
                .build();
        // MOM2 role
        EmRole mom2Role =
            new EmRole.Builder(MOM2_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .emCollector(mom2Col1Role)
                .emCollector(mom2Col2Role)
                .nostartEM().nostartWV().build();
        
        //CDV role
        EmRole cdvRole = new EmRole.Builder(CDV_ROLE_ID, tasResolver)
        .emClusterRole(DeployEMFlowContext.EmRoleEnum.CDV)
        .silentInstallChosenFeatures(emChosenFeatures)
        .emCollector(mom1Col1Role)
        .emCollector(mom1Col2Role)
        .emCollector(mom2Col1Role)
        .emCollector(mom2Col2Role)
        .nostartEM().nostartWV().build();

        // QAAppRole for Tomcat
        WebAppRole<TomcatRole> qaAppTomcatRole =
            new QaAppTomcatRole.Builder(QA_APP_TOMCAT_ROLE_ID, tasResolver)
                .cargoDeploy().contextName("qa-app")
                .build();        

        // Tomcat Role
        TomcatRole tomcatRole =
            new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver).tomcatVersion(TomcatVersion.v70)
                .tomcatCatalinaPort(9091).webApp(qaAppTomcatRole).build();

        // Tomcat Agent Role
        IRole tomcatAgentRole =
            new AgentRole.Builder(TOMCAT_AGENT_ROLE_ID, tasResolver).webAppServer(tomcatRole)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(mom1Role)
                .build();
        
        // QAAppRole for JBoss
        WebAppRole<JbossRole> qaAppJbossRole =
            new QaAppJbossRole.Builder(QA_APP_JBOSS_ROLE_ID, tasResolver)
                .cargoDeploy().contextName("qa-app")
                .build();
        
        // Jboss Role
        JbossRole jbossRole =
            new JbossRole.Builder(JBOSS_ROLE_ID, tasResolver).version(JBossVersion.JBOSS711)
                .addWebAppRole(qaAppJbossRole).build();

        // Jboss Agent Role
        IRole jbossAgentRole =
            new AgentRole.Builder(JBOSS_AGENT_ROLE_ID, tasResolver).webAppServer(jbossRole)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(mom1Role)
                .build();
      
        
        // Configuration of Cdv machine
        TestbedMachine cdvMachine =
            TestBedUtils
                .createWindowsMachine(CDV_MACHINE_ID, EM_TEMPLATE_ID, cdvRole); 
        
        // Configuration of mom1 machine
        TestbedMachine mom1Machine =
            TestBedUtils
                .createWindowsMachine(MOM1_MACHINE_ID, EM_TEMPLATE_ID, mom1Role, mom1Col1Role, mom1Col2Role);
        // Configuration of mom2 machine
        TestbedMachine mom2Machine =
            TestBedUtils
                .createWindowsMachine(MOM2_MACHINE_ID, EM_TEMPLATE_ID, mom2Role, mom2Col1Role, mom2Col2Role);


        // Configuration of Agent Machine
        TestbedMachine agentMachine =
            TestBedUtils.createWindowsMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID,
                tomcatRole, qaAppTomcatRole, tomcatAgentRole, jbossRole, qaAppJbossRole,
                jbossAgentRole);
        
        cdvMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", cdvRole.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        mom1Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", mom1Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        mom1Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", mom1Col1Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        mom1Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", mom1Col2Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        mom2Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", mom2Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        mom2Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", mom2Col1Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        mom2Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", mom2Col2Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        agentMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcatRole.getTomcatFlowContext().getTomcatInstallDir()+ "\\wily\\logs"));
        agentMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", jbossRole.getDeployJbossFlowContext().getJbossInstallDirectory()+ "\\wily\\logs"));
        
        return Testbed.create(this, cdvMachine, mom1Machine, mom2Machine, agentMachine);

    }


}
