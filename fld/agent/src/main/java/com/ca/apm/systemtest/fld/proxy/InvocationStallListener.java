/**
 * 
 */
package com.ca.apm.systemtest.fld.proxy;

/**
 * @author KEYJA01
 *
 */
public interface InvocationStallListener {
    public static final String ERR_INVOCATION_STALLED = "ERR_INVOCATION_STALLED";
    public static final String AGENT_CONTROL = "fld.agent.control";
    
    public String listenForStalls(String invocationId, long stallMs);
    
    public void unregister(String registrationId);
}
