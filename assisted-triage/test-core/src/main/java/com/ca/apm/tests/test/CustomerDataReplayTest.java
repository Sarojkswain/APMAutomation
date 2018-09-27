package com.ca.apm.tests.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.tests.testbed.CustomerDataReplayTestbed;
import com.ca.apm.tests.utils.EmRestRequest;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CustomerDataReplayTest extends TasTestNgTest {

    private final String REGISTRATION_RESULT = "Registration successful. Restart EM.";
    private final Logger log = LoggerFactory.getLogger(getClass());
    RestClient restClient = new RestClient();

    // This test is to register Standalone EM to AGC for replaying customer data

    @Tas(testBeds = @TestBed(name = CustomerDataReplayTestbed.class, executeOn = "emMachine1"), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"DataReplay_Test"})
    public void registerFirstStandalone() throws Exception {

        registerEM("agc_em1", "emRole1");
    }

    @Tas(testBeds = @TestBed(name = CustomerDataReplayTestbed.class, executeOn = "emMachine2"), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"DataReplay_Test"})
    public void registerSecondStandalone() throws Exception {

        registerEM("agc_em2", "emRole2");
    }

    @Tas(testBeds = @TestBed(name = CustomerDataReplayTestbed.class, executeOn = "emMachine3"), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"DataReplay_Test"})
    public void registerThisrdStandalone() throws Exception {

        registerEM("agc_em3", "emRole3");
    }

    @Tas(testBeds = @TestBed(name = CustomerDataReplayTestbed.class, executeOn = "emMachine4"), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"DataReplay_Test"})
    public void registerFourthStandalone() throws Exception {

        registerEM("agc_em4", "emRole4");
    }

    @Tas(testBeds = @TestBed(name = CustomerDataReplayTestbed.class, executeOn = "emMachine5"), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"DataReplay_Test"})
    public void registerFifthStandalone() throws Exception {

        registerEM("agc_em5", "emRole5");
    }

    public void registerEM(String agc_em, String emRole) throws InterruptedException {

        String agcHost = envProperties.getMachineHostnameByRoleId(agc_em);
        String standaloneHost = envProperties.getMachineHostnameByRoleId(emRole);

        checkWebview(agc_em);

        // register STANDALONE
        String agcToken = generateAgcToken(agcHost);
        log.info("AGC token for STANDALONE: " + agcToken);
        String resultStandalone = registerMomtoAgc(standaloneHost, agcHost, agcToken);
        assertEquals(resultStandalone, REGISTRATION_RESULT);
        log.info(standaloneHost + ": " + resultStandalone);

        // restart STANDALONE
        EmUtils emUtils = utilities.createEmUtils();
        ClwRunner standaloneClwRunner = utilities.createClwUtils(emRole).getClwRunner();
        standaloneClwRunner.runClw("shutdown");
        try {
            emUtils.stopLocalEm(standaloneClwRunner, emRole);
        } catch (Exception e) {
            log.warn("EM was not stopped properly!");
        }
        startEmAndWebview(emRole);
        log.info(standaloneHost + " restarted.");

    }

    private void startEmAndWebview(String roleId) throws InterruptedException {
        changeEMHeapSize();
        Thread.sleep(10000);
        runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_WEBVIEW);
        checkWebview(roleId);
    }

    private void changeEMHeapSize() {

        String[] laxFiles = {"Introscope_Enterprise_Manager.lax", "Introscope_WebView.lax"};

        for (String file : laxFiles) {
            try {
                String laxFile =
                    FileUtils.readFileToString(new File(CustomerDataReplayTestbed.EM_INSTALL_DIR
                        + TasBuilder.WIN_SEPARATOR + file));
                String newFile = "";
                if (laxFile.contains("-Xmx1024m")) {
                    newFile = laxFile.replaceAll("Xmx1024m", "Xmx4096m");
                    FileUtils.writeStringToFile(new File(CustomerDataReplayTestbed.EM_INSTALL_DIR
                        + TasBuilder.WIN_SEPARATOR + file), newFile);
                } else {
                    System.out.println(" Heap is already set to 4G");
                }
            }

            catch (IOException e) {
                e.printStackTrace();
                System.out.println(" Failed to increse heap size in LAX file.  ");
                fail();
            }
        }
    }

    private String registerMomtoAgc(String momHost, String agcHost, String agcToken) {
        String payload =
            "{" + "\"agcToken\": \"" + agcToken + "\"," + "\"agcUrl\" : \"http://" + agcHost
                + ":8081\"," + "\"agcWebviewUrl\" : \"http://" + agcHost + ":8082\","
                + "\"url\" : \"http://" + momHost + ":8081\"," + "\"webviewUrl\" : \"http://"
                + momHost + ":8082\"," + "\"validation\" : false" + "}";
        final String urlPart = "http://" + momHost + ":8081/apm/appmap/private/registration";

        try {
            EmRestRequest request = new EmRestRequest(urlPart, payload);
            IRestResponse<String> response = restClient.process(request);
            if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
                throw new IllegalStateException("Error in rest request: "
                    + response.getHttpStatus().getStatusCode() + " "
                    + response.getHttpStatus().getReasonPhrase());
            }
            JsonObject json = (JsonObject) new JsonParser().parse(response.getContent());
            return json.get("message").getAsString();
        } catch (IOException e) {
            throw new IllegalStateException("IOexception", e);
        }
    }

    private String generateAgcToken(String agcHost) {
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/token";

        try {
            EmRestRequest request =
                new EmRestRequest(urlPart,
                    "{\"description\": \"TAS token\", \"expirationDate\": null, \"system\": true}");
            IRestResponse<String> response = restClient.process(request);
            if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
                throw new IllegalStateException("Error in rest request: "
                    + response.getHttpStatus().getStatusCode() + " "
                    + response.getHttpStatus().getReasonPhrase());
            }
            JsonObject json = (JsonObject) new JsonParser().parse(response.getContent());
            return json.get("token").getAsString();
        } catch (IOException e) {
            throw new IllegalStateException("IOexception", e);
        }
    }

    private void checkWebview(String roleId) {
        String wvHost = envProperties.getMachineHostnameByRoleId(roleId);
        String wvPort = envProperties.getRolePropertyById(roleId, "wvPort");

        while (!loadPage("http://" + wvHost + ":" + wvPort)) {
            killWebview(roleId);
            runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_WEBVIEW);
        }
    }

    private boolean loadPage(String pageUrl) {
        try {
            URL url = new URL(pageUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            int responseCode = con.getResponseCode();
            con.disconnect();
            log.info("Response code from Webview: " + Integer.toString(responseCode));
            if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                return true;
            }
        } catch (Exception e) {
            // swallow all
        }
        return false;
    }

    public void killWebview(String roleId) {
        try {
            RunCommandFlowContext runCommandFlowContext =
                new RunCommandFlowContext.Builder("taskkill").args(
                    Arrays.asList("/F", "/T", "/IM", EmRole.Builder.WEBVIEW_EXECUTABLE)).build();
            String machineId = envProperties.getMachineIdByRoleId(roleId);
            runCommandFlowByMachineId(machineId, runCommandFlowContext);
        } catch (Exception e) {
            // swallow all
        }

    }
}
