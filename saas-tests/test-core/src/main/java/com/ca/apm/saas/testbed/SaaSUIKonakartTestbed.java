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

/*
 * @author sinab10
 */

package com.ca.apm.saas.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.saas.role.ClientDeployRole;
import com.ca.apm.saas.role.FileUpdateRole;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.MysqlRole;
import com.ca.tas.role.linux.YumInstallPackageRole;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SaaSUIKonakartTestbed implements ITestbedFactory {

	protected static final String LINUX_TEMPLATE_ID = ITestbedMachine.TEMPLATE_RH7;
	public static final String WIN_TEMPLATE_ID = TEMPLATE_W64;
	public static final String KONAKART_MACHINE_ID = "konakartMachine";
	protected static final String MYSQL_ROLE_ID = "mysqlRole";
	protected static final String NODE_ROLE_ID = "nodeRoleId";
	protected static final String HUB_ROLE_ID = "hubRoleId";
	protected static final String LOAD_ROLE_ID = "loadRoleId";
	protected static final String DB_MACHINE_ID = "dbMachine";
	public static final String DEPLOY_BASE = TasBuilder.WIN_SOFTWARE_LOC;
	
	public static final String KONAKART_ROLE_ID = "konakartRole";
	public static final String KONAKART_DIR = "KonaKart";
	public static final String KONAKART_BASE = DEPLOY_BASE + KONAKART_DIR;
	protected static final String KONAKART_FILE = KONAKART_BASE + "\\konakart.exe";
	public static final String KONAKART_START_BAT= "startkonakart.bat";
    public static final String KONAKART_STOP_BAT= "stopkonakart.bat";
    public static final String TOMCAT_SCRIPT_UPDATE_ROLE_ID = "tomcatStartScript";
    public static final String APPNAME_UPDATE_ROLE_ID = "appNameUpdateRoleID";
    public static final int KONAKART_PORT = 8780;
    public static final String APP_DISPLAY_NAME = "kk";
    
    public static final String JMETER_SCRIPTS_DIR = ClientDeployRole.JMETER_SCRIPTS_DIR;
    public static final String JMETER_SCRIPTS_ROLE_ID      = "jmeterScriptsRole";
    public static final String JMETER_INSTALLER_DIR = ClientDeployRole.JMETER_INSTALLER_DIR;
    

	@Override
	public ITestbed create(ITasResolver tasResolver) {

		// Creating DB Machine (MySql)
		MysqlRole mysqlRole = createMySqlRole();

		IRole prerequisitesRole = new YumInstallPackageRole.Builder("yumInstallPrerequisites")
            .addPackage("gcc-c++")
            .build();
		
		ExecutionRole createDBRole = createDB();
		ExecutionRole createNewUserRole = addNewUserWithPrivilege();
        
        prerequisitesRole.before(mysqlRole);
        mysqlRole.before(createDBRole);
        createDBRole.before(createNewUserRole);
		
		ITestbedMachine dbMachine = TestBedUtils.createLinuxMachine(DB_MACHINE_ID, LINUX_TEMPLATE_ID,
				prerequisitesRole,mysqlRole,createDBRole,createNewUserRole);
		
		
		// Creating Konakart (with Tomcat) machine
		
		// Creating Selenium Grid/Node roles and browser updates etc on WIN machine
		final SeleniumGridNodeRole nodeRole = new SeleniumGridNodeRole.Builder(
				NODE_ROLE_ID, tasResolver)
				.nodeConfiguration(createConfiguration(tasResolver))
				.qResXResolution("1920").qResYResolution("1080")
				.chromeDriver(SeleniumChromeDriver.V2_29_B32).build();
				
		final SeleniumGridHubRole hubRole = new SeleniumGridHubRole.Builder(
				HUB_ROLE_ID, tasResolver).addNodeRole(nodeRole).build();
		
		final ClientDeployRole loadRole = new ClientDeployRole.Builder(LOAD_ROLE_ID, tasResolver)
											.browserInstallWait(120).build();			
		
		
		// Creating Konakart download role
        GenericRole konakartAppRole = new GenericRole.Builder(KONAKART_ROLE_ID,tasResolver)
						.download(new DefaultArtifact("com.ca.apm.coda", "konakart","Windows-Setup", "exe","8.5.0.2"),KONAKART_FILE)
						.build();        
		
		// Creating Konakart install role
        ExecutionRole installKKRole = installKonakart(tasResolver);
        
        // Instrumenting tomcat's start script
        FileUpdateRole tomcatFileUpdateRole = updateTomcatStartScript(tasResolver,KONAKART_BASE);
        
        // Updating Konakart's name update in Tomcat web-inf's web.xnl
        FileUpdateRole appNameUpdateRole= updateAppNameInWebXML(tasResolver,KONAKART_BASE);
        
        //Jmeter Scripts role
        GenericRole jmeterScriptsRole = createJmeterScriptsRole(tasResolver);

		
		// Establishing order of implementation 
		nodeRole.before(hubRole);
		hubRole.before(loadRole);
		loadRole.before(konakartAppRole);
		konakartAppRole.before(installKKRole);
		installKKRole.before(tomcatFileUpdateRole);
		installKKRole.before(appNameUpdateRole);
		
		// Creating a WIN machine and adding all roles to it
		ITestbedMachine testMachine = new TestbedMachine
										.Builder(KONAKART_MACHINE_ID)
										.templateId(WIN_TEMPLATE_ID)
										.build();
		testMachine.addRole(hubRole, nodeRole, loadRole, konakartAppRole, installKKRole, tomcatFileUpdateRole, appNameUpdateRole, jmeterScriptsRole);
		
		// Adding both machines to testbed
		ITestbed testbed = new Testbed(getClass().getSimpleName());
		testbed.addMachine(dbMachine,testMachine);
		
		// Serializing a few useful key-value pairs
		String hubHostName = tasResolver.getHostnameById(HUB_ROLE_ID);
		testbed.addProperty("selenium.webdriverURL", "http://" + hubHostName + ":4444/wd/hub");
		testbed.addProperty("konakart.install.dir", KONAKART_BASE);
		testbed.addProperty("konakart.start.bat",KONAKART_START_BAT);
		testbed.addProperty("konakart.stop.bat",KONAKART_STOP_BAT);
		testbed.addProperty("tomcat.konakart.install.dir", KONAKART_DIR);
		testbed.addProperty("jmeter.install.dir", JMETER_INSTALLER_DIR);
		testbed.addProperty("jmeter.scripts.install.dir", JMETER_SCRIPTS_DIR);
		testbed.addProperty("konakart.display.name", APP_DISPLAY_NAME);
		
		return testbed;
	}
	
	private ExecutionRole installKonakart(ITasResolver tasResolver){
		String mySqlMachine = tasResolver.getHostnameById(MYSQL_ROLE_ID);
		ArrayList<String> args = new ArrayList<String>();
		args.add("-S");
		args.add("-DDatabaseType");
		args.add("mysql");
		args.add("-DDatabaseDriver");
		args.add("com.mysql.jdbc.Driver");
		args.add("-DDatabaseUrl");
		args.add("\"jdbc:mysql://"+ mySqlMachine +":3306/konakartDB?zeroDateTimeBehavior=convertToNull&useSSL=false\"");
		args.add("-DDatabaseUsername");
		args.add("konakartUser");
		args.add("-DDatabasePassword");
		args.add("konakartPwd");
		args.add("-DLoadDB");
		args.add("1");
		args.add("-DJavaJRE");
		args.add("\"C:\\Program Files\\Java\\jdk1.8.0_25\"");
		args.add("-DInstallationDir");
		args.add(KONAKART_BASE);
		
		RunCommandFlowContext context = new RunCommandFlowContext
				.Builder("konakart")
				.workDir(KONAKART_BASE)
				.args(args)
				.build();
		ExecutionRole installKKRole = new ExecutionRole
				.Builder("installKKRole")
				.flow(RunCommandFlow.class, context)
				.build();
		
		return installKKRole;
	}
	
	private ExecutionRole createDB(){
		ArrayList<String> args = new ArrayList<String>();
		args.add("-u");
		args.add("root");
		args.add("-proot");
		args.add("-e");
		args.add("CREATE DATABASE konakartDB;");

		RunCommandFlowContext context = new RunCommandFlowContext
											.Builder("mysql")
											.args(args)
											.build();
		ExecutionRole createDBRole = new ExecutionRole
								.Builder("createDBRole")
								.flow(RunCommandFlow.class, context)
								.build();
			
		return createDBRole;
	}
	
	private ExecutionRole addNewUserWithPrivilege(){
		ArrayList<String> args = new ArrayList<String>();
		args.add("-u");
		args.add("root");
		args.add("-proot");
		args.add("-e");
		args.add("CREATE USER \"konakartUser\"@\"%\" IDENTIFIED BY \"konakartPwd\";GRANT ALL PRIVILEGES ON *.* TO \"konakartUser\"@\"%\";FLUSH PRIVILEGES;");

		RunCommandFlowContext context = new RunCommandFlowContext
											.Builder("mysql")
											.args(args)
											.build();
		ExecutionRole createNewUserRole = new ExecutionRole
								.Builder("createNewUserRole")
								.flow(RunCommandFlow.class, context)
								.build();
		
		return createNewUserRole;
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

	protected MysqlRole createMySqlRole() {
		// mysql role
		return new MysqlRole.LinuxBuilder(MYSQL_ROLE_ID).autoStart().build();
	}
	

    private static GenericRole createJmeterScriptsRole(ITasResolver tasResolver) {
        DefaultArtifact loadJmx =
            new DefaultArtifact("com.ca.apm.saas", "saas-tests-core", "jmeterscripts", "zip",
                tasResolver.getDefaultVersion());
 
        GenericRole jmeterScriptsRole = new GenericRole.Builder(JMETER_SCRIPTS_ROLE_ID, tasResolver)
            .unpack(loadJmx, JMETER_SCRIPTS_DIR)
            .build();
 
        return jmeterScriptsRole;
    }
    
    private FileUpdateRole updateTomcatStartScript(ITasResolver tasResolver,String tomcatDeployPath){
		String fileName = tomcatDeployPath + "/bin/catalina.bat";
		HashMap<String,String> addJavaOpts = new HashMap<String,String>();
		
		String originalString = "set \"JAVA_OPTS=%JAVA_OPTS% -Djava.protocol.handler.pkgs=org.apache.catalina.webresources\"";
		String updatedString = 	"set \"AGENT_JAR=%CATALINA_HOME%\\\\wily\\\\Agent.jar\"\n"+
								"set \"AGENT_PROFILE=%CATALINA_HOME%\\\\wily\\\\core\\\\config\\\\IntroscopeAgent.profile\"\n"+
								"set \"JAVA_OPTS=%JAVA_OPTS% -Djava.protocol.handler.pkgs=org.apache.catalina.webresources "
								+ "-javaagent:%AGENT_JAR% "
								+ "-Dcom.wily.introscope.agentProfile=%AGENT_PROFILE% "
								+ "-Dcom.wily.introscope.agent.agentName=TomcatSpring_Konakart\"";

		
		addJavaOpts.put(originalString,updatedString);
		
		FileUpdateRole fileUpdateRole = new FileUpdateRole.Builder(
				TOMCAT_SCRIPT_UPDATE_ROLE_ID, tasResolver).filePath(fileName)
				.replacePairs(addJavaOpts).build();
		return fileUpdateRole;
	}
    
    private FileUpdateRole updateAppNameInWebXML(ITasResolver tasResolver,String tomcatDeployPath){
    	String fileName = tomcatDeployPath + "/webapps/konakart/WEB-INF/web.xml";
    	HashMap<String,String> addJavaOpts = new HashMap<String,String>();
    	String originalString = "<display-name>KonaKart Store-Front Application</display-name>";
		String updatedString = 	"<display-name>" + APP_DISPLAY_NAME + "</display-name>";
		addJavaOpts.put(originalString,updatedString);
		FileUpdateRole fileUpdateRole = new FileUpdateRole.Builder(
				APPNAME_UPDATE_ROLE_ID, tasResolver).filePath(fileName)
				.replacePairs(addJavaOpts).build();
		return fileUpdateRole;
	}
}
