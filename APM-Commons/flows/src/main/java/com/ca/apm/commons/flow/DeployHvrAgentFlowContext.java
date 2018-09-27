package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Created by nick on 1.10.14.
 */
public class DeployHvrAgentFlowContext implements IFlowContext {

    @NotNull
    private final String stagingDir;

    @NotNull
    private final String installDir;

    @NotNull
    private final URL hvrAgentUrl;

    private DeployHvrAgentFlowContext(final Builder builder) {
        stagingDir = builder.stagingDir;
        installDir = builder.installDir;
        hvrAgentUrl = builder.hvrAgentUrl;
    }

    public String getStagingDir() {
        return stagingDir;
    }

    public String getInstallDir() {
        return installDir;
    }

    public URL getHvrAgentUrl() {
        return hvrAgentUrl;
    }

    public static class Builder implements IBuilder<DeployHvrAgentFlowContext> {

        @NotNull
        private String stagingDir;

        @NotNull
        private String installDir;

        @NotNull
        private URL hvrAgentUrl;

        public Builder stagingDir(String value) {
            stagingDir = value;
            return this;
        }

        public Builder installDir(String value) {
            installDir = value;
            return this;
        }

        public Builder hvrAgentUrl(URL value) {
            hvrAgentUrl = value;
            return this;
        }

        @Override
        public DeployHvrAgentFlowContext build() {

            DeployHvrAgentFlowContext flowContext = new DeployHvrAgentFlowContext(this);

            notNull(flowContext.getStagingDir(), "The staging directory cannot be null.");
            notNull(flowContext.getInstallDir(), "The install directory cannot be null.");
            notNull(flowContext.getHvrAgentUrl(), "The hvrAgent URL cannot be null.");

            return flowContext;
        }
    }
}
