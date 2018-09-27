/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.em.job;

import java.io.IOException;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.NetworkUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.JavaDelegateUtils;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Aggregates {@link JavaDelegate} instances for use in the FLD workflows
 * 
 * @author filja01
 *
 */
@Component("followerTaskBean")
public class FollowerJobsBean implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(FollowerJobsBean.class);
    private final String REGISTRATION_RESULT = "Registration successful. Restart EM.";
    
    private static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    
    RestClient restClient = new RestClient();
    
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private AgentProxyFactory agentProxyFactory;
    
    private RegisterFollowerDelegate registerFollower;

    
    public RegisterFollowerDelegate getRegisterFollower() {
        return registerFollower;
    }

    
    @Override
    public void afterPropertiesSet() throws Exception {
        registerFollower = new RegisterFollowerDelegate(nodeManager, agentProxyFactory);
    }
    
    /**
     * Register MOM as Follower to AGC Master
     * @author filja01
     *
     */
    public class RegisterFollowerDelegate extends AbstractJavaDelegate {

        public RegisterFollowerDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String agcNode = JavaDelegateUtils.getNodeExecutionVariable(execution, "agc.node");
            String momNode = JavaDelegateUtils.getNodeExecutionVariable(execution, "mom.node");
            String mom2Node = JavaDelegateUtils.getNodeExecutionVariable(execution, "mom02.node");
            String webviewNode = JavaDelegateUtils.getNodeExecutionVariable(execution, "webview.node");
            boolean available = nodeManager.checkNodeAvailable(momNode);
            boolean available2 = nodeManager.checkNodeAvailable(agcNode);
            boolean available3 = nodeManager.checkNodeAvailable(mom2Node);
            
            if (available && available2) {
                if(NetworkUtils.isServerListening(momNode+".ca.com", 5001) 
                    && NetworkUtils.isServerListening(agcNode+".ca.com", 5001)) {
                    
                    //get AGC token
                    String agcToken = generateAgcToken(agcNode);
                    log.info("AGC token for MOM: " + agcToken);
                    //register MOM as follower
                    String resultMom = registerMomtoAgc(momNode, webviewNode, agcNode, agcToken);
                    
                    if (!resultMom.equals(REGISTRATION_RESULT)) {
                        ErrorUtils.logErrorAndThrowException(log, "MOM registration as follower is unsuccessful");
                    }
                    log.info(momNode + ": " + resultMom);
                    
                    //restart MOM
                    //EmPlugin pluginWV = loadPlugin(execution, NODE_NAME, "wvPlugin", EmPlugin.class);
                    EmPlugin plugin = loadPlugin(execution, "mom.node", "emPlugin", EmPlugin.class);
                    plugin.stop(null);
                    plugin.start(null);
                }
                else {
                    ErrorUtils.logErrorAndThrowException(log, "MOM server or AGC Master is not running");
                }
            }
            //register 2nd MOM
            if (available2 && available3) {
                if(NetworkUtils.isServerListening(mom2Node+".ca.com", 5001) 
                    && NetworkUtils.isServerListening(agcNode+".ca.com", 5001)) {
                    
                    //get AGC token
                    String agcToken = generateAgcToken(agcNode);
                    log.info("AGC token for MOM#2: " + agcToken);
                    //register MOM#2 as follower
                    String resultMom = registerMomtoAgc(mom2Node, mom2Node, agcNode, agcToken);
                    
                    if (!resultMom.equals(REGISTRATION_RESULT)) {
                        ErrorUtils.logErrorAndThrowException(log, "MOM#2 registration as follower is unsuccessful");
                    }
                    log.info(mom2Node + ": " + resultMom);
                    
                    //restart MOM
                    //EmPlugin pluginWV = loadPlugin(execution, NODE_NAME, "wvPlugin", EmPlugin.class);
                    EmPlugin plugin = loadPlugin(execution, "mom02.node", "emPlugin", EmPlugin.class);
                    plugin.stop(null);
                    plugin.start(null);
                }
                else {
                    ErrorUtils.logErrorAndThrowException(log, "MOM#2 server or AGC Master is not running");
                }
            }
        }
        
        private String generateAgcToken(String agcHost) {
            final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/token";

            try {
                EmRestRequest request =
                    new EmRestRequest(urlPart,
                        "{\"description\": \"FLD token\", \"expirationDate\": null, \"system\": true}");
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
        
        
        private String registerMomtoAgc(String momHost, String webviewHost, String agcHost, String agcToken) {
            String payload =
                "{" + "\"agcToken\": \"" + agcToken + "\","
                    + "\"agcUrl\" : \"http://" + agcHost + ":8081\","
                    + "\"agcWebviewUrl\" : \"http://" + agcHost + ":8080\","
                    + "\"url\" : \"http://" + momHost + ":8081\","
                    + "\"webviewUrl\" : \"http://" + webviewHost + ":8080\","
                    + "\"validation\" : false"
                + "}";
            final String urlPart = "http://" + momHost + ":8081/apm/appmap/private/registration";

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
                throw new IllegalStateException("IOexception", e);
            }
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
