package com.ca.apm.tests.role;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
public class DotNetAgentDeployRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DotNetAgentDeployRole.class);
    private ITasResolver tasResolver;
    private String installDir;
 
    protected DotNetAgentDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
        this.installDir = builder.installDir;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        deployArtifact(aaClient);        
        installAgent(aaClient);
        updateAgentProfile(aaClient);
    } 
    
    private void updateAgentProfile(IAutomationAgentClient aaClient) {
        
        //stopping perfmon process to be able to update profile
        RunCommandFlowContext stopPerfmon = new RunCommandFlowContext.Builder("wmic")
            .args(Arrays.asList("Path", "win32_process", "Where", 
                  "\"CommandLine", "Like", "'%PerfMonCollectorAgent%'\"", "Call", "Terminate"))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, stopPerfmon);
        
        //update profile
        Map<String,String> replacePairs = new HashMap<String,String>();
        replacePairs.put("default-typical.pbl,hotdeploy", "default-full.pbl,hotdeploy");
        
        String fileName = installDir + "/wily/IntroscopeAgent.profile";
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .replace(fileName, replacePairs)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void deployArtifact(IAutomationAgentClient aaClient) {
        
        String artifact = "dotnet-agent-installer";
        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.delivery", 
            artifact, "64", "zip", tasResolver.getDefaultVersion()));            
        LOGGER.info("Downloading agent artifact " + url.toString());
      
        GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(installDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, getAgentContext);  
    }   
    
    private void installAgent(IAutomationAgentClient aaClient) {
        
        //install agent
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("IntroscopeDotNetAgentInstall64.exe")
            .workDir(installDir)
            .args(Arrays.asList("/s", "/v\"", "/qn", "INSTALLDIR=\"" + installDir + "\"\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //reset iis
        command = new RunCommandFlowContext.Builder("C:\\Windows\\System32\\iisreset")
            .args(Arrays.asList("/restart"))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    }
 
    public static class Builder extends BuilderBase<Builder, DotNetAgentDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;  
        protected String installDir;     
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public DotNetAgentDeployRole build() {
            return getInstance();
        }

        @Override
        protected DotNetAgentDeployRole getInstance() {
            return new DotNetAgentDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }
    }
}