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
package com.ca.tas.em.rest.api.test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HTTP;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.WebView;
import com.ca.apm.testbed.atc.QATestAppEmTestBed;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.test.utils.GraphHolder;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Multimap;

/**
 * Tests the new agent way of generating the app map. First step is to validate that the map is
 * generated.
 * The next step tests whether the items (edges, vertices) in the map will not get duplicated if the
 * action is replayed.
 * Additionally, we test whether the traces sent from the Agent are not sent if the same trace was
 * already reported (checking the count of traces for a given endpoint in the WebView UI).
 * 
 * @author surma04
 *
 */
public class MapGenerationTest extends UITest {

    private static final long ONE_MINUTE_TIME = 60000;
    private static final String GRAPH_URL = "http://%s:8081/apm/appmap/private/graph/";

    private static final Logger LOGGER = LoggerFactory.getLogger(MapGenerationTest.class);
    private final RestClient restClient = new RestClient();

    @Test(groups = {"automatic-entry-points"})
    @Tas(testBeds = @TestBed(name = QATestAppEmTestBed.class, executeOn = QATestAppEmTestBed.MACHINE_ID_EM), owner = "surma04", size = SizeType.MEDIUM)
    public void testAppMapGeneration() throws Exception {
        startUpTestBed();
        EnvironmentPropertyContext env =
            new EnvironmentPropertyContextFactory().createFromSystemProperty();
        String hostname = env.getMachineHostnameByRoleId(QATestAppEmTestBed.ROLE_ID_EM);

        getUI().openUrlInANewTab(String.format("http://%s:8080/QATestApp", hostname));
        final RemoteWebDriver driver = getUI().getDriver();
        clickInstrumentPoint(driver);

        GraphHolder graph = getGraph();
        verifyGraphInstrument(graph);

        getUI().openUrlInANewTab(String.format("http://%s:8080/QATestApp", hostname));
        clickSkipClass(driver);

        graph = getGraph();
        verifyAfterSkipClass(graph);

        // click again and look whether nothing has changed
        getUI().openUrlInANewTab(String.format("http://%s:8080/QATestApp", hostname));
        clickInstrumentPoint(driver);
        verifyAfterSkipClass(graph);

        getUI().openUrlInANewTab(String.format("http://%s:8080/QATestApp", hostname));
        clickSkipClass(driver);
        verifyAfterSkipClass(graph);

        verifyTraceCount();
    }


    // @Test(groups = {"automatic-entry-points"})
    // @Tas(testBeds = @TestBed(name = QATestAppEmTestBed.class, executeOn =
    // QATestAppEmTestBed.MACHINE_ID_EM),owner = "surma04", size = SizeType.SMALL)
    public void verifyTraceCount() throws Exception {
        EnvironmentPropertyContext env =
            new EnvironmentPropertyContextFactory().createFromSystemProperty();
        String hostname = env.getMachineHostnameByRoleId(QATestAppEmTestBed.ROLE_ID_EM);

        final UI ui = getUI();
        ui.login();
        ui.getLeftNavigationPanel().goToMapViewPage();

        WebElement wvLink = ui.getTopNavigationPanel().getAnyWebviewLinkElement();
        wvLink.click();
        ui.switchToWebView();

        WebView webView = ui.getWebView();
        final WebElement investigator = webView.getLiTab("Investigator");
        if (investigator != null) {
            investigator.click();
        } else {
            Assert.fail("Investigator tab not found");
        }
        webView.expandTreeItem("*SuperDomain*");
        webView.expandTreeItem(hostname);
        webView.expandTreeItem("Tomcat");
        webView.expandTreeItem("Tomcat Agent (*SuperDomain*)");

        webView.getLiTab("Traces").click();

        int a = webView.countTableOccurences("/QATestApp/directives/SkipClassServlet");
        Assert.assertEquals(a, 1);
        a = webView.countTableOccurences("/QATestApp/directives/InstrumentPointServlet");
        Assert.assertEquals(a, 1);
    }


    private void clickInstrumentPoint(final RemoteWebDriver driver) throws InterruptedException {
        driver.findElementByPartialLinkText("Directives").click();
        driver.findElementByLinkText("InstrumentPoint").click();
        driver.findElementByTagName("Input").click();
        getUI().closeCurrentTab();
        Thread.sleep(ONE_MINUTE_TIME);
    }

    private void clickSkipClass(final RemoteWebDriver driver) throws InterruptedException {
        driver.findElementByPartialLinkText("Directives").click();
        driver.findElementByLinkText("SkipClass").click();
        driver.findElementByTagName("Input").click();
        getUI().closeCurrentTab();
        Thread.sleep(ONE_MINUTE_TIME);
    }

    private void verifyAfterSkipClass(GraphHolder graph) {
        List<String> vertices = graph.getVertices();
        LOGGER.info(String.format("Vertices (%d): %s", vertices.size(), vertices.toString()));

        Multimap<String, String> edges = graph.getEdges();
        LOGGER.info("EDGES:" + edges.toString());

        Collection<String> collection = edges.get("QATestApp");
        Assert.assertEquals(collection.size(), 3,
            "invalid edge count, expected JspServlet and InstrumentPointServlet: " + collection);
        Assert.assertTrue(collection.contains("JspServlet|service"));
        Assert.assertTrue(collection.contains("SkipClassServlet|service"));
        Assert.assertTrue(collection.contains("InstrumentPointServlet|service"));

        verifyJspServletEdges(edges);

    }

    private void verifyGraphInstrument(GraphHolder graph) throws IOException {
        List<String> vertices = graph.getVertices();
        LOGGER.info(String.format("Vertices (%d): %s", vertices.size(), vertices.toString()));

        Multimap<String, String> edges = graph.getEdges();
        LOGGER.info("EDGES:" + edges.toString());

        Collection<String> collection = edges.get("QATestApp");
        Assert.assertEquals(collection.size(), 2,
            "invalid edge count, expected JspServlet and InstrumentPointServlet: " + collection);
        Assert.assertTrue(collection.contains("JspServlet|service"));
        Assert.assertTrue(collection.contains("InstrumentPointServlet|service"));

        verifyJspServletEdges(edges);
    }


    private void verifyJspServletEdges(Multimap<String, String> edges) {
        Collection<String> collection;
        collection = edges.get("JspServlet|service");
        Assert.assertEquals(collection.size(), 2,
            "invalid edge count, expected directives and index: " + collection);
        Assert.assertTrue(collection.contains("directives_jsp|service"));
        Assert.assertTrue(collection.contains("index_jsp|service"));

        collection = edges.get("directives_jsp|service");
        Assert.assertEquals(collection.size(), 1, "invalid edge count, DefaultServlet expected: "
            + collection);
        Assert.assertTrue(collection.contains("DefaultServlet|service"));
    }


    private GraphHolder getGraph() throws IOException {
        String emHost = envProperties.getMachineHostnameByRoleId(QATestAppEmTestBed.ROLE_ID_EM);
        String graphURL = String.format(GRAPH_URL, emHost);
        EmRestRequest graphRequest =
            new EmRestRequest(graphURL, QATestAppEmTestBed.ADMIN_AUX_TOKEN);
        IRestResponse<String> response = restClient.process(graphRequest);
        LOGGER.info(response.getHttpStatus().toString());
        LOGGER.info(response.getPayload());
        LOGGER.info(response.getContent());
        Assert.assertEquals(response.getHttpStatus().getStatusCode(), 200);
        final GraphHolder graph = GraphHolder.initGraph(response.getPayload());
        return graph;
    }

    /**
     * 
     */
    private void startUpTestBed() {
        runSerializedCommandFlowFromRole(QATestAppEmTestBed.ROLE_ID_TOMCAT,
            TomcatRole.ENV_TOMCAT_START);
        runSerializedCommandFlowFromRole(QATestAppEmTestBed.ROLE_ID_EM, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(QATestAppEmTestBed.ROLE_ID_EM, EmRole.ENV_START_WEBVIEW);
    }


    class EmRestRequest implements IRestRequest<String> {

        private final String url;
        private final String authToken;

        EmRestRequest(String url, String authToken) {
            this.url = url;
            this.authToken = authToken;
        }

        @Override
        public HttpRequestBase getRequest() throws IOException {
            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", "Bearer " + authToken);
            request.addHeader(HTTP.CONTENT_TYPE, "application/json");
            return request;
        }

        @Override
        public IRestResponse<String> getResponse(final CloseableHttpResponse httpResponse,
            String payload) {
            return new SimpleResponse(httpResponse, payload);
        }
    }

}
