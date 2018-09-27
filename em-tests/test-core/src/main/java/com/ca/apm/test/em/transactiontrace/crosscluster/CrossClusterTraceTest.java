/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
package com.ca.apm.test.em.transactiontrace.crosscluster;

import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.test.em.agc.RegisterAgcTest;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.role.testapp.custom.NowhereBankBTRole;
import com.ca.tas.test.em.agc.CrossClusterTracesRevertTestBed;
import com.ca.tas.test.em.agc.CrossClusterTracesTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Test for checking cross cluster appmap on Nowhere bank application
 */
public class CrossClusterTraceTest extends RegisterAgcTest {

    private static final String EXPECTED_NOWHEREBANK_GRAPH_JSON =
        "/com/ca/tas/test/em/tt/crosscluster/NoWhereBankFull.json";
    private static final String NOWHEREBANK_WITHOUT_MEDIATOR =
        "/com/ca/tas/test/em/tt/crosscluster/NoWhereBankWithoutMediator.json";
    /**
     * Named constant to indicate no change
     */
    private static final String NO_CHANGE = "noChange";
    /**
     * Profiles to select the hosts for the no where bank agents in the order Engine, Mediator and Portal respectively.
     * In each profile, the hosts are rotated by one position
     */
    private static final String[][] PROFILES =
        {{CrossClusterTracesTestBed.AGC_COLLECTOR_ROLE_ID, CrossClusterTracesTestBed.STANDALONE_ROLE_ID, NO_CHANGE}, 
         {NO_CHANGE, CrossClusterTracesTestBed.AGC_COLLECTOR_ROLE_ID, CrossClusterTracesTestBed.STANDALONE_ROLE_ID}, 
         {CrossClusterTracesTestBed.STANDALONE_ROLE_ID, NO_CHANGE, CrossClusterTracesTestBed.AGC_COLLECTOR_ROLE_ID}
        };
    private static final int SHUTDOWN_DELAY = 180;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final RestClient restClient = new RestClient();

    /* -- public -- */

    @Tas(testBeds = @TestBed(name = CrossClusterTracesRevertTestBed.class, executeOn = "standalone"), owner = "bhusu01", size = SizeType.BIG)
    @Test(groups = {"agc", "smoke"})
    public void configureRevertTestbed() throws Exception {
        startComponents(1);
    }

    @Tas(testBeds = @TestBed(name = CrossClusterTracesTestBed.class, executeOn = "agc"), owner = "bhusu01", size = SizeType.MEDIUM)
    @Test(groups = {"cross-cluster", "rotating-tests"})
    public void testGraphForCrossClusterEdgesOne() {
        testProfile(1);
    }

    @Tas(testBeds = @TestBed(name = CrossClusterTracesTestBed.class, executeOn = "agc"), owner = "bhusu01", size = SizeType.MEDIUM)
    @Test(groups = {"cross-cluster"})
    public void testGraphForCrossClusterEdgesTwo() {
        testProfile(2);
    }

    @Tas(testBeds = @TestBed(name = CrossClusterTracesTestBed.class, executeOn = "agc"), owner = "bhusu01", size = SizeType.MEDIUM)
    @Test(groups = {"cross-cluster"})
    public void testGraphForCrossClusterEdgesThree() {
        testProfile(3);
    }

    @Tas(testBeds = @TestBed(name = CrossClusterTracesTestBed.class, executeOn = "agc"), owner = "bhusu01", size = SizeType.MEDIUM)
    @Test(groups = {"cross-cluster"})
    public void testEMWentAwayFromAGC() throws InterruptedException {
        startComponents(1);
        ClwRunner localClwRunner =
            utilities.createClwUtils(CrossClusterTracesTestBed.AGC_ROLE_ID).getClwRunner();
        ClwRunner clwRunner =
            utilities.createClwUtils(CrossClusterTracesTestBed.STANDALONE_ROLE_ID).getClwRunner();
        EmUtils emUtils = utilities.createEmUtils();
        emUtils.stopRemoteEmWithTimeoutSec(localClwRunner, clwRunner, SHUTDOWN_DELAY);
        log.info("Sleeping for 3 minutes to allow AGC to catch up");
        Thread.sleep(3 * 60 * 1000);
        log.info("Woke up from sleep. Continuing test");
        // Fetch the graph before starting EM
        ATCGraph currentGraph = fetchCurrentGraph();

        // Test that all Portal and Engine vertices and edges are intact
        compareWithExpectedGraph(currentGraph, NOWHEREBANK_WITHOUT_MEDIATOR);
        // Test that no vertices that belong to Mediator are present
        ATCGraph.testGraphForGoneAwayApplication(currentGraph, "[Mediator]");
    }

    /* -- private helper methods -- */

    /**
     * Retrieves graph from AGC and checks for all the expected edges, vertices and their attributes
     */
    private void testProfile(int profile) {
        startComponents(profile);
        checkWebview(CrossClusterTracesTestBed.AGC_ROLE_ID);
        // Wait for fast correlation buffer timeout + AGC correlation timeout (30 + 30) 
        try {
            Thread.sleep((30+30) * 1000);
        } catch (InterruptedException e) {
            // we don't care
        }
        compareWithExpectedGraph(fetchCurrentGraph(), EXPECTED_NOWHEREBANK_GRAPH_JSON);
    }

    private void startComponents(int profile) {
        configureNowhereBankAgents(profile);
        startAllAgents();
        try {
            configureTestbed();
        } catch (Exception e) {
            fail("Exception configuring test bed " + e.getMessage());
            log.error("Exception configuring test bed", e);
        }
    }

    /**
     * Configures NowhereBank agents according to the chosen profile
     *
     * @param profile
     */
    private void configureNowhereBankAgents(int profile) {
        String[] roles = PROFILES[profile - 1];
        assert roles.length == 3 : "Specify exactly 3 roles";
        String hostName = null;
        for (int i = 0; i < 3; i++) {
            String role = roles[i];
            if (role == NO_CHANGE) {
                continue;
            }
            hostName = envProperties.getMachineHostnameByRoleId(role);
            // For this test, we always modify the agents on Collector machine
            configureAgent(hostName, NowhereBankBTRole.NWB_AGENT_NAMES[i], CrossClusterTracesTestBed.MACHINE_ID_COLLECTOR);
        }
    }

    /**
     * Configures the chosen agentName on the chosen machineID to report to the host on the chosen
     * host machine. The port is not modified and left as default - 5001.
     *
     * @param hostName
     * @param agentName
     * @param machineID
     */
    private void configureAgent(String hostName, String agentName, String machineID) {
        String installDirPath = envProperties.getRolePropertiesById(machineID
            + CrossClusterTracesTestBed.NWB_ROLE_ID_SUFFIX).getProperty(NowhereBankBTRole.INSTALL_DIR);
        String profileFileFormatter = envProperties.getRolePropertiesById(machineID
            + CrossClusterTracesTestBed.NWB_ROLE_ID_SUFFIX).getProperty(NowhereBankBTRole.PROFILE_FILE_FORMATTER);

        Map<String, String> replacePairsConfig = new HashMap<String, String>();
        replacePairsConfig.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", hostName);

        ConfigureFlowContext.Builder builder = new ConfigureFlowContext.Builder();
        builder.configurationMap(
            installDirPath + String.format(profileFileFormatter, agentName), replacePairsConfig);

        runConfigureFlowByMachineIdAsync(machineID, builder.build());
    }

    private void startAllAgents() {
        startNowhereBankOnMachine(CrossClusterTracesTestBed.MACHINE_ID_MOM);
        startNowhereBankOnMachine(CrossClusterTracesTestBed.MACHINE_ID_COLLECTOR);
        startNowhereBankOnMachine(CrossClusterTracesTestBed.MACHINE_ID_AGC);
    }

    /**
     * Starts all 5 nowherebank components on the specified machine
     *
     * @param machineId
     */
    private void startNowhereBankOnMachine(String machineId) {
        runSerializedCommandFlowFromRoleAsync(machineId
            + CrossClusterTracesTestBed.NWB_ROLE_ID_SUFFIX, NowhereBankBTRole.MESSAGING_SERVER_01);
        runSerializedCommandFlowFromRoleAsync(machineId
            + CrossClusterTracesTestBed.NWB_ROLE_ID_SUFFIX, NowhereBankBTRole.BANKING_ENGINE_02);
        runSerializedCommandFlowFromRoleAsync(machineId
            + CrossClusterTracesTestBed.NWB_ROLE_ID_SUFFIX, NowhereBankBTRole.BANKING_MEDIATOR_03);
        runSerializedCommandFlowFromRoleAsync(machineId
            + CrossClusterTracesTestBed.NWB_ROLE_ID_SUFFIX, NowhereBankBTRole.BANKING_PORTAL_04);
        runSerializedCommandFlowFromRoleAsync(machineId
            + CrossClusterTracesTestBed.NWB_ROLE_ID_SUFFIX, NowhereBankBTRole.BANKING_GENERATOR_05);
    }

    /**
     * Compare two graphs for equivalence
     *
     * @param actualFilteredGraph
     * @param expectedGraphPath
     */
    private void compareWithExpectedGraph(ATCGraph actualFilteredGraph, String expectedGraphPath) {
        ATCGraph expectedGraph = null;
        try {
            expectedGraph = ATCGraph.createGraphFromFile(expectedGraphPath);
        } catch (IOException e) {
            Assert.fail("IOException reading file " + expectedGraphPath);
        }
        if (expectedGraph == null) {
            Assert.fail("Expected graph can't be null");
        }
        ATCGraph.testForEquivalence(actualFilteredGraph, expectedGraph);
    }

    /**
     * Fetches the latest version of graph from AGC and returns it as ATCGraph object
     * Fails if the graph is empty
     *
     * @return
     */
    private ATCGraph fetchCurrentGraph() {
        JsonObject jsonGraph = fetchAGCJsonGraph();
        log.info("Fetched graph with " + jsonGraph.get("vertices").getAsJsonArray().size()
            + " vertices and " + jsonGraph.get("edges").getAsJsonArray().size() + " edges");
        assertTrue(
            jsonGraph.get("vertices").getAsJsonArray().size() > 0, "No vertices in actual graph");
        assertTrue(jsonGraph.get("edges").getAsJsonArray().size() > 0, "No edges in actual graph");

        ATCGraph actualFilteredGraph = ATCGraph.createGraphFromJSON(jsonGraph);
        return actualFilteredGraph;
    }

    /**
     * Fetches the latest version of graph from AGC and returns it as JSON Object
     *
     * @return
     */
    private JsonObject fetchAGCJsonGraph() {
        String agcHost =
            envProperties.getMachineHostnameByRoleId(CrossClusterTracesTestBed.AGC_ROLE_ID);
        String collectorRoleHost =
            envProperties.getMachineHostnameByRoleId(CrossClusterTracesTestBed.MACHINE_ID_COLLECTOR);
        String agcEMWebPort =
            envProperties.getRolePropertiesById(CrossClusterTracesTestBed.AGC_ROLE_ID).getProperty("wvEmWebPort");

        final String urlPart =
            "http://" + agcHost + ":" + agcEMWebPort + "/apm/appmap/private/graph";
        // No where bank agents running on the collector machine are distributed between collector and stand alone EM
        // Portal and Engine are connected to collector and Mediator is connected to Stand Alone EM
        // Filter for agents from collector box where the distributed no where bank agent set is running
        String payLoad =
            "{\"includedVertices\":[],\"excludedVertices\":[],\"showEntry\":true,\"items\":[{\"operator\":\"AND\",\"attributeName\":\"Hostname\",\"values\":[\""
                + collectorRoleHost + "\"],\"btCoverage\":null}]}";

        try {
            EmRestRequest request = new EmRestRequest(urlPart, payLoad);
            IRestResponse<String> response = restClient.process(request);
            if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
                throw new IllegalStateException(
                    "Error in rest request: " + response.getHttpStatus().getStatusCode() + " "
                        + response.getHttpStatus().getReasonPhrase());
            }
            log.info(response.getPayload());
            JsonObject json = (JsonObject) new JsonParser().parse(response.getPayload());
            return json;
        } catch (IOException e) {
            throw new IllegalStateException("IOException", e);
        }
    }

    class EmRestRequest implements IRestRequest<String> {

        private final String url;
        private final String payload;

        EmRestRequest(String url, String payload) {
            this.url = url;
            this.payload = payload;
        }

        @Override
        public HttpRequestBase getRequest() throws IOException {
            HttpPost request = new HttpPost(url);
            // This auth token is inserted to data base in the test bed
            request.addHeader("Authorization",
                "Bearer " + CrossClusterTracesTestBed.ADMIN_AUX_TOKEN);
            request.addHeader(HTTP.CONTENT_TYPE, "application/json");
            request.setEntity(new StringEntity(payload));
            return request;
        }

        @Override
        public IRestResponse<String> getResponse(final CloseableHttpResponse httpResponse, String payload) {
            return new SimpleResponse(httpResponse, payload);
        }
    }
}
