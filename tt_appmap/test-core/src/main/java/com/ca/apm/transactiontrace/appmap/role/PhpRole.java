/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.transactiontrace.appmap.role;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.linux.YumInstallPackageRole;

/**
 * PhpRole class.
 *
 * Deploys PHP on top of already existing Apache.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public class PhpRole extends AbstractRole {

    private final YumInstallPackageRole yumInstallPhpRole;
    private final ConfigureFlowContext configurePhpContext;

    /**
     * <p>
     * Constructor for PhpRole.
     * </p>
     *
     * @param builder Builder object containing all necessary data
     */
    protected PhpRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        yumInstallPhpRole = builder.yumInstallPhpRole;
        configurePhpContext = builder.configurePhpContext;
    }

    /** {@inheritDoc} */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        installPhp(aaClient);
        configurePhp(aaClient);
    }

    /**
     * <p>
     * installPhp.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void installPhp(IAutomationAgentClient aaClient) {
        if (yumInstallPhpRole == null) {
            return;
        }

        assert getHostingMachine() != null;

        getHostingMachine().addRole(yumInstallPhpRole);
        yumInstallPhpRole.deploy(aaClient);
    }

    /**
     * <p>
     * configurePhp.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void configurePhp(IAutomationAgentClient aaClient) {
        if (configurePhpContext == null) {
            return;
        }

        runFlow(aaClient, ConfigureFlow.class, configurePhpContext);
    }

    /**
     * <p>
     * Getter for the field <code>yumInstallPhpRole</code>.
     * </p>
     *
     * @return a {@link com.ca.tas.role.linux.YumInstallPackageRole} object.
     */
    public YumInstallPackageRole getYumInstallPhpRole() {
        return yumInstallPhpRole;
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate {@link PhpRole}
     */
    public static class LinuxBuilder extends Builder {

        private static final String YUM_INSTALL_ROLE_ID = "yumInstallRole";
        private static final String PHP_PACKAGE = "php";
        private static final String LINUX_PHP_CONFIG_FILE = "/etc/httpd/conf.d/php.conf";

        public LinuxBuilder(String roleId) {
            super(roleId);
        }

        @Override
        public PhpRole build() {
            // to get base builder verification
            super.build();

            initYumInstallRole();

            PhpRole role = getInstance();
            Args.notNull(role.yumInstallPhpRole, "PHP yum install role for linux builder");

            return role;
        }

        protected void initYumInstallRole() {
            YumInstallPackageRole.Builder builder =
                new YumInstallPackageRole.Builder(depRoleId(roleId, YUM_INSTALL_ROLE_ID)).addPackage(PHP_PACKAGE);

            for (String aPackage : packages) {
                builder.addPackage(aPackage);
            }

            yumInstallPhpRole = builder.build();
        }


        @Override
        protected void initPhpConfigureFlow() {
            if (phpConfigurationProps.isEmpty()) {
                return;
            }
            configurePhpContext =
                new ConfigureFlowContext.Builder().configurationMap(LINUX_PHP_CONFIG_FILE,
                    phpConfigurationProps).build();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link PhpRole}
     */
    public static class Builder extends BuilderBase<Builder, PhpRole> {

        private static final String PHP_MYSQL_PACKAGE = "php-mysql";
        private static final String PHP_MCRYPT_PACKAGE = "php-mcrypt";
        private static final String PHP_XML_PACKAGE = "php-xml";
        private static final String PHP_GD_PACKAGE = "php-gd";

        protected final String roleId;
        protected final Map<String, String> phpConfigurationProps = new HashMap<>();

        protected YumInstallPackageRole yumInstallPhpRole;
        protected ConfigureFlowContext configurePhpContext;

        protected Set<String> packages = new HashSet<String>();

        public Builder(String roleId) {
            this.roleId = roleId;
        }

        @Override
        public PhpRole build() {
            initPhpConfigureFlow();

            return getInstance();
        }

        protected void initPhpConfigureFlow() {
            // todo
        }

        @Override
        protected PhpRole getInstance() {
            return new PhpRole(this);
        }

        public Builder configurePhp(String key, String value) {
            phpConfigurationProps.put(key, value);

            return builder();
        }

        public Builder withMySql() {
            packages.add(PHP_MYSQL_PACKAGE);

            return builder();
        }

        public Builder withMCrypt() {
            packages.add(PHP_MCRYPT_PACKAGE);

            return builder();
        }

        public Builder withXml() {
            packages.add(PHP_XML_PACKAGE);

            return builder();
        }

        public Builder withGd() {
            packages.add(PHP_GD_PACKAGE);

            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }

    }
}
