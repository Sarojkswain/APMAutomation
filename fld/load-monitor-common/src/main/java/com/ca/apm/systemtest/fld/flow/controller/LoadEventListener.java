/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller;

/**
 * @author keyja01
 *
 */
public interface LoadEventListener {
    public void onLoadEvent(String loadId, FldLoadStatus status);
}
