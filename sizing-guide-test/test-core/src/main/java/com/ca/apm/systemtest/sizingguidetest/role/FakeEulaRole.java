package com.ca.apm.systemtest.sizingguidetest.role;

import java.util.Arrays;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.sizingguidetest.flow.DeployFileFlow;
import com.ca.apm.systemtest.sizingguidetest.flow.DeployFileFlowContext;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class FakeEulaRole extends AbstractRole {

    private String targetDir;
    private String targetFile;

    protected FakeEulaRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.targetDir = builder.targetDir;
        this.targetFile = builder.targetFile;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        RunCommandFlowContext runCommandFlowContext =
            (new RunCommandFlowContext.Builder("mkdir"))
                .args(Arrays.asList(new String[] {"-p", targetDir})).doNotPrependWorkingDirectory()
                .build();
        runCommandFlow(aaClient, runCommandFlowContext);

        DeployFileFlowContext deployFileFlowContext =
            (new DeployFileFlowContext.Builder()).srcFile("/fake-eula/eula-10.5.1.8.zip")
                .dstFilePath(new String[] {targetDir, targetFile}).build();
        runFlow(aaClient, DeployFileFlow.class, deployFileFlowContext);
    }

    public String getTargetDir() {
        return targetDir;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public static Artifact getFakeEulaArtifact() {
        return (new TasArtifact.Builder("eula")).groupId("com.ca.apm.delivery").version("10.5.1.8")
            .extension("zip").build().getArtifact();
    }

    public static class LinuxBuilder extends Builder {
        public static final String DEFAULT_LINUX_TARGET_DIR = "/opt/automation/tmp/eula/";
        public static final String DEFAULT_LINUX_EULA_URL =
            "file:///opt/automation/tmp/eula/eula-10.5.1.8.zip";

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            targetDir = DEFAULT_LINUX_TARGET_DIR;
        }

    }

    public static class Builder extends BuilderBase<Builder, FakeEulaRole> {
        public static final String DEFAULT_TARGET_FILE = "eula-10.5.1.8.zip";
        public static final String DEFAULT_WINDOWS_TARGET_DIR = "c:\\automation\\tmp\\eula\\";
        public static final String DEFAULT_WINDOWS_EULA_URL =
            "file:///c:/automation/tmp/eula/eula-10.5.1.8.zip";

        private String roleId;
        protected String targetDir = DEFAULT_WINDOWS_TARGET_DIR;
        protected String targetFile = DEFAULT_TARGET_FILE;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
        }

        @Override
        public FakeEulaRole build() {
            Args.notNull(targetDir, "targetDir");
            Args.notNull(targetFile, "targetFile");
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected FakeEulaRole getInstance() {
            FakeEulaRole role = new FakeEulaRole(this);
            return role;
        }

        public Builder targetDir(String targetDir) {
            Args.notNull(targetDir, "targetDir");
            this.targetDir = targetDir;
            return builder();
        }

        public Builder targetFile(String targetFile) {
            Args.notNull(targetFile, "targetFile");
            this.targetFile = targetFile;
            return builder();
        }
    }

}
