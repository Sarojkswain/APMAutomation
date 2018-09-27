package com.ca.apm.tests.agentEMFailoverTests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.LogCheckFlow;
import com.ca.apm.automation.action.flow.utility.LogCheckFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.flow.FailoverModifierFlow;
import com.ca.apm.commons.flow.FailoverModifierFlowContext;
import com.ca.apm.tests.testbed.AgentEMFailoverLinuxClusterTestbed;
import com.ca.apm.tests.testbed.AgentEMFailoverWindowsClusterTestbed;

public class AgentEMFailoverClusterTests extends AgentEMFailoverBase {

	private final String momConfigFile;
	private final String momConfigDir;
	private final String tomcatRoleID;
	private final String momHost;
	private final String collectorHost;
	private final String tomcatHost;
	private final String momEMLog;
	private final String momEMHome;
	private final String momMachine;
	private final String collectorMachine;
	private final String collectorRoleID;
	private final String freeMachine1;
	private final String freeRole1ID;
	private final String freeMachine2;
	private final String freeRole2ID;
	private final String freeMachine3;
	private final String freeRole3ID;
	private final String freeHost1;
	private final String freeHost2;
	private final String freeHost3;
	private final String momRoleID;
	private final String failoverMomPath;
	private final String failoverCollectorPath;
	private final String collectorConfigFile;
	private final String collectorEMHome;
	private final String tomcatMachineID;
	private final String agentProfile;
	private final String agentLogFile;
	private final String primaryAgentLogMessage;
	private final String secondaryAgentLogMessage1;
	private final String secondaryAgentLogMessage2;
	private final String secondaryAgentLogMessage3;
	private final String primaryEmStartupMesssage;
	private final String failoverEmStartupMesssage;
	private final String emFailoverPrimaryProp;
	private final String emFailoverSecondaryProp;
	private final String emFailoverInterval;
	private final String primaryLockMessage;
	private final String secondaryLockMessage;
	private final String collectorEMLog;
	private final String collectorEMConfigDir;
	private final String momPort;
	private final String collectorPort;
	private final String shutdownMessage;
	private final List<String> agentFailoverConnectionOrder = new ArrayList<String>();
	private final List<String> nfsClientWindowsCommandMom = new ArrayList<String>();
	private final List<String> nfsServerLinuxCommandMom = new ArrayList<>();
	private final List<String> nfsClientLinuxCommandMom = new ArrayList<String>();
	private final List<String> nfsServerWindowsCommandMom = new ArrayList<>();

	private final List<String> nfsClientWindowsCommandCollector = new ArrayList<String>();
	private final List<String> nfsServerLinuxCommandCollector = new ArrayList<>();
	private final List<String> nfsClientLinuxCommandCollector = new ArrayList<String>();
	private final List<String> nfsServerWindowsCommandCollector = new ArrayList<>();
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AgentEMFailoverClusterTests.class);
	public RunCommandFlowContext emRunCmdFlowContext;
	private String linuxSubVersion;

	public AgentEMFailoverClusterTests() {

		boolean isWindows = Os.isFamily(Os.FAMILY_WINDOWS);

		momMachine = isWindows ? AgentEMFailoverWindowsClusterTestbed.MOM_MACHINE_ID
				: AgentEMFailoverLinuxClusterTestbed.MOM_MACHINE_ID;
		momRoleID = isWindows ? AgentEMFailoverWindowsClusterTestbed.MOM_ROLE_ID
				: AgentEMFailoverLinuxClusterTestbed.MOM_ROLE_ID;

		collectorMachine = isWindows ? AgentEMFailoverWindowsClusterTestbed.COLLECTOR_MACHINE_ID
				: AgentEMFailoverLinuxClusterTestbed.COLLECTOR_MACHINE_ID;
		collectorRoleID = isWindows ? AgentEMFailoverWindowsClusterTestbed.COLLECTOR_ROLE_ID
				: AgentEMFailoverLinuxClusterTestbed.COLLECTOR_ROLE_ID;

		freeRole1ID = isWindows ? AgentEMFailoverWindowsClusterTestbed.FREE_ROLE1_ID
				: AgentEMFailoverLinuxClusterTestbed.FREE_ROLE1_ID;

		freeRole2ID = isWindows ? AgentEMFailoverWindowsClusterTestbed.FREE_ROLE2_ID
				: AgentEMFailoverLinuxClusterTestbed.FREE_ROLE2_ID;

		freeRole3ID = isWindows ? AgentEMFailoverWindowsClusterTestbed.FREE_ROLE3_ID
				: AgentEMFailoverLinuxClusterTestbed.FREE_ROLE3_ID;

		freeMachine1 = isWindows ? AgentEMFailoverWindowsClusterTestbed.FREE_MACHINE1_ID
				: AgentEMFailoverLinuxClusterTestbed.FREE_MACHINE1_ID;

		freeMachine2 = isWindows ? AgentEMFailoverWindowsClusterTestbed.FREE_MACHINE2_ID
				: AgentEMFailoverLinuxClusterTestbed.FREE_MACHINE2_ID;

		freeMachine3 = isWindows ? AgentEMFailoverWindowsClusterTestbed.FREE_MACHINE3_ID
				: AgentEMFailoverLinuxClusterTestbed.FREE_MACHINE3_ID;

		tomcatRoleID = isWindows ? AgentEMFailoverWindowsClusterTestbed.TOMCAT_ROLE_ID
				: AgentEMFailoverLinuxClusterTestbed.TOMCAT_ROLE_ID;
		tomcatMachineID = isWindows ? AgentEMFailoverWindowsClusterTestbed.AGENT_MACHINE_ID
				: AgentEMFailoverLinuxClusterTestbed.AGENT_MACHINE_ID;
		failoverMomPath = isWindows ? "s:/em/" : "/mnt/em";

		failoverCollectorPath = isWindows ? "t:/collem/"
				: "/opt/remoteCollector/collem";

		collectorPort = envProperties.getRolePropertyById(collectorRoleID,
				DeployEMFlowContext.ENV_EM_PORT);
		momPort = envProperties.getRolePropertyById(momRoleID,
				DeployEMFlowContext.ENV_EM_PORT);

		emFailoverPrimaryProp = "introscope.enterprisemanager.failover.primary=";
		emFailoverSecondaryProp = "introscope.enterprisemanager.failover.secondary=";
		emFailoverInterval = "introscope.enterprisemanager.failover.interval";

		momConfigFile = envProperties.getRolePropertyById(momRoleID,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		momEMHome = (envProperties.getRolePropertyById(momRoleID,
				DeployEMFlowContext.ENV_EM_INSTALL_DIR));

		collectorConfigFile = envProperties.getRolePropertyById(
				collectorRoleID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		collectorEMHome = (envProperties.getRolePropertyById(collectorRoleID,
				DeployEMFlowContext.ENV_EM_INSTALL_DIR));

		momHost = envProperties.getMachineHostnameByRoleId(momRoleID);
		collectorHost = envProperties
				.getMachineHostnameByRoleId(collectorRoleID);
		tomcatHost = envProperties.getMachineHostnameByRoleId(tomcatRoleID);
		freeHost1 = envProperties.getMachineHostnameByRoleId(freeRole1ID);
		freeHost2 = envProperties.getMachineHostnameByRoleId(freeRole2ID);
		freeHost3 = envProperties.getMachineHostnameByRoleId(freeRole3ID);

		momEMLog = envProperties.getRolePropertyById(momRoleID,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		momConfigDir = envProperties.getRolePropertyById(momRoleID,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR);
		agentProfile = envProperties.getRolePropertyById(tomcatRoleID,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		agentLogFile = envProperties.getRolePropertyById(tomcatRoleID,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";

		collectorEMLog = envProperties.getRolePropertyById(collectorRoleID,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		collectorEMConfigDir = envProperties.getRolePropertyById(
				collectorRoleID, DeployEMFlowContext.ENV_EM_CONFIG_DIR);

		primaryAgentLogMessage = "Connected controllable Agent to the Introscope Enterprise Manager at "
				+ collectorHost
				+ ":"
				+ collectorPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory. Host = \""
				+ collectorHost
				+ "\", Process = \"Tomcat\", Agent Name = \"Tomcat Agent\", Active = \"true\".";
		secondaryAgentLogMessage1 = "Connected controllable Agent to the Introscope Enterprise Manager at "
				+ freeHost1
				+ ":"
				+ collectorPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory. Host = \""
				+ freeHost1
				+ "\", Process = \"Tomcat\", Agent Name = \"Tomcat Agent\", Active = \"true\".";
		secondaryAgentLogMessage2 = "Connected controllable Agent to the Introscope Enterprise Manager at "
				+ freeHost1
				+ ":"
				+ collectorPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory. Host = \""
				+ freeHost2
				+ "\", Process = \"Tomcat\", Agent Name = \"Tomcat Agent\", Active = \"true\".";
		secondaryAgentLogMessage3 = "Connected controllable Agent to the Introscope Enterprise Manager at "
				+ freeHost1
				+ ":"
				+ collectorPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory. Host = \""
				+ freeHost3
				+ "\", Process = \"Tomcat\", Agent Name = \"Tomcat Agent\", Active = \"true\".";

		agentFailoverConnectionOrder.add("agentManager.url.2=" + freeHost1
				+ ":" + collectorPort);

		nfsServerLinuxCommandMom.clear();
		nfsClientLinuxCommandMom.clear();
		nfsServerWindowsCommandMom.clear();
		nfsClientWindowsCommandMom.clear();

		nfsServerWindowsCommandMom
				.add("net share emMain=c:\\automation\\deployed /GRANT:Everyone,FULL");
		nfsClientWindowsCommandMom.add("net use s: \\\\" + momHost
				+ "\\emMain /user:administrator Lister@123");

		if (!isWindows) {
			linuxSubVersion = AgentEMFailoverLinuxClusterTestbed
					.getEM_TEMPLATE_ID();
			if (linuxSubVersion.toUpperCase().contains("CO")) {

				LOGGER.info("The OS Template is ..."+linuxSubVersion);
				nfsServerLinuxCommandMom
						.add("yum -y install \"nfs-utils*\"; exportfs -a ; service rpcbind start ; echo \"/opt/automation/deployed/        *(rw,sync,no_root_squash) \" >> /etc/exports ; service nfs start; service nfs restart > /root/output.txt");
				nfsClientLinuxCommandMom
						.add("yum -y install \"nfs-utils*\"; chkconfig nfs on ; mount  "
								+ momHost
								+ ":/opt/automation/deployed/  /mnt ; service rpcbind start ; service nfs start ; service nfs restart");

				nfsServerLinuxCommandCollector
						.add("yum -y install \"nfs-utils*\"; chkconfig nfs on ; service rpcbind start ; echo \"/opt/automation/deployed_Collector/        *(rw,sync,no_root_squash) \" >> /etc/exports ; service nfs start ; service nfs restart > /root/output.txt");
				nfsClientLinuxCommandCollector
						.add("yum -y install \"nfs-utils*\"; chkconfig nfs on  ; mkdir /opt/remoteCollector/ ; mount  "
								+ momHost
								+ ":/opt/automation/deployed_Collector /opt/remoteCollector/  ; service rpcbind start ; service nfs start ; service nfs restart");

			} else {
				LOGGER.info("The OS Template Expected is ..."+linuxSubVersion);
				nfsServerLinuxCommandMom
						.add("yum -y groupinstall \"NFS file server\"; exportfs -a ; service rpcbind start ; echo \"/opt/automation/deployed/        *(rw,sync,no_root_squash) \" >> /etc/exports ; service nfs start ; service nfs restart > /root/output.txt");
				nfsClientLinuxCommandMom
						.add("yum -y groupinstall \"NFS file server\"; chkconfig nfs on ; mount  "
								+ momHost
								+ ":/opt/automation/deployed/  /mnt ; service rpcbind start ; service nfs start ; service nfs restart");

				nfsServerLinuxCommandCollector
						.add("yum -y groupinstall \"NFS file server\"; chkconfig nfs on ; service rpcbind start ; echo \"/opt/automation/deployed_Collector/        *(rw,sync,no_root_squash) \" >> /etc/exports ; service nfs start  ; service nfs restart> /root/output.txt");
				nfsClientLinuxCommandCollector
						.add("yum -y groupinstall \"NFS file server\"; chkconfig nfs on  ; mkdir /opt/remoteCollector/ ; mount  "
								+ momHost
								+ ":/opt/automation/deployed_Collector /opt/remoteCollector/  ; service rpcbind start ; service nfs start ; service nfs restart");

			}

		}

		nfsServerWindowsCommandCollector
				.add("net share Collector=c:\\automation\\deployed_Collector /GRANT:Everyone,FULL");
		nfsClientWindowsCommandCollector.add("net use t: \\\\" + momHost
				+ "\\Collector /user:administrator Lister@123");

		primaryEmStartupMesssage = "[INFO] [main] [Manager.HotFailover] The Introscope Enterprise Manager is running as a Primary EM";
		failoverEmStartupMesssage = "[INFO] [main] [Manager.HotFailover] The Introscope Enterprise Manager is running as a Secondary EM";
		primaryLockMessage = "Acquiring primary lock...";
		secondaryLockMessage = "Acquiring secondary lock...";
		shutdownMessage = "Orderly shutdown complete";
	}

	@BeforeClass(alwaysRun = true)
	public void shareTheNetworkPath() throws Exception {
		try {
			Assert.assertTrue(ApmbaseUtil.invokeProcessBuilder(Os
					.isFamily(Os.FAMILY_WINDOWS) ? nfsServerWindowsCommandMom
					: nfsServerLinuxCommandMom, "/"));

			Assert.assertTrue(ApmbaseUtil.invokeProcessBuilder(
					Os.isFamily(Os.FAMILY_WINDOWS) ? nfsServerWindowsCommandCollector
							: nfsServerLinuxCommandCollector, "/"));
			LOGGER.info("Exeuted NFS commands Succesfully ");
			harvestWait(30);

		} catch (IOException e) {
			LOGGER.error("Unable to Execute the Given commands");
			e.printStackTrace();
		}

		String originalProperty = "<property name=\"hibernate.connection.url\">jdbc:postgresql://127.0.0.1:5432/cemdb</property>";
		replaceProp(originalProperty,
				originalProperty.replace("127.0.0.1", momHost), momMachine,
				momConfigDir + "/tess-db-cfg.xml");
		LOGGER.info("Updated dbconfig file");

		FailoverModifierFlowContext FOM = FailoverModifierFlowContext
				.remoteMount(
						Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommandMom
								: nfsClientLinuxCommandMom, "/");

		runFlowByMachineId(freeMachine1, FailoverModifierFlow.class, FOM);
		LOGGER.info("Executed the command "
				+ (Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommandMom
						: nfsClientLinuxCommandMom) + "ON " + freeMachine1);
		runFlowByMachineId(freeMachine2, FailoverModifierFlow.class, FOM);
		LOGGER.info("Executed the command "
				+ (Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommandMom
						: nfsClientLinuxCommandMom) + "ON " + freeMachine2);
		runFlowByMachineId(freeMachine3, FailoverModifierFlow.class, FOM);
		LOGGER.info("Executed the command "
				+ (Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommandMom
						: nfsClientLinuxCommandMom) + "ON " + freeMachine3);

		FailoverModifierFlowContext FOM1 = FailoverModifierFlowContext
				.remoteMount(
						Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommandCollector
								: nfsClientLinuxCommandCollector, "/");

		runFlowByMachineId(freeMachine1, FailoverModifierFlow.class, FOM1);
		LOGGER.info("Executed the command "
				+ (Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommandCollector
						: nfsClientLinuxCommandCollector) + "ON "
				+ freeMachine1);
		runFlowByMachineId(freeMachine2, FailoverModifierFlow.class, FOM1);
		LOGGER.info("Executed the command "
				+ (Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommandCollector
						: nfsClientLinuxCommandCollector) + "ON "
				+ freeMachine2);
		runFlowByMachineId(freeMachine3, FailoverModifierFlow.class, FOM1);
		LOGGER.info("Executed the command "
				+ (Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommandCollector
						: nfsClientLinuxCommandCollector) + "ON "
				+ freeMachine3);
		harvestWait(20);

	}

	@Test(groups = { "full", "failover" }, enabled = true)
	public void verify_ALM_280515_PrimaryEMCollector_Recovery_WithAgentFailover()
			throws IOException {
		LOGGER.info("Waiting to check the share");
		harvestWait(900);

		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.enable", "true",
				collectorConfigFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.secondary", freeHost1,
				collectorConfigFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.primary", collectorHost,
				collectorConfigFile));

		startEM(momRoleID);
		startEM(collectorRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(collectorEMLog,
				primaryEmStartupMesssage));

		startFailOverEM(freeMachine1, failoverCollectorPath);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(collectorEMLog,
				primaryLockMessage));

		LOGGER.info("Started the Primary and Secondary EM ");

		appendProp(agentFailoverConnectionOrder, tomcatMachineID, agentProfile);
		startTomcatAgent(tomcatRoleID);
		harvestWait(60);
		LogCheckFlowContext LCP = LogCheckFlowContext.createWithNoTimeout(
				agentLogFile, primaryAgentLogMessage);
		runFlowByMachineId(tomcatMachineID, LogCheckFlow.class, LCP);

		stopEM(collectorRoleID);
		harvestWait(180);

		LogCheckFlowContext LCS = LogCheckFlowContext.createWithNoTimeout(
				agentLogFile, secondaryAgentLogMessage1);
		runFlowByMachineId(tomcatMachineID, LogCheckFlow.class, LCS);

		LOGGER.info("The test case 280515 is passed, now cleanup the instances ");

		stopTomcatAgent(tomcatRoleID);
		stopFailOverEM(freeMachine1);
		stopEM(momRoleID);

		backupFile(momEMLog, momEMLog + "_280515", momMachine);
		backupFile(collectorEMLog, collectorEMLog + "_280515", collectorMachine);
		replaceProp(agentFailoverConnectionOrder.get(0), "", tomcatMachineID,
				agentProfile);
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.enable", "false",
				collectorConfigFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.secondary", "",
				collectorConfigFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.primary", "",
				collectorConfigFile));

	}

	@Test(groups = { "full", "failover" }, enabled = true)
	public void verify_ALM_305661_MoMFailover_MultiplePrimary()
			throws IOException {

		LOGGER.info("Waiting to check the share");
		harvestWait(900);

		String multiplePrimaryHosts = momHost + "," + freeHost1 + ","
				+ freeHost2 + "," + freeHost3;
		List<String> agentFailoverEMConnectionList = new ArrayList<String>();
		agentFailoverEMConnectionList.add("agentManager.url.2=" + freeHost3
				+ ":" + momPort);

		String primaryMoMAgentLogMessage = "[INFO] [IntroscopeAgent.ConnectionThread] Connected to "
				+ momHost
				+ momPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
		String secondPrimaryMoMAgentLogMessage = "[INFO] [IntroscopeAgent.ConnectionThread] Connected to "
				+ freeHost3
				+ momPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";

		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.enable", "true",
				momConfigFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.primary",
				multiplePrimaryHosts, momConfigFile));

		harvestWait(1800);

		startEM(momRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(momEMLog,
				primaryEmStartupMesssage));
		LOGGER.info("Started the Primary EM ");

		startFailOverPrimaryEM(freeMachine1, failoverMomPath);

		LOGGER.info("Started the second Primary EM ");

		startPrimaryEMWithoutLock(freeMachine2, failoverMomPath);
		LOGGER.info("Started the third Primary EM ");

		startPrimaryEMWithoutLock(freeMachine3, failoverMomPath);
		LOGGER.info("Started the fourth Primary EM ");

		appendProp(agentFailoverEMConnectionList, tomcatMachineID, agentProfile);
		startTomcatAgent(tomcatRoleID);
		harvestWait(60);
		LogCheckFlowContext LCP = LogCheckFlowContext.createWithNoTimeout(
				agentLogFile, primaryMoMAgentLogMessage);
		runFlowByMachineId(tomcatMachineID, LogCheckFlow.class, LCP);

		stopEM(momRoleID);
		harvestWait(120);

		stopFailOverEM(freeMachine1);

		stopFailOverEM(freeMachine2);
		harvestWait(180);

		LCP = LogCheckFlowContext.createWithNoTimeout(agentLogFile,
				secondPrimaryMoMAgentLogMessage);
		runFlowByMachineId(tomcatMachineID, LogCheckFlow.class, LCP);

		LOGGER.info("The test case 305661 is passed, now cleanup the instances ");

		stopFailOverEM(freeMachine3);
		stopTomcatAgent(tomcatRoleID);
		backupFile(momEMLog, momEMLog + "_305661", momMachine);

		replaceProp(agentFailoverEMConnectionList.get(0), "", tomcatMachineID,
				agentProfile);
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.enable", "false",
				momConfigFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.primary", "",
				momConfigFile));

	}

	@Test(groups = { "deep", "failover" }, enabled = true)
	public void verify_ALM_305660_MoMFailover_SecondPrimaryStartedFirst()
			throws IOException {

		LOGGER.info("Waiting to check the share");
		harvestWait(900);

		String multiplePrimaryHosts = momHost + "," + freeHost1;

		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.enable", "true",
				momConfigFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.primary",
				multiplePrimaryHosts, momConfigFile));

		harvestWait(1800);

		startFailOverPrimaryEMFirst(freeMachine1, failoverMomPath);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(momEMLog,
				primaryEmStartupMesssage));
		LOGGER.info("Started the second Primary EM First and is running");

		startFailOverPrimaryEM(momMachine, momEMHome);
		LOGGER.info("Started the second Primary EM ");

		stopFailOverEM(momMachine);

		stopFailOverEM(freeMachine1);
		harvestWait(120);

		LOGGER.info("The test case 305660 is passed, now cleanup the instances ");

		backupFile(momEMLog, momEMLog + "_305660", momMachine);

		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.enable", "false",
				momConfigFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				"introscope.enterprisemanager.failover.primary", "",
				momConfigFile));

	}
}
