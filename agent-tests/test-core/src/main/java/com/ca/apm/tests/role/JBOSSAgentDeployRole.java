package com.ca.apm.tests.role;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author ahmal01@ca.com
 */
public class JBOSSAgentDeployRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JBOSSAgentDeployRole.class);
    private ITasResolver tasResolver;
    private String appserverDir;
    private String serverName; 
    private String additionalJavaOptions;
    private String javaInstallDir;
    private boolean isLegacyMode;
    private String platform;
    private boolean isAccAgentBundle;   
 
    protected JBOSSAgentDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.serverName = builder.serverName;
        this.tasResolver = builder.tasResolver;
        this.appserverDir = builder.appserverDir;
        this.additionalJavaOptions = builder.additionalJavaOptions;
        this.javaInstallDir = builder.javaInstallDir;
        this.isLegacyMode = builder.isLegacyMode;
        this.platform = builder.platform;
        this.isAccAgentBundle = builder.isAccAgentBundle;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        if(isAccAgentBundle) {
            deployAccArtifact(aaClient);
        }
        else {
            deployAgentArtifact(aaClient);
        }
        deployStartupScripts(aaClient);
        updateJbossCli(aaClient);
        updateStandaloneConf(aaClient);
    }
   
    private void deployAccArtifact(IAutomationAgentClient aaClient) {
       
        //get agent
        AccAgentDownloadFlowContext downloadContext = new AccAgentDownloadFlowContext.Builder()
           // .url(AgentRegressionBaseTestBed.ACC_JBOSS_WIN_AGENT_URL)
            .packageName("JBoss - Spring")
            .osName("windows")
            .installDir(appserverDir)
            .build(); 
        runFlow(aaClient, AccAgentDownloadFlow.class, downloadContext);     
        
    }
    
    private void deployAgentArtifact(IAutomationAgentClient aaClient) {
        
        String artifact = "agent-noinstaller-jboss-" + platform;
        if(isLegacyMode) {
            artifact = "agent-legacy-noinstaller-jboss-" + platform;
        }
        
        String extension = "zip";
        if(platform.equalsIgnoreCase("unix")) {
            extension = "tar"; 
        }
        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.delivery", 
            artifact, "", extension, AgentRegressionBaseTestBed.getAgentArtifactVersion(tasResolver))); 
        LOGGER.info("Downloading agent artifact " + url.toString());
        
        //get wily bundle
        GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(appserverDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, getAgentContext); 
        
        //backup org agent profile 
        String profile = appserverDir + "/wily/core/config/IntroscopeAgent.profile";
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .copy(profile, profile + ".original")
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }   
   
    private void deployStartupScripts(IAutomationAgentClient aaClient) {
        
        DefaultArtifact startupScriptArtifact;
        
        if(serverName.contains("wildfly9")) {
            startupScriptArtifact = new DefaultArtifact("com.ca.apm.binaries.wildfly", "wildfly.startup.scripts", "tas", "zip", "9.0");
        }
        else if(serverName.contains("wildfly11")) {
            startupScriptArtifact = new DefaultArtifact("com.ca.apm.binaries.wildfly", "wildfly.startup.scripts", "tas", "zip", "11.0");
        }
        else {
            startupScriptArtifact = new DefaultArtifact("com.ca.apm.binaries.jboss-as", "jboss.startup.scripts", "zip", "7.1.1");
        }      
        
       GenericFlowContext context = null;
       LOGGER.info("Deploying startup scripts.");
        //get Startup Scripts
        context = new GenericFlowContext.Builder()
           .artifactUrl(tasResolver.getArtifactUrl(startupScriptArtifact))
           .destination(appserverDir + "/bin")
           .build();  
        runFlow(aaClient, GenericFlow.class, context);        
    }

    private void updateJbossCli(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        HashMap<String,String> replacePairs = null;

        //update jboss-cli bat file
        String file = appserverDir + "/bin/jboss-cli.bat";
        if(platform.equalsIgnoreCase("unix")) {
            file = appserverDir + "/bin/jboss-cli.sh";
        }
        LOGGER.info("Updating file " + file);

        replacePairs = new HashMap<String,String>();
        replacePairs.put("\\[JAVA_HOME\\]", javaInstallDir);
                
        context = new FileModifierFlowContext.Builder()
        .replace(file, replacePairs)
        .build();

        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void updateStandaloneConf(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        HashMap<String,String> replacePairs = null;

        //update standalone-conf bat file
        String file = appserverDir + "/bin/standalone.conf.bat";
        if(platform.equalsIgnoreCase("unix")) {
            file = appserverDir + "/bin/standalone.conf.sh";
        }
        LOGGER.info("Updating file " + file);

        replacePairs = new HashMap<String,String>();

        String agentJavaOptions = additionalJavaOptions + " -javaagent:" + appserverDir + "/wily/Agent.jar " +
                "-Dcom.wily.introscope.agentProfile=" + appserverDir + "/wily/core/config/IntroscopeAgent.profile";
        
        replacePairs.put("\\[JAVA_HOME\\]", javaInstallDir);
        replacePairs.put("\\[MIN.HEAP.SIZE\\]","256");
        replacePairs.put("\\[MAX.HEAP.SIZE\\]","512");
        replacePairs.put("\\[MAX.PERM.SPACE.SIZE\\]","512");
        replacePairs.put("\\[AGENT.JAVA.OPTIONS\\]",agentJavaOptions);
                
        context = new FileModifierFlowContext.Builder()
        .replace(file, replacePairs)
        .build();

        runFlow(aaClient, FileModifierFlow.class, context);
    }
    
    public static class Builder extends BuilderBase<Builder, JBOSSAgentDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;  
        protected String appserverDir;     
        protected String serverName;
        protected String additionalJavaOptions;
        protected String javaInstallDir;
        protected boolean isLegacyMode;
        protected String platform;
        protected boolean isAccAgentBundle = false;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public JBOSSAgentDeployRole build() {
            return getInstance();
        }

        @Override
        protected JBOSSAgentDeployRole getInstance() {
            return new JBOSSAgentDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder appserverDir(String appserverDir) {
            this.appserverDir = appserverDir;
            return builder();
        }
        
        public Builder platform(String platform) {
            this.platform = platform;
            return builder();
        }   
   
        public Builder additionalJavaOptions(String additionalJavaOptions) {
            this.additionalJavaOptions = additionalJavaOptions;
            return builder();
        }
        
        public Builder javaInstallDir(String javaInstallDir) {
            this.javaInstallDir = javaInstallDir;
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
        
        public Builder isAccAgentBundle(boolean isAccAgentBundle) {
            this.isAccAgentBundle = isAccAgentBundle;
            return builder();
        }
    }
}