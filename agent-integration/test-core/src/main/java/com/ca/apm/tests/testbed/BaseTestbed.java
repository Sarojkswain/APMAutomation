package com.ca.apm.tests.testbed;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.StartEMRole;
import com.ca.apm.tests.role.UpdateMgmtModuleRole;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
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
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * On-Prem Base test bed
 * 
 * @author kurma05
 */
@TestBedDefinition
public class BaseTestbed implements ITestbedFactory {

    public static final String EM_ROLE_ID            = "emRole";
    public static final String UPDATE_EM_MM_ROLE_ID  = "updateEmMM";
    public static final String START_EM_ROLE_ID      = "startEMRole";
    public static final String MACHINE1_ID           = "machine1";
    public static final String MACHINE2_ID           = "machine2";
    protected static boolean updateEmMM              = false;
    protected static boolean setupJarvis             = false;
    private static final String JARVIS_HOST          = "fldcoll12t";
    public static final String HUB_ROLE_ID           = "seleniumHubRole";
    public static final String NODE_ROLE_ID          = "seleniumNodeRole";
    public static final String LOAD_ROLE_ID          = "seleniumLoadRole";
    public static final String WIN_DEPLOY_BASE       = TasBuilder.WIN_SOFTWARE_LOC;
    protected ITestbed testbed                       = null;
    protected boolean shouldDeployJmeter             = false;
    protected boolean shouldDeployStressapp          = false;
    public static final String ARTIFACTS_VERSION_PROPERTY_KEY = "artifactsVersionProperty";
    public static final String JMETER_SCRIPTS_ROLE_ID = "jmeterScriptsRole"; 
    
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
    
    protected void addEMWinRole(ITasResolver tasResolver, 
                                TestbedMachine machine) {
        addEMRole(tasResolver, machine,
            new EmRole.Builder(EM_ROLE_ID, tasResolver));
    }
    
    protected void addEMLinuxRole(ITasResolver tasResolver, 
                                  TestbedMachine machine) {
        
        if(updateEmMM) {
            addEMUpdatedMMLinuxRole(tasResolver, machine);
        }
        else {
            addEMRole(tasResolver, machine,
                new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver));
        }
    }
    
    private void addEMRole(ITasResolver tasResolver, 
                           TestbedMachine machine,
                           EmRole.Builder builder) {
            
        builder
            .version(tasResolver.getDefaultVersion())
            .configProperty("introscope.changeDetector.disable", "false")
            .configProperty("introscope.enterprisemanager.performance.compressed", "false")
            .configProperty("log4j.logger.Manager", "DEBUG, console,logfile")
            .configProperty("transport.buffer.input.maxNum", "1500")
            .configProperty("transport.buffer.input.maxNumNio", "6000")
            .configProperty("introscope.enterprisemanager.threaddump.storage.max.disk.usage", "50")
            .configProperty("enable.default.BusinessTransaction", "false")
            .configProperty("introscope.apmserver.teamcenter.saas", "false")
            .configProperty("introscope.apmserver.ui.configuration.name.1", "LIVE_MAP_CHANGE_RELOAD_TIME")
            .configProperty("introscope.apmserver.ui.configuration.value.1", "0")
            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "WebView", "Database"));
        
        if(setupJarvis) {
            builder
                .configProperty("ca.apm.ttstore.jarvis.es.url", "http://" + JARVIS_HOST + ":9200")
                .configProperty("ca.apm.ttstore.jarvis.ingestion.url", "http://" + JARVIS_HOST + ":8080/ingestion")
                .configProperty("ca.apm.ttstore.jarvis.onboarding.url", "http://" + JARVIS_HOST + ":8080/onboarding")
                .configProperty("cohortId", tasResolver.getHostnameById(EM_ROLE_ID) + "_" + System.currentTimeMillis())
                .configProperty("com.ca.apm.ttstore", "jarvis");
        }
      
        EmRole emRole = builder.build();
        machine.addRole(emRole);
    }
    
    protected void addEMUpdatedMMLinuxRole(ITasResolver tasResolver, 
                                           TestbedMachine machine) {
        
         EmRole emRole = new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver)
            .version(tasResolver.getDefaultVersion())
            .configProperty("introscope.changeDetector.disable", "false")
            .configProperty("introscope.enterprisemanager.performance.compressed", "false")
            .configProperty("log4j.logger.Manager", "DEBUG, console,logfile")
            .configProperty("transport.buffer.input.maxNum", "1500")
            .configProperty("transport.buffer.input.maxNumNio", "6000")
            .configProperty("introscope.enterprisemanager.threaddump.storage.max.disk.usage", "50")
            .configProperty("enable.default.BusinessTransaction", "false")
            .configProperty("introscope.apmserver.teamcenter.saas", "false")
            .configProperty("introscope.apmserver.ui.configuration.name.1", "LIVE_MAP_CHANGE_RELOAD_TIME")
            .configProperty("introscope.apmserver.ui.configuration.value.1", "0")
            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "WebView", "Database"))
            .nostartEM()
            .nostartWV()
            .build(); 
         
         //update IA MM to enforce alerts
         HashMap<String,String> iaReplacePairs = new HashMap<String,String>();
         //host alerts 'CPU Core Util Percent' & 'CPU Total Util Percent'
         iaReplacePairs.put("<CautionTargetValue>90</CautionTargetValue>","<CautionTargetValue>1</CautionTargetValue>");
         iaReplacePairs.put("<DangerTargetValue>97</DangerTargetValue>", "<DangerTargetValue>2</DangerTargetValue>");
         iaReplacePairs.put("<CautionMinNumPerPeriod>6</CautionMinNumPerPeriod>", "<CautionMinNumPerPeriod>1</CautionMinNumPerPeriod>");
         iaReplacePairs.put("<CautionAlertPeriod>8</CautionAlertPeriod>", "<CautionAlertPeriod>1</CautionAlertPeriod>");
         iaReplacePairs.put("<DangerMinNumPerPeriod>6</DangerMinNumPerPeriod>", "<DangerMinNumPerPeriod>1</DangerMinNumPerPeriod>");
         iaReplacePairs.put("<DangerAlertPeriod>8</DangerAlertPeriod>", "<DangerAlertPeriod>1</DangerAlertPeriod>");      
         //docker alert 'Memory Utilization'
         iaReplacePairs.put("<CautionTargetValue>60</CautionTargetValue>", "<CautionTargetValue>1</CautionTargetValue>");
         iaReplacePairs.put("<DangerTargetValue>80</DangerTargetValue>", "<DangerTargetValue>2</DangerTargetValue>");
         iaReplacePairs.put("<CautionMinNumPerPeriod>3</CautionMinNumPerPeriod>", "<CautionMinNumPerPeriod>1</CautionMinNumPerPeriod>");
         iaReplacePairs.put("<CautionMinNumPerPeriod>4</CautionMinNumPerPeriod>", "<CautionMinNumPerPeriod>1</CautionMinNumPerPeriod>");
         iaReplacePairs.put("<CautionAlertPeriod>4</CautionAlertPeriod>", "<CautionAlertPeriod>1</CautionAlertPeriod>");
         iaReplacePairs.put("<DangerMinNumPerPeriod>4</DangerMinNumPerPeriod>", "<DangerMinNumPerPeriod>1</DangerMinNumPerPeriod>");
         iaReplacePairs.put("<DangerAlertPeriod>4</DangerAlertPeriod>", "<DangerAlertPeriod>1</DangerAlertPeriod>");         
         
         UpdateMgmtModuleRole updateIAMMRole = new UpdateMgmtModuleRole.LinuxBuilder(UPDATE_EM_MM_ROLE_ID + "_IA", tasResolver)
             .emHomeDir(emRole.getDeployEmFlowContext().getInstallDir())
             .mmJarFile(UpdateMgmtModuleRole.IA_MM_JAR_RELATED_PATH)
             .unpackDir(TasBuilder.LINUX_SOFTWARE_LOC + "/mmtemp_ia")
             .replacePairs(iaReplacePairs)
             .build();
         
         //update default MM to enforce alerts
         HashMap<String,String> defaultReplacePairs = new HashMap<String,String>();
         defaultReplacePairs.put("<DangerMinNumPerPeriod>8</DangerMinNumPerPeriod>","<DangerMinNumPerPeriod>1</DangerMinNumPerPeriod>");
         defaultReplacePairs.put("<DangerAlertPeriod>10</DangerAlertPeriod>", "<DangerAlertPeriod>1</DangerAlertPeriod>");      
         //agent alert 'CPU Utilization'
         defaultReplacePairs.put("<CautionTargetValue>60</CautionTargetValue>", "<CautionTargetValue>1</CautionTargetValue>");
         defaultReplacePairs.put("<DangerTargetValue>80</DangerTargetValue>", "<DangerTargetValue>2</DangerTargetValue>");
         defaultReplacePairs.put("<CautionMinNumPerPeriod>4</CautionMinNumPerPeriod>", "<CautionMinNumPerPeriod>1</CautionMinNumPerPeriod>");
         defaultReplacePairs.put("<CautionAlertPeriod>4</CautionAlertPeriod>", "<CautionAlertPeriod>1</CautionAlertPeriod>");
         defaultReplacePairs.put("<DangerMinNumPerPeriod>4</DangerMinNumPerPeriod>", "<DangerMinNumPerPeriod>1</DangerMinNumPerPeriod>");
         defaultReplacePairs.put("<DangerAlertPeriod>4</DangerAlertPeriod>", "<DangerAlertPeriod>1</DangerAlertPeriod>");         
               
         UpdateMgmtModuleRole updateDefaultMMRole = new UpdateMgmtModuleRole.LinuxBuilder(UPDATE_EM_MM_ROLE_ID + "_Default", tasResolver)
             .emHomeDir(emRole.getDeployEmFlowContext().getInstallDir())
             .mmJarFile(UpdateMgmtModuleRole.DEFAULT_MM_JAR_RELATED_PATH)
             .unpackDir(TasBuilder.LINUX_SOFTWARE_LOC + "/mmtemp_default")
             .replacePairs(defaultReplacePairs)
             .build();
         
         //start em
         StartEMRole startEMRole = new StartEMRole.LinuxBuilder(START_EM_ROLE_ID, tasResolver)
             .emHomeDir(emRole.getDeployEmFlowContext().getInstallDir())
             .build();
         
         emRole.before(updateIAMMRole, updateDefaultMMRole, startEMRole);
         startEMRole.after(updateIAMMRole, updateDefaultMMRole);
         machine.addRole(emRole, updateIAMMRole, updateDefaultMMRole, startEMRole);
    }
}