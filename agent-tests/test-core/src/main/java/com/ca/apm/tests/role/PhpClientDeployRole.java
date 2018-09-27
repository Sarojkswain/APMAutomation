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

package com.ca.apm.tests.role;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.testbed.PhpAgentStandAloneTestBed;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author ahmal01
 */
public class PhpClientDeployRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PhpClientDeployRole.class);
    private ITasResolver tasResolver;
    
    protected PhpClientDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        deployTestngSuite(aaClient);
        deployJmeter(aaClient);
    }
    
    private void deployTestngSuite(IAutomationAgentClient aaClient) {
        
        deployZipArtifact(aaClient, "com.ca.apm.coda-projects.test-projects", 
                          "javaagent_v2", "dist", "client");
        deployJarArtifact(aaClient, "com.ca.apm.em", 
                          "com.wily.introscope.clw.feature", "CLWorkstation.jar");
        createResultsDir(aaClient);
    }
    
    private void createResultsDir(IAutomationAgentClient aaClient) {
        
        //results dir
        ArrayList<String> props = new ArrayList<String>(); 
        props.add("Testng results directory.");
       
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .create(TasBuilder.LINUX_SOFTWARE_LOC + PhpAgentStandAloneTestBed.RESULTS_DIR 
                + "/readme.txt", props)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
        
    }

    private void deployJarArtifact(IAutomationAgentClient aaClient,  
                                  String groupId,
                                  String artifactId,                                
                                  String dest) {
        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact(groupId, artifactId, 
            "", "jar", tasResolver.getDefaultVersion()));            
        LOGGER.info("Downloading jar artifact " + url.toString());
        
        GenericFlowContext context = new GenericFlowContext.Builder()
           .notArchive()
           .artifactUrl(url)
           .destination(TasBuilder.LINUX_SOFTWARE_LOC + "/client/lib/em/" + dest)
           .build();  
        runFlow(aaClient, GenericFlow.class, context); 
    }
    
    private void deployZipArtifact(IAutomationAgentClient aaClient,
                                String groupId,
                                String artifactId,
                                String classifier,
                                String homeDir) {
        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact(groupId, artifactId, 
            classifier, "zip", tasResolver.getDefaultVersion()));            
        LOGGER.info("Downloading zip artifact " + url.toString());
        
        GenericFlowContext context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(TasBuilder.LINUX_SOFTWARE_LOC + "/" + homeDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context); 
    }

    private void deployJmeter(IAutomationAgentClient aaClient) {
        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries", 
            "apache-jmeter", "", "zip", "2.11"));            
        LOGGER.info("Downloading artifact " + url.toString());
        
        GenericFlowContext context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(TasBuilder.LINUX_SOFTWARE_LOC + "/jmeter/apache-jmeter-2.11")
           .build();  
        runFlow(aaClient, GenericFlow.class, context);  
    }

    
    public static class Builder extends BuilderBase<Builder, PhpClientDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;  
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public PhpClientDeployRole build() {
            return getInstance();
        }

        @Override
        protected PhpClientDeployRole getInstance() {
            return new PhpClientDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}