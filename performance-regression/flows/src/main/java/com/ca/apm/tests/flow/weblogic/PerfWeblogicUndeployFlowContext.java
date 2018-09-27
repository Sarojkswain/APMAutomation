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
package com.ca.apm.tests.flow.weblogic;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class PerfWeblogicUndeployFlowContext implements IFlowContext {

    private final String sourcesLocation;
    private final String parentDir;
    private final String installDir;

    protected PerfWeblogicUndeployFlowContext(Builder builder) {
        sourcesLocation = builder.sourcesLocation;
        parentDir = builder.parentDir;
        installDir = builder.installDir;
    }

    public String getSourcesLocation() {
        return sourcesLocation;
    }

    public String getParentDir() {
        return parentDir;
    }

    public String getInstallDir() {
        return installDir;
    }

    public static class Builder extends ExtendedBuilderBase<PerfWeblogicUndeployFlowContext.Builder, PerfWeblogicUndeployFlowContext> {

        protected String sourcesLocation;
        protected String parentDir;
        protected String installDir;

        public Builder() {

        }

        public PerfWeblogicUndeployFlowContext build() {
            PerfWeblogicUndeployFlowContext context = this.getInstance();

            Args.notNull(context.sourcesLocation, "sourcesLocation");
            Args.notNull(context.parentDir, "parentDir");
            Args.notNull(context.installDir, "installDir");

            return context;
        }

        protected PerfWeblogicUndeployFlowContext getInstance() {
            return new PerfWeblogicUndeployFlowContext(this);
        }

        public Builder sourcesLocation(String sourcesLocation) {
            this.sourcesLocation = sourcesLocation;
            return this.builder();
        }

        public Builder parentDir(String parentDir) {
            this.parentDir = parentDir;
            return this.builder();
        }

        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return this.builder();
        }

        protected Builder builder() {
            return this;
        }
    }
}
