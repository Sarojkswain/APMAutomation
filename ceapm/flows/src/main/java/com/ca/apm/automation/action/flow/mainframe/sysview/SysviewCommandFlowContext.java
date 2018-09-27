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

package com.ca.apm.automation.action.flow.mainframe.sysview;

import java.util.Arrays;
import java.util.Collection;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.Rc;
import com.ca.tas.builder.BuilderBase;

/**
 * Context for the {@link SysviewCommandFlow} flow.
 */
public class SysviewCommandFlowContext implements IFlowContext {
    private final String loadlib;
    private final String command;
    private final Collection<Rc> acceptableRcs;

    /**
     * Constructor.
     *
     * @param builder Builder.
     */
    protected SysviewCommandFlowContext(Builder builder) {
        assert !builder.command.trim().isEmpty();

        loadlib = builder.loadlib;
        command = builder.command;
        acceptableRcs = builder.acceptableRcs;
    }

    public String getCommand() {
        return command;
    }

    public String getLoadlib() {
        return loadlib;
    }

    public Collection<Rc> getAcceptableRCs() {
        return acceptableRcs;
    }

    /**
     * Builder for the {@link SysviewCommandFlowContext} flow context.
     */
    public static class Builder extends BuilderBase<Builder, SysviewCommandFlowContext> {
        private String loadlib = null;
        private String command;
        private Collection<Rc> acceptableRcs;

        /**
         * Constructor.
         *
         * @param command Command to execute.
         */
        public Builder(String command) {
            Args.notBlank(command, "command");

            this.command = command;
            acceptableRcs = Rc.getOkValues();
        }

        /**
         * Sets an explicit SYSVIEW load library to be used.
         *
         * @param loadlib Load library for the SYSVIEW instance to use.
         * @return Builder instance the method was called on.
         */
        public Builder loadlib(String loadlib) {
            Args.notBlank(loadlib, "loadlib");

            this.loadlib = loadlib;
            return builder();
        }

        /**
         * Defines the set of return code values that are to be considered as a success.
         *
         * @param acceptableRcs Set of return code values.
         * @return Referenced instance.
         */
        public Builder acceptableRcs(Rc... acceptableRcs) {
            Args.check(acceptableRcs.length > 0, "At least one acceptable RC has to be specified");

            this.acceptableRcs = Arrays.asList(acceptableRcs);
            return builder();
        }

        /**
         * Adds additional return code values that are to be considered as a success.
         *
         * @param acceptableRcs Set of return code values.
         * @return Referenced instance.
         */
        public Builder addAcceptableRcs(Rc... acceptableRcs) {
            this.acceptableRcs.addAll(Arrays.asList(acceptableRcs));
            return builder();
        }

        @Override
        public SysviewCommandFlowContext build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected SysviewCommandFlowContext getInstance() {
            return new SysviewCommandFlowContext(this);
        }
    }
}
