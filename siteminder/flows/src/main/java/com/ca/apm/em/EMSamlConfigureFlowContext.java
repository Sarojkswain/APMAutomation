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

package com.ca.apm.em;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * @author Sundeep (bhusu01)
 */
public class EMSamlConfigureFlowContext implements IFlowContext {

    private String apmRootDir;
    private String smHost;
    private String configFilePath;
    private boolean enableInternalIdp;

    public EMSamlConfigureFlowContext(Builder builder) {
        this.apmRootDir = builder.apmRootDir;
        this.smHost = builder.smHost;
        this.configFilePath = builder.configFilePath;
        this.enableInternalIdp = builder.enableInternalIdp;
    }

    public String getAPMRootDir() {
        return this.apmRootDir;
    }

    public String getSMHost() {
        return this.smHost;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }
    
    public boolean isEnabledInternalIdp() {
       return enableInternalIdp;
    }

    public static class Builder implements IBuilder<EMSamlConfigureFlowContext> {

        public String apmRootDir = "c:\\automation\\deployed\\em\\";
        private String smHost;
        private String configFilePath = "config/IntroscopeEnterpriseManager.properties";
        private boolean enableInternalIdp = false;

        public Builder(String smHost) {
            this.smHost = smHost;
        }

        @Override
        public EMSamlConfigureFlowContext build() {
            return new EMSamlConfigureFlowContext(this);
        }

        public Builder apmRootDir(String apmRootDir) {
            this.apmRootDir = apmRootDir;
            return this;
        }

        // Siteminder host name
        public Builder smHost(String smHost) {
            this.smHost = smHost;
            return this;
        }

        public Builder configFilePath(String configPath) {
            this.configFilePath = configPath;
            return this;
        }
        
        public Builder enableInternalIdp() {
            this.enableInternalIdp = true;
            return this;
        }
    }
}
