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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.HTTPMethodTypeEnum;
import com.ca.apm.eum.datamodel.NavigationTiming;
import com.ca.apm.eum.datamodel.Page;
import com.ca.apm.eum.datamodel.Resource;
import com.ca.apm.eum.datamodel.ThinkTime;
import com.ca.apm.tests.test.selenium.EUMValidationUtils.PageType;
import com.ca.apm.tests.test.selenium.jQueryAjax;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.JQueryAjaxTestApp;

/**
 * This class holds a collection of selenium tests covering ajax Browser Agent functionality
 *
 */

public class AJAXTests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(AJAXTests.class);

    public AJAXTests() {

    }

    public AJAXTests(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }


    /**
     * Basic ajax validation and CORS
     */

    @Test
    public void testAjaxValidValues_454733_454739_454742() {
        try {

            // Step 1 - disable page (enabled by default), enable ajax(true by default),
            // disable js funtion(default already false)
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setPageLoadMetricsEnabled(false);
            attrs.setAjaxMetricsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "disablePageEnableAjax");

            // Step 2 - open test page, ignore any metrics (page is disabled anyway)
            String fullUrlToTestPage = getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE;
            WebDriver webDriver = openPage(fullUrlToTestPage);

            // Put in 3 for input, 1000 for delay and click
            int inputValue = 3;
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));

            int delay = 1000;
            WebElement delayInputElement = webDriver.findElement(By.id("Delay"));
            delayInputElement.sendKeys(Integer.toString(delay));

            Date testStart = new Date();
            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            Map<PayloadTypes, AbstractPayloadType> typesMap =
                PayloadUtils.generateTypesMap(PayloadTypes.AJAX_TYPE);
            AbstractPayloadType ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(inputValue);

            List<MetricPostRecord> recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT
                            * inputValue, testStart, typesMap);

            List<Resource> resourceList = EUMValidationUtils.extractAjax(recordList);

            Assert.assertTrue(resourceList.size() == inputValue);

            // The meat of step2 validation done here...
            EUMValidationUtils.validateAjaxGetLocal(resourceList, fullUrlToTestPage);

            // ALM threshold Test 454742
            attrs = collectorConfig.getBaAttributes();
            attrs.setAjaxMetricsThreshold(10000);
            baTestCollector.updateConfiguration(collectorConfig, "ajaxThreshold10000");

            webDriver = openPage(fullUrlToTestPage);

            inputValue = 1;

            valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));

            testStart = new Date();
            sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            recordList = null;
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT,
                        testStart, typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertTrue(recordList.size() == 0,
                "No records expected, ajax threshold should be too high");

            // restore ajax threshold back to 0
            attrs = collectorConfig.getBaAttributes();
            attrs.setAjaxMetricsThreshold(0);
            baTestCollector.updateConfiguration(collectorConfig, "ajaxThresholdSetTo0");


            // Step 3 - disable ajax
            attrs = collectorConfig.getBaAttributes();
            attrs.setAjaxMetricsEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disableAjax");

            // Step 4 - repeat, get GETLOcalDomain.jsp, no data
            webDriver = openPage(fullUrlToTestPage);

            // just do a single record so dont have to wait long
            inputValue = 1;

            valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));

            testStart = new Date();
            sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            recordList = null;
            typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT,
                        testStart, typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertTrue(recordList.size() == 0, "No records expected, ajax disabled");


            // This next portion covers ALM test ID 454739 step 3 (the part that didnt overlap )

            // first must renable ajax
            attrs = collectorConfig.getBaAttributes();
            attrs.setAjaxMetricsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "enableAjax");


            fullUrlToTestPage = getTestAppURL() + BrtmTestApp.GET_CORS_PAGE;
            webDriver = openPage(fullUrlToTestPage);
            inputValue = 3;

            valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));

            delay = 1000;
            delayInputElement = webDriver.findElement(By.id("Delay"));
            delayInputElement.sendKeys(Integer.toString(delay));

            testStart = new Date();
            sendButton = webDriver.findElement(By.name("GetRemoteFile"));
            sendButton.click();

            typesMap = PayloadUtils.generateTypesMap(PayloadTypes.AJAX_TYPE);
            ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(inputValue);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_CORS_MAX_EXPECTED_SINGLE_GET_WAIT, testStart, typesMap);

            resourceList = EUMValidationUtils.extractAjax(recordList);

            Assert.assertTrue(resourceList.size() == inputValue);

            // this is the meat of the validation
            EUMValidationUtils.validateAjaxGetRemote(resourceList, fullUrlToTestPage);


            // Ajax on Soft page validation
            closeDriver();
            Thread.currentThread().sleep(3000);
            webDriver = getDriver();
            webDriver.get(getTestAppURL() + BrtmTestApp.SPA_INDEX);
            WebElement redLink = webDriver.findElement(By.linkText("Red"));
            redLink.click();

            // Dont care about the first payload, tested in SPATests.java
            sleepMetricFrequency();

            WebElement localAjax = webDriver.findElement(By.name("localajaxCall"));
            testStart = new Date();
            localAjax.click();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.SOFT_PAGE_TYPE);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.STANDARD_CLICK_WAIT, testStart, typeList);


            List<Page> pageList = EUMValidationUtils.extractPagesByType(recordList, PageType.SOFT);
            Assert.assertTrue(pageList.size() == 1);

            List<Resource> ajaxResourceList = EUMValidationUtils.extractAjaxFromPages(pageList);
            Assert.assertTrue(ajaxResourceList.size() == 1);

            Resource resource = ajaxResourceList.get(0);
            Assert.assertTrue(resource.getRequest().getUrl()
                .endsWith(EUMValidationUtils.SAMPLE_TXT_FILE));

            // Validate the actual ajax metrics
            String[] hostPortPage =
                EUMValidationUtils.getHostPortPage(getTestAppURL() + BrtmTestApp.SPA_INDEX
                    + "|#/red");
            String host = hostPortPage[0];
            String port = hostPortPage[1];
            String page = hostPortPage[2];

            // This calls assert on metric paths
            EUMValidationUtils.validateAjax(ajaxResourceList, host, port, page,
                HTTPMethodTypeEnum.GET, EUMValidationUtils.SAMPLE_TXT_FILE, host, port,
                EUMValidationUtils.SAMPLE_TXT_SIZE);
        } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
            LOGGER.fatal("Unexpected timeout from waitForNextNotification, failing test",
                timeoutException);

            assertFail(timeoutException);
        } catch (Exception e) {
            LOGGER.fatal("Unexpected exception, failing test", e);

            assertFail(e);
        }
    }

    @Test
    public void testJquerySupport_455850() {
        try {

            // Pre-req Step - No changes needed to default profile in collector

            Date testStart;
            Map<PayloadTypes, AbstractPayloadType> typesMap;
            AbstractPayloadType ajaxType;
            List<MetricPostRecord> recordList;
            List<Resource> resourceList;

            String fullUrlToTestPage, host, port, page;
            String[] hostPortPage;
            WebDriver webDriver = getDriver();
            Map<String, jQueryAjax> jQuery1ExpectedResult, jQuery2ExpectedResult, jQuery3ExpectedResult;

            // Step 1 - Test for JQuery 1.x. Load page
            testStart = new Date();
            fullUrlToTestPage = getTestAppURL() + BrtmTestApp.JQUERY_1_X_PAGE;
            LOGGER.info("Starting jQuery1.x test @ " + testStart + " by loading page "
                + fullUrlToTestPage);
            webDriver.get(fullUrlToTestPage);

            typesMap = PayloadUtils.generateTypesMap(PayloadTypes.AJAX_TYPE);
            ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(JQueryAjaxTestApp.AJAX_CALLS_COUNT);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    JQueryAjaxTestApp.JQUERY_MAX_LOAD_WAIT, testStart, typesMap);

            resourceList = EUMValidationUtils.extractAjax(recordList);
            // Validate # of Resource objects is as expected
            Assert.assertTrue(resourceList.size() == JQueryAjaxTestApp.AJAX_CALLS_COUNT,
                "Resource List size is: " + resourceList.size()
                    + " does not match expected value of " + JQueryAjaxTestApp.AJAX_CALLS_COUNT);

            hostPortPage = EUMValidationUtils.getHostPortPage(fullUrlToTestPage);
            host = hostPortPage[0];
            port = hostPortPage[1];
            page = hostPortPage[2];
            // Create map of expected Result
            jQuery1ExpectedResult = createJQueryExpectedResultMap(host, port, page);
            // Validate Resource data
            EUMValidationUtils.validateAjaxTestJQuery(resourceList, jQuery1ExpectedResult, true);

            // Test for JQuery 2.x
            testStart = new Date();
            fullUrlToTestPage = getTestAppURL() + BrtmTestApp.JQUERY_2_X_PAGE;
            LOGGER.info("Starting jQuery2.x test @ " + testStart + " by loading page "
                + fullUrlToTestPage);
            webDriver.get(fullUrlToTestPage);

            typesMap = PayloadUtils.generateTypesMap(PayloadTypes.AJAX_TYPE);
            ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(JQueryAjaxTestApp.AJAX_CALLS_COUNT);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    JQueryAjaxTestApp.JQUERY_MAX_LOAD_WAIT, testStart, typesMap);

            resourceList = EUMValidationUtils.extractAjax(recordList);
            // Validate # of Resource objects is as expected
            Assert.assertTrue(resourceList.size() == JQueryAjaxTestApp.AJAX_CALLS_COUNT,
                "Resource List size is: " + resourceList.size()
                    + " does not match expected value of " + JQueryAjaxTestApp.AJAX_CALLS_COUNT);

            hostPortPage = EUMValidationUtils.getHostPortPage(fullUrlToTestPage);
            host = hostPortPage[0];
            port = hostPortPage[1];
            page = hostPortPage[2];
            // Create map of expected Result
            jQuery2ExpectedResult = createJQueryExpectedResultMap(host, port, page);
            // Validate Resource data
            EUMValidationUtils.validateAjaxTestJQuery(resourceList, jQuery2ExpectedResult, false);

            // Test for JQuery 3.x
            testStart = new Date();
            fullUrlToTestPage = getTestAppURL() + BrtmTestApp.JQUERY_3_X_PAGE;
            LOGGER.info("Starting jQuery3.x test @ " + testStart + " by loading page "
                + fullUrlToTestPage);
            webDriver.get(fullUrlToTestPage);

            typesMap = PayloadUtils.generateTypesMap(PayloadTypes.AJAX_TYPE);
            ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(JQueryAjaxTestApp.AJAX_CALLS_COUNT);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    JQueryAjaxTestApp.JQUERY_MAX_LOAD_WAIT, testStart, typesMap);

            resourceList = EUMValidationUtils.extractAjax(recordList);
            // Validate # of Resource objects is as expected
            Assert.assertTrue(resourceList.size() == JQueryAjaxTestApp.AJAX_CALLS_COUNT,
                "Resource List size is: " + resourceList.size()
                    + " does not match expected value of " + JQueryAjaxTestApp.AJAX_CALLS_COUNT);

            hostPortPage = EUMValidationUtils.getHostPortPage(fullUrlToTestPage);
            host = hostPortPage[0];
            port = hostPortPage[1];
            page = hostPortPage[2];
            // Create map of expected Result
            jQuery3ExpectedResult = createJQueryExpectedResultMap(host, port, page);
            // Validate Resource data
            EUMValidationUtils.validateAjaxTestJQuery(resourceList, jQuery3ExpectedResult, false);

        } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
            LOGGER.fatal("Unexpected timeout from waitForNextNotification, failing test",
                timeoutException);
            assertFail(timeoutException);
        } catch (Exception e) {
            LOGGER.fatal("Unexpected exception, failing test", e);
            assertFail(e);
        }
    }

    private Map<String, jQueryAjax> createJQueryExpectedResultMap(String host, String port,
        String page) {

        Map<String, jQueryAjax> jQueryExpectedResult = new HashMap<String, jQueryAjax>();

        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT1_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT1_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT1_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_WITH_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT1A_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT1A_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT1A_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT2_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT2_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT2_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_WITH_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT2A_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT2A_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT2A_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_WITH_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT3_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT3_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT3_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_WITH_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT4_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT4_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT4_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT5_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT5_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT5_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_WITH_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT6_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT6_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT6_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT7_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT7_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT7_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_WITH_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT8_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT8_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT8_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT9_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT9_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT9_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT9A_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT9A_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT9A_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT10_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.POST, JQueryAjaxTestApp.CONTENT10_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT10_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_WITH_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT11_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.POST, JQueryAjaxTestApp.CONTENT11_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT11_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT12_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT12_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT12_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_ALL_JQUERY));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT13_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT13_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT13_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_WITH_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT13A_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT13A_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT13A_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT13B_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT13B_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT13B_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT14_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT14_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT14_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT15_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT15_TXT_FILE,
            BATestCollectorUtils.HTTP_NOT_FOUND, JQueryAjaxTestApp.CONTENT15_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.CONTENT16_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.CONTENT16_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.CONTENT16_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1));
        jQueryExpectedResult.put(JQueryAjaxTestApp.SAMPLE1_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.SAMPLE1_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.SAMPLE1_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_ALL_JQUERY));
        jQueryExpectedResult.put(JQueryAjaxTestApp.SAMPLE_KEY, new jQueryAjax(host, port, page,
            HTTPMethodTypeEnum.GET, JQueryAjaxTestApp.SAMPLE_TXT_FILE,
            BATestCollectorUtils.HTTP_OK, JQueryAjaxTestApp.SAMPLE_TXT_SIZE,
            JQueryAjaxTestApp.METRIC_COUNT_ALL_JQUERY));

        return jQueryExpectedResult;
    }

    /**
     * TEMP TEST - This test is an example using the prototyped format types
     */

    @Test
    public void testNewTypesExample() {
        try {

            WebDriver webDriver = getDriver();

            // Start a new test that launches page and see the one expected record (page metrics)
            Date testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

            List<MetricPostRecord> recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // Redundant
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            List<NavigationTiming> myPage = EUMValidationUtils.extractPage(recordList);

            // DO stuff with myPage here...


            // Attributes attrs = collectorConfig.getBaAttributes();
            // attrs.setPageLoadMetricsEnabled(false);
            // baTestCollector.updateConfiguration(collectorConfig, "disablePage");

            testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);



            typeList =
                PayloadUtils
                    .generateTypesList(PayloadTypes.PAGE_TYPE, PayloadTypes.THINK_TIME_TYPE);

            // If for example
            // Map<PayloadTypes, AbstractPayloadType> typesMap =
            // PayloadUtils.generateTypesMap(PayloadTypes.PAGE_TYPE, PayloadTypes.AJAX_TYPE);
            // AbstractPayloadType ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            // ajaxType.setCount(3);


            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // Redundant...
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertTrue(EUMValidationUtils.hasThinkTime(recordList));

            List<ThinkTime> myTimes = EUMValidationUtils.extractThinkTimes(recordList);

            // Do stuff with myTimes here...

            System.out.println("ThinkTime myTimes (expect item)---------------------->" + myTimes);


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
