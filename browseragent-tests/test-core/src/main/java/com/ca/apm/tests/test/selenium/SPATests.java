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
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.HTTPMethodTypeEnum;
import com.ca.apm.eum.datamodel.Metric;
import com.ca.apm.eum.datamodel.Page;
import com.ca.apm.eum.datamodel.Resource;
import com.ca.apm.eum.datamodel.ThinkTime;
import com.ca.apm.tests.test.selenium.EUMValidationUtils.PageType;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/**
 * Single Page Application tests
 */

public class SPATests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(SPATests.class);

    public SPATests() {

    }

    public SPATests(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }

    @Test
    public void testBasicDataIntegrityTest_455156() {
        try {

            //
            // Step 2 part 1 - hard page url, pageloadflag, pagetype, sessions, timestamp,
            // thinktimes, 1 resource , rawData, apmData
            //
            WebDriver webDriver = getDriver();

            Date testStart = new Date();
            String fullPageUrl = getTestAppURL() + BrtmTestApp.SPA_INDEX;
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.HARD_PAGE_TYPE,
                    PayloadTypes.SOFT_PAGE_TYPE);
            webDriver.get(fullPageUrl);

            List<MetricPostRecord> recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.STANDARD_WAIT, testStart, typeList);

            // The initial page load of the spa page expecting a single hard page and a soft page,
            // fail fast.
            List<Page> hardPageList =
                EUMValidationUtils.extractPagesByType(recordList, PageType.HARD);
            Assert.assertTrue(hardPageList.size() == 1, "Only expecting one hard page");

            List<Page> softPageList =
                EUMValidationUtils.extractPagesByType(recordList, PageType.SOFT);
            Assert.assertTrue(softPageList.size() == 1, "Only expecting one soft page");

            // Now lets look into the guts of each hard and soft page

            //
            // First hard page...
            //
            Page hardPage = hardPageList.get(0);
            Assert.assertEquals(hardPage.getUrl(), fullPageUrl);
            Assert.assertTrue(hardPage.isPageLoadFlag());
            Assert.assertEquals(hardPage.getPageType(), EUMValidationUtils.HARD_PAGE_TYPE_VALUE);

            // Has a session object
            Assert.assertTrue(hardPage.getSessions().getSessionList().size() == 1);
            Assert
                .assertTrue(hardPage.getSessions().getSessionList().get(0).getStartTime() == hardPage
                    .getRawData().getNavigationTiming().getNavigationStart());

            // Hard page should have a think time object
            Assert.assertTrue(hardPage.getThinkTimes().getThinkTimeList().size() > 0);

            // Ok to access 0 , the previous assert will fail...
            ThinkTime thinkTime = hardPage.getThinkTimes().getThinkTimeList().get(0);
            EUMValidationUtils.validateThinkTime(thinkTime, fullPageUrl);


            // The initial page this is null
            Assert.assertTrue(hardPage.getPrevPage() == null);
            Assert.assertTrue(hardPage.getReferrer() == null);

            // The hard page has the raw data for page load
            Assert.assertTrue(hardPage.getRawData().getNavigationTiming().getNavigationStart() > 0);
            Assert
                .assertTrue(hardPage.getRawData().getNavigationTiming().getNavigationStart() < hardPage
                    .getRawData().getNavigationTiming().getLoadEventEnd());

            // We should also have apmData for this page
            EUMValidationUtils.validatePageMetrics(hardPage.getApmData().getMetrics()
                .getMetricList(), fullPageUrl);

            //
            // First Soft page on initial load is actually redirect.
            //
            Page softPage = softPageList.get(0);
            Assert.assertEquals(softPage.getUrl(), fullPageUrl + "#/");
            Assert.assertTrue(softPage.isPageLoadFlag());
            Assert.assertEquals(softPage.getPageType(), EUMValidationUtils.SOFT_PAGE_TYPE_VALUE);

            // Has a session object
            Assert.assertTrue(softPage.getSessions().getSessionList().size() == 1);
            Assert.assertFalse(softPage.getSessions().getSessionList().get(0).isNewSessionFlag());

            // Soft page session start equals that of the hard page session start.
            Assert
                .assertTrue(softPage.getSessions().getSessionList().get(0).getStartTime() == hardPage
                    .getSessions().getSessionList().get(0).getStartTime());


            // soft page wont have a think time
            Assert.assertTrue(softPage.getThinkTimes() == null);

            // The redirect comes from the index.html (no hash)
            Assert.assertEquals(softPage.getPrevPage().getUrl(), fullPageUrl);
            Assert.assertEquals(softPage.getReferrer().getUrl(), fullPageUrl);

            Assert.assertTrue(softPage.getReferrer().getTimeStamp() == hardPage.getRawData()
                .getNavigationTiming().getNavigationStart());

            // The soft page does not have the raw data for nav timing, but soft page load
            Assert.assertTrue(softPage.getRawData().getNavigationTiming() == null);
            Assert.assertTrue(softPage.getRawData().getSoftPageTiming().getStartTime() > 0);

            // Check the resource object, this can exist in either the soft page or the hard page
            // above, but not both.
            List<Resource> hardResource = EUMValidationUtils.extractAjaxFromPages(hardPageList);
            List<Resource> softResource = EUMValidationUtils.extractAjaxFromPages(softPageList);

            List<Resource> allResource = new ArrayList<Resource>();
            allResource.addAll(hardResource);
            allResource.addAll(softResource);

            // Only one ajax tied to either the hard or soft page.
            Assert.assertTrue(allResource.size() == 1);

            // Assert passed above so this get is safe
            Resource resource = allResource.get(0);

            // The first soft page is actually a redirection to main.htm
            Assert.assertEquals(resource.getRequest().getUrl(), getTestAppURL() + "/spa/main.htm");

            // Validate the actual ajax metrics
            String[] hostPortPage = EUMValidationUtils.getHostPortPage(fullPageUrl);
            String host = hostPortPage[0];
            String port = hostPortPage[1];
            String page = hostPortPage[2];

            // For firefox the ajax will come in the second page which is the index
            // redirected to index#/
            if (isFireFox()) {
                page += "|#/";
            }

            // This calls assert on metric paths
            EUMValidationUtils.validateAjax(allResource, host, port, page, HTTPMethodTypeEnum.GET,
                "/brtmtestapp/spa/main.htm", host, port, 79);

            //
            // Step 2 part 2 - soft page - click "red"
            // Ajax can be associated with either hard or soft page.
            //
            typeList = PayloadUtils.generateTypesList(PayloadTypes.SOFT_PAGE_TYPE);
            // expecting two soft pages this time. Technically they are going to arrive
            // in the same payload anyway, but to be explicit just in case.
            typeList.get(0).setCount(2);
            testStart = new Date();
            WebElement redLink = webDriver.findElement(By.linkText("Red"));
            redLink.click();
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.STANDARD_WAIT, testStart, typeList);

            softPageList = EUMValidationUtils.extractPagesByType(recordList, PageType.SOFT);
            Assert.assertTrue(softPageList.size() == 2);

            //
            // First soft page - think time Assume its the first one
            //
            Page theThinkTimePage = softPageList.get(0);
            Assert.assertEquals(theThinkTimePage.getPageType(),
                EUMValidationUtils.SOFT_PAGE_TYPE_VALUE);
            thinkTime = theThinkTimePage.getThinkTimes().getThinkTimeList().get(0);
            // Validate against where we just came from which was the redirected initial page
            EUMValidationUtils.validateThinkTime(thinkTime, fullPageUrl + "|#/");
            Assert.assertEquals(theThinkTimePage.getPrevPage().getUrl(), fullPageUrl);
            Assert.assertEquals(theThinkTimePage.getReferrer().getUrl(), fullPageUrl);

            Assert.assertFalse(theThinkTimePage.isPageLoadFlag());
            Assert.assertFalse(theThinkTimePage.getSessions().getSessionList().get(0)
                .isNewSessionFlag());

            //
            // Second soft page
            //

            Page redPage = softPageList.get(1);
            Assert.assertEquals(redPage.getPageType(), EUMValidationUtils.SOFT_PAGE_TYPE_VALUE);

            Assert.assertEquals(redPage.getUrl(), fullPageUrl + "#/red");

            Assert.assertEquals(redPage.getPrevPage().getUrl(), fullPageUrl + "#/");
            Assert.assertEquals(redPage.getReferrer().getUrl(), fullPageUrl);

            Assert.assertTrue(redPage.isPageLoadFlag());
            Assert.assertFalse(redPage.getSessions().getSessionList().get(0).isNewSessionFlag());

            Assert.assertTrue(redPage.getRawData().getSoftPageTiming().getStartTime() > 0);

            Metric pageLoad = redPage.getApmData().getMetrics().getMetricList().get(0);
            Assert.assertTrue(pageLoad.getPath().endsWith("index.html|#/red"));
            
            Assert.assertTrue(pageLoad.getName().equals(EUMValidationUtils.PAGE_LOAD_TIME));

            Assert.assertTrue(theThinkTimePage.getReferrer().getTimeStamp() == redPage
                .getReferrer().getTimeStamp());

            softResource = EUMValidationUtils.extractAjaxFromPages(softPageList);

            // Dont care at this time what page its attached to
            Assert.assertTrue(softResource.size() == 1);

            // Assert passed above so this get is safe
            resource = softResource.get(0);

            String redPageName = "/spa/red.htm";

            // The first soft page is red.htm
            Assert.assertEquals(resource.getRequest().getUrl(), getTestAppURL() + redPageName);

            // Validate the actual ajax metrics
            hostPortPage = EUMValidationUtils.getHostPortPage(fullPageUrl + "|#/red");
            host = hostPortPage[0];
            port = hostPortPage[1];
            page = hostPortPage[2];

            // This calls assert on metric paths
            EUMValidationUtils.validateAjax(softResource, host, port, page, HTTPMethodTypeEnum.GET,
                "/brtmtestapp" + redPageName, host, port, 76);

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
