package com.ca.apm.systemtest.alertstateload.flow;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

public class DeployFileFlowContext implements IFlowContext {

    private final String srcFile;
    private final String[] dstFilePath;

    protected DeployFileFlowContext(Builder builder) {
        this.srcFile = builder.srcFile;
        this.dstFilePath = builder.dstFilePath;
    }

    public String getSrcFile() {
        return srcFile;
    }

    public String[] getDstFilePath() {
        return dstFilePath;
    }

    public static class Builder extends BuilderBase<Builder, DeployFileFlowContext> {
        private String srcFile;
        private String[] dstFilePath;

        @Override
        public DeployFileFlowContext build() {
            DeployFileFlowContext ctx = getInstance();
            Args.notBlank(srcFile, "srcFile");
            Args.notNull(dstFilePath, "dstFilePath");
            Args.check(dstFilePath.length > 0, "dstFilePath.length");
            return ctx;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected DeployFileFlowContext getInstance() {
            return new DeployFileFlowContext(this);
        }

        public Builder srcFile(String srcFile) {
            Args.notBlank(srcFile, "srcFile");
            this.srcFile = srcFile;
            return builder();
        }

        public Builder dstFilePath(String[] dstFilePath) {
            Args.notNull(dstFilePath, "dstFilePath");
            Args.check(dstFilePath.length > 0, "dstFilePath.length");
            this.dstFilePath = dstFilePath;
            return builder();
        }

        public Builder dstFilePath(String dstFilePath) {
            return dstFilePath(new String[] {dstFilePath});
        }
    }

}
