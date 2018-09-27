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
package com.ca.apm.test.em.transactiontrace.ccc;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.test.atc.common.WebViewTestNgTest;
import com.ca.apm.testbed.ccc.FutureAgentTestbed;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.testapp.custom.NowhereBankBTRole;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests agents reporting in future time
 * <p/>
 * We follow an elaborate method to modify the time on the TAS machines because, if we run a command
 * to modify the time on that machine, that run command flow fails because the flow detects that it
 * timed out due to the change in system time. Changing time on the machine where the test is
 * launched also causes the main flow to fail on time out, so, we use the driver machine to launch
 * the test.
 *
 * @author bhusu01
 */
public class FutureAgentTest extends WebViewTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FutureAgentTest.class);

    private static final String ONE_MONTH_AHEAD = "1month";
    private static final String ONE_WEEK_AHEAD = "1week";
    private static final String TRANSACTION_TRACE_SESSION_CLW =
        "trace transactions exceeding 1 ms in agents matching \".*\" for 240 s";
    private static final String TEMP_FILE = "/tmp/paramAhead.py";
    private static final String TIME_AHEAD_PY_FILE = "/tmp/ahead.py";

    private static final String hostNameVar = "hostName";
    private static final String timeOffsetVar = "timeOffset";

    private static final String[] PYTHON_FILE_CONTENT =
        new String[] {"import paramiko", "ssh = paramiko.SSHClient()", "ssh"
            + ".set_missing_host_key_policy(paramiko.AutoAddPolicy())",
            "ssh.connect('${" + hostNameVar + "}', username='root', password='Lister@123')",
            "ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command('date -s \"`date -d \"${"
                + timeOffsetVar + "}\"`\"')", "print ssh_stdout.readlines()"};
    private static final String AGENT_CONFIG_EM_HOST =
        "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT";
    private static final String GRAPH_REST_ENDPOINT_FORMATTER =
        "http://%s:%d/apm/appmap/private/graph";
    private static final JsonPrimitive BUSINESS_TRANSACTION_TYPE =
        new JsonPrimitive("BUSINESSTRANSACTION");

    private RestClient restClient;

    /**
     * Tests if EM can handle agents reporting at a future date.
     * <p/>
     * Have agent reporting at a date one month in the future. Verify that appmap is generated.
     * Advance time on EM and agent by one week and verify map is generated
     * EM will be restarted once and it is important to verify the map generation after the restart.
     */
    @Tas(testBeds = @TestBed(name = FutureAgentTestbed.class, executeOn = FutureAgentTestbed
        .MACHINE_ID_DRIVER), owner = "bhusu01", size = SizeType.MEDIUM)
    @Test(groups = {"future-agent"})
    public void testAgentFromFuture() throws IOException {
        restClient = new RestClient();
        // Set time ahead by a month
        setMachineTimeAhead(FutureAgentTestbed.MACHINE_ID_DRIVER, FutureAgentTestbed
            .ROLE_ID_NWB_FUTURE, ONE_MONTH_AHEAD);
        // Point remote NWB to the EM
        configureRemoteNWB(FutureAgentTestbed.ROLE_ID_NWB_FUTURE, FutureAgentTestbed.ROLE_EM);
        startAllComponents();
        verifyMapComponents();
        stopAllComponents();
        // Set both of them ahead by a week
        setMachineTimeAhead(FutureAgentTestbed.MACHINE_ID_DRIVER, FutureAgentTestbed
            .ROLE_ID_NWB_FUTURE, ONE_WEEK_AHEAD);
        setMachineTimeAhead(FutureAgentTestbed.MACHINE_ID_DRIVER, FutureAgentTestbed.ROLE_EM,
            ONE_WEEK_AHEAD);
        startAllComponents();
        verifyMapComponents();
    }

    /**
     * Verify we have map components from the agent reporting in future time
     *
     * @throws IOException
     */
    private void verifyMapComponents() throws IOException {
        String emHostName = envProperties.getMachineHostnameByRoleId(FutureAgentTestbed.ROLE_EM);
        String futureHost =
            envProperties.getMachineHostnameByRoleId(FutureAgentTestbed.ROLE_ID_NWB_FUTURE);
        String filteredGraph =
            fetchFilteredGraph(emHostName, futureHost, FutureAgentTestbed.ADMIN_AUX_TOKEN);
        JsonObject json = (JsonObject) new JsonParser().parse(filteredGraph);
        JsonArray verticesArray = json.getAsJsonArray("vertices");

        // Assert.assertEquals(verticesArray.size(), 38, "Expecting all vertices");
        // The value is fluctuating between 37 and 38 between runs.
        Assert.assertTrue(verticesArray.size() > 36, "Expecting at least 36 vertices");

        // Verify that all returned vertices have the attribute hostname with the correct value
        for (JsonElement vertexElement : verticesArray) {
            Assert.assertTrue(vertexElement.isJsonObject());
            JsonObject vertex = vertexElement.getAsJsonObject();
            JsonElement attributes = vertex.get("attributes");
            Assert.assertTrue(attributes.isJsonObject());
            JsonElement hostName = attributes.getAsJsonObject().get("hostname");
            // Business transaction vertices are exception
            if (hostName == null) {
                JsonElement type = attributes.getAsJsonObject().get("type");
                Assert.assertTrue(type.isJsonArray());
                JsonArray types = type.getAsJsonArray();
                Assert.assertTrue(types.contains(BUSINESS_TRANSACTION_TYPE));
                continue;
            }
            Assert.assertTrue(hostName.isJsonArray());
            JsonArray hostNames = hostName.getAsJsonArray();
            Assert.assertTrue(hostNames.contains(new JsonPrimitive(futureHost)));
        }
    }

    private String fetchFilteredGraph(String emHostName, String futureHost, String token) throws
        IOException {
        String retAsString;
        String url = String.format(GRAPH_REST_ENDPOINT_FORMATTER, emHostName, 8081);
        String payLoad =
            "{\"includedVertices\":[],\"excludedVertices\":[],\"showEntry\":true,"
                + "\"items\":[{\"operator\":\"AND\",\"attributeName\":\"hostname\",\"values\":[\""
                + futureHost + "\"],\"btCoverage\":null}]}";
        EmRestRequest filteredGraphRequest = new EmRestRequest(url, payLoad, token);
        IRestResponse<String> response = restClient.process(filteredGraphRequest);
        if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
            throw new IllegalStateException(
                "Error in rest request: " + response.getHttpStatus().getStatusCode() + " "
                    + response.getHttpStatus().getReasonPhrase());
        }
        retAsString = response.getContent();
        return retAsString;
    }

    /**
     * Stops EM, Webview and both the NoWhereBank agents
     */
    private void stopAllComponents() {
        runSerializedCommandFlowFromRole(FutureAgentTestbed.ROLE_EM, EmRole.ENV_STOP_EM);
        runSerializedCommandFlowFromRole(FutureAgentTestbed.ROLE_EM, EmRole.ENV_STOP_WEBVIEW);
        runSerializedCommandFlowFromRole(FutureAgentTestbed.ROLE_ID_NWB_LOCAL, NowhereBankBTRole
            .STOP_ALL_COMPONENTS);
        runSerializedCommandFlowFromRole(FutureAgentTestbed.ROLE_ID_NWB_FUTURE, NowhereBankBTRole
            .STOP_ALL_COMPONENTS);
    }

    /**
     * Starts EM, Webview, both the NoWhereBank agents and a transaction trace session
     */
    private void startAllComponents() {
        runSerializedCommandFlowFromRole(FutureAgentTestbed.ROLE_EM, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(FutureAgentTestbed.ROLE_EM, EmRole.ENV_START_WEBVIEW);
        startNowhereBankByRoleId(FutureAgentTestbed.ROLE_ID_NWB_LOCAL);
        startNowhereBankByRoleId(FutureAgentTestbed.ROLE_ID_NWB_FUTURE);
        runCLW(FutureAgentTestbed.ROLE_EM, TRANSACTION_TRACE_SESSION_CLW);
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
        runCommandFlowByMachineId(machineId, permitFile);
    }

    private void configureRemoteNWB(String roleId, String emRoleId) {
        String emHostName = envProperties.getMachineHostnameByRoleId(emRoleId);
        String nwbMachineID = envProperties.getMachineIdByRoleId(roleId);
        for (String component : NowhereBankBTRole.NWB_AGENT_NAMES) {
            configureAgent(emHostName, component, nwbMachineID, roleId);
        }
    }

    /**
     * Configures the chosen agentName on the chosen machineID to report to the host on the chosen
     * host machine. The port is not modified and left as default - 5001.
     *
     * @param hostName
     * @param agentName
     * @param emMachineID
     */
    private void configureAgent(String hostName, String agentName, String emMachineID, String
        roleId) {
        String installDirPath =
            envProperties.getRolePropertiesById(roleId).getProperty(NowhereBankBTRole.INSTALL_DIR);
        String profileFileFormatter =
            envProperties.getRolePropertiesById(roleId).getProperty(NowhereBankBTRole
                .PROFILE_FILE_FORMATTER);

        Map<String, String> replacePairsConfig = new HashMap<>();
        replacePairsConfig.put(AGENT_CONFIG_EM_HOST, hostName);

        ConfigureFlowContext.Builder builder = new ConfigureFlowContext.Builder();
        builder.configurationMap(
            installDirPath + String.format(profileFileFormatter, agentName), replacePairsConfig);

        runConfigureFlowByMachineIdAsync(emMachineID, builder.build());
    }

    /**
     * Modifies the time on the machine with the specified role by the specified time offset
     * <p/>
     * Time on the driverMachineID should not be modified.
     *
     * @param roleId
     * @param timeDifference
     */
    private void setMachineTimeAhead(String driverMachineID, String roleId, String timeDifference) {
        String hostName = envProperties.getMachineHostnameByRoleId(roleId);

        FileCreatorFlowContext tempFileFlowContext =
            new FileCreatorFlowContext.Builder().destinationPath(TEMP_FILE).fromData(Arrays
                .asList(PYTHON_FILE_CONTENT)).build();
        runFlowByMachineId(driverMachineID, FileCreatorFlow.class, tempFileFlowContext);

        FileCreatorFlowContext pySshFlowContext =
            new FileCreatorFlowContext.Builder().destinationPath(TIME_AHEAD_PY_FILE).fromFile
                (TEMP_FILE).substitution(hostNameVar, hostName).substitution(timeOffsetVar,
                timeDifference).build();
        runFlowByMachineId(driverMachineID, FileCreatorFlow.class, pySshFlowContext);

        RunCommandFlowContext fileExecutableFlowContext =
            new RunCommandFlowContext.Builder("chmod").args(Arrays.asList(new String[] {"755",
                TIME_AHEAD_PY_FILE})).build();
        runCommandFlowByMachineId(driverMachineID, fileExecutableFlowContext);

        RunCommandFlowContext changeTimeFlowContext =
            new RunCommandFlowContext.Builder("python").args(Arrays.asList(new String[]
                {TIME_AHEAD_PY_FILE})).build();
        runCommandFlowByMachineId(driverMachineID, changeTimeFlowContext);
    }

    /**
     * Starts all 5 nowherebank components on the specified machine
     *
     * @param roleId
     */
    private void startNowhereBankByRoleId(String roleId) {
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.MESSAGING_SERVER_01);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_ENGINE_02);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_MEDIATOR_03);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_PORTAL_04);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_GENERATOR_05);
    }

    class EmRestRequest implements IRestRequest<String> {

        private final String url;
        private final String payload;
        private final String authToken;

        EmRestRequest(String url, String payload, String authToken) {
            this.url = url;
            this.payload = payload;
            this.authToken = authToken;
        }

        @Override
        public HttpRequestBase getRequest() throws IOException {
            HttpPost request = new HttpPost(url);
            request.addHeader("Authorization", "Bearer " + authToken);
            request.addHeader(HTTP.CONTENT_TYPE, "application/json");
            request.setEntity(new StringEntity(payload));
            return request;
        }

        @Override
        public IRestResponse<String> getResponse(final CloseableHttpResponse httpResponse, String
            payload) {
            return new SimpleResponse(httpResponse, payload);
        }
    }
}
