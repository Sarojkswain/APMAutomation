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
 * Flow Context for installing NET APM Agent
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class DeployNetAgentFlowContext extends DeployAgentFlowContext {

    private final boolean diEnabled;
    private final boolean btOn;

    protected DeployNetAgentFlowContext(DeployNetAgentFlowContext.Builder builder) {
        super(builder);
        this.diEnabled = builder.diEnabled;
        this.btOn = builder.btOn;
    }

    public boolean isDiEnabled() {
        return diEnabled;
    }

    public boolean isBtOn() {
        return btOn;
    }

    public static class Builder extends DeployAgentFlowContext.Builder {

        protected boolean diEnabled;
        protected boolean btOn;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "net_agent"));
        }

        public DeployNetAgentFlowContext build() {
            super.build();
            DeployNetAgentFlowContext context = this.getInstance();

            return context;
        }

        protected DeployNetAgentFlowContext getInstance() {
            return new DeployNetAgentFlowContext(this);
        }

        public Builder diEnabled(boolean diEnabled) {
            this.diEnabled = diEnabled;
            return this.builder();
        }

        public Builder btOn(boolean btOn) {
            this.btOn = btOn;
            return this.builder();
        }

        protected Builder builder() {
            return this;
        }
    }
}