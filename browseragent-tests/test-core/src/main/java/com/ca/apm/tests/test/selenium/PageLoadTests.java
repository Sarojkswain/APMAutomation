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
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.Metric;
import com.ca.apm.eum.datamodel.NavigationTiming;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/**
 * This class holds a collection of selenium tests covering page load tests
 *
 */

public class PageLoadTests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(PageLoadTests.class);

    public PageLoadTests() {

    }

    public PageLoadTests(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }

    /**
     * Tests basic page load
     */


    @Test
    public void testPage_454728_454731_454730_454732() {
        try {

            // Alm threshold test 454732
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setPageLoadMetricsThreshold(15000);
            baTestCollector.updateConfiguration(collectorConfig, "setPageThreshold15000");

            // Get the driver to be used for this test...
            WebDriver webDriver = getDriver();

            Date testStart = new Date();
            String fullPageUrl = getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE;
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
            webDriver.get(fullPageUrl);

            List<MetricPostRecord> recordList = null;
            try {
                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            // Should NOT have page, threshold too high
            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));

            // reset the threshold back to 0
            attrs = collectorConfig.getBaAttributes();
            attrs.setPageLoadMetricsThreshold(0);
            baTestCollector.updateConfiguration(collectorConfig, "setPageThresholdBackTo0");

            // Start a new test that launches page and see the one expected record (page metrics)
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            webDriver.get(fullPageUrl);

            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));

            // 454730 Raw Navigation Data is present
            List<NavigationTiming> navList = EUMValidationUtils.extractPage(recordList);
            if (navList != null && navList.size() > 0) {
                // Test the ones that should always exist
                for (NavigationTiming navTiming : navList) {
                    Assert.assertTrue(navTiming.getNavigationStart() > 0);
                    Assert.assertTrue(navTiming.getFetchStart() > 0);
                    Assert.assertTrue(navTiming.getDomainLookupStart() > 0);
                    Assert.assertTrue(navTiming.getDomainLookupEnd() > 0);
                    Assert.assertTrue(navTiming.getConnectStart() > 0);
                    Assert.assertTrue(navTiming.getConnectEnd() > 0);
                    Assert.assertTrue(navTiming.getRequestStart() > 0);
                    Assert.assertTrue(navTiming.getResponseStart() > 0);
                    Assert.assertTrue(navTiming.getResponseEnd() > 0);
                    Assert.assertTrue(navTiming.getDomLoading() > 0);
                    Assert.assertTrue(navTiming.getDomInteractive() > 0);
                    Assert.assertTrue(navTiming.getDomContentLoadedEventStart() > 0);
                    Assert.assertTrue(navTiming.getDomContentLoadedEventEnd() > 0);
                    Assert.assertTrue(navTiming.getDomComplete() > 0);
                    Assert.assertTrue(navTiming.getLoadEventStart() > 0);
                    Assert.assertTrue(navTiming.getLoadEventEnd() > 0);
                }

            } else {
                Assert.fail("Was expecting NavigationTiming");
            }



            // Step 4
            List<Metric> loadMetricList = new ArrayList<Metric>();
            EUMValidationUtils.extractPage(recordList, loadMetricList);
            EUMValidationUtils.validatePageMetrics(loadMetricList, fullPageUrl);

            // Step 4 and 5 of test 454731
            // disable page metrics, load the page confirm no page metrics
            attrs = collectorConfig.getBaAttributes();
            attrs.setPageLoadMetricsEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disablePageMetrics");

            // Page is disabled, but attempt to wait anyway until timeout
            recordList = null;
            try {
                testStart = new Date();
                typeList = PayloadUtils.generateTypesList(PayloadTypes.TIME_OUT_TYPE);
                webDriver.get(fullPageUrl);

                recordList =
                    EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            } catch (MetricCollectionContextHandler.MetricCollectionTimeoutException timeoutException) {
                recordList = timeoutException.partialResponseList;
            }

            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));

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
