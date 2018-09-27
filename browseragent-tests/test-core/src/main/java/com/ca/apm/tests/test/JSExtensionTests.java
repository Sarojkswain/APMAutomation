/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

/**
 * Tests for Javascript extension feature - BrowserAgent
 *
 * @author gupra04
 * 
 */

package com.ca.apm.tests.test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.io.IOException;

import java.util.Calendar;
import java.util.Date;

import com.ca.apm.tests.testbed.BrowserAgentTomcatChromeWinTestbed;
import com.ca.apm.tests.testbed.BrowserAgentTomcatFirefoxWinTestbed;
import com.ca.apm.tests.testbed.BrowserAgentTomcatIEWinTestbed;
import com.ca.apm.tests.utils.TransactionTraceUtils;
import com.ca.apm.tests.utils.FileUtils;
import com.ca.apm.tests.utils.constants.MetricConstants;
import com.ca.apm.tests.utils.constants.MetricConstants.*;
import com.ca.apm.tests.utils.constants.AgentPropertyConstants.AgentDefaults;
import com.ca.apm.tests.utils.constants.AgentPropertyConstants.BrowseAgentProperties;
import com.ca.apm.tests.utils.constants.JSExtensionConstants.JSExtensionBackUpFile;
import com.ca.apm.tests.utils.constants.JSExtensionConstants.JSExtensionTestScript;
import com.ca.apm.tests.utils.constants.JSExtensionConstants.LogMessages;
import com.ca.apm.tests.utils.constants.JSExtensionConstants.TransactionTraceBackUpFile;
import com.ca.apm.tests.utils.constants.JSExtensionConstants.TransactionTraceOptionalProperties;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;
import com.ca.apm.tests.utils.CommonUtils;
import com.ca.apm.tests.utils.MetricUtils;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;
// import com.ca.apm.tests.common.file.JarUtils;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openqa.selenium.By;

@Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = {
        @TestBed(name = BrowserAgentTomcatChromeWinTestbed.class, executeOn = BrowserAgentTomcatChromeWinTestbed.BROWSERAGENT_MACHINE_ID),
        @TestBed(name = BrowserAgentTomcatFirefoxWinTestbed.class, executeOn = BrowserAgentTomcatFirefoxWinTestbed.BROWSERAGENT_MACHINE_ID),
        @TestBed(name = BrowserAgentTomcatIEWinTestbed.class, executeOn = BrowserAgentTomcatIEWinTestbed.BROWSERAGENT_MACHINE_ID)}, size = SizeType.BIG, owner = "gupra04")
@Test(description = "Tests for Javascript Extension for Browser Agent")
public class JSExtensionTests extends BrowserAgentBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSExtensionTests.class);

    /**
     * This is a helper function to inspect the xml node structure
     * 
     * @param nodeList
     * @param index
     * @param tagName
     * @return the value of the desired node
     */
    
    private static String getValueAt( NodeList nodeList, int index, String tagName )
    {
        String returnValue = "";

        if ( nodeList != null && index < nodeList.getLength() )
        {
            Node node = nodeList.item(index);
            
            if ( node != null )
            {
                NamedNodeMap nodeMap = node.getAttributes();
                
                if ( nodeMap != null )
                {
                    Node item = nodeMap.getNamedItem(tagName);
                    
                    if ( item != null )
                    {
                        returnValue = item.getNodeValue();
                    }
                }
            }
        }
        
        return returnValue;
    }
    
    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "Full"}, description = "Testcase ID: "
        + "454228, 454225, 454231, 454432; Monitor custorm AJAX, Page and Transaction Trace metrics - with BS, Test JS extension with include exclude URL list property")
    public void customPageAjaxTransactionTraceTests() throws IOException {
        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        boolean testPassStatus = true;

        // Step 1: Updating Browser Agent properties for the test - Step 1
        LOGGER.info("customPageAjaxTransactionTraceTests: Updating Browser Agent properties");
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.URL_METRIC_OFF, "false",
            agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_THRESHOLD, "0",
            agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.PAGE_LOAD_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.PAGE_LOAD_METRICS_THRESHOLD,
            "0", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.BROWSER_LOGGING_ENABLED,
            "true", agent);

        // This test runs with Business Services enabled - Nothing needs to be done for Step 2
        // Before class will create the Business Service if it does not exist
        // Step 3 - Update JS Extension File
        // Create a backup File
        Files.copy(Paths.get(agent.getAgentJsExtensionFileLocation()),
            Paths.get(agent.getAgentJsExtensionFileLocation() + JSExtensionBackUpFile.BACKUP),
            StandardCopyOption.REPLACE_EXISTING);

        // Set reset flag for reverting Javascript Extension file after test completes
        resetExtensionFile = true;

        LOGGER
            .info("customPageAjaxTransactionTraceTests: Updating apmbrtmextensibility.js -- stringToInsertforcustomAJAXMetrics: "
                + JSExtensionTestScript.CUSTOM_AJAX_SCRIPT
                + " After: "
                + JSExtensionTestScript.CUSTOM_AJAX_FUNCTION);

        // Update Javascript Extension file for for Custom AJAX metrics
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_AJAX_SCRIPT, JSExtensionTestScript.CUSTOM_AJAX_FUNCTION,
            false);

        LOGGER
            .info("customPageAjaxTransactionTraceTests: Updating apmbrtmextensibility.js -- stringToInsertforcustomPageMetrics: "
                + JSExtensionTestScript.CUSTOM_PAGE_SCRIPT
                + " After: "
                + JSExtensionTestScript.CUSTOM_PAGE_FUNCTION);

        // Update Javascript Extension file for for Custom Page Metrics
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_PAGE_SCRIPT, JSExtensionTestScript.CUSTOM_PAGE_FUNCTION,
            false);

        LOGGER
            .info("customPageAjaxTransactionTraceTests: Updating apmbrtmextensibility.js -- stringToInsertforcustomOptionalProperties: "
                + JSExtensionTestScript.CUSTOM_OPTIONAL_PROPERTIES_SCRIPT
                + " After: "
                + JSExtensionTestScript.CUSTOM_OPTIONAL_PROPERTIES_FUNCTION);

        // Update Javascript Extension file for for Custom Optional Properties
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_OPTIONAL_PROPERTIES_SCRIPT,
            JSExtensionTestScript.CUSTOM_OPTIONAL_PROPERTIES_FUNCTION, false);

        // Update property for Javascript Extension file location
        LOGGER
            .info("customPageAjaxTransactionTraceTests: Updating agent profile to point to modified apmbrtmextensibility.js - "
                + agent.getAgentJsExtensionFileLocation());
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_EXTENSION_LOCATION,
            agent.getAgentJsExtensionFileLocation(), agent);
        // Restart application server for changes to property and Javascript Extension file
        // TODO: Create copy of updated js file here instead of doing it in the end.
        restartAppServer(false);
        CommonUtils.resetBrowser(seleniumData);

        // Launch page - Sustainability metrics will not report data unless BrowserAgent is invoked
        // after agent startup
        LOGGER.info("customPageAjaxTransactionTraceTests: accessing URL:" + seleniumUrl
            + BrtmTestApp.INDEX_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.INDEX_PAGE, seleniumData);

        LOGGER.info("customPageAjaxTransactionTraceTests: Verify Sustainability metric values");
        MetricUtils.verifyLastBRTMSustainabilityMetric(BrowseAgentProperties.URL_METRIC_OFF, "0",
            agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "1", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.AJAX_METRICS_THRESHOLD, "0", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.PAGE_LOAD_METRICS_ENABLED, "1", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.PAGE_LOAD_METRICS_THRESHOLD, "0", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.BROWSER_LOGGING_ENABLED, "1", agent, em);

        String metricPath = MetricUtils.createPageMetricPathWBT(agent, seleniumData);
        LOGGER.info("customPageAjaxTransactionTraceTests: metricPath is : " + metricPath);

        try {
            NodeList params = null;
            // TransactionTraceFilter is used to choose CLW command in TransactionTraceUtils
            TransactionTraceFilter = 1;
            if (new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                .exists()) {
                LOGGER
                    .info("customPageAjaxTransactionTraceTests: Deleting old Transaction trace file");
                new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                    .delete();
            }

            Thread myCLW = new Thread(new TransactionTraceUtils());
            // Step 4 - Start Transaction Trace session with CLW
            myCLW.start();
            CommonUtils.sleep(15000);

            // Step 5
            LOGGER.info("customPageAjaxTransactionTraceTests: accessing URL:" + seleniumUrl
                + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE + " using webdriver");
            // Using launchSinglePageWithRefresh - BT's in use. Metric may get reported to different
            // node.
            CommonUtils.launchSinglePageWithRefresh(seleniumUrl
                + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, seleniumData);

            LOGGER
                .info("customPageAjaxTransactionTraceTests: Verify Custom Page and Ajax metrics report data");

            // Validate Custom Page Metrics
            MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);
            MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, 5, 2,
                em);
            MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, 1, 2,
                em);

            // Validate custom Ajax metrics
            MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH,
                AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 5, 2, agent, em, seleniumData);
            MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE,
                5, 2, agent, em, seleniumData);
            MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT, 5,
                2, agent, em, seleniumData);

            // Wait for CLW / Transaction trace session to complete
            myCLW.join();

            String msg =
                "Transaction Trace XML was not generated for Custom Page, Ajax and Transaction Trace Properties test";
            if (!new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                .exists()) {
                testPassStatus = false;
            } else {
                // Create a copy of the transaction trace file for debugging in case of failure
                Files.copy(
                    Paths.get(agent.getTransactionTraceDirectory()
                        + agent.getTransactionTraceFile()),
                    Paths.get(agent.getTransactionTraceDirectory()
                        + agent.getTransactionTraceFile()
                        + TransactionTraceBackUpFile.PAGE_AJAX_OPTIONAL_PROPERTIES),
                    StandardCopyOption.REPLACE_EXISTING);
            }

            CommonUtils.customAssertTrue(testPassStatus, msg);
            LOGGER
                .info("customPageAjaxTransactionTraceTests: Transaction Trace XML file found for Custom Page, Ajax and Transaction Trace Properties test. TTest # "
                    + TransactionTraceFilter);

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc =
                builder.parse(agent.getTransactionTraceDirectory()
                    + agent.getTransactionTraceFile());

            params = doc.getElementsByTagName("CalledComponent");
            LOGGER
                .info("customPageAjaxTransactionTraceTests: Number of 'CalledComponent' elements : "
                    + params.getLength());

            NodeList params1 = null;

            int substringStartIndex = metricPath.indexOf("Business Segment");
            int substringEndIndex = metricPath.indexOf(":");
            String searchStringPage = metricPath.substring(substringStartIndex, substringEndIndex);
            LOGGER.info("customPageAjaxTransactionTraceTests: SearchString for path = "
                + searchStringPage);

            // Iterate through all CalledComponent Elements
            for (int i = 0; i < params.getLength(); i++) {

                // For Page Transactions
                if ( getValueAt( params, i, "ComponentType"  ).contains("Business Segment") &&
                     getValueAt( params, i, "MetricPath" ).equals(searchStringPage)  )
                {
                    int customPageMetricCountCounter = 0;
                    int customPageMetricAvgCounter = 0;
                    int customOptionalTTProp1 = 0;
                    int customOptionalTTProp2 = 0;

                    NodeList childList = params.item(i).getChildNodes();
                    // Iterate through all CalledComponents that meet the above criterion
                    for (int j = 0; j < childList.getLength(); j++) {
                        // Get Child element 'Parameters'
                        if (childList.item(j).getNodeName().contains("Parameters")) {
                            NodeList childList2 = childList.item(j).getChildNodes();
                            // Iterate through all child Elements for 'Parameters'
                            for (int k = 0; k < childList2.getLength(); k++) {
                                if (childList2.item(k).getNodeName().contains("Parameter")) {
                                    
                                    String nameValue = getValueAt( childList2, k, "Name");
                                    String valueValue = getValueAt( childList2, k, "Value");
                                    
                                    boolean nameContainsSearchStringPage = nameValue.contains(searchStringPage);
                                    
                                    if ( nameContainsSearchStringPage && nameValue.contains( BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT ) &&
                                         Integer.parseInt( valueValue ) == 1 )
                                    {                                            
                                        customPageMetricCountCounter++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom page metric count counter");
                                    }
                                    else if ( nameContainsSearchStringPage && nameValue.contains( BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE ) &&  
                                              Integer.parseInt( valueValue ) == 5 )                                            
                                    {
                                        customPageMetricAvgCounter++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom page metric count counters");
                                    }
                                    else if ( nameValue.equals(TransactionTraceOptionalProperties.TEST_PROPERTY_1) &&
                                              valueValue.equals(TransactionTraceOptionalProperties.TEST_VALUE_1) )
                                    {
                                        customOptionalTTProp1++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom optional TT counter1");
                                    }
                                    else if ( nameValue.equals(TransactionTraceOptionalProperties.TEST_PROPERTY_2) &&
                                              valueValue.equals(TransactionTraceOptionalProperties.TEST_VALUE_2)) 
                                    {
                                        customOptionalTTProp2++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom optional TT counter2");
                                    }                                   
                                }
                            }
                            if (customPageMetricCountCounter != 1
                                || customPageMetricAvgCounter != 1 || customOptionalTTProp1 != 1
                                || customOptionalTTProp2 != 1) {
                                testPassStatus = false;
                                msg =
                                    "Wrong count for customPageMetricCountCounter or customPageMetricAvgCounter or customOptionalTTProp1 or customOptionalTTProp2 : "
                                        + customPageMetricCountCounter
                                        + "and "
                                        + customPageMetricAvgCounter
                                        + "and "
                                        + customOptionalTTProp1
                                        + "and "
                                        + customOptionalTTProp2
                                        + " testPassStatus = " + testPassStatus;
                            }
                            CommonUtils.customAssertTrue(testPassStatus, msg);
                        }
                    }
                }
                // For Ajax transactions               
                if ( getValueAt( params, i, "ComponentType"  ).contains("Business Segment") &&
                     getValueAt( params, i, "MetricPath" ).contains(searchStringPage + "|AJAX Call" ) )    
                {
                    int customAjaxMetricCountCounter = 0;
                    int customAjaxMetricAvgCounter = 0;
                    int customOptionalTTProp1 = 0;
                    int customOptionalTTProp2 = 0;

                    NodeList childList = params.item(i).getChildNodes();
                    // Iterate through all CalledComponents that meet the above criterion
                    for (int j = 0; j < childList.getLength(); j++) {
                        // Get Child element 'Parameters'
                        if (childList.item(j).getNodeName().contains("Parameters")) {
                            NodeList childList2 = childList.item(j).getChildNodes();
                            // Iterate through all child Elements for 'Parameters'
                            for (int k = 0; k < childList2.getLength(); k++) {
                                if (childList2.item(k).getNodeName().contains("Parameter")) {
                                    
                                    String nameValue = getValueAt( childList2, k, "Name");
                                    String valueValue = getValueAt( childList2, k, "Value");
                                    
                                    boolean nameContainsSearchStringPage = nameValue.contains(searchStringPage);
                                    
                                    if ( nameContainsSearchStringPage && nameValue.contains(AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT) &&
                                         Integer.parseInt( valueValue ) == 1 )
                                    {                                            
                                        customAjaxMetricCountCounter++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom page metric count counter");                                                
                                    }
                                    else if ( nameContainsSearchStringPage && nameValue.contains( AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE ) &&
                                              Integer.parseInt( valueValue ) == 5 )
                                    {
                                        customAjaxMetricAvgCounter++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom Ajax metric count counter");                                                
                                    }
                                    else if ( nameValue.equals(TransactionTraceOptionalProperties.TEST_PROPERTY_1) &&
                                              valueValue.equals(TransactionTraceOptionalProperties.TEST_VALUE_1) )
                                    {
                                        customOptionalTTProp1++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom optional TT counter1");                                                
                                    }
                                    else if ( nameValue.equals(TransactionTraceOptionalProperties.TEST_PROPERTY_2) &&
                                              valueValue.equals(TransactionTraceOptionalProperties.TEST_VALUE_2) )
                                    {
                                        customOptionalTTProp2++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom optional TT counter2");                                                
                                    }
                                }
                            }
                            if (customAjaxMetricCountCounter != 1
                                || customAjaxMetricAvgCounter != 1 || customOptionalTTProp1 != 1
                                || customOptionalTTProp2 != 1) {
                                testPassStatus = false;
                                msg =
                                    "Wrong count for customAjaxMetricCountCounter or customAjaxMetricAvgCounter or customOptionalTTProp1 or customOptionalTTProp2 : "
                                        + customAjaxMetricCountCounter
                                        + "and "
                                        + customAjaxMetricAvgCounter
                                        + "and "
                                        + customOptionalTTProp1
                                        + "and "
                                        + customOptionalTTProp2
                                        + " testPassStatus = " + testPassStatus;
                            }
                            CommonUtils.customAssertTrue(testPassStatus, msg);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Step 6 - Validate no Page or Ajax data reported if Page & Ajax metrics are disabled
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.PAGE_LOAD_METRICS_ENABLED,
            "false", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "false", agent);
        LOGGER.info("customPageAjaxTransactionTraceTests: Wait for hot properties to load");
        CommonUtils.sleep(60000);
        CommonUtils.resetBrowser(seleniumData);

        try {
            NodeList params = null;
            // TransactionTraceFilter is used to choose CLW command in TransactionTraceUtils
            TransactionTraceFilter = 1;
            if (new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                .exists()) {
                LOGGER
                    .info("customPageAjaxTransactionTraceTests: Deleting old Transaction trace file");
                new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                    .delete();
            }

            Thread myCLW = new Thread(new TransactionTraceUtils());
            myCLW.start();
            CommonUtils.sleep(15000);

            LOGGER.info("customPageAjaxTransactionTraceTests: accessing URL:" + seleniumUrl
                + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE + " using webdriver");
            // Using launchSinglePageWithRefresh - BT's in use. Metric may get reported to different
            // node.
            CommonUtils.launchSinglePageWithRefresh(seleniumUrl
                + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, seleniumData);

            LOGGER
                .info("customPageAjaxTransactionTraceTests: Verify Custom Page and Ajax metrics do not report data");
            MetricUtils
                .compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, -1, 2, em);
            MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, -1,
                2, em);
            MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, -1, 2,
                em);

            MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH,
                AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em, seleniumData);
            MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE,
                -1, 2, agent, em, seleniumData);
            MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT,
                -1, 2, agent, em, seleniumData);

            // Wait for CLW / Transaction trace session to complete
            myCLW.join();

            String msg =
                "customPageAjaxTransactionTraceTests: Transaction Trace XML was not generated";
            if (!new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                .exists()) {
                testPassStatus = false;
            } else {
                // Create a copy of the transaction trace file for debugging if needed
                Files.copy(
                    Paths.get(agent.getTransactionTraceDirectory()
                        + agent.getTransactionTraceFile()),
                    Paths.get(agent.getTransactionTraceDirectory()
                        + agent.getTransactionTraceFile()
                        + TransactionTraceBackUpFile.PAGE_AJAX_OPTIONAL_PROPERTIES_DISABLED),
                    StandardCopyOption.REPLACE_EXISTING);
            }

            CommonUtils.customAssertTrue(testPassStatus, msg);
            LOGGER
                .info("customPageAjaxTransactionTraceTests: Transaction Trace XML file found for Page, Ajax and Optional Properites - Page and Ajax disabled.  TTest # "
                    + TransactionTraceFilter);

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc =
                builder.parse(agent.getTransactionTraceDirectory()
                    + agent.getTransactionTraceFile());
            params = doc.getElementsByTagName("CalledComponent");
            LOGGER
                .info("customPageAjaxTransactionTraceTests: Number of 'CalledComponent' elements : "
                    + params.getLength());

            NodeList params1 = null;

            int substringStartIndex = metricPath.indexOf("Business Segment");
            int substringEndIndex = metricPath.indexOf(":");
            String searchStringPage = metricPath.substring(substringStartIndex, substringEndIndex);
            LOGGER.info("customPageAjaxTransactionTraceTests: Path for searchString = "
                + searchStringPage);

            // Iterate through all CalledComponent Elements            
            for (int i = 0; i < params.getLength(); i++) {                                
                if ( getValueAt( params, i, "ComponentType"  ).contains("Business Segment") &&
                     getValueAt( params, i, "MetricPath" ).equals(searchStringPage ) )  
                {
                    int customPageMetricCountCounter = 0;
                    int customPageMetricAvgCounter = 0;
                    int customOptionalTTProp1 = 0;
                    int customOptionalTTProp2 = 0;

                    NodeList childList = params.item(i).getChildNodes();
                    // Iterate through all CalledComponents that meet the above criterion
                    for (int j = 0; j < childList.getLength(); j++) {
                        // Get Child element 'Parameters'
                        if (childList.item(j).getNodeName().contains("Parameters")) {
                            NodeList childList2 = childList.item(j).getChildNodes();
                            // Iterate through all child Elements for 'Parameters'
                            for (int k = 0; k < childList2.getLength(); k++) {
                                if (childList2.item(k).getNodeName().contains("Parameter")) {
                                    
                                    String nameValue = getValueAt( childList2, k, "Name");
                                    
                                    boolean nameContainsSearchStringPage = nameValue.contains(searchStringPage);
                                    
                                    if (nameContainsSearchStringPage && nameValue.contains(BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT) )
                                    {
                                        customPageMetricCountCounter++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom page metric count counte");
                                    }
                                    else if (nameContainsSearchStringPage && nameValue.contains(BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE))
                                    {
                                        customPageMetricAvgCounter++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom page metric count counter");                                                
                                    }
                                    else if ( nameValue.equals(TransactionTraceOptionalProperties.TEST_PROPERTY_1) )
                                    {
                                        customOptionalTTProp1++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom optional TT counter1");
                                    }
                                    else if (nameValue.equals(TransactionTraceOptionalProperties.TEST_PROPERTY_2)) 
                                    {
                                        customOptionalTTProp2++;
                                        LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom optional TT counter2");                                                
                                    }
                                }
                            }
                            if (customPageMetricCountCounter != 1
                                || customPageMetricAvgCounter != 1 || customOptionalTTProp1 != 1
                                || customOptionalTTProp2 != 1) {
                                testPassStatus = false;
                                msg =
                                    "Wrong count for customPageMetricCountCounter or customPageMetricAvgCounter or customOptionalTTProp1 or customOptionalTTProp2 : "
                                        + customPageMetricCountCounter
                                        + "and "
                                        + customPageMetricAvgCounter
                                        + "and "
                                        + customOptionalTTProp1
                                        + "and "
                                        + customOptionalTTProp2
                                        + " testPassStatus = " + testPassStatus;
                            }
                            CommonUtils.customAssertTrue(testPassStatus, msg);
                        }
                    }
                }
                // Check for Ajax transactions                
                if ( getValueAt( params, i, "ComponentType"  ).contains("Business Segment") &&
                     getValueAt( params, i, "MetricPath" ).contains(searchStringPage + "|AJAX Call" ) )    
                {

                    int customAjaxMetricCountCounter = 0;
                    int customAjaxMetricAvgCounter = 0;
                    int customOptionalTTProp1 = 0;
                    int customOptionalTTProp2 = 0;

                    NodeList childList = params.item(i).getChildNodes();
                    // Iterate through all CalledComponents that meet the above criterion
                    for (int j = 0; j < childList.getLength(); j++) {
                        // Get Child element 'Parameters'
                        if (childList.item(j).getNodeName().contains("Parameters")) {
                            NodeList childList2 = childList.item(j).getChildNodes();
                            // Iterate through all child Elements for 'Parameters'
                            for (int k = 0; k < childList2.getLength(); k++) {
                                
                                String nameValue = getValueAt( childList2, k, "Name");
                                
                                boolean nameContainsSearchStringPage = nameValue.contains(searchStringPage);
                                
                                if ( nameContainsSearchStringPage && nameValue.contains(AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT) ) 
                                {
                                    customAjaxMetricCountCounter++;
                                    LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom page metric count counter");                                        
                                } 
                                else if ( nameContainsSearchStringPage && nameValue.contains(AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE)) 
                                {
                                    customAjaxMetricAvgCounter++;
                                    LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom Ajax metric count counter");                                        
                                }
                                else if (nameValue.equals(TransactionTraceOptionalProperties.TEST_PROPERTY_1)) 
                                {
                                    customOptionalTTProp1++;
                                    LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom optional TT counter1");                                        
                                }
                                else if ( nameValue.equals(TransactionTraceOptionalProperties.TEST_PROPERTY_2)) 
                                {
                                    customOptionalTTProp2++;
                                    LOGGER.info("customPageAjaxTransactionTraceTests: Found a match for custom optional TT counter2");                                        
                                }
                            }
                        }
                        if (customAjaxMetricCountCounter != 0 || customAjaxMetricAvgCounter != 0
                            || customOptionalTTProp1 != 1 || customOptionalTTProp2 != 1) {
                            testPassStatus = false;
                            msg =
                                "Wrong count for customAjaxMetricCountCounter or customAjaxMetricAvgCounter or customOptionalTTProp1 or customOptionalTTProp2 : "
                                    + customAjaxMetricCountCounter
                                    + "and "
                                    + customAjaxMetricAvgCounter
                                    + "and "
                                    + customOptionalTTProp1
                                    + "and "
                                    + customOptionalTTProp2
                                    + " testPassStatus = "
                                    + testPassStatus;
                        }
                        CommonUtils.customAssertTrue(testPassStatus, msg);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Test case 454232 - Test with Include Exclude URL list
        // Combined in test method to avoid agent restart
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.PAGE_LOAD_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.INCLUDE_URL_LIST,
            "[\".*\"]", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.EXLUDE_URL_LIST,
            "[\".*/brtmtestapp/GETLocalDomain2.jsp\"]", agent);

        LOGGER.info("customPageAjaxTransactionTraceTests: Wait for hot properties to load");
        CommonUtils.sleep(60000);
        CommonUtils.resetBrowser(seleniumData);

        LOGGER.info("customPageAjaxTransactionTraceTests: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE + " using webdriver");
        // Using launchSinglePageWithRefresh - BT's in use. Metric may get reported to different
        // node.
        CommonUtils.launchSinglePageWithRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE,
            seleniumData);

        LOGGER
            .info("customPageAjaxTransactionTraceTests: Verify Custom Page and Ajax metrics do not report data for GETLocalDomain page");
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, -1, 2, em);
        MetricUtils
            .compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, -1, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, -1, 2, em);

        MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em, seleniumData);
        MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE, -1,
            2, agent, em, seleniumData);
        MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT, -1, 2,
            agent, em, seleniumData);

        LOGGER.info("customPageAjaxTransactionTraceTests: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_CORS_PAGE + " using webdriver");
        // Using launchSinglePageWithRefresh - BT's in use. Metric may get reported to different
        // node.
        CommonUtils.launchSinglePageWithRefresh(seleniumUrl + BrtmTestApp.GET_CORS_PAGE,
            seleniumData);

        LOGGER
            .info("customPageAjaxTransactionTraceTests: Verify Custom Page and Ajax metrics report data for GETCors page");
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, 5, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, 1, 2, em);

        MetricUtils.compareAJAXMetricsWBT(AJAXMetricPath.GET_CORS_AJAX_HOST_PORT,
            AJAXMetricPath.GET_CORS_AJAX_PATH, AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 5,
            2, agent, em, seleniumData);
        MetricUtils.compareAJAXMetricsWBT(AJAXMetricPath.GET_CORS_AJAX_HOST_PORT,
            AJAXMetricPath.GET_CORS_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE, 5, 2, agent,
            em, seleniumData);
        MetricUtils.compareAJAXMetricsWBT(AJAXMetricPath.GET_CORS_AJAX_HOST_PORT,
            AJAXMetricPath.GET_CORS_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT, 5, 2, agent,
            em, seleniumData);

        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.INCLUDE_URL_LIST,
            "[\".*/brtmtestapp/GETLocalDomain2.jsp\", \".*sample.txt.*\"]", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.EXLUDE_URL_LIST,
            "[\".*/brtmtestapp/GETCORS2.jsp\"]", agent);

        LOGGER.info("customPageAjaxTransactionTraceTests: Wait for hot properties to load");
        CommonUtils.sleep(60000);
        CommonUtils.resetBrowser(seleniumData);

        LOGGER.info("customPageAjaxTransactionTraceTests: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_CORS_PAGE + " using webdriver");
        // Using launchSinglePageWithRefresh - BT's in use. Metric may get reported to different
        // node.
        CommonUtils.launchSinglePageWithRefresh(seleniumUrl + BrtmTestApp.GET_CORS_PAGE,
            seleniumData);

        LOGGER
            .info("customPageAjaxTransactionTraceTests: Verify Custom Page and Ajax metrics do not report data for GETCors page");
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, -1, 2, em);
        MetricUtils
            .compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, -1, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, -1, 2, em);

        MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_CORS_AJAX_PATH, AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1,
            2, agent, em, seleniumData);
        MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_CORS_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE, -1, 2,
            agent, em, seleniumData);
        MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_CORS_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT, -1, 2, agent,
            em, seleniumData);

        LOGGER.info("customPageAjaxTransactionTraceTests: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE + " using webdriver");
        // Using launchSinglePageWithRefresh - BT's in use. Metric may get reported to different
        // node.
        CommonUtils.launchSinglePageWithRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE,
            seleniumData);

        LOGGER
            .info("customPageAjaxTransactionTraceTests: Verify Custom Page and Ajax metrics report data for GETLocalDomain page");
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, 5, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, 1, 2, em);
        MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 5, 2, agent, em, seleniumData);
        MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE, 5,
            2, agent, em, seleniumData);
        MetricUtils.compareAJAXMetricsWBT(agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT, 5, 2,
            agent, em, seleniumData);

        try {
            // Copy of Javascript Extension file with updated details for Page/Ajax/TransactionTrace
            // tests
            LOGGER.info("customPageAjaxTransactionTraceTests: Copy updated JS Extension file");
            Files.copy(
                Paths.get(agent.getAgentJsExtensionFileLocation()),
                Paths.get(agent.getAgentJsExtensionFileLocation()
                    + JSExtensionBackUpFile.CUSTOM_PAGE_AJAX_OPTIONAL_PROPERTIES),
                StandardCopyOption.REPLACE_EXISTING);
            // Revert Javascript Extension file after stopping agent
        } catch (IOException e) {
            LOGGER
                .info("customPageAjaxTransactionTraceTests: Error creating a copy of updated file.");
            e.printStackTrace();
        }
    }


    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "Full"}, description = "Testcase ID: "
        + "454229; MISC Metrics")
    public void customMISCMetricTest() throws IOException {
        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        // STEP 1
        LOGGER.info("customMISCMetricTest: Updating properties for MISC metrics test");
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.BROWSER_LOGGING_ENABLED,
            "true", agent);

        // Step 2: Deleting existing Business Services - This test runs without Business Services
        try {
            CommonUtils.deleteBizDef(em);
            LOGGER.info("customMISCMetricTest: Deleted Business Definitions");
        } catch (Exception e) {
            LOGGER.info("customMISCMetricTest: Somethings went wrong. Unable to delete the file");
            e.printStackTrace();
        }

        // Step 3 - Update JS Extension File
        // Create a backup File
        Files.copy(Paths.get(agent.getAgentJsExtensionFileLocation()),
            Paths.get(agent.getAgentJsExtensionFileLocation() + JSExtensionBackUpFile.BACKUP),
            StandardCopyOption.REPLACE_EXISTING);

        // Set reset flag for reverting Javascript Extension file after test completes
        resetExtensionFile = true;

        LOGGER
            .info("customMISCMetricTest: Updating apmbrtmextensibility.js -- stringToInsertforMISCMetrics: "
                + JSExtensionTestScript.CUSTOM_MISC_METRIC_SCRIPT_1
                + " After: "
                + JSExtensionTestScript.CUSTOM_MISC_METRIC_FUNCTION_1);

        // Update Javascript Extension file for for MISC Metrics
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_MISC_METRIC_SCRIPT_1,
            JSExtensionTestScript.CUSTOM_MISC_METRIC_FUNCTION_1, false);

        LOGGER
            .info("customMISCMetricTest: Updating apmbrtmextensibility.js -- stringToInsertforcustomMISCMetrics: "
                + JSExtensionTestScript.CUSTOM_MISC_METRIC_SCRIPT_2
                + " After: "
                + JSExtensionTestScript.CUSTOM_MISC_METRIC_FUNCTION_2);

        // Update Javascript Extension file for for MISC Metrics
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_MISC_METRIC_SCRIPT_2,
            JSExtensionTestScript.CUSTOM_MISC_METRIC_FUNCTION_2, false);

        LOGGER
            .info("customMISCMetricTest: Updating agent profile to point to modified apmbrtmextensibility.js - "
                + agent.getAgentJsExtensionFileLocation());
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_EXTENSION_LOCATION,
            agent.getAgentJsExtensionFileLocation(), agent);
        // Restart application server for changes to property and Javascript Extension file
        // TODO: Create copy of updated js file here instead of doing it in the end.
        restartAppServer(false);
        CommonUtils.resetBrowser(seleniumData);

        // Step 4
        LOGGER.info("customMISCMetricTest: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE + " using webdriver");

        CommonUtils.launchSinglePageWithRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE,
            seleniumData);

        LOGGER.info("customMISCMetricTest: Clicking 'GetLocalFile Button 3 times");
        for (int i = 0; i < 3; i++) {
            seleniumData.getDriver().findElement(By.name("GetLocalFile")).click();;
            CommonUtils.sleep(3000);
        }
        CommonUtils.sleep(45000);


        String metricPath =
            MetricUtils.createPageMetricPathNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, agent);
        MetricUtils.checkMISCMetricsNoBT(metricPath, 3, 2, em);

        try {
            // Copy of Javascript Extension file with updated details
            LOGGER.info("customMISCMetricTest: Copy updated JS Extension file");
            Files.copy(
                Paths.get(agent.getAgentJsExtensionFileLocation()),
                Paths.get(agent.getAgentJsExtensionFileLocation()
                    + JSExtensionBackUpFile.MISC_METRICS), StandardCopyOption.REPLACE_EXISTING);
            // Revert Javascript Extension file after stopping agent
        } catch (IOException e) {
            LOGGER.info("customMISCMetricTest: Error creating a copy of updated file.");
            e.printStackTrace();
        }
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "Full"}, description = "Testcase ID: "
        + "454265;JS Extension with BrowserAgent in Batch Mode")
    public void customMetricBatchModeTest() throws IOException {
        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        // STEP 1
        LOGGER.info("customMetricBatchModeTest: Updating properties for Batch Mode metrics test");
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.URL_METRIC_OFF, "false",
            agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_THRESHOLD, "0",
            agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.PAGE_LOAD_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.PAGE_LOAD_METRICS_THRESHOLD,
            "0", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.METRIC_FREQUENCY, "60000",
            agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.BROWSER_LOGGING_ENABLED,
            "true", agent);

        // Step 2: Deleting existing Business Services - This test runs without Business Services
        try {
            CommonUtils.deleteBizDef(em);
            LOGGER.info("customMetricBatchModeTest: Deleted Business Definitions");
        } catch (Exception e) {
            LOGGER
                .info("customMetricBatchModeTest: Somethings went wrong. Unable to delete the file");
            e.printStackTrace();
        }

        // Step 3 - Update JS Extension File
        // Create a backup File
        Files.copy(Paths.get(agent.getAgentJsExtensionFileLocation()),
            Paths.get(agent.getAgentJsExtensionFileLocation() + JSExtensionBackUpFile.BACKUP),
            StandardCopyOption.REPLACE_EXISTING);

        // Set reset flag for reverting Javascript Extension file after test completes
        resetExtensionFile = true;

        LOGGER
            .info("customMetricBatchModeTest: Updating apmbrtmextensibility.js -- stringToInsertforMISCMetrics: "
                + JSExtensionTestScript.CUSTOM_MISC_METRIC_SCRIPT_1
                + " After: "
                + JSExtensionTestScript.CUSTOM_MISC_METRIC_FUNCTION_1);

        // Update Javascript Extension file for for MISC Metrics
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_MISC_METRIC_SCRIPT_1,
            JSExtensionTestScript.CUSTOM_MISC_METRIC_FUNCTION_1, false);

        LOGGER
            .info("customMetricBatchModeTest: Updating apmbrtmextensibility.js -- stringToInsertforcustomMISCMetrics: "
                + JSExtensionTestScript.CUSTOM_MISC_METRIC_SCRIPT_2
                + " After: "
                + JSExtensionTestScript.CUSTOM_MISC_METRIC_FUNCTION_2);

        // Update Javascript Extension file for for MISC Metrics
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_MISC_METRIC_SCRIPT_2,
            JSExtensionTestScript.CUSTOM_MISC_METRIC_FUNCTION_2, false);

        LOGGER
            .info("customMetricBatchModeTest: Updating apmbrtmextensibility.js -- stringToInsertforcustomAJAXMetrics: "
                + JSExtensionTestScript.CUSTOM_AJAX_SCRIPT
                + " After: "
                + JSExtensionTestScript.CUSTOM_AJAX_FUNCTION);

        // Update Javascript Extension file for for Custom AJAX metrics
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_AJAX_SCRIPT, JSExtensionTestScript.CUSTOM_AJAX_FUNCTION,
            false);

        LOGGER
            .info("customMetricBatchModeTest: Updating apmbrtmextensibility.js -- stringToInsertforcustomPageMetrics: "
                + JSExtensionTestScript.CUSTOM_PAGE_SCRIPT
                + " After: "
                + JSExtensionTestScript.CUSTOM_PAGE_FUNCTION);

        // Update Javascript Extension file for for Custom Page Metrics
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_PAGE_SCRIPT, JSExtensionTestScript.CUSTOM_PAGE_FUNCTION,
            false);

        LOGGER
            .info("customMetricBatchModeTest: Updating agent profile to point to modified apmbrtmextensibility.js - "
                + agent.getAgentJsExtensionFileLocation());
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_EXTENSION_LOCATION,
            agent.getAgentJsExtensionFileLocation(), agent);
        // Restart application server for changes to property and Javascript Extension file
        // TODO: Create copy of updated js file here instead of doing it in the end.
        restartAppServer(false);
        CommonUtils.resetBrowser(seleniumData);

        // Launch page - Sustainability metrics will not report data unless BrowserAgent is invoked
        // after agent startup
        LOGGER.info("customMetricBatchModeTest: accessing URL:" + seleniumUrl
            + BrtmTestApp.INDEX_PAGE + " using webdriver");

        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.INDEX_PAGE, seleniumData);

        LOGGER.info("customMetricBatchModeTest: Verify Sustainability metric values");
        MetricUtils.verifyLastBRTMSustainabilityMetric(BrowseAgentProperties.URL_METRIC_OFF, "0",
            agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "1", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.AJAX_METRICS_THRESHOLD, "0", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.PAGE_LOAD_METRICS_ENABLED, "1", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.PAGE_LOAD_METRICS_THRESHOLD, "0", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(BrowseAgentProperties.METRIC_FREQUENCY,
            "60000", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.BROWSER_LOGGING_ENABLED, "1", agent, em);

        String metricPath =
            MetricUtils.createPageMetricPathNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, agent);
        LOGGER.info("customMetricBatchModeTest: metricPath is : " + metricPath);

        // Step 4
        LOGGER.info("customMetricBatchModeTest: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE,
            seleniumData);

        LOGGER.info("customMetricBatchModeTest: Verify Custom Page report data in Live mode");

        // Validate Page Metrics are reported right away.
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 1, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, 5, 1, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, 1, 1, em);

        // Ajax call
        seleniumData.getDriver().findElement(By.name("NumberOfRequests")).sendKeys("1");
        seleniumData.getDriver().findElement(By.name("GetLocalFile")).click();

        CommonUtils.sleep(30000);
        // Ajax data & MISC metrics should not be reported when queried after 30 seconds
        LOGGER
            .info("customMetricBatchModeTest: Verify Custom Ajax metrics & MISC do not report data in Live mode");

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 1, agent, em);
        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE, -1,
            1, agent, em);
        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT, -1, 1,
            agent, em);
        MetricUtils.checkMISCMetricsNoBT(metricPath, 0, 1, em);

        CommonUtils.sleep(60000);
        LOGGER
            .info("customMetricBatchModeTest: Verify Custom Ajax metrics & MISC report data in Batch mode");

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 1, 1, agent, em);
        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE, 5,
            1, agent, em);
        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT, 1, 1,
            agent, em);

        // Step 5 - Wait before re-running test
        CommonUtils.sleep(60000);
        LOGGER.info("customPageAjaxTransactionTraceTests: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE,
            seleniumData);

        LOGGER
            .info("customMetricBatchModeTest: Verify Custom Metrics Page do not report duplicate data ");
        // Validate Page Metrics are reported in Live mode.
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 1, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, 5, 1, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, 1, 1, em);

        // Ajax call
        seleniumData.getDriver().findElement(By.name("NumberOfRequests")).sendKeys("1");
        seleniumData.getDriver().findElement(By.name("GetLocalFile")).click();

        // Wait for > 1 minute before checking for metrics
        CommonUtils.sleep(90000);
        // Ajax data & MISC metrics should be reported in Batch mode and do not report duplicate
        // data
        LOGGER
            .info("customMetricBatchModeTest: Verify Custom Metrics Ajax metrics & MISC metrics do not report duplicate data ");

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 1, 1, agent, em);
        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_AVERAGE, 5,
            1, agent, em);
        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH, AJAXMetrics.CUSTOM_AJAX_METRIC_COUNT, 1, 1,
            agent, em);
        MetricUtils.checkMISCMetricsNoBT(metricPath, 1, 1, em);

        try {
            // Copy of Javascript Extension file with updated details for custom Metric Batchmode
            // test
            LOGGER.info("customMetricBatchModeTest: Copy updated JS Extension file");
            Files.copy(
                Paths.get(agent.getAgentJsExtensionFileLocation()),
                Paths.get(agent.getAgentJsExtensionFileLocation()
                    + JSExtensionBackUpFile.BATCH_MODE_METRICS),
                StandardCopyOption.REPLACE_EXISTING);
            // Revert Javascript Extension file after stopping agent
        } catch (IOException e) {
            LOGGER.info("customMetricBatchModeTest: Error creating a copy of updated file.");
            e.printStackTrace();
        }
    }


    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: "
        + "452913; Name Formatter")
    public void nameFormatterTest() throws IOException {
        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        boolean testPassStatus = true;

        // Step 2: Deleting existing Business Services - This test runs without Business Services
        try {
            CommonUtils.deleteBizDef(em);
            LOGGER.info("nameFormatterTest: Deleted Business Definitions");
        } catch (Exception e) {
            LOGGER.info("nameFormatterTest: Somethings went wrong. Unable to delete the file");
            e.printStackTrace();
        }

        // Updating Browser Agent properties for the test - Step 1
        LOGGER.info("nameFormatterTest: Updating properties for Name Formatter test");
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.URL_METRIC_OFF, "false",
            agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_THRESHOLD, "0",
            agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.BROWSER_LOGGING_ENABLED,
            "true", agent);

        LOGGER.info("nameFormatterTest: Wait for hot properties to load");
        CommonUtils.sleep(60000);
        CommonUtils.resetBrowser(seleniumData);

        // Launch page - Sustainability metrics will not report data unless BrowserAgent is invoked
        LOGGER.info("nameFormatterTest: accessing URL:" + seleniumUrl + BrtmTestApp.AJAX_CLAMP_PAGE
            + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.AJAX_CLAMP_PAGE,
            seleniumData);

        // Step 3
        LOGGER.info("nameFormatterTest: Verify Sustainability metric values");
        MetricUtils.verifyLastBRTMSustainabilityMetric(BrowseAgentProperties.URL_METRIC_OFF, "0",
            agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "1", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.AJAX_METRICS_THRESHOLD, "0", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.BROWSER_LOGGING_ENABLED, "1", agent, em);

        // Step 4
        LOGGER.info("nameFormatterTest: accessing URL:" + seleniumUrl + BrtmTestApp.AJAX_CLAMP_PAGE
            + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.AJAX_CLAMP_PAGE,
            seleniumData);

        LOGGER.info("nameFormatterTest: Making ajax calls");
        seleniumData.getDriver().findElement(By.name("GetLocalFile")).click();
        CommonUtils.sleep(30000);

        String metricPath =
            MetricUtils.createPageMetricPathNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.AJAX_CLAMP_PAGE, agent);

        // Check metrics are reported for page
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);

        LOGGER
            .info("nameFormatterTest: Verify if ajax metric are reported for all 7 urls and not reported for combined url");
        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.AJAX_CLAMP_PAGE_1_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 1, 2, agent, em);

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.AJAX_CLAMP_PAGE_2_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 1, 2, agent, em);

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.AJAX_CLAMP_PAGE_3_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 1, 2, agent, em);

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.AJAX_CLAMP_PAGE_4_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 1, 2, agent, em);

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.AJAX_CLAMP_PAGE_5_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 1, 2, agent, em);

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.AJAX_CLAMP_PAGE_6_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 1, 2, agent, em);

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.AJAX_CLAMP_PAGE_7_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 1, 2, agent, em);

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.AJAX_CLAMP_PAGE_ALL_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

        // Creating a backup file for JS Extension
        Files.copy(Paths.get(agent.getAgentJsExtensionFileLocation()),
            Paths.get(agent.getAgentJsExtensionFileLocation() + JSExtensionBackUpFile.BACKUP),
            StandardCopyOption.REPLACE_EXISTING);

        resetExtensionFile = true;

        // Step 5
        LOGGER
            .info("nameFormatterTest: Updating apmbrtmextensibility.js -- stringToInsertforNameFormatter: "
                + JSExtensionTestScript.NAME_FORMATTER_SCRIPT
                + " After: "
                + JSExtensionTestScript.NAME_FORMATTER_FUNCTION);

        // Update Javascript Extension file for NameFormatter
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.NAME_FORMATTER_SCRIPT,
            JSExtensionTestScript.NAME_FORMATTER_FUNCTION, false);

        LOGGER
            .info("nameFormatterTest: Updating agent profile to point to modified apmbrtmextensibility.js - "
                + agent.getAgentJsExtensionFileLocation());
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_EXTENSION_LOCATION,
            agent.getAgentJsExtensionFileLocation(), agent);
        // Restart application server for changes to property and Javascript Extension file
        // TODO: Create copy of updated js file here instead of doing it in the end.
        restartAppServer(false);
        CommonUtils.resetBrowser(seleniumData);

        try {
            NodeList params = null;
            // TransactionTraceFilter is used to choose CLW command to be used in
            // TransactionTraceUtils
            TransactionTraceFilter = 1;
            if (new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                .exists()) {
                LOGGER.info("nameFormatterTest: Deleting old Transaction trace file");
                new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                    .delete();
            }

            Thread myCLW = new Thread(new TransactionTraceUtils());
            // Step 6 - Start Transaction Trace session with CLW
            myCLW.start();
            CommonUtils.sleep(15000);

            // Step 7
            LOGGER.info("nameFormatterTest: accessing URL:" + seleniumUrl
                + BrtmTestApp.AJAX_CLAMP_PAGE + " using webdriver");
            CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.AJAX_CLAMP_PAGE,
                seleniumData);

            LOGGER.info("nameFormatterTest: Making ajax calls");
            seleniumData.getDriver().findElement(By.name("GetLocalFile")).click();
            CommonUtils.sleep(30000);

            LOGGER.info("nameFormatterTest: Verify ajax metrics reported only for updated path");

            MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.AJAX_CLAMP_PAGE_1_AJAX_PATH,
                AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

            MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.AJAX_CLAMP_PAGE_2_AJAX_PATH,
                AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

            MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.AJAX_CLAMP_PAGE_3_AJAX_PATH,
                AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

            MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.AJAX_CLAMP_PAGE_4_AJAX_PATH,
                AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

            MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.AJAX_CLAMP_PAGE_5_AJAX_PATH,
                AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

            MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.AJAX_CLAMP_PAGE_6_AJAX_PATH,
                AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

            MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.AJAX_CLAMP_PAGE_7_AJAX_PATH,
                AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

            MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
                AJAXMetricPath.AJAX_CLAMP_PAGE_ALL_AJAX_PATH,
                AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 1, 2, agent, em);

            // Wait for CLW / Transaction trace session to complete
            myCLW.join();

            String msg = "nameFormatterTest: Error - Transaction Trace XML was not generated";
            if (!new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                .exists()) {
                testPassStatus = false;
            } else {
                // Create a copy of the transaction trace file for debugging if needed
                Files.copy(
                    Paths.get(agent.getTransactionTraceDirectory()
                        + agent.getTransactionTraceFile()),
                    Paths.get(agent.getTransactionTraceDirectory()
                        + agent.getTransactionTraceFile()
                        + TransactionTraceBackUpFile.NAME_FORMATTER),
                    StandardCopyOption.REPLACE_EXISTING);
            }

            CommonUtils.customAssertTrue(testPassStatus, msg);
            LOGGER.info("nameFormatterTest: Transaction Trace XML file found.  TTest # "
                + TransactionTraceFilter);

            int ajaxTransactionCounter = 0;
            int failingTestCounter = 0;

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc =
                builder.parse(agent.getTransactionTraceDirectory()
                    + agent.getTransactionTraceFile());

            params = doc.getElementsByTagName("CalledComponent");
            LOGGER.info("nameFormatterTest: Number of 'CalledComponent' elements : "
                + params.getLength());

            LOGGER.info("nameFormatterTest: Path for name formatter test = "
                + AJAXMetricPath.AJAX_CLAMP_PAGE_ALL_AJAX_PATH);

            // Iterate through all CalledComponent Elements
            for (int i = 0; i < params.getLength(); i++) {
                // Check for all 'ComponentType' items for Ajax Calls                
                if ( getValueAt( params, i, "ComponentType"  ).contains("Business Segment") &&
                     getValueAt( params, i, "MetricPath" ).contains("AJAX Call" ) )  
                {

                    // Increment ajax transaction counter
                    ajaxTransactionCounter++;

                    // If MetricPath or Component does not use updated path, test fails
                    if (!params.item(i).getAttributes().getNamedItem("MetricPath").getNodeValue()
                        .contains(AJAXMetricPath.AJAX_CLAMP_PAGE_ALL_AJAX_PATH)
                        || !params.item(i).getAttributes().getNamedItem("ComponentName")
                            .getNodeValue().contains(AJAXMetricPath.AJAX_CLAMP_PAGE_ALL_AJAX_PATH)) {
                        LOGGER.info("nameFormatterTest: Failing on Node Value : "
                            + params.item(i).getAttributes().getNamedItem("MetricPath")
                                .getNodeValue());
                        LOGGER.info("nameFormatterTest: Failing on Node Value : "
                            + params.item(i).getAttributes().getNamedItem("ComponentName")
                                .getNodeValue());
                        testPassStatus = false;
                        failingTestCounter++;
                    }

                    NodeList childList = params.item(i).getChildNodes();
                    // Iterate through all CalledComponents that meet the above criterion
                    for (int j = 0; j < childList.getLength(); j++) {
                        // Get Child element 'Parameters'
                        if (childList.item(j).getNodeName().contains("Parameters")) {
                            NodeList childList2 = childList.item(j).getChildNodes();
                            // Iterate through all child Elements for 'Parameters'
                            for (int k = 0; k < childList2.getLength(); k++) {
                                if (childList2.item(k).getNodeName().contains("Parameter")) {
                                    // All Browser Agent Parameter elements should have 'Business
                                    // Segment' and updated path in Name
                                    String nameValue = getValueAt( childList2, k, "Name");  
                                    
                                    if ( nameValue.contains("Business Segment") &&
                                        !(nameValue.contains(AJAXMetricPath.AJAX_CLAMP_PAGE_ALL_AJAX_PATH))) 
                                    {
                                        testPassStatus = false;
                                        failingTestCounter++;
                                        LOGGER
                                            .info("nameFormatterTest: Failing on child node value : "
                                                + childList2.item(k).getAttributes()
                                                    .getNamedItem("Name").getNodeValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            LOGGER.info("nameFormatterTest: Ajax transaction counter at the end is : "
                + ajaxTransactionCounter);
            LOGGER.info("nameFormatterTest: Status of test : " + testPassStatus
                + " with count of incorrect paths : " + failingTestCounter);
            String msgAjaxTransactionCount =
                "Count of Ajax transactions is not 7, it is : " + ajaxTransactionCounter;
            String msgTestfail =
                "Test for name formatter failed with "
                    + "count of elements with in-correct Path : " + failingTestCounter;
            CommonUtils.customAssertTrue(testPassStatus, msgTestfail);
            // If # of Ajax transactions is not 7, test fails
            if (ajaxTransactionCounter != 7) {
                boolean transCountCorrect = false;
                CommonUtils.customAssertTrue(transCountCorrect, msgAjaxTransactionCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Test for no data reported if ajax metrics are disabled
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "false", agent);
        LOGGER.info("nameFormatterTest: Wait for hot properties to load");
        CommonUtils.sleep(60000);
        CommonUtils.resetBrowser(seleniumData);

        LOGGER.info("nameFormatterTest: accessing URL:" + seleniumUrl + BrtmTestApp.AJAX_CLAMP_PAGE
            + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.AJAX_CLAMP_PAGE,
            seleniumData);

        LOGGER.info("nameFormatterTest: Making ajax calls");
        seleniumData.getDriver().findElement(By.name("GetLocalFile")).click();

        CommonUtils.sleep(45000);
        MetricUtils.verifyAllPageMetrics(metricPath, em);

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.AJAX_CLAMP_PAGE_1_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.AJAX_CLAMP_PAGE, agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.AJAX_CLAMP_PAGE_ALL_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

        try {
            // Copy of Javascript Extension file with updated details for name formatter test
            LOGGER.info("nameFormatterTest: Copy updated JS Extension file");
            Files.copy(
                Paths.get(agent.getAgentJsExtensionFileLocation()),
                Paths.get(agent.getAgentJsExtensionFileLocation()
                    + JSExtensionBackUpFile.NAME_FORMATTER), StandardCopyOption.REPLACE_EXISTING);
            // Revert Javascript Extension file after stopping agent
        } catch (IOException e) {
            LOGGER.info("nameFormatterTest: Error creating a copy of updated file.");
            e.printStackTrace();
        }
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "Full"}, description = "Testcase ID: "
        + "454230, 454226; Monitor custom javascript function and metrics with Javascript Extension")
    public void customJsFunctionAndMetricsTest() throws IOException {
        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        // Step 2: Deleting existing Business Services - This test runs without Business Services
        try {
            CommonUtils.deleteBizDef(em);
            LOGGER.info("customJsFunctionAndMetricsTest: Deleted Business Definitions");
        } catch (Exception e) {
            LOGGER
                .info("customJsFunctionAndMetricsTest: Somethings went wrong. Unable to delete the file");
            e.printStackTrace();
        }

        // Step 1 - Updating Browser Agent properties for the test
        LOGGER
            .info("customJsFunctionAndMetricsTest: Updating properties for JS Function and Metrics Test");
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.URL_METRIC_OFF, "false",
            agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_FUNCTION_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(
            BrowseAgentProperties.JS_FUNCTION_METRICS_THRESHOLD, "0", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_THRESHOLD, "0",
            agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.BROWSER_LOGGING_ENABLED,
            "true", agent);

        String metricPath =
            MetricUtils.createPageMetricPathNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, agent);
        LOGGER.info("customJsFunctionAndMetricsTest: metricPath is " + metricPath);

        // Step 3
        // Create a backup file for JS Extension
        Files.copy(Paths.get(agent.getAgentJsExtensionFileLocation()),
            Paths.get(agent.getAgentJsExtensionFileLocation() + JSExtensionBackUpFile.BACKUP),
            StandardCopyOption.REPLACE_EXISTING);

        // Set reset flag for reverting Javascript Extension file after test completes
        resetExtensionFile = true;

        LOGGER
            .info("customJsFunctionAndMetricsTest: Updating apmbrtmextensibility.js -- scriptToInsertforcustomJSFunction: "
                + JSExtensionTestScript.CUSTOM_JS_FUNCTION_SCRIPT
                + " After: "
                + JSExtensionTestScript.CUSTOM_JS_FUNCTION);

        // Update Javascript Extension file for for Custom Javascript function
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_JS_FUNCTION_SCRIPT,
            JSExtensionTestScript.CUSTOM_JS_FUNCTION, false);

        LOGGER
            .info("customJsFunctionAndMetricsTest: Updating apmbrtmextensibility.js -- scriptToInsertforcustomJSMetric: "
                + JSExtensionTestScript.CUSTOM_JS_METRIC_SCRIPT
                + " After: "
                + JSExtensionTestScript.CUSTOM_JS_METRIC_FUNCTION);

        // Update Javascript Extension file for for Custom Javascript Metrics
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_JS_METRIC_SCRIPT,
            JSExtensionTestScript.CUSTOM_JS_METRIC_FUNCTION, false);


        LOGGER
            .info("customJsFunctionAndMetricsTest: Updating agent profile to point to modified apmbrtmextensibility.js - "
                + agent.getAgentJsExtensionFileLocation());
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_EXTENSION_LOCATION,
            agent.getAgentJsExtensionFileLocation(), agent);
        // Restart application server for changes to property and Javascript Extension file
        // TODO: Create copy of updated js file here instead of doing it in the end.
        restartAppServer(false);
        CommonUtils.resetBrowser(seleniumData);

        // Launch page - Sustainability metrics will not report data unless BrowserAgent is invoked
        LOGGER.info("customJsFunctionAndMetricsTest: accessing URL:" + seleniumUrl
            + BrtmTestApp.INDEX_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.INDEX_PAGE, seleniumData);

        LOGGER.info("customJsFunctionAndMetricsTest: Verify Sustainability metric values");
        MetricUtils.verifyLastBRTMSustainabilityMetric(BrowseAgentProperties.URL_METRIC_OFF, "0",
            agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.JS_FUNCTION_METRICS_ENABLED, "1", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.JS_FUNCTION_METRICS_THRESHOLD, "0", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.BROWSER_LOGGING_ENABLED, "1", agent, em);

        // Step 4
        LOGGER.info("customJsFunctionAndMetricsTest: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE,
            seleniumData);

        LOGGER
            .info("customJsFunctionAndMetricsTest: Verify java script metrics are reported for custom function and custom metrics");

        // Validate metrics reported for page
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);

        // Validate metrics are reported for custom javascript functions
        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_CUSTOM_MATH_RANDOM,
            JavaScriptMetrics.JS_INVOCATION_COUNT_PER_INTERVAL, 5, 2, agent, em);

        MetricUtils.compareJSMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_CUSTOM_MATH_RANDOM,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_AVERAGE, 5, 2, agent, em);

        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_CUSTOM_MATH_RANDOM,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_COUNT, 5, 2, agent, em);

        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_CUSTOM_SEND_REQUEST,
            JavaScriptMetrics.JS_INVOCATION_COUNT_PER_INTERVAL, 1, 2, agent, em);

        MetricUtils.compareJSMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_CUSTOM_SEND_REQUEST,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_AVERAGE, 5, 2, agent, em);

        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_CUSTOM_SEND_REQUEST,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_COUNT, 1, 2, agent, em);

        // Validate custom metrics are reported for javascript functions.
        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_OPEN,
            JavaScriptMetrics.JS_INVOCATION_COUNT_PER_INTERVAL, 5, 2, agent, em);

        MetricUtils.compareJSMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_OPEN,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_AVERAGE, 5, 2, agent, em);

        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_OPEN,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_COUNT, 5, 2, agent, em);

        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_SEND,
            JavaScriptMetrics.JS_INVOCATION_COUNT_PER_INTERVAL, 5, 2, agent, em);

        MetricUtils.compareJSMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_SEND,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_AVERAGE, 5, 2, agent, em);

        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_SEND,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_COUNT, 5, 2, agent, em);

        // Step 5: Test for no data reported if javascript metrics are disabled
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_FUNCTION_METRICS_ENABLED,
            "false", agent);
        LOGGER.info("customJsFunctionAndMetricsTest: Wait for hot properties to load");
        CommonUtils.sleep(60000);
        CommonUtils.resetBrowser(seleniumData);

        LOGGER.info("customJsFunctionAndMetricsTest: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE,
            seleniumData);

        // Validate metrics reported for page
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);

        // Validate metrics are not reported for custom javascript functions
        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_CUSTOM_MATH_RANDOM,
            JavaScriptMetrics.JS_INVOCATION_COUNT_PER_INTERVAL, 0, 2, agent, em);


        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_CUSTOM_SEND_REQUEST,
            JavaScriptMetrics.JS_INVOCATION_COUNT_PER_INTERVAL, 0, 2, agent, em);

        // Validate custom metrics are reported for javascript functions.
        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_OPEN,
            JavaScriptMetrics.JS_INVOCATION_COUNT_PER_INTERVAL, 0, 2, agent, em);

        MetricUtils.compareJSMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_OPEN,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_AVERAGE, -1, 2, agent, em);

        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_OPEN,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_COUNT, 0, 2, agent, em);

        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_SEND,
            JavaScriptMetrics.JS_INVOCATION_COUNT_PER_INTERVAL, 0, 2, agent, em);

        MetricUtils.compareJSMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_SEND,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_AVERAGE, -1, 2, agent, em);

        MetricUtils.checkJSCountMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, JavaScriptMetrics.JS_SEND,
            JavaScriptMetrics.JS_CUSTOM_FUNC_METRIC_COUNT, 0, 2, agent, em);

        try {
            // Copy of Javascript Extension file with updated details for custome JS Function &
            // Metric test
            LOGGER.info("customJsFunctionAndMetricsTest: Copy updated JS Extension file");
            Files.copy(
                Paths.get(agent.getAgentJsExtensionFileLocation()),
                Paths.get(agent.getAgentJsExtensionFileLocation()
                    + JSExtensionBackUpFile.CUSTOM_JS_FUNC_METRICS),
                StandardCopyOption.REPLACE_EXISTING);
            // Revert Javascript Extension file after stopping agent
        } catch (IOException e) {
            LOGGER.info("customJsFunctionAndMetricsTest: Error creating a copy of updated file.");
            e.printStackTrace();
        }
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "Full"}, description = "Testcase ID: "
        + "454233; Using valid or invalid external JS extension file")
    public void jsExtensionFileUpdatesTest() throws IOException {
        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        // Deleting existing Business Services - This test runs without Business Services
        try {
            CommonUtils.deleteBizDef(em);
            LOGGER.info("jsExtensionFileUpdatesTest: Deleted Business Definitions");
        } catch (Exception e) {
            LOGGER
                .info("jsExtensionFileUpdatesTest: Somethings went wrong. Unable to delete the file");
            e.printStackTrace();
        }

        // STEP 1
        LOGGER
            .info("jsExtensionFileUpdatesTest: Step 1 - Updating properties for JS Extension File test");
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AGENT_LOGGING_LEVEL,
            "INFO, logfile", agent);
        CommonUtils.updateIntroscopeAgentProfileForPropertyDelete(
            BrowseAgentProperties.BROWSER_AGENT_LOGGING_LEVEL, agent);
        LOGGER
            .info("jsExtensionFileUpdatesTest: Updating agent profile to point to valid apmbrtmextensibility.js - "
                + agent.getAgentJsExtensionFileLocation());
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_EXTENSION_LOCATION,
            agent.getAgentHome() + AgentDefaults.DEFAULT_EXTERNAL_JS_EXTENSION_FILE, agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.PAGE_LOAD_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.PAGE_LOAD_METRICS_THRESHOLD,
            "0", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.BROWSER_LOGGING_ENABLED,
            "true", agent);

        // Step 2 - Update JS Extension File - Using Custom Page metrics
        // Create a backup File
        Files.copy(Paths.get(agent.getAgentJsExtensionFileLocation()),
            Paths.get(agent.getAgentJsExtensionFileLocation() + JSExtensionBackUpFile.BACKUP),
            StandardCopyOption.REPLACE_EXISTING);

        // Set reset flag for reverting Javascript Extension file after test completes
        resetExtensionFile = true;

        LOGGER
            .info("jsExtensionFileUpdatesTest: Step 2 - Updating apmbrtmextensibility.js -- stringToInsertforcustomPageMetrics: "
                + JSExtensionTestScript.CUSTOM_PAGE_SCRIPT
                + " After: "
                + JSExtensionTestScript.CUSTOM_PAGE_FUNCTION);

        // Update Javascript Extension file for for Custom Page Metrics
        FileUtils.insertToFile(agent.getAgentJsExtensionFileLocation(),
            JSExtensionTestScript.CUSTOM_PAGE_SCRIPT, JSExtensionTestScript.CUSTOM_PAGE_FUNCTION,
            false);

        LOGGER
            .info("jsExtensionFileUpdatesTest: Updating agent profile to point to modified apmbrtmextensibility.js - "
                + agent.getAgentJsExtensionFileLocation());
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_EXTENSION_LOCATION,
            agent.getAgentJsExtensionFileLocation(), agent);

        // Get current date and time for checking log messages
        Date agentLogStartTime = Calendar.getInstance().getTime();
        LOGGER.info("jsExtensionFileUpdatesTest: Start time for checking log messages is: "
            + agentLogStartTime);

        // Restart application server for changes to property and Javascript Extension file
        // TODO: Change agent log file from default to a different file. Shorter file will be faster
        // to check and can be backed up.
        // TODO: Create copy of updated js file here instead of doing it in the end.
        restartAppServer(false);
        CommonUtils.resetBrowser(seleniumData);

        // Launch page - Sustainability metrics will not report data unless BrowserAgent is invoked
        // after agent startup
        LOGGER.info("jsExtensionFileUpdatesTest: accessing URL:" + seleniumUrl
            + BrtmTestApp.INDEX_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.INDEX_PAGE, seleniumData);

        LOGGER.info("jsExtensionFileUpdatesTest: Verify Sustainability metric values");
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.PAGE_LOAD_METRICS_ENABLED, "1", agent, em);
        MetricUtils.verifyLastBRTMSustainabilityMetric(
            BrowseAgentProperties.PAGE_LOAD_METRICS_THRESHOLD, "0", agent, em);

        // Step 3
        LOGGER.info("jsExtensionFileUpdatesTest: Step 3 - accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE,
            seleniumData);

        String metricPath =
            MetricUtils.createPageMetricPathNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE, agent);
        LOGGER.info("jsExtensionFileUpdatesTest: metricPath is : " + metricPath);

        String[] validFileMessages1 = {LogMessages.INFO, LogMessages.GET_JS_EXT_FILE_MSG};
        LOGGER.info("jsExtensionFileUpdatesTest: Checking for log message: " + validFileMessages1
            + " in File " + agent.getAgentLogFile() + " after: " + agentLogStartTime);
        CommonUtils.customAssertTrue(CommonUtils.checkForMultipleMsgs(validFileMessages1,
            agent.getAgentLogFile(), agentLogStartTime),
            "Failed to find expected message in agent log.");

        String[] validFileMessages2 =
            {LogMessages.INFO, LogMessages.VALID_JS_EXT_FILE_MSG,
                    agent.getAgentHome() + AgentDefaults.DEFAULT_EXTERNAL_JS_EXTENSION_FILE};
        LOGGER.info("jsExtensionFileUpdatesTest: Checking for log message: " + validFileMessages2
            + " in File " + agent.getAgentLogFile() + " after: " + agentLogStartTime);
        CommonUtils.customAssertTrue(CommonUtils.checkForMultipleMsgs(validFileMessages2,
            agent.getAgentLogFile(), agentLogStartTime),
            "Failed to find expected message in agent log.");

        LOGGER.info("jsExtensionFileUpdatesTest: Verify Custom Page metrics report data");

        // Validate Custom Page Metrics
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, 5, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, 1, 2, em);

        // Step 4.1 - JS Extension File property is blank - Should use default property
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_EXTENSION_LOCATION, "",
            agent);

        // Get current date and time for checking log messages
        agentLogStartTime = Calendar.getInstance().getTime();
        LOGGER
            .info("jsExtensionFileUpdatesTest: Step 4.1 - Start time for checking log messages is: "
                + agentLogStartTime);

        // Restart application server for Javascript Extension file property change
        restartAppServer(false);
        CommonUtils.resetBrowser(seleniumData);

        LOGGER.info("jsExtensionFileUpdatesTest: : accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE,
            seleniumData);

        String[] defaultFileMessage = {LogMessages.INFO, LogMessages.USING_BUILTIN_JS_EXT_FILE_MSG};
        LOGGER.info("jsExtensionFileUpdatesTest: Checking for log message: " + defaultFileMessage
            + " in File " + agent.getAgentLogFile() + " after: " + agentLogStartTime);
        CommonUtils.customAssertTrue(CommonUtils.checkForMultipleMsgs(defaultFileMessage,
            agent.getAgentLogFile(), agentLogStartTime),
            "Failed to find expected message in agent log.");

        LOGGER
            .info("jsExtensionFileUpdatesTest: 4.1 - Verify Custom Page metrics do not report data");

        // Validate Custom Page Metrics do not report data
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);
        MetricUtils
            .compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, -1, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, -1, 2, em);

        // Step 4.2 - JS Extension File property is deleted
        CommonUtils.updateIntroscopeAgentProfileForPropertyDelete(
            BrowseAgentProperties.JS_EXTENSION_LOCATION, agent);

        // Get current date and time for checking log messages
        agentLogStartTime = Calendar.getInstance().getTime();
        LOGGER.info("jsExtensionFileUpdatesTest: 4.2 - Start time for checking log messages is: "
            + agentLogStartTime);

        // Restart application server for Javascript Extension file property change
        restartAppServer(false);
        CommonUtils.resetBrowser(seleniumData);

        LOGGER.info("jsExtensionFileUpdatesTest: : accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE,
            seleniumData);

        LOGGER.info("jsExtensionFileUpdatesTest: Checking for log message: " + defaultFileMessage
            + " in File " + agent.getAgentLogFile() + " after: " + agentLogStartTime);
        CommonUtils.customAssertTrue(CommonUtils.checkForMultipleMsgs(defaultFileMessage,
            agent.getAgentLogFile(), agentLogStartTime),
            "Failed to find expected message in agent log.");

        LOGGER.info("jsExtensionFileUpdatesTest: Verify Custom Page metrics do not report data");

        // Validate Custom Page Metrics do not report data
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);
        MetricUtils
            .compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, -1, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, -1, 2, em);

        // Step 5 -- File does not exist
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_EXTENSION_LOCATION,
            agent.getAgentHome() + AgentDefaults.BAD_EXTERNAL_JS_EXTENSION_FILE, agent);

        // Set new start time for log file parsing
        agentLogStartTime = Calendar.getInstance().getTime();
        LOGGER
            .info("jsExtensionFileUpdatesTest: Step 5 - Start time for checking log messages is: "
                + agentLogStartTime);

        // Restart application server for change to and Javascript Extension file property
        restartAppServer(false);
        CommonUtils.resetBrowser(seleniumData);

        LOGGER.info("jsExtensionFileUpdatesTest: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE,
            seleniumData);

        // Validate Log file is has log message for file being empty or not-readable @WARN level
        String[] inValidFileMessage1 =
            {LogMessages.WARN, agent.getAgentHome() + AgentDefaults.BAD_EXTERNAL_JS_EXTENSION_FILE,
                    LogMessages.INVALID_JS_EXT_FILE_MSG};
        LOGGER.info("jsExtensionFileUpdatesTest: Checking for log messages: " + LogMessages.WARN
            + "," + agent.getAgentHome() + AgentDefaults.BAD_EXTERNAL_JS_EXTENSION_FILE + " and "
            + LogMessages.INVALID_JS_EXT_FILE_MSG + " in File " + agent.getAgentLogFile()
            + " after: " + agentLogStartTime);
        CommonUtils.customAssertTrue(CommonUtils.checkForMultipleMsgs(inValidFileMessage1,
            agent.getAgentLogFile(), agentLogStartTime),
            "Failed to find expected message in agent log.");

        String[] inValidFileMessage2 =
            {LogMessages.INFO, LogMessages.USING_BUILTIN_JS_EXT_FILE_MSG};
        LOGGER.info("jsExtensionFileUpdatesTest: Checking for log message: " + inValidFileMessage2
            + " in File " + agent.getAgentLogFile() + " after: " + agentLogStartTime);
        CommonUtils.customAssertTrue(CommonUtils.checkForMultipleMsgs(inValidFileMessage2,
            agent.getAgentLogFile(), agentLogStartTime),
            "Failed to find expected message in agent log.");

        LOGGER.info("jsExtensionFileUpdatesTest: Verify Custom Page metrics do not report data");

        // Validate Custom Page Metrics do not report data
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);
        MetricUtils
            .compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, -1, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, -1, 2, em);

        // Step 6 -- File is empty

        // Delete old file if it exists
        String emptyFile = agent.getAgentHome() + AgentDefaults.EMPTY_EXTERNAL_JS_EXTENSION_FILE;
        if (new File(emptyFile).exists()) {
            LOGGER
                .info("jsExtensionFileUpdatesTest: Step 6 - : Deleting old empty JS Extension file - "
                    + emptyFile);
            new File(emptyFile).delete();
        }

        // Create an empty file
        if (new File(emptyFile).createNewFile()) {
            LOGGER
                .info("jsExtensionFileUpdatesTest: Step 6 - jsExtensionFileUpdatesTest: Step 6 - Create an empty file - "
                    + emptyFile);
        } else {
            LOGGER.info("jsExtensionFileUpdatesTest: Step 6 - Unable to create new empty file - "
                + emptyFile);
        }

        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.JS_EXTENSION_LOCATION,
            agent.getAgentHome() + AgentDefaults.EMPTY_EXTERNAL_JS_EXTENSION_FILE, agent);

        // Set new start time for log file parsing
        agentLogStartTime = Calendar.getInstance().getTime();
        LOGGER
            .info("jsExtensionFileUpdatesTest: Step 6 - Start time for checking log messages is: "
                + agentLogStartTime);

        // Restart application server for change to and Javascript Extension file property
        // TODO: Create copy of updated js file here instead of doing it in the end.
        restartAppServer(false);
        CommonUtils.resetBrowser(seleniumData);

        LOGGER.info("jsExtensionFileUpdatesTest: accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE + " using webdriver");
        CommonUtils.launchSinglePageNoRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE,
            seleniumData);

        // Validate Log file is has log message for file being empty or not-readable @WARN level
        String[] emptyFileMessage1 =
            {LogMessages.WARN,
                    agent.getAgentHome() + AgentDefaults.EMPTY_EXTERNAL_JS_EXTENSION_FILE,
                    LogMessages.EMPTY_JS_EXT_FILE_MSG};
        LOGGER.info("jsExtensionFileUpdatesTest: Checking for log message: " + LogMessages.WARN
            + " -- " + agent.getAgentHome() + AgentDefaults.EMPTY_EXTERNAL_JS_EXTENSION_FILE
            + " -- & -- " + LogMessages.EMPTY_JS_EXT_FILE_MSG + " in File "
            + agent.getAgentLogFile() + " after: " + agentLogStartTime);
        CommonUtils.customAssertTrue(CommonUtils.checkForMultipleMsgs(emptyFileMessage1,
            agent.getAgentLogFile(), agentLogStartTime),
            "Failed to find expected message in agent log.");

        String[] emptyFileMessage2 = {LogMessages.INFO, LogMessages.USING_BUILTIN_JS_EXT_FILE_MSG};
        LOGGER.info("jsExtensionFileUpdatesTest: Checking for log message: " + LogMessages.INFO
            + " -- " + LogMessages.USING_BUILTIN_JS_EXT_FILE_MSG + " in File "
            + agent.getAgentLogFile() + " after: " + agentLogStartTime);
        CommonUtils.customAssertTrue(CommonUtils.checkForMultipleMsgs(emptyFileMessage2,
            agent.getAgentLogFile(), agentLogStartTime),
            "Failed to find expected message in agent log.");

        LOGGER.info("jsExtensionFileUpdatesTest: Verify Custom Page metrics do not report data");

        // Validate Custom Page Metrics do not report data
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);
        MetricUtils
            .compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_AVERAGE, -1, 2, em);
        MetricUtils.compareMetrics(metricPath, BrowserMetrics.CUSTOM_PAGE_METRIC_COUNT, -1, 2, em);

        try {
            // Make a copy of Javascript Extension file with updated details for JSExtension File
            // updates test
            LOGGER.info("jsExtensionFileUpdatesTest: Copy updated JS Extension file");
            Files.copy(Paths.get(emptyFile),
                Paths.get(agent.getAgentJsExtensionFileLocation() + JSExtensionBackUpFile.EMPTY),
                StandardCopyOption.REPLACE_EXISTING);
            // Revert Javascript Extension file after stopping agent
        } catch (IOException e) {
            LOGGER.info("jsExtensionFileUpdatesTest: Error creating a copy of updated file.");
            e.printStackTrace();
        }
    }

}
