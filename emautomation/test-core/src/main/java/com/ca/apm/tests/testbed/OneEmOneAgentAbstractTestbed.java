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
public abstract class OneEmOneAgentAbstractTestbed implements ITestbedFactory {

    public static final String MACHINE_ID = "testingMachine";

    public static final String TOMCAT_ROLE_ID = "tomcatRole";

    public static final String EM_ROLE_ID = "emRole";

    public static final String AGENT_ROLE_ID = "agentRole";

    public static final String TomcatInstallDir = "tomcatInstallationPath";

    public static final String KeyEmInstallDir = "EmInstallDir";

    public static final String KeyClWorkstationJarFileLocation = "ClWorkstationJarFileLocation";

    public static final String AGENT_DEFAULT_PROFILE = "IntroscopeAgent.profile";

    public static final int EM_PORT = 5001;

    public static String TESTBED_NAME = "OneEmOneAgentAbstractTestbed";
    public static Platform PLATFORM;
    protected static String NODE_TEMPLATE;
    public static String EM_INSTALL_DIR;
    public static String TOMCAT_INSTALL_DIR;
    public static String MACHINE_TEMPLATE_ID;



    public ITestbed create(ITasResolver tasResolver) {


        TomcatRole webAppRole =
            BuilderFactories.getTomcatBuilder(PLATFORM, TOMCAT_ROLE_ID, tasResolver)
                .installDir(TOMCAT_INSTALL_DIR).build();

        EmRole emRole =
            BuilderFactories.getEmBuilder(PLATFORM, EM_ROLE_ID, tasResolver).nostartWV()
                .installDir(EM_INSTALL_DIR).emPort(EM_PORT).build();

        IRole agentRole =
            BuilderFactories.getAgentBuilder(PLATFORM, AGENT_ROLE_ID, tasResolver)
                .webAppRole(webAppRole).emRole(emRole).disableWebAppAutoStart().build();

        TestbedMachine testingMachine =
            BuilderFactories.getTestbedMachineBuilder(PLATFORM, MACHINE_ID)
                .templateId(NODE_TEMPLATE).build();

        testingMachine.addRole(emRole);
        testingMachine.addRole(webAppRole);
        testingMachine.addRole(agentRole);

        testingMachine.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);
        testingMachine.addProperty(TomcatInstallDir, TOMCAT_INSTALL_DIR);
        testingMachine.addProperty(KeyClWorkstationJarFileLocation, EM_INSTALL_DIR + File.separator
            + "lib" + File.separator + "CLWorkstation.jar");

        ITestbed testbed = new Testbed(TESTBED_NAME);
        testbed.addMachine(testingMachine);

        return testbed;

    }

}
