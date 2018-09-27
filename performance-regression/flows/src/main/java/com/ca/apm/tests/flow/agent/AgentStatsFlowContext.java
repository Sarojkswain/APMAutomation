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
package com.ca.apm.tests.flow.agent;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.tests.flow.INetShareUser;
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.List;
import java.util.Map;

/**
 * Flow Context for JMeterStatsFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class AgentStatsFlowContext implements IFlowContext, EnvPropSerializable<AgentStatsFlowContext>, INetShareUser {

    private final List<String> agentNames;
    private final List<String> buildNumbers;
    private final List<String> buildSuffixes;
    private final List<Boolean> spmOn;
    private final List<Boolean> siOn;
    private final List<Boolean> accOn;
    private final List<Boolean> accMockOn;
    private final List<Boolean> btOn;
    private final List<Boolean> brtmOn;
    private final transient AgentStatsFlowContextSerializer envPropSerializer;
    private String outputFile;
    private String copyResultsDestinationDir;
    private String copyResultsDestinationFileName;
    private String copyResultsDestinationUser;
    private String copyResultsDestinationPassword;

    protected AgentStatsFlowContext(AgentStatsFlowContext.Builder builder) {
        this.agentNames = builder.agentNames;
        this.buildNumbers = builder.buildNumbers;
        this.buildSuffixes = builder.buildSuffixes;
        this.spmOn = builder.spmOn;
        this.siOn = builder.siOn;
        this.accOn = builder.accOn;
        this.accMockOn = builder.accMockOn;
        this.btOn = builder.btOn;
        this.brtmOn = builder.brtmOn;
        this.outputFile = builder.outputFile;

        this.copyResultsDestinationDir = builder.copyResultsDestinationDir;
        this.copyResultsDestinationFileName = builder.copyResultsDestinationFileName;
        this.copyResultsDestinationUser = builder.copyResultsDestinationUser;
        this.copyResultsDestinationPassword = builder.copyResultsDestinationPassword;

        this.envPropSerializer = new AgentStatsFlowContextSerializer(this);
    }

    public List<String> getAgentNames() {
        return agentNames;
    }

    public List<String> getBuildNumbers() {
        return buildNumbers;
    }

    public List<String> getBuildSuffixes() {
        return buildSuffixes;
    }

    public List<Boolean> getSpmOn() {
        return spmOn;
    }

    public List<Boolean> getSiOn() {
        return siOn;
    }

    public List<Boolean> getAccOn() {
        return accOn;
    }

    public List<Boolean> getAccMockOn() {
        return accMockOn;
    }

    public List<Boolean> getBtOn() {
        return btOn;
    }

    public List<Boolean> getBrtmOn() {
        return brtmOn;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getCopyResultsDestinationDir() {
        return copyResultsDestinationDir;
    }

    public void setCopyResultsDestinationDir(String copyResultsDestinationDir) {
        this.copyResultsDestinationDir = copyResultsDestinationDir;
    }

    public String getCopyResultsDestinationFileName() {
        return copyResultsDestinationFileName;
    }

    public void setCopyResultsDestinationFileName(String copyResultsDestinationFileName) {
        this.copyResultsDestinationFileName = copyResultsDestinationFileName;
    }

    public String getCopyResultsDestinationUser() {
        return copyResultsDestinationUser;
    }

    public void setCopyResultsDestinationUser(String copyResultsDestinationUser) {
        this.copyResultsDestinationUser = copyResultsDestinationUser;
    }

    public String getCopyResultsDestinationPassword() {
        return copyResultsDestinationPassword;
    }

    public void setCopyResultsDestinationPassword(String copyResultsDestinationPassword) {
        this.copyResultsDestinationPassword = copyResultsDestinationPassword;
    }

    @Override
    public AgentStatsFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<AgentStatsFlowContext.Builder, AgentStatsFlowContext> {

        protected List<String> agentNames;
        protected List<String> buildNumbers;
        protected List<String> buildSuffixes;
        protected List<Boolean> spmOn;
        protected List<Boolean> siOn;
        protected List<Boolean> accOn;
        protected List<Boolean> accMockOn;
        protected List<Boolean> btOn;
        protected List<Boolean> brtmOn;
        protected String outputFile;

        protected String copyResultsDestinationDir;
        protected String copyResultsDestinationFileName;
        protected String copyResultsDestinationUser;
        protected String copyResultsDestinationPassword;

        public Builder() {
            this.outputFile = "output.csv";
            this.copyResultsDestinationUser = DEFAULT_COPY_RESULTS_USER;
            this.copyResultsDestinationPassword = DEFAULT_COPY_RESULTS_PASSWORD;
        }

        public AgentStatsFlowContext build() {
            AgentStatsFlowContext context = this.getInstance();
            Args.notNull(context.agentNames, "agentNames");
            Args.notNull(context.buildNumbers, "buildNumbers");
            Args.notNull(context.buildSuffixes, "buildSuffixes");
            Args.notNull(context.spmOn, "spmOn");
            Args.notNull(context.siOn, "siOn");
            Args.notNull(context.accOn, "accOn");
            Args.notNull(context.accMockOn, "accMockOn");
            Args.notNull(context.btOn, "btOn");
            Args.notNull(context.brtmOn, "brtmOn");
            Args.notNull(context.outputFile, "outputFile");

            return context;
        }

        protected AgentStatsFlowContext getInstance() {
            return new AgentStatsFlowContext(this);
        }

        public Builder agentNames(List<String> agentNames) {
            this.agentNames = agentNames;
            return this.builder();
        }

        public Builder buildNumbers(List<String> buildNumbers) {
            this.buildNumbers = buildNumbers;
            return this.builder();
        }

        public Builder buildSuffixes(List<String> buildSuffixes) {
            this.buildSuffixes = buildSuffixes;
            return this.builder();
        }

        public Builder spmOn(List<Boolean> spmOn) {
            this.spmOn = spmOn;
            return this.builder();
        }

        public Builder siOn(List<Boolean> siOn) {
            this.siOn = siOn;
            return this.builder();
        }

        public Builder accOn(List<Boolean> accOn) {
            this.accOn = accOn;
            return this.builder();
        }

        public Builder accMockOn(List<Boolean> accMockOn) {
            this.accMockOn = accMockOn;
            return this.builder();
        }

        public Builder btOn(List<Boolean> btOn) {
            this.btOn = btOn;
            return this.builder();
        }

        public Builder brtmOn(List<Boolean> brtmOn) {
            this.brtmOn = brtmOn;
            return this.builder();
        }

        public Builder outputFile(String outputFile) {
            this.outputFile = outputFile;
            return this.builder();
        }

        public Builder copyResultsDestinationDir(String copyResultsDestinationDir) {
            this.copyResultsDestinationDir = copyResultsDestinationDir;
            return this.builder();
        }

        public Builder copyResultsDestinationFileName(String copyResultsDestinationFileName) {
            this.copyResultsDestinationFileName = copyResultsDestinationFileName;
            return this.builder();
        }

        public Builder copyResultsDestinationUser(String copyResultsDestinationUser) {
            this.copyResultsDestinationUser = copyResultsDestinationUser;
            return this.builder();
        }

        public Builder copyResultsDestinationPassword(String copyResultsDestinationPassword) {
            this.copyResultsDestinationPassword = copyResultsDestinationPassword;
            return this.builder();
        }

        protected AgentStatsFlowContext.Builder builder() {
            return this;
        }
    }

}