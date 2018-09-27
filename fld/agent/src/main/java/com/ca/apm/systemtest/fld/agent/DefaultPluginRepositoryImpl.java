/**
 *
 */
package com.ca.apm.systemtest.fld.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * Simple implementation that just scans the {@link ApplicationContext} for beans implementing the
 * {@link Plugin} interface.
 *
 * @author keyja01
 */
public class DefaultPluginRepositoryImpl implements PluginRepository, ApplicationContextAware,
    InitializingBean {
    private Logger log = LoggerFactory.getLogger(DefaultPluginRepositoryImpl.class);

    private ApplicationContext ctx;
    private Map<String, Plugin> map;

    /**
     *
     */
    public DefaultPluginRepositoryImpl() {
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        map = new HashMap<String, Plugin>();
        Map<String, Plugin> beans = ctx.getBeansOfType(Plugin.class);

        for (Entry<String, Plugin> entry : beans.entrySet()) {
            Plugin p = entry.getValue();
            PluginAnnotationComponent annotation = ctx
                .findAnnotationOnBean(entry.getKey(), PluginAnnotationComponent.class);

            log.info("Annotation found by context for plugin {}, {}", entry.getKey(), annotation);
            if (annotation == null) {
                // try to find in interface
                Class<?>[] ifaces = p.getClass().getInterfaces();
                for (Class<?> iface : ifaces) {
                    if (Plugin.class.isAssignableFrom(iface)) {
                        // we have a winner
                        annotation = iface.getAnnotation(PluginAnnotationComponent.class);
                        if (annotation != null) {
                            break;
                        }
                    }
                }
            }

            String key = null;
            if (annotation != null) {
                key = annotation.pluginType();
            } else {
                // fall back to the class name
                key = p.getClass().getSimpleName();
            }
            log.info("Registering plugin {}", key);
            map.put(key, p);
        }
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.agent.PluginRepository#findPlugin(java.lang.String)
     */
    public Plugin findPlugin(String name) {
        Plugin p = map.get(name);
        if (log.isDebugEnabled() && (p == null)) {
            log.warn("Unable to find plugin {} in registry", name);
        }
        return p;
    }

    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }

}
