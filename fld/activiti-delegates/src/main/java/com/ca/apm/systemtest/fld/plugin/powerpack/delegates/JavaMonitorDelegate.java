package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;


import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Factory to create PowerPack performance test monitoring Java delegates.
 * 
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com) 
 */
public class JavaMonitorDelegate {

    /**
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @return
     */
    public static AbstractJavaDelegate getStartMonitoringDelegate(NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        return new StartMonitoringDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    /**
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @return
     */
    public static AbstractJavaDelegate getStopMonitoringDelegate(NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        return new StopMonitoringDelegate(nodeManager, agentProxyFactory, fldLogger);
    }


    /**
     *
     * @param nodeManager
     * @param agentProxyFactory
     * @return
     */
    public static AbstractJavaDelegate getCheckMonitoringDelegate(NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        return new CheckMonitoringDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

}