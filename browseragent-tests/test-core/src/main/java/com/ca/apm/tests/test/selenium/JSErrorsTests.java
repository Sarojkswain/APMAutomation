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
import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.ErrorTypeEnum;
import com.ca.apm.eum.datamodel.Metric;
import com.ca.apm.eum.datamodel.Page;
import com.ca.apm.tests.test.selenium.EUMValidationUtils.PageType;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/**
 * This class holds a collection of selenium tests covering JS Errors
 *
 */

public class JSErrorsTests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(JSErrorsTests.class);

    public JSErrorsTests() {

    }

    public JSErrorsTests(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }


    /**
     * Valid test of reference error
     */

    @Test
    public void testJSErrorsEnabled_454885() {

        try {

            // Step 1 - enable jsErrors, enabled by default being explicit
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setJsErrorsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "enableJSErrors");

            // Step 2 - navigate to test page click reference error
            // Get the driver to be used for this test...

            WebDriver webDriver = openPage(getTestAppURL() + BrtmTestApp.ERROR_MULTI_ERROR_PAGE);



            // Now click on 'Reference Error' button
            Date testStart = new Date();
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.JS_ERROR_TYPE);
            WebElement sendButton = webDriver.findElement(By.id("refError"));
            sendButton.click();

            // wait for the page to load and there is page and errors
            List<MetricPostRecord> recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.REFERENCE_ERROR_MAX_EXPECTED_CLICK_WAIT, testStart,
                        typeList);

            List<com.ca.apm.eum.datamodel.Error> extractedJSErrors =
                EUMValidationUtils.extractJSErrors(recordList);
            Assert.assertTrue(extractedJSErrors.size() > 0);

            // Expected result testing of step 2
            for (com.ca.apm.eum.datamodel.Error error : extractedJSErrors) {
                // In this case we know it will be greater than zero (65 actually).
                // Not a good test, because 0 could technically be valid but could
                // also just be not initialized
                int lineNumber = error.getLineNumber();
                Assert.assertTrue(lineNumber > 0);

                // Again, anything non zero should suffice
                long timeStamp = error.getTimeStamp();
                Assert.assertTrue(timeStamp > 0);

                ErrorTypeEnum typeEnum = error.getType();
                // System.out.println("typeEnum" + typeEnum);
                Assert.assertTrue(ErrorTypeEnum.CLIENT.equals(typeEnum));

                // Assert the subtype too
                String subType = error.getSubType();
                // Chrome,Firefox and IE all report the subType, verified by manual testing
                Assert.assertEquals(subType, "ReferenceError");

                // Assert column number
                int colNumber = error.getColumnNumber();
                Assert.assertTrue(colNumber > 0);

                // Assert stackTrace is not null
                String stackTrace = error.getStackTrace();
                Assert.assertNotNull(stackTrace);
                Assert.assertTrue(stackTrace.trim().length() > 0);

            }

            // now click on couple of other error buttons
            // Step 3: click on the 'Type Error' button
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.JS_ERROR_TYPE);
            sendButton = webDriver.findElement(By.id("typeError"));
            sendButton.click();


            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.TYPE_ERROR_MAX_EXPECTED_CLICK_WAIT, testStart, typeList);

            extractedJSErrors = EUMValidationUtils.extractJSErrors(recordList);
            Assert.assertTrue(extractedJSErrors.size() > 0);

            // validate the result
            for (com.ca.apm.eum.datamodel.Error error : extractedJSErrors) {

                int lineNumber = error.getLineNumber();
                Assert.assertTrue(lineNumber > 0);

                // Again, anything non zero should suffice
                long timeStamp = error.getTimeStamp();
                Assert.assertTrue(timeStamp > 0);

                ErrorTypeEnum typeEnum = error.getType();
                // System.out.println("typeEnum" + typeEnum);
                Assert.assertTrue(ErrorTypeEnum.CLIENT.equals(typeEnum));

                // Assert the subtype too
                String subType = error.getSubType();
                // Chrome,Firefox and IE all report the subType, verified by manual testing
                Assert.assertEquals(subType, "TypeError");

                // Assert column number
                int colNumber = error.getColumnNumber();
                Assert.assertTrue(colNumber > 0);

                // Assert stackTrace is not null
                String stackTrace = error.getStackTrace();
                Assert.assertNotNull(stackTrace);
                Assert.assertTrue(stackTrace.trim().length() > 0);
            }
            // Step 4: click on the 'Range Error' button
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.JS_ERROR_TYPE);
            sendButton = webDriver.findElement(By.id("rangeError"));
            sendButton.click();


            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.TYPE_ERROR_MAX_EXPECTED_CLICK_WAIT, testStart, typeList);

            extractedJSErrors = EUMValidationUtils.extractJSErrors(recordList);
            Assert.assertTrue(extractedJSErrors.size() > 0);

            // validate the result
            for (com.ca.apm.eum.datamodel.Error error : extractedJSErrors) {

                int lineNumber = error.getLineNumber();
                Assert.assertTrue(lineNumber > 0);

                // Again, anything non zero should suffice
                long timeStamp = error.getTimeStamp();
                Assert.assertTrue(timeStamp > 0);

                ErrorTypeEnum typeEnum = error.getType();
                // System.out.println("typeEnum" + typeEnum);
                Assert.assertTrue(ErrorTypeEnum.CLIENT.equals(typeEnum));

                // Assert the subtype too
                String subType = error.getSubType();
                // Chrome,Firefox and IE all report the subType, verified by manual testing
                Assert.assertEquals(subType, "RangeError");

                // Assert column number
                int colNumber = error.getColumnNumber();
                Assert.assertTrue(colNumber > 0);

                // Assert stackTrace is not null
                String stackTrace = error.getStackTrace();
                Assert.assertNotNull(stackTrace);
                Assert.assertTrue(stackTrace.trim().length() > 0);
            }

            // Step 5: click on the 'URI Error' button
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.JS_ERROR_TYPE);
            sendButton = webDriver.findElement(By.id("uriError"));
            sendButton.click();


            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.TYPE_ERROR_MAX_EXPECTED_CLICK_WAIT, testStart, typeList);

            extractedJSErrors = EUMValidationUtils.extractJSErrors(recordList);
            Assert.assertTrue(extractedJSErrors.size() > 0);

            // validate the result
            for (com.ca.apm.eum.datamodel.Error error : extractedJSErrors) {

                int lineNumber = error.getLineNumber();
                Assert.assertTrue(lineNumber > 0);

                // Again, anything non zero should suffice
                long timeStamp = error.getTimeStamp();
                Assert.assertTrue(timeStamp > 0);

                ErrorTypeEnum typeEnum = error.getType();
                // System.out.println("typeEnum" + typeEnum);
                Assert.assertTrue(ErrorTypeEnum.CLIENT.equals(typeEnum));

                // Assert the subtype too
                String subType = error.getSubType();
                // Chrome,Firefox and IE all report the subType, verified by manual testing
                Assert.assertEquals(subType, "URIError");

                // Assert column number
                int colNumber = error.getColumnNumber();
                Assert.assertTrue(colNumber > 0);

                // Assert stackTrace is not null
                String stackTrace = error.getStackTrace();
                Assert.assertNotNull(stackTrace);
                Assert.assertTrue(stackTrace.trim().length() > 0);
            }

            // All these are following are special cases of JS errors, which each browser reports
            // differently

            // Step 6: Click on the 'Custom Error' button
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.JS_ERROR_TYPE);
            sendButton = webDriver.findElement(By.id("customError"));
            sendButton.click();


            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.CUSTOM_ERROR_MAX_EXPECTED_CLICK_WAIT, testStart, typeList);

            extractedJSErrors = EUMValidationUtils.extractJSErrors(recordList);
            Assert.assertTrue(extractedJSErrors.size() > 0);

            // validate the result
            for (com.ca.apm.eum.datamodel.Error error : extractedJSErrors) {

                // For custom errors, Firefox doesn't report line number,column number if it is not
                // assigned during instantiation

                if (isChrome()) {

                    // Assert line number
                    int lineNumber = error.getLineNumber();
                    Assert.assertTrue(lineNumber > 0);

                    // Assert column number
                    int colNumber = error.getColumnNumber();
                    Assert.assertTrue(colNumber > 0);
                }

                // Again, anything non zero should suffice
                long timeStamp = error.getTimeStamp();
                Assert.assertTrue(timeStamp > 0);

                ErrorTypeEnum typeEnum = error.getType();
                // System.out.println("typeEnum" + typeEnum);
                Assert.assertTrue(ErrorTypeEnum.CLIENT.equals(typeEnum));

                // Assert the subType too
                String subType = error.getSubType();
                // Chrome,Firefox and IE all report the subType, verified by manual testing
                Assert.assertEquals(subType, "MyError");

                // only IE11 reports stackTrace() for custom errors
                if (!isChrome() && !isFireFox()) {
                    String stackTrace = error.getStackTrace();
                    Assert.assertNotNull(stackTrace);
                    Assert.assertTrue(stackTrace.trim().length() > 0);
                }
            }
            // Step 7: Click on the 'Recursive Call' button
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.JS_ERROR_TYPE);
            sendButton = webDriver.findElement(By.id("recurse"));
            sendButton.click();


            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.CUSTOM_ERROR_MAX_EXPECTED_CLICK_WAIT, testStart, typeList);

            extractedJSErrors = EUMValidationUtils.extractJSErrors(recordList);
            Assert.assertTrue(extractedJSErrors.size() > 0);

            // validate the result
            for (com.ca.apm.eum.datamodel.Error error : extractedJSErrors) {

                // Do checks that will be same for all both browsers:
                // type, timestamp

                ErrorTypeEnum typeEnum = error.getType();
                Assert.assertTrue(ErrorTypeEnum.CLIENT.equals(typeEnum));

                // Again, anything non zero should suffice
                long timeStamp = error.getTimeStamp();
                Assert.assertTrue(timeStamp > 0);

                // The custom error is a special case on firefox and chrome
                // Chrome:
                // Initial investigation seemed to indicate RangeError would
                // occur on chrome version prior to v53. However, it now appears
                // that maybe chrome could report RangeError as a subtype or
                // Syntax Error in the message message
                // firefox:
                // called internal error and contains many other information
                // line number, column number, stack trace, etc.

                // Chrome,Firefox and IE all report the subType,
                // but for this JS error they have different subType for it
                String subType = error.getSubType();

                if (isChrome()) {
                    // Some odd things (see above). Its either RangeError in the subtype
                    // or its Script error in the message
                    boolean hasRange = subType != null && subType.contains("RangeError");

                    String message = error.getMessage() != null ? error.getMessage() : "";
                    boolean hasScript = message.contains("Script error");

                    Assert.assertTrue(hasRange || hasScript, "subType value of: " + subType
                        + " , and message: " + message + " is unexpected type");

                } else if (isFireFox()) {

                    // Assert line number
                    int lineNumber = error.getLineNumber();
                    Assert.assertTrue(lineNumber > 0);

                    // Assert column number
                    int colNumber = error.getColumnNumber();
                    Assert.assertTrue(colNumber > 0);

                    Assert.assertEquals(subType, "InternalError");

                    // for IE 11 it is called "Error" but we are skipping IE for now

                    // Assert stackTrace
                    String stackTrace = error.getStackTrace();
                    Assert.assertNotNull(stackTrace);
                    Assert.assertTrue(stackTrace.trim().length() > 0);
                }

            }

            // Step 8: Click on the 'Evaluation Error' button
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.JS_ERROR_TYPE);
            sendButton = webDriver.findElement(By.id("evalError"));
            sendButton.click();


            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.CUSTOM_ERROR_MAX_EXPECTED_CLICK_WAIT, testStart, typeList);

            extractedJSErrors = EUMValidationUtils.extractJSErrors(recordList);
            Assert.assertTrue(extractedJSErrors.size() > 0);

            // validate the result
            for (com.ca.apm.eum.datamodel.Error error : extractedJSErrors) {


                // Assert line number
                int lineNumber = error.getLineNumber();
                Assert.assertTrue(lineNumber > 0);

                // Assert column number
                // firefox is does not report column number for EvalError
                if (isChrome()) {
                    int colNumber = error.getColumnNumber();
                    Assert.assertTrue(colNumber > 0);
                }

                // Again, anything non zero should suffice
                long timeStamp = error.getTimeStamp();
                Assert.assertTrue(timeStamp > 0);

                ErrorTypeEnum typeEnum = error.getType();
                Assert.assertTrue(ErrorTypeEnum.CLIENT.equals(typeEnum));

                // Assert the subType too
                String subType = error.getSubType();
                // Chrome,Firefox and IE all report the subType,
                // but for this JS error they have different Subtype for it
                Assert.assertEquals(subType, "EvalError");

                // for IE 11 it is called "Error" but we are skipping IE for now
                String stackTrace = error.getStackTrace();
                Assert.assertNotNull(stackTrace);
                Assert.assertTrue(stackTrace.trim().length() > 0);

            }

            // Step 9 - disable jsErrors

            collectorConfig = baTestCollector.getConfiguration();
            attrs = collectorConfig.getBaAttributes();
            attrs.setJsErrorsEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disableJSErrors");

            // Send a click to push metrics, this will response with a 204

            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.JS_ERROR_TYPE);
            sendButton.click();

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.CUSTOM_ERROR_MAX_EXPECTED_CLICK_WAIT, testStart, typeList);

            // YES. Confirm again has error object. the response here will get the 204
            Assert.assertTrue(EUMValidationUtils.hasJSError(recordList));

            // 204 now seen allow the browser to download and apply new config
            Thread.currentThread().sleep(3000);

            // Step 10 - click reference error again confirm no metrics
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);

            sendButton.click();

            recordList = null;
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.CUSTOM_ERROR_MAX_EXPECTED_CLICK_WAIT, testStart,
                        typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            // Confirm not seeing any page nor ajax.
            Assert.assertFalse(EUMValidationUtils.hasJSError(recordList));


            //
            // Errors for soft pages
            //

            // reenable from the test above
            collectorConfig = baTestCollector.getConfiguration();
            attrs = collectorConfig.getBaAttributes();
            attrs.setJsErrorsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "enableJSErrors");

            closeDriver();
            Thread.currentThread().sleep(3000);
            webDriver = getDriver();
            webDriver.get(getTestAppURL() + BrtmTestApp.SPA_INDEX);
            WebElement redLink = webDriver.findElement(By.linkText("Red"));
            redLink.click();

            // Dont care about the first payload, tested in SPATests.java
            // Thread.currentThread().sleep(attrs.getMetricFrequency() + 1000);
            sleepMetricFrequency();

            WebElement evalError = webDriver.findElement(By.id("evalError"));
            testStart = new Date();
            evalError.click();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.SOFT_PAGE_TYPE);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.STANDARD_CLICK_WAIT, testStart, typeList);

            List<Page> pageList = EUMValidationUtils.extractPagesByType(recordList, PageType.SOFT);
            Assert.assertTrue(pageList.size() == 1);

            extractedJSErrors = EUMValidationUtils.extractJSErrors(recordList);
            Assert.assertTrue(extractedJSErrors.size() > 0);
            com.ca.apm.eum.datamodel.Error softError = extractedJSErrors.get(0);

            // This is not controlled by BA. Chrome reports index.html, firefox is someFile.js
            // As long as a value is present that is sufficient
            Assert.assertTrue(softError.getSource().length() > 0);
            Assert.assertTrue(softError.getMessage().contains("EvalError: Hello"));

            // Check a few of the important items .. the full structure is tested above
            Metric metric = softError.getApmData().getMetrics().getMetricList().get(0);
            Assert.assertTrue(metric.getName().equals(EUMValidationUtils.JS_ERRORS_PER_INTERVAL));
            Assert.assertTrue(metric.getValue() == 1);
            Assert.assertTrue(metric.getAccumulatorType() == 1);
            Assert.assertTrue(metric.getPath().endsWith(BrtmTestApp.SPA_INDEX + "|#/red"));
            Assert.assertTrue(softError.getLineNumber() > 0);

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
     * Testing invalid setting
     */

    @Test
    public void testJSInvalidProfile_454886() {
        try {
            // Step 1 - use invalid value in jsErrorsEnabled -1

            String asString = BATestCollectorUtils.getPrettyJson(collectorConfig);

            asString = asString.replace("\"jsErrorsEnabled\" : true,", "\"jsErrorsEnabled\" : -1,");
            baTestCollector.updateConfiguration(asString, "toInvalid-1");

            // Step 2 - navigate to test page click reference error
            // Get the driver to be used for this test...

            WebDriver webDriver = openPage(getTestAppURL() + BrtmTestApp.ERROR_MULTI_ERROR_PAGE);

            // now send the click on reference error
            WebElement sendButton = webDriver.findElement(By.id("refError"));
            sendButton.click();

            Assert
                .assertTrue(checkBrowserLogForMessage("jsErrorsEnabled is not provided or invalid. Defaulting to true"));

            // Step 3 - remove the property

            // replace the last property with empty string
            asString = asString.replace("\"jsErrorsEnabled\" : -1,", "");
            baTestCollector.updateConfiguration(asString, "jsErrorsEnabledIsMissing");

            // Step 4 - load page, click button confirm debug
            closeDriver(); // close previous
            webDriver = openPage(getTestAppURL() + BrtmTestApp.ERROR_MULTI_ERROR_PAGE);
            sendButton = webDriver.findElement(By.id("refError"));
            sendButton.click();

            Assert
                .assertTrue(checkBrowserLogForMessage("jsErrorsEnabled is not provided or invalid. Defaulting to true"));
        } catch (Exception e) {
            LOGGER.fatal("Unexpected exception, failing test", e);

            assertFail(e);
        } finally {
            baTestCollector.stopServer();
            closeDriver();
        }
    }
}
