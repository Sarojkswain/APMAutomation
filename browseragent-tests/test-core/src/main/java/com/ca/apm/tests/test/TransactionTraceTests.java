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
 * Tests for Transaction Trace tests - BrowserAgent
 *
 * @author Legacy Browser Agent automation code
 *         Updates for TAS - gupra04
 * 
 */

package com.ca.apm.tests.test;

import com.ca.apm.tests.testbed.BrowserAgentTomcatChromeWinTestbed;
import com.ca.apm.tests.testbed.BrowserAgentTomcatFirefoxWinTestbed;
import com.ca.apm.tests.testbed.BrowserAgentTomcatIEWinTestbed;
import com.ca.apm.tests.utils.TransactionTraceUtils;
import com.ca.apm.tests.utils.constants.BusinessServiceConstants.Test_BS_BT;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;
import com.ca.apm.tests.utils.CommonUtils;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = {
        @TestBed(name = BrowserAgentTomcatChromeWinTestbed.class, executeOn = BrowserAgentTomcatChromeWinTestbed.BROWSERAGENT_MACHINE_ID),
        @TestBed(name = BrowserAgentTomcatFirefoxWinTestbed.class, executeOn = BrowserAgentTomcatFirefoxWinTestbed.BROWSERAGENT_MACHINE_ID),
        @TestBed(name = BrowserAgentTomcatIEWinTestbed.class, executeOn = BrowserAgentTomcatIEWinTestbed.BROWSERAGENT_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "gupra04")
@Test(description = "Tests for Transaction traces")
public class TransactionTraceTests extends BrowserAgentBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionTraceTests.class);

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 451459 451456 451457; Browser Agent Transaction Trace Tests")
    public void transactionTraceTests() {
        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        // Making sure the page has the snippet inserted
        LOGGER
            .info("accessing URL:" + seleniumUrl + BrtmTestApp.GET_CORS_PAGE + " using webdriver");
        seleniumData.getDriver().get(seleniumUrl + BrtmTestApp.GET_CORS_PAGE);
        CommonUtils.clearCache(seleniumData);
        CommonUtils.sleep(15000);
        seleniumData.getDriver().navigate().refresh();
        CommonUtils.sleep(15000);
        boolean result = true;

        try {
            String msg;
            NodeList params = null;

            // TransactionTraceFilter is used to choose CLW command in TransactionTraceUtils
            TransactionTraceFilter = 0;
            // If TransactionTraceFile exists from previous test, delete it
            if (new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                .exists()) {
                new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                    .delete();
            }
            // Starting a new thread for CLW so that we can generate traffic while it runs
            Thread myCLW = new Thread(new TransactionTraceUtils());
            myCLW.start();
            CommonUtils.sleep(15000);

            // Generating a single pair of transactions
            LOGGER.info("accessing URL:" + seleniumUrl + BrtmTestApp.GET_CORS_PAGE
                + " using webdriver");
            seleniumData.getDriver().get(seleniumUrl + BrtmTestApp.GET_CORS_PAGE);

            // Waiting for CLW to finish its run
            myCLW.join();

            // Verifying if the XML file got created
            msg = "TT XML was not generated correctly";
            if (!new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                .exists()) {
                result = false;
            }

            CommonUtils.customAssertTrue(result, msg);
            LOGGER.info("TT XML file found correctly.  TTest # " + TransactionTraceFilter);

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc =
                builder.parse(agent.getTransactionTraceDirectory()
                    + agent.getTransactionTraceFile());

            // Creating a searchable nodelist of the XML file with all of the Parameters

            params = doc.getElementsByTagName("Parameter");
            String GUID = "";
            result = false;
            msg =
                "Trace XML does not contain any CorBrowsGUID parameters, meaning no Browser Agent trace data is being generated!  TTest # "
                    + TransactionTraceFilter;

            LOGGER.info("Nodelist Length: " + params.getLength());
            // Checking for the first CorBrowsGUID and saving the value for later comparison
            for (int i = 0; i < params.getLength(); i++) {
                if (params.item(i).getAttributes().getNamedItem("Name") != null) {
                    if (params.item(i).getAttributes().getNamedItem("Name").getNodeValue()
                        .contains("CorBrowsGUID")) {
                        GUID = params.item(i).getAttributes().getNamedItem("Value").getNodeValue();
                        result = true;
                        break;
                    }
                }
            }

            CommonUtils.customAssertTrue(result, msg);

            // Testing whether the CorBrowsGUID appears in the XML file at least 3 times, which is
            // the minimum if the two traces from before are correlated correctly
            result = false;
            int counter = 0;

            for (int i = 0; i < params.getLength(); i++) {
                if (params.item(i).getAttributes().getNamedItem("Name") != null) {
                    if (params.item(i).getAttributes().getNamedItem("Name").getNodeValue()
                        .contains("CorBrowsGUID")) {
                        if (params.item(i).getAttributes().getNamedItem("Value").getNodeValue()
                            .contains(GUID)) {
                            counter++;
                        }
                    }
                }
            }
            LOGGER.info("counter = " + counter);
            if (counter >= 3) result = true;

            msg =
                "Browser Agent and Introscope traces do not contain matching CorBrowsGUID values.  Correlation failing!  TTest # "
                    + TransactionTraceFilter;
            CommonUtils.customAssertTrue(result, msg);

            // }

            // Testing for the presence of several other BRTM-related TT parameters and making sure
            // their values are correct
            String nodeName = "Browser Type";
            String nodeValue = CommonUtils.getBrowserName(seleniumData);
            result = TransactionTraceUtils.matchXMLValue(params, nodeName, nodeValue);

            msg =
                "Cound not find Browser Agent TT parameter " + nodeName + " with value "
                    + nodeValue;
            CommonUtils.customAssertTrue(result, msg);

            nodeName = "Browser Version";
            nodeValue = CommonUtils.getBrowserVersion(seleniumData);
            result = TransactionTraceUtils.matchXMLValue(params, nodeName, nodeValue);

            msg =
                "Cound not find Browser Agent TT parameter " + nodeName + " with value "
                    + nodeValue;
            CommonUtils.customAssertTrue(result, msg);

            nodeName = "EUM Business Transaction";
            nodeValue =
                Test_BS_BT.TEST_BUSINESS_TRANSACTION + " via " + CommonUtils.getBrowserName(seleniumData);
            result = TransactionTraceUtils.matchXMLValue(params, nodeName, nodeValue);

            msg =
                "Cound not find Browser Agent TT parameter " + nodeName + " with value "
                    + nodeValue;
            CommonUtils.customAssertTrue(result, msg);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 453416 451442; Verify no duplicated url path properties decorated in TT Test. Different BTs get created per browser")
    public void transactionTraceWithoutBTTests() {
        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        try {
            // Deleting existing BT to generate TT where there is no duplicate URL path
            CommonUtils.deleteBizDef(em);

            // Making sure the page has the snippet inserted
            LOGGER.info("accessing URL:" + seleniumUrl + BrtmTestApp.GET_CORS_PAGE
                + " using webdriver");
            seleniumData.getDriver().get(seleniumUrl + BrtmTestApp.GET_CORS_PAGE);
            CommonUtils.clearCache(seleniumData);
            CommonUtils.sleep(15000);

            LOGGER.info("accessing URL:" + seleniumUrl + BrtmTestApp.GET_CORS_PAGE
                + " using webdriver");
            seleniumData.getDriver().navigate().refresh();
            CommonUtils.sleep(15000);
            boolean result = true;

            String msg;
            NodeList params = null;

            if (new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                .exists()) {
                new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                    .delete();
            }
            // Starting a new thread for CLW so that we can generate traffic while it runs
            Thread myCLW = new Thread(new TransactionTraceUtils());
            myCLW.start();
            CommonUtils.sleep(15000);

            // Generating a single pair of transactions
            LOGGER.info("accessing URL:" + seleniumUrl + BrtmTestApp.GET_CORS_PAGE
                + " using webdriver");
            seleniumData.getDriver().get(seleniumUrl + BrtmTestApp.GET_CORS_PAGE);

            // Waiting for CLW to finish its run
            myCLW.join();

            // Verifying if the XML file got created
            msg = "TT XML was not generated correctly";
            if (!new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile())
                .exists()) {
                result = false;
            }

            CommonUtils.customAssertTrue(result, msg);
            LOGGER.info("TT XML file found correctly.  TTest # " + TransactionTraceFilter);

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc =
                builder.parse(agent.getTransactionTraceDirectory()
                    + agent.getTransactionTraceFile());

            // Creating a searchable nodelist of the XML file with all of the Parameters

            params = doc.getElementsByTagName("Parameter");
            result = false;

            LOGGER.info("Nodelist Length: " + params.getLength());
            // Checking for the first CorBrowsGUID and saving the value for later comparison
            for (int i = 0; i < params.getLength(); i++) {
                String paramName = params.item(i).getAttributes().getNamedItem("Name").toString();
                if (paramName.contains("Business Segment")) {
                    result = true;
                    int countURL = countMatches(paramName, BrtmTestApp.GET_CORS_PAGE);
                    LOGGER.info("Parameter Name: " + paramName + " contains URL Path "
                        + BrtmTestApp.GET_CORS_PAGE + " with the occurance number: " + countURL);
                    if (countURL != 1) {
                        result = false;
                        break;
                    }
                }
            }

            msg = "Transaction Trace has duplicate or no enteries for URL path.";
            CommonUtils.customAssertTrue(result, msg);

        } catch (Exception e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int countMatches(String strSource, String strFind) {
        int count = 0;
        int idx = 0;

        while ((idx = strSource.indexOf(strFind, idx)) != -1) {
            idx++;
            count++;
        }

        return count;
    }
}
