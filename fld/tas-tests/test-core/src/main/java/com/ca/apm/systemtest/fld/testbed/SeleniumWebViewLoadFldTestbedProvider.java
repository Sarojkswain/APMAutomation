package com.ca.apm.systemtest.fld.testbed;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJavaDir;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumStandaloneServer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public class SeleniumWebViewLoadFldTestbedProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {

    public static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_18;

    private ITestbedMachine[] wvLoadMachines;
    private ITestbedMachine seleniumHubMachine;

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumWebViewLoadFldTestbedProvider.class);

    @Override
    public Collection<ITestbedMachine> initMachines() {
        wvLoadMachines = new TestbedMachine[WEBVIEW_LOAD_MACHINE_IDS.length];
        for (int i = 0; i < WEBVIEW_LOAD_MACHINE_IDS.length; i++) {
            String machineId = WEBVIEW_LOAD_MACHINE_IDS[i];
            wvLoadMachines[i] = new TestbedMachine.Builder(machineId)
                    .platform(Platform.WINDOWS)
                    .templateId(ITestbedMachine.TEMPLATE_W64)
                    .bitness(Bitness.b64).build();
        }

        seleniumHubMachine = wvLoadMachines[0];
        return Arrays.asList(wvLoadMachines);
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        // We should not need an extra machine, install it on machine #1
        
        String hubHost = tasResolver.getHostnameById(WEBVIEW_LOAD_01_MACHINE_ID + "_seleniumNodeRole");
        hubHost = tasResolver.getHostnameById(SELENIUM_HUB_ROLE_ID);
        LOGGER.info("Using selenium grid hub: " + hubHost);

        ArrayList<SeleniumGridNodeRole> seleinumRolesList = new ArrayList<>();

        seleinumRolesList.add(initSeleniumNodeMachine(tasResolver, hubHost, 0, 5, 5, 0));
        seleinumRolesList.add(initSeleniumNodeMachine(tasResolver, hubHost, 1, 5, 5, 0));
        seleinumRolesList.add(initSeleniumNodeMachine(tasResolver, hubHost, 2, 5, 0, 5));
        seleinumRolesList.add(initSeleniumNodeMachine(tasResolver, hubHost, 3, 5, 0, 5));
        seleinumRolesList.add(initSeleniumNodeMachine(tasResolver, hubHost, 4, 0, 5, 5));

        createSeleniumHubRole(tasResolver, seleinumRolesList);
    }
    
    
    private SeleniumGridNodeRole initSeleniumNodeMachine(ITasResolver tasResolver, 
            String hubHost, int idx, int maxFF, int maxIE, int maxChrome) {
        String machineId = WEBVIEW_LOAD_MACHINE_IDS[idx];
        ITestbedMachine machine = wvLoadMachines[idx];

        JavaRole javaRole = new JavaRole.Builder("javaRole_" + machineId, tasResolver)
            .dir(getJavaDir(JAVA_VERSION)).version(JAVA_VERSION).build();

        SeleniumGridNodeRole seleniumNodeRole
                = createSeleniumNodeRole(tasResolver, machineId, hubHost, maxFF, maxIE, maxChrome);

        machine.addRole(javaRole, seleniumNodeRole);
        
        return seleniumNodeRole;
    }

    private void createSeleniumHubRole(ITasResolver tasResolver, ArrayList<SeleniumGridNodeRole> seleinumRolesList) {
        SeleniumGridHubRole.Builder hubRoleBuilder =
                new SeleniumGridHubRole.Builder(SELENIUM_HUB_ROLE_ID, tasResolver);
        SeleniumGridHubRole hubRole;

        for (SeleniumGridNodeRole role : seleinumRolesList) {
            hubRoleBuilder.addNodeRole(role);
        }

        hubRole = hubRoleBuilder.standaloneServerVersion(SeleniumStandaloneServer.V3_4_0).build();

        for (SeleniumGridNodeRole role : seleinumRolesList) {
            hubRole.after(role);
        }

        seleniumHubMachine.addRole(hubRole);
    }

    private SeleniumGridNodeRole createSeleniumNodeRole(ITasResolver tasResolver,
            String machineId, String hubHost, int maxFF, int maxIE, int maxChrome) {
        LOGGER.info("Using HUB host" + hubHost);
        URL hubUrl = null;
        try {
            hubUrl = new URL("http://" + hubHost + ":4444/grid/register/");
        } catch (MalformedURLException ex) {
            LOGGER.error("HUB URL IS malformed", ex);
        }

        NodeCapability firefoxCapability = new NodeCapability.Builder()
                .browserType(BrowserType.FIREFOX)
                .platform(NodePlatform.WINDOWS)
                .maxInstances(maxFF)
                .build();

        NodeCapability internetExplorerCapability = new NodeCapability.Builder()
                .browserType(BrowserType.INTERNET_EXPLORER)
                .platform(NodePlatform.WINDOWS)
                .maxInstances(maxIE)
                .build();

        NodeCapability chromeCapability = new NodeCapability.Builder()
                .browserType(BrowserType.CHROME)
                .platform(NodePlatform.WINDOWS)
                .maxInstances(maxChrome)
                .build();

        // Define the whole node configuration
        NodeConfiguration nodeConfiguration = new NodeConfiguration.Builder()
                .addCapability(firefoxCapability)
                .addCapability(internetExplorerCapability)
                .addCapability(chromeCapability)
                .maxSession(100)
                .hub(hubUrl)
                .build();

        return new SeleniumGridNodeRole
                .Builder(machineId + "_seleniumNodeRole", tasResolver)
                .nodeConfiguration(nodeConfiguration)
                .chromeDriver(SeleniumChromeDriver.V2_29_B32)
                .standaloneServerVersion(SeleniumStandaloneServer.V3_4_0)
                .build();
    }

}
