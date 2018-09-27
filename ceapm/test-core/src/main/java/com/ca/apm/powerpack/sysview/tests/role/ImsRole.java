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

import com.ca.apm.automation.action.flow.mainframe.ImsStartupFlow;
import com.ca.apm.automation.action.flow.mainframe.ImsStartupFlowContext;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole.MqZosConfig;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole.SysviewConfig;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

import org.apache.http.util.Args;

/**
 * Role to bring up an already installed IMS region on z/OS.
 * This role can start a region that follows the structure as described
 * <a href='https://cawiki.ca.com/x/r7DIKQ'>here</a>.
 */
public class ImsRole extends AbstractRole {
    private final ImsStartupFlowContext startupContext;

    private ImsRole(Builder builder) {
        super(builder.roleId);

        ImsStartupFlowContext.Builder contextBuilder = new ImsStartupFlowContext
            .Builder(builder.region, builder.version, builder.queueManagerName)
            .sysviewLoadlib(builder.sysviewLoadlib);

        if (!builder.startImsConnect) {
            contextBuilder.noImsConnect();
        }

        if (builder.onlyVerify) {
            contextBuilder.onlyVerify();
        }

        startupContext = contextBuilder.build();
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, ImsStartupFlow.class, startupContext);
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link ImsRole}.
     *
     * <p>By default the built role only verifies the deployment, if you wish to deploy it call the
     * {@link #deployRole()} method.
     */
    public static class Builder extends BuilderBase<Builder, ImsRole> {
        private final String roleId;
        private final String region;
        private final String version;
        private final String queueManagerName;
        private String sysviewLoadlib = null;
        private boolean startImsConnect;
        private boolean onlyVerify = true;

        /**
         * Constructor.
         *
         * @param roleId Id of the role.
         * @param region IMS region identifier.
         * @param version IMS region version.
         * @param queueManagerName Name of the MQ queue manager to deploy.
         */
        public Builder(String roleId, String region, String version, String queueManagerName) {
            Args.notBlank(roleId, "roleId");
            Args.notBlank(region, "region");
            Args.notBlank(version, "version");
            Args.notBlank(queueManagerName, "queueManagerName");

            this.roleId = roleId;
            this.region = region;
            this.version = version;
            this.queueManagerName = queueManagerName;
            startImsConnect = true;
        }

        /**
         * Constructor using predefined configuration.
         *
         * @param config Known IMS configuration.
         */
        public Builder(ImsConfig config) {
            this(config.getRoleId(), config.getRegion(), config.getVersion(), config.getMq()
                .getQueueManagerName());
            sysviewLoadlib(config.getSysviewLoadlib());
            if (!config.isStartImsConnect()) {
                noImsConnect();
            }
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
         * Prevent starting IMS Connect for this IMS region.
         *
         * @return Builder instance the method was called on.
         */
        public Builder noImsConnect() {
            startImsConnect = false;
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
        public ImsRole build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected ImsRole getInstance() {
            return new ImsRole(this);
        }
    }

    /**
     * Default configurations for IMS role.
     */
    public enum ImsConfig {
        SVPD("SVPD", "13", false, MqZosConfig.CSQ4, "WILY_IMS_QUEUE", "WILY_REPLY_QUEUE",
            SysviewConfig.SYSVIEW_DEVELOPMENT.getLoadlib()),
        SVPE("SVPE", "14", false, MqZosConfig.CSQ4, "WILY_IMS_QUEUE_A", "WILY_REPLY_QUEUE",
            SysviewConfig.WILY_14_1.getLoadlib()),
        ;

        private final String region;
        private final String version;
        private final boolean startImsConnect;
        public final MqZosConfig mq;
        private final String inputQueue;
        private final String replyQueue;
        private final String loadlib;

        /**
         * Configuration for IMS role.
         *
         * @param region IMS region identifier.
         * @param version IMS region version.
         * @param startImsConnect Start IMS connect region?
         * @param mq Associated MQ manager configuration.
         * @param inputQueue Input MQ queue.
         * @param replyQueue Reply MQ queue.
         * @param loadlib Sysview load library.
         */
        ImsConfig(String region, String version, boolean startImsConnect, MqZosConfig mq,
                  String inputQueue, String replyQueue, String loadlib) {
            this.region = region;
            this.version = version;
            this.startImsConnect = startImsConnect;
            this.mq = mq;
            this.inputQueue = inputQueue;
            this.replyQueue = replyQueue;
            this.loadlib = loadlib;
        }

        public String getSysviewLoadlib() {
            return loadlib;
        }

        public String getRegion() {
            return region;
        }

        public String getVersion() {
            return version;
        }

        public MqZosConfig getMq() {
            return mq;
        }

        public String getInputQueue() {
            return inputQueue;
        }

        public String getReplyQueue() {
            return replyQueue;
        }

        public boolean isStartImsConnect() {
            return startImsConnect;
        }

        public String getRoleId() {
            return "ims" + CommonUtils.constantToCamelCase(name()) + "Role";
        }

        @Override
        public String toString() {
            return "IMS " + region;
        }
    }
}
