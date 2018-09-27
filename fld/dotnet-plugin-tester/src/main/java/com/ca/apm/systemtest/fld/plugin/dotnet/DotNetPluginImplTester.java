package com.ca.apm.systemtest.fld.plugin.dotnet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;


@Component
public class DotNetPluginImplTester {
    static Logger log = LoggerFactory.getLogger(DotNetPluginImplTester.class);

    @Autowired
    DotNetPlugin dotNetPlugin;

    ClassPathXmlApplicationContext applicationContext;

    @BeforeSuite
    public void beforeSuite() {
        applicationContext = new ClassPathXmlApplicationContext("test-context.xml");
        dotNetPlugin = applicationContext.getBean(DotNetPluginImpl.class);
    }

    @AfterSuite
    public void afterSuite() {
        applicationContext.close();
    }

    //@Test
    public void testInstallAgentFromArtifactory() throws Exception {
        dotNetPlugin.makeInstallPrefix();
        dotNetPlugin.deleteAgentDirectory();
        String file = dotNetPlugin.fetchInstallerArtifactFromArtifactory("9.7.0.23", "64", null);
        log.info("Downloaded artifact: {}", file);
        String installer = dotNetPlugin.unzipInstallerArtifact();
        log.info("Installer: {}", installer);
        Configuration config = new Configuration();
        config.enableSpp = true;
        dotNetPlugin.installAgent("sqw64xeoserv31", 5001, config);
        DotNetPlugin.AgentCheckResult result = dotNetPlugin.checkAgent();
        log.info("Agent check result: {}", result);
        dotNetPlugin.uninstallAgent();
    }

    /**
     * Test installation of agent from truss.
     *
     * @throws Exception
     */
    @Test
    public void testInstallAgentFromTruss() throws Exception {
        dotNetPlugin.makeInstallPrefix();
        dotNetPlugin.deleteAgentDirectory();
        String file = dotNetPlugin
            .fetchInstallerArtifactFromTruss("http://truss.ca.com/builds/InternalBuilds",
                "10.0.0-NET", "990301", "10.0.0.18", "64");
        log.info("Downloaded artifact to {}", file);
        dotNetPlugin.installAgent("sqw64xeoserv31", 5001, null);
        DotNetPlugin.AgentCheckResult result = dotNetPlugin.checkAgent();
        log.info("Agent check result: {}", result);
        dotNetPlugin.uninstallAgent();
    }
}