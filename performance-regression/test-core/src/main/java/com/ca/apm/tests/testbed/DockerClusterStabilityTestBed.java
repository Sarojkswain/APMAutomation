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
package com.ca.apm.tests.testbed;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.artifact.*;
import com.ca.apm.tests.role.*;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.IBuiltArtifact.Version;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.HammondRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;
import org.eclipse.aether.artifact.Artifact;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ClusterRegression class
 * <p/>
 * TestBed description
 */
@TestBedDefinition
public class DockerClusterStabilityTestBed implements ITestbedFactory {

    public static final String MOM_DOCKER_ROLE_ID = "momDockerComposeRole";
    public static final String DB_DOCKER_ROLE_ID = "dbDockerComposeRole";
    public static final String WV_DOCKER_ROLE_ID = "wvDockerComposeRole";
    public static final String COL1_DOCKER_ROLE_ID = "col1DockerComposeRole";
    public static final String COL2_DOCKER_ROLE_ID = "col2DockerComposeRole";
    public static final String COL3_DOCKER_ROLE_ID = "col3DockerComposeRole";
    public static final String[] EM_C_DOCKER_ROLES = {COL1_DOCKER_ROLE_ID, COL2_DOCKER_ROLE_ID, COL3_DOCKER_ROLE_ID};

    public static final String HAMMOND1_ROLE_ID = "hammond1Role";
    public static final String HAMMOND2_ROLE_ID = "hammond2Role";
    public static final String HAMMOND3_ROLE_ID = "hammond3Role";
    public static final String WURLITZER_ROLE_ID = "wurlitzerRole";
    public static final String FAKE_WORKSTATION_ROLE_ID = "fakeWorkstationRole";
    public static final String FAKE_WORKSTATION_CAUTL_ROLE_ID = "fakeWorkstationCautlRole";
    public static final String JMETER_ROLE_ID = "jmeterRole";
    public static final String JMETER_SCRIPT_ROLE_ID = "jmeterScriptRole";
    public static final String WEB_VIEW_LOAD_ROLE = "webViewLoadRole";

    public static final String DB_DOCKER_MACHINE_ID = "dbDockerMachine";
    public static final String MOM_DOCKER_MACHINE_ID = "momDockerMachine";
    public static final String COL1_DOCKER_MACHINE_ID = "col1DockerMachine";
    public static final String COL2_DOCKER_MACHINE_ID = "col2DockerMachine";
    public static final String COL3_DOCKER_MACHINE_ID = "col3DockerMachine";
    public static final String[] EM_C_DOCKER_MACHINES = {COL1_DOCKER_MACHINE_ID, COL2_DOCKER_MACHINE_ID, COL3_DOCKER_MACHINE_ID};

    public static final String LOAD1_MACHINE_ID = "load1Machine";
    public static final String LOAD2_MACHINE_ID = "load2Machine";
    public static final String LOAD3_MACHINE_ID = "load3Machine";
    public static final String LOAD4_MACHINE_ID = "load4Machine";
    public static final String LOAD5_MACHINE_ID = "load5Machine";

    public static final String XM_MOM_MEM = "5120";
    public static final String XM_COL_MEM = "2560";
    public static final String XM_WV_MEM = "1024";

    public static final String DOCKER_MACHINE_TEMPLATE = "co7";
    public static final String DOCKER_MOM_MACHINE_TEMPLATE = "fldcoll01";//"co7_8gb";
    public static final String LOAD_MACHINE_TEMPLATE = "w64";
    public static final String HAMMOND_LOAD_MACHINE_TEMPLATE = "w64";//"w64_16gb";

    public static final String MEMORY_MONITOR_WEBAPP_ROLE_ID = "memoryMonitorRole";
    public static final String MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID = "memoryMonitorTomcatRole";
    public static final String MEMORY_MONITOR = "memory_monitor";

    public static final String DOCKER_VERSION = "99.99.sys-SNAPSHOT";
    public static final String IMAGE_REGISTRY = "oerth-scx.ca.com:4443";

    public static final String GC_LOG_FILE = "/opt/ca/apm/logs/gclog.txt";
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final int DEBUG_PORT = 4444;

    protected static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms" + XM_MOM_MEM + "m",
            "-Xmx" + XM_MOM_MEM + "m", "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
            "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    protected static final Collection<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-XX:CMSInitiatingOccupancyFraction=50", "-Xms" + XM_COL_MEM + "m", "-Xmx" + XM_COL_MEM + "m", "-verbose:gc",
            "-Xloggc:" + GC_LOG_FILE);

    protected static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
            "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Xdebug", "-agentlib:jdwp=transport=dt_socket,address=" + (DEBUG_PORT + 1) + ",server=y,suspend=n",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms" + XM_WV_MEM + "m", "-Xmx" + XM_WV_MEM + "m",
            "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError", "-verbose:gc",
            "-Xloggc:" + GC_LOG_FILE);

    public static int getRunDuration(TimeUnit unit) {
        return (int) unit.convert(1, TimeUnit.HOURS);
    }

    @Override
    public ITestbed create(ITasResolver resolver) {

        ITestbed testbed = new Testbed("ClusterRegressionTestBed");

        TestbedMachine dbDockerMachine = new TestbedMachine.Builder(DB_DOCKER_MACHINE_ID)
                .templateId(DOCKER_MACHINE_TEMPLATE)
                .platform(Platform.LINUX)
                .bitness(Bitness.b64)
                .build();
        testbed.addMachine(dbDockerMachine);
        DockerComposeRole dbDockerRole = new DockerComposeRole.DbBuilder(DB_DOCKER_ROLE_ID, resolver)
                .version(DOCKER_VERSION)
                .imageRegistry(IMAGE_REGISTRY)
                .build();
        dbDockerMachine.addRole(dbDockerRole);

        TestbedMachine momDockerMachine = new TestbedMachine.Builder(MOM_DOCKER_MACHINE_ID)
                .templateId(DOCKER_MOM_MACHINE_TEMPLATE)
                .platform(Platform.LINUX)
                .bitness(Bitness.b64)
                .build();
        DockerComposeRole wvDockerRole = new DockerComposeRole.WVBuilder(WV_DOCKER_ROLE_ID, resolver)
                .version(DOCKER_VERSION)
                .imageRegistry(IMAGE_REGISTRY)
                .javaOptions(WV_LAXNL_JAVA_OPTION)
                .installDir(TasBuilder.LINUX_SOFTWARE_LOC + "docker-compose-wv")
                .build();
        momDockerMachine.addRole(wvDockerRole);
        DockerComposeRole momDockerRole = new DockerComposeRole.MomBuilder(MOM_DOCKER_ROLE_ID, resolver)
                .version(DOCKER_VERSION)
                .imageRegistry(IMAGE_REGISTRY)
                .installDir(TasBuilder.LINUX_SOFTWARE_LOC + "docker-compose-mom")
                .javaOptions(MOM_LAXNL_JAVA_OPTION)
                .databaseHost(resolver.getHostnameById(DB_DOCKER_ROLE_ID))
                .addCollector(resolver.getHostnameById(COL1_DOCKER_ROLE_ID))
                .addCollector(resolver.getHostnameById(COL2_DOCKER_ROLE_ID))
                .addCollector(resolver.getHostnameById(COL3_DOCKER_ROLE_ID))
                .build();
        momDockerMachine.addRole(momDockerRole);
        momDockerMachine.addRemoteResource(RemoteResource.createFromLocation(momDockerRole.getInstallDir() + "/logs"));
        testbed.addMachine(momDockerMachine);

        for (int i = 0; i < 3; i++) {
            TestbedMachine colDockerMachine = new TestbedMachine.Builder(EM_C_DOCKER_MACHINES[i])
                    .templateId(DOCKER_MACHINE_TEMPLATE)
                    .platform(Platform.LINUX)
                    .bitness(Bitness.b64)
                    .build();
            DockerComposeRole colDockerRole = new DockerComposeRole.CollectorBuilder(EM_C_DOCKER_ROLES[i], resolver)
                    .version(DOCKER_VERSION)
                    .imageRegistry(IMAGE_REGISTRY)
                    .javaOptions(COLL_LAXNL_JAVA_OPTION)
                    .databaseHost(resolver.getHostnameById(DB_DOCKER_ROLE_ID))
                    .build();
            colDockerMachine.addRole(colDockerRole);
            colDockerMachine.addRemoteResource(RemoteResource.createFromLocation(colDockerRole.getInstallDir() + "/logs"));
            testbed.addMachine(colDockerMachine);
        }

        // set management modules on MOM
        Artifact mm = new WurlitzerMmArtifact().createArtifact().getArtifact();
        UniversalRole mmRole = new UniversalRole.Builder("mmConfigRol", resolver)
                .unpack(mm, momDockerRole.getInstallDir() + "/modules")
                .build();
        mmRole.before(momDockerRole);
        momDockerMachine.addRole(mmRole);

        // ///////////////////////////////////////////////////
        // wurlitzer machine
        // ///////////////////////////////////////////////////
        TestbedMachine loadMachine1 = new TestbedMachine.Builder(LOAD1_MACHINE_ID)
                .templateId(LOAD_MACHINE_TEMPLATE)
                .bitness(Bitness.b64)
                .build();
        testbed.addMachine(loadMachine1);

        WurlitzerRole wurlitzerRole =
                new WurlitzerRole.Builder(WURLITZER_ROLE_ID, resolver)
                        .wurlitzerMachine(resolver.getHostnameById(WURLITZER_ROLE_ID))
                        .targetMachine(resolver.getHostnameById(MOM_DOCKER_ROLE_ID))
                        .runDuration(getRunDuration(TimeUnit.MINUTES)).version(Version.SNAPSHOT_DEV_99_99)
                        .antScriptPathSegments("scripts", "xml", "appmap-stress", "load-test", "build.xml")
                        .antScriptArgs("20-agents-150-apps-15-backends-1-frontends")
                        .terminateOnMatch("Connected to MOM").build();

        loadMachine1.addRole(wurlitzerRole);

        // PREPARE DASHBOARD LOAD
        String host = resolver.getHostnameById(MOM_DOCKER_ROLE_ID);
        WebViewLoadRole webViewLoadRole =
                (new WebViewLoadRole.Builder(WEB_VIEW_LOAD_ROLE, resolver))
                        .webViewServerHost(host)
                        .webViewServerPort(8082)
                        .webViewCredentials("admin", "")
                        .workDir(TasBuilder.WIN_SOFTWARE_LOC + "webview-load")
                        .openWebViewUrl(
                                "http://" + host + ":8082/#console;db=Calc;dn=SuperDomain;mm=WurlitzerApp;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=Calc1;dn=SuperDomain;mm=WurlitzerApp1;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=Calc2;dn=SuperDomain;mm=WurlitzerApp2;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=Calc3;dn=SuperDomain;mm=WurlitzerApp3;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=Calc4;dn=SuperDomain;mm=WurlitzerApp4;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=Calc5;dn=SuperDomain;mm=WurlitzerApp5;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=Calc6;dn=SuperDomain;mm=WurlitzerApp6;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=Calc7;dn=SuperDomain;mm=WurlitzerApp7;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=Calc8;dn=SuperDomain;mm=WurlitzerApp8;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=Calc9;dn=SuperDomain;mm=WurlitzerApp9;tr=0")
                        .openWebViewUrl(
                                "http://" + host + ":8082/#console;db=EJB;dn=SuperDomain;mm=WurlitzerApp;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=EJB1;dn=SuperDomain;mm=WurlitzerApp1;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=EJB2;dn=SuperDomain;mm=WurlitzerApp2;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=EJB3;dn=SuperDomain;mm=WurlitzerApp3;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=EJB4;dn=SuperDomain;mm=WurlitzerApp4;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=EJB5;dn=SuperDomain;mm=WurlitzerApp5;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=EJB6;dn=SuperDomain;mm=WurlitzerApp6;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=EJB7;dn=SuperDomain;mm=WurlitzerApp7;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=EJB8;dn=SuperDomain;mm=WurlitzerApp8;tr=0")
                        .openWebViewUrl(
                                "http://" + host
                                        + ":8082/#console;db=EJB9;dn=SuperDomain;mm=WurlitzerApp9;tr=0").build();
        loadMachine1.addRole(webViewLoadRole);

        // ///////////////////////////////////////////////////
        // fake workstation + jmeter machine
        // ///////////////////////////////////////////////////
        FakeWorkstationRole fwRole = new FakeWorkstationRole.Builder(FAKE_WORKSTATION_ROLE_ID, resolver).user("Admin")
                .host(resolver.getHostnameById(MOM_DOCKER_ROLE_ID))
                .port(5001)
                .historicalQuery()
                .liveQuery()
                .resolution(15)
                .agent(".*DatabaseAgent_%2")
                .metric("EJB\\|.*:Average Response Time \\(ms\\)")
                .runDuration(getRunDuration(TimeUnit.SECONDS))
                .build();
        CautlRole fakeWorkstationCautlRole = new CautlRole.Builder(FAKE_WORKSTATION_CAUTL_ROLE_ID, resolver)
                .executedRole(fwRole)
                .build();

        TestbedMachine loadMachine2 = new TestbedMachine.Builder(LOAD2_MACHINE_ID)
                .templateId(LOAD_MACHINE_TEMPLATE)
                .bitness(Bitness.b64)
                .build();
        // JMeter
        String jMeterInstallDir = TasBuilder.WIN_SOFTWARE_LOC + "jmeter\\";

        UniversalRole jMeterScriptRole = new UniversalRole.Builder(JMETER_SCRIPT_ROLE_ID, resolver)
                .unpack(JmeterScriptsVersion.v10_3.getArtifact(), jMeterInstallDir)
                .build();

        Map<String, String> params = new HashMap<>();
        params.put("testDurationInSeconds", Integer.toString(getRunDuration(TimeUnit.SECONDS)));
        params.put("appServerHost", resolver.getHostnameById(MOM_DOCKER_ROLE_ID));
        params.put("csvFolder", "Generated Files");

        JMeterRole jmeterRole = new JMeterRole.Builder(JMETER_ROLE_ID, resolver)
                .deploySourcesLocation(jMeterInstallDir)
                .scriptFilePath(jMeterInstallDir + "fld-jmeter-scripts\\TeamCenterWithCode.jmx")
                .outputJtlFile(jMeterInstallDir + "perfLoad.jtl")
                .outputLogFile(jMeterInstallDir + "perfLoad.log")
                .params(params)
                .build();

        loadMachine2.addRole(fwRole, fakeWorkstationCautlRole, jmeterRole, jMeterScriptRole);
        testbed.addMachine(loadMachine2);

        // ///////////////////////////////////////////////////
        // Hammond
        // ///////////////////////////////////////////////////
        HammondRole hammond1Role = new HammondRole.Builder(HAMMOND1_ROLE_ID, resolver)
                .heapMemory("3072m")
                .scale(1)
                .collector(resolver.getHostnameById(MOM_DOCKER_ROLE_ID))
                .data(HammondDataVersion.ATT_em1)
                .runDuration(getRunDuration(TimeUnit.SECONDS))
                .build();
        TestbedMachine loadMachine3 = new TestbedMachine.Builder(LOAD3_MACHINE_ID)
                .templateId(HAMMOND_LOAD_MACHINE_TEMPLATE)
                .bitness(Bitness.b64)
                .build();
        loadMachine3.addRole(hammond1Role);
        testbed.addMachine(loadMachine3);
        HammondRole hammond2Role = new HammondRole.Builder(HAMMOND2_ROLE_ID, resolver)
                .heapMemory("3072m")
                .scale(1)
                .collector(resolver.getHostnameById(MOM_DOCKER_ROLE_ID))
                .data(HammondDataVersion.ATT_em1, HammondDataVersion.ATT_em2,
                        HammondDataVersion.ATT_em3)
                .runDuration(getRunDuration(TimeUnit.SECONDS))
                .build();
        TestbedMachine loadMachine4 = new TestbedMachine.Builder(LOAD4_MACHINE_ID)
                .templateId(HAMMOND_LOAD_MACHINE_TEMPLATE)
                .bitness(Bitness.b64)
                .build();
        loadMachine4.addRole(hammond2Role);
        testbed.addMachine(loadMachine4);
        HammondRole hammond3Role = new HammondRole.Builder(HAMMOND3_ROLE_ID, resolver)
                .heapMemory("3072m")
                .scale(1)
                .collector(resolver.getHostnameById(MOM_DOCKER_ROLE_ID))
                .data(HammondDataVersion.ATT_em3)
                .runDuration(getRunDuration(TimeUnit.SECONDS))
                .build();
        TestbedMachine loadMachine5 = new TestbedMachine.Builder(LOAD5_MACHINE_ID)
                .templateId(HAMMOND_LOAD_MACHINE_TEMPLATE)
                .bitness(Bitness.b64)
                .build();
        loadMachine5.addRole(hammond3Role);
        testbed.addMachine(loadMachine5);


        // ///////////////////////////////////////////////////
        // Memory monitor
        // ///////////////////////////////////////////////////

        ITasArtifact mmWebApp = new MemoryMonitorWebappArtifact(resolver).createArtifact();
        WebAppRole<TomcatRole> memoryMonitorWebappRole = new WebAppRole.Builder<TomcatRole>(MEMORY_MONITOR_WEBAPP_ROLE_ID)
                .artifact(mmWebApp).cargoDeploy()
                .contextName(MEMORY_MONITOR).build();

        TomcatRole tomcatRole = new TomcatRole.Builder(MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID, resolver)
                .additionalVMOptions(Arrays.asList(
                                "-Xms256m", "-Xmx512m", "-XX:PermSize=256m",
                                "-XX:MaxPermSize=512m", "-server"))
                .tomcatVersion(TomcatVersion.v80)
                .webApp(memoryMonitorWebappRole)
                .autoStart()
                .jdkHomeDir("C:\\Program Files\\Java\\jre7")
                .build();
        tomcatRole.before(memoryMonitorWebappRole);

        loadMachine1.addRole(memoryMonitorWebappRole, tomcatRole);

        String webappHost = resolver.getHostnameById(MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID);

        // EM machines
        for (String machineId : Arrays.asList(
                MOM_DOCKER_MACHINE_ID,
                COL1_DOCKER_MACHINE_ID,
                COL2_DOCKER_MACHINE_ID,
                COL3_DOCKER_MACHINE_ID)) {

            ITestbedMachine memoryMonitorMachine = testbed.getMachineById(machineId);

            String installDir = null;
            loop:
            for (IRole role : memoryMonitorMachine.getRoles()) {
                if (role instanceof DockerComposeRole) {
                    switch (role.getRoleId()) {
                        case MOM_DOCKER_ROLE_ID:
                        case COL1_DOCKER_ROLE_ID:
                        case COL2_DOCKER_ROLE_ID:
                        case COL3_DOCKER_ROLE_ID:
                            installDir = ((DockerComposeRole) role).getInstallDir();
                            break loop;
                        default:
                    }
                }
            }
            // memory monitoring (start/stop scripts)
            String memoryMonitorRoleId = "memoryMonitorRole_" + machineId;
            MemoryMonitorRole memoryMonitorRole = (new MemoryMonitorRole.LinuxBuilder(memoryMonitorRoleId, resolver))
                    .gcLogFile(installDir + "/logs/gclog.txt")
                    .memoryMonitorGroup("PERF")
                    .memoryMonitorRoleName(machineId)
                    .memoryMonitorWebappHost(webappHost)
                    .memoryMonitorWebappPort(8080)
                    .memoryMonitorWebappContextRoot(MEMORY_MONITOR)
                    .build();
            memoryMonitorMachine.addRole(memoryMonitorRole);
        }

        return testbed;
    }
}
