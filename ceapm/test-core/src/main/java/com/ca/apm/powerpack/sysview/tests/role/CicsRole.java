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

import com.ca.apm.automation.action.flow.mainframe.CicsStartupFlow;
import com.ca.apm.automation.action.flow.mainframe.CicsStartupFlowContext;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole.MqZosConfig;
import com.ca.apm.powerpack.sysview.tests.role.SysvDb2Role.SysvDb2Config;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * This role brings up a CICS region.
 */
public class CicsRole extends AbstractRole {
    private final CicsStartupFlowContext startupContext;

    private CicsRole(Builder builder) {
        super(builder.roleId);

        CicsStartupFlowContext.Builder startupBuilder = new CicsStartupFlowContext
            .Builder(builder.task, builder.lpar)
            .sysviewLoadlib(builder.sysviewLoadlib);

        if (builder.onlyVerify) {
            startupBuilder.onlyVerify();
        }

        if (builder.db2Subsystem != null) {
            startupBuilder.monitorDb2Subsystem(builder.db2Subsystem);
        }

        startupContext = startupBuilder.build();
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, CicsStartupFlow.class, startupContext);
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link CicsRole}.
     *
     * <p>By default the built role only verifies the deployment, if you wish to deploy it call the
     * {@link #deployRole()} method.
     */
    public static class Builder extends BuilderBase<Builder, CicsRole> {
        private final String roleId;
        private final String task;
        private final String lpar;
        private String sysviewLoadlib = null;
        private String db2Subsystem = null;
        private boolean onlyVerify = true;

        /**
         * Constructor using predefined configuration.
         *
         * @param config Known CICS configuration.
         */
        public Builder(CicsConfig config) {
            this.roleId = config.getRoleId();
            this.task = config.getJobName();
            this.lpar = config.getLpar();
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
         * Sets a DB2 subsystem to be monitored by the region.
         *
         * @param db2Config DB2 configuration.
         * @return Builder instance the method was called on.
         */
        public Builder monitorDb2Subsystem(SysvDb2Config db2Config) {
            db2Subsystem = db2Config.getSubsystem();
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
        public CicsRole build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected CicsRole getInstance() {
            return new CicsRole(this);
        }
    }

    /** Port number is undefined. */
    public final static int NO_PORT = -1;

    /**
     * Default configurations for CICS role.
     */
    public enum CicsConfig {
        WILY_4_1_0("WC660CA", "CA31", "USILCA31", "660ECI", 1436, "660IPIC", 3551, 17080, NO_PORT,
            MqZosConfig.CSQ4, "WILY_QUEUE", "WILY_REPLY_QUEUE"),
        /*
         * TODO IPIC and ECI identifiers are temporarily colliding with definitions above,
         * because CICS Test Driver xml definitions have them hard coded.
         * This is not an issue unless you need to use both CICS instances in one testbed.
         * The identifiers do not have a special meaning and can be generated rather than predefined
         * here as long as the ID in CTG server definition matches what's used in the flow() call.
         */
        WILY_5_3_0("WILYC530", "CA31", "USILCA31", "660ECI", 15101, "660IPIC", 15102, NO_PORT,
            15104, MqZosConfig.CSQ4, "WILY_QUEUE_WILYC530", "WILY_REPLY_QUEUE"),
        WILY_5_3_0_11("WILYC530", "CA11", "USILCA11", "660ECI", 15101, "660IPIC", 15102, NO_PORT,
            15104, null, null, null),
        ;

        private final String jobName;
        private final String lpar;
        private final String host;
        private final String eciId;
        private final int eciPort;
        private final String ipicId;
        private final int ipicPort;
        private final int httpPort;
        private final int wsPort;
        private final MqZosConfig mq;
        private final String inputQueue;
        private final String replyQueue;

        /**
         * Configuration for CICS role.
         *
         * @param jobName CICS task job name
         * @param lpar LPAR of the CICS instance
         * @param host Host
         * @param eciId ECI name
         * @param eciPort ECI port
         * @param ipicId IPIC name
         * @param ipicPort IPIC port
         * @param httpPort HTTP port
         * @param wsPort Web services port
         * @param mq Associated MQ manager configuration
         * @param inputQueue Input MQ queue
         * @param replyQueue Reply MQ queue
         */
        CicsConfig(String jobName, String lpar, String host, String eciId, int eciPort,
                   String ipicId, int ipicPort, int httpPort, int wsPort, MqZosConfig mq,
                   String inputQueue, String replyQueue) {
            this.jobName = jobName;
            this.lpar = lpar;
            this.host = host;
            this.eciId = eciId;
            this.eciPort = eciPort;
            this.ipicId = ipicId;
            this.ipicPort = ipicPort;
            this.httpPort = httpPort;
            this.wsPort = wsPort;
            this.mq = mq;
            this.inputQueue = inputQueue;
            this.replyQueue = replyQueue;
        }

        public String getJobName() {
            return jobName;
        }

        public String getLpar() {
            return lpar;
        }

        public String getHost() {
            return host;
        }

        public String getEciId() {
            return eciId;
        }

        public int getEciPort() {
            return eciPort;
        }

        public String getIpicId() {
            return ipicId;
        }

        public int getIpicPort() {
            return ipicPort;
        }

        public int getHttpPort() {
            return httpPort;
        }

        public int getWsPort() {
            return wsPort;
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

        public String getRoleId() {
            return "cics" + CommonUtils.constantToCamelCase(name()) + "Role";
        }

        @Override
        public String toString() {
            return "CICS " + jobName;
        }
    }
}
