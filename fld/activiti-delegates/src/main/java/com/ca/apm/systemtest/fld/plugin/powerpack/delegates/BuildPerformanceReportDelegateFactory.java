package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Factory class to create delegate instances that build performance test result reports.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class BuildPerformanceReportDelegateFactory {

    public static AbstractJavaDelegate createBuildReportDelegate(NodeManager nodeManager,
                                                                 AgentProxyFactory agentProxyFactory, 
                                                                 FldLogger fldLogger) {
        return new BuildPowerPackPerformanceTestReportDelegate(nodeManager, agentProxyFactory, fldLogger);
    }
    
}
