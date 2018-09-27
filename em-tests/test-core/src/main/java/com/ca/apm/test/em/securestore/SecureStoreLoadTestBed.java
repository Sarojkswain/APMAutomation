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

/**
 * 
 */
package com.ca.apm.test.em.securestore;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * @author svazd01
 *
 */
@TestBedDefinition
public class SecureStoreLoadTestBed implements ITestbedFactory {


    public static final String MOM_ROLE = "mom_role";

    public static final String COL1_ROLE = "col1_role";

    public static final String COL2_ROLE = "col2_role";

    public static final String COL3_ROLE = "col3_role";

    public static final String COL1_MACHINE = "col1Machine";

    public static final String COL2_MACHINE = "col2Machine";

    public static final String COL3_MACHINE = "col3Machine";

    public static final String MOM_MACHINE = "momMachine";

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public ITestbed create(ITasResolver tasResolver) {


        EmRole col1Role =
            new EmRole.LinuxBuilder(COL1_ROLE, tasResolver).emClusterRole(EmRoleEnum.COLLECTOR)
                .build();
        EmRole col2Role =
            new EmRole.LinuxBuilder(COL2_ROLE, tasResolver).emClusterRole(EmRoleEnum.COLLECTOR)
                .build();
        EmRole col3Role =
            new EmRole.LinuxBuilder(COL3_ROLE, tasResolver).emClusterRole(EmRoleEnum.COLLECTOR)
                .build();

        EmRole momRole =
            new EmRole.LinuxBuilder(MOM_ROLE, tasResolver).nostartWV()
                .emClusterRole(EmRoleEnum.MANAGER).emCollector(col1Role).emCollector(col2Role)
                .emCollector(col3Role).build();


        ITestbedMachine momMachine =
            new TestbedMachine.Builder(MOM_MACHINE).platform(Platform.LINUX).templateId("co65")
                .bitness(Bitness.b64).build();

        momMachine.addRole(momRole);

        ITestbedMachine col1Machine =
            new TestbedMachine.Builder(COL1_MACHINE).platform(Platform.LINUX).templateId("co65")
                .bitness(Bitness.b64).build();

        col1Machine.addRole(col1Role);

        ITestbedMachine col2Machine =
            new TestbedMachine.Builder(COL2_MACHINE).platform(Platform.LINUX).templateId("co65")
                .bitness(Bitness.b64).build();

        col2Machine.addRole(col2Role);

        ITestbedMachine col3Machine =
            new TestbedMachine.Builder(COL3_MACHINE).platform(Platform.LINUX).templateId("co65")
                .bitness(Bitness.b64).build();

        col3Machine.addRole(col3Role);



        return new Testbed("secureStore/loadTest").addMachine(momMachine, col1Machine, col2Machine,
            col3Machine);
    }

}
