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
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole.CeapmConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole.CicsConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsTestDriverRole;
import com.ca.apm.powerpack.sysview.tests.role.CtgRole;
import com.ca.apm.powerpack.sysview.tests.role.CtgServerDefinition;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole.SysviewConfig;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/** Testbed contains: CicsTestDriver, CTG, EM and single CEAPM agent. */
@TestBedDefinition(cleanUpTestBed = MainframeTestbedCleanup.class)
public class CeapmCtgTransactionTestbed implements ITestbedFactory {
    public static final String TESTBED_ID = CeapmCtgTransactionTestbed.class.getSimpleName();

    public static final String MF_MACHINE_ID = "mainframeMachine";
    public static final String EM_MACHINE_ID = "winMachine";
    public static final String CTG_ROLE_ID = "ctgRole";
    public static final String CTD_ROLE_ID = "ctdRole";
    public static final String EM_ROLE_ID = "emRole";
    public static final SysviewConfig SYSVIEW = SysviewConfig.WILY_14_1;
    public static final CicsConfig CICS = CicsConfig.WILY_5_3_0;
    public static final CeapmConfig CEAPM = CeapmConfig.NEW_14_1;

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed(TESTBED_ID);

        // Distributed Win64
        TestbedMachine winMachine = new TestbedMachine.Builder(EM_MACHINE_ID)
            .templateId(TEMPLATE_W64)
            .build();
        testbed.addMachine(winMachine);

        // (Win64) EM with enabled CE-APM powerpack
        EmRole emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver)
                .installerProperty("shouldEnableSysview", "true")
                .build();
        winMachine.addRole(emRole);

        // (Win64) CTG Server
        CtgRole ctgRole =
            new CtgRole.Builder(CTG_ROLE_ID, tasResolver)
                .addServerDefinition(
                    CtgServerDefinition.getEciDefinition(CICS.getEciId(), CICS.getHost(),
                        CICS.getEciPort()))
                .addServerDefinition(
                    CtgServerDefinition.getIpicDefinition(CICS.getIpicId(), CICS.getHost(),
                        CICS.getIpicPort()))
                .build();
        winMachine.addRole(ctgRole);

        // (Win64) CICSTestDriver
        CicsTestDriverRole ctdRole =
            new CicsTestDriverRole.Builder(CTD_ROLE_ID, tasResolver)
                .emHost(tasResolver.getHostnameById(emRole.getRoleId()))
                .emPort(emRole.getEmPort())
                .build();
        ctdRole.after(emRole, ctgRole);
        winMachine.addRole(ctdRole);

        // Mainframe CA31
        TestbedMachine mfMachine = new MainframeTestbedMachine.Builder(MF_MACHINE_ID)
            .templateId(TEMPLATE_CA31)
            .build();
        testbed.addMachine(mfMachine);

        // (CA31) SYSVIEW
        IRole sysviewRole = new SysviewRole.Builder(SYSVIEW)
            .addSmfPort(CEAPM.getSmfPort())
            .build();
        mfMachine.addRole(sysviewRole);

        // (CA31) CE-APM Agent
        IRole ceapmRole = new CeapmRole.Builder(CEAPM, tasResolver)
            .configSysviewXapi(SYSVIEW)
            .configDb2(CEAPM)
            .configEm(emRole).build();
        mfMachine.addRole(ceapmRole);

        return testbed;
    }
}
