/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.transactiontrace.appmap.testbed;

import com.ca.apm.transactiontrace.appmap.role.NoWhereBankRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Test bed that installs
 * 1. Stand Alone EM
 * 2. Nowhere bank application
 * Starts the nowhere bank application
 * Doesn't start traces - you need to do it yourself if needed
 */
@TestBedDefinition
public class StandAloneTestbedForLegacyMode implements ITestbedFactory {

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_ROLE_ID = "emRole";

    private static final String NO_WHERE_BANK_ID = "nowWhereBank";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed(getClass().getSimpleName());

        // create EM role
        EmRole emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver).nostartEM().nostartWV().build();

        // No where bank doesn't seem to have linux scripts, use windows
        NoWhereBankRole noWhereBankRole =
            new NoWhereBankRole.Builder(NO_WHERE_BANK_ID, tasResolver).nostart().build();

        // map EM role to machine
        ITestbedMachine emMachine =
            TestBedUtils.createWindowsMachine(EM_MACHINE_ID,
                com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64);

        emMachine.addRole(noWhereBankRole, emRole);

        emRole.before(noWhereBankRole);

        testbed.addMachine(emMachine);

        return testbed;
    }
}
