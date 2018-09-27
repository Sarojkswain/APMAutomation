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
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.fld.common.ErrorUtils;

@Flow
public class TIMSettingsFlow extends FlowBase implements IAutomationFlow {
    public static final String TIM_QA_AUTOMATION_PATH = 
        "cgi-bin/ca/apm/tim-qa-automation/TimQaWebServices";

    private static final Logger log = LoggerFactory.getLogger(TIMSettingsFlow.class);

    @FlowContext
    private TIMSettingsFlowContext context;

    @Override
    public void run() throws Exception {
        
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String qaUrl = "http://" + context.getTimHostname() + "/" + TIM_QA_AUTOMATION_PATH;
            HttpGetWithEntity req = new HttpGetWithEntity(new URI(qaUrl));
            req.setHeader("X-TIM-QA-Protocol-Version", "1");
            req.setHeader("X-TIM-QA-Request-Type", context.getRequestType());//"setDatabaseSetting");
            //setDatabaseSetting
            if (context.getRequestType().equals("setDatabaseSetting")) {
                req.setHeader("X-Setting-Name", context.getSettingName());
                req.setHeader("X-Setting-Value", context.getSettingValue());
            } //setNetworkInterface
            else if (context.getRequestType().equals("setNetworkInterfaces")) {
                req.setEntity(new StringEntity("if=" + context.getNetworkInterfaces()));
            }
            log.info("Setting TIM setting {} to value {}.", context.getSettingName(), context.getSettingValue());
            try (CloseableHttpResponse resp = httpclient.execute(req)) {
                log.info("HTTP response status line: {}", resp.getStatusLine().toString());
            }
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Exception during interface configuration. Exception: {0}");
        } catch (URISyntaxException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Exception when building QA API URL. Exception: {0}");
        }
    }
    
    
    /**
     *
     * This class allows us to issue GET requests with body
     * with Apache Commons HTTP client.
     * 
     **/
    private class HttpGetWithEntity extends HttpPost {

        public static final String METHOD_NAME = "GET";

        public HttpGetWithEntity(URI url) {
            super(url);
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }
}
