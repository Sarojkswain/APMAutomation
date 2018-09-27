package com.ca.apm.saas.testbed;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.ca.apm.saas.role.ClientDeployRole;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * SAAS Base test bed
 * 
 * @author kurma05
 */
@TestBedDefinition
public class SaasBaseTestbed implements ITestbedFactory {

    public static final String HUB_ROLE_ID     = "seleniumHubRole";
    public static final String NODE_ROLE_ID    = "seleniumNodeRole";
    public static final String LOAD_ROLE_ID    = "seleniumLoadRole";
    public static final String WIN_DEPLOY_BASE = TasBuilder.WIN_SOFTWARE_LOC;
    protected ITestbed testbed                 = null;
    protected boolean shouldDeployJmeter       = false;
    protected boolean shouldDeployStressapp    = false;
    public static final String ARTIFACTS_VERSION_PROPERTY_KEY = "artifactsVersionProperty";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        testbed = new Testbed(this.getClass().getSimpleName());
        initMachines(tasResolver); 
        
        testbed.addProperty(ARTIFACTS_VERSION_PROPERTY_KEY, tasResolver.getDefaultVersion());
        return testbed;        
    }
    
    protected void initMachines(ITasResolver tasResolver) {
        
        //override with your own implementation
    }
    
    /**
     * Add Selenium & Client (jmeter, stressapp, browser) roles
     * @param tasResolver
     * @param machine
     */
    protected void addClientRoles(ITasResolver tasResolver, ITestbedMachine machine) {
       
        SeleniumGridNodeRole nodeRole =
            new SeleniumGridNodeRole.Builder(NODE_ROLE_ID, tasResolver)
                .nodeConfiguration(createConfiguration(tasResolver))
                .qResXResolution("1920")
                .qResYResolution("1080")
                .chromeDriver(SeleniumChromeDriver.V2_29_B32)
                .build();

        SeleniumGridHubRole hubRole =
            new SeleniumGridHubRole.Builder(HUB_ROLE_ID, tasResolver)
                .addNodeRole(nodeRole).build();

        ClientDeployRole clientRole =
            new ClientDeployRole.Builder(LOAD_ROLE_ID, tasResolver)
            .shouldDeployJmeter(shouldDeployJmeter)
            .shouldDeployStressapp(shouldDeployStressapp)
            .browserInstallWait(180)
            .build();

        machine.addRole(hubRole, nodeRole, clientRole);
        nodeRole.before(hubRole);
        hubRole.before(clientRole);
        testbed.addProperty("selenium.webdriverURL",
            "http://" + tasResolver.getHostnameById(HUB_ROLE_ID) + ":4444/wd/hub");
    }

    private NodeConfiguration createConfiguration(ITasResolver tasResolver) {

        List<NodeCapability> capabilities = new ArrayList<NodeCapability>();

        NodeCapability chromeCapability =
            new NodeCapability.Builder().browserType(BrowserType.CHROME)
                .platform(NodePlatform.WINDOWS).maxInstances(8).build();
        capabilities.add(chromeCapability);

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
}
