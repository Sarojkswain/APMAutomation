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

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Deploys simple cluster with a MOM and Collector
 * and make sure it starts.
 */
@TestBedDefinition
public class SimpleClusterNoAgcTestBed implements ITestbedFactory {

    public static final String MOM_NOAGC_MACHINE = "momNoAgc";
    public static final String MOM_NOAGC_ROLE = "momNoAgcRole";
    public static final String COLL_MACHINE = "collNoAgc";
    public static final String COLL_ROLE = "collNoAgcRole";

    public static final String ADMIN_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String ADMIN_TOKEN_CMDLINE = "-Dappmap.token=" + ADMIN_TOKEN
        + " -Dappmap.user=admin";

    @Override
    public ITestbed create(ITasResolver arg0) {

        ITestbed testbed = new Testbed(this.getClass().getSimpleName());

        EmRole collRole =
            new EmRole.LinuxBuilder(COLL_ROLE, arg0).emClusterRole(EmRoleEnum.COLLECTOR)
                .nostartWV().emLaxNlJavaOption(Arrays.asList(ADMIN_TOKEN_CMDLINE)).build();
        ITestbedMachine collMachine =
            TestBedUtils.createWindowsMachine(COLL_MACHINE, TEMPLATE_CO66, collRole);

        EmRole momRole =
            new EmRole.LinuxBuilder(MOM_NOAGC_ROLE, arg0).emClusterRole(EmRoleEnum.MANAGER)
                .emCollector(collRole).emLaxNlJavaOption(Arrays.asList(ADMIN_TOKEN_CMDLINE))
                .nostartWV().build();
        ITestbedMachine momMachine =
            TestBedUtils.createWindowsMachine(MOM_NOAGC_MACHINE, ITestbedMachine.TEMPLATE_CO66,
                momRole);
        testbed.addMachine(momMachine, collMachine);

        return testbed;
    }
}
