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

package com.ca.apm.role;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.linux.YumInstallPackageRole;

/**
 * ApacheRole class.
 *
 * Deploys Apache.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public class ApacheRole extends AbstractRole {

    /** Constant <code>APACHE_START="apacheStart"</code> */
    public static final String APACHE_START = "apacheStart";
    /** Constant <code>APACHE_STOP="apacheStop"</code> */
    public static final String APACHE_STOP = "apacheStop";

    private final YumInstallPackageRole yumInstallApacheRole;
    private final RunCommandFlowContext stopCommandContext;
    private final RunCommandFlowContext startCommandContext;
    private final ConfigureFlowContext configureApacheContext;
    private final boolean autoStart;

    /**
     * <p>
     * Constructor for ApacheRole.
     * </p>
     *
     * @param builder Builder object containing all necessary data
     */
    protected ApacheRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        yumInstallApacheRole = builder.yumInstallApacheRole;
        startCommandContext = builder.startApacheContext;
        stopCommandContext = builder.stopApacheContext;
        configureApacheContext = builder.configureApacheContext;
        autoStart = builder.autoStart;
    }

    /** {@inheritDoc} */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        installApache(aaClient);
        configureApache(aaClient);

        if (autoStart) {
            start(aaClient);
        }
    }

    /**
     * <p>
     * installApache.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void installApache(IAutomationAgentClient aaClient) {
        if (yumInstallApacheRole == null) {
            return;
        }

        assert getHostingMachine() != null;

        getHostingMachine().addRole(yumInstallApacheRole);
        yumInstallApacheRole.deploy(aaClient);
    }

    /**
     * <p>
     * configureApache.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void configureApache(IAutomationAgentClient aaClient) {
        if (configureApacheContext == null) {
            return;
        }

        runFlow(aaClient, ConfigureFlow.class, configureApacheContext);
    }

    /**
     * <p>
     * start.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    public void start(IAutomationAgentClient aaClient) {
        runCommandFlow(aaClient, startCommandContext);
    }

    /**
     * <p>
     * stop.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    public void stop(IAutomationAgentClient aaClient) {
        runCommandFlow(aaClient, stopCommandContext);
    }

    /**
     * <p>
     * isAutoStart.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * <p>
     * Getter for the field <code>startCommandContext</code>.
     * </p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext} object.
     */
    public RunCommandFlowContext getStartCommandContext() {
        return startCommandContext;
    }

    /**
     * <p>
     * Getter for the field <code>stopCommandContext</code>.
     * </p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext} object.
     */
    public RunCommandFlowContext getStopCommandContext() {
        return stopCommandContext;
    }

    /**
     * <p>
     * Getter for the field <code>yumInstallApacheRole</code>.
     * </p>
     *
     * @return a {@link com.ca.tas.role.linux.YumInstallPackageRole} object.
     */
    public YumInstallPackageRole getYumInstallApacheRole() {
        return yumInstallApacheRole;
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate
     * {@link ApacheRole}
     */
    public static class LinuxBuilder extends Builder {

        private static final String YUM_INSTALL_ROLE_ID = "yumInstallRole";
        private static final String HTTPD_PACKAGE = "httpd";
        private static final String LINUX_APACHE_CONFIG_FILE = "/etc/httpd/conf/httpd.conf";
        private static final String HTTPD_DEAMON_CMD = "httpd";

        public LinuxBuilder(String roleId) {
            super(roleId);
        }

        @Override
        public ApacheRole build() {
            // to get base builder verification
            super.build();

            initYumInstallRole();

            ApacheRole role = getInstance();
            Args.notNull(role.yumInstallApacheRole, "Apache yum install role for linux builder");

            return role;
        }

        protected void initYumInstallRole() {
            yumInstallApacheRole =
                new YumInstallPackageRole.Builder(depRoleId(roleId, YUM_INSTALL_ROLE_ID)).addPackage(HTTPD_PACKAGE)
                    .build();
        }

        @Override
        protected void initStartStopCommands() {
            startApacheContext = getCommand("start", "apache-start");
            getEnvProperties().add(APACHE_START, startApacheContext);
            stopApacheContext = getCommand("stop", "apache-stop");
            getEnvProperties().add(APACHE_STOP, stopApacheContext);
        }

        protected RunCommandFlowContext getCommand(String command, String name) {
            return new RunCommandFlowContext.Builder("service")
                .args(Arrays.asList(HTTPD_DEAMON_CMD, command)).name(name).build();
        }

        @Override
        protected void initApacheConfigureFlow() {
            if (apacheConfigurationProps.isEmpty()) {
                return;
            }
            configureApacheContext =
                new ConfigureFlowContext.Builder().configurationMap(LINUX_APACHE_CONFIG_FILE,
                    apacheConfigurationProps).build();
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
     * Builder responsible for holding all necessary properties to instantiate {@link ApacheRole}
     */
    public static class Builder extends BuilderBase<Builder, ApacheRole> {

        protected final String roleId;
        protected final Map<String, String> apacheConfigurationProps = new HashMap<>();

        protected YumInstallPackageRole yumInstallApacheRole;
        protected RunCommandFlowContext startApacheContext;
        protected RunCommandFlowContext stopApacheContext;
        protected ConfigureFlowContext configureApacheContext;
        protected boolean autoStart;

        public Builder(String roleId) {
            this.roleId = roleId;
        }

        @Override
        public ApacheRole build() {
            initStartStopCommands();
            initApacheConfigureFlow();

            return getInstance();
        }

        protected void initApacheConfigureFlow() {
            // todo
        }

        protected void initStartStopCommands() {
            // todo
        }

        @Override
        protected ApacheRole getInstance() {
            return new ApacheRole(this);
        }

        public Builder autoStart() {
            autoStart = true;
            return builder();
        }

        public Builder configureApache(String key, String value) {
            apacheConfigurationProps.put(key, value);

            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }

    }
}
