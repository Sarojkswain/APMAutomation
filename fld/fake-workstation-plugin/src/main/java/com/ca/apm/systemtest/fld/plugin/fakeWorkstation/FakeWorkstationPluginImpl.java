package com.ca.apm.systemtest.fld.plugin.fakeWorkstation;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeAttribute;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.annotations.OperationParameter;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryLiteDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.run.RunPlugin;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * An agent plugin to download a fake workstation and run loads with help of it.
 * 
 * @author sinal04
 *
 */
public class FakeWorkstationPluginImpl extends AbstractPluginImpl implements FakeWorkstationPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeWorkstationPluginImpl.class);

    @Autowired
    ArtifactoryLiteDownloadMethod dm;

    @ExposeAttribute(description = "Run plugin.")
    private RunPlugin runPlugin;

    
    /**
     * Returns default artifact group Id if any.
     * 
     * @return default group id
     */
    @Override
    @ExposeMethod(description = "Returns default group id for fake workstation tool.")
    public String getDefaultGroupId() {
        return DEFAULT_GROUP_ID;
    }

    /**
     * Returns default artifact Id if any.
     * 
     * @return default artifact id
     */
    @Override
    @ExposeMethod(description = "Returns default artifact id for fake workstation tool.")
    public String getDefaultArtifactId() {
        return DEFAULT_ARTIFACT_ID;
    }

    /**
     * Returns default artifact version if any.
     * 
     * @return default artifact version
     */
    @Override
    @ExposeMethod(description = "Returns default version for fake workstation tool.")
    public String getDefaultVersion() {
        return DEFAULT_VERSION;
    }

    /**
     * Returns default artifact type if any.
     * 
     * @return default artifact type
     */
    @Override
    @ExposeMethod(description = "Returns default type for fake workstation tool.")
    public String getDefaultType() {
        return DEFAULT_TYPE;
    }

    /**
     * Return default artifact classifier if any.
     * 
     * @return default artifact classifier
     */
    @Override
    @ExposeMethod(description = "Returns default classifier for fake workstation tool.")
    public String getDefaultClassifier() {
        return null;
    }

    @Override
    @ExposeMethod(description = "Downloads fake workstation tool to the specified location.")
    public String downloadFakeWorkstation(@OperationParameter(description = "Fake workstation artifact version", 
                                                              name = "version", 
                                                              required = true) String version, 
                                          @OperationParameter(description = "Download target directory", 
                                                              name = "toDir", 
                                                              required = true) String toDir, 
                                          @OperationParameter(description = "Download file name", 
                                                              name = "fileName", 
                                                              required = true) String fileName, 
                                          @OperationParameter(description = "Download file name extension", 
                                                              name = "extension", 
                                                              required = true) String extension) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Fake Workstation plugin downloadFakeWorkstation(...) called with parameters: ");
            LOGGER.info("version: {}", version);
            LOGGER.info("to dir: {}", toDir);
            LOGGER.info("file name: {}", fileName);
            LOGGER.info("extension: {}", extension);
        }
        
        version = version == null ? getDefaultVersion() : version;

        ArtifactFetchResult result = dm.fetchTempArtifact(DEFAULT_ARTIFACTORY_URL, getDefaultGroupId(),
            getDefaultArtifactId(), version, getDefaultClassifier(), getDefaultType());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Fake Workstation download result: {}", result);
        }

        if (toDir != null && isNotBlank(toDir)) {
            File targetDir = new File(toDir);
            if (!targetDir.exists()) {
                if (!targetDir.mkdirs()) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Failed to create directory: {}", targetDir);
                        return result != null && result.getFile() != null ? result.getFile().getAbsolutePath() : null;
                    }
                } else {
                    System.out.println("Successfully created directory at " + targetDir);
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Successfully created directory at {}", targetDir);
                    }                    
                }
            }
            
            if (fileName != null && isNotBlank(fileName)) {
                File targetFile = new File(targetDir, isNotBlank(extension) ? fileName + extension : fileName);
                int i = 1;
                while (targetFile.exists()) {                
                    targetFile = new File(targetDir, isNotBlank(extension) ? fileName + i + extension : fileName + i);
                    i++;
                }
                try {
                    System.out.println("Moving downloaded artifact from " + result.getFile() + " to " + targetFile);
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Moving downloaded artifact from {} to {}", result.getFile(), targetFile);
                    }

                    FileUtils.moveFile(result.getFile(), targetFile);
                    return targetFile.getAbsolutePath();
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled()) {
                        ErrorUtils.logExceptionFmt(LOGGER, e,
                            "Failed to move file {1} to {2}. Exception: {0}",
                            result.getFile(), targetFile);
                    }
                }
            } 
        }
        return result != null && result.getFile() != null ? result.getFile().getPath() : null;
    }

    @Override
    @ExposeMethod(description = "Runs fake workstation against the specified MOM.")
    public String runQueriesAgainstMOM(String fakeWorkstationJar, String[] javaOptions, String host,
        long port, String user, String password, long resolution, long sleepBetween,
        String historicalOption, String metric, String agent) {
        
        Map<String, Object> vars = new LinkedHashMap<>(11);
        vars.put("JVM_OPTIONS", javaOptions != null ? javaOptions : DEFAULT_JVM_OPTIONS);
        vars.put("FAKE_WORKSTATION_JAR", fakeWorkstationJar);
        vars.put("HOST", host);
        vars.put("PORT", port);
        vars.put("USER", user);
        vars.put("PASSWORD", password);
        vars.put("RESOLUTION", resolution);
        vars.put("SLEEP_BETWEEN", sleepBetween);
        vars.put("AGENT", agent);
        vars.put("METRIC_NAME", metric);
        historicalOption = historicalOption == null ? "" : historicalOption;
        vars.put("HISTORICAL_OPTION", historicalOption);
        
        return runPlugin.runProcess(RUN_FAKE_WORKSTATION_COMMAND, vars);
        
    }

    @Override
    @ExposeMethod(description = "Returns logs for the fake workstation process specified by the given id.")
    public String getFakeWorkstationLogs(String id, int size) {
        return runPlugin.getLog(id, size);
    }

    @Override
    @ExposeMethod(description = "Stops the specified fake workstation process.")
    public void stopFakeWorkstationProcess(String id) {
        runPlugin.stopProcess(id);
    }

    @Override
    @ExposeMethod(description = "Stops all fake workstation processes.")
    public void stopAllFakeWorkstationProcesses() {
        runPlugin.stopAllProcesses();
    }
    
    @Autowired
    public void setRunPlugin(RunPlugin runPlugin) {
        this.runPlugin = runPlugin;
    }

    @Override
    @ExposeMethod(description = "Tells if the particular fake workstation process connected to EM and is running its load.")
    public Boolean isLoadRunning(String procId) {
        String log = getFakeWorkstationLogs(procId, 512);
        return log != null && log.contains("Average (for past");
    }
    
}
