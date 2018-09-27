/*
 * Copyright (c) 2017 CA. All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.saas.standalone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.saas.standalone.atc.SeleniumGridMachinesFactory;
import com.ca.apm.systemtest.fld.artifact.FLDHvrAgentLoadExtractArtifact;
import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerBaseRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 *
 * @author banda06
 * @auther ahmal01
 */

@TestBedDefinition
public class FLDStandAloneTestbed implements ITestbedFactory,
		FLDStandAloneConstants {

    public static final Logger log = LoggerFactory.getLogger(FLDStandAloneTestbed.class);
    
	private static final String SYSTEM_XML = "xml/appmap-stress/load-test/system.xml";
	public static final String INSTALL_DIR = "/em/Introscope";
	public static final String INSTALL_TG_DIR = "/em/Installer";
	public static final String DATABASE_DIR = "/em/database";
	public static final String GC_LOG_FILE = INSTALL_DIR + "/logs/gclog.txt";
	public static final String DB_PASSWORD = "password";
	public static final String DB_USERNAME = "cemadmin";
	public static final String DB_ADMIN_USERNAME = "postgres";
	public static final int WVPORT = 8084;
	public static final int EMWEBPORT = 8081;
	public static final int EM_PORT=5001;
	public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
	
	private static final Collection<String> EM_LAXNL_JAVA_OPTION = Arrays
			.asList("-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
					"-Dorg.owasp.esapi.resources=./config/esapi",
					"-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-Xss512k",
					"-Dcom.wily.assert=false", "-showversion",
					"-XX:CMSInitiatingOccupancyFraction=50",
					"-XX:+HeapDumpOnOutOfMemoryError", "-Xms1024m",
					"-Xmx4096m", "-verbose:gc", "-Xloggc:" + GC_LOG_FILE,
					"-Dappmap.user=admin", "-Dappmap.token=" + ADMIN_AUX_TOKEN);

	public static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays
			.asList("-Djava.awt.headless=true",
					"-Dorg.owasp.esapi.resources=./config/esapi",
					"-Dsun.java2d.noddraw=true",
					"-javaagent:./product/webview/agent/wily/Agent.jar",
					"-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
					"-Dcom.wily.introscope.wilyForWilyPrefix=com.wily",
					"-Xms2048m", "-Xmx2048m", "-XX:+PrintGCDateStamps",
					"-XX:+HeapDumpOnOutOfMemoryError", "-verbose:gc",
					"-Xloggc:" + GC_LOG_FILE);

	@Override
	public ITestbed create(ITasResolver tasResolver) {

		Testbed testbed = new Testbed("FLDStandAloneTestbed");

		// StandAlone EM
		ITestbedMachine emMachine = new TestbedMachine.LinuxBuilder(
				EM_MACHINE_ID).templateId(EM_TEMPLATE_ID).bitness(Bitness.b64)
				.build();
		EmRole emRole = new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver)
				.silentInstallChosenFeatures(
						Arrays.asList("Enterprise Manager", "ProbeBuilder",
								"EPA", "Database", "WebView"))
				.dbuser(DB_USERNAME).dbpassword(DB_PASSWORD)
				.dbAdminUser(DB_ADMIN_USERNAME).dbAdminPassword(DB_PASSWORD)
				.databaseDir(DATABASE_DIR).emWebPort(EMWEBPORT)
				.installDir(INSTALL_DIR).installerTgDir(INSTALL_TG_DIR)
				.wvPort(WVPORT).emLaxNlClearJavaOption(EM_LAXNL_JAVA_OPTION)
				.version(EM_VERSION)
				.wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION).configProperty("introscope.apmserver.teamcenter.master", "true")
                .configProperty("introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast","10")
                .configProperty("introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.slow","60")
                .configProperty("enable.default.BusinessTransaction", "true")
                .configProperty("introscope.apmserver.teamcenter.saas", "true")
                .build();
		emMachine.addRole(emRole);
		testbed.addMachine(emMachine);
		
		log.info("EM Machine name " + emMachine.getHostname());

        testbed.addProperty("test.applicationBaseURL",
            "http://" + tasResolver.getHostnameById(emRole.getRoleId()) + ":" + WVPORT + "/ApmServer");

        
        // Selenium Grid setup.
        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines =
            seleniumGridMachinesFactory.createMachines(tasResolver);
        
        testbed.addMachines(seleniumGridMachines);

        // register remote Selenium Grid
        String hubHostName = tasResolver.getHostnameById(SeleniumGridMachinesFactory.HUB_ROLE_ID);
        testbed.addProperty("selenium.webdriverURL", "http://" + hubHostName + ":4444/wd/hub");
        testbed.addProperty("driverPath", DRIVERS_PATH);

		// Load machine
		ITestbedMachine loadMachine = new TestbedMachine.Builder(
				LOAD_MACHINE1_ID).templateId(LOADMACHINE_TEMPLATE_ID).build();
		
		String emHost = tasResolver.getHostnameById(EM_ROLE_ID);
		// HVR Load
		FLDHvrAgentLoadExtractArtifact artifactFactory = new FLDHvrAgentLoadExtractArtifact(
				tasResolver);
		ITasArtifact artifact = artifactFactory.createArtifact("10.3");
		HVRAgentLoadRole hvrLoadRole = new HVRAgentLoadRole.Builder(
				HVR_ROLE_ID, tasResolver)
				.emHost(emHost).emPort("5001")
				.cloneagents(26).cloneconnections(8).agentHost("HVRAgent")
				.secondspertrace(1).addMetricsArtifact(artifact.getArtifact())
				.build();
		
		// Wurlitzer Load
		WurlitzerBaseRole wurlitzerBaseRole = new WurlitzerBaseRole.Builder(
				"wurlitzer_base", tasResolver).deployDir("wurlitzerBase")
				.build();
		
		loadMachine.addRole(wurlitzerBaseRole);
		
		EmRole coll = (EmRole) testbed.getRoleById(EM_ROLE_ID);
		String xml = "3Complex-200agents-2apps-25frontends-100EJBsession";
		
		WurlitzerLoadRole wurlitzerLoadrole = new WurlitzerLoadRole.Builder(
				WURLITZER_ROLE_ID, tasResolver).emRole(coll)
				.buildFileLocation(SYSTEM_XML).target(xml)
				.logFile(xml + ".log").wurlitzerBaseRole(wurlitzerBaseRole)
				.build();		
		
		JMeterRole at_Jmeter = new JMeterRole.Builder(JMETER_LOAD8, tasResolver)
				.installJmeter(true).jmxFile("AT.JMX")
				.targetHost(emHost).build();
		
		JMeterRole ttviewer_Jmeter = new JMeterRole.Builder(JMETER_LOAD9,
				tasResolver).jmxFile("TT_Viewer.JMX")
				.targetHost(emHost).build();
		
		// CLW Load
		CLWWorkStationLoadRole clwRole = new CLWWorkStationLoadRole.Builder(
				CLW_ROLE_ID, tasResolver).emHost(emHost).agentName("tas.*")
				.build();
		
		loadMachine.addRole(clwRole, wurlitzerLoadrole, hvrLoadRole, at_Jmeter,	ttviewer_Jmeter);
		
		testbed.addMachine(loadMachine);

		List<FldTestbedProvider> testbedProviders = new ArrayList<>();
		testbedProviders.add(new FldLoadTomcatProvider());
//		testbedProviders.add(new FLDWebSphereLoadProvider());
		testbedProviders.add(new FLDJbossLoadProvider());
		testbedProviders.add(new FLDWebLogicCrossClusterProvider());

		for (FldTestbedProvider provider : testbedProviders) {
			Collection<ITestbedMachine> machines = provider.initMachines();
			if (machines != null) {
				testbed.addMachines(machines);
			}
		}
		// and initialize the roles
		for (FldTestbedProvider provider : testbedProviders) {
			provider.initTestbed(testbed, tasResolver);
		}

		return testbed;
	}
}
