/*
 * Copyright (c) 2015 CA. All rights reserved.
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
 * Context for the {@link MvsCommandFlow} flow.
 */
public class MvsCommandFlowContext implements IFlowContext {
    private String command;

    /**
     * Constructor.
     * 
     * @param builder Builder.
     */
    protected MvsCommandFlowContext(Builder builder) {
        assert !builder.command.trim().isEmpty();

        command = builder.command;
    }

    public String getCommand() {
        return command;
    }

    public static class Builder extends BuilderBase<Builder, MvsCommandFlowContext> {
        private String command;

        /**
         * Constructor.
         * 
         * @param command Command to execute.
         */
        public Builder(String command) {
            Args.notBlank(command, "command");

            this.command = command;
        }

        @Override
        public MvsCommandFlowContext build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected MvsCommandFlowContext getInstance() {
            return new MvsCommandFlowContext(this);
        }
    }
}
