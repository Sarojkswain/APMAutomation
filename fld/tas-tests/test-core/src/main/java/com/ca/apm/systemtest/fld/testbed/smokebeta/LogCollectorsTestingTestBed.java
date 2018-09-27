/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.testbed.smokebeta;

import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.TOMCAT_6_AGENT_ROLE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.TOMCAT_6_AXIS2_ROLE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.TOMCAT_6_QATESTAPP_ROLE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.TOMCAT_6_ROLE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.TOMCAT_6_TESTAPP_ROLE_ID;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJavaDir;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.systemtest.fld.artifact.QATestAppArtifact;
import com.ca.apm.systemtest.fld.artifact.TessTestArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.Axis2WebappVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.FLDJmeterScriptsVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.JMeterVersion;
import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.apm.systemtest.fld.role.loads.JMeterLoadRole;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.EmRole.LinuxBuilder;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * Minimal testbed for EM, Collector, Tomcat, jmeter installation.
 * Used mostly for logger testing.
 * Extracted from FLD testbed.
 *
 * @author filja01 (original FLD one)
 * @author shadm01
 */
@TestBedDefinition
public class LogCollectorsTestingTestBed implements ITestbedFactory {

    public static final String DEFAULT_EM_VERSION = "99.99.sys-SNAPSHOT";

    public static final String INSTALL_DIR = "/home/sw/em/Introscope";
    public static final String INSTALL_TG_DIR = "/home/sw/em/Installer";
    public static final String DATABASE_DIR = "/data/em/database";
    public static final String GC_LOG_FILE = INSTALL_DIR + "/logs/gclog.txt";

    public static final String DB_PASSWORD = "password";
    public static final String DB_ADMIN_PASSWORD = "password123";
    public static final String DB_USERNAME = "cemadmin";
    public static final String DB_ADMIN_USERNAME = "postgres";

    public static final int EMWEBPORT = 8081;

    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
            "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1600m", "-Xmx1600m",
            "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
            "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    private static final Collection<String> COLL01_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
            "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50", "-Xms1600m", "-Xmx1600m",
            "-verbose:gc", "-Xloggc:" + GC_LOG_FILE);


    private final String EM_DB_ROLE = "emDataBaseRole";
    private final String EM_MOM_ROLE = "emMomRole";
    private final String COLLECTOR_ROLE = "collectorRole";

    private final String MOM_MACHINE = "momMachine"; //linux
    private final String COLLECTOR_MACHINE = "collectorMachine"; //linux
    private final String TOMCAT_MACHINE = "tomcatMachine"; //w64
    private final String JMETER_MACHINE = "jmeterMachine"; //w64

    JavaBinary javaVersion = JavaBinary.WINDOWS_64BIT_JDK_17;

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        Testbed testbed = new Testbed("FLDMainClusterTestbed");

        //MOM machine
        ITestbedMachine momMachine =
                new TestbedMachine.LinuxBuilder(MOM_MACHINE)
                        .templateId("cod64")
                        .bitness(Bitness.b64)
                        .build();
        LinuxBuilder momBuilder = new LinuxBuilder(EM_MOM_ROLE, tasResolver);

        //Database Role
        EmRole databaseRole =
                new LinuxBuilder(EM_DB_ROLE, tasResolver)
                        .silentInstallChosenFeatures(Arrays.asList("Database"))
                        .dbuser(DB_USERNAME)
                        .dbpassword(DB_PASSWORD)
                        .dbAdminUser(DB_ADMIN_USERNAME)
                        .dbAdminPassword(DB_ADMIN_PASSWORD)
                        .nostartEM()
                        .nostartWV()
                        .version(DEFAULT_EM_VERSION)
                        .databaseDir(DATABASE_DIR)
                        .installDir(INSTALL_DIR)
                        .installerTgDir(INSTALL_TG_DIR)
                        .build();

        momMachine.addRole(databaseRole);

        //Collectors machine
        ITestbedMachine collectorMachine =
                new TestbedMachine.LinuxBuilder(COLLECTOR_MACHINE)
                        .templateId("w64")
                        .bitness(Bitness.b64)
                        .build();

        //Collector role
        LinuxBuilder collBuilder = new LinuxBuilder(COLLECTOR_ROLE, tasResolver);
        collBuilder
                .silentInstallChosenFeatures(
                        Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .nostartEM()
                .nostartWV()
                .dbpassword(DB_PASSWORD)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbuser(DB_USERNAME)
                .dbhost(tasResolver.getHostnameById(EM_DB_ROLE))
                .version(DEFAULT_EM_VERSION)
                .installDir(INSTALL_DIR)
                .installerTgDir(INSTALL_TG_DIR);


        collBuilder.emLaxNlClearJavaOption(COLL01_LAXNL_JAVA_OPTION);
        EmRole collectorRole = collBuilder.build();

        collectorRole.after(databaseRole);
        collectorMachine.addRole(collectorRole);
        momBuilder.emCollector(collectorRole);

        testbed.addMachine(collectorMachine);

        //MOM role settings
        momBuilder
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .nostartEM()
                .nostartWV()
                .dbpassword(DB_PASSWORD)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbuser(DB_USERNAME)
                .dbhost(tasResolver.getHostnameById(EM_DB_ROLE))
                .version(DEFAULT_EM_VERSION)
                .emWebPort(EMWEBPORT)
                .installDir(INSTALL_DIR)
                .installerTgDir(INSTALL_TG_DIR)
                .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION);

        EmRole momRole = momBuilder.build();
        momRole.after(collectorRole);

        momMachine.addRole(momRole);
        testbed.addMachine(momMachine);


        //LOADS

        addTomcatLoad(tasResolver, testbed, momRole);
        addJmeterLoad(tasResolver, testbed);

        return testbed;
    }


    private void addJmeterLoad(ITasResolver tasResolver, Testbed testbed) {
        ITestbedMachine jmeterMachine = new TestbedMachine.Builder(JMETER_MACHINE)
                .platform(Platform.WINDOWS)
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .bitness(Bitness.b64)
                .build();


        JavaRole javaRole = new JavaRole.Builder("jmeterJavaRole" + 0, tasResolver)
                .dir(getJavaDir(JavaBinary.WINDOWS_64BIT_JDK_17))
                .version(JavaBinary.WINDOWS_64BIT_JDK_17)
                .build();

        JMeterRole jmeterRole = new JMeterRole.Builder("jmeterRole01", tasResolver)
                .jmeterVersion(JMeterVersion.v213)
                .jmeterScriptsArchive(FLDJmeterScriptsVersion.v10_3_1)
                .customJava(javaRole)
                .build();



        String hostTomcat9080 = tasResolver.getHostnameById(TOMCAT_6_ROLE_ID);
        String script = "Tomcat_fld_01_02_Tomcat_9080_Axis2.jmx";
        int targetPort = 8080;

        JMeterLoadRole jmeterLoadRole = new JMeterLoadRole.Builder("tomcat9080Role", tasResolver)
                .host(hostTomcat9080)
                .hostHeader(hostTomcat9080)
                .port(targetPort)
                .script(script)
                .resultFile(script.replace(".jmx", ".csv"))
                .isOutputToFile(true)
                .jmeter(jmeterRole)
                .build();

        jmeterMachine.addRole(javaRole, jmeterRole, jmeterLoadRole);

        testbed.addMachine(jmeterMachine);
    }


    private void addTomcatLoad(ITasResolver tasResolver, Testbed testbed, EmRole momRole) {
        ITestbedMachine tomcat = new TestbedMachine.Builder(TOMCAT_MACHINE)
                .platform(Platform.WINDOWS)
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .bitness(Bitness.b64)
                .build();


        JavaRole javaRole = new JavaRole.Builder("TomcatJavaRole" + 0, tasResolver)
                .dir(getJavaDir(javaVersion))
                .version(javaVersion)
                .build();


        WebAppRole<TomcatRole> axis2Role = null;
        axis2Role = new WebAppRole.Builder<TomcatRole>(TOMCAT_6_AXIS2_ROLE_ID)
                .artifact(Axis2WebappVersion.v154.getArtifact())
                .cargoDeploy()
                .contextName("axis2")
                .build();

        WebAppRole<TomcatRole> tessTestRole = new WebAppRole.Builder<TomcatRole>(TOMCAT_6_TESTAPP_ROLE_ID)
                .artifact(new TessTestArtifact(tasResolver).createArtifact())
                .cargoDeploy()
                .contextName("tesstest")
                .build();

        WebAppRole<TomcatRole> qaTestAppRole = null;

        qaTestAppRole = new WebAppRole.Builder<TomcatRole>(TOMCAT_6_QATESTAPP_ROLE_ID)
                .artifact(new QATestAppArtifact("jvm7-tomcat", tasResolver).createArtifact())
                .cargoDeploy()
                .contextName("QATestApp")
                .build();

        TomcatRole tomcat6Role = new TomcatRole.Builder(TOMCAT_6_ROLE_ID, tasResolver)
                .additionalVMOptions(Arrays.asList("-Xms256m", "-Xmx512m", "-XX:PermSize=256m",
                        "-XX:MaxPermSize=512m", "-server",
                        "-Dcom.wily.introscope.agent.agentName=Tomcat6"))
                .tomcatArtifact(new DefaultArtifact("com.ca.apm.binaries", "tomcat", "zip", "6.0.45"), TomcatVersion.v60)
                .webApp(axis2Role)
                .webApp(qaTestAppRole)
                .webApp(tessTestRole)
                .customJava(javaRole)
                .jdkHomeDir(getJavaDir(javaVersion))
                .autoStart()
                .build();

        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("introscope.agent.hostName", "Tomcat6_axis2_QATestApp_tesstest_" + tasResolver.getHostnameById(TOMCAT_6_ROLE_ID));
        IRole tomcatAgentRole = new AgentRole.Builder(TOMCAT_6_AGENT_ROLE_ID, tasResolver)
                .webAppServer(tomcat6Role)
                .intrumentationLevel(AgentInstrumentationLevel.FULL)
                .emRole(momRole)
                .additionalProperties(additionalProperties)
                .build();


        tomcat.addRole(javaRole, axis2Role, qaTestAppRole, tomcat6Role, tomcatAgentRole, tessTestRole);

        testbed.addMachine(tomcat);
    }

}
