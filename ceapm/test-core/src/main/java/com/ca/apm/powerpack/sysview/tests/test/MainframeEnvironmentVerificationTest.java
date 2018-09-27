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
 */

package com.ca.apm.powerpack.sysview.tests.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.utils.mainframe.ims.ImsSubsystem;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.ExecResult;
import com.ca.apm.automation.utils.mainframe.sysview.TabularData;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole.CeapmJavaConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole.CicsConfig;
import com.ca.apm.powerpack.sysview.tests.role.DatacomRole.DatacomConfig;
import com.ca.apm.powerpack.sysview.tests.role.ImsRole.ImsConfig;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole.MqZosConfig;
import com.ca.apm.powerpack.sysview.tests.role.SysvDb2Role.SysvDb2Config;
import com.ca.apm.powerpack.sysview.tests.role.SysviewRole.SysviewConfig;
import com.ca.apm.powerpack.sysview.tests.testbed.MainframeEnvironmentVerification;
import com.ca.apm.powerpack.sysview.tests.testbed.MainframeTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ibm.jzos.Exec;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

/**
 * This test verifies basic assumptions about the mainframe environment described by the attached
 * testbed.
 *
 * <p>It can be called for any testbed that implements the {@link MainframeTestbed} interface and is
 * in the same package as the {@link MainframeEnvironmentVerification} testbed.
 */
@Tas(testBeds = @TestBed(name = MainframeEnvironmentVerification.class,
    executeOn = MainframeEnvironmentVerification.MF_MACHINE_ID), size = SizeType.MEDIUM)
@Test(groups = TestClassification.SPECIAL)
public class MainframeEnvironmentVerificationTest extends TasTestNgTest {
    private static final Logger logger =
        LoggerFactory.getLogger(MainframeEnvironmentVerificationTest.class);

    private Collection<SysviewConfig> sysviewConfigs;
    private Collection<SysvDb2Config> sysvDb2Configs;
    private Collection<CicsConfig> cicsConfigs;
    private Collection<MqZosConfig> mqConfigs;
    private Collection<ImsConfig> imsConfigs;
    private Collection<DatacomConfig> datacomConfigs;
    private Sysview sysview;

    private static final Collection<String> SYSVIEW_REQUIRED_OPTIONS =
        Collections.unmodifiableList(Arrays.asList(
            "CICS", "IMS", "MQSERIES", "MVS", "CEAPM"
        ));

    private static final Collection<String> SYSVIEW_REQUIRED_LOGSTREAMS =
        Collections.unmodifiableList(Arrays.asList(
            "CICSLOGR.TRAN", "CICSLOGR.TSUM", "CICSLOGR.XLOG", "IMSLOGR.IMRS", "IMSLOGR.IMTR"
        ));

    private static final Map<String, String> MQ_INPUTQ_CHECKS;
    static {
        Map<String, String> checks = new HashMap<>();
        checks.put("Get", "ENABLED");
        checks.put("Put", "ENABLED");
        checks.put("Trigger", "TRIGGER");
        checks.put("TrigDpth", "1");
        checks.put("TrigType", "EVERY");
        MQ_INPUTQ_CHECKS = Collections.unmodifiableMap(checks);
    }

    private static final Map<String, String> MQ_INITQ_CHECKS;
    static {
        Map<String, String> checks = new HashMap<>();
        checks.put("Get", "ENABLED");
        checks.put("Put", "ENABLED");
        checks.put("Trigger", "NOTRIGGER");
        MQ_INITQ_CHECKS = Collections.unmodifiableMap(checks);
    }

    private static final Map<String, String> MQ_REPLYQ_CHECKS = MQ_INITQ_CHECKS;

    private static final String CICS_TEST_TRANSACTION = "SLP3";

    private static final Collection<String> CICS_REQUIRED_TRANSACTIONS =
        Collections.unmodifiableList(Arrays.asList(
            CICS_TEST_TRANSACTION, "LOOP", "ECD1", "ECD2", "ECD4"
        ));

    private static final Collection<String> CICS_REQUIRED_PROGRAMS =
        Collections.unmodifiableList(Arrays.asList(
            "SLEEP03", "XPFTLOOP", "ECIDB2A", "ECIDB2B", "ECIDB2C", "ECIDB2D"
        ));

    private static final Collection<String> CICS_REQUIRED_JVM_PROGRAMS =
        Collections.unmodifiableList(Arrays.asList(
            "PROCMQM", "CALLPROG", "CALLPRO1", "CALLPRO2", "CALLPRO3", "CALLPRO4"
        ));

    private static final String IMS_TEST_TRANSACTION = "GSVIMSMQ";

    private static final Collection<String> IMS_REQUIRED_TRANSACTIONS =
        Collections.singletonList(IMS_TEST_TRANSACTION);

    /**
     * Asserts that no error message was returned from a sysview command.
     *
     * @param execResult Result object of sysview command to be checked.
     * @throws AssertionError If the assertion fails.
     * @throws IOException If unable to query the system state.
     */
    private static void assertNoErrorMessage(ExecResult execResult)
        throws AssertionError, IOException {
        for (String message : execResult.getTabularData().getMessages()) {
            if (message.matches(".{7}E")) {
                fail("Error message found: " + message);
            }
        }
    }

    /**
     * Asserts the existence of an MQ queue and state of an additional set of queue properties.
     *
     * @param sysview Sysview instance used for queries.
     * @param qmgr MQ Queue Manager to check.
     * @param queue Queue to expect.
     * @param checks Field/value property pairs to be checked for the queue.
     * @throws AssertionError If the assertion fails.
     * @throws IOException If unable to query the system state.
     */
    private static void assertMqQueue(Sysview sysview, String qmgr, String queue,
                                      Map<String, String> checks)
        throws AssertionError, IOException {
        ExecResult detailsResult = sysview.execute("MQ {0}; MQALTER Queue {1}", qmgr, queue);
        assertNoErrorMessage(detailsResult);
        TabularData details = detailsResult.getTabularData();

        for (Map.Entry<String, String> c : checks.entrySet()) {
            String actual = details.getFirstRowMatching("Field", c.getKey()).get("Value");
            logger.debug("  {}:{}.{} = {}", qmgr, queue, c.getKey(), actual);
            assertEquals(actual, c.getValue());
        }
    }

    /**
     * Asserts that a job is listening on a port.
     *
     * @param job Name of the job to check.
     * @param port Port number to check.
     * @throws AssertionError If the assertion fails.
     * @throws IOException If unable to query the system state.
     */
    private static void assertJobListeningOnPort(String job, int port)
        throws AssertionError, IOException {
        // The expected output format is:
        // <jobname> <conn> <state>
        //   Local Socket: <ip>..<port>
        //   Foregin Socket: <ip>..<port>
        // ...
        // 1 ~ jobname, 2 ~ state
        final Pattern reEntry = Pattern.compile("^([^\\s]+)\\s+[^\\s]+\\s+([^\\s]+).*");
        // 1 ~ port
        final Pattern reSocket = Pattern.compile("^\\s+Local Socket:\\s+.*\\.\\.(\\d+).*");

        logger.info("Asserting that job {} is listening on port {}", job, port);
        Exec exec = new Exec(new String[]{"sh", "-c", "netstat -c SERVER -P " + port});
        exec.run();
        try (InputStream is = exec.getStdoutStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr)) {

            String cJob = null;
            String cState = null;
            String cPort = null;
            String line;
            while ((line = br.readLine()) != null) {
                Matcher entry = reEntry.matcher(line);
                Matcher socket = reSocket.matcher(line);
                if (entry.matches()) {
                    cJob = entry.group(1);
                    cState = entry.group(2);
                    cPort = null;
                } else if (socket.matches()) {
                    assertNotNull(cJob);
                    assertNotNull(cState);

                    cPort = socket.group(1);
                }

                if (cPort != null) {
                    logger.debug("Port {} is in '{}' state for job {}", cPort, cState, cJob);
                    if (cState.equalsIgnoreCase("Listen")
                        && cPort.equals(String.valueOf(port))
                        && job.equalsIgnoreCase(cJob)) {
                        return;
                    }
                }
            }
        }

        fail("Job " + job + " is not listening on port " + port);
    }

    /**
     * Asserts that a MQ user connection of a specific type is established between a job
     * and an MQ queue.
     *
     * @param sysview Sysview instance used for queries.
     * @param qmgr MQ Queue Manager to check.
     * @param queue Queue of the expected connection.
     * @param job Jobname of the expected connection.
     * @param type Tyep of the expected connection.
     * @throws AssertionError If the assertion fails.
     * @throws IOException If unable to query the system state.
     */
    private static void assertMqUserConnection(Sysview sysview, String qmgr, String queue,
                                               String job, String type)
        throws AssertionError, IOException {
        logger.info("Asserting MQ queue connection from ({}){} into {}:{}", type, job, qmgr, queue);

        TabularData connections = sysview.execute("MQ {0}; MQQUSERS", qmgr).getTabularData();
        Map<String, String> details = connections.getFirstRowMatching("Queue", queue);
        assertNotNull(details);
        assertEquals(details.get("Jobname"), job);
        assertEquals(details.get("ConType"), type);
    }

    /**
     * Asserts that it is possible to put a message into a MQ queue.
     *
     * @param qmgr MQ Queue Manager to check.
     * @param inputQueue Input queue to use.
     * @param replyQueue Reply queue to use.
     * @throws AssertionError If the assertion fails.
     */
    private static void assertMqSendMessage(String qmgr, String inputQueue, String replyQueue)
        throws AssertionError {
        MQQueueManager queueManager = null;
        MQQueue queue = null;

        com.ibm.msg.client.commonservices.trace.Trace.setOn(true);
        try {
            logger.info("Asserting ability to put MQ message into {}:{}->{}",
                qmgr, inputQueue, replyQueue);

            queueManager = new MQQueueManager(qmgr);

            queue = queueManager.accessQueue(inputQueue, MQConstants.MQOO_INQUIRE
                | MQConstants.MQOO_OUTPUT | MQConstants.MQOO_FAIL_IF_QUIESCING);

            MQMessage message = new MQMessage();
            message.replyToQueueName = replyQueue;
            message.messageType = MQConstants.MQMT_REQUEST;
            message.writeString("Hello, World!");

            queue.put(message);
        } catch (Exception e) {
            fail("Failed to put a message into MQ " + qmgr + ":" + inputQueue, e);
        } finally {
            try {
                if (queue != null) {
                    queue.close();
                }

                if (queueManager != null) {
                    queueManager.disconnect();
                    queueManager.close();
                }
            } catch (MQException e) {
                logger.warn("Failed to clean up MQ connection", e);
            }
        }
    }

    /**
     * Returns the initiation queue configured for a specific MQ queue.
     *
     * @param sysview Sysview instance used for queries.
     * @param qmgr MQ Queue Manager to query.
     * @param queue Queue to query.
     * @return Name of the initiation queue or an empty string if none is configued.
     * @throws IOException If unable to query the system state.
     */
    private String getInitQueueForQueue(Sysview sysview, String qmgr, String queue)
        throws IOException {
        TabularData details = sysview.execute("MQ {0}; MQALTER Queue {1}", qmgr, queue)
            .getTabularData();
        return details.getFirstRowMatching("Field", "InitQ").get("Value");
    }

    @BeforeClass
    protected void initialize() {
        // This initializes the various configuration collections dynamically by using the 'id'
        // testbed property. This enables the use of this test on any testbed that implements the
        // MainframeTestbed interface.
        // Note that this currently assumes that the set of possible testbeds is in the same package
        // as the testbed we call getPackage() on below because the 'id' property is not fuly
        // qualified.
        try {
            Class<?> testbedClass = Class.forName(
                MainframeEnvironmentVerification.class.getPackage().getName()
                    + "." + envProperties.getTestbedPropertyById("id"));

            if (!MainframeTestbed.class.isAssignableFrom(testbedClass)) {
                fail("This test can only be called on a testbed that implements the "
                    + MainframeTestbed.class.getName() + " interface");
            }
            MainframeTestbed testbed = (MainframeTestbed)testbedClass.newInstance();

            sysviewConfigs = testbed.getSysviewInstances();
            sysvDb2Configs = testbed.getSysvdb2Instances();
            cicsConfigs = testbed.getCicsRegions();
            mqConfigs = testbed.getMqSubsystems();
            imsConfigs = testbed.getImsRegions();
            datacomConfigs = testbed.getDatacomInstances();
        } catch (Exception e) {
            fail("Unable to query testbed for environment configuration values", e);
        }

        if (!sysviewConfigs.isEmpty()) {
            // If the testbed has multiple Sysview instances we use the first for generic queries.
            try {
                sysview = new Sysview(sysviewConfigs.iterator().next().getLoadlib());
            } catch (IOException e) {
                fail("Unable to initialize Sysview instance used for generic queries");
            }
        } else {
            logger.warn("Some checks will be skipped as the testbed contains no sysview instance");
        }
    }

    @AfterClass
    protected void cleanup() {
        if (sysview != null) {
            sysview.close();
        }
    }

    private void verifyJava(CeapmJavaConfig config) {
        logger.info("Verifying {}", config);

        // Availability of a Java environment
        File javaExec = new File(config.getUssPath() + "/bin/java");
        assertTrue(javaExec.canExecute());
    }

    private void verifySysview(Sysview sysview, SysviewConfig config) {
        try {
            logger.info("Verifying {}", config);

            // Basic assumptions via the STATUS panel
            ExecResult statusResult = sysview.execute("status");
            for (String line : statusResult.getOutput()) {
                if (line.contains("Subsystem Id")) {
                    logger.info("Checking Sysview Subsystem Id");
                    // <prefix>  Subsystem Id  <subsystemId>
                    String[] components = line.substring(2).trim().split("\\s+");
                    assertEquals(components.length, 3);
                    logger.debug("  Subsystem Id: {}", components[2]);
                    assertEquals(components[2], config.getSubsystemId());
                } else if (line.contains("Options")) {
                    logger.info("Checking Sysview Options");
                    // <prefix>  Options       <option>,<option>,...
                    String[] components = line.substring(2).trim().split("\\s+");
                    assertEquals(components.length, 2);
                    logger.debug("  Options: {}", components[1]);
                    Collection<String> options = Arrays.asList(components[1].split(","));
                    assertTrue(options.containsAll(SYSVIEW_REQUIRED_OPTIONS));
                }
            }

            // State of required log-streams
            logger.info("Checking status of log streams in Sysview");
            TabularData lglogs = sysview.execute("lglogs").getTabularData();
            for (String required : SYSVIEW_REQUIRED_LOGSTREAMS) {
                Map<String, String> values = lglogs.getFirstRowMatching("Name", required);
                String status = values.get("Status");
                logger.debug("Log stream '{}' is {}", required, status);
                assertTrue(status.equalsIgnoreCase("ACTIVE") || status.equalsIgnoreCase("EMPTY"));
            }

            // No obvious error with JES integration
            logger.info("Checking Sysview JES integration");
            assertNoErrorMessage(sysview.execute("menu jes"));
        } catch (IOException e) {
            fail("Caught exception while verifying " + config, e);
        }
    }

    private void verifyMq(Sysview sysview, MqZosConfig config) {
        try {
            logger.info("Verifying {}", config);

            String qmgr = config.getQueueManagerName();

            // MQ state
            logger.info("Checking MQ '{}'", qmgr);
            assertNoErrorMessage(sysview.execute("MQ {0}", qmgr));

            // Port
            if (config.getPort() != 0) {
                // The port is serviced by the Channel Initiator Job
                TabularData mqs = sysview.execute("MQLIST").getTabularData();
                String chinitJob = mqs.getFirstRowMatching("Qmgr", qmgr).get("CJobname");
                assertNotNull(chinitJob);

                assertJobListeningOnPort(chinitJob, config.getPort());
            }

            // Input/Reply queues
            Map<String, String> queuePairs = new HashMap<>();
            for (ImsConfig imsConfig : imsConfigs) {
                if (imsConfig.getMq() == config) {
                    queuePairs.put(imsConfig.getInputQueue(), imsConfig.getReplyQueue());
                }
            }

            for (CicsConfig cicsConfig : cicsConfigs) {
                if (cicsConfig.getMq() == config) {
                    queuePairs.put(cicsConfig.getInputQueue(), cicsConfig.getReplyQueue());
                }
            }

            Collection<String> initiationQueues = new ArrayList<>();
            Collection<String> expectedProcesses = new ArrayList<>();
            TabularData queues = sysview.execute("MQ {0}; MQQUEUE", qmgr).getTabularData();
            for (Map.Entry<String, String> queueNames : queuePairs.entrySet()) {
                String inputQueue = queueNames.getKey();
                String replyQueue = queueNames.getValue();

                logger.info("Checking MQ queue pair {}:{}->{}", qmgr, inputQueue, replyQueue);
                assertFalse(queues.getFirstRowMatching("Queue", inputQueue).isEmpty());
                assertFalse(queues.getFirstRowMatching("Queue", replyQueue).isEmpty());

                assertMqQueue(sysview, qmgr, inputQueue, MQ_INPUTQ_CHECKS);
                assertMqQueue(sysview, qmgr, replyQueue, MQ_REPLYQ_CHECKS);

                // Try to send a message into the queue
                assertMqSendMessage(qmgr, inputQueue, replyQueue);

                // Extract initiation queue and process
                String initQ = getInitQueueForQueue(sysview, qmgr, inputQueue);
                assertFalse(initQ.isEmpty());
                initiationQueues.add(initQ);
                TabularData details = sysview.execute("MQ {0}; MQALTER Queue {1}", qmgr, inputQueue)
                    .getTabularData();
                String process = details.getFirstRowMatching("Field", "Process").get("Value");
                assertFalse(process.isEmpty());
                expectedProcesses.add(process);
            }

            // Initiation queues referenced from the input queues
            for (String queue : initiationQueues) {
                logger.info("Checking MQ initiation queue {}:{}", qmgr, queue);
                assertMqQueue(sysview, qmgr, queue, MQ_INITQ_CHECKS);
            }

            // Processes referenced from the input queues
            TabularData processes = sysview.execute("MQ {0}; MQPROC", qmgr).getTabularData();
            for (String process : expectedProcesses) {
                logger.info("Checking MQ process {}:{}", qmgr, process);
                assertFalse(processes.getFirstRowMatching("Process", process).isEmpty());
            }
        } catch (IOException e) {
            fail("Caught exception while verifying " + config, e);
        }
    }

    private void verifyCics(Sysview sysview, CicsConfig config) {
        try {
            logger.info("Verifying {}", config);

            final String region = config.getJobName();

            // Ports
            for (int port : Arrays.asList(config.getEciPort(), config.getIpicPort(),
                config.getHttpPort(), config.getWsPort())) {
                if (port != CicsRole.NO_PORT) {
                    assertJobListeningOnPort(region, port);
                }
            }

            // Programs
            // TODO: Investigate whether the fact that JVM programs are not listed in the CPROGRAM
            // panel is a Sysview bug (in functionality and/or documentation).
            TabularData programs = sysview.execute("CICS {0}; CPROGRAM", region).getTabularData();
            for (String expected : CICS_REQUIRED_PROGRAMS) {
                logger.info("Checking CICS program {}:{}", region, expected);
                Map<String, String> details = programs.getFirstRowMatching("Program", expected);
                assertNotNull(details);
                assertNotEquals(details.get("Disabled"), "DISABLED");
            }

            // JVM Programs
            TabularData jvmPrograms =
                sysview.execute("CICS {0}; CJVMPROG", region).getTabularData();
            for (String expected : CICS_REQUIRED_JVM_PROGRAMS) {
                logger.info("Checking CICS JVM program {}:{}", region, expected);
                Map<String, String> details = jvmPrograms.getFirstRowMatching("Program", expected);
                assertNotNull(details);
                assertNotEquals(details.get("Disabled"), "DISABLED");
            }

            // Transactions
            TabularData transactions = sysview.execute("CICS {0}; CTRAN", region).getTabularData();
            for (String expected : CICS_REQUIRED_TRANSACTIONS) {
                logger.info("Checking CICS transaction {}:{}", region, expected);
                Map<String, String> details = transactions.getFirstRowMatching("Tran", expected);
                assertNotNull(details);
                assertNotEquals(details.get("Disabled"), "DISABLED");
            }

            if (config.getMq() != null) {
                // Referenced MQ is in the testbed
                assertTrue(mqConfigs.contains(config.getMq()));
                String qmgr = config.getMq().getQueueManagerName();

                // Trigger monitor connection to MQ
                if (config.getInputQueue() != null) {
                    logger.info("Checking CICS {} CKTI connection to MQ", region);
                    String initQ = getInitQueueForQueue(sysview, qmgr, config.getInputQueue());
                    assertFalse(initQ.isEmpty());
                    assertMqUserConnection(sysview, qmgr, initQ, region, "CICS");
                }
            }

            // Transaction execution
            String transaction = CICS_TEST_TRANSACTION;
            logger.info("Checking ability to execute CICS transaction {}:{}", region, transaction);
            // Get state before the transaction executes
            Map<String, String> before = sysview.execute(
                "CICS {0}; CTRAN; SELECT Tran = {1}", region, transaction)
                .getTabularData().getFirstRowMatching("Tran", transaction);
            assertNotNull(before);
            // Execute the transaction
            ExecResult result = sysview.execute("MVS MODIFY {0},{1}", region, transaction);
            assertNoErrorMessage(result);
            Thread.sleep(1_000);
            // Get state after the transaction executes
            Map<String, String> after = sysview.execute(
                "CICS {0}; CTRAN; SELECT Tran = {1}", region, transaction)
                .getTabularData().getFirstRowMatching("Tran", transaction);
            assertNotNull(after);
            // Compare and verify expectations
            int beforeCount = before.get("Count").trim().isEmpty() ?
                0 : Integer.valueOf(before.get("Count"));
            int afterCount = after.get("Count").trim().isEmpty() ?
                0 : Integer.valueOf(after.get("Count"));
            int beforeAbends = before.get("Abends").trim().isEmpty() ?
                0 : Integer.valueOf(before.get("Abends"));
            int afterAbends = after.get("Abends").trim().isEmpty() ?
                0 : Integer.valueOf(after.get("Abends"));
            assertTrue(afterCount > beforeCount);
            assertTrue(afterAbends <= beforeAbends);
        } catch (IOException e) {
            fail("Caught exception while verifying " + config, e);
        } catch (InterruptedException e) {
            fail("Interrupted while verifying " + config, e);
            Thread.currentThread().interrupt();
        }
    }

    private void verifyIms(Sysview sysview, ImsConfig config) {
        try {
            logger.info("Verifying {}", config);

            String region = config.getRegion();

            if (config.getMq() != null) {
                // Referenced MQ is in the testbed
                assertTrue(mqConfigs.contains(config.getMq()));
                String qmgr = config.getMq().getQueueManagerName();

                // Trigger monitor connection to MQ
                if (config.getInputQueue() != null) {
                    logger.info("Checking IMS {} trigger monitor connection to MQ", region);
                    String initQ = getInitQueueForQueue(sysview, qmgr, config.getInputQueue());
                    assertFalse(initQ.isEmpty());
                    String taskName = ImsSubsystem.TRIGGER_MONITOR.getTaskName(config.getVersion());
                    assertMqUserConnection(sysview, qmgr, initQ, taskName, "IMS");
                }

                // Transaction execution (through MQ)
                if (config.getInputQueue() != null && config.getReplyQueue() != null) {
                    String transaction = IMS_TEST_TRANSACTION;
                    logger.info("Checking ability to execute IMS transaction {}:{}",
                        region, transaction);
                    // Get state before the transaction executes
                    Map<String, String> before = sysview.execute("IMS {0}; IMSTRAN", region)
                        .getTabularData().getFirstRowMatching("Trancode", transaction);
                    assertNotNull(before);
                    // Execute the transaction
                    assertMqSendMessage(qmgr, config.getInputQueue(), config.getReplyQueue());
                    Thread.sleep(5_000);
                    // Get state after the transaction executes
                    Map<String, String> after = sysview.execute("IMS {0}; IMSTRAN", region)
                        .getTabularData().getFirstRowMatching("Trancode", transaction);
                    assertNotNull(after);

                    // Compare and verify expectations
                    int beforeCount = before.get("Enq").trim().isEmpty() ?
                        0 : Integer.valueOf(before.get("Enq"));
                    int afterCount = after.get("Enq").trim().isEmpty() ?
                        0 : Integer.valueOf(after.get("Enq"));
                    assertTrue(afterCount > beforeCount);
                }
            }

            // Transactions
            TabularData transactions = sysview.execute("IMS {0}; IMSTRAN", region).getTabularData();
            for (String expected : IMS_REQUIRED_TRANSACTIONS) {
                logger.info("Checking IMS transaction {}:{}", region, expected);

                Map<String, String> details =
                    transactions.getFirstRowMatching("Trancode", expected);
                assertNotNull(details);
                assertEquals(details.get("Stopped").trim(), ""); // "" ~ Not stopped
                assertEquals(details.get("Queue").trim(), ""); // "" ~ 0
            }
        } catch (IOException e) {
            fail("Caught exception while verifying " + config, e);
        } catch (InterruptedException e) {
            fail("Interrupted while verifying " + config, e);
            Thread.currentThread().interrupt();
        }
    }

    @Test(groups = TestClassification.SPECIAL)
    public void testJava() {
        for (CeapmRole.CeapmJavaConfig javaConfig : CeapmJavaConfig.values()) {
            verifyJava(javaConfig);
        }
    }

    @Test(groups = TestClassification.SPECIAL)
    public void testSysview() {
        if (sysview != null) {
            for (SysviewConfig sysviewConfig : sysviewConfigs) {
                verifySysview(sysview, sysviewConfig);
            }
        }
    }

    @Test(groups = TestClassification.SPECIAL)
    public void testSysvDb2() {
        for (@SuppressWarnings("unused") SysvDb2Config sysvDb2Config : sysvDb2Configs) {
            // Don't have anything specific outside of what's done in the role at the moment
        }
    }

    @Test(groups = TestClassification.SPECIAL)
    public void testMq() {
        if (sysview != null) {
            for (MqZosConfig mqConfig : mqConfigs) {
                verifyMq(sysview, mqConfig);
            }
        }
    }

    @Test(groups = TestClassification.SPECIAL)
    public void testCics() {
        if (sysview != null) {
            for (CicsConfig cicsConfig : cicsConfigs) {
                verifyCics(sysview, cicsConfig);
            }
        }
    }

    @Test(groups = TestClassification.SPECIAL)
    public void testDatacom() {
        for (@SuppressWarnings("unused") DatacomConfig datacomConfig : datacomConfigs) {
            // Don't have anything specific outside of what's done in the role at the moment
        }
    }

    @Test(groups = TestClassification.SPECIAL)
    public void testIms() {
        if (sysview != null) {
            for (ImsConfig imsConfig : imsConfigs) {
                verifyIms(sysview, imsConfig);
            }
        }
    }
}
