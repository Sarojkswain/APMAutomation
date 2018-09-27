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
package com.ca.apm.automation.action.flow.etc;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * Flow context for {@link ConfigureProviderFlow}
 *
 * @author bhusu01
 */
public class ConfigureProviderFlowContext implements IFlowContext {
    private String authToken;
    private String masterHost;
    private String providerHost;

    public ConfigureProviderFlowContext(Builder builder) {
        this.authToken = builder.authToken;
        this.masterHost = builder.masterHost;
        this.providerHost = builder.providerHost;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getMasterHost() {
        return masterHost;
    }

    public String getProviderHost() {
        return providerHost;
    }

    public static class Builder extends BuilderBase<Builder, ConfigureProviderFlowContext> {

        private String authToken;
        private String masterHost;
        private String providerHost;

        @Override
        protected ConfigureProviderFlowContext getInstance() {
            return build();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public ConfigureProviderFlowContext build() {
            return new ConfigureProviderFlowContext(this);
        }

        public Builder authToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public Builder masterHost(String masterHost) {
            this.masterHost = masterHost;
            return this;
        }

        public Builder providerHost(String providerHost) {
            this.providerHost = providerHost;
            return this;
        }
    }
}
