/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.webapp.weblogic.ConfigureWebLogicAgentFlow;
import com.ca.apm.automation.action.flow.webapp.weblogic.ConfigureWebLogicAgentFlowContext;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.built.AgentNoInstaller;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.WebApplication;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.testbed.ITestbedMachine;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * WebLogicPortalRole class
 *
 * Role is immutable and is designed to be instantiated via Builder attached to the class.
 *
 * <pre><code>
 * WebLogicPortalRole wlRole = new WebLogicPortalRole.Builder("weblogic-role")
 *      .installLocation("C:\\Oracle\\Middleware")
 *      .responseFileDir("C:\\Oracle\\responseFiles")
 *      .version(WebLogicVersion.v103x86w)
 *      .installDir("C:\\Oracle\\Middleware\\wlserver_10.3")
 * .build();
 * </code></pre>
 *
 * @author Jan Pojer (pojja01@ca.com)
 * @author Nick Giles (gilni04@ca.com)
 * @since 1.0
 * @version $Id: $Id
 */
public class WebLogicPortalRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogicPortalRole.class);

    private static final String SERVER_RUNNING_TEXT_TO_MATCH = "Server started in RUNNING mode";

    @NotNull
    private final DeployWebLogicFlowContext webLogicFlowContext;
    @NotNull
    private final ITasResolver tasResolver;



    /**
     * Sets up the WebSphere role and defines its properties
     *
     * @param builder Builder object containing all necessary data
     */
    protected WebLogicPortalRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        tasResolver = builder.tasResolver;
        webLogicFlowContext = builder.webLogicFlowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        deployWlsContext(aaClient);

        
    }

    /**
     * <p>deployWlsContext.</p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void deployWlsContext(IAutomationAgentClient aaClient) {
        runFlow(aaClient, DeployWebLogicPortalFlow.class, webLogicFlowContext);
    }

    

    public ApplicationServerType getApplicationServerType() {
        return ApplicationServerType.WEBLOGIC;
    }

   
    public String getInstallDir() {
        return webLogicFlowContext.getWlsInstallDir();
    }

    public String getWebappsDirectory() {
        //todo
        return webLogicFlowContext.getWlsInstallDir();
    }

   
   
   
    /**
     * <p>Getter for the field <code>webLogicFlowContext</code>.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.webapp.weblogic.DeployWebLogicFlowContext} object.
     */
    @NotNull
    public DeployWebLogicFlowContext getWebLogicFlowContext() {
        return webLogicFlowContext;
    }



    /**
     * Builder responsible for holding all necessary properties to instantiate new WebLogic object
     */
    public static class Builder extends BuilderBase<Builder, WebLogicPortalRole> {

        private static final WebLogicPortalVersion DEFAULT_WEBLOGIC_VERSION = WebLogicPortalVersion.v103x86win;
        private static final String DEFAULT_DOMAIN_RELATIVE_PATH = "samples\\domains\\wl_server";
        private static final boolean DEFAULT_AUTOSTART = false;

        //required
        private final String roleId;
        private final ITasResolver tasResolver;
        //optional
        protected Artifact webLogicArtifact;

        private final DeployWebLogicFlowContext.Builder webLogicFlowCtxBuilder;


        protected DeployWebLogicFlowContext webLogicFlowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this(roleId, tasResolver, new DeployWebLogicFlowContext.Builder());
            version(DEFAULT_WEBLOGIC_VERSION);
        }

        private Builder(String roleId, ITasResolver tasResolver, DeployWebLogicFlowContext.Builder deployBuilder) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            webLogicFlowCtxBuilder = deployBuilder;
        }

        @Override
        public WebLogicPortalRole build() {

            initWebLogicDeployContext();

            WebLogicPortalRole WebLogicPortalRole = getInstance();

            return WebLogicPortalRole;
        }

        
        protected void initWebLogicDeployContext() {
            URL artifactUrl = tasResolver.getArtifactUrl(webLogicArtifact);
            String fileName = getFilename(webLogicArtifact);
            Args.notNull(fileName, "File name");

            webLogicFlowContext = webLogicFlowCtxBuilder
                .webLogicInstallerUrl(artifactUrl)
                .webLogicInstallerFilename(fileName).customComponentPaths(new HashSet(Arrays.asList(
                	"WebLogic Portal|WebLogic Server"))).responseFileDir("C:\\test")
                .build();
        }

        
        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected WebLogicPortalRole getInstance() {
            return new WebLogicPortalRole(this);
        }

        public Builder installLocation(@NotNull String installLocation) {
            webLogicFlowCtxBuilder.installLocation(installLocation);
            return builder();
        }

        public Builder version(@NotNull WebLogicPortalVersion webLogicVersion) {
            webLogicArtifact = webLogicVersion.getArtifact();
            return builder();
        }

        public Builder version(@NotNull Artifact webLogicArtifact) {
            this.webLogicArtifact = webLogicArtifact;
            return builder();
        }

        public Builder installDir(@NotNull String wlsInstallDir) {
            webLogicFlowCtxBuilder.wlsInstallDir(wlsInstallDir);
            getEnvProperties().add("wls.home", wlsInstallDir);

            return builder();
        }

        public Builder customComponentPaths(Set<String> componentsPaths) {
            webLogicFlowCtxBuilder.customComponentPaths(componentsPaths);
            return this;
        }

        public Builder noNodeManagerService() {
            webLogicFlowCtxBuilder.noNodeManagerService();
            return builder();
        }

        public Builder nodeManagerPort(int nodeManagerPort) {
            webLogicFlowCtxBuilder.nodeManagerPort(nodeManagerPort);
            return builder();
        }

        public Builder responseFileDir(@NotNull String responseFileDir) {
            webLogicFlowCtxBuilder.responseFileDir(responseFileDir);
            return builder();
        }

        public Builder webLogicInstallerDir(@NotNull String webLogicInstallerDir) {
            webLogicFlowCtxBuilder.webLogicInstallerDir(webLogicInstallerDir);
            return builder();
        }

        public Builder installLogFile(@NotNull String installLogFile) {
            webLogicFlowCtxBuilder.installLogFile(installLogFile);
            return builder();
        }


        public Builder genericJavaInstaller() {
            webLogicFlowCtxBuilder.genericJavaInstaller();
            return builder();
        }

        public Builder customJvm(String customJvm) {
            webLogicFlowCtxBuilder.localJvm(customJvm);
            return builder();
        }

        
    }
}
