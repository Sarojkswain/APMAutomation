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

import java.util.HashMap;
import java.util.Map;

/**
 * Flow Context for JMeterRunFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class JMeterRunFlowContext
        implements IFlowContext, EnvPropSerializable<JMeterRunFlowContext>, INetShareUser {

    private final String jmeterPath;
    private final String jmeterLogConverterJarPath;
    private final String jmeterLogConverterOutputFileName;
    private final transient JMeterRunFlowContextSerializer envPropSerializer;
    private String scriptFilePath;
    private String outputJtlFile;
    private String outputLogFile;
    private Boolean deleteOutputLogsAfterCopy;
    private Boolean deleteOutputLogsBeforeRun;
    private Map<String, String> params;
    private String copyResultsDestinationDir;
    private String copyResultsDestinationJtlFileName;
    private String copyResultsDestinationLogFileName;
    private String copyResultsDestinationUser;
    private String copyResultsDestinationPassword;

    protected JMeterRunFlowContext(JMeterRunFlowContext.Builder builder) {
        this.jmeterPath = builder.jmeterPath;
        this.scriptFilePath = builder.scriptFilePath;
        this.outputJtlFile = builder.outputJtlFile;
        this.outputLogFile = builder.outputLogFile;
        this.deleteOutputLogsAfterCopy = builder.deleteOutputLogsAfterCopy;
        this.deleteOutputLogsBeforeRun = builder.deleteOutputLogsBeforeRun;

        this.params = builder.params;

        this.copyResultsDestinationDir = builder.copyResultsDestinationDir;
        this.copyResultsDestinationJtlFileName = builder.copyResultsDestinationJtlFileName;
        this.copyResultsDestinationLogFileName = builder.copyResultsDestinationLogFileName;
        this.copyResultsDestinationUser = builder.copyResultsDestinationUser;
        this.copyResultsDestinationPassword = builder.copyResultsDestinationPassword;

        this.jmeterLogConverterJarPath = builder.jmeterLogConverterJarPath;
        this.jmeterLogConverterOutputFileName = builder.jmeterLogConverterOutputFileName;

        this.envPropSerializer = new JMeterRunFlowContextSerializer(this);
    }

    public String getJmeterPath() {
        return jmeterPath;
    }

    public String getScriptFilePath() {
        return scriptFilePath;
    }

    public void setScriptFilePath(String scriptFilePath) {
        this.scriptFilePath = scriptFilePath;
    }

    public String getOutputJtlFile() {
        return outputJtlFile;
    }

    public void setOutputJtlFile(String outputJtlFile) {
        this.outputJtlFile = outputJtlFile;
    }

    public String getOutputLogFile() {
        return outputLogFile;
    }

    public void setOutputLogFile(String outputLogFile) {
        this.outputLogFile = outputLogFile;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getCopyResultsDestinationDir() {
        return copyResultsDestinationDir;
    }

    public void setCopyResultsDestinationDir(String copyResultsDestinationDir) {
        this.copyResultsDestinationDir = copyResultsDestinationDir;
    }

    public String getCopyResultsDestinationJtlFileName() {
        return copyResultsDestinationJtlFileName;
    }

    public void setCopyResultsDestinationJtlFileName(String copyResultsDestinationJtlFileName) {
        this.copyResultsDestinationJtlFileName = copyResultsDestinationJtlFileName;
    }

    public String getCopyResultsDestinationLogFileName() {
        return copyResultsDestinationLogFileName;
    }

    public void setCopyResultsDestinationLogFileName(String copyResultsDestinationLogFileName) {
        this.copyResultsDestinationLogFileName = copyResultsDestinationLogFileName;
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

    public String getJmeterLogConverterJarPath() {
        return jmeterLogConverterJarPath;
    }

    public String getJmeterLogConverterOutputFileName() {
        return jmeterLogConverterOutputFileName;
    }

    public Boolean getDeleteOutputLogsAfterCopy() {
        return deleteOutputLogsAfterCopy;
    }

    public Boolean getDeleteOutputLogsBeforeRun() {
        return deleteOutputLogsBeforeRun;
    }

    @Override
    public JMeterRunFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<JMeterRunFlowContext.Builder, JMeterRunFlowContext> {

        protected String jmeterPath;
        protected String scriptFilePath;
        protected String outputJtlFile;
        protected String outputLogFile;
        protected Boolean deleteOutputLogsAfterCopy;
        protected Boolean deleteOutputLogsBeforeRun;

        protected Map<String, String> params;

        protected String copyResultsDestinationDir;
        protected String copyResultsDestinationJtlFileName;
        protected String copyResultsDestinationLogFileName;
        protected String copyResultsDestinationUser;
        protected String copyResultsDestinationPassword;

        protected String jmeterLogConverterJarPath;
        protected String jmeterLogConverterOutputFileName;

        public Builder() {

            this.outputJtlFile = "output.jtl";
            this.outputLogFile = "output.log";
            this.deleteOutputLogsAfterCopy = false;
            this.deleteOutputLogsBeforeRun = false;
            this.copyResultsDestinationUser = DEFAULT_COPY_RESULTS_USER;
            this.copyResultsDestinationPassword = DEFAULT_COPY_RESULTS_PASSWORD;

            this.params = new HashMap<>();

        }

        public JMeterRunFlowContext build() {
            JMeterRunFlowContext context = this.getInstance();
            Args.notNull(context.jmeterPath, "jmeterPath");
            Args.notNull(context.scriptFilePath, "scriptFilePath");
            Args.notNull(context.outputJtlFile, "outputJtlFile");
            Args.notNull(context.outputLogFile, "outputLogFile");
            Args.notNull(context.deleteOutputLogsAfterCopy, "deleteOutputLogsAfterCopy");
            Args.notNull(context.deleteOutputLogsBeforeRun, "deleteOutputLogsBeforeRun");

            return context;
        }

        protected JMeterRunFlowContext getInstance() {
            return new JMeterRunFlowContext(this);
        }

        public Builder jmeterPath(String jmeterPath) {
            this.jmeterPath = jmeterPath;
            return this.builder();
        }

        public Builder scriptFilePath(String scriptFilePath) {
            this.scriptFilePath = scriptFilePath;
            return this.builder();
        }

        public Builder outputJtlFile(String outputJtlFile) {
            this.outputJtlFile = outputJtlFile;
            return this.builder();
        }

        public Builder outputLogFile(String outputLogFile) {
            this.outputLogFile = outputLogFile;
            return this.builder();
        }

        public Builder params(Map<String, String> params) {
            this.params = params;
            return this.builder();
        }

        public Builder copyResultsDestinationDir(String copyResultsDestinationDir) {
            this.copyResultsDestinationDir = copyResultsDestinationDir;
            return this.builder();
        }

        public Builder copyResultsDestinationJtlFileName(String copyResultsDestinationJtlFileName) {
            this.copyResultsDestinationJtlFileName = copyResultsDestinationJtlFileName;
            return this.builder();
        }

        public Builder copyResultsDestinationLogFileName(String copyResultsDestinationLogFileName) {
            this.copyResultsDestinationLogFileName = copyResultsDestinationLogFileName;
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

        public Builder jmeterLogConverterJarPath(String jmeterLogConverterJarPath) {
            this.jmeterLogConverterJarPath = jmeterLogConverterJarPath;
            return this.builder();
        }

        public Builder jmeterLogConverterOutputFileName(String jmeterLogConverterOutputFileName) {
            this.jmeterLogConverterOutputFileName = jmeterLogConverterOutputFileName;
            return this.builder();
        }

        public Builder deleteOutputLogsAfterCopy(Boolean deleteOutputLogsAfterCopy) {
            this.deleteOutputLogsAfterCopy = deleteOutputLogsAfterCopy;
            return this.builder();
        }

        public Builder deleteOutputLogsBeforeRun(Boolean deleteOutputLogsBeforeRun) {
            this.deleteOutputLogsBeforeRun = deleteOutputLogsBeforeRun;
            return this.builder();
        }

        protected Builder builder() {
            return this;
        }
    }

}