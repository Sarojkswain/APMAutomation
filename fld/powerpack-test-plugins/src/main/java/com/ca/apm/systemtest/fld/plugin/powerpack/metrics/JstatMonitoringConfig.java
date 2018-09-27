package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

/**
 * Performance configuration for running Jstat
 *
 * @author Saravana Raguram (rsssa02).
 */
public class JstatMonitoringConfig implements PerfMonitoringConfig {

    private String jstatLogFileName;
    private String outputLogDirPath;
    private Integer samplesCount;
    private Integer sampleIntervalMillis;
    private String processName;
    private String cmdLineName;

    /**
     * Default constructor.
     */
    public JstatMonitoringConfig() {
    }

    /**
     * 
     * @param outputLogDirPath
     * @param sampleIntervalMillis
     * @param samplesCount
     * @param jstatLogFileName
     * @param processName
     * @param cmdLineName
     */
    public JstatMonitoringConfig(String outputLogDirPath, Integer sampleIntervalMillis, Integer samplesCount, 
                                 String jstatLogFileName, String processName, String cmdLineName) {
        this.jstatLogFileName = jstatLogFileName;
        this.outputLogDirPath = outputLogDirPath;
        this.sampleIntervalMillis = sampleIntervalMillis;
        this.samplesCount = samplesCount;
        this.processName = processName;
        this.cmdLineName = cmdLineName;
    }

    public String getJstatLogFileName() {
        return jstatLogFileName;
    }

    public void setJstatLogFileName(String jstatLogFileName) {
        this.jstatLogFileName = jstatLogFileName;
    }

    public String getCmdLineName() {
        return cmdLineName;
    }

    public void setCmdLineName(String cmdLineName) {
        this.cmdLineName = cmdLineName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getOutputLogDirPath() {
        return outputLogDirPath;
    }

    public void setOutputLogDirPath(String outputLogDirPath) {
        this.outputLogDirPath = outputLogDirPath;
    }

    public Integer getSampleIntervalMillis() {
        return sampleIntervalMillis;
    }

    public void setSampleIntervalMillis(Integer sampleIntervalMillis) {
        this.sampleIntervalMillis = sampleIntervalMillis;
    }

    public Integer getSamplesCount() {
        return samplesCount;
    }

    public void setSamplesCount(Integer samplesCount) {
        this.samplesCount = samplesCount;
    }
}
