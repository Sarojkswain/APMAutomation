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

package com.ca.apm.automation.action.flow.mainframe;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * Context for the {@link ImsStartupFlow} flow.
 */
public class ImsStartupFlowContext implements IFlowContext {
    private String region;
    private String version;
    private String queueManagerName;
    private String sysviewLoadlib;
    private boolean startImsConnect;
    private boolean onlyVerify;

    /**
     * Constructor.
     *
     * @param builder Builder.
     */
    private ImsStartupFlowContext(Builder builder) {
        assert !builder.region.trim().isEmpty();
        assert !builder.version.trim().isEmpty();
        assert !builder.queueManagerName.trim().isEmpty();

        region = builder.region;
        version = builder.version;
        queueManagerName = builder.queueManagerName;
        sysviewLoadlib = builder.sysviewLoadlib;
        startImsConnect = builder.startImsConnect;
        onlyVerify = builder.onlyVerify;
    }

    String getRegion() {
        return region;
    }

    String getVersion() {
        return version;
    }

    String getQueueManagerName() {
        return queueManagerName;
    }

    String getSysviewLoadlib() {
        return sysviewLoadlib;
    }

    boolean isStartImsConnect() {
        return startImsConnect;
    }

    public boolean isOnlyVerify() {
        return onlyVerify;
    }

    /**
     * Builder responsible for holding all necessary properties to
     * instantiate {@link ImsStartupFlow}.
     */
    public static class Builder extends BuilderBase<Builder, ImsStartupFlowContext> {
        private String region;
        private String version;
        private String queueManagerName;
        private String sysviewLoadlib = null;
        private boolean startImsConnect = true;
        private boolean onlyVerify = false;

        /**
         * Constructor.
         * @param region IMS region identifier.
         * @param version IMS region version.
         * @param queueManagerName Name of the queue manager.
         */
        public Builder(String region, String version, String queueManagerName) {
            Args.notBlank(region, "region");
            Args.notBlank(version, "version");
            Args.notBlank(queueManagerName, "queueManagerName");

            this.region = region;
            this.version = version;
            this.queueManagerName = queueManagerName;
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
         * Disable startup of IMS Connect region.
         *
         * @return Builder instance the method was called on.
         */
        public Builder noImsConnect() {
            startImsConnect = false;
            return builder();
        }

        /**
         * Only verify whether a IMS region matching the requirements is already running.
         *
         * @return Builder instance the method was called on.
         */
        public Builder onlyVerify() {
            onlyVerify = true;
            return builder();
        }

        @Override
        public ImsStartupFlowContext build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected ImsStartupFlowContext getInstance() {
            return new ImsStartupFlowContext(this);
        }
    }
}
