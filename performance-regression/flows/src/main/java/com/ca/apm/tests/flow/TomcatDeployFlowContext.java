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

package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class TomcatDeployFlowContext implements IFlowContext {

    private final DeployTomcatFlowContext originalContext; // main context that has a private constructor so it can't be extended

    private final String unpackDir;

    protected TomcatDeployFlowContext(TomcatDeployFlowContext.Builder builder) {
        originalContext = builder.originalContext;
        unpackDir = builder.unpackDir;
    }

    public DeployTomcatFlowContext getOriginalContext() {
        return originalContext;
    }

    public String getUnpackDir() {
        return unpackDir;
    }

    public static class Builder extends ExtendedBuilderBase<TomcatDeployFlowContext.Builder, TomcatDeployFlowContext> {

        protected DeployTomcatFlowContext originalContext;
        protected String unpackDir;

        public Builder() {

        }

        public TomcatDeployFlowContext build() {
            TomcatDeployFlowContext context = this.getInstance();

            Args.notNull(context.originalContext, "originalContext");
            Args.notNull(context.unpackDir, "unpackDir");

            return context;
        }

        protected TomcatDeployFlowContext getInstance() {
            return new TomcatDeployFlowContext(this);
        }

        public TomcatDeployFlowContext.Builder unpackDir(String unpackDir) {
            this.unpackDir = unpackDir;
            return this.builder();
        }

        public TomcatDeployFlowContext.Builder originalContext(DeployTomcatFlowContext originalContext) {
            this.originalContext = originalContext;
            return this.builder();
        }

        protected TomcatDeployFlowContext.Builder builder() {
            return this;
        }
    }
}
