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

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import com.ca.apm.transactiontrace.appmap.pages.LoginPage;
import com.ca.apm.transactiontrace.appmap.pages.PrivateAPIGraphJson;
import com.ca.apm.transactiontrace.appmap.pages.TeamCenterPage;
import com.ca.apm.transactiontrace.appmap.role.DeferredInitiateTransactionTraceSessionRole;
import com.ca.apm.transactiontrace.appmap.testbed.SeleniumGridMachinesFactory;
import com.ca.apm.transactiontrace.appmap.util.SampleGraph;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.test.TasTestNgTest;

/**
 * Abstract superclass for tests that start a TT session during which they execute some actions
 * on a test application and then log into the Team Center and request the graph object using
 * private API.
 *
 * We check whether there are any edges in the resulting graph.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public abstract class AbstractAgentTest extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAgentTest.class);
    protected WebDriver driver;
    private EnvironmentPropertyContext envProp;
    
    private DesiredCapabilities dc;

    protected AbstractAgentTest(DesiredCapabilities dc) {
        this.dc = dc;
    }

    @BeforeMethod
    public void initWebDriver() throws MalformedURLException {
        driver = createWebDriver();
        driver.manage().window().maximize();
        LOGGER.info("Browser window size: " + driver.manage().window().getSize()); 
    }

    /**
     * Creates a remote web driver
     * 
     * @return
     * @throws MalformedURLException
     */
    protected WebDriver createWebDriver() throws MalformedURLException {
        return createWebDriver(dc);
    }

    protected WebDriver createWebDriver(DesiredCapabilities dc) throws MalformedURLException {
        final String seleniumGridHubUrl = getSeleniumGridHubUrl();
        LOGGER.info("Creating web driver from " + seleniumGridHubUrl);
        return new RemoteWebDriver(new URL(seleniumGridHubUrl), dc);
    }

    @BeforeTest
    public void loadEnvProperties() throws IOException {
        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
        LOGGER.info("Selenium Grid Hub URL: {}", getSeleniumGridHubUrl());
    }

    protected void checkAtcGraph(String emRoleId) {
        final String webViewMapUrl = getWebViewMapUrl(emRoleId);

        LOGGER.info("Accessing WebView at " + webViewMapUrl);
        driver.get(webViewMapUrl);

        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("admin");

        TeamCenterPage teamCenterPage = loginPage.submitLogin();
        teamCenterPage.checkTeamCenterPageContent();

        PrivateAPIGraphJson graphJson = new PrivateAPIGraphJson(driver);
        final String webViewUrl = getWebViewUrl(emRoleId);
        String graphAsJSON = graphJson.getJSonGraph(webViewUrl);

        LOGGER.info(graphAsJSON);

        SampleGraph loadedGraph = SampleGraph.initGraph(graphAsJSON);

        assertTrue(loadedGraph.getEdgeCount() > 0, "The graph doesn't contain any edges!");
    }

    protected void initiateTTSession(String initiateTtSessionRoleId) {
        runSerializedCommandFlowFromRoleAsync(initiateTtSessionRoleId,
            DeferredInitiateTransactionTraceSessionRole.ENV_INITIATE_TT_SESSION_COMMAND);
    }

    private String getWebViewUrl(String emRoleId) {
        String hostname = envProperties.getRolePropertiesById(emRoleId).getProperty("em_hostname");
        String port = envProperties.getRolePropertiesById(emRoleId).getProperty("wvPort");
        return String.format("http://%s:%s", hostname, port);
    }
    
    private String getWebViewMapUrl(String emRoleId) {
        return getWebViewUrl(emRoleId) + "/ApmServer/#/map";
    }

    private String getSeleniumGridHubUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SeleniumGridMachinesFactory.HUB_ROLE_ID);
        return String.format("http://%s:4444/wd/hub", hostname);
    }
}
