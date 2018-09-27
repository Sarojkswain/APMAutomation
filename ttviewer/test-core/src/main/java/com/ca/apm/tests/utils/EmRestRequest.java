/*
 * Copyright (c) 2017 CA. All rights reserved.
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

package com.ca.apm.tests.utils;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.apm.tests.testbed.StandAloneTestBed;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;

public class EmRestRequest implements IRestRequest<String> {

    private final String url;
    private final String payload;

    public EmRestRequest(String url, String payload) {
        this.url = url;
        this.payload = payload;
    }

    @Override
    public HttpRequestBase getRequest() throws IOException {
        HttpPost request = new HttpPost(url);
        request.addHeader("Authorization", "Bearer " + StandAloneTestBed.ADMIN_TOKEN);
        request.addHeader(HTTP.CONTENT_TYPE, "application/json");
        request.setEntity(new StringEntity(payload));
        return request;
    }

    @Override
    public IRestResponse<String> getResponse(final CloseableHttpResponse httpResponse,
        String payload) {
        return new SimpleResponse(httpResponse, payload);
    }
};
