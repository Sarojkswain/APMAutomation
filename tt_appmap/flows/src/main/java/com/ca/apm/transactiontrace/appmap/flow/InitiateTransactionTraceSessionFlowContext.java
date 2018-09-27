/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.transactiontrace.appmap.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * Flow context for initiating transaction trace session using CLW command
 *
 * @author bhusu01
 */
public class InitiateTransactionTraceSessionFlowContext implements IFlowContext {

    private String transactionTraceCLWCommand =
        "trace transactions exceeding %d ms in agents matching \"%s\" for %d s";

    private String clwCommand;
    private String apmLibDir;
    private String clwJarFile;
    private String jreBinDir;

    @SuppressWarnings("unused")
    protected InitiateTransactionTraceSessionFlowContext(Builder builder) {
        clwCommand =
            String.format(transactionTraceCLWCommand, builder.timeFilterInMillis, builder.agentSpecifier, builder.traceSessionTimeInSeconds);
        apmLibDir = builder.apmLibDir;
        clwJarFile = builder.clwJarName;
        jreBinDir = builder.jreBinDir;
    }

    public String getClwCommand() {
        return clwCommand;
    }

    public String getApmLibDir() {
        return apmLibDir;
    }

    public String getClwJarFile() {
        return clwJarFile;
    }

    public String getJreBinDir() {
        return jreBinDir;
    }

    public static class LinuxBuilder extends Builder {

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }
    }


    public static class Builder extends BuilderBase<Builder, InitiateTransactionTraceSessionFlowContext> {

        private String emDir = "em";
        private String apmLibDir = "lib" + getPathSeparator();
        private String jreBinDir = "jre" + getPathSeparator() + "bin" + getPathSeparator();
        private String clwJarName = "CLWorkstation.jar";
        private int timeFilterInMillis = 100;
        private int traceSessionTimeInSeconds = 600;
        private String agentSpecifier = ".*|.*|.*";

        @Override
        public InitiateTransactionTraceSessionFlowContext build() {
            String deployBase = getDeployBase();
            String emBaseDir = deployBase + getPathSeparator() + emDir + getPathSeparator();
            apmLibDir = emBaseDir + apmLibDir;
            jreBinDir = emBaseDir + jreBinDir;
            return getInstance();
        }

        @Override
        protected InitiateTransactionTraceSessionFlowContext getInstance() {
            return new InitiateTransactionTraceSessionFlowContext(this);
        }

        public Builder timeFilterInMillis(int timeFilter) {
            timeFilterInMillis = timeFilter;
            return this;
        }

        public Builder traceSessionTime(int sessionTimeInSeconds) {
            traceSessionTimeInSeconds = sessionTimeInSeconds;
            return this;
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
