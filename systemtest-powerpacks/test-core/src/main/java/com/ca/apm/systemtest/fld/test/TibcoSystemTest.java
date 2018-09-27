package com.ca.apm.systemtest.fld.test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.role.TibcoTradeAppRole;
import com.ca.apm.systemtest.fld.test.generic.PPBaseSystemTest;
import com.ca.apm.systemtest.fld.testbed.STTibcoTradeTestbed;
import com.ca.apm.systemtest.fld.testbed.machines.WASTradeAppTestbed;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlowContext;
import com.ca.apm.tests.role.JMeterRole;
import com.ca.apm.tests.role.Websphere85Role;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * @Author rsssa02
 */
public class TibcoSystemTest extends PPBaseSystemTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TibcoSystemTest.class);

    public ITasResolver tasResolver;
    public static final String containerPath =
        "|Tibco|Processes|WSDL.*TradeWSServiceslogin.*:Responses Per Interval";

    public int minutes = 6;
    public int expValue = 300;

    public String appserverHostname;
    public String agentName;
    public String processName;
    public String javaPath;

    protected TibcoSystemTest() throws Exception {
        super();
    }

    @SuppressWarnings("static-access")
    @Tas(testBeds = @TestBed(name = STTibcoTradeTestbed.class, executeOn = STTibcoTradeTestbed.appServerMachine), owner = "bocto01", size = SizeType.DEBUG, exclusivity = ExclusivityType.NONEXCLUSIVE)
    @Test(groups = {"tibcobw"})
    public void testDeployment() throws Exception {
        try {// cleanup
            initTestVariables();
            LOGGER.info("printing check" + metric + this.emUser + this.appserverHostname);
            stopAllServers();
            Thread.sleep(60000);
            startTibTradeApp();
            Thread.sleep(80000);

            startTime = System.currentTimeMillis();
            runJmeterScript();

            startTypePerfMonitor(STTibcoTradeTestbed.TYPE_PERF_ROLE_ID,
                STTibcoTradeTestbed.appServerMachine);

            runJstatMonitoring(javaPath, "bwengine", "Tibco_Trade6");

            validateMetricsMin(minutes, expValue);
            if (error != null) throw error;
            // Thread.sleep(runTime/1000);
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
            envProperties.getMachinePropertyById(STTibcoTradeTestbed.appServerMachine,
                "machine.appserver.hostname");
        agentName =
            envProperties.getMachinePropertyById(STTibcoTradeTestbed.appServerMachine,
                "machine.appserver.agent.name");
        processName =
            envProperties.getMachinePropertyById(STTibcoTradeTestbed.appServerMachine,
                "machine.appserver.process.name");
        javaPath =
            envProperties.getMachinePropertyById(STTibcoTradeTestbed.appServerMachine,
                "server.java7.home.dir");
        containerName = "Tibco BW";
        emHost =
            envProperties.getMachinePropertyById(STTibcoTradeTestbed.loadMachine,
                "machine.em.hostname");
        metric =
            "*SuperDomain*|" + appserverHostname + "|" + processName + "|" + agentName
                + containerPath;
        initCLWBean();
        initLogFiles();
    }

    @SuppressWarnings("static-access")
    private void runJmeterScript() {
        LOGGER.info("Starting jmeter thread");
        Date date = new Date();
        Format formatter = new SimpleDateFormat("YYYYMdd_hhmmss");
        JMeterRunFlowContext flowContext =
            (JMeterRunFlowContext) deserializeFlowContextFromRole(
                STTibcoTradeTestbed.JMETER_ROLE_ID, JMeterRole.RUN_JMETER,
                JMeterRunFlowContext.class);

        flowContext.setScriptFilePath(CLIENT_HOME + "/Tibco_Load.jmx");
        flowContext.setOutputLogFile(jmeterOutDir + "jmeter_output_" + formatter.format(date)
            + ".log");
        flowContext.setOutputJtlFile(jmeterOutDir + "jmeter_output_jtl_" + formatter.format(date)
            + ".jtl");
        flowContext.getParams().put("appServerHost", appserverHostname);
        flowContext.getParams().put("testNumberOfCVUS", "25");
        flowContext.getParams().put("testDurationInSeconds", String.valueOf(getRunTimeInSeconds()));
        flowContext.getParams().put("logDir", flowContext.getOutputLogFile());
        flowContext.getParams().put("testWarmupInSeconds",
            String.valueOf(getRunTimeInSeconds() * 0.01));
        LOGGER.info("display the parameters.... " + appserverHostname
            + flowContext.getOutputLogFile());
        runJmeterScript(STTibcoTradeTestbed.loadMachine, flowContext);
        // runFlowByMachineId(STTibcoTradeTestbed.TRADE_MACHINE_ID, JMeterRunFlow.class,
        // flowContext, TimeUnit.SECONDS, SizeType.MAMMOTH.getTimeout());
    }

    @SuppressWarnings("static-access")
    private void stopAllServers() {
        LOGGER.info("Going to stop servers. WAS and Tibco");
        runSerializedCommandFlowFromRole(STTibcoTradeTestbed.loadMachine
            + WASTradeAppTestbed.WAS_85_ROLE_ID, Websphere85Role.ENV_WEBSPHERE_STOP);

        RunCommandFlowContext runCommandFlowContext =
            deserializeCommandFlowFromRole(STTibcoTradeTestbed.TIBCO_SERVER_ROLE_ID,
                TibcoTradeAppRole.TIB_STOP);
        runCommandFlowByMachineId(STTibcoTradeTestbed.appServerMachine, runCommandFlowContext);

        runSerializedCommandFlowFromRole(STTibcoTradeTestbed.TIBCO_SERVER_ROLE_ID,
            TibcoTradeAppRole.EMS_STOP);
    }

    @SuppressWarnings("static-access")
    private void startTibTradeApp() {
        LOGGER.info(this.getClass().getSimpleName() + STTibcoTradeTestbed.TIBCO_ROLE_ID);

        runSerializedCommandFlowFromRole(STTibcoTradeTestbed.TIBCO_SERVER_ROLE_ID,
            TibcoTradeAppRole.EMS_START);

        runSerializedCommandFlowFromRole(STTibcoTradeTestbed.loadMachine
            + WASTradeAppTestbed.WAS_85_ROLE_ID, Websphere85Role.ENV_WEBSPHERE_START);

        RunCommandFlowContext runCommandFlowContext =
            deserializeCommandFlowFromRole(STTibcoTradeTestbed.TIBCO_SERVER_ROLE_ID,
                TibcoTradeAppRole.TIB_START);
        runCommandFlowByMachineId(STTibcoTradeTestbed.appServerMachine, runCommandFlowContext);
    }

}
