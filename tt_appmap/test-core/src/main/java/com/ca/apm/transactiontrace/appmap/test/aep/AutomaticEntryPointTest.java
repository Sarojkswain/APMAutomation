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
package com.ca.apm.transactiontrace.appmap.test.aep;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.apm.transactiontrace.appmap.testbed.aep.AutomaticEntryPointTestbed;
import com.ca.apm.transactiontrace.appmap.util.GraphHolder;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bhusu01
 */
public class AutomaticEntryPointTest extends TasTestNgTest{

    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticEntryPointTest.class);

    private static final String GRAPH_URL = "http://%s:8081/apm/appmap/private/graph/";
    private static final long WAIT_TIME_MS = 60000;
    private static final String TRANSACTION_TRACE_SESSION_CLW =
        "trace transactions exceeding 1 ms in agents matching \".*\" for 2000 s";

    private final RestClient restClient = new RestClient();

    @Test(groups = {"automatic-entry-points"})
    @Tas(testBeds = @TestBed(name = AutomaticEntryPointTestbed.class, executeOn =
        AutomaticEntryPointTestbed.MACHINE_ID_EM),owner = "bhusu01", size = SizeType.MEDIUM)
    public void testAutomaticEntryPoints() throws IOException, InterruptedException {
        configureAgentForAEP();
        setJREHome();
        startComponents();
        runCLW(AutomaticEntryPointTestbed.ROLE_ID_EM, TRANSACTION_TRACE_SESSION_CLW);
        for(int i = 0; i < 10 ; i++) {
            startLoad(100);
            LOGGER.info("Sleeping for 60 seconds before next request cycle");
            Thread.sleep(WAIT_TIME_MS);
        }
        // verify initial graph
        verifyInitGraph();
        resetFrontendConfiguration();
        LOGGER.info("Sleeping for 60 seconds to allow agent to reload configuration");
        Thread.sleep(WAIT_TIME_MS);
        for(int i = 0; i < 15 ; i++) {
            startLoad(100);
            LOGGER.info("Sleeping for 60 seconds before next request cycle");
            Thread.sleep(WAIT_TIME_MS);
        }
        verifyFinalGraph();
    }

    private void verifyFinalGraph() throws IOException {
        String emHost = envProperties.getMachineHostnameByRoleId(AutomaticEntryPointTestbed
            .ROLE_ID_EM);
        String graphURL = String.format(GRAPH_URL, emHost);
        EmRestRequest graphRequest = new EmRestRequest(graphURL, AutomaticEntryPointTestbed.ADMIN_AUX_TOKEN);
        IRestResponse<String> response = restClient.process(graphRequest);
        LOGGER.info(response.getHttpStatus().toString());
        LOGGER.info(response.getPayload());
        LOGGER.info(response.getContent());
        Assert.assertEquals(response.getHttpStatus().getStatusCode(), 200);
        final GraphHolder graphHolder = GraphHolder.initGraph(response.getPayload());
        final GraphHolder expectedFinalGraph = new GraphHolder();
        expectedFinalGraph.addVertex("tas-itc-n36_8080|Paths|Default");
        expectedFinalGraph.addVertex("Automatic Entry Points|Http11Processor|process");
        expectedFinalGraph.addVertex("Default BT");
        expectedFinalGraph.addVertex("TestServlet|service");
        expectedFinalGraph.addVertex("Automatic Entry Points|Http11Protocol$Http11ConnectionHandler|process");
        expectedFinalGraph.addVertex("HttpURLConnectionPost|service");

        expectedFinalGraph.addEdge("Automatic Entry Points|Http11Protocol$Http11ConnectionHandler|process",
            "tas-itc-n36_8080|Paths|Default");
        expectedFinalGraph.addEdge("Automatic Entry Points|Http11Processor|process",
            "tas-itc-n36_8080|Paths|Default");
        expectedFinalGraph.addEdge("Default BT", "HttpURLConnectionPost|service");
        expectedFinalGraph.addEdge("HttpURLConnectionPost|service", "tas-itc-n36_8080|Paths|Default");
        expectedFinalGraph.addEdge("TestServlet|service", "HttpURLConnectionPost|service");
        expectedFinalGraph.addEdge("TestServlet|service", "TestServlet|service");

        graphHolder.assertEqualsTo(expectedFinalGraph);
    }

    private void verifyInitGraph() throws IOException {
        String emHost = envProperties.getMachineHostnameByRoleId(AutomaticEntryPointTestbed
            .ROLE_ID_EM);
        String graphURL = String.format(GRAPH_URL, emHost);
        EmRestRequest graphRequest = new EmRestRequest(graphURL, AutomaticEntryPointTestbed.ADMIN_AUX_TOKEN);
        IRestResponse<String> response = restClient.process(graphRequest);
        LOGGER.info(response.getHttpStatus().toString());
        LOGGER.info(response.getPayload());
        LOGGER.info(response.getContent());
        Assert.assertEquals(response.getHttpStatus().getStatusCode(), 200);
        final GraphHolder graphHolder = GraphHolder.initGraph(response.getPayload());
        final GraphHolder expectedFinalGraph = new GraphHolder();
        expectedFinalGraph.addVertex("tas-itc-n36_8080|Paths|Default");
        expectedFinalGraph.addVertex("Automatic Entry Points|Http11Processor|process");
        expectedFinalGraph.addVertex("Automatic Entry Points|Http11Protocol$Http11ConnectionHandler|process");

        expectedFinalGraph.addEdge("Automatic Entry Points|Http11Protocol$Http11ConnectionHandler|process",
            "tas-itc-n36_8080|Paths|Default");
        expectedFinalGraph.addEdge("Automatic Entry Points|Http11Processor|process",
            "tas-itc-n36_8080|Paths|Default");

        graphHolder.assertEqualsTo(expectedFinalGraph);
    }

    private void resetFrontendConfiguration() {
        String tomcatDir =
            envProperties.getRolePropertyById(AutomaticEntryPointTestbed.ROLE_ID_TOMCAT, "tomcatInstallDir");
        String agentConfigDir = tomcatDir + File.separator + "wily" + File.separator + "core" +
            File.separator + "config" + File.separator;

        // Change toggles typical
        String togglesTypical = agentConfigDir + "toggles-typical.pbd";
        Map<String, String> togglesReplaceMap = new HashMap<>();
        togglesReplaceMap.put("#TurnOn: HTTPServletTracing", "TurnOn: HTTPServletTracing");
        togglesReplaceMap.put("#TurnOn: JSPTracing", "TurnOn: JSPTracing");
        togglesReplaceMap.put("#TurnOn: JSPServletTracing", "TurnOn: JSPServletTracing");
        replaceContent(togglesTypical, togglesReplaceMap);
    }

    /**
     * Runs the specified CLW command on the specified EM role
     *
     * @param roleEm
     * @param command
     */
    private void runCLW(String roleEm, String command) {
        String machineId = envProperties.getMachineIdByRoleId(roleEm);
        String clwPath = envProperties.getRolePropertyById(roleEm, EmRole.ENV_PROPERTY_INSTALL_DIR)
            + "/lib/CLWorkstation.jar";
        RunCommandFlowContext permitFile =
            new RunCommandFlowContext.Builder("java").args(Arrays.asList(new String[] {"-jar",
                clwPath, command})).build();
        runCommandFlowByMachineIdAsync(machineId, permitFile);
    }

    private void startLoad(int count) throws IOException {
        String qaTestAppURL = envProperties.getRolePropertyById(AutomaticEntryPointTestbed
            .ROLE_ID_TOMCAT, "qaTestAppRole_url");
        String postRequestURL = qaTestAppURL + "/helloworld/HttpURLConnectionPost";
        Map<String, String> params = new HashMap<>();
        params.put("host", envProperties.getMachineHostnameByRoleId(AutomaticEntryPointTestbed
            .ROLE_ID_TOMCAT));
        params.put("port", envProperties.getRolePropertyById(AutomaticEntryPointTestbed
            .ROLE_ID_TOMCAT, "tomcatCatalinaPort"));
        params.put("path", "/QATestApp/helloworld/TestServlet");
        params.put("start", "HttpURLConnection+-+Post");
        QATestAppPOSTRequest postRequest = new QATestAppPOSTRequest(postRequestURL, params);

        for(int i = 0; i < count ; i++) {
            LOGGER.info("Issuing request " + i);
            IRestResponse<String> response = restClient.process(postRequest);

            if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
                throw new IllegalStateException(
                    "Error in rest request: " + response.getHttpStatus().getStatusCode() + " "
                        + response.getHttpStatus().getReasonPhrase());
            }
            // LOGGER.info(response.getPayload());
        }
    }

    private void setJREHome() {
        String tomcatDir =
            envProperties.getRolePropertyById(AutomaticEntryPointTestbed.ROLE_ID_TOMCAT, "tomcatInstallDir");
        String tomcatSetEnvFile = tomcatDir + File.separator + "bin" + File.separator + "setenv"
            + ".bat";

        try {
            String content = IOUtils.toString(new FileInputStream(tomcatSetEnvFile));
            String jreHome = "set JRE_HOME=C:\\Program Files\\Java\\jdk1.7.0_51";
            content = jreHome + "\n" + content;
            IOUtils.write(content, new FileOutputStream(tomcatSetEnvFile));
        } catch (IOException e) {
            LOGGER.error("Error opening " + tomcatSetEnvFile);
        }

    }

    // Perform required configuration for AEP vertices
    private void configureAgentForAEP() {
        String tomcatDir =
            envProperties.getRolePropertyById(AutomaticEntryPointTestbed.ROLE_ID_TOMCAT, "tomcatInstallDir");
        String agentConfigDir = tomcatDir + File.separator + "wily" + File.separator + "core" +
            File.separator + "config" + File.separator;

        // Change agent profile file
        String profilePath = agentConfigDir + "IntroscopeAgent.profile";
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put(" = ", "=");
        replaceContent(profilePath, replaceMap);

        // Change toggles typical
        String togglesTypical = agentConfigDir + "toggles-typical.pbd";
        Map<String, String> togglesReplaceMap = new HashMap<>();
        togglesReplaceMap.put("TurnOn: HTTPServletTracing", "#TurnOn: HTTPServletTracing");
        togglesReplaceMap.put("TurnOn: JSPTracing", "#TurnOn: JSPTracing");
        togglesReplaceMap.put("TurnOn: JSPServletTracing", "#TurnOn: JSPServletTracing");
        replaceContent(togglesTypical, togglesReplaceMap);

        // Change intelligent.pbd
        String intelligent = agentConfigDir + "intelligent.pbd";
        Map<String, String> intelligentMap = new HashMap<>();
        intelligentMap.put("IncludeIntelligentInstrumentationPackagePrefix: org.apache.axis.",
            "IncludeIntelligentInstrumentationPackagePrefix: org.apache."
                + "\nIncludeIntelligentInstrumentationPackagePrefix: org.apache.axis.");
        replaceContent(intelligent, intelligentMap);
    }

    private void replaceContent(String filePath, Map<String, String> replaceMap) {
        LOGGER.info("Attempting to replace content " + replaceMap + " in file " + filePath);
        try {
            String content = IOUtils.toString(new FileInputStream(filePath));
            for(Map.Entry<String, String> entry: replaceMap.entrySet()) {
                content = content.replaceAll(entry.getKey(), entry.getValue());
            }
            IOUtils.write(content, new FileOutputStream(filePath));
        } catch (IOException e) {
            LOGGER.error("Error opening file " + filePath);
        }
    }

    // Start Em and agent components
    private void startComponents() {
        runSerializedCommandFlowFromRole(AutomaticEntryPointTestbed.ROLE_ID_TOMCAT, TomcatRole.ENV_TOMCAT_START);
        runSerializedCommandFlowFromRole(AutomaticEntryPointTestbed.ROLE_ID_EM, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(AutomaticEntryPointTestbed.ROLE_ID_EM, EmRole.ENV_START_WEBVIEW);
    }

    class QATestAppPOSTRequest implements IRestRequest<String> {

        private final String url;
        private final Map<String, String> params;

        QATestAppPOSTRequest(String url, Map<String, String> params) {
            this.url = url;
            this.params = params;
        }

        @Override
        public HttpRequestBase getRequest() throws IOException {
            HttpPost request = new HttpPost(url);
            List<NameValuePair> postParameters = new ArrayList<>();
            for(Map.Entry<String, String> entry: params.entrySet()) {
                BasicNameValuePair nameValuePair =
                    new BasicNameValuePair(entry.getKey(), entry.getValue());
                postParameters.add(nameValuePair);

            }
            request.setEntity(new UrlEncodedFormEntity(postParameters));
            return request;
        }

        @NotNull
        @Override
        public IRestResponse<String> getResponse(CloseableHttpResponse closeableHttpResponse,
                                                 String payload) {
            return new SimpleResponse(closeableHttpResponse, payload);
        }
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
        public IRestResponse<String> getResponse(final CloseableHttpResponse httpResponse, String
            payload) {
            return new SimpleResponse(httpResponse, payload);
        }
    }

}
