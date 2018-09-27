/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

package com.ca.apm.tests.testbed;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.utility.Win32RegistryFlow;
import com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.CodaTestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegHive.LOCAL_MACHINE;
import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegValueType.DWORD;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * WebDriverkansr04 class.
 *
 * WebDriverkansr04 test-bed
 *
 * @author jamsa07@ca.com
 */
@TestBedDefinition
public class CEMBatTestBed extends CodaTestBed {

	public static final String JAVA6_ROLE_ID = "java6Role";
	public static final String JAVA7_ROLE_ID = "java7Role";
	public static final String JAVA7_LINUX_ROLE_ID = "java7LinuxRole";
	public static final String TIM_ROLE_ID = "timRole";
	public static final String WEBLOGIC_ROLE_ID = "Weblogic103Role";

	public static final String JAVA6_HOME = "C:\\automation\\deployed\\Java1.6";
	public static final String JAVA7_HOME = "C:\\automation\\deployed\\Java1.7";
	public static final String JAVA7_LINUX_HOME = "/opt/automation/java1.7";

	public static String MEDREC_HOSTNAME, EM_HOSTNAME, CLIENT_HOSTNAME, TIM_HOSTNAME;


	@Override
	public ITestbed create(ITasResolver tasResolver) {

        IRole controllerRole = initControllerBuilder(tasResolver).build();
        ITestbedMachine controller = TestBedUtils
            .createWindowsMachine("controllerMachine", TEMPLATE_W64, controllerRole);
        ITestbed testBed = new Testbed(getTestBedName())
            .addMachine(controller);

		WebLogicRole weblogic01 =
			new WebLogicRole.Builder("wls103", tasResolver)
					.customComponentPaths(
							Sets.newHashSet(
									"WebLogic Server/Core Application Server",
									"WebLogic Server/Administration Console",
									"WebLogic Server/Configuration Wizard and Upgrade Framework",
									"WebLogic Server/Web 2.0 HTTP Pub-Sub Server",
									"WebLogic Server/WebLogic JDBC Drivers",
									"WebLogic Server/Third Party JDBC Drivers",
									"WebLogic Server/WebLogic Server Clients",
									"WebLogic Server/WebLogic Web Server Plugins",
									"WebLogic Server/UDDI and Xquery Support",
									"WebLogic Server/Server Examples"))
					.build();

        IRole java6Role = new JavaRole.Builder(JAVA6_ROLE_ID, tasResolver)
            .dir(JAVA6_HOME)
            .build();

        DeployFreeRole tim01Role = new DeployFreeRole("tim01");

        IRole java7Role = new JavaRole.Builder(JAVA7_ROLE_ID, tasResolver)
            .dir(JAVA7_HOME)
            .build();

        IRole java7LinuxRole = new JavaRole.LinuxBuilder(JAVA7_LINUX_ROLE_ID, tasResolver)
            .dir(JAVA7_LINUX_HOME)
            .build();

        TestbedMachine clientResultsMachine = new TestbedMachine.Builder("clientResultsMachine").templateId(TEMPLATE_W64).build();
		TestbedMachine emMachine = new TestbedMachine.Builder("emMachine").templateId(TEMPLATE_W64).build();
		TestbedMachine agentMachine = new TestbedMachine.Builder("agentMachine").templateId(TEMPLATE_W64).build();
		TestbedMachine timMachine = new TestbedMachine.LinuxBuilder("timMachine").templateId(TEMPLATE_CO66).build();
		DeployFreeRole em01Role = new DeployFreeRole("em01");
		DeployFreeRole clientresultsRole = new DeployFreeRole("clientresults");

		MEDREC_HOSTNAME=tasResolver.getHostnameById(weblogic01.getRoleId());
		EM_HOSTNAME=tasResolver.getHostnameById(em01Role.getRoleId());
		CLIENT_HOSTNAME=tasResolver.getHostnameById(clientresultsRole.getRoleId());
		TIM_HOSTNAME=tasResolver.getHostnameById(tim01Role.getRoleId());


		//create machines used in CODA test
		em01Role.addProperty("bat.name", "remoteCopy.bat");
		em01Role.addProperty("db", "postgres");
		em01Role.addProperty("dbHost", EM_HOSTNAME);
		em01Role.addProperty("dbName", "cemdb");
		em01Role.addProperty("dbOwner", "admin");
		em01Role.addProperty("dbOwnerPwd", "quality");
		em01Role.addProperty("dbPort", "5432");
		em01Role.addProperty("dbSystemUser", "SYSTEM");
		em01Role.addProperty("dbSystemUserPwd", "tiger123");
		em01Role.addProperty("default.home", "C:/default");
		em01Role.addProperty("eem.hostname", "10.131.57.4");
		em01Role.addProperty("eem.password", "admin");
		em01Role.addProperty("eem.username", "EiamAdmin");
		em01Role.addProperty("em.loc", "C:/sw/em");
		em01Role.addProperty("emProxyHost", "votsu01-i66169");
		em01Role.addProperty("emProxyPort", "8888");
		em01Role.addProperty("hostfullname", EM_HOSTNAME);
		em01Role.addProperty("install.parent.dir", "C:/sw");
		em01Role.addProperty("invalidhostname", "xyz");
		em01Role.addProperty("ip", EM_HOSTNAME);
		em01Role.addProperty("ipaddress", EM_HOSTNAME);
		em01Role.addProperty("java.home", JAVA7_HOME);
		em01Role.addProperty("local.em.dir", "C:/Builds");
		em01Role.addProperty("mapped.dir", "C$/sw/results");
		em01Role.addProperty("medrec.hostname", MEDREC_HOSTNAME);
		em01Role.addProperty("medrec.ip", MEDREC_HOSTNAME);
		em01Role.addProperty("medrec.port", "7011");
		em01Role.addProperty("myresults.dir", "C:/sw/results");
		em01Role.addProperty("otherhostname", "deepdive22");
		em01Role.addProperty("proxyMappedHost", "xxxxx");
		em01Role.addProperty("qcuploadtool.java.home", JAVA6_HOME);
		em01Role.addProperty("slave1", CLIENT_HOSTNAME);
		em01Role.addProperty("tim.admin", "admin");
		em01Role.addProperty("tim.hostname", TIM_HOSTNAME);
		em01Role.addProperty("tim.ip", TIM_HOSTNAME);
		em01Role.addProperty("tim.password", "Lister@123");
		emMachine.addRole(java6Role);
		emMachine.addRole(java7Role);
		emMachine.addRole(em01Role);

		testBed.addMachine(emMachine);

		tim01Role.addProperty("automation.dir", "/root/CA_AUTOMATION");
		tim01Role.addProperty("base.results.dir", "/root/sw/coda-results");
		tim01Role.addProperty("downloader", "http");
		tim01Role.addProperty("el.version", "6");
		tim01Role.addProperty("ipaddress", TIM_HOSTNAME);
		tim01Role.addProperty("java.home", JAVA7_LINUX_HOME);
		tim01Role.addProperty("os.codename", "RedHat");
		tim01Role.addProperty("os.specific", "Linux");
		tim01Role.addProperty("os.version", "6.4");
		tim01Role.addProperty("platform", "TIM");
		tim01Role.addProperty("python.executable", "/usr/bin/python");

		timMachine.addRole(java7LinuxRole);
		timMachine.addRole(tim01Role);
		testBed.addMachine(timMachine);

		clientresultsRole.addProperty("agent.hostname", "10.131.74.76");
		clientresultsRole.addProperty("agent.ip", "10.134.12.249");
		clientresultsRole.addProperty("agent.wls.port", "7011");
		clientresultsRole.addProperty("bat.name", "remoteCopy.bat");
		clientresultsRole.addProperty("bea.home", "C:/bea");
		clientresultsRole.addProperty("dbHost", EM_HOSTNAME);
		clientresultsRole.addProperty("dbName", "cemdb");
		clientresultsRole.addProperty("dbOwner", "admin");
		clientresultsRole.addProperty("dbOwnerPwd", "quality");
		clientresultsRole.addProperty("dbPort", "5432");
		clientresultsRole.addProperty("dbSystemUser", "postgres");
		clientresultsRole.addProperty("dbSystemUserPwd", "quality1!");
		clientresultsRole.addProperty("default.home", "C:/default");
		clientresultsRole.addProperty("eem.hostname", "10.131.57.4");
		clientresultsRole.addProperty("eem.password", "admin");
		clientresultsRole.addProperty("eem.username", "EiamAdmin");
		clientresultsRole.addProperty("em.loc", "C:/sw/em");
		clientresultsRole.addProperty("emailId", "test@ca.com");
		clientresultsRole.addProperty("emailSMTPHost", "bilsa02-test.ca.com");
		clientresultsRole.addProperty("emProxyHost", "votsu01-i66169");
		clientresultsRole.addProperty("emProxyPort", "8888");
		clientresultsRole.addProperty("fromAddressMail", "Technicalsupport@ca.com");
		clientresultsRole.addProperty("hostfullname", CLIENT_HOSTNAME);
		clientresultsRole.addProperty("invalidhostname", "xyz");
		clientresultsRole.addProperty("ip", "10.131.74.63");
		clientresultsRole.addProperty("ipaddress", "10.131.74.63");
		clientresultsRole.addProperty("java.home", JAVA7_HOME);
		clientresultsRole.addProperty("local.em.dir", "C:/Builds");
		clientresultsRole.addProperty("mapped.dir", "C$/sw/results");
		clientresultsRole.addProperty("medrec.hostname", JAVA7_HOME);
		clientresultsRole.addProperty("medrec.ip", MEDREC_HOSTNAME);
		clientresultsRole.addProperty("medrec.port", "7011");
		clientresultsRole.addProperty("myresults.dir", "C:/sw/results");
		clientresultsRole.addProperty("otherhostname", "deepdive22");
		clientresultsRole.addProperty("passwordMail", "wilyc@1mage");
		clientresultsRole.addProperty("portValue", "25");
		clientresultsRole.addProperty("proxyMappedHost", "xxxxx");
		clientresultsRole.addProperty("qcuploadtool.java.home", JAVA6_HOME);
		clientresultsRole.addProperty("slave1", "votsu01-I66166");
		clientresultsRole.addProperty("tim.admin", "admin");
		clientresultsRole.addProperty("tim.hostname", TIM_HOSTNAME);
		clientresultsRole.addProperty("tim.ip", TIM_HOSTNAME);
		clientresultsRole.addProperty("tim.password", "Lister@123");
		clientresultsRole.addProperty("toAddressMail", "dudi@cem.com");
		clientresultsRole.addProperty("userNameMail", "dudi@cem.com");
		clientresultsRole.addProperty("was7.admin.port", "9060");
		clientresultsRole.addProperty("was7.appserver.dir", "C:/IBM/WebSphere/AppServer");
		clientresultsRole.addProperty("was7.home", "C:/IBM/WebSphere");
		clientresultsRole.addProperty("was7.node.info", "Node01");
		clientresultsRole.addProperty("was7.port", "9080");
		clientresultsRole.addProperty("weblogic.version", "10.3");
		clientresultsRole.addProperty("wils.baseurl", "/medrec/index.action");
		clientresultsRole.addProperty("wls.home", "C:/bea/wlserver_10.3");
		clientresultsRole.addProperty("wls.port", "7011");
		clientResultsMachine.addRole(java6Role);
		clientResultsMachine.addRole(java7Role);
		clientResultsMachine.addRole(clientresultsRole);

		clientResultsMachine.addRole(new DeployFreeRole("client01"));
		testBed.addMachine(clientResultsMachine);

		DeployFreeRole agent03Role = new DeployFreeRole("agent03");
		agent03Role.addProperty("bat.name", "remoteCopy.bat");
		agent03Role.addProperty("bea.home", "C:/bea");
		agent03Role.addProperty("default.home", "C:/Default");
		agent03Role.addProperty("em.loc", "${testbed_em.install.parent.dir}/em");
		agent03Role.addProperty("epagent.home", "C:/Progra~1/CA Wily/Introscope9.1.0.0");
		agent03Role.addProperty("epagent.java.home", JAVA6_HOME);
		agent03Role.addProperty("hostfullname", MEDREC_HOSTNAME);
		agent03Role.addProperty("invalidhostname", "xyz");
		agent03Role.addProperty("ip", "10.131.98.60");
		agent03Role.addProperty("ipaddress", MEDREC_HOSTNAME);
		agent03Role.addProperty("java.home", JAVA6_HOME);
		agent03Role.addProperty("jbosseap.adminport", "9990");
		agent03Role.addProperty("jbosseap.home", "C:/jboss-eap-6.1");
		agent03Role.addProperty("jbosseap.port", "8880");
		agent03Role.addProperty("local.agent.dir", "C:/builds");
		agent03Role.addProperty("local.em.dir", "C:/builds");
		agent03Role.addProperty("mapped.dir", "C$/sw/results");
		agent03Role.addProperty("medrec.hostname", MEDREC_HOSTNAME);
		agent03Role.addProperty("medrec.ip", MEDREC_HOSTNAME);
		agent03Role.addProperty("medrec.port", "7011");
		agent03Role.addProperty("myresults.dir", "C:/sw/results");
		agent03Role.addProperty("otherhostname", "deepdive22");
		agent03Role.addProperty("qcuploadtool.java.home", JAVA6_HOME);
		agent03Role.addProperty("slave1", "votsu01-i66166");
		agent03Role.addProperty("tim.admin", "admin");
		agent03Role.addProperty("tim.hostname", TIM_HOSTNAME);
		agent03Role.addProperty("tim.ip", TIM_HOSTNAME);
		agent03Role.addProperty("tim.password", "quality");
		agent03Role.addProperty("tomcat.agent.hostname", "votsu01-w2k3");
		agent03Role.addProperty("tomcat.home", "C:/tomcat");
		agent03Role.addProperty("tomcat.port", "8088");
		agent03Role.addProperty("tomcat.version", "6.0");
		agent03Role.addProperty("tomcatcxf.home", "C:/tomcatcxf");
		agent03Role.addProperty("tomcatcxf.port", "8383");
		agent03Role.addProperty("tt83784.jdbcConnectionURL", "jdbc:clarity:sqlserver://CLRTSTAGE01:1433;DatabaseName=niku;InsensitiveResultSetBufferSize=0;ProgramName=niku");
		agent03Role.addProperty("tt83784.jdbcDelay", "2");
		agent03Role.addProperty("tt83784.jdbcDriverClass", "com.ca.clarity.jdbc.sqlserver.SQLServerDriver");
		agent03Role.addProperty("tt83784.jdbcPassword", "clarity#123");
		agent03Role.addProperty("tt83784.jdbcQuery", "select * from cmn_sec_users");
		agent03Role.addProperty("tt83784.jdbcUserName", "niku");
		agent03Role.addProperty("was.admin.port", "9060");
		agent03Role.addProperty("was.appserver.dir", "C:/IBM/WebSphere/AppServer/profiles/AppSrv01");
		agent03Role.addProperty("was.home", "C:/IBM/WebSphere");
		agent03Role.addProperty("was.port", "9080");
		agent03Role.addProperty("was.server.info", "server1");
		agent03Role.addProperty("was8.admin.port", "9060");
		agent03Role.addProperty("was8.appserver.dir", "C:/IBM/WebSphere/AppServer/profiles/AppSrv01");
		agent03Role.addProperty("was8.home", "C:/IBM/WebSphere");
		agent03Role.addProperty("was8.node.info", "Node01");
		agent03Role.addProperty("was8.port", "9080");
		agent03Role.addProperty("was8.server.info", "server1");
		agent03Role.addProperty("weblogic.version", "10.3");
		agent03Role.addProperty("wls.home", "C:/bea/wlserver_10.3");
		agent03Role.addProperty("wls.port", "7011");
		agentMachine.addRole(java6Role);
		agentMachine.addRole(weblogic01);
		agentMachine.addRole(agent03Role);

		agentMachine.addRole(new DeployFreeRole("client07"));
		agentMachine.addRole(new DeployFreeRole("agent04"));
		agentMachine.addRole(new DeployFreeRole("agent01"));
		testBed.addMachine(agentMachine);

        fixRegistryForJenkinsRole(tasResolver, controller, controller.getRoles());
        fixRegistryForJenkinsRole(tasResolver, emMachine, emMachine.getRoles());
        fixRegistryForJenkinsRole(tasResolver, timMachine, timMachine.getRoles());
        fixRegistryForJenkinsRole(tasResolver, clientResultsMachine, clientResultsMachine.getRoles());
        fixRegistryForJenkinsRole(tasResolver, agentMachine, agentMachine.getRoles());

		return testBed;
	}

	@NotNull
	@Override
	protected String getTestBedName() {
		return getClass().getSimpleName();
	}

    public static IRole fixRegistryForJenkinsRole(ITasResolver tasResolver, ITestbedMachine machine,
        IRole... beforeRoles) {
        Win32RegistryFlowContext context = new Win32RegistryFlowContext.Builder()
            .setValue(LOCAL_MACHINE, "SYSTEM\\CurrentControlSet\\Services\\LanmanServer"
                    + "\\Parameters\\SMB1",
                DWORD, 1)
            .build();

        UniversalRole role = new UniversalRole.Builder(machine.getMachineId() + "_SMBv1Enable",
            tasResolver)
            .runFlow(Win32RegistryFlow.class, context)
            .build();
        machine.addRole(role);

        if (beforeRoles != null) {
            for (IRole r : beforeRoles) {
                role.before(r);
            }
        }

        return role;
    }
}

