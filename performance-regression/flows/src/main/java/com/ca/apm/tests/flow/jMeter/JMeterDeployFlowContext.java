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
package com.ca.apm.tests.flow.jMeter;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * Flow Context for installing NET APM Agent
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class JMeterDeployFlowContext implements IFlowContext {

    private final URL deployPackageUrl;
    private final String deploySourcesLocation;

    protected JMeterDeployFlowContext(JMeterDeployFlowContext.Builder builder) {
        this.deployPackageUrl = builder.deployPackageUrl;
        this.deploySourcesLocation = builder.deploySourcesLocation;
    }

    public URL getDeployPackageUrl() {
        return deployPackageUrl;
    }

    public String getDeploySourcesLocation() {
        return deploySourcesLocation;
    }

    public static class Builder extends ExtendedBuilderBase<JMeterDeployFlowContext.Builder, JMeterDeployFlowContext> {

        protected URL deployPackageUrl;
        protected String deploySourcesLocation;

        public Builder() {
            this.deploySourcesLocation(this.concatPaths(this.getDeployBase(), "jmeter"));
        }

        public JMeterDeployFlowContext build() {
            JMeterDeployFlowContext context = this.getInstance();

            Args.notNull(context.deployPackageUrl, "deployPackageUrl");
            Args.notNull(context.deploySourcesLocation, "deploySourcesLocation");

            return context;
        }

        protected JMeterDeployFlowContext getInstance() {
            return new JMeterDeployFlowContext(this);
        }


        public JMeterDeployFlowContext.Builder deployPackageUrl(URL deployPackageUrl) {
            this.deployPackageUrl = deployPackageUrl;
            return this.builder();
        }

        public JMeterDeployFlowContext.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.deploySourcesLocation = deploySourcesLocation;
            return this.builder();
        }

        protected JMeterDeployFlowContext.Builder builder() {
            return this;
        }
    }
}