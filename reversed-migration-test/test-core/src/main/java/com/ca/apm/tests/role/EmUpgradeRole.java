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

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.tests.flow.UpgradeEMFlow;
import com.ca.apm.tests.flow.UpgradeEMFlowContext;
import com.ca.tas.AutoSerializable;
import com.ca.tas.annotation.SerializableBuilder;
import com.ca.tas.annotation.SerializableBuilderFactory;
import com.ca.tas.annotation.SerializableBuilderSetter;
import com.ca.tas.annotation.SerializableBuilderSetterAttr;
import com.ca.tas.annotation.TasDocRole;
import com.ca.tas.annotation.TasEnvironmentPropertyKey;
import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.built.IntroscopeInstaller;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.type.Platform;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform.WINDOWS_AMD_64;

/**
 * Upgrades Enterprise Manager and WebView on Windows with given parameters and starts them.
 * Use {@link Builder} to configure the installation
 *
 * @author pojja01@ca.com
 * @version $Id: $Id
 */
@TasDocRole(platform = {Platform.LINUX, Platform.WINDOWS})
public class EmUpgradeRole extends AbstractRole implements AutoSerializable {

    public static final String ENV_UPGRADE_START = "START_UPGRADE";

    /**
     * Constant <code>ENV_PROPERTY_INSTALL_DIR="installDir"</code>
     */
    @TasEnvironmentPropertyKey
    public static final String ENV_PROPERTY_INSTALL_DIR = "installDir";

    @NotNull
    private final UpgradeEMFlowContext upgradeContext;
    private final int installTimeout;
    private final long startTimeout;
    private final String platform;
    private final boolean execute;

    /**
     * <p>Constructor for EmRole.</p>
     *
     * @param builder a {@link Builder} object.
     */
    protected EmUpgradeRole(final Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        this.upgradeContext = builder.upgradeEmFlowContext;
        this.installTimeout = builder.installTimeout;
        this.startTimeout = builder.startTimeout;
        this.platform = builder.platform;
        this.execute = builder.execute;
    }

    @Override
    public String getPlatformName() {
        return this.platform;
    }

    @Override
    public String getId() {
        return this.getRoleId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(final IAutomationAgentClient aaClient) {
        // run install
        if (execute)
            runFlow(aaClient, UpgradeEMFlow.class, this.upgradeContext, this.installTimeout);
    }

    /**
     * @return a {@link UpgradeEMFlowContext} object.
     */
    public UpgradeEMFlowContext getUpgradeEmFlowContext() {
        return this.upgradeContext;
    }


    /**
     * <p>Getter for the field <code>installTimeout</code>.</p>
     *
     * @return a int.
     */
    public int getInstallTimeout() {
        return this.installTimeout;
    }

    /**
     * <p>Getter for the field <code>startTimeout</code>.</p>
     *
     * @return a int.
     */
    public int getStartTimeout() {
        return (int) this.startTimeout;
    }

    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(final String roleId, final ITasResolver tasResolver) {
            super(roleId, tasResolver);
            this.upgradeEmFlowContextBuilder = new UpgradeEMFlowContext.LinuxBuilder();
            this.introscopePlatform = ArtifactPlatform.LINUX_AMD_64;
        }

        @Override
        protected LinuxBuilder builder() {
            return this;
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    /**
     * Builds instance of {@link EmUpgradeRole} with given parameters for EM installation
     *
     * @author turyu01
     * @author pojja01
     */
    @SerializableBuilder
    public static class Builder extends BuilderBase<Builder, EmUpgradeRole> {

        protected final ITasResolver tasResolver;
        protected String roleId;
        // optional
        protected UpgradeEMFlowContext.Builder upgradeEmFlowContextBuilder;
        protected UpgradeEMFlowContext upgradeEmFlowContext;
        protected ArtifactPlatform introscopePlatform = WINDOWS_AMD_64;
        protected Artifact introscopeArtifact;
        @Nullable
        protected String instroscopeVersion;

        protected int installTimeout;
        protected int startTimeout;
        protected String platform = Platform.WINDOWS.name();
        protected boolean execute = true;

        public Builder(final String roleId, final ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.upgradeEmFlowContextBuilder = new UpgradeEMFlowContext.Builder();
        }

        /**
         * Factory used for deserialization based on type
         *
         * @param platform OS platform
         * @param roleId   ROle ID
         * @return Platform specific builder
         */
        @SerializableBuilderFactory
        public static Builder fromPlatform(final Platform platform, final String roleId, final ITasResolver resolver) {
            switch (platform) {
                case LINUX:
                case SOLARIS:
                case AIX:
                case MAC:
                    return new LinuxBuilder(roleId, resolver);
                default:
                    return new Builder(roleId, resolver);
            }
        }

        /**
         * Builds instance of {@link EmUpgradeRole}
         */
        @Override
        public EmUpgradeRole build() {

            verifyTimeouts();
            initIntroscopeArtifact();
            initUpgradeContext();
            initEnvProperties();

            final EmUpgradeRole emRole = getInstance();
            Args.notNull(emRole.upgradeContext, "EM upgrade flow context");

            return emRole;
        }

        protected void verifyTimeouts() {
            if ((this.startTimeout != 0) && (this.installTimeout != this.startTimeout)) {
                throw new IllegalStateException("Use noTimeout OR installTimeout not both of them at one time.");
            }
        }

        protected void initIntroscopeArtifact() {
            if (this.introscopeArtifact != null) {
                return;
            }
            this.introscopeArtifact = new IntroscopeInstaller(this.introscopePlatform, this.tasResolver).createArtifact(this.instroscopeVersion).getArtifact();
        }

        protected void initEnvProperties() {
            assert this.tasResolver != null;
            assert this.upgradeEmFlowContext != null;

            final RolePropertyContainer envProperties = getEnvProperties();
            envProperties.add(ENV_PROPERTY_INSTALL_DIR, this.upgradeEmFlowContext.getOlderEmInstallDir());
            for (final Map.Entry<String, String> installerProperty : this.upgradeEmFlowContext.getInstallerProperties().entrySet()) {
                envProperties.add(installerProperty.getKey(), installerProperty.getValue());
            }
        }

        @Override
        protected EmUpgradeRole getInstance() {
            return new EmUpgradeRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        protected void initUpgradeContext() {
            assert this.introscopeArtifact != null : "Missing introscope artifact";

            this.upgradeEmFlowContextBuilder.introscopeVersion(this.introscopeArtifact.getVersion());
            this.upgradeEmFlowContextBuilder.introscopeUrl(this.tasResolver.getArtifactUrl(this.introscopeArtifact));

            this.upgradeEmFlowContext = this.upgradeEmFlowContextBuilder.build();

            getEnvProperties().add(ENV_UPGRADE_START, upgradeEmFlowContext);
        }

        public Builder introscopePlatform(final ArtifactPlatform introscopePlatform) {
            this.introscopePlatform = introscopePlatform;
            return builder();
        }

        public Builder introscopeArtifact(final Artifact introscopeArtifact) {
            this.introscopeArtifact = introscopeArtifact;
            return builder();
        }

        public Builder version(@NotNull final String version) {
            Args.notNull(version, "Version");
            this.instroscopeVersion = version;
            return this;
        }

        public Builder version(final IArtifactVersion version) {
            this.instroscopeVersion = version.getValue();
            return builder();
        }

        @SerializableBuilderSetter
        public Builder olderEmInstallDir(@SerializableBuilderSetterAttr("olderEmInstallDir") final String olderEmInstallDir) {
            this.upgradeEmFlowContextBuilder.olderEmInstallDir(olderEmInstallDir);
            return builder();
        }


        public Builder installerProperty(final String key, final String value) {
            this.upgradeEmFlowContextBuilder.installerProp(key, value);
            return builder();
        }

        @SerializableBuilderSetter
        public Builder useOracle() {
            this.upgradeEmFlowContextBuilder.useOracle();
            return builder();
        }

        @SerializableBuilderSetter
        public Builder dbhost(@SerializableBuilderSetterAttr("dbhost") String dbhost) {
            this.upgradeEmFlowContextBuilder.dbhost(dbhost);
            return builder();
        }

        public Builder dbport(int dbport) {
            this.upgradeEmFlowContextBuilder.dbport(dbport);
            return builder();
        }

        @SerializableBuilderSetter
        public Builder dbname(@SerializableBuilderSetterAttr("dbname") String dbname) {
            this.upgradeEmFlowContextBuilder.dbname(dbname);
            return builder();
        }

        @SerializableBuilderSetter
        public Builder caEulaPath(@SerializableBuilderSetterAttr("caEulaPath") String caEulaPath) {
            this.upgradeEmFlowContextBuilder.caEulaPath(caEulaPath);
            return builder();
        }

        @SerializableBuilderSetter
        public Builder sampleResponseFile(@SerializableBuilderSetterAttr("sampleResponseFile") String sampleResponseFile) {
            this.upgradeEmFlowContextBuilder.sampleResponseFile(sampleResponseFile);
            return builder();
        }

        @SerializableBuilderSetter
        public Builder dbuser(@SerializableBuilderSetterAttr("user") String dbuser) {
            this.upgradeEmFlowContextBuilder.dbuser(dbuser);
            return builder();
        }

        @SerializableBuilderSetter
        public Builder dbpassword(@SerializableBuilderSetterAttr("dbpassword") String dbpassword) {
            this.upgradeEmFlowContextBuilder.dbpassword(dbpassword);
            return builder();
        }

        @SerializableBuilderSetter
        public Builder dbAdminUser(@SerializableBuilderSetterAttr("dbAdminUser") String dbAdminUser) {
            this.upgradeEmFlowContextBuilder.dbAdminUser(dbAdminUser);
            return builder();
        }

        @SerializableBuilderSetter
        public Builder dbAdminPassword(@SerializableBuilderSetterAttr("dbAdminPassword") String dbAdminPassword) {
            this.upgradeEmFlowContextBuilder.dbAdminPassword(dbAdminPassword);
            return builder();
        }

        @SerializableBuilderSetter
        public Builder databaseDir(@SerializableBuilderSetterAttr("databaseDir") String databaseDir) {
            this.upgradeEmFlowContextBuilder.databaseDir(databaseDir);
            return builder();
        }

        /**
         * @param silentInstallChosenFeatures ie. ENTERPRISE_MANAGER,database
         */
        public Builder silentInstallChosenFeatures(final String silentInstallChosenFeatures) {
            Args.notBlank(silentInstallChosenFeatures, "Silent installer features");
            silentInstallChosenFeatures(Arrays.asList(silentInstallChosenFeatures.split(",")));
            return builder();
        }

        public Builder silentInstallChosenFeatures(final Collection<String> silentInstallChosenFeatures) {
            this.upgradeEmFlowContextBuilder.silentInstallChosenFeatures(silentInstallChosenFeatures);
            return builder();
        }

        public Builder nostartUpgrade() {
            this.execute = false;
            return builder();
        }

        /**
         * Sets no custom timeout on em installation, the time out is going to be determined by max polling agent timeout
         */
        public Builder noTimeout() {
            // Maximum value has to be lower than Int max, because of FlowExecutionBase works with
            // value in millis and after multiplying the value would overflow
            this.installTimeout = FlowConfigBuilder.MAX_TIMEOUT_FLAG;
            this.startTimeout = FlowConfigBuilder.MAX_TIMEOUT_FLAG;
            return builder();
        }

        /**
         * Sets EM installation timeout in custom units.
         *
         * @param timeout - timeout value in seconds
         */
        public Builder installTimeout(final int timeout, final TimeUnit unit) {
            installTimeout((int) unit.toSeconds(timeout));
            return builder();
        }

        /**
         * Sets EM installation timeout in seconds.
         *
         * @param timeout - timeout value in seconds
         */
        public Builder installTimeout(final int timeout) {
            Args.check(timeout > 0, "Timeout cannot be less or equal to 0");
            this.installTimeout = timeout;
            return builder();
        }
    }
}
