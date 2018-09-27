/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.powerpack.sysview.tests.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Role to deploy web test application in Websphere.
 */
public class WasAppRole extends AbstractRole {
    public static final String APP_URL_ROOT_PROP = "APP_URL_ROOT";
    private URL webAppUrl;
    private String webAppName;
    private String contextRoot;
    private String fileName;

    protected WasAppRole(Builder builder) {
        super(builder.roleId);
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        // Download web app
        GenericFlowContext ctx =
            new GenericFlowContext.Builder(webAppUrl).destination(Was8Role.WAS_BIN)
                .targetFilename(fileName).notArchive().build();

        runFlow(aaClient, GenericFlow.class, ctx);

        // Write install script
        FileCreatorFlowContext fileCreatorFlowContext =
            new FileCreatorFlowContext.Builder().destinationDir(Was8Role.WAS_BIN)
                .destinationFilename("installwebapp.tcl")
                .fromResource("/com/ca/tas/role/web/installwebapp.tcl")
                .substitution("webAppName", webAppName).substitution("contextRoot", contextRoot)
                .substitution("webArchive", fileName).build();

        runFlow(aaClient, FileCreatorFlow.class, fileCreatorFlowContext);

        // Install web app
        List<String> args =
            new ArrayList<>(Arrays.asList("-host", "localhost", "-port", "8880", "-f",
                "installwebapp.tcl", "-lang", "jacl"));

        RunCommandFlowContext runCommandFlowContext =
            new RunCommandFlowContext.Builder("wsadmin.bat").workDir(Was8Role.WAS_BIN).args(args)
                .build();

        runFlow(aaClient, RunCommandFlow.class, runCommandFlowContext);
    }

    public static class Builder extends BuilderBase<Builder, WasAppRole> {
        private final String roleId;
        private final ITasResolver tasResolver;
        private DefaultArtifact webAppArtifact;
        private String webAppName;
        private String contextRoot;
        private String hostName;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public WasAppRole build() {
            WasAppRole role = getInstance();

            role.webAppName = webAppName;
            role.contextRoot = contextRoot;
            role.fileName = webAppName + "." + webAppArtifact.getExtension();

            if (webAppArtifact.getVersion() == null || webAppArtifact.getVersion().isEmpty()) {
                Artifact wa = webAppArtifact.setVersion(tasResolver.getDefaultVersion());
                role.webAppUrl = tasResolver.getArtifactUrl(wa);
            } else {
                role.webAppUrl = tasResolver.getArtifactUrl(webAppArtifact);
            }

            // TODO get the port and hostname from WAS rather than TAS
            if (hostName != null) {
                role.addProperty(APP_URL_ROOT_PROP, "http://" + hostName + ":9080/" + contextRoot);
            }

            return role;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected WasAppRole getInstance() {
            return new WasAppRole(this);
        }

        /**
         * Web application to deploy
         *
         * @param artifact repository artifact (group, artifact name, extension, version)
         * @return Builder instance the method was called on.
         */
        public Builder webAppArtifact(DefaultArtifact artifact) {
            this.webAppArtifact = artifact;
            return builder();
        }

        /**
         * WAS host name to be used for application URL.
         *
         * @param hostname host name of the WAS server
         * @return Builder instance the method was called on.
         */
        public Builder hostName(String hostname) {
            this.hostName = hostname;
            return builder();
        }

        /**
         * Set WAS host name based on parent role.
         *
         * @param wasRoleId role name of the WAS server
         * @return Builder instance the method was called on.
         */
        public Builder parentRole(String wasRoleId) {
            return hostName(tasResolver.getHostnameById(wasRoleId));
        }

        /**
         * Web application name
         *
         * @param name web application filename (without extension)
         * @return Builder instance the method was called on.
         */
        public Builder webAppName(String name) {
            this.webAppName = name;
            return builder();
        }

        /**
         * Web application web context to deploy
         *
         * @param ctxRoot application context root (the same as application name usually)
         * @return Builder instance the method was called on.
         */
        public Builder webAppContextRoot(String ctxRoot) {
            this.contextRoot = ctxRoot;
            return builder();
        }
    }
}
