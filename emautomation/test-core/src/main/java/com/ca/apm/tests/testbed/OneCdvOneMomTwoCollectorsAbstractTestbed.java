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
 */

package com.ca.apm.tests.testbed;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.tests.utils.BuilderFactories;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * This abstract class exists only to define common constants for Windows and
 * Linux implementations
 */
@TestBedDefinition
public abstract class OneCdvOneMomTwoCollectorsAbstractTestbed implements ITestbedFactory {
    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String CDV_MACHINE_ID = "cdvMachine";

    public static final String COLLECTOR_1_MACHINE_ID = "collector1Machine";
    public static final String COLLECTOR_2_MACHINE_ID = "collector2Machine";

    public static final String MOM_ROLE_ID = "momRole";
    public static final String CDV_ROLE_ID = "cdvRole";

    public static final String COLLECTOR_1_ROLE_ID = "collector1Role";
    public static final String COLLECTOR_2_ROLE_ID = "collector2Role";

    public static final String KeyClWorkstationJarFileLocation = "ClWorkstationJarFileLocation";
    public static final String KeyEmInstallDir = "EmInstallDir";
    public static final String KeyTomcatInstallDir = "TomcatInstallDir";
    public static final String KeyWvEmPort = "wvEmPort";
    public static final String KeyAgentHost =
        "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT";

    private ITasResolver tasResolver;

    public static String TESTBED_NAME = "OneCdvTwoMomsFourCollectorsAbstractTestbed";
    public static Platform PLATFORM;
    protected static String NODE_TEMPLATE;
    public static String EM_INSTALL_DIR;
    public static String TOMCAT_INSTALL_DIR;

    public ITestbed create(ITasResolver tasResolver) {
        this.tasResolver = tasResolver;

        // ************************************
        // Cluster A
        // ************************************
        final EmRole collectorRole1 = createCollectorRole(COLLECTOR_1_ROLE_ID);
        final EmRole collectorRole2 = createCollectorRole(COLLECTOR_2_ROLE_ID);
        final EmRole mom1Role = createMomRole(MOM_ROLE_ID, collectorRole1, collectorRole2);



        // ************************************
        // CDV
        // ************************************
        final EmRole cdvRole =
            BuilderFactories.getEmBuilder(PLATFORM, CDV_MACHINE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.CDV).emCollector(collectorRole1)
                .emCollector(collectorRole2).nostartEM().nostartWV().installDir(EM_INSTALL_DIR)
                .build();

        // ************************************
        // Configuration of 1st Collector machine
        // ************************************
        final TestbedMachine collectorMachine1 = createCollectorMachine(COLLECTOR_1_MACHINE_ID);
        {
            collectorMachine1.addRole(collectorRole1);
            final TomcatRole tomcatRole1 = createTomcatRole("tomcatRole1", collectorMachine1);
            final IRole agentRole1 = createTomcatAgentRole("agentRole1", mom1Role, tomcatRole1);
            collectorMachine1.addRole(agentRole1);
        }
        // ************************************
        // Configuration of 2nd Collector machine
        // ************************************
        final TestbedMachine collectorMachine2 = createCollectorMachine(COLLECTOR_2_MACHINE_ID);
        {
            collectorMachine2.addRole(collectorRole2);
            final TomcatRole tomcatRole2 = createTomcatRole("tomcatRole2", collectorMachine2);
            final IRole agentRole2 = createTomcatAgentRole("agentRole2", mom1Role, tomcatRole2);
            collectorMachine2.addRole(agentRole2);
        }



        final TestbedMachine mom1Machine = createMomMachine(MOM_MACHINE_ID);
        mom1Machine.addRole(mom1Role);

        // ************************************
        // Configuration of CDV machine
        // ************************************
        final TestbedMachine cdvMachine =
            BuilderFactories.getTestbedMachineBuilder(PLATFORM, CDV_MACHINE_ID)
                .templateId(NODE_TEMPLATE).build();
        cdvMachine.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
        cdvMachine.addProperty(KeyClWorkstationJarFileLocation, EM_INSTALL_DIR
            + "/lib/CLWorkstation.jar");
        cdvMachine.addRole(cdvRole);

        // ************************************
        // map machines to testbed
        // ************************************
        final ITestbed ret = new Testbed(TESTBED_NAME);
        ret.addMachine(collectorMachine1);
        ret.addMachine(collectorMachine2);

        ret.addMachine(mom1Machine);
        ret.addMachine(cdvMachine);

        return ret;
    }


    private EmRole createCollectorRole(String roleId) {
        // 1st Collector role
        return BuilderFactories.getEmBuilder(PLATFORM, roleId, tasResolver)
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).nostartEM().nostartWV()
            .installDir(EM_INSTALL_DIR).build();
    }

    private EmRole createMomRole(String roleId, EmRole collectorRole1, EmRole collectorRole2) {
        return BuilderFactories.getEmBuilder(PLATFORM, roleId, tasResolver)
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).emCollector(collectorRole1)
            .emCollector(collectorRole2).nostartEM().nostartWV().installDir(EM_INSTALL_DIR).build();
    }

    private TomcatRole createTomcatRole(String roleId, TestbedMachine collectorMachine) {
        final TomcatRole tomcatRole =
            BuilderFactories.getTomcatBuilder(PLATFORM, roleId, tasResolver)
                .installDir(TOMCAT_INSTALL_DIR).build();
        collectorMachine.addRole(tomcatRole);
        collectorMachine.addProperty(KeyTomcatInstallDir, TOMCAT_INSTALL_DIR);
        return tomcatRole;
    }

    private IRole createTomcatAgentRole(String roleId, EmRole momRole, TomcatRole tomcatRole) {
        return BuilderFactories.getAgentBuilder(PLATFORM, roleId, tasResolver)
            .webAppRole(tomcatRole).emRole(momRole).disableWebAppAutoStart().build();
    }


    private TestbedMachine createCollectorMachine(String machineId) {
        final TestbedMachine collectorMachine =
            BuilderFactories.getTestbedMachineBuilder(PLATFORM, machineId)
                .templateId(NODE_TEMPLATE).build();
        collectorMachine.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
        collectorMachine.addProperty(KeyClWorkstationJarFileLocation, EM_INSTALL_DIR
            + "/lib/CLWorkstation.jar");
        return collectorMachine;
    }

    private TestbedMachine createMomMachine(String machineId) {
        final TestbedMachine momMachine =
            BuilderFactories.getTestbedMachineBuilder(PLATFORM, machineId)
                .templateId(NODE_TEMPLATE).build();
        momMachine.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
        momMachine.addProperty(KeyClWorkstationJarFileLocation, EM_INSTALL_DIR
            + "/lib/CLWorkstation.jar");
        return momMachine;
    }

}
