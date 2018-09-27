/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.transactiontrace.appmap.test;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for tests that start a TT session during which they execute some actions
 * on QATestApp and then log into the Team Center and request the graph object using private API.
 *
 * We check whether there are any edges in the resulting graph.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public abstract class AbstractJavaAgentTest extends AbstractAgentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJavaAgentTest.class);

    private static final String QA_TEST_APP_URL_PART = "QATestApp";

    private static final String TEST_URL_PART = "/transactiontraces/ForwardServlet";
    private static final String TEST2_URL_PART = "/transactiontraces/IncludeServlet";

    protected AbstractJavaAgentTest(DesiredCapabilities dc) {
        super(dc);
    }

    protected void verifyWithJavaAgent(String appServerRoleId, String appServerPort,
        String emRoleId, String initiateTtSessionRoleId) {

        LOGGER.info("Waiting 120s for a Java agent to get connected...");
        try {
            Thread.sleep(120 * 1000L);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while waiting for a Java agent", e);
        }

        initiateTTSession(initiateTtSessionRoleId);

        executeQaTestAppActions(appServerRoleId, appServerPort);

        LOGGER.info("Waiting 60s for the TT processing...");
        try {
            Thread.sleep(60 * 1000L);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while waiting for the TT processing", e);
        }

        checkAtcGraph(emRoleId);
    }

    private void executeQaTestAppActions(String appServerRoleId, String appServerPort) {
        final String qaTestAppUrl = getQATestAppUrl(appServerRoleId, appServerPort);

        LOGGER.info("Accessing QATestApp at " + qaTestAppUrl + TEST_URL_PART);
        driver.get(qaTestAppUrl + TEST_URL_PART);
        LOGGER.info("Accessing QATestApp at " + qaTestAppUrl + TEST2_URL_PART);
        driver.get(qaTestAppUrl + TEST2_URL_PART);
    }

    private String getQATestAppUrl(String appServerRoleId, String appServerPort) {
        String hostname = envProperties.getMachineHostnameByRoleId(appServerRoleId);
        return String.format("http://%s:%s/%s", hostname, appServerPort, QA_TEST_APP_URL_PART);
    }

}
