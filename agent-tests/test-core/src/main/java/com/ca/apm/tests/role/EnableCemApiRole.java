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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.flow.JarArchiveFlow;
import com.ca.apm.tests.flow.JarArchiveFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author kurma05
 */
public class EnableCemApiRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnableCemApiRole.class);
    private String emHomeDir;
    private String unpackDir;
    private String emInstallVersion;
    
    /**
     * @param builder Builder object containing all necessary data
     */
    protected EnableCemApiRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.emHomeDir = builder.emHomeDir;
        this.unpackDir = builder.unpackDir;
        this.emInstallVersion = builder.emInstallVersion;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        unpackJar(aaClient);
        modifyXml(aaClient);
        packJar(aaClient);
    }
    
    private void unpackJar(IAutomationAgentClient aaClient) {
        
        String jarFilePath = emHomeDir + "/product/enterprisemanager/plugins/com.wily.apm.tess_" + 
            emInstallVersion.replace("-SNAPSHOT", "") + ".jar";

        JarArchiveFlowContext context = new JarArchiveFlowContext.Builder()
            .archivePath(jarFilePath)
            .tempUnpackDir(unpackDir)
            .unpack(true)
            .build();
    
        runFlow(aaClient, JarArchiveFlow.class, context);    
    }
 
    private void modifyXml(IAutomationAgentClient aaClient) {
        
        String xmlPath = unpackDir + "/WebContent/WEB-INF/tess-security.xml";
        
        LOGGER.info("Updating file : {}", xmlPath);
        
        Map<String,String> replacePairs = new HashMap<String,String>();
        replacePairs.put("internal","Internal");
        replacePairs.put("disallow", "allow");
        
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .replace(xmlPath, replacePairs)
            .build();
        
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void packJar(IAutomationAgentClient aaClient) {
        
        String jarFilePath = emHomeDir + "/product/enterprisemanager/plugins/com.wily.apm.tess_" + 
                    emInstallVersion.replace("-SNAPSHOT", "") + ".jar";
    
        JarArchiveFlowContext context = new JarArchiveFlowContext.Builder()
            .archivePath(jarFilePath)
            .tempUnpackDir(unpackDir)
            .pack(true)
            .build();

        runFlow(aaClient, JarArchiveFlow.class, context);  
    }
  
    /**
     * Linux Builder responsible for holding all necessary properties to instantiate {@link EnableCemApiRole}
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
     * Builder responsible for holding all necessary properties to instantiate {@link EnableCemApiRole}
     */
    public static class Builder extends BuilderBase<Builder, EnableCemApiRole> {

        private final String roleId;
        @SuppressWarnings("unused")
        private final ITasResolver tasResolver;
        protected String emHomeDir;
        protected String emInstallVersion;
        protected String unpackDir; 
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public EnableCemApiRole build() {
            return getInstance();
        }

        @Override
        protected EnableCemApiRole getInstance() {
            return new EnableCemApiRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder emHomeDir(String emHomeDir) {
            this.emHomeDir = emHomeDir;
           
            return builder();
        }
        
        public Builder unpackDir(String unpackDir) {
            this.unpackDir = unpackDir;
           
            return builder();
        }
        
        public Builder emInstallVersion(String emInstallVersion) {
            this.emInstallVersion = emInstallVersion;
           
            return builder();
        }
    }
}