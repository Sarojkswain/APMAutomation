package com.ca.apm.systemtest.fld.plugin.dotnet.powerpack.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.dotnet.DotNetPlugin;
import com.ca.apm.systemtest.fld.plugin.file.transformation.FileTransformationPlugin;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.plugin.windows.perfmon.WindowsPerfmonPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * 
 * @author haiva01
 */
public abstract class AbstractSharePointPpDelegate extends AbstractJavaDelegate {
    public AbstractSharePointPpDelegate(NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory) {
        super(nodeManager, agentProxyFactory);
    }

    public DotNetPlugin getDotNetPlugin(DelegateExecution execution) {
        return loadPlugin(execution, NODE, DotNetPlugin.PLUGIN, DotNetPlugin.class);
    }

    public FileTransformationPlugin getFileTransformationPlugin(DelegateExecution execution) {
        return loadPlugin(execution, NODE, FileTransformationPlugin.PLUGIN,
            FileTransformationPlugin.class);
    }

    public JMeterPlugin getJMeterPlugin(DelegateExecution execution) {
        return loadPlugin(execution, NODE, JMeterPlugin.PLUGIN, JMeterPlugin.class);
    }

    public WindowsPerfmonPlugin getWindowsPerfmonPlugin(DelegateExecution execution) {
        return loadPlugin(execution, NODE, WindowsPerfmonPlugin.PLUGIN, WindowsPerfmonPlugin.class);
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

}