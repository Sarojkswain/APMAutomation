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
import com.ca.apm.tests.artifact.JmxMonitorVersion;
import com.ca.apm.tests.flow.JmxMonitorFlowContext;
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
public class JmxMonitorRole extends AbstractRole {

    public static final String RUN_JMX_MONITOR = "runJmxMonitor";

    private final JmxMonitorFlowContext runFlowContext;

    private final GenericFlowContext installContext;

    protected JmxMonitorRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        installContext = builder.installContext;
        runFlowContext = builder.runFlowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        runFlow(client, GenericFlow.class, installContext);
    }

    public static class Builder extends BuilderBase<Builder, JmxMonitorRole> {
        protected ITasResolver tasResolver;
        protected String roleId;

        @Nullable
        protected GenericFlowContext installContext;
        protected JmxMonitorFlowContext.Builder runFlowContextBuilder;
        protected JmxMonitorFlowContext runFlowContext;

        protected String installPath;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            installPath(getWinDeployBase());
            this.initFlowContext();
        }

        protected void initFlowContext() {
            runFlowContextBuilder = new JmxMonitorFlowContext.Builder();
        }

        @Override
        public JmxMonitorRole build() {

            JmxMonitorVersion artifact = new JmxMonitorVersion(tasResolver);
            URL artifactUrl = tasResolver.getArtifactUrl(artifact.createArtifact());

            installContext =
                    new GenericFlowContext.Builder().artifactUrl(artifactUrl)
                            .destination(installPath).targetFilename(artifact.getFilename()).notArchive().build();


            runFlowContext = runFlowContextBuilder
                    .jmxMonitorJarPath(
                            FileUtils.getFile(this.installContext.getDestination(), artifact.getFilename()).getAbsolutePath()
                    ).build();
            getEnvProperties().add(RUN_JMX_MONITOR, runFlowContext);

            return getInstance();
        }

        @Override
        protected JmxMonitorRole getInstance() {
            return new JmxMonitorRole(this);
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

        public Builder host(String host) {
            this.runFlowContextBuilder.host(host);
            return this.builder();
        }

        public Builder port(Integer port) {
            this.runFlowContextBuilder.port(port);
            return this.builder();
        }

        public Builder javaHome(String javaHome) {
            this.runFlowContextBuilder.javaHome(javaHome);
            return this.builder();
        }

        public Builder jmxMonitorJarPath(String jmxMonitorJarPath) {
            this.runFlowContextBuilder.jmxMonitorJarPath(jmxMonitorJarPath);
            return this.builder();
        }

        public Builder runTime(Long runTime) {
            this.runFlowContextBuilder.runTime(runTime);
            return this.builder();
        }

        public Builder jmxCollectionString(String collectionString) {
            this.runFlowContextBuilder.jmxCollectionString(collectionString);
            return this.builder();
        }

        public Builder outputFileName(String outputFileName) {
            this.runFlowContextBuilder.outputFileName(outputFileName);
            return this.builder();
        }

        public Builder copyResultsDestinationDir(String copyResultsDestinationDir) {
            this.runFlowContextBuilder.copyResultsDestinationDir(copyResultsDestinationDir);
            return this.builder();
        }

        public Builder copyResultsDestinationFileName(String copyResultsDestinationFileName) {
            this.runFlowContextBuilder.copyResultsDestinationFileName(copyResultsDestinationFileName);
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
