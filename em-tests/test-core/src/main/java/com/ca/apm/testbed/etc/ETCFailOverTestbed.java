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
package com.ca.apm.testbed.etc;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

import java.util.Arrays;
import java.util.Collection;

/**
 * Test bed with two stand alone EMs, one acting as master and other as provider.
 * Both EM will be configured with fail over configuration.
 *
 * @author bhusu01
 */
@TestBedDefinition
public class ETCFailOverTestbed implements ITestbedFactory {

    public static final String MACHINE_ID_ETC = "etc";
    public static final String MACHINE_ID_STANDALONE = "standAlone";
    public static final String MACHINE_ID_FAIL_OVER = "failOver";
    public static final String ROLE_ID_ETC = "etcRole";
    public static final String ROLE_ID_STANDALONE = "standAloneRole";
    public static final String ROLE_ID_FREE = "freeRole";

    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String ADMIN = "admin";

    private static final String PROPERTY_TEAM_CENTER_MASTER =
        "introscope.apmserver.teamcenter.master";
    private static final String PROPERTY_FAIL_OVER = "introscope.enterprisemanager.failover.enable";
    private static final String PROPERTY_FAIL_OVER_PRIMARY =
        "introscope.enterprisemanager.failover.primary";
    private static final String PROPERTY_FAIL_OVER_SECONDARY =
        "introscope.enterprisemanager.failover.secondary";
    private static final Collection<String> EM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Dappmap.token=" + ADMIN_AUX_TOKEN, "-Dappmap.user="
            + ADMIN, "-Dcom.wily.assert=false", "-XX:+HeapDumpOnOutOfMemoryError", "-verbosegc",
        "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=20555");


    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("ETCFailOverTestBed");
        /* -- Initialize machines -- */
        ITestbedMachine etcMachine =
            TestBedUtils.createWindowsMachine(MACHINE_ID_ETC, ITestbedMachine.TEMPLATE_W64);
        ITestbedMachine standAloneMachine =
            TestBedUtils.createWindowsMachine(MACHINE_ID_STANDALONE, ITestbedMachine.TEMPLATE_W64);
        ITestbedMachine failOverMachine =
            TestBedUtils.createWindowsMachine(MACHINE_ID_FAIL_OVER, ITestbedMachine.TEMPLATE_W64);
        /* -- Define roles -- */
        EmRole etcRole, standAloneRole;
        DeployFreeRole freeRole;

        /* -- Build roles -- */
        // For the moment, configure both roles with the same fail over machine and test can decide
        // which one to fail over. If we fail over is required on both machines, another machine
        // can be added
        etcRole =
            new EmRole.Builder(ROLE_ID_ETC, tasResolver).configProperty
                (PROPERTY_TEAM_CENTER_MASTER, String.valueOf(Boolean.TRUE)).configProperty
                (PROPERTY_FAIL_OVER, String.valueOf(Boolean.TRUE)).configProperty
                (PROPERTY_FAIL_OVER_PRIMARY, tasResolver.getHostnameById(ROLE_ID_ETC))
                .configProperty(PROPERTY_FAIL_OVER_SECONDARY, tasResolver.getHostnameById
                    (ROLE_ID_FREE)).emLaxNlJavaOption(EM_LAXNL_JAVA_OPTION).dbhost(tasResolver
                .getHostnameById(ROLE_ID_ETC)).nostartEM().nostartWV().build();

        standAloneRole =
            new EmRole.Builder(ROLE_ID_STANDALONE, tasResolver).configProperty
                (PROPERTY_FAIL_OVER, String.valueOf(Boolean.TRUE)).configProperty
                (PROPERTY_FAIL_OVER_PRIMARY, tasResolver.getHostnameById(ROLE_ID_STANDALONE))
                .configProperty(PROPERTY_FAIL_OVER_SECONDARY, tasResolver.getHostnameById
                    (ROLE_ID_FREE)).emLaxNlJavaOption(EM_LAXNL_JAVA_OPTION).nostartEM().nostartWV
                ().build();

        freeRole = new DeployFreeRole(ROLE_ID_FREE);
        /* -- Role orchestration -- */
        /* -- Map roles to machines -- */
        etcMachine.addRole(etcRole);
        standAloneMachine.addRole(standAloneRole);
        failOverMachine.addRole(freeRole);
        /* -- Add machines to testbed -- */
        testbed.addMachine(etcMachine, standAloneMachine, failOverMachine);
        return testbed;
    }
}
