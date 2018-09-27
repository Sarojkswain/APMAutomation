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

import com.ca.apm.tests.flow.TypeperfFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author meler02
 */
public class TypeperfRole extends AbstractRole {

    public static final String RUN_TYPEPERF = "runTypeperf";

    private final TypeperfFlowContext runFlowContext;


    /**
     * @param builder Builder object containing all necessary data
     */
    protected TypeperfRole(TypeperfRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        runFlowContext = builder.runFlowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

    }

    public static class Builder extends BuilderBase<TypeperfRole.Builder, TypeperfRole> {

        private final String roleId;
        private final ITasResolver tasResolver;

        protected TypeperfFlowContext.Builder runFlowContextBuilder;
        protected TypeperfFlowContext runFlowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.initFlowContext();
        }

        protected void initFlowContext() {
            runFlowContextBuilder = new TypeperfFlowContext.Builder();
        }

        public TypeperfRole build() {
            initRunFlow();

            TypeperfRole role = this.getInstance();
            return role;
        }

        protected TypeperfRole getInstance() {
            return new TypeperfRole(this);
        }

        protected void initRunFlow() {
            runFlowContext = runFlowContextBuilder.build();
            getEnvProperties().add(RUN_TYPEPERF, runFlowContext);
        }

        private TypeperfRole.Builder samplesInterval(long defaultTypperfSamples) {
            this.runFlowContextBuilder.samplesInterval(defaultTypperfSamples);
            return this.builder();
        }

        public TypeperfRole.Builder metrics(String[] metrics) {
            this.runFlowContextBuilder.metrics(metrics);
            return this.builder();
        }

        public TypeperfRole.Builder runTime(Long runTime) {
            this.runFlowContextBuilder.runTime(runTime);
            return this.builder();
        }

        public TypeperfRole.Builder outputFileName(String outputFileName) {
            this.runFlowContextBuilder.outputFileName(outputFileName);
            return this.builder();
        }

        public TypeperfRole.Builder copyResultsDestinationDir(String copyResultsDestinationDir) {
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

        public TypeperfRole.Builder copyResultsDestinationFileName(String copyResultsDestinationFileName) {
            this.runFlowContextBuilder.copyResultsDestinationFileName(copyResultsDestinationFileName);
            return this.builder();
        }

        protected TypeperfRole.Builder builder() {
            return this;
        }
    }


}