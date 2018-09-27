/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
package com.ca.apm.siteminder;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Sundeep (bhusu01)
 */
public class ConfigureSMFederationRole extends AbstractRole {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConfigureSMFederationRole.class);

    private static ConfigureSMFedPartnershipFlowContext flowContext;

    public ConfigureSMFederationRole(Builder builder) {
        super(builder.roleID);
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        client.runJavaFlow(new FlowConfig.FlowConfigBuilder(ConfigureSMFedPartnershipFlow.class,
            flowContext,
            getHostingMachine().getHostnameWithPort()));
    }

    public static class Builder implements IBuilder<ConfigureSMFederationRole> {

        private String roleID;
        private ITasResolver tasResolver;
        private boolean wvFederation = false;
        private boolean wsFederation = false;
        private boolean emFederation = false;
        ConfigureSMFedPartnershipFlowContext.Builder contextBuilder = new ConfigureSMFedPartnershipFlowContext.Builder();

        public Builder(String roleID,ITasResolver tasResolver) {
            this.roleID = roleID;
            this.tasResolver = tasResolver;
        }

        @Override
        public ConfigureSMFederationRole build() {
            String smHostName = tasResolver.getHostnameById("siteMinderRole");
            if(smHostName==null || smHostName.equals("localhost")) {
                try {
                    smHostName = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    LOGGER.warn("Unable to resolve host. Using localhost");
                    smHostName = "localhost";
                }
            }
            if(!smHostName.equals("localhost") && !smHostName.endsWith(".ca.com")) {
                smHostName = smHostName + ".ca.com";
            }
            contextBuilder.adminUIHost(smHostName);
            
            if (wvFederation) {
                contextBuilder.importWvSP();
            }
            if (wsFederation) {
                contextBuilder.importWsSP();
            }
            if (emFederation) {
                contextBuilder.importEmSP();
            }
            
            
            flowContext = contextBuilder.build();
            return new ConfigureSMFederationRole(this);
        }
        
        public Builder createWvFederation() {
            wvFederation = true;
            return this;
        }
        
        public Builder createWsFederation() {
            wsFederation = true;
            return this;
        }
        
        public Builder createEmFederation() {
            emFederation = true;
            return this;
        }
    }
}
