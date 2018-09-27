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
public class QcUploadToolSimpleUploadFlowContext implements IFlowContext, EnvPropSerializable<QcUploadToolSimpleUploadFlowContext> {

    private final String installPath;
    private final String javaHome;
    private String testSetFolder;
    private String testSetName;
    private String testId;
    private Boolean passed;

    private final transient QcUploadToolSimpleUploadFlowContextSerializer envPropSerializer;

    protected QcUploadToolSimpleUploadFlowContext(Builder builder) {
        this.installPath = builder.installPath;
        this.javaHome = builder.javaHome;
        this.testSetFolder = builder.testSetFolder;
        this.testSetName = builder.testSetName;
        this.testId = builder.testId;
        this.passed = builder.passed;

        this.envPropSerializer = new QcUploadToolSimpleUploadFlowContextSerializer(this);
    }

    public String getInstallPath() {
        return installPath;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public String getTestSetFolder() {
        return testSetFolder;
    }

    public void setTestSetFolder(String testSetFolder) {
        this.testSetFolder = testSetFolder;
    }

    public String getTestSetName() {
        return testSetName;
    }

    public void setTestSetName(String testSetName) {
        this.testSetName = testSetName;
    }

    @Override
    public QcUploadToolSimpleUploadFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<Builder, QcUploadToolSimpleUploadFlowContext> {

        protected String installPath;
        protected String javaHome;
        protected String testSetFolder;
        protected String testSetName;
        protected String testId;
        protected Boolean passed;

        public Builder() {
        }

        public QcUploadToolSimpleUploadFlowContext build() {
            QcUploadToolSimpleUploadFlowContext context = this.getInstance();
            Args.notNull(context.installPath, "installPath");

            return context;
        }

        protected QcUploadToolSimpleUploadFlowContext getInstance() {
            return new QcUploadToolSimpleUploadFlowContext(this);
        }

        public Builder installPath(String installPath) {
            this.installPath = installPath;
            return this.builder();
        }

        public Builder javaHome(String javaHome) {
            this.javaHome = javaHome;
            return this.builder();
        }

        public Builder testSetFolder(String testSetFolder) {
            this.testSetFolder = testSetFolder;
            return this.builder();
        }

        public Builder testSetName(String testSetName) {
            this.testSetName = testSetName;
            return this.builder();
        }

        public Builder testId(String testId) {
            this.testId = testId;
            return this.builder();
        }

        public Builder passed(Boolean passed) {
            this.passed = passed;
            return this.builder();
        }

        protected Builder builder() {
            return this;
        }
    }

}