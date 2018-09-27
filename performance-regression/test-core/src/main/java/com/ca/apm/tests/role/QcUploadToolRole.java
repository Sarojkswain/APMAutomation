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

import com.ca.apm.tests.artifact.QcUploadToolVersion;
import com.ca.apm.tests.flow.QcUploadToolDeployFlow;
import com.ca.apm.tests.flow.QcUploadToolDeployFlowContext;
import com.ca.apm.tests.flow.QcUploadToolSimpleUploadFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

import java.net.URL;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class QcUploadToolRole extends AbstractRole {

    public static final String RUN_QC_SIMPLE_UPLOAD = "runQcSimpleUpload";

    private final QcUploadToolDeployFlowContext deployContext;
    private final QcUploadToolSimpleUploadFlowContext simpleUploadFlowContext;

    private final boolean predeployed;

    protected QcUploadToolRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        deployContext = builder.deployContext;
        simpleUploadFlowContext = builder.simpleUploadFlowContext;

        this.predeployed = builder.predeployed;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        if (!predeployed) {
            runFlow(client, QcUploadToolDeployFlow.class, deployContext);
        }
    }

    public QcUploadToolSimpleUploadFlowContext getSimpleUploadFlowContext() {
        return simpleUploadFlowContext;
    }

    public static class Builder extends BuilderBase<Builder, QcUploadToolRole> {
        protected ITasResolver tasResolver;
        protected String roleId;

        protected boolean predeployed;

        protected QcUploadToolDeployFlowContext.Builder deployContextBuilder;
        protected QcUploadToolDeployFlowContext deployContext;
        protected QcUploadToolSimpleUploadFlowContext.Builder simpleUploadFlowContextBuilder;
        protected QcUploadToolSimpleUploadFlowContext simpleUploadFlowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.initFlowContext();
        }

        protected void initFlowContext() {
            deployContextBuilder = new QcUploadToolDeployFlowContext.Builder();
            simpleUploadFlowContextBuilder = new QcUploadToolSimpleUploadFlowContext.Builder();
        }

        @Override
        public QcUploadToolRole build() {

            QcUploadToolVersion artifact = new QcUploadToolVersion(tasResolver);
            URL artifactUrl = tasResolver.getArtifactUrl(artifact.createArtifact());

            deployContext = deployContextBuilder.deployPackageUrl(artifactUrl).build();

            simpleUploadFlowContext =
                    simpleUploadFlowContextBuilder.installPath(deployContext.getDeploySourcesLocation())
                            .build();
            getEnvProperties().add(RUN_QC_SIMPLE_UPLOAD, simpleUploadFlowContext);

            return getInstance();
        }

        @Override
        protected QcUploadToolRole getInstance() {
            return new QcUploadToolRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deployContextBuilder.deploySourcesLocation(deploySourcesLocation);
            return this.builder();
        }

        public Builder predeployed() {
            this.predeployed = true;
            return builder();
        }

        public Builder predeployed(boolean predeployed) {
            this.predeployed = predeployed;
            return builder();
        }

        public Builder javaHome(String javaHome) {
            this.simpleUploadFlowContextBuilder.javaHome(javaHome);
            return this.builder();
        }
    }
}
