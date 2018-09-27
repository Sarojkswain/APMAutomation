package com.ca.apm.tests.test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.test.utils.IABomUtil;
import com.ca.apm.tests.testbed.BaseTestbed;
import com.ca.tas.builder.TasBuilder;

/**
 * Test base class for Infrastructure agent
 * @author kurma05
 *
 */
public class InfrastructureBaseTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureBaseTest.class);
    protected static final String DOCKER_TOMCAT_PORT = "8080";
    protected static boolean agentLessContainerConfigured = false;
    protected static final String IA_HOME_LINUX = "/opt/apmia";
    protected static final String IA_HOME_WIN = TasBuilder.WIN_SOFTWARE_LOC + "\\apmia";  
    public static final String DEFAULT_DOCKER_MONITOR_IMAGE = "caapm/dockermonitor:latest";
    
    protected void installLinuxAgentOnPrem(String host, String machineId,
                                           boolean shouldEnableDockerMonitor) {
        
        try {    
            //update Extensions.profile
            String property = "introscope.agent.extensions.bundles.load";
            String value = "=HostMonitor/g";
            
            //standalone docker setup
            if(shouldEnableDockerMonitor) {
                value = "=DockerMonitor\\,HostMonitor/g";
            }
            
            String file = IA_HOME_LINUX + "/extensions/Extensions.profile";
            Assert.assertTrue(runRemoteCommand(machineId, "sed", "", 
                Arrays.asList("-i", "s/" + property + "=/" + property + value, file)),
                "Updating Extensions.profile didn't succeed.");
            
            //backup dir
            Assert.assertTrue(
                runRemoteCommand(machineId, "cp", "", 
                    Arrays.asList("-r", IA_HOME_LINUX, IA_HOME_LINUX + ".backup")),
                "Infrastructure agent dir backup didn't succeed.");
            
            //install & start ia                                                                                              
            Assert.assertTrue(
                runRemoteCommand(machineId, IABomUtil.IA_INSTALL_SCRIPT_UNIX, IA_HOME_LINUX, Arrays.asList("install")),
                "Infrastructure agent installation/startup didn't succeed.");

            //start tomcat container in case of standalone docker setup
            if(shouldEnableDockerMonitor) {
                installTomcatDockerAgent(machineId, null, host);
                restartDockerContainers(machineId, new String[]{"tomcat_container_" + host});
            }
        }
        catch (Exception e) {
            Assert.fail("Error occurred during agent install: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    private void restartDockerContainers(String machineId, String[] containers) {
        
        //sometimes network issues happen on tas vm with docker;
        //restarting daemon because of that:
        Assert.assertTrue(
            runRemoteCommand(machineId, "service", "", Arrays.asList(
                "docker","restart")), "Restarting docker daemon didn't succeed."); 
        
        for(String container: containers) {
            Assert.assertTrue(
                runRemoteCommand(machineId, "docker", "", Arrays.asList(
                "start",container)), "Restarting " + container + " container didn't succeed.");
        }
    }
    
    /**
     * Install docker monitor via docker-compose
     * 
     * @param host agent host name
     * @param machineId tas machine id
     */
    protected void installDockerMonitor(String host, String machineId) {
       
        //create yml file
        ArrayList<String> lines = new ArrayList<String>(); 
        lines.add("version: \"3\"");
        lines.add("services:");
        lines.add("  dockermonitor:");
        lines.add("    image: " + dockerImage);
        lines.add("    environment:");
        lines.add("      - agentManager_url_1=" + host + ":5001");
        lines.add("      - containerflow=enabled");
        lines.add("      - interval=30");
        lines.add("      - type=Docker");
        lines.add("    pid: host");
        lines.add("    command: [\"sh\",\"install.sh\"]");
        lines.add("    deploy:");
        lines.add("      mode: global");
        lines.add("    volumes:");
        lines.add("      - /var/run/docker.sock:/var/run/docker.sock");
        lines.add("      - /:/host/");
        
        FileModifierFlowContext dockerFileContext = new FileModifierFlowContext.Builder()
            .create("/opt/docker_monitor_install.yml", lines)
            .build();          
        runFlowByMachineId(machineId, FileModifierFlow.class, dockerFileContext);
        
        //start IA with Docker & Host monitor
        Assert.assertTrue(
            runRemoteCommand(machineId, "docker-compose", "", Arrays.asList(
                "-f","/opt/docker_monitor_install.yml","up","-d")),
            "Starting docker monitor didn't succeed.");   
        
        //start tomcat container
        installTomcatDockerAgent(machineId, null, host);
        restartDockerContainers(machineId, new String[]{"tomcat_container_" + host, "dockermonitor"});
    }

    protected void reInstallLinuxAgentOnPrem(String host, String machineId, 
                                             String installOption) throws Exception {
        
        //uninstall
        Assert.assertTrue(
            runRemoteCommand(machineId, IA_HOME_LINUX + "/" + IABomUtil.IA_INSTALL_SCRIPT_UNIX, 
                "", Arrays.asList("uninstall")),
            "Infrastructure agent unistall didn't succeed.");
        
        //restore backup installer
        Assert.assertTrue(
            runRemoteCommand(machineId, "rm", "", 
                Arrays.asList("-rf", IA_HOME_LINUX)),
            "Infrastructure agent removing install directory didn't succeed.");            
        Assert.assertTrue(
            runRemoteCommand(machineId, "cp", "", 
                Arrays.asList("-r", IA_HOME_LINUX + ".backup", IA_HOME_LINUX)),
            "Infrastructure agent backup restore didn't succeed.");
        
        //install & start ia                                                                                              
        Assert.assertTrue(
            runRemoteCommand(machineId, IA_HOME_LINUX + "/" + IABomUtil.IA_INSTALL_SCRIPT_UNIX, 
                "", Arrays.asList(installOption)),
            "Infrastructure agent installation/startup didn't succeed.");      
    }
    
    protected void installInfrastructureAgent(String machineId, String installerPath) {

        logger.info("Installing Infrastructure Agent...");
        
        //untar packages
        Assert.assertTrue(
            runRemoteCommand(machineId, "tar", "", Arrays.asList("-xf", installerPath, "-C", "/opt")),
            "Unpacking " + installerPath + " didn't succeed.");
        
        //install & start both sysedge & uma in one script                                                                                               
        Assert.assertTrue(
            runRemoteCommand(machineId, IABomUtil.IA_INSTALL_SCRIPT_UNIX, "/opt/" + 
                IABomUtil.IA_PARENT_DIR_SAAS + "/", Arrays.asList("install")),
            "Infrastructure agent installation/startup didn't succeed.");
    }
    
    protected void installTomcatDockerAgent(String machineId, String installerPath, String agentHost) {
    
        logger.info("Installing docker container with tomcat agent...");
        
        //create Docker file
        ArrayList<String> lines = new ArrayList<String>(); 
        lines.add("FROM tomcat:8.5");
        lines.add("ADD ./wily /usr/local/tomcat/wily");
        
        String javaOpts = "ENV JAVA_OPTS=\"-javaagent:/usr/local/tomcat/wily/Agent.jar "
                + "-DagentManager.url.1=" + agentHost + ":5001 -Dcom.wily.introscope.agentProfile=/usr/local/tomcat/wily/core/config/IntroscopeAgent.profile\"";
        lines.add(javaOpts);
       
        FileModifierFlowContext dockerFileContext = new FileModifierFlowContext.Builder()
            .create("/opt/tomcat/Dockerfile-agent", lines)
            .build();          
        runFlowByMachineId(machineId, FileModifierFlow.class, dockerFileContext);  
        
        //build docker image
        Assert.assertTrue(
            runRemoteCommand(machineId, "docker", "", Arrays.asList(
                "build","-t","tomcat_agent_image","-f","/opt/tomcat/Dockerfile-agent","/opt/tomcat")),
            "Building docker image didn't succeed.");     
        
        //start docker container
        Assert.assertTrue(
            runRemoteCommand(machineId, "docker", "", Arrays.asList(
                "run","--name","tomcat_container_" + agentHost,"-d","-p",DOCKER_TOMCAT_PORT + ":" + DOCKER_TOMCAT_PORT,"-h","tomcat_host_" + agentHost,"tomcat_agent_image")),
            "Starting docker container didn't succeed.");   
    }
    
    protected void validateIAHostData(String host) {
        validateIAHostData(host, false);
    }
   
    protected void validateIAHostData(String host, boolean shouldValidateAlerts) {

        String[] data =
            new String[] {"HOST", "TotalUserPercent", "TotalSystemPercent", "MemInUsePercent",
                    "SwapInUsePercent", "AttemptFailures", "ResetConnections", "Size", "Used", "Hostname", host};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + " doesn't exist");
        }
        
        if(shouldValidateAlerts) {
            logger.info("Validating danger alerts for HOST component view...");            
            String alertStatusXpath = "//*[td/span[text()[contains(.,'Alert Status')]] and td/span[text()[contains(.,'Danger')]]]";
            String alertMetricXpath = "//div[img[@ng-src='res/APM-Icons_Danger.svg'] and a[text()[contains(.,'CPU Core Util Percent')]]]";
            Assert.assertTrue(mapHelper.attemptToFindElement(alertStatusXpath, 5000, 20), 
                "Expected alert status doesn't exist.");
            Assert.assertTrue(mapHelper.attemptToFindElement(alertMetricXpath, 5000, 20), 
                "Expected metric doesn't exist.");
        }
    }
        
    protected void validateIADockerData(String machineId, 
                                        boolean isMainNode, String nodeName) throws Exception {
        validateIADockerData(machineId, isMainNode, nodeName, false);
    }
    
    protected void validateIADockerData(String machineId, 
                                        boolean isMainNode, 
                                        String nodeName,
                                        boolean shouldValidateAlerts) throws Exception {

        String host = envProperties.getMachinePropertiesById(machineId).getProperty("hostname");
        
        String[] data =
            new String[] {"DOCKER", host, "CPU % (rounded)", "CPU Count", "Dropped Packets during Receive", 
                          "Errors Received", "Errors Sent", "docker_host", "docker_node", "docker_id", 
                          "docker_image", "docker_name", "docker_state"};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + " doesn't exist");
        }
        
        //check docker image & name attributes
        if(isMainNode) {
            //check docker images
            Assert.assertTrue(mapHelper.clickAttribute("docker_image"), "Unable to click on 'docker_image'");
            Assert.assertTrue(mapHelper.isMetricPresent("tomcat_agent_image"), 
                "Attribute value 'tomcat_agent_image' doesn't exist");        
            if(agentLessContainerConfigured) {
                Assert.assertTrue(mapHelper.isMetricPresent("containerflow_ca-agent"), 
                    "Attribute value 'containerflow_ca-agent' doesn't exist");
            }
                
            //check docker names
            Assert.assertTrue(mapHelper.clickAttribute("docker_name"), "Unable to click on 'docker_name'");
            Assert.assertTrue(mapHelper.isMetricPresent("tomcat_container"), 
                "Attribute value 'tomcat_container' doesn't exist");
            if(agentLessContainerConfigured) {
                Assert.assertTrue(mapHelper.isMetricPresent("containerflow-agent"), 
                    "Attribute value 'containerflow-agent' doesn't exist");
            }   
        }
        
        Assert.assertTrue(mapHelper.isMetricPresent(nodeName), 
            "Attribute " + nodeName + " doesn't exist");
        
        if(shouldValidateAlerts) {
            logger.info("Validating danger alerts for DOCKER component view...");            
            String alertStatusXpath = "//*[td/span[text()[contains(.,'Alert Status')]] and td/span[text()[contains(.,'Danger')]]]";
            String alertMetricXpath = "//div[img[@ng-src='res/APM-Icons_Danger.svg'] and a[text()[contains(.,'Memory Utilization')]]]";
            Assert.assertTrue(mapHelper.attemptToFindElement(alertStatusXpath, 5000, 20), 
                "Expected alert status doesn't exist.");
            Assert.assertTrue(mapHelper.attemptToFindElement(alertMetricXpath, 5000, 20), 
                "Expected metric doesn't exist.");
        }
    }
    
    protected void validateInfraLayerDockerHost(String agentHost) throws Exception {
        validateInfraLayerDockerHost(agentHost, false);
    }
    
    protected void validateInfraLayerDockerHost(String agentHost,
                                                boolean shouldValidateAlerts) throws Exception {
        
        //find ATC node           
        Assert.assertNotNull(mapHelper, "MapPage wasn't instantiated.");
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view.");
        mapHelper.clickLiveTimeRange();
        Assert.assertTrue(mapHelper.clickNoPerspective(), 
            "Unable to select 'No Perspective' perspective. Check map loading wasn't hanging.");
        Assert.assertTrue(mapHelper.clickInfrastructureLayer(), "Unable to select Infrastructure layer.");    
         
        //validate main node
        Assert.assertTrue(isDockerTabVisible(agentHost, ui.getDriver()), 
            "Docker tab doesn't exist for node " + agentHost);
        validateIADockerData(BaseTestbed.MACHINE2_ID, true, agentHost, shouldValidateAlerts);
        
        //validate agent & host attributes
        Assert.assertTrue(mapHelper.clickComponentViewTab("Host"), 
            "Host tab doesn't exist for node " + agentHost);
        validateIAHostData(agentHost, shouldValidateAlerts);
        Assert.assertTrue(this.clickAgentTab(), "Agent tab is missing.");
        validateAgentTabData(shouldValidateAlerts);     
        
        //validate single node
        String nodeName = "tomcat_container_" + agentHost;
        Assert.assertTrue(isDockerTabVisible(nodeName, ui.getDriver()),
            "Either 'Docker' tab doesn't exist for node " + nodeName + " or node didn't show up.");
        validateIADockerData(BaseTestbed.MACHINE2_ID, false, nodeName, shouldValidateAlerts);
    }
    
    protected void validateInfraLayerHost(String host) throws Exception {
        
        //find ATC node           
        Assert.assertNotNull(mapHelper, "MapPage wasn't instantiated.");
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view.");
        mapHelper.clickLiveTimeRange();
        Assert.assertTrue(mapHelper.clickNoPerspective(), 
            "Unable to select 'No Perspective' perspective. Check map loading wasn't hanging.");
        Assert.assertTrue(mapHelper.clickInfrastructureLayer(), "Unable to select Infrastructure layer.");    
         
        //validate host attributes
        Assert.assertTrue(waitForComponentViewTab(host, "Host"), 
            "Host tab doesn't exist for node '" + host + "' or node didn't show up.");
        validateIAHostData(host);
    }
    
    private boolean clickAgentTab() throws InterruptedException {
        
        long startTime = System.currentTimeMillis();
        long maxWaiting = 1200000;//20 min
        
        while((System.currentTimeMillis() - startTime) < maxWaiting) {
            logger.info("Waiting for 'Agent' tab to appear... Max {} ms.", maxWaiting);
            
            if(mapHelper.clickComponentViewTab("Agent")) {
                return true;
            }
            mapHelper.clickReloadMap();
            Thread.sleep(20000);
        }
        
        return false;
    }
    
    protected void validateAppLayerDockerHost(String agentHost) throws Exception {
        validateAppLayerDockerHost(agentHost, false);
    }
    
    protected void validateAppLayerDockerHost(String agentHost,
                                              boolean shouldValidateAlerts) throws Exception {
        
        //send sample app requests
        String url = "http://" + agentHost + ":" + DOCKER_TOMCAT_PORT + "/examples/jsp/jsp2/el/basic-arithmetic.jsp";
        sendHttpRequests(url, 50);
        
        //add map filters 
        Assert.assertNotNull(mapHelper, "MapPage wasn't instantiated.");
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view."); 
        if (!containsIgnoreCase(instance, "onprem")) {
            Assert.assertTrue(mapHelper.clickApplicationLayer(), "Unable to select 'Application Layer'");
        }
        Assert.assertTrue(mapHelper.clickNoPerspective(), 
            "Unable to select 'No Perspective' perspective. Check map loading wasn't hanging.");
        mapHelper.clickLiveTimeRange();
        
        String host = "tomcat_host_" + agentHost;
        if (!containsIgnoreCase(instance, "onprem")) {
            Assert.assertTrue(mapHelper.addNewMapFilter("Hostname", new String[]{host}), "Failed to add host filter");  
        }
        //validate docker attributes        
        Assert.assertTrue(isDockerTabVisible("JspServlet", ui.getDriver()), 
            "Either 'Docker' tab doesn't exist for node 'JspServlet' or node didn't show up.");
        validateIADockerData(BaseTestbed.MACHINE2_ID, false, host, shouldValidateAlerts);
        mapHelper.clickComponentViewTab("Host");
        validateIAHostData(agentHost, shouldValidateAlerts);
    }
    
    protected void validateAppLayerHost(String host, String port, 
                                        String[] nodeNames) throws Exception {
        
        //send sample app requests
        String url = "http://" + host + ":" + port + "/examples/jsp/jsp2/el/basic-arithmetic.jsp";
        sendHttpRequests(url, 50);
        
        //add map filters
        Assert.assertNotNull(mapHelper, "MapPage wasn't instantiated.");
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view.");         
        Assert.assertTrue(mapHelper.clickNoPerspective(), 
            "Unable to select 'No Perspective' perspective. Check map loading wasn't hanging.");
        mapHelper.clickLiveTimeRange();
        
        //validate host attributes 
        for(String nodeName: nodeNames) {
            Assert.assertTrue(waitForComponentViewTab(nodeName, "Host"), 
                "Host tab doesn't exist for node '" + nodeName + "' or node didn't show up.");
            validateIAHostData(host);
        }
    }
    
    protected void validateInfraLayerAgent(String host, String port) throws Exception {
        
        //send sample app requests
        String url = "http://" + host + ":" + port + "/examples/jsp/jsp2/el/basic-arithmetic.jsp";
        sendHttpRequests(url, 50);
        
        //open map    
        Assert.assertNotNull(mapHelper, "MapPage wasn't instantiated.");
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view.");
        mapHelper.clickLiveTimeRange();
        Assert.assertTrue(mapHelper.clickNoPerspective(), 
            "Unable to select 'No Perspective' perspective. Check map loading wasn't hanging.");
        Assert.assertTrue(mapHelper.clickInfrastructureLayer(), "Unable to select Infrastructure layer.");    
          
        //validate attributes
        Assert.assertTrue(waitForComponentViewTab(host, "Agent"), 
            "Agent tab doesn't exist for node '" + host + "' or node didn't show up.");
        validateAgentTabData();   
    }
    
    protected void validateDockerHostCorrelation(String host) throws Exception {
     
        //send app requests
        String url = "http://" + host + ":" + DOCKER_TOMCAT_PORT + "/examples/jsp/jsp2/el/basic-arithmetic.jsp";
        sendHttpRequests(url, 20);
        
        //open map view
        Assert.assertNotNull(mapHelper, "MapPage wasn't instantiated.");
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view."); 
        Assert.assertTrue(mapHelper.clickApplicationLayer(), "Unable to select 'Application Layer'");         
        
        createDockerHostPerpective();        
        mapHelper.clickLiveTimeRange();
        
        Assert.assertTrue(mapHelper.addNewMapFilter("Hostname", 
            new String[]{"tomcat_host_" + host}), "Failed to add host filter");        
        
        //expand docker & host nodes
        Assert.assertTrue(mapHelper.expandMapNode(), "Failed to expand docker node."); 
        Assert.assertTrue(mapHelper.expandMapNode(), "Failed to expand host node."); 
        mapHelper.clickFitAll();   
        //validate servlet node
        Assert.assertTrue(isDockerTabVisible("JspServlet", ui.getDriver()), 
            "Either 'Docker' tab doesn't exist for node 'JspServlet' or node didn't show up.");
    }
    
    protected void validateHostCorrelation(String host, String port,
                                           String[] nodeNames) throws Exception {
      
        //send app requests
        String url = "http://" + host + ":" + port + "/examples/jsp/jsp2/el/basic-arithmetic.jsp";
        sendHttpRequests(url, 20);
        
        //open map view
        Assert.assertNotNull(mapHelper, "MapPage wasn't instantiated.");
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view."); 
        Assert.assertTrue(mapHelper.clickApplicationLayer(), "Unable to select 'Application Layer'");        
        
        createDockerHostPerpective();        
        mapHelper.clickLiveTimeRange();
        
        if (!containsIgnoreCase(instance, "onprem")) {
            Assert.assertTrue(mapHelper.addNewMapFilter("Hostname", new String[]{host}), "Failed to add host filter");        
        }
        
        //expand host nodes
        Assert.assertTrue(mapHelper.expandMapNode(), "Failed to expand host node."); 
        mapHelper.clickFitAll();   
        
        for(String nodeName: nodeNames) {
            Assert.assertTrue(waitForComponentViewTab(nodeName, "Host"), 
                "Host tab doesn't exist for node '" + nodeName + "' or node didn't show up.");
        }
    }
    
    private void createDockerHostPerpective() throws InterruptedException {
        
        try {
            Assert.assertTrue(mapHelper.clickPerspective("Docker object, Host object"), 
                "Perspective 'Docker object, Host object' doesn't exist.");
        }
        catch (java.lang.AssertionError error) {
            logger.info("Perspective 'Docker object, Host object' doesn't exist. Creating new one...");
            mapHelper.clickPerspectiveList();
            mapHelper.createDockerHostPerspective();
            Assert.assertTrue(mapHelper.clickPerspective("Docker object, Host object"), 
                "Perspective 'Docker object, Host object' doesn't exist.");
        }
    }
   
    protected boolean isDockerTabVisible(String nodeName, WebDriver driver) throws Exception {
        
        long startTime = System.currentTimeMillis();
        //need wait time (10-15 min) for docker nodes and
        //docker tab to show up (per saas em properties setup)
        long maxWaiting = 900000;

        while((System.currentTimeMillis() - startTime) < maxWaiting) {
            logger.info("Waiting for Node & Docker tab to appear for node {}... Max {} ms.", nodeName, maxWaiting);
            mapHelper.clickReloadMap();
            Thread.sleep(30000);
            
            if(!mapHelper.clickNode(nodeName)) {
                continue;
            }       
            if(mapHelper.clickComponentViewTab("Docker")) {
                return true;
            }
        }
        
        return false;
    }
    
    protected void validatePackageContents(String packagePath, String packageType) throws Exception {
        
        TarArchiveInputStream tarInput = new TarArchiveInputStream(new FileInputStream(packagePath));
        TarArchiveEntry entry;
        logger.info("Contents for tar {}", packagePath);
        
        List<String> actualFiles = new ArrayList<String>();
        List<String> expectedFiles = IABomUtil.getFileList(packageType); 
        Collections.sort(expectedFiles);
            
        while ((entry=tarInput.getNextTarEntry()) != null) {
            String fileName = entry.getName();                    
            if(fileName.startsWith(IABomUtil.IA_PARENT_DIR_SAAS + "/jre") && 
                !fileName.equals(IABomUtil.IA_PARENT_DIR_SAAS + "/jre/bin/java")) {
                logger.debug("Ignoring file {}", fileName);
                continue;
            }
            actualFiles.add(fileName);
        }
        
       Collections.sort(actualFiles);
       
       logger.info("Actual files:");
       for(String sFile:actualFiles) {
           logger.info(sFile + "(AF)");
       }
       
       logger.info("Expected files:");
       for(String sFile:expectedFiles) {
           logger.info(sFile + "(EF)");   
       }
       
       Assert.assertEquals(actualFiles.size(), expectedFiles.size(), 
           "Number of expected files " + expectedFiles.size() + 
           " doesn't match with the actual number " + actualFiles.size());
       
       for (String expectedFile : expectedFiles) {
           boolean foundMatch = false;
           for (String actualFile: actualFiles) {
              if(actualFile.contains(expectedFile)) {
                  foundMatch = true; break;
              }   
           }
           Assert.assertTrue(foundMatch, "Expected file pattern wasn't found: " + expectedFile);
       }
    } 
    
    protected void startTomcatLinux(String machineId, String tomcatHome) {
        
        Assert.assertTrue(runRemoteCommand(machineId, "catalina.sh", 
            tomcatHome + "/bin", Arrays.asList("start")),
                 "Starting Tomcat didn't succeed.");     
    }
    
    protected void validateFrontendProblems(String host) throws Exception {
        
        //simulate http 404 errors
        String url = "http://" + host + ":" + DOCKER_TOMCAT_PORT + "/examples/jsp/jsp2/el/nonexisting.jsp";
        sendHttpRequests(url, 250);
        
        //open experience view card
        homeHelper.clickHomepage();        
        homeHelper.clickCard("Experiences in " + host + ".ca.com components universe");
       
        //validate problems
        Assert.assertTrue(homeHelper.attemptToFindElement(
            "//span[@class='ng-scope'][contains(text(),'CULPRIT')]", 5000), "Failed to find 'CULPRIT' text.");
        Assert.assertTrue(homeHelper.attemptToClick(
            "//span[@class='description ng-binding'][contains(text(),'Internal component')]", 5000), "Failed to click on 'Internal component'");
        Assert.assertTrue(homeHelper.attemptToFindElement(
            "//span[@class='red bold ng-binding'][contains(text(),'Servlet and JSP Examples')]", 5000), "Failed to find 'Servlet and JSP Examples' text.");
        
        //open notebook
        Assert.assertTrue(openAnalysisNotebook(), "Analysis Notebook wasn't updated with IA data.");
        
        //validate host alerts
        Assert.assertTrue(homeHelper.attemptToFindElement(
            "//span[@class='ng-binding'][contains(text(),'" + host + ".ca.com')]", 5000), "Failed to find '" + host + ".ca.com'");
        Assert.assertTrue(homeHelper.attemptToFindElement(
            "//span[@class='ng-binding'][contains(text(),'Alerts')]", 5000), "Failed to find 'Alerts'");  
        //Assert.assertTrue(homeHelper.attemptToFindElement(
        //    "//div[contains(text(),'Memory In Use Percent')]", 5000), "Failed to find 'Memory In Use Percent' alert.");
        Assert.assertTrue(homeHelper.attemptToFindElement(
            "//div[contains(text(),'CPU Total Util Percent')]", 5000), "Failed to find 'CPU Total Util Percent' alert.");
        Assert.assertTrue(homeHelper.attemptToFindElement(
            "//div[contains(text(),'CPU Core Util Percent')]", 5000), "Failed to find 'CPU Core Util Percent' alert.");
        
        //validate docker alerts
        Assert.assertTrue(homeHelper.attemptToFindElement(
            "//span[@class='ng-binding'][contains(text(),'tomcat_container_" + host + "')]", 20000), "Failed to find 'tomcat_container_" + host + "'");
        Assert.assertTrue(homeHelper.attemptToFindElement(
            "//div[contains(text(),'Memory Utilization')]", 5000), "Failed to find 'Memory Utilization' alert.");       
    }
    
    private boolean openAnalysisNotebook() throws Exception {
        
        int attempts = 1;
        
        while(attempts <= 30) {
            logger.info("Waiting for notebook to be updated with IA alerts... Attempt #{}", attempts);            
            Assert.assertTrue(homeHelper.attemptToClick(
                "//span[@class='ng-scope'][contains(text(),'Open an Analysis Notebook')]", 5000), "Failed to click on 'Open an Analysis Notebook'");         
            boolean isBookUpdated = homeHelper.attemptToFindElement(
                "//*[(span[contains(.,'AFFECTED')]) and (*[contains(.,'INFRASTRUCTURE')]) and (*[contains(.,'COMPONENTS')])]",
                5000, 1);
            
            if(isBookUpdated) {
               return true; 
            }
            
            Assert.assertTrue(homeHelper.attemptToClick(
                "//span[@class='ng-scope'][contains(text(),'Close')]", 5000), "Failed to click on 'Close'");
            Thread.sleep(10000);              
            attempts++;        
        }
        
        return false;
    }
}