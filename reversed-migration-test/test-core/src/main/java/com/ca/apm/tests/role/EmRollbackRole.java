/*
 * Copyright (c) 2017 CA.  All rights reserved.
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

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.tests.flow.RollbackEMFlow;
import com.ca.apm.tests.flow.RollbackEMFlowContext;
import com.ca.tas.AutoSerializable;
import com.ca.tas.annotation.SerializableBuilder;
import com.ca.tas.annotation.SerializableBuilderFactory;
import com.ca.tas.annotation.SerializableBuilderSetter;
import com.ca.tas.annotation.SerializableBuilderSetterAttr;
import com.ca.tas.annotation.SerializableGetter;
import com.ca.tas.annotation.TasDocRole;
import com.ca.tas.annotation.TasEnvironmentPropertyKey;
import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.type.Platform;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

import static com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform.WINDOWS_AMD_64;

/**
 * Rollbacks Enterprise Manager and WebView on Windows with given parameters and starts them.
 * Use {@link EmRollbackRole.Builder} to configure the installation
 *
 * @author dugra04
 */
@TasDocRole(platform = {Platform.LINUX, Platform.WINDOWS})
public class EmRollbackRole extends AbstractRole implements AutoSerializable {
    public static final String ENV_ROLLBACK_START = "START_ROLLBACK";

    /**
     * Constant <code>ENV_PROPERTY_INSTALL_DIR="installDir"</code>
     */
    @TasEnvironmentPropertyKey
    public static final String ENV_PROPERTY_INSTALL_DIR = "installDir";

    @NotNull
    private final RollbackEMFlowContext rollbackContext;
    private final int installTimeout;
    private final long startTimeout;
    private final String platform;
    private final String instroscopeVersion;
    private boolean success;
    private final boolean allowFinishedRollback;
    private final boolean doCleanupOnly;
    private final boolean execute;

    /**
     * <p>Constructor for EmRole.</p>
     *
     * @param builder a {@link EmUpgradeRole.Builder} object.
     */
    protected EmRollbackRole(final EmRollbackRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        this.rollbackContext = builder.rollbackEmFlowContext;
        this.installTimeout = builder.installTimeout;
        this.startTimeout = builder.startTimeout;
        this.platform = builder.platform;
        this.instroscopeVersion = builder.instroscopeVersion;
        this.allowFinishedRollback = builder.allowFinishedRollback;
        this.doCleanupOnly = builder.doCleanupOnly;
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
        if (execute) {
            runFlow(aaClient, RollbackEMFlow.class, this.rollbackContext, this.installTimeout);
            this.success = true;
        }
    }

    @NotNull
    public RollbackEMFlowContext getRollbackEmFlowContext() {
        return rollbackContext;
    }

    public String getInstroscopeVersion() {
        return instroscopeVersion;
    }

    public int getInstallTimeout() {
        return installTimeout;
    }

    public long getStartTimeout() {
        return startTimeout;
    }

    @SerializableGetter("allowFinishedRollback")
    public boolean isAllowFinishedRollback() {
        return this.allowFinishedRollback;
    }

    @SerializableGetter("doCleanupOnly")
    public boolean isDoCleanupOnly() {
        return this.doCleanupOnly;
    }

    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(final String roleId, final ITasResolver tasResolver) {
            super(roleId, tasResolver);
            this.rollbackEmFlowContextBuilder = new RollbackEMFlowContext.LinuxBuilder();
            this.platform = Platform.LINUX.name();
            this.introscopePlatform = IBuiltArtifact.ArtifactPlatform.LINUX_AMD_64;
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
     * Builds instance of {@link EmRollbackRole} with given parameters for EM rollback
     *
     * @author dugra04
     */
    @SerializableBuilder
    public static class Builder extends BuilderBase<EmRollbackRole.Builder, EmRollbackRole> {

        protected final ITasResolver tasResolver;
        protected String roleId;
        // optional
        protected RollbackEMFlowContext.Builder rollbackEmFlowContextBuilder;
        protected RollbackEMFlowContext rollbackEmFlowContext;
        protected IBuiltArtifact.ArtifactPlatform introscopePlatform = WINDOWS_AMD_64;
        @Nullable
        private String instroscopeVersion;

        protected int installTimeout;
        protected int startTimeout;
        protected String platform = Platform.WINDOWS.name();
        private boolean allowFinishedRollback;
        private boolean doCleanupOnly;
        private boolean execute = true;

        public Builder(final String roleId, final ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.rollbackEmFlowContextBuilder = new RollbackEMFlowContext.Builder();
        }

        /**
         * Factory used for deserialization based on type
         *
         * @param resolver Host and URL resolver
         * @param platform OS platform
         * @param roleId   ROle ID
         * @return Platform specific builder
         */
        @SerializableBuilderFactory
        public static Builder fromPlatform(final ITasResolver resolver, final Platform platform, final String roleId) {
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

        @SerializableBuilderSetter
        public Builder olderEmInstallDir(@SerializableBuilderSetterAttr("olderEmInstallDir") final String olderEmInstallDir) {
            this.rollbackEmFlowContextBuilder.olderEmInstallDir(olderEmInstallDir);
            return builder();
        }

        @SerializableBuilderSetter("allowFinishedRollback")
        public Builder allowFinishedRollback() {
            this.rollbackEmFlowContextBuilder.allowFinishedRollback(true);
            this.allowFinishedRollback = true;

            return builder();
        }

        @SerializableBuilderSetter("doCleanupOnly")
        public Builder doCleanupOnly() {
            this.rollbackEmFlowContextBuilder.doCleanupOnly(true);
            this.doCleanupOnly = true;

            return builder();
        }

        public Builder version(@NotNull final String version) {
            Args.notNull(version, "Version");
            this.instroscopeVersion = version;
            return builder();
        }

        public Builder nostartRollback() {
            execute = false;
            return builder();
        }

        public Builder useReversedMigration() {
            this.rollbackEmFlowContextBuilder.useReversedMigration();
            return builder();
        }

        /**
         * Sets no custom timeout on em installation, the time out is going to be determined by max polling agent timeout
         */
        public Builder noTimeout() {
            // Maximum value has to be lower than Int max, because of FlowExecutionBase works with
            // value in millis and after multiplying the value would overflow
            this.installTimeout = FlowConfig.FlowConfigBuilder.MAX_TIMEOUT_FLAG;
            this.startTimeout = FlowConfig.FlowConfigBuilder.MAX_TIMEOUT_FLAG;
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

        /**
         * Builds instance of {@link EmUpgradeRole}
         */
        @Override
        public EmRollbackRole build() {
            verifyTimeouts();
            // initIntroscopeArtifact();
            initRollbackContext();
            initEnvProperties();

            final EmRollbackRole emRole = getInstance();
            Args.notNull(emRole.rollbackContext, "EM upgrade flow context");

            return emRole;
        }

        protected void verifyTimeouts() {
            if ((this.startTimeout != 0) && (this.installTimeout != this.startTimeout)) {
                throw new IllegalStateException("Use noTimeout OR installTimeout not both of them at one time.");
            }
        }

        protected void initEnvProperties() {
            assert this.tasResolver != null;
            assert this.rollbackEmFlowContext != null;

            final RolePropertyContainer envProperties = getEnvProperties();
            envProperties.add(ENV_PROPERTY_INSTALL_DIR, this.rollbackEmFlowContext.getOlderEmInstallDir());
        }

        @Override
        protected EmRollbackRole getInstance() {
            return new EmRollbackRole(this);
        }

        @Override
        protected Builder builder() {
            this.rollbackEmFlowContextBuilder.linux(platform.equalsIgnoreCase(Platform.LINUX.name()));
            return this;
        }

        protected void initRollbackContext() {
            this.rollbackEmFlowContext = this.rollbackEmFlowContextBuilder.build();

            getEnvProperties().add(ENV_ROLLBACK_START, rollbackEmFlowContext);

        }
    }
}