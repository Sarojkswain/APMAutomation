/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

/**
 * @author keyja01
 *
 */
public class TessWebServerFilterConfig {
    private String name;
    private String timName;
    private String appServerHostname;
    private int appServerPort;

    
    public TessWebServerFilterConfig(String name, String timName, String appServerHostname, int appServerPort) {
        this.name = name;
        this.timName = timName;
        this.appServerHostname = appServerHostname;
        this.appServerPort = appServerPort;
    }

    public String getName() {
        return name;
    }

    public String getTimName() {
        return timName;
    }

    public String getAppServerHostname() {
        return appServerHostname;
    }

    public int getAppServerPort() {
        return appServerPort;
    }
}
