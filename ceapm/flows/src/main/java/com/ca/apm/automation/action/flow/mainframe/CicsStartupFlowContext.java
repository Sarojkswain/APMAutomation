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

import org.apache.commons.lang3.Validate;
import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * Context for the {@link CicsStartupFlow} flow.
 */
public class CicsStartupFlowContext implements IFlowContext {
    private final String taskName;
    private final String lpar;
    private final String sysviewLoadlib;
    private final String db2Subsystem;
    private final boolean onlyVerify;

    /**
     * Constructor.
     *
     * @param builder Builder.
     */
    private CicsStartupFlowContext(Builder builder) {
        assert !builder.taskName.trim().isEmpty();

        taskName = builder.taskName;
        lpar = builder.lpar;
        sysviewLoadlib = builder.sysviewLoadlib;
        db2Subsystem = builder.db2Subsystem;
        onlyVerify = builder.onlyVerify;
    }

    String getTaskName() {
        return taskName;
    }

    String getLpar() {
        return lpar;
    }

    String getSysviewLoadlib() {
        return sysviewLoadlib;
    }

    String getDb2Subsystem() {
        return db2Subsystem;
    }

    boolean isOnlyVerify() {
        return onlyVerify;
    }

    public static class Builder extends BuilderBase<Builder, CicsStartupFlowContext> {
        private String taskName;
        private String lpar;
        private String sysviewLoadlib = null;
        private String db2Subsystem = null;
        private boolean onlyVerify = false;

        /**
         * Constructor.
         *
         * @param taskName Name of the CICS region task.
         * @param lpar LPAR of the CICS instance.
         */
        public Builder(String taskName, String lpar) {
            Args.notBlank(taskName, "taskName");

            this.taskName = taskName;
            this.lpar = lpar;
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
         * Sets a DB2 subsystem to be monitored by the region.
         *
         * @param db2Subsystem DB2 subsystem to monitor.
         * @return Builder instance the method was called on.
         */
        public Builder monitorDb2Subsystem(String db2Subsystem) {
            Validate.notEmpty(db2Subsystem);
            Validate.inclusiveBetween(1, 4, db2Subsystem.length());

            this.db2Subsystem = db2Subsystem;
            return builder();
        }

        /**
         * Only verify whether a CICS region matching the requirements is already running.
         *
         * @return Builder instance the method was called on.
         */
        public Builder onlyVerify() {
            onlyVerify = true;
            return builder();
        }

        @Override
        public CicsStartupFlowContext build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected CicsStartupFlowContext getInstance() {
            return new CicsStartupFlowContext(this);
        }
    }
}
