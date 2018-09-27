/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.tas.test.em.transactiontrace.appmap;

import com.ca.apm.automation.action.flow.testapp.NowhereBankVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.testapp.custom.NowhereBankBTRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

@TestBedDefinition
public class StandAloneWithNowhereBankBTWindowsTestBed implements ITestbedFactory {

    public static final String EM_ROLE_ID = "standAloneEM";
    public static final String NWB_ROLE_ID = "nowhereBankBT";
    public static final String MACHINE_ID = "standAlone";

    private static final String EM_CONF_PROP_TT_TIME_FAST =
        "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast";


    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("StandAloneWithNowhereBankBT");

        /* -- Initialize machines -- */
        ITestbedMachine emMachine = new TestbedMachine.Builder(MACHINE_ID)
                .platform(Platform.WINDOWS)
                .templateId("w64")
                .build();

        /* -- Define roles -- */
        EmRole emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver)
                .configProperty(EM_CONF_PROP_TT_TIME_FAST, "30")
                .build();

        NowhereBankBTRole nowhereBankBTRole = new NowhereBankBTRole
            .Builder(NWB_ROLE_ID,tasResolver)
            .nowhereBankVersion(NowhereBankVersion.v103)
            .stagingBaseDir(emMachine.getAutomationBaseDir())
            .build();


        /* -- Map roles to machines -- */
        emMachine.addRole(emRole, nowhereBankBTRole);

        /* -- Add machines to testbed -- */
        testbed.addMachine(emMachine);

        return testbed;
    }
}
