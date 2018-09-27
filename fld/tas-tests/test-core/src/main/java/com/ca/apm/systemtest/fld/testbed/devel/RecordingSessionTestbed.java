package com.ca.apm.systemtest.fld.testbed.devel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.systemtest.fld.role.RecordingSessionRole;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.tess.ConfigureTessRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Development testbed to test Agent and TIM recording session automation.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@TestBedDefinition
public class RecordingSessionTestbed implements ITestbedFactory {
	public static final String EM_MACHINE_ID = "emMachine";
	public static final String TIM_MACHINE_ID = "timMachine";
	public static final String EM_INSTALL_DIR = "C:/sw/em";
	public static final String EM_INSTALLATOR_DIR = "C:/sw/emInstaller";
	public static final String TIM_INSTALL_DIR = "/opt";
	public static final String DATABASE_DIR = "C:/sw/db";
	public static final String EM_VERSION = "99.99.sys-SNAPSHOT";
	public static final String TIM_VERSION = "99.99.sys-SNAPSHOT";
	public static final String DB_PASSWORD = "Password1";
	public static final String DB_USERNAME = "cemadmin";
	public static final String DB_ADMIN_USERNAME = "postgres";
	public static final String CONFIGURE_TESS_ROLE_ID = "configureTess";

	private static final String DAILYSTATS_JAVA_OPTION = "-XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC"
			+ " -XX:+UseBiasedLocking -XX:SurvivorRatio=8 -XX:TargetSurvivorRatio=90 -Dlog4j.configuration=log4j.properties"
			+ " -Xms256M -Xss512k -Xms4096m -Xmx4096m -XX:+HeapDumpOnOutOfMemoryError";

	private static final String JDK7_HOME_DIR = "C:/sw/jsdk7_64";

	private static final String EM_ROLE_ID = "em";
	
	@Override
	public ITestbed create(ITasResolver tasResolver) {
		ITestbedMachine emMachine = new TestbedMachine.Builder(EM_MACHINE_ID)
				.templateId(ITestbedMachine.TEMPLATE_W64).build();

		ITestbedMachine timMachine = new TestbedMachine.LinuxBuilder(TIM_MACHINE_ID)
			.templateId(ITestbedMachine.TEMPLATE_CO65)
			.build();
		
        TIMRole.Builder timBuilder = new TIMRole.Builder("tim", tasResolver);
        TIMRole timRole = timBuilder.timVersion(TIM_VERSION).installDir(TIM_INSTALL_DIR).build();
        timMachine.addRole(timRole);

		EmRole.Builder emBuilder = new EmRole.Builder(EM_ROLE_ID, tasResolver);
		emBuilder.dbuser(DB_USERNAME).dbpassword(DB_PASSWORD)
				.dbAdminUser(DB_ADMIN_USERNAME).dbAdminPassword(DB_PASSWORD)
				.nostartEM().nostartWV().version(EM_VERSION)
				.databaseDir(DATABASE_DIR).installDir(EM_INSTALL_DIR)
				.installerTgDir(EM_INSTALLATOR_DIR)
				.tim(timRole);

		EmRole emRole = emBuilder.build();
        timRole.before(emRole);

		Map<String, String> propsMap = new HashMap<>(2);
		propsMap.put("dailystats.jvmArgs", DAILYSTATS_JAVA_OPTION);
		propsMap.put("dailystats.aggregateInSeparateJvm", "true");

		ConfigureFlowContext ctx = new ConfigureFlowContext.Builder()
				.configurationMap(
						EM_INSTALL_DIR + "/config/tess-default.properties",
						propsMap).build();

		UniversalRole configureCollectorRole = new UniversalRole.Builder(
				"configCollectorRole", tasResolver).runFlow(
				ConfigureFlow.class, ctx).build();

		configureCollectorRole.after(emRole);

		emMachine.addRole(emRole, configureCollectorRole);

		ExecutionRole.Builder startEmBuilder = new ExecutionRole.Builder(
				emRole.getRoleId() + "_startEM");

		startEmBuilder.asyncCommand(emRole.getEmRunCommandFlowContext());
		ExecutionRole startEmRole = startEmBuilder.build();

		startEmRole.after(emRole);
		emMachine.addRole(startEmRole);

		ExecutionRole.Builder startWvBuilder = new ExecutionRole.Builder(
				emRole.getRoleId() + "_startWV");

		startWvBuilder.asyncCommand(emRole.getWvRunCommandFlowContext());
		ExecutionRole startWvRole = startWvBuilder.build();

		startWvRole.after(startEmRole);
		emMachine.addRole(startWvRole);


		JavaRole javaRole = new JavaRole.Builder("javaRole", tasResolver)
				.dir(JDK7_HOME_DIR).version(JavaBinary.WINDOWS_64BIT_JDK_17)
				.build();

		emMachine.addRole(javaRole);

		TomcatRole tomcat6Role = new TomcatRole.Builder("tomcat6", tasResolver)
				.additionalVMOptions(
						Arrays.asList("-Xms256m", "-Xmx512m",
								"-XX:PermSize=256m", "-XX:MaxPermSize=512m",
								"-server",
								"-Dcom.wily.introscope.agent.agentName=Tomcat6"))
				.tomcatVersion(TomcatVersion.v60).customJava(javaRole)
				.jdkHomeDir(JDK7_HOME_DIR).build();

		tomcat6Role.after(javaRole);
		emMachine.addRole(tomcat6Role);

		IRole tomcatAgentRole = new AgentRole.Builder("tomcat6Agent",
				tasResolver).webAppServer(tomcat6Role).webAppAutoStart()
				.intrumentationLevel(AgentInstrumentationLevel.FULL)
				.emRole(emRole).build();

		tomcatAgentRole.after(tomcat6Role);
		emMachine.addRole(tomcatAgentRole);

	    //configure Tess
        ConfigureTessRole.Builder configTessRoleBuilder = new ConfigureTessRole.Builder(CONFIGURE_TESS_ROLE_ID, tasResolver)
        .mom(emRole)
        .tim(timRole);

        ConfigureTessRole configTessRole = configTessRoleBuilder.build();
		configTessRole.after(tomcatAgentRole);
        configTessRole.after(timRole);
	    emMachine.addRole(configTessRole);

	    RecordingSessionRole.Builder agentRecordingSessionRoleBuilder = new RecordingSessionRole.Builder(FLDLoadConstants.AGENT_SESSION_RECORDING_ROLE_ID, tasResolver)
	    	.setTessHost("localhost")
	    	.setTessPort(8081)
	    	.setTessUser("cemadmin")
	    	.setTessPassword("quality")
	    	.setRecordingDurationMillis(180000);//3 minutes
	    
	    RecordingSessionRole agentRecordingRole = agentRecordingSessionRoleBuilder.build();
		agentRecordingRole.after(tomcatAgentRole);
	    emMachine.addRole(agentRecordingRole);

	    RecordingSessionRole.Builder timRecordingSessionRoleBuilder = new RecordingSessionRole.Builder(FLDLoadConstants.TIM_SESSION_RECORDING_ROLE_ID, tasResolver)
    	.setTessHost("localhost")
    	.setTessPort(8081)
    	.setTessUser("cemadmin")
    	.setTessPassword("quality")
    	.setTIMRecording()
    	.setRecordingDurationMillis(180000);//3 minutes
    
	    RecordingSessionRole timRecordingRole = timRecordingSessionRoleBuilder.build();
	    timRecordingRole.after(tomcatAgentRole);
	    emMachine.addRole(timRecordingRole, configTessRole);

	    
		Testbed testbed = new Testbed("TestBed");
		testbed.addMachine(emMachine, timMachine);
		
		return testbed;

	}

}
