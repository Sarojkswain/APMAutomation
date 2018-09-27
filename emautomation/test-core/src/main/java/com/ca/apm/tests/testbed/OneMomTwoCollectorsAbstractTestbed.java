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
public abstract class OneMomTwoCollectorsAbstractTestbed implements ITestbedFactory {
    public static final String MOM_MACHINE_ID = "momMachine";

    public static final String COLLECTOR_1_MACHINE_ID = "collector1Machine";
    public static final String COLLECTOR_2_MACHINE_ID = "collector2Machine";

    public static final String MOM_ROLE_ID = "momRole";

    public static final String COLLECTOR_1_ROLE_ID = "collector1Role";
    public static final String COLLECTOR_2_ROLE_ID = "collector2Role";

    public static final String AGENT_DEFAULT_PROFILE = "IntroscopeAgent.profile";
    public static final String AGENT_DEFAULT_LOG = "IntroscopeAgent.log";

    public static final String KeyClWorkstationJarFileLocation = "ClWorkstationJarFileLocation";
    public static final String KeyEmInstallDir = "EmInstallDir";
    public static final String KeyTomcatInstallDir = "TomcatInstallDir";

    protected static String TESTBED_NAME;

    protected static Platform PLATFORM;

    protected static String NODE_TEMPLATE;

    protected static String EM_INSTALL_DIR;

    protected static String TOMCAT_INSTALL_DIR;
    private ITasResolver tasResolver;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        this.tasResolver = tasResolver;
        // ************************************
        // MOM Roles
        // ************************************

        // 1st Collector role
        final EmRole collectorRole1 = createCollectorRole(COLLECTOR_1_ROLE_ID);

        // 2nd Collector role
        final EmRole collectorRole2 = createCollectorRole(COLLECTOR_2_ROLE_ID);

        // MOM role
        final EmRole momRole = createMomRole(MOM_ROLE_ID, collectorRole1, collectorRole2);

        // ************************************
        // Configuration of 1st Collector machine
        // ************************************
        final TestbedMachine collectorMachine1 = createCollectorMachine(COLLECTOR_1_MACHINE_ID);
        {
            collectorMachine1.addRole(collectorRole1);

            // tomcat
            final TomcatRole tomcatRole1 = createTomcatRole("tomcat1");
            collectorMachine1.addRole(tomcatRole1);
            collectorMachine1.addProperty(KeyTomcatInstallDir, TOMCAT_INSTALL_DIR);

            // create Agent role
            final IRole agentRole1 = createTomcatAgentRole("agentRole1", momRole, tomcatRole1);
            collectorMachine1.addRole(agentRole1);
        }

        // ************************************
        // Configuration of 2nd Collector machine
        // ************************************
        final TestbedMachine collectorMachine2 = createCollectorMachine(COLLECTOR_2_MACHINE_ID);
        {
            collectorMachine2.addRole(collectorRole2);

            // tomcat
            final TomcatRole tomcatRole2 = createTomcatRole("tomcat2");
            collectorMachine2.addRole(tomcatRole2);
            collectorMachine2.addProperty(KeyTomcatInstallDir, TOMCAT_INSTALL_DIR);

            // create Agent role
            final IRole agentRole2 = createTomcatAgentRole("agentRole2", momRole, tomcatRole2);
            collectorMachine2.addRole(agentRole2);
        }

        // ************************************
        // Configuration of MOM machine
        // ************************************
        final TestbedMachine momMachine = createMomMachine(MOM_MACHINE_ID, momRole);


        // ************************************
        // map machines to testbed
        // ************************************
        final ITestbed ret = new Testbed(TESTBED_NAME);
        ret.addMachine(collectorMachine1);
        ret.addMachine(collectorMachine2);
        ret.addMachine(momMachine);

        return ret;
    }


    private EmRole createCollectorRole(String roleId) {
        // 1st Collector role
        final EmRole collectorRole =
            BuilderFactories.getEmBuilder(PLATFORM, roleId, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).nostartEM().nostartWV()
                .installDir(EM_INSTALL_DIR).build();

        return collectorRole;
    }

    private EmRole createMomRole(String roleId, EmRole collector1Role, EmRole collector2Role) {
        final EmRole momRole =
            BuilderFactories.getEmBuilder(PLATFORM, roleId, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).emCollector(collector1Role)
                .emCollector(collector2Role).nostartEM().nostartWV().installDir(EM_INSTALL_DIR)
                .build();
        return momRole;
    }

    private TomcatRole createTomcatRole(String roleId) {
        final TomcatRole tomcatRole =
            BuilderFactories.getTomcatBuilder(PLATFORM, roleId, tasResolver)
                .installDir(TOMCAT_INSTALL_DIR).build();
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

    private TestbedMachine createMomMachine(String machineId, IRole momRole) {
        final TestbedMachine momMachine =
            BuilderFactories.getTestbedMachineBuilder(PLATFORM, machineId)
                .templateId(NODE_TEMPLATE).build();
        momMachine.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
        momMachine.addProperty(KeyClWorkstationJarFileLocation, EM_INSTALL_DIR
            + "/lib/CLWorkstation.jar");
        momMachine.addRole(momRole);
        return momMachine;
    }

}
