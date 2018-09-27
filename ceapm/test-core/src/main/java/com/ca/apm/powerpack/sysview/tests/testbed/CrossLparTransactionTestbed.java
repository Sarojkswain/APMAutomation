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

import static com.ca.apm.powerpack.sysview.tests.testbed.MainframeTestbedMachine.TEMPLATE_CA11;
import static com.ca.apm.powerpack.sysview.tests.testbed.MainframeTestbedMachine.TEMPLATE_CA31;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole.CeapmConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole.CicsConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsTestDriverRole;
import com.ca.apm.powerpack.sysview.tests.role.CtgRole;
import com.ca.apm.powerpack.sysview.tests.role.CtgServerDefinition;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole.SysviewConfig;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Testbed for cross-LPAR transaction tests.
 */
@TestBedDefinition(cleanUpTestBed = MainframeTestbedCleanup.class)
public class CrossLparTransactionTestbed implements ITestbedFactory {
    public static final String TESTBED_ID = CrossLparTransactionTestbed.class.getSimpleName();

    public static final String DIST_WIN_ID = "winMachine";
    public static final String EM_ID = "emRole";
    public static final String CTG_ID = "ctgRole";
    public static final String CTD_ID = "ctdRole";
    public static final String SELENIUM_ID = "seleniumRole";

    public static final String MF_CA11_ID = "mainframeCA11";
    public static final String MF_CA31_ID = "mainframeCA31";

    public static final CeapmConfig CEAPM_11 = CeapmConfig.TEST0111;

    public static final CeapmConfig CEAPM_31 = CeapmConfig.TEST0031;

    public static final CicsConfig CICS_11 = CicsConfig.WILY_5_3_0_11;

    public static final CicsConfig CICS_31 = CicsConfig.WILY_5_3_0;

    public static final SysviewConfig SYSVIEW_11 = SysviewConfig.WILY_14_1_11;

    public static final SysviewConfig SYSVIEW_31 = SysviewConfig.WILY_14_1;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed(TESTBED_ID);

        // Distributed Win64
        TestbedMachine winMachine = new TestbedMachine.Builder(DIST_WIN_ID)
            .templateId(TEMPLATE_W64).build();
        testbed.addMachine(winMachine);

        // (win64) EM
        EmRole emRole = new EmRole.Builder(EM_ID, tasResolver)
            .installerProperty("shouldEnableSysview", "true")
            .build();
        winMachine.addRole(emRole);

        // (win64) CTG Server
        CtgRole ctgRole = new CtgRole.Builder(CTG_ID, tasResolver)
            .addServerDefinition(CtgServerDefinition.getEciDefinition(CICS_31.getEciId(),
                CICS_31.getHost(), CICS_31.getEciPort()))
            .addServerDefinition(CtgServerDefinition.getIpicDefinition(CICS_31.getIpicId(),
                CICS_31.getHost(), CICS_31.getIpicPort()))
            .build();
        winMachine.addRole(ctgRole);

        // (win64) CTD
        CicsTestDriverRole ctdRole = new CicsTestDriverRole.Builder(CTD_ID, tasResolver)
            .emHost(tasResolver.getHostnameById(emRole.getRoleId()))
            .emPort(emRole.getEmPort())
            .build();
        ctdRole.after(emRole, ctgRole);
        winMachine.addRole(ctdRole);

        // (win64) Selenium
        Artifact chromeDriverArtifact =
            new DefaultArtifact("com.ca.apm.binaries.selenium:chromedriver:zip:win32:2.14");
        IRole seleniumRole = new GenericRole.Builder(SELENIUM_ID, tasResolver)
            .unpack(chromeDriverArtifact, "c:/automation/deployed/driver")
            .build();
        winMachine.addRole(seleniumRole);

        // Mainframe CA11
        TestbedMachine mfCa11 = new MainframeTestbedMachine.Builder(MF_CA11_ID)
            .templateId(TEMPLATE_CA11)
            .build();
        testbed.addMachine(mfCa11);

        // (CA11) SYSVIEW
        IRole sysview11 = new SysviewRole.Builder(SYSVIEW_11)
            .addSmfPort(CEAPM_11.getSmfPort())
            .deployRole()
            .build();
        mfCa11.addRole(sysview11);

        // (CA11) CICS
        IRole cics11 = new CicsRole.Builder(CICS_11)
            .sysviewLoadlib(SYSVIEW_11.getLoadlib())
            .deployRole()
            .build();
        mfCa11.addRole(cics11);
        cics11.after(sysview11);

        // (CA11) CE-APM Agent
        IRole ceapmRole11 = new CeapmRole.Builder(CEAPM_11, tasResolver)
            .configEm(emRole)
            .build();
        mfCa11.addRole(ceapmRole11);
        ceapmRole11.after(cics11, emRole);

        // Mainframe CA31
        TestbedMachine mfCa31 = new MainframeTestbedMachine.Builder(MF_CA31_ID)
            .templateId(TEMPLATE_CA31)
            .build();
        testbed.addMachine(mfCa31);

        // (CA31) Sysview
        IRole sysview31 = new SysviewRole.Builder(SYSVIEW_31)
            .addSmfPort(CEAPM_31.getSmfPort())
            .build();
        mfCa31.addRole(sysview31);

        // (CA31) CICS
        IRole cics31 = new CicsRole.Builder(CICS_31)
            .sysviewLoadlib(SYSVIEW_31.getLoadlib())
            .build();
        mfCa31.addRole(cics31);
        cics31.after(sysview31);

        // (CA31) CE-APM Agent
        IRole ceapmRole31 = new CeapmRole.Builder(CEAPM_31, tasResolver)
            .configEm(emRole)
            .build();
        mfCa31.addRole(ceapmRole31);
        ceapmRole31.after(cics31, emRole);

        return testbed;
    }
}
