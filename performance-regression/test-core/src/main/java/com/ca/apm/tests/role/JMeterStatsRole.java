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

import com.ca.apm.tests.flow.jMeter.JMeterStatsFlow;
import com.ca.apm.tests.flow.jMeter.JMeterStatsFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author meler02
 */
public class JMeterStatsRole extends AbstractRole {

    public static final String RUN_JMETER_STATS = "runJmeterStats";

    private final JMeterStatsFlowContext runFlowContext;

    private final boolean runJmeterStats;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected JMeterStatsRole(JMeterStatsRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        runFlowContext = builder.runFlowContext;

        runJmeterStats = builder.runJmeterStats;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (runJmeterStats) {
            this.runFlow(aaClient, JMeterStatsFlow.class, this.runFlowContext);
        }
    }

    public static class Builder extends BuilderBase<JMeterStatsRole.Builder, JMeterStatsRole> {

        private final String roleId;
        private final ITasResolver tasResolver;

        protected JMeterStatsFlowContext.Builder runFlowContextBuilder;
        protected JMeterStatsFlowContext runFlowContext;

        protected boolean runJmeterStats;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.initFlowContext();
        }

        protected void initFlowContext() {
            this.runFlowContextBuilder = new JMeterStatsFlowContext.Builder();

            this.runJmeterStats = false;
        }

        public JMeterStatsRole build() {
            this.initFlow();

            JMeterStatsRole role = this.getInstance();

            return role;
        }

        protected JMeterStatsRole getInstance() {
            return new JMeterStatsRole(this);
        }

        protected void initFlow() {

            runFlowContext = runFlowContextBuilder.build();
            getEnvProperties().add(RUN_JMETER_STATS, runFlowContext);
        }


        public JMeterStatsRole.Builder numThreads(Long numThreads) {
            this.runFlowContextBuilder.numThreads(numThreads);
            return this.builder();
        }

        public JMeterStatsRole.Builder rampUpTime(Long rampUpTime) {
            this.runFlowContextBuilder.rampUpTime(rampUpTime);
            return this.builder();
        }

        public JMeterStatsRole.Builder runMinutes(Long runMinutes) {
            this.runFlowContextBuilder.runMinutes(runMinutes);
            return this.builder();
        }

        public JMeterStatsRole.Builder delayBetweenRequests(Long delayBetweenRequests) {
            this.runFlowContextBuilder.delayBetweenRequests(delayBetweenRequests);
            return this.builder();
        }

        public JMeterStatsRole.Builder startupDelaySeconds(Long startupDelaySeconds) {
            this.runFlowContextBuilder.startupDelaySeconds(startupDelaySeconds);
            return this.builder();
        }

        public JMeterStatsRole.Builder outputFile(String outputFile) {
            this.runFlowContextBuilder.outputFile(outputFile);
            return this.builder();
        }

        public JMeterStatsRole.Builder copyResultsDestinationDir(String copyResultsDestinationDir) {
            this.runFlowContextBuilder.copyResultsDestinationDir(copyResultsDestinationDir);
            return this.builder();
        }

        public JMeterStatsRole.Builder copyResultsDestinationFileName(String copyResultsDestinationFileName) {
            this.runFlowContextBuilder.copyResultsDestinationFileName(copyResultsDestinationFileName);
            return this.builder();
        }

        public JMeterStatsRole.Builder copyResultsDestinationPassword(String copyResultsDestinationPassword) {
            this.runFlowContextBuilder.copyResultsDestinationPassword(copyResultsDestinationPassword);
            return this.builder();
        }

        public JMeterStatsRole.Builder copyResultsDestinationUser(String copyResultsDestinationUser) {
            this.runFlowContextBuilder.copyResultsDestinationUser(copyResultsDestinationUser);
            return this.builder();
        }


        protected JMeterStatsRole.Builder builder() {
            return this;
        }
    }


}