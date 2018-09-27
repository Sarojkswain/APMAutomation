/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller;

/**
 * @author keyja01
 *
 */
public class LoadEvent {
    public String loadId;
    public LoadEventType type;
    
    public LoadEvent() {
    }
    
    public LoadEvent(String loadId, LoadEventType type) {
        this.loadId = loadId;
        this.type = type;
    }
    
    public enum LoadEventType {
        RequestStart, RequestStop, LoadTestEnd
    }
    
    @Override
    public String toString() {
        return "LoadEvent(" + loadId + "," + type + ")";
    }
}
