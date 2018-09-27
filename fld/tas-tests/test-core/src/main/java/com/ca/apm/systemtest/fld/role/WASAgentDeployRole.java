package com.ca.apm.systemtest.fld.role;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.mortbay.jetty.servlet.HashSessionIdManager;
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
 * @author banra06
 */
public class WASAgentDeployRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WASAgentDeployRole.class);
    private ITasResolver tasResolver;
    private String appserverDir;
    private String serverName; 
    private String installScriptPath;
    private boolean isLegacyMode;
    private String emHost;
    private String customProcessName;
    private String agentName;
    private String agentHostName;
    private String agentVersion;
 
    protected WASAgentDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.serverName = builder.serverName;     
        this.tasResolver = builder.tasResolver;
        this.appserverDir = builder.appserverDir;
        this.installScriptPath = builder.installScriptPath;
        this.isLegacyMode = builder.isLegacyMode;
        this.emHost=builder.emHost;
        this.customProcessName = builder.customProcessName;
        this.agentName = builder.agentName;
        this.agentHostName = builder.agentHostName;
        this.agentVersion = builder.agentVersion;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        deployArtifact(aaClient);        
        setupAgent(aaClient);
        updateAgentProfile(aaClient);
    }
   
    private void deployArtifact(IAutomationAgentClient aaClient) {
        
        String artifact = "agent-noinstaller-websphere-windows";
        if (isLegacyMode) {
            artifact = "agent-legacy-noinstaller-websphere-windows";
        }
        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.delivery", 
            artifact, "", "zip", 
            agentVersion == null ? tasResolver.getDefaultVersion() : agentVersion));            
        LOGGER.info("Downloading agent artifact " + url.toString());
      
        //get wily bundle
        GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(appserverDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, getAgentContext);  
        
        //rename wily dir
        FileModifierFlowContext updateDirContext = new FileModifierFlowContext.Builder()
            .move(appserverDir + "/wily", appserverDir + "/wily_" + serverName)
            .build();
        runFlow(aaClient, FileModifierFlow.class, updateDirContext);
    }   
    
    private void setupAgent(IAutomationAgentClient aaClient) {
        
        RunCommandFlowContext command = null;
        
        //start was
        command = new RunCommandFlowContext.Builder("startServer.bat")
            .workDir(appserverDir + "/bin")
            .args(Arrays.asList(serverName))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //install agent
        command = new RunCommandFlowContext.Builder("wsadmin.bat")
            .workDir(appserverDir + "/bin")
            .args(Arrays.asList("-f", installScriptPath + "/setup_apm_agent.jacl", 
                appserverDir + "/wily_" + serverName, serverName))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
       
        //stop was     
        command = new RunCommandFlowContext.Builder("stopServer.bat")
            .workDir(appserverDir + "/bin/")
            .args(Arrays.asList(serverName))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    }
    
    /**
     * Update IntroscopeAgent.profile - enables crossjvm, and sets the host:agent:process values used
     * for sending metrics to the EM
     * @param aaClient
     */
    private void updateAgentProfile(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Set<String> adds = new HashSet<>();
        Map<String, String> replacePairs = new HashMap<String,String>();
        String prop="agentManager.url.1="+emHost+":5001";
        
        replacePairs.put("#introscope.agent.websphere.crossjvm=true", "introscope.agent.websphere.crossjvm=true" );
        replacePairs.put("agentManager.url.1=localhost:5001", prop);

        if (agentHostName != null) {
            adds.add("introscope.agent.hostName=" + agentHostName);
        }
        if (customProcessName != null) {
            adds.add("introscope.agent.customProcessName=" + customProcessName);
        }
        if (agentName != null) {
            replacePairs.put("introscope.agent.agentAutoNamingEnabled=true", "introscope.agent.agentAutoNamingEnabled=false");
            replacePairs.put("introscope.agent.agentName=WebSphere Agent", "introscope.agent.agentName=" + agentName);
        }

        String fileName = appserverDir + "/wily_" + serverName + "/core/config/IntroscopeAgent.profile";
        
        
        
        context = new FileModifierFlowContext.Builder()
            .replace(fileName, replacePairs)
            .append(fileName, adds)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }
 
    public static class Builder extends BuilderBase<Builder, WASAgentDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;  
        protected String appserverDir;     
        protected String serverName;     
        protected String installScriptPath;
        protected boolean isLegacyMode;
        protected String emHost;
        private String customProcessName;
        private String agentName;
        private String agentHostName;
        private String agentVersion;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public WASAgentDeployRole build() {
            return getInstance();
        }

        @Override
        protected WASAgentDeployRole getInstance() {
            return new WASAgentDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        /**
         * Sets a custom process name for the agent to report metrics under
         * @param name
         * @return
         */
        public Builder customProcessName(String name) {
            this.customProcessName = name;
            return this;
        }
        
        /**
         * Sets a custom agent name to report metrics under
         * @param agentName
         * @return
         */
        public Builder agentName(String agentName) {
            this.agentName = agentName;
            return this;
        }
        
        /**
         * Sets a custom "host" name for the agent to report metrics under
         * @param agentHostName
         * @return
         */
        public Builder agentHostName(String agentHostName) {
            this.agentHostName = agentHostName;
            return this;
        }
        
        public Builder agentVersion(String agentVersion) {
            this.agentVersion = agentVersion;
            return this;
        }
        
        public Builder appserverDir(String appserverDir) {
            this.appserverDir = appserverDir;
            return builder();
        }
   
        public Builder installScriptPath(String installScriptPath) {
            this.installScriptPath = installScriptPath;
            return builder();
        }
        
        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return builder();
        }
        
        public Builder isLegacyMode(boolean isLegacyMode) {
            this.isLegacyMode = isLegacyMode;
            return builder();
        }
        public Builder emHost(String emHost) {
            this.emHost = emHost;
            return builder();
        }
    }
}