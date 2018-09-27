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

import java.net.URL;

/**
 * Flow Context for deploying Gacutils
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class OjdbcFlowContext implements IFlowContext {

    private final URL deployPackageUrl;
    private final String deploySourcesLocation;

    private final String jarName;


    protected OjdbcFlowContext(OjdbcFlowContext.Builder builder) {
        this.deployPackageUrl = builder.deployPackageUrl;
        this.deploySourcesLocation = builder.deploySourcesLocation;

        this.jarName = builder.jarName;
    }

    public URL getDeployPackageUrl() {
        return deployPackageUrl;
    }

    public String getDeploySourcesLocation() {
        return deploySourcesLocation;
    }

    public String getJarName() {
        return jarName;
    }

    public static class Builder extends ExtendedBuilderBase<OjdbcFlowContext.Builder, OjdbcFlowContext> {

        protected URL deployPackageUrl;
        protected String deploySourcesLocation;

        protected String jarName;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "ojdbc"));

        }

        public OjdbcFlowContext build() {
            OjdbcFlowContext context = this.getInstance();
            Args.notNull(context.deployPackageUrl, "deployPackageUrl");
            Args.notNull(context.deploySourcesLocation, "deploySourcesLocation");

            Args.notNull(context.jarName, "jarName");

            return context;
        }

        protected OjdbcFlowContext getInstance() {
            return new OjdbcFlowContext(this);
        }

        public OjdbcFlowContext.Builder deployPackageUrl(URL deployPackageUrl) {
            this.deployPackageUrl = deployPackageUrl;
            return this.builder();
        }

        public OjdbcFlowContext.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deploySourcesLocation = deploySourcesLocation;
            return this.builder();
        }

        public OjdbcFlowContext.Builder jarName(String jarName) {
            this.jarName = jarName;
            return this.builder();
        }

        protected OjdbcFlowContext.Builder builder() {
            return this;
        }
    }
}