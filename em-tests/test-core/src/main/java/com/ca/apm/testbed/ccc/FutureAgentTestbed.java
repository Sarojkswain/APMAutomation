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
package com.ca.apm.testbed.ccc;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.testapp.custom.NowhereBankBTRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

import java.util.Arrays;
import java.util.Collection;

/**
 * Simple test bed with one EM and two NWB agents.
 * <p/>
 * Driver machine is used only to launch and execute the test
 *
 * @author bhusu01
 */
@TestBedDefinition
public class FutureAgentTestbed implements ITestbedFactory {

    public static final String MACHINE_ID_EM = "em";
    public static final String MACHINE_ID_NWB = "futureMachine";
    public static final String MACHINE_ID_DRIVER = "driverMachine";
    public static final String ROLE_EM = "standalone";
    public static final String ROLE_ID_NWB_LOCAL = "nwb_local";
    public static final String ROLE_ID_NWB_FUTURE = "nwb_future";

    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String ADMIN = "admin";
    public static final Collection<String> EM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Dappmap.token=" + ADMIN_AUX_TOKEN, "-Dappmap.user="
            + ADMIN, "-Dcom.wily.assert=false", "-XX:+HeapDumpOnOutOfMemoryError", "-verbosegc",
        "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=20555");
    private static final String TRANSACTION_TRACES_FAST_BUFFER_PROPERTY =
        "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast";
    private static final String FAST_BUFFER_FAST_TIME = "30";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("FutureTimeTestbed");
        /* -- Initialize machines -- */
        ITestbedMachine emMachine =
            TestBedUtils.createLinuxMachine(MACHINE_ID_EM, ITestbedMachine.TEMPLATE_RH66);
        ITestbedMachine futureMachine =
            TestBedUtils.createLinuxMachine(MACHINE_ID_NWB, ITestbedMachine.TEMPLATE_RH66);
        ITestbedMachine driverMachine =
            TestBedUtils.createLinuxMachine(MACHINE_ID_DRIVER, ITestbedMachine.TEMPLATE_RH66);
        /* -- Define roles -- */
        EmRole emRole;
        NowhereBankBTRole nwbLocal, nwbFuture;

        /* -- Build roles -- */
        emRole =
            new EmRole.LinuxBuilder(ROLE_EM, tasResolver).configProperty
                (TRANSACTION_TRACES_FAST_BUFFER_PROPERTY, FAST_BUFFER_FAST_TIME)
                .emLaxNlJavaOption(EM_LAXNL_JAVA_OPTION).nostartEM().nostartWV().build();

        nwbLocal =
            new NowhereBankBTRole.LinuxBuilder(ROLE_ID_NWB_LOCAL, tasResolver).stagingBaseDir
                (emMachine.getAutomationBaseDir()).noStart().build();

        nwbFuture =
            new NowhereBankBTRole.LinuxBuilder(ROLE_ID_NWB_FUTURE, tasResolver).stagingBaseDir
                (emMachine.getAutomationBaseDir()).noStart().build();

        /* -- Role orchestration -- */
        /* -- Map roles to machines -- */
        emMachine.addRole(emRole, nwbLocal);
        futureMachine.addRole(nwbFuture);
        /* -- Add machines to testbed -- */
        testbed.addMachine(emMachine, futureMachine, driverMachine);
        return testbed;
    }
}
