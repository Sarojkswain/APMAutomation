/**
 *
 */
package com.ca.apm.systemtest.fld.plugin.tomcat;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.zeroturnaround.exec.ProcessExecutor;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.common.files.InsertPoint;
import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl;
import com.ca.apm.systemtest.fld.plugin.AgentInstallException;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.TrussDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.tomcat.TomcatPluginConfiguration.TomcatServerConfig;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;

/**
 * @author filja01
 */
public class TomcatPluginImpl extends AbstractAppServerPluginImpl<TomcatPluginConfiguration> implements TomcatPlugin {
    private static final String FMT_AGENT_WIN = "set CATALINA_OPTS=%CATALINA_OPTS%  "
        + "-javaagent:{0}\\{1}\\Agent.jar "
        + "-Dcom.wily.introscope.agentProfile={0}\\{1}\\core\\config\\IntroscopeAgent.profile";
    private static final String FMT_AGENT_LINUX = "  CATALINA_OPTS=\"$CATALINA_OPTS  "
        + "-javaagent:{0}/{1}/Agent.jar "
        + "-Dcom.wily.introscope.agentProfile={0}/{1}/core/config/IntroscopeAgent.profile\"";

    private static final Logger log = LoggerFactory.getLogger(TomcatPluginImpl.class);

    private boolean isAgentSet = false;

    private final Map<String, InsertPoint[]> scriptMap = new HashMap<>();

    @Autowired
    TrussDownloadMethod trussDm;
    
    @Autowired
    ArtifactoryDownloadMethod artifactoryDm;

    public TomcatPluginImpl() {
        super(PLUGIN, TomcatPluginConfiguration.class);
        InsertPoint[] ip = new InsertPoint[] {
            InsertPoint.after("^:doStart.*"), InsertPoint.after("^:doRun.*")
        };
        scriptMap.put(TomcatPluginConfiguration.TomcatVersion.Tomcat6 + ":" + SystemUtil.OperatingSystemFamily.Windows, ip);
        scriptMap.put(TomcatPluginConfiguration.TomcatVersion.Tomcat7 + ":" + SystemUtil.OperatingSystemFamily.Windows, ip);
        scriptMap.put(TomcatPluginConfiguration.TomcatVersion.Tomcat8 + ":" + SystemUtil.OperatingSystemFamily.Windows, ip);
        ip = new InsertPoint[] {
            InsertPoint.after(".*\"\\$1\" = \"run\".*"), InsertPoint.after(".*\"\\$1\" = \"start\".*")
        };
        scriptMap.put(TomcatPluginConfiguration.TomcatVersion.Tomcat6 + ":" + SystemUtil.OperatingSystemFamily.Linux, ip);
        scriptMap.put(TomcatPluginConfiguration.TomcatVersion.Tomcat7 + ":" + SystemUtil.OperatingSystemFamily.Linux, ip);
        scriptMap.put(TomcatPluginConfiguration.TomcatVersion.Tomcat8 + ":" + SystemUtil.OperatingSystemFamily.Linux, ip);
    }

    @Override
    @ExposeMethod(description = "Download and setup Tomcat server based on configuration parameter.")
    public void installTomcat(Configuration config) {
        if (StringUtils.isBlank(config.tomcatArtifactGroupID)) {
            config.tomcatArtifactGroupID = "com.ca.apm.binaries";
        }
        if (StringUtils.isBlank(config.tomcatArtifactArtifactID)) {
            config.tomcatArtifactArtifactID = "tomcat";
        }
        if (StringUtils.isBlank(config.tomcatArtifactoryURL)) {
            config.tomcatArtifactoryURL = "http://oerth-scx.ca.com:8081/artifactory/apm-third-party-isl";
        }
        if (StringUtils.isBlank(config.tomcatArtifactVersion)) {
            config.tomcatArtifactVersion = "6.0.36";
        }
        if (StringUtils.isBlank(config.tomcatInstallDir)) {
            config.tomcatInstallDir = "c:/sw/tomcat";
        }
        File tomcatInstallDir = new File(config.tomcatInstallDir);

        try {
            clearTargetInstallationFolder(tomcatInstallDir);

            File tempDir = java.nio.file.Files.createTempDirectory("tomcat-plugin").toFile();

            File binaryTomcat = downloadTomcatBinary(config, tempDir);

            tomcatInstallDir = unpackBinaryIntoTargetInstallationDir(binaryTomcat, tomcatInstallDir);
            config.tomcatInstallDir = tomcatInstallDir.getAbsolutePath();

            FileUtils.deleteDirectory(tempDir);

            configureUnpackedBinary(tomcatInstallDir, config);
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Unable to deploy Apache Tomcat into {1}. Exception: {0}", config.tomcatInstallDir);
        }
        log.info("Apache Tomcat has been deployed into " + config.tomcatInstallDir);
    }

    private void configureUnpackedBinary(File tomcatInstallDir, Configuration config) throws IOException {
        // configure server.xml
        //File serverConfigFile = new File(tomcatInstallDir, "conf/server.xml");
        // configure Connector port?

        // create setenv.bat or setenv.sh
        if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_UNIX) {
            throw ErrorUtils.logErrorAndThrowException(log, "Unable to establish script to set environment variables: OS not supported!");
        }

        File setEnvScriptFile = new File(tomcatInstallDir, "bin/setenv.bat");
        if (SystemUtils.IS_OS_UNIX) {
            // Change permissions to enable Tomcat to run on UNIX
            // This is required as Tomcat is installed from a zip so permissions are not maintained
            setEnvScriptFile = new File(tomcatInstallDir, "bin/setenv.sh");
            File startUpScript = new File(tomcatInstallDir, "bin/startup.sh");
            startUpScript.setExecutable(true);
            File catalina = new File(tomcatInstallDir, "bin/catalina.sh");
            catalina.setExecutable(true);
            File setclasspath = new File(tomcatInstallDir, "bin/setclasspath.sh");
            setclasspath.setExecutable(true);
            if (StringUtils.isBlank(config.jdkHomeDir))
                    config.jdkHomeDir = "/opt/jdk1.8.0_25/";
        }

        if (!StringUtils.isBlank(config.jdkHomeDir)) {
            Map<String, String> envVariables = new HashMap<>(1);
            envVariables.put("JAVA_HOME", FilenameUtils.separatorsToSystem(config.jdkHomeDir));

            writeEnvironmentVariablesAsScript(setEnvScriptFile, envVariables);
            log.info("Script with environment variables established at {}",
                        setEnvScriptFile.getAbsolutePath());
        }
    }

    private void writeEnvironmentVariablesAsScript(File scriptFile, Map<String, String> envVariables) throws IOException {
        List<String> fileLines = new ArrayList<>(envVariables.size());
        String scriptCommand = "set";
        String scriptEntryTemplate = "%s %s=%s";
        if (SystemUtils.IS_OS_UNIX) {
            scriptCommand = "export";
            scriptEntryTemplate = "%s %s=\"%s\"";
        }

        for (Map.Entry<String, String> envVariable : envVariables.entrySet()) {
            fileLines.add(format(scriptEntryTemplate, scriptCommand, envVariable.getKey(), envVariable.getValue()));
            log.debug("Setting {} to {} variable within script file {}", envVariable.getValue(), envVariable.getKey(),
                         scriptFile.getAbsolutePath());
        }

        FileUtils.writeLines(scriptFile, fileLines);
    }

    private void clearTargetInstallationFolder(File tomcatInstallDir) throws IOException {
        if (tomcatInstallDir.exists()) {
            log.info("Deleting folder {}", tomcatInstallDir.getAbsolutePath());
            FileUtils.deleteDirectory(tomcatInstallDir);
        }
    }

    private File downloadTomcatBinary(Configuration config, File tempDir) {


        ArtifactFetchResult fetchResult;

            fetchResult = artifactoryDm.fetchTempArtifact(config.tomcatArtifactoryURL, //TODO - DM - why everyone is using DEFAULT_ARTIFACTORY_URL, but this class - uses custom ?
                    config.tomcatArtifactGroupID,
                    config.tomcatArtifactArtifactID,
                    config.tomcatArtifactVersion,
                    null,
                    "zip",
                    tempDir.getAbsolutePath());


        return fetchResult.getFile();
    }

    private File unpackBinaryIntoTargetInstallationDir(File binaryTomcat, File tomcatInstallDir) {
        try {
            ZipFile artifactZip = new ZipFile(binaryTomcat);
            artifactZip.extractAll(tomcatInstallDir.getAbsolutePath());
            log.info("Extracted ZIP file {} into {}.", binaryTomcat.getAbsolutePath(),
                 tomcatInstallDir.getAbsolutePath());
        }
        catch (ZipException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create extract artifact {1}. Exception: {0}",
                binaryTomcat);
        }

        File[] unpackedArtifactFiles = tomcatInstallDir.listFiles();
        if (unpackedArtifactFiles == null) {
            throw ErrorUtils.logErrorAndThrowException(log,
                "Unpacked directory contains no files ({1}). Exception: {0}",
                tomcatInstallDir);
        }
        if ((unpackedArtifactFiles.length == 1) && unpackedArtifactFiles[0].isDirectory()) {
            tomcatInstallDir = unpackedArtifactFiles[0];
        }

        return tomcatInstallDir;
    }

    @Override
    @ExposeMethod(description = "Configures tomcat not to use any agent")
    public void unsetAgent(Configuration config) {
        setupAgent(config, true);
    }

    @Override
    @ExposeMethod(description = "Configures tomcat to use a specific agent")
    public void setAgent(Configuration config) {
        setupAgent(config, false);
    }

    @Override
    @ExposeMethod(description = "Starts the tomcat server. Returns true if server was started with return code 0")
    public boolean startServer(Configuration config) {
        log.info("Starting server {}", config.tomcatServerName);

        Path agentJar = Paths.get(config.agentInstallDir + AGENT_JAR_PATH_REL);
        Path agentProfile = Paths.get(config.agentInstallDir + AGENT_PROFILE_PATH_REL);

        ProcessBuilder ps =
                ProcessUtils.newProcessBuilder().command("cmd", "/c", config.tomcatInstallDir + "/bin/startup.bat")
                        .directory(new File(config.tomcatInstallDir + "/bin")).redirectErrorStream(true)
                        .redirectOutput(Redirect.appendTo(new File(config.logs, "start.txt")));
        if (isAgentSet) {
            ps.environment()
                    .put("JAVA_OPTS", "-javaagent:" + agentJar + " -Dcom.wily.introscope.agentProfile=" + agentProfile);
        }
        ProcessUtils.startProcess(ps);
        return true;
    }

    @Override
    @ExposeMethod(description = "Stops the tomcat server. Returns true if server was stopped with return code 0")
    public boolean stopServer(Configuration config) {
        log.info("Stopping server {}", config.tomcatServerName);
        ProcessBuilder ps =
                ProcessUtils.newProcessBuilder().command("cmd", "/c", config.tomcatInstallDir + "/bin/shutdown.bat")
                        .directory(new File(config.tomcatInstallDir + "/bin")).redirectErrorStream(true)
                        .redirectOutput(Redirect.appendTo(new File(config.logs, "stop.txt")));

        ProcessUtils.startProcess(ps);
        return true;
    }

    @Override
    public void installAgent(Configuration config) {
        String agentName = "Test Tomcat Agent";
        log.debug("Installing tomcat Agent");

        try {
            clearTargetInstallationFolder(new File(config.agentInstallDir));
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e, "Cannot clear installation directory {1}.", config.agentInstallDir);
        }

        // Download agent
        Path installerPath = trussDm.downloadAgent(config.trussServer, config.codeName, config.buildId, config.buildNumber, config.platform == null ? SystemUtil.OperatingSystemFamily.Windows : config.platform);
        System.out.println(installerPath);
        // Response file
        Map<String, String> res = new HashMap<>();
        res.put("USER_INSTALL_DIR", config.agentInstallDir);
        res.put("emHost", config.emHost);
        res.put("agentName", agentName);
        res.put("appServer", SERVER_TOMCAT);
        res.put("shouldEnableSPM", "true");

        // Install agent
        installAgent(installerPath.toString() + config.agentExecute, installerPath.toString()
                + "/SampleResponseFile.Agent.txt", res);
        try {
            FileUtils.deleteDirectory(installerPath.toFile());
        } catch (IOException e) {
            ErrorUtils.logExceptionAndWrapFmt(log, e, "Cannot delete temporary directory {}.", installerPath.toString());
        }
        // Configure agent (turn SI off)
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("introscope.agent.deep.instrumentation.enabled", "false");
        configMap.put("introscope.autoprobe.dynamicinstrument.enabled", "true");
        configMap.put("log4j.appender.logfile.File", Paths.get(config.logs, agentName + ".IntroscopeAgent.log").toString());
        configMap.put("introscope.autoprobe.logfile", Paths.get(config.logs, agentName + ".AutoProbe.log").toString());
        configMap.put("introscope.agent.agentAutoNamingMaximumConnectionDelayInSeconds", "0");
        configMap.put("introscope.agent.decorator.enabled", "true");
        configMap.put("log4j.appender.logfile.MaxFileSize", "50MB");
        configMap.put("introscope.agent.agentAutoNamingEnabled", "false");

        configureAgent(Paths.get(config.agentInstallDir), configMap);

    }

    /**
     * Sets / unsets an agent into Tomcat
     *
     * @param config configuration
     * @param unset TRUE if we want to unset the agent (run tomcat without an Agent)
     */
    protected void setupAgent(Configuration config, boolean unset) {

        Path srcFolder = Paths.get(config.agentInstallDir, "wily\\examples\\SOAPerformanceManagement\\ext");
        Path destFolder = Paths.get(config.agentInstallDir, "wily\\core\\ext");

        if (Files.exists(srcFolder) && Files.exists(destFolder)) {
            try {
                if (unset) {
                    for (File file : FileUtils.listFiles(srcFolder.toFile(), FileFilterUtils.trueFileFilter(), null)) {
                        FileUtils.deleteQuietly(new File(destFolder.getParent().toFile(), file.getName()));
                    }
                } else {
                    FileUtils.copyDirectoryToDirectory(srcFolder.toFile(), destFolder.toFile());
                }
            } catch (IOException e) {
                throw ErrorUtils.logExceptionAndWrap(log, e, "Cannot copy SOAPerformanceManagement extensions.");
            }
        }
        isAgentSet = !unset;
    }

    @Override
    protected String getAppServerName() {
        return "tomcat";
    }

    @Override
    protected String createStartScript(AppServerConfiguration serverConfig) {
        try {
            OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
            switch (osFamily) {
                case Linux:
                    return createLinuxStartScript(serverConfig);
                case Windows:
                    return createWindozeStartScript(serverConfig);
                default:
                    throw new AgentInstallException("Operating system " + osFamily + " not yet supported", AgentInstallException.ERR_OS_NOT_SUPPORTED);
            }
        } catch (Exception e) {
            if (e instanceof AgentInstallException) {
                throw (AgentInstallException) e;
            }
            throw new AgentInstallException("Unable to configure startup script", e);
        }
    }


    private String createWindozeStartScript(AppServerConfiguration serverConfig) throws IOException {
        log.debug("Adding startup lines to executionscript");
        TomcatServerConfig tomcat = (TomcatServerConfig) serverConfig;
        File startScript = new File(serverConfig.baseDir + "\\bin\\catalina.bat");
        File backupStartScript = new File(serverConfig.baseDir + "\\bin\\catalina.bat.bak");

        try {
            if (!backupStartScript.exists()) {
                FileCopyUtils.copy(startScript, backupStartScript);
            }
        } catch (IOException ex) {
            log.error("Cannot backup script file", ex);
        }

        String setAgent = MessageFormat.format(FMT_AGENT_WIN, serverConfig.baseDir, serverConfig.defaultAgentInstallDir);
        String[] toInsert = {
            "rem --------------- Begin APM Agent Configuration ------------------",
            setAgent,
            "rem --------------- End APM Agent Configuration ------------------"
        };
        InsertPoint[] ips = scriptMap.get(tomcat.version + ":" + OperatingSystemFamily.Windows);
        com.ca.apm.systemtest.fld.common.files.FileUtils.insertIntoFile(backupStartScript, startScript, toInsert, ips);

        return backupStartScript.getAbsolutePath();
    }

    private String createLinuxStartScript(AppServerConfiguration serverConfig) throws IOException {
        TomcatServerConfig tomcat = (TomcatServerConfig) serverConfig;
        File startScript = new File(serverConfig.baseDir + "/bin/catalina.sh");
        File backupStartScript = new File(serverConfig.baseDir + "/bin/catalina.sh.bak");
        if (!backupStartScript.exists()) {
            FileCopyUtils.copy(startScript, backupStartScript);
        }

        String setAgent = MessageFormat.format(FMT_AGENT_LINUX, serverConfig.baseDir, serverConfig.defaultAgentInstallDir);
        String[] toInsert = {
            "  # --------------- Begin APM Agent Configuration ------------------",
            setAgent,
            "  # --------------- End APM Agent Configuration --------------------"
        };
        InsertPoint[] ips = scriptMap.get(tomcat.version + ":" + OperatingSystemFamily.Linux);
        com.ca.apm.systemtest.fld.common.files.FileUtils.insertIntoFile(backupStartScript, startScript, toInsert, ips);

        return backupStartScript.getAbsolutePath();
    }
    
    
    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl#startAppServer(java.lang.String)
     */
    @Override
    public void startAppServer(String serverId) {
        TomcatPluginConfiguration pluginConfig = readConfiguration();
        TomcatServerConfig serverConfig = pluginConfig.getServerConfig(serverId);
        
        if (serverConfig == null) {
            log.warn("Server configuration not found for serverId=={}", serverId);
            return;
        }
        
        String[] command = new String[0];
        OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
        switch (osFamily) {
            case Linux:
                command = new String[] {"./startup.sh"};
                break;
            case Windows:
                command = new String[] {"cmd.exe", "/c", "startup.bat"};
                break;
            default:
                log.warn("Unsupported operation system {} exiting", osFamily);
                return;
        }
        File tomcatBase = new File(serverConfig.baseDir);
        File tomcatBin = new File(tomcatBase, "bin");
        Map<String, String> map = new HashMap<>();
        ProcessExecutor procExecutor = ProcessUtils2.newProcessExecutor()
            .command(command)
            .directory(tomcatBin)
            .environment(map);
        int result = ProcessUtils2.waitForProcess(ProcessUtils2.startProcess(procExecutor), 5, TimeUnit.MINUTES, true);
        if (result != 0) {
            ErrorUtils.throwRuntimeException("Tomcat did not start successfully: result code == " + result);
        }

    }
    


    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl#createServerConfiguration(java.lang.String)
     */
    @Override
    protected AppServerConfiguration createServerConfiguration(String serverId) {
        TomcatPluginConfiguration cfg = readConfiguration();
        TomcatServerConfig sc = new TomcatServerConfig();
        sc = new TomcatServerConfig();
        sc.id = serverId;
        cfg.addServerConfig(serverId, sc);
        saveConfiguration(cfg);
        return sc;
    }
}
