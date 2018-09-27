package com.ca.apm.saas.role;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

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
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;


/**
 * @author banra06, Abhishek Sinha, akujo01
 */
public class ClientDeployRole extends AbstractRole {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientDeployRole.class);
	private ITasResolver tasResolver;
	Process jmeterProcess = null;	
	public static final String[] allAgentTypes = {"GlassFish","Java","JBoss","Tomcat","WebLogic","WebSphere"};   
    public static final String[] allAgentSubTypes = {"Java","Spring"};    
    public static final String ALL_AGENTS_LOC = TasBuilder.WIN_SOFTWARE_LOC + "agents\\";
    private boolean shouldDeployStressapp;
    private boolean shouldDeployJmeter;
    private int browserInstallWait;
    
    public static final String JMETER_HOME = TasBuilder.WIN_SOFTWARE_LOC + "jmeter";
    public static final String JMETER_INSTALLER_DIR = JMETER_HOME + "/apache-jmeter-3.1";
    public static final String JMETER_SCRIPTS_DIR = JMETER_HOME + "/scripts";
    
	protected ClientDeployRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.shouldDeployJmeter = builder.shouldDeployJmeter;
	    this.shouldDeployStressapp = builder.shouldDeployStressapp;
	    this.browserInstallWait = builder.browserInstallWait;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {
		
		 if(shouldDeployStressapp) {
		     deployStressApp(aaClient);	
		 }
		 deployChromeBrowser(aaClient);
		 deployChromeDriver(aaClient);
		 
		 if(shouldDeployJmeter) {
		     deployJmeter(aaClient);
		 }
	}
	
	private void createStressLoadBatchFile(IAutomationAgentClient aaClient, String agentType, String subPackageType) {
		
		String agentFolder = ALL_AGENTS_LOC+agentType+subPackageType;
		String agentJar = "Agent.jar";
		String agentProfile = "IntroscopeAgent.profile";
		
		if(agentType.equalsIgnoreCase("websphere")) {
		    agentJar = "AgentNoRedefNoRetrans.jar";
		}
		
		String parameters = "/stressapp/StressApp.jar doSQL=true,doWideStructure=true,doErrors=true,maxAppIndex=10,maxMetricIndex=5,stackDepth=10,maxBackendIndex=5,sleepOnMethods=70,testDuration=45000000,threadServiceMaxThreads=100,numberOfConcurrentUsers=20,savePid=pid.txt";		
		Collection<String> createBatch = Arrays.asList("java -javaagent:"
						+ agentFolder
						+ "/wily/" + agentJar + " -Dcom.wily.introscope.agentProfile="
						+ agentFolder
						+ "/wily/core/config/" + agentProfile + " "
						+ "-Dintroscope.agent.agentAutoNamingEnabled=false "
						+ "-Dcom.wily.introscope.agent.agentName="+(agentType+subPackageType)+"_StressApp "
						+ "-Dcom.wily.autoprobe.logSizeInKB=100000  -Duser.dir="
						+ agentFolder
						+ " -Dlog4j.configuration=file:"
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/stressapp/resources/log4j-StressApp.properties -classpath "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/stressapp/lib/* -Xms256m -Xmx512m -XX:PermSize=20m -XX:MaxPermSize=30m -XX:+UseSerialGC -XX:+HeapDumpOnOutOfMemoryError -verbosegc -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:"
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/stressapp/RemoteConfig_agent_true.gc.log -Dcom.wily.autoprobe.logSizeInKB=100000 -jar "
						+ TasBuilder.WIN_SOFTWARE_LOC + parameters);

		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(agentFolder + "/StressLoad.bat",
						createBatch).build();
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
	}
	
	private void deployChromeDriver(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.binaries.selenium", "chromedriver", "win32", "zip", "2.33"));
		LOGGER.info("Downloading artifact " + url.toString());

		GenericFlowContext context = new GenericFlowContext.Builder()
				.artifactUrl(url)
				.destination(
						TasBuilder.WIN_SOFTWARE_LOC
								+ "/seleniumgrid").build();
	
		runFlow(aaClient, GenericFlow.class, context);		
	}
	
	private void deployChromeBrowser(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.binaries.selenium", "chromesetup", "win32", "zip", "58"));
		LOGGER.info("Downloading artifact " + url.toString());

		GenericFlowContext context = new GenericFlowContext.Builder()
				.artifactUrl(url)
				.destination(
						TasBuilder.WIN_SOFTWARE_LOC
								+ "/chromeBrowser").build();
		
		runFlow(aaClient, GenericFlow.class, context);
		createAndRunBatchFile(aaClient);
		
	}
	
	private void createAndRunBatchFile(IAutomationAgentClient aaClient) {
		Collection<String> createBatch = Arrays
				.asList("start " 
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/chromeBrowser/ChromeSetup.exe");

		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/chromeBrowser/InstallChrome.bat",
						createBatch).build();
		//create batch file
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
		
		//run batch file
		RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
				"InstallChrome.bat").workDir(TasBuilder.WIN_SOFTWARE_LOC + "/chromeBrowser" )
				.build();
		if(browserInstallWait > 0) {
		    runCommandFlowAsync(aaClient, runCmdFlowContext, browserInstallWait);
		}
		else {
		    runFlow(aaClient, RunCommandFlow.class, runCmdFlowContext);
		}
	}
	
	private void deployStressApp(IAutomationAgentClient aaClient) {
        
		deployZipArtifact(aaClient, "stressapp", "jvm8-dist");
		
		for(String agentType:allAgentTypes){
            if(agentType.equals("Java")){
                createStressLoadBatchFile(aaClient,agentType,"");
                continue;
            }
            for(String subPackageType:allAgentSubTypes){
                createStressLoadBatchFile(aaClient,agentType,subPackageType);
            }
        }
	}

	private void deployZipArtifact(IAutomationAgentClient aaClient,
			String artifactId, String classifier) {

		deployZipArtifact(aaClient, "com.ca.apm.coda-projects.test-tools",
				artifactId, classifier, artifactId);
	}

	private void deployZipArtifact(IAutomationAgentClient aaClient,
			String groupId, String artifactId, String classifier, String homeDir) {

		URL url = tasResolver
				.getArtifactUrl(new DefaultArtifact(groupId, artifactId,
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
	    .destination(JMETER_INSTALLER_DIR)
	    .build();  
	    runFlow(aaClient, GenericFlow.class, context);  
	       
	}
	
	public static class Builder extends BuilderBase<Builder, ClientDeployRole> {

		private final String roleId;
		private final ITasResolver tasResolver;		
		private boolean shouldDeployJmeter = true;
		private boolean shouldDeployStressapp = true;
		private boolean updateChromeBrowser = true;
		private int browserInstallWait = 0;

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public ClientDeployRole build() {
			linkStressAppLoads();
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
		
		public Builder updateChromeBrowser(boolean updateChromeBrowser) {
		    
            this.updateChromeBrowser = updateChromeBrowser;           
            return builder();
        }
	
		public Builder shouldDeployJmeter(boolean shouldDeployJmeter) {
		    
            this.shouldDeployJmeter = shouldDeployJmeter;           
            return builder();
        }
		
		public Builder shouldDeployStressapp(boolean shouldDeployStressapp) {
            
            this.shouldDeployStressapp = shouldDeployStressapp;           
            return builder();
        }
		
		public Builder browserInstallWait(int browserInstallWait) {
            
            this.browserInstallWait = browserInstallWait;           
            return builder();
        }
		
		private void linkStressAppLoads() {			
			for(String agentType:allAgentTypes){
				if(agentType.equals("Java")){
					linkStressLoad(agentType,"");
					continue;
				}
				for(String subPackageType:allAgentSubTypes){
					linkStressLoad(agentType,subPackageType);
				}
			}
		}
		
		private void linkStressLoad(String agentType, String subPackageType){
			String agentFolder = ALL_AGENTS_LOC+agentType+subPackageType;
			String linkString = agentType+subPackageType+"_stressapp_load_start";
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"StressLoad.bat").workDir(agentFolder)
					.terminateOnMatch("Up and Running.").build();
			getEnvProperties().add(linkString, runCmdFlowContext);
		}
		
		
	}
}