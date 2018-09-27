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
 * Constants used for JS Extension tests
 * Browser Agent automation
 *
 * @author gupra04
 */

package com.ca.apm.tests.utils.constants;

public class JSExtensionConstants {

    public static final class JSExtensionTestScript {

        public static final String CUSTOM_PAGE_SCRIPT =
            "BrowserAgentExtension.addCustomPageMetric(\"Custom Page Metric Average\", \"ms\", 0, 5);"
                + "\n BrowserAgentExtension.addCustomPageMetric(\"Custom Page Metric Count\", null, 1, 1);";

        public static final String CUSTOM_PAGE_FUNCTION = "extAddCustomPageMetric : function ()";

        public static final String CUSTOM_AJAX_SCRIPT =
            "BrowserAgentExtension.addCustomAjaxMetric(\"Custom Ajax Metric Average\", \"ms\", 0, 5);"
                + "\n BrowserAgentExtension.addCustomAjaxMetric(\"Custom Ajax Metric Count\", null, 1, 1);";

        public static final String CUSTOM_AJAX_FUNCTION = "extAddCustomAjaxMetric : function ()";

        public static final String CUSTOM_OPTIONAL_PROPERTIES_SCRIPT =
            "BrowserAgentExtension.addCustomOptionalProperty(\"testProp1\", \"testValue1\");"
                + "\n BrowserAgentExtension.addCustomOptionalProperty(\"testProp2\", \"testValue2\");";

        public static final String CUSTOM_OPTIONAL_PROPERTIES_FUNCTION =
            "extAddCustomOptionalProperty : function ()";

        public static final String CUSTOM_JS_FUNCTION_SCRIPT =
            "BrowserAgentExtension.addJSFuncToInstrument(\"Math_Random\", \"Math.random\");"
                + "\n BrowserAgentExtension.addJSFuncToInstrument(\"Send_Request\", \"sendRequest\");";

        public static final String CUSTOM_JS_FUNCTION = "extAddJSFuncToInstrument : function ()";

        public static final String CUSTOM_JS_METRIC_SCRIPT =
            "BrowserAgentExtension.addCustomJSFuncMetric(\"Custom JS Func Metric Average\", \"ms\", 0, 5);"
                + "\n BrowserAgentExtension.addCustomJSFuncMetric(\"Custom JS Func Metric Count\", null, 1, 1);";

        public static final String CUSTOM_JS_METRIC_FUNCTION =
            "extAddCustomJSFuncMetric : function ()";

        public static final String CUSTOM_MISC_METRIC_SCRIPT_1 =
            "var el = document.getElementById(\"GetLocalFile\");"
                + "\n var key = \"Button Click Count Per Interval\"; "
                + "\n BrowserAgentGlobals.metricTypeToAccumulatorMap[BrowserAgentGlobals.metricType.MISC][key] = {count : 0};"
                + "\n el.addEventListener('click', function()"
                + "\n { BrowserAgentGlobals.metricTypeToAccumulatorMap[BrowserAgentGlobals.metricType.MISC][key].count += 1; "
                + "\n BrowserAgentUtils.metricUtils.harvestMetrics(BrowserAgentGlobals.metricType.MISC); });";

        public static final String CUSTOM_MISC_METRIC_FUNCTION_1 =
            "extCollectMiscMetrics : function ()";

        public static final String CUSTOM_MISC_METRIC_SCRIPT_2 =
            "var accumulator = BrowserAgentGlobals.metricTypeToAccumulatorMap[BrowserAgentGlobals.metricType.MISC];"
                + "\n var metricPath = BrowserAgentGlobals.pageMetricPath + BrowserAgentGlobals.pipeChar + \"MISC\";"
                + "\n var metricList = [];"
                + "\n for (var key in accumulator) {"
                + "\n if (accumulator[key].count > 0) {"
                + "\n BrowserAgentUtils.jsonUtils.addToList("
                + "\n BrowserAgentUtils.jsonUtils.createMetric(metricPath, key, null, 1, accumulator[key].count, metricList), metricList);"
                + "\n accumulator[key].count = 0;"
                + "\n } }"
                + "\n var businessService = BrowserAgentUtils.jsonUtils.createBS(BrowserAgentGlobals.bs, BrowserAgentGlobals.bt, BrowserAgentGlobals.btc);"
                + "\n BrowserAgentExtension.addExtensionJSONObject(metricList, null, businessService);";

        public static final String CUSTOM_MISC_METRIC_FUNCTION_2 =
            "extHarvestCustomMiscMetric : function ()";

        public static final String NAME_FORMATTER_SCRIPT =
            "var path = path.replace(/\\d+\\/sample.txt/g, 'allsample.txt');"
                + "\n return BrowserAgentExtension.createCustomMetric(name, unit, type, value, path);";

        public static final String NAME_FORMATTER_FUNCTION =
            "extNameFormatter : function ( path, name, unit, type, value )";

    }

    public static final class JSExtensionBackUpFile {
        public static final String ORIGINAL = ".orig";
        public static final String BACKUP = ".backup";
        public static final String EMPTY = ".empty";
        public static final String MISC_METRICS = ".MiscMetrics";
        public static final String BATCH_MODE_METRICS = ".BatchModeMetrics";
        public static final String CUSTOM_JS_FUNC_METRICS = ".JSCustomJSFunctionAndMetrics";
        public static final String NAME_FORMATTER = ".JSForNameFormatter";
        public static final String CUSTOM_PAGE_AJAX_OPTIONAL_PROPERTIES =
            ".JSForCustomPageAjaxOptionalProperties";
    }

    public static final class TransactionTraceBackUpFile {
        public static final String NAME_FORMATTER = ".traceForNameFormatter";
        public static final String PAGE_AJAX_OPTIONAL_PROPERTIES =
            ".tracePageAjaxOptionalProperties";
        public static final String PAGE_AJAX_OPTIONAL_PROPERTIES_DISABLED =
            ".tracePageAjaxOptionalPropertiesDisabled";
    }

    public static final class TransactionTraceOptionalProperties {
        public static final String TEST_PROPERTY_1 = "testProp1";
        public static final String TEST_PROPERTY_2 = "testProp2";
        public static final String TEST_VALUE_1 = "testValue1";
        public static final String TEST_VALUE_2 = "testValue2";
    }

    public static final class LogMessages {
        public static final String INFO = "[INFO]";
        public static final String WARN = "[WARN]";
        public static final String GET_JS_EXT_FILE_MSG = "Get JavaScript extension file path";
        public static final String USING_BUILTIN_JS_EXT_FILE_MSG =
            "Using built-in JavaScript extension file";
        public static final String VALID_JS_EXT_FILE_MSG = "Using JavaScript extension file";
        public static final String INVALID_JS_EXT_FILE_MSG =
            "apmbrowseragentextensibility.js is empty or not readable. Will use built-in extension file.";
        public static final String EMPTY_JS_EXT_FILE_MSG =
            "apmbrowseragentextensibilityEmpty.js is empty or not readable. Will use built-in extension file.";
    }

    private JSExtensionConstants() {
        throw new AssertionError();
    }

}
