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
 * Date : 20/04/2016
 */
package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;


import java.util.Arrays;
import java.util.List;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.GenericRole;
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
 * The testbed is for deployment of EM with Tomcat Agent
 * in Windows for execution of all Property Configuration test cases for GA EM. *
 */

@TestBedDefinition
public class EMPropertyConfigurationtWindowsTestbed implements ITestbedFactory {

    public static final String EM_MACHINE_ID = "testMachine";
    

    public static final String EM_ROLE_ID = "emRole";     
    public static final String TOMCAT_ROLE_ID = "tomcatRole";
    public static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    
        
    protected static String TEMPLATE_ID = TEMPLATE_W64;
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {

        // EM1 role
        EmRole em1Role =
            new EmRole.Builder(EM_ROLE_ID, tasResolver)              
                .build();

   
        // Tomcat Role
        TomcatRole tomcatRole =
            new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver).tomcatVersion(TomcatVersion.v70)
                .tomcatCatalinaPort(9091).build();

        // Tomcat Agent Role
        IRole tomcatAgentRole =
            new AgentRole.Builder(TOMCAT_AGENT_ROLE_ID, tasResolver).webAppServer(tomcatRole)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(em1Role).webAppAutoStart()
                .build();
        
        // Configuration of test machine
        TestbedMachine testMachine =
            TestBedUtils.createWindowsMachine(EM_MACHINE_ID, TEMPLATE_ID, em1Role, tomcatRole, tomcatAgentRole );
        
        return Testbed.create(this, testMachine);

    }

}
