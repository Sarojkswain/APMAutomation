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

/**
 * Flow Context for installing Java APM Agent
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class DeployJavaAgentFlowContext extends DeployAgentFlowContext {

    private final Boolean siEnabled;
    private final Boolean accEnabled;
    private final Boolean accDefault;
    private final Boolean accMockOn;
    private final Boolean btOn;
    private final Boolean brtmEnabled;

    protected DeployJavaAgentFlowContext(DeployJavaAgentFlowContext.Builder builder) {
        super(builder);
        this.siEnabled = builder.siEnabled;
        this.accEnabled = builder.accEnabled;
        this.accDefault = builder.accDefault;
        this.accMockOn = builder.accMockOn;
        this.btOn = builder.btOn;
        this.brtmEnabled = builder.brtmEnabled;
    }

    public Boolean isSiEnabled() {
        return siEnabled;
    }

    public Boolean isAccEnabled() {
        return accEnabled;
    }

    public Boolean isAccDefault() {
        return accDefault;
    }

    public Boolean isAccMockOn() {
        return accMockOn;
    }

    public Boolean isBtOn() {
        return btOn;
    }

    public Boolean isBrtmEnabled() {
        return brtmEnabled;
    }

    public static class Builder extends DeployAgentFlowContext.Builder {

        protected Boolean siEnabled;
        protected Boolean accEnabled;
        protected Boolean accDefault;
        protected Boolean accMockOn;
        protected Boolean btOn;
        protected Boolean brtmEnabled;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "java_agent"));

        }

        public DeployJavaAgentFlowContext build() {
            super.build();
            DeployJavaAgentFlowContext context = this.getInstance();

            return context;
        }

        protected DeployJavaAgentFlowContext getInstance() {
            return new DeployJavaAgentFlowContext(this);
        }

        public Builder siEnabled(boolean siEnabled) {
            this.siEnabled = siEnabled;
            return this.builder();
        }

        public Builder accEnabled(boolean accEnabled) {
            this.accEnabled = accEnabled;
            return this.builder();
        }

        public Builder accDefault(boolean accDefault) {
            this.accDefault = accDefault;
            return this.builder();
        }

        public Builder accMockOn(boolean accMockOn) {
            this.accMockOn = accMockOn;
            return this.builder();
        }

        public Builder btOn(boolean btOn) {
            this.btOn = btOn;
            return this.builder();
        }

        public Builder brtmEnabled(boolean brtmEnabled) {
            this.brtmEnabled = brtmEnabled;
            return this.builder();
        }

        protected DeployJavaAgentFlowContext.Builder builder() {
            return this;
        }
    }
}