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
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class LogsGathererFlowContext implements IFlowContext, EnvPropSerializable<LogsGathererFlowContext>,
        INetShareUser {

    private final transient LogsGathererFlowContextSerializer envPropSerializer;
    private String targetDir;
    private Boolean deleteSource;
    private Boolean addTimestamp;
    private Boolean ignoreDeletionErrors;
    private Boolean ignoreEmpty;
    private Map<String, String> filesMapping;
    private String sourceDir;
    private String targetZipFile;
    private String copyResultsDestinationDir;
    private String copyResultsDestinationFileName;
    private String copyResultsDestinationUser;
    private String copyResultsDestinationPassword;

    protected LogsGathererFlowContext(LogsGathererFlowContext.Builder builder) {
        this.sourceDir = builder.sourceDir;
        this.targetDir = builder.targetDir;
        this.targetZipFile = builder.targetZipFile;
        this.deleteSource = builder.deleteSource;
        this.addTimestamp = builder.addTimestamp;
        this.ignoreDeletionErrors = builder.ignoreDeletionErrors;
        this.ignoreEmpty = builder.ignoreEmpty;
        this.filesMapping = builder.filesMapping;
        this.copyResultsDestinationDir = builder.copyResultsDestinationDir;
        this.copyResultsDestinationFileName = builder.copyResultsDestinationFileName;
        this.copyResultsDestinationUser = builder.copyResultsDestinationUser;
        this.copyResultsDestinationPassword = builder.copyResultsDestinationPassword;
        this.envPropSerializer = new LogsGathererFlowContextSerializer(this);
    }

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public String getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public Map<String, String> getFilesMapping() {
        return filesMapping;
    }

    public void setFilesMapping(Map<String, String> filesMapping) {
        this.filesMapping = filesMapping;
    }

    public String getTargetZipFile() {
        return targetZipFile;
    }

    public void setTargetZipFile(String targetZipFile) {
        this.targetZipFile = targetZipFile;
    }

    public Boolean getDeleteSource() {
        return deleteSource;
    }

    public void setDeleteSource(Boolean deleteSource) {
        this.deleteSource = deleteSource;
    }

    public Boolean getIgnoreDeletionErrors() {
        return ignoreDeletionErrors;
    }

    public void setIgnoreDeletionErrors(Boolean ignoreDeletionErrors) {
        this.ignoreDeletionErrors = ignoreDeletionErrors;
    }

    public Boolean getIgnoreEmpty() {
        return ignoreEmpty;
    }

    public void setIgnoreEmpty(Boolean ignoreEmpty) {
        this.ignoreEmpty = ignoreEmpty;
    }

    public String getCopyResultsDestinationDir() {
        return copyResultsDestinationDir;
    }

    public void setCopyResultsDestinationDir(String copyResultsDestinationDir) {
        this.copyResultsDestinationDir = copyResultsDestinationDir;
    }

    public Boolean getAddTimestamp() {
        return addTimestamp;
    }

    public void setAddTimestamp(Boolean addTimestamp) {
        this.addTimestamp = addTimestamp;
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
    public LogsGathererFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<LogsGathererFlowContext.Builder, LogsGathererFlowContext> {

        protected String sourceDir;
        protected String targetDir;
        protected String targetZipFile;
        protected Boolean deleteSource;
        protected Boolean addTimestamp;
        protected Boolean ignoreDeletionErrors;
        protected Boolean ignoreEmpty;
        protected Map<String, String> filesMapping;
        protected String copyResultsDestinationDir;
        protected String copyResultsDestinationFileName;
        protected String copyResultsDestinationUser;
        protected String copyResultsDestinationPassword;

        public Builder() {
            this.copyResultsDestinationUser = DEFAULT_COPY_RESULTS_USER;
            this.copyResultsDestinationPassword = DEFAULT_COPY_RESULTS_PASSWORD;
            this.deleteSource = true;
            this.addTimestamp = true;
            this.ignoreDeletionErrors = true;
            this.ignoreEmpty = true;
        }

        public LogsGathererFlowContext build() {
            LogsGathererFlowContext context = this.getInstance();
            Args.notNull(context.targetDir, "targetDir");
            Args.check(context.filesMapping != null || (sourceDir != null && targetZipFile != null), "filesMapping or sourceDir and targetZipFile");
            Args.notNull(context.deleteSource, "deleteSource");
            Args.notNull(context.addTimestamp, "addTimestamp");
            Args.notNull(context.ignoreDeletionErrors, "ignoreDeletionErrors");
            Args.notNull(context.ignoreEmpty, "ignoreEmpty");
            return context;
        }

        protected LogsGathererFlowContext getInstance() {
            return new LogsGathererFlowContext(this);
        }

        public Builder sourceDir(String sourceDir) {
            this.sourceDir = sourceDir;
            return this.builder();
        }

        public Builder targetDir(String targetDir) {
            this.targetDir = targetDir;
            return this.builder();
        }

        public Builder targetZipFile(String targetFileName) {
            this.targetZipFile = targetFileName;
            return this.builder();
        }

        public Builder deleteSource(Boolean deleteSource) {
            this.deleteSource = deleteSource;
            return this.builder();
        }

        public Builder addTimestamp(Boolean addTimestamp) {
            this.addTimestamp = addTimestamp;
            return this.builder();
        }

        public Builder ignoreDeletionErrors(Boolean ignoreDeletionErrors) {
            this.ignoreDeletionErrors = ignoreDeletionErrors;
            return this.builder();
        }

        public Builder ignoreEmpty(Boolean ignoreEmpty) {
            this.ignoreEmpty = ignoreEmpty;
            return this.builder();
        }

        public Builder filesMapping(Map<String, String> filesMapping) {
            this.filesMapping = filesMapping;
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

        protected LogsGathererFlowContext.Builder builder() {
            return this;
        }
    }

}