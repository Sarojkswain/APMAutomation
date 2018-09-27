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

package com.ca.apm.powerpack.sysview.tests.role;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.mainframe.SysvDb2StartupFlow;
import com.ca.apm.automation.action.flow.mainframe.SysvDb2StartupFlowContext;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * Role to bring up an already installed SYSVIEW for DB2 (SYSVDB2) instance on z/OS.
 * This role can start work with instances as defined <a href='http://ca31:6600'>here</a>.
 */
public class SysvDb2Role extends AbstractRole {
    private final SysvDb2StartupFlowContext startupContext;

    private SysvDb2Role(Builder builder) {
        super(builder.roleId);

        SysvDb2StartupFlowContext.Builder contextBuilder = new SysvDb2StartupFlowContext
            .Builder(builder.version, builder.propertiesFilePath)
            .sysviewLoadlib(builder.sysviewLoadlib);
        if (builder.subsystem != null) {
            contextBuilder.requireSubsystem(builder.subsystem);
        }
        if (builder.onlyVerify) {
            contextBuilder.onlyVerify();
        }
        startupContext = contextBuilder.build();
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, SysvDb2StartupFlow.class, startupContext);
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link SysvDb2Role}.
     *
     * <p>By default the built role only verifies the deployment, if you wish to deploy it call the
     * {@link #deployRole()} method.
     */
    public static class Builder extends BuilderBase<Builder, SysvDb2Role> {
        private final String roleId;
        private final String version;
        private String subsystem = null;
        private final String propertiesFilePath;
        private String sysviewLoadlib = null;
        private boolean onlyVerify = true;

        /**
         * Constructor.
         *
         * @param roleId Id of the role.
         * @param version Required SYSVDB2 version.
         * @param propertiesFilePath Path to file where the connection properties of the instance
         *        will be saved.
         */
        public Builder(String roleId, String version, String propertiesFilePath) {
            Args.notBlank(roleId, "roleId");
            Args.notBlank(version, "version");
            Args.notBlank(propertiesFilePath, "propertiesFilePath");

            this.roleId = roleId;
            this.version = version;
            this.propertiesFilePath = propertiesFilePath;
        }

        /**
         * Constructor using predefined configuration.
         *
         * @param config Known SYSVDB2 configuration.
         */
        public Builder(SysvDb2Config config) {
            Args.notNull(config, "config");

            roleId = config.getRoleId();
            version = config.getVersion();
            propertiesFilePath = config.getPropertiesFile();
            requireSubsystem(config.getSubsystem());
        }

        /**
         * Specifies a DB2 subsystem that needs to be monitored by the chosen SYSVDB2 instance.
         * Subsequent calls of this method will overwrite previous ones.
         *
         * @param subsystem DB2 subsystem to be monitored.
         * @return Builder instance the method was called on.
         */
        public Builder requireSubsystem(String subsystem) {
            Args.notBlank(subsystem, "subsystem");

            this.subsystem = subsystem;
            return builder();
        }

        /**
         * Sets an explicit SYSVIEW load library to be used.
         *
         * @param sysviewLoadlib Load library for the SYSVIEW instance to use.
         * @return Builder instance the method was called on.
         */
        public Builder sysviewLoadlib(String sysviewLoadlib) {
            Args.notBlank(sysviewLoadlib, "sysviewLoadlib");

            this.sysviewLoadlib = sysviewLoadlib;
            return builder();
        }

        /**
         * Do not just verify the role, deploy it.
         *
         * @return Builder instance the method was called on.
         */
        public Builder deployRole() {
            onlyVerify = false;
            return builder();
        }

        /**
         * Builds an instance of the role based on the provided parameters.
         *
         * @return Role instance.
         */
        @Override
        public SysvDb2Role build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected SysvDb2Role getInstance() {
            return new SysvDb2Role(this);
        }
    }

    /**
     * Known Sysview for DB2 configurations.
     */
    public enum SysvDb2Config {
        D10A_18_0("18.0", "D10A"),
        ;

        private static final String SYSVDB2_PROPERTIES_FILE = "/tmp/sysvdb2.properties";
        private final String version;
        private final String subsystem;

        SysvDb2Config(String version, String subsystem) {
            this.version = version;
            this.subsystem = subsystem;
        }

        public String getVersion() {
            return version;
        }

        public String getSubsystem() {
            return subsystem;
        }

        public String getPropertiesFile() {
            return SYSVDB2_PROPERTIES_FILE;
        }

        public String getRoleId() {
            return "sysvdb2" + CommonUtils.constantToCamelCase(name()) + "Role";
        }

        @Override
        public String toString() {
            return "SYSVDB2 " + version + " (" + subsystem + ")";
        }
    }
}
