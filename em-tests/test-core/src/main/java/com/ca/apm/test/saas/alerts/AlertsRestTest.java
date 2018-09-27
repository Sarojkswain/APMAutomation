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

package com.ca.apm.test.saas.alerts;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.test.em.saas.SimpleSaaSEmTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Tas(testBeds = @TestBed(name = SimpleSaaSEmTestBed.class, executeOn = SimpleSaaSEmTestBed.MACHINE_ID), owner = "svazd01", size = SizeType.MEDIUM)
@Test(groups = "saas")
public class AlertsRestTest extends TasTestNgTest {
    RestClient restClient = new RestClient();


    @Test
    public void testList() throws IOException {
        assertALerts(getAlerts());
    }



    @Test
    public void testUpdateAllTresholds() throws IOException {
        JsonObject json = getAlerts();

        final String urlPart = getAlertsRestUrl();
        JsonObject original = (JsonObject) new JsonParser().parse(json.toString());
        
        assertNotNull(json.getAsJsonArray("alerts"));
        assertTrue(json.getAsJsonArray("alerts").isJsonArray());
        for (JsonElement e : json.getAsJsonArray("alerts")) {
            JsonObject o = (JsonObject) e;
            String operator = o.get("operator").getAsString();

            long dangerTreshold = o.get("dangerTreshold").getAsLong();
            long cautionTreshold = o.get("cautionTreshold").getAsLong();

            switch (operator) {
                case "GREATER_THAN":
                    dangerTreshold ++;
                    cautionTreshold++;
                    break;
                case "LESS_THAN":
                    dangerTreshold--;
                    cautionTreshold--;
                    break;
                case "EQUAL_TO":
                    dangerTreshold++;
                    cautionTreshold--;
                    break;
                case "NOT_EQUAL_TO":
                    dangerTreshold++;
                    cautionTreshold++;
                    break;
                default:
                    assertTrue(false, "UNKNOWN OPERATOR TYPE");
                    break;
            }

            o.addProperty("dangerTreshold", dangerTreshold);
            o.addProperty("cautionTreshold", cautionTreshold);



            EmRestRequest req = new EmRestRequest(urlPart, o.toString(), Method.POST);
            IRestResponse<String> responseGet = restClient.process(req);
            if (responseGet.getHttpStatus().getStatusCode() != 200) {
                System.out.println(responseGet);
            }
            assertEquals(responseGet.getHttpStatus().getStatusCode(), 200);
        }
        
        JsonObject postUpdate = getAlerts();
        for (JsonElement orig : original.getAsJsonArray("alerts")) {
            JsonObject o = (JsonObject) orig;
            for (JsonElement updated : postUpdate.getAsJsonArray("alerts")) {
                JsonObject u = (JsonObject) updated;
                if (o.get("id").getAsLong() == u.get("id").getAsLong()) {
                    String operator = o.get("operator").getAsString();


                    long dangerTresholdOrig = o.get("dangerTreshold").getAsLong();
                    long cautionTresholdOrig = o.get("cautionTreshold").getAsLong();

                    long dangerTresholdIUpdated = u.get("dangerTreshold").getAsLong();
                    long cautionTresholdUpdated = u.get("cautionTreshold").getAsLong();

                    switch (operator) {
                        case "GREATER_THAN":
                            assertTrue(dangerTresholdIUpdated > dangerTresholdOrig);
                            assertTrue(cautionTresholdUpdated > cautionTresholdOrig);
                            break;
                        case "LESS_THAN":
                            assertTrue(dangerTresholdIUpdated < dangerTresholdOrig);
                            assertTrue(cautionTresholdUpdated < cautionTresholdOrig);
                            break;
                        case "EQUAL_TO":
                            assertTrue(dangerTresholdIUpdated > dangerTresholdOrig);
                            assertTrue(cautionTresholdUpdated > cautionTresholdOrig);
                            assertTrue(cautionTresholdUpdated == dangerTresholdIUpdated);
                            break;
                        case "NOT_EQUAL_TO":
                            assertTrue(dangerTresholdIUpdated > dangerTresholdOrig);
                            assertTrue(cautionTresholdUpdated > cautionTresholdOrig);
                            break;
                        default:
                            assertTrue(false, "UNKNOWN OPERATOR TYPE");
                            break;
                    }
                }
            }
        }
    }



    @Test
    public void testAllFailureTresholds() throws IOException {
        JsonObject json = getAlerts();

        final String urlPart = getAlertsRestUrl();

        JsonObject original = (JsonObject) new JsonParser().parse(json.toString());


        assertNotNull(json.getAsJsonArray("alerts"));
        assertTrue(json.getAsJsonArray("alerts").isJsonArray());
        for (JsonElement e : json.getAsJsonArray("alerts")) {
            JsonObject o = (JsonObject) e;
            String operator = o.get("operator").getAsString();

            long dangerTreshold = o.get("dangerTreshold").getAsLong();
            long cautionTreshold = o.get("cautionTreshold").getAsLong();

            switch (operator) {
                case "GREATER_THAN":

                    cautionTreshold += dangerTreshold;
                    break;
                case "LESS_THAN":
                    cautionTreshold -= dangerTreshold;
                    break;
                case "EQUAL_TO":
                    dangerTreshold++;
                    cautionTreshold--;
                    break;
                case "NOT_EQUAL_TO":
                    dangerTreshold += cautionTreshold;
                    break;
                default:
                    assertTrue(false, "UNKNOWN OPERATOR TYPE");
                    break;
            }

            o.addProperty("dangerTreshold", dangerTreshold);
            o.addProperty("cautionTreshold", cautionTreshold);

            EmRestRequest req = new EmRestRequest(urlPart, o.toString(), Method.POST);
            IRestResponse<String> responseGet = restClient.process(req);
            assertEquals(responseGet.getHttpStatus().getStatusCode(), 500);
        }

        JsonObject postUpdate = getAlerts();
        for (JsonElement orig : original.getAsJsonArray("alerts")) {
            JsonObject o = (JsonObject) orig;
            for (JsonElement updated : postUpdate.getAsJsonArray("alerts")) {
                JsonObject u = (JsonObject) updated;
                if (o.get("id").getAsLong() == u.get("id").getAsLong()) {

                    long dangerTresholdOrig = o.get("dangerTreshold").getAsLong();
                    long cautionTresholdOrig = o.get("cautionTreshold").getAsLong();

                    long dangerTresholdIUpdated = u.get("dangerTreshold").getAsLong();
                    long cautionTresholdUpdated = u.get("cautionTreshold").getAsLong();

                    assertTrue(dangerTresholdIUpdated == dangerTresholdOrig);
                    assertTrue(cautionTresholdUpdated == cautionTresholdOrig);

                }
            }
        }
    }


    @Test
    public void testAllActivate() throws IOException {
        JsonObject json = getAlerts();

        final String urlPart = getAlertsRestUrl();

        JsonObject original = (JsonObject) new JsonParser().parse(json.toString());


        assertNotNull(json.getAsJsonArray("alerts"));
        assertTrue(json.getAsJsonArray("alerts").isJsonArray());
        for (JsonElement e : json.getAsJsonArray("alerts")) {
            JsonObject o = (JsonObject) e;

            boolean active = o.get("active").getAsBoolean();
            o.addProperty("active", !active);


            EmRestRequest req = new EmRestRequest(urlPart, o.toString(), Method.POST);
            IRestResponse<String> responseGet = restClient.process(req);
            assertEquals(responseGet.getHttpStatus().getStatusCode(), 200);
        }

        JsonObject postUpdate = getAlerts();
        for (JsonElement orig : original.getAsJsonArray("alerts")) {
            JsonObject o = (JsonObject) orig;
            for (JsonElement updated : postUpdate.getAsJsonArray("alerts")) {
                JsonObject u = (JsonObject) updated;
                if (o.get("id").getAsLong() == u.get("id").getAsLong()) {

                    boolean activeOrig = o.get("active").getAsBoolean();

                    boolean activeUpdated = u.get("active").getAsBoolean();

                    assertTrue(activeOrig != activeUpdated);

                }

            }
        }
    }






    private JsonObject getAlerts() throws IOException {
        final String urlPart = getAlertsRestUrl();

        EmRestRequest req = new EmRestRequest(urlPart, null, Method.GET);
        IRestResponse<String> responseGet = restClient.process(req);

        assertEquals(200, responseGet.getHttpStatus().getStatusCode());

        JsonObject json = (JsonObject) new JsonParser().parse(responseGet.getContent());
        
        return json;
    }




    private void assertALerts(JsonObject json){
        assertNotNull(json.getAsJsonArray("alerts"));
        assertTrue(json.getAsJsonArray("alerts").isJsonArray());
        
        
        for (JsonElement e : json.getAsJsonArray("alerts")) {
            assertTrue(e.isJsonObject());
            JsonObject o = (JsonObject) e;

            assertNotNull(o.get("id"));
            assertNotNull(o.get("id").getAsLong());

            assertNotNull(o.get("name"));
            assertNotNull(o.get("name").getAsString());
            assertTrue(!o.get("name").getAsString().isEmpty());

            assertNotNull(o.get("description"));
            assertNotNull(o.get("description").getAsString());

            assertNotNull(o.get("active"));
            assertNotNull(o.get("active").getAsBoolean());


            assertNotNull(o.get("operator"));
            assertNotNull(o.get("operator").getAsString());
            
            assertNotNull(o.get("cautionTreshold"));
            assertNotNull(o.get("cautionTreshold").getAsLong());
            
            long cautionTreshold = o.get("cautionTreshold").getAsLong();
            
            assertNotNull(o.get("dangerTreshold"));
            assertNotNull(o.get("dangerTreshold").getAsLong());
            
            long dangerTreshold = o.get("dangerTreshold").getAsLong();
            
            String operator = o.get("operator").getAsString();
            switch (operator) {
                case "GREATER_THAN":
                    assertTrue(dangerTreshold >= cautionTreshold);
                    break;
                case "LESS_THAN":
                    assertTrue(dangerTreshold <= cautionTreshold);
                    break;
                case "EQUAL_TO":
                    // no condition apply
                    break;
                case "NOT_EQUAL_TO":
                    assertTrue(dangerTreshold == cautionTreshold);
                    break;
                default:
                    assertTrue(false, "UNKNOWN OPERATOR TYPE");
                    break;
            }


            assertNotNull(o.get("vertexTypes"));
            assertTrue(o.get("vertexTypes").isJsonArray());
            assertTrue(o.get("vertexTypes").getAsJsonArray().size() > 0);

            assertNotNull(o.get("unit"));
            assertNotNull(o.get("unit").getAsString());
            assertTrue(!o.get("unit").getAsString().isEmpty());



            assertNotNull(o.get("differentialAnalysisEnabled"));
            assertNotNull(o.get("differentialAnalysisEnabled").getAsBoolean());


            assertNotNull(o.get("typesDescriptions"));
            assertTrue(o.get("typesDescriptions").isJsonArray());
            assertTrue(o.get("typesDescriptions").getAsJsonArray().size() > 0);

        }
        
    }

    private String getAlertsRestUrl() {
        String emHost = envProperties.getMachineHostnameByRoleId(SimpleSaaSEmTestBed.EM_ROLE_ID);
        // String emHost = "localhost";
        return "http://" + emHost + ":8081/apm/appmap/private/alert";
    }

    private enum Method {
        GET, POST,
    }

    class EmRestRequest implements IRestRequest<String> {

        private final String url;
        private final String payload;
        private final Method method;

        EmRestRequest(String url, String payload, Method method) {
            this.url = url;
            this.payload = payload;
            this.method = method;

        }



        @Override
        public HttpRequestBase getRequest() throws IOException {
            HttpRequestBase request = null;

            switch (method) {
                case POST:
                    HttpPost requestPost = new HttpPost(url);
                    requestPost.setEntity(new StringEntity(payload));
                    request = requestPost;
                    break;
                case GET:
                    request = new HttpGet(url);
                    break;
            }


            request.addHeader("Authorization", "Bearer " + RoleUtility.ADMIN_AUX_TOKEN);
            request.addHeader(HTTP.CONTENT_TYPE, "application/json");
            return request;
        }

        @Override
        public IRestResponse<String> getResponse(final CloseableHttpResponse httpResponse,
            String payload) {
            return new SimpleResponse(httpResponse, payload);
        }
    };
}