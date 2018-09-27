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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.flow.webapp.websphere.ConfigureWebSphereAgentFlow;
import com.ca.apm.automation.action.flow.webapp.websphere.ConfigureWebSphereAgentFlowContext;
import com.ca.apm.automation.utils.archive.TasArchiveFactory;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * WebSphere AS deployment on distributed platform.
 * Example of usage: <BR>
 * Was8Role.Builder(...).wasVersion(version).config(configResource).build()
 *
 * @author hecka01@ca.com
 */
public class Was8Role extends AbstractRole {

    private static final String WIN_SEPARATOR = "\\";
    private static final String WIN_SOFTWARE_LOC = "C:" + WIN_SEPARATOR + "automation"
        + WIN_SEPARATOR + "deployed" + WIN_SEPARATOR;

    private static final String IMCL_LOCATION_WIN =
        "C:\\Program Files (x86)\\IBM\\Installation Manager\\eclipse\\tools";
    private static final String BASE_DIR = WIN_SOFTWARE_LOC + "IBM" + WIN_SEPARATOR;
    private static final String IM_DIR = "install_im";
    private static final String WAS_TEMP_DIR = "install_was";
    private static final String WAS_DIR = "WAS8";

    private static final String BIN = WIN_SEPARATOR + "bin";
    private static final String PROFILE_TEMPLATE = WIN_SEPARATOR + "profileTemplates"
        + WIN_SEPARATOR + "default";
    private static final String PROFILE_NAME = "AppSrv01";
    private static final String PROFILE_PATH = WIN_SEPARATOR + "profiles" + WIN_SEPARATOR
        + PROFILE_NAME;
    private static final String PROFILE_BIN = PROFILE_PATH + BIN;

    private static final String IM_LOCATION = BASE_DIR + IM_DIR;
    private static final String WAS_TEMP_LOCATION = BASE_DIR + WAS_TEMP_DIR;
    static final String WAS_LOCATION = BASE_DIR + WAS_DIR;
    static final String WAS_BIN = WAS_LOCATION + BIN;


    // TODO: You can autodetect package name by:
    // "imcl listAvailablePackages -repositories <unpackedWAS>"
    private String packageId = "com.ibm.websphere.EXPRESS.v85_8.5.5000.20130514_1044";
    // Currently works only with V8.5.5. Note that V8.5 has incompatible coordinates in artifactory.



    private URL imUrl;
    private Set<URL> wasUrls = new LinkedHashSet<>();
    private String configResourcePath;

    protected final TasArchiveFactory archiveFactory = new TasArchiveFactory();

    protected Was8Role(Builder builder) {
        super(builder.roleId);
    }

    /**
     * Deploys WebSphere using IBM Installation Manager and configures it.
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        unpackInstallationManager(aaClient);
        unpackWebsphereApplicationServer(aaClient);
        installInstallationManager(aaClient);
        installWebsphereApplicationServer(aaClient);
        createProfile(aaClient);

        startWas(aaClient);
        if (configResourcePath != null) {
            configureWas(aaClient, configResourcePath);
        }
    }

    /**
     * Unpacks IBM Installation Manager installer.
     */
    protected void unpackInstallationManager(IAutomationAgentClient aaClient) {
        GenericFlowContext ctx =
            new GenericFlowContext.Builder(imUrl).destination(IM_LOCATION).build();
        runFlow(aaClient, GenericFlow.class, ctx);
    }

    /**
     * Unpacks WebSphere packages to single common location.
     */
    protected void unpackWebsphereApplicationServer(IAutomationAgentClient aaClient) {
        GenericFlowContext ctx;

        for (URL wasUrl : wasUrls) {
            ctx = new GenericFlowContext.Builder(wasUrl).destination(WAS_TEMP_LOCATION).build();
            runFlow(aaClient, GenericFlow.class, ctx);
        };
    }

    /**
     * Installs IBM Installation Manager.
     */
    protected void installInstallationManager(IAutomationAgentClient aaClient) {

        String[] args = {"-acceptLicense", "-log", "im_install.log"};
        RunCommandFlowContext cmdCtx =
            new RunCommandFlowContext.Builder("installc").workDir(IM_LOCATION)
                .args(Arrays.asList(args)).name(getRoleId()).build();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdCtx, getHostWithPort()));
    }

    /**
     * Installs WebSphere using Installation Manager.
     */
    protected void installWebsphereApplicationServer(IAutomationAgentClient aaClient) {
        String[] args =
            {"install", packageId, "-repositories", WAS_TEMP_LOCATION, "-installationDirectory",
                    WAS_LOCATION, "-acceptLicense", "-log", "was_install.xml"};
        RunCommandFlowContext cmdCtx =
            new RunCommandFlowContext.Builder("imcl").workDir(IMCL_LOCATION_WIN)
                .args(Arrays.asList(args)).name(getRoleId()).build();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdCtx, getHostWithPort()));
    }

    /**
     * Creates default WebSphere profile (so can be started).
     */
    protected void createProfile(IAutomationAgentClient aaClient) {
        String[] args =
            {"-create", "-templatePath", ".." + PROFILE_TEMPLATE, "-profileName", PROFILE_NAME,
                    "-profilePath", ".." + PROFILE_PATH, "-isDefault"};
        RunCommandFlowContext cmdCtx =
            new RunCommandFlowContext.Builder("manageprofiles.bat").workDir(WAS_BIN)
                .args(Arrays.asList(args)).name(getRoleId()).build();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdCtx, getHostWithPort()));
    }

    public String getInstallDir() {
        return WAS_LOCATION;
    }

    public String getBinDir() {
        return WAS_BIN;
    }

    /**
     * Starts WebSphere
     *
     * @param aaClient Automation agent.
     */
    public void startWas(IAutomationAgentClient aaClient) {
        String[] args = {"server1"};
        RunCommandFlowContext cmdCtx =
            new RunCommandFlowContext.Builder("startServer.bat")
                .workDir(WAS_LOCATION + PROFILE_BIN).args(Arrays.asList(args)).name(getRoleId())
                .build();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdCtx, getHostWithPort()));
    }

    /**
     * Stops WebSphere
     *
     * @param aaClient Automation agent.
     */
    public void stopWas(IAutomationAgentClient aaClient) {
        String[] args = {"server1"};
        RunCommandFlowContext cmdCtx =
            new RunCommandFlowContext.Builder("stopServer.bat").workDir(WAS_LOCATION + PROFILE_BIN)
                .args(Arrays.asList(args)).name(getRoleId()).build();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdCtx, getHostWithPort()));
    }

    /**
     * Imports WebSphere configuration (*.properties)
     *
     * @param aaClient Automation agent.
     * @param configPath Path to the configuration file resource.
     */
    public void configureWas(IAutomationAgentClient aaClient, String configPath) {
        String targetPath = WAS_TEMP_LOCATION + WIN_SEPARATOR + "was.properties";

        runFlow(aaClient, FileModifierFlow.class,
            new FileModifierFlowContext.Builder().resource(targetPath, configPath).build());

        String[] args =
            {
                    "-lang",
                    "jython",
                    "-c",
                    "\"AdminTask.applyConfigProperties('-propertiesFileName "
                        + targetPath.replace("\\", "\\\\") + " -reportFileName wasadmin_report.txt"
                        + "')\""};
        RunCommandFlowContext cmdCtx =
            new RunCommandFlowContext.Builder("wsadmin").workDir(WAS_BIN)
                .args(Arrays.asList(args)).name(getRoleId()).build();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdCtx, getHostWithPort()));
    }

    /**
     * Instruments APM agent to WAS jvm arguments
     * Agent is expected to be deployed in default location: WAS_LOCATION\wily
     * (as hardcoded in TAS flow)
     *
     * @param aaClient Automation agent.
     */
    public void configureAgent(IAutomationAgentClient aaClient) {
        ConfigureWebSphereAgentFlowContext configureAgentFlowContext =
            new ConfigureWebSphereAgentFlowContext.Builder().websphereHostname(getHostingMachine().getHostname())
                .webSphereDirectory(getInstallDir()).build();

        aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(ConfigureWebSphereAgentFlow.class,
            configureAgentFlowContext, getHostWithPort()));
    }

    public static class Builder extends BuilderBase<Builder, Was8Role> {
        private final String roleId;
        private final ITasResolver tasResolver;
        private String wasVersion = "8.5.5";
        private String configResourcePath;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public Was8Role build() {
            Was8Role role = getInstance();
            role.configResourcePath = configResourcePath;

            role.imUrl =
                tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.ibm",
                    "InstalMgr", "WIN_WAS_8.5", "zip", "1.5.2"));

            Set<URL> wasUrls = new LinkedHashSet<>();
            for (int i = 1; i <= 3; i++) {
                wasUrls.add(tasResolver.getArtifactUrl(new DefaultArtifact(
                    "com.ca.apm.binaries.ibm", "was", "part" + i, "zip", wasVersion)));
            };
            role.wasUrls = wasUrls;

            return role;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected Was8Role getInstance() {
            return new Was8Role(this);
        }

        /**
         * Version to deploy
         *
         * @param version WAS version to use.
         * @return Builder instance the method was called on.
         */
        public Builder wasVersion(String version) {
            this.wasVersion = version;
            return builder();
        }

        /**
         * WAS properties file (resource path).
         * Pay attention to proper file structure (SubSection header and other sections).
         *
         * @param configResourcePath Path to the WAS configuration properties file.
         * @return Builder instance the method was called on.
         */
        public Builder config(String configResourcePath) {
            this.configResourcePath = configResourcePath;
            return builder();
        }

    }
}
