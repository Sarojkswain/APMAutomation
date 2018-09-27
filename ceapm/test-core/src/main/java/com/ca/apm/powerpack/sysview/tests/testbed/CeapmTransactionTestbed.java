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
 */

package com.ca.apm.powerpack.sysview.tests.testbed;

import static com.ca.apm.powerpack.sysview.tests.testbed.MainframeTestbedMachine.TEMPLATE_CA31;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH66;

import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole.CeapmConfig;
import com.ca.apm.powerpack.sysview.tests.test.CeapmTransactionSelectionTest;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Testbed with CEAPM agent and EM without further dependencies. Used by e.g.
 * {@link CeapmTransactionSelectionTest}.
 */
@TestBedDefinition(cleanUpTestBed = MainframeTestbedCleanup.class)
public class CeapmTransactionTestbed implements ITestbedFactory {
    public static final String TESTBED_ID = CeapmTransactionTestbed.class.getSimpleName();

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_ROLE_ID = "emRole";

    public static final String MF_MACHINE_ID = "mainframeMachine";

    public static final CeapmConfig CEAPM = CeapmConfig.NEW_14_1;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed(TESTBED_ID);

        // Distributed Linux
        TestbedMachine emMachine =
            new TestbedMachine.LinuxBuilder(EM_MACHINE_ID).templateId(TEMPLATE_RH66).build();
        testbed.addMachine(emMachine);

        // (Linux) EM
        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver)
                .nostartWV()
                .build();
        emMachine.addRole(emRole);

        // Mainframe CA31
        TestbedMachine mfMachine = new MainframeTestbedMachine.Builder(MF_MACHINE_ID)
            .templateId(TEMPLATE_CA31)
            .build();
        testbed.addMachine(mfMachine);

        // (CA31) CE-APM Agent
        IRole ceapmRole = new CeapmRole.Builder(CEAPM, tasResolver)
            .configEm(emRole)
            .debugLog(com.wily.powerpack.sysview.SmfSelectionTask.class)
            .build();
        mfMachine.addRole(ceapmRole);

        return testbed;
    }
}
