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

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.tests.artifact.*;
import com.ca.apm.tests.role.*;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.artifact.IBuiltArtifact.Version;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.HammondRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;

/**
 * ClusterRegression class
 * <p/>
 * TestBed description
 */
public abstract class ClusterRegressionTestBed implements ITestbedFactory {

    public static final String PERFMON_MOM_ROLE_ID = "perfmonMomRole";
    public static final String PERFMON_C1_ROLE_ID = "perfmonC1Role";
    public static final String PERFMON_C2_ROLE_ID = "perfmonC2Role";
    public static final String PERFMON_C3_ROLE_ID = "perfmonC3Role";
    public static final String PERFMON_C4_ROLE_ID = "perfmonC4Role";
    public static final String PERFMON_C5_ROLE_ID = "perfmonC5Role";
    public static final String[] PERFMON_C_ROLES = {PERFMON_C1_ROLE_ID, PERFMON_C2_ROLE_ID,
            PERFMON_C3_ROLE_ID, PERFMON_C4_ROLE_ID, PERFMON_C5_ROLE_ID};

    public static final String AGC_ROLE_ID = "agcRole";
    public static final String AGC_C_ROLE_ID = "agcCollectorRole";
    public static final String EM_WV_DB_ROLE_ID = "emWvDbRole";
    public static final String EM_MOM_ROLE_ID = "emMomRole";
    public static final String EM_C1_ROLE_ID = "emC1Role";
    public static final String EM_C2_ROLE_ID = "emC2Role";
    public static final String EM_C3_ROLE_ID = "emC3Role";
    public static final String EM_C4_ROLE_ID = "emC4Role";
    public static final String EM_C5_ROLE_ID = "emC5Role";
    public static final String[] EM_C_ROLES = {EM_C1_ROLE_ID, EM_C2_ROLE_ID, EM_C3_ROLE_ID,
            EM_C4_ROLE_ID, EM_C5_ROLE_ID};

    public static final String HAMMOND_ROLE_ID = "hammondRole";
    public static final String HAMMOND_AGC_ROLE_ID = "hammondAgcRole";
    public static final String WURLITZER_ROLE_ID = "wurlitzerRole";
    public static final String FAKE_WORKSTATION_ROLE_ID = "fakeWorkstationRole";
    public static final String FAKE_WORKSTATION_CAUTL_ROLE_ID = "fakeWorkstationCautlRole";
    public static final String JMETER_ROLE_ID = "jmeterRole";
    public static final String JMETER_SCRIPT_ROLE_ID = "jmeterScriptRole";
    public static final String DASHBOARD_ROLE_ID = "dashboardRole";
    public static final String AGC_MOM_REGISTER_ROLE_ID = "agcMOMRegisterRole";
    public static final String STOP_MOM_ROLE_ID = "stopMomRole";
    public static final String STOP_AGC_ROLE_ID = "stopAgcRole";
    public static final String STOP_AGC_C_ROLE_ID = "stopAgcCollectorRole";

    public static final String AGC_MACHINE_ID = "agcMachine";
    public static final String AGC_C_MACHINE_ID = "agcCollectorMachine";
    public static final String DB_MACHINE_ID = "dbMachine";
    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String C1_MACHINE_ID = "c1Machine";
    public static final String C2_MACHINE_ID = "c2Machine";
    public static final String C3_MACHINE_ID = "c3Machine";
    public static final String C4_MACHINE_ID = "c4Machine";
    public static final String C5_MACHINE_ID = "c5Machine";
    public static final String[] C_MACHINES = {C1_MACHINE_ID, C2_MACHINE_ID, C3_MACHINE_ID,
            C4_MACHINE_ID, C5_MACHINE_ID};

    public static final String[] MEMORY_MONITOR_MACHINES = {MOM_MACHINE_ID, C1_MACHINE_ID,
            C2_MACHINE_ID, C3_MACHINE_ID, C4_MACHINE_ID, C5_MACHINE_ID};

    public static final String LOAD1_MACHINE_ID = "load1Machine";
    public static final String LOAD2_MACHINE_ID = "load2Machine";
    public static final String LOAD3_MACHINE_ID = "load3Machine";
    public static final String LOAD4_MACHINE_ID = "load4Machine";
    public static final String[] LOAD_MACHINES = {LOAD1_MACHINE_ID, LOAD2_MACHINE_ID,
            LOAD3_MACHINE_ID, LOAD4_MACHINE_ID};

    public static final String JAVA_ROLE_ID = "javaRole";
    public static final String CSV_TO_XLS_ROLE_ID = "csvToXlsRole";

    public static final String CREATE_SHARE_DIR_ROLE_ID = "createShareDirRoleId";
    public static final String CREATE_SHARE_ROLE_ID = "createShareRoleId";

    public static final String WEB_VIEW_LOAD_ROLE = "webViewLoadRole";

    public static final String GC_LOG_FILE = TasBuilder.WIN_SOFTWARE_LOC + "em\\logs\\gclog.txt";

    public static final String ADMIN_AUX_TOKEN_HASHED =
            "8f400c257611ed5d30c0e6607ac61074307dfa24cf70a8e92c3e8147d67d2c70";
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    public static final String XM_MOM_MEM = "8g";
    public static final String XM_COL_MEM = "3g";
    public static final String XM_AGC_MEM = "3g";
    public static final String XM_WV_MEM = "4g";

    private static final int DEBUG_PORT = 4444;

    protected static final Collection<String> MOM_LAXNL_JAVA_OPTION_G1 = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Xss512k", "-Dcom.wily.assert=false", "-showversion", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200",
            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-XX:+HeapDumpOnOutOfMemoryError", "-Xms" + getMem(XM_MOM_MEM),
            "-Xmx" + getMem(XM_MOM_MEM), "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
            "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    protected static final Collection<String> COLL_LAXNL_JAVA_OPTION_G1 = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Xss512k", "-Dcom.wily.assert=false", "-showversion", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200",
            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-Xms" + getMem(XM_COL_MEM), "-Xmx" + getMem(XM_COL_MEM), "-verbose:gc",
            "-Xloggc:" + GC_LOG_FILE);

    private static final Collection<String> AGC_LAXNL_JAVA_OPTION_G1 = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Xss512k", "-Dcom.wily.assert=false", "-showversion", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200",
//            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-XX:+HeapDumpOnOutOfMemoryError", "-Xms" + getMem(XM_AGC_MEM), "-Xmx" + getMem(XM_AGC_MEM),
            "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
            "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    protected static final Collection<String> WV_LAXNL_JAVA_OPTION_G1 = Arrays.asList(
            "-Djava.awt.headless=true",
            "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
            "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Xdebug", "-agentlib:jdwp=transport=dt_socket,address=" + DEBUG_PORT + ",server=y,suspend=n",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms" + getMem(XM_WV_MEM), "-Xmx" + getMem(XM_WV_MEM),
            "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError", "-verbose:gc",
            "-Xloggc:" + GC_LOG_FILE);


    protected static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms" + getMem(XM_MOM_MEM),
            "-Xmx" + getMem(XM_MOM_MEM), "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
            "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    protected static final Collection<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-XX:CMSInitiatingOccupancyFraction=50", "-Xms" + getMem(XM_COL_MEM), "-Xmx" + getMem(XM_COL_MEM), "-verbose:gc",
            "-Xloggc:" + GC_LOG_FILE);

    private static final Collection<String> AGC_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
            "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
//            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms" + getMem(XM_AGC_MEM), "-Xmx" + getMem(XM_AGC_MEM),
            "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
            "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    protected static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
            "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Xdebug", "-agentlib:jdwp=transport=dt_socket,address=" + DEBUG_PORT + ",server=y,suspend=n",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms" + getMem(XM_WV_MEM), "-Xmx" + getMem(XM_WV_MEM),
            "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError", "-verbose:gc",
            "-Xloggc:" + GC_LOG_FILE);

    public static final String EM_PERF_LOAD_1 = "w64_16gb";
    public static final String[] EM_PERF_COLLECTORS = {"w64p_16gb", "w64p_16gb", "w64p_16gb", "w64p_16gb", "w64p_16gb"};
    public static final String EM_PERF_MOM = "w64p_16gb";
    public static final String EM_PERF_EMDB = "w64p_16gb";
    public static final String EM_PERF_AGC = "w64_16gb";
    public static final String EM_PERF_AGC_C = "w64";
    public static final String EM_PERF_LOAD_2 = "w64_16gb";
    public static final String EM_PERF_LOAD_3 = "w64_16gb";


    public static final String MEMORY_MONITOR_WEBAPP_ROLE_ID = "memoryMonitorRole";
    public static final String MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID = "memoryMonitorTomcatRole";
    public static final String MEMORY_MONITOR = "memory_monitor";

    private IRole addStartEmRole(ITestbedMachine machine, EmRole emRole, boolean startWV,
                                 boolean startEM, IRole...beforeRole) {
        //starts EM or WebView
        ExecutionRole.Builder builder =
                new ExecutionRole.Builder(emRole.getRoleId() + "_start");

        if (startEM) {
            builder.syncCommand(emRole.getEmRunCommandFlowContext());
        }
        if (startWV) {
            builder.syncCommand(emRole.getWvRunCommandFlowContext());
        }
        ExecutionRole startRole = builder.build();
        emRole.before(startRole);

        if (beforeRole != null) {
            startRole.after(Arrays.asList(beforeRole));
        }

        machine.addRole(startRole);
        return startRole;
    }
    private void addStopEmRole(ITestbedMachine machine, EmRole emRole, boolean stopWV,
                                 boolean stopEM, IRole...beforeRole) {
        //stop EM or WebView
        ExecutionRole.Builder builder =
                new ExecutionRole.Builder(emRole.getRoleId() + "_stop");

        if (stopWV) {
            builder.syncCommand(emRole.getWvStopCommandFlowContext());
        }
        if (stopEM) {
            builder.syncCommand(emRole.getEmStopCommandFlowContext());
        }
        ExecutionRole stopRole = builder.build();
        emRole.before(stopRole);

        if (beforeRole != null) {
            stopRole.after(Arrays.asList(beforeRole));
        }

        // delete log files
        String perflog = emRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR) + "\\logs\\perflog.txt";
        RunCommandFlowContext deletePerflogFlowContext =
                new RunCommandFlowContext.Builder("if")
                        .args(Arrays.asList(
                                "exist", perflog, "del", perflog))
                        .doNotPrependWorkingDirectory()
                        .build();
        ExecutionRole deletePerflogMomRole =
                new ExecutionRole.Builder(emRole.getRoleId() + "_deletePerflog").syncCommand(
                        deletePerflogFlowContext).build();

        String gclog = emRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR) + "\\logs\\gclog.txt";
        RunCommandFlowContext deleteGclogFlowContext =
                new RunCommandFlowContext.Builder("if")
                        .args(Arrays.asList(
                                "exist", gclog, "del", gclog
                        ))
                        .doNotPrependWorkingDirectory()
                        .build();
        ExecutionRole deleteGclogMomRole =
                new ExecutionRole.Builder(emRole.getRoleId() + "_deleteGclog").syncCommand(
                        deleteGclogFlowContext).build();

        deletePerflogMomRole.after(stopRole);
        deleteGclogMomRole.after(stopRole);

        // wait for EM shutdown
        RunCommandFlowContext sleepFlowContext =
                new RunCommandFlowContext.Builder("ping").args(
                        Arrays.asList("127.0.0.1", "-n", "60", ">", "nul")).build();
        ExecutionRole sleepRole =
                new ExecutionRole.Builder(emRole.getRoleId() + "_sleep").syncCommand(
                        sleepFlowContext).build();
        sleepRole.after(stopRole);
        sleepRole.before(deleteGclogMomRole, deletePerflogMomRole);

        machine.addRole(stopRole, sleepRole, deletePerflogMomRole, deleteGclogMomRole);
    }

    private void addTimeSyncRole(ITestbedMachine machine) {
        RunCommandFlowContext timeSyncFlowContext =  new RunCommandFlowContext.Builder("cmd")
                .args(Arrays.asList(
                        "/C",
                        "net start w32time & " +
                        "w32tm /config /manualpeerlist:isltime02.ca.com & " +
                        "net stop w32time & " +
                        "net start w32time & " +
                        "w32tm /config /update & " +
                        "w32tm /resync /force"))
                .doNotPrependWorkingDirectory()
                .dontUseWindowsShell()
                .build();
        ExecutionRole timeSyncRole =
                new ExecutionRole.Builder("timesync_" + machine.getMachineId()).syncCommand(
                        timeSyncFlowContext).build();
        timeSyncRole.before(Arrays.asList(machine.getRoles()));
        machine.addRole(timeSyncRole);
    }

    public abstract IArtifactVersion getEmVersion();

    public static String getMem(String mem) {
        return mem;
//        return "2g";
    }

    public static String getTemplate(String machine) {
        return machine;
//        return "w64";
    }

    public static int getRunDuration(TimeUnit unit) {
        return (int) unit.convert(2, TimeUnit.DAYS);
//        return (int) unit.convert(30, TimeUnit.MINUTES);
    }

    @Override
    public ITestbed create(ITasResolver resolver) {

        ITestbed testbed = new Testbed("ClusterRegressionTestBed");

        TestbedMachine loadMachine1 =
                new TestbedMachine.Builder(LOAD1_MACHINE_ID).templateId(getTemplate(EM_PERF_LOAD_1))
                        .bitness(Bitness.b64).build();
        testbed.addMachine(loadMachine1);

        // CREATE SHARE
        RunCommandFlowContext createShareDirFlowContext =
                new RunCommandFlowContext.Builder("if").args(
                        Arrays.asList("not", "exist", "c:\\share", "mkdir", "c:\\share")).build();
        ExecutionRole createShareDirRole =
                new ExecutionRole.Builder(CREATE_SHARE_DIR_ROLE_ID).syncCommand(
                        createShareDirFlowContext).build();

        RunCommandFlowContext createShareFlowContext =
                new RunCommandFlowContext.Builder("if").args(
                        Arrays.asList("not", "exist", "\\\\localhost\\share", "net", "share",
                                "share=c:\\share", "/GRANT:Everyone,FULL")).build();
        ExecutionRole createShareRole =
                new ExecutionRole.Builder(CREATE_SHARE_ROLE_ID)
                        .syncCommand(createShareFlowContext)
                        .build();

        createShareRole.after(createShareDirRole);
        loadMachine1.addRole(createShareDirRole, createShareRole);

        EmRole.Builder momBuilder = new EmRole.Builder(EM_MOM_ROLE_ID, resolver);
        String shareHost = resolver.getHostnameById(CREATE_SHARE_ROLE_ID);
        String shareFolder = "share";

        for (int i = 0; i < EM_C_ROLES.length; i++) {
            ITestbedMachine collectorMachine =
                    new TestbedMachine.Builder(C_MACHINES[i])
                            .templateId(getTemplate(EM_PERF_COLLECTORS[i]))
                            .bitness(Bitness.b64)
                            .build();
            EmRole collectorRole =
                    new EmRole.Builder(EM_C_ROLES[i], resolver)
                            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager"))
                            .dbhost(resolver.getHostnameById(EM_WV_DB_ROLE_ID))
                            .emClusterRole(EmRoleEnum.COLLECTOR)
                            .nostartEM()
                            .nostartWV()
                            .version(getEmVersion())
                            .ignoreStopCommandErrors()
                            .ignoreUninstallCommandErrors()
                            .emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION)
                            .configProperty("transport.buffer.input.maxNum", "2400")
                            .configProperty("transport.outgoingMessageQueueSize", "6000")
                            .configProperty("transport.override.isengard.high.concurrency.pool.min.size", "10")
                            .configProperty("transport.override.isengard.high.concurrency.pool.max.size", "10")
                            .configProperty("introscope.enterprisemanager.transactiontrace.arrivalbuffer.capacity", "5000")
                            .build();

            Map<String, String> mapping = new HashMap<String, String>();
            mapping.put("performance*.csv", String.format("collector%02d_pm.csv", 1 + i));
            mapping.put(collectorRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR)
                    + "\\logs\\perflog.txt", String.format("collector%02d_em.csv", i + 1));
            mapping.put(collectorRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR)
                    + "\\logs\\gclog.txt", String.format("collector%02d_gclog.txt", i + 1));
            mapping.put(TasBuilder.WIN_SOFTWARE_LOC + "gc.png", String.format("collector%02d_mem.png", i + 1));
            mapping.put(TasBuilder.WIN_SOFTWARE_LOC + "mm.csv", String.format("collector%02d_mm.csv", i + 1));
            List<String> command = Arrays.asList(
                    "java -Duser=admin -jar " +
                    collectorRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR) + "\\lib\\CLWorkstation.jar " +
                    "get historical data from agents matching \".*Virtual.*\" " +
                    "and metrics matching \".*In Use Post GC.*|.*SmartStor Disk Usage.*\" " +
                    "for past " + getRunDuration(TimeUnit.MINUTES) + 20 + " minutes with frequency of 15 sec " +
                    ">" + TasBuilder.WIN_SOFTWARE_LOC + "mm.csv");
            PerfMonitorRole perfmonRole = new PerfMonitorRole.Builder(PERFMON_C_ROLES[i], resolver)
                    .sharedFolder(shareHost, shareFolder)
                    .perfLogFileMapping(mapping)
                    .sampleInterval("15")
                    .perfLogPrepareCommands(command)
                    .build();

            collectorMachine.addRole(collectorRole, perfmonRole);
            momBuilder.emCollector(collectorRole);
            addTimeSyncRole(collectorMachine);
            testbed.addMachine(collectorMachine);
        }

        EmRole momRole = momBuilder
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager"))
                .emClusterRole(EmRoleEnum.MANAGER)
                .nostartEM()
                .nostartWV()
                .dbhost(resolver.getHostnameById(EM_WV_DB_ROLE_ID))
                .version(getEmVersion())
                .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION)
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .configProperty("transport.buffer.input.maxNum", "2400")
                .configProperty("transport.outgoingMessageQueueSize", "6000")
                .configProperty("transport.override.isengard.high.concurrency.pool.min.size", "10")
                .configProperty("transport.override.isengard.high.concurrency.pool.max.size", "10")
                .configProperty("introscope.enterprisemanager.transactiontrace.arrivalbuffer.capacity", "5000")
                .configProperty("log4j.logger.Manager.Action", "WARN,console,logfile")
                .build();

        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("performance*.csv", "mom01_pm.csv");
        String momInstallDir = momRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR);
        mapping.put(momInstallDir + "\\logs\\perflog.txt", "mom01_em.csv");
        mapping.put(momInstallDir + "\\logs\\gclog.txt", "mom01_gclog.txt");
        mapping.put(TasBuilder.WIN_SOFTWARE_LOC + "gc.png", "mom01_mem.png");
        mapping.put(TasBuilder.WIN_SOFTWARE_LOC + "mm.csv", "mom01_mm.csv");
        List<String> command = Arrays.asList(
                "java -Duser=admin -jar " +
                        momRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR) + "\\lib\\CLWorkstation.jar " +
                        "get historical data from agents matching \".*Virtual.$\" " +
                        "and metrics matching \".*In Use Post GC.*|.*SmartStor Disk Usage.*\" " +
                        "for past " + (getRunDuration(TimeUnit.MINUTES) + 20) + " minutes with frequency of 15 sec " +
                        ">" + TasBuilder.WIN_SOFTWARE_LOC + "mm.csv");
        PerfMonitorRole perfmonMomRole = new PerfMonitorRole.Builder(PERFMON_MOM_ROLE_ID, resolver)
                .sharedFolder(shareHost, shareFolder)
                .sharePassword("interOP@123")
                .perfLogFileMapping(mapping)
                .sampleInterval("15")
                .perfLogPrepareCommands(command)
                .build();

        // set management modules on MOM
        Artifact mm = new WurlitzerMmArtifact().createArtifact().getArtifact();
        UniversalRole mmRole = new UniversalRole.Builder("mmConfigRol", resolver)
                .unpack(mm, momInstallDir + "\\config\\modules")
                .build();
        mmRole.after(momRole);

        ITestbedMachine momMachine = new TestbedMachine.Builder(MOM_MACHINE_ID)
                .templateId(getTemplate(EM_PERF_MOM))
                .bitness(Bitness.b64)
                .build();
        momMachine.addRole(momRole, mmRole, perfmonMomRole);
        addTimeSyncRole(momMachine);
        testbed.addMachine(momMachine);
        IRole startMomRole = addStartEmRole(momMachine, momRole, false, true);

        ITestbedMachine webviewMachine = new TestbedMachine.Builder(DB_MACHINE_ID)
                .templateId(getTemplate(EM_PERF_EMDB))
                .bitness(Bitness.b64)
                .build();
        EmRole webviewRole = new EmRole.Builder(EM_WV_DB_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Arrays.asList("WebView", "Database"))
                .wvEmHost(resolver.getHostnameById(EM_MOM_ROLE_ID))
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .version(getEmVersion())
                .nostartEM()
                .nostartWV()
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .build();
        webviewRole.before(momRole);
        webviewMachine.addRole(webviewRole);
        addTimeSyncRole(webviewMachine);
        testbed.addMachine(webviewMachine);
        IRole startWebviewRole = addStartEmRole(webviewMachine, webviewRole, true, false, startMomRole);

        // AGC
        ITestbedMachine agcCollectorMachine = new TestbedMachine.Builder(AGC_C_MACHINE_ID)
                .templateId(getTemplate(EM_PERF_AGC_C))
                .bitness(Bitness.b64)
                .build();
        ITestbedMachine agcMachine = new TestbedMachine.LinuxBuilder(AGC_MACHINE_ID)
                .templateId(getTemplate(EM_PERF_AGC))
                .bitness(Bitness.b64)
                .build();
        testbed.addMachine(agcMachine, agcCollectorMachine);

        EmRole agcCollectorRole = new EmRole.Builder(AGC_C_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA"))
                .dbhost(resolver.getHostnameById(EM_WV_DB_ROLE_ID))
                .emClusterRole(EmRoleEnum.COLLECTOR)
                .nostartEM()
                .nostartWV()
                .version(getEmVersion())
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION)
                .build();
        agcCollectorMachine.addRole(agcCollectorRole);
        IRole startAgcCollectorRole = addStartEmRole(agcCollectorMachine, agcCollectorRole, false, true);

        //AGC machine
        EmRole agcRole = new EmRole.Builder(AGC_ROLE_ID, resolver)
                .emCollector(agcCollectorRole)
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA", "Database", "WebView"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .nostartEM()
                .nostartWV()
                .version(getEmVersion())
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .configProperty("introscope.apmserver.teamcenter.master", "true")
                .configProperty("introscope.enterprisemanager.appmap.model.edgesclamp", "20000")
                .emLaxNlClearJavaOption(AGC_LAXNL_JAVA_OPTION)
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .build();
        agcMachine.addRole(agcRole);
        IRole startAgcRole = addStartEmRole(agcMachine, agcRole, true, true);

        //register MOM to AGC
        String agcHost = resolver.getHostnameById(AGC_ROLE_ID);

        AGCRegisterRole agcRegister = new AGCRegisterRole.Builder(AGC_MOM_REGISTER_ROLE_ID, resolver)
                .agcHostName(agcHost)
                .hostName(resolver.getHostnameById(EM_MOM_ROLE_ID))
                .wvHostName(resolver.getHostnameById(EM_WV_DB_ROLE_ID))
                .startCommandContext(momRole.getEmRunCommandFlowContext())
                .stopCommandContext(momRole.getEmStopCommandFlowContext())
                .build();

        addTimeSyncRole(agcCollectorMachine);
        addTimeSyncRole(agcMachine);

        agcRegister.after(startMomRole, startWebviewRole, startAgcRole, startAgcCollectorRole);
        agcRegister.after(new HashSet<IRole>(Arrays.asList(testbed.getMachineById(DB_MACHINE_ID).getRoles())));
        agcMachine.addRole(agcRegister);

        addStopEmRole(momMachine, momRole, false, true, agcRegister);
        addStopEmRole(agcMachine, agcRole, true, true, agcRegister);
        addStopEmRole(agcCollectorMachine, agcCollectorRole, false, true, agcRegister);

        // ///////////////////////////////////////////////////
        // wurlitzer machine
        // ///////////////////////////////////////////////////
        WurlitzerRole wurlitzerRole =
                new WurlitzerRole.Builder(WURLITZER_ROLE_ID, resolver)
                        .wurlitzerMachine(resolver.getHostnameById(WURLITZER_ROLE_ID))
                        .targetMachine(resolver.getHostnameById(EM_MOM_ROLE_ID))
                        .runDuration(getRunDuration(TimeUnit.MINUTES)).version(Version.SNAPSHOT_DEV_99_99)
                        .antScriptPathSegments("scripts", "xml", "appmap-stress", "load-test", "build.xml")
                        .antScriptArgs("20-agents-150-apps-15-backends-1-frontends")
                        .terminateOnMatch("Connected to MOM").build();

        loadMachine1.addRole(wurlitzerRole);

        String csvToXlsInstallPath = TasBuilder.WIN_SOFTWARE_LOC + "csvToXls";
        CsvToXlsTemplateRole csvToXlsTemplateRole =
                new CsvToXlsTemplateRole.Builder("csvToXlsTemplateRoleId", resolver)
                        .installPath(csvToXlsInstallPath).version(CsvToXlsTemplateVersion.EM_PERFORMANCE_2)
                        .build();

        // DEPLOY CSV2XLS
        Map<String, String> sheetsMapping = new HashMap<String, String>();
        sheetsMapping.put("mom01_em.csv", "mom_em");
        sheetsMapping.put("mom01_pm.csv", "mom_pm");
        sheetsMapping.put("mom01_mm.csv", "mom_mm");
        sheetsMapping.put("collector01_em.csv", "c1_em");
        sheetsMapping.put("collector01_pm.csv", "c1_pm");
        sheetsMapping.put("collector01_mm.csv", "c1_mm");
        sheetsMapping.put("collector02_em.csv", "c2_em");
        sheetsMapping.put("collector02_pm.csv", "c2_pm");
        sheetsMapping.put("collector02_mm.csv", "c2_mm");
        sheetsMapping.put("collector03_em.csv", "c3_em");
        sheetsMapping.put("collector03_pm.csv", "c3_pm");
        sheetsMapping.put("collector03_mm.csv", "c3_mm");
        sheetsMapping.put("collector04_em.csv", "c4_em");
        sheetsMapping.put("collector04_pm.csv", "c4_pm");
        sheetsMapping.put("collector04_mm.csv", "c4_mm");
        sheetsMapping.put("collector05_em.csv", "c5_em");
        sheetsMapping.put("collector05_pm.csv", "c5_pm");
        sheetsMapping.put("collector05_mm.csv", "c5_mm");

        String xlsOutputFileName = String.format("result_%s_%s.xls",
                new SimpleDateFormat("yyyyMMdd_hhmm").format(new Date()),
                getEmVersion().getValue());
        CsvToXlsRole csvToXlsRole = new CsvToXlsRole.Builder(CSV_TO_XLS_ROLE_ID, resolver)
                .shareDir("c:\\share")
                .installPath(csvToXlsInstallPath)
                .templateFileName(csvToXlsTemplateRole.getTemplateFilePath())
                .outputFileName("c:\\" + xlsOutputFileName)
                .sheetsMapping(sheetsMapping).build();

        loadMachine1.addRole(csvToXlsTemplateRole, csvToXlsRole);

        // PREPARE DASHBOARD LOAD
        String host = resolver.getHostnameById(EM_WV_DB_ROLE_ID);
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

        // AGC hammond
        HammondRole hammondAgcRole =
                new HammondRole.Builder(HAMMOND_AGC_ROLE_ID, resolver)
                        .heapMemory("4g")
                        .scale(50)
                        .collector(resolver.getHostnameById(AGC_C_ROLE_ID))
                        .data(HammondDataVersion.FLD_mainframe).runDuration(getRunDuration(TimeUnit.SECONDS)).build();
        loadMachine1.addRole(hammondAgcRole);
        addTimeSyncRole(loadMachine1);

        // resources
        loadMachine1.addRemoteResource(RemoteResource.createFromName("", "c:\\" + xlsOutputFileName));
        loadMachine1.addRemoteResource(RemoteResource.createFromLocation("c:\\share"));

        // ///////////////////////////////////////////////////
        // fake workstation + jmeter machine
        // ///////////////////////////////////////////////////
        FakeWorkstationRole fwRole = new FakeWorkstationRole.Builder(FAKE_WORKSTATION_ROLE_ID, resolver).user("Admin")
                .host(resolver.getHostnameById(EM_MOM_ROLE_ID))
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
                .templateId(getTemplate(EM_PERF_LOAD_2))
                .bitness(Bitness.b64)
                .build();
        // JMeter
        String jMeterInstallDir = TasBuilder.WIN_SOFTWARE_LOC + "jmeter\\";

        UniversalRole jMeterScriptRole = new UniversalRole.Builder(JMETER_SCRIPT_ROLE_ID, resolver)
                .unpack(JmeterScriptsVersion.v10_3.getArtifact(), jMeterInstallDir)
                .build();

        Map<String, String> params = new HashMap<>();
        params.put("testDurationInSeconds", Integer.toString(getRunDuration(TimeUnit.SECONDS)));
        params.put("appServerHost", resolver.getHostnameById(EM_MOM_ROLE_ID));
        params.put("csvFolder", "Generated Files");

        JMeterRole jmeterRole = new JMeterRole.Builder(JMETER_ROLE_ID, resolver)
                .deploySourcesLocation(jMeterInstallDir)
                .scriptFilePath(jMeterInstallDir + "fld-jmeter-scripts\\TeamCenterWithCode.jmx")
                .outputJtlFile(jMeterInstallDir + "perfLoad.jtl")
                .outputLogFile(jMeterInstallDir + "perfLoad.log")
                .params(params)
                .build();

        loadMachine2.addRole(fwRole, fakeWorkstationCautlRole, jmeterRole, jMeterScriptRole);
        addTimeSyncRole(loadMachine2);
        testbed.addMachine(loadMachine2);

        // ///////////////////////////////////////////////////
        // Hammond
        // ///////////////////////////////////////////////////
        HammondRole hammondRole = new HammondRole.Builder(HAMMOND_ROLE_ID, resolver)
                .heapMemory("4608m")
                .scale(3)
                .collector(resolver.getHostnameById(EM_MOM_ROLE_ID))
                .data(HammondDataVersion.ATT_em1, HammondDataVersion.ATT_em2,
                        HammondDataVersion.ATT_em3)
                .runDuration(getRunDuration(TimeUnit.SECONDS))
                .build();
        TestbedMachine loadMachine3 = new TestbedMachine.Builder(LOAD3_MACHINE_ID)
                .templateId(getTemplate(EM_PERF_LOAD_3))
                .bitness(Bitness.b64)
                .build();
        loadMachine3.addRole(hammondRole);
        addTimeSyncRole(loadMachine3);
        testbed.addMachine(loadMachine3);

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

        // EM machines
        for (String machineId : MEMORY_MONITOR_MACHINES) {

            try {
                ITestbedMachine memoryMonitorMachine = testbed.getMachineById(machineId);

                // memory monitoring (start/stop scripts)
                String memoryMonitorRoleId = "memoryMonitorRole_" + machineId;
                MemoryMonitorRole memoryMonitorRole = (new MemoryMonitorRole.Builder(memoryMonitorRoleId, resolver))
                        .gcLogFile(GC_LOG_FILE)
                        .memoryMonitorGroup("PERF")
                        .memoryMonitorRoleName(machineId)
                        .memoryMonitorWebappHost(webappHost)
                        .memoryMonitorWebappPort(8080)
                        .memoryMonitorWebappContextRoot(MEMORY_MONITOR)
                        .build();
                memoryMonitorMachine.addRole(memoryMonitorRole);
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return testbed;
    }
}
