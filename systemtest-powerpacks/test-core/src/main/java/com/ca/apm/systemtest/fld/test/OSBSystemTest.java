package com.ca.apm.systemtest.fld.test;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.flow.ConfigureOrclSrvBusAgentFlowContext;
import com.ca.apm.systemtest.fld.role.OSBRole;
import com.ca.apm.systemtest.fld.role.PPWLSStockTraderRole;
import com.ca.apm.systemtest.fld.test.generic.PPBaseSystemTest;
import com.ca.apm.systemtest.fld.testbed.STOracleSBTestbed;
import com.ca.apm.systemtest.fld.testbed.machines.JMeterLoadMachine;
import com.ca.apm.systemtest.fld.testbed.machines.WLSTradeTestbed;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlowContext;
import com.ca.apm.tests.role.JMeterRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * @Author rsssa02
 */
public class OSBSystemTest extends PPBaseSystemTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OSBSystemTest.class);

    public ITasResolver tasResolver;
    public static final String containerPath =
        "|OSB|BusinessServices|.*Business Service 1.*:Responses Per Interval";

    public int minutes = 6;
    public int expValue = 6000;

    public String appserverHostname;
    public String agentName;
    public String processName;

    OSBSystemTest() throws Exception {
        super();
    }

    @SuppressWarnings("static-access")
    @Tas(testBeds = @TestBed(name = STOracleSBTestbed.class, executeOn = STOracleSBTestbed.appServerMachine), 
        owner = "bocto01", size = SizeType.DEBUG, exclusivity = ExclusivityType.NONEXCLUSIVE)
    @Test(groups = {"osb"})
    public void testDeployment() throws Exception {
        try {// cleanup
            initTestVariables();
            tearDownServers();

            // starting the OSB Server
            startAllServices();

            startTime = System.currentTimeMillis();
            // generate the needed load
            runJmeterScript();

            runJMXMonitoring(runTime);

            startTypePerfMonitor(STOracleSBTestbed.TYPE_PERF_ROLE_ID,
                STOracleSBTestbed.appServerMachine);

            validateMetricsMin(minutes, expValue);
            if (error != null) throw error;

            // teardown the test
            testTeardown(activeThreads);

            // generate and send report email
            Collection<String> agentErrors = sendResultEmail();

            // Check logs for errors before completing the test
            Assert.assertTrue(agentErrors == null || agentErrors.isEmpty(),
                "Errors found in Agent log: " + agentErrors);
        } catch (Exception e) {
            Assert.fail("Exception during test execution !", e);
        }
    }

    @SuppressWarnings("static-access")
    private void initTestVariables() throws Exception {
        appserverHostname =
            envProperties.getMachinePropertyById(STOracleSBTestbed.appServerMachine,
                "machine.appserver.hostname");
        agentName =
            envProperties.getMachinePropertyById(STOracleSBTestbed.appServerMachine,
                "machine.appserver.agent.name");
        processName =
            envProperties.getMachinePropertyById(STOracleSBTestbed.appServerMachine,
                "machine.appserver.process.name");
        containerName = "Oracle ServiceBus";
        emHost =
            envProperties.getMachinePropertyById(STOracleSBTestbed.loadMachine,
                "machine.em.hostname");
        metric =
            "*SuperDomain*|" + appserverHostname + "|" + processName + "|" + agentName
                + containerPath;
        jmxMetrics =
            "java.lang:type=Memory|HeapMemoryUsage/used,max;java.lang:type=GarbageCollector,name=Copy|CollectionCount|CollectionTime;java.lang:type=GarbageCollector,name=MarkSweepCompact|CollectionCount|CollectionTime";
        initCLWBean();
        initLogFiles();
    }

    @SuppressWarnings("static-access")
    private void runJmeterScript() {
        LOGGER.info("Starting jmeter thread");
        Date date = new Date();
        Format formatter = new SimpleDateFormat("YYYYMdd_hhmmss");
        JMeterRunFlowContext flowContext =
            (JMeterRunFlowContext) deserializeFlowContextFromRole(JMeterLoadMachine.JMETER_ROLE_ID,
                JMeterRole.RUN_JMETER, JMeterRunFlowContext.class);

        flowContext.setScriptFilePath(CLIENT_HOME + "/OSBfld_loadScript.jmx");
        flowContext.setOutputLogFile(jmeterOutDir + "jmeter_output_" + formatter.format(date)
            + ".log");
        flowContext.setOutputJtlFile(jmeterOutDir + "jmeter_output_jtl_" + formatter.format(date)
            + ".jtl");
        flowContext.getParams().put("appServerHost", appserverHostname);
        flowContext.getParams().put("testNumberOfCVUS", "147");
        flowContext.getParams().put("testDurationInSeconds", String.valueOf(getRunTimeInSeconds()));
        flowContext.getParams().put("logDir", flowContext.getOutputLogFile());
        flowContext.getParams().put("testWarmupInSeconds",
            String.valueOf(getRunTimeInSeconds() * 0.01));

        runJmeterScript(STOracleSBTestbed.loadMachine, flowContext);
        // runFlowByMachineId(STOracleSBTestbed.ORCL_MACHINE_ID, JMeterRunFlow.class, flowContext,
        // TimeUnit.SECONDS, SizeType.MAMMOTH.getTimeout());
    }

    @SuppressWarnings("static-access")
    private void tearDownServers() throws InterruptedException {
        // Stopping OSB
        RunCommandFlowContext osbFlowContext =
            deserializeCommandFlowFromRole(STOracleSBTestbed.OSB_ROLE_ID, OSBRole.EP_OSB_STOP);
        runCommandFlowByMachineId(STOracleSBTestbed.appServerMachine, osbFlowContext);

        // Stopping stocktrader WLS server.
        RunCommandFlowContext wlsFlowContext =
            deserializeCommandFlowFromRole(WLSTradeTestbed.STOCKTRADE_ROLE_ID,
                PPWLSStockTraderRole.EP_WEBAPP_STOP);
        runCommandFlowByMachineId(STOracleSBTestbed.dbMachine, wlsFlowContext);
        Thread.sleep(STARTUP_SLEEP_MS);
    }

    @SuppressWarnings({"static-access", "unused"})
    private void startAllServices() throws Exception {
        startWlsWebappServer();
        String hostname = envProperties.getMachineHostnameByRoleId(STOracleSBTestbed.OSB_ROLE_ID);
        ConfigureOrclSrvBusAgentFlowContext agentContext =
            (ConfigureOrclSrvBusAgentFlowContext) deserializeFlowContextFromRole(
                STOracleSBTestbed.OSB_ROLE_ID, OSBRole.EP_AGENT_ARGS,
                ConfigureOrclSrvBusAgentFlowContext.class);

        // getting the javaagent arguments to start OSB with wily agent.
        String javaArgs = agentContext.getAgentJarPath();
        String profileAgrs = agentContext.getProfileFilePath();
        String domainPath = agentContext.getDomainDirRelativePath();
        String envFilename = agentContext.getEnvironmentFileRelativePath();
        String envFileBkp = agentContext.getEnvironmentFileBackupExtension();
        String javaAgentProfileArgs =
            String.format("-javaagent:%s -Dcom.wily.introscope.agentProfile=%s", javaArgs,
                profileAgrs);

        String jmxArgsStartup =
            "-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false "
                + "-Dcom.sun.management.jmxremote.authenticate=false -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote.port=1099";

        RunCommandFlowContext osbFlowContext =
            deserializeCommandFlowFromRole(STOracleSBTestbed.OSB_ROLE_ID, OSBRole.EP_OSB_START);

        osbFlowContext.getEnvironment().put("JAVA_OPTIONS",
            javaAgentProfileArgs + "\t" + jmxArgsStartup);
        osbFlowContext.getEnvironment().put("JAVA_VENDOR", "Sun");
        osbFlowContext.getEnvironment().put("USER_MEM_ARGS",
            "-XX:MaxPermSize=512m -Xms400m -Xmx400m");
        runCommandFlowByMachineId(STOracleSBTestbed.appServerMachine, osbFlowContext);
        LOGGER.info("Sleeping for " + (STARTUP_SLEEP_MS / 1000) * 3 + "s buffer time");
        Thread.sleep(STARTUP_SLEEP_MS * 3);
    }

    private void startWlsWebappServer() throws Exception {
        LOGGER.info("Starting the Trade application server ...");
        runSerializedCommandFlowFromRole(WLSTradeTestbed.STOCKTRADE_ROLE_ID,
            PPWLSStockTraderRole.EP_WEBAPP_START);
    }

}
