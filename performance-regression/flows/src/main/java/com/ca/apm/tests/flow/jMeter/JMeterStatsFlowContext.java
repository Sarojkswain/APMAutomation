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
package com.ca.apm.tests.flow.jMeter;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.tests.flow.INetShareUser;
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.Map;

/**
 * Flow Context for JMeterStatsFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class JMeterStatsFlowContext
        implements IFlowContext, EnvPropSerializable<JMeterStatsFlowContext>, INetShareUser {

    private final Long numThreads;
    private final Long delayBetweenRequests;
    private final transient JMeterStatsFlowContextSerializer envPropSerializer;
    private Long rampUpTime;
    private Long runMinutes;
    private Long startupDelaySeconds;
    private String outputFile;
    private String copyResultsDestinationDir;
    private String copyResultsDestinationFileName;
    private String copyResultsDestinationUser;
    private String copyResultsDestinationPassword;

    protected JMeterStatsFlowContext(JMeterStatsFlowContext.Builder builder) {
        this.numThreads = builder.numThreads;
        this.rampUpTime = builder.rampUpTime;
        this.runMinutes = builder.runMinutes;
        this.delayBetweenRequests = builder.delayBetweenRequests;
        this.startupDelaySeconds = builder.startupDelaySeconds;
        this.outputFile = builder.outputFile;

        this.copyResultsDestinationDir = builder.copyResultsDestinationDir;
        this.copyResultsDestinationFileName = builder.copyResultsDestinationFileName;
        this.copyResultsDestinationUser = builder.copyResultsDestinationUser;
        this.copyResultsDestinationPassword = builder.copyResultsDestinationPassword;

        this.envPropSerializer = new JMeterStatsFlowContextSerializer(this);
    }

    public Long getNumThreads() {
        return numThreads;
    }

    public Long getRampUpTime() {
        return rampUpTime;
    }

    public void setRampUpTime(Long rampUpTime) {
        this.rampUpTime = rampUpTime;
    }

    public Long getRunMinutes() {
        return runMinutes;
    }

    public void setRunMinutes(Long runMinutes) {
        this.runMinutes = runMinutes;
    }

    public Long getDelayBetweenRequests() {
        return delayBetweenRequests;
    }

    public Long getStartupDelaySeconds() {
        return startupDelaySeconds;
    }

    public void setStartupDelaySeconds(Long startupDelaySeconds) {
        this.startupDelaySeconds = startupDelaySeconds;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
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
    public JMeterStatsFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<JMeterStatsFlowContext.Builder, JMeterStatsFlowContext> {

        protected Long numThreads;
        protected Long rampUpTime;
        protected Long runMinutes;
        protected Long delayBetweenRequests;
        protected Long startupDelaySeconds;
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

        public JMeterStatsFlowContext build() {
            JMeterStatsFlowContext context = this.getInstance();
            Args.notNull(context.numThreads, "numThreads");
            Args.notNull(context.rampUpTime, "rampUpTime");
            Args.notNull(context.runMinutes, "runMinutes");
            Args.notNull(context.delayBetweenRequests, "delayBetweenRequests");
            Args.notNull(context.startupDelaySeconds, "startupDelaySeconds");
            Args.notNull(context.outputFile, "outputFile");

            return context;
        }

        protected JMeterStatsFlowContext getInstance() {
            return new JMeterStatsFlowContext(this);
        }

        public Builder numThreads(Long numThreads) {
            this.numThreads = numThreads;
            return this.builder();
        }

        public Builder rampUpTime(Long rampUpTime) {
            this.rampUpTime = rampUpTime;
            return this.builder();
        }

        public Builder runMinutes(Long runMinutes) {
            this.runMinutes = runMinutes;
            return this.builder();
        }

        public Builder delayBetweenRequests(Long delayBetweenRequests) {
            this.delayBetweenRequests = delayBetweenRequests;
            return this.builder();
        }

        public Builder startupDelaySeconds(Long startupDelaySeconds) {
            this.startupDelaySeconds = startupDelaySeconds;
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

        protected JMeterStatsFlowContext.Builder builder() {
            return this;
        }
    }

}