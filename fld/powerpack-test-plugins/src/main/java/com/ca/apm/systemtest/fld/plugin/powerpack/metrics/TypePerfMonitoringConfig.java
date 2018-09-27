package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

/**
 * Configuration object representing settings for typeperf tool.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class TypePerfMonitoringConfig implements PerfMonitoringConfig {

    private Integer sampleIntervalMillis;
    private Integer samplesCount;
    private String cmdLineArguments;
    private String outputLogDirPath;
    private String typePerfLogFileName;
    private String processNamePattern;
    private String javaProcessNamePattern;
    private String procInstanceName;
    private String performanceCounters;
    
    /**
     * Default constructor.
     */
    public TypePerfMonitoringConfig() {
    }

    /**
     * Constructor.
     * 
     * @param sampleIntervalMillis
     * @param samplesCount
     * @param cmdLineArguments
     * @param outputLogDirPath
     * @param typePerfLogFileName
     * @param procNamePattern
     * @param javaProcNamePattern
     * @param procInstName
     * @param performanceCounters
     */
    public TypePerfMonitoringConfig(Integer sampleIntervalMillis, Integer samplesCount, 
                                    String cmdLineArguments, String outputLogDirPath, 
                                    String typePerfLogFileName, String procNamePattern, 
                                    String javaProcNamePattern, String procInstName, 
                                    String performanceCounters) {
        this.sampleIntervalMillis = sampleIntervalMillis;
        this.samplesCount = samplesCount;
        this.cmdLineArguments = cmdLineArguments;
        this.outputLogDirPath = outputLogDirPath;
        this.typePerfLogFileName = typePerfLogFileName;
        this.processNamePattern = procNamePattern;
        this.javaProcessNamePattern = javaProcNamePattern;
        this.procInstanceName = procInstName;
        this.performanceCounters = performanceCounters; 
    }

    /**
     * @return the samplesCount
     */
    public Integer getSamplesCount() {
        return samplesCount;
    }

    /**
     * @return the javaProcessNamePattern
     */
    public String getJavaProcessNamePattern() {
        return javaProcessNamePattern;
    }

    /**
     * 
     * @param javaProcNamePattern the javaProcessNamePattern to set
     */
    public void setJavaProcessNamePattern(String javaProcNamePattern) {
        this.javaProcessNamePattern = javaProcNamePattern;
    }

    /**
     * @return the sampleIntervalMillis
     */
    public Integer getSampleIntervalMillis() {
        return sampleIntervalMillis;
    }

    /**
     * @param sampleIntervalMillis the sampleIntervalMillis to set
     */
    public void setSampleIntervalMillis(Integer testDuration) {
        this.sampleIntervalMillis = testDuration;
    }

    /**
     * @return the cmdLineArguments
     */
    public String getCmdLineArguments() {
        return cmdLineArguments;
    }

    /**
     * @param cmdLineArguments the cmdLineArguments to set
     */
    public void setCmdLineArguments(String cmdLineArguments) {
        this.cmdLineArguments = cmdLineArguments;
    }

    /**
     * @return the outputLogDirPath
     */
    public String getOutputLogDirPath() {
        return outputLogDirPath;
    }

    /**
     * @param outputLogDirPath the outputLogDirPath to set
     */
    public void setOutputLogDirPath(String outputLogDirPath) {
        this.outputLogDirPath = outputLogDirPath;
    }

    /**
     * @return the typePerfLogFileName
     */
    public String getTypePerfLogFileName() {
        return typePerfLogFileName;
    }

    /**
     * @param typePerfLogFileName the typePerfLogFileName to set
     */
    public void setTypePerfLogFileName(String typePerfLogFileName) {
        this.typePerfLogFileName = typePerfLogFileName;
    }

    /**
     * @return the processNamePattern
     */
    public String getProcessNamePattern() {
        return processNamePattern;
    }

    /**
     * @param processNamePattern the processNamePattern to set
     */
    public void setProcessNamePattern(String processName) {
        this.processNamePattern = processName;
    }

    /**
     * @return the procInstanceName
     */
    public String getProcInstanceName() {
        return procInstanceName;
    }

    /**
     * @param procInstanceName the procInstanceName to set
     */
    public void setProcInstanceName(String procInstanceName) {
        this.procInstanceName = procInstanceName;
    }

    /**
     * @param samplesCount the samplesCount to set
     */
    public void setSamplesCount(Integer samplesCount) {
        this.samplesCount = samplesCount;
    }

    /**
     * @return the performanceCounters
     */
    public String getPerformanceCounters() {
        return performanceCounters;
    }

    /**
     * @param performanceCounters the performanceCounters to set
     */
    public void setPerformanceCounters(String performanceCounters) {
        this.performanceCounters = performanceCounters;
    }

}
