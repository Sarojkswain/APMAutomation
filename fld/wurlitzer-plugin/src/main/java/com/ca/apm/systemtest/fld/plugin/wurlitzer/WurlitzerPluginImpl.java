package com.ca.apm.systemtest.fld.plugin.wurlitzer;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ACFileUtils;
import com.ca.apm.systemtest.fld.common.FreemarkerUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeAttribute;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Plugin to handle Wurlitzer load generator.
 *
 * @author haiva01
 */
public class WurlitzerPluginImpl extends AbstractPluginImpl implements WurlitzerPlugin {
    private static final Logger log = LoggerFactory.getLogger(WurlitzerPluginImpl.class);
    private static final String WURLITZER_ARTIFACT_GROUP_ID =
        "com.ca.apm.coda-projects.test-projects";
    private static final String WURLITZER_ARTIFACT_ARTIFACT_ID = "Wurlitzer";
    private static final String WURLITZER_ARTIFACT_DEFAULT_VERSION = "99.99.sys-SNAPSHOT";
    private static final String DEFAULT_ARTIFACTORY_URL =
        "http://oerth-scx.ca.com:8081/artifactory/repo";
    private static final String ANT_BUILD_XML_TEMPLATE = "build.xml.ftl";
    private static final String COMSPEC = System.getenv("ComSpec");
    private static final String ANT_EXECUTABLE_NAME = SystemUtils.IS_OS_WINDOWS ? "ant.bat" : "ant";
    private static final String APPMAP_BTC_MINIAGENT_DIR = "appmap-btc-miniagent";
    private static final String SCRIPTS_DIR = "scripts";
    private static final String XML_DIR = "xml";

    @ExposeAttribute(description = "Wurlitzer distribution ZIP file")
    private File wurlitzerZip;
    @ExposeAttribute(description = "Directory of Wurlitzer distribution extracted from its distribution ZIP file")
    private File wurlitzerDir;
    @ExposeAttribute(description = "Working directory")
    private File tempDir;
    private String builtInScenario;
    private URL scenarioUrl;
    // XXX Part of incomplete implementation:
    private String buildXMLFile;
    private String targetInBuilXmlFile = "entity-alert-metrics";
    // XXX ^^^
    private int durationHours = 1;
    private boolean debug = false;
    private int initialEdgeSetDelayMinutes = 0;
    private String emHost = "localhost";
    private int emPort = 5001;

    private ArtifactoryDownloadMethod dm;

    @ExposeMethod(description = "")
    @Override
    public int getDurationHours() {
        return durationHours;
    }

    @ExposeMethod(description = "")
    @Override
    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    @ExposeMethod(description = "")
    @Override
    public boolean isDebug() {
        return debug;
    }

    @ExposeMethod(description = "")
    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @ExposeMethod(description = "")
    @Override
    public int getInitialEdgeSetDelayMinutes() {
        return initialEdgeSetDelayMinutes;
    }

    @ExposeMethod(description = "")
    @Override
    public void setInitialEdgeSetDelayMinutes(int initialEdgeSetDelayMinutes) {
        this.initialEdgeSetDelayMinutes = initialEdgeSetDelayMinutes;
    }

    @ExposeMethod(description = "")
    @Override
    public String getEmHost() {
        return emHost;
    }

    @ExposeMethod(description = "")
    @Override
    public void setEmHost(String emHost) {
        this.emHost = emHost;
    }

    @ExposeMethod(description = "")
    @Override
    public int getEmPort() {
        return emPort;
    }

    @ExposeMethod(description = "")
    @Override
    public void setEmPort(int emPort) {
        this.emPort = emPort;
    }

    private enum ScenarioSource {
        BUILT_IN, URL, NO_SCENARIO, BUILD_TARGET
    }

    ScenarioSource scenarioSource;

    public WurlitzerPluginImpl() {}

    private File getTempDir() {
        if (tempDir == null || !tempDir.exists()) {
            createTempDir();
        }

        return tempDir;
    }

    @ExposeMethod(description = "Creates temporary working directory.")
    @Override
    public void createTempDir() {
        try {
            setTempDir(java.nio.file.Files.createTempDirectory("wurlitzer-plugin").toFile());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e,
                "Failed to create temporary directory. Exception: {0}");
        }
    }

    private void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    @ExposeMethod(description = "Delete temporary working directory.")
    @Override
    public void deleteTempDir() {
        File dir = getTempDir();
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to delete directory {1}. Exception: {0}", dir);
        }
        setTempDir(null);
    }

    @ExposeMethod(description = "Download Wurlitzer ZIP file.")
    @Override
    public File downloadWurlitzer(String version) {
        String wurlitzerVersion = version;
        if (StringUtils.isBlank(wurlitzerVersion)) {
            wurlitzerVersion = WURLITZER_ARTIFACT_DEFAULT_VERSION;
        }

        ArtifactFetchResult fetchResult =  dm.fetchTempArtifact(DEFAULT_ARTIFACTORY_URL, WURLITZER_ARTIFACT_GROUP_ID, WURLITZER_ARTIFACT_ARTIFACT_ID, wurlitzerVersion, null, "zip");

        wurlitzerZip = fetchResult.getFile();
        return wurlitzerZip;
    }

    @ExposeMethod(description = "Extracts installer artifact from given zip file.")
    @Override
    public File unzipWurlitzerZip() {
        wurlitzerDir = new File(tempDir, "wurlitzer");
        try {
            FileUtils.forceMkdir(wurlitzerDir);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create directory {1}. Exception: {0}", wurlitzerDir);
        }

        try {
            ZipFile artifactZip = new ZipFile(wurlitzerZip);
            artifactZip.extractAll(wurlitzerDir.getAbsolutePath());
            log.info("Extracted ZIP file {} into {}.", wurlitzerZip.getAbsolutePath(),
                wurlitzerDir.getAbsolutePath());
        } catch (ZipException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create extract artifact {1}. Exception: {0}", wurlitzerZip);
        }

        return wurlitzerDir;
    }

    @Override
    public void setBuiltInScenario(String scenario) {
        builtInScenario = scenario;
        scenarioSource = ScenarioSource.BUILT_IN;
    }

    @Override
    public void setScenarioUrl(URL scenario) {
        scenarioUrl = scenario;
        scenarioSource = ScenarioSource.URL;
    }

    @Override
    public void setNoScenario(String scenario) {
        scenarioSource = ScenarioSource.NO_SCENARIO;
    }

    @Override
    public void setBuiltTargetScenario(String scenario) {
        scenarioSource = ScenarioSource.BUILD_TARGET;
    }

    /**
     * This function creates Apache Ant project file from template.
     *
     * @param scenarioFile
     *        Wurlitzer scenario file to be executed by the generated Apache
     *        Ant project file
     * @return generated Apache Ant project file name
     */
    private File processBuildXmlTemplate(File scenarioFile) {
        Map<String, Object> emprops = new HashMap<>(2);
        emprops.put("host", emHost);
        emprops.put("port", emPort);

        Map<String, Object> wprops = new HashMap<>(6);
        wprops.put("scenarioFile", scenarioFile.getAbsoluteFile());
        wprops.put("durationHours", durationHours);
        wprops.put("debug", debug);
        wprops.put("initialEdgeSetDelayMinutes", initialEdgeSetDelayMinutes);
        wprops.put("em", emprops);

        Map<String, Object> props = new HashMap<>(2);
        props.put("wurlitzer", wprops);
        props.put("baseDir", new File(wurlitzerDir, "scripts").getAbsolutePath());

        Configuration freemarkerConfig = FreemarkerUtils.getConfig();
        Template template = FreemarkerUtils.getTemplate(freemarkerConfig, ANT_BUILD_XML_TEMPLATE);
        File outputBuildXml = ACFileUtils.generateTemporaryFile("build", ".xml", wurlitzerDir);
        FreemarkerUtils.processTemplate(outputBuildXml, template, props);

        return outputBuildXml;
    }

    /**
     * Execute given Apache Ant build.xml file.
     *
     * @param buildXml
     *        Apache Ant XML project file
     * @return process ID (not necessarily OS PID)
     */
    private String executeAntOnBuildXml(File buildXml, String target) {
        if (SystemUtils.IS_OS_WINDOWS) {
            String id = null;
            try {
                id = "" + System.currentTimeMillis();

                if (target == null || target.isEmpty()) target = "execute";

                ProcessBuilder builder = ProcessUtils.newProcessBuilder(false);
                builder
                    .command(COMSPEC != null ? COMSPEC : "cmd.exe", "/C", findAntExecutable(),
                        "-v", "-f", buildXml.getAbsolutePath(), target).directory(wurlitzerDir)
                    .redirectErrorStream(true).redirectOutput(new File(wurlitzerDir, "output.log"));

                builder.environment().put("ANT_OPTS", "-Did=" + id);

                builder.start();
            } catch (IOException e) {
                log.error("Cannot start wurlitzer process", e);
            }
            return id;
        } else {
            // TODO: Linux implementation.
            throw new NotImplementedException(
                "Executing Wurlitzer on non-Windows system is not implemented, yet.");
        }
    }

    private String executeScenarioFile(File scenarioFile) {
        File buildXml = processBuildXmlTemplate(scenarioFile);
        return executeAntOnBuildXml(buildXml, null);
    }

    /**
     * Get Apache Ant executable path. Use ant.home system property, if it is
     * set, or use ANT_HOME environment variable otherwise, if it is set and it
     * contains the expected files.
     */
    private String findAntExecutable() {
        String antPath = null;
        String antHome = System.getProperty("ant.home", System.getenv("ANT_HOME"));
        if (antHome != null) {
            File antBin = new File(antHome, "bin");
            File ant = new File(antBin, ANT_EXECUTABLE_NAME);
            if (ant.exists()) {
                antPath = ant.getAbsolutePath();
            } else {
                log.warn("Cannot find {} in {}.", ANT_EXECUTABLE_NAME, antBin.getAbsolutePath());
            }
        }

        if (antPath == null) {
            log.warn("Apache Ant executable found neither using ant.home system property"
                + " nor using ANT_HOME environment variable. Assuming it is in PATH.");
            antPath = ANT_EXECUTABLE_NAME;
        }

        log.info("Using {} to execute Apache Ant.", antPath);

        return antPath;
    }

    /**
     * Download Wurlitzer scenario file from given URL.
     *
     * @param scenarioUrl
     *        Wurlitzer scenario file URL
     * @return file where given Wurlitzer scenario has been downloaded to
     */
    private File downloadScenarioFile(URL scenarioUrl) {
        log.info("Downloading scenario from {}.", scenarioUrl);

        ReadableByteChannel rbc;
        try {
            rbc = Channels.newChannel(scenarioUrl.openStream());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e, "Failed to open {1}. Exception: {0}",
                scenarioUrl.toString());
        }

        File downloadedScenarioFile;
        try {
            downloadedScenarioFile = File.createTempFile("scenario", ".xml", wurlitzerDir);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create temporary file for scenario download. Exception: {0}");
        }

        long downloadedBytes;
        try (FileOutputStream fos = new FileOutputStream(downloadedScenarioFile)) {
            try {
                downloadedBytes = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Error during scenario file download. Exception: {0}");
            }

            log.debug("Transferred {} bytes.", downloadedBytes);
        } catch (FileNotFoundException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to open file {1} for scenario download. Exception: {0}",
                downloadedScenarioFile);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Error closing file {1}. Exception: {0}", downloadedScenarioFile);
        }

        log.info("Downloaded {} bytes into {}.", downloadedBytes, downloadedScenarioFile);
        return downloadedScenarioFile;
    }

    @ExposeMethod(description = "Execute Wurlitzer scenario.")
    @Override
    public String execute() {
        switch (scenarioSource) {
            case BUILT_IN:
                return executeScenarioFile(new File(wurlitzerDir, builtInScenario));

            case URL: {
                File file = downloadScenarioFile(scenarioUrl);
                return executeScenarioFile(file);
            }

            case BUILD_TARGET: {
                return executeBuildFileWithTarget(buildXMLFile, targetInBuilXmlFile);
            }

            default:
                throw ErrorUtils.logErrorAndReturnException(log, "Unknown scenario source: {0}",
                    scenarioSource);
        }
    }

    @ExposeMethod(description = "Stop running Wurlitzer scenario.")
    @Override
    public void stop(String wurlitzerId) {
        if (SystemUtils.IS_OS_WINDOWS) {
            Long pid = ProcessUtils.findJavaProcessPid(wurlitzerId);
            if (pid != null) {
                ProcessUtils.killWindowsProcess(pid);
            }
        } else {
            // TODO: Linux implementation.
            throw new NotImplementedException(
                "Executing Wurlitzer on non-Windows system is not implemented, yet.");
        }
    }

    /**
     * Execute buildFile name and target name inside now used only for path
     * wurlitzer/script/xml/appmap-btc-miniagent/build.xml in future maybe loop
     * through subdirectories{DIR} and {buildFilename} in
     * wurlitzer/script/xml/{DIR}/{buildFilename} ?
     *
     * @param buildFileName
     *        , target
     * @return wurlitzer ID
     */
    @Override
    public String executeBuildFileWithTarget(String buildFileName, String target) {
        String respWurlitzerId = null;

        File scriptxmlSubDir =
            new File(wurlitzerDir + File.separator + SCRIPTS_DIR + File.separator + XML_DIR);
        File[] files = scriptxmlSubDir.listFiles();
        if (files == null) {
            throw ErrorUtils.logErrorAndReturnException(log, "{0} is not a directory.",
                scriptxmlSubDir.getAbsolutePath());
        }

        outerloop: for (File wFile : files) {
            if (wFile.isDirectory()) {
                switch (wFile.getName()) {
                    case APPMAP_BTC_MINIAGENT_DIR:
                        File tempFile =
                            new File(scriptxmlSubDir + File.separator + APPMAP_BTC_MINIAGENT_DIR
                                + File.separator + buildFileName);
                        respWurlitzerId = executeAntOnBuildXml(tempFile, target);
                        break outerloop;
                    default:
                        log.debug("No dir, subdir check in path: {}",
                            scriptxmlSubDir.getAbsolutePath());
                }
            }
        }

        return respWurlitzerId;
    }

    /**
     * Execute buildFile name and target name inside now used only for path
     * wurlitzer/script/xml/.../build.xml in future maybe loop through
     * subdirectories{DIR} and {buildFilename} in
     * wurlitzer/script/xml/{DIR}/{buildFilename} ?
     *
     * @param buildFileName
     *        , target
     * @return wurlitzer ID
     */
    @Override
    public String executeBuildFileWithTarget(String subPath, String buildFileName, String target) {
        String respWurlitzerId = null;
        File scriptxmlSubDir =
            new File(wurlitzerDir + File.separator + SCRIPTS_DIR + File.separator + XML_DIR);
        File tempFile =
            new File(scriptxmlSubDir + File.separator + subPath + File.separator + buildFileName);
        log.info("Wurlitzer build file: {}", tempFile.getAbsolutePath());
        if (tempFile.exists()) {
            respWurlitzerId = executeAntOnBuildXml(tempFile, target);
        } else {
            log.debug("File doesn't exist: {}", tempFile.getAbsolutePath());
        }
        return respWurlitzerId;
    }

    @Override
    public String editBuildFile(String subPath, String buildFileName, String emHost, int emPort) {
        File scriptxmlSubDir =
            new File(wurlitzerDir + File.separator + SCRIPTS_DIR + File.separator + XML_DIR
                + File.separator + subPath);
        File tempFile = new File(scriptxmlSubDir + File.separator + buildFileName);
        log.info("Wurlitzer build file: {}", tempFile.getAbsolutePath());

        try {
            String content = FileUtils.readFileToString(tempFile, "UTF-8");
            content =
                content.replaceAll("\\$\\{WURLITZER.TARGET.EM.HOST\\}", emHost).replaceAll(
                    "\\$\\{WURLITZER.TARGET.EM.PORT\\}", String.valueOf(emPort));
            File outputFile = File.createTempFile(buildFileName, ".xml", scriptxmlSubDir);
            FileUtils.writeStringToFile(outputFile, content, "UTF-8");
            return outputFile.getName();
        } catch (IOException e) {
            log.debug("Modifying file failed: {}", tempFile.getAbsolutePath());
        }
        return tempFile.getName();
    }

    /**
     * Replace content in found file.
     *
     * @param searchForFile
     */
    public void replaceContent(String searchForFile) {
        // XXX TODO: This function is incomplete! It is used from loads/432951
        // Entity Alerts.bpmn
        ACFileUtils.searchForFileTree(wurlitzerDir, searchForFile);
    }

    @Autowired
    public void setDownloadMethod(ArtifactoryDownloadMethod dm) {
        this.dm = dm;
    }
}
