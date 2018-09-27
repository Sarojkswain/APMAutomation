package com.ca.apm.systemtest.alertstateload.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

public class AlertStateLoadMMFlowContext implements IFlowContext {

    private String srcFile;
    private String[] dstFilePath;
    private String workDir;

    protected AlertStateLoadMMFlowContext(Builder builder) {
        this.srcFile = builder.srcFile;
        this.dstFilePath = builder.dstFilePath;
        this.workDir = builder.workDir;
    }

    public String getSrcFile() {
        return srcFile;
    }

    public String[] getDstFilePath() {
        return dstFilePath;
    }

    public String getWorkDir() {
        return workDir;
    }

    public static class LinuxBuilder extends Builder {
        public static final String DEFAULT_EM_INSTALL_DIR_linux = "/opt/automation/deployed/em";
        public static final String DEFAULT_WORK_DIR_linux = "/opt/mm";

        public LinuxBuilder() {
            emInstallDir = DEFAULT_EM_INSTALL_DIR_linux;
            workDir = DEFAULT_WORK_DIR_linux;
        }
    }

    public static class Builder extends BuilderBase<Builder, AlertStateLoadMMFlowContext> {
        public static final String DEFAULT_SRC_FILE = "/mm/ManagementModule.xml";
        public static final String DEFAULT_EM_INSTALL_DIR_windows = "C:\\automation\\deployed\\em";
        public static final String[] DEFAULT_DST_FILE_PATH = new String[] {"config", "modules",
                "AlertStateLoadMM.jar"};
        public static final String DEFAULT_WORK_DIR_windows = "c:\\sw\\mm\\";

        private String srcFile = DEFAULT_SRC_FILE;
        protected String emInstallDir = DEFAULT_EM_INSTALL_DIR_windows;
        private String[] dstFilePath = DEFAULT_DST_FILE_PATH;
        protected String workDir = DEFAULT_WORK_DIR_windows;

        @Override
        public AlertStateLoadMMFlowContext build() {
            AlertStateLoadMMFlowContext ctx = getInstance();
            Args.notNull(srcFile, "srcFile");
            Args.notNull(emInstallDir, "emInstallDir");
            Args.check(dstFilePath.length > 0, "dstFilePath.length");

            Collection<String> c = new ArrayList<>(Collections.singleton(emInstallDir));
            c.addAll(Arrays.asList(dstFilePath));
            dstFilePath = c.toArray(new String[0]);

            return ctx;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected AlertStateLoadMMFlowContext getInstance() {
            return new AlertStateLoadMMFlowContext(this);
        }

        // public Builder srcFile(String srcFile) {
        // Args.notBlank(srcFile, "srcFile");
        // this.srcFile = srcFile;
        // return builder();
        // }

        public Builder emInstallDir(String emInstallDir) {
            Args.notBlank(emInstallDir, "emInstallDir");
            this.emInstallDir = emInstallDir;
            return builder();
        }

        // public Builder dstFilePath(String[] dstFilePath) {
        // Args.notNull(dstFilePath, "dstFilePath");
        // Args.check(dstFilePath.length > 0, "dstFilePath.length");
        // this.dstFilePath = dstFilePath;
        // return builder();
        // }

        // public Builder dstFilePath(String dstFilePath) {
        // return dstFilePath(new String[] {dstFilePath});
        // }
    }

}
