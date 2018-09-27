/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 * Author : KETSW01/ KETHIREDDY SWETHA
 * Date : 20/11/2015
 */

package com.ca.apm.tests.testbed;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.QaAppJbossRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.TomcatVersion;



import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.builder.TasBuilder;
import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;

/**
 * SampleTestbed class.
 *
 * Testbed description.
 */
@TestBedDefinition
public class SmartStorLinux2EMSTestbed implements ITestbedFactory {

    public static final String EM_MACHINE_ID_1 = "emMachine1";
    public static final String EM_MACHINE_ID_2 = "emMachine2";
    public static final String EM_ROLE_ID_1 = "emRole1";
    public static final String EM_ROLE_ID_2 = "emRole2";
    private static final String EM_MACHINE_1_TEMPLATE_ID = TEMPLATE_CO66;
    private static final String EM_MACHINE_2_TEMPLATE_ID = TEMPLATE_CO66;
    
    public static final String AGENT_MACHINE_ID = "agentMachine";
    private static final String AGENT_MACHINE_TEMPLATE_ID = TEMPLATE_CO66;
    
    public static final String TOMCAT_ROLE_ID="tomcatRole";
    public static final String TOMCAT_AGENT_ROLE_ID="tomcatAgentRole";
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";
    
    public static final String JBOSS_ROLE_ID="JBossRole";    
    public static final String JBOSS_AGENT_ROLE_ID="JbossAgentRole";
    public static final String QA_APP_JBOSS_ROLE_ID = "qaAppJBossRole";
	
    public static final String CONFIG_FILES_ARTIFACT_VERSION = "1.0";
    public static final String CONFIG_FILES_LOC = TasBuilder.LINUX_SOFTWARE_LOC+"configFiles/" ;
    @Override
    public ITestbed create(ITasResolver tasResolver) {
      //create EM role
        EmRole emRole_1 = new EmRole.LinuxBuilder(EM_ROLE_ID_1, tasResolver)
            .nostartEM()
            .nostartWV()
            .build();
        
        EmRole emRole_2 = new EmRole.LinuxBuilder(EM_ROLE_ID_2, tasResolver)
        .nostartEM()
        .nostartWV()
        .build();
	  
      //create QAApp role for Jboss
        WebAppRole<JbossRole> qaAppJbossRole = new QaAppJbossRole.Builder(QA_APP_JBOSS_ROLE_ID, tasResolver)
        .cargoDeploy()
        .contextName("qa-app")
        .build();
      
      //create QAApp role for Tomcat   
        WebAppRole<TomcatRole> qaAppTomcatRole = new QaAppTomcatRole.Builder(QA_APP_TOMCAT_ROLE_ID, tasResolver)
        .cargoDeploy()
        .contextName("qa-app")
        .build();
      
        //create Jboss role  
        JbossRole jbossRole = new JbossRole.LinuxBuilder(JBOSS_ROLE_ID, tasResolver)
        .version(JBossVersion.JBOSS711)
        .addWebAppRole(qaAppJbossRole)
        .build();
        
        //create Jboss agent role
        IRole jbossAgentRole = new AgentRole.LinuxBuilder(JBOSS_AGENT_ROLE_ID, tasResolver)
        .webAppRole(jbossRole)
		.intrumentationLevel(AgentInstrumentationLevel.FULL)
		.emRole(emRole_2)
        .build();
           
        //create Tomcat role
        TomcatRole tomcatRole = new TomcatRole.LinuxBuilder(TOMCAT_ROLE_ID, tasResolver)
        .tomcatVersion(TomcatVersion.v70)
		.tomcatCatalinaPort(9091)
        .webApp(qaAppTomcatRole)
        .build();
        
        
        //create Tomcat Agent role
        IRole tomcatAgentRole = new AgentRole.LinuxBuilder(TOMCAT_AGENT_ROLE_ID, tasResolver)
        .webAppRole(tomcatRole)
		.intrumentationLevel(AgentInstrumentationLevel.FULL)
		.emRole(emRole_1)
        .build();  
        
        //map roles to machines
        ITestbedMachine emMachine_1 = TestBedUtils.createLinuxMachine(EM_MACHINE_ID_1, EM_MACHINE_1_TEMPLATE_ID);
        emMachine_1.addRole(emRole_1);
       
        ITestbedMachine emMachine_2 = TestBedUtils.createLinuxMachine(EM_MACHINE_ID_2, EM_MACHINE_2_TEMPLATE_ID);
        emMachine_1.addRole(emRole_2);
        
        ITestbedMachine tomcatAgentMachine = TestBedUtils.createLinuxMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID);
        tomcatAgentMachine.addRole(tomcatRole, qaAppTomcatRole,tomcatAgentRole, jbossRole, qaAppJbossRole,jbossAgentRole);
        
        return new Testbed(getClass().getSimpleName()).addMachine(emMachine_1,emMachine_2,tomcatAgentMachine);
    }
}

