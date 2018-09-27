package com.ca.apm.saas.test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.saas.pagefactory.DownloadAgentPage;
import com.ca.apm.saas.role.TixChangeRole;
import com.ca.apm.saas.test.utils.IABomUtil;
import com.ca.apm.saas.test.utils.TraceInfo;
import com.ca.apm.saas.test.utils.WebAppTraceDetails;
import com.ca.apm.saas.testbed.SaasNodeJSTestbed;
import com.ca.tas.builder.TasBuilder;

/**
 * Test base class for NodeJS agent
 * @author kurma05
 */
public class NodeJSAgentBaseTest extends SaaSBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(NodeJSAgentBaseTest.class);
    protected static final String IA_HOME_LINUX = "/opt/apmia";
   
    protected void validateAgentsConnected(String agentHost) throws Exception {
        
        //download agent  
        DownloadAgentPage objDownloadAgent = new DownloadAgentPage(ui.getDriver());
     
        DownloadAgentPage.downloadPath = System.getenv("USERPROFILE") + "\\Downloads";
        String packagePath = objDownloadAgent.downloadAgentPackage(
            "Unix", "Node.js_", "nodejs", null);        
        Assert.assertTrue(packagePath != null, "Download didn't succeed for NodeJS package.");    
        objDownloadAgent.closeDownloadAgent();
       
        //copy package to remote machine & untar  
        copyPackageRemotely(SaasNodeJSTestbed.MACHINE1_ID, agentHost, 
            packagePath, TasBuilder.LINUX_SOFTWARE_LOC);
        
        //untar package
        String srcPath = TasBuilder.LINUX_SOFTWARE_LOC + new File(packagePath).getName();
        Assert.assertTrue(
            runRemoteCommand(SaasNodeJSTestbed.MACHINE2_ID, "tar", "", 
                Arrays.asList("-xf", srcPath, "-C", TasBuilder.LINUX_SOFTWARE_LOC)),
            "Unpacking " + srcPath + " didn't succeed.");
        
        String agentDir = TasBuilder.LINUX_SOFTWARE_LOC + "/" + IABomUtil.IA_PARENT_DIR_SAAS;
        
        //install/start umagent
        Assert.assertTrue(
            runRemoteCommand(SaasNodeJSTestbed.MACHINE2_ID, IABomUtil.IA_INSTALL_SCRIPT_UNIX, agentDir, Arrays.asList("install")),
            "IA installation/startup didn't succeed.");
        
        //start nodejs app
        runSerializedCommandFlowFromRole(SaasNodeJSTestbed.TIXCHANGE_ROLE_ID,
            TixChangeRole.ENV_TIXCHANGE_START);
        
        //check agents connected
        HashMap<String,String> agents = new HashMap<String,String>();        
        agents.put(agentHost, "Agent"); //umagent
        agents.put(agentHost, "server Agent"); //probe
        checkAgentsConnectedUI(objDownloadAgent, agents);
    }
    
    protected void validateNodeJSMap(String appName, String host, 
                                     String[] mapSubNodes) throws Exception {
        
        //start load  
        Assert.assertTrue(startJMeter(host, "3000", "tixChange_nodejs_simple.jmx", 120000), 
            "Jmeter script didn't start successfully");  
        
        //open map & add filter
        filterMapByHost(host);
        
        //validate generic frontends data
        String frontendNode = "Apps|" + appName;
        Assert.assertTrue(mapHelper.clickComponentViewTab(frontendNode, "Generic Front-end"), 
            "'Generic Front-end' tab is missing for node " + frontendNode + " or node itself didn't appear.");
        validateGenericFrontendData(appName);
        
        for(String node: mapSubNodes) {
            logger.info("Validating agent & expressjs data for node '{}'...", node);
            Assert.assertTrue(
                mapHelper.clickFitAll(), "Failed to click on 'Fit all to view' map option.");
            
            //validate Agent data
            Assert.assertTrue(clickAgentTab(node), 
                "Either 'Agent' tab is missing for node '" + node + "' or node itself didn't appear.");
            validateAgentTabData(); 
            
            //validate Expressjs data
            Assert.assertTrue(mapHelper.clickComponentViewTab(node, "Express.js"), 
                "'Express.js' tab is missing for node " + node + " or node itself didn't appear.");
            validateExpressjsData(appName);
        }
    }
    
    private void validateExpressjsData(String appName) {
        
        String[] data =
            new String[] {"EXPRESSJS", "Average Response Time (ms)", "Errors Per Interval", "Responses Per Interval",
                          "Stall Count", "Concurrent Invocations", appName};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + " doesn't exist");
        }
    }
    
    protected void validateNodeJsTTViewer(String agentName, String host, String port,
                                          String nodeName) throws Exception {
        
        filterMapByHost(host);
        
        for(TraceInfo trace: WebAppTraceDetails.TRACES_INFO) {
            if(trace.getNodeName().contains(nodeName)) {
                String url = "http://" + host + ":" + port + trace.getUrl();
                validateTrace(agentName, host, url, trace);
            }
        }
    }
    
    @Override   
    protected void startTransactionTrace(String agentName, String host) throws Exception {
        
        startUITransactionTrace(agentName, host, "1", "2");
    }
}