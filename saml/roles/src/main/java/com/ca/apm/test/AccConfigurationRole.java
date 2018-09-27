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
package com.ca.apm.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.resolver.TasResolver;
import com.ca.tas.role.AbstractRole;

public class AccConfigurationRole extends AbstractRole {
    private static final int ASYNC_DELAY = 90;
    
    private static final String RUN_CMD = "apmccsrv.cmd";


    private static FileModifierFlowContext flowContext;

    private Builder builder;

    private AccConfigurationRole(Builder builder) {
        super(builder.roleId);
        this.builder = builder;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        client.runJavaFlow(new FlowConfigBuilder(FileModifierFlow.class,
                flowContext,
                getHostingMachine().getHostnameWithPort()));

        startAcc(client);
    }

    public static class Builder extends BuilderBase<Builder, AccConfigurationRole> {

        public String roleId;
        public String configFile;
        public String accRootDir;
        public String apmRootDir;
        public ITasResolver tasResolver;

        private final FileModifierFlowContext.Builder contextBuilder =
                new FileModifierFlowContext.Builder();

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }
        
        public Builder accRootDir(String accRootDir) {
            this.accRootDir = accRootDir;
            return this;
        }

        public Builder apmRootDir(String apmRootDir) {
            this.apmRootDir = apmRootDir;
            return this;
        }


        @Override
        public AccConfigurationRole build() {
            String hostname = tasResolver.getHostnameById(roleId);
            String accConfigFile = accRootDir + "/config/apmccsrv.properties";
            Map<String, String> customConfig = new HashMap<>();
            customConfig.put("authentication.central.idp.url=http://localhost:8081",
                            "authentication.central.idp.url=http://" + hostname + ".ca.com:8081");
            contextBuilder.replace(accConfigFile, customConfig);

            // Change acc metadata to http
            String accMetadataFile = apmRootDir + "/config/saml-sp-acc-metadata.xml";
            Map<String, String> metadataConfig = new HashMap<>();
            metadataConfig.put("https://" + hostname + ".ca.com:8443", 
                            "http://" + hostname + ".ca.com:8088");
            contextBuilder.replace(accMetadataFile, metadataConfig);

            flowContext = contextBuilder.build();

            return getInstance();
        }

        @Override
        @NotNull
        public AccConfigurationRole getInstance() {
            return new AccConfigurationRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, TasResolver tasResolver) {
            super(roleId, tasResolver);
        }

    }

    private void startAcc(IAutomationAgentClient client) {

        
        RunCommandFlowContext consoleStartFlowContext = new RunCommandFlowContext.Builder(RUN_CMD)
        .workDir(builder.accRootDir)
        .args(Collections.singletonList("console"))
        .name(getRoleId())
        .terminateOnMatch("CA APM Command Center Configuration Server").build();

        client.runJavaFlow(
                new FlowConfigBuilder(RunCommandFlow.class, consoleStartFlowContext, getHostingMachine().getHostnameWithPort())
                        .delay(ASYNC_DELAY).async()
                );
    }

}
