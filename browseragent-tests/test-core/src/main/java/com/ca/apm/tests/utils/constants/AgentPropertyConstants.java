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
 * Browser Agent automation - Agent property names
 *
 * @author gupra04
 */

package com.ca.apm.tests.utils.constants;

public class AgentPropertyConstants {

    // TODO: Add these properties as we needed for tests
    /*
     * excludePatterns=introscope.agent.brtm.excludePatterns
     * 
     * introscope.agent.browseragent.trace.starttime.adjustment.enabled
     * introscope.agent.browseragent.jarManifestAttributeName
     * introscope.agent.browseragent.jarManifestAttributeValue
     * 
     * introscope.agent.browseragent.wilyURL
     * introscope.agent.browseragent.jserror.enabled
     * introscope.agent.browseragent.jserror.includeErrorList
     * introscope.agent.browseragent.jserror.excludeErrorList
     * 
     * introscope.agent.browseragent.errorMessages.max
     * introscope.agent.browseragent.errorMessages.resetTime.inSeconds
     * 
     * introscope.agent.browseragent.checkHeadFilterPresence.enabled
     * introscope.agent.browseragent.checkTailFilterPresence.enabled
     * introscope.agent.browseragent.searchingmethodhierarchy.max.depth
     */
    public static final class BrowseAgentProperties {

        public static final String BROWSER_AGENT_ENABLED = "introscope.agent.browseragent.enabled";
        public static final String URL_METRIC_OFF = "introscope.agent.browseragent.urlMetricOff";
        public static final String METRIC_FREQUENCY =
            "introscope.agent.browseragent.metricFrequency";
        public static final String JS_EXTENSION_LOCATION =
            "introscope.agent.browseragent.jsExtensionLocation";
        public static final String BROWSER_LOGGING_ENABLED =
            "introscope.agent.browseragent.browserLoggingEnabled";
        public static final String INSTRUMENT_CLASS_AUTOSKIP_DEPTH =
            "introscope.agent.browseragent.instrumentclass.autoskip.depth";
        public static final String CONTENT_ENCODING_ENABLED =
            "introscope.agent.browseragent.contentencoding.enabled";

        public static final String THROTTLE_RESPONSE_LIMIT =
            "introscope.agent.browseragent.throttleResponseLimit";

        public static final String SNIPPET_INSERTION_ENABLED =
            "introscope.agent.browseragent.snippetInsertionEnabled";
        public static final String SNIPPET_LOCATION =
            "introscope.agent.browseragent.snippetLocation";
        public static final String SNIPPET_MAX_SEARCHING_LENGTH =
            "introscope.agent.browseragent.snippet.maxSearchingLength";

        public static final String BROWSER_AGENT_SUSTAINABILITY_ENABLED =
            "introscope.agent.browseragent.sustainabilityMetrics.enabled";

        public static final String JS_FUNCTION_METRICS_ENABLED =
            "introscope.agent.browseragent.jsFunctionMetricsEnabled";
        public static final String JS_FUNCTION_METRICS_THRESHOLD =
            "introscope.agent.browseragent.jsFunctionMetricsThreshold";

        public static final String PAGE_LOAD_METRICS_ENABLED =
            "introscope.agent.browseragent.pageLoadMetricsEnabled";
        public static final String PAGE_LOAD_METRICS_THRESHOLD =
            "introscope.agent.browseragent.pageLoadMetricsThreshold";

        public static final String INCLUDE_URL_LIST =
            "introscope.agent.browseragent.includeURLList";
        public static final String EXLUDE_URL_LIST = "introscope.agent.browseragent.excludeURLList";

        public static final String AJAX_METRICS_ENABLED =
            "introscope.agent.browseragent.ajaxMetricsEnabled";
        public static final String AJAX_METRICS_THRESHOLD =
            "introscope.agent.browseragent.ajaxMetricsThreshold";

        public static final String RESOURCE_LIMIT = "introscope.agent.browseragent.resourceLimit";
        public static final String RESOURCE_COUNT = "introscope.agent.browseragent.resourceCount";

        public static final String GEO_LOCATION_ENABLED =
            "introscope.agent.browseragent.geolocation.enabled";
        public static final String GEO_LOCATION_MAXIMUM_AGE =
            "introscope.agent.browseragent.geolocation.maximumAge";
        public static final String GEO_LOCATION_TIMEOUT =
            "introscope.agent.browseragent.geolocation.timeout";
        public static final String GEO_LOCATION_HIGH_ACCURACY_ENABLED =
            "introscope.agent.browseragent.geolocation.highAccuracyEnabled";

        public static final String AGENT_LOGGING_LEVEL = "log4j.logger.IntroscopeAgent";
        public static final String BROWSER_AGENT_LOGGING_LEVEL =
            "log4j.logger.IntroscopeAgent.BrowserAgent";

    }

    public static final class AgentDefaults {
        // Prefix agent home to these values
        public static final String DEFAULT_LOG_FILE = "\\logs\\IntroscopeAgent.log";
        public static final String DEFAULT_EXTERNAL_JS_EXTENSION_FILE =
            "\\examples\\APM\\BrowserAgent\\js\\apmbrowseragentextensibility.js";
        public static final String BAD_EXTERNAL_JS_EXTENSION_FILE =
            "\\examples\\APM\\BrowserAgent\\apmbrowseragentextensibility.js";
        public static final String EMPTY_EXTERNAL_JS_EXTENSION_FILE =
            "\\examples\\APM\\BrowserAgent\\js\\apmbrowseragentextensibilityEmpty.js";
        public static final String DEFAULT_BROWSER_AGENT_JAR = "\\core\\ext\\BrowserAgentExt.jar";
        public static final String TRANSACTION_TRACE_FILE = "BrowserAgentTransactionTraces.xml";
    }

    private AgentPropertyConstants() {
        throw new AssertionError();
    }

}
