package com.ca.apm.systemtest.fld.plugin.agenthvr;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeAttribute;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;

/**
 * Created by meler02 on 5/28/2015.
 */
public class AgentHvrPluginImpl extends AbstractPluginImpl implements AgentHvrPlugin {

    private static final String AGENT_HVR_ARTIFACT_GROUP_ID =
        "com.ca.apm.coda-projects.test-tools";
    private static final String AGENT_HVR_ARTIFACT_ARTIFACT_ID = "hvragent";
    private static final String AGENT_HVR_ARTIFACT_DEFAULT_VERSION = "99.99.sys-SNAPSHOT";

    private static final String MANAGEMENT_MODULES_ARTIFACT_GROUP_ID = "com.ca.apm.systemtest.fld";
    private static final String MANAGEMENT_MODULES_ARTIFACT_ARTIFACT_ID = "management-modules";
    private static final String MANAGEMENT_MODULES_ARTIFACT_DEFAULT_VERSION
        = "99.99.aquarius-SNAPSHOT";
    private static final String MANAGEMENT_MODULES_BASELINE_JAR = "FLDBaseline-mm.jar";

    private static final String DEFAULT_ARTIFACTORY_URL =
        "http://oerth-scx.ca.com:8081/artifactory/repo";

    @ExposeAttribute(description = "AgentHVR distribution ZIP file")
    private File agentHvrZip;
    @ExposeAttribute(description
        = "Directory of AgentHVR distribution extracted from its distribution ZIP file")
    private File agentHvrDir;

    @ExposeAttribute(description = "Management Modules distribution ZIP file")
    private File managementModulesZip;
    @ExposeAttribute(description
        = "Directory of Management Modules distribution extracted from its distribution ZIP file")
    private File managementModulesDir;

    @ExposeAttribute(description = "Working directory")
    private File tempDir;

    private static boolean interrupted = false;
    private static Thread agentProcessThread;

    @Autowired
    ArtifactoryDownloadMethod dm;

    public AgentHvrPluginImpl() {
    }


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
            setTempDir(java.nio.file.Files.createTempDirectory("agent-hvr-plugin").toFile());
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


    @ExposeMethod(description = "Download AgentHVR ZIP file.")
    @Override
    public File downloadAgentHvr(String version) {
        String agentHvrVersion = version;
        if (StringUtils.isBlank(agentHvrVersion)) {
            agentHvrVersion = AGENT_HVR_ARTIFACT_DEFAULT_VERSION;
        }

        info("Downloading Agent HVR version " + agentHvrVersion);

        ArtifactFetchResult fetchResult = dm.fetchTempArtifact(DEFAULT_ARTIFACTORY_URL,
                AGENT_HVR_ARTIFACT_GROUP_ID, AGENT_HVR_ARTIFACT_ARTIFACT_ID,
                agentHvrVersion, "dist", "zip");

        agentHvrZip = fetchResult.getFile();
        return agentHvrZip;
    }


    @ExposeMethod(description = "Extracts installer artifact from given zip file.")
    @Override
    public File unzipAgentHvrZip() {
        agentHvrDir = new File(tempDir, "agent-hvr");
        info("Unzipping Agent HVR into " + agentHvrDir.getPath());
        try {
            FileUtils.forceMkdir(agentHvrDir);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create directory {1}. Exception: {0}",
                agentHvrDir);
        }
        try {
            ZipFile artifactZip = new ZipFile(agentHvrZip);
            artifactZip.extractAll(agentHvrDir.getAbsolutePath());
            log.info("Extracted ZIP file {} into {}.", agentHvrZip.getAbsolutePath(),
                agentHvrDir.getAbsolutePath());
        } catch (ZipException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create extract artifact {1}. Exception: {0}",
                agentHvrZip);
        }
        return agentHvrDir;
    }

    @ExposeMethod(description = "Creates temporary working directory.")
    @Override
    public void configureExecutables(String agentEmDir, String momHost, String momPort, String userNameMom,
        String passwordMom, String agentHostName) {
        try {
            info("Configuring Agent HVR executables");
            //
            ClassLoader classLoader = getClass().getClassLoader();
            Charset charset = StandardCharsets.UTF_8;
            //
            // HVRAgent.9.8.bat
            Path source = Paths.get(classLoader.getResource("HVRAgent.9.8.bat").toURI());
            Path destination = Paths.get(agentHvrDir.getPath() + "/HVRAgent.9.8.bat");
            //
            String content = new String(Files.readAllBytes(source), charset);
            content = content.replace("[WORKING_DIR]", agentHvrDir.getPath());
            content = content.replace("[EM_DIR]", Paths.get(agentEmDir).toString());
            Files.write(destination, content.getBytes(charset));
            //
            // Replay_HVRAgent.9.8.bat
            source = Paths.get(classLoader.getResource("Replay_HVRAgent.9.8.bat").toURI());
            destination = Paths.get(agentHvrDir.getPath() + "/Replay_HVRAgent.9.8.bat");
            //
            content = new String(Files.readAllBytes(source), charset);
            content = content.replace("[WORKING_DIR]", agentHvrDir.getPath());
            content = content.replace("[EM_HOST]", momHost);
            content = content.replace("[EM_PORT]", momPort);
            content = content.replace("[USER_NAME_EM]", userNameMom);
            content = content.replace("[PASSWORD_EM]", passwordMom);
            content = content.replace("[AGENT_HOST_NAME]", agentHostName);
            Files.write(destination, content.getBytes(charset));
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e,
                "Failed to copy template files. Exception: {0}");
        } catch (URISyntaxException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e,
                "Failed to resolve file names. Exception: {0}");
        }

    }

    @ExposeMethod(description = "Execute Agent HVR")
    @Override
    public String execute() {
        agentProcessThread = new Thread() {
            @Override
            public void run() {
                try {
                    info("Executing " + agentHvrDir.getPath() + "\\Replay_HVRAgent.9.8.bat");
                    //
                    Process p = Runtime.getRuntime().exec(new String[]{
                        "cmd.exe", "/C", /*"start",*/
                        agentHvrDir.getPath() + "\\Replay_HVRAgent.9.8.bat"}, null, agentHvrDir);
                    Integer exitValue = null;
                    while (exitValue == null && !interrupted) {
                        try {
                            exitValue = p.exitValue();
                            continue;
                        } catch (IllegalThreadStateException e) {
                            // silent, Process has not terminated yet
                        }
                        info("Agent HVR is running");
                        Thread.sleep(10000);
                    }
                    if (interrupted) {
                        info("Stopping Agent HVR programmatically");
                        p.destroy();
                        info("Agent HVR stopped");
                    } else {
                        info("Agent HVR stopped with code '" + p.exitValue() + "'");
                    }
                    agentProcessThread = null;
                } catch (IOException e) {
                    throw ErrorUtils.logExceptionAndWrap(log, e,
                        "Failed to run the batch file. Exception: {0}");
                } catch (InterruptedException e) {
                    throw ErrorUtils.logExceptionAndWrap(log, e,
                        "Process interrupted. Exception: {0}");
                }
            }
        };
        agentProcessThread.start();
        return null;
    }


    @ExposeMethod(description = "Check if Agent HVR is running")
    @Override
    public boolean checkRunning() {
        return agentProcessThread != null && agentProcessThread.isAlive();
    }

    @ExposeMethod(description = "Stop Agent HVR")
    @Override
    public void stop() {
        interrupted = true;
    }

    @ExposeMethod(description = "Download Management Modules ZIP file.")
    @Override
    public File downloadManagementModules(String version) {
        String mmVersion = version;
        if (StringUtils.isBlank(mmVersion)) {
            mmVersion = MANAGEMENT_MODULES_ARTIFACT_DEFAULT_VERSION;
        }

        info("Downloading Management Modules version " + mmVersion);

        ArtifactFetchResult fetchResult = dm.fetchTempArtifact(DEFAULT_ARTIFACTORY_URL,
                MANAGEMENT_MODULES_ARTIFACT_GROUP_ID, MANAGEMENT_MODULES_ARTIFACT_ARTIFACT_ID,
                mmVersion, "dist", "zip");

        managementModulesZip = fetchResult.getFile();
        return managementModulesZip;
    }


    @ExposeMethod(description = "Extracts MMs from given zip file.")
    @Override
    public File unzipManagementModules() {
        managementModulesDir = new File(tempDir, "management-modules");
        info("Unzipping Management Modules into " + managementModulesDir.getPath());
        try {
            FileUtils.forceMkdir(managementModulesDir);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create directory {1}. Exception: {0}",
                managementModulesDir);
        }
        try {
            ZipFile artifactZip = new ZipFile(managementModulesZip);
            artifactZip.extractAll(managementModulesDir.getAbsolutePath());
            log.info("Extracted ZIP file {} into {}.", managementModulesZip.getAbsolutePath(),
                managementModulesDir.getAbsolutePath());
        } catch (ZipException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create extract artifact {1}. Exception: {0}",
                managementModulesZip);
        }
        return managementModulesDir;
    }

    @ExposeMethod(description = "Copies the selected Management Module to EM")
    @Override
    public void configureManagementModule(String emDir, String mmJarName) {
        try {
            info("Configuring Management Module");
            //
            String mmJar = mmJarName;
            if (StringUtils.isBlank(mmJarName)) {
                mmJar = MANAGEMENT_MODULES_BASELINE_JAR;
            }
            //
            info("Configuring Management Module " + mmJar);
            //
            Path source = Paths.get(managementModulesDir.getPath() + "/" + mmJar);
            Path destination = Paths.get(emDir + "/deploy/" + mmJar);
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e,
                "Failed to copy JAR file. Exception: {0}");
        }
    }

    @ExposeMethod(description = "Check if Management Module is installed")
    @Override
    public boolean checkManagementModuleInstalled(String emDir, String mmJarName) {
        String mmJar = mmJarName;
        if (StringUtils.isBlank(mmJarName)) {
            mmJar = MANAGEMENT_MODULES_BASELINE_JAR;
        }
        Path mmInstallationPath = Paths.get(emDir + "/config/modules/" + mmJar);
        return Files.exists(mmInstallationPath);
    }
}
