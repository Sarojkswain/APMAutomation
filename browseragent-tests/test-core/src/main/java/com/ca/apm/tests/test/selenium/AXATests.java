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
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/**
 * This class holds a collection of selenium tests covering axa related items cookie, fingerprint,
 * session
 *
 */

public class AXATests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(AXATests.class);

    public AXATests() {

    }

    public AXATests(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }



    /**
     * Fingerprint testing - 7 of 10 agree steps implemented, however tabs doesn't work on tas
     * driver issue? Latest information is likely not a driver issue, but how the test runs on tas
     * It appears to run under some executor which backgrounds the task. The way this tests opens
     * a new tab is through needing the focus and suspect a backgrounded task never truly gets
     * the focus
     */

    // Keep this commented out, appears to now have issue on local runs too.
    // Always was a manual test because tabs doesnt work on tas
    // @Test
    public void testFingerprint_454759() {
        try {

            // Prereq - enable js function
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setJsFunctionMetricsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "enableJsFunction");

            // Step 1 - verify page reported, ajax not reported, get finger print

            // Get the driver to be used for this test...
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

            StringBuilder fingerPrintLoadStep1 = new StringBuilder();
            boolean allSameFingerPrint =
                EUMValidationUtils.hasAllSameFingerPrint(recordList, fingerPrintLoadStep1);
            Assert.assertTrue(allSameFingerPrint);

            // verify we DO have page metrics
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // verify we DONT have ajax metrics - Yes redundant since no ajax activity on getlocal
            // domain
            Assert.assertFalse(EUMValidationUtils.hasAjaxMetrics(recordList));

            // Step 1 continued , Click the button to get ajax, confirm same finger print
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys("1");

            // reset the starttime and click the send button.. ajax metrics add to the count
            testStart = new Date();
            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT, testStart,
                    typeList);

            // Shouldnt see any page metrics here
            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));
            // verify we DO have ajax metrics
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));


            StringBuilder fingerPrintAjaxStep1 = new StringBuilder();
            allSameFingerPrint =
                EUMValidationUtils.hasAllSameFingerPrint(recordList, fingerPrintAjaxStep1);
            Assert.assertTrue(allSameFingerPrint);
            Assert.assertEquals(fingerPrintLoadStep1.toString(), fingerPrintAjaxStep1.toString());

            // Step 2 - load another page same origin, same tab, more ajax. same fingerprint
            testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE);
            Map<PayloadTypes, AbstractPayloadType> typesMap =
                PayloadUtils.generateTypesMap(PayloadTypes.PAGE_TYPE, PayloadTypes.AJAX_TYPE);
            AbstractPayloadType ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(EUMValidationUtils.GET_LOCAL_DOMAIN_2_DEFAULT_AJAX_AT_LOAD);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_2_MAX_EXPECTED_LOAD_WAIT, testStart,
                    typesMap);
            StringBuilder fingerPrintStep2 = new StringBuilder();
            allSameFingerPrint =
                EUMValidationUtils.hasAllSameFingerPrint(recordList, fingerPrintStep2);
            Assert.assertTrue(allSameFingerPrint);
            Assert.assertEquals(fingerPrintAjaxStep1.toString(), fingerPrintStep2.toString());

            // Step 3 - load same page from step 2, same origin, with another tab same fingerprint

            // Take a snap shot of the handles (tabs)
            Set<String> handleSetBefore = webDriver.getWindowHandles();
            LOGGER.debug("handleSetBefore " + handleSetBefore);
            // Open new tab
            // webDriver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL + "t");
            // webDriver.manage().window().maximize();
            webDriver.findElement(By.tagName("body")).sendKeys(Keys.CONTROL + "t");
            Thread.currentThread().sleep(5000);
            // Take another snapshot, this set should contain more
            Set<String> handleSetAfter = webDriver.getWindowHandles();

            LOGGER.debug("handleSetAfter " + handleSetAfter);
            // Now remove everything from the previous
            handleSetAfter.removeAll(handleSetBefore);

            // Should just be one, if this fails new tab didnt open

            String newTab = handleSetAfter.iterator().next(); // BUG: fails on tas machine,
                                                              // why!?!?!?
            testStart = new Date();
            webDriver.switchTo().window(newTab);
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE);

            typesMap =
                PayloadUtils.generateTypesMap(PayloadTypes.PAGE_TYPE, PayloadTypes.AJAX_TYPE);
            ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(EUMValidationUtils.GET_LOCAL_DOMAIN_2_DEFAULT_AJAX_AT_LOAD);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_2_MAX_EXPECTED_LOAD_WAIT, testStart,
                    typesMap);
            StringBuilder fingerPrintStep3 = new StringBuilder();
            allSameFingerPrint =
                EUMValidationUtils.hasAllSameFingerPrint(recordList, fingerPrintStep3);
            Assert.assertTrue(allSameFingerPrint);
            Assert.assertEquals(fingerPrintStep2.toString(), fingerPrintStep3.toString());


            // Step 4 - Same page, new browser, generate ajax, same fingerprint
            handleSetBefore = webDriver.getWindowHandles();
            // webDriver.manage().window().maximize();
            webDriver.findElement(By.tagName("body")).sendKeys(Keys.CONTROL + "n");
            Thread.currentThread().sleep(5000);
            handleSetAfter = webDriver.getWindowHandles();
            handleSetAfter.removeAll(handleSetBefore);
            newTab = handleSetAfter.iterator().next();

            testStart = new Date();

            webDriver.switchTo().window(newTab);
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);
            StringBuilder fingerPrintStep4 = new StringBuilder();
            allSameFingerPrint =
                EUMValidationUtils.hasAllSameFingerPrint(recordList, fingerPrintStep4);
            Assert.assertTrue(allSameFingerPrint);
            Assert.assertEquals(fingerPrintStep3.toString(), fingerPrintStep4.toString());


            Thread.currentThread().sleep(1000);

            // Step 5 - load page with different origin in same/new tab/window - new fingerprint
            String invertedUrl = invertTestAppURL();

            testStart = new Date();
            webDriver.get(invertedUrl + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);
            StringBuilder fingerPrintStep5 = new StringBuilder();
            allSameFingerPrint =
                EUMValidationUtils.hasAllSameFingerPrint(recordList, fingerPrintStep5);
            Assert.assertTrue(allSameFingerPrint);

            // DO expect different finger print
            Assert.assertFalse(fingerPrintStep4.equals(fingerPrintStep5));


            // Step 6 - access 2 pages of different origin -
            // page 1 -> page 2 -> page 1 -> page 2 . no new fingerprint
            testStart = new Date();
            webDriver.get(invertedUrl + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE);
            typesMap =
                PayloadUtils.generateTypesMap(PayloadTypes.PAGE_TYPE, PayloadTypes.AJAX_TYPE);
            ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(EUMValidationUtils.GET_LOCAL_DOMAIN_2_DEFAULT_AJAX_AT_LOAD);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_2_MAX_EXPECTED_LOAD_WAIT, testStart,
                    typesMap);
            StringBuilder fingerPrintStep6 = new StringBuilder();
            allSameFingerPrint =
                EUMValidationUtils.hasAllSameFingerPrint(recordList, fingerPrintStep6);
            Assert.assertTrue(allSameFingerPrint);
            Assert.assertEquals(fingerPrintStep5.toString(), fingerPrintStep5.toString());


            // Step 7 - exit browser instance, no cache clearing - same finger print

            // The selenium test is clearing the cache on exit.

            // Step 8 - load page after clearing cache, new finger print
            closeDriver();
            webDriver = getDriver();
            testStart = new Date();
            webDriver.get(invertedUrl + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE);
            typesMap =
                PayloadUtils.generateTypesMap(PayloadTypes.PAGE_TYPE, PayloadTypes.AJAX_TYPE);
            ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
            ajaxType.setCount(EUMValidationUtils.GET_LOCAL_DOMAIN_2_DEFAULT_AJAX_AT_LOAD);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_2_MAX_EXPECTED_LOAD_WAIT, testStart,
                    typesMap);
            StringBuilder fingerPrintStep8 = new StringBuilder();
            allSameFingerPrint =
                EUMValidationUtils.hasAllSameFingerPrint(recordList, fingerPrintStep8);
            Assert.assertTrue(allSameFingerPrint);
            Assert.assertFalse(fingerPrintStep5.toString().equals(fingerPrintStep8));


            // Step 9 - load test page for browser in icognito mode - new each time

            // not impossible, requires setting the capability on the browser,
            // by default, when the driver is created. skipping for automation

            // Step 10 - load in another browser type -

            // redundant skipping

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
