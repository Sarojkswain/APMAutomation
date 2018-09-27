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
package com.ca.apm.tests.flow.websphere85;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class Websphere85UndeployFlowContext implements IFlowContext {

    private final String installManagerDir;
    private final String installLocation;

    protected Websphere85UndeployFlowContext(Builder builder) {

        installManagerDir = builder.installManagerDir;
        installLocation = builder.installLocation;
    }

    public String getInstallManagerDir() {
        return installManagerDir;
    }

    public String getInstallLocation() {
        return installLocation;
    }

    public static class Builder extends ExtendedBuilderBase<Websphere85UndeployFlowContext.Builder, Websphere85UndeployFlowContext> {

        protected String installManagerDir;
        protected String installLocation;

        public Builder() {

        }

        public Websphere85UndeployFlowContext build() {
            Websphere85UndeployFlowContext context = this.getInstance();

            Args.notNull(context.installManagerDir, "installManagerDir");
            Args.notNull(context.installLocation, "installLocation");

            return context;
        }

        protected Websphere85UndeployFlowContext getInstance() {
            return new Websphere85UndeployFlowContext(this);
        }

        public Websphere85UndeployFlowContext.Builder installManagerDir(String installManagerDir) {
            this.installManagerDir = installManagerDir;
            return this.builder();
        }

        public Websphere85UndeployFlowContext.Builder installLocation(String installLocation) {
            this.installLocation = installLocation;
            return this.builder();
        }

        protected Websphere85UndeployFlowContext.Builder builder() {
            return this;
        }
    }
}
