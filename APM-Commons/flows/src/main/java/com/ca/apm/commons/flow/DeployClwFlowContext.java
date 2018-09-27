package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Created by nick on 15.10.14.
 */
public class DeployClwFlowContext implements IFlowContext {

    @NotNull
    private final String installDir;

    @NotNull
    private final URL clwUrl;

    private final String clwLocalFilename;

    private DeployClwFlowContext(final Builder builder) {
        installDir = builder.installDir;
        clwUrl = builder.clwUrl;
        clwLocalFilename = builder.clwLocalFilename;
    }

    public String getInstallDir() {
        return installDir;
    }

    public URL getClwUrl() {
        return clwUrl;
    }

    public String getClwLocalFilename() {
        return clwLocalFilename;
    }

    public static class Builder implements IBuilder<DeployClwFlowContext> {

        @NotNull
        private String installDir;

        @NotNull
        private URL clwUrl;

        private String clwLocalFilename;

        public Builder installDir(String value) {
            installDir = value;
            return this;
        }

        public Builder clwUrl(URL value) {
            clwUrl = value;
            return this;
        }

        public Builder clwLocalFilename(String value) {
            clwLocalFilename = value;
            return this;
        }

        @Override
        public DeployClwFlowContext build() {

            DeployClwFlowContext flowContext = new DeployClwFlowContext(this);

            notNull(flowContext.getInstallDir(), "The install directory cannot be null.");
            notNull(flowContext.getClwUrl(), "The clw URL cannot be null.");
            notNull(flowContext.getClwUrl(), "The clw local filename cannot be null.");

            return flowContext;
        }
    }
}
