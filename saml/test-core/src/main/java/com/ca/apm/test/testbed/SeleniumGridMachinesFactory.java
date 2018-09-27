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

package com.ca.apm.test.testbed;

import java.util.ArrayList;
import java.util.Collection;

import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumInternetExplorerDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumStandaloneServer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.seleniumgrid.NodeConfigurationFactory;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.testbed.ITestbedMachine;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * Factory for Selenium Grid machines. To be replaced by composite test-beds once available in TAS.
 * 
 * @author zakja01
 *
 */
public class SeleniumGridMachinesFactory {

    public static final String NODE_TEMPLATE_ID = TEMPLATE_W64;

    public static final String HUB_ROLE_ID = "seleniumHubRole";
    public static final String NODE_ROLE_ID = "seleniumNodeRole";
    public static final String BROWSER_ROLE_ID = "browserRole";

    public static final SeleniumStandaloneServer SELENIUM_STANDALONE_SERVER_VERSION
        = SeleniumStandaloneServer.V3_4_0;

    public Collection<ITestbedMachine> createMachines(ITasResolver tasResolver, ITestbedMachine hubMachine, ITestbedMachine nodeMachine) {



        
        // Create Node
        final SeleniumGridNodeRole node1Role = createNodeRole(tasResolver, HUB_ROLE_ID, NODE_TEMPLATE_ID, NODE_ROLE_ID);
        nodeMachine.addRole(node1Role);

        // Create Hub
        final SeleniumGridHubRole hubRole = new SeleniumGridHubRole.Builder(HUB_ROLE_ID, tasResolver)
            .addNodeRole(node1Role)
            .standaloneServerVersion(SELENIUM_STANDALONE_SERVER_VERSION)
            .build();
        
        hubMachine.addRole(hubRole);

        node1Role.before(hubRole);

        final Collection<ITestbedMachine> machines = new ArrayList<>();
        machines.add(hubMachine);
        machines.add(nodeMachine);

        return machines;
    }

    private SeleniumGridNodeRole createNodeRole(final ITasResolver tasResolver,
                                                final String hubRoleId,
                                                final String templateId,
                                                final String roleId) {
        return new SeleniumGridNodeRole.Builder(roleId, tasResolver)
            .nodeConfiguration(new NodeConfigurationFactory(tasResolver).defaultConfiguration(hubRoleId, templateId))
            .standaloneServerVersion(SELENIUM_STANDALONE_SERVER_VERSION)
            .chromeDriver(SeleniumChromeDriver.V2_29_B32)
            .internetExplorerDriver(SeleniumInternetExplorerDriver.V3_4_0_B64)
            .build();
    }
}
