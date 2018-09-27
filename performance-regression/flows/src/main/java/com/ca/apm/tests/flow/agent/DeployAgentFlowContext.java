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
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * Flow Context for installing Java APM Agent
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class DeployAgentFlowContext implements IFlowContext {

    private final URL deployPackageUrl;
    private final String deploySourcesLocation;

    private final String agentName;
    private final String buildNumber;
    private final String emLocation;

    private final boolean undeployExistingBeforeInstall;

    protected DeployAgentFlowContext(Builder builder) {
        this.deployPackageUrl = builder.deployPackageUrl;
        this.deploySourcesLocation = builder.deploySourcesLocation;

        this.agentName = builder.agentName;
        this.buildNumber = builder.buildNumber;
        this.emLocation = builder.emLocation;

        this.undeployExistingBeforeInstall = builder.undeployExistingBeforeInstall;
    }

    public URL getDeployPackageUrl() {
        return deployPackageUrl;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getEmLocation() {
        return emLocation;
    }

    public boolean isUndeployExistingBeforeInstall() {
        return undeployExistingBeforeInstall;
    }

    public String getDeploySourcesLocation() {
        return deploySourcesLocation;
    }

    public static class Builder extends ExtendedBuilderBase<Builder, DeployAgentFlowContext> {

        protected URL deployPackageUrl;
        protected String deploySourcesLocation;

        protected String agentName;
        protected String buildNumber;
        protected String emLocation;

        protected boolean undeployExistingBeforeInstall;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "java_agent"));

        }

        public DeployAgentFlowContext build() {
            DeployAgentFlowContext context = this.getInstance();
            Args.notNull(context.deployPackageUrl, "deployPackageUrl");
            Args.notNull(context.deploySourcesLocation, "deploySourcesLocation");

            return context;
        }

        protected DeployAgentFlowContext getInstance() {
            return new DeployAgentFlowContext(this);
        }

        public Builder deployPackageUrl(URL deployPackageUrl) {
            this.deployPackageUrl = deployPackageUrl;
            return this.builder();
        }

        public Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deploySourcesLocation = deploySourcesLocation;
            return this.builder();
        }

        public Builder agentName(String agentName) {
            this.agentName = agentName;
            return this.builder();
        }

        public Builder buildNumber(String buildNumber) {
            this.buildNumber = buildNumber;
            return this.builder();
        }

        public Builder emLocation(String emLocation) {
            this.emLocation = emLocation;
            return this.builder();
        }

        public Builder undeployExistingBeforeInstall(boolean undeployExistingBeforeInstall) {
            this.undeployExistingBeforeInstall = undeployExistingBeforeInstall;
            return this.builder();
        }

        protected Builder builder() {
            return this;
        }
    }
}