/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Default client class which encapsulates logic for making reports of test and load statuses to the 
 * FLD controller application via REST calls.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class DefaultFLDReportClient implements FLDReportClient {

    public static final String FLD_CONTROLLER_APP_REPORTS_BASE_URL = "http://%s:8080/loadmon/api/reports";
    public static final String NOTIFY_LOAD_STATUS_REST_URL_TEMPLATE = FLD_CONTROLLER_APP_REPORTS_BASE_URL + "/notify-load-info";//"?loadName=%s&status=%s&timestamp=%s"; 

    public static final DateFormat LOAD_TIMESTAMP_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFLDReportClient.class);
        
    private String controllerHost;
    private HttpClient httpClient;
    
    private DefaultFLDReportClient(String controllerHost) {
        this.controllerHost = controllerHost;
        this.httpClient = HttpClientBuilder.create().build();
    }

    @Override
    public JsonReportResult notifyLoadStatus(String name, LoadStatus status, Date timestamp) throws ClientProtocolException, IOException {
        Args.notBlank(name, "Load name");
        Args.notNull(status, "Load status");
        Args.notNull(timestamp, "Load status update timestamp");
        
        String url = String.format(NOTIFY_LOAD_STATUS_REST_URL_TEMPLATE, controllerHost);
        
        HttpPost postRequest = new HttpPost(url);
        List<BasicNameValuePair> requestParams = new ArrayList<>(3);

        requestParams.add(new BasicNameValuePair("loadName", name));
        requestParams.add(new BasicNameValuePair("status", status.name()));
        requestParams.add(new BasicNameValuePair("timestamp", LOAD_TIMESTAMP_FORMAT.format(timestamp)));
        
        postRequest.setEntity(new UrlEncodedFormEntity(requestParams));
        
        LOGGER.info("Report client | request info: url='{}', name='{}', status='{}', timestamp='{}'", url, name, status, timestamp);
        
        HttpResponse response = httpClient.execute(postRequest);
          
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        StringBuffer buffer = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            buffer.append(line);
        }
        
        LOGGER.info("Report client | response: {}", buffer.toString());
        
        Gson gson = new Gson();
        return gson.fromJson(buffer.toString(), JsonReportResult.class);
    }

    public static FLDReportClient create(String controllerHost) {
        return new DefaultFLDReportClient(controllerHost);
    }
}