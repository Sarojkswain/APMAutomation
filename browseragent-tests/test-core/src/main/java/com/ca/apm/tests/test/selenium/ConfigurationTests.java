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

package com.ca.apm.tests.test.selenium;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/**
 * This class holds a collection of selenium tests covering configuration
 *
 */

public class ConfigurationTests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationTests.class);

    public ConfigurationTests() {

    }

    public ConfigurationTests(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }

    /**
     * Tests that 204 is sent on the next metric post when the config was updated just prior.
     */

    @Test
    public void test204UpdatedConfigIsValid_454821() {

        try {

            // Step 1 - enable page, enable ajax metrics
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setPageLoadMetricsEnabled(true);
            attrs.setAjaxMetricsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "enablePageAndAjax");

            // Step 2 - verify page reported, ajax not reported

            // Get the driver to be used for this test...
            WebDriver webDriver = getDriver();

            // Start a new test that launches page and see the one expected record (page metrics)
            Date testStart = new Date();
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            List<MetricPostRecord> recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // verify we DO have page metrics
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // verify we DONT have ajax metrics - Yes redundant since no ajax activity on getlocal
            // domain
            Assert.assertFalse(EUMValidationUtils.hasAjaxMetrics(recordList));


            // Step 3 - Click the button to get ajax
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys("1");

            // reset the starttime and click the send button.. ajax metrics add to the count
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);
            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT, testStart,
                    typeList);

            // Shouldnt see any page metrics here
            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));

            // verify we DO have ajax metrics
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));

            // Step 4 - disable ajax metrics
            collectorConfig = baTestCollector.getConfiguration();
            attrs = collectorConfig.getBaAttributes();
            attrs.setAjaxMetricsEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disableAjaxMetrics");

            // Step 5 - create page activity so the 204 is sent back
            testStart = new Date();

            sendButton.click();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT, testStart,
                    typeList);

            // Shouldnt see any page metrics here
            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));

            // verify we still DO have ajax metrics - this will get the 204
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));

            // Allow some time for the browser agent to response to 204, download and apply new
            // config...

            Thread.currentThread().sleep(3000);

            // Step 6 - create more activity on the page, not reload, this time expect NO ajax
            // metrics
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.THINK_TIME_TYPE);
            sendButton.click();

            // Since ajax will now be disabled there will be no records. This wait is expected to
            // timeout
            recordList = null;
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT,
                        testStart, typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            // Confirm not seeing any page nor ajax.
            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertFalse(EUMValidationUtils.hasAjaxMetrics(recordList));

            Assert.assertTrue(checkBrowserLogForMessage("AJAX Metrics are DISABLED"));

        } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
            LOGGER.fatal("Unexpected timeout from waitForNextNotification, failing test",
                timeoutException);

            assertFail(timeoutException);
        } catch (Exception e) {
            LOGGER.fatal("Unexpected exception, failing test", e);

            assertFail(e);
        }
    }

    /**
     * Tests metric frequency valid values
     */

    @Test
    public void testMetricFrequency_454830() {

        try {

            // Step 1 - metric freq 0, default value
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setJsFunctionMetricsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "enableJSFunction");

            // Step 2 - load getlocal domain, click button verify 3 metrics
            WebDriver webDriver = openPage(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);


            // Start a new test that launches page and see the one expected record (page metrics)
            Date testStart = new Date();
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys("1");
            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            List<MetricPostRecord> recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT, testStart,
                    typeList);

            // verify we DO have ajax metrics and JS Function -
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));



            // Step 3 - set metric freq to 15000

            long metricFrequency = 15000;

            collectorConfig = baTestCollector.getConfiguration();
            attrs = collectorConfig.getBaAttributes();
            attrs.setMetricFrequency(metricFrequency);
            baTestCollector.updateConfiguration(collectorConfig, "setMetricFrequency15000");


            // Step 4 - reload and click the button verify 15000 and comes as single record
            closeDriver();
            webDriver = openPage(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);
            testStart = new Date();

            valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys("1");
            sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT
                        + metricFrequency, testStart, typeList);


            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));


            // step 5 - load errors page, click button verify get errors under batch mode
            closeDriver();
            webDriver = openPage(getTestAppURL() + BrtmTestApp.ERROR_MULTI_ERROR_PAGE);
            testStart = new Date();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.JS_ERROR_TYPE);
            sendButton = webDriver.findElement(By.id("refError"));
            sendButton.click();

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.REFERENCE_ERROR_MAX_EXPECTED_CLICK_WAIT + metricFrequency,
                    testStart, typeList);

            Assert.assertTrue(EUMValidationUtils.hasJSError(recordList));

        } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
            LOGGER.fatal("Unexpected timeout from waitForNextNotification, failing test",
                timeoutException);

            assertFail(timeoutException);
        } catch (Exception e) {
            LOGGER.fatal("Unexpected exception, failing test", e);

            assertFail(e);
        }
    }

    /**
     * Tests the ability to disable browser agent
     */

    @Test
    public void testBAEnabled_454826() {

        try {

            // Step 1 & 2 - confirm page and ajax metrics

            // Get the driver to be used for this test...
            WebDriver webDriver = getDriver();

            // Start a new test that launches page and see the one expected record (page metrics)
            Date testStart = new Date();
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            List<MetricPostRecord> recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // Now enter 1 in the "Number of requests" text box.
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys("1");

            // reset the starttime and click the send button.. ajax metrics add to the count
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);
            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT, testStart,
                    typeList);

            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));

            // step 3 - Now change the config to disable browser agent
            collectorConfig = baTestCollector.getConfiguration();
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setBrowserAgentEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disableBA");

            // Step 3a (bonus not in alm, carry over from previous test) - disable a live page with
            // 204
            // now repeat above test (text input already contains 1)
            // Yes will get ajax records again, but this "send" will cause the 204 to be seen on
            // response
            // by the client requesting the config again which will see browser agent is disabled
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);
            sendButton.click();

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT, testStart,
                    typeList);

            // Yes should still have ajax
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));

            // Not to cause edge cases, allow the client to read the profile
            Thread.currentThread().sleep(3000);

            recordList = null;
            try {
                testStart = new Date();
                // Now just wait for even a single record... we shouldnt see anymore records!
                typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
                sendButton.click();
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT,
                        testStart, typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertFalse(EUMValidationUtils.hasAjaxMetrics(recordList));

            // Step 4 - reopen the page no page
            testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);
            recordList = null;
            try {
                typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            // Confirm not seeing any page nor ajax, actually nothing.
            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertFalse(EUMValidationUtils.hasAjaxMetrics(recordList));

            Assert.assertTrue(checkBrowserLogForMessage("Browser Agent is DISABLED."));

        } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
            LOGGER.fatal("Unexpected timeout from waitForNextNotification, failing test",
                timeoutException);

            assertFail(timeoutException);
        } catch (Exception e) {
            LOGGER.fatal("Unexpected exception, failing test", e);

            assertFail(e);
        }
    }


    /**
     * This tests valid configuration url in the profile
     */

    @Test
    public void testValidConfigUrl_454832() {

        try {

            // Step 1 - set collector url to valid value
            // Already done

            // Step 2 - load GETLocalDomain.jsp request 1 file, verify page and ajax

            WebDriver webDriver = getDriver();

            // Start a new test that launches page and see the one expected record (page metrics)
            Date testStart = new Date();
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            List<MetricPostRecord> recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // set 1 request file and click the button
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys("1");

            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT, testStart,
                    typeList);

            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));

            // Step 3 - reset the collector url to empty string
            collectorConfig = baTestCollector.getConfiguration();
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setCollectorUrl("");
            baTestCollector.updateConfiguration(collectorConfig, "badCollectorUrlString");

            // First open the page, this will get thinktime metrics, but will also allow the new
            // config to be downloaed
            openPage(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            // Step 4 open the page again
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);
            recordList = null;
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }
            Assert.assertTrue(recordList.size() == 0, "No  metrics should be reported");

            Assert
                .assertTrue(checkBrowserLogForMessage("Invalid collector URL. Disabling Browser Agent"));
        } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
            LOGGER.fatal("Unexpected timeout from waitForNextNotification, failing test",
                timeoutException);

            assertFail(timeoutException);
        } catch (Exception e) {
            LOGGER.fatal("Unexpected exception, failing test", e);

            assertFail(e);
        }
    }
}
