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

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author ahmal01
 */
public class JBOSSWebappDeployRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JBOSSWebappDeployRole.class);
    private ITasResolver tasResolver;
    private String appserverDir;
    private String jvmVersionQATestapp;
    private String serverPort;
    private String serverName;
 
    protected JBOSSWebappDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
        this.appserverDir = builder.appserverDir;
        this.jvmVersionQATestapp = builder.jvmVersionQATestapp;
        this.serverPort = builder.serverPort;
        this.appserverDir = builder.appserverDir;
        this.serverName = builder.serverName;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        deployArtifacts(aaClient);
        updateFiles(aaClient);
        
        if (serverName.contains("jboss")) {          
            deployOjdbc(aaClient, appserverDir + "/modules/com/oracle/jdbc/main");   
        }
        else if (serverName.contains("wildfly11")) {
            deployOjdbc(aaClient, appserverDir + "/modules/system/layers/base/com/oracle/jdbc/main");
            deployCommonsLogging(aaClient, appserverDir + "/modules/system/layers/base/org/apache/commons/logging/main");
        }
    }
   
    private void deployArtifacts(IAutomationAgentClient aaClient) {
     
        GenericFlowContext context = null;
        
        LOGGER.info("Deploying Artifacts...");
        
        //get po jar file
        context = new GenericFlowContext.Builder()
            .notArchive()
           .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.pipeorgan", 
               "pipeorgan", "", "jar", tasResolver.getDefaultVersion())))
           .destination(appserverDir + "/standalone/pipeorgan/pipeorgan.jar")
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get qatestapp ear
        context = new GenericFlowContext.Builder()    
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools", 
                "qatestapp", jvmVersionQATestapp, "ear", tasResolver.getDefaultVersion())))
            .destination(appserverDir + "/standalone/deployments/QATestApp.ear")
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get pipeorgan_ear_ejb3 ear for deployment
        //TODO fix Pipeorgan for wildfly11 then update code here to deploy it
        if (!serverName.contains("wildfly11")) {
            context = new GenericFlowContext.Builder()    
                .notArchive()
                .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.pipeorgan", 
                     "pipeorgan-ear-ejb3-jboss", "", "ear", tasResolver.getDefaultVersion())))
                .destination(appserverDir + "/standalone/deployments/pipeorgan_ear_ejb3.ear")
                .build();  
            runFlow(aaClient, GenericFlow.class, context);
        }
    }

    private void updateFiles(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        HashMap<String,String> replacePairs = null;
        String file; 
        //update standalone.xml file      
        if(serverName.contains("wildfly9")) {
            file = appserverDir + "/bin/standalone-pipeorgan-wildfly9.xml";
        }
        else if(serverName.contains("wildfly11")) {
            file = appserverDir + "/bin/standalone-pipeorgan-wildfly11.xml";
        }
        else {
            file = appserverDir + "/bin/standalone-pipeorgan-jboss711.xml";
        }  
        
        LOGGER.info("Updating file " + file);
        
        replacePairs = new HashMap<String,String>();
        replacePairs.put("\\[EJB3.EAR.PREFIX\\]","pipeorgan_ear_ejb3");
        replacePairs.put("\\[EJB3.JAR.PREFIX\\]","pipeorgan_ejb3-" + tasResolver.getDefaultVersion() +"-jboss");
        
        //set http port
        if (serverName.contains("wildfly11")) {
            replacePairs.put("${jboss.http.port:8080}", serverPort);
        }
        else {
            replacePairs.put("http\" port=\"8080", "http\" port=\"" + serverPort);
        }
        
        //set ejb2 jar prefix
        if (appserverDir.contains("jboss")) {
            replacePairs.put("\\[EJB2.JAR.PREFIX\\]","pipeorgan_ejb-" + tasResolver.getDefaultVersion());
        }
        
        // replacing values
        context = new FileModifierFlowContext.Builder()
            .replace(file, replacePairs)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
        
        // making backup of original file
        context = new FileModifierFlowContext.Builder()
            .move(appserverDir + "/standalone/configuration/standalone.xml", appserverDir + "/standalone/configuration/standalone.xml_ORIG" )
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
        
        // moving updated file
        context = new FileModifierFlowContext.Builder()
            .move(file, appserverDir + "/standalone/configuration/standalone.xml")
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);        
    }

    private void deployOjdbc(IAutomationAgentClient aaClient, String destinationDir) {

        GenericFlowContext context = null;
        FileModifierFlowContext fcontext = null;
        HashSet hashSet = new HashSet();
        
        //get ojdbc6 - 11.2.0.1.0 jar
        context = new GenericFlowContext.Builder()
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.libs", 
                "ojdbc6", "", "jar", "11.2.0.1.0")))
            .destination(destinationDir + "/ojdbc6-11.2.0.1.0.jar")
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        // Creating module.xml file
        String ojbcModule = destinationDir + "/module.xml";
        
        hashSet.add("<?xml version='1.0' encoding='UTF-8'?><module xmlns='urn:jboss:module:1.0' name='com.oracle.jdbc'><resources><resource-root path='ojdbc6-11.2.0.1.0.jar'/></resources><dependencies><module name='javax.api'/><module name='javax.transaction.api'/></dependencies></module>");
 
        fcontext = new FileModifierFlowContext.Builder()
            .create(ojbcModule, hashSet)
            .build();
        runFlow(aaClient, FileModifierFlow.class, fcontext);

    }
    
    private void deployCommonsLogging(IAutomationAgentClient aaClient, String destinationDir) {
        
        //get jar
        GenericFlowContext getJarContext = new GenericFlowContext.Builder()
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.libs", 
                "commons-logging", "", "jar", "1.2")))
            .destination(destinationDir + "/commons-logging-1.2.jar")
            .build();  
        runFlow(aaClient, GenericFlow.class, getJarContext);
        
        //move old module.xml
        String xml = destinationDir + "/module.xml";           
        FileModifierFlowContext fileContext = new FileModifierFlowContext.Builder()
            .move(xml, xml + ".org")
            .build();
        runFlow(aaClient, FileModifierFlow.class, fileContext);
        
        //create new module.xml 
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.add("<?xml version='1.0' encoding='UTF-8'?><module xmlns='urn:jboss:module:1.5' name='org.apache.commons.logging'><resources><resource-root path='commons-logging-1.2.jar'/></resources></module>");
        
        fileContext = new FileModifierFlowContext.Builder()
            .create(xml, hashSet)
            .build();
        runFlow(aaClient, FileModifierFlow.class, fileContext);
    }
    
    public static class Builder extends BuilderBase<Builder, JBOSSWebappDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        protected String appserverDir;
        protected String serverName;
        protected String jvmVersionQATestapp;
        protected String serverPort;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public JBOSSWebappDeployRole build() {
            return getInstance();
        }

        @Override
        protected JBOSSWebappDeployRole getInstance() {
            return new JBOSSWebappDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder appserverDir(String appserverDir) {
            this.appserverDir = appserverDir;
            return builder();
        }
        
        
        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return builder();
        }
        
        public Builder jvmVersionQATestapp(String jvmVersionQATestapp) {
            this.jvmVersionQATestapp = jvmVersionQATestapp;
            return builder();
        }
        
        public Builder serverPort(String serverPort) {
            this.serverPort = serverPort;
            return builder();
        }
        
    }
}