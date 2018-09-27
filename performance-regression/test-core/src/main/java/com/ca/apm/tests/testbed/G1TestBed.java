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

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.apm.tests.artifact.*;
import com.ca.apm.tests.role.*;
import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.artifact.IBuiltArtifact.Version;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.HammondRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;
import org.eclipse.aether.artifact.Artifact;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.tests.artifact.HammondDataVersion.ATT_em1;
import static com.ca.apm.tests.artifact.HammondDataVersion.ATT_em2;
import static com.ca.apm.tests.artifact.HammondDataVersion.ATT_em3;

/**
 * G1 testbed class
 * <p/>
 * TestBed description
 */
@TestBedDefinition(cleanUpTestBed = G1TestBedCleaner.class)
public class G1TestBed implements ITestbedFactory {

    public static final String PERFMON_EM_ROLE_ID = "perfmonC1Role";
    public static final String EM_ROLE_ID = "emEMRole";
    public static final String HAMMOND_ROLE_ID = "hammondRole";
    public static final String WURLITZER_ROLE_ID = "wurlitzerRole";
    private static final String MEMORY_MONITOR_WEBAPP_ROLE_ID = "memoryMonitorRole";
    private static final String MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID = "memoryMonitorTomcatRole";
    private static final String MEMORY_MONITOR = "memory_monitor";
    public static final String WEB_VIEW_LOAD_ROLE = "webViewLoadRole";
    private static final String FAKE_WORKSTATION_ROLE_ID = "fakeWorkstationRole";
    public static final String FAKE_WORKSTATION_CAUTL_ROLE_ID = "fakeWorkstationCautlRole";

    public static final String EM_MACHINE_ID = "g1EmMachine";
    public static final String LOAD1_MACHINE_ID = "g1Load1Machine";
    private static final String LOAD2_MACHINE_ID = "g1Load2Machine";

    private static final String EM_PERF_TEMPLATE = "EmPerf_SPARE";
    private static final String EM_PERF_LOAD_1_TEMPLATE = "w64";
    private static final String EM_PERF_LOAD_2_TEMPLATE = "w64_16gb";

    public static final String GC_EM_LOG_FILE = TasBuilder.WIN_SOFTWARE_LOC + "em\\logs\\gclog.txt";
    public static final String GC_WV_LOG_FILE = TasBuilder.WIN_SOFTWARE_LOC + "em\\logs\\gclog_wv.txt";

    public static final String XM_COL_MEM = "6g";
    private static final String XM_WV_MEM = "4g";

    private static final Collection<String> EM_LAXNL_JAVA_OPTION_G1 = Arrays.asList(
            "-Djava.awt.headless=true",
            "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Xss512k",
            "-Dcom.wily.assert=false",
            "-showversion",
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=200",
            "-Dcom.sun.management.jmxremote",
            "-Dcom.sun.management.jmxremote.port=4444",
            "-Dcom.sun.management.jmxremote.authenticate=false",
            "-Dcom.sun.management.jmxremote.ssl=false",
            "-Xms" + getMem(XM_COL_MEM),
            "-Xmx" + getMem(XM_COL_MEM),
            "-verbose:gc",
            "-Xloggc:" + GC_EM_LOG_FILE);

    private static final Collection<String> WV_LAXNL_JAVA_OPTION_G1 = Arrays.asList(
            "-Djava.awt.headless=true",
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=200",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
            "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Xdebug",
            "-agentlib:jdwp=transport=dt_socket,address=4445,server=y,suspend=n",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily",
            "-Xms" + getMem(XM_WV_MEM),
            "-Xmx" + getMem(XM_WV_MEM),
            "-XX:+PrintGCDateStamps",
            "-XX:+HeapDumpOnOutOfMemoryError",
            "-verbose:gc",
            "-Xloggc:" + GC_WV_LOG_FILE);

    private IArtifactVersion getEmVersion() {
        return new IArtifactVersion() {
            @Override
            public String getValue() {
                return "99.99.sys-SNAPSHOT";
            }
        };
    }

    public static String getMem(String mem) {
        return mem;
//        return "2g";
    }

    private static String getTemplate(String machine) {
        return machine;
//        return "w64";
    }

    public static int getRunDuration(TimeUnit unit) {
        return (int) unit.convert(3, TimeUnit.HOURS);
    }

    @Override
    public ITestbed create(ITasResolver resolver) {

        final String memoryMonitorWorkDir = TasBuilder.WIN_SOFTWARE_LOC + "memory_monitor\\";

        ITestbed testbed = new Testbed("G1TestBed");

        EmRole.Builder emBuilder = new EmRole.Builder(EM_ROLE_ID, resolver);

        emBuilder.silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "Database", "WebView"))
                .emClusterRole(EmRoleEnum.COLLECTOR)
                .nostartEM()
                .nostartWV()
                .version(getEmVersion())
                .emLaxNlClearJavaOption(EM_LAXNL_JAVA_OPTION_G1)
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION_G1)
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors();
        EmRole emRole = emBuilder.build();

        long timestamp = System.currentTimeMillis() / 1000;

        Map<String, String> mapping = new HashMap<>();
        mapping.put("performance*.csv", timestamp + "_pm.csv");
        String emInstallDir = emRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR);
        mapping.put(emInstallDir + "\\logs\\perflog.txt", timestamp + "_em.csv");
        mapping.put(emInstallDir + "\\logs\\gclog.txt", timestamp + "_gclog.txt");
        mapping.put(memoryMonitorWorkDir + "gc.png", timestamp + "_mem.png");
        mapping.put(memoryMonitorWorkDir + "mm.csv", timestamp + "_mm.csv");
        List<String> command = Collections.singletonList(
                "java -Duser=admin -jar " +
                        emRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR) + "\\lib\\CLWorkstation.jar " +
                        "get historical data from agents matching \".*Virtual.$\" " +
                        "and metrics matching \".*In Use Post GC.*|.*SmartStor Disk Usage.*|.*EM CPU Used.*\" " +
                        "for past " + (getRunDuration(TimeUnit.MINUTES) + 20) + " minutes with frequency of 15 sec " +
                        ">" + memoryMonitorWorkDir + "mm.csv");
        PerfMonitorRole perfmonMomRole = new PerfMonitorRole.Builder(PERFMON_EM_ROLE_ID, resolver)
                .sharedFolder(null, "C:")
                .perfLogFileMapping(mapping)
                .sampleInterval("15")
                .perfLogPrepareCommands(command)
                .build();

        // set management modules on MOM
        Artifact mm = new WurlitzerMmArtifact().createArtifact().getArtifact();
        UniversalRole mmRole = new UniversalRole.Builder("mmConfigRol", resolver)
                .unpack(mm, emInstallDir + "\\config\\modules")
                .build();
        mmRole.after(emRole);

        ITestbedMachine emMachine = new TestbedMachine.Builder(EM_MACHINE_ID)
                .templateId(getTemplate(EM_PERF_TEMPLATE))
                .bitness(Bitness.b64)
                .platform(Platform.WINDOWS)
                .build();
        emMachine.addRole(emRole, mmRole, perfmonMomRole);
        testbed.addMachine(emMachine);

        // ///////////////////////////////////////////////////
        // wurlitzer machine
        // ///////////////////////////////////////////////////
        TestbedMachine loadMachine1 = new TestbedMachine.Builder(LOAD1_MACHINE_ID)
                .templateId(getTemplate(EM_PERF_LOAD_1_TEMPLATE))
                .bitness(Bitness.b64)
                .platform(Platform.WINDOWS)
                .build();
        testbed.addMachine(loadMachine1);

        WurlitzerRole wurlitzerRole =
                new WurlitzerRole.Builder(WURLITZER_ROLE_ID, resolver)
                        .wurlitzerMachine(resolver.getHostnameById(WURLITZER_ROLE_ID))
                        .targetMachine(resolver.getHostnameById(EM_ROLE_ID))
                        .runDuration(getRunDuration(TimeUnit.MINUTES)).version(Version.SNAPSHOT_DEV_99_99)
                        .antScriptPathSegments("scripts", "xml", "appmap-stress", "load-test", "build.xml")
                        .antScriptArgs("20-agents-150-apps-15-backends-1-frontends")
                        .terminateOnMatch("Connected to").build();

        loadMachine1.addRole(wurlitzerRole);

        // PREPARE DASHBOARD LOAD
        String host = resolver.getHostnameById(EM_ROLE_ID);
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
        // fake workstation
        // ///////////////////////////////////////////////////
        TestbedMachine loadMachine2 = new TestbedMachine.Builder(LOAD2_MACHINE_ID)
                .templateId(getTemplate(EM_PERF_LOAD_2_TEMPLATE))
                .bitness(Bitness.b64)
                .platform(Platform.WINDOWS)
                .build();
        testbed.addMachine(loadMachine2);

        FakeWorkstationRole fwRole = new FakeWorkstationRole.Builder(FAKE_WORKSTATION_ROLE_ID, resolver).user("Admin")
                .host(resolver.getHostnameById(EM_ROLE_ID))
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

        // ///////////////////////////////////////////////////
        // Hammond
        // ///////////////////////////////////////////////////
        HammondRole hammondRole = new HammondRole.Builder(HAMMOND_ROLE_ID, resolver)
                .heapMemory("2048m")
                .scale(1)
                .collector(resolver.getHostnameById(EM_ROLE_ID))
                .data(ATT_em1, ATT_em2, ATT_em3)
                .runDuration(getRunDuration(TimeUnit.SECONDS))
                .build();

        loadMachine2.addRole(fakeWorkstationCautlRole, hammondRole);

        // ///////////////////////////////////////////////////
        // Memory monitor
        // ///////////////////////////////////////////////////

        ITasArtifact mmWebApp = new MemoryMonitorWebappArtifact(resolver).createArtifact();
        WebAppRole<TomcatRole> memoryMonitorWebappRole = new WebAppRole.Builder<TomcatRole>(MEMORY_MONITOR_WEBAPP_ROLE_ID)
                .artifact(mmWebApp).cargoDeploy()
                .contextName(MEMORY_MONITOR).build();

        TomcatRole tomcatRole = new TomcatRole.Builder(MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID, resolver)
                .additionalVMOptions(
                        Arrays.asList("-Xms256m", "-Xmx512m", "-XX:PermSize=256m",
                                "-XX:MaxPermSize=512m", "-server"))
                .tomcatVersion(TomcatVersion.v80)
                .webApp(memoryMonitorWebappRole)
                .autoStart()
                .jdkHomeDir("C:\\Program Files\\Java\\jre7")
                .build();
        tomcatRole.before(memoryMonitorWebappRole);

        loadMachine1.addRole(memoryMonitorWebappRole, tomcatRole);

        String webappHost = resolver.getHostnameById(MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID);

        ITestbedMachine memoryMonitorMachine = testbed.getMachineById(EM_MACHINE_ID);

        // memory monitoring (start/stop scripts)
        String memoryMonitorRoleId = "memoryMonitorRole_" + EM_MACHINE_ID;
        MemoryMonitorRole memoryMonitorRole = (new MemoryMonitorRole.Builder(memoryMonitorRoleId, resolver))
                .gcLogFile(GC_EM_LOG_FILE)
                .workDir(memoryMonitorWorkDir)
                .memoryMonitorGroup("PERF")
                .memoryMonitorRoleName(EM_MACHINE_ID)
                .memoryMonitorWebappHost(webappHost)
                .memoryMonitorWebappPort(8080)
                .memoryMonitorWebappContextRoot(MEMORY_MONITOR)
                .build();
        memoryMonitorMachine.addRole(memoryMonitorRole);

        return testbed;
    }
}
