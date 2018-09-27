package com.ca.apm.systemtest.fld.plugin.agentdownload;

import java.net.URI;

import org.apache.http.client.methods.HttpPost;

/**
 * Created by haiva01 on 2.12.2014.
 *
 * This class allows us to issue GET requests with body
 * with Apache Commons HTTP client.
 * This is based on http://stackoverflow.com/a/25019452/341065.
 */


public class HttpGetWithEntity extends HttpPost {

    public static final String METHOD_NAME = "GET";

    public HttpGetWithEntity(URI url) {
        super(url);
    }

    public HttpGetWithEntity(String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
