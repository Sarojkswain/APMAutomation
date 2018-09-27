/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

package com.ca.apm.saas.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.saas.role.ClientDeployRole;
import com.ca.apm.saas.role.FileUpdateRole;
import com.ca.apm.saas.role.JBossDeployRole;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;


/**
 * SeleniumTestBed class.
 *
 * Testbed description.
 */
@TestBedDefinition
public class SaasUITestbed implements ITestbedFactory {
    
	public static final String MACHINE_ID                   = "testMachine";
	public static final String TEMPLATE_ID                  = TEMPLATE_W64;
	public static final String HUB_ROLE_ID 					= "seleniumHubRole";
	public static final String NODE_ROLE_ID 				= "seleniumNodeRole";
	public static final String LOAD_ROLE_ID 				= "seleniumLoadRole";
    public static final String DEPLOY_BASE                  = TasBuilder.WIN_SOFTWARE_LOC;
    public static final String WEBAPP_ROLE_ID 				= "webAppRole";
	public static final String TOMCAT_ROLE_ID 				= "tomcatRole";
	public static final String TOMCAT_SCRIPT_UPDATE_ROLE_ID = "tomcatStartScript";
	public static final String JBOSS_ROLE_ID 				= "jbossRole";
	public static final String JBOSS_SCRIPT_UPDATE_ROLE_ID 	= "jbossStartScript";
	public static final String TOMCAT_8_INSTALL_DIR 		= "tomcat" + TomcatVersion.v80;
	public static final String TOMCAT_8_HOME				= DEPLOY_BASE + TOMCAT_8_INSTALL_DIR;
	public static final String TOMCAT_THIEVES				= TOMCAT_8_HOME + "/webapps/thieves.war";
	public static final String JBOSS_INSTALL_DIR			= ""+JBossVersion.WILDFLY900;
	public static final String JBOSS_HOME 					= DEPLOY_BASE + JBOSS_INSTALL_DIR;
	private static final String TIXMONSTER_ROLE_ID			= "tixMonsterRole";
	public static final String JBOSS_TIXMONSTER				= JBOSS_HOME+"/standalone/deployments/ticket-monster.war";
	public static final String JBOSS_START_ROLE_ID			= "JBossStartRoleID";
    public static final int TOMCAT_PORT                     = 9091;
    public static final String JMETER_PARENT_HOME          = DEPLOY_BASE + "/jmeter";
    public static final String JMETER_SCRIPTS_ROLE_ID      = "jmeterScriptsRole"; 
    public static final String JMETER_HOME                 = JMETER_PARENT_HOME + "/apache-jmeter-3.1";
    public static final String JMETER_SCRIPTS_HOME         = JMETER_PARENT_HOME + "/scripts";
  
	@Override
	public ITestbed create(ITasResolver tasResolver) {

		ITestbed testbed = new Testbed("SaaS UI Testbed");

		final SeleniumGridNodeRole nodeRole = new SeleniumGridNodeRole.Builder(
				NODE_ROLE_ID, tasResolver)
				.nodeConfiguration(createConfiguration(tasResolver))
				.qResXResolution("1920").qResYResolution("1080")
				.chromeDriver(SeleniumChromeDriver.V2_29_B32).build();
				
		final SeleniumGridHubRole hubRole = new SeleniumGridHubRole.Builder(
				HUB_ROLE_ID, tasResolver).addNodeRole(nodeRole).build();
		final ClientDeployRole loadRole = new ClientDeployRole.Builder(LOAD_ROLE_ID, tasResolver)
											.build();
	
		ITestbedMachine testMachine = new TestbedMachine.Builder(
				MACHINE_ID).templateId(TEMPLATE_ID).build();
		testMachine.addRole(hubRole);
		testMachine.addRole(nodeRole);
		testMachine.addRole(loadRole);
		nodeRole.before(hubRole);
		hubRole.before(loadRole);
	
		String hubHostName = tasResolver.getHostnameById(HUB_ROLE_ID);
        testbed.addProperty("selenium.webdriverURL", "http://" + hubHostName + ":4444/wd/hub");        
      
        //Deploy Thieves on Tomcat
        //TODO add property to testbed, read it from testbed properties in the test.  add a higher level appserver folder.
        testbed.addProperty("tomcat.thieves.install.dir", TOMCAT_8_INSTALL_DIR);
        
        GenericRole thievesAppRole = new GenericRole.Builder(WEBAPP_ROLE_ID,tasResolver)
						.download(new DefaultArtifact("com.ca.apm", "thieves", "war","0.0.1-SNAPSHOT"),TOMCAT_THIEVES)
						.build();        
        
        TomcatRole tomcatRole = new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver)
				        .installDir(TOMCAT_8_HOME)
				        .tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(TOMCAT_PORT)
				        .build();
        
        FileUpdateRole tomcatFileUpdateRole = updateTomcatStartScript(tasResolver,TOMCAT_8_HOME);

        loadRole.before(tomcatRole);
        tomcatRole.before(thievesAppRole);
        tomcatRole.before(tomcatFileUpdateRole);
        testMachine.addRole(tomcatRole, thievesAppRole,tomcatFileUpdateRole);		        
        
        //Deploy Ticket Monster on JBoss/ Wildfly 9.0
        testbed.addProperty("jboss.tixmonster.install.dir",JBOSS_INSTALL_DIR);
        GenericRole tixMonsterAppRole = new GenericRole.Builder(TIXMONSTER_ROLE_ID,tasResolver)
        				.download(new DefaultArtifact("com.ca.apm.binaries.testapps","ticket-monster",
        							"war","2.6.0"),JBOSS_TIXMONSTER)
        				.build();        										
       
        JbossRole jbossRole = new JbossRole.Builder(JBOSS_ROLE_ID, tasResolver)
						.jbossInstallDirectory(JBOSS_HOME)
        				.version(JBossVersion.WILDFLY900)
        				.build();
        
        FileUpdateRole jBossFileUpdateRole = updateJbossScript(tasResolver,JBOSS_HOME);
        
        final JBossDeployRole JBossStartRole = new JBossDeployRole.Builder(JBOSS_START_ROLE_ID, tasResolver)
													.installDir(JBOSS_HOME)
													.build();         
        thievesAppRole.before(jbossRole);
        jbossRole.before(jBossFileUpdateRole);
        jbossRole.before(tixMonsterAppRole);
        jBossFileUpdateRole.before(JBossStartRole);
        testMachine.addRole(jbossRole,jBossFileUpdateRole,tixMonsterAppRole,JBossStartRole);

        testbed.addProperty("jmeter.install.dir", JMETER_HOME);
        testbed.addProperty("jmeter.scripts.install.dir", JMETER_SCRIPTS_HOME);
        GenericRole jmeterScriptsRole = createJmeterScriptsRole(tasResolver); 
        testMachine.addRole(jmeterScriptsRole);
        
        testbed.addMachine(testMachine);
    	return testbed;
	}

	private NodeConfiguration createConfiguration(ITasResolver tasResolver) {
		List<NodeCapability> capabilities = new ArrayList<NodeCapability>();

		NodeCapability chromeCapability = new NodeCapability.Builder()
				.browserType(BrowserType.CHROME).platform(NodePlatform.WINDOWS)
				.maxInstances(8).build();
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
	
	private FileUpdateRole updateTomcatStartScript(ITasResolver tasResolver,String tomcatDeployPath){
		String fileName = tomcatDeployPath + "/bin/catalina.bat";
		HashMap<String,String> addJavaOpts = new HashMap<String,String>();
		
		String originalString = "set \"JAVA_OPTS=%JAVA_OPTS% %LOGGING_MANAGER%\"";
		String updatedString = 	"set \"AGENT_JAR=%CATALINA_HOME%\\\\wily\\\\Agent.jar\"\n"+
								"set \"AGENT_PROFILE=%CATALINA_HOME%\\\\wily\\\\core\\\\config\\\\IntroscopeAgent.profile\"\n"+
								"set \"JAVA_OPTS=%JAVA_OPTS% %LOGGING_MANAGER% "
								+ "-javaagent:%AGENT_JAR% "
								+ "-Dcom.wily.introscope.agentProfile=%AGENT_PROFILE% "
								+ "-Dcom.wily.introscope.agent.agentName=TomcatSpring_Thieves\"";

		String fileFinishPrompt_orig = ":end";
		String fileFinishPrompt_updated = ":end\n"
											+"echo Tomcat started";
		
		
		addJavaOpts.put(originalString,updatedString);
		addJavaOpts.put(fileFinishPrompt_orig,fileFinishPrompt_updated);
		
		FileUpdateRole fileUpdateRole = new FileUpdateRole.Builder(
				TOMCAT_SCRIPT_UPDATE_ROLE_ID, tasResolver).filePath(fileName)
				.replacePairs(addJavaOpts).build();
		return fileUpdateRole;
	}
	

	private FileUpdateRole updateJbossScript(ITasResolver tasResolver,String jBossDeployPath){
		String fileName = jBossDeployPath + "/bin/standalone.bat";
		HashMap<String,String> addJavaOpts = new HashMap<String,String>();
		
		String originalString = "set \"JAVA_OPTS=-Dprogram.name=%PROGNAME% %JAVA_OPTS%\"";
		String updatedString = "set \"AGENT_HOME=%JBOSS_HOME%\\\\wily\"\n"+						   
							   "set \"JAVA_OPTS=-Dprogram.name=%PROGNAME% %JAVA_OPTS% "
							   + "-Djboss.modules.system.pkgs=org.jboss.byteman,org.jboss.logmanager,com.wily,com.wily.* "
							   + "-Djava.util.logging.manager=org.jboss.logmanager.LogManager "
							   + "-javaagent:%AGENT_HOME%\\\\Agent.jar "
							   + "-DagentProfile=%AGENT_HOME%\\\\core\\\\config\\\\IntroscopeAgent.profile "
							   + "-Dcom.wily.introscope.agent.agentName=JBossSpring_TicketMonster "
							   + "-Xbootclasspath/p:%JBOSS_HOME%\\\\modules\\\\system\\\\layers\\\\base\\\\org\\\\jboss\\\\log4j\\\\logmanager\\\\main\\\\log4j-jboss-logmanager-1.1.2.Final.jar;%JBOSS_HOME%\\\\modules\\\\system\\\\layers\\\\base\\\\org\\\\jboss\\\\logmanager\\\\main\\\\jboss-logmanager-2.0.0.Final.jar;%JBOSS_HOME%\\\\modules\\\\system\\\\layers\\\\base\\\\org\\\\slf4j\\\\impl\\\\main\\\\slf4j-jboss-logmanager-1.0.3.GA.jar\"";
		
		/*String fileFinishPrompt_orig = ":end";
		String fileFinishPrompt_updated = ":end\n"
											+"echo Tomcat started";*/
		
		addJavaOpts.put(originalString,updatedString);
		//addJavaOpts.put(fileFinishPrompt_orig,fileFinishPrompt_updated);
		
		FileUpdateRole fileUpdateRole = new FileUpdateRole.Builder(
				JBOSS_SCRIPT_UPDATE_ROLE_ID, tasResolver).filePath(fileName)
				.replacePairs(addJavaOpts).build();
		return fileUpdateRole;
	}
	
    private static GenericRole createJmeterScriptsRole(ITasResolver tasResolver) {
        
        DefaultArtifact loadJmx =
            new DefaultArtifact("com.ca.apm.saas", "saas-tests-core", "jmeterscripts", "zip",
                tasResolver.getDefaultVersion());
 
        GenericRole jmeterScriptsRole = new GenericRole.Builder(JMETER_SCRIPTS_ROLE_ID, tasResolver)
            .unpack(loadJmx, JMETER_SCRIPTS_HOME)
            .build();
 
        return jmeterScriptsRole;
    }    
}