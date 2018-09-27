package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;


import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Factory class to create Jmeter related task delegates.
 * 
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com) 
 */
public class JmeterDelegates {

    /**
     * Returns a Start Jmeter Load delegate instance.
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @return
     */
    public static AbstractJavaDelegate getJmeterRunDelegate(NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        return new RunJmeterLoadDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    /**
     * 
     * @param   nodeManager
     * @param   agentProxyFactory
     * @param   fldLogger
     * @return
     */
    public static AbstractJavaDelegate getCheckJmeterInstallationStatusDelegate(NodeManager nodeManager, 
                                                                                AgentProxyFactory agentProxyFactory, 
                                                                                FldLogger fldLogger) {
        return new CheckJmeterIsInstalledDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    /**
     * Returns a Check Jmeter Status delegate instance.
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @return
     */
    public static AbstractJavaDelegate getCheckJmeterDelegate(NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        return new CheckJmeterRunStatusDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    /**
     * Returns a Stop Jmeter Load delegate instance.
     *  
     * @param nodeManager
     * @param agentProxyFactory
     * @return
     */
    public static AbstractJavaDelegate getStopJmeterDelegate(NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        return new StopJmeterLoadDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    /**
     * 
     * 
     * @param  nodeManager
     * @param  agentProxyFactoy
     * @param  fldLogger
     * @return
     */
    public static AbstractJavaDelegate getDownloadJmeterDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, 
                                                                 FldLogger fldLogger) {
        return new DownloadJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    /**
     * 
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @param fldLogger
     * @return
     */
    public static AbstractJavaDelegate getInstallJmeterDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, 
                                                                FldLogger fldLogger) {
        return new InstallJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }
    
}
