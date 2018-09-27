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

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.browseragent.testsupport.collector.BATestCollector;
import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.App;
import com.ca.apm.eum.datamodel.EUM;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/**
 * This class holds a collection of selenium tests covering Snippet
 *
 */

public class SnippetTests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(PageLoadTests.class);

    public SnippetTests() {

    }

    public SnippetTests(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }


    /**
     * This is a redundant test because already performed by the template. Allows
     * TAS to update ALM
     */

    @Test
    public void testSnipptAttrs_454740_454741_454738() {
        try {
            WebDriver webDriver = getDriver();

            Date testStart = new Date();
            String fullPageUrl = getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE;
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            webDriver.get(fullPageUrl);

            List<MetricPostRecord> recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // Just to make sure we ARE getting into this loop, actually this list
            // is expected right now to be size of 1, but in the future could change..
            boolean processedLoop = false;
            for (MetricPostRecord record : recordList) {
                EUM eumObject = record.getEumObject();

                App app = eumObject.getApp();

                // Covers ALM test 454740
                Assert.assertEquals(app.getId(), BATestCollectorUtils.DEFAULT_APP);
                // Covers ALM test 454741
                Assert.assertEquals(app.getKey(), BATestCollector.APP_KEY);
                // Covers ALM test 454738
                Assert.assertEquals(app.getTenantId(), BATestCollectorUtils.DEFAULT_TENANT);

                processedLoop = true;
            }
            Assert.assertTrue(processedLoop);

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
     * Two tests combined testin valid snippet information id and src tags
     */

    @Test
    public void testValidIdSrcTags_454735_454736() {
        try {
            String path = getTestAppDirectoryPath();

            String jspPage =
                BATestCollectorUtils.readStringFromFile(new File(path + File.separator
                    + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE));

            // Steps 1 & 2 insert snipppet information
            Assert.assertTrue(jspPage.contains("<script id=\"BA_AXA\""));
            Assert.assertTrue(jspPage.contains("BA.js"));

            // Step 3 - load the page
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
