package com.ca.apm.systemtest.fld.plugin.jmeter;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.common.files.FileUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeAttribute;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.HttpDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.ca.apm.systemtest.fld.common.ACFileUtils.generateTemporaryFile;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.FileUtils.openOutputStream;

/**
 * Plugin to handle jMeter load generator.
 *
 * @author haiva01
 */
public class JMeterPluginImpl extends AbstractPluginImpl implements JMeterPlugin {
    public static final String TEMP_DIR_PREFIX = "jmeter-plugin";
    static final String JMETER_ARTIFACTORY_URL = "http://oerth-scx.ca.com:8081/artifactory/repo";
    private static final Logger log = LoggerFactory.getLogger(JMeterPluginImpl.class);
    private static final String JMETER_ARTIFACT_TYPE = "zip";
    private static final String COMSPEC = System.getenv("ComSpec");
    private static final Pattern LAST_RESULT_PARSER_PATTERN =
        Pattern.compile("^.+\\p{Upper}+ \\d{2}:\\d{2}:\\d{2} \\d{2}/\\d{2}/\\d{4}$");
    private static String jmeterStoppingPort;
    final SimpleDateFormat dFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");

    @ExposeAttribute(description = "jMeter version")
    String jMeterVersion;

    @Autowired
    ArtifactoryDownloadMethod dm;

    @Autowired
    HttpDownloadMethod httpDownloadMethod;

    @ExposeAttribute(description = "jMeter distribution ZIP file")
    private File jMeterZip;
    @ExposeAttribute(
        description = "Directory of jMeter distribution extracted from its distribution ZIP file")
    private File jMeterDir;
    @ExposeAttribute(description = "Working directory")
    private File tempDir;
    @ExposeAttribute(description = "Currently set scenario type")
    private ScenarioType scenarioType;
    @ExposeAttribute(description = "Currently set scenario")
    private String scenario;

    public JMeterPluginImpl() {

    }

    private File getTempDir(boolean create) {
        if (tempDir == null && create) {
            createTempDir();
        }

        return tempDir;
    }

    private void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    public String getJmeterStoppingPort() {
        return jmeterStoppingPort;
    }

    @ExposeMethod(description = "Creates temporary working directory.")
    @Override
    public String createTempDir() {
        return createTempDir(null, null);
    }

    @ExposeMethod(description = "Creates temporary working directory with the provided prefix in the specified parent directory.")
    @Override
    public String createTempDir(String parentDirPath, String prefix) {
        File tempDir = null;
        prefix = prefix == null ? TEMP_DIR_PREFIX : prefix;
        try {
            if (parentDirPath == null) {
                tempDir = Files.createTempDirectory(prefix).toFile();
            } else {
                tempDir = Files.createTempDirectory(Paths.get(parentDirPath), prefix).toFile();
            }
            setTempDir(tempDir);
            return tempDir != null ? tempDir.getAbsolutePath() : null;
        } catch (IOException e) {
            String errMsg = "Failed to create temporary directory for Jmeter!";
            error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }

    @ExposeMethod(description = "Delete temporary working directory.")
    @Override
    public void deleteTempDir() {
        File dir = getTempDir(false);
        if (dir != null) {
            try {
                deleteDirectory(dir);
            } catch (IOException e) {
                String errMsg = MessageFormat.format("Failed to delete Jmeter temporary directory at ''{0}''!", dir);
                error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
            setTempDir(null);
        } else {
            info("Jmeter temporary directory is null. Nothing to delete.");
        }
    }

    @ExposeMethod(description = "Download jMeter ZIP file.")
    @Override
    public String downloadJMeter(String version) {
        jMeterVersion = version;
        if (StringUtils.isBlank(jMeterVersion)) {
            jMeterVersion = JMETER_ARTIFACT_DEFAULT_VERSION;
        }

        if (jMeterZip != null) {
            // Avoid "already exists" exceptions, if possible.
            if (!deleteQuietly(jMeterZip)) {
                warn("Failed to delete ''{0}''", jMeterZip.getAbsolutePath());
            }
        }

        ArtifactFetchResult fetchResult;
        fetchResult = dm.fetchTempArtifact(JMETER_ARTIFACTORY_URL,
            JMETER_ARTIFACT_GROUP_ID, JMETER_ARTIFACT_ARTIFACT_ID,
            jMeterVersion, null, JMETER_ARTIFACT_TYPE);

        jMeterZip = fetchResult.getFile();
        return jMeterZip.getAbsolutePath();
    }

    @ExposeMethod(description = "Download jMeter ZIP file from URL.")
    @Override
    public String downloadJmeterByUrl(String url) {
        try {
            ArtifactFetchResult fetchResult = httpDownloadMethod.fetch(url, getTempDir(true), true);
            jMeterZip = fetchResult.getFile();
            return jMeterZip.getAbsolutePath();
        } catch (ArtifactManagerException e) {
            String errMsg = MessageFormat.format("Failed to download Jmeter from {0}", url);
            error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }

    @ExposeMethod(description = "Extracts installer artifact from given zip file.")
    @Override
    public String unzipJMeterZip() {
        // jMeterDir = new File(tempDir, "jMeter");
        jMeterDir = getTempDir(true);
        try {
            forceMkdir(jMeterDir);
        } catch (IOException e) {
            String errMsg = MessageFormat.format("Failed to create Jmeter directory {0}", jMeterDir);
            error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        try {
            ZipFile artifactZip = new ZipFile(jMeterZip);
            artifactZip.extractAll(jMeterDir.getAbsolutePath());
            info("Extracted Jmeter ZIP file ''{0}'' into ''{1}''.",
                jMeterZip.getAbsolutePath(),
                jMeterDir.getAbsolutePath());
        } catch (ZipException e) {
            String errMsg = MessageFormat.format("Failed to extract Jmeter from {0}", jMeterZip);
            error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        return jMeterDir.getAbsolutePath();
    }

    /**
     * Download jMeter scenario file from given URL.
     *
     * @param scenarioUrl jMeter scenario file URL
     * @return file where given jMeter scenario has been downloaded to
     */
    private File downloadScenarioFile(String scenarioUrl) {
        info("Downloading Jmeter scenario from {0}.", scenarioUrl);
        return FileUtils.downloadResource(scenarioUrl, "scenario", ".jmx", getTempDir(true));
    }

    @Override
    public ScenarioType getScenarioType() {
        return scenarioType;
    }

    @Override
    public String getScenario() {
        return scenario;
    }

    @Override
    public void setScenarioUrl(String url) {
        scenario = url;
        scenarioType = ScenarioType.URL;
    }

    @Override
    public void setBuiltinScenario(String sc) {
        scenario = sc;
        scenarioType = ScenarioType.BUILT_IN;
    }

    private int getRandomPortForStoppingJmeter() {
        final int min = 4445;
        final int max = 5000;
        Random rand = new Random();

        return rand.nextInt((max - min) + 1) + min;
    }

    /**
     * This function creates a unique file in working directory and transfers data from
     * ReadableByteChannel into it.
     *
     * @param rbc    data source
     * @param prefix file name prefix
     * @param ext    file name extension
     * @return file containing data from given ReadableByteChannel
     */
    private File transferChannelToFile(ReadableByteChannel rbc, String prefix, String ext) {
        File file;
        try {
            file = File.createTempFile(prefix, ext, getTempDir(true));
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create temporary file for transfer. Exception: {0}");
        }

        long downloadedBytes;
        try (FileOutputStream fos = new FileOutputStream(file)) {
            try {
                downloadedBytes = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Error during file transfer. Exception: {0}");
            }

            log.debug("Transferred {} bytes.", downloadedBytes);
        } catch (FileNotFoundException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to open file {1} for transfer. Exception: {0}", file);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Error closing file {1}. Exception: {0}", file);
        }

        log.info("Transferred {} bytes into {}.", downloadedBytes, file);
        return file;
    }

    private File resourceToFile(String resource) {
        log.info("about to retrieve resource {}", resource);
        InputStream resourceStream = JMeterPlugin.class.getResourceAsStream(resource);
        if (resourceStream == null) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "resource {0} could not be opened as stream", resource);
        }

        ReadableByteChannel rbc =
            Channels.newChannel(JMeterPlugin.class.getResourceAsStream(resource));
        return transferChannelToFile(rbc, "scenario", ".jmx");
    }

    public boolean checkIfJmeterIsInstalled() {
        File jMeterBasePath = Paths
            .get(getTempDir(true).getAbsolutePath(), "apache-jmeter-" + jMeterVersion)
            .toAbsolutePath().toFile();

        return jMeterBasePath.exists();
    }

    private File getJMeterBasePath() {
        File jMeterBasePath = Paths
            .get(getTempDir(true).getAbsolutePath(), "apache-jmeter-" + jMeterVersion)
            .toAbsolutePath().toFile();
        assert jMeterBasePath.exists();
        return jMeterBasePath;
    }

    private File getJMeterBatPath() {
        File jMeterBat = Paths.get(getJMeterBasePath().getAbsolutePath(), "bin", "jmeter.bat")
            .toAbsolutePath().toFile();
        assert jMeterBat.exists();
        return jMeterBat;
    }

    private StringBuilder appendPath(StringBuilder buffer, String... strings) {
        for (String str : strings) {
            if (str != null && !str.isEmpty()) {
                buffer.append(str);
            } else {
                break;
            }
        }
        return buffer;
    }

    @Override
    public String execute(Map<String, String> scenarioProperties) {
        // First prepare batch file that will be run by scheduled task.

        File scenarioFile = retrieveScenarioFile();
        File jmeterBat = getJMeterBatPath();
        log.debug("jMeter batch file: {}", jmeterBat.getAbsolutePath());

        // Set up logging directory for jMeter's View Results Tree logging.
        String logDir = "";
        if (!scenarioProperties.containsKey("logDir")) {
            logDir = getTempDir(true).getAbsolutePath();
            debug("setting logDir to {}", logDir);
            scenarioProperties.put("logDir", logDir);
        } else {
            logDir = scenarioProperties.get("logDir");
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            String id = null;
            try {
                String sc = scenarioFile.getName();
                id = tempDir.getName() + "/" + sc.subSequence(0, sc.lastIndexOf('.'));

                List<String> command = new ArrayList<>(20);
                command.add(COMSPEC != null ? COMSPEC : "cmd.exe");
                command.add("/C");
                command.add(jmeterBat.toString());

                String noExtensionScenarioFile = FilenameUtils.getBaseName(scenarioFile.toString());

                command.add("-n"); //No-console
                command.add("-t" + noExtensionScenarioFile + ".jmx"); //JMX-file

                command.add("-j" + noExtensionScenarioFile + ".log"); //Jmeter run log file
                command.add("-l" + noExtensionScenarioFile + ".jtl"); //Sample results file
                final File batchLog = new File(
                    getJmeterPath() + "/" + noExtensionScenarioFile + "_batch.log");

                log.info("Base folder is: {}", getJmeterPath());

                if (scenarioProperties.containsKey(JMETER_STOPPING_PORT_KEY)) {
                    jmeterStoppingPort = scenarioProperties.get(JMETER_STOPPING_PORT_KEY);
                } else {
                    jmeterStoppingPort = String.valueOf(getRandomPortForStoppingJmeter());
                    scenarioProperties
                        .put(JMETER_STOPPING_PORT_KEY, String.valueOf(jmeterStoppingPort));
                }

                Iterable<String> sortedKeys = new TreeSet<>(scenarioProperties.keySet());
                for (String key : sortedKeys) {
                    StringBuilder sb = new StringBuilder(128);
                    sb.append("-J").append(key).append("=").append(scenarioProperties.get(key));
                    command.add(sb.toString());
                }

                StringBuilder path = new StringBuilder(260);
                appendPath(path, System.getProperty("java.home", System.getenv("JAVA_HOME")),
                    "\\bin;");
                appendPath(path, System.getenv("windir"), "\\System32;");
                appendPath(path, System.getenv("PATH"));

                ProcessBuilder builder = ProcessUtils.newProcessBuilder(false);
                info("Jmeter command: {0}", command);
                builder.command(command).directory(getTempDir(true)).redirectErrorStream(true)
                    .redirectOutput(batchLog);

                // builder.environment().put("jMeterLogFile", tempDir.toString() + "\\" + runid +
                // ".log");
                builder.environment().put("PATH", path.toString());
                builder.environment().put("JVM_ARGS", "-Did=" + id);

                builder.start();
            } catch (IOException e) {
                ErrorUtils.logExceptionFmt(log, e, "Cannot start jMeter process. Exception: {0}");
                return null;
            }
            return id;
        } else {
            // TODO: Linux implementation.
            throw new NotImplementedException(
                "Executing jMeter on non-Windows system is not implemented, yet.");
        }

    }

    @ExposeMethod(description = "Query if the task is still running.")
    @Override
    public boolean isRunning(String jmeterHandle) {
        Long pid = ProcessUtils.findJavaProcessPid(jmeterHandle);
        log.debug("state of task \"{}\" is \"{}\"", jmeterHandle, pid == null
            ? "Not Running"
            : "Running");
        return pid != null;
    }

    private static String getJavaExePath() {
        return Paths.get(System.getProperty("java.home", System.getenv("JAVA_HOME")), "bin",
            "java" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "")).toAbsolutePath().toString();
    }

    private boolean runJmeterShutdown(int terminationPort) {
        try {
            // java -cp %~dp0ApacheJMeter.jar org.apache.jmeter.util.ShutdownClient Shutdown %*
            List<String> command = new ArrayList<>(5);
            command.add(getJavaExePath());

            command.add("-cp");
            command.add(Paths.get(getJMeterBasePath().getAbsolutePath(), "bin", "ApacheJMeter.jar")
                .toAbsolutePath().toString());

            command.add("org.apache.jmeter.util.ShutdownClient");
            //command.add("Shutdown");
            command.add("StopTestNow");
            command.add(Integer.toString(terminationPort));


            final File shutdownLogFile = generateTemporaryFile("jmeter-", "-shutdown.log",
                getTempDir(true));
            try (OutputStream shutdownLogStream = openOutputStream(shutdownLogFile)) {
                ProcessExecutor builder = ProcessUtils2.newProcessExecutor()
                    .command(command)
                    .directory(getTempDir(true).getAbsoluteFile())
                    .redirectErrorStream(true)
                    .redirectErrorAlsoTo(shutdownLogStream);

                Map<String, String> env = new TreeMap<>(System.getenv());
                env.put("CLASSPATH", null);
                builder.environment(env);
                StartedProcess sp = ProcessUtils2.startProcess(builder);
                final int exitCode = ProcessUtils2.waitForProcess(sp, 1, TimeUnit.MINUTES, true);

                if (exitCode != 0) {
                    log.error("Exit code of Jmeter shutdown process: {}", exitCode);
                    return false;
                }
            }
        } catch (IOException e) {
            ErrorUtils.logExceptionFmt(log, e, "Failed to start Jmeter shutdown command. Exception: {0}");
            return false;
        }

        return true;
    }

    private boolean runWmicJmeterKill(int terminationPort) {
        // wmic Path win32_process WHERE "CommandLine LIKE '%java%jmeterStoppingPort=4935%' AND
        // NOT(CommandLine LIKE '%WMIC%')"
        try {
            List<String> command = new ArrayList<>(5);
            command.add("WMIC.exe");
            command.add("Path");
            command.add("win32_process");
            command.add("WHERE");
            command.add(
                "CommandLine LIKE '%java%" + JMETER_STOPPING_PORT_KEY + "=" + terminationPort
                    + "%' AND NOT(CommandLine LIKE '%WMIC%')");
            command.add("Call");
            command.add("Terminate");

            StringBuilder path = new StringBuilder(260);
            //appendPath(path, System.getProperty("java.home", System.getenv("JAVA_HOME")),
            // "\\bin;");
            appendPath(path,
                Paths.get(System.getenv("windir"), "System32").toAbsolutePath().toString() + ";");
            appendPath(path,
                Paths.get(System.getenv("windir"), "System32", "wbem").toAbsolutePath().toString()
                    + ";");
            appendPath(path, System.getenv("PATH"));
            final File killLogFile = generateTemporaryFile("jmeter-", "-kill.log", getTempDir(true));
            try (OutputStream killLogStream = openOutputStream(killLogFile)) {
                ProcessExecutor builder = ProcessUtils2.newProcessExecutor()
                    .command(command)
                    .directory(getTempDir(true))
                    .redirectErrorStream(true)
                    .redirectErrorAlsoTo(killLogStream);

                Map<String, String> env = new TreeMap<>(System.getenv());
                env.put("PATH", path.toString());
                env.put("CLASSPATH", null);
                builder.environment(env);
                StartedProcess sp = ProcessUtils2.startProcess(builder);
                final int exitCode = ProcessUtils2.waitForProcess(sp, 1, TimeUnit.MINUTES, true);
                if (exitCode != 0) {
                    log.error("Exit code of WMIC process: {}", exitCode);
                    return false;
                }
            }
        } catch (IOException e) {
            ErrorUtils.logExceptionFmt(log, e, "Cannot start jMeter kill process. Exception: {0}");
            return false;
        }
        log.info("jMeter shutdown complete");
        return true;
    }

    public boolean shutDown(int terminationPort) {
        boolean returnValue = runJmeterShutdown(terminationPort);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e, "Sleep interrupted.");
        }
        returnValue = runWmicJmeterKill(terminationPort) && returnValue;
        return returnValue;
    }

    @ExposeMethod(description = "Ask for result.")
    @Override
    public String getLastResult(String jmeterHandle) {
        String logTail = null;

        String logName = jmeterHandle + ".log";
        File file = new File(System.getProperty("java.io.tmpdir"), logName);

        try (RandomAccessFile fileHandler = new RandomAccessFile(file, "r")) {
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            StringBuilder ll = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer < fileLength) {
                        String trim = ll.toString().trim();
                        if (LAST_RESULT_PARSER_PATTERN.matcher(trim).matches()) {
                            break;
                        }
                    }
                    ll.setLength(0);
                } else if (readByte == 0xD) {
                    continue;
                }
                sb.append((char) readByte);
                ll.append((char) readByte);
            }

            logTail = sb.reverse().toString();
        } catch (FileNotFoundException ex) {
            log.error("Cannot find log file {}", file.toString(), ex);
        } catch (IOException ex) {
            log.error("Cannot read form log file.", ex);
        }
        return logTail;
    }

    /**
     * This function retrieves scenario and stores it in a file.
     *
     * @return File pointing at retrieved jMeter scenario.
     */
    private File retrieveScenarioFile() {
        File scenarioFile;
        log.info("Scenario is: {}", scenario);
        switch (scenarioType) {
            case BUILT_IN:
                scenarioFile = resourceToFile(scenario);
                break;

            case URL:
                scenarioFile = downloadScenarioFile(scenario);
                break;

            default:
                throw ErrorUtils.logErrorAndReturnException(log, "Unknown scenario type: {0}",
                    scenarioType);
        }
        return scenarioFile;
    }


    @Override
    public String deployExtension(String jmeterDir, String artifactoryUrl, String groupId,
        String artifactId, String version, String classifier, String type) {
        if (artifactoryUrl == null) {
            artifactoryUrl = JMETER_ARTIFACTORY_URL;
        }

        try {
            ArtifactFetchResult fetchResult = dm
                .fetchTempArtifact(artifactoryUrl, groupId, artifactId, version, classifier, type);
            File jmeterExtension = fetchResult.getFile();
            File destFile = Paths
                .get(getJMeterBasePath().getAbsolutePath(), "lib", "ext", jmeterExtension.getName())
                .toAbsolutePath().toFile();
            try {
                Files.move(jmeterExtension.toPath(), destFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw ErrorUtils
                    .logExceptionAndWrapFmt(log, e, "Failed to move {1} to {2}. Exception: {0}",
                        jmeterExtension.getAbsolutePath(), destFile.getAbsolutePath());
            }
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            throw ErrorUtils
                .logExceptionAndWrapFmt(log, e,
                    "Failed to deploy jMeter extension {1}:{2}:{3}. Exception: {0}", groupId,
                    artifactId, version);

        }
    }

    @Override
    public String getJmeterPath() {
        File tmpDir = getTempDir(false);
        return tmpDir != null ? tmpDir.getAbsolutePath() : null;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
