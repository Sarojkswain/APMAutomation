/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

package com.ca.apm.transactiontrace.appmap.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.Collection;

import com.ca.apm.transactiontrace.appmap.role.InitiateTransactionTraceSessionRole;
import com.ca.apm.transactiontrace.appmap.role.NoWhereBankRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Test bed that installs
 * <p/>
 * 1. Stand Alone EM
 * 2. Nowhere bank application
 * 3. Chrome Driver
 * <p/>
 * Start the nowhere bank application
 * Start traces using CLW command
 * <p/>
 * Login to team center and get the graph from private API
 * Compare with expected output
 */
@TestBedDefinition
public class StandAloneTestbed implements ITestbedFactory {

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_ROLE_ID = "emRole";
    private static final String EM_MACHINE_TEMPLATE_ID = TEMPLATE_W64;

    private static final String NO_WHERE_BANK_ID = "nowWhereBank";
    private static final String CLW_TRACE_ID = "clwTraceCommand";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed(getClass().getSimpleName());

        //create EM role
        EmRole emRole =
            new EmRole.Builder(EM_ROLE_ID, tasResolver).build();

        // No where bank doesn't seem to have linux scripts, use windows
        NoWhereBankRole noWhereBankRole =
            new NoWhereBankRole.Builder(NO_WHERE_BANK_ID, tasResolver).build();
        InitiateTransactionTraceSessionRole traceSessionRole =
            new InitiateTransactionTraceSessionRole.Builder(CLW_TRACE_ID, tasResolver).build();

        //map EM role to machine
        ITestbedMachine emMachine =
            TestBedUtils.createWindowsMachine(EM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID);

        emMachine.addRole(noWhereBankRole, emRole, traceSessionRole);

        emRole.before(noWhereBankRole);
        noWhereBankRole.before(traceSessionRole);

        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver);

        testbed.addMachine(emMachine);
        testbed.addMachines(seleniumGridMachines);

        return testbed;
    }
}

