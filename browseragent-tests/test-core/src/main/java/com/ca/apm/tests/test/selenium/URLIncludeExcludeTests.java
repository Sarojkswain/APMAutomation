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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.ca.apm.eum.datamodel.Request;
import com.ca.apm.eum.datamodel.Resource;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/**
 * This class holds a collection of selenium tests covering url include/exclude tests
 *
 */

public class URLIncludeExcludeTests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(URLIncludeExcludeTests.class);

    public URLIncludeExcludeTests() {

    }

    public URLIncludeExcludeTests(SeleniumDetails details, String testAppUrl,
        String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }

    /**
     * Tests url exclude configuration
     */

    @Test
    public void testURLExclude_454760() {
        try {

            // 1. enable js function, urlExcludeList" : ["spa/#/$", "spa/green.htm"],
            String[] excludeArray = new String[] {"spa/#/$", "spa/green.htm"};
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setUrlExcludeList(excludeArray);
            attrs.setJsFunctionMetricsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "excludeGetLocalDomain");

            // 2. load page brtmtestapp/spa/#/, make ajax, click button to click for js error
            // Expect: no metrics
            // [INFO] addNewPageBucket: Page is configured to be EXCLUDED. Skipping all
            // instrumentation on this page
            WebDriver webDriver = getDriver();
            Date testStart = new Date();
            String fullPageUrl = getTestAppURL() + BrtmTestApp.SPA_INDEX;
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.ANY_TYPE);

            webDriver.get(fullPageUrl);

            WebElement localAjax = webDriver.findElement(By.name("localajaxCall"));
            localAjax.click();

            WebElement refError = webDriver.findElement(By.id("refError"));
            refError.click();

            List<MetricPostRecord> recordList = null;

            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.STANDARD_CLICK_WAIT, testStart, typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            // Needs to be revisted.
            // Assert.assertTrue(recordList.size() == 0,
            // "No records expected, should have been excluded");



            // 3. nav to red page, make local ajax, click for js error
            // expect:
            // BA reports
            // - page load metrics for Red page
            // - referrer: brtmtestapp/spa/#/
            // - prePage: brtmtestapp/spa/#/
            // - AJAX metrics for red.htm
            // - AJAX metrics for sample.txt
            // - error metrics for Red page.

            testStart = new Date();
            WebElement redLink = webDriver.findElement(By.linkText("Red"));
            redLink.click();


            // 4. nav to green, make local ajax, click for js error
            // expect:
            // - think time for Red page
            // - referrer: brtmtestapp/spa/#/
            // - prePage: brtmtestapp/spa/#/
            // - page load metrics for Green page
            // - referrer: brtmtestapp/spa/#/
            // - prePage: brtmtestapp/spa/#/red
            // - AJAX metrics for sample.txt
            // - error metrics for Green page.
            // Browser console log prints the following message:
            // [CA Browser Agent]: [INFO] xhrOpenPre: AJAX URL
            // http://localhost:8080/brtmtestapp/spa/green.htm is configured to be EXCLUDED.

            // 5. click blue, make local ajax call, click for js error
            // expect:
            // - think time for Green page
            // - referrer: brtmtestapp/spa/#/
            // - prePage: brtmtestapp/spa/#/red
            // - page load metrics for Blue page
            // - referrer: brtmtestapp/spa/#/
            // - prePage: brtmtestapp/spa/#/green
            // - AJAX metrics for blue.htm
            // - AJAX metrics for sample.txt
            // - error metrics for Blue page.

            // 6. navigate to main page, make local ajax, click js error
            // expect
            // - think time for Blue page
            // - referrer: brtmtestapp/spa/#/
            // - prePage: brtmtestapp/spa/#/green


            // /////////////////////////////////////////////
            // START OF OLD TEST
            // /////////////////////////////////////////////

            // Step 1 - assign [".*GETLocalDomain.*"] as the value of urlExcludeList property
            excludeArray = new String[] {".*GETLocalDomain.*"};
            attrs = collectorConfig.getBaAttributes();
            attrs.setUrlExcludeList(excludeArray);
            baTestCollector.updateConfiguration(collectorConfig, "excludeGetLocalDomain");

            // Step 2 - load index.html confirm page metrics
            webDriver = getDriver();

            // Start a new test that launches page and see the one expected record (page metrics)
            testStart = new Date();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            webDriver.get(getTestAppURL() + BrtmTestApp.INDEX_PAGE);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.INDEX_LOAD_WAIT, testStart, typeList);

            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));


            // Step 3 - load GETLocalDomain.jsp confirm log message:
            // Page is configured to be EXCLUDED
            //

            testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);
            // this is a page load (true) and a navigate away(true) from the first page get. 0 ajax
            recordList = null;
            typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));

            // Step 4 - load GETLocalDomainQueryParams.jsp still no metrics
            recordList = null;
            try {
                testStart = new Date();
                typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
                webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_QUERY_PARAMS_PAGE);
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_QUERY_MAX_LOAD_WAIT, testStart,
                        typeList);

            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertTrue(recordList.size() == 0,
                "Not expecting page metrics for get local domain query params");

            // Step 5 - load GETCORS.jsp, page is reproted, enter 1 and ajax is reported
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_CORS_PAGE);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_CORS_MAX_EXPECTED_LOAD_WAIT, testStart, typeList);
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));


            // Step 6 -modify exclude list add following 2 values
            // ".*cors-test.appspot.*",".*/brtmtestapp/ajaxclamp/[1234567]/sample.txt"
            excludeArray =
                new String[] {".*GETLocalDomain.*", ".*cors-test.appspot.*",
                        ".*/brtmtestapp/ajaxclamp/[1234567]/sample.txt"};
            attrs = collectorConfig.getBaAttributes();
            attrs.setUrlExcludeList(excludeArray);
            baTestCollector.updateConfiguration(collectorConfig, "ajaxExclude");

            // Step 7 - Load GETCORS.jsp enter 1, page reported and ajax not reported
            // browser message: AJAX URL https://cors-test.appspot.com/test is configured to be
            // EXCLUDED.
            // load AjaxClamp.jsp , get local file (no number). page reported, ajax blocked
            //
            testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_CORS_PAGE);

            try {
                typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_CORS_MAX_EXPECTED_LOAD_WAIT, testStart, typeList);

            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }


            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertFalse(EUMValidationUtils.hasAjaxMetrics(recordList));


            int inputValue = 1;
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));

            testStart = new Date();
            WebElement sendButton = webDriver.findElement(By.name("GetRemoteFile"));
            sendButton.click();

            // not expecting ajax
            recordList = null;
            try {
                typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_CORS_MAX_EXPECTED_SINGLE_GET_WAIT, testStart,
                        typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertTrue(recordList.size() == 0, "Not expecting ajax metrics ");

            // load AjaxClap.jsp
            testStart = new Date();

            webDriver.get(getTestAppURL() + BrtmTestApp.AJAX_CLAMP_PAGE);

            recordList = null;
            try {
                typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.AJAX_CLAMP_MAX_LOAD_WAIT, testStart, typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // ajax clamp page calls with value 7 , doesnt use input value
            testStart = new Date();
            sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            recordList = null;
            try {
                typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.AJAX_CLAMP_MAX_CLICK_WAIT, testStart, typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertFalse(EUMValidationUtils.hasAjaxMetrics(recordList));

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
     * Tests url include property
     */

    @Test
    public void testURLInclude_454761() {
        try {
            // Step 1 - assign [".*GETCORS.*"] as the value of urlIncludeList property
            String[] urlIncludeList = new String[] {".*GETCORS.*"};
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setUrlIncludeList(urlIncludeList);
            baTestCollector.updateConfiguration(collectorConfig, "includeCors");

            // Step 2 -load GETLocalDomain.jsp no data reported
            WebDriver webDriver = getDriver();

            // Start a new test that launches page and see the one expected record (page metrics)
            Date testStart = new Date();


            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            List<MetricPostRecord> recordList = null;
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            // Not in the include list, wont get metrics
            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));

            // Step 3 - load GETCORS.jsp page reported.
            // enter 1 click, ajax not reported, browser log reports:
            // AJAX URL https://cors-test.appspot.com/test is configured to be EXCLUDED.
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_CORS_PAGE);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_CORS_MAX_EXPECTED_LOAD_WAIT, testStart, typeList);
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            int inputValue = 1;
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));

            testStart = new Date();
            WebElement sendButton = webDriver.findElement(By.name("GetRemoteFile"));
            sendButton.click();

            // not expecting ajax
            typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
            recordList = null;
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_CORS_MAX_EXPECTED_SINGLE_GET_WAIT, testStart,
                        typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertTrue(recordList.size() == 0, "Not expecting ajax metrics ");

            // Step 4 - add ".*cors-test.appspot.*" to the urlIncludeList
            urlIncludeList = new String[] {};
            attrs = collectorConfig.getBaAttributes();
            attrs.setUrlIncludeList(urlIncludeList);
            baTestCollector.updateConfiguration(collectorConfig, "ajaxInclude");

            // Step 5 - load GETCORS.jsp, confirm page metrics
            // enter 1 , confirm ajaxs reported

            // confirm page
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

            webDriver.get(getTestAppURL() + BrtmTestApp.GET_CORS_PAGE);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_CORS_MAX_EXPECTED_LOAD_WAIT, testStart, typeList);
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // confirm ajax
            inputValue = 1;
            valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));

            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);
            sendButton = webDriver.findElement(By.name("GetRemoteFile"));
            sendButton.click();

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_CORS_MAX_EXPECTED_SINGLE_GET_WAIT, testStart, typeList);

            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));

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
     * Tests both include and exclude
     */

    @Test
    public void testURLIncludeExclude_454762() {
        try {
            // Step 1 - urlIncludeList contains the following: ".*"
            // Make sure the urlExcludeList contains the following: ".*GETLocalDomain.*"

            Attributes attrs = collectorConfig.getBaAttributes();

            String[] includeArray = new String[] {".*"};
            attrs.setUrlIncludeList(includeArray);

            String[] excludeArray = new String[] {".*GETLocalDomain.*"};
            attrs.setUrlExcludeList(excludeArray);

            baTestCollector.updateConfiguration(collectorConfig, "setIncludeExclude");

            // Step 2 -
            // load index.html confirm page metrics
            // load GETLocalDomain no page
            // request 1 file no ajax metrics
            WebDriver webDriver = getDriver();

            // Start a new test that launches page and see the one expected record (page metrics)
            Date testStart = new Date();


            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

            webDriver.get(getTestAppURL() + BrtmTestApp.INDEX_PAGE);

            List<MetricPostRecord> recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.INDEX_LOAD_WAIT, testStart, typeList);

            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // Now load GETLocalDOmain confirm no page
            testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);
            recordList = null;
            typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));

            // request 1 file confirm no ajax
            int inputValue = 1;
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));

            testStart = new Date();
            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            recordList = null;
            try {
                typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT,
                        testStart, typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }
            Assert.assertFalse(EUMValidationUtils.hasAjaxMetrics(recordList));



            // Step 3 - modify to include:
            // "urlIncludeList":[".*/brtmtestapp/ajaxclamp/AjaxClamp.jsp",".*/brtmtestapp/ajaxclamp/[1234567]/sample.txt],
            // "urlExcludeList": [".*/brtmtestapp/ajaxclamp/[1234]/sample.txt"],

            attrs = collectorConfig.getBaAttributes();

            includeArray =
                new String[] {includeArray[0], ".*/brtmtestapp/ajaxclamp/AjaxClamp.jsp",
                        ".*/brtmtestapp/ajaxclamp/[1234567]/sample.txt"};
            attrs.setUrlIncludeList(includeArray);

            excludeArray =
                new String[] {excludeArray[0], ".*/brtmtestapp/ajaxclamp/[1234]/sample.txt"};
            attrs.setUrlExcludeList(excludeArray);

            baTestCollector.updateConfiguration(collectorConfig, "updateIncludeExclude");

            // Step 4 - load AjaxClamp.jsp , confirm page, press submit , see ajax for only 5,6, 7
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            webDriver.get(getTestAppURL() + BrtmTestApp.AJAX_CLAMP_PAGE);

            // confirm page
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.AJAX_CLAMP_MAX_LOAD_WAIT, testStart, typeList);
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // Partial ajax
            // ajax clamp page calls with value 7 , doesnt use input value
            testStart = new Date();
            sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            Map<PayloadTypes, AbstractPayloadType> typesMap =
                PayloadUtils.generateTypesMap(PayloadTypes.PAGE_TYPE, PayloadTypes.AJAX_TYPE);
            AbstractPayloadType ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(7);
            recordList = null;
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.AJAX_CLAMP_MAX_CLICK_WAIT, testStart, typesMap);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            // Yes waited for 7 records to ensure they never came, really only expected 3
            List<Resource> resourceList = EUMValidationUtils.extractAjax(recordList);
            Assert.assertTrue(resourceList.size() == 3, "Expecting only 5,6,7");

            // These are the only items we are expecting
            Set<String> expectedItems = new HashSet<String>();
            expectedItems.add("5/sample.txt");
            expectedItems.add("6/sample.txt");
            expectedItems.add("7/sample.txt");

            for (Resource resource : resourceList) {
                Request request = resource.getRequest();

                // For each request search the expected items
                String foundItem = null;
                for (String item : expectedItems) {
                    if (request.getUrl().endsWith(item)) {
                        foundItem = item;
                        break;
                    }
                }

                // Found the item, remove it... list should be empty after resourceList is processed
                if (foundItem != null) {
                    expectedItems.remove(foundItem);
                } else
                    Assert.fail("Unexpected url request found: " + request.getUrl());
            }

            Assert.assertTrue(expectedItems.size() == 0,
                "Didnt find an item we were expecting, remaining: " + expectedItems);

            // Step 5 - modify list to contain:
            // "urlIncludeList": [".*/brtmtestapp/ajaxclamp/[1234567]/sample.txt"],
            // "urlExcludeList": [".*/brtmtestapp/ajaxclamp/[234]/sample.txt"],
            attrs = collectorConfig.getBaAttributes();

            includeArray = new String[] {".*/brtmtestapp/ajaxclamp/[1234567]/sample.txt"};
            attrs.setUrlIncludeList(includeArray);

            excludeArray = new String[] {".*/brtmtestapp/ajaxclamp/[234]/sample.txt"};
            attrs.setUrlExcludeList(excludeArray);

            baTestCollector.updateConfiguration(collectorConfig, "updateIncludeExcludeStep5");


            // Step 6 - load AjaxClamp.jsp, submit. No page, no ajax. browser log:
            // :[INFO] BrowserAgent.main: Page is configured to be EXCLUDED. Disabling Browser Agent
            // no ajax is capatured

            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
            webDriver.get(getTestAppURL() + BrtmTestApp.AJAX_CLAMP_PAGE);

            // confirm no page
            recordList = null;
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.AJAX_CLAMP_MAX_LOAD_WAIT, testStart, typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));

            // ajax clamp page calls with value 7 , doesnt use input value
            testStart = new Date();
            sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            // Yes wait for this if no exclude... expecting nothing
            typesMap =
                PayloadUtils.generateTypesMap(PayloadTypes.PAGE_TYPE, PayloadTypes.AJAX_TYPE);
            ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(7);
            recordList = null;
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.AJAX_CLAMP_MAX_CLICK_WAIT, testStart, typesMap);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertFalse(EUMValidationUtils.hasAjaxMetrics(recordList));

            Assert.assertTrue(recordList.size() == 0, "Not expecting ajax metrics");

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
