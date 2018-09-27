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

import com.ca.apm.tests.flow.agent.AgentStatsFlow;
import com.ca.apm.tests.flow.agent.AgentStatsFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

import java.util.ArrayList;
import java.util.List;

/**
 * @author meler02
 */
public class AgentStatsRole extends AbstractRole {

    public static final String RUN_AGENT_STATS = "runAgentStats";

    private final AgentStatsFlowContext runFlowContext;

    private final boolean runAgentStats;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected AgentStatsRole(AgentStatsRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        runFlowContext = builder.runFlowContext;

        runAgentStats = builder.runAgentStats;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (runAgentStats) {
            this.runFlow(aaClient, AgentStatsFlow.class, this.runFlowContext);
        }
    }

    public static class Builder extends BuilderBase<AgentStatsRole.Builder, AgentStatsRole> {

        private final String roleId;
        private final ITasResolver tasResolver;

        protected AgentStatsFlowContext.Builder runFlowContextBuilder;
        protected AgentStatsFlowContext runFlowContext;

        protected boolean runAgentStats;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.initFlowContext();
        }

        protected void initFlowContext() {
            this.runFlowContextBuilder = new AgentStatsFlowContext.Builder();

            this.runAgentStats = true;
        }

        public AgentStatsRole build() {
            this.initFlow();

            AgentStatsRole role = this.getInstance();

            return role;
        }

        protected AgentStatsRole getInstance() {
            return new AgentStatsRole(this);
        }

        protected void initFlow() {

            runFlowContext = runFlowContextBuilder.build();
            getEnvProperties().add(RUN_AGENT_STATS, runFlowContext);
        }

        public Builder agents(JavaAgentRole... agentRole) {
            List<String> agentNames = new ArrayList<>();
            List<String> buildNumbers = new ArrayList<>();
            List<String> buildSuffixes = new ArrayList<>();
            List<Boolean> spmOn = new ArrayList<>();
            List<Boolean> siOn = new ArrayList<>();
            List<Boolean> accOn = new ArrayList<>();
            List<Boolean> accMockOn = new ArrayList<>();
            List<Boolean> btOn = new ArrayList<>();
            List<Boolean> brtmOn = new ArrayList<>();
            for (int i = 0; i < agentRole.length; ++i) {
                agentNames.add(agentRole[i] == null ? "" : agentRole[i].getAgentName());
                buildNumbers.add((agentRole[i] == null || agentRole[i].getBuildNumber() == null) ? "-" : agentRole[i].getBuildNumber());
                buildSuffixes.add(agentRole[i] == null ? "" : agentRole[i].getAgentVersion());
                spmOn.add(true); // todo
                siOn.add(agentRole[i] != null && agentRole[i].isSiEnabled() != null && agentRole[i].isSiEnabled());
                accOn.add((agentRole[i] != null && agentRole[i].isAccEnabled() != null && agentRole[i].isAccEnabled()) ||
                        agentRole[i] != null && agentRole[i].isAccDefault() != null && agentRole[i].isAccDefault());
                accMockOn.add(agentRole[i] != null && agentRole[i].isAccMockOn() != null && agentRole[i].isAccMockOn());
                btOn.add(agentRole[i] != null && agentRole[i].isBtOn() != null && agentRole[i].isBtOn());
                brtmOn.add(agentRole[i] != null && agentRole[i].isBrtmEnabled() != null && agentRole[i].isBrtmEnabled());
            }
            this.runFlowContextBuilder.agentNames(agentNames);
            this.runFlowContextBuilder.buildNumbers(buildNumbers);
            this.runFlowContextBuilder.buildSuffixes(buildSuffixes);
            this.runFlowContextBuilder.spmOn(spmOn);
            this.runFlowContextBuilder.siOn(siOn);
            this.runFlowContextBuilder.accOn(accOn);
            this.runFlowContextBuilder.accMockOn(accMockOn);
            this.runFlowContextBuilder.btOn(btOn);
            this.runFlowContextBuilder.brtmOn(brtmOn);
            return this.builder();
        }

        public Builder agents(NetAgentRole... agentRole) {
            List<String> agentNames = new ArrayList<>();
            List<String> buildNumbers = new ArrayList<>();
            List<String> buildSuffixes = new ArrayList<>();
            List<Boolean> spmOn = new ArrayList<>();
            List<Boolean> diOn = new ArrayList<>();
            List<Boolean> accOn = new ArrayList<>();
            List<Boolean> accMockOn = new ArrayList<>();
            List<Boolean> btOn = new ArrayList<>();
            List<Boolean> brtmOn = new ArrayList<>();
            for (int i = 0; i < agentRole.length; ++i) {
                agentNames.add(agentRole[i] == null ? "" : agentRole[i].getAgentName());
                buildNumbers.add((agentRole[i] == null || agentRole[i].getBuildNumber() == null) ? "-" : agentRole[i].getBuildNumber());
                buildSuffixes.add(agentRole[i] == null ? "" : agentRole[i].getAgentVersion());
                spmOn.add(true); // todo
                diOn.add(agentRole[i] != null && agentRole[i].isDiEnabled());
                accOn.add(false); // todo
                accMockOn.add(false); // todo
                btOn.add(agentRole[i] != null && agentRole[i].isBtOn());
                brtmOn.add(false); // todo
            }
            this.runFlowContextBuilder.agentNames(agentNames);
            this.runFlowContextBuilder.buildNumbers(buildNumbers);
            this.runFlowContextBuilder.buildSuffixes(buildSuffixes);
            this.runFlowContextBuilder.spmOn(spmOn);
            this.runFlowContextBuilder.siOn(diOn);
            this.runFlowContextBuilder.accOn(accOn);
            this.runFlowContextBuilder.accMockOn(accMockOn);
            this.runFlowContextBuilder.btOn(btOn);
            this.runFlowContextBuilder.brtmOn(brtmOn);
            return this.builder();
        }

        public Builder outputFile(String outputFile) {
            this.runFlowContextBuilder.outputFile(outputFile);
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

        public Builder copyResultsDestinationPassword(String copyResultsDestinationPassword) {
            this.runFlowContextBuilder.copyResultsDestinationPassword(copyResultsDestinationPassword);
            return this.builder();
        }

        public Builder copyResultsDestinationUser(String copyResultsDestinationUser) {
            this.runFlowContextBuilder.copyResultsDestinationUser(copyResultsDestinationUser);
            return this.builder();
        }


        protected AgentStatsRole.Builder builder() {
            return this;
        }
    }


}