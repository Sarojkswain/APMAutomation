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
 */

package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.WorkstationRole;
import com.ca.tas.role.web.QaAppJbossRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/*
 * Testbed class with following components installed : 
 * 	EM
 * 	Webview
 */
@TestBedDefinition

public class AccWindowsTestbed implements ITestbedFactory{

    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String COLLECTOR1_MACHINE_ID = "collector1Machine";
    public static final String COLLECTOR2_MACHINE_ID = "collector2Machine";
    public static final String COLLECTOR3_MACHINE_ID = "collector3Machine";

    public static final String MOM_ROLE_ID = "momRole";
    public static final String COLLECTOR1_ROLE_ID = "collector1Role";
    public static final String COLLECTOR2_ROLE_ID = "collector2Role";
    public static final String COLLECTOR3_ROLE_ID = "collector3Role";

    protected static String EM_TEMPLATE_ID = TEMPLATE_W64;

    public static final String AGENT_MACHINE_ID = "agentMachine";
    private static final String AGENT_MACHINE_TEMPLATE_ID = TEMPLATE_W64;    
    
    public static final String TOMCAT_ROLE_ID = "tomcatRole";
    public static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";

    public static final String JBOSS_ROLE_ID = "JBossRole";
    public static final String JBOSS_AGENT_ROLE_ID = "JbossAgentRole";
    public static final String QA_APP_JBOSS_ROLE_ID = "qaAppJBossRole";     


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

        // Configuration of Agent Machine
        TestbedMachine agentMachine =
            TestBedUtils.createWindowsMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID,
                tomcatRole, qaAppTomcatRole, tomcatAgentRole, jbossRole, qaAppJbossRole,
                jbossAgentRole);    
        
        return Testbed.create(this, momMachine, collector1Machine, collector2Machine, collector3Machine, agentMachine);

    }

}
