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
 * Test application page url's for
 * Browser Agent automation
 *
 * @author gupra04
 */

package com.ca.apm.tests.utils.constants;

import java.io.File;

public class TestAppUrlConstants {

    public static final class ClickListenerTest_2 {
        // TODO: Add url's as needed
    }

    // TODO: Add additional pages as needed by tests
    public static final class BrtmTestApp {
        public static final String INDEX_PAGE = "/index.html";
        public static final String GET_LOCAL_DOMAIN_PAGE = "/GETLocalDomain.jsp";
        public static final String GET_LOCAL_DOMAIN_2_PAGE = "/GETLocalDomain2.jsp";
        public static final String GET_CORS_PAGE = "/GETCORS.jsp";
        public static final String GET_CORS_PAGE2 = "/GETCORS2.jsp";
        public static final String AJAX_CLAMP_PAGE = "/ajaxclamp/AjaxClamp.jsp";
        public static final String ERROR_MULTI_ERROR_PAGE = "/jserrors/error_MultipleErrors.jsp";
        public static final String GET_LOCAL_QUERY_PARAMS_PAGE = "/GETLocalDomainQueryParams.jsp";
        public static final String GEO_LOCATION_PAGE = "/GeoLocation.html";
        public static final String JS_FUNCTION_RETRY = "/JSFunctionRetry.jsp";
        public static final String SPA_INDEX = "/spa/index.html";
        public static final String JQUERY_1_X_PAGE = "/jquery/ajaxTest.html";
        public static final String JQUERY_2_X_PAGE = "/jquery/ajaxTest2.html";
        public static final String JQUERY_3_X_PAGE = "/jquery/ajaxTest3.html";

        public static String getPagePathInFileSystemFormat(String urlPage) {
            return urlPage.replace("/", File.separator);
        }
    }

    public static final class JQueryAjaxTestApp {

        // Resources used for ajax calls for ajaxTest page used in JQuery tests

        public static final int METRIC_COUNT_NO_CALLBACK_EXEC_TIME_JQUERY1 = 2;
        public static final int METRIC_COUNT_WITH_CALLBACK_EXEC_TIME_JQUERY1 = 3;
        public static final int METRIC_COUNT_ALL_JQUERY = 5;
        public static final int AJAX_CALLS_COUNT = 25;
        public static final long JQUERY_MAX_LOAD_WAIT = 120000;

        public static final long CONTENT1_TXT_SIZE = 311;
        public static final String CONTENT1_KEY = "ajaxContent1";
        public static final String CONTENT1_TXT_FILE = "/brtmtestapp/jquery/ajaxContent1.jsp";

        public static final String CONTENT1A_KEY = "ajaxContent1a";
        public static final long CONTENT1A_TXT_SIZE = 323;
        public static final String CONTENT1A_TXT_FILE = "/brtmtestapp/jquery/ajaxContent1a.jsp";

        public static final String CONTENT2_KEY = "ajaxContent2";
        public static final long CONTENT2_TXT_SIZE = 336;
        public static final String CONTENT2_TXT_FILE = "/brtmtestapp/jquery/ajaxContent2.jsp";

        public static final String CONTENT2A_KEY = "ajaxContent2a";
        public static final long CONTENT2A_TXT_SIZE = 349;
        public static final String CONTENT2A_TXT_FILE = "/brtmtestapp/jquery/ajaxContent2a.jsp";

        public static final String CONTENT3_KEY = "ajaxContent3";
        public static final long CONTENT3_TXT_SIZE = 336;
        public static final String CONTENT3_TXT_FILE = "/brtmtestapp/jquery/ajaxContent3.jsp";

        public static final String CONTENT4_KEY = "ajaxContent4";
        public static final long CONTENT4_TXT_SIZE = 336;
        public static final String CONTENT4_TXT_FILE = "/brtmtestapp/jquery/ajaxContent4.jsp";

        public static final String CONTENT5_KEY = "ajaxContent5";
        public static final long CONTENT5_TXT_SIZE = 115;
        public static final String CONTENT5_TXT_FILE = "/brtmtestapp/jquery/ajaxContent5.json";

        public static final String CONTENT6_KEY = "ajaxContent6";
        public static final long CONTENT6_TXT_SIZE = 115;
        public static final String CONTENT6_TXT_FILE = "/brtmtestapp/jquery/ajaxContent6.json";

        public static final String CONTENT7_KEY = "ajaxContent7";
        public static final long CONTENT7_TXT_SIZE = 54;
        public static final String CONTENT7_TXT_FILE = "/brtmtestapp/jquery/ajaxContent7.js";

        public static final String CONTENT8_KEY = "ajaxContent8";
        public static final long CONTENT8_TXT_SIZE = 56;
        public static final String CONTENT8_TXT_FILE = "/brtmtestapp/jquery/ajaxContent8.js";

        public static final String CONTENT9_KEY = "ajaxContent9";
        public static final long CONTENT9_TXT_SIZE = 336;
        public static final String CONTENT9_TXT_FILE = "/brtmtestapp/jquery/ajaxContent9.jsp";

        public static final String CONTENT9A_KEY = "ajaxContent9a";
        public static final long CONTENT9A_TXT_SIZE = 349;
        public static final String CONTENT9A_TXT_FILE = "/brtmtestapp/jquery/ajaxContent9a.jsp";

        public static final String CONTENT10_KEY = "ajaxContent10";
        public static final long CONTENT10_TXT_SIZE = 81;
        public static final String CONTENT10_TXT_FILE = "/brtmtestapp/jquery/ajaxContent10.jsp";

        public static final String CONTENT11_KEY = "ajaxContent11";
        public static final long CONTENT11_TXT_SIZE = 81;
        public static final String CONTENT11_TXT_FILE = "/brtmtestapp/jquery/ajaxContent11.jsp";

        public static final String CONTENT12_KEY = "ajaxContent12";
        public static final long CONTENT12_TXT_SIZE = 154085;
        public static final String CONTENT12_TXT_FILE = "/brtmtestapp/jquery/ajaxContent12.jpg";

        public static final String CONTENT13_KEY = "ajaxContent13";
        public static final long CONTENT13_TXT_SIZE = 349;
        public static final String CONTENT13_TXT_FILE = "/brtmtestapp/jquery/ajaxContent13.html";

        public static final String CONTENT13A_KEY = "ajaxContent13a";
        public static final long CONTENT13A_TXT_SIZE = 362;
        public static final String CONTENT13A_TXT_FILE = "/brtmtestapp/jquery/ajaxContent13a.html";

        public static final String CONTENT13B_KEY = "ajaxContent13b";
        public static final long CONTENT13B_TXT_SIZE = 362;
        public static final String CONTENT13B_TXT_FILE = "/brtmtestapp/jquery/ajaxContent13b.html";

        public static final String CONTENT14_KEY = "ajaxContent14";
        public static final long CONTENT14_TXT_SIZE = 349;
        public static final String CONTENT14_TXT_FILE = "/brtmtestapp/jquery/ajaxContent14.html";

        // Size is not needed for ajaxContent15.html as this resource results in
        // 404 - Not Found.. Using -1 for easy check
        public static final String CONTENT15_KEY = "ajaxContent15";
        public static final long CONTENT15_TXT_SIZE = -1;
        public static final String CONTENT15_TXT_FILE = "/brtmtestapp/jquery/ajaxContent15.html";

        public static final String CONTENT16_KEY = "ajaxContent16";
        public static final long CONTENT16_TXT_SIZE = 349;
        public static final String CONTENT16_TXT_FILE = "/brtmtestapp/jquery/ajaxContent16.html";

        public static final String SAMPLE_KEY = "sample";
        public static final long SAMPLE_TXT_SIZE = 10206000;
        public static final String SAMPLE_TXT_FILE = "/brtmtestapp/jquery/sample.txt";

        public static final String SAMPLE1_KEY = "sample1";
        public static final long SAMPLE1_TXT_SIZE = 10206000;
        public static final String SAMPLE1_TXT_FILE = "/brtmtestapp/jquery/sample1.txt";

        public static String getPagePathInFileSystemFormat(String urlPage) {
            return urlPage.replace("/", File.separator);
        }
    }

    private TestAppUrlConstants() {
        throw new AssertionError();
    }

}
