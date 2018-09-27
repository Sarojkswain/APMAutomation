package com.ca.apm.systemtest.fld.plugin.wls.powerPack;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.FileCopyUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.common.files.FileUtils;
import com.ca.apm.systemtest.fld.common.files.InsertPoint;
import com.ca.apm.systemtest.fld.plugin.AgentInstallException;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPluginConfiguration;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPluginConfiguration.WLVersion;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPluginConfiguration.WlsServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPluginImpl;

@PluginAnnotationComponent(pluginType = PowerPackWlsPlugin.POWERPACK_WLS_PLUGIN)
@Qualifier(PowerPackWlsPlugin.POWERPACK_WLS_PLUGIN)
public class PowerPackWlsPluginImpl extends WlsPluginImpl implements PowerPackWlsPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerPackWlsPluginImpl.class);

    public static final InsertPoint[] DEFAULT_IP_JAVA_OPTS_INSERT_POINT = new InsertPoint[] { InsertPoint.before("^@REM We need to reset the value of JAVA_HOME to get it shortened AND ") };
    public static final InsertPoint[] DEFAULT_IP_MEM_OPTS_INSERT_POINT  = new InsertPoint[] { InsertPoint.after("^set MEM_MAX_PERM_SIZE=.*") };
    public static final InsertPoint[] DEFAULT_IP_JMX_INSERT_POINT       = new InsertPoint[] { InsertPoint.endOfFile() };
    public static final InsertPoint[] DEFAULT_IP_GC_INSERT_POINT        = new InsertPoint[] { InsertPoint.endOfFile() };
    public static final InsertPoint[] DEFAULT_IP_AGENT_INSERT_POINT     = new InsertPoint[] { InsertPoint.endOfFile() };



    static final String PATH_SUFFIX_START_SCRIPT = "\\bin\\setDomainEnv.cmd";
    static final String PATH_SUFFIX_START_SCRIPT_ORIGINAL = "\\bin\\setDomainEnv.cmd.ORIGINAL";
    static final String PATH_SUFFIX_START_SCRIPT_NOAGENT = "\\bin\\setDomainEnv.cmd.NOAGENT";
    static final String PATH_SUFFIX_START_SCRIPT_AGENT = "\\bin\\setDomainEnv.cmd.AGENT";

    static final String JAVAOPTS_PARAMETERS_1 = "set JAVA_HOME=%SUN_JAVA_HOME%";
    static final String JAVAOPTS_PARAMETERS_2 = "set JAVA_VENDOR=Sun";

    static final String MEMOPTS_PARAMETERS_1 = "set MEM_ARGS=-Xms256m -Xmx1024m";
    static final String MEMOPTS_PARAMETERS_2 =
        "set MEM_MAX_PERM_SIZE=-XX:PermSize=128m -XX:MaxPermSize=256m";

    static final String JMX_OPTS = "set JAVA_VM=%JAVA_VM% " + "-Dcom.sun.management.jmxremote "
        + "-Dcom.sun.management.jmxremote.ssl=false "
        + "-Dcom.sun.management.jmxremote.authenticate=false " + "-Djava.net.preferIPv4Stack=true "
        + "-Dcom.sun.management.jmxremote.port={0}";

    static final String GC_OPTS = "set JAVA_VM=%JAVA_VM% -XX:+UseSerialGC"; // this option is
                                                                            // related to
                                                                            // WeblogicPortalPowerPackImpl.MonitorDelegate.JMX_METRICS_serialGc

    static final String AGENT_OPTS =
        "set JAVA_VM=%JAVA_VM% -javaagent:{0}\\{1}\\Agent.jar -Dcom.wily.introscope.agentProfile={0}\\{1}\\core\\config\\IntroscopeAgent.profile";

    static final String SCRIPT_KEY_FORMAT = "{0}:{1}:{2}";
    

    private final Map<String, InsertPoint[]> scriptMap = new HashMap<String, InsertPoint[]>();

    public PowerPackWlsPluginImpl() {
        super();

        for (WLVersion wlVersion : WlsPluginConfiguration.WLVersion.values()) {
            for (SystemUtil.OperatingSystemFamily osVersion : SystemUtil.OperatingSystemFamily.values()) {
                scriptMap.put(MessageFormat.format(SCRIPT_KEY_FORMAT, wlVersion.name(), 
                    osVersion.name(), INSERTPOINTKEY_JAVAOPTS), DEFAULT_IP_JAVA_OPTS_INSERT_POINT);
                scriptMap.put(MessageFormat.format(SCRIPT_KEY_FORMAT, wlVersion.name(), 
                    osVersion.name(), INSERTPOINTKEY_MEMOPTS), DEFAULT_IP_MEM_OPTS_INSERT_POINT);
                scriptMap.put(MessageFormat.format(SCRIPT_KEY_FORMAT, wlVersion.name(), 
                    osVersion.name(), INSERTPOINTKEY_JMX), DEFAULT_IP_JMX_INSERT_POINT);
                scriptMap.put(MessageFormat.format(SCRIPT_KEY_FORMAT, wlVersion.name(), 
                    osVersion.name(), INSERTPOINTKEY_GC), DEFAULT_IP_GC_INSERT_POINT);
                scriptMap.put(MessageFormat.format(SCRIPT_KEY_FORMAT, wlVersion.name(), 
                    osVersion.name(), INSERTPOINTKEY_AGENT), DEFAULT_IP_AGENT_INSERT_POINT);
            }
        }
    }

    public InsertPoint[] setScriptInsertPoints(String wlsVersion, String osVersion, String key, InsertPoint[] insertPoint) {
        return scriptMap.put(MessageFormat.format(SCRIPT_KEY_FORMAT, wlsVersion, osVersion, key), insertPoint);
    }
    
    @Override
    @ExposeMethod(description = "Sets or replaces 'after' insert point for Weblogic startup scripts.")
    public void setInsertAfterPoints(String wlsVersion, String osVersion, String key, String[] searchAfterTexts, boolean replace) {
        if (searchAfterTexts == null || searchAfterTexts.length == 0) {
            throw new IllegalArgumentException("searchAfterTexts array must not be null!");
        }
        String mapKey = MessageFormat.format(SCRIPT_KEY_FORMAT, wlsVersion, osVersion, key);
        
        int numOfNewInsPoints = searchAfterTexts.length;
        ArrayList<InsertPoint> newInsertPointsList = new ArrayList<InsertPoint>(numOfNewInsPoints); 
        for (String searchAfterText : searchAfterTexts) {
            if (searchAfterText == null) {
                throw new NullPointerException("searchAfterText must not be null!");
            }
            newInsertPointsList.add(InsertPoint.after(searchAfterText));
        }
        InsertPoint[] newInsertPoints = newInsertPointsList.toArray(new InsertPoint[numOfNewInsPoints]);
        InsertPoint[] result = null;
        if (replace) {
            result = newInsertPoints;
        } else {
            InsertPoint[] existingInsPoints = scriptMap.get(mapKey);
            int numOfExistingInsPoints = existingInsPoints != null ? existingInsPoints.length : 0;
            result = new InsertPoint[numOfExistingInsPoints + numOfNewInsPoints];
            
            if (numOfExistingInsPoints > 0) {
                System.arraycopy(existingInsPoints, 0, result, 0, numOfExistingInsPoints);
            }
            
            System.arraycopy(newInsertPoints, 0, result, numOfExistingInsPoints, numOfNewInsPoints);
        }
        
        InsertPoint[] previousInsertPoints = scriptMap.put(mapKey, result);
        LOGGER.info("Replacing {} insert points with {}", Arrays.toString(previousInsertPoints), 
            Arrays.toString(result));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ca.apm.systemtest.fld.plugin.wls.WlsPluginImpl#createStartScript(com.ca.apm.systemtest
     * .fld.plugin.AppServerConfiguration)
     * 
     * This method is called during installAgentNoInstaller()
     */
    @Override
    protected String createStartScript(AppServerConfiguration serverConfig) {
        LOGGER.info("createStartScript - entry");

        try {
            OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
            switch (osFamily) {
                case Windows: {
                    return createWindozeSetEnvScriptAgent((WlsServerConfiguration) serverConfig);
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

    @Override
    public void uninstallAgentNoInstaller(String serverId, boolean archiveExisting) {
        LOGGER.info("uninstallAgentNoInstaller - entry");

        super.uninstallAgentNoInstaller(serverId, archiveExisting);
        setupAgent(serverId, true, false);

        LOGGER.info("uninstallAgentNoInstaller - exit");
    }

    @Override
    @ExposeMethod(description = "Configures Weblogic Portal for Agent usage")
    public void setupAgent(String serverName, boolean unset, boolean legacy) {
        LOGGER.info("setupAgent - entry");

        try {
            WlsPluginConfiguration pluginConfig = readConfiguration();
            WlsServerConfiguration serverConfig = pluginConfig.getServerConfig(serverName);

            OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
            switch (osFamily) {
                case Windows: {
                    if (unset) {
                        createWindozeSetEnvScriptNoAgent(serverConfig);
                    } else {
                        createWindozeSetEnvScriptAgent(serverConfig);
                    }
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

        LOGGER.info("setupAgent - exit");
    }

    @Override
    public void stopAppServer(String serverName) {
        WlsPluginConfiguration pluginConfig = readConfiguration();
        WlsServerConfiguration serverConfig = pluginConfig.getServerConfig(serverName);
        switch (serverConfig.getVersion()) {
            case WEBLOGICPORTAL_103: {
                LOGGER.info("stopAppServer - going to call stopWLPortal");
                stopWLPortal(serverName, serverConfig);
                break;
            }
            case WEBLOGIC_103:
            default: {
                super.stopAppServer(serverName);
                break;
            }
        }
    }

    public void stopWLPortal(String serverName, WlsServerConfiguration serverConfig) {
        info("stopWLPortal - entry");
        OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
        switch (osFamily) {
            case Windows: {
                boolean exitedOk;
                try {
                    exitedOk = stopServer(serverName);
                }
                catch (Exception e) {
                    exitedOk = false;
                }
                info("stopWLPortal - exitedOk = {0}", exitedOk);

                boolean isServerRunning;
                try {
                    isServerRunning = isServerRunning("http://localhost:" + serverConfig.httpPort, 10000);
                }
                catch (Exception e) {
                    isServerRunning = false;
                }
                info("stopWLPortal - isServerRunning = {0}", isServerRunning);

                if (/*!exitedOk ||*/ isServerRunning) {
                    info("stopWLPortal - need to kill WL Portal process");
                    ProcessExecutor procExecutor = ProcessUtils2.newProcessExecutor().command("wmic", "process", "where", "\"name like 'java%' and CommandLine like '%weblogic.Server'\"", "Call", "Terminate");
                    StartedProcess startedProcess = ProcessUtils2.startProcess(procExecutor);
                    int exitCode = ProcessUtils2.waitForProcess(startedProcess, 10, TimeUnit.MINUTES, false);
                    info("stopWLPortal - exitCode = {0}", exitCode);
                }
                info("stopWLPortal - exit");
                break;
            }
            case Linux:
            default: {
                throw new AgentInstallException("Operating system " + osFamily
                    + " not yet supported", AgentInstallException.ERR_OS_NOT_SUPPORTED);
            }
        }
    }

    /*
     * setDomainEnv.cmd.NOAGENT ==> setDomainEnv.cmd
     */
    private void createWindozeSetEnvScriptNoAgent(WlsServerConfiguration serverConfig)
        throws IOException {
        LOGGER.info("createWindozeSetEnvScriptNoAgent - entry");

        File startScript = getExistingFile(serverConfig.baseDir + PATH_SUFFIX_START_SCRIPT); // setDomainEnv.cmd

        File startScriptTemplateOriginal =
            getStartScriptTemplateOriginal(serverConfig, startScript); // if needed,
                                                                       // setDomainEnv.cmd ==>
                                                                       // setDomainEnv.cmd.ORIGINAL

        File startScriptTemplateNoAgent =
            getStartScriptTemplateNoAgent(serverConfig, startScriptTemplateOriginal); // if needed,
                                                                                      // (setDomainEnv.cmd
                                                                                      // + insert
                                                                                      // entries)
                                                                                      // ==>
                                                                                      // setDomainEnv.cmd.NOAGENT
        copyFile(startScriptTemplateNoAgent, startScript, true, false); // setDomainEnv.cmd.NOAGENT
                                                                        // ==> setDomainEnv.cmd
    }

    /*
     * setDomainEnv.cmd.AGENT ==> setDomainEnv.cmd
     */
    private String createWindozeSetEnvScriptAgent(WlsServerConfiguration serverConfig)
        throws IOException {
        LOGGER.info("createWindozeSetEnvScriptAgent - entry");

        File startScript = getExistingFile(serverConfig.baseDir + PATH_SUFFIX_START_SCRIPT); // setDomainEnv.cmd

        File startScriptTemplateOriginal =
            getStartScriptTemplateOriginal(serverConfig, startScript); // if needed,
                                                                       // setDomainEnv.cmd ==>
                                                                       // setDomainEnv.cmd.ORIGINAL

        File startScriptTemplateAgent =
            getStartScriptTemplateAgent(serverConfig, startScriptTemplateOriginal); // if needed,
                                                                                    // (setDomainEnv.cmd
                                                                                    // + insert
                                                                                    // entries) ==>
                                                                                    // setDomainEnv.cmd.AGENT
        copyFile(startScriptTemplateAgent, startScript, true, false); // setDomainEnv.cmd.AGENT ==>
                                                                      // setDomainEnv.cmd

        return startScriptTemplateOriginal.getAbsolutePath();
    }

    private File getStartScriptTemplateOriginal(WlsServerConfiguration serverConfig,
        File startScript) {
        return copyFile(startScript, serverConfig.baseDir + PATH_SUFFIX_START_SCRIPT_ORIGINAL,
            false, false);
    }

    private File getStartScriptTemplateNoAgent(WlsServerConfiguration serverConfig, File startScript)
        throws IOException {
        File startScriptTemplateNoAgent =
            new File(serverConfig.baseDir + PATH_SUFFIX_START_SCRIPT_NOAGENT);
        if (!startScriptTemplateNoAgent.exists()) {
            prepareStartScriptTemplateNoAgent(serverConfig, startScript, startScriptTemplateNoAgent);
        }
        return startScriptTemplateNoAgent;
    }

    private File getStartScriptTemplateAgent(WlsServerConfiguration serverConfig, File startScript)
        throws IOException {
        File startScriptTemplateAgent =
            new File(serverConfig.baseDir + PATH_SUFFIX_START_SCRIPT_AGENT);
        if (!startScriptTemplateAgent.exists()) {
            prepareStartScriptTemplateAgent(serverConfig, startScript, startScriptTemplateAgent);
        }
        return startScriptTemplateAgent;
    }

    private void prepareStartScriptTemplateNoAgent(WlsServerConfiguration serverConfig,
        File startScript, File startScriptTemplateAgent) throws IOException {
        insertJavaOpts(serverConfig, startScript, startScriptTemplateAgent);
        insertMemOpts(serverConfig, startScriptTemplateAgent, startScriptTemplateAgent);
        insertJmxOpts(serverConfig, startScriptTemplateAgent, startScriptTemplateAgent);
        insertGcOpts(serverConfig, startScriptTemplateAgent, startScriptTemplateAgent);
    }

    private void prepareStartScriptTemplateAgent(WlsServerConfiguration serverConfig,
        File startScript, File startScriptTemplateAgent) throws IOException {
        insertJavaOpts(serverConfig, startScript, startScriptTemplateAgent);
        insertMemOpts(serverConfig, startScriptTemplateAgent, startScriptTemplateAgent);
        insertAgentOpts(serverConfig, startScriptTemplateAgent, startScriptTemplateAgent);
        insertJmxOpts(serverConfig, startScriptTemplateAgent, startScriptTemplateAgent);
        insertGcOpts(serverConfig, startScriptTemplateAgent, startScriptTemplateAgent);
    }

    private void insertJavaOpts(WlsServerConfiguration serverConfig, File source, File target)
        throws IOException {
        // java opts
        String[] toInsert =
            new String[] {
                    "rem --------------- Begin APM Tests Configuration - Java options ------------------",
                    JAVAOPTS_PARAMETERS_1,
                    JAVAOPTS_PARAMETERS_2,
                    "rem --------------- End APM Tests Configuration - Java options ------------------",
                    ""};
        InsertPoint[] insertPoints = getInsertPoints(serverConfig, INSERTPOINTKEY_JAVAOPTS);
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(source, target, toInsert, insertPoints);
    }

    private void insertMemOpts(WlsServerConfiguration serverConfig, File source, File target)
        throws IOException {
        // mem opts
        String[] toInsert =
            new String[] {
                    "",
                    "rem --------------- Begin APM Tests Configuration - Memory options ------------------",
                    MEMOPTS_PARAMETERS_1, MEMOPTS_PARAMETERS_2,
                    "rem --------------- End APM Tests Configuration - Memory options ------------------"};
        InsertPoint[] insertPoints = getInsertPoints(serverConfig, INSERTPOINTKEY_MEMOPTS);
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(source, target, toInsert, insertPoints);
    }

    private void insertJmxOpts(WlsServerConfiguration serverConfig, File source, File target)
        throws IOException {
        // jmx opts
        String jmxOptions = MessageFormat.format(JMX_OPTS, serverConfig.getJmxPortString());
        String[] toInsert =
            new String[] {
                    "",
                    "rem --------------- Begin APM Tests Configuration - JMX options ------------------",
                    jmxOptions,
                    "rem --------------- End APM Tests Configuration - JMX options ------------------"};
        InsertPoint[] insertPoints = getInsertPoints(serverConfig, INSERTPOINTKEY_JMX);
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(source, target, toInsert, insertPoints);
    }

    private void insertGcOpts(WlsServerConfiguration serverConfig, File source, File target)
        throws IOException {
        // gc opts
        String[] toInsert =
            new String[] {
                    "",
                    "rem --------------- Begin APM Tests Configuration - GC options ------------------",
                    GC_OPTS,
                    "rem --------------- End APM Tests Configuration - GC options ------------------"};
        InsertPoint[] insertPoints = getInsertPoints(serverConfig, INSERTPOINTKEY_GC);
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(source, target, toInsert, insertPoints);
    }

    private void insertAgentOpts(WlsServerConfiguration serverConfig, File source, File target)
        throws IOException {
        // agent opts
        String agentOptions =
            MessageFormat.format(AGENT_OPTS, serverConfig.baseDir,
                serverConfig.defaultAgentInstallDir);
        String[] toInsert =
            new String[] {
                    "",
                    "rem --------------- Begin APM Tests Configuration - Agent options ------------------",
                    agentOptions,
                    "rem --------------- End APM Tests Configuration - Agent options ------------------"};
        InsertPoint[] insertPoints = getInsertPoints(serverConfig, INSERTPOINTKEY_AGENT);
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(source, target, toInsert, insertPoints);
    }

    private File copyFile(File file, String targetFileName, boolean force, boolean warningIfExists) {
        return copyFile(file, new File(targetFileName), force, warningIfExists);
    }

    private File copyFile(File file, File targetFile, boolean force, boolean warningIfExists) {
        if (targetFile.exists() && !force) {
            if (warningIfExists) {
                LOGGER.warn("File {} already exists, skipping to copy {}!", targetFile, file);
            }
        } else {
            try {
                FileCopyUtils.copy(file, targetFile);
                LOGGER.info("File {} copied as {}", file, targetFile);
            } catch (IOException e) {
                throw new AgentInstallException("Unable to copy file " + file + " as " + targetFile
                    + ": " + e, e);
            }
        }
        return targetFile;
    }

    private File getExistingFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new AgentInstallException("Cannot continue, file does not exist: " + fileName);
        }
        return file;
    }

    private InsertPoint[] getInsertPoints(WlsServerConfiguration serverConfig, String insertPointKey) {
        return scriptMap.get(serverConfig.getVersion() + ":" + OperatingSystemFamily.Windows + ":"
            + insertPointKey);
    }

}
