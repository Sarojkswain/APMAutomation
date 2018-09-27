package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

/**
 * Performance configuration implementor for JMX monitoring.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class JmxMonitoringConfig implements PerfMonitoringConfig {

    private String outputLogDirPath;
    private String jmxMetrics;
    private String jmxOutputFileName;
    private String jmxConnectionHost;
    private Integer jmxConnectionPort;
    private Integer sampleIntervalMillis;
    private Integer samplesCount;
    
    
    /**
     * Default constructor.
     */
    public JmxMonitoringConfig() {
    }
    
    /**
     * 
     * @param outputLogDirPath
     * @param jmxMetrics
     * @param jmxConnectionHost
     * @param jmxConnectionPort
     * @param sampleIntervalMillis
     * @param samplesCount
     */
    public JmxMonitoringConfig(String outputLogDirPath, String jmxMetrics,
        String jmxConnectionHost, String jmxOutputFileName, Integer jmxConnectionPort, Integer sampleIntervalMillis,
        Integer samplesCount) {

        this.outputLogDirPath = outputLogDirPath;
        this.jmxMetrics = jmxMetrics;
        this.jmxConnectionHost = jmxConnectionHost;
        this.jmxConnectionPort = jmxConnectionPort;
        this.sampleIntervalMillis = sampleIntervalMillis;
        this.samplesCount = samplesCount;
        this.jmxOutputFileName = jmxOutputFileName;
    }

    /**
     * @return log directory path
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
     * @return the jmxMetrics
     */
    public String getJmxMetrics() {
        return jmxMetrics;
    }

    /**
     * @param jmxMetrics the jmxMetrics to set
     */
    public void setJmxMetrics(String jmxMetrics) {
        this.jmxMetrics = jmxMetrics;
    }

    /**
     * @return the jmxConnectionHost
     */
    public String getJmxConnectionHost() {
        return jmxConnectionHost;
    }

    /**
     * @param jmxConnectionHost the jmxConnectionHost to set
     */
    public void setJmxConnectionHost(String jmxConnectionHost) {
        this.jmxConnectionHost = jmxConnectionHost;
    }

    /**
     * @return the jmxConnectionPort
     */
    public Integer getJmxConnectionPort() {
        return jmxConnectionPort;
    }

    /**
     * @param jmxConnectionPort the jmxConnectionPort to set
     */
    public void setJmxConnectionPort(Integer jmxConnectionPort) {
        this.jmxConnectionPort = jmxConnectionPort;
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
    public void setSampleIntervalMillis(Integer metricCollectionInterval) {
        this.sampleIntervalMillis = metricCollectionInterval;
    }

    /**
     * @return the samplesCount
     */
    public Integer getSamplesCount() {
        return samplesCount;
    }

    /**
     * @param samplesCount the samplesCount to set
     */
    public void setSamplesCount(Integer samplesCount) {
        this.samplesCount = samplesCount;
    }

    /**
     * @return the jmxOutputFileName
     */
    public String getJmxOutputFileName() {
        return jmxOutputFileName;
    }

    /**
     * @param jmxOutputFileName the jmxOutputFileName to set
     */
    public void setJmxOutputFileName(String jmxOutputFileName) {
        this.jmxOutputFileName = jmxOutputFileName;
    }
    
}
