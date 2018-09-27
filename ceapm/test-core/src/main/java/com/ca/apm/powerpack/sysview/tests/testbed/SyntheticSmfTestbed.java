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

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO65;
import static com.ca.apm.powerpack.sysview.tests.testbed.MainframeTestbedMachine.TEMPLATE_CA31;

import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole.CeapmConfig;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.wily.powerpack.sysview.SMFRecordStats;
import com.wily.powerpack.sysview.SmfInputQueue;
import com.wily.powerpack.sysview.SmfSelectionBuffer;
import com.wily.powerpack.sysview.SmfSelectionTask;

/**
 * Testbed for running synthetic SMF tests against a single CE-APM agent.
 */
@TestBedDefinition(cleanUpTestBed = MainframeTestbedCleanup.class)
public class SyntheticSmfTestbed implements ITestbedFactory {
    public static final String TESTBED_ID = SyntheticSmfTestbed.class.getSimpleName();

    public static final String LINUX_MACHINE_ID = "linuxMachine";
    public static final String EM_ROLE_ID = "emRole";

    public static final String MF_MACHINE_ID = "mainframeMachine";

    public static final CeapmConfig CEAPM = CeapmConfig.NEW_14_1;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed(TESTBED_ID);

        // Distributed Linux
        TestbedMachine emMachine = new TestbedMachine.LinuxBuilder(LINUX_MACHINE_ID)
            .templateId(TEMPLATE_CO65)
            .build();
        testbed.addMachine(emMachine);

        // (Linux) EM
        EmRole emRole = new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver)
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
                .debugLog(SmfInputQueue.class, SmfSelectionBuffer.class, SmfSelectionTask.class,
                    SMFRecordStats.class)
            .configEm(emRole)
            .build();
        mfMachine.addRole(ceapmRole);

        return testbed;
    }
}
