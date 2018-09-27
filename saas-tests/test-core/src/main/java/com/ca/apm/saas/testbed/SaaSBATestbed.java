package com.ca.apm.saas.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH7;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.saas.role.ClientDeployRole;
import com.ca.apm.saas.role.FileUpdateRole;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * SAAS testbed for Browser agent (BA)
 * 
 * @author akujo01
 */
@TestBedDefinition
public class SaaSBATestbed implements ITestbedFactory {

    public static final String MACHINE1WIN_ID 			= "machine1Win";
    public static final String MACHINE2RH_ID 			= "machine2RH";
    public static final String HUB_ROLE_ID 				= "seleniumHubRole";
    public static final String NODE_ROLE_ID 			= "seleniumNodeRole";
    public static final String LOAD_ROLE_ID 			= "seleniumLoadRole";
    public static final String WIN_DEPLOY_BASE 			= TasBuilder.WIN_SOFTWARE_LOC;
    public static final String RH7_DEPLOY_BASE 			= TasBuilder.LINUX_SOFTWARE_LOC;
	public static final String TOMCAT_ROLE_ID			= "tomcatRole";
	public static final String TOMCAT_8_INSTALL_DIR 	= "tomcat" + TomcatVersion.v80;
	public static final String TOMCAT_8_HOME			= RH7_DEPLOY_BASE + TOMCAT_8_INSTALL_DIR;
	public static final String TOMCAT_BRTM				= TOMCAT_8_HOME + "/webapps/brtmtestapp.war";
    public static final String WEBAPP_ROLE_ID 			= "webAppRole";
	public static final String TOMCAT_SCRIPT_ROLE_ID 	= "tomcatStartScript";
	public static final int    TOMCAT_PORT              = 8080;
	public static final String TOMCAT_AGENT_NAME		= "TomcatSpring_BRTMTestApp"; 
	
    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed("SaasBATestbed");

        // add client machine (with selenium)
        ITestbedMachine clientMachine =
            new TestbedMachine.Builder(MACHINE1WIN_ID).templateId(TEMPLATE_W64).build();

        SeleniumGridNodeRole nodeRole =
            new SeleniumGridNodeRole.Builder(NODE_ROLE_ID, tasResolver)
                .nodeConfiguration(createConfiguration(tasResolver))
                .qResXResolution("1920")
                .qResYResolution("1080")
                .chromeDriver(SeleniumChromeDriver.V2_29_B32)
                .build();

        SeleniumGridHubRole hubRole =
            new SeleniumGridHubRole.Builder(HUB_ROLE_ID, tasResolver)
                .addNodeRole(nodeRole).build();

        ClientDeployRole clientRole =
            new ClientDeployRole.Builder(LOAD_ROLE_ID, tasResolver)
            .shouldDeployJmeter(false)
            .shouldDeployStressapp(false)
            .browserInstallWait(180)
            .build();

        clientMachine.addRole(hubRole, nodeRole, clientRole);
        nodeRole.before(hubRole);
        hubRole.before(clientRole);
        testbed.addProperty("selenium.webdriverURL",
            "http://" + tasResolver.getHostnameById(HUB_ROLE_ID) + ":4444/wd/hub");
 
        // add agent machine
        ITestbedMachine agentMachine = new TestbedMachine
        		.LinuxBuilder(MACHINE2RH_ID)
        		.templateId(TEMPLATE_RH7)
        		.build();

        initAgentMachine(agentMachine, tasResolver);
        testbed.addMachine(clientMachine, agentMachine);  
        return testbed;
        
    }

    private void initAgentMachine(ITestbedMachine machine, ITasResolver tasResolver) {

    	//Deploy BRTMTestApp on Tomcat with JAVA_HOME=/opt/jdk1.8 
       TomcatRole tomcatRole = new TomcatRole.LinuxBuilder(TOMCAT_ROLE_ID, tasResolver)
			        .installDir(TOMCAT_8_HOME)
			        .tomcatVersion(TomcatVersion.v80)
			        .tomcatCatalinaPort(TOMCAT_PORT)
			        .build();
        GenericRole BRTMWebAppRole = new GenericRole.Builder(WEBAPP_ROLE_ID,tasResolver)
						.download(new DefaultArtifact("com.ca.apm.coda-projects.test-tools", "brtmtestapp", "war", "master-SNAPSHOT"),TOMCAT_BRTM)
						.build();              
        
        ExecutionRole startTomcatRole = updateTomcatStartScriptLinux(tasResolver,TOMCAT_8_HOME);
        startTomcatRole.after(tomcatRole);
        BRTMWebAppRole.after(startTomcatRole);
        machine.addRole(tomcatRole, startTomcatRole, BRTMWebAppRole);
        
    }
    
	private ExecutionRole updateTomcatStartScriptLinux(ITasResolver tasResolver,String tomcatHome){
		
		String tomcatJavaOptsFile = tomcatHome + "/bin/setenv.sh";
		System.out.println("fileName "  + tomcatJavaOptsFile);
		HashMap<String,String> javaOpts = new HashMap<String,String>();
		
		String originalString = "JAVA_HOME=\"/opt/jdk1.8\";export JAVA_HOME";
		String updatedString = "export JAVA_HOME=\"/opt/jdk1.8\"\n" + 
					"export JAVA_OPTS=\"\\${JAVA_OPTS} " + 
					"-javaagent:" + TOMCAT_8_HOME + "/wily/Agent.jar " +
					"-Dcom.wily.introscope.agentProfile=" + TOMCAT_8_HOME + "/wily/core/config/IntroscopeAgent.profile " +
					"-Dcom.wily.introscope.agent.agentName=" + TOMCAT_AGENT_NAME + "\"";
		javaOpts.put(originalString,updatedString);

		FileModifierFlowContext context = new FileModifierFlowContext.Builder()
				.replace(tomcatJavaOptsFile, javaOpts)
				.build();
		ExecutionRole startTomcatRole = new ExecutionRole.Builder(TOMCAT_SCRIPT_ROLE_ID)
        		.flow(FileModifierFlow.class, context)
        		.build();

		return startTomcatRole;
    
	}
	
    private NodeConfiguration createConfiguration(ITasResolver tasResolver) {

        List<NodeCapability> capabilities = new ArrayList<NodeCapability>();

        NodeCapability chromeCapability =
            new NodeCapability.Builder().browserType(BrowserType.CHROME)
                .platform(NodePlatform.WINDOWS).maxInstances(8).build();
        capabilities.add(chromeCapability);

        String hubHostname = tasResolver.getHostnameById(HUB_ROLE_ID);
        URL hubUrl;
        try {
            hubUrl = new URL("http", hubHostname, 4444, "/grid/register/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return new NodeConfiguration.Builder().hub(hubUrl).maxSession(7)
            .addCapabilities(capabilities).build();
    }
}
