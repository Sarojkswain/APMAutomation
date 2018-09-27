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
 * The testbed is for deployment of CDV , a MOM and a Collector
 *  
 */

@TestBedDefinition
public class CDVOneClusterOneTomcatLinuxTestbed implements ITestbedFactory {

   public final static String CDV_MACHINE_ID = CDVConstants.CDV_MACHINE_ID;
   protected final String EM_MACHINE_ID = CDVConstants.EM_MACHINE_ID;
   
   protected final String CDV_ROLE_ID = CDVConstants.CDV_ROLE_ID;
   protected final String MOM_ROLE_ID = CDVConstants.MOM_ROLE_ID;
   protected final String COLLECTOR1_ROLE_ID = CDVConstants.COLLECTOR1_ROLE_ID;

   protected final String EM_TEMPLATE_ID = CDVConstants.EM_TEMPLATE_ID_LINUX;

   protected final String AGENT_MACHINE_ID = CDVConstants.AGENT_MACHINE_ID;
   protected final String AGENT_MACHINE_TEMPLATE_ID = CDVConstants.AGENT_MACHINE_TEMPLATE_ID_LINUX;
       
   protected final String TOMCAT_ROLE_ID = CDVConstants.TOMCAT_ROLE_ID;
   protected final String TOMCAT_AGENT_ROLE_ID = CDVConstants.TOMCAT_AGENT_ROLE_ID;
   protected final String QA_APP_TOMCAT_ROLE_ID = CDVConstants.QA_APP_TOMCAT_ROLE_ID;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Features to be installed for collector
        List<String> emChosenFeatures = Arrays.asList("Enterprise Manager", "WebView");

        
        // Collector1 role
        EmRole collector1Role =
            new EmRole.LinuxBuilder(COLLECTOR1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .silentInstallChosenFeatures(emChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID))
                .installDir(TasBuilder.LINUX_SOFTWARE_LOC+"em_col")
                .emPort(5005).emWebPort(8085).wvEmPort(8090)
                .nostartEM().nostartWV()
                .build();
    
        // MOM role
        EmRole momRole =
            new EmRole.LinuxBuilder(MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .emCollector(collector1Role)
                .nostartEM().nostartWV().build();
        
        //CDV role
        EmRole cdvRole = new EmRole.LinuxBuilder(CDV_ROLE_ID, tasResolver)
        .emClusterRole(DeployEMFlowContext.EmRoleEnum.CDV)
        .silentInstallChosenFeatures(emChosenFeatures)
        .emCollector(collector1Role)
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
      
        
        // Configuration of Cdv machine
        TestbedMachine cdvMachine =
            TestBedUtils
                .createLinuxMachine(CDV_MACHINE_ID, EM_TEMPLATE_ID, cdvRole); 
        
        // Configuration of Collector1 machine
        TestbedMachine emMachine =
            TestBedUtils
                .createLinuxMachine(EM_MACHINE_ID, EM_TEMPLATE_ID, momRole, collector1Role);


        // Configuration of Agent Machine
        TestbedMachine agentMachine =
            TestBedUtils.createLinuxMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID,
                tomcatRole, qaAppTomcatRole, tomcatAgentRole);
        
        return Testbed.create(this, cdvMachine, emMachine, agentMachine);

    }


}
