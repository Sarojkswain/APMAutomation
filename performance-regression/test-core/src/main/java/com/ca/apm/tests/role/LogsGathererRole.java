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

import com.ca.apm.tests.flow.LogsGathererFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

import java.util.Map;

/**
 * @author meler02
 */
public class LogsGathererRole extends AbstractRole {

    public static final String RUN_LOGS_GATHERER = "runLogsGatherer";

    private final LogsGathererFlowContext runFlowContext;


    /**
     * @param builder Builder object containing all necessary data
     */
    protected LogsGathererRole(LogsGathererRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        runFlowContext = builder.runFlowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

    }

    public static class Builder extends BuilderBase<LogsGathererRole.Builder, LogsGathererRole> {

        private final String roleId;
        private final ITasResolver tasResolver;

        protected LogsGathererFlowContext.Builder runFlowContextBuilder;
        protected LogsGathererFlowContext runFlowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.initFlowContext();
        }

        protected void initFlowContext() {
            runFlowContextBuilder = new LogsGathererFlowContext.Builder();
        }

        public LogsGathererRole build() {
            initRunFlow();

            LogsGathererRole role = this.getInstance();
            return role;
        }

        protected LogsGathererRole getInstance() {
            return new LogsGathererRole(this);
        }

        protected void initRunFlow() {
            runFlowContext = runFlowContextBuilder.build();
            getEnvProperties().add(RUN_LOGS_GATHERER, runFlowContext);
        }

        public Builder sourceDir(String sourceDir) {
            this.runFlowContextBuilder.sourceDir(sourceDir);
            return this.builder();
        }

        public Builder targetDir(String targetDir) {
            this.runFlowContextBuilder.targetDir(targetDir);
            return this.builder();
        }

        public Builder targetZipFile(String targetFileName) {
            this.runFlowContextBuilder.targetZipFile(targetFileName);
            return this.builder();
        }

        public Builder deleteSource(Boolean deleteSource) {
            this.runFlowContextBuilder.deleteSource(deleteSource);
            return this.builder();
        }

        public Builder addTimestamp(Boolean addTimestamp) {
            this.runFlowContextBuilder.addTimestamp(addTimestamp);
            return this.builder();
        }

        public Builder ignoreDeletionErrors(Boolean ignoreDeletionErrors) {
            this.runFlowContextBuilder.ignoreDeletionErrors(ignoreDeletionErrors);
            return this.builder();
        }

        public Builder ignoreEmpty(Boolean ignoreEmpty) {
            this.runFlowContextBuilder.ignoreEmpty(ignoreEmpty);
            return this.builder();
        }

        public Builder filesMapping(Map<String, String> filesMapping) {
            this.runFlowContextBuilder.filesMapping(filesMapping);
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

        protected Builder builder() {
            return this;
        }
    }


}