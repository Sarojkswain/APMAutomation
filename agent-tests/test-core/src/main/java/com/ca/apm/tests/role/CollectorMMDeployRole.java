/*
 * Copyright (c) 2016 CA.  All rights reserved.
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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.flow.JarArchiveFlow;
import com.ca.apm.tests.flow.JarArchiveFlowContext;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author kurma05
 */
public class CollectorMMDeployRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorMMDeployRole.class);
    private ITasResolver tasResolver;
    private String emHomeDir;
    private String unpackDir;
    private static final String JAR_RELATIVE_PATH = "/config/modules/collector-tas-JASSManagementModule.jar";
   
    protected CollectorMMDeployRole(Builder builder) {
        
        super(builder.roleId, builder.getEnvProperties());
        this.tasResolver = builder.tasResolver;
        this.emHomeDir = builder.emHomeDir;
        this.unpackDir = TasBuilder.WIN_SOFTWARE_LOC + "/mmtemp";
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        downloadJar(aaClient);
        unpackJar(aaClient);
        modifyXml(aaClient);
        packJar(aaClient);
    }
    
    private void downloadJar(IAutomationAgentClient aaClient) {
     
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda.em.jass", 
            "collector", "JASSManagementModule", "jar", "1.0"));            
        LOGGER.info("Downloading artifact " + url.toString());
        
        GenericFlowContext context = new GenericFlowContext.Builder()
            .notArchive()
            .artifactUrl(url)
            .destination(emHomeDir + JAR_RELATIVE_PATH)
            .build();  
        runFlow(aaClient, GenericFlow.class, context);          
    }

    private void unpackJar(IAutomationAgentClient aaClient) {
        
        String jarFilePath = emHomeDir + JAR_RELATIVE_PATH;

        JarArchiveFlowContext context = new JarArchiveFlowContext.Builder()
            .archivePath(jarFilePath)
            .tempUnpackDir(unpackDir)
            .unpack(true)
            .build();
    
        runFlow(aaClient, JarArchiveFlow.class, context);    
    }
 
    private void modifyXml(IAutomationAgentClient aaClient) {

        String xmlPath = unpackDir + "/ManagementModule.xml";
        
        LOGGER.info("Updating file : {}", xmlPath);
        
        Map<String,String> replacePairs = new HashMap<String,String>();
        replacePairs.put("\\[to\\]", AgentRegressionBaseTestBed.EMAIL_RECIPIENTS_JASS);
        replacePairs.put("\\[from\\]", "Team-APM-JASS@ca.com");
        replacePairs.put("\\[smtp.server\\]", "mail.ca.com");
        
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .replace(xmlPath, replacePairs)
            .build();        
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void packJar(IAutomationAgentClient aaClient) {

        String jarFilePath = emHomeDir + "/config/modules/collector-tas-JASSManagementModule.jar";

        JarArchiveFlowContext context = new JarArchiveFlowContext.Builder()
            .archivePath(jarFilePath)
            .tempUnpackDir(unpackDir)
            .pack(true)
            .build();

        runFlow(aaClient, JarArchiveFlow.class, context);  
    }
  
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

    public static class Builder extends BuilderBase<Builder, CollectorMMDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        protected String emHomeDir;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public CollectorMMDeployRole build() {
            return getInstance();
        }

        @Override
        protected CollectorMMDeployRole getInstance() {
            return new CollectorMMDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder emHomeDir(String emHomeDir) {
            this.emHomeDir = emHomeDir;           
            return builder();
        }
    }
}