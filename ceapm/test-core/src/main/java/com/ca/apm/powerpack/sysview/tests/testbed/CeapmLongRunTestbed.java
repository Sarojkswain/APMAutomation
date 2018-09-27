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

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole.CeapmConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole.CicsConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsTestDriverRole;
import com.ca.apm.powerpack.sysview.tests.role.CtgRole;
import com.ca.apm.powerpack.sysview.tests.role.CtgServerDefinition;
import com.ca.apm.powerpack.sysview.tests.role.DatacomRole;
import com.ca.apm.powerpack.sysview.tests.role.DatacomRole.DatacomConfig;
import com.ca.apm.powerpack.sysview.tests.role.ForwarderRole;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole.MqZosConfig;
import com.ca.apm.powerpack.sysview.tests.role.SysvDb2Role;
import com.ca.apm.powerpack.sysview.tests.role.SysvDb2Role.SysvDb2Config;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole.SysviewConfig;
import com.ca.apm.powerpack.sysview.tests.role.Was8Role;
import com.ca.apm.powerpack.sysview.tests.role.WasAppRole;
import com.ca.apm.powerpack.sysview.tests.test.CeapmLongRunTest;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/** Testbed for {@link CeapmLongRunTest} */
@TestBedDefinition
public class CeapmLongRunTestbed implements ITestbedFactory {

    public static final String WIN_MACHINE_BASE_ID = "winMachineBase";
    public static final String EM_BASE_ID = "emRoleBase";
    public static final CeapmConfig CEAPM_BASE = CeapmConfig.LONG1;
    public static final CeapmConfig CEAPM_TEST = CeapmConfig.LONG2;

    public static final String MF_MACHINE_ID = "mainframeMachine";
    public static final String WIN_MACHINE_ID = "winMachine";
    public static final String WAS_ROLE_ID = "wasRole";
    public static final String MQ_APP_ROLE_ID = "mqAppRole";
    public static final String CTG_ROLE_ID = "ctgRole";
    public static final String CTD_ROLE_ID = "ctdRole";
    public static final String EM_ROLE_ID = "emRole";

    public static final SysviewConfig SYSVIEW = SysviewConfig.WILY_14_1;
    public static final SysvDb2Config SYSVDB2 = SysvDb2Config.D10A_18_0;
    public static final MqZosConfig MQ = MqZosConfig.CSQ4;
    public static final CicsConfig CICS = CicsConfig.WILY_5_3_0;

    public static final DatacomConfig DATACOM = DatacomConfig.PATLAMUF;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("CeapmAppmapTestbed");

        // ********************* Baseline EM machine *********************
        TestbedMachine winMachineBase =
            new TestbedMachine.Builder(WIN_MACHINE_BASE_ID).templateId(TEMPLATE_W64).build();
        testbed.addMachine(winMachineBase);

        // EM role with enabled CEAPM powerpack of the same version as baseline agent
        EmRole emRoleBase =
            new EmRole.Builder(EM_BASE_ID, tasResolver)
                .version(CeapmRole.CEAPM_GA_VERSION)
                .installerProperty("shouldEnableSysview", "true").build();
        winMachineBase.addRole(emRoleBase);

        // ************************ New EM machine ************************
        TestbedMachine winMachineTest =
            new TestbedMachine.Builder(WIN_MACHINE_ID).templateId(TEMPLATE_W64).build();
        testbed.addMachine(winMachineTest);

        // EM role with enabled CEAPM powerpack of the new version
        EmRole emRoleTest = new EmRole.Builder(EM_ROLE_ID, tasResolver)
            .installerProperty("shouldEnableSysview", "true").build();
        winMachineTest.addRole(emRoleTest);

        Was8Role wasRole = new Was8Role.Builder(WAS_ROLE_ID, tasResolver).build();
        winMachineTest.addRole(wasRole);

        // Application that allows CICS/IMS calls through MQ
        IRole mqAppRole = new WasAppRole.Builder(MQ_APP_ROLE_ID, tasResolver)
            .webAppArtifact(new DefaultArtifact("com.ca.apm.powerpack.sysview.tests",
                "ceapm.tools.CPTJCAApp", "war", null))
            .webAppName("CPTJCAApp").parentRole(WAS_ROLE_ID).webAppContextRoot("CPTJCAApp").build();
        mqAppRole.after(wasRole);
        winMachineTest.addRole(mqAppRole);

        // CTG Server role
        CtgRole ctgRole =
            new CtgRole.Builder(CTG_ROLE_ID, tasResolver)
                .addServerDefinition(
                    CtgServerDefinition.getEciDefinition(CICS.getEciId(), CICS.getHost(),
                        CICS.getEciPort()))
                .addServerDefinition(
                    CtgServerDefinition.getIpicDefinition(CICS.getIpicId(), CICS.getHost(),
                        CICS.getIpicPort())).build();
        winMachineTest.addRole(ctgRole);

        // CICSTestDriver role
        CicsTestDriverRole ctdRole =
            new CicsTestDriverRole.Builder(CTD_ROLE_ID, tasResolver)
                .emHost(tasResolver.getHostnameById(emRoleTest.getRoleId()))
                .emPort(emRoleTest.getEmPort()).build();
        ctdRole.after(emRoleTest, ctgRole);
        winMachineTest.addRole(ctdRole);

        // ************************ MF machine ************************
        TestbedMachine mfMachine =
            new TestbedMachine.LinuxBuilder(MF_MACHINE_ID).templateId(TEMPLATE_CO66).build();
        testbed.addMachine(mfMachine);

        mfMachine.addRole(ForwarderRole.FW_CA31);

        // SYSVIEW
        IRole sysviewRole =
            new SysviewRole.Builder(SYSVIEW)
                .addSmfPort(CEAPM_TEST.getSmfPort())
                .addSmfPort(CEAPM_BASE.getSmfPort())
                .build();
        mfMachine.addRole(sysviewRole);

        // SYSVIEW for DB2
        IRole sysvDb2Role = new SysvDb2Role.Builder(SYSVDB2)
            .sysviewLoadlib(SYSVIEW.getLoadlib())
            .build();
        mfMachine.addRole(sysvDb2Role);

        // MQ
        IRole mqRole = new MqZosRole.Builder(MQ)
            .sysviewLoadlib(SYSVIEW.getLoadlib())
            .build();
        mfMachine.addRole(mqRole);

        // CICS
        IRole cicsRole = new CicsRole.Builder(CICS)
            .sysviewLoadlib(SYSVIEW.getLoadlib())
            .monitorDb2Subsystem(SYSVDB2)
            .build();
        mfMachine.addRole(cicsRole);

        // DATACOM
        IRole datacomRole = new DatacomRole.Builder(DATACOM)
            .build();
        mfMachine.addRole(datacomRole);

        // ************************CE-APM agents ************************
        // CE-APM agent - baseline (GA)
        IRole ceapmRoleBase =
            new CeapmRole.Builder(CEAPM_BASE, tasResolver)
                .version(CeapmRole.CEAPM_GA_VERSION)
                .configEmHost(tasResolver.getHostnameById(EM_BASE_ID))
                .configDb2(CEAPM_BASE)
                .configJavaVersionParameters(CeapmRole.CEAPM_GA_JAVA_VERSION)
                .setProfile("log4j.rootLogger", "WARN,logfile")
                .setProfile("log4j.logger.IntroscopeAgent", "WARN,logfile").build();
        mfMachine.addRole(ceapmRoleBase);

        // CE-APM agent - in test
        IRole ceapmRoleTest =
            new CeapmRole.Builder(CEAPM_TEST, tasResolver)
                .configEmHost(tasResolver.getHostnameById(EM_ROLE_ID))
                .configDb2(CEAPM_TEST)
                .setProfile("log4j.rootLogger", "WARN,logfile")
                .setProfile("log4j.logger.IntroscopeAgent", "WARN,logfile").build();
        mfMachine.addRole(ceapmRoleTest);

        // Control the deployment order of all the mainframe roles.
        ForwarderRole.FW_CA31.before(sysviewRole, sysvDb2Role);
        mqRole.after(sysviewRole);
        cicsRole.after(sysviewRole, mqRole);
        datacomRole.after(sysviewRole);
        ceapmRoleBase.after(cicsRole, datacomRole, sysvDb2Role);
        ceapmRoleTest.after(cicsRole, datacomRole, sysvDb2Role);

        return testbed;
    }
}
