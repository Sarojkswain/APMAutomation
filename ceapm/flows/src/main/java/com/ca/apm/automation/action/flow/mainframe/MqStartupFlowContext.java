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
 * Context for the {@link MqStartupFlow} flow.
 */
public class MqStartupFlowContext implements IFlowContext {
    private final String queueManagerName;
    private final String sysviewLoadlib;
    private final boolean onlyVerify;

    /**
     * Constructor.
     *
     * @param builder Builder.
     */
    private MqStartupFlowContext(Builder builder) {
        assert !builder.queueManagerName.trim().isEmpty();

        queueManagerName = builder.queueManagerName;
        sysviewLoadlib = builder.sysviewLoadlib;
        onlyVerify = builder.onlyVerify;
    }

    String getQueueManagerName() {
        return queueManagerName;
    }

    String getSysviewLoadlib() {
        return sysviewLoadlib;
    }

    public boolean isOnlyVerify() {
        return onlyVerify;
    }

    public static class Builder extends BuilderBase<Builder, MqStartupFlowContext> {
        private String queueManagerName;
        private String sysviewLoadlib = null;
        private boolean onlyVerify = false;

        /**
         * Constructor.
         * @param queueManagerName Name of the queue manager.
         */
        public Builder(String queueManagerName) {
            Args.notBlank(queueManagerName, "queueManagerName");

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
         * Only verify whether a MQ instance matching the requirements is already running.
         *
         * @return Builder instance the method was called on.
         */
        public Builder onlyVerify() {
            onlyVerify = true;
            return builder();
        }

        @Override
        public MqStartupFlowContext build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected MqStartupFlowContext getInstance() {
            return new MqStartupFlowContext(this);
        }
    }
}
