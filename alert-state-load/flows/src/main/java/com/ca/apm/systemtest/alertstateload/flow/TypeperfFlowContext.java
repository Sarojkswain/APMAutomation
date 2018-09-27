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
package com.ca.apm.systemtest.alertstateload.flow;

import java.util.Map;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.property.EnvPropSerializable;

/**
 * Flow Context for installing NET StockTrader WebApp into IIS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class TypeperfFlowContext implements IFlowContext, EnvPropSerializable<TypeperfFlowContext> {

    private final String[] metrics;
    private final transient TypeperfFlowContextSerializer envPropSerializer;
    private Long runTime;
    private String outputFileName;
    private Long samplesInterval;
    private String copyResultsDestinationDir;
    private String copyResultsDestinationFileName;
    private String copyResultsDestinationUser;
    private String copyResultsDestinationPassword;

    protected TypeperfFlowContext(TypeperfFlowContext.Builder builder) {
        this.metrics = builder.metrics;
        this.runTime = builder.runTime;
        this.outputFileName = builder.outputFileName;
        this.copyResultsDestinationDir = builder.copyResultsDestinationDir;
        this.copyResultsDestinationFileName = builder.copyResultsDestinationFileName;
        this.copyResultsDestinationUser = builder.copyResultsDestinationUser;
        this.copyResultsDestinationPassword = builder.copyResultsDestinationPassword;
        this.samplesInterval = builder.samplesInterval;
        this.envPropSerializer = new TypeperfFlowContextSerializer(this);
    }

    public String[] getMetrics() {
        return metrics;
    }

    public Long getSamplesInterval() {
        return samplesInterval;
    }

    public void setSamplesInterval(Long samplesInterval) {
        this.samplesInterval = samplesInterval;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
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
    public TypeperfFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder
        extends ExtendedBuilderBase<TypeperfFlowContext.Builder, TypeperfFlowContext> {
        protected String[] metrics;
        protected Long runTime;
        protected String outputFileName;
        protected Long samplesInterval;
        protected String copyResultsDestinationDir;
        protected String copyResultsDestinationFileName;
        protected String copyResultsDestinationUser;
        protected String copyResultsDestinationPassword;

        public Builder() {
            this.copyResultsDestinationUser = "administrator";
            this.copyResultsDestinationPassword = "Lister@123";
            this.samplesInterval = 1L;
        }

        public TypeperfFlowContext build() {
            TypeperfFlowContext context = this.getInstance();
            Args.notNull(context.metrics, "metrics");
            Args.notNull(context.runTime, "runTime");
            Args.notNull(context.outputFileName, "outputFileName");
            Args.notNull(context.samplesInterval, "samplesInterval");
            return context;
        }

        protected TypeperfFlowContext getInstance() {
            return new TypeperfFlowContext(this);
        }

        public Builder metrics(String[] metrics) {
            this.metrics = metrics;
            return this.builder();
        }

        public Builder runTime(Long runTime) {
            this.runTime = runTime;
            return this.builder();
        }

        public Builder outputFileName(String outputFileName) {
            this.outputFileName = outputFileName;
            return this.builder();
        }

        public Builder samplesInterval(long sampInterval) {
            this.samplesInterval = sampInterval;
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

        protected TypeperfFlowContext.Builder builder() {
            return this;
        }
    }

}
