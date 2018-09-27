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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler.MetricCollectionTimeoutException;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.Cookie;
import com.ca.apm.eum.datamodel.Geolocation;
import com.ca.apm.eum.datamodel.ThinkTime;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/**
 * This class holds a collection of selenium tests covering non specific Browser Agent functionality
 *
 */

public class GeneralTests extends SeleniumBase {

    private static final Logger LOGGER = Logger.getLogger(GeneralTests.class);

    public GeneralTests() {

    }

    public GeneralTests(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        super(details, testAppUrl, collectorWorkingDir);
    }


    /**
     * Tests the page load flag
     */

    @Test
    public void testPageLoad_455101() {
        try {
            // Get the driver to be used for this test...
            WebDriver webDriver = getDriver();

            // step 2 load page confirm page and flag is true
            Date testStart = new Date();

            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

            List<MetricPostRecord> recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // This is the page record,
            MetricPostRecord pageMetric = recordList.get(0);
            Assert.assertTrue(EUMValidationUtils.getPageLoadFlag(pageMetric));


            // Step 3 - ajax call and page load is false
            int inputValue = 1;
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));

            testStart = new Date();
            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT
                            * inputValue, testStart, typeList);
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));

            // This record will have pageLoad false
            MetricPostRecord ajaxRecord = recordList.get(0);
            Assert.assertFalse(EUMValidationUtils.getPageLoadFlag(ajaxRecord));

            // Step 4 - disable page
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setPageLoadMetricsEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disablePage");

            // Step 5 - reload confirm page not reported
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

            Assert.assertFalse(EUMValidationUtils.hasPageMetrics(recordList));

            // Step 6 click button confirm ajax and pageload is true
            testStart = new Date();
            inputValue = 1;
            valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));
            sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT
                            * inputValue, testStart, typeList);
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));

            // This time the ajax record will have pageLoad true
            ajaxRecord = recordList.get(0);
            Assert.assertTrue(EUMValidationUtils.getPageLoadFlag(ajaxRecord));
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
     * This tests checks whether the cookie snapshot is reported accurately or not based on several
     * scenarios:
     * Geo location is enabled
     * 1)User accepts location tracking and there is an application level cookie present -- cookie
     * snapshot should be sent only once
     * 2,3)User doesn't accept location tracking/does nothing and there is an application level
     * cookie present -- cookie snapshot should not be sent
     * 
     * Geo location is disabled and there is an application level cookie present -- cookie snapshot
     * should not be sent
     *
     */
    @Test
    public void testCookieSnapshot_454734() {
        // Step1: - enable Location tracking(Geo location)
        Attributes attrs = collectorConfig.getBaAttributes();
        attrs.setGeoEnabled(true);
        attrs.setCookieCaptureEnabled(true);
        baTestCollector.updateConfiguration(collectorConfig, "enableGeoLocation");

        // Step2: Load the GeoLocation.html test page
        // We don't care about page load metrics for this initial load
        WebDriver webDriver = openPage(getTestAppURL() + BrtmTestApp.GEO_LOCATION_PAGE);
        // Spoof the user action of accepting the Geo location
        WebElement sendButton = webDriver.findElement(By.id("enableLocationTrackingValid"));
        sendButton.click();
        // Now load the page again, the real test now starts
        webDriver.get(getTestAppURL() + BrtmTestApp.GEO_LOCATION_PAGE);
        // Get the list of current cookies
        try {
            Object obj = executeInBrowserConsole("document.cookie");
            List<Cookie> currentCookies = EUMValidationUtils.createCookieList((String) obj);

            // Step3: Add application level cookie
            WebElement keyInput = webDriver.findElement(By.id("key"));
            keyInput.sendKeys("AppCookie1");
            WebElement valueInput = webDriver.findElement(By.id("value"));
            valueInput.sendKeys("AppValue1");
            WebElement addCookieButton = webDriver.findElement(By.id("addCustomCookie"));
            addCookieButton.click();
            // check that the cookie got added
            obj = executeInBrowserConsole("document.cookie");
            String tempS = (String) obj;
            Assert.assertTrue(tempS.contains("AppCookie1=AppValue1"));

            // Step4: Reload the page
            webDriver.get(getTestAppURL() + BrtmTestApp.GEO_LOCATION_PAGE);
            Date testStart = new Date();

            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

            webDriver.get(getTestAppURL() + BrtmTestApp.GEO_LOCATION_PAGE);

            List<MetricPostRecord> recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GEO_LOCATION_MAX_LOAD_WAIT, testStart, typeList);
            List<List<Cookie>> cookieSnapShot = EUMValidationUtils.extractCookieList(recordList);
            // Assert that the first EUM object, in this case page load metrics contains cookie
            // snapshot
            Assert.assertNotNull(cookieSnapShot.get(0));

            // Assert that cookie snapshot data reports all cookies except BA specific cookies in
            // cookies object in page load metrics.
            for (int i = 0; i < currentCookies.size(); i++) {
                for (int j = 0; j < cookieSnapShot.get(0).size(); j++) {
                    Assert.assertNotEquals(currentCookies.get(i), cookieSnapShot.get(0).get(j),
                        "they are equal");
                }
            }

            // Step5: Make an Ajax call
            testStart = new Date();
            WebElement ajaxCallButton = webDriver.findElement(By.id("ajaxCall"));
            ajaxCallButton.click();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GEO_LOCATION_MAX_LOAD_WAIT, testStart, typeList);
            cookieSnapShot = EUMValidationUtils.extractCookieList(recordList);
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));
            // Assert that the cookie snapshot is null in the Ajax metric EUM object
            Assert.assertNull(cookieSnapShot.get(0));


            // Step10 and 11: Close and reopen browser, this time User denies the location tracking
            // permission
            // The steps to automate this, is exactly the same as previous steps 6,7,8 because
            // we are spoofing the user actions, denying or not responding to location tracking
            // is the same spoof.
            // hence Skipping steps 10 and 11.

            // Step12 and 13: Disable location tracking and close and reopen browser
            // I will close and reopen the browser and then set the Geo location to false
            // and the refresh the page, so that it indeed gets the new profile
            closeDriver();
            Thread.currentThread().sleep(3000);
            webDriver = getDriver();
            webDriver.get(getTestAppURL() + BrtmTestApp.GEO_LOCATION_PAGE);
            attrs = collectorConfig.getBaAttributes();
            attrs.setGeoEnabled(false);
            attrs.setCookieCaptureEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disableGeoLocation");
            // refresh the page
            webDriver.get(getTestAppURL() + BrtmTestApp.GEO_LOCATION_PAGE);
            // Step14: is to repeat step 7 and 8
            // Performing step 7 again
            keyInput = webDriver.findElement(By.id("key"));
            keyInput.clear();
            keyInput.sendKeys("AppCookie1");
            valueInput = webDriver.findElement(By.id("value"));
            valueInput.sendKeys("AppValue1");
            addCookieButton = webDriver.findElement(By.id("addCustomCookie"));
            addCookieButton.click();
            obj = executeInBrowserConsole("document.cookie");
            // Assert that newly added cookie is present
            System.out.println("current cookies: " + (String) obj);
            Assert.assertTrue(((String) obj).contains("AppCookie1=AppValue1"));
            // Performing Step8 again
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            webDriver.get(getTestAppURL() + BrtmTestApp.GEO_LOCATION_PAGE);

            // Check the page load metrics to verify that we do not have cookie snapshot
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GEO_LOCATION_MAX_LOAD_WAIT, testStart, typeList);
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            cookieSnapShot = EUMValidationUtils.extractCookieList(recordList);
            // Assert that we do not have cookie snapshot
            Assert.assertNull(cookieSnapShot.get(0));


        } catch (Exception e) {
            LOGGER.info("Error in executing script in browser console");
            e.printStackTrace();
        }


    }

    /**
     * This test looks at specific JSON response and doesn't use EUM object in all cases.
     * Not a good example to follow only for this test do we look at specific JSON
     */


    @Test
    public void testJSON_454928_454929_454926_454931() {
        try {

            // Prereq - enable js function
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setJsFunctionMetricsEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "enableJsFunction");

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

            MetricPostRecord loadMetric = recordList.get(0);

            // Look at the JSON see if we have page
            boolean foundPage = false;
            if (loadMetric.getJsonMetricPost().contains(EUMValidationUtils.NAVIGATION_TIMING)) {
                // for added santiy (as all other tests should do)...
                foundPage =
                    EUMValidationUtils.hasPageMetrics(EUMValidationUtils.createList(loadMetric));
            }
            Assert.assertTrue(foundPage);

            testStart = new Date();
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys("1");
            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT, testStart,
                    typeList);

            // 454931 - after single click validate profileId, profileName, created, lasteUpdated
            // 454926 - after single click eum basic structure 2.0, BA
            // 454929 - after single click clientinfo : browserType, browserMajorVersion,
            // fingerPrint, userAgent
            // 454928 - after single click pageList, clientEventList, resources type

            // This is redundant because waitAndBasicValidate will call. However just in case that
            // method changes we will explicitly call here. Additionally, runBasicValidation
            // has been modified to call out specific items we are looking for
            Assert.assertTrue(EUMValidationUtils.runBasicValidation(recordList));

            boolean foundAjax = false;


            for (MetricPostRecord record : recordList) {
                String json = record.getJsonMetricPost();

                Assert.assertTrue(json.contains(EUMValidationUtils.SCHEMA_VERSION_VALUE));

                // Found possible ajax
                if (json.contains(EUMValidationUtils.RESOURCE_LIST)
                    && json.contains(EUMValidationUtils.AJAX_TYPE)) {
                    // As added sanity test lets confirm.. this is the way all other tests should
                    // use!!
                    foundAjax =
                        EUMValidationUtils.hasAjaxMetrics(EUMValidationUtils.createList(record));
                }
            }

            Assert.assertTrue(foundAjax);


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
     * Tests Geo location user actions
     * Looks at the following scenarios:
     * 1)User accepts location tracking and browser obtains valid location values(lat,lon)
     * 2)User accepts location tracking but browser cannot find location values, hence they are
     * missing
     * 3)User accepts location tracking but browser obtains invalid/out-of-range location values
     * 4)User denies location tracking
     * 5)Geo location is disabled
     */



    @Test
    public void testGeoLocation_454932() {
        try {

            // Step1: - enable Geo location
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setGeoEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "enableGeoLocation");

            // Open the Geo Location page
            String fullUrlToTestPage = getTestAppURL() + BrtmTestApp.GEO_LOCATION_PAGE;
            WebDriver webDriver = openPage(fullUrlToTestPage);

            // Simulate the valid scenario:
            // 1)User accepts location tracking and the browser finds the accurate location
            // and adds it to session storage

            // Click on the 'Accept Location Tracking' button

            WebElement sendButton = webDriver.findElement(By.id("enableLocationTrackingValid"));
            sendButton.click();
            // This will set the lat,lon to valid values
            // Now refresh the page, and make an Ajax call
            webDriver = openPage(fullUrlToTestPage);

            Date testStart = new Date();
            WebElement ajaxButton = webDriver.findElement(By.id("ajaxCall"));
            ajaxButton.click();
            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            List<MetricPostRecord> recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GEO_LOCATION_MAX_CLICK_WAIT, testStart, typeList);

            List<Geolocation> geoList = EUMValidationUtils.extractGeoLocation(recordList);
            Assert.assertNotNull(geoList.get(0));
            double latitude = geoList.get(0).getLatitude();
            double longitude = geoList.get(0).getLongitude();
            // Assert that we get an accurate location
            Assert.assertTrue(latitude >= -90 && latitude <= 90);
            Assert.assertTrue(longitude >= -180 && longitude <= 180);

            // 2)User accepts the location tracking but the browser cannot find it and times out,
            // that is, to say, that the location values are missing,
            // BA now stores -255,-255 into the session storage

            // Click on the 'Accept Location Tracking but Browser cannot find it' button
            sendButton = webDriver.findElement(By.id("enableLocationTrackingMissing"));
            sendButton.click();
            // refresh the page, so as to simulate that we opened the page afresh
            webDriver = openPage(fullUrlToTestPage);
            // make an Ajax call
            testStart = new Date();
            ajaxButton = webDriver.findElement(By.id("ajaxCall"));
            ajaxButton.click();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GEO_LOCATION_MAX_CLICK_WAIT, testStart, typeList);

            geoList = EUMValidationUtils.extractGeoLocation(recordList);
            // we do not expect any Geo location information in the EUM object
            Assert.assertNull(geoList.get(0));

            // 3)User accepts the location tracking but due to some error the browser stores
            // incorrect/invalid/out of range values for lat,lon
            // BA now stores 180,-256 into the session storage
            sendButton = webDriver.findElement(By.id("enableLocationTrackingInvalid"));
            sendButton.click();
            // refresh the page, so as to simulate that we opened the page afresh
            webDriver = openPage(fullUrlToTestPage);
            // make an Ajax call
            testStart = new Date();
            ajaxButton = webDriver.findElement(By.id("ajaxCall"));
            ajaxButton.click();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GEO_LOCATION_MAX_CLICK_WAIT, testStart, typeList);

            geoList = EUMValidationUtils.extractGeoLocation(recordList);
            // we do not expect any Geo location information in the EUM object
            Assert.assertNull(geoList.get(0));

            // 4)User declines location tracking
            // In this case, the BA puts -401,-401 into the Session storage
            sendButton = webDriver.findElement(By.id("declineLocationTracking"));
            sendButton.click();
            // refresh the page, so as to simulate that we opened the page afresh
            webDriver = openPage(fullUrlToTestPage);
            // make an Ajax call
            testStart = new Date();
            ajaxButton = webDriver.findElement(By.id("ajaxCall"));
            ajaxButton.click();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GEO_LOCATION_MAX_CLICK_WAIT, testStart, typeList);

            geoList = EUMValidationUtils.extractGeoLocation(recordList);
            // we do not expect any Geo location information in the EUM object
            Assert.assertNull(geoList.get(0));

            // 5)Disable Geo location
            // Set valid values for location in session storage
            // Tests that BA does not send out geo location (even if we have it) if it is disabled
            attrs = collectorConfig.getBaAttributes();
            attrs.setGeoEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disableGeoLocation");
            sendButton = webDriver.findElement(By.id("enableLocationTrackingValid"));
            sendButton.click();
            webDriver = openPage(fullUrlToTestPage);
            // make an Ajax call
            testStart = new Date();
            ajaxButton = webDriver.findElement(By.id("ajaxCall"));
            ajaxButton.click();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GEO_LOCATION_MAX_CLICK_WAIT, testStart, typeList);

            geoList = EUMValidationUtils.extractGeoLocation(recordList);
            // we do not expect any Geo location information in the EUM object
            Assert.assertNull(geoList.get(0));


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
     * Tests whether the ThinkTime is being dispatched or not, for valid scenarios.
     */
    @Test
    public void testThinkTimeValidValues_455014() {
        try {
            WebDriver webDriver = getDriver();
            // Step 1: Open the GETLocalDomain.jsp page
            Date testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            List<AbstractPayloadType> typeList =
                PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

            List<MetricPostRecord> recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // Step 2: Make sure that we only get page load metrics and no Think Time metrics
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertFalse(EUMValidationUtils.hasThinkTime(recordList));
            recordList = null;

            // Step 3: Load the page GETCORS
            // We now expect one Think Time object and one Page load metrics
            testStart = new Date();
            typeList = null;
            typeList =
                PayloadUtils
                    .generateTypesList(PayloadTypes.PAGE_TYPE, PayloadTypes.THINK_TIME_TYPE);

            webDriver.get(getTestAppURL() + BrtmTestApp.GET_CORS_PAGE);

            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.GET_CORS_MAX_EXPECTED_LOAD_WAIT, testStart, typeList);


            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertTrue(EUMValidationUtils.hasThinkTime(recordList));

            // Get the Url from both pages and compare, they should be different

            List<List<String>> urlList = EUMValidationUtils.extractPageUrl(recordList);
            // The urlList should contain 2 values, one from each record and they should be
            // different
            if (urlList.size() > 0) {
                Assert.assertNotEquals(urlList.get(0).get(0), urlList.get(1).get(0));
            } else {
                LOGGER.fatal("Unexpected exception, urlList size is less than 1, failing test");
                Assert.fail();
            }

            // Check the ThinkTime object, ensure that the start time is less than or equal to end
            // time.
            List<ThinkTime> myTimes = EUMValidationUtils.extractThinkTimes(recordList);
            for (ThinkTime tt : myTimes) {
                Assert.assertTrue(tt.getStartTime() <= tt.getEndTime());
            }

            // Step 4: Close the browser confirm think time
            testStart = new Date();
            typeList = PayloadUtils.generateTypesList(PayloadTypes.THINK_TIME_TYPE);
            // calling closeDriver will exit the browser too fast on firefox and we wont
            // get the metrics. Use delay version.
            closeDriverWithDelay(EUMValidationUtils.STANDARD_WAIT);
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.STANDARD_WAIT * 2, testStart, typeList);
            Assert.assertTrue(EUMValidationUtils.hasThinkTime(recordList));
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
     * Tests to check that ThinkTime should not be dispatched when BA is disabled
     * due to 204 config update.
     */
    @Test
    public void testThinkTimeUpdatedConfig_455015() {
        WebDriver webDriver = getDriver();
        // Step 1: Open the GETLocalDomain.jsp page
        Date testStart = new Date();
        webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

        List<AbstractPayloadType> typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

        List<MetricPostRecord> recordList;
        try {
            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // Step 2: Make sure that we only receive Page load metrics and no Think time metrics
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertFalse(EUMValidationUtils.hasThinkTime(recordList));

            // Step 3: Refresh the page, this we receive Think Time EUM object and Page load metrics

            testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);


            typeList =
                PayloadUtils
                    .generateTypesList(PayloadTypes.PAGE_TYPE, PayloadTypes.THINK_TIME_TYPE);

            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertTrue(EUMValidationUtils.hasThinkTime(recordList));

            // Step 4: Update the profile, disable BA
            Attributes attrs = collectorConfig.getBaAttributes();
            attrs.setBrowserAgentEnabled(false);
            baTestCollector.updateConfiguration(collectorConfig, "disableBrowserAgent");

            // Step 5: Make an AJAX call, that is request one file,
            // The server will send a 204 back
            // After the browser processes that 204 and gets the new profile, the BA will be
            // disabled.

            int inputValue = 1;
            WebElement valueInputElement = webDriver.findElement(By.id("NumberOfRequests"));
            valueInputElement.sendKeys(Integer.toString(inputValue));

            testStart = new Date();
            WebElement sendButton = webDriver.findElement(By.name("GetLocalFile"));
            sendButton.click();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.AJAX_TYPE);

            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // The browser should receive AJAX metrics
            Assert.assertTrue(EUMValidationUtils.hasAjaxMetrics(recordList));

            // Allow some time for the browser agent to response to 204, download and apply new
            // config...

            Thread.sleep(3000);
            // Enable Browser Agent for the next page.
            attrs = collectorConfig.getBaAttributes();
            attrs.setBrowserAgentEnabled(true);
            baTestCollector.updateConfiguration(collectorConfig, "reEnableBrowserAgent");

            // Step 6: Refresh the page, this time we should only see the Page load metric and not
            // the Think time

            testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);


            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // Make sure that we only receive Page load metrics and no Think time metrics
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertFalse(EUMValidationUtils.hasThinkTime(recordList));



        } catch (MetricCollectionTimeoutException timeoutException) {
            LOGGER.fatal("Unexpected timeout from waitForNextNotification, failing test",
                timeoutException);
            assertFail(timeoutException);
        } catch (InterruptedException e) {
            LOGGER.fatal("Unexpected exception from sleep, failing test", e);
            assertFail(e);
        }

    }

    /**
     * Test checks that when opening a link in a new window,the collector does not receive
     * any Think Time object from the previous page because it is still open.
     */
    @Test
    public void testThinkTimeOpenLinkInNewWindow_455016() {

        WebDriver webDriver = getDriver();
        // Step 1: Open the GETLocalDomain.jsp page
        Date testStart = new Date();
        webDriver.get(getTestAppURL() + BrtmTestApp.INDEX_PAGE);

        List<AbstractPayloadType> typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);

        List<MetricPostRecord> recordList;
        try {
            recordList =
                EUMValidationUtils.waitAndBasicValidate(baTestCollector,
                    EUMValidationUtils.INDEX_LOAD_WAIT, testStart, typeList);

            // Step 2: Make sure that we only receive Page load metrics and no Think time metrics
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertFalse(EUMValidationUtils.hasThinkTime(recordList));

            // Step 3: Right click on GETLocalDomain link and open in new window
            testStart = new Date();
            // Get the element which has the link for "GETLocalDomain.jsp"
            WebElement element = webDriver.findElement(By.linkText("GET Request Local Domain"));
            Actions action = new Actions(webDriver);
            action.contextClick(element).sendKeys(Keys.SHIFT).sendKeys(Keys.UP).build().perform();
            action.sendKeys(Keys.ARROW_UP).click().build().perform();

            typeList = PayloadUtils.generateTypesList(PayloadTypes.PAGE_TYPE);
            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // Step 4: Make sure that we only receive Page load metrics and no Think time metrics
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertFalse(EUMValidationUtils.hasThinkTime(recordList));

            // Step 5: Navigate to GETLocalDomain.jsp in the original window
            testStart = new Date();
            webDriver.get(getTestAppURL() + BrtmTestApp.GET_LOCAL_DOMAIN_PAGE);

            typeList =
                PayloadUtils
                    .generateTypesList(PayloadTypes.PAGE_TYPE, PayloadTypes.THINK_TIME_TYPE);
            recordList =
                EUMValidationUtils
                    .waitAndBasicValidate(baTestCollector,
                        EUMValidationUtils.GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT, testStart,
                        typeList);

            // We expect Think Time EUM object for the Index.html page and Page load EUM object for
            // the GETLocalDomain.jsp
            Assert.assertTrue(EUMValidationUtils.hasPageMetrics(recordList));
            Assert.assertTrue(EUMValidationUtils.hasThinkTime(recordList));
            List<List<String>> urlList = EUMValidationUtils.extractPageUrl(recordList);
            // The urlList should contain 2 values, one from each record and they should be
            // different.
            if (urlList.size() > 0) {
                Assert.assertNotEquals(urlList.get(0).get(0), urlList.get(1).get(0));
            } else {
                LOGGER.fatal("Unexpected exception from urlList size is less than 1, failing test");
                Assert.fail();
            }
            recordList = null;
            typeList = null;
        } catch (MetricCollectionTimeoutException timeoutException) {
            LOGGER.fatal("Unexpected timeout from waitForNextNotification, failing test",
                timeoutException);
            assertFail(timeoutException);
        }
    }


}
