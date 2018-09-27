/**
 * 
 */
package com.ca.apm.testing.synth.defs;

/**
 * @author keyja01
 *
 */
public class WebFrontend {
    private String host;
    private String url;
    private int port = 8080;
    
    /**
     * 
     */
    public WebFrontend() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
