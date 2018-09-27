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
 * Author : BALRA06/ RADHA BALASUBRAMANIAM 
 * 
 */
package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.tests.bpem.BPEMConstants;
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

/**
 * The testbed is for deployment of Cluster with two collectors
 * and 1 agent on Windows for execution of bpem test cases. *
 */

@TestBedDefinition
public class BPEMWindowsTestbed implements ITestbedFactory {
  
    public static final String MOM_MACHINE_ID = BPEMConstants.MOM_MACHINE_ID;
    public static final String COLLECTOR1_MACHINE_ID = BPEMConstants.COLLECTOR1_MACHINE_ID;
    public static final String COLLECTOR2_MACHINE_ID = BPEMConstants.COLLECTOR2_MACHINE_ID;
   
    public static final String MOM_ROLE_ID = BPEMConstants.MOM_ROLE_ID;
    public static final String COLLECTOR1_ROLE_ID = BPEMConstants.COLLECTOR1_ROLE_ID;
    public static final String COLLECTOR2_ROLE_ID = BPEMConstants.COLLECTOR2_ROLE_ID;
   
    protected static String EM_TEMPLATE_ID = BPEMConstants.EM_TEMPLATE_ID_WIN;

    public static final String TOMCAT_ROLE_ID = BPEMConstants.TOMCAT_ROLE_ID;
    public static final String TOMCAT_AGENT_ROLE_ID = BPEMConstants.TOMCAT_AGENT_ROLE_ID;
    public static final String QA_APP_TOMCAT_ROLE_ID = BPEMConstants.QA_APP_TOMCAT_ROLE_ID;    
    
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
       

        // MOM role
        EmRole momRole =
            new EmRole.Builder(MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .emCollector(collector1Role)
                .emCollector(collector2Role).nostartEM().nostartWV().build();    

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
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(momRole)
                .build();         
        
        // Configuration of Collector1 machine
        TestbedMachine collector1Machine =
            TestBedUtils
                .createWindowsMachine(COLLECTOR1_MACHINE_ID, EM_TEMPLATE_ID, collector1Role);

        // Configuration of Collector2 & agent machine
        TestbedMachine collector2Machine =
            TestBedUtils
                .createWindowsMachine(COLLECTOR2_MACHINE_ID, EM_TEMPLATE_ID, collector2Role, tomcatRole, qaAppTomcatRole, tomcatAgentRole);
       
        // Configuration of mom machine
        TestbedMachine momMachine =
            TestBedUtils.createWindowsMachine(MOM_MACHINE_ID, EM_TEMPLATE_ID, momRole); 
        
        
        momMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", momRole.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        collector1Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector1Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        collector2Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", collector2Role.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        collector2Machine.addRemoteResource(RemoteResource.createFromRegExp(".*", tomcatRole.getTomcatFlowContext().getTomcatInstallDir()+ "\\wily\\logs"));
        
        return Testbed.create(this, momMachine, collector1Machine, collector2Machine);

    }

}
