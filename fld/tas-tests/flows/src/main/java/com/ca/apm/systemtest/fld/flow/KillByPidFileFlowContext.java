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

package com.ca.apm.systemtest.fld.flow;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * Flow to setup Kill by pid File process
 *
 */
public class KillByPidFileFlowContext implements IFlowContext {
    
    private final String pidFile;

    public KillByPidFileFlowContext(Builder builder) {
        this.pidFile = builder.pidFile;
    }

    public String getPidFile() {
        return pidFile;
    }
    
    public static class Builder extends BuilderBase<Builder,KillByPidFileFlowContext> {

        private String pidFile;

        public Builder() {
            ;
        }

        public Builder pidFile(String pidFile) {
            this.pidFile = pidFile;
            return this;
        }

        @Override
        public KillByPidFileFlowContext build() {
            KillByPidFileFlowContext flowContext = getInstance();
            Args.notNull(flowContext.pidFile, "pidFile with PID");

            return flowContext;
        }

        @Override
        protected KillByPidFileFlowContext getInstance() {
            return new KillByPidFileFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
