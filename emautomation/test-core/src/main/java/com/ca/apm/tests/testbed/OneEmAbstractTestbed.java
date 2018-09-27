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

import java.io.File;

import com.ca.apm.tests.utils.BuilderFactories;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * One EM on a single server, no clusters
 */
@TestBedDefinition
public abstract class OneEmAbstractTestbed implements ITestbedFactory {

    public static final String EM_MACHINE_ID = "emMachine";

    public static final String EM_ROLE_ID = "emRole";

    public static final int EM_PORT = 5001;

    public static final String KeyEmInstallDir = "emInstallDir";

    public static String TESTBED_NAME;

    public static Platform PLATFORM;

    protected static String NODE_TEMPLATE;

    public static String EM_INSTALL_DIR;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // create EM role
        EmRole emRole =
            BuilderFactories.getEmBuilder(PLATFORM, EM_ROLE_ID, tasResolver).nostartWV()
                .installDir(EM_INSTALL_DIR).emPort(EM_PORT).build();

        // map EM role to machine
        TestbedMachine emMachine =
            BuilderFactories.getTestbedMachineBuilder(PLATFORM, EM_MACHINE_ID)
                .templateId(NODE_TEMPLATE).build();
        emMachine.addRole(emRole);
        emMachine.addProperty("clWorkstationJarFileLocation", EM_INSTALL_DIR + File.pathSeparator
            + "lib" + File.pathSeparator + "CLWorkstation.jar");
        emMachine.addProperty(KeyEmInstallDir, EM_INSTALL_DIR);

        // map machines to test-bed
        ITestbed testbed = new Testbed(TESTBED_NAME);
        testbed.addMachine(emMachine);

        return testbed;
    }
}
