package com.ca.apm.tests.utils;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import com.ca.apm.automation.action.flow.restClient.SimpleResponse;
import com.ca.apm.tests.testbed.AssistedTriageTestbed;
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
        request.addHeader("Authorization", "Bearer " + AssistedTriageTestbed.ADMIN_TOKEN);
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
