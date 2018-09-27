package com.ca.apm.tests.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author Devesh Bajpai (bajde02), Marina Kur (kurma05)
 */
public class ClientDeployLinuxRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDeployLinuxRole.class);
    private ITasResolver tasResolver;
    private boolean shouldDeployConsoleApps;
    private String jvmVersion;
    
    protected ClientDeployLinuxRole(Builder builder) {
        
        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
        this.jvmVersion = builder.jvmVersion;
        this.shouldDeployConsoleApps = builder.shouldDeployConsoleApps;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        deployTestngSuite(aaClient);
        deployJmeter(aaClient);        
    }
    
    private void deployTestngSuite(IAutomationAgentClient aaClient) {
        
        deployZipArtifact(aaClient, "com.ca.apm.coda-projects.test-projects", "javaagent_v2", "dist", "client");
        deployJarArtifact(aaClient, "com.ca.apm.em", "com.wily.introscope.clw.feature", "CLWorkstation.jar");
        deployJarArtifact(aaClient, "com.ca.apm.agent", "pbddoclet", "WilyPBDGenerator.jar");
        createResultsDir(aaClient);
    }    

    private void createResultsDir(IAutomationAgentClient aaClient) {
        
        //results dir
        ArrayList<String> props = new ArrayList<String>(); 
        props.add("Testng results directory.");
       
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .create(TasBuilder.LINUX_SOFTWARE_LOC + AgentRegressionBaseTestBed.RESULTS_DIR 
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
        
        //get jmeter artifact
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries", 
            "apache-jmeter", "", "zip", "3.1"));            
        LOGGER.info("Downloading artifact " + url.toString());
        
        String dir = TasBuilder.LINUX_SOFTWARE_LOC + "jmeter/apache-jmeter-3.1";
        
        GenericFlowContext context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(dir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context);  
        
        //set permission
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("chmod")
          .args(Arrays.asList("-R", "+x", dir + "/bin"))
          .build();
        aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, command,
            getHostingMachine().getHostnameWithPort()));
    }
 
    public static class Builder extends BuilderBase<Builder, ClientDeployLinuxRole> {

        private final String roleId;
        private final ITasResolver tasResolver;  
        protected boolean shouldDeployConsoleApps;
        private String jvmVersion;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public ClientDeployLinuxRole build() {
            return getInstance();
        }

        @Override
        protected ClientDeployLinuxRole getInstance() {
            return new ClientDeployLinuxRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder jvmVersion(String jvmVersion) {
            this.jvmVersion = jvmVersion;
            return builder();
        }
        
        public Builder shouldDeployConsoleApps(boolean shouldDeployConsoleApps) {
            this.shouldDeployConsoleApps = shouldDeployConsoleApps;
            return builder();
        } 
    }
}