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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
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
import com.ca.apm.saas.role.ClientDeployRole;
import com.ca.apm.saas.role.FileUpdateRole;
import com.ca.apm.saas.role.JBossDeployRole;

/**
 * SAAS  testbed to setup apps and keep them running for demo
 * 
 * @author akujo01
 */
@TestBedDefinition
public class SaasDemoTestbed implements ITestbedFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(SaasDemoTestbed.class);	
	
	public static final String MACHINE1_ID 						= "Machine1";

	public static final String TEMPLATE_ID 						= TEMPLATE_W64;
	
	public static final String HUB1_ROLE_ID 					= "seleniumHub1Role";
	public static final String NODE1_ROLE_ID 					= "seleniumNode1Role";
	public static final String LOAD1_ROLE_ID 					= "seleniumLoadRole";
	public static final String CIENT_ROLE_ID 					= "clientRole";
	public static final String THIEVES_ROLE_ID 					= "ThievesRole";
    
    public static final String TOMCAT1_ROLE_ID					= "tomcat1Role";	
    public static final String TOMCAT2_ROLE_ID					= "tomcat2Role";	
    public static final String TOMCAT3_ROLE_ID					= "tomcat3Role";	
    public static final String TOMCAT4_ROLE_ID					= "tomcat4Role";	

	public static final String TOMCAT1_SCRIPT_UPDATE_ROLE_ID 	= "tomcat1StartScript";
	public static final String TOMCAT2_SCRIPT_UPDATE_ROLE_ID 	= "tomcat2StartScript";
	public static final String TOMCAT3_SCRIPT_UPDATE_ROLE_ID 	= "tomcat3StartScript";
	public static final String TOMCAT4_SCRIPT_UPDATE_ROLE_ID 	= "tomcat4StartScript";
	

	public static final String MATH_CLIENT_ROLE_ID 				= "mathClientRole";   
    public static final String MATH_PROXY_ROLE_ID 				= "mathProxyRole";   
    public static final String MATH_SIMPLE_BACKEND_ROLE_ID 		= "mathSimpleBackendRole";   
    public static final String MATH_COMPLEX_BACKEND_ROLE_ID 	= "mathComplexBackendRole";   
  
    public static final String JBOSS1_ROLE_ID 					= "jboss1Role";
	public static final String JBOSS1_SCRIPT_UPDATE_ROLE_ID 	= "jboss1StartScript";
	
	//Initialize folders
	public static final String DEPLOY_BASE                   	= TasBuilder.WIN_SOFTWARE_LOC;
	public static final String TOMCAT1_INSTALL_DIR 				= "tomcat8_1";
	public static final String TOMCAT2_INSTALL_DIR 				= "tomcat8_2";
	public static final String TOMCAT3_INSTALL_DIR 				= "tomcat8_3";
	public static final String TOMCAT4_INSTALL_DIR 				= "tomcat8_4";
	public static final String TOMCAT1_HOME						= DEPLOY_BASE + TOMCAT1_INSTALL_DIR;
	public static final String TOMCAT2_HOME						= DEPLOY_BASE + TOMCAT2_INSTALL_DIR;
	public static final String TOMCAT3_HOME						= DEPLOY_BASE + TOMCAT3_INSTALL_DIR;
	public static final String TOMCAT4_HOME						= DEPLOY_BASE + TOMCAT4_INSTALL_DIR;

	//Thieves and Math Client on tomcat1, MathProxy on tomcat2, SimpleBackend on tomcat3, ComplexBackend on tomcat4
	public static final String TOMCAT_THIEVES					= TOMCAT1_HOME + "/webapps/thieves.war";
	public static final String TOMCAT_MATH_CLIENT				= TOMCAT1_HOME + "/webapps/MathClient.war";
	public static final String TOMCAT_MATH_PROXY				= TOMCAT2_HOME + "/webapps/MathProxy.war";
	public static final String TOMCAT_MATH_SIMPLE_BACKEND		= TOMCAT3_HOME + "/webapps/MathSimpleBackend.war";
	public static final String TOMCAT_MATH_COMPLEX_BACKEND		= TOMCAT4_HOME + "/webapps/MathComplexBackend.war";

	public static final String JBOSS1_INSTALL_DIR				= "wildfly9_1";
	public static final String JBOSS1_HOME 						= DEPLOY_BASE + JBOSS1_INSTALL_DIR;
	private static final String TIXMONSTER_ROLE_ID				= "tixMonsterRole";
	public static final String JBOSS1_TIXMONSTER				= JBOSS1_HOME + "/standalone/deployments/ticket-monster.war";
	public static final String JBOSS1_START_ROLE_ID				= "JBoss1StartRoleID";
	
	//private static final String TICKET_MONSTER_ROLE_ID = null;
	//public static final String COPY_ROLE_ID = "copyRole";
	 //jmeter roles
    public static final String JMETER_ROLE_ID              = "jmeterRole";
    public static final String JMETER_SCRIPTS_ROLE_ID      = "jmeterScriptsRole";   
    public static final String JMETER_PERMISSIONS_ROLE_ID  = "jmeterPermissionsRole";
    public static final String JMETER_MACHINE              = "jmeterMachine";
    public static final String JMETER_PARENT_HOME          = DEPLOY_BASE + "/jmeter";
    public static final String JMETER_HOME                 = JMETER_PARENT_HOME + "/apache-jmeter-3.1";
    public static final String JMETER_SCRIPTS_HOME         = JMETER_PARENT_HOME + "/scripts";

    @Override
	public ITestbed create(ITasResolver tasResolver) {

		ITestbed testbed = new Testbed("SaaS Demo Testbed");


		ITestbedMachine machine1 = new TestbedMachine.Builder(
				MACHINE1_ID).templateId(TEMPLATE_ID).build();

		
		final ClientDeployRole loadRole = new ClientDeployRole.Builder(LOAD1_ROLE_ID, tasResolver)
												.shouldDeployStressapp(false)
												.build();
		
		machine1.addRole(loadRole);

		final SeleniumGridNodeRole nodeRole = new SeleniumGridNodeRole.Builder(
				NODE1_ROLE_ID, tasResolver)
				.nodeConfiguration(createConfiguration(tasResolver))
				.qResXResolution("1920").qResYResolution("1080")
				.chromeDriver(SeleniumChromeDriver.V2_29_B32).build();
				
		final SeleniumGridHubRole hubRole = new SeleniumGridHubRole.Builder(
				HUB1_ROLE_ID, tasResolver).addNodeRole(nodeRole).build();

		//final CopyFileRole copyRole = new CopyFileRole.Builder(COPY_ROLE_ID, tasResolver).build();

		machine1.addRole(hubRole);
		machine1.addRole(nodeRole);

		//seleniumGridMachine.addRole(copyRole);
		nodeRole.before(hubRole);
		hubRole.before(loadRole);
	
		String hubHostName = tasResolver.getHostnameById(HUB1_ROLE_ID);
        testbed.addProperty("selenium.webdriverURL", "http://" + hubHostName + ":4444/wd/hub");
        
      
        //add a property for tomcat installation to testbed, read it from testbed properties in the test.  
        testbed.addProperty("tomcat1.install.dir", TOMCAT1_INSTALL_DIR);
        testbed.addProperty("tomcat2.install.dir", TOMCAT2_INSTALL_DIR);
        testbed.addProperty("tomcat3.install.dir", TOMCAT3_INSTALL_DIR);
        testbed.addProperty("tomcat4.install.dir", TOMCAT4_INSTALL_DIR);

        //Deploy Ticket Monster on JBoss/ Wildfly 9.0
        testbed.addProperty("jboss1.install.dir",JBOSS1_INSTALL_DIR);
        GenericRole tixMonsterRole = new GenericRole.Builder(TIXMONSTER_ROLE_ID,tasResolver)
        				.download(new DefaultArtifact("com.ca.apm.binaries.testapps","ticket-monster",
        							"war","2.6.0"),JBOSS1_TIXMONSTER)
        				.build();
        										
       
        JbossRole jboss1Role = new JbossRole.Builder(JBOSS1_ROLE_ID, tasResolver)
						.jbossInstallDirectory(JBOSS1_HOME)
        				.version(JBossVersion.WILDFLY900)
        				.build();
        
        FileUpdateRole jBoss1FileUpdateRole = updateJbossStartScript(tasResolver,JBOSS1_HOME, JBOSS1_SCRIPT_UPDATE_ROLE_ID);
        
        
        final JBossDeployRole JBossStartRole = new JBossDeployRole.Builder(JBOSS1_START_ROLE_ID, tasResolver)
        											.installDir(JBOSS1_HOME)
													.build();
        
        
        
        // add to property map, create JMeter roles and add to machine
        testbed.addProperty("jmeter.install.dir", JMETER_HOME);
        testbed.addProperty("jmeter.scripts.install.dir", JMETER_SCRIPTS_HOME);

        ArrayList<IRole> roles = createJmeterRoles(tasResolver);     
        for (IRole role: roles ){
            machine1.addRole(role);
        }
        	
	   	TomcatRole tomcat1Role = new TomcatRole.Builder(TOMCAT1_ROLE_ID, tasResolver)
			       .installDir(TOMCAT1_HOME)
			       .tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(2020)
			       .build();
	
		TomcatRole tomcat2Role = new TomcatRole.Builder(TOMCAT2_ROLE_ID, tasResolver)
			       .installDir(TOMCAT2_HOME)
			       .tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(3030)
			       .build();
		    
		TomcatRole tomcat3Role = new TomcatRole.Builder(TOMCAT3_ROLE_ID, tasResolver)
			       .installDir(TOMCAT3_HOME)
			       .tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(4040)
			       .build();      
		    
		TomcatRole tomcat4Role = new TomcatRole.Builder(TOMCAT4_ROLE_ID, tasResolver)
			       .installDir(TOMCAT4_HOME)
			       .tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(5050)
			       .build();
	    	
 		GenericRole mathClientRole = new GenericRole.Builder(MATH_CLIENT_ROLE_ID,tasResolver)
					.download(new DefaultArtifact("com.mathapp", "MathClient", "war","0.0.1-SNAPSHOT"),TOMCAT_MATH_CLIENT)
					.build();        

		GenericRole mathProxyRole = new GenericRole.Builder(MATH_PROXY_ROLE_ID,tasResolver)
					.download(new DefaultArtifact("com.mathapp", "MathProxy", "war","0.0.1-SNAPSHOT"),TOMCAT_MATH_PROXY)
					.build();  
        
		//Deploy mathComplexRole
		GenericRole mathSimpleBackendRole = new GenericRole.Builder(MATH_SIMPLE_BACKEND_ROLE_ID,tasResolver)
					.download(new DefaultArtifact("com.mathapp", "MathSimpleBackend", "war","0.0.1-SNAPSHOT"),TOMCAT_MATH_SIMPLE_BACKEND)
					.build();        
	    
		//Deploy mathComplexBackendRole
		GenericRole mathComplexBackendRole = new GenericRole.Builder(MATH_COMPLEX_BACKEND_ROLE_ID, tasResolver)
					.download(new DefaultArtifact("com.mathapp", "MathComplexBackend", "war","0.0.1-SNAPSHOT"),TOMCAT_MATH_COMPLEX_BACKEND)
					.build();     
         
	    //Deploy  Thieves
        GenericRole thievesRole = new GenericRole.Builder(THIEVES_ROLE_ID,tasResolver)
					.download(new DefaultArtifact("com.ca.apm", "thieves", "war","0.0.1-SNAPSHOT"),TOMCAT_THIEVES)
					.build();        
    
        FileUpdateRole tomcat1FileUpdateRole = updateTomcatStartScript(tasResolver,TOMCAT1_HOME, TOMCAT1_SCRIPT_UPDATE_ROLE_ID);
        FileUpdateRole tomcat2FileUpdateRole = updateTomcatStartScript(tasResolver,TOMCAT2_HOME, TOMCAT2_SCRIPT_UPDATE_ROLE_ID);
        FileUpdateRole tomcat3FileUpdateRole = updateTomcatStartScript(tasResolver,TOMCAT3_HOME, TOMCAT3_SCRIPT_UPDATE_ROLE_ID);
        FileUpdateRole tomcat4FileUpdateRole = updateTomcatStartScript(tasResolver,TOMCAT4_HOME, TOMCAT4_SCRIPT_UPDATE_ROLE_ID);
       
        tomcat1Role.before( mathClientRole, thievesRole, tomcat1FileUpdateRole);
        tomcat2Role.before(mathProxyRole,tomcat2FileUpdateRole);
        tomcat3Role.before(mathSimpleBackendRole,tomcat3FileUpdateRole);
        tomcat4Role.before(mathComplexBackendRole,tomcat4FileUpdateRole);
        machine1.addRole(tomcat1Role, tomcat2Role, tomcat3Role, tomcat4Role,
        					mathClientRole, mathProxyRole, mathSimpleBackendRole, mathComplexBackendRole, thievesRole, 
        					tomcat1FileUpdateRole, tomcat2FileUpdateRole, tomcat3FileUpdateRole, tomcat4FileUpdateRole);		
 
 
        jboss1Role.before(jBoss1FileUpdateRole, tixMonsterRole);
        jBoss1FileUpdateRole.before(JBossStartRole);
        machine1.addRole(jboss1Role,jBoss1FileUpdateRole,tixMonsterRole,JBossStartRole);
        
        testbed.addMachine(machine1);
    	return testbed;
	}

	private NodeConfiguration createConfiguration(ITasResolver tasResolver) {
		List<NodeCapability> capabilities = new ArrayList<NodeCapability>();

		NodeCapability chromeCapability = new NodeCapability.Builder()
				.browserType(BrowserType.CHROME).platform(NodePlatform.WINDOWS)
				.maxInstances(8).build();
		capabilities.add(chromeCapability);

		String hubHostname = tasResolver.getHostnameById(HUB1_ROLE_ID);
		URL hubUrl;
		try {
			hubUrl = new URL("http", hubHostname, 4444, "/grid/register/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return new NodeConfiguration.Builder().hub(hubUrl).maxSession(7)
				.addCapabilities(capabilities).build();
	}
	
	private FileUpdateRole updateTomcatStartScript(ITasResolver tasResolver,String tomcatDeployPath, String roleID){
		String fileName = tomcatDeployPath + "/bin/catalina.bat";
		HashMap<String,String> addJavaOpts = new HashMap<String,String>();
		
		String originalString = "set \"JAVA_OPTS=%JAVA_OPTS% %LOGGING_MANAGER%\"";
		String updatedString = 	"set \"AGENT_JAR=%CATALINA_HOME%\\\\wily\\\\Agent.jar\"\n"+
								"set \"AGENT_PROFILE=%CATALINA_HOME%\\\\wily\\\\core\\\\config\\\\IntroscopeAgent.profile\"\n"+
								"set \"JAVA_OPTS=%JAVA_OPTS% %LOGGING_MANAGER% "
								+ "-javaagent:%AGENT_JAR% "
								+ "-Dcom.wily.introscope.agentProfile=%AGENT_PROFILE% "
								+ "-Dcom.wily.introscope.agent.agentName=TomcatSpring_" + roleID.substring(0,7);
		
		if(   roleID.equalsIgnoreCase(TOMCAT1_SCRIPT_UPDATE_ROLE_ID)   ){
			updatedString += " -DproxyPort=3030 -DproxyHost=localhost\"";			
		} else {
			if (roleID.equalsIgnoreCase(TOMCAT2_SCRIPT_UPDATE_ROLE_ID))		
					updatedString += " -DsimpleHost=localhost  -DsimplePort=4040 -DcomplexHost=localhost -DcomplexPort=8080\"";
			else updatedString += "\"";
		}
		
		String fileFinishPrompt_orig = ":end";
		String fileFinishPrompt_updated = ":end\n"
											+"echo Tomcat started";
		
		
		addJavaOpts.put(originalString,updatedString);
		addJavaOpts.put(fileFinishPrompt_orig,fileFinishPrompt_updated);
		
		FileUpdateRole fileUpdateRole = new FileUpdateRole.Builder(
				roleID, tasResolver).filePath(fileName)
				.replacePairs(addJavaOpts).build();
		return fileUpdateRole;
	}
	

	private FileUpdateRole updateJbossStartScript(ITasResolver tasResolver,String jBossDeployPath, String roleID){
		String fileName = jBossDeployPath + "/bin/standalone.bat";
		HashMap<String,String> addJavaOpts = new HashMap<String,String>();
		
		String originalString = "set \"JAVA_OPTS=-Dprogram.name=%PROGNAME% %JAVA_OPTS%\"";
		String updatedString = "set \"AGENT_HOME=%JBOSS1_HOME%\\\\wily\"\n"+						   
							   "set \"JAVA_OPTS=-Dprogram.name=%PROGNAME% %JAVA_OPTS% "
							   + "-Djboss.modules.system.pkgs=org.jboss.byteman,org.jboss.logmanager,com.wily,com.wily.* "
							   + "-Djava.util.logging.manager=org.jboss.logmanager.LogManager "
							   + "-javaagent:%AGENT_HOME%\\\\Agent.jar "
							   + "-DagentProfile=%AGENT_HOME%\\\\core\\\\config\\\\IntroscopeAgent.profile "
							   + "-Dcom.wily.introscope.agent.agentName=JBossSpring_TicketMonster "
							   + "-Xbootclasspath/p:%JBOSS1_HOME%\\\\modules\\\\system\\\\layers\\\\base\\\\org\\\\jboss\\\\log4j\\\\logmanager\\\\main\\\\log4j-jboss-logmanager-1.1.2.Final.jar;%JBOSS1_HOME%\\\\modules\\\\system\\\\layers\\\\base\\\\org\\\\jboss\\\\logmanager\\\\main\\\\jboss-logmanager-2.0.0.Final.jar;%JBOSS1_HOME%\\\\modules\\\\system\\\\layers\\\\base\\\\org\\\\slf4j\\\\impl\\\\main\\\\slf4j-jboss-logmanager-1.0.3.GA.jar\"";
		
		/*String fileFinishPrompt_orig = ":end";
		String fileFinishPrompt_updated = ":end\n"
											+"echo Tomcat started";*/
		
		addJavaOpts.put(originalString,updatedString);
		//addJavaOpts.put(fileFinishPrompt_orig,fileFinishPrompt_updated);
		
		FileUpdateRole fileUpdateRole = new FileUpdateRole.Builder(
				roleID, tasResolver).filePath(fileName)
				.replacePairs(addJavaOpts).build();
		return fileUpdateRole;
	}
	

    // create Jmeter roles
    public static ArrayList<IRole> createJmeterRoles(ITasResolver tasResolver) {        
        GenericRole jmeterRole = createJmeterBundleRole (tasResolver);        
        GenericRole jmeterScriptsRole = createJmeterScriptsRole(tasResolver);       
        //ExecutionRole permissionsRole = createJmeterPermissionRole (tasResolver);

        //jmeterRole.before(permissionsRole);
        //permissionsRole.after(jmeterRole);
        ArrayList<IRole> roles = new ArrayList<IRole>();
        roles.add(jmeterRole);
        roles.add(jmeterScriptsRole);
        //roles.add(permissionsRole);
        return roles;
    }   
    
    private static GenericRole createJmeterBundleRole(ITasResolver tasResolver) {
        
        DefaultArtifact jmeterArtifact = new DefaultArtifact("com.ca.apm.binaries", "apache-jmeter", "", "zip", "3.1");
        
        GenericRole jmeterRole = new GenericRole.Builder(JMETER_ROLE_ID, tasResolver)
        .unpack(jmeterArtifact, JMETER_HOME)
        .build(); 
        
        return jmeterRole;
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
