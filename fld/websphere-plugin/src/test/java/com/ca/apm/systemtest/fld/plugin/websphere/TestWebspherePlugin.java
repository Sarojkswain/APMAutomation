/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.websphere;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.plugin.cm.ConfigurationManager;
import com.ca.apm.systemtest.fld.plugin.websphere.WebSpherePluginConfiguration.WebSphereServerConfiguration;

/**
 * @author meler02
 *
 */
public class TestWebspherePlugin {

    @Test
    public void testIsServerRunningReturnsFalse() {
        WebspherePlugin plugin = new WebspherePluginImpl();

        String wasUrlString = "http://localhost:9999";

        Assert.assertFalse(plugin.isServerRunning(wasUrlString, 1000));
    }

    @Test
    public void testStartServerFails() throws IOException {
        WebspherePluginImpl plugin = new WebspherePluginImpl();
        

        Path cmd =
                Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "testing_",
                        ".cmd");
        Files.write(cmd, "EXIT /B 1".getBytes());

        WebSphereServerConfiguration wsc = new WebSphereServerConfiguration();
        wsc.id = "someName";
        wsc.wasServerStartScript = cmd.toString();
        wsc.wasServerName = "someName";
        ConfigurationManager cm = mock(ConfigurationManager.class);
        WebSpherePluginConfiguration pluginConfig = new WebSpherePluginConfiguration();
        pluginConfig.addServerConfig(wsc.id, wsc);
        when(cm.loadPluginConfiguration(WebspherePlugin.PLUGIN, WebSpherePluginConfiguration.class)).thenReturn(pluginConfig);
        plugin.setConfigurationManager(cm);

        Assert.assertFalse(plugin.startServer("someName"));

        Files.delete(cmd);
    }

    @Test
    public void testChangeWasConfigSuccessfull() throws IOException {
        WebspherePluginImpl plugin = new WebspherePluginImpl();

        Path xml =
                Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "testing_",
                        ".xml");
        String xmlString =
                "<" + WebspherePlugin.WEBSPHERE_CONFIG_JVM_ELEMENT + " "
                        + WebspherePlugin.WEBSPHERE_CONFIG_JVM_ATTRIBUTE + "=\"\"/>";
        Files.write(xml, xmlString.getBytes());

        ConfigurationManager cm = mock(ConfigurationManager.class);
        WebSpherePluginConfiguration pluginConfig = new WebSpherePluginConfiguration();
        WebSphereServerConfiguration wsc = new WebSphereServerConfiguration();
        wsc.id = "someServer";
        wsc.currentAgentInstallDir = "/somedir";
        wsc.agentInstalled = true;
        wsc.wasConfigFile = xml.toString();
        pluginConfig.addServerConfig(wsc.id, wsc);
        when(cm.loadPluginConfiguration(WebspherePlugin.PLUGIN, WebSpherePluginConfiguration.class)).thenReturn(pluginConfig);
        plugin.setConfigurationManager(cm);

//        plugin.setAgent("someServer");

        Files.delete(xml);
    }

//    @Test(expectedExceptions = RuntimeException.class) //TODO - DM - update using new config
    public void testChangeWasConfigFails() throws IOException {
        WebspherePluginImpl plugin = new WebspherePluginImpl();

        Path xml =
                Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "testing_",
                        ".xml");
        String xmlString = "<InvalidElement/>";
        Files.write(xml, xmlString.getBytes());

        ConfigurationManager cm = mock(ConfigurationManager.class);
        WebSpherePluginConfiguration pluginConfig = new WebSpherePluginConfiguration();
        WebSphereServerConfiguration wsc = new WebSphereServerConfiguration();
        wsc.id = "someServer";
        wsc.currentAgentInstallDir = "/somedir";
        wsc.agentInstalled = true;
        wsc.wasConfigFile = xml.toString();
        pluginConfig.addServerConfig(wsc.id, wsc);
        when(cm.loadPluginConfiguration(WebspherePlugin.PLUGIN, WebSpherePluginConfiguration.class)).thenReturn(pluginConfig);
        plugin.setConfigurationManager(cm);

//        plugin.setAgent("someServer");

        Files.delete(xml);
    }



}
