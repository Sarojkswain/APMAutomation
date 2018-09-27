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
package com.ca.apm.automation.action.flow.etc;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;

/**
 * Flow to configure a team center provider with a master
 * <p/>
 * Both master and provider should be up and running while this flow is invoked.
 * This flow does not restart the provider.
 *
 * @author bhusu01
 */
@Flow
public class ConfigureProviderFlow extends FlowBase {
    @FlowContext
    ConfigureProviderFlowContext flowContext;
    private RestClient restClient;
    private String auxToken;

    @Override
    public void run() throws Exception {
        restClient = new RestClient();
        auxToken = flowContext.getAuthToken();
        String etcHost = flowContext.getMasterHost();
        String providerHost = flowContext.getProviderHost();

        String standAloneToken = generateProviderToken(etcHost);
        // Register Stand alone with ETC and shutdown
        String registrationResult = registerProviderWithAgc(providerHost, etcHost, standAloneToken);

        logger.info("Flow finished with result: {}", registrationResult);
    }

    private String generateProviderToken(String etcHost) {
        final String urlPart = "http://" + etcHost + ":8081/apm/appmap/private/token";

        try {
            EmRestRequest request =
                new EmRestRequest(urlPart, "{\"description\": \"TAS token\", \"expirationDate\": "
                    + "null, \"system\": true}");
            IRestResponse<String> response = restClient.process(request);
            JsonObject json = (JsonObject) new JsonParser().parse(response.getContent());
            return json.get("token").getAsString();
        } catch (IOException e) {
            throw new IllegalStateException("IOexception", e);
        }
    }

    private String registerProviderWithAgc(String providerHost, String masterHost, String
        providerToken) {
        String payload =
            "{" + "\"agcToken\": \"" + providerToken + "\"," + "\"agcUrl\" : \"http://" + masterHost
                + ":8081\"," + "\"agcWebviewUrl\" : \"http://" + masterHost + ":8082\","
                + "\"url\" : \"http://" + providerHost + ":8081\"," + "\"webviewUrl\" : \"http://"
                + providerHost + ":8082\"," + "\"validation\" : false" + "}";
        final String urlPart = "http://" + providerHost + ":8081/apm/appmap/private/registration";

        try {
            EmRestRequest request = new EmRestRequest(urlPart, payload);
            IRestResponse<String> response = restClient.process(request);
            if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
                throw new IllegalStateException(
                    "Error in rest request: " + response.getHttpStatus().getStatusCode() + " "
                        + response.getHttpStatus().getReasonPhrase());
            }
            JsonObject json = (JsonObject) new JsonParser().parse(response.getContent());
            return json.get("message").getAsString();
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
            request.addHeader("Authorization", "Bearer " + auxToken);
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
