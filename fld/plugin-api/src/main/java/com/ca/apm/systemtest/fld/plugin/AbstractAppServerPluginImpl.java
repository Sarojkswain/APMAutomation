/**
 *
 */
package com.ca.apm.systemtest.fld.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.ca.apm.systemtest.fld.common.ACFileUtils;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.NetworkUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryLiteDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.HttpDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.TrussDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.ca.apm.systemtest.fld.plugin.downloader.InvalidArtifactSpecificationException;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.apm.systemtest.fld.plugin.vo.KeyValuePair;

import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.moveDirectory;
import static org.apache.commons.lang3.StringUtils.isBlank;


/**
 * Abstract Plugin class for application server plugins (Websphere, Weblogic, Tomcat, etc.)
 * <p>
 * Contains some common logic for Agent download/installation/config
 *
 * @author meler02
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractAppServerPluginImpl<T extends AppServerPluginConfiguration> extends AbstractPluginImpl implements AppServerPlugin, InitializingBean {
    // Path to Agent.jar file relative to the installation directory
    public static final String AGENT_JAR_PATH_REL = "/wily/Agent.jar";
    // Path to IntroscopeAgent.profile file relative to the installation directory
    public static final String AGENT_PROFILE_PATH_REL = "/wily/core/config/IntroscopeAgent.profile";
    public static final String AGENT_PROFILE_PATH_REL_LEGACY
        = "/wily/core/config/IntroscopeAgent.NoRedef.profile";

    public static final String SERVER_WEBSPHERE = "WebSphere";
    public static final String SERVER_WEBLOGIC = "WebLogic";
    public static final String SERVER_TOMCAT = "Tomcat";
    protected String jmxPort = "1099";

    protected String artifactoryServer = "";

    private String configName;
    private Class<T> klass;

    @Autowired
    TrussDownloadMethod dm;

    @Autowired
    ArtifactoryLiteDownloadMethod artifactoryLiteDm;

    @Autowired
    HttpDownloadMethod httpDownloadMethod;
    
    public AbstractAppServerPluginImpl(String pluginName, Class<T> klass) {
        this.configName = pluginName;
        this.klass = klass;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        T cfg = readConfiguration();
        info("Plugin " + configName + " loaded configuration " + cfg);
    }

    protected T readConfiguration() {
        T cfg = configurationManager.loadPluginConfiguration(configName, klass);
        return cfg;
    }

    protected void saveConfiguration(T config) {
        configurationManager.savePluginConfiguration(configName, config);
    }

    /**
     * Rewrites properties in a file
     *
     * @param propertiesFilePath path of the Properties file (both input and output)
     * @param propertiesMap      properties to be replaced
     * @param createNonexistent  indicates, whether to create a new file if it doesn't exist
     * @throws IOException in case the properties file cannot be loaded (and
     *                     createNonexistent=false) or saved
     */
    public void configurePropertiesFile(String propertiesFilePath,
        Map<String, String> propertiesMap, boolean createNonexistent)
        throws ConfigurationException {
        PropertiesConfiguration prop = new PropertiesConfiguration();
        try {
            prop.load(propertiesFilePath);
        } catch (ConfigurationException e) {
            ErrorUtils.logExceptionFmt(log, e, "Exception: {0}");
            if (createNonexistent) {
                log.warn("Cannot load config file '{}'. Creating new...", propertiesFilePath);
            } else {
                throw e;
            }
        }
        if (propertiesMap != null) {
            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                if (log.isDebugEnabled())
                    log.debug("Config file property: {} = {}", entry.getKey(), entry.getValue());
                prop.setProperty(entry.getKey(), entry.getValue());
            }
        }
        prop.save(propertiesFilePath);
    }

    /**
     * A convenience method to create a the properties map for Truss downloader
     *
     * @return
     */


    /**
     * A convenience method to create a response file map from some common properties
     *
     * @return
     */
    protected static Map<String, String> createResponseFileMap(String installDir, String emHost,
        String agentName, String appServer, boolean enableSPM) {
        Map<String, String> map = new HashMap<>(5);
        map.put("USER_INSTALL_DIR", installDir);
        map.put("emHost", emHost);
        map.put("agentName", agentName);
        map.put("appServer", appServer);
        map.put("shouldEnableSPM", enableSPM ? "true" : "false");
        return map;
    }

    public void configureAgent(Path agentInstallDir, Map<String, String> propertiesMap) {
        try {
            configurePropertiesFile(agentInstallDir + AGENT_PROFILE_PATH_REL, propertiesMap, false);
        } catch (ConfigurationException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e, "Error configuring agent");
        }
    }

    public void configureAgent(Path agentInstallDir, Map<String, String> propertiesMap,
        boolean legacy, String serverId) {
        if (legacy) {
            try {
                if(serverId.contains("webspherePortal")) {
                    configurePropertiesFile(agentInstallDir + AGENT_PROFILE_PATH_REL_LEGACY,
                            propertiesMap, false);
                } else {
                    configurePropertiesFile(agentInstallDir + AGENT_PROFILE_PATH_REL,
                            propertiesMap, false);
                }

            } catch (ConfigurationException e) {
                throw ErrorUtils.logExceptionAndWrap(log, e, "Error configuring agent");
            }
        } else {
            configureAgent(agentInstallDir, propertiesMap);
        }
    }


    private static final File BRTM_EXT_JAR
        = Paths.get("examples", "APM", "BRTM", "ext", "BrtmExt.jar").toFile();
    private static final File BROWSER_AGENT_EXT_JAR
        = Paths.get("examples", "APM", "BrowserAgent", "ext", "BrowserAgentExt.jar").toFile();


    private enum BrtmKind {
        BRTM(BRTM_EXT_JAR, "brtm.pbd"),
        BROWSER_AGENT(BROWSER_AGENT_EXT_JAR, "browseragent.pbd");

        public final File extPath;
        public final String pbdFile;

        BrtmKind(File extPath, String pbdFile) {
            this.extPath = extPath;
            this.pbdFile = pbdFile;
        }
    }


    private BrtmKind detectBrtmKind(File wilyBase) {
        File brtmExtJar = BRTM_EXT_JAR;
        if (new File(wilyBase, brtmExtJar.toString()).exists()) {
            return BrtmKind.BRTM;
        }

        brtmExtJar = BROWSER_AGENT_EXT_JAR;
        if (new File(wilyBase, brtmExtJar.toString()).exists()) {
            return BrtmKind.BROWSER_AGENT;
        }

        throw new AgentInstallException(
            "Could not find appropriate BRTM or Browser Agent extension jar file.");
    }


    private static <T> void appendToValue(Map<T, String> map, T key, CharSequence separator,
        String extraContent) {
        String existing = map.get(key);
        map.put(key, existing == null ? extraContent : existing + separator + extraContent);
    }


    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.AppServerPlugin#installAgentNoInstaller(java.lang.String, java.lang.String, java.util.Properties)
     */
    @Override
    public void installAgentNoInstaller(String serverId, String noInstallerSpecification,
        Map<String,String> extraProperties, List<String> extraModules, boolean brtmExt) {

        T config = readConfiguration();
        AppServerConfiguration serverConfig = config.getServerConfig(serverId);

        if (serverConfig == null) {
            throw new AgentInstallException("Unable to install agent - target server instance "
                + serverId + " does not exist", AgentInstallException.ERR_SERVER_INSTANCE_MISSING);
        }

        if (!(new File(serverConfig.baseDir).exists())) {
            log.error("Server installation folder does not exist");
            throw new AgentInstallException("Server installation folder does not exist",
                AgentInstallException.ERR_SERVER_NOT_INSTALLED);
        }

        if (serverConfig.agentInstalled) {
            throw new AgentInstallException("Agent is already installed, according to config file",
                AgentInstallException.ERR_AGENT_ALREADY_INSTALLED);
        }

        File tmpDirectory = createTempDirectory(getAppServerName() + "Plugin");
        // verify that the server instance exists, if not, fail

        ArtifactFetchResult result = handlePluginConfig(noInstallerSpecification, tmpDirectory);

        // at this point it is downloaded into a temp directory

        // 1. Unpack the file
        File outputDir = new File(tmpDirectory, "unpack");
        try {
            ACFileUtils.unpackFile(result.getFile(), outputDir);
        } catch (Exception e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Error unpacking agent no-installer archive. Exception: {0}");
        }
        
        String defaultAgentInstallDirName = isBlank(serverConfig.defaultAgentInstallDir) ? "wily" : serverConfig.defaultAgentInstallDir;

        File wilyBase = new File(outputDir, defaultAgentInstallDirName);

        // 1.2 Set up properties for BRTM.
        BrtmKind brtmKind = null;
        Map<String, String> propertiesMap = new LinkedHashMap<>(extraProperties.size() + 3);
        if (brtmExt) {
            // Detect whether this agent has BRTM or Browser Agent extensions.
            brtmKind = detectBrtmKind(wilyBase);

            switch (brtmKind) {
                case BRTM:
                    propertiesMap.put("introscope.agent.brtm.jsFunctionMetricsEnabled", "true");
                    propertiesMap.put("introscope.agent.brtm.ajaxMetricsEnabled", "true");
                    propertiesMap.put("introscope.agent.brtm.enabled", "true");
                    break;

                case BROWSER_AGENT:
                    propertiesMap
                        .put("introscope.agent.browseragent.jsFunctionMetricsEnabled", "true");
                    propertiesMap.put("introscope.agent.browseragent.ajaxMetricsEnabled", "true");
                    propertiesMap.put("introscope.agent.browseragent.enabled", "true");
                    break;

                default:
                    throw ErrorUtils.logErrorAndReturnException(log,
                        "Unexpected BrtmKind: {0}", brtmKind);
            }
        }
        propertiesMap.putAll(extraProperties);
        if (brtmExt) {
            appendToValue(propertiesMap, "introscope.autoprobe.directivesFile", ",",
                brtmKind.pbdFile);
        }

        // 2. apply the required properties to IntroscopeAgent.profile
        Path agentInstallDir = outputDir.toPath();
        boolean legacy = noInstallerSpecification.endsWith("legacy");
        configureAgent(agentInstallDir, propertiesMap, legacy, serverId);

        File targetDir = new File(wilyBase, "/core/ext");

        // 2.5 pick the right module, either BRTM or Browser Agent, jar file.
        if (brtmExt) {
            extraModules.add(brtmKind.extPath.getPath());
        }

        // 3. Install required modules
        if (extraModules != null) {
            for (String module: extraModules) {
                OperatingSystemFamily os = SystemUtil.getOsFamily();
                if (os == OperatingSystemFamily.Linux) {
                    module = module.replace('\\', '/');
                } else if (os == OperatingSystemFamily.Windows) {
                    module = module.replace('/', '\\');
                }

                File src = new File(wilyBase, module);
                File tgt = new File(targetDir, src.getName());
                if (src.exists()) {
                    try {
                        Files.copy(src.toPath(), tgt.toPath(), StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES);
                    } catch (IOException e) {
                        // TODO do we really want to completely fail, or just log the error?
                        ErrorUtils.logExceptionFmt(log, e,
                            "Error installing agent - could not move required extension module "
                                + "{1}. Exception: {0}",
                            module);
                    }
                }
            }
        }

        // 4. Move the agent files to the app server's directory
        File dest = new File(serverConfig.baseDir, defaultAgentInstallDirName);
        if (dest.exists() && dest.isDirectory()) {
            log.error("Warning: agent not installed, but target directory {} already exists",
                dest.getAbsolutePath());
            // TODO Throw exception?
            //AgentInstallException.ERR_TARGET_DIR_NOT_EMPTY
            try {
                FileUtils.deleteDirectory(dest); //TODO - DM  - check why?
            } catch (Exception ex) {
                throw new AgentInstallException("Cannot remove old agent directory", ex);
            }
        }
        try {
            moveDirectory(wilyBase, dest);
        } catch (FileExistsException e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Can't move, because file exists at {1}. Exception: {0}", dest.getPath());
        } catch (IOException e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Error installing agent - could not move wily to {1}. Exception: {0}",
                dest.getPath());
        }

        // 5. Make a copy of the server instance's startup file
        serverConfig.startScriptWithAgent = createStartScript(serverConfig);

        // 6. And save the status
        serverConfig.agentInstalled = true;
        serverConfig.currentAgentInstallDir = dest.toString();
        saveConfiguration(config);

        info("Installation complete ok");
    }

    public ArtifactFetchResult handlePluginConfig(String noInstallerSpecification, File tmpDirectory) {
        if (noInstallerSpecification.startsWith("http:") || noInstallerSpecification.startsWith("https:")) {
            try {
                return httpDownloadMethod.fetch(noInstallerSpecification, tmpDirectory, true);
            } catch (ArtifactManagerException e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e, 
                    "Failed to download Introscope Agent artifact from {1}", noInstallerSpecification); 
            }
        }
        
        if (noInstallerSpecification.startsWith("truss")) {
            return dm.fetchResultFromDownloadSource(noInstallerSpecification, tmpDirectory, getAppServerName());
        }

        if (noInstallerSpecification.startsWith("maven")) {
            String[] splits = noInstallerSpecification.split(":");

            if (splits.length != 2) {
                String message = "Wrong number of arguments";
                throw ErrorUtils.logExceptionAndWrap(log, new InvalidArtifactSpecificationException(message), message);
            }

            String VERSION = splits[1];
            String groupID = "com.ca.apm.delivery";
            String artifactId = "agent-noinstaller-" + getAppServerName().toLowerCase() + "-";

            switch (SystemUtil.getOsFamily()) {
                case Linux:
                    artifactId = artifactId + "unix";
                    break;
                case Windows:
                    artifactId = artifactId + "windows";
                    break;
                default:
                    throw new AgentInstallException("Operating system " + SystemUtil.getOsFamily() + " not yet supported", AgentInstallException.ERR_OS_NOT_SUPPORTED);
            }

            String classifier = null;
            String type = "zip";

            return artifactoryLiteDm.fetchTempArtifact(ArtifactoryLiteDownloadMethod.DEFAULT_ARTIFACTORY_URL, groupID, artifactId, VERSION, classifier, type);
        }

        throw new AgentInstallException("Installation method is not http, truss or maven");
    }


    @Override
    public void uninstallAgentNoInstaller(String serverId, boolean archiveExisting) {
        info("Uninstalling agent for " + serverId);
        T config = readConfiguration();

        // verify that the server instance exists, if not, fail
        AppServerConfiguration serverConfig = config.getServerConfig(serverId);
        if (serverConfig == null) {
            error("No such server instance found: " + serverId);
            throw new AgentInstallException("Unable to uninstall agent - target server instance " + serverId + " does not exist",
                AgentInstallException.ERR_SERVER_INSTANCE_MISSING);
        }

        File agentInstallDir = new File(serverConfig.baseDir + "/" + serverConfig.defaultAgentInstallDir);

        if (!agentInstallDir.exists()) {
            info("Agent not installed, skipping uninstall");
            serverConfig.agentInstalled = false;
            // TODO [TB] - consider saving configuraton here:
            //saveConfiguration(config);
            return;
        }
        File baseDir = new File(serverConfig.baseDir);
        
        if (archiveExisting) {
            new File(baseDir, "wilyArchive").mkdirs();
            int tryCounter = 10;
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmm_SSS");
            File newAgentDir = null;
            String newName = null;
            while (tryCounter-- > 0) {
                String date = fmt.format(new Date());
                newName = "wily_" + date;
                newAgentDir = new File(baseDir + "/wilyArchive", newName);
                if (newAgentDir.exists()) {
                    log.info("Agent archive folder {} already exists, trying another name", newAgentDir);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        error("Thread interrupted while sleeping for 500 milliseconds: ", e);
                    }
                } else {
                    break;
                }
            }

            try {
                copyDirectory(agentInstallDir, newAgentDir);
                deleteDirectory(agentInstallDir);
            } catch (IOException e) { //TODO - DM - catch correct error if server is still running
                String msg = MessageFormat.format("Unable to uninstall agent: unable to move agent from ''{0}'' to ''{1}''", 
                    agentInstallDir.getPath(), newName);
                error(msg, e);
                throw new AgentInstallException(msg, e);
            }
        } else {
            try {
                FileUtils.deleteDirectory(agentInstallDir);
            } catch (IOException e) {
                String msg = MessageFormat.format("Unable uninstall agent: unable to delete agent directory ''{0}''", agentInstallDir.getPath());
                error(msg, e);
                throw new AgentInstallException(msg, e);
            }
        }

        serverConfig.agentInstalled = false;
        saveConfiguration(config);
        info("Uninstalling agent for " + serverId + " complete");
    }



    /**
     * Creates a backup copy of the standard startup script used by the server instance,
     * and configures the startup script to use the introscope agent.
     * @param serverConfig
     * @return The path of the backup copy
     */
    protected abstract String createStartScript(AppServerConfiguration serverConfig);

    protected abstract String getAppServerName();


    public boolean installAgent(String agentInstallationFile, String responseFilePath,
            Map<String, String> propertiesMap) {
        try {
            configurePropertiesFile(responseFilePath, propertiesMap, true);
        } catch (ConfigurationException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e, "Error configuring agent");
        }
        //
        log.info("Starting Agent installation from '{}', response file {}", agentInstallationFile,
            responseFilePath);
        ProcessBuilder ps =
                ProcessUtils.newProcessBuilder().command(agentInstallationFile, "-f",
                        responseFilePath);
        Process pr = ProcessUtils.startProcess(ps);
        int exitValue = ProcessUtils.waitForProcess(pr, 3, TimeUnit.MINUTES, true);
        log.info("Agent installation completed with exit value {}", exitValue);
        return exitValue == 0;
    }

    public boolean isServerRunning(String urlString, int timeout) {
        return isServerState(urlString, timeout, true);
    }

    public boolean isServerStopped(String urlString, int timeout) {
        return isServerState(urlString, timeout, false);
    }

    /**
     * Check the service status. The call will wait for server to start listening or stop listening depending on <code>running</code> parameter
     *
     * @param urlString server name and port
     * @param timeout how long should wait in milliseconds
     * @param running if true wait for server start, false wait for shut down
     * @return is server started/stopped before timeout
     */
    protected static boolean isServerState(String urlString, int timeout, boolean running) {
        boolean isListening;
        try {
            URL url = new URL(urlString);
            isListening = false;
            for (int i = 0; i <= timeout / 1000; i++) {
                isListening = NetworkUtils.isServerListening(url.getHost(), url.getPort());
                if (running ? isListening : !isListening) {
                    break;
                }

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {}
            }

            if (running) {
                if (!isListening) {
                    log.info("Server is not running at {}", urlString);
                } else {
                    log.info("Server is running at {}", urlString);
                }
            } else {
                if (isListening) {
                    log.error("Server is running at {}", urlString);
                } else {
                    log.info("Server is not running at {}", urlString);
                }
            }

            return running ? isListening : !isListening;

        } catch (MalformedURLException e) {
            log.error("Invalid URL {}", urlString);
            return false;
        }
    }





    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.AppServerPlugin#configureServerInstance(java.lang.String, java.lang.String, int, com.ca.apm.systemtest.fld.plugin.vo.KeyValuePair[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public void configureServerInstance(String serverId, int httpPort, KeyValuePair... values) {
        T pluginConfig = readConfiguration(); // it returns deep copy and causes overwriting values
        AppServerConfiguration sc = pluginConfig.getServerConfig(serverId);
        if (sc == null) { //TODO test
            sc = createServerConfiguration(serverId);
            pluginConfig.addServerConfig(serverId, sc);
        }

        sc.httpPort = httpPort;
        DirectFieldAccessor wrapper = new DirectFieldAccessor(sc);
        for (KeyValuePair kvp: values) {
            try {
                wrapper.setPropertyValue(kvp.getKey(), kvp.getValue());
            } catch (InvalidPropertyException ipe) {
                log.error("InvalidPropertyException, unable to set property {} on class {}",
                    kvp.getKey(), sc.getClass().getName(), ipe);
            } catch (PropertyAccessException pae) {
                log.error("PropertyAccessException, unable to set property {} on class {}",
                    kvp.getKey(), sc.getClass().getName(), pae);
            } catch (Exception e) {
                log.error("Unable to set property {} on class {}", kvp.getKey(),
                    sc.getClass().getName(), e);
            }
        }

        saveConfiguration(pluginConfig);
    }


    protected abstract AppServerConfiguration createServerConfiguration(String serverId);


    @Override
    public boolean isAgentInstalled(String serverId) {
        AppServerConfiguration serverConfig = getAppServerConfiguration(serverId);

        if (serverConfig == null) {
            error("Server configuration '" + serverId +"' not found");
            throw new AgentInstallException("Configuration '" + serverId + "' cannot be found in config file", AgentInstallException.ERR_SERVER_INSTANCE_MISSING);
        }
        return serverConfig.agentInstalled;
    }

    @Override
    public AppServerConfiguration getAppServerConfiguration(String serverId) {
        T config = readConfiguration();
        return config.getServerConfig(serverId);
    }

    public void setArtifactoryLiteDm(ArtifactoryLiteDownloadMethod artifactoryLiteDm) {
        this.artifactoryLiteDm = artifactoryLiteDm;
    }

    @Override
    public void startAppServer(String serverId) {
        throw new UnsupportedOperationException("startAppServer not implemented yet for " + getClass().getSimpleName());
    }

    @Override
    public void stopAppServer(String serverId) {
        throw new UnsupportedOperationException("stopAppServer not implemented yet for " + getClass().getSimpleName() +
                ". Override method stopAppServer inside your plugin and call method.");
    }

    @Override
    public void setupAgent(String serverName, boolean unset, boolean legacy) {
        throw new UnsupportedOperationException("setupAgent not implemented yet for " + getClass().getSimpleName());
    }

    /**
     * Returns default JMX string
     * You can set port for this JMX string by executing setJmxPort
     * @return
     */
    protected String getDefaultJmxParameters() {
        return String.format("-Djavax.management.builder.initial= -Dcom.sun.management.jmxremote "
            + "-Dcom.sun.management.jmxremote.authenticate=false "
            + "-Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=" + jmxPort);
    }

    public void setJmxPort(String jmxPort) {
        this.jmxPort = jmxPort;
    }


}
