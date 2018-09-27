package com.ca.apm.systemtest.sizingguidetest.flow;

import java.io.File;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

public class GenerateExcelReportFlowContext implements IFlowContext {

    private File resultsFile;
    private File templateFile;
    private File reportFile;
    private String sheetName;
    private boolean expectHeader;

    protected GenerateExcelReportFlowContext(Builder builder) {
        this.resultsFile = builder.resultsFile;
        this.templateFile = builder.templateFile;
        this.reportFile = builder.reportFile;
        this.sheetName = builder.sheetName;
        this.expectHeader = builder.expectHeader;
    }

    public File getResultsFile() {
        return resultsFile;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public File getReportFile() {
        return reportFile;
    }

    public String getSheetName() {
        return sheetName;
    }

    public boolean isExpectHeader() {
        return expectHeader;
    }

    @Override
    public String toString() {
        return "GenerateExcelReportFlowContext{" + "resultsFile=" + this.resultsFile
            + ", templateFile=" + this.templateFile + ", reportFile=" + this.reportFile
            + ", sheetName=" + this.sheetName + ", expectHeader=" + this.expectHeader + '}';
    }

    public static class Builder extends BuilderBase<Builder, GenerateExcelReportFlowContext> {
        private File resultsFile;
        private File templateFile;
        private File reportFile;
        private String sheetName;
        private boolean expectHeader = true;

        @Override
        public GenerateExcelReportFlowContext build() {
            GenerateExcelReportFlowContext ctx = getInstance();
            Args.notNull(resultsFile, "resultsFile");
            Args.notNull(templateFile, "templateFile");
            Args.notNull(reportFile, "reportFile");
            Args.notNull(sheetName, "sheetName");
            return ctx;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected GenerateExcelReportFlowContext getInstance() {
            return new GenerateExcelReportFlowContext(this);
        }

        public Builder resultsFile(String resultsFile) {
            Args.notBlank(resultsFile, "resultsFile");
            this.resultsFile = new File(resultsFile);
            return builder();
        }

        public Builder resultsFile(File resultsFile) {
            Args.notNull(resultsFile, "resultsFile");
            this.resultsFile = resultsFile;
            return builder();
        }

        public Builder templateFile(String templateFile) {
            Args.notBlank(templateFile, "templateFile");
            this.templateFile = new File(templateFile);
            return builder();
        }

        public Builder templateFile(File templateFile) {
            Args.notNull(templateFile, "templateFile");
            this.templateFile = templateFile;
            return builder();
        }

        public Builder reportFile(String reportFile) {
            Args.notBlank(reportFile, "reportFile");
            this.reportFile = new File(reportFile);
            return builder();
        }

        public Builder reportFile(File reportFile) {
            Args.notNull(reportFile, "reportFile");
            this.reportFile = reportFile;
            return builder();
        }

        public Builder sheetName(String sheetName) {
            Args.notBlank(sheetName, "sheetName");
            this.sheetName = sheetName;
            return builder();
        }

        public Builder expectHeader(boolean expectHeader) {
            this.expectHeader = expectHeader;
            return builder();
        }
    }

}
