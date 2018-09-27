package com.ca.apm.systemtest.fld.pplugin.agenthvr;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.agenthvr.AgentHvrPlugin;
import com.ca.apm.systemtest.fld.plugin.agenthvr.AgentHvrPluginImpl;


public class AgentHvrPluginImplTest {
    public static final Logger log = LoggerFactory.getLogger(AgentHvrPluginImplTest.class);

    AgentHvrPlugin agent;

    @Before
    public void before() {
        agent = new AgentHvrPluginImpl();
        agent.createTempDir();
    }

    @Test
    public void test() throws InterruptedException, IOException, URISyntaxException {
        /*File file = agent.downloadManagementModules(null);
        assertNotNull(file);
        log.info("Management Modules downloaded in {} file.", file);

        File dir = agent.unzipManagementModules();
        assertNotNull(dir);
        log.info("Management Modules extracted to {} directory.", dir);

        Files.createDirectories(Paths.get("c:/tmp/mmtest/deploy"));
        agent.configureManagementModule("c:/tmp/mmtest", null);
        */
        /*File file = agent.downloadAgentHvr(null);
        assertNotNull(file);
        log.info("AgentHVR downloaded in {} file.", file);

        File dir = agent.unzipAgentHvrZip();
        assertNotNull(dir);
        log.info("AgentHVR extracted to {} directory.", dir);

        agent.configureExecutables("C:/SW/wily/em", "localhost", "5001", "Admin", "",
            "agentHostName");

        log.info("Executing agent...");
        agent.execute();

        System.out.println("Is agent running: " + agent.checkRunning());
        Thread.sleep(30000);

        agent.stop();
        Thread.sleep(5000);
        System.out.println("Is agent running: " + agent.checkRunning());*/
    }

    @After
    public void after() {
        agent.deleteTempDir();
        agent = null;
    }
}