package com.ca.apm.systemtest.fld.util.networktrafficmonitor;

import java.io.File;

public class Configuration {

    private String workDir;
    private long waitInterval;
    private File dataDir;
    private File chartsDir;
    private File summaryDir;

    private String chartTitle;
    private String chartXAxisLabel;
    private String chartYAxisLabel;
    private int chartWidth;
    private int chartHeight;

    private String inputDataFilePrefix;
    private String inputDataFileSufix;

    private int durationFromStart_index;
    private int bytesPerInterval_in_index;
    private int bytesPerInterval_out_index;
    private int totalBytes_in_index;
    private int totalBytes_out_index;

    private String thisHost; // i.e. BOCTO01/130.119.141.86
    private String thisHostName; // i.e. BOCTO01
    private String networkTrafficMonitorWebappHost;
    private int networkTrafficMonitorWebappPort;
    private String networkTrafficMonitorWebappContextRoot;
    private String networkTrafficMonitorWebappUploadImageUrl;

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public long getWaitInterval() {
        return waitInterval;
    }

    public void setWaitInterval(long waitInterval) {
        this.waitInterval = waitInterval;
    }

    public File getDataDir() {
        return dataDir;
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    public File getChartsDir() {
        return chartsDir;
    }

    public void setChartsDir(File chartsDir) {
        this.chartsDir = chartsDir;
    }

    public File getSummaryDir() {
        return summaryDir;
    }

    public void setSummaryDir(File summaryDir) {
        this.summaryDir = summaryDir;
    }

    public String getChartTitle() {
        return chartTitle;
    }

    public void setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
    }

    public String getChartXAxisLabel() {
        return chartXAxisLabel;
    }

    public void setChartXAxisLabel(String chartXAxisLabel) {
        this.chartXAxisLabel = chartXAxisLabel;
    }

    public String getChartYAxisLabel() {
        return chartYAxisLabel;
    }

    public void setChartYAxisLabel(String chartYAxisLabel) {
        this.chartYAxisLabel = chartYAxisLabel;
    }

    public int getChartWidth() {
        return chartWidth;
    }

    public void setChartWidth(int chartWidth) {
        this.chartWidth = chartWidth;
    }

    public int getChartHeight() {
        return chartHeight;
    }

    public void setChartHeight(int chartHeight) {
        this.chartHeight = chartHeight;
    }

    public String getInputDataFilePrefix() {
        return inputDataFilePrefix;
    }

    public void setInputDataFilePrefix(String inputDataFilePrefix) {
        this.inputDataFilePrefix = inputDataFilePrefix;
    }

    public String getInputDataFileSufix() {
        return inputDataFileSufix;
    }

    public void setInputDataFileSufix(String inputDataFileSufix) {
        this.inputDataFileSufix = inputDataFileSufix;
    }

    public int getDurationFromStart_index() {
        return durationFromStart_index;
    }

    public void setDurationFromStart_index(int durationFromStart_index) {
        this.durationFromStart_index = durationFromStart_index;
    }

    public int getBytesPerInterval_in_index() {
        return bytesPerInterval_in_index;
    }

    public void setBytesPerInterval_in_index(int bytesPerInterval_in_index) {
        this.bytesPerInterval_in_index = bytesPerInterval_in_index;
    }

    public int getBytesPerInterval_out_index() {
        return bytesPerInterval_out_index;
    }

    public void setBytesPerInterval_out_index(int bytesPerInterval_out_index) {
        this.bytesPerInterval_out_index = bytesPerInterval_out_index;
    }

    public int getTotalBytes_in_index() {
        return totalBytes_in_index;
    }

    public void setTotalBytes_in_index(int totalBytes_in_index) {
        this.totalBytes_in_index = totalBytes_in_index;
    }

    public int getTotalBytes_out_index() {
        return totalBytes_out_index;
    }

    public void setTotalBytes_out_index(int totalBytes_out_index) {
        this.totalBytes_out_index = totalBytes_out_index;
    }

    public String getThisHost() {
        return thisHost;
    }

    public void setThisHost(String thisHost) {
        this.thisHost = thisHost;
    }

    public String getThisHostName() {
        return thisHostName;
    }

    public void setThisHostName(String thisHostName) {
        this.thisHostName = thisHostName;
    }

    public String getNetworkTrafficMonitorWebappHost() {
        return networkTrafficMonitorWebappHost;
    }

    public void setNetworkTrafficMonitorWebappHost(String networkTrafficMonitorWebappHost) {
        this.networkTrafficMonitorWebappHost = networkTrafficMonitorWebappHost;
    }

    public int getNetworkTrafficMonitorWebappPort() {
        return networkTrafficMonitorWebappPort;
    }

    public void setNetworkTrafficMonitorWebappPort(int networkTrafficMonitorWebappPort) {
        this.networkTrafficMonitorWebappPort = networkTrafficMonitorWebappPort;
    }

    public String getNetworkTrafficMonitorWebappContextRoot() {
        return networkTrafficMonitorWebappContextRoot;
    }

    public void setNetworkTrafficMonitorWebappContextRoot(
        String networkTrafficMonitorWebappContextRoot) {
        this.networkTrafficMonitorWebappContextRoot = networkTrafficMonitorWebappContextRoot;
    }

    public String getNetworkTrafficMonitorWebappUploadImageUrl() {
        return networkTrafficMonitorWebappUploadImageUrl;
    }

    public void setNetworkTrafficMonitorWebappUploadImageUrl(
        String networkTrafficMonitorWebappUploadImageUrl) {
        this.networkTrafficMonitorWebappUploadImageUrl = networkTrafficMonitorWebappUploadImageUrl;
    }

}
