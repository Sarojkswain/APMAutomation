/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin;

/**
 * 
 * Manager class providing business API for working with Load Orchestrator's nodes. 
 * 
 * @author keyja01
 *
 */
public interface NodeManager {
    /**
     * Checks if a node is available for calls to its proxies
     * @param nodeName
     * @return
     */
    public boolean checkNodeAvailable(String nodeName);
    
    
    /**
     * JMS listener delegate method for processing received heartbeat messages
     * @param heartbeatJson
     * @throws Exception
     */
    public void receiveHeartbeat(String heartbeatJson) throws Exception;
}
