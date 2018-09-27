/*
 * Copyright (c) 2016 CA.  All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.seleniumgrid.NodeConfigurationFactory;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * Factory for Selenium Grid machines. To be replaced by composite test-beds once available in TAS.
 * 
 * @author zakja01
 *
 */
public class SeleniumGridMachinesFactory {

    public static final String HUB_MACHINE_ID = "hubMachine";
    public static final String NODE1_MACHINE_ID = "nodeMachine1";
    public static final String NODE2_MACHINE_ID = "nodeMachine2";

    public static final String HUB_TEMPLATE_ID = TEMPLATE_W64;
    public static final String NODE1_TEMPLATE_ID = TEMPLATE_W64;
    public static final String NODE2_TEMPLATE_ID = TEMPLATE_W64;

    public static final String HUB_ROLE_ID = "seleniumHubRole";
    public static final String NODE1_ROLE_ID = "seleniumNode1Role";
    public static final String NODE2_ROLE_ID = "seleniumNode2Role";
    
    public Collection<ITestbedMachine> createMachines(ITasResolver tasResolver) {

        // Create Node1
        final SeleniumGridNodeRole node1Role = createNodeRole(tasResolver, HUB_ROLE_ID, NODE1_TEMPLATE_ID, NODE1_ROLE_ID);
        ITestbedMachine seleniumGridNode1Machine = new TestbedMachine.Builder(NODE1_MACHINE_ID)
            .templateId(NODE1_TEMPLATE_ID)
            .build();
        seleniumGridNode1Machine.addRole(node1Role);

        // Create Node2
        final SeleniumGridNodeRole node2Role = createNodeRole(tasResolver, HUB_ROLE_ID, NODE2_TEMPLATE_ID, NODE2_ROLE_ID);
        ITestbedMachine seleniumGridNode2Machine = new TestbedMachine.Builder(NODE2_MACHINE_ID)
            .templateId(NODE2_TEMPLATE_ID)
            .build();
        seleniumGridNode2Machine.addRole(node2Role);

        // Create Hub
        final SeleniumGridHubRole hubRole = new SeleniumGridHubRole.Builder(HUB_ROLE_ID, tasResolver)
            .addNodeRole(node1Role)
            .addNodeRole(node2Role)
            .build();
        ITestbedMachine seleniumGridHubMachine = new TestbedMachine.Builder(HUB_MACHINE_ID)
            .templateId(HUB_TEMPLATE_ID)
            .build();
        seleniumGridHubMachine.addRole(hubRole);

        node1Role.before(hubRole);
        node2Role.before(hubRole);

        final Collection<ITestbedMachine> machines = new ArrayList<>();
        machines.add(seleniumGridHubMachine);
        machines.add(seleniumGridNode1Machine);
        machines.add(seleniumGridNode2Machine);

        return machines;
    }

    private SeleniumGridNodeRole createNodeRole(final ITasResolver tasResolver,
                                                final String hubRoleId,
                                                final String templateId,
                                                final String roleId) {
        return new SeleniumGridNodeRole.Builder(roleId, tasResolver)
            .nodeConfiguration(new NodeConfigurationFactory(tasResolver).defaultConfiguration(hubRoleId, templateId))
            .build();
    }
}
