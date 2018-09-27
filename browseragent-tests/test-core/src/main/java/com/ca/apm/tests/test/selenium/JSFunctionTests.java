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

import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.ClientEvent;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/**
 * This class holds a collection of selenium tests covering JS function
 *
 */

public class JSFunctionTests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(JSFunctionTests.class);

    public JSFunctionTests() {

    }

    public JSFunctionTests(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }

    /**
     * Test basic JS function XHR_Open and XHR_Send
     * UPDATE: This test is no longer valid, keep perhaps used for extension testing
     */

    // @Test
    public void testJSFunction_454883() {
        try {

            // Step 1 - enable jsFunction
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setJsFunctionMetricsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "enableJSFunction");

            // Step 2 - open GETLocalDomain.jsp, verify page, then click for one file verify ajax/js

            Date testStart = new Date();
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            WebDriver webDriver = getDriver();
            String fullUrlToTestPage = getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE;
            webDriver.get(fullUrlToTestPage);

            List<MetricPostRecord> recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // verify we DO have page metrics
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // Now perform the click for 1 file
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys("1");

            // reset the starttime and click the send button..
            testStart = new Date();
            typeList =
                PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE,
                    PayloadTypes.JS_FUNCTION_TYPE);

            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT, testStart,
                    typeList);

            // verify we DO have ajax metrics and JS function
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));
            Assert.assertTrue(EUMValidationUtils.hasJSFunctionMetrics(recordList));

            List<ClientEvent> clientEventList = EUMValidationUtils.extractJSFunction(recordList);
            EUMValidationUtils.validateJSFunction(clientEventList, fullUrlToTestPage);

            // Step 3 - disable js function
            attrs = collectorConfig.getBaAttributes();
            attrs.setJsFunctionMetricsEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disableJSFunction");

            // Step 4 - get page, request 1 file verify only page and ajax

            testStart = new Date();
            typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE, PayloadTypes.AJAX_TYPE);

            webDriver = getDriver();
            webDriver.get(fullUrlToTestPage);

            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // verify we DO have page metrics
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));


            valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys("1");

            // reset the starttime and click the send button..
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);
            sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT, testStart,
                    typeList);

            // verify we DO have ajax metrics and NOT JS function
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));
            Assert.assertFalse(EUMValidationUtils.hasJSFunctionMetrics(recordList));

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
