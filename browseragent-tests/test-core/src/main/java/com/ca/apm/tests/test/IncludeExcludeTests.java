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
 * Tests for IncludeExclude URL list feature - BrowserAgent
 *
 * @author Legacy BRTM automation code
 *         Updates for TAS - gupra04
 * 
 */

package com.ca.apm.tests.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.BrowserAgentTomcatChromeWinTestbed;
import com.ca.apm.tests.testbed.BrowserAgentTomcatFirefoxWinTestbed;
import com.ca.apm.tests.testbed.BrowserAgentTomcatIEWinTestbed;
import com.ca.apm.tests.utils.constants.MetricConstants;
import com.ca.apm.tests.utils.constants.MetricConstants.*;
import com.ca.apm.tests.utils.constants.AgentPropertyConstants.BrowseAgentProperties;
import com.ca.apm.tests.utils.constants.BusinessServiceConstants.Test_BS_BT;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;
import com.ca.apm.tests.utils.CommonUtils;
import com.ca.apm.tests.utils.MetricUtils;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;

import org.openqa.selenium.By;

@Tas(snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE, testBeds = {
        @TestBed(name = BrowserAgentTomcatChromeWinTestbed.class, executeOn = BrowserAgentTomcatChromeWinTestbed.BROWSERAGENT_MACHINE_ID),
        @TestBed(name = BrowserAgentTomcatFirefoxWinTestbed.class, executeOn = BrowserAgentTomcatFirefoxWinTestbed.BROWSERAGENT_MACHINE_ID),
        @TestBed(name = BrowserAgentTomcatIEWinTestbed.class, executeOn = BrowserAgentTomcatIEWinTestbed.BROWSERAGENT_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "gupra04")
@Test(description = "Tests for Include / Exclude URL List properties and Sustainability Metrics")
public class IncludeExcludeTests extends BrowserAgentBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncludeExcludeTests.class);

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 451475 453447; Verify the ExcludeList property and Sustainability Metric")
    public void excludeURLListProperty1() {

        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        // Step -1 - Pre-requisites
        includeExcludeURLListPreReq();

        String excludeURLListProp, metricPath;

        LOGGER.info("excludeURLListProperty1 -- Starting Step 2");
        excludeURLListProp = "[\".*/brtmtestapp/GETLocalDomain2.jsp\"]";

        LOGGER.info("excludeURLListProperty1 -- Updating excludeURLList property (hot): "
            + excludeURLListProp);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.EXLUDE_URL_LIST,
            excludeURLListProp, agent);

        // For Debugging
        // prop = getIntroscopeAgentProfileProperty(excludeList);
        // LOGGER.info("\nexcludeURLListProperty1 -- excludeList value is ******** : " + prop
        // +"\n");
        // Wait for 60 sec for property value to be applied
        CommonUtils.sleep(60000);

        CommonUtils.resetBrowser(seleniumData);

        // Launch page - GetLocalDomain2
        LOGGER.info("\nexcludeURLListProperty1 -- accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE + " using webdriver");

        CommonUtils.launchSinglePageWithRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE,
            seleniumData);

        MetricUtils.verifyBRTMSustainabilityMetric(BrowseAgentProperties.EXLUDE_URL_LIST,
            excludeURLListProp, agent, em);

        metricPath =
            MetricUtils.createPageMetricPathNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, agent);

        LOGGER.info("\nexcludeURLListProperty1 -- metric path for GetLocalDomain2 is ******** : "
            + metricPath + "\n");

        // Validate Page and Ajax metrics are not reported for GetLocalDomain2.jsp
        MetricUtils.verifyAllPageMetricsZero(metricPath, em);
        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

        // Launch page - GetCORS2
        LOGGER.info("\nexcludeURLListProperty1 -- accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_CORS_PAGE + " using webdriver");

        CommonUtils.launchSinglePageWithRefresh(seleniumUrl + BrtmTestApp.GET_CORS_PAGE,
            seleniumData);

        metricPath =
            MetricUtils.createPageMetricPathNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.GET_CORS_PAGE, agent);

        LOGGER.info("\nexcludeURLListProperty1 -- metric path for getCORSPage is ******** : "
            + metricPath + "\n");

        // Validate Page and Ajax metrics are reported for GetCORS2.jsp
        MetricUtils.verifyAllPageMetrics(metricPath, em);
        MetricUtils.compareAJAXMetricsNoBT(
            NoBTMetricPath.BRTM_TEST_APP + BrtmTestApp.GET_CORS_PAGE,
            AJAXMetricPath.GET_CORS_AJAX_HOST_PORT, AJAXMetricPath.GET_CORS_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, 5, 2, agent, em);

        LOGGER.info("excludeURLListProperty1 -- Ended Step 2");
        LOGGER.info("excludeURLListProperty1 -- Test Ended");
    }

    @Test(groups = {"Websphere", "Weblogic", "Tomcat", "BAT"}, description = "Testcase ID: 453445 453448; Verify the IncludeList property and Sustainability Metric")
    public void includeURLListProperty1() {

        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        // Step -1 - Pre-requisites
        includeExcludeURLListPreReq();

        String includeURLListProp, prop, metricPath;

        LOGGER.info("includeURLListProperty1 -- Starting Step 2");
        includeURLListProp = "[\".*/brtmtestapp/GETCORS2.jsp\"]";

        LOGGER.info("includeURLListProperty1 -- Updating includeURLList property (hot) : "
            + includeURLListProp);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.INCLUDE_URL_LIST,
            includeURLListProp, agent);

        // For Debugging
        // prop = getIntroscopeAgentProfileProperty(includeList);
        // LOGGER.info("\nincludeURLListProperty1 -- includeList value is ******** : " + prop
        // +"\n");
        // Wait for 60 sec for property value to be applied
        CommonUtils.sleep(60000);

        CommonUtils.resetBrowser(seleniumData);

        // Launch page - GetLocalDomain2
        LOGGER.info("\nincludeURLListProperty1 -- accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE + " using webdriver");
        CommonUtils.launchSinglePageWithRefresh(seleniumUrl + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE,
            seleniumData);

        // Check value of Sustainability metric
        MetricUtils.verifyBRTMSustainabilityMetric(BrowseAgentProperties.INCLUDE_URL_LIST,
            includeURLListProp, agent, em);

        metricPath =
            MetricUtils.createPageMetricPathNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, agent);

        LOGGER.info("\nincludeURLListProperty1 -- metric path for GetLocalDomain2 is ******** : "
            + metricPath + "\n");

        // Validate Page and Ajax metrics are not reported for GetLocalDomain2.jsp
        MetricUtils.verifyAllPageMetricsZero(metricPath, em);

        MetricUtils.compareAJAXMetricsNoBT(NoBTMetricPath.BRTM_TEST_APP
            + BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE, "" + agent.getAgentHost()
            + MetricConstants.METRIC_BROWSER_PAGE_NAME_SEP + agent.getApplicationServerPort(),
            AJAXMetricPath.GET_LOCAL_DOMAIN_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

        // Launch page - GetCORS2
        LOGGER.info("\nincludeURLListProperty1 -- accessing URL:" + seleniumUrl
            + BrtmTestApp.GET_CORS_PAGE + " using webdriver");
        CommonUtils.launchSinglePageWithRefresh(seleniumUrl + BrtmTestApp.GET_CORS_PAGE,
            seleniumData);

        metricPath =
            MetricUtils.createPageMetricPathNoBT(NoBTMetricPath.BRTM_TEST_APP
                + BrtmTestApp.GET_CORS_PAGE, agent);

        LOGGER.info("\nincludeURLListProperty1 -- metric path for getCORSPage is ******** : "
            + metricPath + "\n");

        // Validate Page metris are reported and Ajax metrics are not reported for GetCORS2.jsp
        MetricUtils.verifyAllPageMetrics(metricPath, em);

        MetricUtils.compareAJAXMetricsNoBT(
            NoBTMetricPath.BRTM_TEST_APP + BrtmTestApp.GET_CORS_PAGE,
            AJAXMetricPath.GET_CORS_AJAX_HOST_PORT, AJAXMetricPath.GET_CORS_AJAX_PATH,
            AJAXMetrics.AJAX_INVOCATION_COUNT_PER_INTERVAL, -1, 2, agent, em);

        LOGGER.info("includeURLListProperty1 -- Ended Step 2");
        LOGGER.info("includeURLListProperty1 -- Test Ended");
    }

    private void includeExcludeURLListPreReq() {
        LOGGER.info("\nExecuting method: " + CommonUtils.getCurrentMethodName() + "\n");

        // Step -1 - Pre-requisites -
        // update agent profile for Ajax Metrics
        LOGGER.info("includeExcludeURLListProperty -- Starting Step - 1 - Pre-req");
        LOGGER
            .info("includeExcludeURLListProperty -- Updating ajaxMetricsEnabled and ajaxMetricsThreshold (hot)");
        // TODO: Add check to see if need to update for each test
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_ENABLED,
            "true", agent);
        CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.AJAX_METRICS_THRESHOLD, "0",
            agent);

        // Deleting old CEM definitions. Not needed for Include/ExcludeURLListProperty test.
        try {
            if (CommonUtils.doesBSExist(Test_BS_BT.TEST_BUSINESS_SERVICE, em)) {
                CommonUtils.deleteBizDef(em);
                System.out.println("includeExcludeURLListProperty -- Deleted Biz Def");
                LOGGER
                    .info("includeExcludeURLListProperty -- Deleted Business Definitions - Not needed for this test");
            } else {
                LOGGER
                    .info("includeExcludeURLListProperty -- No need to delete Business Definitions - Do not exist");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LOGGER.info("includeExcludeURLListProperty -- Ended Step - 1 - Pre-req");

    }

}
