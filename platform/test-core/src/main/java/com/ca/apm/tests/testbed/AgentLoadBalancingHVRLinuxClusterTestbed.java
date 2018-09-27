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
 * Author : KETSW01/ KETHIREDDY SWETHA
 * Date : 20/11/2015
 */

package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.role.CommonHvrAgentRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * SampleTestbed class.
 *
 * Testbed description.
 */
@TestBedDefinition
public class AgentLoadBalancingHVRLinuxClusterTestbed implements ITestbedFactory {

    private static final String EM_MACHINE_TEMPLATE_ID = TEMPLATE_CO66;

    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String COLLECTOR1_MACHINE_ID = "collector1Machine";
    public static final String COLLECTOR2_MACHINE_ID = "collector2Machine";
    public static final String COLLECTOR3_MACHINE_ID = "collector3Machine";

    public static final String MOM_ROLE_ID = "momRole";
    public static final String COLLECTOR1_ROLE_ID = "collector1Role";
    public static final String COLLECTOR2_ROLE_ID = "collector2Role";
    public static final String COLLECTOR3_ROLE_ID = "collector3Role";


    public static final String TOMCAT_ROLE_ID = "tomcatRole";
    public static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";

    public static final String JBOSS_ROLE_ID = "JBossRole";
    public static final String JBOSS_AGENT_ROLE_ID = "JbossAgentRole";
    public static final String QA_APP_JBOSS_ROLE_ID = "qaAppJBossRole";


    public static final String HVRAGENT_ROLE = "hvragentRole";
    public static final String HVRAGENT_MACHINE_ID = "hvragentMachine";
    public static final String HVRAGENT_MACHINE_TEMPLATE_ID = TEMPLATE_W64;

    public static final String HVRAGENT_INSTALL_DIRECTORY = "C:\\sw\\hvragent\\";
    public static final String HVRAGENT_STAGE_DIRECTORY = "C:\\sw\\stage";


    @Override
    public ITestbed create(ITasResolver tasResolver) {

        List<String> collectorChosenFeatures = Arrays.asList("Enterprise Manager", "WebView");


        // Collector1 role
        EmRole collector1Role =
            new EmRole.LinuxBuilder(COLLECTOR1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .installDir("/opt/automation/deployed/collector1").emPort(5002).emWebPort(8082)
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID)).nostartEM().nostartWV().build();



        EmRole collector2Role =
            new EmRole.LinuxBuilder(COLLECTOR2_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).emPort(5001)
                .emWebPort(8081).installDir("/opt/automation/deployed/collector2")
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID)).nostartEM().nostartWV().build();

        EmRole collector3Role =
            new EmRole.LinuxBuilder(COLLECTOR3_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).emPort(5003)
                .emWebPort(8083).installDir("/opt/automation/deployed/collector3")
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID)).nostartEM().nostartWV().build();

        // MOM role
        EmRole momRole =
            new EmRole.LinuxBuilder(MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).emCollector(collector1Role)
                .emCollector(collector2Role).emCollector(collector3Role).nostartEM().nostartWV()
                .build();

        CommonHvrAgentRole hvrAgentRole = CommonHvrAgentRole(tasResolver, HVRAGENT_ROLE);

        // map roles to machines
        ITestbedMachine momMachine =
            TestBedUtils.createLinuxMachine(MOM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID);

        ITestbedMachine collector1Machine =
            TestBedUtils.createLinuxMachine(COLLECTOR1_MACHINE_ID, EM_MACHINE_TEMPLATE_ID);
        ITestbedMachine collector2Machine =
            TestBedUtils.createLinuxMachine(COLLECTOR2_MACHINE_ID, EM_MACHINE_TEMPLATE_ID);
        ITestbedMachine collector3Machine =
            TestBedUtils.createLinuxMachine(COLLECTOR3_MACHINE_ID, EM_MACHINE_TEMPLATE_ID);

        momMachine.addRole(momRole);
        collector1Machine.addRole(collector1Role);
        collector2Machine.addRole(collector2Role);
        collector3Machine.addRole(collector3Role);

        ITestbedMachine hvrMachine =
            TestBedUtils.createWindowsMachine(HVRAGENT_MACHINE_ID, HVRAGENT_MACHINE_TEMPLATE_ID,
                new DeployFreeRole("HVRAGENT"), hvrAgentRole);


        return new Testbed(getClass().getSimpleName()).addMachine(momMachine, collector1Machine,
            collector2Machine, collector3Machine, hvrMachine);
    }

    private CommonHvrAgentRole CommonHvrAgentRole(ITasResolver tasResolver, String Role) {
        return new CommonHvrAgentRole.Builder(HVRAGENT_ROLE, tasResolver)
            .installDir(HVRAGENT_INSTALL_DIRECTORY).stagingDir(HVRAGENT_STAGE_DIRECTORY).build();
    }
}