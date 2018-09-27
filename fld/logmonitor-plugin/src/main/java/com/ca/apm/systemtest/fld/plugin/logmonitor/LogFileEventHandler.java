/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.logmonitor;

/**
 * @author keyja01
 *
 */
public interface LogFileEventHandler {
    public void onLogFileRotated();
    
    public void onLogFileDeleted();
}
