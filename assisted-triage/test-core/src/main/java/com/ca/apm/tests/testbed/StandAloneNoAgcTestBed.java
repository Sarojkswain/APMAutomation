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

package com.ca.apm.tests.testbed;


import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

import java.util.Arrays;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Deploy just standalone and make sure it
 * starts.
 */
@TestBedDefinition
public class StandAloneNoAgcTestBed implements ITestbedFactory {

    public static final String SA_NOAGC_MACHINE = "saNoAgc";
    public static final String SA_NOAGC_ROLE = "saNoAgcRole";

    public static final String SA_NOAGC_MACHINE1 = "saNoAgc1";
    public static final String SA_NOAGC_ROLE1 = "saNoAgcRole1";

    public static final String ADMIN_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String ADMIN_TOKEN_CMDLINE = "-Dappmap.token=" + ADMIN_TOKEN
        + " -Dappmap.user=admin";

    @Override
    public ITestbed create(ITasResolver arg0) {

        ITestbed testbed = new Testbed(this.getClass().getSimpleName());

        EmRole saNoAgcRole =
            new EmRole.LinuxBuilder(SA_NOAGC_ROLE, arg0).emLaxNlJavaOption(
                Arrays.asList(ADMIN_TOKEN_CMDLINE)).build();
        ITestbedMachine saNoAgcMachine =
            TestBedUtils.createWindowsMachine(SA_NOAGC_MACHINE, TEMPLATE_CO66, saNoAgcRole);

        EmRole saNoAgcRole1 =
            new EmRole.LinuxBuilder(SA_NOAGC_ROLE1, arg0).emLaxNlJavaOption(
                Arrays.asList(ADMIN_TOKEN_CMDLINE)).build();
        ITestbedMachine saNoAgcMachine1 =
            TestBedUtils.createWindowsMachine(SA_NOAGC_MACHINE1, TEMPLATE_CO66, saNoAgcRole1);

        testbed.addMachine(saNoAgcMachine, saNoAgcMachine1);
        return testbed;
    }
}
