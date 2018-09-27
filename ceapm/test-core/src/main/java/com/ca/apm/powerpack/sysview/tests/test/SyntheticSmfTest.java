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
import static org.testng.Assert.fail;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.mainframe.ConfigureEncodedFlow;
import com.ca.apm.automation.action.flow.mainframe.ConfigureEncodedFlowContext;
import com.ca.apm.automation.action.flow.mainframe.ConfigureEncodedFlowContext.Builder;
import com.ca.apm.automation.action.flow.test.LogVerifyFlow;
import com.ca.apm.automation.action.flow.test.LogVerifyFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.test.Clw;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.apm.automation.utils.smf.SmfData;
import com.ca.apm.automation.utils.smf.SmfSender;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.testbed.SyntheticSmfTestbed;
import com.ca.apm.powerpack.sysview.tools.smfgenerator.SmfRecordGenerator;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.install.KIntroscopeConfigConstants;
import com.wily.org.apache.commons.io.output.ByteArrayOutputStream;
import com.wily.powerpack.sysview.SYSVIEWAgent;
import com.wily.powerpack.sysview.config.TransformerConfig;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Regression tests that involve sending synthetic (or pre-recorded) SMF records directly to a
 * CE-APM agent.
 */
public class SyntheticSmfTest extends TasTestNgTest {

    private static final Logger log = LoggerFactory.getLogger(SyntheticSmfTest.class);

    private static final int SMF_PORT = SyntheticSmfTestbed.CEAPM.getSmfPort();
    private static final String CEAPM_ROLE_ID = SyntheticSmfTestbed.CEAPM.getRole();
    private static final String EM_ROLE_ID = SyntheticSmfTestbed.EM_ROLE_ID;

    private static final String ANTIFLOOD_KEY =
        KIntroscopeConfigConstants.kAgentTransactionTracerReportingLimitConfigKey;
    private static final String TT_CLAMP_KEY =
        KIntroscopeConfigConstants.kAgentTransactionTracerClamp;

    /** When {@code false}, some asserts will not fail the test immediately. */
    private static final boolean failFast = false;
    private final Set<AssertionError> fails = new LinkedHashSet<>();

    /** Nanoseconds in second. */
    final long NANOS = 1_000_000_000L;

    private String smfHost;
    private String logFile;
    private String profile;

    @BeforeClass
    public void initialize() throws Exception {
        smfHost = envProperties.getMachineHostnameByRoleId(CEAPM_ROLE_ID);
        final String agentHome =
            envProperties.getRolePropertyById(CEAPM_ROLE_ID, CeapmRole.CEAPM_HOME_PROPERTY);
        logFile = agentHome + SYSVIEWAgent.SYSVIEW_AGENT_LOG_FILE;
        profile = agentHome + CeapmRole.CEAPM_PROFILE_FILE;
    }

    /**
     * Regression test for
     * <a href="https://rally1.rallydev.com/#/45760473329d/detail/defect/48558538159">DE65922</a>.
     *
     * <p>
     * This involves repeatedly sending two specific CICS transactions in succession and checking
     * whether their processing didn't generate any exceptions in the agent.
     *
     * @throws Exception When the test fails.
     */
    @Tas(testBeds = @TestBed(name = SyntheticSmfTestbed.class,
        executeOn = SyntheticSmfTestbed.LINUX_MACHINE_ID), size = SizeType.MEDIUM)
    @Test(groups = TestClassification.FULL)
    public void regressionDe65922() throws Exception {
        final int preSendDelay = 10_000; // [ms]
        final int sendTime = 30_000; // [ms]
        final int sendTransactions = 200;

        // Load the definitions of the broken smf records
        ArrayList<SmfData> smfData = new ArrayList<>();
        for (String smfFile : Arrays.asList("synthetic-de65922-1.smf", "synthetic-de65922-2.smf")) {
            byte[] rawData =
                IOUtils.toByteArray(SyntheticSmfTest.class.getResourceAsStream(smfFile));
            smfData.add(SmfData.fromRawHexData(rawData));
        }

        startAgent();

        // Run capture for Pre-send time + Send time + Two minutes to ensure delivery [s]
        CaptureThread captureThread = new CaptureThread(120 + (preSendDelay + sendTime) / 1_000);

        Thread.sleep(preSendDelay);

        // Send smf records
        int sent = sendSmf(smfHost, SMF_PORT, smfData, sendTime / 1_000, sendTransactions);

        // Wait for capture to finish
        int captured = captureThread.getCaptured();

        // Validate that everything arrived
        log.info("Sent {} smf records, captured {} transaction traces", sent, captured);
        AssertJUnit.assertEquals(sent, captured);

        // Check the agent log file for errors/exceptions
        Set<String> keywords = new HashSet<>(Arrays.asList("Exception", "Caught"));
        runFlowByMachineId(SyntheticSmfTestbed.MF_MACHINE_ID, LogVerifyFlow.class,
            LogVerifyFlowContext.verifyNotContained(logFile, keywords));
    }

    /**
     * Regression test for
     * <a href="https://rally1.rallydev.com/#/45760473329d/detail/defect/56421337897">DE166525</a>.
     *
     * @throws Exception When the test fails.
     */
    @Tas(testBeds = @TestBed(name = SyntheticSmfTestbed.class,
        executeOn = SyntheticSmfTestbed.LINUX_MACHINE_ID), size = SizeType.MEDIUM)
    @Test(groups = TestClassification.FULL)
    public void regressionDe166525() throws Exception {
        final int forcedAntiflood = 15000;

        /*
         * Note that ttClamp is sometimes reached in test cases where load is under but still close
         * to ttClamp limit. Hence we only verify the TT throughput to 99%, and ignore
         * the "ttClamp reached" messages unless the load is significantly smaller.
         */

        // installation defaults, load below clamp
        startAgent();
        assertDelivery(19, 19);
        expectNotInLog(getAntifloodReachedMsg(forcedAntiflood));
        expectLog(getAntifloodMsg(forcedAntiflood), getClampMsg(150));

        // installation defaults, load is clamped by ttClamp
        restartAgent();
        assertDelivery(25, 19);
        expectNotInLog(getAntifloodReachedMsg(forcedAntiflood));
        expectLog(getAntifloodMsg(forcedAntiflood), getClampMsg(150));

        // configure ttClamp to lower value, load is clamped by that
        configureClamps(60, null);
        restartAgent();
        assertDelivery(15, 7);
        expectNotInLog(getAntifloodReachedMsg(forcedAntiflood));
        expectLog(getAntifloodMsg(forcedAntiflood), getClampMsg(60));

        // if load is well under the clamp the ttClamp must not trigger
        configureClamps(60, null);
        restartAgent();
        assertDelivery(6, 6);
        expectNotInLog(getAntifloodReachedMsg(forcedAntiflood), getClampReachedMsg(100));
        expectLog(getAntifloodMsg(forcedAntiflood), getClampMsg(60));

        // configure ttClamp to higher value, load is clamped by that
        configureClamps(200, null);
        restartAgent();
        assertDelivery(30, 25);
        expectNotInLog(getAntifloodReachedMsg(forcedAntiflood));
        expectLog(getAntifloodMsg(forcedAntiflood), getClampMsg(200));

        // configure ttClamp to higher value, load below clamp (but higher than default clamp)
        configureClamps(200, null);
        restartAgent();
        assertDelivery(24, 24);
        expectNotInLog(getAntifloodReachedMsg(forcedAntiflood));
        expectLog(getAntifloodMsg(forcedAntiflood), getClampMsg(200));

        // configure ttClamp to default explicitly
        configureClamps(50, null);
        restartAgent();
        assertDelivery(6, 6);
        expectNotInLog(getAntifloodReachedMsg(forcedAntiflood));
        expectLog(getAntifloodMsg(forcedAntiflood), getClampMsg(50, false));

        // users who keep old config file are limited by default ttClamp,
        // load below clamp
        configureClamps(null, 200);
        restartAgent();
        assertDelivery(6, 6);
        expectNotInLog(getAntifloodReachedMsg(forcedAntiflood));
        expectLog(getAntifloodMsg(forcedAntiflood), getAntifloodWarnMsg(200), getClampMsg(50, true));

        // users who keep old config file are limited by default ttClamp,
        // load clamped by ttClamp
        configureClamps(null, 200);
        restartAgent();
        assertDelivery(12, 6);
        expectNotInLog(getAntifloodReachedMsg(forcedAntiflood));
        expectLog(getAntifloodMsg(forcedAntiflood), getAntifloodWarnMsg(200), getClampMsg(50, true));

        // configure antiflood to higher than forced value
        configureClamps(10000, 20000);
        restartAgent();
        /*
         * The outgoing TT rates needed to verify the effectiveness of antiflood > 15000 (>1000tps)
         * are not achievable.
         * TT transfer to EM currently caps at ~70tps. SMF generation currently caps at ~200tps.
         * CEAPM agent has design limit of processing 200 SMF records (selection buffer) every 1s.
         * EM is set by default to accept 1000 tps.
         */
        assertDelivery(65, 65);
        expectNotInLog(getAntifloodReachedMsg(forcedAntiflood));
        expectLog(getAntifloodMsg(20000), getAntifloodWarnMsg(20000), getClampMsg(10000));

        if (!fails.isEmpty()) {
            for (AssertionError fail : fails) {
                log.error(fail.getMessage(), fail);
            }
            fail("See reported assertions above.");
        }
    }

    private String getAntifloodMsg(int antiflood) {
        return MessageFormat.format(
            "The internal Agent Transaction Tracer anti-flood collection limit has been configured"
                + " to {0,number,#} traces every {1,number,#} seconds.", antiflood, 15);
    }

    private String getAntifloodReachedMsg(int antiflood) {
        return MessageFormat.format(
            "The internal Agent Transaction Tracer anti-flood collection limit"
                + " of {0,number,#} traces every {1,number,#} seconds has been reached.",
            antiflood, 15);
    }

    private String getAntifloodWarnMsg(int antiflood) {
        return MessageFormat.format(
            "The Agent Transaction Tracer Anti-flood Limit ({0}) configuration option"
                + " is deprecated. It has been replaced by the Agent Transaction Trace Limit"
                + " ({1}) option. To maintain the original behavior"
                + " you should set {1} to at least {2,number,#}.", ANTIFLOOD_KEY, TT_CLAMP_KEY,
            Math.ceil((double) antiflood / 2));
    }

    private String getClampMsg(int ttClamp) {
        return getClampMsg(ttClamp, false);
    }

    private String getClampMsg(int ttClamp, boolean isDefault) {
        String defaultValue = isDefault ? "default value of " : "";
        return MessageFormat.format("Agent Transaction Trace Limit ({0}) set to " + defaultValue
            + "\"{1,number,#}\"", TT_CLAMP_KEY, ttClamp);
    }

    private String getClampReachedMsg(int ttClamp) {
        return MessageFormat.format(
            "The Agent Transaction Trace limit ({0}) of {1,number,#} transactions every interval has been "
                + "reached. No more transactions traces will be reported for this interval.",
            TT_CLAMP_KEY, ttClamp);
    }

    /**
     * Inspect log for expected messages.
     *
     * @param messages Expected messages
     */
    private void expectLog(String... messages) {
        Set<String> messageSet = new HashSet<>(Arrays.asList(messages));
        LogVerifyFlowContext verify = LogVerifyFlowContext.verifyContained(logFile, messageSet);
        runFlowByMachineId(SyntheticSmfTestbed.MF_MACHINE_ID, LogVerifyFlow.class, verify);
    }

    /**
     * Inspect log for unexpected messages. Splits the message into fragments around numbers (to
     * avoid false negatives due to different numeric value). Only fragments 4 words or longer are
     * considered (to avoid false positives due to too short generic fragments).
     *
     * TODO Implement log searches for patterns, rather than working with message fragments.
     *
     * @param messages Expected messages
     */
    private void expectNotInLog(String... messages) {
        Set<String> fragmentSet = new HashSet<>();
        for (String message : messages) {
            for (String fragment : message.split("[0-9]+")) {
                if (fragment.trim().split("\\s+").length > 3) {
                    fragmentSet.add(fragment);
                }
            }
        }
        LogVerifyFlowContext verify = LogVerifyFlowContext.verifyNotContained(logFile, fragmentSet);
        runFlowByMachineId(SyntheticSmfTestbed.MF_MACHINE_ID, LogVerifyFlow.class, verify);
    }

    /**
     * Configure clamps limiting the transaction trace rate.
     *
     * @param ttClamp ttClamp value, {@code null} to delete
     * @param antifloodThreshold antifloodThreshold value, {@code null} to delete
     */
    private void configureClamps(Integer ttClamp, Integer antifloodThreshold) {
        HashMap<String, String> configMap = new HashMap<>();
        HashSet<String> deleteSet = new HashSet<>();
        if (ttClamp == null) {
            deleteSet.add(TT_CLAMP_KEY);
        } else {
            configMap.put(TT_CLAMP_KEY, ttClamp.toString());
        }
        if (antifloodThreshold == null) {
            deleteSet.add(ANTIFLOOD_KEY);
        } else {
            configMap.put(ANTIFLOOD_KEY, antifloodThreshold.toString());
        }

        Builder builder =
            new ConfigureEncodedFlowContext.Builder().encoding(CeapmRole.CEAPM_PROPERTIES_ENCODING);
        builder.configurationMap(profile, configMap).configurationDeleteMap(profile, deleteSet);
        ConfigureEncodedFlowContext context = builder.build();
        runFlowByMachineId(SyntheticSmfTestbed.MF_MACHINE_ID, ConfigureEncodedFlow.class, context);
    }

    /** Restart agent and wait until it's ready to send SMF records to EM. Cleans up agent log. */
    private void restartAgent() {
        stopAgent();
        cleanupLog();
        startAgent();
    }

    /** Start agent and wait until it's ready to send SMF records to EM. */
    private void startAgent() {
        CeapmRole.startAgent(aaClient, envProperties, CEAPM_ROLE_ID, null, true);
        // wait for agent to connect to EM
        // TODO ask the EM for connected agents instead, and move to CeapmRole
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            fail("Interrupted while waiting");
        }
    }

    private void stopAgent() {
        CeapmRole.stopAgent(aaClient, envProperties, CEAPM_ROLE_ID);
    }

    /** Delete log to get clean slate for next test. Backup the log for analysis */
    private void cleanupLog() {
        FileModifierFlowContext clean =
            new FileModifierFlowContext.Builder()
                .copy(logFile, logFile + "." + RandomStringUtils.randomAlphanumeric(3))
                .delete(logFile).build();
        runFlowByMachineId(SyntheticSmfTestbed.MF_MACHINE_ID, FileModifierFlow.class, clean);
    }

    /**
     * Generate smf load using smf records that generate single TT each, and verify expected TT
     * response numbers.
     *
     * @param tpsSent Rate of sending smf records.
     * @param tpsReceived Expected rate of receiving TTs.
     * @throws Exception on failure.
     */
    private void assertDelivery(int tpsSent, int tpsReceived) throws Exception {
        Args.check(tpsReceived <= tpsSent,
            "With 1:1 smf to TT ratio, agent can't generate extra TTs");

        final int duration = 100;
        final int headWait = 5;
        // after the high transaction load finishes, the smf buffer slowly drains
        final int bufferCapacity = TransformerConfig.transactionTraceInputBufferSizeDefaultValue;
        final int tailWait = 5 + (tpsSent > tpsReceived ? bufferCapacity / tpsReceived : 0);
        CaptureThread thread = new CaptureThread(headWait + duration + tailWait);
        int toSend = duration * tpsSent;
        Thread.sleep(headWait * 1000);
        // use IMS smf record to guarantee 1:1 smf to TT ratio
        List<SmfData> smfData = Collections.singletonList(getImsSmfData());
        int sent = sendSmf(smfHost, SMF_PORT, smfData, duration, toSend);
        int captured = thread.getCaptured();
        double tpsActualSent = (double) sent / duration;
        double tpsActualReceived = (double) captured / duration;
        log.debug("sentReq={} sent={}:{} captured={}:{}", tpsSent, sent, round(tpsActualSent),
            captured, round(tpsActualReceived));
        assertEquals(sent, toSend, toSend * 0.01, "generator sent expected number of tranactions");
        if (tpsSent <= tpsReceived) {
            assertApprox(captured, sent, sent * 0.03, "Unclamped capture of transaction traces");
        } else {
            int expected = tpsReceived * duration + bufferCapacity;
            log.debug("Expected approx {} = ({} tps * {} s + {})", expected, tpsReceived, duration,
                bufferCapacity);
            assertApprox(captured, expected, expected * 0.1,
                "Clamped, capture approximate transaction traces");
        }
    }

    /**
     * Custom assert that allows for delayed assertions if {@link #failFast} is {@code false}.
     *
     * @param actual Actual value.
     * @param expected Expected value.
     * @param epsilon Absolute acceptable difference.
     * @param message Assertion message.
     */
    private void assertApprox(double actual, double expected, double epsilon, String message) {
        try {
            assertEquals(actual, expected, epsilon, message);
        } catch (AssertionError e) {
            if (failFast) {
                throw e;
            }
            log.error(e.getMessage());
            fails.add(e);
        }
    }

    private static SmfData getImsSmfData() throws IOException {
        // combine prefix record and actual SMF record
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream()) {
            for (String smf : Arrays.asList("ims.01p.smf", "ims.01.smf")) {
                InputStream is = SmfRecordGenerator.class.getResourceAsStream(smf);
                byte[] bytes = IOUtils.toByteArray(is);
                bas.write(bytes, 0, bytes.length);
            }
            return new SmfData(bas.toByteArray());
        }
    }

    /**
     * Send SMF records within specified time period. The sends are spaced to produce uniform
     * average load.
     *
     * @param host Target host.
     * @param port Target port.
     * @param smfData List of SMF data variants, sent round-robin.
     * @param seconds period allotted to sending of the SMF records.
     * @param transactions Number of SMF records to be sent.
     * @return Number of actually sent SMF records.
     * @throws IOException when sending the records fails.
     * @throws InterruptedException when interrupted during schedule waits.
     * @throws Exception when cleaning up the connections.
     */
    private int sendSmf(String host, int port, List<SmfData> smfData, int seconds, int transactions)
        throws IOException, InterruptedException, Exception {
        int sent = 0;

        log.info("Started sending smf records to {}:{}", host, port);
        try (SmfSender smfSender = new SmfSender(host, Collections.singletonList(port))) {
            // average duration [ns] per sent transaction
            final long nspt = seconds * NANOS / transactions;
            long start = System.nanoTime();
            long elapsed = 0;
            int late = 0;
            int lateDelay = 0;
            do {
                SmfData smf = smfData.get(sent % smfData.size());
                smfSender.send(smf);
                ++sent;
                // [ms] until next send
                long wait = ((((sent + 1) * nspt) + start - System.nanoTime()) * 1000 / NANOS);
                if (wait > 0) {
                    Thread.sleep(wait);
                } else {
                    late++;
                    lateDelay -= wait;
                }
                elapsed = System.nanoTime() - start;
            } while (elapsed < seconds * NANOS);
            if (late > 0) {
                log.warn("{} smf records were sent late, with average backlog of {} ms", late,
                    lateDelay / late);
            }
            log.info("Sent {} smf records in {} seconds ({} tps)", sent, round((double) elapsed
                / NANOS), round(sent / ((double) elapsed / NANOS)));
        }
        return sent;
    }

    /**
     * Format, rounding to two decimal places for purpose of log output.
     *
     * @param d number to format.
     * @return Formatted number.
     */
    private String round(double d) {
        return MessageFormat.format("{0,number,#.##}", d);
    }

    /** Helper that performs TT capture in EM for specified duration. */
    private class CaptureThread {
        private final int[] capturedTransactions = {0};
        private final Thread captureThread;

        /**
         * Start TT capture in EM for specified duration.
         *
         * @param captureDuration Capture duration [s]
         */
        public CaptureThread(final int captureDuration) {
            final String emLibDir =
                envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);

            captureThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    assert emLibDir != null && !emLibDir.isEmpty();
                    assert captureDuration > 0;

                    String query = ".*";
                    Clw clw = new Clw.Builder().clwWorkStationDir(emLibDir).build();
                    log.info("Started TT capture");
                    Document doc = clw.getTransactions(query, captureDuration);
                    log.info("Finished TT capture");

                    // save the traces for debugging
                    try {
                        File dir = new File("test-output/tt");
                        dir.mkdirs();
                        File file = File.createTempFile("capturedTraces", ".xml", dir);
                        CommonUtils.saveDocumentToFile(doc, file);
                    } catch (IOException e) {
                        log.warn("Unable to save captured traces.", e);
                    }

                    doc.getDocumentElement().normalize();
                    NodeList nodes = doc.getElementsByTagName("TransactionTrace");
                    capturedTransactions[0] = nodes.getLength();
                    log.debug("Captured {} transactions", capturedTransactions[0]);
                }
            });

            captureThread.start();
        }

        /**
         * Wait until capture finishes, and get count of captured traces.
         *
         * @return Number of captured traces.
         * @throws InterruptedException when interrupted while waiting.
         */
        public int getCaptured() throws InterruptedException {
            captureThread.join();
            return capturedTransactions[0];
        }
    }
}
