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

import java.io.File;


/**
 * Abstract class defining testbed with One Mom machine,
 * One Collector machine with tomcat + agent and two machines with tomcat + agent.
 * 
 * 
 * @author sobar03
 *
 */
@TestBedDefinition
public abstract class OneMomOneCollectorThreeAgentsAbstractTestbed implements ITestbedFactory {
    public static final String MOM_MACHINE_ID = "momMachine";

    public static final String COLLECTOR_1_MACHINE_ID = "collector1Machine";
    public static final String AGENT_2_MACHINE_ID = "agent2Machine";
    public static final String AGENT_3_MACHINE_ID = "agent3Machine";

    public static final String MOM_ROLE_ID = "momRole";

    public static final String COLLECTOR_1_ROLE_ID = "collector1Role";

    public static final String KeyClWorkstationJarFileLocation = "ClWorkstationJarFileLocation";
    public static final String KeyEmInstallDir = "EmInstallDir";
    public static final String KeyTomcatInstallDir = "TomcatInstallDir";

    protected static String TESTBED_NAME = "OneMomOneCollectorThreeAgentsAbstractTestbed";
    protected static String NODE_TEMPLATE = "nodeTemplate";
    protected static String EM_INSTALL_DIR = "emInstallDir";
    protected static String TOMCAT_INSTALL_DIR = "tomcatInstallDir";
    protected static Platform PLATFORM;


    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // ************************************
        // Configuration of EM role
        // ************************************
        // Collector 1
        final EmRole collectorRole1 =
            BuilderFactories.getEmBuilder(PLATFORM, COLLECTOR_1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).nostartEM().nostartWV()
                .installDir(EM_INSTALL_DIR).build();


        // MOM
        final EmRole momRole =
            BuilderFactories.getEmBuilder(PLATFORM, MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).emCollector(collectorRole1)
                .nostartEM().nostartWV().installDir(EM_INSTALL_DIR).build();

        final String CLworkstationRelativePath =
            File.separator + "lib" + File.separator + "CLWorkstation.jar";


        // ************************************
        // Configuration of Collector machine
        // ************************************
        final TestbedMachine collectorMachine1 =
            new TestbedMachine.Builder(COLLECTOR_1_MACHINE_ID).templateId(NODE_TEMPLATE).build();
        collectorMachine1.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
        collectorMachine1.addProperty(KeyClWorkstationJarFileLocation, EM_INSTALL_DIR
            + CLworkstationRelativePath);
        {
            collectorMachine1.addRole(collectorRole1);

            final TomcatRole tomcatRole1 =
                BuilderFactories.getTomcatBuilder(PLATFORM, "tomcatRole1", tasResolver)
                    .installDir(TOMCAT_INSTALL_DIR).build();

            collectorMachine1.addRole(tomcatRole1);
            collectorMachine1.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
            collectorMachine1.addProperty(KeyTomcatInstallDir, TOMCAT_INSTALL_DIR);

            // create Agent role
            final IRole agentRole1 =
                BuilderFactories.getAgentBuilder(PLATFORM, "agentRole1", tasResolver)
                    .webAppRole(tomcatRole1).emRole(momRole).disableWebAppAutoStart().build();

            collectorMachine1.addRole(agentRole1);
        }

        // ************************************
        // Configuration of 1st Agent machine
        // ************************************
        final TestbedMachine agentMachine2 =
            new TestbedMachine.Builder(AGENT_2_MACHINE_ID).templateId(NODE_TEMPLATE).build();
        {

            final TomcatRole tomcatRole2 =
                BuilderFactories.getTomcatBuilder(PLATFORM, "tomcatRole2", tasResolver)
                    .installDir(TOMCAT_INSTALL_DIR).build();

            agentMachine2.addRole(tomcatRole2);

            // create Agent role
            final IRole agentRole2 =
                BuilderFactories.getAgentBuilder(PLATFORM, "agentRole2", tasResolver)
                    .webAppRole(tomcatRole2).emRole(momRole).disableWebAppAutoStart().build();

            agentMachine2.addRole(agentRole2);
        }

        // ************************************
        // Configuration of 2nd Agent machine
        // ************************************
        final TestbedMachine agentMachine3 =
            new TestbedMachine.Builder(AGENT_3_MACHINE_ID).templateId(NODE_TEMPLATE).build();
        {

            final TomcatRole tomcatRole3 =
                BuilderFactories.getTomcatBuilder(PLATFORM, "tomcatRole3", tasResolver)
                    .installDir(TOMCAT_INSTALL_DIR).build();

            agentMachine3.addRole(tomcatRole3);

            // create Agent role
            final IRole agentRole3 =
                BuilderFactories.getAgentBuilder(PLATFORM, "agentRole3", tasResolver)
                    .webAppRole(tomcatRole3).emRole(momRole).disableWebAppAutoStart().build();

            agentMachine3.addRole(agentRole3);
        }

        // ************************************
        // Configuration of MOM machine
        // ************************************
        final TestbedMachine momMachine =
            new TestbedMachine.Builder(MOM_MACHINE_ID).templateId(NODE_TEMPLATE).build();
        momMachine.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
        momMachine.addProperty(KeyClWorkstationJarFileLocation, EM_INSTALL_DIR
            + CLworkstationRelativePath);
        momMachine.addRole(momRole);

        // ************************************
        // map machines to testbed
        // ************************************
        final ITestbed ret = new Testbed(TESTBED_NAME);
        ret.addMachine(collectorMachine1);
        ret.addMachine(agentMachine2);
        ret.addMachine(agentMachine3);
        ret.addMachine(momMachine);

        return ret;
    }

}
