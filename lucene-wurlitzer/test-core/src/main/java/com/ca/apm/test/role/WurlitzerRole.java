/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.test.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.apm.test.artifact.AntVersion;
import com.ca.apm.test.artifact.WurlitzerArtifact;
import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * WurlitzerRole class.
 *
 * Deploys Wurlitzer and starts a script according to the given parameters.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public class WurlitzerRole extends AbstractRole {

    private static final int UNPACK_TIMEOUT = 60;

    @Nullable
    private final GenericFlowContext antFlowContext;

    private final GenericFlowContext wurlitzerFlowContext;

    private final RunCommandFlowContext runCommandFlowContext;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected WurlitzerRole(AbstractBuilder<?> builder) {
        super(builder.roleId, builder.getEnvProperties());

        antFlowContext = builder.antFlowContext;
        wurlitzerFlowContext = builder.wurlitzerFlowContext;
        runCommandFlowContext = builder.runCommandFlowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (antFlowContext != null) {
            aaClient.runJavaFlow(new FlowConfigBuilder(GenericFlow.class, antFlowContext,
                getHostWithPort()).timeout(UNPACK_TIMEOUT));
        }

        aaClient.runJavaFlow(new FlowConfigBuilder(GenericFlow.class, wurlitzerFlowContext,
            getHostWithPort()).timeout(UNPACK_TIMEOUT));

        aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, runCommandFlowContext,
            getHostWithPort()));
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate
     * {@link WurlitzerRole}
     */
    public static class LinuxBuilder extends AbstractBuilder<LinuxBuilder> {

        @Nullable
        protected GenericFlowContext.Builder antFlowContextBuilder;

        protected AntVersion antVersionToDeploy;

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected void initFlowContextBuilders() {
            super.initFlowContextBuilders();
            antFlowContextBuilder = new GenericFlowContext.Builder();
        }

        public LinuxBuilder antVersionToDeploy(AntVersion antVersionToDeploy) {
            Args.notNull(antVersionToDeploy, "antVersionToDeploy");
            this.antVersionToDeploy = antVersionToDeploy;
            return builder();
        }

        @Override
        protected void initAntFlow() {
            URL artifactURL = tasResolver.getArtifactUrl(antVersionToDeploy);
            antFlowContext =
                antFlowContextBuilder.artifactUrl(artifactURL)
                    .destination(getDeployBase() + TasFileUtils.getBasename(artifactURL)).build();
        }

        @Override
        protected void initRunCommandFlowWithAntWorkDir() {
            runCommandFlowContextBuilder
                .workDir(concatPaths(antFlowContext.getDestination(), "bin"));
        }

        @Override
        protected LinuxBuilder builder() {
            return this;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link WurlitzerRole}
     */
    public static class Builder extends AbstractBuilder<Builder> {
        public Builder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    /**
     * Abstract superclass of builders responsible for holding all necessary properties
     * to instantiate {@link WurlitzerRole}
     */
    public static abstract class AbstractBuilder<B extends AbstractBuilder<B>>
        extends BuilderBase<B, WurlitzerRole> {

        private final String roleId;
        protected final ITasResolver tasResolver;

        protected GenericFlowContext.Builder wurlitzerFlowContextBuilder;
        protected RunCommandFlowContext.Builder runCommandFlowContextBuilder;

        @Nullable
        protected GenericFlowContext antFlowContext;
        @Nullable
        protected GenericFlowContext wurlitzerFlowContext;
        @Nullable
        protected RunCommandFlowContext runCommandFlowContext;
        @Nullable
        protected String wurlitzerVersion;
        @Nullable
        protected Artifact wurlitzerArtifact;
        @Nullable
        protected String[] wurlitzerAntScriptPathSegments;
        @Nullable
        protected String[] wurlitzerAntScriptArgs;
        protected String wurlitzerTerminateOnMatch = "";

        public AbstractBuilder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            initFlowContextBuilders();
        }

        protected void initFlowContextBuilders() {
            wurlitzerFlowContextBuilder = new GenericFlowContext.Builder();
            runCommandFlowContextBuilder = new RunCommandFlowContext.Builder("ant");
        }

        @Override
        public WurlitzerRole build() {
            initAntFlow();
            initWurlitzerArtifact();
            initWurlitzerFlow();
            initRunCommandFlow();

            return getInstance();
        }

        public B version(IArtifactVersion version) {
            Args.notNull(version, "version");
            this.wurlitzerVersion = version.toString();
            return builder();
        }

        public B version(String version) {
            Args.notNull(version, "version");
            this.wurlitzerVersion = version;
            return builder();
        }

        public B antScriptPathSegments(String... antScriptPathSegments) {
            Args.notNull(antScriptPathSegments, "antScriptPathSegments");
            this.wurlitzerAntScriptPathSegments = antScriptPathSegments;
            return builder();
        }

        public B antScriptArgs(String... antScriptArgs) {
            Args.notNull(antScriptArgs, "antScriptArgs");
            this.wurlitzerAntScriptArgs = antScriptArgs;
            return builder();
        }

        public B terminateOnMatch(String stringToMatch) {
            Args.notNull(stringToMatch, "stringToMatch");
            this.wurlitzerTerminateOnMatch = stringToMatch;
            return builder();
        }


        protected void initAntFlow() {
            // noop - for Linux only
        }

        protected void initWurlitzerArtifact() {
            if (wurlitzerArtifact != null) {
                return;
            }
            wurlitzerArtifact =
                new WurlitzerArtifact(tasResolver).createArtifact(wurlitzerVersion).getArtifact();
        }

        protected void initWurlitzerFlow() {
            URL artifactURL = tasResolver.getArtifactUrl(wurlitzerArtifact);
            wurlitzerFlowContext =
                wurlitzerFlowContextBuilder.artifactUrl(artifactURL)
                    .destination(getDeployBase() + TasFileUtils.getBasename(artifactURL)).build();
        }

        protected void initRunCommandFlow() {
            List<String> args = new ArrayList<String>();

            args.add("-f");
            String[] extendedPathSegments =
                (String[]) ArrayUtils.add(wurlitzerAntScriptPathSegments, 0,
                    wurlitzerFlowContext.getDestination());
            args.add(concatPaths(extendedPathSegments));

            if (wurlitzerAntScriptArgs != null) {
                args.addAll(Arrays.asList(wurlitzerAntScriptArgs));
            }

            initRunCommandFlowWithAntWorkDir();

            runCommandFlowContext =
                runCommandFlowContextBuilder.args(args).terminateOnMatch(wurlitzerTerminateOnMatch)
                    .build();
        }

        protected void initRunCommandFlowWithAntWorkDir() {
            // noop - for Linux only
        }

        @Override
        protected WurlitzerRole getInstance() {
            return new WurlitzerRole(this);
        }
    }
}
