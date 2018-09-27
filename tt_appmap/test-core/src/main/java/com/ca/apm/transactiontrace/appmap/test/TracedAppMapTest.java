/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.transactiontrace.appmap.test;

import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.transactiontrace.appmap.pages.LoginPage;
import com.ca.apm.transactiontrace.appmap.pages.PrivateAPIGraphJson;
import com.ca.apm.transactiontrace.appmap.pages.TeamCenterPage;
import com.ca.apm.transactiontrace.appmap.testbed.SeleniumGridMachinesFactory;
import com.ca.apm.transactiontrace.appmap.util.SampleGraph;
import com.ca.apm.transactiontrace.appmap.util.SampleGraph.Vertex;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.test.TasTestNgTest;

/**
 * Test that logs into the Team center and requests the graph object using private API
 *
 * We compare the resultant output with the expected output
 *
 * @author TAS (tas@ca.com)
 * @since 1.0
 */
public class TracedAppMapTest extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TracedAppMapTest.class);
    protected WebDriver driver;
    private EnvironmentPropertyContext envProp;

    private DesiredCapabilities dc;
    protected String emRoleId;

    /**
     * @param driverClassName - WebDriver full class name
     */
    public TracedAppMapTest(DesiredCapabilities dc, String emRoleId) {
        this.dc = dc;
        this.emRoleId = emRoleId;
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
        LOGGER.info("Webview URL: {}", getWebViewUrl());
        LOGGER.info("Selenium Grid Hub URL: {}", getSeleniumGridHubUrl());
    }

    @AfterTest
    public void closeDriver() {
        if(driver!=null) {
            driver.close();
        }
    }

    @Test
    public void populatedMapTest() throws IOException {
        LOGGER.info("Accessing webview at " + getWebViewMapUrl());
        driver.get(getWebViewMapUrl());

        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("admin");

        TeamCenterPage teamCenterPage = loginPage.submitLogin();
        teamCenterPage.checkTeamCenterPageContent();

        PrivateAPIGraphJson graphJson = new PrivateAPIGraphJson(driver);
        String graphAsJSON = graphJson.getJSonGraph(getWebViewUrl());

        LOGGER.info(graphAsJSON);

        SampleGraph loadedGraph = SampleGraph.initGraph(graphAsJSON);

        InputStream stream =
            TracedAppMapTest.class.getResourceAsStream("/sample/graphs/sampleGraph_1.json");

        String expectedGraphAsString = IOUtils.toString(stream);

        SampleGraph expectedGraph = SampleGraph.initGraph(expectedGraphAsString);

        Map<String, List<String>> edges = loadedGraph.getEdges();

        int edgesNotInExpectedMap = 0;
        // When vertices in produced graph don't match any in expected graph, we have a new vertex
        // and edge not in the expected graph
        int edgesWithVerticesNotInExpectedGraph = 0;

        // Id's in expected graph and loaded graph are different from each other.
        // So we iterate through all edges in loaded graph to match with those in expected graph
        for (String tail : edges.keySet()) {
            Vertex tailVertex = loadedGraph.getVertex(tail);
            String tailID = expectedGraph.matchVertex(tailVertex.getName());
            List<String> heads = edges.get(tail);
            for (String head : heads) {
                Vertex headVertex = loadedGraph.getVertex(head);
                String headID = expectedGraph.matchVertex(headVertex.getName());
                if (!expectedGraph.matchEdge(tailID, headID)) {
                    ++edgesNotInExpectedMap;
                } else {
                    ++edgesWithVerticesNotInExpectedGraph;
                }
            }
        }

        assertTrue(expectedGraph.matchedEdgeCount() > 0);

        LOGGER.info(
            "Edges from transaction traces matched " + expectedGraph.matchedEdgeCount() + " out of "
                + expectedGraph.getEdgeCount() + " known edges");
        LOGGER.info(expectedGraph.unMatchedEdgeCount() + " edges still remain undiscovered");
        LOGGER.info(edgesNotInExpectedMap
            + " edges(non-unique) with known vertices that are not in the expected map graph");
        LOGGER.info(edgesWithVerticesNotInExpectedGraph
            + " edges(non-unique) with unknown vertices that are not in the expected map graph");

    }

    private String getWebViewUrl() {
        String hostname = envProp.getRolePropertiesById(emRoleId).getProperty("em_hostname");
        String port = envProp.getRolePropertiesById(emRoleId).getProperty("wvPort");
        return String.format("http://%s:%s", hostname, port);
    }
    
    private String getWebViewMapUrl() {
        return getWebViewUrl() + "/ApmServer/#/map";
    }

    private String getSeleniumGridHubUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SeleniumGridMachinesFactory.HUB_ROLE_ID);
        return String.format("http://%s:4444/wd/hub", hostname);
    }
}
