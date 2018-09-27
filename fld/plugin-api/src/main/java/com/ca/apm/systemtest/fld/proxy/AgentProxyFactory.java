package com.ca.apm.systemtest.fld.proxy;



public interface AgentProxyFactory {

    public abstract AgentProxy createProxy(String target);

    /**
     * 
     * @param target
     * @param processInstanceId
     * @return
     */
    public abstract AgentProxy createProxy(String target, String processInstanceId);

}
