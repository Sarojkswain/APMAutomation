/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.test.devel;

import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPlugin.Configuration;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPluginImpl;
import com.ca.apm.systemtest.fld.role.ActivitiRole;
import com.ca.apm.systemtest.fld.testbed.WebLogicAgentTestbed;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * Test WebLogic agent deployment using Activiti workflow.
 * 
 * @author jirji01
 *
 */
public class WebLogicAgentTest {

    protected static final Logger log = LoggerFactory.getLogger(WebLogicAgentTest.class);
    private final EnvironmentPropertyContext envProp;

    public WebLogicAgentTest() throws IOException {
        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
    }

    @Tas(
        testBeds = @TestBed(
            name = WebLogicAgentTestbed.class,
            executeOn = WebLogicAgentTestbed.TEST_MACHINE_ID),
        owner = "jirji01",
        size = SizeType.DEBUG,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"fld_agent"})
    public void deploy() throws IOException {
        // Config file
        Configuration config = new Configuration();
        config.buildNumber = "000065";
        config.codeName = "9.7.0-ISCP";
        config.buildId = "9.7.0.0";
        config.platform = SystemUtil.OperatingSystemFamily.Windows;

        config.wlsServerScriptPath =
            "C:\\sw\\wily\\weblogic\\wlserver_10.3\\samples\\domains\\wl_server\\bin\\";
        config.agentInstallDir = "c:\\sw\\testagent";
        config.wlsServerName = envProp.getMachineHostnameByRoleId(WebLogicAgentTestbed.WL_ROLE_ID);
        config.emHost = "lod0389.ca.com";
        config.logs = "c:/sw/testagent";

        //
        WlsPluginImpl plugin = new WlsPluginImpl();

        plugin.installAgent(config);

        // Configure WAS to run with the Agent
        plugin.setAgent(config);
        // Start / Stop WAS
        plugin.startServer(config);
        assertTrue(plugin.isServerRunning("http://localhost:7001", 300000));

        plugin.stopServer(config);

        assertTrue(plugin.isServerStopped("http://localhost:7001", 300000));
    }

    @Tas(
        testBeds = @TestBed(
            name = WebLogicAgentTestbed.class,
            executeOn = WebLogicAgentTestbed.TEST_MACHINE_ID),
        owner = "jirji01",
        size = SizeType.DEBUG,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"fld_agent"})
    public void activitiDeploy() throws Exception {


        // uploade file
        String hostname = envProp.getMachineHostnameByRoleId(WebLogicAgentTestbed.TC_ROLE_ID);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile =
                new HttpPost(
                    new URI("http://" + hostname + ":8080/load-orchestrator-webapp/upload"));

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("field1", "yes", ContentType.TEXT_PLAIN);
            InputStream bpmnStream =
                getClass().getClassLoader().getResourceAsStream(
                    "diagrams/Install WLS agent from TRUSS.bpmn");
            builder.addBinaryBody("processArchive", bpmnStream,
                ContentType.APPLICATION_OCTET_STREAM, "Install WLS agent from TRUSS.bpmn20.xml");
            HttpEntity multipart = builder.build();

            uploadFile.setEntity(multipart);

            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                readHttpResponse(response);
            }
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile =
                new HttpPost(
                    new URI("http://" + hostname + ":8080/load-orchestrator-webapp/api/agent"));

            Path agentsZip = Paths.get(envProp
                .getRolePropertiesById(WebLogicAgentTestbed.ACT_ROLE_ID)
                .getProperty(ActivitiRole.AGENT_DIST));
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody(
                "agentZip",
                Files.newInputStream(agentsZip),
                ContentType.APPLICATION_OCTET_STREAM, agentsZip.getFileName().toString());
            HttpEntity multipart = builder.build();

            uploadFile.setEntity(multipart);

            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                readHttpResponse(response);
            }
        }

        // start workflow
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("processName", "installWlsAgent"));
            nvps.add(new BasicNameValuePair("p1name", "buildNumber"));
            nvps.add(new BasicNameValuePair("p1val", "000078"));
            nvps.add(new BasicNameValuePair("p2name", "codeName"));
            nvps.add(new BasicNameValuePair("p2val", "9.7.0-ISCP"));
            nvps.add(new BasicNameValuePair("p3name", "buildId"));
            nvps.add(new BasicNameValuePair("p3val", "9.7.0.0"));
            nvps.add(new BasicNameValuePair("p4name", "platform"));
            nvps.add(new BasicNameValuePair("p4val", "windows"));
            nvps.add(new BasicNameValuePair("p5name", "wlsServerScriptPath"));
            nvps.add(new BasicNameValuePair("p5val",
                "C:\\sw\\wily\\weblogic\\wlserver_10.3\\samples\\domains\\wl_server\\bin\\"));
            nvps.add(new BasicNameValuePair("p6name", "agentInstallDir"));
            nvps.add(new BasicNameValuePair("p6val", "c:\\sw\\testagent"));
            nvps.add(new BasicNameValuePair("p7name", "wlsServerName"));
            nvps.add(new BasicNameValuePair("p7val", envProp
                .getMachineHostnameByRoleId(WebLogicAgentTestbed.WL_ROLE_ID)));
            nvps.add(new BasicNameValuePair("p8name", "emHost"));
            nvps.add(new BasicNameValuePair("p8val", "lod0389.ca.com"));
            nvps.add(new BasicNameValuePair("p9name", "logs"));
            nvps.add(new BasicNameValuePair("p9val", "c:/sw/testagent"));

            HttpPost startFlow =
                new HttpPost(new URI("http://" + hostname
                    + ":8080/load-orchestrator-webapp/startProcess"));
            startFlow.setEntity(new UrlEncodedFormEntity(nvps));

            try (CloseableHttpResponse response = httpClient.execute(startFlow)) {
                readHttpResponse(response);
            }
        }

        Files.exists(Paths.get(envProp.getRolePropertiesById(WebLogicAgentTestbed.WL_ROLE_ID)
            .getProperty("home"), ""));
        
        assertTrue(Files.exists(Paths.get("c:\\sw\\testagent")), "Is test agent installed.");
        
    }

    private void readHttpResponse(CloseableHttpResponse response) throws IOException {
        HttpEntity responseEntity = response.getEntity();
        if (responseEntity.getContentLength() > 0) {
            BufferedReader br =
                new BufferedReader(new InputStreamReader(responseEntity.getContent()));
            StringBuilder out = new StringBuilder((int) responseEntity.getContentLength());
            String line;
            while ((line = br.readLine()) != null) {
                out.append(line);
                out.append("\n");
            }
            log.info(out.toString());
            br.close();
        }
    }

    protected File fetchDistIntoStagingDir(URL artifactUrl, File stagingDir) throws IOException {

        String tradeServiceAppArifactBasename = FilenameUtils.getBaseName(artifactUrl.getPath());
        File stagedTradeServiceAppArifact = new File(stagingDir, tradeServiceAppArifactBasename);

        log.info("Checking if file {} already exists in staging directory",
            tradeServiceAppArifactBasename);
        if (stagedTradeServiceAppArifact.exists()) {
            log.info("File already exists, download is not required");
        } else {
            log.info("Downloading from {}", artifactUrl);
            FileUtils.copyURLToFile(artifactUrl, stagedTradeServiceAppArifact);
            log.info("Download completed.");
        }

        return stagedTradeServiceAppArifact;
    }


    public static void main(String[] args) throws Exception {
        WebLogicAgentTest wt = new WebLogicAgentTest();
        wt.activitiDeploy();
    }
}
