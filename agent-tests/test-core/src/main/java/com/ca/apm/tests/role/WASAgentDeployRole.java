package com.ca.apm.tests.role;

import java.net.URL;
import java.util.Arrays;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.flow.AccAgentDownloadFlow;
import com.ca.apm.tests.flow.AccAgentDownloadFlowContext;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author kurma05
 */
public class WASAgentDeployRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WASAgentDeployRole.class);
    private ITasResolver tasResolver;
    private String appserverDir;
    private String serverName; 
    private String installScriptPath;
    private boolean isLegacyMode;
    private boolean isNoRedefEnabled;    
	private String accServerUrl;
	private String accPackageName;
	private String accPackageOsName;
 
    protected WASAgentDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.serverName = builder.serverName;     
        this.tasResolver = builder.tasResolver;
        this.appserverDir = builder.appserverDir;
        this.installScriptPath = builder.installScriptPath;
        this.isLegacyMode = builder.isLegacyMode;
        this.isNoRedefEnabled = builder.isNoRedefEnabled;
        this.accServerUrl = builder.accServerUrl;
        this.accPackageName = builder.accPackageName;
        this.accPackageOsName = builder.accPackageOsName;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
		if (accPackageName != null) {
			deployAccArtifact(aaClient);
		}
        else {
            deployArtifact(aaClient);
        }
        setupAgent(aaClient);
    }
   
    private void deployAccArtifact(IAutomationAgentClient aaClient) {
    
        //get agent
        AccAgentDownloadFlowContext downloadContext = new AccAgentDownloadFlowContext.Builder()
            //.url(AgentRegressionBaseTestBed.ACC_WEBSPHERE_WIN_AGENT_URL)
            .packageName(accPackageName)
            .osName(accPackageOsName)
            .accServerUrl(accServerUrl)
            .installDir(appserverDir)
            .build(); 
        
        runFlow(aaClient, AccAgentDownloadFlow.class, downloadContext);     
        
        //copy WebAppSupport (until it's implemented as dynamic ext in 10.6) 
        /*
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .copy(appserverDir + "/wily/WebAppSupport.jar", 
                appserverDir + "/wily/common/WebAppSupport.jar")
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
        */
        
        //temporary remove poc bundle.props until it's fixed in the build
        //2/13/17 - poc-base-profile was temporary removed OOTB from the bundle
        //commenting this flow for now
        /*
        context = new FileModifierFlowContext.Builder()
            .deleteFiltered(appserverDir + "/wily/extensions/deploy", 
                new TasFileNameFilter("poc-base-profile.*tar\\.gz", TasFileNameFilter.FilterMatchType.REGULAR_EXPRESSION))     
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
        */
        
        //rename wily dir
        FileModifierFlowContext updateDirContext = new FileModifierFlowContext.Builder()
            .move(appserverDir + "/wily", appserverDir + "/wily_" + serverName)
            .build();
        runFlow(aaClient, FileModifierFlow.class, updateDirContext);
    }
    
    private void deployArtifact(IAutomationAgentClient aaClient) {
        
        String artifact = "agent-noinstaller-websphere-windows";
        if(isLegacyMode) {
            artifact = "agent-legacy-noinstaller-websphere-windows";
        }
        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.delivery", 
            artifact, "", "zip", AgentRegressionBaseTestBed.getAgentArtifactVersion(tasResolver)));            
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
        String agentJar = "Agent.jar";
        String agentProfile = "IntroscopeAgent.profile";
        
        if(isNoRedefEnabled) {
            agentJar = "AgentNoRedefNoRetrans.jar";
            if (accPackageName == null) { //use noredef profile for non-acc package only
                agentProfile = "IntroscopeAgent.NoRedef.profile";
            }
        }
        
        command = new RunCommandFlowContext.Builder("wsadmin.bat")
            .workDir(appserverDir + "/bin")
            .args(Arrays.asList("-f", installScriptPath + "/setup_apm_agent.jacl", 
                appserverDir + "/wily_" + serverName, serverName, agentProfile, agentJar))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);        
       
        //stop was     
        command = new RunCommandFlowContext.Builder("stopServer.bat")
            .workDir(appserverDir + "/bin/")
            .args(Arrays.asList(serverName))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    }
 
    public static class Builder extends BuilderBase<Builder, WASAgentDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;  
        protected String appserverDir;     
        protected String serverName;     
        protected String installScriptPath;
        protected boolean isLegacyMode;
        protected boolean isNoRedefEnabled = false;
        
		// for pulling acc package
		private String accServerUrl;
		private String accPackageName;
		private String accPackageOsName;
        
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
        
        public Builder accServerUrl(String url) {
            this.accServerUrl = url;
            return builder();
        }
        
        public Builder accPackageName(String accPackageName) {
            this.accPackageName = accPackageName;
            return builder();
        }
        
        public Builder accPackageOsName(String accPackageOsName) {
            this.accPackageOsName = accPackageOsName;
            return builder();
        }
        
        public Builder isNoRedefEnabled(boolean isNoRedefEnabled) {
            this.isNoRedefEnabled = isNoRedefEnabled;
            return builder();
        }
    }
}