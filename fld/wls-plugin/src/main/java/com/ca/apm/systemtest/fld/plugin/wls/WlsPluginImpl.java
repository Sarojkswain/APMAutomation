package com.ca.apm.systemtest.fld.plugin.wls;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.RemoteCallException;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.TrussDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPluginConfiguration.WlsServerConfiguration;

@PluginAnnotationComponent(pluginType = WlsPlugin.WLS_PLUGIN)
@Qualifier(WlsPlugin.WLS_PLUGIN)
public class WlsPluginImpl extends AbstractAppServerPluginImpl<WlsPluginConfiguration> implements WlsPlugin {

    private long WEBLOGIC_STOP_SERVER_WAIT_TIME_MS = 150000;
    private boolean isAgentSet = false;

    public WlsPluginImpl() {
        super(WLS_PLUGIN, WlsPluginConfiguration.class);
    }

    @Override
    protected String getAppServerName() {
        return "weblogic";
    }
    

    @Override
    @ExposeMethod(description = "Configures weblogic not to use any agent")
    public void unsetAgent(Configuration config) {
        setupAgent(config, true);
    }

    @Override
    @ExposeMethod(description = "Configures weblogic to use a specific agent")
    public void setAgent(Configuration config) {
        setupAgent(config, false);
    }

    @Override
    @ExposeMethod(description = "Starts the weblogic server. Returns true if server was started.")
    public boolean startServer(Configuration config) {
        log.info("Starting server {}", config.wlsServerName);

        Path agentJar = Paths.get(config.agentInstallDir + AGENT_JAR_PATH_REL);
        Path agentProfile = Paths.get(config.agentInstallDir + AGENT_PROFILE_PATH_REL);

        ProcessBuilder ps =
            ProcessUtils.newProcessBuilder()
                .command("cmd", "/c", config.wlsServerScriptPath + "startWebLogic.cmd")
                .directory(new File(config.wlsServerScriptPath)).redirectErrorStream(true)
                .redirectOutput(Redirect.appendTo(new File(config.logs, "start.txt")));
        if (isAgentSet) {
            ps.environment()
                .put(
                    "JAVA_OPTIONS",
                    "-javaagent:" + agentJar + " -Dcom.wily.introscope.agentProfile="
                        + agentProfile);
        }
        ProcessUtils.startProcess(ps);
        return true;
    }

    @Override
    @ExposeMethod(description = "Stops the weblogic server. Returns true if server was stopped")
    public boolean stopServer(Configuration config) {
        log.info("Stopping server {}", config.wlsServerName);
        ProcessBuilder ps =
            ProcessUtils.newProcessBuilder()
                .command("cmd", "/c", config.wlsServerScriptPath + "stopWebLogic.cmd")
                .directory(new File(config.wlsServerScriptPath)).redirectErrorStream(true)
                .redirectOutput(Redirect.appendTo(new File(config.logs, "stop.txt")));

        ProcessUtils.startProcess(ps);
        return true;
    }

    public boolean stopServer(String serverName) {
        log.info("stopServer - entry");
        WlsPluginConfiguration pluginConfig = readConfiguration();
        WlsServerConfiguration cfg = pluginConfig.getServerConfig(serverName);
        
        if (cfg == null) {
            String msg = MessageFormat.format("Unable to stop server: target server instance ''{0}'' does not exist", serverName);
            error(msg);
            throw new RemoteCallException(msg);
        }

        int exitValue = -1;
        try {
            log.info("Stopping server {}", serverName);
            Map<String, String> map = new HashMap<>(1);
            map.put("CLASSPATH", ".");
            ProcessExecutor procExecutor = ProcessUtils2.newProcessExecutor()
                .command(cfg.baseDir + "\\bin\\" + "stopWebLogic.cmd")
                .environment(map);
            StartedProcess startedProcess = ProcessUtils2.startProcess(procExecutor);
            Thread.sleep(WEBLOGIC_STOP_SERVER_WAIT_TIME_MS);
            exitValue = startedProcess.getProcess().exitValue();
        } catch (InterruptedException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Error stopping WebLogic. Exception: {0}");
        }
        log.info("Server {0} stopped with exit value {1}", serverName, exitValue);
        boolean exitedOk = (exitValue == 0);
        if (exitedOk) {
            info("Server {0} stopped with exit value {1}", serverName, exitValue);
        } else {
            warn("Server {0} stopped with exit value {1}", serverName, exitValue);
        }
        return exitedOk;
    }

    @Override
    public void stopAppServer(String serverName) {
        stopServer(serverName);
    }

    @Autowired
    TrussDownloadMethod td;

    @Override
    public void installAgent(Configuration config) {
        final String agentName = "Test WLS Agent";

        try {
            Files.createFile(Paths.get("C:\\debug.txt"));
        } catch (IOException e) {
            return;
        }
        // Download agent
        Path installerPath = td.downloadAgent(null, config.codeName, config.buildId, config.buildNumber,
                config.platform == null
                        ? SystemUtil.OperatingSystemFamily.Windows
                        : config.platform);
        System.out.println(installerPath);
        // Response file
        Map<String, String> res = new HashMap<>();
        res.put("USER_INSTALL_DIR", config.agentInstallDir);
        res.put("emHost", config.emHost);
        res.put("agentName", agentName);
        res.put("appServer", SERVER_WEBLOGIC);
        res.put("shouldEnableSPM", "true");

        // Install agent
        installAgent(installerPath.toString() + "/IntroscopeAgent9.7.0.0windows.exe",
            installerPath.toString()
                + "/SampleResponseFile.Agent.txt", res);
        // Configure agent (turn SI off)
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("introscope.agent.deep.instrumentation.enabled", "false");
        configMap.put("introscope.autoprobe.dynamicinstrument.enabled", "true");
        configMap.put("log4j.appender.logfile.File",
            Paths.get(config.logs, agentName + ".IntroscopeAgent.log").toString());
        configMap.put("introscope.autoprobe.logfile",
            Paths.get(config.logs, agentName + ".AutoProbe.log").toString());
        configMap.put("introscope.agent.agentAutoNamingMaximumConnectionDelayInSeconds", "0");
        configMap.put("introscope.agent.decorator.enabled", "true");
        configMap.put("log4j.appender.logfile.MaxFileSize", "50MB");
        configMap.put("introscope.agent.agentAutoNamingEnabled", "false");

        configureAgent(Paths.get(config.agentInstallDir), configMap);

    }

    /**
     * Sets / unsets an agent into Weblogic.
     * 
     * @param config configuration
     * @param unset TRUE if we want to unset the agent (run weblogic without an Agent)
     */
    protected void setupAgent(Configuration config, boolean unset) {

        Path srcFolder =
            Paths.get(config.agentInstallDir, "wily\\examples\\SOAPerformanceManagement\\ext");
        Path destFolder = Paths.get(config.agentInstallDir, "wily\\core\\ext");

        if (Files.exists(srcFolder) && Files.exists(destFolder)) {
            try {
                if (unset) {
                    for (File file : FileUtils.listFiles(srcFolder.toFile(),
                        FileFilterUtils.trueFileFilter(), null)) {
                        FileUtils.deleteQuietly(new File(destFolder.getParent().toFile(), file
                            .getName()));
                    }
                } else {
                    FileUtils.copyDirectoryToDirectory(srcFolder.toFile(), destFolder.toFile());
                }
            } catch (IOException e) {
                throw ErrorUtils.logExceptionAndWrap(log, e,
                    "Cannot copy SOAPerformanceManagement extensions.");
            }
        }
        isAgentSet = !unset;
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl#createStartScript(com.ca.apm.systemtest.fld.plugin.AppServerConfiguration)
     */
    @Override
    protected String createStartScript(AppServerConfiguration serverConfig) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl#startAppServer(java.lang.String)
     */
    @Override
    public void startAppServer(String serverId) {
        log.info("startAppServer - entry");
        WlsPluginConfiguration pluginConfig = readConfiguration();
        WlsServerConfiguration serverConfig = pluginConfig.getServerConfig(serverId);
        
        if (serverConfig == null) {
            log.warn("Server configuration not found for serverId=={}", serverId);
            return;
        }
        
        String command = "noop";
        OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
        switch (osFamily) {
            case Linux:
                command = "./startWebLogic.sh";
                break;
            case Windows:
                command = "startWebLogic.cmd";
                break;
            default:
                log.warn("Unsupported operation system {} exiting", osFamily);
                return;
        }
        try {
            Map<String, String> map = new HashMap<>(1);
            map.put("CLASSPATH", ".");
            ProcessExecutor procExecutor = ProcessUtils2.newProcessExecutor()
                .command(serverConfig.baseDir + "\\" + command)
                .environment(map);
            ProcessUtils2.startProcess(procExecutor);
            Thread.sleep(WEBLOGIC_STOP_SERVER_WAIT_TIME_MS);
            log.info("startAppServer - exit");
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Error starting WebLogic. Exception: {0}");
        }
    }
    
    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl#createServerConfiguration(java.lang.String)
     */
    @Override
    protected AppServerConfiguration createServerConfiguration(String serverId) {
        WlsPluginConfiguration cfg = readConfiguration();
        WlsServerConfiguration sc = new WlsServerConfiguration();
        sc.id = serverId;
        cfg.addServerConfig(serverId, sc);
        saveConfiguration(cfg);
        return sc;
    }

}
