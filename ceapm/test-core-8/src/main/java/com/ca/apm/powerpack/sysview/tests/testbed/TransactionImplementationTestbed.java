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
import com.ca.apm.powerpack.sysview.tests.role.CicsRole;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole.CicsConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsTestDriverRole;
import com.ca.apm.powerpack.sysview.tests.role.CtgRole;
import com.ca.apm.powerpack.sysview.tests.role.CtgServerDefinition;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole.MqZosConfig;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole.SysviewConfig;
import com.ca.apm.powerpack.sysview.tests.role.Was8Role;
import com.ca.apm.powerpack.sysview.tests.role.WasAgentRole;
import com.ca.apm.powerpack.sysview.tests.role.WasAppRole;
import com.ca.apm.powerpack.sysview.tests.test.TransactionImplementationTest;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import org.eclipse.aether.artifact.DefaultArtifact;

/** Test bed for {@link TransactionImplementationTest}. */
@TestBedDefinition(cleanUpTestBed = MainframeTestbedCleanup.class)
public class TransactionImplementationTestbed implements ITestbedFactory {
    private static final String TESTBED_ID = TransactionImplementationTestbed.class.getSimpleName();

    public static final String MAINFRAME_ID = "mainframe";
    public static final String WINDOWS_ID = "windows";
    public static final String WAS_ID = "wasRole";
    public static final String MQ_APP_ID = "mqAppRole";
    public static final String WS_APP_ID = "wsAppRole";
    public static final String CTG_ID = "ctgRole";
    public static final String CTD_ID = "ctdRole";
    public static final String EM_ID = "emRole";
    public static final CeapmConfig CEAPM = CeapmConfig.NEW_14_1;
    public static final SysviewConfig SYSVIEW = SysviewConfig.WILY_14_1;
    public static final MqZosConfig MQ = MqZosConfig.CSQ4;
    public static final CicsConfig CICS = CicsConfig.WILY_5_3_0;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed(TESTBED_ID);

        // Distributed Win64
        TestbedMachine winMachine = new TestbedMachine.Builder(WINDOWS_ID)
            .templateId(TEMPLATE_W64)
            .build();
        testbed.addMachine(winMachine);

        // (Win64) EM with enabled CE-APM powerpack
        EmRole emRole = new EmRole.Builder(EM_ID, tasResolver)
            .installerProperty("shouldEnableSysview", "true")
            .nostartWV()
            .build();
        winMachine.addRole(emRole);

        // (Win64) Websphere 8
        Was8Role wasRole = new Was8Role.Builder(WAS_ID, tasResolver).build();
        winMachine.addRole(wasRole);

        // (Win64) Agent instrumenting Websphere
        WasAgentRole wasAgentRole = new WasAgentRole.Builder("agentRole1", tasResolver)
            .wasRole(wasRole)
            .emHost(tasResolver.getHostnameById(emRole.getRoleId()))
            .emPort(emRole.getEmPort())
            .build();
        wasAgentRole.after(wasRole);
        winMachine.addRole(wasAgentRole);

        // (Win64) Application that allows CICS calls through web services
        IRole wsAppRole = new WasAppRole.Builder(WS_APP_ID, tasResolver)
            .webAppArtifact(
                new DefaultArtifact("com.ca.apm.binaries.ibm", "cics-example-app", "ear", "6"))
            .parentRole(WAS_ID).webAppName("ExampleAppClientV6Web")
            .webAppContextRoot("ExampleAppClientV6Web")
            .build();
        wsAppRole.after(wasRole);
        winMachine.addRole(wsAppRole);

        // (Win64) Application that allows CICS/IMS calls through MQ
        IRole mqAppRole =
            new WasAppRole.Builder(MQ_APP_ID, tasResolver)
                .webAppArtifact(
                    new DefaultArtifact("com.ca.apm.powerpack.sysview.tests",
                        "ceapm.tools.CPTJCAApp", "war", null)).webAppName("CPTJCAApp")
                .parentRole(WAS_ID).webAppContextRoot("CPTJCAApp")
                .build();
        mqAppRole.after(wasRole);
        winMachine.addRole(mqAppRole);

        // (Win64) CTG Server
        CtgRole ctgRole =
            new CtgRole.Builder(CTG_ID, tasResolver)
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
            new CicsTestDriverRole.Builder(CTD_ID, tasResolver)
                .emHost(tasResolver.getHostnameById(emRole.getRoleId()))
                .emPort(emRole.getEmPort())
                .build();
        ctdRole.after(emRole, ctgRole);
        winMachine.addRole(ctdRole);

        // Mainframe CA31
        TestbedMachine mfMachine = new MainframeTestbedMachine.Builder(MAINFRAME_ID)
            .templateId(TEMPLATE_CA31)
            .build();
        testbed.addMachine(mfMachine);

        // (CA31) SYSVIEW
        IRole sysviewRole = new SysviewRole.Builder(SYSVIEW)
            .addSmfPort(CEAPM.getSmfPort())
            .build();
        mfMachine.addRole(sysviewRole);

        // (CA31) MQ
        IRole mqRole = new MqZosRole.Builder(MQ)
            .sysviewLoadlib(SYSVIEW.getLoadlib())
            .build();
        mfMachine.addRole(mqRole);
        mqRole.after(sysviewRole);

        // (CA31) CICS
        IRole cicsRole = new CicsRole.Builder(CICS)
            .sysviewLoadlib(SYSVIEW.getLoadlib())
            .build();
        mfMachine.addRole(cicsRole);
        cicsRole.after(sysviewRole, mqRole);

        // (CA31) CE-APM Agent
        IRole ceapmRole = new CeapmRole.Builder(CEAPM, tasResolver)
            .configSysviewXapi(SYSVIEW)
            .configEm(emRole).build();
        mfMachine.addRole(ceapmRole);
        ceapmRole.after(cicsRole);

        return testbed;
    }
}
