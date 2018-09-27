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

package com.ca.apm.saas.role;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author kurma05
 */
public class FileUpdateRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUpdateRole.class);
    private String filePath;
    private Map<String,String> replacePairs;
    
    /**
     * @param builder Builder object containing all necessary data
     */
    protected FileUpdateRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.filePath = builder.filePath;
        this.replacePairs = builder.replacePairs;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        modifyFile(aaClient);
    }
    
    private void modifyFile(IAutomationAgentClient aaClient) {
        
        LOGGER.info("Updating file : {}", filePath);
        
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .replace(filePath, replacePairs)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate {@link FileUpdateRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
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
     * Builder responsible for holding all necessary properties to instantiate {@link FileUpdateRole}
     */
    public static class Builder extends BuilderBase<Builder, FileUpdateRole> {

        private final String roleId;
        @SuppressWarnings("unused")
        private final ITasResolver tasResolver;
        protected String filePath;
        protected Map<String,String> replacePairs;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public FileUpdateRole build() {
            return getInstance();
        }

        @Override
        protected FileUpdateRole getInstance() {
            return new FileUpdateRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return builder();
        }
        
        public Builder replacePairs(Map<String,String> replacePairs) {
            this.replacePairs = replacePairs;
            return builder();
        }
    }
}