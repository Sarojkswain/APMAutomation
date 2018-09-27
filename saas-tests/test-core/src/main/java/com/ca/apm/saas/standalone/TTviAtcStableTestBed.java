package com.ca.apm.saas.standalone;

import java.util.Arrays;
import java.util.Collection;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class TTviAtcStableTestBed implements ITestbedFactory {

	private static final String EM_ROLE_ID = "emRole";
	public static final String EM_VERSION = "99.99.ttvi_atc_ui_stable-SNAPSHOT";// 10.7.0_dev-SNAPSHOT
																				// ;//"10.2.0.13";//"99.99.metadata";99.99.ttvi_stable-SNAPSHOT

	public static final String INSTALL_DIR = "/em/Introscope";
	public static final String INSTALL_TG_DIR = "/em/Installer";
	public static final String DATABASE_DIR = "/em/database";
	public static final String GC_LOG_FILE = INSTALL_DIR + "/logs/gclog.txt";

	public static final String DB_PASSWORD = "password";
	public static final String DB_USERNAME = "cemadmin";
	public static final String DB_ADMIN_USERNAME = "postgres";
	public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

	private static final Collection<String> EM_LAXNL_JAVA_OPTION = Arrays
			.asList("-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
					"-Dorg.owasp.esapi.resources=./config/esapi",
					"-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-Xss256k",
					"-Dcom.wily.assert=false", "-showversion",
					"-XX:CMSInitiatingOccupancyFraction=50",
					"-XX:+HeapDumpOnOutOfMemoryError", "-Xms4096m",
					"-Xmx4096m", "-verbose:gc", "-Xloggc:" + GC_LOG_FILE,
					"-Dappmap.user=admin", "-Dappmap.token=" + ADMIN_AUX_TOKEN);
	private static final String EM_TEMPLATE_ID = "co7";
	private static final String EM_MACHINE_ID = "emMachine";

	@Override
	public ITestbed create(ITasResolver tasResolver) {
		Testbed testbed = new Testbed("TTviAtcStableTestBed TestBed");

		// EM machine
		ITestbedMachine emMachine = new TestbedMachine.LinuxBuilder(
				EM_MACHINE_ID).templateId(EM_TEMPLATE_ID).build();

		// EM Role
		EmRole emRole = new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver)
				.accServerPort(9090)
				.version(EM_VERSION)
				.installDir("/em/")			
				.configProperty("ca.apm.ttstore.jarvis.es.url",
						"http://fldcoll12t:9200")
				.configProperty("ca.apm.ttstore.jarvis.ingestion.url",
						"http://fldcoll12t:8080/ingestion")
				.configProperty("ca.apm.ttstore.jarvis.onboarding.url",
						"http://fldcoll12t:8080/onboarding")
				.configProperty("cohortId", "sculptor_123")
				.configProperty("com.ca.apm.ttstore", "jarvis")
				.configProperty("introscope.appmap.acc.security.token",
						ADMIN_AUX_TOKEN)
				.emLaxNlClearJavaOption(EM_LAXNL_JAVA_OPTION)
				.build();
		emMachine.addRole(emRole);

		testbed.addMachine(emMachine);
		return testbed;
	}
}
