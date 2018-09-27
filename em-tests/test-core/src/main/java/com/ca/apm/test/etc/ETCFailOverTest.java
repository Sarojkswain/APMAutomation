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
package com.ca.apm.test.etc;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.etc.ConfigureProviderFlow;
import com.ca.apm.automation.action.flow.etc.ConfigureProviderFlowContext;
import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.testbed.etc.ETCFailOverTestbed;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HTTP;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author bhusu01
 */
public class ETCFailOverTest extends TasTestNgTest {

    private static final String SHUTDOWN = "shutdown";
    private static final String SHARE_NAME = "failOver";
    private static final String MOUNT_DRIVE = "P:";
    private static final String SECONDARY_STARTED_LOG = "Acquiring primary lock...";
    private static final String FULL_PERMISSION = "/GRANT:Everyone,FULL";
    private static final String TEMP_FILE = "C:\\share.bat";
    private static final String MOUNT_COMMAND =
        "net use " + MOUNT_DRIVE + " %s /USER:Administrator Lister@123";
    private static final String FOLLOWER_ENDPOINT_URL_FORMATTER =
        "http://%s:%d/apm/appmap/private/follower";
    private static final String REGISTERING = "REGISTERING";
    private static final String JOINING = "JOINING";
    private static final String ONLINE = "ONLINE";
    private static final String NOT_RESPONDING = "NOT RESPONDING";

    private RestClient restClient;

    @BeforeTest
    public void initRestClient() {
        restClient = new RestClient();
    }

    /**
     * Configures ETC machine for fail over.
     * Registers provider with ETC and verifies follower status
     *
     * TODO: Perform fail over and make sure status comes back to ONLINE after fail over
     *
     */
    @Tas(testBeds = @TestBed(name = ETCFailOverTestbed.class, executeOn = ETCFailOverTestbed
        .MACHINE_ID_ETC), owner = "bhusu01", size = SizeType.MEDIUM)
    @Test(groups = {"config-failover"})
    public void testETCFailOver() throws IOException {
        runSerializedCommandFlowFromRole(ETCFailOverTestbed.ROLE_ID_ETC, EmRole.ENV_START_EM);
        startFailOverEM(ETCFailOverTestbed.ROLE_ID_ETC, ETCFailOverTestbed.MACHINE_ID_FAIL_OVER);
        String providerHost =
            envProperties.getMachineHostnameByRoleId(ETCFailOverTestbed.ROLE_ID_STANDALONE);
        runSerializedCommandFlowFromRole(ETCFailOverTestbed.ROLE_ID_STANDALONE, EmRole
            .ENV_START_EM);
        String status = fetchFollowerStatus(ETCFailOverTestbed.ROLE_ID_ETC, providerHost);
        Assert.assertEquals(status,"", "Expected no followers");
        // Configure provider with master
        String etcHost = envProperties.getMachineHostnameByRoleId(ETCFailOverTestbed.ROLE_ID_ETC);
        ConfigureProviderFlowContext providerFlowContext =
            new ConfigureProviderFlowContext.Builder().masterHost(etcHost).providerHost
                (providerHost).authToken(ETCFailOverTestbed.ADMIN_AUX_TOKEN).build();
        runFlowByMachineId(ETCFailOverTestbed.MACHINE_ID_ETC, ConfigureProviderFlow.class,
            providerFlowContext);
        status = fetchFollowerStatus(ETCFailOverTestbed.ROLE_ID_ETC, providerHost);
        Assert.assertEquals(status,JOINING);
        // Restart stand alone. Stop it first
        // EmRole.ENV_STOP_EM on windows has some problem not resolved in TAS at the time of
        // writing this test
        runCLW(ETCFailOverTestbed.ROLE_ID_STANDALONE, SHUTDOWN);
        // runSerializedCommandFlowFromRole(MinimalETCTestBed.ROLE_ID_STANDALONE, EmRole
        // .ENV_STOP_EM);
        // Give some time for EM shutdown and start ETC webview meanwhile
        runSerializedCommandFlowFromRole(ETCFailOverTestbed.ROLE_ID_ETC, EmRole.ENV_START_WEBVIEW);
        // Start stand alone
        runSerializedCommandFlowFromRole(ETCFailOverTestbed.ROLE_ID_STANDALONE, EmRole
            .ENV_START_EM);
        runSerializedCommandFlowFromRole(ETCFailOverTestbed.ROLE_ID_STANDALONE, EmRole
            .ENV_START_WEBVIEW);
        status = fetchFollowerStatus(ETCFailOverTestbed.ROLE_ID_ETC, providerHost);
        Assert.assertEquals(status,ONLINE);
    }

    /**
     * Configures provider machine for fail over.
     * Registers provider with ETC and verifies follower status
     *
     * TODO: Perform fail over and make sure status comes back to ONLINE after fail over
     *
     */
    @Tas(testBeds = @TestBed(name = ETCFailOverTestbed.class, executeOn = ETCFailOverTestbed.MACHINE_ID_ETC), owner = "bhusu01", size = SizeType.MEDIUM)
    @Test(groups = {"config-failover"})
    public void testProviderFailOver() throws IOException {
        runSerializedCommandFlowFromRole(ETCFailOverTestbed.ROLE_ID_STANDALONE, EmRole.ENV_START_EM);
        startFailOverEM(ETCFailOverTestbed.ROLE_ID_STANDALONE, ETCFailOverTestbed.MACHINE_ID_FAIL_OVER);
        runSerializedCommandFlowFromRole(ETCFailOverTestbed.ROLE_ID_ETC, EmRole.ENV_START_EM);
        // Configure provider with master
        String providerHost =
            envProperties.getMachineHostnameByRoleId(ETCFailOverTestbed.ROLE_ID_STANDALONE);
        String etcHost = envProperties.getMachineHostnameByRoleId(ETCFailOverTestbed.ROLE_ID_ETC);
        String status = fetchFollowerStatus(ETCFailOverTestbed.ROLE_ID_ETC, providerHost);
        Assert.assertEquals(status,"", "Expected no followers");
        ConfigureProviderFlowContext providerFlowContext =
            new ConfigureProviderFlowContext.Builder().masterHost(etcHost).providerHost(providerHost).authToken(ETCFailOverTestbed.ADMIN_AUX_TOKEN).build();
        runFlowByMachineId(ETCFailOverTestbed.MACHINE_ID_ETC, ConfigureProviderFlow.class, providerFlowContext);
        status = fetchFollowerStatus(ETCFailOverTestbed.ROLE_ID_ETC, providerHost);
        Assert.assertEquals(status,JOINING);
        // Restart stand alone. Stop it first
        // EmRole.ENV_STOP_EM on windows has some problem not resolved in TAS at the time of
        // writing this test
        runCLW(ETCFailOverTestbed.ROLE_ID_STANDALONE, SHUTDOWN);
        // runSerializedCommandFlowFromRole(MinimalETCTestBed.ROLE_ID_STANDALONE, EmRole
        // .ENV_STOP_EM);
        // Give some time for EM shutdown and start ETC webview meanwhile
        runSerializedCommandFlowFromRole(ETCFailOverTestbed.ROLE_ID_ETC, EmRole.ENV_START_WEBVIEW);
        // Start stand alone
        runSerializedCommandFlowFromRole(ETCFailOverTestbed.ROLE_ID_STANDALONE, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(ETCFailOverTestbed.ROLE_ID_STANDALONE, EmRole.ENV_START_WEBVIEW);
        status = fetchFollowerStatus(ETCFailOverTestbed.ROLE_ID_ETC, providerHost);
        Assert.assertEquals(status,ONLINE);
    }

    /**
     * On EM machine, share the em folder on network
     * On fail over machine, mount the shared folder
     * Start the fail over EM
     *
     * @param emRole
     * @param failOverMachineID
     */
    private void startFailOverEM(String emRole, String failOverMachineID) {
        String emMachineID = envProperties.getMachineIdByRoleId(emRole);
        String emHost = envProperties.getMachineHostnameByRoleId(emRole);
        String etcInstallDir =
            envProperties.getRolePropertyById(emRole, EmRole.ENV_PROPERTY_INSTALL_DIR);
        String dbFilePath = etcInstallDir + "/config/tess-db-cfg.xml";
        String backUpFilePath = etcInstallDir + "/config/tess-db-cfg-backup.xml";
        // Modify tess-db-cfg.xml to have host name instead of 127.0.0.1
        FileCreatorFlowContext backupFileContext =
            new FileCreatorFlowContext.Builder().destinationPath(backUpFilePath).fromFile
                (dbFilePath).build();
        runFlowByMachineId(emMachineID, FileCreatorFlow.class, backupFileContext);
        FileCreatorFlowContext modifyDBFileContext =
            new FileCreatorFlowContext.Builder().destinationPath(dbFilePath).fromFile
                (backUpFilePath).replace("127.0.0.1", emHost).build();
        runFlowByMachineId(emMachineID, FileCreatorFlow.class, modifyDBFileContext);
        // Share EM directory on network
        RunCommandFlowContext shareEMFolder =
            new RunCommandFlowContext.Builder("net").args(Arrays.asList(new String[] {"share",
                SHARE_NAME + "=" + etcInstallDir, FULL_PERMISSION})).build();
        runFlowByMachineId(emMachineID, RunCommandFlow.class, shareEMFolder);
        // Mount network share to local drive
        String shareUNCPath = "\\\\" + emHost + "\\" + SHARE_NAME;
        String mountCommand = String.format(MOUNT_COMMAND, shareUNCPath);
        // Running the command directly complains about user detail, so, run command from a file
        FileCreatorFlowContext fileCreatorFlowContext =
            new FileCreatorFlowContext.Builder().destinationPath(TEMP_FILE).fromData(Arrays
                .asList(mountCommand)).build();
        runFlowByMachineId(failOverMachineID, FileCreatorFlow.class, fileCreatorFlowContext);
        RunCommandFlowContext mountShareFolder =
            new RunCommandFlowContext.Builder(TEMP_FILE).build();
        runFlowByMachineId(failOverMachineID, RunCommandFlow.class, mountShareFolder);
        // Start fail over EM. It should print the expected log to terminate
        RunCommandFlowContext startFailOverEM =
            new RunCommandFlowContext.Builder(EmRole.Builder.INTROSCOPE_EXECUTABLE).workDir
                (MOUNT_DRIVE).terminateOnMatch(SECONDARY_STARTED_LOG).build();
        runFlowByMachineId(failOverMachineID, RunCommandFlow.class, startFailOverEM);
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

    /**
     * Fetches status of the specified follower. If follower is not found in the response, empty
     * string is returned.
     *
     * @param etcRole
     * @return
     * @throws IOException
     */
    private String fetchFollowerStatus(String etcRole, String followerHost) throws IOException {
        String etcHost = envProperties.getMachineHostnameByRoleId(etcRole);
        String endpoint = String.format(FOLLOWER_ENDPOINT_URL_FORMATTER, etcHost, 8081);
        EmRestRequest followerRequest = new EmRestRequest(endpoint, "", ETCFailOverTestbed
            .ADMIN_AUX_TOKEN);
        IRestResponse<String> response = restClient.process(followerRequest);
        if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
            throw new IllegalStateException(
                "Error in rest request: " + response.getHttpStatus().getStatusCode() + " "
                    + response.getHttpStatus().getReasonPhrase());
        }
        String followerJson = response.getContent();
        JsonObject json = (JsonObject) new JsonParser().parse(followerJson);
        JsonElement followers = json.get("followers");
        Assert.assertTrue(followers.isJsonArray());
        JsonArray followersArray = followers.getAsJsonArray();
        for(JsonElement follower: followersArray) {
            Assert.assertTrue(follower.isJsonObject());
            JsonObject followerInfo = follower.getAsJsonObject();
            if(followerInfo.get("id").getAsString().contains(followerHost)) {
                return followerInfo.get("status").getAsString();
            }
        }
        return "";
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
