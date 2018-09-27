/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.fakeWorkstation;


import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * Load Orchestrator Agent plugin to run fake workstation. Uses Run Plugin to start, monitor logs and stop fake workstation process.  
 * 
 * @author sinal04
 *
 */
@PluginAnnotationComponent(pluginType = "fakeWorkstation")
public interface FakeWorkstationPlugin extends Plugin {

    String RUN_FAKE_WORKSTATION_COMMAND = "runFakeWorkstation";
    String DEFAULT_GROUP_ID = "com.ca.apm.coda-projects.test-tools";
    String DEFAULT_ARTIFACT_ID = "fakeworkstation";
    String DEFAULT_VERSION = "99.99.sys-SNAPSHOT";
    String DEFAULT_TYPE = "jar";
    String DEFAULT_ARTIFACTORY_URL = "http://artifactory-emea-cz.ca.com:8081/artifactory/repo";
    String DEFAULT_TEMP_DIR_NAME = "fakeWorkstation";
    String[] DEFAULT_JVM_OPTIONS = new String[] { "-Xms96m", "-Xmx512m", "-XX:+HeapDumpOnOutOfMemoryError" };
    
    
    /**
     * Returns default artifact group Id if any.
     * 
     * @return default group id
     */
    String getDefaultGroupId();

    /**
     * Returns default artifact Id if any.
     * 
     * @return default artifact id
     */
    String getDefaultArtifactId();

    /**
     * Returns default artifact version if any.
     * 
     * @return default artifact version
     */
    String getDefaultVersion();

    /**
     * Returns default artifact type if any.
     * 
     * @return default artifact type
     */
    String getDefaultType();

    /**
     * Return default artifact classifier if any.
     * 
     * @return default artifact classifier
     */
    String getDefaultClassifier();

    /**
     * Downloads fake workstation jar for the given <code>version</code>.
     * 
     * @param version     artifact version
     * @param toDir       directory where the artifact should be copied to; maybe null
     * @param fileName    desired target file name for the artifact
     * @param extension   desired extension for the artifact (e.g. ".jar", ".war", etc.) 
     * @return            downloaded artifact file; in case at least <code>toDir</code> and 
     *                    <code>fileName</code> are specified then the original file is moved to the specified location 
     *                
     */
    String downloadFakeWorkstation(String version, String toDir,
        String fileName, String extension);

    /**
     * Runs fake workstation queries against EM (MOM). 
     * 
     * @param fakeWorkstationJar
     * @param javaOptions
     * @param host
     * @param port
     * @param user
     * @param password
     * @param resolution
     * @param sleepBetween
     * @param historicalOption
     * @param metric
     * @param agent
     * @return  unique id to use to fetch fake workstation logs
     */
    String runQueriesAgainstMOM(String fakeWorkstationJar, String[] javaOptions, String host,
        long port, String user,
        String password, long resolution, long sleepBetween, String historicalOption, String metric,
        String agent);


    /**
     * Gets logs for the running fake workstation process.
     * 
     * @param procId       unique id obtained after launching a fake workstation process
     * @param size     size of the log content to fetch
     * @return         log content
     */
    String getFakeWorkstationLogs(String procId, int size);

    /**
     * Returns true if the fake workstation's load is up and reaching EM.
     * 
     * @param procId  fake workstation process id
     * @return        true if all good; otherwise false
     */
    Boolean isLoadRunning(String procId);
    
    /**
     * Stops the running fake workstation process mapped by the <code>id</code>.
     * @param id
     * 
     */
    void stopFakeWorkstationProcess(String id);
    
    /**
     * Stops all running fake workstation processes.
     */
    void stopAllFakeWorkstationProcesses();
}
