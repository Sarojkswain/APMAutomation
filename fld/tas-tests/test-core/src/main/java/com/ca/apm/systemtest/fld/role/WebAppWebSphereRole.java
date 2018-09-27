package com.ca.apm.systemtest.fld.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.IWebSphereRole;

/**
 * Role to download BRTMTestApp tool from Artifactory and deploy to WebSphere.
 *
 * code inspired by com.ca.tas.role.web.QaAppWebSphereRole
 *
 * @author MELER02
 *
 */
public class WebAppWebSphereRole extends WebAppRole<IWebSphereRole> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebAppWebSphereRole.class);

    private final com.ca.apm.automation.action.flow.utility.GenericFlowContext.Builder genericFlowContextBuilder;
    private final com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext.Builder runCommandFlowContextBuilder;

    protected final String fileName;
    protected final String webAppName;
    protected final String contextRoot;
    protected final IWebSphereRole appServerRole;

    protected WebAppWebSphereRole(WebAppWebSphereRole.Builder builder) {
        super(builder);
        this.fileName = builder.fileName;
        this.webAppName = builder.webAppName;
        this.contextRoot = builder.contextRoot;
        this.appServerRole = builder.appServerRole;
        this.genericFlowContextBuilder = builder.genericContextBuilder;
        this.runCommandFlowContextBuilder = builder.runCommandFlowContextBuilder;
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // a BIG FIXME - use Cargo deployment once a fix for
    // https://codehaus-cargo.atlassian.net/browse/CARGO-1392 is in release
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        LOGGER.info("WebAppWebSphereRole.deploy():: entry");
        this.setMachine(appServerRole);
        this.runFlow(aaClient, GenericFlow.class,
            this.genericFlowContextBuilder.destination(appServerRole.getProfileBin())
                .targetFilename(this.fileName).build());

        // write install script
        FileCreatorFlowContext fileCreatorFlowContext =
            (new FileCreatorFlowContext.Builder()).destinationDir(appServerRole.getProfileBin())
                .destinationFilename("installwebapp.tcl")
                .fromResource("/com/ca/tas/role/web/installwebapp.tcl")
                // .substitution("webAppName", "QATestApp")
                .substitution("webAppName", this.webAppName)
                // .substitution("contextRoot", "QATestApp")
                .substitution("contextRoot", this.contextRoot)
                // .substitution("webArchive", fileName)
                .substitution("webArchive", this.fileName).build();
        this.runFlow(aaClient, FileCreatorFlow.class, fileCreatorFlowContext);

        // install web app
        List<String> args =
            new ArrayList<>(Arrays.asList("-host", "localhost", "-port", "8880", "-f",
                "installwebapp.tcl", "-lang", "jacl"));

        RunCommandFlowContext runCommandFlowContext =
            this.runCommandFlowContextBuilder.workDir(appServerRole.getProfileBin()).args(args)
                .build();
        this.runFlow(aaClient, RunCommandFlow.class, runCommandFlowContext);
        LOGGER.info("WebAppWebSphereRole.deploy():: exit");
    }

    // currenly not used method
    // @Override
    // public void cargoLessDeployAndConfigure(IWebSphereRole appServerRole) {
    // LOGGER.info("In WebAppWebSphereRole::cargoLessDeployAndConfigure()");
    // GenericFlowContext download =
    // genericFlowContextBuilder.destination(appServerRole.getProfileBin())
    // .targetFilename(fileName).build();
    // IRole downloadRole =
    // UtilityRole.flow(depRoleId("download-app"), GenericFlow.class, download);
    // this.before(downloadRole);
    //
    // FileCreatorFlowContext fileCreatorFlowContext =
    // new FileCreatorFlowContext.Builder().destinationDir(appServerRole.getProfileBin())
    // .destinationFilename("installwebapp.tcl")
    // .fromResource("/com/ca/tas/role/web/installwebapp.tcl")
    // .substitution("webAppName", this.webAppName)
    // .substitution("contextRoot", this.contextRoot)
    // .substitution("webArchive", this.fileName).build();
    //
    // IRole scriptRole =
    // UtilityRole.flow(depRoleId("create-deploy-script"), FileCreatorFlow.class,
    // fileCreatorFlowContext);
    // scriptRole.after(downloadRole);
    //
    // List<String> args =
    // new ArrayList<>(Arrays.asList("-host", "localhost", "-port", "8880", "-f",
    // "installwebapp.tcl", "-lang", "jacl", ">", "c:\\foo.log"));
    //
    // RunCommandFlowContext runCommandFlowContext =
    // runCommandFlowContextBuilder.workDir(appServerRole.getProfileBin()).args(args).build();
    // LOGGER.info("Using run context: " + runCommandFlowContext);
    // LOGGER.info("In directory " + runCommandFlowContext.getWorkDir() + " -> "
    // + runCommandFlowContext.getExec());
    // IRole commandRole =
    // UtilityRole.flow(depRoleId("run-cargoless-qa-deploy"), RunCommandFlow.class,
    // runCommandFlowContext);
    // commandRole.after(scriptRole);
    //
    // List<IRole> internalRoles = Arrays.asList(downloadRole, scriptRole, commandRole);
    // this.before(internalRoles);
    //
    // this.dependentRoles.addAll(internalRoles);
    // }

    // TODO - not used - remove ?
    // public void deployAndConfigure(IAutomationAgentClient aaClient, IWebSphereRole appServerRole)
    // {
    // if(!this.hasCargoDeploy()) {
    // this.setMachine(appServerRole);
    // this.runFlow(aaClient, GenericFlow.class,
    // this.genericFlowContextBuilder.destination(appServerRole.getProfileBin())
    // .targetFilename(this.fileName)
    // .build());
    // this.runFlow(aaClient, GenericFlow.class, this.genericFlowContextBuilder.build());
    // FileCreatorFlowContext fileCreatorFlowContext = (new FileCreatorFlowContext.Builder())
    // .destinationDir(appServerRole.getProfileBin())
    // .destinationFilename("installwebapp.tcl")
    // .fromResource("/com/ca/tas/role/web/installwebapp.tcl")
    // .substitution("webAppName", this.webAppName)
    // .substitution("contextRoot", this.contextRoot)
    // .substitution("webArchive", this.fileName)
    // .build();
    // this.runFlow(aaClient, FileCreatorFlow.class, fileCreatorFlowContext);
    // List<String> args = Arrays.asList(new String[]{"-host", "localhost", "-port", "8880", "-f",
    // "installwebapp.tcl", "-lang", "jacl"});
    // RunCommandFlowContext runCommandFlowContext =
    // this.runCommandFlowContextBuilder.workDir(appServerRole.getProfileBin())
    // .args(args)
    // .build();
    // this.runFlow(aaClient, RunCommandFlow.class, runCommandFlowContext);
    // }
    // }

    public static class Builder extends WebAppRole.Builder<IWebSphereRole> {
        private GenericFlowContext.Builder genericContextBuilder;
        private RunCommandFlowContext.Builder runCommandFlowContextBuilder;

        protected String fileName;
        @Nullable
        protected String webAppName;
        @Nullable
        protected String contextRoot;
        protected IWebSphereRole appServerRole;
        protected final ITasResolver tasResolver;
        @Nullable
        protected String version;

        public Builder(String roleId, ITasResolver tasResolver) {
            super(roleId);
            this.tasResolver = tasResolver;
        }

        @Override
        protected WebAppWebSphereRole.Builder builder() {
            return this;
        }

        @Override
        public WebAppWebSphereRole.Builder artifact(ITasArtifact artifact) {
            Args.notNull(artifact, "Tas artifact");
            this.webAppArtifact = artifact.getArtifact();
            return this.builder();
        }

        @Override
        public WebAppWebSphereRole.Builder contextName(String contextName) {
            this.contextName = contextName;
            return this.builder();
        }

        public WebAppWebSphereRole.Builder webAppName(String webAppName) {
            Args.notNull(webAppName, "Web App Name");
            this.webAppName = webAppName;
            return this.builder();
        }

        public WebAppWebSphereRole.Builder contextRoot(String contextRoot) {
            Args.notNull(contextRoot, "Context root");
            this.contextRoot = contextRoot;
            return this.builder();
        }

        public WebAppWebSphereRole.Builder appServerRole(IWebSphereRole appServerRole) {
            Args.notNull(appServerRole, "App server role");
            this.appServerRole = appServerRole;
            return this.builder();
        }

        @Override
        public WebAppWebSphereRole build() {
            return (WebAppWebSphereRole) super.build();
        }

        @Override
        protected WebAppWebSphereRole getInstance() {
            return new WebAppWebSphereRole(this);
        }

        @Override
        protected void initialize() {
            super.initialize();
            this.initFileName();
            this.initWebAppName();
            this.initContextRoot();
            this.initDeployContext();
            this.initInstallContext();
        }

        protected void initFileName() {
            assert this.webAppArtifact != null;
            assert this.contextName != null;
            this.fileName = this.contextName + "." + this.webAppArtifact.getExtension();
        }

        protected void initWebAppName() {
            assert this.contextName != null;
            if (this.webAppName == null) {
                this.webAppName = this.contextName;
            }
        }

        protected void initContextRoot() {
            assert this.contextName != null;
            if (this.contextRoot == null) {
                this.contextRoot = this.contextName;
            }
        }

        protected void initDeployContext() {
            URL artifactUrl = this.tasResolver.getArtifactUrl(this.webAppArtifact);
            this.genericContextBuilder = (new GenericFlowContext.Builder(artifactUrl)).notArchive();
        }

        protected void initInstallContext() {
            this.runCommandFlowContextBuilder =
                new RunCommandFlowContext.Builder(this.getWsAdminFileName());
        }

        protected String getWsAdminFileName() {
            return "wsadmin.bat";
        }
    }
}
