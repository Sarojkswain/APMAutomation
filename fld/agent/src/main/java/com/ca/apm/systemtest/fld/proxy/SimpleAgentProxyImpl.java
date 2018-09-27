/**
 * 
 */
package com.ca.apm.systemtest.fld.proxy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.plugin.agentdownload.AgentDownloadPlugin;

/**
 * @author keyja01
 *
 */
public class SimpleAgentProxyImpl implements AgentProxy {
	private JmsTemplate jmsTemplate;
	private Map<String, Plugin> plugins = new HashMap<String, Plugin>();
	private Map<Class<? extends Plugin>, Map<String, ? extends Plugin>> pluginClassMap = new HashMap<Class<? extends Plugin>, Map<String, ? extends Plugin>>();

	
    protected FldLogger fldLogger;

//	/**
//	 * Constructor. This one just calls {@link #SimpleAgentProxyImpl(ConnectionFactory, String, Map, String)} with 
//	 * <code>processInstanceId</code> set to <code>null</code>.
//	 * 
//	 * @param connectionFactory      JMS connection factory
//	 * @param target                 target agent this proxy is created for
//	 * @param pluginsConfig          a map containing the name and class of configured plugins on the remote host
//	 */
//	protected SimpleAgentProxyImpl(ConnectionFactory connectionFactory, String target, Map<String, Class<? extends Plugin>> pluginsConfig) {
//	    this(connectionFactory, target, pluginsConfig, null);
//	}
//    
//	/**
//     * Constructor.
//     * 
//     * @param connectionFactory      JMS connection factory
//     * @param target                 target agent this proxy is created for
//     * @param pluginsConfig          a map containing the name and class of configured plugins on the remote host
//     * @param processInstanceId      Activiti process instance id we need to reference historic runs
//     */
//    protected SimpleAgentProxyImpl(ConnectionFactory connectionFactory, String target, Map<String, Class<? extends Plugin>> pluginsConfig, String processInstanceId) {
//        this(connectionFactory, target, pluginsConfig, processInstanceId, null);
//    }
    
	/**
	 * Constructor.
	 * 
     * @param connectionFactory      JMS connection factory
     * @param target                 target agent this proxy is created for
     * @param pluginsConfig          a map containing the name and class of configured plugins on the remote host
	 * @param processInstanceId      Activiti process instance id we need to reference historic runs
	 * @param fldLogger              FLD persistent logger
	 */
	protected SimpleAgentProxyImpl(ConnectionFactory connectionFactory, String target, Map<String, Class<? extends Plugin>> pluginsConfig, 
	                               String processInstanceId, FldLogger fldLogger, InvocationStallListener stallListener, long stallMs) {
        jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.setTimeToLive(30000L);
        
        AgentPluginProxyFactory fact = new AgentPluginProxyFactory(jmsTemplate, target, processInstanceId, fldLogger, stallListener, stallMs);
        
        for (Entry<String, Class<? extends Plugin>> e: pluginsConfig.entrySet()) {
            Plugin plugin = null;
            if (e.getValue().isAssignableFrom(AgentDownloadPlugin.class)) {
                // quick hack to override the timeout for the agent download plugin
                plugin = fact.createJdkProxyWithTimeout(e.getValue(), e.getKey(), 5000L);
            } else {
                plugin = fact.createJdkProxy(e.getValue(), e.getKey());
            }
            plugins.put(e.getKey(), plugin);
            pluginClassMap.put(e.getValue(), Collections.singletonMap(e.getKey(), plugin));
        }
	    
	}

	/* (non-Javadoc)
	 * @see com.ca.apm.systemtest.fld.proxy.AgentProxy#getPlugin(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Plugin> T getPlugin(Class<? extends T> klass) {
		Map<String, ? extends Plugin> plugins = pluginClassMap.get(klass);
		// TODO expand method to return map instead
		T t = (T) plugins.values().iterator().next();
		return t;
	}

	/* (non-Javadoc)
	 * @see com.ca.apm.systemtest.fld.proxy.AgentProxy#getPlugin(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Plugin> T getPlugin(String name, Class<? extends T> klass) {
		return (T) plugins.get(name);
	}

	/* (non-Javadoc)
	 * @see com.ca.apm.systemtest.fld.proxy.AgentProxy#getPlugins()
	 */
	public Map<String, Plugin> getPlugins() {
		return plugins;
	}

	public void setPlugins(Map<String, Plugin> plugins) {
		this.plugins = plugins;
	}
	
	public void setFldLogger(FldLogger fldLogger) {
	    this.fldLogger = fldLogger;
	}
	
}
