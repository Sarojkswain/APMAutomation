package com.ca.apm.systemtest.fld.testbed.devel;

import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.role.ATCUISetLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.seleniumgrid.*;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.*;

public class FLDSeleniumATTestbedProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {

    public static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_17;
    
    private static final String WEBAPPS_EXTENSIONS = "/webapps/extensions/fldMetrics/";
    public static final String AT_LOAD_01_MACHINE_ID = "at01";
    public static final String AT_LOAD_02_MACHINE_ID = "at02";
    public static final String AT_LOAD_03_MACHINE_ID = "at03";
    public static final String[] AT_LOAD_MACHINE_IDS = {AT_LOAD_01_MACHINE_ID, AT_LOAD_02_MACHINE_ID,
                                                        AT_LOAD_03_MACHINE_ID};
    
    private ITestbedMachine[] atLoadMachines;
    private ITestbedMachine seleniumHubMachine;

    private static final Logger LOGGER = LoggerFactory.getLogger(FLDSeleniumATTestbedProvider.class);
    
    private String webviewHost;
    private String webviewPort;

    public FLDSeleniumATTestbedProvider(String webviewHost, String webviewPort) {
        this.webviewHost = webviewHost;
        this.webviewPort = webviewPort;
    }
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        atLoadMachines = new TestbedMachine[AT_LOAD_MACHINE_IDS.length];
        for (int i = 0; i < AT_LOAD_MACHINE_IDS.length; i++) {
            String machineId = AT_LOAD_MACHINE_IDS[i];
            atLoadMachines[i] = new TestbedMachine.Builder(machineId)
                    .platform(Platform.WINDOWS)
                    .templateId(ITestbedMachine.TEMPLATE_W64)
                    .bitness(Bitness.b64).build();
        }

        seleniumHubMachine = new TestbedMachine
                .Builder(SELENIUM_ATCUI_HUB_MACHINE_ID)
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .build();
        return Arrays.asList((ITestbedMachine[]) ArrayUtils.add(atLoadMachines, seleniumHubMachine));
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        String hubHost = tasResolver.getHostnameById(SELENIUM_ATCUI_HUB_ROLE_ID);

        ArrayList<SeleniumGridNodeRole> seleniumRolesList = new ArrayList<>();

        for (int i = 0; i < AT_LOAD_MACHINE_IDS.length; i++) {
            String machineId = AT_LOAD_MACHINE_IDS[i];
            ITestbedMachine machine = atLoadMachines[i];

            JavaRole javaRole =
                    (new JavaRole.Builder("javaRole_" + machineId, tasResolver))
                            .dir(getJavaDir(JAVA_VERSION)).version(JAVA_VERSION).build();

            SeleniumGridNodeRole seleniumNodeRole
                    = createSeleniumNodeRole(tasResolver, machineId, hubHost);

            machine.addRole(javaRole, /*tasTestsCoreRole,*/ seleniumNodeRole);

            seleniumRolesList.add(seleniumNodeRole);
        }

        createSeleniumHubRole(tasResolver, seleniumRolesList);
        
        createATCUILoadRole(tasResolver);
        
        installNotebookExtension(testbed, tasResolver);
    }

    
    /**
     * Installs a custom notebook extension (F20699)
     * @param testbed
     * @param tasResolver
     */
    private void installNotebookExtension(ITestbed testbed, ITasResolver tasResolver) {
        EmRole agcRole = (EmRole) testbed.getRoleById(FLDConstants.AGC_ROLE_ID);
        
        String installDir = agcRole.getEnvProperties().get("emInstallDir");
        FileModifierFlowContext fileModifierCtx = new FileModifierFlowContext.Builder()
            .resource(installDir + WEBAPPS_EXTENSIONS + "extension.json", "/com/ca/apm/systemtest/fld/atc/extension/fldmetrics/extension.json")
            .resource(installDir + WEBAPPS_EXTENSIONS + "FldMetricsDirective.js", "/com/ca/apm/systemtest/fld/atc/extension/fldmetrics/FldMetricsDirective.js")
            .resource(installDir + WEBAPPS_EXTENSIONS + "FldMetricsTemplate.html", "/com/ca/apm/systemtest/fld/atc/extension/fldmetrics/FldMetricsTemplate.html")
            .build();
        UniversalRole installExtensionRole = new UniversalRole.Builder("nbeRole", tasResolver)
            .flow(fileModifierCtx)
            .build();
        
        testbed.getMachineById(FLDConstants.AGC_MACHINE_ID).addRole(installExtensionRole);
    }
    
    
    private void createATCUILoadRole(ITasResolver tasResolver) {
        
        ATCUISetLoadRole ATCUIRole = new ATCUISetLoadRole.Builder(ATCUI_SET_LOAD_ROLE_ID, tasResolver)
                .setNumberOfBrowsers(3)
                .setPassword("")
                .setUser("admin")
                .setWebviewHost(webviewHost)
                .setWebviewPort(webviewPort)
                .build();
        
        seleniumHubMachine.addRole(ATCUIRole);
    }

    private void createSeleniumHubRole(ITasResolver tasResolver, ArrayList<SeleniumGridNodeRole> seleniumRolesList) {
        SeleniumGridHubRole.Builder hubRoleBuilder =
                new SeleniumGridHubRole.Builder(SELENIUM_ATCUI_HUB_ROLE_ID, tasResolver);
        SeleniumGridHubRole hubRole;

        for (SeleniumGridNodeRole role : seleniumRolesList) {
            hubRoleBuilder.addNodeRole(role);
        }

        hubRole = hubRoleBuilder.build();

        for (SeleniumGridNodeRole role : seleniumRolesList) {
            hubRole.after(role);
        }

        seleniumHubMachine.addRole(hubRole);
    }

    private SeleniumGridNodeRole createSeleniumNodeRole(ITasResolver tasResolver,
                                                        String machineId, String hubHost) {
        LOGGER.info("Using HUB host {}", hubHost);
        URL hubUrl = null;
        try {
            hubUrl = new URL("http://" + hubHost + ":4444/grid/register/");
        } catch (MalformedURLException ex) {
            LOGGER.error("HUB URL IS malformed", ex);
        }

        NodeCapability firefoxCapability = new NodeCapability.Builder()
                .browserType(BrowserType.FIREFOX)
                .platform(NodePlatform.WINDOWS)
                .maxInstances(1)
                .build();

        NodeCapability internetExplorerCapability = new NodeCapability.Builder()
                .browserType(BrowserType.INTERNET_EXPLORER)
                .platform(NodePlatform.WINDOWS)
                .maxInstances(1)
                .build();

        NodeCapability chromeCapability = new NodeCapability.Builder()
                .browserType(BrowserType.CHROME)
                .platform(NodePlatform.WINDOWS)
                .maxInstances(1)
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
                .build();
    }

}
