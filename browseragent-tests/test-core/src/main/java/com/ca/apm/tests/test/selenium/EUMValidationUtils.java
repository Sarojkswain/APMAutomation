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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.Assert;

import com.ca.apm.tests.test.selenium.jQueryAjax;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.JQueryAjaxTestApp;

import com.ca.apm.browseragent.testsupport.collector.BATestCollector;
import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.APMData;
import com.ca.apm.eum.datamodel.App;
import com.ca.apm.eum.datamodel.BA;
import com.ca.apm.eum.datamodel.ClientEvent;
import com.ca.apm.eum.datamodel.ClientEvents;
import com.ca.apm.eum.datamodel.ClientInfo;
import com.ca.apm.eum.datamodel.Cookie;
import com.ca.apm.eum.datamodel.Creator;
import com.ca.apm.eum.datamodel.EUM;
import com.ca.apm.eum.datamodel.Geolocation;
import com.ca.apm.eum.datamodel.HTTPMethodTypeEnum;
import com.ca.apm.eum.datamodel.Metric;
import com.ca.apm.eum.datamodel.NavigationTiming;
import com.ca.apm.eum.datamodel.Page;
import com.ca.apm.eum.datamodel.Pages;
import com.ca.apm.eum.datamodel.ProfileInfo;
import com.ca.apm.eum.datamodel.RawData;
import com.ca.apm.eum.datamodel.Request;
import com.ca.apm.eum.datamodel.Resource;
import com.ca.apm.eum.datamodel.ResourceTypeEnum;
import com.ca.apm.eum.datamodel.Resources;
import com.ca.apm.eum.datamodel.Response;
import com.ca.apm.eum.datamodel.Session;
import com.ca.apm.eum.datamodel.Sessions;
import com.ca.apm.eum.datamodel.ThinkTime;
import com.ca.apm.eum.datamodel.ThinkTimes;

/**
 * Helper util class for EUM validation
 * 
 */

public class EUMValidationUtils {

    private static final Logger LOGGER = Logger.getLogger(EUMValidationUtils.class);

    /**
     * These times are the maximum expected wait for the particular page and
     * function This is the max time the collector will wait for the metrics
     */

    // Most pages will probably fall under this time, if not then create ones
    // for that page

    public static final long STANDARD_WAIT = 10000;
    public static final long STANDARD_CLICK_WAIT = 15000;

    // index.html
    public static final long INDEX_LOAD_WAIT = 5000;

    // GETLocalDomain.jsp load time
    public static final long GET_LOCAL_DOMAIN_MAX_EXPECTED_LOAD_WAIT = 15000;

    // GETLocalDomain.jsp single button click
    public static final long GET_LOCAL_DOMAIN_MAX_EXPECTED_SINGLE_CLICK_WAIT = 15000;

    public static final int GET_LOCAL_DOMAIN_DEFAULT_AJAX_AT_LOAD = 0;

    // GETLocalDomain2.jsp load time
    public static final long GET_LOCAL_DOMAIN_2_MAX_EXPECTED_LOAD_WAIT = 20000;
    public static final long GET_LOCAL_DOMAIN_2_MAX_EXPECTED_SINGLE_CLICK_WAIT = 10000;
    public static final int GET_LOCAL_DOMAIN_2_DEFAULT_AJAX_AT_LOAD = 5;

    // GETCORS.jsp
    public static final long GET_CORS_MAX_EXPECTED_LOAD_WAIT = 15000;
    public static final long GET_CORS_MAX_EXPECTED_SINGLE_GET_WAIT = 15000;

    // GETLocalDomainQueryParams.jsp
    public static final long GET_LOCAL_DOMAIN_QUERY_MAX_LOAD_WAIT = 20000;
    public static final long GET_LOCAL_DOMAIN_QUERY_MAX_CLICK_WAIT = 5000;

    // AjaxClamp.jsp
    public static final long AJAX_CLAMP_MAX_LOAD_WAIT = 10000;
    public static final long AJAX_CLAMP_MAX_CLICK_WAIT = 20000;

    // GeoLocation.html
    public static final long GEO_LOCATION_MAX_LOAD_WAIT = 10000;
    public static final long GEO_LOCATION_MAX_CLICK_WAIT = 5000;

    // Page load wait time for jserrors/error_MultipleErrors.jsp
    public static final long ERROR_MAX_EXPECTED_LOAD_WAIT = 10000;

    // Reference button click wait time for jserrors/error_MultipleErrors.jsp
    public static final long REFERENCE_ERROR_MAX_EXPECTED_CLICK_WAIT = 5000;

    // Type Error button click wait time for jserrors/error_MultipleErrors.jsp
    public static final long TYPE_ERROR_MAX_EXPECTED_CLICK_WAIT = 5000;

    // Custom Error button click wait time for jserrors/error_MultipleErrors.jsp
    public static final long CUSTOM_ERROR_MAX_EXPECTED_CLICK_WAIT = 5000;

    // JSON values
    public static final String SCHEMA_VERSION_VALUE = "2.0";
    public static final String CREATOR_NAME = "BA";
    public static final String CREATOR_VERSION = "1.0";
    public static final String AJAX_TYPE = "AJAX";

    // JSON props ...
    public static final String RESOURCE_LIST = "resourceList";
    public static final String CLIENT_EVENT_LIST = "clientEventList";
    public static final String NAVIGATION_TIMING = "navigationTiming";

    // Business segment construction
    public static final String BUSINESS_SEGMENT_NAME = "Business Segment";
    public static final String BUSINESS_SEGMENT_SEPERATOR = "|";
    public static final String BUSINESS_SEGMENT_HOST_PORT_SEPERATOR = "/";
    public static final String BUSINESS_SEGMENT_AJAX_CALL_TYPE = "AJAX Call";
    public static final String BUSINESS_SEGMENT_JS_FUNC_CALL_TYPE = "JavaScript Function";

    // size of the sample.txt test file used for ajax testing
    public static final long SAMPLE_TXT_SIZE = 2915997;
    public static final String SAMPLE_TXT_FILE = "/brtmtestapp/sample.txt";

    public static final String CORS_TEST_HOST = "cors-test.appspot.com";
    public static final String CORS_TEST_PORT = "443";
    public static final String CORS_TEST_FILE = "/test";
    public static final long CORS_TEST_PAGE_SIZE = 15;

    // apmData
    public static final String RESOURCE_LOAD_TIME = "Resource Load Time";
    public static final String TIME_TO_FIRST_BYTE_AJAX = "Time To First Byte";
    public static final String RESPONSE_DOWNLOAD_TIME = "Response Download Time";
    public static final String CALLBACK_EXECUTION_TIME = "Callback Execution Time";
    public static final String INVOCATION_COUNT_PER_INTERVAL = "Invocation Count Per Interval";
    public static final String EXECUTION_TIME = "Execution Time";
    public static final String USER_DECISION_TIME = "User Decision Time";
    public static final String JS_ERRORS_PER_INTERVAL = "JavaScript Errors Per Interval";

    // BUG: Rename once reports are complete
    public static final String PAGE_RENDER_TIME = "Page Render Time";
    public static final String CONN_EST_TIME = "Connection Establishment Time";
    public static final String DOMAIN_LOOKUP_TIME = "Domain Lookup Time";
    public static final String DOM_PROCESSING_TIME = "DOM Processing Time";
    public static final String PAGE_LOAD_TIME = "Page Load Time";
    public static final String PAGE_STALL_TIME = "Page Stall Time";
    public static final String PREV_PAGE_UNLOAD_TIME = "Previous Page Unload Time";
    public static final String TIME_TO_FIRST_BYTE = "Time to First Byte";
    public static final String TIME_TO_LAST_BYTE = "Time to Last Byte";
    public static final String PAGE_HITS_PER_INTERVAL = "Page Hits Per Interval";

    // JS Function stuff

    public static final String JAVASCRIPT_FUNCTION = "JavaScript Function";
    public static final String XHR_OPEN = "XHR_Open";
    public static final String XHR_SEND = "XHR_Send";

    // Page type - the EUM object will contain attribute pageType with following
    // values:
    //
    public static final String SOFT_PAGE_TYPE_VALUE = "SP";
    public static final String HARD_PAGE_TYPE_VALUE = "HP";

    // When parsing the EUM object, each page has a specific type of above.
    //
    public static enum PageType {
        HARD(HARD_PAGE_TYPE_VALUE), SOFT(SOFT_PAGE_TYPE_VALUE);
        private String type = null;

        private PageType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    };

    /**
     * This method is a helper method to call into the collector's
     * waitForNextNotification and then run basic validation against the
     * returned MetricPostRecord list.
     * 
     * @param baTestCollector
     * @param maxWait
     *        how long should the collector wait for the expectedRecords ?
     *        if 3 records are expected in 5 seconds then 10 seconds is
     *        probably ok. Please try to use an existing constant defined in
     *        this class.
     * @param testStart
     * @param typesMap
     *        the map of types you are looking for, generated from
     *        PayloadUtils.generateTypesMap only need to use if updating the
     *        count of a type
     * @return List<MetricPostRecord> the returned records.
     * @throws MetricCollectionContextHandler.MetricCollectionTimeoutException
     */

    public static List<MetricPostRecord> waitAndBasicValidate(BATestCollector baTestCollector,
        long maxWait, Date testStart, Map<PayloadTypes, AbstractPayloadType> typesMap)
        throws MetricCollectionContextHandler.MetricCollectionTimeoutException {

        List<AbstractPayloadType> mapAsList = PayloadUtils.convertToList(typesMap);
        return waitAndBasicValidate(baTestCollector, maxWait, testStart, mapAsList);
    }

    /**
     * This method is a helper method to call into the collector's
     * waitForNextNotification and then run basic validation against the
     * returned MetricPostRecord list.
     * 
     * @param baTestCollector
     * @param maxWait
     *        how long should the collector wait for the expectedRecords ?
     *        if 3 records are expected in 5 seconds then 10 seconds is
     *        probably ok. Please try to use an existing constant defined in
     *        this class.
     * @param testStart
     * @param typesList
     *        the list of types you are looking for, generated from
     *        PayloadUtils.generateTypesList
     * @return List<MetricPostRecord> the returned records.
     * @throws MetricCollectionContextHandler.MetricCollectionTimeoutException
     */

    public static List<MetricPostRecord> waitAndBasicValidate(BATestCollector baTestCollector,
        long maxWait, Date testStart, List<AbstractPayloadType> typesList)
        throws MetricCollectionContextHandler.MetricCollectionTimeoutException {

        List<MetricPostRecord> recordList =
            baTestCollector.waitForNextNotification(maxWait, testStart, typesList);

        Assert.assertTrue(EUMValidationUtils.runBasicValidation(recordList));

        return recordList;
    }

    /**
     * Helper method since most of these function take the full list, in the
     * cases where you have only a single record to test
     * 
     * @param record
     * @return List<MetricPostRecord> the passed in record added to the list,
     *         empty if record was null;
     */

    public static List<MetricPostRecord> createList(MetricPostRecord record) {
        List<MetricPostRecord> returnList = new ArrayList<MetricPostRecord>();

        if (record != null) {
            returnList.add(record);
        }

        return returnList;
    }

    /**
     * Attempts to return the boolean for the page load flag. Will return null
     * if no pages found or error occurred
     * 
     * @param record
     * @return
     */

    public static Boolean getPageLoadFlag(MetricPostRecord record) {
        Boolean returnValue = null;

        try {
            returnValue =
                record.getEumObject().getApp().getBa().getPages().getPageList().get(0)
                    .isPageLoadFlag();
        } catch (Exception e) {
            LOGGER.error("getPageLoadFlag failed with error: " + e.getMessage());
            returnValue = null;
        }
        return returnValue;
    }

    /**
     * Determines if any think times exist in the passed list
     * 
     * @param recordList
     * @return boolean true if any think time was found
     */

    public static boolean hasThinkTime(List<MetricPostRecord> recordList) {
        return extractThinkTimes(recordList).size() > 0;
    }

    /**
     * Extracts out the think time objects from the passed recordList
     * 
     * @param recordList
     * @return List<ThinkTime> of think times, empty list of no times.
     */

    public static List<ThinkTime> extractThinkTimes(List<MetricPostRecord> recordList) {
        List<ThinkTime> returnList = new ArrayList<ThinkTime>();

        for (MetricPostRecord r : recordList) {
            List<Page> pageList = getPageList(r);

            for (Page p : pageList) {
                ThinkTimes times = p.getThinkTimes();

                if (times != null) {
                    List<ThinkTime> thinkList = times.getThinkTimeList();

                    if (thinkList != null) {
                        returnList.addAll(thinkList);
                    }
                }
            }
        }
        return returnList;
    }

    /**
     * Performs some asserts on the APM data for think time object.
     * 
     * @param thinkTime
     * @param fullUrlToTestPage
     */

    public static void validateThinkTime(ThinkTime thinkTime, String fullUrlToTestPage) {

        String[] hostPortPage = getHostPortPage(fullUrlToTestPage);

        String host = hostPortPage[0];
        String port = hostPortPage[1];
        String page = hostPortPage[2];

        List<Metric> metricList = thinkTime.getApmData().getMetrics().getMetricList();

        // Create a set for the ones we are looking for
        Set<String> metricNames = new HashSet<String>();
        metricNames.add(USER_DECISION_TIME);

        String expectedPath = constructBSPath(host, port, page, null);

        for (Metric metric : metricList) {
            Assert.assertEquals(metric.getPath(), expectedPath, "BS Path validation");
            metricNames.remove(metric.getName());

            if (USER_DECISION_TIME.equals(metric.getName())) {
                Assert.assertTrue(metric.getValue() > 0);
            }
        }

        Assert.assertTrue(metricNames.size() == 0, "Unable to find metric(s): " + metricNames);
    }

    /**
     * Takes in a full page url, and extracts out the host and port
     * 
     * @param origUrl
     * @return String [] host , port and page in locations 0,1,2 respectively
     */

    public static String[] getHostPortPage(String origUrl) {
        String[] hostPortPage = new String[3];

        String protocolSuffix = "://";
        int indexStartHost = origUrl.indexOf(protocolSuffix) + protocolSuffix.length();
        int indexEndHost = origUrl.indexOf(":", indexStartHost);

        if (indexEndHost > 0) {
            String host = origUrl.substring(indexStartHost, indexEndHost);
            hostPortPage[0] = host;

            int indexEndPort = origUrl.indexOf("/", indexEndHost);

            // assume was only passed something like
            // http://host:port <-- no page, no slash
            if (indexEndPort < 0) {
                indexEndPort = origUrl.length();
            }

            String port = origUrl.substring(indexEndHost + 1, indexEndPort);
            hostPortPage[1] = port;

        } else { // there is no port
            indexEndHost = origUrl.indexOf("/", indexStartHost);

            // http://brido02-win7 , no port , no page
            if (indexEndHost < 0) {
                indexEndHost = origUrl.length();
            }

            String host = origUrl.substring(indexStartHost, indexEndHost);
            hostPortPage[0] = host;
            hostPortPage[1] = null;
        }

        String hostPort = hostPortPage[0] + (hostPortPage[1] == null ? "" : ":" + hostPortPage[1]);

        int indexPageStart = origUrl.indexOf(hostPort) + hostPort.length();
        String page = null;

        if (indexPageStart < origUrl.length() - 1) {
            page = origUrl.substring(indexPageStart, origUrl.length());
        }

        hostPortPage[2] = page;

        return hostPortPage;
    }

    /**
     * This method will determine if the passed list has ajax metrics
     * 
     * @param recordList
     * @return boolean true if any records was ajax metrics
     */

    public static boolean hasAjaxMetrics(List<MetricPostRecord> recordList) {
        return extractAjax(recordList).size() > 0;
    }

    /**
     * This method will return any found ajax metrics
     * 
     * @param recordList
     * @return List<Resource> list of Resource that represent objects of found
     *         ajax metrics, empty if nothing found
     * 
     */

    public static List<Resource> extractAjax(List<MetricPostRecord> recordList) {
        List<Resource> returnResourceList = new ArrayList<Resource>();

        for (int i = 0; i < recordList.size(); i++) {
            List<Page> pageList = getPageList(recordList.get(i));
            returnResourceList.addAll(extractAjaxFromPages(pageList));
        }
        return returnResourceList;
    }

    /**
     * Extracts the resource objects from a list of passed pages.
     * 
     * @param pageList
     * @return
     */

    public static List<Resource> extractAjaxFromPages(List<Page> pageList) {
        List<Resource> returnResourceList = new ArrayList<Resource>();

        for (Page p : pageList) {
            Resources resources = p.getResources();

            if (resources != null) {
                List<Resource> resourceList = resources.getResourceList();

                for (Resource resource : resourceList) {
                    if (resource.getType().equals(ResourceTypeEnum.AJAX)) {
                        returnResourceList.add(resource);
                    }
                }
            }
        }

        return returnResourceList;
    }

    /**
     * WARNING: This method will make calls to assert failing your test Ajax
     * call assumes sample.txt, size, and GET, for more complex use validateAjax
     * 
     * @param resourceList
     *        your ajax to validate, likely from call extractAjax
     * @param fullUrlToTestPage
     */

    public static void validateAjaxGetLocal(List<Resource> resourceList, String fullUrlToTestPage) {
        String[] hostPortPage = getHostPortPage(fullUrlToTestPage);

        String host = hostPortPage[0];
        String port = hostPortPage[1];
        String page = hostPortPage[2];

        validateAjax(resourceList, host, port, page, HTTPMethodTypeEnum.GET, SAMPLE_TXT_FILE, host,
            port, SAMPLE_TXT_SIZE);
    }

    /**
     * WARNING: This method will make calls to assert failing your test
     * 
     * @param resourceList
     *        your ajax to validate, likely from call extractAjax
     * @param fullUrlToTestPage
     */

    public static void validateAjaxGetRemote(List<Resource> resourceList, String fullUrlToTestPage) {
        String[] hostPortPage = getHostPortPage(fullUrlToTestPage);

        String host = hostPortPage[0];
        String port = hostPortPage[1];
        String page = hostPortPage[2];

        validateAjax(resourceList, host, port, page, HTTPMethodTypeEnum.GET, CORS_TEST_FILE,
            CORS_TEST_HOST, CORS_TEST_PORT, CORS_TEST_PAGE_SIZE);
    }

    /**
     * WARNING: This method will make calls to assert failing your test
     * 
     * @param resourceList
     *        your ajax to validate, likely from call extractAjax
     * @param pageHost
     *        the host of the test app
     * @param pagePort
     *        the port of the test app
     * @param localPage
     *        the page i.e. GETLocalDomain.jsp
     * @param requestType
     *        GET @see HTTPMethodTypeEnum
     * @param ajaxRequestPage
     *        the ajax file/page i.e. sample.txt
     * @param ajaxCallHost
     *        the ajax host where the ajaxRequestPage lives
     * @param ajaxCallPort
     *        the port where the ajaxRequestPage lives
     * @param expectedPageSize
     *        the expected file size of the request in bytes
     */

    public static void validateAjax(List<Resource> resourceList, String pageHost, String pagePort,
        String localPage, HTTPMethodTypeEnum requestType, String ajaxRequestPage,
        String ajaxCallHost, String ajaxCallPort, long expectedPageSize) {

        for (Resource resource : resourceList) {

            // Confirm its AJAX, yes redudant because extractAjax was likely
            // called to make the passed in resourceList
            Assert.assertTrue(resource.getType().equals(ResourceTypeEnum.AJAX),
                "Resource type wasnt correct");

            // Confirm timestamp
            Assert.assertTrue(resource.getTimeStamp() > 0, "Timestamp wasnt set");

            // apmData with 5 metrics
            APMData apmData = resource.getApmData();

            List<Metric> metricList = apmData.getMetrics().getMetricList();

            // Create a set for the ones we are looking for
            Set<String> metricNames = new HashSet<String>();
            metricNames.add(RESOURCE_LOAD_TIME);
            metricNames.add(TIME_TO_FIRST_BYTE_AJAX);
            metricNames.add(RESPONSE_DOWNLOAD_TIME);
            metricNames.add(CALLBACK_EXECUTION_TIME);
            metricNames.add(INVOCATION_COUNT_PER_INTERVAL);

            String ajaxSegment = createAJAXSegment(ajaxRequestPage, ajaxCallHost, ajaxCallPort);
            String expectedPath = constructBSPath(pageHost, pagePort, localPage, ajaxSegment);

            for (Metric metric : metricList) {
                // System.out.println("PATH IS " + metric.getPath());
                Assert.assertEquals(metric.getPath(), expectedPath, "BS Path validation");
                metricNames.remove(metric.getName());

                // System.out.println("AJAX VALIDATE" + metric);
            }

            Assert.assertTrue(metricNames.size() == 0, "Unable to find metric(s): " + metricNames);

            // confirm request, sample.txt and method is GET
            Request request = resource.getRequest();
            String urlRequest = "http://" + ajaxCallHost + ":" + ajaxCallPort + ajaxRequestPage;

            // System.out.println("MY URL -----------------> " + urlRequest);
            // System.out.println("REQUEST ----------------> " +
            // request.getUrl());

            // Cant do this assert.
            // Assert.assertEquals(request.getUrl(), urlRequest,
            // "Request file url");
            // local http://brido02-win7:8181/brtmtestapp/sample.txt
            // remote https://cors-test.appspot.com/test

            // This accomplishes just about the same thing as above....
            Assert.assertTrue(request.getUrl().endsWith(ajaxRequestPage),
                "Request file page validation");
            Assert.assertTrue(request.getUrl().contains(ajaxCallHost),
                "Request file host validation");

            Assert.assertEquals(request.getMethod(), requestType, "Request method type");

            // confirm response status 200, content size is 2915997
            Response response = resource.getResponse();
            Assert.assertEquals(response.getStatus(), BATestCollectorUtils.HTTP_OK,
                "HTTP Status of 200");
            Assert.assertEquals(response.getContent().getSize(), expectedPageSize, "Content size");
        }
    }

    /**
     * WARNING: This method will make calls to assert failing your test
     * 
     * @param resourceList
     *        your ajax to validate, likely from call extractAjax
     * @param jQueryExpectedResult
     *        Map of expected result for
     * @param useJquery1MetricCount
     *        - For jQuery 1.x, metric count will vary for different ajax
     *        calls, set as true to use the metric count specified in map,
     *        false otherwise
     */
    public static void validateAjaxTestJQuery(List<Resource> resourceList,
        Map<String, jQueryAjax> jQueryExpectedResult, boolean useJQuery1MetricCount) {

        for (Resource resource : resourceList) {
            String resUrl = resource.getRequest().getUrl();

            int startIdx, endIdx;
            startIdx = resUrl.lastIndexOf('/');
            endIdx = resUrl.indexOf('.', startIdx);
            // Get page name - used as key for expected result map
            String key = resUrl.substring(startIdx + 1, endIdx);
            jQueryAjax currentItem = jQueryExpectedResult.get(key);

            Assert.assertTrue(resource.getType().equals(ResourceTypeEnum.AJAX),
                "Resource type wasnt correct");

            // Confirm timestamp
            Assert.assertTrue(resource.getTimeStamp() > 0, "Timestamp wasnt set");

            // apmData with 5 metrics
            APMData apmData = resource.getApmData();
            List<Metric> metricList = apmData.getMetrics().getMetricList();

            // Create a set for all ajax metrics
            Set<String> metricNames = new HashSet<String>();
            metricNames.add(RESOURCE_LOAD_TIME);
            metricNames.add(TIME_TO_FIRST_BYTE_AJAX);
            metricNames.add(RESPONSE_DOWNLOAD_TIME);
            metricNames.add(CALLBACK_EXECUTION_TIME);
            metricNames.add(INVOCATION_COUNT_PER_INTERVAL);

            String ajaxSegment =
                createAJAXSegment(currentItem.resourceFile, currentItem.ajaxHost,
                    currentItem.ajaxPort);
            String expectedPath =
                constructBSPath(currentItem.pageHost, currentItem.pagePort, currentItem.page,
                    ajaxSegment);
            long resourceLoadTime = -1, callBackExecTime = -1;

            for (Metric metric : metricList) {
                Assert.assertEquals(metric.getPath(), expectedPath, "BS Path validation");
                // Save Resource Load Time metric & Callback Execution time
                if (metric.getName().contains(RESOURCE_LOAD_TIME)) {
                    resourceLoadTime = metric.getValue();
                } else if (metric.getName().contains(CALLBACK_EXECUTION_TIME)) {
                    callBackExecTime = metric.getValue();
                }
                metricNames.remove(metric.getName());
            }

            // If test jQuery 1.x is used - # of metrics reported will vary for
            // various ajax calls, for jQuery 2.x+ all 5 metrics are reported
            if (useJQuery1MetricCount) {
                // For calls without success or complete callback
                if (currentItem.ajaxMetricCount == JQueryAjaxTestApp.METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1) {
                    Assert.assertTrue(metricNames.size() == 3,
                        "The following metrics were not reported : " + metricNames);
                    for (String metric : metricNames) {
                        // If Resource Load time or Invocation count is not reported, fail.
                        if (metric.equals(RESOURCE_LOAD_TIME)
                            || metric.equals(INVOCATION_COUNT_PER_INTERVAL)) {
                            Assert.fail("Required metrics" + RESOURCE_LOAD_TIME + " Or "
                                + INVOCATION_COUNT_PER_INTERVAL
                                + " were not reported for this Ajax call");
                        }
                    }
                } else if (currentItem.ajaxMetricCount == JQueryAjaxTestApp.METRIC_COUNT_WITH_CALLBACK_EXEC_TIME_JQUERY1) {
                    Assert.assertTrue(metricNames.size() == 2,
                        "The following metrics were not reported : " + metricNames);
                    for (String metric : metricNames) {
                        // If Resource Load time, Callback Execution Time or Invocation Count is not
                        // reported, fail.
                        if (metric.equals(RESOURCE_LOAD_TIME)
                            || metric.equals(INVOCATION_COUNT_PER_INTERVAL)
                            || metric.equals(CALLBACK_EXECUTION_TIME)) {
                            Assert.fail("Required metrics" + RESOURCE_LOAD_TIME + " Or "
                                + INVOCATION_COUNT_PER_INTERVAL + " or " + CALLBACK_EXECUTION_TIME
                                + " were not reported for this Ajax call");
                        }
                    }

                } else if (currentItem.ajaxMetricCount == JQueryAjaxTestApp.METRIC_COUNT_ALL_JQUERY) {
                    LOGGER.info("## metricNames.size() at this point is : " + metricNames.size());
                    Assert.assertTrue(metricNames.size() == 0, "Unable to find metric(s): "
                        + metricNames);
                }
            } else { // All 5 metrics are expected - JQuery 2.x & 3.x
                Assert.assertTrue(metricNames.size() == 0, "Unable to find metric(s): "
                    + metricNames);
            }

            // Check - Resource Load Time > Callback Execution Time
            if (resourceLoadTime != -1 && callBackExecTime != -1) {
                Assert.assertTrue(resourceLoadTime > callBackExecTime,
                    "Expected Resource Load Time  >  Callback Execution Time. ResourceLoadTime "
                        + resourceLoadTime + " CallBackExecTime " + callBackExecTime);
            }

            // Request, confirm that url resource File and method is as expected
            Request request = resource.getRequest();
            String urlRequest =
                "http://" + currentItem.ajaxHost + ":" + currentItem.ajaxPort
                    + currentItem.resourceFile;

            Assert.assertTrue(request.getUrl().contains(currentItem.resourceFile),
                "Expected Request resource file contains " + currentItem.resourceFile
                    + " found Request resource file " + request.getUrl());

            Assert.assertTrue(request.getUrl().contains(currentItem.ajaxHost),
                "Expected Request resource host to contain " + currentItem.ajaxHost
                    + " found Request Resource host " + request.getUrl());

            Assert.assertEquals(request.getMethod(), currentItem.method,
                "Expected Request method type " + request.getMethod()
                    + " found Request method type " + currentItem.method);

            Response response = resource.getResponse();

            Assert.assertEquals(response.getStatus(), currentItem.resourceStatuCode,
                "Expected HTTP Status of " + currentItem.resourceStatuCode
                    + " found HTTP status of " + response.getStatus());

            // Skip check for size resource that results in 404
            if (currentItem.resourceSize > 0) {
                Assert.assertEquals(response.getContent().getSize(), currentItem.resourceSize,
                    "Expected Content size " + currentItem.resourceSize + " found Content Size "
                        + response.getContent().getSize());
            }
        }
    }

    /**
     * This method will determine if the passed list has page metrics
     * 
     * @param recordList
     * @return boolean true if any records was page metrics
     */

    public static boolean hasPageMetrics(List<MetricPostRecord> recordList) {
        return extractPage(recordList).size() > 0;
    }

    /**
     * This method will determine if the passed list has page metrics
     * 
     * @param recordList
     * @return List<NavigationTiming> objects found, empty if nothing found
     */

    public static List<NavigationTiming> extractPage(List<MetricPostRecord> recordList) {
        return extractPage(recordList, null);
    }

    /**
     * This method will determine if the passed list has page metrics
     * 
     * @param recordList
     * @param loadClientEventList
     *        an optional list to get back the associated BusinessSegments
     * @return List<NavigationTiming> objects found, empty if nothing found
     */

    public static List<NavigationTiming> extractPage(List<MetricPostRecord> recordList,
        List<Metric> loadMetricList) {
        List<NavigationTiming> returnNavigationTimingList = new ArrayList<NavigationTiming>();

        for (int i = 0; i < recordList.size(); i++) {

            List<Page> pageList = getPageList(recordList.get(i));

            for (Page p : pageList) {
                RawData rawData = p.getRawData();

                if (rawData != null) {
                    NavigationTiming navTiming = rawData.getNavigationTiming();

                    if (navTiming != null) {

                        long start = navTiming.getNavigationStart();

                        // if we get this far clearly this data exists
                        if (start > 0) {
                            returnNavigationTimingList.add(navTiming);

                            if (loadMetricList != null) {
                                loadMetricList.addAll(p.getApmData().getMetrics().getMetricList());
                            }
                        }
                    }
                }

            }
        }
        return returnNavigationTimingList;
    }

    /**
     * WARNING: This method will make calls to assert failing your test
     * 
     * @param loadMetricList
     *        extracted from extractPage
     * @param fullUrlToTestPage
     */

    public static void validatePageMetrics(List<Metric> loadMetricList, String fullUrlToTestPage) {

        String[] hostPortPage = getHostPortPage(fullUrlToTestPage);

        String host = hostPortPage[0];
        String port = hostPortPage[1];
        String page = hostPortPage[2];

        String pageBS = constructBSPath(host, port, page, null);

        // We expect all these business segments...
        Set<String> expectedBS = new HashSet<String>();
        expectedBS.add(PAGE_RENDER_TIME);
        expectedBS.add(CONN_EST_TIME);
        expectedBS.add(DOMAIN_LOOKUP_TIME);
        expectedBS.add(DOM_PROCESSING_TIME);
        expectedBS.add(PAGE_LOAD_TIME);
        expectedBS.add(PAGE_STALL_TIME);
        expectedBS.add(PREV_PAGE_UNLOAD_TIME);
        expectedBS.add(TIME_TO_FIRST_BYTE);
        expectedBS.add(TIME_TO_LAST_BYTE);
        expectedBS.add(PAGE_HITS_PER_INTERVAL);

        for (Metric metric : loadMetricList) {
            // System.out.println("PROCESSING " + metric);
            Assert.assertEquals(metric.getPath(), pageBS);

            // remove each as they are processed...
            expectedBS.remove(metric.getName());

            // We can confirm a value for at least this metric.. others might
            // really be 0
            if (metric.getName().equals(PAGE_LOAD_TIME)) {
                Assert.assertTrue(metric.getValue() > 0, "Confirm non zero value");
                // System.out.println("PROCESSING " + metric);
            }
        }

        // now confirm they were all found
        Assert.assertTrue(expectedBS.size() == 0, "Unable to find BS: " + expectedBS);
    }

    /**
     * This method will determine if the passed list has JS Function metrics
     * 
     * @param recordList
     * @return boolean true if any records was page metrics
     */

    public static boolean hasJSFunctionMetrics(List<MetricPostRecord> recordList) {
        return extractJSFunction(recordList).size() > 0;
    }

    /**
     * This method will determine if the passed list has JS Function metrics
     * 
     * @param recordList
     * @return List<ClientEvent> objects, empty if nothing found
     */

    public static List<ClientEvent> extractJSFunction(List<MetricPostRecord> recordList) {
        List<ClientEvent> returnClientEventList = new ArrayList<ClientEvent>();

        for (int i = 0; i < recordList.size(); i++) {

            List<Page> pageList = getPageList(recordList.get(i));

            for (Page p : pageList) {
                ClientEvents clientEvents = p.getClientEvents();

                if (clientEvents != null) {
                    List<ClientEvent> eventList = clientEvents.getClientEventList();

                    if (eventList != null && eventList.size() > 0) {
                        returnClientEventList.addAll(eventList);
                    }
                }

            }
        }
        return returnClientEventList;
    }

    /**
     * WARNING: This method will make calls to assert failing your test
     * 
     * @param clientEventList
     * @param fullUrlToTestPage
     */

    public static void validateJSFunction(List<ClientEvent> clientEventList,
        String fullUrlToTestPage) {
        String[] hostPortPage = getHostPortPage(fullUrlToTestPage);

        String host = hostPortPage[0];
        String port = hostPortPage[1];
        String page = hostPortPage[2];

        String openBS = constructBSPath(host, port, page, createJSSegment(XHR_OPEN));
        String sendBS = constructBSPath(host, port, page, createJSSegment(XHR_SEND));

        Set<String> expectedBS = new HashSet<String>();
        expectedBS.add(openBS);
        expectedBS.add(sendBS);

        for (int i = 0; i < clientEventList.size(); i++) {
            ClientEvent event = clientEventList.get(i);
            APMData apmData = event.getApmData();

            List<Metric> metricList = apmData.getMetrics().getMetricList();

            Set<String> metricNames = new HashSet<String>();

            metricNames.add(INVOCATION_COUNT_PER_INTERVAL);
            metricNames.add(EXECUTION_TIME);

            String lastBSpath = null;

            for (Metric m : metricList) {
                metricNames.remove(m.getName());

                boolean isDefined = expectedBS.contains(m.getPath());
                Assert.assertTrue(isDefined, "Not a defined: " + isDefined + " expected: "
                    + expectedBS);

                // System.out.println("JS VALIDATE" + m);
            }

            Assert.assertTrue(metricNames.size() == 0, "Unable to find metric(s): " + metricNames);
        }
    }

    /**
     * This method will determine if the passed list has JS errors
     * 
     * @param recordList
     * @return boolean true if any records was page metrics
     */

    public static boolean hasJSError(List<MetricPostRecord> recordList) {
        return extractJSErrors(recordList).size() > 0;
    }

    /**
     * This method will determine if the passed list has JS errors
     * 
     * @param recordList
     * @return the list of found errors across all the passed recordList
     */

    public static List<com.ca.apm.eum.datamodel.Error> extractJSErrors(
        List<MetricPostRecord> recordList) {
        List<com.ca.apm.eum.datamodel.Error> returnErrorList =
            new ArrayList<com.ca.apm.eum.datamodel.Error>();

        for (int i = 0; i < recordList.size(); i++) {

            List<Page> pageList = getPageList(recordList.get(i));

            for (Page p : pageList) {
                com.ca.apm.eum.datamodel.Errors errors = p.getErrors();

                if (errors != null) {
                    List<com.ca.apm.eum.datamodel.Error> errorList = errors.getErrorList();

                    if (errorList != null && errorList.size() > 0) {
                        returnErrorList.addAll(errorList);
                    }
                }

            }
        }
        return returnErrorList;
    }

    /**
     * This method will simply look for the existence of non-null values for
     * json items that should exist in every payload. as detailed here
     * https://cawiki
     * .ca.com/display/APM/Browser+Agent+AXA+Manual+Testing+for+July
     * 
     * @param recordList
     *        the list to validate
     * @return boolean true or false on overall success
     */

    public static boolean runBasicValidation(List<MetricPostRecord> recordList) {
        return runBasicValidation(recordList, null);
    }

    /**
     * This method will simply look for the existence of non-null values for
     * json items that should exist in every payload. as detailed here
     * https://cawiki
     * .ca.com/display/APM/Browser+Agent+AXA+Manual+Testing+for+July
     * 
     * @param recordList
     *        the list to validate
     * @param resultList
     *        maybe null, optional list for getting results on each record.
     *        the list order corresponds to the passed recordList.
     * @return boolean true or false on overall success, the first record
     *         failure results in false
     */

    public static boolean runBasicValidation(List<MetricPostRecord> recordList,
        List<Boolean> resultList) {

        boolean overAllresult = true;

        for (MetricPostRecord record : recordList) {

            boolean result = runBasicValidation(record.getEumObject());

            // User passed in non-null wishes to get values back
            if (resultList != null) {
                resultList.add(result);
            }

            // When any false is found set the overall to false.
            if (result == false) {
                overAllresult = false;

                // of the user passed null doesnt care, since the first false is
                // found
                // can exit early.
                if (resultList == null) {
                    break;
                }
            }
        }

        return overAllresult;
    }

    /***
     * Looks for the same fingerprint and can return in option string builder
     * 
     * @param recordList
     * @param returnFingerPrint
     * @return boolean true if all the same fingerprint
     */

    public static boolean hasAllSameFingerPrint(List<MetricPostRecord> recordList,
        StringBuilder returnFingerPrint) {
        Set<String> fingerPrintSet = new HashSet<String>();

        for (MetricPostRecord record : recordList) {
            EUM eum = record.getEumObject();
            ClientInfo clientInfo = eum.getClientInfo();
            String fingerPrint = clientInfo.getFingerPrint();

            if (fingerPrint == null || fingerPrint.length() == 0) {
                break;
            }

            // if we didnt break add the finger print
            fingerPrintSet.add(fingerPrint);
        }

        // If the size is one then anthing else added was equal to the first.
        // All the same would also be true if only one record was passed.
        boolean isAllSame = fingerPrintSet.size() == 1;

        String[] fpAsArray = (String[]) fingerPrintSet.toArray(new String[] {});

        // user wants the fingerPrint
        if (returnFingerPrint != null && fpAsArray.length > 0) {
            returnFingerPrint.append(fpAsArray[0]);
        }

        return isAllSame;
    }

    /**
     * Retrieves the Url present in each page object
     * 
     * @param recordList
     * @return List<List<String>
     */

    public static List<List<String>> extractPageUrl(List<MetricPostRecord> recordList) {
        List<List<String>> returnList = new ArrayList<List<String>>();

        for (int i = 0; i < recordList.size(); i++) {
            List<Page> pageList = getPageList(recordList.get(i));
            List<String> urlList = new ArrayList<String>();
            for (Page p : pageList) {
                urlList.add(p.getUrl());
            }
            returnList.add(urlList);
        }
        return returnList;
    }

    /**
     * Extracts the Geolocation info present in the clientInfo object
     * 
     * @param recordList
     * @return list of Geolocation objects or a list of null
     */
    public static List<Geolocation> extractGeoLocation(List<MetricPostRecord> recordList) {
        List<Geolocation> geoLocationList = new ArrayList<Geolocation>();

        for (MetricPostRecord record : recordList) {
            EUM eum = record.getEumObject();
            ClientInfo clientInfo = eum.getClientInfo();
            geoLocationList.add(clientInfo.getGeolocation());
        }
        return geoLocationList;
    }

    public static List<Cookie> createCookieList(String cookies) {
        List<Cookie> cookieList = new ArrayList<Cookie>();
        String[] cookieArray = cookies.split("; ");
        for (int i = 0; i < cookieArray.length; i++) {
            Cookie c = new Cookie();
            c.setName(cookieArray[i].split("=")[0]);
            c.setValue(cookieArray[i].split("=")[1]);
            cookieList.add(c);
        }
        return cookieList;

    }

    public static List<List<Cookie>> extractCookieList(List<MetricPostRecord> recordList) {
        List<List<Cookie>> returnList = new ArrayList<List<Cookie>>();

        for (int i = 0; i < recordList.size(); i++) {
            List<Page> pageList = getPageList(recordList.get(i));
            List<Cookie> cookieList = new ArrayList<Cookie>();
            for (Page p : pageList) {
                if (p.getCookies() != null) {
                    returnList.add(p.getCookies().getCookieList());
                } else {
                    returnList.add(null);
                }
            }
        }
        return returnList;

    }

    /**
     * Extracts all the pages of the passed type.
     * 
     * @param recordList
     * @param type
     * @return
     */

    public static List<Page> extractPagesByType(List<MetricPostRecord> recordList, PageType type) {
        List<Page> returnList = new ArrayList<Page>();

        for (int i = 0; i < recordList.size(); i++) {
            List<Page> pageList = getPageList(recordList.get(i));

            for (Page p : pageList) {
                if (p.getPageType().equals(type.getType())) {
                    returnList.add(p);
                }
            }
        }

        return returnList;
    }

    /**
     * These some of the basic required values for each.... required by all
     * tests, specifically 454931, 454926,454929, 454928, 454740, 454741, 454738
     * 
     * 
     * @param eum
     * @return
     */

    private static boolean runBasicValidation(EUM eum) {

        String schemaVersion = eum.getSchemaVersion();
        LOGGER.info("start runBasicValidation ");

        if (nullOrEmpty(schemaVersion) || !schemaVersion.equals(SCHEMA_VERSION_VALUE)) {
            LOGGER.info("schemaVersion failed nullOrEmpty check, value: " + schemaVersion);
            return false;
        }

        // Verify non-null values for creator
        //
        Creator creator = eum.getCreator();

        String creatorName = creator.getName();
        if (nullOrEmpty(creatorName) || !creatorName.equals(CREATOR_NAME)) {
            LOGGER.info("creatorName failed nullOrEmpty check, value: " + creatorName);
            return false;
        }

        String creatorVersion = creator.getVersion();
        if (nullOrEmpty(creatorVersion) || !creatorVersion.equals(CREATOR_VERSION)) {
            LOGGER.info("creatorVersion failed nullOrEmpty check, value: " + creatorVersion);
            return false;
        }

        // finger print info, etc.
        //
        ClientInfo clientInfo = eum.getClientInfo();

        String fingerPrint = clientInfo.getFingerPrint();
        if (nullOrEmpty(fingerPrint)) {
            LOGGER.info("fingerPrint failed nullOrEmpty check, value: " + fingerPrint);
            return false;
        }

        String userAgent = clientInfo.getUserAgent();
        if (nullOrEmpty(userAgent)) {
            LOGGER.info("userAgent failed nullOrEmpty check, value: " + userAgent);
            return false;
        }

        String browserType = clientInfo.getBrowserType();
        if (nullOrEmpty(browserType)) {
            LOGGER.info("browserType failed nullOrEmpty check, value: " + browserType);
            return false;
        }

        String browserMV = clientInfo.getBrowserMajorVersion();
        if (nullOrEmpty(browserMV)) {
            LOGGER.info("browserMV failed nullOrEmpty check, value: " + browserMV);
            return false;
        }

        // verify non-null values for app
        //
        App app = eum.getApp();

        String appId = app.getId();
        if (nullOrEmpty(appId)) {
            LOGGER.info("appId failed nullOrEmpty check, value: " + appId);
            return false;
        }

        // Covers ALM test 454740
        Assert.assertEquals(appId, BATestCollectorUtils.DEFAULT_APP);

        String appKey = app.getKey();
        if (nullOrEmpty(appKey)) {
            LOGGER.info("appKey failed nullOrEmpty check, value: " + appKey);
            return false;
        }

        // Covers ALM test 454741
        Assert.assertEquals(appKey, BATestCollector.APP_KEY);

        String appVersion = app.getVersion();
        if (nullOrEmpty(appVersion)) {
            LOGGER.info("appVersion failed nullOrEmpty check, value: " + appVersion);
            return false;
        }

        String appTenantId = app.getTenantId();
        if (nullOrEmpty(appTenantId)) {
            LOGGER.info("appTenantId failed nullOrEmpty check, value: " + appTenantId);
            return false;
        }

        // Covers ALM test 454738
        Assert.assertEquals(appTenantId, BATestCollectorUtils.DEFAULT_TENANT);

        // Verify profile info
        //
        ProfileInfo profileInfo = app.getProfileInfo();

        String profileId = profileInfo.getId();
        if (nullOrEmpty(profileId)) {
            LOGGER.info("profileId failed nullOrEmpty check, value: " + profileId);
            return false;
        }

        String profileName = profileInfo.getName();
        if (nullOrEmpty(profileName)) {
            LOGGER.info("profileName failed nullOrEmpty check, value: " + profileName);
            return false;
        }

        long createdAt = profileInfo.getCreatedAt();
        long lastUpdatedAt = profileInfo.getLastUpdatedAt();
        // make sure the times make sense...
        if (!(createdAt > 0 && lastUpdatedAt > 0 && createdAt <= lastUpdatedAt)) {
            LOGGER.info("createdAt & lastUpdatedAt failed time check, createdAt: " + createdAt
                + ", lastUpdatedAt: " + lastUpdatedAt);
            return false;
        }

        // Actual pages
        //
        BA browserAgent = app.getBa();
        Pages pages = browserAgent.getPages();
        List<Page> pageList = pages.getPageList();

        if (pageList.size() == 0) {
            LOGGER.info("page size was zero ");
            return false;
        }

        for (Page page : pageList) {

            // timestamp part of 454928
            Assert.assertTrue(page.getTimeStamp() > 0);

            String url = page.getUrl();
            Sessions sessions = page.getSessions();

            List<Session> sessionsList = sessions.getSessionList();

            for (Session session : sessionsList) {
                String id = session.getId();
                if (nullOrEmpty(id)) {
                    LOGGER.info("session id failed nullOrEmpty check, value: " + id);
                    return false;
                }

                long startTime = session.getStartTime();
                if (startTime <= 0) {
                    LOGGER.info("startTime not set: " + startTime);
                    return false;
                }

                boolean newSession = session.isNewSessionFlag();
                // What to do with this one? it will be false for non initlized
            }
        }

        // got this far....
        return true;
    }

    private static boolean nullOrEmpty(String v) {
        return v == null || v.length() == 0;
    }

    private static List<Page> getPageList(MetricPostRecord record) {
        EUM eum = record.getEumObject();
        App app = eum.getApp();
        BA browserAgent = app.getBa();
        Pages pages = browserAgent.getPages();
        List<Page> pageList = pages.getPageList();

        return pageList;
    }

    /**
     * This is a helper for validateAjax et al methods to construct a Business
     * Segment
     * 
     * @param pageHost
     *        the host of the test app
     * @param pagePort
     *        the port of the test app
     * @param localPage
     *        the page i.e. GETLocalDomain.jsp
     * @param segmentPageOrFunction
     *        created depending, createAJAXSegment, createJSSegment
     * 
     * @return String the BusinessSegment
     */

    private static String constructBSPath(String pageHost, String pagePort, String localPage,
        String segmentPageOrFunction) {
        // String ajaxCallHost, String ajaxCallPort) {

        // Constructs the following:
        // Business Segment|brido02-win7/8181|/brtmtestapp/GETLocalDomain.jsp
        // |AJAX Call|brido02-win7/8181|/brtmtestapp/sample.txt
        StringBuilder stringBuilder = new StringBuilder();

        // Business Segment|
        stringBuilder.append(BUSINESS_SEGMENT_NAME);
        stringBuilder.append(BUSINESS_SEGMENT_SEPERATOR);

        // brido02-win7/8181|
        stringBuilder.append(pageHost);
        stringBuilder.append(BUSINESS_SEGMENT_HOST_PORT_SEPERATOR);
        stringBuilder.append(pagePort);
        stringBuilder.append(BUSINESS_SEGMENT_SEPERATOR);

        // /brtmtestapp/GETLocalDomain.jsp

        stringBuilder.append(localPage);

        if (segmentPageOrFunction != null && segmentPageOrFunction.length() > 0) {
            stringBuilder.append(BUSINESS_SEGMENT_SEPERATOR);

            stringBuilder.append(segmentPageOrFunction);
        }

        return stringBuilder.toString();
    }

    private static String createAJAXSegment(String ajaxRequestPage, String ajaxCallHost,
        String ajaxCallPort) {
        StringBuilder stringBuilder = new StringBuilder();

        // |AJAX Call|brido02-win7/8181|
        stringBuilder.append(BUSINESS_SEGMENT_AJAX_CALL_TYPE);
        stringBuilder.append(BUSINESS_SEGMENT_SEPERATOR);

        stringBuilder.append(ajaxCallHost);
        stringBuilder.append(BUSINESS_SEGMENT_HOST_PORT_SEPERATOR);
        stringBuilder.append(ajaxCallPort);

        stringBuilder.append(BUSINESS_SEGMENT_SEPERATOR);

        // /brtmtestapp/sample.txt
        stringBuilder.append(ajaxRequestPage);

        return stringBuilder.toString();
    }

    private static String createJSSegment(String functionName) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(JAVASCRIPT_FUNCTION);
        stringBuilder.append(BUSINESS_SEGMENT_SEPERATOR);
        stringBuilder.append(functionName);

        return stringBuilder.toString();
    }

}
