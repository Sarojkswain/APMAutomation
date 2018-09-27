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

package com.ca.apm.testbed.atc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumEdgeDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumInternetExplorerDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumStandaloneServer;
import com.ca.tas.resolver.IHostnameResolver;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

import static com.ca.apm.testbed.IE11RegistryFix.addRoleToFixIE11Registry;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W10;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * Factory for Selenium Grid machines.
 */
public class SeleniumGridMachinesFactory {

    public static final String MACHINE_ID = "seleniumMachine";
    public static final String TEMPLATE_ID = TEMPLATE_W64;
    public static final String W10_MACHINE_ID = "seleniumW10Machine";

    public static final String HUB_ROLE_ID = "seleniumHubRole";
    public static final String NODE_ROLE_ID = "seleniumNodeRole";
    public static final String W10_NODE_ROLE_ID = "w10SeleniumNodeRole";
    private static final SeleniumStandaloneServer SELENIUM_STANDALONE_SERVER_VERSION
        = SeleniumStandaloneServer.V3_4_0;
    private static final SeleniumInternetExplorerDriver SELENIUM_INTERNET_EXPLORER_DRIVER_VERSION
        = SeleniumInternetExplorerDriver.V3_4_0_B64;

    public Collection<ITestbedMachine> createMachines(ITasResolver tasResolver) {

        final SeleniumGridNodeRole nodeRole = new SeleniumGridNodeRole.Builder(NODE_ROLE_ID,
            tasResolver)
            .nodeConfiguration(createConfiguration(tasResolver))
            .qResXResolution("1920")
            .qResYResolution("1080")
            .chromeDriver(SeleniumChromeDriver.V2_29_B32)
            .internetExplorerDriver(SELENIUM_INTERNET_EXPLORER_DRIVER_VERSION)
            .standaloneServerVersion(SELENIUM_STANDALONE_SERVER_VERSION)
            .build();

        final SeleniumGridNodeRole w10NodeRole = new SeleniumGridNodeRole.Builder(W10_NODE_ROLE_ID,
            tasResolver)
            .nodeConfiguration(createW10Configuration(tasResolver))
            .qResXResolution("1920")
            .qResYResolution("1080")
            .internetExplorerDriver(SELENIUM_INTERNET_EXPLORER_DRIVER_VERSION)
            .edgeDriver(SeleniumEdgeDriver.V3_14393)
            .standaloneServerVersion(SELENIUM_STANDALONE_SERVER_VERSION)
            .build();

        final SeleniumGridHubRole hubRole = new SeleniumGridHubRole.Builder(HUB_ROLE_ID,
            tasResolver)
            .addNodeRole(nodeRole)
            .addNodeRole(w10NodeRole)
            .standaloneServerVersion(SELENIUM_STANDALONE_SERVER_VERSION)
            .build();

        ITestbedMachine w10SeleniumGridMachine = new TestbedMachine.Builder(W10_MACHINE_ID)
            .templateId(TEMPLATE_W10)
            .build();
        w10SeleniumGridMachine.addRole(w10NodeRole);

        ITestbedMachine seleniumGridMachine = new TestbedMachine.Builder(MACHINE_ID)
            .templateId(TEMPLATE_ID)
            .build();
        seleniumGridMachine.addRole(hubRole);
        seleniumGridMachine.addRole(nodeRole);

        hubRole.after(nodeRole, w10NodeRole);

        final Collection<ITestbedMachine> machines = new ArrayList<>(1);
        machines.add(seleniumGridMachine);
        machines.add(w10SeleniumGridMachine);
        machines.forEach(machine -> addRoleToFixIE11Registry(machine, tasResolver));
        return machines;
    }

    private NodeConfiguration createW10Configuration(ITasResolver tasResolver) {
        List<NodeCapability> capabilities = new ArrayList<>(3);

        NodeCapability edgeCapability = new NodeCapability.Builder()
            .browserType(BrowserType.EDGE)
            .platform(NodePlatform.WINDOWS)
            // As of of version 14.14393, Edge does not support multiple sessions, thus we limit
            // the number of instances to 1.
            .maxInstances(1)
            .build();
        capabilities.add(edgeCapability);

        return createNodeConfiguration(tasResolver, capabilities);
    }

    private NodeConfiguration createNodeConfiguration(IHostnameResolver tasResolver,
        Collection<NodeCapability> capabilities) {
        String hubHostname = tasResolver.getHostnameById(HUB_ROLE_ID);
        URL hubUrl;
        try {
            hubUrl = new URL("http", hubHostname, 4444, "/grid/register/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return new NodeConfiguration.Builder().hub(hubUrl).maxSession(7)
            .addCapabilities(capabilities).build();
    }

    private NodeConfiguration createConfiguration(ITasResolver tasResolver) {
        List<NodeCapability> capabilities = new ArrayList<>(3);

        NodeCapability chromeCapability = new NodeCapability.Builder()
            .browserType(BrowserType.CHROME)
            .platform(NodePlatform.WINDOWS)
            .maxInstances(8)
            .build();
        NodeCapability firefoxCapability = new NodeCapability.Builder()
            .browserType(BrowserType.FIREFOX)
            .platform(NodePlatform.WINDOWS)
            .maxInstances(8)
            .build();
        NodeCapability ie11Capability = new NodeCapability.Builder()
            .browserType(BrowserType.INTERNET_EXPLORER)
            .platform(NodePlatform.WINDOWS)
            .version("11")
            .maxInstances(8)
            .build();
        capabilities.add(chromeCapability);
        capabilities.add(firefoxCapability);
        capabilities.add(ie11Capability);

        return createNodeConfiguration(tasResolver, capabilities);
    }

}
