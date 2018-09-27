package com.ca.apm.tests.role;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author kurma05
 */
public class WASWebappDeployRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WASWebappDeployRole.class);
    private ITasResolver tasResolver;
    private String appserverDir;
    private boolean shouldDeployJassApps;
    private String profileName;
    private String serverName;
    private String nodeName;
    private String minHeapSize;
    private String maxHeapSize;
    private String permSpaceSize;
    private String maxPermSpaceSize;
    private String jvmVersion;
 
    protected WASWebappDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.serverName = builder.serverName;
        this.nodeName = builder.nodeName;
        this.minHeapSize = builder.minHeapSize;
        this.maxHeapSize = builder.maxHeapSize;
        this.permSpaceSize = builder.permSpaceSize;
        this.maxPermSpaceSize = builder.maxPermSpaceSize;
        this.profileName = builder.profileName;
        this.tasResolver = builder.tasResolver;
        this.appserverDir = builder.appserverDir;
        this.shouldDeployJassApps = builder.shouldDeployJassApps;
        this.jvmVersion = builder.jvmVersion;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        deployArtifacts(aaClient);
        deployPOJars(aaClient);
        updateFiles(aaClient);
        installApps(aaClient);
    }
   
    private void deployArtifacts(IAutomationAgentClient aaClient) {
     
        GenericFlowContext context = null;
        
        //get po install scripts
        context = new GenericFlowContext.Builder()
           .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.was", 
               "was_po_deployment_scripts", "", "zip", "3.3")))
           .destination(appserverDir + "/TestApps/ws_pipeorgan3")
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get generic webapp install scripts
        context = new GenericFlowContext.Builder()        
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.was", 
                "was_webapp_deployment_scripts", "", "zip", "1.0")))
            .destination(appserverDir + "/TestApps/qatestapp/python")
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get pipeorgan ear
        context = new GenericFlowContext.Builder()    
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.pipeorgan", 
                 "pipeorgan_ear_ejb3", "", "ear", tasResolver.getDefaultVersion())))
            .destination(appserverDir + "/TestApps/ws_pipeorgan3/pipeorgan.was.ear")
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get qatestapp ear
        context = new GenericFlowContext.Builder()    
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools", 
                "qatestapp", "jvm" + jvmVersion + "-genericnodb", "ear", tasResolver.getDefaultVersion())))
            .destination(appserverDir + "/TestApps/qatestapp/QATestApp.ear")
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get jax webservices  ear
        context = new GenericFlowContext.Builder()    
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.testapps", 
                "JaxWSServicesSamples", "", "ear", "1.0")))
            .destination(appserverDir + "/TestApps/jaxsampleapp/JaxWSServicesSamples.ear")
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        if(shouldDeployJassApps) {
            context = new GenericFlowContext.Builder()    
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools", 
                "ditestappjass", "dist", "war", tasResolver.getDefaultVersion())))
            .destination(appserverDir + "/TestApps/ditestappjass/DITestAppJass.war")
            .build();  
            runFlow(aaClient, GenericFlow.class, context);
        }
    }
    
    private void deployPOJars(IAutomationAgentClient aaClient) {
        
        GenericFlowContext context = null;
        
        //get po jar
        context = new GenericFlowContext.Builder()    
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.pipeorgan", "pipeorgan",
                "", "jar", tasResolver.getDefaultVersion())))
            .destination(appserverDir + "/lib/ext/pipeorgan.jar")
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get po ejb jar
        context = new GenericFlowContext.Builder()    
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.pipeorgan",
                "pipeorgan_ejb", "", "jar", tasResolver.getDefaultVersion())))
            .destination(appserverDir + "/lib/ext/pipeorgan_ejb.jar")
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get po ejb3 jar
        context = new GenericFlowContext.Builder()    
            .notArchive()
            .artifactUrl(tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools.pipeorgan",
                "pipeorgan_ejb3", "", "jar", tasResolver.getDefaultVersion())))
            .destination(appserverDir + "/lib/ext/pipeorgan_ejb3.jar")
            .build();  
        runFlow(aaClient, GenericFlow.class, context);
                
        //copy rt.jar for JDK 6 tests as a workaround for NoClassDefFoundError
        if(jvmVersion.equals("6")) {
            FileModifierFlowContext copyFile = new FileModifierFlowContext.Builder()
                .copy("C:/Program Files/Java/jdk1.6.0_45/jre/lib/rt.jar", 
                    appserverDir + "/lib/ext/rt.jar")
                .build();
            runFlow(aaClient, FileModifierFlow.class, copyFile);
        }
    }
    
    private void updateFiles(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        HashMap<String,String> replacePairs = null;
        
        //update soap properties
        String file = appserverDir + "/profiles/" + profileName + "/properties/soap.client.props";
        LOGGER.info("Updating file " + file);
        
        replacePairs = new HashMap<String,String>();
        replacePairs.put("com.ibm.SOAP.requestTimeout=180","com.ibm.SOAP.requestTimeout=6000");        
        context = new FileModifierFlowContext.Builder()
            .replace(file, replacePairs)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
        
        //update memory settings fo wsadmin utility
        file = appserverDir + "/bin/wsadmin.bat";
        LOGGER.info("Updating file " + file);
        
        replacePairs = new HashMap<String,String>();
        replacePairs.put("set PERFJAVAOPTION=-Xms256m -Xmx256m","set PERFJAVAOPTION=-Xms512m -Xmx1024m");
        context = new FileModifierFlowContext.Builder()
            .replace(file, replacePairs)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);   
    }
   
    private void installApps(IAutomationAgentClient aaClient) {
        
        RunCommandFlowContext command = null;
        
        //start was
        command = new RunCommandFlowContext.Builder("startServer.bat")
            .workDir(appserverDir + "/profiles/" + profileName + "/bin")
            .args(Arrays.asList(serverName))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //update memory settings
        command = new RunCommandFlowContext.Builder("wsadmin.bat")
            .workDir(appserverDir + "/profiles/" + profileName + "/bin")
            .args(Arrays.asList("-f", appserverDir + "/TestApps/ws_pipeorgan3/set_memory_settings.jacl", 
                serverName, minHeapSize, maxHeapSize, permSpaceSize, maxPermSpaceSize))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    
        //deploy po        
        command = new RunCommandFlowContext.Builder("wsadmin.bat")
            .workDir(appserverDir + "/profiles/" + profileName + "/bin")
            .args(Arrays.asList("-f", appserverDir + "/TestApps/ws_pipeorgan3/deploy_po_infrastructure.jacl", 
                appserverDir + "/TestApps/ws_pipeorgan3/props.properties"))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    
        //install po
        command = new RunCommandFlowContext.Builder("wsadmin.bat")
            .workDir(appserverDir + "/profiles/" + profileName + "/bin")
            .args(Arrays.asList("-f", appserverDir + "/TestApps/ws_pipeorgan3/install_app.jacl", tasResolver.getDefaultVersion()))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    
        //start po  
        command = new RunCommandFlowContext.Builder("wsadmin.bat")
            .workDir(appserverDir + "/profiles/" + profileName + "/bin")
            .args(Arrays.asList("-f", appserverDir + "/TestApps/ws_pipeorgan3/start_app.jacl"))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //install qatestapp
        command = new RunCommandFlowContext.Builder("wsadmin.bat")
            .workDir(appserverDir + "/profiles/" + profileName + "/bin")
            .args(Arrays.asList("-lang", "jython", "-f", appserverDir + "/TestApps/qatestapp/python/install_app.py", 
                appserverDir + "/TestApps/qatestapp", "\"QA Consolidated Test App\"", "QATestApp.ear", nodeName, serverName))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //install jax ws app
        command = new RunCommandFlowContext.Builder("wsadmin.bat")
            .workDir(appserverDir + "/profiles/" + profileName + "/bin")
            .args(Arrays.asList("-lang", "jython", "-f", appserverDir + "/TestApps/qatestapp/python/install_app.py", 
                appserverDir + "/TestApps/jaxsampleapp", "JaxWSServicesSamples", "JaxWSServicesSamples.ear", nodeName, serverName))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //install ditestappjass
        if(shouldDeployJassApps) {     
            command = new RunCommandFlowContext.Builder("wsadmin.bat")
                .workDir(appserverDir + "/bin")
                .args(Arrays.asList("-lang", "jython", "-f", appserverDir + "/TestApps/qatestapp/python/install_app.py", 
                    appserverDir + "/TestApps/ditestappjass", "DITestAppJass", "DITestAppJass.war", nodeName, serverName))
                .build();        
            runFlow(aaClient, RunCommandFlow.class, command);    
        }
        
        //stop was     
        command = new RunCommandFlowContext.Builder("stopServer.bat")
            .workDir(appserverDir + "/profiles/" + profileName + "/bin/")
            .args(Arrays.asList(serverName))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    }
 
    public static class Builder extends BuilderBase<Builder, WASWebappDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        protected boolean shouldDeployJassApps;
        protected String appserverDir;
        protected String profileName;
        protected String serverName;
        protected String nodeName;
        protected String minHeapSize;
        protected String maxHeapSize;
        protected String permSpaceSize;
        protected String maxPermSpaceSize;
        protected String jvmVersion = "7";
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public WASWebappDeployRole build() {
            return getInstance();
        }

        @Override
        protected WASWebappDeployRole getInstance() {
            return new WASWebappDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder appserverDir(String appserverDir) {
            this.appserverDir = appserverDir;
            return builder();
        }
        
        public Builder profileName(String profileName) {
            this.profileName = profileName;
            return builder();
        }
        
        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return builder();
        }
        
        public Builder nodeName(String nodeName) {
            this.nodeName = nodeName;
            return builder();
        }
        
        public Builder minHeapSize(String minHeapSize) {
            this.minHeapSize = minHeapSize;
            return builder();
        }
        
        public Builder maxHeapSize(String maxHeapSize) {
            this.maxHeapSize = maxHeapSize;
            return builder();
        }
        
        public Builder permSpaceSize(String permSpaceSize) {
            this.permSpaceSize = permSpaceSize;
            return builder();
        }
        
        public Builder maxPermSpaceSize(String maxPermSpaceSize) {
            this.maxPermSpaceSize = maxPermSpaceSize;
            return builder();
        }
        
        public Builder shouldDeployJassApps(boolean shouldDeployJassApps) {
            this.shouldDeployJassApps = shouldDeployJassApps;
            return builder();
        }
        
        public Builder jvmVersion(String jvmVersion) {
            this.jvmVersion = jvmVersion;
            return builder();
        }
    }
}