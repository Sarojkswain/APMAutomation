package com.ca.apm.systemtest.fld.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.SystemProperties;
import com.ca.apm.systemtest.fld.common.ServerStateException;
import com.ca.apm.systemtest.fld.test.generic.PPBaseSystemTest;
import com.ca.apm.systemtest.fld.testbed.STMQLoanTestbed;
import com.ca.apm.systemtest.fld.testbed.machines.JMeterLoadMachine;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlowContext;
import com.ca.apm.tests.role.JMeterRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.WebSphere8Role;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * MQ PowerPack System test.
 * 
 * @Author rsssa02
 */
public class MQSystemTest extends PPBaseSystemTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MQSystemTest.class);
    public ITasResolver tasResolver;
    public static final String containerPath =
        "|WebSphereMQ|JMS|[HOST]|QMGR1|Queues|replySOJQueue|Receive:Responses Per Interval";

    private static final String[] SERVER_CRASH_TOKENS = { "OutOfMemory" }; 
    private static final String SERVER_ERROR_LOG_FILE_NAME = "native_stderr.log";    
    
    public int minutes = 6;
    public int expValue = 150;

    public String appserverHostname;
    public String agentName;
    public String processName;

    MQSystemTest() throws Exception {
        super();
    }

    @Tas(testBeds = @TestBed(name = STMQLoanTestbed.class, executeOn = STMQLoanTestbed.appServerMachine), 
        owner = "sinal04", size = SizeType.DEBUG, exclusivity = ExclusivityType.NONEXCLUSIVE)
    @Test(groups = {"mq_st"})
    public void testDeployment() throws Exception {
        try {
            // cleanup
            initTestVariables();
            tearDownServers();

            // starting the WAS Server
            startAllServices();

            startTime = System.currentTimeMillis();
            // generate the needed load
            runJmeterScript();

            runJMXMonitoring(runTime);

            startTypePerfMonitor(STMQLoanTestbed.TYPE_PERF_ROLE_ID,
                STMQLoanTestbed.appServerMachine);

            validateMetricsMin(minutes, expValue);
            if (error != null) throw error;

            // teardown the test
            testTeardown(activeThreads);

            // generate and send reports
            Collection<String> agentErrors = sendResultEmail();

            // Check logs for errors before completing the test
            Assert.assertTrue(agentErrors == null || agentErrors.isEmpty(),
                "Errors found in Agent log: " + agentErrors);
        } catch (Exception e) {
            Assert.fail("Exception during test execution !", e);
        }
    }

    @Override
    protected void checkServerState() throws ServerStateException {
        //
        String logsDir = properties.getProperty(STMQLoanTestbed.APP_SERVER_LOG_DIR_PROPERTY_NAME);
        String nativeStdErrLog = Paths.get(logsDir, SERVER_ERROR_LOG_FILE_NAME).toString();
        File logFile = new File(nativeStdErrLog);

        if (logFile.exists()) {
            try (LineNumberReader lineReader = new LineNumberReader(new FileReader(logFile))) {
                String line = null;
                while ((line = lineReader.readLine()) != null) {
                    for (String searchToken : SERVER_CRASH_TOKENS) {
                        int tokenInd = line.indexOf(searchToken);
                        if (tokenInd != -1) {
                            String message = "App server state check failed. Found token '" + searchToken + "' in log file '" + 
                                nativeStdErrLog + "': line number=" + lineReader.getLineNumber() + ", line = '" + line + "'"; 
                            throw new ServerStateException(message); 
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("Skipping server log check. Could not find it: ", e);
            } catch (IOException e) {
                LOGGER.error("Skipping server log check. IO error occurred: ", e);
            }
        } else {
            LOGGER.info("Checking app server status: log file '{}' not found, skipping..", logFile);
        }
    }

    private void startAllServices() {
        runSerializedCommandFlowFromRole(STMQLoanTestbed.WAS_85_ROLE_ID,
            WebSphere8Role.ENV_WEBSPHERE_START);
    }

    private void tearDownServers() {
        LOGGER.info("Check point for system properties"
            + properties.getProperty("testbed_client.hostname") + SystemProperties.getEmailSender()
            + SystemProperties.getEmailRecipients()
            + properties.getProperty("role_webapp.container.type")
            + properties.getProperty("role_webapp.appserver.dir"));
        runSerializedCommandFlowFromRole(STMQLoanTestbed.WAS_85_ROLE_ID,
            WebSphere8Role.ENV_WEBSPHERE_STOP);
    }

    private void initTestVariables() throws Exception {
        appserverHostname =
            envProperties.getMachinePropertyById(STMQLoanTestbed.appServerMachine,
                "machine.appserver.hostname");
        agentName =
            envProperties.getMachinePropertyById(STMQLoanTestbed.appServerMachine,
                "machine.appserver.agent.name");
        processName =
            envProperties.getMachinePropertyById(STMQLoanTestbed.appServerMachine,
                "machine.appserver.process.name");
        containerName = "IBM MQ";
        emHost =
            envProperties
                .getMachinePropertyById(STMQLoanTestbed.loadMachine, "machine.em.hostname");
        metric =
            "*SuperDomain*|" + appserverHostname + "|" + processName + "|" + agentName
                + containerPath.replaceAll("\\[HOST\\]", appserverHostname);
        jmxMetrics =
            "java.lang:type=MemoryPool,name=Java heap|Usage/used,max;java.lang:type=GarbageCollector,name=Copy|CollectionCount|CollectionTime;java.lang:type=GarbageCollector,name=MarkSweepCompact|CollectionCount|CollectionTime";
        initCLWBean();
        initLogFiles();
    }

    private void runJmeterScript() {
        LOGGER.info("Starting jmeter thread");
        Date date = new Date();
        Format formatter = new SimpleDateFormat("YYYYMdd_hhmmss");
        JMeterRunFlowContext flowContext =
            (JMeterRunFlowContext) deserializeFlowContextFromRole(JMeterLoadMachine.JMETER_ROLE_ID,
                JMeterRole.RUN_JMETER, JMeterRunFlowContext.class);

        flowContext.setScriptFilePath(CLIENT_HOME + "/mqloanapp_load.jmx");
        flowContext.setOutputLogFile(jmeterOutDir + "jmeter_output_" + formatter.format(date)
            + ".log");
        flowContext.setOutputJtlFile(jmeterOutDir + "jmeter_output_jtl_" + formatter.format(date)
            + ".jtl");
        flowContext.getParams().put("appServerHost", appserverHostname);
        flowContext.getParams().put("testNumberOfCVUS", "70");
        flowContext.getParams().put("testDurationInSeconds", String.valueOf(getRunTimeInSeconds()));
        flowContext.getParams().put("logDir", flowContext.getOutputLogFile());
        flowContext.getParams().put("testWarmupInSeconds",
            String.valueOf(getRunTimeInSeconds() * 0.01));

        runJmeterScript(STMQLoanTestbed.loadMachine, flowContext);
        // runFlowByMachineId(STOracleSBTestbed.ORCL_MACHINE_ID,
        // JMeterRunFlow.class, flowContext, TimeUnit.SECONDS,
        // SizeType.MAMMOTH.getTimeout());
    }

}
