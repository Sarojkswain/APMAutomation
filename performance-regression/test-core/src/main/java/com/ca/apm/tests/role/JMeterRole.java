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

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.artifact.JMeterVersion;
import com.ca.apm.tests.flow.jMeter.*;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;

import java.net.URL;
import java.util.Map;

/**
 * @author meler02
 */
public class JMeterRole extends AbstractRole {

    public static final String RUN_JMETER = "runJmeter";
    public static final String STOP_JMETER = "stopJmeter";
    public static final String CONFIGURE_JMETER = "configureJmeter";

    private final JMeterDeployFlowContext deployFlowContext;
    private final JMeterConfigureFlowContext configureFlowContext;
    private final JMeterRunFlowContext runFlowContext;

    private final boolean configureJmeter;
    private final boolean runJmeter;

    private final boolean undeployOnly;
    private final boolean predeployed;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected JMeterRole(JMeterRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        deployFlowContext = builder.deployFlowContext;
        configureFlowContext = builder.configureFlowContext;
        runFlowContext = builder.runFlowContext;

        configureJmeter = builder.configureJmeter;
        runJmeter = builder.runJmeter;

        undeployOnly = builder.undeployOnly;
        predeployed = builder.predeployed;
    }

    public boolean isUndeployOnly() {
        return undeployOnly;
    }

    public boolean isPredeployed() {
        return predeployed;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (!predeployed) {
            this.runFlow(aaClient, JMeterDeployFlow.class, this.deployFlowContext);
        }
        if (configureJmeter) {
            this.runFlow(aaClient, JMeterConfigureFlow.class, this.configureFlowContext);
        }
        if (runJmeter) {
            this.runFlow(aaClient, JMeterRunFlow.class, this.runFlowContext);
        }
    }

    public static class Builder extends BuilderBase<JMeterRole.Builder, JMeterRole> {

        private static final JMeterVersion DEFAULT_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;

        protected URL artifactUrl;
        protected String unpackDir;

        protected JMeterDeployFlowContext.Builder deployFlowContextBuilder;
        protected JMeterDeployFlowContext deployFlowContext;

        protected JMeterRunFlowContext.Builder runFlowContextBuilder;
        protected JMeterRunFlowContext runFlowContext;

        protected RunCommandFlowContext stopCommandFlowContext;

        protected JMeterConfigureFlowContext.Builder configureFlowContextBuilder;
        protected JMeterConfigureFlowContext configureFlowContext;

        protected boolean configureJmeter;
        protected boolean runJmeter;

        protected boolean undeployOnly;
        protected boolean predeployed;


        static {
            DEFAULT_ARTIFACT = JMeterVersion.VER_2_11;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.initFlowContext();

            this.version(DEFAULT_ARTIFACT);
        }

        protected void initFlowContext() {
            this.deployFlowContextBuilder = new JMeterDeployFlowContext.Builder();
            this.configureFlowContextBuilder = new JMeterConfigureFlowContext.Builder();
            this.runFlowContextBuilder = new JMeterRunFlowContext.Builder();

            this.configureJmeter = false;
            this.runJmeter = false;
        }

        public JMeterRole build() {
            this.initFlow();

            JMeterRole role = this.getInstance();
            Args.notNull(role.deployFlowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected JMeterRole getInstance() {
            return new JMeterRole(this);
        }

        protected void initFlow() {
            assert this.artifactUrl != null;

            this.deployFlowContextBuilder.deployPackageUrl(artifactUrl);
            this.deployFlowContext = this.deployFlowContextBuilder.build();

            configureFlowContext = configureFlowContextBuilder.build();
            getEnvProperties().add(CONFIGURE_JMETER, configureFlowContext);

            assert unpackDir != null;

            runFlowContext =
                    runFlowContextBuilder.jmeterPath(
                            deployFlowContext.getDeploySourcesLocation() + "/" + unpackDir).build();
            getEnvProperties().add(RUN_JMETER, runFlowContext);

            stopCommandFlowContext =
                    new RunCommandFlowContext.Builder("stoptest.cmd")
                            .workDir(runFlowContext.getJmeterPath() + "\\bin").name(roleId).build();
            getEnvProperties().add(STOP_JMETER, stopCommandFlowContext);
        }

        public JMeterRole.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deployFlowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            return this.builder();
        }

        public JMeterRole.Builder scriptFilePath(String scriptFilePath) {
            this.configureFlowContextBuilder.scriptFilePath(scriptFilePath);
            this.runFlowContextBuilder.scriptFilePath(scriptFilePath);
            return this.builder();
        }

        public JMeterRole.Builder outputJtlFile(String outputJtlFile) {
            this.runFlowContextBuilder.outputJtlFile(outputJtlFile);
            return this.builder();
        }

        public JMeterRole.Builder outputLogFile(String outputLogFile) {
            this.runFlowContextBuilder.outputLogFile(outputLogFile);
            return this.builder();
        }

        public Builder deleteOutputLogsAfterCopy() {
            this.runFlowContextBuilder.deleteOutputLogsAfterCopy(true);
            return this.builder();
        }

        public Builder deleteOutputLogsBeforeRun() {
            this.runFlowContextBuilder.deleteOutputLogsBeforeRun(true);
            return this.builder();
        }

        public JMeterRole.Builder params(Map<String, String> params) {
            this.configureFlowContextBuilder.params(params);
            this.runFlowContextBuilder.params(params);
            return this.builder();
        }

        public JMeterRole.Builder configureJmeter() {
            this.configureJmeter = true;
            return this.builder();
        }

        public JMeterRole.Builder runJmeter() {
            this.runJmeter = true;
            return this.builder();
        }

        public JMeterRole.Builder version(JMeterVersion version) {
            this.artifactUrl = version.getArtifactUrl(this.tasResolver.getRegionalArtifactory());
            this.unpackDir = version.getUnpackDir();
            return this.builder();
        }

        public JMeterRole.Builder copyResultsDestinationDir(String copyResultsDestinationDir) {
            this.runFlowContextBuilder.copyResultsDestinationDir(copyResultsDestinationDir);
            return this.builder();
        }

        public Builder copyResultsDestinationPassword(String copyResultsDestinationPassword) {
            this.runFlowContextBuilder.copyResultsDestinationPassword(copyResultsDestinationPassword);
            return this.builder();
        }

        public Builder copyResultsDestinationUser(String copyResultsDestinationUser) {
            this.runFlowContextBuilder.copyResultsDestinationUser(copyResultsDestinationUser);
            return this.builder();
        }

        public JMeterRole.Builder copyResultsDestinationJtlFileName(
                String copyResultsDestinationJtlFileName) {
            this.runFlowContextBuilder
                    .copyResultsDestinationJtlFileName(copyResultsDestinationJtlFileName);
            return this.builder();
        }

        public JMeterRole.Builder copyResultsDestinationLogFileName(
                String copyResultsDestinationLogFileName) {
            this.runFlowContextBuilder
                    .copyResultsDestinationLogFileName(copyResultsDestinationLogFileName);
            return this.builder();
        }

        public JMeterRole.Builder jmeterLogConverter(JMeterLogConverterRole jMeterLogConverterRole) {
            this.runFlowContextBuilder.jmeterLogConverterJarPath(jMeterLogConverterRole
                    .getJmeterLogConverterJarPath());
            this.runFlowContextBuilder.jmeterLogConverterOutputFileName(jMeterLogConverterRole
                    .getOutputFileName());
            return this.builder();
        }

        public Builder undeployOnly(boolean undeployOnly) {
            this.undeployOnly = undeployOnly;
            return this.builder();
        }

        public Builder undeployOnly() {
            this.undeployOnly = true;
            return this.builder();
        }

        public Builder predeployed(boolean predeployed) {
            this.predeployed = predeployed;
            return this.builder();
        }

        public Builder predeployed() {
            this.predeployed = true;
            return this.builder();
        }


        protected JMeterRole.Builder builder() {
            return this;
        }
    }


}
