package com.ca.apm.systemtest.fld.plugin.webmethods;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.common.files.InsertPoint;
import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl;
import com.ca.apm.systemtest.fld.plugin.AgentInstallException;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.webmethods.WebmethodsPluginConfiguration.WMVersion;
import com.ca.apm.systemtest.fld.plugin.webmethods.WebmethodsPluginConfiguration
    .WebmethodsServerConfiguration;

import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.systemtest.fld.common.files.FileUtils.insertIntoFileSkipIfAlreadyIncludes;
import static org.apache.commons.io.FileUtils.copyFile;


/**
 * implementation for webmethods plugin interface.
 * @author rsssa02
 */
@PluginAnnotationComponent(pluginType = WebmethodsPlugin.WEBM_PLUGIN)
@Qualifier(WebmethodsPlugin.WEBM_PLUGIN)
public class WebmethodsPluginImpl extends AbstractAppServerPluginImpl<WebmethodsPluginConfiguration>
        implements WebmethodsPlugin {

    public static final InsertPoint[] DEFAULT_IP_MEM_OPTS_INSERT_POINT  = new InsertPoint[] { InsertPoint.endOfFile() };
    public static final InsertPoint[] DEFAULT_IP_AGENT_INSERT_POINT     = new InsertPoint[] { InsertPoint.endOfFile() };


    private static final String MEMOPTS_PARAMETERS_1 = "set JAVA_MAX_MEM=2048M";
    private static final String MEMOPTS_PARAMETERS_2 = "set JAVA_MAX_PERM_SIZE=512M";
    private static final String AGENT_OPTS = "set JAVA_CUSTOM_OPTS=%JAVA_CUSTOM_OPTS% %JAVA_JMX_OPTS% -javaagent:{0}\\wily\\Agent.jar"
            + " -Dcom.wily.introscope.agentProfile="
            + "{0}\\wily\\core\\config\\IntroscopeAgent.profile -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath={0}";

    public final long START_THREAD_BUFFER = 60000;

    private final Map<String, InsertPoint[]> scriptMap = new HashMap<String, InsertPoint[]>();

    static final String SCRIPT_KEY_FORMAT = "{0}:{1}:{2}";

    public WebmethodsPluginImpl() {
        super(WebmethodsPlugin.WEBM_PLUGIN, WebmethodsPluginConfiguration.class);

        for (WMVersion WMVersion : WebmethodsPluginConfiguration.WMVersion.values()) {
            for (SystemUtil.OperatingSystemFamily osVersion : SystemUtil.OperatingSystemFamily.values()) {
                scriptMap.put(MessageFormat.format(SCRIPT_KEY_FORMAT, WMVersion.name(),
                        osVersion.name(), INSERTPOINTKEY_MEMOPTS), DEFAULT_IP_MEM_OPTS_INSERT_POINT);
                scriptMap.put(MessageFormat.format(SCRIPT_KEY_FORMAT, WMVersion.name(),
                        osVersion.name(), INSERTPOINTKEY_AGENT), DEFAULT_IP_AGENT_INSERT_POINT);
            }
        }
    }

    /**
     * Creates a backup copy of the standard startup script used by the server instance,
     * and configures the startup script to use the introscope agent.
     *
     * @param serverConfig - webm server configuration
     * @return The path of the backup copy
     */
    @Override
    @ExposeMethod(description = "startup script for appserver")
    protected String createStartScript(AppServerConfiguration serverConfig) {
        return null;
    }

    @Override
    @ExposeMethod(description = "appserver name")
    protected String getAppServerName() {
        return "default";
    }

    @Override
    @ExposeMethod(description = "server configuration")
    protected AppServerConfiguration createServerConfiguration(String serverId) {
        WebmethodsPluginConfiguration cfg = readConfiguration();
        WebmethodsServerConfiguration sc = new WebmethodsServerConfiguration();
        sc.id = serverId;
        cfg.addServerConfig(serverId, sc);
        saveConfiguration(cfg);
        return sc;
    }

    /**
     * Unsets any agent settings from App server configuration so that the App server can be later
     * started without an agent.
     *
     * @param config plugin configuration
     */
    @Override
    public void unsetAgent(Configuration config) {

    }

    /**
     * Adds the agent into App server configuration file so that the App server can be later started
     * with this agent.
     *
     * @param config plugin configuration
     */
    @Override
    @ExposeMethod(description = "setup configurations")
    public void setAgent(Configuration config) {

    }

    @Override
    @ExposeMethod(description = "setup the appserver")
    public void setupAgent(String serverId, boolean unConfigure, boolean legacy) {
        WebmethodsPluginConfiguration cfg = readConfiguration();
        WebmethodsServerConfiguration serverConfig = cfg.getServerConfig(serverId);
        try {
            info("Server configuration is set, the agent configuration is set to {0}", unConfigure);
            SystemUtil.OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
            switch (osFamily) {
                case Windows: {
                    createWindozeSetEnvScriptNoAgent(serverConfig, unConfigure);
                    break;
                }
                case Linux:
                default: {
                    throw new AgentInstallException("Operating system " + osFamily
                        + " not yet supported", AgentInstallException.ERR_OS_NOT_SUPPORTED);
                }
            }
        } catch (Exception e) {
            if (e instanceof AgentInstallException) {
                throw (AgentInstallException) e;
            }
            throw new AgentInstallException("Unable to configure startup script", e);
        }
    }

    private void createWindozeSetEnvScriptNoAgent(WebmethodsServerConfiguration serverConfig, boolean unConfigure) {
        info("unConfigure: {0}", unConfigure);

        final File orgStartScript = Paths.get(serverConfig.getWebmInstanceHome(), "bin",
            serverConfig.getWebmEnvScript() + ".ORIGINAL").toFile();
        final File startScript = Paths.get(serverConfig.getWebmInstanceHome(), "bin",
            serverConfig.getWebmEnvScript()).toFile();
        final File targetScript = Paths.get(serverConfig.getWebmInstanceHome(), "bin",
            serverConfig.getWebmEnvScript() + ".AGENT").toFile();
        final File targetScriptNoAgent = Paths.get(serverConfig.getWebmInstanceHome(), "bin",
            serverConfig.getWebmEnvScript() + ".NOAGENT").toFile();
        try {
            if (!orgStartScript.exists()) {
                copyFile(startScript, orgStartScript);
            }

            if (unConfigure) {
                if (!targetScriptNoAgent.exists()) {
                    info("Agent will not be configured since the flag unconfigure is {0}",
                        unConfigure);
                    insertMemOpts(serverConfig, startScript, targetScriptNoAgent);
                }
                copyFile(targetScriptNoAgent, startScript);
            } else {
                if (!targetScript.exists()) {
                    info("Agent will be configured since the flag unconfigure is {0}", unConfigure);
                    insertAgentOpts(serverConfig, startScript, targetScript);
                    insertMemOpts(serverConfig, targetScript, targetScript);
                }
                copyFile(targetScript, startScript);
            }
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to configure startup scripts. Exception: {0}");
        }
    }

    /**
     * Starts the App server.
     *
     * @param config plugin configuration
     * @return true if server was started with return code 0, false otherwise
     */
    @Override
    public boolean startServer(Configuration config) {
        return false;
    }

    @Override
    @ExposeMethod(description = "start the appserver")
    public void startAppServer(String serverId)  {
        WebmethodsPluginConfiguration cfg = readConfiguration();
        WebmethodsServerConfiguration serverConfig = cfg.getServerConfig(serverId);

        Path binDir = Paths.get(serverConfig.getWebmInstanceHome(), "bin");
        info("Starting Trade app from this Dir {0}", binDir.toString());
        ProcessBuilder pb = new ProcessBuilder();
        Map<String, String> env = pb.environment();

        ProcessBuilder ps =
                ProcessUtils.newProcessBuilder()
                        .command(serverConfig.getWebmInstanceHome() + "/bin/server.bat")
                        .directory(binDir.toFile())
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.to(new
                                File(serverConfig.getWebmInstanceHome(), "startLog.txt")));
        ProcessUtils.startProcess(ps);
        try {
            Thread.sleep(START_THREAD_BUFFER);
        } catch (InterruptedException e) {
            log.info("exception",e);
        }
    }

    @Override
    @ExposeMethod(description = "stop the appserver")
    public void stopAppServer(String serverId) {
        WebmethodsPluginConfiguration cfg = readConfiguration();
        WebmethodsServerConfiguration serverConfig = cfg.getServerConfig(serverId);
        ProcessBuilder ps =
                ProcessUtils.newProcessBuilder()
                        .command(serverConfig.getWebmInstanceHome() + "/bin/shutdown.bat")
                        .directory(new File(serverConfig.getWebmInstanceHome() + "/bin/"))
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.to(new
                                File(serverConfig.getWebmInstanceHome(), "stop.txt")));
        Process proc = ProcessUtils.startProcess(ps);
        try {
            Thread.sleep(START_THREAD_BUFFER);
        } catch (InterruptedException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e, "Interrupted sleep. Exception: {0}");
        }
        ProcessUtils.waitForProcess(proc, (int)START_THREAD_BUFFER, TimeUnit.MILLISECONDS, false);
    }
    /**
     * Stops the App server.
     *
     * @param config plugin configuration
     * @return true if server was stopped with return code 0, false otherwise
     */
    @Override
    public boolean stopServer(Configuration config) {
        return false;
    }

    @Override
    @ExposeMethod(description = "install default agent for appserver")
    public void installAgent(Configuration config) {

    }

    private void insertMemOpts(WebmethodsServerConfiguration serverConfig, File source, File target)
            throws IOException {
        // mem opts
        String[] toInsert =
                new String[]{
                        "",
                        "rem --------------- Begin APM Tests Configuration - Memory options ------------------",
                        MEMOPTS_PARAMETERS_1, MEMOPTS_PARAMETERS_2,
                        "rem --------------- End APM Tests Configuration - Memory options ------------------"};
        info("going to insert, using insert point. {0} {1}",source,target);
        InsertPoint[] insertPoints = getInsertPoints(serverConfig, INSERTPOINTKEY_MEMOPTS);
        insertIntoFileSkipIfAlreadyIncludes(source, target, toInsert, insertPoints);
    }

    private void insertAgentOpts(WebmethodsServerConfiguration serverConfig, File source, File target)
            throws IOException {
        // agent opts
        String agentOptions =
                MessageFormat.format(AGENT_OPTS, serverConfig.baseDir);
        String[] toInsert =
                new String[] {
                        "",
                        "rem --------------- Begin APM Tests Configuration - Agent options ------------------",
                        agentOptions,
                        "rem --------------- End APM Tests Configuration - Agent options ------------------"};
        info("going to insert, using insert point. {0} {1}",source,target);
        InsertPoint[] insertPoints = getInsertPoints(serverConfig, INSERTPOINTKEY_AGENT);
        insertIntoFileSkipIfAlreadyIncludes(source, target, toInsert, insertPoints);
    }

    private InsertPoint[] getInsertPoints(WebmethodsServerConfiguration serverConfig, String insertPointKey) {
        info("return map {0}",scriptMap.toString());
        return scriptMap.get(serverConfig.getVersion() + ":" + SystemUtil.OperatingSystemFamily.Windows + ":"
                + insertPointKey);
    }
}
