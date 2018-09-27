/**
 *
 */
package com.ca.apm.systemtest.fld.plugin.websphere;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.common.XmlUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.websphere.WebSpherePluginConfiguration
    .WebSphereServerConfiguration;

/**
 * @author meler02
 */
public class WebspherePluginImpl extends
    AbstractAppServerPluginImpl<WebSpherePluginConfiguration> implements WebspherePlugin {

    public static final Logger log = LoggerFactory.getLogger(WebspherePluginImpl.class);

    public WebspherePluginImpl() {
        super(PLUGIN, WebSpherePluginConfiguration.class);
    }

    @Override
    @ExposeMethod(
        description = "Starts the Websphere server. Returns true if server was started with "
            + "return code 0")
    public boolean startServer(String serverName) {
        WebSpherePluginConfiguration pluginConfig = readConfiguration();
        WebSphereServerConfiguration cfg = (WebSphereServerConfiguration) pluginConfig
            .getServerConfig(serverName);
        if (cfg.wasServerName == null) {
            cfg.wasServerName = serverName;
        }
        info("Starting server {0}", serverName);
        ProcessBuilder ps =
            ProcessUtils.newProcessBuilder().command(cfg.wasServerStartScript, cfg.wasServerName);
        Process pr = ProcessUtils.startProcess(ps);
        //Websphere portal can take a while to load, so the value here is minutes.
        int exitValue = ProcessUtils.waitForProcess(pr, 10, TimeUnit.MINUTES, true);
        if (exitValue == 0) {
        	info("Server {0} started. Start command exit value: 0", cfg.wasServerName);
        	return true;
        }
        error("Failed to start server {0}, start command exited with code: {1,number,#} ", cfg.wasServerName, exitValue);
        return false;
    }

    @Override
    public void startAppServer(String serverName) {
        startServer(serverName);
    }

    @Override
    @ExposeMethod(
        description = "Stops the Websphere server. Returns true if server was stopped with return"
            + " code 0")
    public boolean stopServer(String serverName) {
        WebSpherePluginConfiguration pluginConfig = readConfiguration();
        WebSphereServerConfiguration cfg = (WebSphereServerConfiguration) pluginConfig
            .getServerConfig(
                serverName);
        info("Stopping server {0} (saved config server name: {1})", serverName, cfg.wasServerName);
        ProcessBuilder ps =
            ProcessUtils.newProcessBuilder().command(cfg.wasServerStopScript, cfg.wasServerName);
        Process pr = ProcessUtils.startProcess(ps);
        int exitValue = ProcessUtils.waitForProcess(pr, 10, TimeUnit.MINUTES, true);
        if (exitValue == 0) {
        	info("Server {0} stopped with exit value 0", cfg.wasServerName, exitValue);
        	return true;
        }
        error("Failed to stop server {0}, stop command exited with value: {1,number,#}", cfg.wasServerName, exitValue);
        return false;
    }

    @Override
    public void stopAppServer(String serverName) {
        if (isServerRunning(serverName)) {
            stopServer(serverName);
        } else {
            log.info("Server is not running, stop operation is cancelled");
        }
    }


    /**
     * Sets / unsets an agent into Websphere
     *
     * @param serverName Name of server inside config json file
     * @param unset      TRUE if we want to unset the agent (run websphere without an Agent)
     */
    @Override
    @ExposeMethod(description = "Configures Websphere for Agent usage")
    public void setupAgent(String serverName, boolean unset, boolean legacy) {
        WebSpherePluginConfiguration pluginConfig = readConfiguration();
        WebSphereServerConfiguration cfg = (WebSphereServerConfiguration) pluginConfig
            .getServerConfig(serverName);

        //FIXME - DM - really dirty fixes with substrings, but i dont want to brake FLD for now
        Path agentJar = Paths.get(cfg.currentAgentInstallDir + AGENT_JAR_PATH_REL.substring(5));
        Path agentProfile;

        if (legacy) {
            agentProfile = Paths
                .get(cfg.currentAgentInstallDir + AGENT_PROFILE_PATH_REL_LEGACY.substring(5));
        } else {
            agentProfile = Paths.get(
                cfg.currentAgentInstallDir + AGENT_PROFILE_PATH_REL.substring(5));
        }

        try {
            Document document = XmlUtils.openDocument(cfg.wasConfigFile);
            NodeList nodes = document.getElementsByTagName(WEBSPHERE_CONFIG_JVM_ELEMENT);
            if (nodes.getLength() != 1) {
                throw ErrorUtils.logErrorAndThrowException(log,
                    "Error setting startup parameters. No element " + WEBSPHERE_CONFIG_JVM_ELEMENT
                        + " found");
            }
            Node attr = nodes.item(0).getAttributes().getNamedItem(WEBSPHERE_CONFIG_JVM_ATTRIBUTE);
            if (attr == null) {
                throw ErrorUtils.logErrorAndThrowException(log,
                    "Error setting startup parameters. No attribute "
                        + WEBSPHERE_CONFIG_JVM_ATTRIBUTE + " found");
            }

            String agentOptions = getDefaultJmxParameters();

            if (unset) {
                log.info("Unsetting agent");
            } else {
                log.info("Setting agent to {}", agentJar);
                agentOptions = agentOptions + " -javaagent:" + agentJar
                    + " -Dcom.wily.introscope.agentProfile=" + agentProfile;
            }
            attr.setNodeValue(agentOptions);  //TODO - DM - debug/test me here


            XmlUtils.saveDocument(document, cfg.wasConfigFile);
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException
            e) {
            throw ErrorUtils.logExceptionAndWrap(log, e, "Error setting startup parameters");
        }
    }

    @Override
    public boolean isServerRunning(String serverName) {
        WebSpherePluginConfiguration pluginConfig = readConfiguration();
        WebSphereServerConfiguration cfg = (WebSphereServerConfiguration) pluginConfig
            .getServerConfig(serverName);
        if (cfg == null) {
            log.error("Configuration is not found for server: " + serverName);
        }

        return super.isServerRunning("http://localhost:" + cfg.httpPort, 60000);
    }

    @Override
    public boolean isServerStopped(String serverName) {
        WebSpherePluginConfiguration pluginConfig = readConfiguration();
        WebSphereServerConfiguration cfg = (WebSphereServerConfiguration) pluginConfig
            .getServerConfig(serverName);
        return super.isServerStopped("http://localhost:" + cfg.httpPort, 60000);
    }


    @Override
    protected String createStartScript(AppServerConfiguration serverConfig) {
        // TODO Implement me
        return null;
    }


    @Override
    protected String getAppServerName() {
        return "websphere";
    }


    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin
     * .AbstractAppServerPluginImpl#createServerConfiguration(java.lang.String)
     */
    @Override
    protected AppServerConfiguration createServerConfiguration(String serverId) {
        WebSpherePluginConfiguration cfg = readConfiguration();
        WebSphereServerConfiguration sc = new WebSphereServerConfiguration();
        sc.id = serverId;
        cfg.addServerConfig(serverId, sc);
        saveConfiguration(cfg);
        return sc;
    }

    @Override
    public void addPowerPackAttributeToServerConfigFile(String serverId) {
        updatePowerPackAttributeToServerConfigFile(serverId, true);
    }

    @Override
    public void removePowerPackAttributeToServerConfigFile(String serverId) {
        updatePowerPackAttributeToServerConfigFile(serverId, false);
    }

    private void updatePowerPackAttributeToServerConfigFile(String serverId, boolean addOrRemove) {
        log.info("Updating server.xml file with customServices tag start");
        try {
            WebSphereServerConfiguration serverConfig = readConfiguration()
                .getServerConfig(serverId);
            final File inputFile = new File(serverConfig.wasConfigFile);
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(inputFile);
            final Node root = doc.getFirstChild();
            final NodeList lotsOfNodes = root.getChildNodes();

            if (addOrRemove) {
                log.info("Adding customServices attribute");
                if (!doc.getDocumentElement().hasAttribute("xmlns:xmi")) {
                    doc.getDocumentElement().setAttribute("xmlns:xmi", "http://www.omg.org/XMI");
                }

                for (int z = 0; z < lotsOfNodes.getLength(); z++) {
                    if (lotsOfNodes.item(z).getNodeName().equals("customServices")) {
                        log.info("Node for PowerPack configuration is already in server.xml");
                        return;
                    }
                }
                Element childNode = doc.createElement("customServices");
                childNode.setAttribute("classname",
                    "com.wily.introscope.api.websphere.IntroscopeCustomService");
                childNode.setAttribute("classpath", "C:/sw/IBM/wilyref/common/WebAppSupport.jar");
                childNode.setAttribute("displayName", "WebSphere Powerpack Custom service");
                childNode.setAttribute("enable", "true");
                childNode.setAttribute("xmi:id", "CustomService_1444040046981");
                doc.getFirstChild().appendChild(childNode);
            } else {
                log.info("Removing customServices attribute");
                for (int z = 0; z < lotsOfNodes.getLength(); z++) {
                    if (lotsOfNodes.item(z).getNodeName().equals("customServices")) {
                        root.removeChild(lotsOfNodes.item(z));
                    }
                }
            }

            DOMSource source = new DOMSource(doc);
            File outputFile = new File("D:/temp/testXml/server_res.xml");
            StreamResult result = new StreamResult(outputFile);

            TransformerFactory.newInstance().newTransformer().transform(source, result);

        } catch (Exception ex) {
            ErrorUtils.logExceptionFmt(log, ex, "Exception: {0}");
        }
        log.info("Updating server.xml file with customServices tag end");
    }


}
