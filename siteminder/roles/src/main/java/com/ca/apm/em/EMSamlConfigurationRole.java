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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author Sundeep (bhusu01)
 */
public class EMSamlConfigurationRole extends AbstractRole {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EMSamlConfigurationRole.class);

    private static final int ASYNC_DELAY = 90;
    private static final String EM_STATUS = "Introscope Enterprise Manager started";
    private static final String WEBVIEW_STATUS = "Introscope WebView started";

    private static EMSamlConfigureFlowContext flowContext;

    public EMSamlConfigurationRole(Builder builder) {
        super(builder.roleID);
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        client.runJavaFlow(new FlowConfigBuilder(EMSamlConfigurationFlow.class, flowContext, getHostingMachine().getHostnameWithPort()));

        startEM(client);
        startWV(client);
    }

    private void startWV(IAutomationAgentClient client) {
        RunCommandFlowContext cmdFlowContext =
            new RunCommandFlowContext.Builder("Introscope_Webview.exe").workDir(flowContext.getAPMRootDir()).name(getRoleId()).terminateOnMatch(WEBVIEW_STATUS).build();

        client.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdFlowContext, getEmHostWithPort()).delay(ASYNC_DELAY).async());

    }

    private void startEM(IAutomationAgentClient client) {
        RunCommandFlowContext cmdFlowContext =
            new RunCommandFlowContext.Builder("Introscope_Enterprise_Manager.exe").workDir(flowContext.getAPMRootDir()).name(getRoleId()).terminateOnMatch(EM_STATUS).build();

        client.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdFlowContext, getEmHostWithPort()).delay(ASYNC_DELAY).async());
    }

    public String getEmHostWithPort() {
        return getHostingMachine().getHostnameWithPort();
    }

    public static class Builder implements IBuilder<EMSamlConfigurationRole> {

        private String roleID;
        private ITasResolver tasResolver;
        private boolean enableInternalIdp = false;

        private EMSamlConfigureFlowContext.Builder contextBuilder;


        public Builder(String roleID, ITasResolver tasResolver) {
            this.roleID = roleID;
            this.tasResolver = tasResolver;
        }
        
        public Builder enableInternalIdp() {
            enableInternalIdp = true;
            return this;
        }

        @Override
        public EMSamlConfigurationRole build() {
            String hostName = tasResolver.getHostnameById("siteMinderRole");
            if(hostName==null || hostName.equals("localhost")) {
                try {
                    hostName = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    LOGGER.warn("Unable to resolve host. Using localhost");
                    hostName = "localhost";
                }
            }
            if(!hostName.equals("localhost") && !hostName.endsWith(".ca.com")) {
                hostName = hostName + ".ca.com";
            }
            contextBuilder = new EMSamlConfigureFlowContext.Builder(hostName);
            
            if (enableInternalIdp) {
                contextBuilder.enableInternalIdp();
            }
            
            flowContext = contextBuilder.build();
            return new EMSamlConfigurationRole(this);
        }
    }
}
