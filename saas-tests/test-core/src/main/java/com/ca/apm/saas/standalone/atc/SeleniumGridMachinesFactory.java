/*
 * Copyright (c) 2017 CA.  All rights reserved.
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

package com.ca.apm.saas.standalone.atc;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumInternetExplorerDriver;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * Factory for Selenium Grid machines.
 *
 */
public class SeleniumGridMachinesFactory {

    public static final String SELENIUM_MACHINE_ID = "seleniumMachine";
    public static final String TEMPLATE_ID = TEMPLATE_W64;
    
    public static final String HUB_ROLE_ID = "seleniumHubRole";
    public static final String NODE_ROLE_ID = "seleniumNodeRole";
    
    public Collection<ITestbedMachine> createMachines(ITasResolver tasResolver) {
        
        final SeleniumGridNodeRole nodeRole = new SeleniumGridNodeRole.Builder(NODE_ROLE_ID, tasResolver)
            .nodeConfiguration(createConfiguration(tasResolver))
            .qResXResolution("1920")
            .qResYResolution("1080")
            .chromeDriver(SeleniumChromeDriver.V2_22_B32)
            .internetExplorerDriver(SeleniumInternetExplorerDriver.V2_45_0_B64)
            .build();
        
        final SeleniumGridHubRole hubRole = new SeleniumGridHubRole.Builder(HUB_ROLE_ID, tasResolver)
            .addNodeRole(nodeRole)
            .build();
        
        ITestbedMachine seleniumGridMachine = new TestbedMachine.Builder(SELENIUM_MACHINE_ID)
            .templateId(TEMPLATE_ID)
            .build();
        seleniumGridMachine.addRole(hubRole);
        seleniumGridMachine.addRole(nodeRole);
        
        nodeRole.before(hubRole);
        
        final Collection<ITestbedMachine> machines = new ArrayList<>();
        machines.add(seleniumGridMachine);
        return machines;
    }
    
    private NodeConfiguration createConfiguration(ITasResolver tasResolver) {
        List<NodeCapability> capabilities = new ArrayList<NodeCapability>();

        NodeCapability chromeCapability = 
                new NodeCapability.Builder().browserType(BrowserType.CHROME)
                    .platform(NodePlatform.WINDOWS).maxInstances(20).build();
        
        NodeCapability firefoxCapability =
                new NodeCapability.Builder().browserType(BrowserType.FIREFOX)
                    .platform(NodePlatform.WINDOWS).maxInstances(20).build();

        NodeCapability internetExplorerCapability =
                new NodeCapability.Builder().browserType(BrowserType.INTERNET_EXPLORER)
                .platform(NodePlatform.WINDOWS).maxInstances(20).build();

        capabilities.add(chromeCapability);
        capabilities.add(firefoxCapability);
        capabilities.add(internetExplorerCapability);
                
        String hubHostname = tasResolver.getHostnameById(HUB_ROLE_ID);
        URL hubUrl;
        try {
            hubUrl = new URL("http", hubHostname, 4444, "/grid/register/");
        } catch (MalformedURLException e) {
            throw new RuntimeException("HUB URL is malformed. Exception: {0}" + e.getMessage());
        }
        return new NodeConfiguration.Builder().hub(hubUrl).maxSession(50).addCapabilities(capabilities).build();
    }
    
    
}
