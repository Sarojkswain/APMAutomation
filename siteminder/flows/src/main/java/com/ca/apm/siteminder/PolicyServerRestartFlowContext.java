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
package com.ca.apm.siteminder;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * @author Sundeep (bhusu01)
 */
public class PolicyServerRestartFlowContext implements IFlowContext {

    private String defaultRootDir;
    private String siteMinderServiceName;

    public PolicyServerRestartFlowContext(Builder builder) {
        this.defaultRootDir = builder.defaultRootDir;
        this.siteMinderServiceName = builder.siteMinderServiceName;
    }

    public String getDefaultRootDir() {
        return defaultRootDir;
    }

    public String getSiteMinderServiceName() {
        return siteMinderServiceName;
    }

    public static class Builder implements IBuilder<PolicyServerRestartFlowContext> {

        private String defaultRootDir = "C:\\";
        private String siteMinderServiceName = "SiteMinder Policy Server";

        public Builder defaultRootDir(String psRootDir) {
            this.defaultRootDir = psRootDir;
            return this;
        }

        public Builder smServiceName(String smServiceName) {
            this.siteMinderServiceName = smServiceName;
            return this;
        }

        @Override
        public PolicyServerRestartFlowContext build() {
            return new PolicyServerRestartFlowContext(this);
        }
    }
}
