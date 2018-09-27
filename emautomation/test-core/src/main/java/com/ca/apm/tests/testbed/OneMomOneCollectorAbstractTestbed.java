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


@TestBedDefinition
public abstract class OneMomOneCollectorAbstractTestbed implements ITestbedFactory {

    public static final String MOM_MACHINE_ID = "momMachine";

    public static final String COLLECTOR_MACHINE_ID = "collectorMachine";

    public static final String MOM_ROLE_ID = "momRole";

    public static final String COLLECTOR_ROLE_ID = "collectorRole";


    // To overwrite

    protected static Platform PLATFORM;

    protected static String TESTBED_NAME = "OneMomOneCollectorAbstractTestbed";

    protected static String NODE_TEMPLATE = "nodeTemplate";

    protected static String EM_INSTALL_DIR = "installDir";

    protected static String TOMCAT_INSTALL_DIR = "tomcatDir";

    // KEYS

    public static final String KeyEmInstallDir = EmRole.ENV_PROPERTY_INSTALL_DIR;

    public static final String KeyClWorkstationJarFileLocation = "ClWorkstationJarFileLocation";

    public static final String KeyTomcatInstallDir = "TomcatInstallDir";



    public ITestbed create(ITasResolver tasResolver) {
        // ************************************
        // Configuration of EM roles
        // ************************************

        // Collector role

        final EmRole collectorRole =
            BuilderFactories.getEmBuilder(PLATFORM, COLLECTOR_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).nostartEM().nostartWV()
                .installDir(EM_INSTALL_DIR).build();

        collectorRole.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);

        // MOM role

        final EmRole momRole =
            BuilderFactories.getEmBuilder(PLATFORM, MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).emCollector(collectorRole)
                .nostartEM().nostartWV().installDir(EM_INSTALL_DIR).build();


        momRole.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);



        // ************************************
        // Configuration of Collector machine
        // ************************************
        final TestbedMachine collectorMachine =
            new TestbedMachine.Builder(COLLECTOR_MACHINE_ID).templateId(NODE_TEMPLATE).build();
        collectorMachine.addProperty(KeyClWorkstationJarFileLocation, EM_INSTALL_DIR
            + File.separator + "lib" + File.separator + "CLWorkstation.jar");
        {
            collectorMachine.addRole(collectorRole);
            collectorMachine.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
            collectorMachine.addProperty(KeyTomcatInstallDir, TOMCAT_INSTALL_DIR);


            final TomcatRole tomcatRole =
                BuilderFactories.getTomcatBuilder(PLATFORM, "tomcatRole", tasResolver)
                    .installDir(TOMCAT_INSTALL_DIR).build();

            collectorMachine.addRole(tomcatRole);

            // create Agent role
            final IRole agentRole =
                BuilderFactories.getAgentBuilder(PLATFORM, "agentRole", tasResolver)
                    .webAppRole(tomcatRole).emRole(momRole).disableWebAppAutoStart().build();

            collectorMachine.addRole(agentRole);
        }

        // ************************************
        // Configuration of MOM machine
        // ************************************

        final TestbedMachine momMachine =
            BuilderFactories.getTestbedMachineBuilder(PLATFORM, MOM_MACHINE_ID)
                .templateId(NODE_TEMPLATE).build();
        momMachine.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
        momMachine.addProperty(KeyClWorkstationJarFileLocation, EM_INSTALL_DIR + File.separator
            + "lib" + File.separator + "CLWorkstation.jar");

        momMachine.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
        momMachine.addRole(momRole);

        // ************************************
        // map machines to testbed
        // ************************************
        final ITestbed ret = new Testbed(TESTBED_NAME);
        ret.addMachine(collectorMachine);
        ret.addMachine(momMachine);

        return ret;
    }
}
