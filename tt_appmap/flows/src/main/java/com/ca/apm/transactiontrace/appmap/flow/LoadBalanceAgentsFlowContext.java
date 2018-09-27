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
 * LoadBalanceAgentsFlowContext
 *
 * @author ...
 */
public class LoadBalanceAgentsFlowContext implements IFlowContext {

    private String loadbalancingXMLPath;
    private String[] agentNames;
    private String[] collectorInfo;
    private String agentSpecifierPrefix;
    private String apmRootDir;

    @SuppressWarnings("unused")
    protected LoadBalanceAgentsFlowContext(Builder builder) {
        this.loadbalancingXMLPath = builder.loadbalancingPath;
        this.agentNames = builder.agentNames;
        this.collectorInfo = builder.collectorInfo;
        this.agentSpecifierPrefix = builder.agentSpecifierPrefix;
        this.apmRootDir = builder.apmBaseDir;
    }

    public String getLoadbalancingXMLPath() {
        return loadbalancingXMLPath;
    }

    public String[] getAgentNames() {
        return agentNames;
    }

    public String[] getCollectorInfo() {
        return collectorInfo;
    }

    public String getAgentSpecifierPrefix() {
        return agentSpecifierPrefix;
    }

    public String getApmRootDir() {
        return apmRootDir;
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

    public static class Builder extends BuilderBase<Builder, LoadBalanceAgentsFlowContext> {

        private String emDir = "em";
        private String apmBaseDir;
        private String loadbalancingPath = "config" + getPathSeparator() + "loadbalancing.xml";
        private String[] agentNames = new String[]{"Engine","Mediator","Portal"};
        private String agentSpecifierPrefix = ".*\\|.*\\|";
        private String[] collectorInfo;

        @Override
        public LoadBalanceAgentsFlowContext build() {
            apmBaseDir = getDeployBase() + getPathSeparator() + emDir + getPathSeparator();
            loadbalancingPath = apmBaseDir + loadbalancingPath;
            return getInstance();
        }

        @Override
        protected LoadBalanceAgentsFlowContext getInstance() {
            return new LoadBalanceAgentsFlowContext(this);
        }

        public Builder collectorInfo(String[] collectorInfo) {
            this.collectorInfo = collectorInfo;
            return this;
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
