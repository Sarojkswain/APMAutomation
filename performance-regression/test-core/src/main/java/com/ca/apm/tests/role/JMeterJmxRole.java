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

import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.artifact.JMeterJmxVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class JMeterJmxRole extends AbstractRole {

    private final GenericFlowContext installContext;

    protected JMeterJmxRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        installContext = builder.installContext;
    }

    public String getScriptsDirectory() {
        return this.installContext.getDestination();
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        runFlow(client, GenericFlow.class, installContext);
    }

    public static class Builder extends BuilderBase<Builder, JMeterJmxRole> {

        private static final JMeterJmxVersion DEFAULT_ARTIFACT;
        protected ITasResolver tasResolver;
        protected String roleId;

        protected JMeterJmxVersion version;

        @Nullable
        protected GenericFlowContext installContext;

        protected String installPath;

        static {
            DEFAULT_ARTIFACT = JMeterJmxVersion.Agent_Performance_1_0;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            installPath(getWinDeployBase());
            this.version(DEFAULT_ARTIFACT);
        }


        @Override
        public JMeterJmxRole build() {
            assert this.version != null;

            // Non-standard URL is resolved by the Artifact itself
            URL artifactUrl = this.version.getArtifactUrl(this.tasResolver.getRegionalArtifactory());

            installContext =
                    new GenericFlowContext.Builder().artifactUrl(artifactUrl)
                            .destination(installPath).build();

            return getInstance();
        }

        @Override
        protected JMeterJmxRole getInstance() {
            return new JMeterJmxRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder installPath(String installPath) {
            Args.notBlank(installPath, "install dir");
            this.installPath = installPath;
            return builder();
        }

        public Builder version(JMeterJmxVersion version) {
            this.version = version;
            return this.builder();
        }
    }
}
