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
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author kurma05
 */
public class ClientDeployRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDeployRole.class);
    private ITasResolver tasResolver;
    private boolean shouldDeployJassApps;
    private boolean shouldDeployConsoleApps;
    private String jvmVersion;
    
    protected ClientDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
        this.jvmVersion = builder.jvmVersion;
        this.shouldDeployJassApps = builder.shouldDeployJassApps;
        this.shouldDeployConsoleApps = builder.shouldDeployConsoleApps;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        deployTestngSuite(aaClient);
        deployJmeter(aaClient);
        
        if(shouldDeployConsoleApps) {
            deploySqlmetricgen(aaClient);
            deployDITestApp(aaClient);
            deployDeepInheritanceApp(aaClient);
            deployStressApp(aaClient);
            deployProbebuilderApp(aaClient);
            deployCCProxy(aaClient);
        }
    }
   
    private void deployTestngSuite(IAutomationAgentClient aaClient) {
        
        deployZipArtifact(aaClient, "com.ca.apm.coda-projects.test-projects", 
            "javaagent_v2", "dist", "client");
        deployJarArtifact(aaClient, "com.ca.apm.em", "com.wily.introscope.clw.feature", 
            TasBuilder.WIN_SOFTWARE_LOC + "/client/lib/em/CLWorkstation.jar");
        deployJarArtifact(aaClient, "com.ca.apm.agent", "pbddoclet", 
            TasBuilder.WIN_SOFTWARE_LOC + "/client/lib/em/WilyPBDGenerator.jar");        
        deployJarArtifact(aaClient, "com.ca.apm.agent", "WebAppSupport", 
            TasBuilder.WIN_SOFTWARE_LOC + "/client/resources/jmx/WebAppSupport.jar");        
        createResultsDir(aaClient);
        createExcelCacheDirs(aaClient);
    }
    
    private void createExcelCacheDirs(IAutomationAgentClient aaClient) {
        
        String dir1 = "C:\\Windows\\SysWOW64\\config\\systemprofile\\Desktop";
        String dir2 = "C:\\Windows\\System32\\config\\systemprofile\\Desktop";    

        ArrayList<String> props = new ArrayList<String>(); 
        props.add("Agent automation - excel cache directory.");
       
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .create(dir1 + "/readme.txt", props)
            .create(dir2 + "/readme.txt", props)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void createResultsDir(IAutomationAgentClient aaClient) {
        
        //results dir
        ArrayList<String> props = new ArrayList<String>(); 
        props.add("Testng results directory.");
       
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .create(TasBuilder.WIN_SOFTWARE_LOC + AgentRegressionBaseTestBed.RESULTS_DIR 
                + "/readme.txt", props)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
        
        //create gc dir
        if(shouldDeployJassApps) {
            props = new ArrayList<String>(); 
            props.add("GC logs directory.");
           
            context = new FileModifierFlowContext.Builder()
                .create(TasBuilder.WIN_SOFTWARE_LOC + AgentRegressionBaseTestBed.RESULTS_DIR 
                    + "/gc/readme.txt", props)
                .build();
            runFlow(aaClient, FileModifierFlow.class, context);
        }
    }

    private void deployProbebuilderApp(IAutomationAgentClient aaClient) {

        if(shouldDeployJassApps) {
            deployZipArtifact(aaClient, "probebuilderapp", "dist");
        }
    }
    
    private void deployCCProxy(IAutomationAgentClient aaClient) {
      
        //http proxy server for http tunneling tests        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries", 
            "ccproxy", "", "zip", "8.0"));            
        LOGGER.info("Downloading artifact " + url.toString());
        
        GenericFlowContext context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(TasBuilder.WIN_SOFTWARE_LOC + "/ccproxy")
           .build();  
        runFlow(aaClient, GenericFlow.class, context);  
    }

    private void deployStressApp(IAutomationAgentClient aaClient) {
    
        if(shouldDeployJassApps) {            
            String classifier = "jvm7-dist";        
            if(jvmVersion.equals("6")) {
                classifier = "dist";
            }
            else if(jvmVersion.equals("8")) {
                classifier = "jvm8-dist";
            }
            deployZipArtifact(aaClient, "stressapp", classifier);
        }        
    }

    private void deploySqlmetricgen(IAutomationAgentClient aaClient) {
        
        String classifier = "jvm7-dist";        
        if(jvmVersion.equals("6")) {
            classifier = "dist";
        }
        else if(jvmVersion.equals("8")) {
            classifier = "jvm8-dist";
        }
        
        deployZipArtifact(aaClient, "sqlmetricgen", classifier);
    }
 
    private void deployDeepInheritanceApp(IAutomationAgentClient aaClient) {
        
        deployZipArtifact(aaClient, "deepInheritance", "dist");
    }

    private void deployDITestApp(IAutomationAgentClient aaClient) {
        
        deployZipArtifact(aaClient, "ditestapp", "dist");   
    }
  
    private void deployZipArtifact(IAutomationAgentClient aaClient,
                                   String artifactId,
                                   String classifier) {
        
        deployZipArtifact(aaClient, "com.ca.apm.coda-projects.test-tools", 
                       artifactId, classifier, artifactId);
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
           .destination(dest)
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
           .destination(TasBuilder.WIN_SOFTWARE_LOC + "/" + homeDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context); 
    }
         
    private void deployJmeter(IAutomationAgentClient aaClient) {
        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries", 
            "apache-jmeter", "", "zip", "3.1"));            
        LOGGER.info("Downloading artifact " + url.toString());
        
        GenericFlowContext context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(TasBuilder.WIN_SOFTWARE_LOC + "/jmeter/apache-jmeter-3.1")
           .build();  
        runFlow(aaClient, GenericFlow.class, context);  
    }
 
    public static class Builder extends BuilderBase<Builder, ClientDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;  
        protected boolean shouldDeployJassApps;
        protected boolean shouldDeployConsoleApps;
        private String jvmVersion;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public ClientDeployRole build() {
            return getInstance();
        }

        @Override
        protected ClientDeployRole getInstance() {
            return new ClientDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder jvmVersion(String jvmVersion) {
            this.jvmVersion = jvmVersion;
            return builder();
        }
   
        public Builder shouldDeployJassApps(boolean shouldDeployJassApps) {
            this.shouldDeployJassApps = shouldDeployJassApps;
            return builder();
        } 
        
        public Builder shouldDeployConsoleApps(boolean shouldDeployConsoleApps) {
            this.shouldDeployConsoleApps = shouldDeployConsoleApps;
            return builder();
        } 
    }
}