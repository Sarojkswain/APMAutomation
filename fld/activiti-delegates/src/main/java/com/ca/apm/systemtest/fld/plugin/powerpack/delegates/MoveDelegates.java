package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Factory which provides delegates for moving different file resources. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class MoveDelegates {

    /**
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @param fldLogger
     * @return
     */
    public static AbstractJavaDelegate getMoveTypePerfResultsDelegate(NodeManager nodeManager,
                                                                      AgentProxyFactory agentProxyFactory, 
                                                                      FldLogger fldLogger) {
        return new MoveTypePerfLogsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }
    
    /**
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @param fldLogger
     * @return
     */
    public static AbstractJavaDelegate getMoveJmxResultsDelegate(NodeManager nodeManager,
                                                                 AgentProxyFactory agentProxyFactory, 
                                                                 FldLogger fldLogger) {
        return new MoveJmxLogsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }
    
    /**
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @param fldLogger
     * @return
     */
    public static AbstractJavaDelegate getMoveJmeterResultsDelegate(NodeManager nodeManager,
                                                                    AgentProxyFactory agentProxyFactory, 
                                                                    FldLogger fldLogger) {
        return new MoveJmeterLogsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }
    
    /**
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @param fldLogger
     * @return
     */
    public static AbstractJavaDelegate getMoveJstatResultsDelegate(NodeManager nodeManager,
                                                                   AgentProxyFactory agentProxyFactory, 
                                                                   FldLogger fldLogger) {
        return new MoveJstatLogsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    /**
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @param fldLogger
     * @param appServerPluginProvider
     * @return
     */
    public static AbstractJavaDelegate getMoveIntroscopeAgentLogsDelegate(NodeManager nodeManager,
                                                                          AgentProxyFactory agentProxyFactory, 
                                                                          FldLogger fldLogger, 
                                                                          IAppServerPluginProvider appServerPluginProvider) {
        return new MoveWilyLogsDelegate(nodeManager, agentProxyFactory, fldLogger, appServerPluginProvider);
    }
    
    /**
     * 
     * @param nodeManager
     * @param agentProxyFactory
     * @param fldLogger
     * @return
     */
    public static AbstractJavaDelegate getArchiveLogsDelegate(NodeManager nodeManager,
                                                              AgentProxyFactory agentProxyFactory, 
                                                              FldLogger fldLogger) {
        return new ArchiveServerLogsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }
    
}
