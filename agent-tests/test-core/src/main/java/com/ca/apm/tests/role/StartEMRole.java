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

package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.EmRole;

/**
 * @author kurma05
 */
public class StartEMRole extends AbstractRole {

    private static final int ASYNC_DELAY = 90;
    private final RunCommandFlowContext emRunCommandFlowContext;
    private final RunCommandFlowContext wvRunCommandFlowContext;
    private boolean shouldStartView;
    
    /**
     * @param builder Builder object containing all necessary data
     */
    protected StartEMRole(Builder builder) {
        
        super(builder.roleId, builder.getEnvProperties());
        emRunCommandFlowContext = builder.emRunCmdFlowContext;
        wvRunCommandFlowContext = builder.wvRunCmdFlowContext;
        shouldStartView = builder.shouldStartView;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        startEm(aaClient);
        if(shouldStartView) {
            startWebView(aaClient);
        }
    }
    
    private void startEm(IAutomationAgentClient aaClient) {
       
        runCommandFlowAsync(aaClient, emRunCommandFlowContext, ASYNC_DELAY);
    }
    
    private void startWebView(IAutomationAgentClient aaClient) {    

        runCommandFlowAsync(aaClient, wvRunCommandFlowContext, ASYNC_DELAY);
    }
    /**
     * Linux Builder responsible for holding all necessary properties to instantiate {@link StartEMRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            introscopeExecutable = EmRole.LinuxBuilder.INTROSCOPE_EXECUTABLE;
            webviewExecutable = EmRole.LinuxBuilder.WEBVIEW_EXECUTABLE;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link StartEMRole}
     */
    public static class Builder extends BuilderBase<Builder, StartEMRole> {

        private final String roleId;
        @SuppressWarnings("unused")
        private final ITasResolver tasResolver;
        protected String emHomeDir;
        protected RunCommandFlowContext emRunCmdFlowContext;
        protected RunCommandFlowContext wvRunCmdFlowContext;
        protected boolean shouldStartView = true;
        protected String introscopeExecutable = EmRole.Builder.INTROSCOPE_EXECUTABLE;
        protected String webviewExecutable = EmRole.Builder.WEBVIEW_EXECUTABLE;
             
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public StartEMRole build() {
            
            initEmRunCommand();
            initWvRunCommand();
            return getInstance();
        }

        @Override
        protected StartEMRole getInstance() {
            return new StartEMRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
          
        public Builder emHomeDir(String emHomeDir) {
            this.emHomeDir = emHomeDir;
           
            return builder();
        }
        
        public Builder shouldStartView(boolean shouldStartView) {
            this.shouldStartView = shouldStartView;
           
            return builder();
        }
        
        protected void initEmRunCommand() {
            
            emRunCmdFlowContext = new RunCommandFlowContext.Builder(introscopeExecutable)
                .workDir(emHomeDir)
                .name(roleId)
                .terminateOnMatch(EmRole.EM_STATUS)
                .build();
            getEnvProperties().add(EmRole.ENV_START_EM, emRunCmdFlowContext);
        }

        protected void initWvRunCommand() {
            
            wvRunCmdFlowContext = new RunCommandFlowContext.Builder(webviewExecutable)
                .workDir(emHomeDir)
                .name(roleId)
                .terminateOnMatch(EmRole.WEBVIEW_STATUS)
                .build();
            getEnvProperties().add(EmRole.ENV_START_WEBVIEW, wvRunCmdFlowContext);
        }
    }
}