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
package com.ca.apm.test.atc.security;

import com.ca.apm.automation.action.flow.em.config.LocalRealmUsersFlow;
import com.ca.apm.automation.action.flow.em.config.LocalRealmUsersFlowContext;
import com.ca.apm.automation.action.flow.em.config.ModifyDomainsXmlFlow;
import com.ca.apm.automation.action.flow.em.config.ModifyDomainsXmlFlowContext;
import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.test.atc.common.WebViewTestNgTest;
import com.ca.apm.testbed.aca.AcaUniverseRevertTestbed;
import com.ca.apm.testbed.aca.AcaUniverseTestbed;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.testapp.custom.NowhereBankBTRole;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.ca.apm.automation.action.flow.em.config.ModifyDomainsXmlFlowContext.*;
import static org.testng.Assert.assertEquals;

/**
 */
public class ACAUniverseConfigureTest extends WebViewTestNgTest {

    private static final String SUCCESSFULLY_REGISTERED = "Registration successful. Restart EM.";
    private static final String TRANSACTION_TRACE_SESSION_CLW = "trace transactions exceeding 1 ms in agents matching \".*\" for 120 s";
    RestClient restClient = new RestClient();

    @Tas(testBeds = @TestBed(name = AcaUniverseRevertTestbed.class, executeOn = AcaUniverseTestbed.MACHINE_ID_ETC), owner = "bhusu01", size = SizeType.BIG)
    @Test(groups = {"configure", "register"})
    public void configureClustersRevert() throws IOException {
        configureClusters();
    }

    @Tas(testBeds = @TestBed(name = AcaUniverseTestbed.class, executeOn = AcaUniverseTestbed.MACHINE_ID_ETC), owner = "bhusu01", size = SizeType.BIG)
    @Test(groups = {"configure", "register"})
    public void configureClusters() throws IOException {
        modifyRealms(AcaUniverseTestbed.ROLE_ID_ETC);
        modifyDomains(AcaUniverseTestbed.ROLE_ID_ETC);
        modifyUsers(AcaUniverseTestbed.ROLE_ID_ETC);
        modifyDomains(AcaUniverseTestbed.ROLE_ID_ETC_COLLECTOR);
        modifyUsers(AcaUniverseTestbed.ROLE_ID_ETC_COLLECTOR);
        startEM(AcaUniverseTestbed.ROLE_ID_MOM_COLLECTOR_LOCAL);
        startEM(AcaUniverseTestbed.ROLE_ID_MOM_COLLECTOR_REMOTE);
        startEM(AcaUniverseTestbed.ROLE_ID_ETC_COLLECTOR);
        startEM(AcaUniverseTestbed.ROLE_ID_STANDALONE);
        startEM(AcaUniverseTestbed.ROLE_ID_MOM);
        startEM(AcaUniverseTestbed.ROLE_ID_ETC);
        String etcHost = envProperties.getMachineHostnameByRoleId(AcaUniverseTestbed.ROLE_ID_ETC);
        String standaloneHost = envProperties.getMachineHostnameByRoleId(AcaUniverseTestbed.ROLE_ID_STANDALONE);
        String momHost = envProperties.getMachineHostnameByRoleId(AcaUniverseTestbed.ROLE_ID_MOM);

        String standAloneToken = generateProviderToken(etcHost);
        // Register Stand alone with ETC and shutdown
        String registrationResult =
            registerProviderWithAgc(standaloneHost, etcHost, standAloneToken);
        assertEquals(registrationResult, SUCCESSFULLY_REGISTERED);
        stopEM(AcaUniverseTestbed.ROLE_ID_ETC, AcaUniverseTestbed.ROLE_ID_STANDALONE);

        String momToken = generateProviderToken(etcHost);
        // Register Mom with ETC and shutdown
        registrationResult = registerProviderWithAgc(momHost, etcHost, momToken);
        assertEquals(registrationResult, SUCCESSFULLY_REGISTERED);
        stopEM(AcaUniverseTestbed.ROLE_ID_ETC, AcaUniverseTestbed.ROLE_ID_MOM);

        // Restart Standalone
        startEM(AcaUniverseTestbed.ROLE_ID_STANDALONE);
        startEM(AcaUniverseTestbed.ROLE_ID_MOM);

        runSerializedCommandFlowFromRole(AcaUniverseTestbed.ROLE_ID_ETC, EmRole.ENV_START_WEBVIEW);
        runSerializedCommandFlowFromRole(AcaUniverseTestbed.ROLE_ID_MOM, EmRole.ENV_START_WEBVIEW);
        runSerializedCommandFlowFromRole(AcaUniverseTestbed.ROLE_ID_STANDALONE, EmRole.ENV_START_WEBVIEW);

        startAllAgents();

        // Start transaction trace session
        ClwUtils etcCLW = utilities.createClwUtils(AcaUniverseTestbed.ROLE_ID_ETC);
        ClwUtils momCLW = utilities.createClwUtils(AcaUniverseTestbed.ROLE_ID_MOM);
        ClwUtils standaloneCLW = utilities.createClwUtils(AcaUniverseTestbed.ROLE_ID_STANDALONE);

        etcCLW.getClwRunner().runClw(TRANSACTION_TRACE_SESSION_CLW);
        momCLW.getClwRunner().runClw(TRANSACTION_TRACE_SESSION_CLW);
        standaloneCLW.getClwRunner().runClw(TRANSACTION_TRACE_SESSION_CLW);
    }

    private void modifyRealms(String roleId) throws IOException {
        String emBaseDir = envProperties.getRolePropertyById(roleId, EmRole.ENV_PROPERTY_INSTALL_DIR);
        String realmsResource = "/com/ca/tas/test/em/aca/realmsWithLdap.xml";
        File configDir = new File(emBaseDir, "config");
        File realms = new File(configDir, "realms.xml");

        File realmsBackup = new File(configDir, "realmsOriginal.xml");
        Files.copy(realms, realmsBackup);
        // Copy realms file
        InputStream in = ACAUniverseConfigureTest.class.getResourceAsStream(realmsResource);
        FileOutputStream fileOutputStream = new FileOutputStream(realms, false);
        IOUtils.copy(in, fileOutputStream);
        in.close();
        fileOutputStream.close();
    }

    private void modifyUsers(String roleId) {
        String emBaseDir = envProperties.getRolePropertyById(roleId, EmRole.ENV_PROPERTY_INSTALL_DIR);
        String machineID = envProperties.getMachineIdByRoleId(roleId);
        LocalRealmUsersFlowContext usersFlowContext = createUserFlowContext(emBaseDir);
        runFlowByMachineId(machineID, LocalRealmUsersFlow.class, usersFlowContext);
    }

    private void modifyDomains(String roleId) {
        String emBaseDir = envProperties.getRolePropertyById(roleId, EmRole.ENV_PROPERTY_INSTALL_DIR);
        String machineID = envProperties.getMachineIdByRoleId(roleId);
        // Configure Domains for EM
        ModifyDomainsXmlFlowContext
            domainsFlowContext = createDomainFlowContext(emBaseDir);
        runFlowByMachineId(machineID, ModifyDomainsXmlFlow.class, domainsFlowContext);
    }

    private void stopEM(String roleIdLocal, String roleIdRemote) {
        ClwRunner localClwRunner = utilities.createLocalClwRunner(roleIdLocal);
        ClwRunner remoteClwRunner = utilities.createRemoteClwRunner(roleIdLocal,roleIdRemote);
        utilities.createEmUtils().stopRemoteEm(localClwRunner,remoteClwRunner);
    }

    private void startEM(String roleId) {
        runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_EM);
    }

    private void startAllAgents() {
        startNowhereBank(AcaUniverseTestbed.ROLE_ID_ETC_NWB);
        startNowhereBank(AcaUniverseTestbed.ROLE_ID_COLLECTOR_NWB);
        startNowhereBank(AcaUniverseTestbed.ROLE_ID_MOM_NWB);
    }

    /**
     * Starts all 5 nowherebank components on the specified machine
     *
     * @param roleId
     */
    private void startNowhereBank(String roleId) {
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.MESSAGING_SERVER_01);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_ENGINE_02);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_MEDIATOR_03);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_PORTAL_04);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_GENERATOR_05);
    }

    private ModifyDomainsXmlFlowContext createDomainFlowContext(String emBaseDir) {
        Builder domainsFlowContextBuilder =
            new LinuxBuilder().emBase(emBaseDir);
        Domain engineDomain = new Domain("EngineDomain");
        engineDomain.addAgentSpecifier("(.*)Engine");
        Domain mediatorDomain = new Domain("MediatorDomain");
        mediatorDomain.addAgentSpecifier("(.*)Mediator");
        Domain portalDomain = new Domain("PortalDomain");
        portalDomain.addAgentSpecifier("(.*)Portal");
        engineDomain.addGrant(new Grant(Grant.Principal.USER,"reader","read"));
        engineDomain.addGrant(new Grant(Grant.Principal.GROUP,"readers","read"));
        mediatorDomain.addGrant(new Grant(Grant.Principal.USER,"editor","write"));
        mediatorDomain.addGrant(new Grant(Grant.Principal.GROUP,"editors","write"));
        portalDomain.addGrant(new Grant(Grant.Principal.USER,"manager","full"));
        portalDomain.addGrant(new Grant(Grant.Principal.GROUP,"managers","full"));
        portalDomain.addGrant(new Grant(Grant.Principal.USER,"toggler","full"));
        portalDomain.addGrant(new Grant(Grant.Principal.GROUP,"togglers","full"));
        domainsFlowContextBuilder.domain(engineDomain);
        domainsFlowContextBuilder.domain(mediatorDomain);
        domainsFlowContextBuilder.domain(portalDomain);

        Domain superDomain = new Domain("SuperDomain");
        superDomain.addAgentSpecifier("(.*)");
        superDomain.addGrant(new Grant(Grant.Principal.GROUP,"Admin","full"));
        superDomain.addGrant(new Grant(Grant.Principal.USER,"Guest","read"));
        superDomain.addGrant(new Grant(Grant.Principal.USER, AcaUniverseTestbed.TOKEN_ADMIN,"full"));
        domainsFlowContextBuilder.domain(superDomain);

        return domainsFlowContextBuilder.build();
    }

    private LocalRealmUsersFlowContext createUserFlowContext(String emBaseDir) {
        LocalRealmUsersFlowContext.Builder usersFlowContextBuilder =
            new LocalRealmUsersFlowContext.LinuxBuilder().emBase(emBaseDir);
        usersFlowContextBuilder.user("reader");
        usersFlowContextBuilder.user("editor");
        usersFlowContextBuilder.user("manager");
        usersFlowContextBuilder.user("toggler");
        usersFlowContextBuilder.user("ghost");
        usersFlowContextBuilder.user(AcaUniverseTestbed.TOKEN_ADMIN);
        usersFlowContextBuilder.group("readers", "reader");
        usersFlowContextBuilder.group("editors", "editor");
        usersFlowContextBuilder.group("managers", "manager");
        usersFlowContextBuilder.group("togglers", "toggler");
        usersFlowContextBuilder.group("readers", "ghost");
        return usersFlowContextBuilder.build();
    }

    private String registerProviderWithAgc(String emHost, String etcHost, String etcToken) {
        String payload =
            "{" + "\"agcToken\": \"" + etcToken + "\","
                + "\"agcUrl\" : \"http://" + etcHost + ":8081\","
                + "\"agcWebviewUrl\" : \"http://" + etcHost + ":8082\","
                + "\"url\" : \"http://" + emHost + ":8081\","
                + "\"webviewUrl\" : \"http://" + emHost + ":8082\","
                + "\"validation\" : false"
                + "}";
        final String urlPart = "http://" + emHost + ":8081/apm/appmap/private/registration";

        try {
            EmRestRequest request = new EmRestRequest(urlPart, payload);
            IRestResponse<String> response = restClient.process(request);
            if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
                throw new IllegalStateException("Error in rest request: "
                    + response.getHttpStatus().getStatusCode() + " "
                    + response.getHttpStatus().getReasonPhrase());
            }
            JsonObject json = (JsonObject)new JsonParser().parse(response.getContent());
            return json.get("message").getAsString();
        } catch (IOException e) {
            throw new IllegalStateException("IOException", e);
        }
    }

    private String generateProviderToken(String etcHost) {
        final String urlPart = "http://" + etcHost + ":8081/apm/appmap/private/token";

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
            JsonObject json = (JsonObject)new JsonParser().parse(response.getContent());
            return json.get("token").getAsString();
        } catch (IOException e) {
            throw new IllegalStateException("IOexception", e);
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
            HttpPost request  = new HttpPost(url);
            request.addHeader("Authorization", "Bearer " + AcaUniverseTestbed.ADMIN_AUX_TOKEN);
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
