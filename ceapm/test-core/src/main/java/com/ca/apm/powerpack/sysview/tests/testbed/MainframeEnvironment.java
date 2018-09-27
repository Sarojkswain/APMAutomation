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

import com.ca.apm.powerpack.sysview.tests.role.CicsRole;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole.CicsConfig;
import com.ca.apm.powerpack.sysview.tests.role.DatacomRole;
import com.ca.apm.powerpack.sysview.tests.role.DatacomRole.DatacomConfig;
import com.ca.apm.powerpack.sysview.tests.role.ImsRole;
import com.ca.apm.powerpack.sysview.tests.role.ImsRole.ImsConfig;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole.MqZosConfig;
import com.ca.apm.powerpack.sysview.tests.role.SysvDb2Role;
import com.ca.apm.powerpack.sysview.tests.role.SysvDb2Role.SysvDb2Config;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole.SysviewConfig;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import java.util.Collection;
import java.util.Collections;

/**
 * Testbed containing the common Mainframe software stack for CE-APM testing on CA31.
 */
// TODO: This testbed doesn't technically require cleanup but there is a bug in TAS that causes
//       testbeds with physical machines that do not include an explicit cleanup testbed to fail.
//       The issues is tracked under DE163116.
@TestBedDefinition(cleanUpTestBed = MainframeTestbedCleanup.class)
public class MainframeEnvironment implements ITestbedFactory, MainframeTestbed {
    public static final String TESTBED_ID = MainframeEnvironment.class.getSimpleName();
    public static final String MF_MACHINE_ID = "mainframeMachine";
    public static final SysviewConfig SYSVIEW = SysviewConfig.WILY_14_1;
    public static final SysvDb2Config SYSVDB2 = SysvDb2Config.D10A_18_0;
    public static final ImsConfig IMS = ImsConfig.SVPD;
    public static final MqZosConfig MQ = MqZosConfig.CSQ4;
    public static final CicsConfig CICS = CicsConfig.WILY_5_3_0;
    public static final DatacomConfig DATACOM = DatacomConfig.PATLAMUF;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed(TESTBED_ID);

        // Mainframe CA31
        TestbedMachine mfMachine = new MainframeTestbedMachine.Builder(MF_MACHINE_ID)
            .templateId(TEMPLATE_CA31)
            .build();
        testbed.addMachine(mfMachine);

        // (CA31) SYSVIEW
        SysviewRole.Builder sysviewBuilder = new SysviewRole.Builder(SYSVIEW);
        if (!shouldOnlyVerify()) {
            sysviewBuilder.deployRole();
        }
        SysviewRole sysviewRole = sysviewBuilder.build();
        mfMachine.addRole(sysviewRole);

        // (CA31) SYSVIEW for DB2
        SysvDb2Role.Builder sysvDb2Builder = new SysvDb2Role.Builder(SYSVDB2)
            .sysviewLoadlib(SYSVIEW.getLoadlib());
        if (!shouldOnlyVerify()) {
            sysvDb2Builder.deployRole();
        }
        IRole sysvDb2Role = sysvDb2Builder.build();
        mfMachine.addRole(sysvDb2Role);
        sysvDb2Role.after(sysviewRole);

        // (CA31) MQ
        MqZosRole.Builder mqBuilder = new MqZosRole.Builder(MQ)
            .sysviewLoadlib(SYSVIEW.getLoadlib());
        if (!shouldOnlyVerify()) {
            mqBuilder.deployRole();
        }
        IRole mqRole = mqBuilder.build();
        mfMachine.addRole(mqRole);
        mqRole.after(sysviewRole);

        // (CA31) CICS
        CicsRole.Builder cicsBuilder = new CicsRole.Builder(CICS)
            .sysviewLoadlib(SYSVIEW.getLoadlib())
            .monitorDb2Subsystem(SYSVDB2);
        if (!shouldOnlyVerify()) {
            cicsBuilder.deployRole();
        }
        IRole cicsRole = cicsBuilder.build();
        mfMachine.addRole(cicsRole);
        cicsRole.after(sysviewRole, mqRole);

        // (CA31) DATACOM
        DatacomRole.Builder datacomBuilder = new DatacomRole.Builder(DATACOM);
        if (!shouldOnlyVerify()) {
            datacomBuilder.deployRole();
        }
        IRole datacomRole = datacomBuilder.build();
        mfMachine.addRole(datacomRole);
        datacomRole.after(sysviewRole);

        // (CA31) IMS
        ImsRole.Builder imsBuilder = new ImsRole.Builder(IMS)
            .sysviewLoadlib(SYSVIEW.getLoadlib());
        if (!shouldOnlyVerify()) {
            imsBuilder.deployRole();
        }
        IRole imsRole = imsBuilder.build();
        mfMachine.addRole(imsRole);
        imsRole.after(sysviewRole, mqRole);

        return testbed;
    }

    /**
     * Indicates whether the testbed should only verify the environment.
     *
     * @return {@code true} if the environment should only be verified, {@code false} otherwise.
     */
    protected boolean shouldOnlyVerify() {
        return false;
    }

    @Override
    public Collection<SysviewConfig> getSysviewInstances() {
        return Collections.singletonList(SYSVIEW);
    }

    @Override
    public Collection<SysvDb2Config> getSysvdb2Instances() {
        return Collections.singletonList(SYSVDB2);
    }

    @Override
    public Collection<CicsConfig> getCicsRegions() {
        return Collections.singletonList(CICS);
    }

    @Override
    public Collection<MqZosConfig> getMqSubsystems() {
        return Collections.singletonList(MQ);
    }

    @Override
    public Collection<ImsConfig> getImsRegions() {
        return Collections.singletonList(IMS);
    }

    @Override
    public Collection<DatacomConfig> getDatacomInstances() {
        return Collections.singletonList(DATACOM);
    }
}
