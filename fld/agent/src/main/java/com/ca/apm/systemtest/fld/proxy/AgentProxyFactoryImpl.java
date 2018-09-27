package com.ca.apm.systemtest.fld.proxy;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.jms.ConnectionFactory;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.plugin.vo.ProcessInstanceIdStore;

/**
 * Creates a JDK proxy that can communicate with the plugins installed on a remote agent. In this
 * implementation, the
 *
 * @author keyja01
 */
public class AgentProxyFactoryImpl implements AgentProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(AgentProxyFactoryImpl.class);
    private ConnectionFactory connFactory;
    private Map<String, Class<? extends Plugin>> pluginsMap;
    private NodeManager nodeManager;

    @Autowired(required=false)
    protected FldLogger fldLogger;
    
    @Autowired
    protected InvocationStallListener stallListener;
    
    @Value("${proxy.stall.ms:30000}")
    protected long stallMs = 31000L;

    /**
     * Public constructor for AgentProxyFactoryImpl.
     *
     * @param basePackages the packages to scan for plugins
     */
    public AgentProxyFactoryImpl(Iterable<String> basePackages) {
        pluginsMap = new TreeMap<>();

        Map<String, List<Class<? extends Plugin>>> map = new HashMap<>(10);
        for (String packageName : basePackages) {
            scanPackageForPlugins(packageName, map);
        }

        for (Entry<String, List<Class<? extends Plugin>>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<Class<? extends Plugin>> list = entry.getValue();
            if (list.size() != 1) {
                logger.warn("Found {} plugins for key {}", list.size(), key);
            }
            Class<? extends Plugin> plugin = list.get(0);
            pluginsMap.put(key, plugin);
            logger.info("Registering plugin {} with interface {}", key, plugin.getName());
        }

        logger.info("Registered {} interfaces", pluginsMap.size());
    }


    /**
     * Scans the specified base package for plugins.  Populates a map containing the name of the
     * plugin, along with its
     * <b>interface</b>.
     *
     * @param packageName
     * @param pluginsMap  the plugins located will be stored in this map
     * @return
     */
    private void scanPackageForPlugins(String packageName,
        Map<String, List<Class<? extends Plugin>>> pluginsMap) {
        logger.info("Scanning package {} for plugins", packageName);
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends Plugin>> set = reflections.getSubTypesOf(Plugin.class);

        for (Class<? extends Plugin> klass : set) {
            List<Class<? extends Plugin>> found = new ArrayList<>(20);
            if (!klass.isAnnotationPresent(PluginAnnotationComponent.class)) {
                continue;
            }
            PluginAnnotationComponent pluginAnnotation = klass
                .getAnnotation(PluginAnnotationComponent.class);
            String key = pluginAnnotation.pluginType();
            Class<? extends Plugin> iface = findPluginInterface(klass);
            if (iface == null) {
                logger.warn("Unable to find Plugin interface for plugin {}", key);
                continue;
            }
            found.add(iface);

            List<Class<? extends Plugin>> plugins = pluginsMap.get(key);
            if (plugins == null) {
                plugins = found;
            } else {
                plugins.addAll(found);
            }
            pluginsMap.put(key, plugins);
        }
    }


    /**
     * Locates the lowest interface in the class hierarchy that extends {@link Plugin}
     *
     * @param klass
     * @return
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Plugin> findPluginInterface(Class<?> klass) {
        if (klass == null) {
            return null;
        }

        if (klass.isInterface()) {
            if (Plugin.class.isAssignableFrom(klass)) {
                return (Class<? extends Plugin>) klass;
            } else {
                return null;
            }
        }

        Class<?>[] interfaces = klass.getInterfaces();
        for (Class<?> iface : interfaces) {
            if (Plugin.class.isAssignableFrom(iface)) {
                return (Class<? extends Plugin>) iface;
            }
        }

        return findPluginInterface(klass.getSuperclass());
    }

    private void warnUnknownNode(String nodeName) {
        if (nodeManager != null && ! nodeManager.checkNodeAvailable(nodeName)) {
            if (logger.isErrorEnabled()) {
                logger.error("Node {} is unknown.", nodeName);
            }
            
            if (fldLogger != null) {
                String msg = MessageFormat.format("Node {0} not found", nodeName);
                fldLogger.error(getClass().getName(), "NODE_NOT_FOUND", msg);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.proxy.AgentProxyFactory#createProxy(java.lang.String)
     */
    @Override
    public AgentProxy createProxy(String target) {
        return createProxy(target, null);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.proxy.AgentProxyFactory#createProxy(java.lang.String, java
     * .lang.String)
     */
    @Override
    public AgentProxy createProxy(String target, String processInstanceId) {
        warnUnknownNode(target);
        return new SimpleAgentProxyImpl(connFactory, target, pluginsMap, 
            ProcessInstanceIdStore.getProcessInstanceId(), fldLogger, stallListener, stallMs);
    }

    @Autowired
    public void setConnectionFactory(ConnectionFactory connFactory) {
        this.connFactory = connFactory;
    }

    @Autowired
    public void setNodeManager(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }
}
