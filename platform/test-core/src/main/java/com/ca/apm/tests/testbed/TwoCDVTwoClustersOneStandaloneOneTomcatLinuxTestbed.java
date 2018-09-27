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
 * Date : 1/11/2017
 */
package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.tests.cdv.CDVConstants;

import static com.ca.apm.tests.cdv.CDVConstants.*;

import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
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

/**
 * The testbed is for deployment of Two CDVs connecting to both collectors on single machine, 
 * Two Cluster with one Collector each on different machine, Standalone EM and Tomcat Agent(connecting to MoM1)
 * on another Machine.
 */

@TestBedDefinition
public class TwoCDVTwoClustersOneStandaloneOneTomcatLinuxTestbed implements ITestbedFactory {


    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Features to be installed for collector
        List<String> emChosenFeatures = Arrays.asList("Enterprise Manager", "WebView");
        
        
        // MOM1 Collector1 role
        EmRole mom1Col1Role =
            new EmRole.LinuxBuilder(MOM1_COL1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(emChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM1_ROLE_ID))
                .installDir(TasBuilder.LINUX_SOFTWARE_LOC+"em_col1")
                .emPort(5002).emWebPort(8082).wvEmPort(8090)
                .nostartEM().nostartWV()
                .build();
    
        // MOM1 role
        EmRole mom1Role =
            new EmRole.LinuxBuilder(MOM1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .emCollector(mom1Col1Role)
                .nostartEM().nostartWV().build();
        
        // MOM2 Collector1 role
        EmRole mom2Col1Role =
            new EmRole.LinuxBuilder(MOM2_COL1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(emChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM2_ROLE_ID))
                .installDir(TasBuilder.LINUX_SOFTWARE_LOC+"em_col1")
                .emPort(5002).emWebPort(8082).wvEmPort(8090)
                .nostartEM().nostartWV()
                .build();
    
        // MOM2 role
        EmRole mom2Role =
            new EmRole.LinuxBuilder(MOM2_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .emCollector(mom2Col1Role)
                .nostartEM().nostartWV().build();
        
        //CDV1 role
        EmRole cdv1Role = new EmRole.LinuxBuilder(CDV1_ROLE_ID, tasResolver)
        .emClusterRole(DeployEMFlowContext.EmRoleEnum.CDV)
        .silentInstallChosenFeatures(emChosenFeatures)
        .installDir(TasBuilder.LINUX_SOFTWARE_LOC+"cdv1")
        .emCollector(mom1Col1Role)
        .emCollector(mom2Col1Role)
        .nostartEM().nostartWV().build();

      //CDV2 role
        EmRole cdv2Role = new EmRole.LinuxBuilder(CDV2_ROLE_ID, tasResolver)
        .emClusterRole(DeployEMFlowContext.EmRoleEnum.CDV)
        .silentInstallChosenFeatures(emChosenFeatures)
        .installDir(TasBuilder.LINUX_SOFTWARE_LOC+"cdv2")
        .emCollector(mom1Col1Role)
        .emCollector(mom2Col1Role)
        .emPort(5002).emWebPort(8082).wvEmPort(8090)
        .nostartEM().nostartWV().build();
        
        // Standalone EM role
        EmRole standaloneEmRole =
            new EmRole.LinuxBuilder(STANDALONE_EM_ROLE_ID, tasResolver)
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
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(mom1Role)
                .build();
      
        
        // Configuration of Cdv machine
        TestbedMachine cdvMachine =
            TestBedUtils
                .createLinuxMachine(CDV_MACHINE_ID, EM_TEMPLATE_ID_LINUX, cdv1Role, cdv2Role); 
        
        // Configuration of Cluster1 machine
        TestbedMachine mom1Machine =
            TestBedUtils
                .createLinuxMachine(MOM1_MACHINE_ID, EM_TEMPLATE_ID_LINUX, mom1Role, mom1Col1Role);

        // Configuration of Cluster2 machine
        TestbedMachine mom2Machine =
            TestBedUtils
                .createLinuxMachine(MOM2_MACHINE_ID, EM_TEMPLATE_ID_LINUX, mom2Role, mom2Col1Role);
        
        // Configuration of Agent Machine
        TestbedMachine agentMachine =
            TestBedUtils.createLinuxMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID_LINUX,
                tomcatRole, qaAppTomcatRole, tomcatAgentRole, standaloneEmRole);
        
        return Testbed.create(this, cdvMachine, mom1Machine, mom2Machine, agentMachine);

    }


}
