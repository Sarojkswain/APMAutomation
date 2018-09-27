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

package com.ca.apm.tests.utils;

/**
 * Metric Utilities class for BrowserAgent Automation
 *
 * @author Legacy BRTM automation code
 *         Updates - gupra04
 * 
 */

import com.ca.apm.tests.utils.constants.BusinessServiceConstants.Test_BS_BT;
import com.ca.apm.tests.utils.constants.MetricConstants;
import com.ca.apm.tests.utils.constants.MetricConstants.BrowserMetrics;
import com.ca.apm.tests.utils.constants.MetricConstants.MiscMetric;
import com.ca.apm.tests.utils.constants.AgentPropertyConstants.BrowseAgentProperties;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.MetricUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

public class MetricUtils {

    protected String browser;
    protected static CLWBean clwBean = null;
    protected static final Logger LOGGER = LoggerFactory.getLogger(MetricUtils.class);

    /**
     * This method is used to create an instance of CLWBean class.
     * 
     * @param em
     *        EmDetails
     */
    protected static CLWBean getClwBeanInstance(EmDetails em) {
        LOGGER.info("Start of getClwBeanInstance");
        LOGGER.info("emHostName::" + em.getEmHost() + "   EmUserName::" + em.getEmUsername()
            + " EmPassword::" + em.getEmPassword() + "  EmPort::" + em.getEmPort()
            + "    CLW jar file location" + em.getClwJar());
        if (clwBean == null) {
            clwBean =
                new CLWBean(em.getEmHost(), em.getEmUsername(), em.getEmPassword(), em.getEmPort(),
                    em.getClwJar());
        }
        LOGGER.info("End of getClwBeanInstance");
        return clwBean;
    }

    /**
     * This method is used to validate Sustainability metric has expected value
     * 
     * @param metricType
     *        Sustainability metric to check
     * @param metricValue
     *        Expected value for Sustainability metric
     * @param agent
     *        AgentDetails
     * @param em
     *        EmDetails
     */
    public static void verifyBRTMSustainabilityMetric(String metricType, String metricValue,
        AgentDetails agent, EmDetails em) {
        String metricpath;
        metricpath = createMetricPathSustainability(metricType, agent);

        String msg =
            "The Metric " + metricType + " does not match the expected value of " + metricValue;
        LOGGER.info("Getting metric values for: " + metricpath);
        String[] browserAgentMetric = getMetricValue(metricpath, em);
        boolean result = false;

        for (String value : browserAgentMetric) {
            // Workaround for CLW returning includelist & excludelist with extra " within [
            if (metricType.equals(BrowseAgentProperties.EXLUDE_URL_LIST)
                || metricType.equals(BrowseAgentProperties.INCLUDE_URL_LIST)) {
                value = value.replace("\"\"", "\"");
                value = value.replace("\"[", "[");
                value = value.replace("]\"", "]");
            }
            if (value.equals(metricValue.trim())) {
                result = true;
            }
        }
        CommonUtils.customAssertTrue(result, msg);
    }

    /**
     * Verify last sustainability metric value. clw will scan for past 1 minute.
     * 
     * @param metricType
     *        Sustainability metric to check
     * @param metricValue
     *        Expected value for Sustainability metric
     * @param agent
     *        AgentDetails
     * @param em
     *        EmDetails
     */
    public static void verifyLastBRTMSustainabilityMetric(String metricType, String metricValue,
        AgentDetails agent, EmDetails em) {
        String value = getBRTMSustainabilityMetric(metricType, agent, em);
        if (value == null) {
            Assert.fail("Sustainability metrics for " + metricType + " returns no results");
        }
        if (value.length() > 0
            && (metricType.equals(BrowseAgentProperties.EXLUDE_URL_LIST) || metricType
                .equals(BrowseAgentProperties.INCLUDE_URL_LIST))) {
            value = value.replace("\"\"", "\"");
            value = value.replace("\"[", "[");
            value = value.replace("]\"", "]");
        }
        String msg =
            "The Metric " + metricType + " does not match the expected value of " + metricValue;
        CommonUtils.customAssertTrue(value.equals(metricValue), msg);
    }

    public static String getBRTMSustainabilityMetric(String metricType, AgentDetails agent,
        EmDetails em) {
        String metricpath;
        metricpath = createMetricPathSustainability(metricType, agent);
        LOGGER.info("Getting metric values for: " + metricpath);
        String[] brtmMetric = getMetricValue(metricpath, em, 1);
        // getMetricValue returns an array with most recent values first
        return (brtmMetric.length > 0) ? brtmMetric[0] : null;
    }

    public static void verifyAllPageMetrics(String metricPath, EmDetails em) {

        LOGGER.info("Verifying all 10 Page Metrics");

        compareMetrics(metricPath, BrowserMetrics.AVERAGE_PREVIOUS_PAGE_UNLOAD_TIME, 0, 2, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_PAGE_LOAD_COMPLETE_TIME, 1, 2, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_BROWSER_RENDER_TIME, 1, 2, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_ROUND_TRIP_TIME, 0, 2, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_DOM_CONSTRUCTION_TIME, 1, 2, em);
        compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, 1, 2, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_CONNECTION_ESTABLISHMENT_TIME, 0, 2, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_DNS_LOOKUP_TIME, 0, 2, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_TIME_TO_FIRST_BYTE, 1, 2, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_TIME_TO_LAST_BYTE, 1, 2, em);

    }

    public static void verifyAllPageMetricsZero(String metricPath, EmDetails em) {
        LOGGER.info("Verifying all 10 Page Metrics are zero or do not exist");

        compareMetrics(metricPath, BrowserMetrics.AVERAGE_PREVIOUS_PAGE_UNLOAD_TIME, -1, 1, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_PAGE_LOAD_COMPLETE_TIME, -1, 1, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_BROWSER_RENDER_TIME, -1, 1, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_ROUND_TRIP_TIME, -1, 1, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_DOM_CONSTRUCTION_TIME, -1, 1, em);
        compareMetrics(metricPath, BrowserMetrics.RESPONSES_PER_INTERVAL, -1, 1, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_CONNECTION_ESTABLISHMENT_TIME, -1, 1, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_DNS_LOOKUP_TIME, -1, 1, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_TIME_TO_FIRST_BYTE, -1, 1, em);
        compareMetrics(metricPath, BrowserMetrics.AVERAGE_TIME_TO_LAST_BYTE, -1, 1, em);
    }

    /**
     * Verify if a metric exist using clw.
     * 
     * @param metricPath regex of metric
     * @param clwScanTime past period of time to check in minutes
     * @param leastCount expect at least this number of count. e.g. Ajax metric is 5, url metric is
     *        10.
     */
    public static void verifyMetricExists(String metricPath, int clwScanTime, int leastCount,
        EmDetails em) {
        CLWBean clw = getClwBeanInstance(em);
        MetricUtil mu = new MetricUtil(metricPath, clw);
        String[] vals = mu.getLastNMinutesMetricValues(clwScanTime);
        Assert.assertTrue(vals.length >= leastCount, "Metric count should be at least "
            + leastCount + ", but got " + vals.length);
    }

    private static String createMetricPathSustainability(String metricType, AgentDetails agent) {
        String metricPath =
            MetricConstants.SUPERDOMAIN + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentHost() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentProcessName() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentName() + MetricConstants.METRIC_NODE_DELIMETER + "Agent Stats"
                + MetricConstants.METRIC_NODE_DELIMETER + "Sustainability"
                + MetricConstants.METRIC_NODE_DELIMETER + "Browser Agent Business Transaction"
                + MetricConstants.METRIC_NAME_DELIMETER + metricType;
        return metricPath;
    }

    public static String createPageMetricPathNoBT(String pageUrl, AgentDetails agent) {
        String metricPath =
            MetricConstants.SUPERDOMAIN + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentHost() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentProcessName() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentName() + MetricConstants.METRIC_NODE_DELIMETER + "Business Segment"
                + MetricConstants.METRIC_NODE_DELIMETER + agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort()
                + MetricConstants.METRIC_NODE_DELIMETER + pageUrl
                + MetricConstants.METRIC_NAME_DELIMETER;
        return metricPath;
    }

    public static String createPageMetricPathWBT(AgentDetails agent, SeleniumDetails seleniumData) {
        String metricPath =
            MetricConstants.SUPERDOMAIN + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentHost() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentProcessName() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentName() + MetricConstants.METRIC_NODE_DELIMETER + "Business Segment"
                + MetricConstants.METRIC_NODE_DELIMETER + Test_BS_BT.TEST_BUSINESS_SERVICE
                + MetricConstants.METRIC_NODE_DELIMETER + Test_BS_BT.TEST_BUSINESS_TRANSACTION
                + " via " + seleniumData.getBrowser() + MetricConstants.METRIC_NODE_DELIMETER
                + Test_BS_BT.TEST_BUSINESS_TRANSACTION_COMPONENT
                + MetricConstants.METRIC_NODE_DELIMETER + "Browser"
                + MetricConstants.METRIC_NAME_DELIMETER;
        return metricPath;
    }

    public static String createAjaxMetricPathNoBT(String pageUrl, AgentDetails agent) {
        String metricPath =
            MetricConstants.SUPERDOMAIN + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentHost() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentProcessName() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentName() + MetricConstants.METRIC_NODE_DELIMETER + "Business Segment"
                + MetricConstants.METRIC_NODE_DELIMETER + agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort()
                + MetricConstants.METRIC_NODE_DELIMETER + pageUrl
                + MetricConstants.METRIC_NODE_DELIMETER + "AJAX Call"
                + MetricConstants.METRIC_NODE_DELIMETER;
        return metricPath;
    }

    private static String createAjaxMetricPathWBT(AgentDetails agent, SeleniumDetails seleniumData) {
        String metricPath =
            MetricConstants.SUPERDOMAIN + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentHost() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentProcessName() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentName() + MetricConstants.METRIC_NODE_DELIMETER + "Business Segment"
                + MetricConstants.METRIC_NODE_DELIMETER + Test_BS_BT.TEST_BUSINESS_SERVICE
                + MetricConstants.METRIC_NODE_DELIMETER + Test_BS_BT.TEST_BUSINESS_TRANSACTION
                + " via " + seleniumData.getBrowser() + MetricConstants.METRIC_NODE_DELIMETER
                + Test_BS_BT.TEST_BUSINESS_TRANSACTION_COMPONENT
                + MetricConstants.METRIC_NODE_DELIMETER + "Browser"
                + MetricConstants.METRIC_NODE_DELIMETER + "AJAX Call"
                + MetricConstants.METRIC_NODE_DELIMETER;
        return metricPath;
    }

    private static String createJavascriptMetricPathNoBT(String pageUrl, AgentDetails agent) {
        String metricPath =
            MetricConstants.SUPERDOMAIN + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentHost() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentProcessName() + MetricConstants.METRIC_NODE_DELIMETER
                + agent.getAgentName() + MetricConstants.METRIC_NODE_DELIMETER + "Business Segment"
                + MetricConstants.METRIC_NODE_DELIMETER + agent.getAgentHost()
                + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort()
                + MetricConstants.METRIC_NODE_DELIMETER + pageUrl
                + MetricConstants.METRIC_NODE_DELIMETER + "JavaScript Function";
        return metricPath;
    }

    /**
     * Method to Get, Compare and Assert the AJAX metric values.
     * 
     * @param metricPath
     *        - Metric path up to the ":" of the target metric
     * @param metricType
     *        - One of the 5 AJAX metrics you are comparing. Should be a property.
     * @param value
     *        - The int value you expect the metric to be. If -1 it tries to match 0 or if it does
     *        not exist. If 0 it looks for >= 0. If 1 it tries to match >=1. If > 1 it will try to
     *        match exact value.
     *        Most tests will likely check for non-zero positive values since we cannot control the
     *        AJAX values well enough to predict most of them.
     * @param clwScanTime
     *        clw scan time in minutes
     * @param value em
     *        EmDetails
     */
    // Same Utility method is used for tests with BT & NoBTs
    public static void compareMetrics(String metricPath, String metricType, int valueMatch,
        int clwScanTime, EmDetails em) {

        metricPath = metricPath + metricType;
        String msg;
        LOGGER.info("Getting metric values for: " + metricPath);
        String[] browserAgentMetric = getMetricValue(metricPath, em, clwScanTime);
        boolean result = false;
        if (valueMatch == -1) {
            result = true;
            msg = "The Metric " + metricType + " is not 0 as expected";
            for (String value : browserAgentMetric) {
                if (Integer.parseInt(value) != 0) {
                    result = false;
                }
            }
        } else if (valueMatch == 0) {
            msg = "The Metric " + metricType + " is not 0 or higher as expected";
            for (String value : browserAgentMetric) {
                if (Integer.parseInt(value) >= 0) {
                    result = true;
                }
            }
        } else if (valueMatch == 1) {
            msg = "The Metric " + metricType + " is not greater than or equal to 1 as expected";
            for (String value : browserAgentMetric) {
                if (Integer.parseInt(value) >= 1) {
                    result = true;
                }
            }
        } else {
            msg = "The Metric " + metricType + " is not " + valueMatch + " as expected";
            for (String value : browserAgentMetric) {
                if (Integer.parseInt(value) == valueMatch) {
                    result = true;
                }
            }
        }
        CommonUtils.customAssertTrue(result, msg);
    }

    /**
     * Method to Get, Compare and Assert the AJAX metric values - when no BT's are used.
     * 
     * @param pageURL - page url of the ajax request
     * @param ajaxHostAndPort - hostname and port of the ajax request host, e.g. "localhost/8086"
     * @param ajaxPath - path for the ajax call
     * @param metricType - One of the 5 AJAX metrics you are comparing. Should be a property.
     * @param valueMatch - The int value you expect the metric to be. If -1 it tries to match 0 or
     *        if it
     *        does not exist. If 0 it looks for >= 0. If > 1 it will try to match exact value.
     *        Most tests will likely check for non-zero positive values since we cannot control the
     *        AJAX values well enough to predict most of them.
     * @param clwScanTime - clw scan time in minutes
     * @param value agent
     *        AgentDetails
     * @param value em
     *        EmDetails
     */
    public static void compareAJAXMetricsNoBT(String pageUrl, String ajaxHostAndPort,
        String ajaxPath, String metricType, int valueMatch, int clwScanTime, AgentDetails agent,
        EmDetails em) {
        String metricpath;
        metricpath =
            createAjaxMetricPathNoBT(pageUrl, agent) + ajaxHostAndPort
                + MetricConstants.METRIC_NODE_DELIMETER + ajaxPath
                + MetricConstants.METRIC_NAME_DELIMETER + metricType;
        LOGGER.info("Compare Ajax Metrics NoBT : Metric path : " + metricpath);
        String msg;
        String[] browserAgentMetric = getMetricValue(metricpath, em, clwScanTime);
        boolean result = false;
        if (valueMatch == -1) {
            result = true;
            msg = "The Metric " + metricType + " is not 0 as expected";
            for (String value : browserAgentMetric) {
                if (Integer.parseInt(value) != 0) {
                    result = false;
                }
            }
        } else if (valueMatch == 0) {
            msg = "The Metric " + metricType + " is not 0 or higher as expected";
            for (String value : browserAgentMetric) {
                if (Integer.parseInt(value) >= 0) {
                    result = true;
                }
            }
        } else if (valueMatch == 1) {
            msg = "The Metric " + metricType + " is not greater than or equal to 1 as expected";
            for (String value : browserAgentMetric) {
                if (Integer.parseInt(value) >= 1) {
                    result = true;
                }
            }
        } else {
            msg = "The Metric " + metricType + " is not " + valueMatch + " as expected";
            for (String value : browserAgentMetric) {
                if (Integer.parseInt(value) == valueMatch) {
                    result = true;
                }
            }
        }
        CommonUtils.customAssertTrue(result, msg);
    }


    /**
     * Method to Get, Compare and Assert the AJAX metric values.
     * 
     * @param AJAXURL
     *        - URL of the AJAX call with the port after it
     * @param AJAXPATH
     *        - the path of the AJAX call
     * @param metricType
     *        - One of the 5 AJAX metrics you are comparing. Should be a property.
     * @param value
     *        - The int value you expect the metric to be. If -1 it tries to match 0 or if it does
     *        not exist. If 0 it looks for >= 0. If > 1 it will try to match exact value.
     *        Most tests will likely check for non-zero positive values since we cannot control the
     *        AJAX values well enough to predict most of them.
     * @param agent
     *        - AgentDetails
     * @param em
     *        - EmDetails
     * @param seleniumData
     *        - SeleniumDetails
     */
    public static void compareAJAXMetricsWBT(String ajaxUrl, String ajaxPath, String metricType,
        int valueMatch, int clwScanTime, AgentDetails agent, EmDetails em,
        SeleniumDetails seleniumData) {
        String metricpath;

        metricpath =
            createAjaxMetricPathWBT(agent, seleniumData) + ajaxUrl
                + MetricConstants.METRIC_NODE_DELIMETER + ajaxPath
                + MetricConstants.METRIC_NAME_DELIMETER + metricType;
        LOGGER
            .info("compareAJAXMetricsWBT - Getting metric values for Metric path : " + metricpath);
        String msg;
        String[] brtmMetric = getMetricValue(metricpath, em, clwScanTime);
        boolean result = false;
        if (valueMatch == -1) {
            result = true;
            msg = "The Metric " + metricType + " is not 0 as expected";
            for (String value : brtmMetric) {
                if (Integer.parseInt(value) != 0) {
                    result = false;
                }
            }
        } else if (valueMatch == 0) {
            msg = "The Metric " + metricType + " is not 0 or higher as expected";
            for (String value : brtmMetric) {
                if (Integer.parseInt(value) >= 0) {
                    result = true;
                }
            }
        } else if (valueMatch == 1) {
            msg = "The Metric " + metricType + " is not greater than or equal to 1 as expected";
            for (String value : brtmMetric) {
                if (Integer.parseInt(value) >= 1) {
                    result = true;
                }
            }
        } else {
            msg = "The Metric " + metricType + " is not " + valueMatch + " as expected";
            for (String value : brtmMetric) {
                if (Integer.parseInt(value) == valueMatch) {
                    result = true;
                }
            }
        }
        CommonUtils.customAssertTrue(result, msg);
    }


    /**
     * Method to Get, Compare and Assert the Javascript metric values.
     * 
     * @param pageUrl
     *        - URL of the page
     * @param JSPath
     *        - JS metric path
     * @param metricType
     *        - One of the 2 standard javascript metrics you are comparing or a custom metric.
     *        Should be a property.
     * @param valueMatch
     *        - The int value you expect the metric to be. If -1 it tries to match 0. If 1 it tries
     *        to match >=1. If 1 it tries to match >=1. If > 1 it will try to match exact value.
     *        Most tests will likely check for non-zero positive values since we cannot control the
     *        AJAX values well enough to predict most of them.
     * @param clwScanTime Query time
     * @param agent
     *        - AgentDetails
     * @param em
     *        - EmDetails
     */
    public static void compareJSMetricsNoBT(String pageUrl, String JSPath, String metricType,
        int valueMatch, int clwScanTime, AgentDetails agent, EmDetails em) {
        String metricpath;

        metricpath =
            createJavascriptMetricPathNoBT(pageUrl, agent) + MetricConstants.METRIC_NODE_DELIMETER
                + JSPath + ":" + metricType;
        LOGGER.info("IN COMPAREJS METRICS -- DATA RECIEVED : PAGEURL " + pageUrl + " JSpath: "
            + JSPath + " METRIC TYPE: " + metricType + " VALUE MATCH : " + valueMatch);
        LOGGER.info("IN COMPARE JS METRICS -- METRIS PATH IS : " + metricpath);

        LOGGER.info("Compare JS Metrics NoBT : Metric path : " + metricpath);
        String msg;
        LOGGER.info("Getting metric values for: " + metricpath);
        String[] brtmMetric = getMetricValue(metricpath, em, clwScanTime);
        boolean result = false;
        if (valueMatch == -1) {
            result = true;
            msg = "The Metric " + metricType + " is not 0 as expected";
            for (String value : brtmMetric) {
                if (Integer.parseInt(value) != 0) {
                    result = false;
                }
            }
        }
        if (valueMatch == 0) {
            msg = "The Metric " + metricType + " is not greater than or equal to 0 as expected";
            for (String value : brtmMetric) {
                if (Integer.parseInt(value) >= 0) {
                    result = true;
                }
            }
        } else if (valueMatch == 1) {
            msg = "The Metric " + metricType + " is not greater than or equal to 1 as expected";
            for (String value : brtmMetric) {
                if (Integer.parseInt(value) >= 1) {
                    result = true;
                }
            }
        } else {
            msg = "The Metric " + metricType + " is not " + valueMatch + " as expected";
            for (String value : brtmMetric) {
                if (Integer.parseInt(value) == valueMatch) {
                    result = true;
                }
            }
        }
        CommonUtils.customAssertTrue(result, msg);
    }


    // Use this method to validate when metric count may possibly be spread over multiple intervals
    public static void checkJSCountMetricsNoBT(String pageUrl, String JSPath,
        String metricType, int metricCount, int clwScanTime, AgentDetails agent, EmDetails em) {
        String metricPath;

        metricPath =
            createJavascriptMetricPathNoBT(pageUrl, agent) + MetricConstants.METRIC_NODE_DELIMETER
                + JSPath + ":" + metricType;
        LOGGER
            .info("checkJSCountMetricsNoBT: check JS count Metrics NoBT - Getting metric values for Metric path : "
                + metricPath);
        int invocationCountSum = 0;
        boolean testPassStatus = true;
        String msg = metricType + " is not " + metricCount + " as expected. It is: ";

        String[] browserAgentMetric = MetricUtils.getMetricValue(metricPath, em, clwScanTime);
        for (String value : browserAgentMetric) {
            invocationCountSum += Integer.parseInt(value);
        }
        // Validate metric count is as expected
        if (invocationCountSum != metricCount) {
            testPassStatus = false;
        }
        CommonUtils.customAssertTrue(testPassStatus, msg + invocationCountSum);
    }

    public static void checkMISCMetricsNoBT(String metricPath, int MiscMetricCount,
        int clwScanTime, EmDetails em) {

        metricPath = metricPath.substring(0, metricPath.indexOf(':'));
        LOGGER.info("checkMISCMetricTest: Getting metric values for Metric path : " + metricPath
            + MiscMetric.MISC_METRIC_PATH + MetricConstants.METRIC_NAME_DELIMETER
            + MiscMetric.MISC_METRIC);

        int sumOfMISCMetrics = 0;
        boolean testPassStatus = true;
        String msg =
            MiscMetric.MISC_METRIC + " is not " + MiscMetricCount + " as expected. It is: ";
        String[] browserAgentMetric =
            MetricUtils.getMetricValue(metricPath + MiscMetric.MISC_METRIC_PATH
                + MetricConstants.METRIC_NAME_DELIMETER + MiscMetric.MISC_METRIC, em, clwScanTime);
        for (String value : browserAgentMetric) {
            sumOfMISCMetrics += Integer.parseInt(value);
        }
        // Validate MISC metric count is as expected
        if (sumOfMISCMetrics != MiscMetricCount) {
            testPassStatus = false;
        }
        CommonUtils.customAssertTrue(testPassStatus, msg + sumOfMISCMetrics);
    }

    /**
     * Method to get the metric values using CLW
     * 
     * @param metric
     *        - Complete Path to the metric
     *        - Example:*SuperDomain*|autoem01|Tomcat|Tomcat Agent|Browser|URL Group|Default:Average
     *        Browser Render Time (ms)
     * @param emHost - EM Host name
     * 
     * @return
     *         - Returns the String Array with all metric values with 5 minute data
     */
    private static String[] getMetricValue(String metric, EmDetails em) {
        return getMetricValue(metric, em, MetricConstants.BA_METRIC_VERIFICATION_TIME);
    }

    /**
     * Method to get the metric values using CLW
     * 
     * @param metric
     *        - Complete Path to the metric
     *        - Example:*SuperDomain*|autoem01|Tomcat|Tomcat Agent|Browser|URL Group|Default:Average
     *        Browser Render Time (ms)
     * @param emHost - EM Host name
     * @param minutes - No. of minutes for which EM metrics are to be retrieved. Default =
     *        BRTMMetricVerificationTime
     * 
     * @return
     *         - Returns the String Array with all metric values with 5 minute data
     */
    private static String[] getMetricValue(String metric, EmDetails em, int nMinutes) {
        LOGGER.info("Start of getMetricValue");
        StopWatch sw = new StopWatch();
        sw.start();
        CLWBean clw = getClwBeanInstance(em);
        MetricUtil mu = new MetricUtil(metric, clw);
        String vals[] = mu.getLastNMinutesMetricValues(nMinutes);
        for (int i = 0; i < vals.length; i++) {
            LOGGER.info("values:: " + vals[i]);
        }
        LOGGER.info("End of getMetricValue");
        sw.stop();
        LOGGER.info("Time taken to get metric: " + sw.getElapsedTimeSecs() + " seconds");
        CommonUtils.counter(sw.getElapsedTimeSecs(), "metrictime");
        return vals;
    }

}
