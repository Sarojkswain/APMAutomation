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

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.transactiontrace.appmap.role.InitiateTransactionTraceSessionRole;
import com.ca.apm.transactiontrace.appmap.role.LoadBalanceAgentsRole;
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
 * 1. MOM
 * 2. Nowhere bank application
 * 3. Chrome Driver
 * 4. 3 Collectors
 *
 * Each collector is deployed in its own machine
 * All other components are installed in the same machine
 *
 * Loadbalancing.xml is modified to distribute the agents among collectors
 *
 * <p/>
 * Start the nowhere bank application
 * Start traces using CLW command
 * <p/>
 * Login to team center and get the graph from private API
 * Compare with expected output
 */
@TestBedDefinition
public class ClusteredTestbed implements ITestbedFactory {

    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String MOM_ROLE_ID = "momRole";

    public static final String COLLECTOR_1_MACHINE_ID = "c1Machine";
    public static final String COLLECTOR_1_ROLE_ID = "c1Role";
    public static final String COLLECTOR_2_MACHINE_ID = "c2Machine";
    public static final String COLLECTOR_2_ROLE_ID = "c2Role";
    public static final String COLLECTOR_3_MACHINE_ID = "c3Machine";
    public static final String COLLECTOR_3_ROLE_ID = "c3Role";


    private static final String MOM_MACHINE_TEMPLATE_ID = TEMPLATE_W64;
    private static final String COLLECTOR_MACHINE_TEMPLATE_ID = TEMPLATE_RH66;

    private static final String NO_WHERE_BANK_ID = "nowWhereBank";
    private static final String CLW_TRACE_ID = "clwTraceCommand";
    private static final String LB_XML_ID = "loadBalanceAgents";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed(getClass().getSimpleName());

        // Collector needs just the Enterprise manager
        List<String> collectorChosenFeatures =
            Arrays.asList(new String[] {"Enterprise Manager"});

        // Deploy collectors on linux, but deploy MOM on Windows
        // as MOM and No where bank get deployed in the same machine
        // Nowhere bank role currently runs only on Windows
        //map EM role to machine
        ITestbedMachine momMachine =
            TestBedUtils.createWindowsMachine(MOM_MACHINE_ID, MOM_MACHINE_TEMPLATE_ID);

        ITestbedMachine c1Machine = TestBedUtils.createLinuxMachine(COLLECTOR_1_MACHINE_ID, COLLECTOR_MACHINE_TEMPLATE_ID);
        ITestbedMachine c2Machine = TestBedUtils.createLinuxMachine(COLLECTOR_2_MACHINE_ID, COLLECTOR_MACHINE_TEMPLATE_ID);
        ITestbedMachine c3Machine = TestBedUtils.createLinuxMachine(COLLECTOR_3_MACHINE_ID, COLLECTOR_MACHINE_TEMPLATE_ID);

        EmRole c1Role =
            new EmRole.LinuxBuilder(COLLECTOR_1_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID))
                .dbpassword("quality")
                .nostartWV()
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .build();

        EmRole c2Role =
            new EmRole.LinuxBuilder(COLLECTOR_2_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID))
                .dbpassword("quality").nostartWV()
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .build();

        EmRole c3Role =
            new EmRole.LinuxBuilder(COLLECTOR_3_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID))
                .dbpassword("quality").nostartWV()
                .silentInstallChosenFeatures(collectorChosenFeatures)
                .build();

        EmRole momRole =
            new EmRole.Builder(MOM_ROLE_ID, tasResolver)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .dbpassword("quality")
                .emCollector(c1Role)
                .emCollector(c2Role)
                .emCollector(c3Role)
                .nostartEM()
                .nostartWV()
                .build();

        // Modifies loadbalancing.xml to distribute the agents to all collectors
        LoadBalanceAgentsRole agentLoadBalanceRole = new LoadBalanceAgentsRole.Builder(LB_XML_ID, tasResolver).build();

        NoWhereBankRole noWhereBankRole =
            new NoWhereBankRole.Builder(NO_WHERE_BANK_ID, tasResolver).build();

        // Starts a transaction trace session
        InitiateTransactionTraceSessionRole traceSessionRole =
            new InitiateTransactionTraceSessionRole.Builder(CLW_TRACE_ID, tasResolver).build();

        momMachine.addRole(noWhereBankRole, momRole, traceSessionRole, agentLoadBalanceRole);

        momRole.before(agentLoadBalanceRole);
        agentLoadBalanceRole.before(noWhereBankRole);
        noWhereBankRole.before(traceSessionRole);

        c1Machine.addRole(c1Role);
        c2Machine.addRole(c2Role);
        c3Machine.addRole(c3Role);

        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver);

        testbed.addMachine(momMachine);
        testbed.addMachine(c1Machine);
        testbed.addMachine(c2Machine);
        testbed.addMachine(c3Machine);
        testbed.addMachines(seleniumGridMachines);

        return testbed;
    }
}

