/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 * 
 * Author : TUUJA01/ JAYARAM PRASAD TADIMETI
 * Date : 13/04/2016
 */
package com.ca.apm.tests.agentEMFailoverTests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.LogCheckFlow;
import com.ca.apm.automation.action.flow.utility.LogCheckFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.flow.FailoverModifierFlow;
import com.ca.apm.commons.flow.FailoverModifierFlowContext;
import com.ca.apm.tests.testbed.AgentEMFailoverLinuxStandAloneTestbed;
import com.ca.apm.tests.testbed.AgentEMFailoverWindowsStandAloneTestbed;

public class AgentEMFailoverStandAloneTests extends AgentEMFailoverBase {

	private final String configFile;
	private final String configDir;
	private final String tomcatRoleID;
	private final String host;
	private final String freeHost;
	private final String tomcatHost;
	private final String dummyHost;
	private final String emLog;
	private final String emExecutable;
	private final String emHome;
	private final String emPort;
	private final String emLaxFile;
	private final String emMachine;
	private final String freeMachine;
	private final String freeRoleID;
	private final String emRoleID;
	private final String filoverEmPath;
	private final String tomcatMachineID;
	private final String agentProfile;
	private final String agentLogFile;
	private final String primaryAgentLogMessage;
	private final String secondaryAgentLogMessage;
	private final String negativeValueMessage;
	private final String defaulteValueMessage;
	private final String blankValueMessage;
	private final String abcValueMessage;
	private final String decimalValueMessage;
	private final String newLaxFile;
	private final String newConfigFile;
	private final String newExecutable;
	private final String emLaxJavaOptions;
	private final String emLaxNewJavaOptionsWithEmProp;
	private final String emFailoverPrimaryProp;
	private final String emFailoverSecondaryProp;
	private final List<String> agentFailoverConnectionOrder = new ArrayList<String>();
	private final List<String> nfsClientWindowsCommand = new ArrayList<String>();
	private final List<String> nfsServerLinixCommand = new ArrayList<>();
	private final List<String> nfsClientLinuxCommand = new ArrayList<String>();
	private final List<String> nfsServerWindowsCommand = new ArrayList<>();
	private final List<String> permissionCommands = new ArrayList<String>();
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AgentEMFailoverStandAloneTests.class);
	public RunCommandFlowContext emRunCmdFlowContext;
	public String linuxSubVersion;
	public String linuxNfsServerPackageCmd;
	public String linuxNfsClientPackageCmd;

	public AgentEMFailoverStandAloneTests() {

		boolean isWindows = Os.isFamily(Os.FAMILY_WINDOWS);

		emMachine = isWindows ? AgentEMFailoverWindowsStandAloneTestbed.EM_MACHINE_ID
				: AgentEMFailoverLinuxStandAloneTestbed.EM_MACHINE_ID;
		emRoleID = isWindows ? AgentEMFailoverWindowsStandAloneTestbed.EM_ROLE_ID
				: AgentEMFailoverLinuxStandAloneTestbed.EM_ROLE_ID;
		freeRoleID = isWindows ? AgentEMFailoverWindowsStandAloneTestbed.freeRole_ID
				: AgentEMFailoverLinuxStandAloneTestbed.freeRole_ID;
		freeMachine = isWindows ? AgentEMFailoverWindowsStandAloneTestbed.freeMachineID
				: AgentEMFailoverLinuxStandAloneTestbed.freeMachineID;
		tomcatRoleID = isWindows ? AgentEMFailoverWindowsStandAloneTestbed.TOMCAT_ROLE_ID
				: AgentEMFailoverLinuxStandAloneTestbed.TOMCAT_ROLE_ID;
		tomcatMachineID = isWindows ? AgentEMFailoverWindowsStandAloneTestbed.AGENT_MACHINE_ID
				: AgentEMFailoverLinuxStandAloneTestbed.AGENT_MACHINE_ID;
		filoverEmPath = isWindows ? "s:/em/" : "/mnt/em";

		emFailoverPrimaryProp = ApmbaseConstants.failoverPrimaryEMProperty
				+ "=";
		emFailoverSecondaryProp = ApmbaseConstants.failoverSecondaryEMProperty
				+ "=";

		configFile = envProperties.getRolePropertyById(emRoleID,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		emHome = (envProperties.getRolePropertyById(emRoleID,
				DeployEMFlowContext.ENV_EM_INSTALL_DIR));
		emExecutable = Os.isFamily(Os.FAMILY_WINDOWS) ? new String(emHome
				+ "/Introscope_Enterprise_Manager.exe") : new String(emHome
				+ "/Introscope_Enterprise_Manager");

		emLaxFile = emHome + "/Introscope_Enterprise_Manager.lax";

		newLaxFile = emLaxFile.replace(".lax", "2.lax");
		newConfigFile = configFile.replace(".properties", "2.properties");
		newExecutable = emHome + "/" + "Introscope_Enterprise_Manager2";
		emLaxJavaOptions = "lax.nl.java.option.additional";
		emLaxNewJavaOptionsWithEmProp = ApmbaseConstants.emLaxJavaOptions
				+ "  -Dcom.wily.introscope.em.properties=";

		host = envProperties.getMachineHostnameByRoleId(emRoleID);
		freeHost = envProperties.getMachineHostnameByRoleId(freeRoleID);
		tomcatHost = envProperties.getMachineHostnameByRoleId(tomcatRoleID);

		emLog = envProperties.getRolePropertyById(emRoleID,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		configDir = envProperties.getRolePropertyById(emRoleID,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR);
		emPort = envProperties.getRolePropertyById(emRoleID,
				DeployEMFlowContext.ENV_EM_PORT);
		agentProfile = envProperties.getRolePropertyById(tomcatRoleID,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		agentLogFile = envProperties.getRolePropertyById(tomcatRoleID,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";

		primaryAgentLogMessage = "Connected controllable Agent to the Introscope Enterprise Manager at "
				+ host
				+ ":"
				+ emPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory. Host = \"+host+\", Process = \"Tomcat\", Agent Name = \"Tomcat Agent\", Active = \"true\".";
		secondaryAgentLogMessage = "Connected controllable Agent to the Introscope Enterprise Manager at "
				+ freeHost
				+ ":"
				+ emPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory. Host = \"+freehost+\", Process = \"Tomcat\", Agent Name = \"Tomcat Agent\", Active = \"true\".";

		agentFailoverConnectionOrder.add("agentManager.url.2=" + freeHost + ":"
				+ emPort);

		nfsServerLinixCommand.clear();
		nfsClientLinuxCommand.clear();
		nfsServerWindowsCommand.clear();
		nfsClientWindowsCommand.clear();

		if (!isWindows) {
			linuxSubVersion = AgentEMFailoverLinuxStandAloneTestbed
					.getEM_TEMPLATE_ID();
			if (linuxSubVersion.toUpperCase().contains("CO")) {
				
				LOGGER.info("The OS Template is ..."+linuxSubVersion);
				linuxNfsServerPackageCmd = "yum -y install \"nfs-utils*\"; service rpcbind start ; echo \"/opt/automation/deployed        *(rw,sync,no_root_squash) \" >> /etc/exports ; service nfs start ; service nfs restart > /root/output.txt";
				linuxNfsClientPackageCmd = "yum -y install \"nfs-utils*\"; mount "
						+ host
						+ ":/opt/automation/deployed /mnt ; service rpcbind start ; service nfs start";
			} else {
				LOGGER.info("The OS Template Expected is ..."+linuxSubVersion);
				linuxNfsServerPackageCmd = "yum -y groupinstall \"NFS file server\"; service rpcbind start ; echo \"/opt/automation/deployed        *(rw,sync,no_root_squash) \" >> /etc/exports ; service nfs start ; service nfs restart > /root/output.txt";
				linuxNfsClientPackageCmd = "yum -y groupinstall \"NFS file server\"; mount "
						+ host
						+ ":/opt/automation/deployed /mnt ; service rpcbind start ; service nfs start";
			}

		}

		nfsServerWindowsCommand
				.add("net share emMain=c:\\automation\\deployed /GRANT:Everyone,FULL");
		nfsClientWindowsCommand.add("net use s: \\\\" + host
				+ "\\emMain /user:administrator Lister@123");
		nfsServerLinixCommand.add(linuxNfsServerPackageCmd);
		nfsClientLinuxCommand.add(linuxNfsClientPackageCmd);

		negativeValueMessage = "[WARN] [main] [Manager.HotFailover] introscope.enterprisemanager.failover.interval is negative: -20";
		defaulteValueMessage = "[INFO] [main] [Manager.HotFailover] Using default value for introscope.enterprisemanager.failover.interval: 120";
		blankValueMessage = "[WARN] [PO Async Executor] [Manager.HotFailover] introscope.enterprisemanager.failover.interval is not an integer:";
		abcValueMessage = "[WARN] [PO Async Executor] [Manager.HotFailover] introscope.enterprisemanager.failover.interval is not an integer: abc";
		decimalValueMessage = "[WARN] [PO Async Executor] [Manager.HotFailover] introscope.enterprisemanager.failover.interval is not an integer: 60.12";

		dummyHost = "tas-itc-n1d";

	}

	@BeforeSuite(alwaysRun = true)
	public void shareTheNetworkPath() throws Exception {

		stopEM(emRoleID);
		harvestWait(60);

		try {
			Assert.assertTrue(ApmbaseUtil.invokeProcessBuilder(Os
					.isFamily(Os.FAMILY_WINDOWS) ? nfsServerWindowsCommand
					: nfsServerLinixCommand, "/"));
			LOGGER.info(" ");
			harvestWait(30);

		} catch (IOException e) {
			LOGGER.error("Failed to Execute NFS commands");
			e.printStackTrace();
		}

		Assert.assertTrue(ApmbaseUtil.updateProperties(
				ApmbaseConstants.failoverEnableDisableProperty, "true",
				configFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				ApmbaseConstants.failoverPrimaryEMProperty, host, configFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(
				ApmbaseConstants.failoverSecondaryEMProperty, freeHost,
				configFile));
		LOGGER.info("Updated EM properties");

		String orginonal = "<property name=\"hibernate.connection.url\">jdbc:postgresql://127.0.0.1:5432/cemdb</property>";
		replaceProp(orginonal, orginonal.replace("127.0.0.1", host), emMachine,
				configDir + "/tess-db-cfg.xml");
		LOGGER.info("Updated dbconfig file");

		FailoverModifierFlowContext FOM = FailoverModifierFlowContext
				.remoteMount(
						Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommand
								: nfsClientLinuxCommand, "/");
		runFlowByMachineId(freeMachine, FailoverModifierFlow.class, FOM);

		LOGGER.info("Executed the NFS Client command "
				+ (Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommand
						: nfsClientLinuxCommand) + "ON " + freeHost);
		harvestWait(20);

	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_280513_emFailoverWithAgent() {

		takeBackupAndDelete(emLog, emMachine);
		takeBackupAndDelete(agentLogFile, tomcatMachineID);
		startEM(emRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryEmLogMessage));
		startFailOverEM(freeMachine, filoverEmPath);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryLockMessage));

		LOGGER.info("Started the Primary and Secondary EM ");

		startTomcatAgent(tomcatRoleID);
		harvestWait(60);
		checkRemoteLog(agentLogFile, primaryAgentLogMessage, tomcatMachineID);

		stopEM(emRoleID);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.secondaryEmLogMessage));

		LogCheckFlowContext LCS = LogCheckFlowContext.createWithNoTimeout(
				agentLogFile, secondaryAgentLogMessage);
		runFlowByMachineId(tomcatMachineID, LogCheckFlow.class, LCS);

		LOGGER.info("The Testcase 280513 is passed, now cleanup the instances ");

		stopFailOverEM(freeMachine);
		stopTomcatAgent(tomcatRoleID);
		backupFile(emLog, emLog + "_280513", emMachine);
		renameLogWithTestID("280513", agentLogFile, tomcatMachineID);
	}

	@Test(groups = { "Smoke Regression" }, enabled = true)
	public void verify_ALM_305657_FailPrimaryCollectorStartSecondary() {

		replaceProp(ApmbaseConstants.clusteringMode + "=" + "StandAlone",
				ApmbaseConstants.clusteringMode + "=" + "Collector", emMachine,
				configFile);
		startEM(emRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryEmLogMessage));
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.collectorModeMessage));

		startFailOverEM(freeMachine, filoverEmPath);
		harvestWait(60);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.emFailoverModeLogMessage));

		LOGGER.info("Started the Primary and Secondary EM ");

		startTomcatAgent(tomcatRoleID);
		harvestWait(60);
		checkRemoteLog(agentLogFile, primaryAgentLogMessage, tomcatMachineID);
		stopEM(emRoleID);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.secondaryEmLogMessage));
		checkRemoteLog(agentLogFile, secondaryAgentLogMessage, tomcatMachineID);

		LOGGER.info("The Testcase 305657 is passed, now cleanup the instances...");

		stopFailOverEM(freeMachine);
		replaceProp(ApmbaseConstants.clusteringMode + "=" + "Collector",
				ApmbaseConstants.clusteringMode + "=" + "StandAlone",
				emMachine, configFile);
		stopTomcatAgent(tomcatRoleID);

	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_280514_FailPrimaryMomStartSecondary() {

		takeBackupAndDelete(emLog, emMachine);
		takeBackupAndDelete(agentLogFile, tomcatMachineID);
		replaceProp(ApmbaseConstants.clusteringMode + "=" + "StandAlone",
				ApmbaseConstants.clusteringMode + "=" + "MOM", emMachine,
				configFile);
		startEM(emRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryEmLogMessage));
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.momModeMessage));
		startFailOverEM(freeMachine, filoverEmPath);
		harvestWait(60);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.emFailoverModeLogMessage));

		LOGGER.info("Started the Primary and Secondary EM ");

		startTomcatAgent(tomcatRoleID);
		harvestWait(60);
		checkRemoteLog(agentLogFile, primaryAgentLogMessage, tomcatMachineID);
		stopEM(emRoleID);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.secondaryEmLogMessage));
		checkRemoteLog(agentLogFile, secondaryAgentLogMessage, tomcatMachineID);

		LOGGER.info("The Testcase 280514 is passed, now cleanup the instances...");

		stopFailOverEM(freeMachine);
		replaceProp(ApmbaseConstants.clusteringMode + "=" + "MOM",
				ApmbaseConstants.clusteringMode + "=" + "StandAlone",
				emMachine, configFile);
		stopTomcatAgent(tomcatRoleID);
		renameLogWithTestID("280514", agentLogFile, tomcatMachineID);
		renameLogWithTestID("280514", emLog, emMachine);
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_305653_killPrimaryAndVerifySecondaryEM()
			throws Exception {

		takeBackupAndDelete(emLog, emMachine);

		startEM(emRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryEmLogMessage));

		startFailOverEM(freeMachine, filoverEmPath);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryLockMessage));
		LOGGER.info("Started the Primary and Secondary EM ");

		stopEM(emRoleID);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.secondaryEmLogMessage));

		startEM(emRoleID);
		harvestWait(60);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.failoverEmShudownMessage));

		LOGGER.info("The Testcase 305653 is Passed, now cleanup the instances...");

		stopEM(emRoleID);
		stopFailOverEM(freeMachine);
		renameLogWithTestID("305653", emLog, emMachine);
	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void verify_ALM_280518_secondaryEM_DoesnotStart_UntilKillThePrimary() {

		takeBackupAndDelete(emLog, emMachine);

		startEM(emRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryEmLogMessage));

		startFailOverEM(freeMachine, filoverEmPath);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryLockMessage));

		stopFailOverEM(freeMachine);
		harvestWait(60);
		Assert.assertTrue(ApmbaseUtil.isPortAvailable(Integer.parseInt(emPort),
				host));

		LOGGER.info("The Testcase 280518 is passed, now cleanup the instances....");

		stopEM(emRoleID);
		renameLogWithTestID("280518", emLog, emMachine);
	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void verify_ALM_305659_primaryAndSecondaryEM_onSigle_Host()
			throws Exception {

		takeBackupAndDelete(emLog, emMachine);
		backupFile(configFile, newConfigFile, emMachine);
		backupFile(emLaxFile, newLaxFile, emMachine);

		permissionCommands.add("cp -p " + emExecutable + " " + newExecutable);

		if (!(Os.isFamily(Os.FAMILY_WINDOWS))) {
			LOGGER.info("Copying the EM Executable to Introscope_Enterprise_Manager2");
			Assert.assertTrue(ApmbaseUtil
					.invokeProcessBuilder(permissionCommands));
		} else {
			backupFile(emExecutable, newExecutable + ".exe", emMachine);
		}

		Assert.assertTrue(ApmbaseUtil.updateProperties(emLaxJavaOptions,
				emLaxNewJavaOptionsWithEmProp + newConfigFile, newLaxFile));

		Assert.assertTrue(ApmbaseUtil.updateProperties("lax.application.name",
				newExecutable, newLaxFile));
		Assert.assertTrue(ApmbaseUtil.updateProperties(emLaxJavaOptions,
				emLaxNewJavaOptionsWithEmProp + configFile, emLaxFile));

		replaceProp(emFailoverPrimaryProp + host, emFailoverPrimaryProp,
				emMachine, newConfigFile);
		replaceProp(emFailoverSecondaryProp + freeHost, emFailoverSecondaryProp
				+ "localhost", emMachine, newConfigFile);

		replaceProp(emFailoverSecondaryProp + freeHost,
				emFailoverSecondaryProp, emMachine, configFile);
		replaceProp(emFailoverPrimaryProp + host, emFailoverPrimaryProp
				+ "localhost", emMachine, configFile);

		startEM(emRoleID);
		startFailOverLocalEM(emMachine, emHome);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryLockMessage));

		stopEM(emRoleID);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.secondaryEmLogMessage));

		LOGGER.info("The Testcase 305659 is passed, now cleanup the instances....");

		replaceProp(emFailoverPrimaryProp + "localhost", emFailoverPrimaryProp
				+ host, emMachine, configFile);
		replaceProp(emFailoverSecondaryProp,
				emFailoverSecondaryProp + freeHost, emMachine, configFile);

		replaceProp(emFailoverPrimaryProp, emFailoverPrimaryProp + host,
				emMachine, newConfigFile);
		replaceProp(emFailoverSecondaryProp + "localhost",
				emFailoverSecondaryProp + freeHost, emMachine, newConfigFile);
		stopFailOverLocalEM(emMachine);
		Assert.assertTrue(ApmbaseUtil.updateProperties(emLaxJavaOptions,
				ApmbaseConstants.emLaxJavaOptions, emLaxFile));
		deleteFile(newConfigFile, emMachine);
		deleteFile(newExecutable, emMachine);
		deleteFile(newLaxFile, emMachine);
		renameLogWithTestID("305659", emLog, emMachine);

	}

	@Test(groups = { "Smoke Regression" }, enabled = true)
	public void verify_ALM_392307_invalidFailoverIntervalValue() {

		takeBackupAndDelete(emLog, emMachine);

		Assert.assertTrue(ApmbaseUtil.updateProperties(
				ApmbaseConstants.failoverInterval, "-20", configFile));
		startEM(emRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryEmLogMessage));

		startFailOverEM(freeMachine, filoverEmPath);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryLockMessage));

		LOGGER.info("Started the Primary and Secondary EM ");

		stopEM(emRoleID);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.secondaryEmLogMessage));

		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog, negativeValueMessage));
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog, defaulteValueMessage));

		Assert.assertTrue(ApmbaseUtil.updateProperties(
				ApmbaseConstants.failoverInterval, " ", configFile));
		harvestWait(120);

		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog, blankValueMessage));
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog, defaulteValueMessage));

		Assert.assertTrue(ApmbaseUtil.updateProperties(
				ApmbaseConstants.failoverInterval, "60.12 ", configFile));
		harvestWait(120);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog, decimalValueMessage));
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog, defaulteValueMessage));

		Assert.assertTrue(ApmbaseUtil.updateProperties(
				ApmbaseConstants.failoverInterval, "abc ", configFile));
		harvestWait(120);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog, abcValueMessage));
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog, defaulteValueMessage));

		LOGGER.info("The Testcase 392307 is passed, now cleanup the instances...");

		Assert.assertTrue(ApmbaseUtil.updateProperties(
				ApmbaseConstants.failoverInterval, "120", configFile));
		stopFailOverEM(freeMachine);
		renameLogWithTestID("392307", emLog, emMachine);
	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void verify_ALM_305663_emShareOnMultipleHosts() {

		takeBackupAndDelete(emLog, emMachine);

		FailoverModifierFlowContext FOM_2 = FailoverModifierFlowContext
				.remoteMount(
						Os.isFamily(Os.FAMILY_WINDOWS) ? nfsClientWindowsCommand
								: nfsClientLinuxCommand, "/");

		runFlowByMachineId(tomcatMachineID, FailoverModifierFlow.class, FOM_2);

		replaceProp(emFailoverSecondaryProp + freeHost, emFailoverSecondaryProp
				+ freeHost + "," + tomcatHost, emMachine, configFile);

		startEM(emRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryEmLogMessage));

		startFailOverEM(freeMachine, filoverEmPath);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryLockMessage));

		startFailOverEM(tomcatMachineID, filoverEmPath);
		LOGGER.info("Started Primary EM and TWO failover EMS, One is on free machine and one is ON Tomcat Machine");

		stopEM(emRoleID);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.secondaryEmLogMessage));

		stopFailOverEM(freeMachine);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.secondaryEmLogMessage));

		LOGGER.info("The Testcase 305663 is passed, now cleanup the instances...");

		stopFailOverEM(tomcatMachineID);
		replaceProp(emFailoverSecondaryProp + freeHost + "," + tomcatHost,
				emFailoverSecondaryProp + freeHost, emMachine, configFile);
		renameLogWithTestID("305663", emLog, emMachine);
	}

	@Test(groups = { "Smoke Regression" }, enabled = true)
	public void verify_ALM_305662_non_EM_Failover_Tests() {

		takeBackupAndDelete(emLog, emMachine);

		replaceProp(emFailoverPrimaryProp + host, emFailoverPrimaryProp
				+ dummyHost, emMachine, configFile);
		startEM(emRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.nonFailoverEmLogMessage));

		LOGGER.info("The test case 305662 is passed, now cleanup the instances...");

		stopEM(emRoleID);
		replaceProp(emFailoverPrimaryProp + dummyHost, emFailoverPrimaryProp
				+ host, emMachine, configFile);
		renameLogWithTestID("305662", emLog, emMachine);
	}

	@Test(groups = { "Deep Regression" }, enabled = true)
	public void verify_ALM_420327_failoverCheck_Ipaddress() throws IOException {

		takeBackupAndDelete(emLog, emMachine);

		replaceProp(emFailoverSecondaryProp + freeHost, emFailoverSecondaryProp
				+ returnIPforGivenHost(freeHost), emMachine, configFile);

		startEM(emRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryEmLogMessage));

		startFailOverEM(freeMachine, filoverEmPath);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryLockMessage));

		stopEM(emRoleID);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.secondaryEmLogMessage));
		Assert.assertFalse(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.emErrorLogMessage));

		LOGGER.info("The Testcase 420327 is passed, now cleanup the instances...");

		replaceProp(emFailoverSecondaryProp + returnIPforGivenHost(freeHost),
				emFailoverSecondaryProp + freeHost, emMachine, configFile);
		stopFailOverEM(freeMachine);
		renameLogWithTestID("420327", emLog, emMachine);

	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_305656_EmAndAgentFailover() throws IOException {

		takeBackupAndDelete(emLog, emMachine);

		startEM(emRoleID);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryEmLogMessage));
		startFailOverEM(freeMachine, filoverEmPath);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.primaryLockMessage));

		LOGGER.info("Started the Primary and Secondary EM ");

		appendProp(agentFailoverConnectionOrder, tomcatMachineID, agentProfile);
		startTomcatAgent(tomcatRoleID);
		harvestWait(60);
		checkRemoteLog(agentLogFile, primaryAgentLogMessage, tomcatMachineID);

		stopEM(emRoleID);
		harvestWait(180);
		Assert.assertTrue(ApmbaseUtil.checklogMsg(emLog,
				ApmbaseConstants.secondaryEmLogMessage));
		checkRemoteLog(agentLogFile, secondaryAgentLogMessage, tomcatMachineID);

		LOGGER.info("The Testcase 305656 is passed, now cleanup the instances.... ");

		stopFailOverEM(freeMachine);
		stopTomcatAgent(tomcatRoleID);
		renameLogWithTestID("305656", emLog, emMachine);
		replaceProp(agentFailoverConnectionOrder.get(0), "#"
				+ agentFailoverConnectionOrder.get(0), tomcatMachineID,
				agentProfile);
	}
	
    public void renameLogWithTestID(String testID, String logFile, String MachineID) {
        backupFile(logFile, logFile + testID, MachineID);
        deleteFile(logFile, MachineID);
        moveFile(logFile + "_1", logFile, MachineID);

    }

}
