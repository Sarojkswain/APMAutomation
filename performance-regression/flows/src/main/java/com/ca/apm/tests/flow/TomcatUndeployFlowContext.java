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
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class TomcatUndeployFlowContext implements IFlowContext {

    private final String installLocation;

    protected TomcatUndeployFlowContext(Builder builder) {
        installLocation = builder.installLocation;
    }

    public String getInstallLocation() {
        return installLocation;
    }

    public static class Builder extends ExtendedBuilderBase<TomcatUndeployFlowContext.Builder, TomcatUndeployFlowContext> {

        protected String installLocation;

        public Builder() {

        }

        public TomcatUndeployFlowContext build() {
            TomcatUndeployFlowContext context = this.getInstance();

            Args.notNull(context.installLocation, "installLocation");

            return context;
        }

        protected TomcatUndeployFlowContext getInstance() {
            return new TomcatUndeployFlowContext(this);
        }

        public TomcatUndeployFlowContext.Builder installLocation(String installLocation) {
            this.installLocation = installLocation;
            return this.builder();
        }

        protected TomcatUndeployFlowContext.Builder builder() {
            return this;
        }
    }
}
