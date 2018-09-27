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

import com.ca.apm.automation.action.flow.mainframe.MqStartupFlow;
import com.ca.apm.automation.action.flow.mainframe.MqStartupFlowContext;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole.SysviewConfig;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * Role to bring up an already installed Websphere MQ instance on z/OS.
 */
public class MqZosRole extends AbstractRole {
    private final MqStartupFlowContext startupContext;

    private MqZosRole(Builder builder) {
        super(builder.roleId);

        MqStartupFlowContext.Builder contextBuilder = new MqStartupFlowContext
            .Builder(builder.queueManagerName)
            .sysviewLoadlib(builder.sysviewLoadlib);
        if (builder.onlyVerify) {
            contextBuilder.onlyVerify();
        }
        startupContext = contextBuilder.build();
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, MqStartupFlow.class, startupContext);
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link MqZosRole}.
     *
     * <p>By default the built role only verifies the deployment, if you wish to deploy it call the
     * {@link #deployRole()} method.
     */
    public static class Builder extends BuilderBase<Builder, MqZosRole> {
        private final String roleId;
        private final String queueManagerName;
        private String sysviewLoadlib = null;
        private boolean onlyVerify = true;

        /**
         * Constructor.
         *
         * @param roleId Id of the role.
         * @param queueManagerName Name of the MQ queue manager to deploy.
         */
        public Builder(String roleId, String queueManagerName) {
            Args.notNull(queueManagerName, "queueManagerName");
            Args.check(queueManagerName.length() >= 1 && queueManagerName.length() <= 4,
                "Queue manager name has to be between 1 and 4 characters long.");

            this.roleId = roleId;
            this.queueManagerName = queueManagerName;
        }

        /**
         * Constructor using predefined configuration.
         *
         * @param config Known MQ configuration.
         */
        public Builder(MqZosConfig config) {
            this(config.getRoleId(), config.getQueueManagerName());
            sysviewLoadlib(config.getSysviewLoadlib());
        }

        /**
         * Sets an explicit SYSVIEW load library to be used.
         *
         * @param sysviewLoadlib Load library for the SYSVIEW instance to use.
         * @return Builder instance the method was called on.
         */
        public Builder sysviewLoadlib(String sysviewLoadlib) {
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
        public MqZosRole build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected MqZosRole getInstance() {
            return new MqZosRole(this);
        }
    }

    /**
     * Default configurations for MQ role.
     */
    public enum MqZosConfig {
        CSQ4("CSQ4", "USILCA31", 4404, SysviewConfig.WILY_14_0.getLoadlib()),
        ;

        private final String queueManagerName;
        private final String host;
        private final int port;
        private final String loadlib;

        /**
         * Configuration for MQ role.
         *
         * @param queueManagerName Name of the MQ queue manager to deploy.
         * @param host MQ host.
         * @param port MQ port.
         * @param loadlib Sysview load library.
         */
        MqZosConfig(String queueManagerName, String host, int port, String loadlib) {
            this.host = host;
            this.port = port;
            this.loadlib = loadlib;
            this.queueManagerName = queueManagerName;
        }

        public String getSysviewLoadlib() {
            return loadlib;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getQueueManagerName() {
            return queueManagerName;
        }

        public String getRoleId() {
            return "mq" + CommonUtils.constantToCamelCase(name()) + "Role";
        }

        @Override
        public String toString() {
            return "MQ " + queueManagerName;
        }
    }
}
