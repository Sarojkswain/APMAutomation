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

package com.ca.apm.systemtest.fld.flow;

import java.io.IOException;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Flow
public class AGCRegisterFlow extends FlowBase implements IAutomationFlow {
    
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    private static final String REGISTRATION_RESULT = "Registration successful. Restart EM.";
    
    private static final Logger log = LoggerFactory.getLogger(AGCRegisterFlow.class);
    
    private RestClient restClient = new RestClient();
        

    @FlowContext
    private AGCRegisterFlowContext context;

    @Override
    public void run() throws Exception {
        
        log.info("AGC registration wait 30s for startup.");
        Thread.sleep(30*1000); //wait 30s
    
        //get AGC token
        String agcToken = generateAgcToken(context.getAGCHostName(), context.getAgcEmWvPort());
        log.info("AGC token for MOM: " + agcToken);
        //register MOM as follower
        String resultMom = registerMomtoAgc(context.getHostName(), context.getEmWvPort(),
            context.getWvHostName(), context.getWvPort() ,context.getAGCHostName(),
            context.getAgcEmWvPort(), context.getAgcWvPort(), agcToken);
        
        if (!resultMom.equals(REGISTRATION_RESULT)) {
            ErrorUtils.logErrorAndThrowException(log, "MOM registration as follower is unsuccessful");
        }
        log.info(context.getHostName() + ": " + resultMom);
        
    }
    
    
    private String generateAgcToken(String agcHost, String agcEmPort) {
        final String urlPart = "http://" + agcHost + ":"+agcEmPort+"/apm/appmap/private/token";

        try {
            EmRestRequest request =
                new EmRestRequest(urlPart,
                    "{\"description\": \"FLD token\", \"expirationDate\": null, \"system\": true}");
            synchronized (restClient) {
                IRestResponse<String> response = restClient.process(request);
                if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
                    throw new IllegalStateException("Error in rest request: "
                        + response.getHttpStatus().getStatusCode() + " "
                        + response.getHttpStatus().getReasonPhrase()
                        + " . Result status: " + response.getResultStatus());
                }
                JsonObject json = (JsonObject)new JsonParser().parse(response.getContent());
                return json.get("token").getAsString();
            }
        } catch (IOException e) {
            throw new IllegalStateException("IOexception", e);
        }
    }
    
    
    private String registerMomtoAgc(String momHost, String momEmPort, String webviewHost, String wvPort, 
                                    String agcHost, String agcEmPort, String agcWvPort, String agcToken) {
        String payload =
            "{" + "\"agcToken\": \"" + agcToken + "\","
                + "\"agcUrl\" : \"http://" + agcHost + ":"+agcEmPort+"\","
                + "\"agcWebviewUrl\" : \"http://" + agcHost + ":"+agcWvPort+"\","
                + "\"url\" : \"http://" + momHost + ":"+momEmPort+"\","
                + "\"webviewUrl\" : \"http://" + webviewHost + ":"+wvPort+"\","
                + "\"validation\" : false"
            + "}";
        final String urlPart = "http://" + momHost + ":"+momEmPort+"/apm/appmap/private/registration";

        log.info("Register REST url: " + urlPart);
        log.info("Register REST payload: " + payload);
        
        try {
            EmRestRequest request = new EmRestRequest(urlPart, payload);
            synchronized (restClient) {
                IRestResponse<String> response = restClient.process(request);
                if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
                    throw new IllegalStateException("Error in rest request: "
                        + response.getHttpStatus().getStatusCode() + " "
                        + response.getHttpStatus().getReasonPhrase()
                        + " . Result status: " + response.getResultStatus());
                }
                JsonObject json = (JsonObject)new JsonParser().parse(response.getContent());
                return json.get("message").getAsString();
            }
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
            request.addHeader("Authorization", "Bearer " + ADMIN_AUX_TOKEN);
            request.addHeader(HTTP.CONTENT_TYPE, "application/json");
            request.setEntity(new StringEntity(payload));
            return request;
        }

        @Override
        public IRestResponse<String> getResponse(final CloseableHttpResponse httpResponse, String payload) {
            return new SimpleResponse(httpResponse, payload);
        }
    }
    
    class SimpleResponse implements IRestResponse<String> {

        private final CloseableHttpResponse response;
        private final String rawContent;
        private String content;
        private Status status;

        public SimpleResponse(CloseableHttpResponse response, String rawContent) {
            this.response = response;
            this.rawContent = rawContent;
        }

        @Override
        public void parse() {
            content = rawContent;
            status = Status.SUCCESS;
        }

        @Override
        public Status getResultStatus() {
            return status;
        }

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public StatusLine getHttpStatus() {
            return response.getStatusLine();
        }

        @Override
        public String getPayload() {
            return content;
        }
    }
}
