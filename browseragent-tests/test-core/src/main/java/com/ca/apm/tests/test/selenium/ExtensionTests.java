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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.ClientEvent;
import com.ca.apm.eum.datamodel.Metric;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/**
 * This class is a collection of extension tests such as tracers and soon name formatter, etc
 * The process to add a test resource (BAExt.js) is the following:
 * 
 * 1. Add customized file to:
 * \testing\test-projects\browseragent-tests\test-core\src\main\resources\jstestfiles
 * if for example your test is ALM id 455003 then the file should have name: BAExt-45003.js
 * 
 * 2. In your test call function from SeleniumBase: deployExtensionFile("BAExt-455003.js");
 * The above command will extract the file from the jar and deploy/copy to the collector
 * working directory. This WILL overwrite any BAExt.js that exists there BE ADVISED
 * 
 * 3. If for some reason your tests needs multiple extension tests files try to be consistent
 * in the naming for example BAExt-45003-part1.js BAExt-45003-part2.js doesnt have to
 * be consistent with alm steps since that could always change.
 * 
 * 4. When you are done your tests no revert needed. SeleniumBase.afterMethod will
 * remove any BAExt.js that exists
 * 
 */

public class ExtensionTests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(ExtensionTests.class);

    public ExtensionTests() {

    }

    public ExtensionTests(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }


    @Test
    public void testNonCollidingTracers_455003() {
        try {

            // Step 1 - Disable page
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setPageLoadMetricsEnabled(false);
            attrs.setJsFunctionMetricsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "disablePage");

            // Now deploy the BAExt.js that will be used for this test
            deployExtensionFile("BAExt-455003.js");

            String fullUrlToTestPage = getTestAppURL() + BrtmTestApp.JS_FUNCTION_RETRY;
            WebDriver webDriver = openPage(fullUrlToTestPage);

            // step 2 optional
            Object resultObject =
                executeInBrowserConsole("BrowserAgent.globals.functionsToInstrumentList");


            if (resultObject != null) {
                String resultString = resultObject.toString();

                LOGGER.debug("Optional step, resultString= " + resultString);

                Assert.assertTrue(resultString.contains("XMLHttpRequest.prototype.open"));
                Assert.assertTrue(resultString.contains("XMLHttpRequest.prototype.send"));
                Assert.assertTrue(resultString.contains("dummyObject.instRetryFunc"));
            }


            // step 3 - check logs for messages

            // There is a bug with Chrome driver that appears to not have updated messages
            // we can get around this bug if only make the request of the logs ONCE
            List<String> logMessagesToCheck = new ArrayList<String>();

            logMessagesToCheck
                .add("[INFO] addFuncToCollection: Found invalid or unspecified postTracerList for JS Function [XMLHttpRequest.prototype.open]. Defaulting to [].");
            logMessagesToCheck
                .add("[WARN] constructInstrumentFunctionList - 3: Could not find pre tracer [non-Existent] for JS Function [dummyObject.instRetryFunc] in global scope.");
            logMessagesToCheck
                .add("[WARN] constructInstrumentFunctionList - 4: Could not find post tracer [non-Existent] for JS Function [dummyObject.instRetryFunc] in global scope.");

            // Step 4 - click define function, check output
            WebElement defineFunction = webDriver.findElement(By.name("Define a new JS Function"));

            defineFunction.click();
            // without this sleep the follow up click to invoke wont work
            Thread.currentThread().sleep(5000);

            logMessagesToCheck
                .add("[INFO] instrumentFunc: Instrumenting JS Function [dummyObject.instRetryFunc]...");
            logMessagesToCheck
                .add("[INFO] instrumentFunc: Finished instrumentation for JS Function [dummyObject.instRetryFunc].");

            // step 5
            Date testStart = new Date();

            WebElement invokeFunction =
                webDriver.findElement(By.name("Invoke the new JS Function"));
            invokeFunction.click();

            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.ANY_TYPE);
            List<MetricPostRecord> recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.STANDARD_CLICK_WAIT, testStart, typeList);


            int metricNames = 0;
            List<ClientEvent> clientList = EUMValidationUtils.extractJSFunction(recordList);
            for (ClientEvent event : clientList) {
                List<Metric> metricList = event.getApmData().getMetrics().getMetricList();

                for (Metric metric : metricList) {
                    Assert.assertTrue(metric.getPath().contains("dummyObject.instRetryFunc"));
                    if (metric.getName().equals(EUMValidationUtils.EXECUTION_TIME)) {
                        Assert.assertTrue(metric.getValue() > 0);
                        metricNames += 1;
                    } else if (metric.getName().equals(
                        EUMValidationUtils.INVOCATION_COUNT_PER_INTERVAL)) {
                        Assert.assertTrue(metric.getValue() == 1);
                        metricNames += 1;
                    }
                    Assert.assertTrue(metric.getValue() > 0);

                }
            }

            // verify the names were found
            Assert.assertTrue(metricNames == 2);

            // Step 6
            logMessagesToCheck.add("I try all things, I achieve what I can - Herman Melville.");
            logMessagesToCheck.add("instRetryFunc: done");


            // Step 7
            logMessagesToCheck.add("callMeIshmael");

            // Step 8 - disable js functions
            attrs = collectorConfig.getBaAttributes();
            attrs.setJsFunctionMetricsEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disableJS");

            // Step 9 - reopen page
            webDriver = openPage(fullUrlToTestPage);

            // Step 10 - click define function
            defineFunction = webDriver.findElement(By.name("Define a new JS Function"));
            defineFunction.click();
            // without this sleep the follow up click to invoke wont work
            Thread.currentThread().sleep(5000);

            logMessagesToCheck
                .add("[INFO] instrumentFunc: Instrumenting JS Function [dummyObject.instRetryFunc]...");
            logMessagesToCheck
                .add("[INFO] instrumentFunc: Finished instrumentation for JS Function [dummyObject.instRetryFunc].");

            // Step 11 - Invoke function
            testStart = new Date();
            invokeFunction = webDriver.findElement(By.name("Invoke the new JS Function"));
            invokeFunction.click();

            recordList = null;
            typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.STANDARD_CLICK_WAIT, testStart, typeList);
            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            // False because its disabled
            Assert.assertFalse(EUMValidationUtils.hasJSFunctionMetrics(recordList));


            // Step 12 -
            logMessagesToCheck.add("I try all things, I achieve what I can - Herman Melville.");
            logMessagesToCheck
                .add("[INFO] JSFuncPre (dummyObject.instRetryFunc): JS Function Metrics are DISABLED.");
            logMessagesToCheck.add("instRetryFunc: done");

            // Step 13 -
            logMessagesToCheck.add("callMeIshmael");

            // Logging doesnt work on firefox
            if (isChrome()) {
                // Sleep just to ensure the one and only call to get logs everything has finished
                Thread.currentThread().sleep(3000);
                LogEntries logEntries = webDriver.manage().logs().get(LogType.BROWSER);

                // this index is a pointer of the current log entry to check.
                // logMessagesToCheck is assumed to be in the order
                int index = 0;
                // there will likely be more entries than messages being looked for
                // once find the next entry in logMessagesToCheck advance the index
                for (LogEntry entry : logEntries) {
                    System.out.println(entry.toString());
                    if (entry.toString().contains(logMessagesToCheck.get(index))) {
                        LOGGER.debug("Found message: " + entry.toString());
                        index += 1;

                        // Once all entries have been found exit from this loop
                        if (index == logMessagesToCheck.size()) {
                            break;
                        }
                    }

                }

                // for failure, use the index that was being searched above
                String lookingFor =
                    index < logMessagesToCheck.size() ? logMessagesToCheck.get(index) : "";

                // If this point has been reached, index should be equal to size if all were found
                Assert.assertTrue(logMessagesToCheck.size() == index, "Could not find message: "
                    + lookingFor);
            }
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
