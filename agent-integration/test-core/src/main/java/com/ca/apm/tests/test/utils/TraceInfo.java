package com.ca.apm.tests.test.utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author kurma05
 *
 */
public class TraceInfo {

    private String nodeName;
    private String btNodeName;
    private String url;
    private int numberRequests;
    private String traceType;
    private ArrayList<HashMap<String, String>> components;
    
    public TraceInfo(String nodeName, String url, int numberRequests,
                     String traceType, ArrayList<HashMap<String, String>> components) {
        
        this.nodeName = nodeName;
        this.url = url;
        this.numberRequests = numberRequests;
        this.traceType = traceType;
        this.components = components;
    }
    
    public TraceInfo(String nodeName, String btNodeName, String url, int numberRequests,
                     String traceType, ArrayList<HashMap<String, String>> components) {
        
        this.nodeName = nodeName;
        this.btNodeName = btNodeName;
        this.url = url;
        this.numberRequests = numberRequests;
        this.traceType = traceType;
        this.components = components;
    }
    
    public String getTraceType() {
        return traceType;
    }

    public void setTraceType(String traceType) {
        this.traceType = traceType;
    }

    public ArrayList<HashMap<String, String>> getComponents() {
        return components;
    }

    public void setComponents(ArrayList<HashMap<String, String>> components) {
        this.components = components;
    }

    public String getNodeName() {
        return nodeName;
    }
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public String getBtNodeName() {
        return btNodeName;
    }

    public void setBtNodeName(String btNodeName) {
        this.btNodeName = btNodeName;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public int getNumberRequests() {
        return numberRequests;
    }
    
    public void setNumberRequests(int numberRequests) {
        this.numberRequests = numberRequests;
    }
}
