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

/**
 * Browser Agent automation - Agent & Browser Agent metrics
 *
 * @author gupra04
 */

package com.ca.apm.tests.utils.constants;

public class MetricConstants {

    public static final String METRIC_NODE_DELIMETER = "|";
    public static final String METRIC_NAME_DELIMETER = ":";
    public static final String METRIC_BROWSER_PAGE_NAME_SEP = "/";
    public static final String SUPERDOMAIN = "*SuperDomain*";
    public static final String BUSINESS_SEGMENT = "Business Segment";
    public static final int BA_METRIC_VERIFICATION_TIME = 7;

    public static final class BlameTypeMetrics {
        public static final String AVERAGE_RESPONSE_TIME = "Average Response Time (ms)";
        public static final String CONCURRENT_INVOCATIONS = "Concurrent Invocations";
        public static final String ERRORS_PER_INTERVAL = "Errors Per Interval";
        public static final String RESPONSES_PER_INTERVAL = "Responses Per Interval";
        public static final String STALL_COUNT = "Stall Count";
    }

    public static final class BrowserMetrics {
        public static final String AVERAGE_BROWSER_RENDER_TIME = "Average Browser Render Time (ms)";
        public static final String AVERAGE_CONNECTION_ESTABLISHMENT_TIME =
            "Average Connection Establishment Time (ms)";
        public static final String AVERAGE_DNS_LOOKUP_TIME = "Average DNS Lookup Time (ms)";
        public static final String AVERAGE_DOM_CONSTRUCTION_TIME =
            "Average DOM Construction Time (ms)";
        public static final String AVERAGE_PAGE_LOAD_COMPLETE_TIME =
            "Average Page Load Complete Time (ms)";
        public static final String AVERAGE_PREVIOUS_PAGE_UNLOAD_TIME =
            "Average Previous Page Unload Time (ms)";
        public static final String AVERAGE_ROUND_TRIP_TIME = "Average Round Trip Time (ms)";
        public static final String AVERAGE_TIME_TO_FIRST_BYTE = "Average Time to First Byte (ms)";
        public static final String AVERAGE_TIME_TO_LAST_BYTE = "Average Time to Last Byte (ms)";
        public static final String RESPONSES_PER_INTERVAL = "Responses Per Interval";

        // Custom metrics for JS Extension tests
        public static final String CUSTOM_PAGE_METRIC_AVERAGE = "Custom Page Metric Average (ms)";
        public static final String CUSTOM_PAGE_METRIC_COUNT = "Custom Page Metric Count";
    }

    public static final class AJAXMetrics {
        public static final String AJAX_CALLBACK_EXECUTION_TIME = "Callback Execution Time (ms)";
        public static final String AJAX_INVOCATION_COUNT_PER_INTERVAL =
            "Invocation Count Per Interval";
        public static final String AJAX_TOTAL_RESOURCE_DOWNLOAD_TIME =
            "Total Resource Load Time (ms)";
        public static final String AJAX_TIME_TO_FIRST_BYTE = "Time To First Byte (ms)";
        public static final String AJAX_TOTAL_RESOURCE_LOAD_TIME = "Total Resource Load Time (ms)";

        // Custom Ajax metrics for JS Extension tests
        public static final String CUSTOM_AJAX_METRIC_AVERAGE = "Custom Ajax Metric Average (ms)";
        public static final String CUSTOM_AJAX_METRIC_COUNT = "Custom Ajax Metric Count";
    }

    public static final class NoBTMetricPath {
        public static final String BRTM_TEST_APP = "/brtmtestapp";
    }

    public static final class AJAXMetricPath {
        public static final String GET_CORS_AJAX_HOST_PORT = "cors-test.appspot.com/443";
        public static final String GET_CORS_AJAX_PATH = "/test";
        public static final String GET_LOCAL_DOMAIN_AJAX_PATH = NoBTMetricPath.BRTM_TEST_APP
            + "/sample.txt";
        public static final String AJAX_CLAMP_PAGE_1_AJAX_PATH =
            "/brtmtestapp/ajaxclamp/1/sample.txt";
        public static final String AJAX_CLAMP_PAGE_2_AJAX_PATH =
            "/brtmtestapp/ajaxclamp/2/sample.txt";
        public static final String AJAX_CLAMP_PAGE_3_AJAX_PATH =
            "/brtmtestapp/ajaxclamp/3/sample.txt";
        public static final String AJAX_CLAMP_PAGE_4_AJAX_PATH =
            "/brtmtestapp/ajaxclamp/4/sample.txt";
        public static final String AJAX_CLAMP_PAGE_5_AJAX_PATH =
            "/brtmtestapp/ajaxclamp/5/sample.txt";
        public static final String AJAX_CLAMP_PAGE_6_AJAX_PATH =
            "/brtmtestapp/ajaxclamp/6/sample.txt";
        public static final String AJAX_CLAMP_PAGE_7_AJAX_PATH =
            "/brtmtestapp/ajaxclamp/7/sample.txt";
        public static final String AJAX_CLAMP_PAGE_ALL_AJAX_PATH =
            "/brtmtestapp/ajaxclamp/allsample.txt";
    }

    public static final class JavaScriptMetrics {
        public static final String JS_OPEN = "XHR_Open";
        public static final String JS_SEND = "XHR_Send";
        public static final String JS_AVERAGE_EXECUTION_TIME = "Average Execution Time (ms)";
        public static final String JS_INVOCATION_COUNT_PER_INTERVAL =
            "Invocation Count Per Interval";
        // Custom Function and Metrics for JS extension test cases
        public static final String JS_CUSTOM_MATH_RANDOM = "Math_Random";
        public static final String JS_CUSTOM_SEND_REQUEST = "Send_Request";
        public static final String JS_CUSTOM_FUNC_METRIC_AVERAGE =
            "Custom JS Func Metric Average (ms)";
        public static final String JS_CUSTOM_FUNC_METRIC_COUNT = "Custom JS Func Metric Count";
    }

    public static final class MiscMetric {
        public static final String MISC_METRIC_PATH = METRIC_NODE_DELIMETER + "MISC";
        public static final String MISC_METRIC = "Button Click Count Per Interval";
    }

    private MetricConstants() {
        throw new AssertionError();
    }

}
