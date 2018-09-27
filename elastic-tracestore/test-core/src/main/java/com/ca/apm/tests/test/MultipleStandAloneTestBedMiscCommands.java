package com.ca.apm.tests.test;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.apm.systemtest.fld.role.CLWWorkStationLoadRole;
import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.ElasticSearchRole;
import com.ca.apm.tests.testbed.MultipleStandAloneTestBed;
import com.ca.apm.tests.testbed.SimpleDevTestBedWithElastic;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import com.google.common.io.Resources;

@Test
public class MultipleStandAloneTestBedMiscCommands extends TasTestNgTest
    implements
        FLDLoadConstants,
        FLDConstants {

    // choose one of emMachine{i} to invoke these commands - any will do
    @Tas(testBeds = @TestBed(name = MultipleStandAloneTestBed.class, executeOn = "emMachine0"), owner = "venpr05", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void startAllEms() throws Exception {

        for (int i = 0; i < MultipleStandAloneTestBed.NUM_OF_SA; i++) {

            runSerializedCommandFlowFromRoleAsync("emRole" + i, EmRole.ENV_START_EM);
        }
    }

    @Tas(testBeds = @TestBed(name = MultipleStandAloneTestBed.class, executeOn = "emMachine0"), owner = "venpr05", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void startAllWebviews() throws Exception {

        for (int i = 0; i < MultipleStandAloneTestBed.NUM_OF_SA; i++) {

            runSerializedCommandFlowFromRoleAsync("emRole" + i, EmRole.ENV_START_WEBVIEW);
        }
    }

    @Tas(testBeds = @TestBed(name = MultipleStandAloneTestBed.class, executeOn = "emMachine0"), owner = "venpr05", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void stopAllEms() throws Exception {

        for (int i = 0; i < MultipleStandAloneTestBed.NUM_OF_SA; i++) {

            runSerializedCommandFlowFromRoleAsync("emRole" + i, EmRole.ENV_STOP_EM);
        }
    }

    @Tas(testBeds = @TestBed(name = MultipleStandAloneTestBed.class, executeOn = "emMachine0"), owner = "venpr05", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void stopAllWebviews() throws Exception {

        for (int i = 0; i < MultipleStandAloneTestBed.NUM_OF_SA; i++) {

            runSerializedCommandFlowFromRoleAsync("emRole" + i, EmRole.ENV_STOP_WEBVIEW);
        }
    }

    @Tas(testBeds = @TestBed(name = MultipleStandAloneTestBed.class, executeOn = "emMachine0"), owner = "venpr05", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void startAllLoads() throws Exception {

        for (int i = 0; i < MultipleStandAloneTestBed.NUM_OF_SA; i++) {

            runSerializedCommandFlowFromRoleAsync("clientDeployRole" + i,
                ClientDeployRole.STRESSAPP_START_LOAD);
            runSerializedCommandFlowFromRoleAsync("wurlitzerRole" + i,
                WurlitzerLoadRole.START_WURLITZER_FLOW_KEY);
            runSerializedCommandFlowFromRoleAsync("hvrRole" + i,
                HVRAgentLoadRole.START_HVR_LOAD_KEY);
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            runSerializedCommandFlowFromRoleAsync("clwRole" + i,
                CLWWorkStationLoadRole.CLW_START_LOAD);
            // runSerializedCommandFlowFromRoleAsync("clientDeployRole" + i,
            // ClientDeployRole.JMETER_START_LOAD);

        }
    }

    // change name to all loads when we can stop all loads
    @Tas(testBeds = @TestBed(name = MultipleStandAloneTestBed.class, executeOn = "emMachine0"), owner = "venpr05", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void stopPartialLoads() throws Exception {

        for (int i = 0; i < MultipleStandAloneTestBed.NUM_OF_SA; i++) {

            // runSerializedCommandFlowFromRoleAsync("clientDeployRole" + i,
            // ClientDeployRole.STRESSAPP_START_LOAD);
            runSerializedCommandFlowFromRoleAsync("wurlitzerRole" + i,
                WurlitzerLoadRole.STOP_WURLITZER_FLOW_KEY);
            runSerializedCommandFlowFromRoleAsync("hvrRole" + i, HVRAgentLoadRole.STOP_HVR_LOAD_KEY);
            runSerializedCommandFlowFromRoleAsync("clwRole" + i,
                CLWWorkStationLoadRole.CLW_STOP_LOAD);
        }
    }

    @Tas(testBeds = @TestBed(name = SimpleDevTestBedWithElastic.class, executeOn = "emMachine"), owner = "venpr05", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void startStressApp() throws Exception {

        // start elasticsearch
        runSerializedCommandFlowFromRoleAsync("esRole", ElasticSearchRole.ELASTICSEARCH_START);

        // create mapping
        String esHost = envProperties.getMachineHostnameByRoleId("esRole");
        RestClient client = new RestClient();
        String body =
            Resources.toString(Resources.getResource("es/ttindex.json"), Charset.forName("UTF-8"));
        IRestResponse<String> response =
            client.process(new EsRestRequest("http://" + esHost + ":9200/tt", body));
        System.out.println("The Index Creation Status code: "
            + response.getHttpStatus().getStatusCode());

        // start agent
        runSerializedCommandFlowFromRoleAsync("clientDeployRole",
            ClientDeployRole.STRESSAPP_START_LOAD);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        runSerializedCommandFlowFromRoleAsync("clwRole", CLWWorkStationLoadRole.CLW_START_LOAD);

    }

    public static class EsRestRequest implements IRestRequest<String> {

        private final String url;
        private final String payload;

        public EsRestRequest(String url, String payload) {
            this.url = url;
            this.payload = payload;
        }

        @Override
        public HttpRequestBase getRequest() throws IOException {
            HttpPost request = new HttpPost(url);
            request.addHeader(HTTP.CONTENT_TYPE, "application/json");
            request.setEntity(new StringEntity(payload));
            return request;
        }

        @Override
        public IRestResponse<String> getResponse(final CloseableHttpResponse httpResponse,
            String payload) {
            return new SimpleResponse(httpResponse, payload);
        }
    }
}
