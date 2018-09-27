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
package com.ca.apm.test.em.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RestUtility {
    RestClient restClient = new RestClient();
    
    public String registerMomtoAgc(String momHost, String agcHost, String agcToken) {
        String payload =
            "{" + "\"agcToken\": \"" + agcToken + "\","
                + "\"agcUrl\" : \"http://" + agcHost + ":8081\","
                + "\"agcWebviewUrl\" : \"http://" + agcHost + ":8082\","
                + "\"url\" : \"http://" + momHost + ":8081\","
                + "\"webviewUrl\" : \"http://" + momHost + ":8082\","
                + "\"validation\" : false"
            + "}";
        final String urlPart = "http://" + momHost + ":8081/apm/appmap/private/registration";

        try {
            EmRestRequest request = new EmRestRequest(urlPart, payload);
            IRestResponse<String> response = restClient.process(request);
            checkResponse(response);
            JsonObject json = (JsonObject)new JsonParser().parse(response.getContent());
            return json.get("message").getAsString();
        } catch (IOException e) {
            throw new IllegalStateException("IOexception", e);
        }
    }

    public String generateAgcToken(String agcHost) {
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/token";

        try {
            EmRestRequest request =
                new EmRestRequest(urlPart,
                    "{\"description\": \"TAS token\", \"expirationDate\": null, \"system\": true}");
            IRestResponse<String> response = restClient.process(request);
            checkResponse(response);
            JsonObject json = (JsonObject)new JsonParser().parse(response.getContent());
            return json.get("token").getAsString();
        } catch (IOException e) {
            throw new IllegalStateException("IOexception", e);
        }
    }
    
    public static interface VertexCallback {
        public void noticeVertex(String vertexId, JsonObject attributes);
    }

    public void processVertices(String emHost, VertexCallback callback) {
        final String urlPart = "http://" + emHost + ":8081/apm/appmap/private/graph/?liveModeVersion=0";

        try {
            EmRestRequest request =
                new EmRestRequest(urlPart,
                    "{\"includedVertices\":[],\"excludedVertices\":[],\"showEntry\":true,\"items\":[]}");
            IRestResponse<String> response = restClient.process(request);
            checkResponse(response);
            JsonObject json = (JsonObject)new JsonParser().parse(response.getContent());
            JsonArray vertices = json.get("vertices").getAsJsonArray();
            for (int i = 0; i < vertices.size(); i++) {
                JsonObject ithObject = vertices.get(i).getAsJsonObject();
                JsonObject attributes = ithObject.get("attributes").getAsJsonObject();
                callback.noticeVertex(ithObject.get("vertexId").getAsString(), attributes);
            }
        } catch (IOException e) {
            throw new IllegalStateException("IOexception", e);
        }
    }

    public List<List<String>> getBTVertices(String emHost, List<String> names) {
        final String urlPart = "http://" + emHost + ":8081/apm/appmap/private/graph/?liveModeVersion=0";

        try {
            EmRestRequest request =
                new EmRestRequest(urlPart,
                    "{\"includedVertices\":[],\"excludedVertices\":[],\"showEntry\":true,\"items\":[]}");
            IRestResponse<String> response = restClient.process(request);
            checkResponse(response);
            JsonObject json = (JsonObject)new JsonParser().parse(response.getContent());
            JsonArray vertices = json.get("vertices").getAsJsonArray();
            List<List<String>> result = new ArrayList<List<String>>(names.size());
            for (String name : names) {
                List<String> ids = new ArrayList<String>();
                for (int i = 0; i < vertices.size(); i++) {
                    JsonObject ithObject = vertices.get(i).getAsJsonObject();
                    JsonObject attributes = ithObject.get("attributes").getAsJsonObject();
                    if ("BUSINESSTRANSACTION".equals(attributes.get("type").getAsString()) &&
                            name.equals(attributes.get("transactionName").getAsString())) {
                        ids.add(ithObject.get("vertexId").getAsString());
                    }
                }
                result.add(ids);
            }
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("IOexception", e);
        }
    }
    
    public void assignAttributeToVertices(String emHost, List<String> ids, String name, String value) {
        if (ids != null && !ids.isEmpty()) {
            final String urlPart = "http://" + emHost + ":8081/apm/appmap/private/attributes/assign";
    
            try {
                EmRestRequest request = new EmRestRequest(urlPart,
                        "{\"vertexIds\":[\"" + StringUtils.join(ids, "\",\"")
                        + "\"],\"attributeName\":\"" + name
                        + "\",\"attributeValue\":\"" + value + "\"}");
                IRestResponse<String> response = restClient.process(request);
                checkResponse(response);
            } catch (IOException e) {
                throw new IllegalStateException("IOexception", e);
            }
        }
    }
    
    public String addGroup(String agcHost, String name) {
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/grouping";

        try {
            EmRestRequest requestGet = new EmRestRequest(urlPart);
            IRestResponse<String> responseGet = restClient.process(requestGet);
            checkResponse(responseGet);
            JsonObject json = (JsonObject)new JsonParser().parse(responseGet.getContent());
            int order = json.get("items").getAsJsonArray().size();

            EmRestRequest requestPost =
                new EmRestRequest(urlPart, "{\"id\":\"0\",\"name\":\"" + name
                    + "\",\"groupBy\":[{\"attributeName\":\"" + name
                    + "\",\"prefix\":\"\"}],\"public\":false,\"order\":" + Integer.toString(order + 1)
                    + ",\"owner\":\"admin\"}");
            IRestResponse<String> responsePost = restClient.process(requestPost);
            checkResponse(responsePost);
            json = (JsonObject)new JsonParser().parse(responsePost.getContent());

            return json.get("id").getAsString();
        } catch (IOException e) {
            throw new IllegalStateException("IOexception", e);
        }
    }
    
    private void checkResponse(IRestResponse<?> response) {
        if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
            throw new IllegalStateException("Error in rest request: "
                + response.getHttpStatus().getStatusCode() + " "
                + response.getHttpStatus().getReasonPhrase());
        }
    }

    class EmRestRequest implements IRestRequest<String> {
        
        private final String url;
        private final String payload;
        
        EmRestRequest(String url, String payload) {
            this.url = url;
            this.payload = payload;
        }
        
        EmRestRequest(String url) {
            this.url = url;
            this.payload = null;
        }
        
        @Override
        public HttpRequestBase getRequest() throws IOException {
            HttpRequestBase request;
            
            if (payload != null) {
                HttpPost requestPost  = new HttpPost(url);
                requestPost.setEntity(new StringEntity(payload));
                request = requestPost;
            } else {
                request  = new HttpGet(url);
            }
            request.addHeader("Authorization", "Bearer " + RoleUtility.ADMIN_AUX_TOKEN);
            request.addHeader(HTTP.CONTENT_TYPE, "application/json");
            return request;
        }

        @Override
        public IRestResponse<String> getResponse(final CloseableHttpResponse httpResponse, String payload) {
            return new SimpleResponse(httpResponse, payload);
        }
    };
}
