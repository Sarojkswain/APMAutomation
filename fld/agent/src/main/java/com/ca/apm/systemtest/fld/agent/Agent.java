/**
 *
 */
package com.ca.apm.systemtest.fld.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * @author keyja01
 */
@Component
@EnableScheduling
public class Agent {
    public static ApplicationContext ctx;
    @Autowired
    private PluginRepository pluginRepository;

    public Agent() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String configFile = "fldagent-test-context.xml";
        if (args != null && args.length > 0) {
            configFile = args[0];
        }
        // Creating the context will start up the agent
        ClassPathXmlApplicationContext ctx2 = new ClassPathXmlApplicationContext(configFile);
        ctx = ctx2;
        System.out.println(ctx);

    }


}
