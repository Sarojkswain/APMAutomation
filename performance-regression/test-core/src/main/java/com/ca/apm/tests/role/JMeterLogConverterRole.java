/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.artifact.JMeterLogConverterVersion;
import com.ca.apm.tests.flow.jMeter.JMeterLogConverterFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.commons.io.FileUtils;
import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class JMeterLogConverterRole extends AbstractRole {

    public static final String RUN_JMETER_LOG_CONVERTER = "runJmeterLogConverter";

    private final JMeterLogConverterFlowContext runFlowContext;

    private final GenericFlowContext installContext;

    protected JMeterLogConverterRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        installContext = builder.installContext;
        runFlowContext = builder.runFlowContext;
    }

    public String getJmeterLogConverterJarPath() {
        return runFlowContext.getJmeterLogConverterJarPath();
    }

    public String getOutputFileName() {
        return runFlowContext.getOutputFileName();
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        runFlow(client, GenericFlow.class, installContext);
    }

    public static class Builder extends BuilderBase<Builder, JMeterLogConverterRole> {
        protected ITasResolver tasResolver;
        protected String roleId;

        @Nullable
        protected GenericFlowContext installContext;
        protected JMeterLogConverterFlowContext.Builder runFlowContextBuilder;
        protected JMeterLogConverterFlowContext runFlowContext;

        protected String installPath;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            installPath(getWinDeployBase());
            this.initFlowContext();
        }

        protected void initFlowContext() {
            runFlowContextBuilder = new JMeterLogConverterFlowContext.Builder();
        }

        @Override
        public JMeterLogConverterRole build() {

            JMeterLogConverterVersion artifact = new JMeterLogConverterVersion(tasResolver);
            URL artifactUrl = tasResolver.getArtifactUrl(artifact.createArtifact());

            installContext =
                    new GenericFlowContext.Builder().artifactUrl(artifactUrl)
                            .destination(installPath).targetFilename(artifact.getFilename()).notArchive().build();


            runFlowContext = runFlowContextBuilder
                    .jmeterLogConverterJarPath(
                            FileUtils.getFile(this.installContext.getDestination(), artifact.getFilename()).getAbsolutePath()
                    ).build();
            getEnvProperties().add(RUN_JMETER_LOG_CONVERTER, runFlowContext);

            return getInstance();
        }

        @Override
        protected JMeterLogConverterRole getInstance() {
            return new JMeterLogConverterRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder installPath(String installPath) {
            Args.notBlank(installPath, "install dir");
            this.installPath = installPath;
            return builder();
        }

        public Builder outputFileName(String outputFileName) {
            this.runFlowContextBuilder.outputFileName(outputFileName);
            return this.builder();
        }

        public class LinuxBuilder extends Builder {
            public LinuxBuilder(String roleId, ITasResolver tasResolver) {
                super(roleId, tasResolver);
            }

            @Override
            protected String getPathSeparator() {
                return LINUX_SEPARATOR;
            }

            @Override
            protected LinuxBuilder builder() {
                return this;
            }

        }
    }
}
