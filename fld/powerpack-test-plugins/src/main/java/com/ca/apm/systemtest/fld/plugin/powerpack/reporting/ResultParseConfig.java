package com.ca.apm.systemtest.fld.plugin.powerpack.reporting;

/**
 * Configuration object for PowerPack test result parser.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ResultParseConfig {

    private boolean buildTypePerfReport;
    private boolean buildJmxReport;
    private boolean buildJstatReport;
    private boolean buildJmeterReport;
    private String typePerfResultFileName;
    private String jmxResultFileName;
    private String jstatResultFileName;
    private String reportTemplateFileUrl;
    private String reportTemplateFilePath;
    private String resultsFolder;
    private String finalResultReportFileName;
    private Long monitoredProcessId;
    private boolean includeNewAgentWithPowerPackResults;
    private boolean includeNewAgentResults;
    private boolean includeOldAgentWithPowerPackResults;
    private boolean includeOldAgentResults;
    private boolean includeNoAgentResults;
    
    /**
     * Default constructor.
     */
    public ResultParseConfig() {
    }

    /**
     * Construct a new object. 
     * 
     * @param typePerfResultFile
     * @param jmxResultFile
     * @param reportTemplateFileUrl
     * @param reportTemplateFilePath
     * @param resultsFolder
     * @param monitoredProcessId
     */
    public ResultParseConfig(String typePerfResultFile, String jmxResultFile, String jstatResultFile,
                             String reportTemplateFileUrl, String reportTemplateFilePath, 
                             String resultsFolder, Long monitoredProcessId, String finalResultReportFileName, 
                             boolean buildTypePerfReport, 
                             boolean buildJmxReport, 
                             boolean buildJstatReport, 
                             boolean buildJmeterReport,
                             boolean includeNewAgentWithPowerPackResults,
                             boolean includeNewAgentResults, 
                             boolean includeOldAgentWithPowerPackResults,
                             boolean includeOldAgentResults, 
                             boolean includeNoAgentResults) {
        this.typePerfResultFileName = typePerfResultFile;
        this.jmxResultFileName = jmxResultFile;
        this.jstatResultFileName = jstatResultFile;
        this.reportTemplateFileUrl = reportTemplateFileUrl;
        this.reportTemplateFilePath = reportTemplateFilePath;
        this.resultsFolder = resultsFolder;
        this.monitoredProcessId = monitoredProcessId;
        this.buildTypePerfReport = buildTypePerfReport;
        this.buildJmxReport = buildJmxReport;
        this.buildJstatReport = buildJstatReport;
        this.buildJmeterReport = buildJmeterReport;
        this.finalResultReportFileName = finalResultReportFileName;
        this.includeNewAgentWithPowerPackResults = includeNewAgentWithPowerPackResults;
        this.includeNewAgentResults = includeNewAgentResults;
        this.includeOldAgentWithPowerPackResults = includeOldAgentWithPowerPackResults;
        this.includeOldAgentResults = includeOldAgentResults;
        this.includeNoAgentResults = includeNoAgentResults;
    }

    
    /**
     * @return the reportTemplateFilePath
     */
    public String getReportTemplateFilePath() {
        return reportTemplateFilePath;
    }

    /**
     * @param reportTemplateFilePath the reportTemplateFilePath to set
     */
    public void setReportTemplateFilePath(String reportTemplateFilePath) {
        this.reportTemplateFilePath = reportTemplateFilePath;
    }

    /**
     * @return the typePerfResultFileName
     */
    public String getTypePerfResultFileName() {
        return typePerfResultFileName;
    }
    
    /**
     * @param typePerfResultFileName the typePerfResultFile to set
     */
    public void setTypePerfResultFileName(String typePerfResultFileName) {
        this.typePerfResultFileName = typePerfResultFileName;
    }
    
    /**
     * @return the jmxResultFileName
     */
    public String getJmxResultFileName() {
        return jmxResultFileName;
    }
    
    /**
     * @param jmxResultFileName the jmxResultFile to set
     */
    public void setJmxResultFileName(String jmxResultFileName) {
        this.jmxResultFileName = jmxResultFileName;
    }
    
    /**
     * @return the reportTemplateFileUrl
     */
    public String getReportTemplateFileUrl() {
        return reportTemplateFileUrl;
    }
    
    /**
     * @param reportTemplateFileUrl the reportTemplateFileUrl to set
     */
    public void setReportTemplateFileUrl(String reportTemplateFileUrl) {
        this.reportTemplateFileUrl = reportTemplateFileUrl;
    }

    /**
     * @return the resultsFolder
     */
    public String getResultsFolder() {
        return resultsFolder;
    }

    /**
     * @param resultsFolder the resultsFolder to set
     */
    public void setResultsFolder(String resultsFolder) {
        this.resultsFolder = resultsFolder;
    }

    /**
     * @return the finalResultReportFileName
     */
    public String getFinalResultReportFileName() {
        return finalResultReportFileName;
    }

    /**
     * @param finalResultReportFileName the finalResultReportFileName to set
     */
    public void setFinalResultReportFileName(String finalResultReportFileName) {
        this.finalResultReportFileName = finalResultReportFileName;
    }

    /**
     * @return the monitoredProcessId
     */
    public Long getMonitoredProcessId() {
        return monitoredProcessId;
    }

    /**
     * @param monitoredProcessId the monitoredProcessId to set
     */
    public void setMonitoredProcessId(Long monitoredProcessId) {
        this.monitoredProcessId = monitoredProcessId;
    }

    /**
     * @return the buildTypePerfReport
     */
    public boolean isBuildTypePerfReport() {
        return buildTypePerfReport;
    }

    /**
     * @param buildTypePerfReport the buildTypePerfReport to set
     */
    public void setBuildTypePerfReport(boolean buildTypePerfReport) {
        this.buildTypePerfReport = buildTypePerfReport;
    }

    /**
     * @return the buildJmxReport
     */
    public boolean isBuildJmxReport() {
        return buildJmxReport;
    }

    /**
     * @param buildJmxReport the buildJmxReport to set
     */
    public void setBuildJmxReport(boolean buildJmxReport) {
        this.buildJmxReport = buildJmxReport;
    }

    /**
     * @return the buildJstatReport
     */
    public boolean isBuildJstatReport() {
        return buildJstatReport;
    }

    /**
     * @param buildJstatReport the buildJstatReport to set
     */
    public void setBuildJstatReport(boolean buildJstatReport) {
        this.buildJstatReport = buildJstatReport;
    }

    /**
     * @return the buildJmeterReport
     */
    public boolean isBuildJmeterReport() {
        return buildJmeterReport;
    }

    /**
     * @param buildJmeterReport the buildJmeterReport to set
     */
    public void setBuildJmeterReport(boolean buildJmeterReport) {
        this.buildJmeterReport = buildJmeterReport;
    }

    /**
     * @return the jstatResultFileName
     */
    public String getJstatResultFileName() {
        return jstatResultFileName;
    }

    /**
     * @param jstatResultFileName the jstatResultFileName to set
     */
    public void setJstatResultFileName(String jstatResultFileName) {
        this.jstatResultFileName = jstatResultFileName;
    }

    /**
     * @return the includeNewAgentWithPowerPackResults
     */
    public boolean isIncludeNewAgentWithPowerPackResults() {
        return includeNewAgentWithPowerPackResults;
    }

    /**
     * @param includeNewAgentWithPowerPackResults the includeNewAgentWithPowerPackResults to set
     */
    public void setIncludeNewAgentWithPowerPackResults(boolean includeNewAgentWithPowerPackResults) {
        this.includeNewAgentWithPowerPackResults = includeNewAgentWithPowerPackResults;
    }

    /**
     * @return the includeNewAgentResults
     */
    public boolean isIncludeNewAgentResults() {
        return includeNewAgentResults;
    }

    /**
     * @param includeNewAgentResults the includeNewAgentResults to set
     */
    public void setIncludeNewAgentResults(boolean includeNewAgentResults) {
        this.includeNewAgentResults = includeNewAgentResults;
    }

    /**
     * @return the includeOldAgentWithPowerPackResults
     */
    public boolean isIncludeOldAgentWithPowerPackResults() {
        return includeOldAgentWithPowerPackResults;
    }

    /**
     * @param includeOldAgentWithPowerPackResults the includeOldAgentWithPowerPackResults to set
     */
    public void setIncludeOldAgentWithPowerPackResults(boolean includeOldAgentWithPowerPackResults) {
        this.includeOldAgentWithPowerPackResults = includeOldAgentWithPowerPackResults;
    }

    /**
     * @return the includeOldAgentResults
     */
    public boolean isIncludeOldAgentResults() {
        return includeOldAgentResults;
    }

    /**
     * @param includeOldAgentResults the includeOldAgentResults to set
     */
    public void setIncludeOldAgentResults(boolean includeOldAgentResults) {
        this.includeOldAgentResults = includeOldAgentResults;
    }

    /**
     * @return the includeNoAgentResults
     */
    public boolean isIncludeNoAgentResults() {
        return includeNoAgentResults;
    }

    /**
     * @param includeNoAgentResults the includeNoAgentResults to set
     */
    public void setIncludeNoAgentResults(boolean includeNoAgentResults) {
        this.includeNoAgentResults = includeNoAgentResults;
    }
    
    
}
