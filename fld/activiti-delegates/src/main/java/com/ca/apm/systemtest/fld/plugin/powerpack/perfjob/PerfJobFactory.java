/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.powerpack.perfjob;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author keyja01
 *
 */

@Component("perfJobFactory")
public class PerfJobFactory implements InitializingBean, ApplicationContextAware {
    private Logger logger = LoggerFactory.getLogger(PerfJobFactory.class);

    private Map<String, PerfJob> map = new HashMap<>();
    private ApplicationContext applicationContext;

    public PerfJob getPerfJob(String name) {
        return map.get(name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        map = applicationContext.getBeansOfType(PerfJob.class);
        logger.debug("PerfJob map: ");
        if (map != null) {
            for (Map.Entry<String, PerfJob> perfJobEntry : map.entrySet()) {
                logger.debug("{}: {}", perfJobEntry.getKey(),
                    perfJobEntry.getValue().getClass().getName());
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}