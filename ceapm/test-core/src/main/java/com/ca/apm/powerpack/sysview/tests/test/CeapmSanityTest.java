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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.mainframe.MvsCommandFlow;
import com.ca.apm.automation.action.flow.mainframe.MvsCommandFlowContext;
import com.ca.apm.automation.action.flow.mainframe.sysview.SysviewCommandFlow;
import com.ca.apm.automation.action.flow.mainframe.sysview.SysviewCommandFlowContext;
import com.ca.apm.automation.action.test.Clw;
import com.ca.apm.automation.action.test.Metric;
import com.ca.apm.automation.action.test.MetricSet;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.apm.automation.utils.mainframe.MvsTask.Command;
import com.ca.apm.automation.utils.mainframe.MvsTask.CommandBuilder;
import com.ca.apm.automation.utils.mainframe.Transactions;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.Rc;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole.CeapmConfig;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole.CeapmJavaConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsTestDriverRole;
import com.ca.apm.powerpack.sysview.tests.testbed.CeapmAppmapTestbed;
import com.ca.apm.powerpack.sysview.tests.testbed.CeapmSanityTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/** Sanity tests comparing results between current and reference CEAPM agent. */
public class CeapmSanityTest extends TasTestNgTest {
    private static final Logger logger = LoggerFactory.getLogger(CeapmSanityTest.class);

    // Number metrics tolerance (in percents)
    protected static final int TOLERANCE = 10;
    // Transaction timepoints limit (all received)
    protected static final int MIN_TIMEPOINTS = 20;
    // Edge of transient metrics which may not be captured by the other agent
    private static final int METRIC_EDGE_DATAPOINTS = 1;

    private static final String BUILD_RELEASE_METRIC = "Agent Stats|Build and Release";

    protected static DateFormat formatter = DateFormat.getDateTimeInstance();

    private static final CeapmConfig[] CEAPM_TASKS = {CeapmSanityTestbed.CEAPM_GA,
            CeapmSanityTestbed.CEAPM_NEW};

    @Tas(testBeds = @TestBed(name = CeapmSanityTestbed.class,
        executeOn = CeapmSanityTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM)
    @Test(groups = TestClassification.BAT)
    public void metricsTest() throws Exception {
        for (CeapmJavaConfig java : CeapmRole.SUPPORTED_JAVA_VERSIONS) {
            logger.info("Testing using {}", java);
            metricsTest(java);
        }
    }

    @Tas(testBeds = @TestBed(name = CeapmSanityTestbed.class,
        executeOn = CeapmSanityTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM)
    @Test(groups = TestClassification.BAT)
    public void transactionTest() throws Exception {
        for (CeapmJavaConfig java : CeapmRole.SUPPORTED_JAVA_VERSIONS) {
            logger.info("Testing using {}", java);
            transactionTest(java);
        }
    }

    /**
     * This test verifies the following about a CE-APM Agent:
     * <ul>
     * <li>a set of expected metrics is generated.</li>
     * <li>no unexpected metrics are being generated.</li>
     * <li>metric values do not (significantly) differ from those reported by another Agent</li>
     * </ul>
     *
     * @throws Exception On any failure.
     */
    protected void metricsTest(CeapmJavaConfig java) throws Exception {
        setupMetrics();
        startAgents(java);
        // Skip two polling intervals to avoid data inconsistency between agents
        Thread.sleep(30_000);

        logger.info("Generating metrics");
        Calendar start = Calendar.getInstance();
        generateMetrics();
        Calendar end = Calendar.getInstance();

        // Gather metrics data from both agents.
        logger.info("Gathering metrics from agents");
        String emLibDir =
            envProperties.getRolePropertyById(CeapmSanityTestbed.EM_ROLE_ID,
                DeployEMFlowContext.ENV_EM_LIB_DIR);
        Clw clw = new Clw.Builder().clwWorkStationDir(emLibDir).build();
        Set<Pattern> expectedMetrics =
            new LinkedHashSet<>(getPatternsFromResource("all_metrics.txt"));
        Set<Pattern> ignoredMetrics = new HashSet<>(getPatternsFromResource("ignored_metrics.txt"));
        Set<Pattern> valuesIgnoreList =
            new HashSet<>(getPatternsFromResource("ignored_values.txt"));

        MetricSet newMetrics = MetricSet.fromAgentMetrics(clw,
            CeapmSanityTestbed.CEAPM_NEW.getAgentName(), start, end);
        MetricSet gaMetrics = MetricSet.fromAgentMetrics(clw,
            CeapmSanityTestbed.CEAPM_GA.getAgentName(), start, end);

        logger.debug("Removing ignored metrics");
        for (MetricSet set : Arrays.asList(gaMetrics, newMetrics)) {
            Set<String> toRemove = new HashSet<>();

            Set<String> uniquePaths = set.getUniquePaths();
            assert uniquePaths != null;

            M: for (String path : uniquePaths) {
                for (Pattern re : ignoredMetrics) {
                    if (re.matcher(path).matches()) {
                        toRemove.add(path);
                        continue M;
                    }
                }
            }
            set.removePaths(toRemove);
        }

        logger.debug("Updating gathered metrics with replacement rules");
        Map<Pattern, String> replacements = getReplacementsFromResource("metric_replacements.txt");
        for (Pattern pattern : replacements.keySet()) {
            gaMetrics.updatePaths(pattern, replacements.get(pattern));
            newMetrics.updatePaths(pattern, replacements.get(pattern));
        }

        logger.info("Validating that all expected and no unexpected metrics are present");
        Set<String> unexpected = new HashSet<>();
        Set<Pattern> expectedFound = new HashSet<>();

        Set<String> newUniquePaths = newMetrics.getUniquePaths();
        assert newUniquePaths != null;

        M: for (String path : newUniquePaths) {
            for (Pattern re : expectedMetrics) {
                if (re.matcher(path).matches()) {
                    expectedFound.add(re);
                    continue M;
                }
            }

            unexpected.add(path);
        }

        validateBuildReleaseMetric(newMetrics);

        logger.debug("Filtering out metrics before value validation");
        Set<String> toFilter = new HashSet<>(unexpected);
        for (String path : newMetrics.getUniquePaths()) {
            for (Pattern pattern : valuesIgnoreList) {
                if (pattern.matcher(path).matches()) {
                    toFilter.add(path);
                    break;
                }
            }
        }
        toFilter.add(BUILD_RELEASE_METRIC);
        newMetrics.removePaths(toFilter);

        Map<String, String> differences = new TreeMap<>();
        @SuppressWarnings("rawtypes")
        final Comparator<Metric> dateComparator = new Comparator<Metric>() {
            @Override
            public int compare(Metric o1, Metric o2) {
                return o1.timestamp.compareTo(o2.timestamp);
            }
        };

        logger.info("Validating number metric values");
        Set<String> gaNumberPaths = gaMetrics.getNumberMetrics();
        assert gaNumberPaths != null;
        Set<String> newNumberPaths = newMetrics.getNumberMetrics();
        assert newNumberPaths != null;

        for (String path : newNumberPaths) {
            if (!gaNumberPaths.contains(path)) {
                if (newMetrics.getNumberMetric(path).size() > METRIC_EDGE_DATAPOINTS) {
                    differences.put(path,
                        "Unable to compare metric because it doesn't appear in the GA set.");
                }
                continue;
            }

            List<Metric<Double>> newSet = newMetrics.getNumberMetric(path);
            List<Metric<Double>> gaSet = gaMetrics.getNumberMetric(path);

            Collections.sort(newSet, dateComparator);
            Collections.sort(gaSet, dateComparator);

            normalizeTimePoints(path, newSet, gaSet);

            String result = compareNumberMetric(newSet, gaSet);
            if (result != null) {
                differences.put(path, result);
            }
        }

        logger.info("Validating text metric values");
        Set<String> gaTextPaths = gaMetrics.getTextMetrics();
        assert gaTextPaths != null;
        Set<String> newTextPaths = newMetrics.getTextMetrics();
        assert newTextPaths != null;

        for (String path : newTextPaths) {
            if (!gaTextPaths.contains(path)) {
                if (newMetrics.getTextMetric(path).size() > METRIC_EDGE_DATAPOINTS) {
                    differences.put(path,
                        "Unable to compare metric because it doesn't appear in the GA set.");
                }
                continue;
            }

            List<Metric<String>> newSet = newMetrics.getTextMetric(path);
            List<Metric<String>> gaSet = gaMetrics.getTextMetric(path);

            normalizeTimePoints(path, newSet, gaSet);

            Collections.sort(newSet, dateComparator);
            Collections.sort(gaSet, dateComparator);

            String result = compareTextMetric(newSet, gaSet);
            if (result != null) {
                differences.put(path, result);
            }
        }

        logger.info("Validating date metric values");
        Set<String> gaDatePaths = gaMetrics.getDateMetrics();
        assert gaDatePaths != null;
        Set<String> newDatePaths = newMetrics.getDateMetrics();
        assert newDatePaths != null;

        for (String path : newDatePaths) {
            if (!gaDatePaths.contains(path)) {
                if (newMetrics.getDateMetric(path).size() > METRIC_EDGE_DATAPOINTS) {
                    differences.put(path,
                        "Unable to compare metric because it doesn't appear in the GA set.");
                }
                continue;
            }

            List<Metric<Date>> newSet = newMetrics.getDateMetric(path);
            List<Metric<Date>> gaSet = gaMetrics.getDateMetric(path);

            normalizeTimePoints(path, newSet, gaSet);

            Collections.sort(newSet, dateComparator);
            Collections.sort(gaSet, dateComparator);

            String result = compareDateMetric(newSet, gaSet);
            if (result != null) {
                differences.put(path, result);
            }
        }


        // Report identified problems
        boolean failed = false;
        if (!expectedFound.containsAll(expectedMetrics)) {
            failed = true;
            logger.error("Missing expected metrics:");
            expectedMetrics.removeAll(expectedFound);
            for (Pattern re : expectedMetrics) {
                logger.error(" * {}", re.pattern());
            }
        }
        if (!unexpected.isEmpty()) {
            failed = true;
            logger.error("Found unexpected metrics:");
            for (String path : unexpected) {
                logger.error(" * {}", path);
            }
        }
        if (!differences.isEmpty()) {
            failed = true;
            logger.error("Found differences in metric values:");
            for (Map.Entry<String, String> difference : differences.entrySet()) {
                logger.error(" * {}: {}", difference.getKey(), difference.getValue());
            }
            logger.error(
                "Note this test is supposed to be passed manually after examining errors above caused likely"
                    + " by changing mainframe enviromnment affecting metrics values exceeding expected limit.");
        }

        assertFalse(failed);
    }

    /**
     * This test generates transactions that are captured by two agents (one GA, one new) and
     * then compares these captures to make sure there is no regression happening.
     *
     * @throws Exception On test failure.
     */
    protected void transactionTest(CeapmJavaConfig java) throws Exception {

        final int captureDuration = 180 + 30; // 3 minute test + 30s CTD overhead
        final Document[] doc = new Document[1];
        Thread capture = new Thread(new Runnable() {
            @Override
            public void run() {
                final String emLibDir =
                    envProperties.getRolePropertyById(CeapmSanityTestbed.EM_ROLE_ID,
                        DeployEMFlowContext.ENV_EM_LIB_DIR);

                Clw clw = new Clw.Builder().clwWorkStationDir(emLibDir).build();
                doc[0] = clw.getTransactions(".*", captureDuration);
            }
        });

        // (Re)start the agents and let them settle
        startAgents(java);
        Thread.sleep(10_000);

        // Start capture, and transaction generation
        capture.start();
        String ctdDir = envProperties.getRolePropertyById(CeapmAppmapTestbed.CTD_ROLE_ID,
            CicsTestDriverRole.INSTALL_DIR_PROPERTY);
        Transactions.generateCtgCics(ctdDir, "DB2ExtendedBig50PerMinuteFor3minutesLocal.xml",
            false);

        // Wait for capture to finish
        capture.join();
        assertNotNull(doc[0]);

        // Validate
        validate(doc[0]);
    }

    /**
     * Validates that the document contains equal transactions from the two agents.
     *
     * @param doc XML Document.
     * @throws Exception If a difference is detected.
     */
    private static void validate(Document doc) throws Exception {
        final List<String> agentNames = new ArrayList<>();
        // For the transactionTraces container the first level maps AgentName to a container with
        // all the associated elements. The inner container then maps a 'timestamp' to a list
        // of transactions that happened at that time (this is important because e.g. cics-db2
        // generates multiple transactions with the same time).
        final Map<String, Map<String, List<Element>>> transactionTraces = new HashMap<>();

        // Compile list of agent names and pre-allocate some of the data structures.
        for (CeapmConfig task : CEAPM_TASKS) {
            final String agentName = task.getAgentName();
            agentNames.add(agentName);
            transactionTraces.put(agentName, new HashMap<String, List<Element>>());
        }

        // Try to get the encoding of the document.
        String encoding = doc.getInputEncoding();
        if (encoding == null) {
            encoding = "UTF-8";
        }

        // Write the whole unmodified XML to a file for potential analysis.
        CommonUtils.saveDocumentToFile(doc, new File("test-output/capturedTraces.xml"));

        // Remove nodes that need to be ignored (are expected to be different)
        Collection<String> ignoredNodes = Collections.singletonList(
            "//TransactionTrace/CalledComponent/Parameters/Parameter[@Name='Web Service Name']");
        removeXmlNodes(doc, ignoredNodes);

        // Save TransactionTrace elements corresponding to the two CE-APM agents and
        // normalize the AgentName and Process attributes.
        doc.getDocumentElement().normalize();
        NodeList nodes = doc.getElementsByTagName("TransactionTrace");
        for (int i = 0; i < nodes.getLength(); ++i) {
            final Element tt = (Element) nodes.item(i);
            final String name = tt.getAttribute("AgentName");

            if (agentNames.contains(name)) {
                final Map<String, List<Element>> agentTransactions = transactionTraces.get(name);
                assert agentTransactions != null;

                // Normalize the attributes that are expected to differ
                tt.setAttribute("AgentName", "-");
                tt.setAttribute("Process", "-");

                // Normalize 10.4 parameter name change
                String parameterXpath =
                    "//CalledComponent/Parameters/Parameter[@Name='Initial Program Name']/@Name";
                XPathExpression expression =
                    XPathFactory.newInstance().newXPath().compile(parameterXpath);
                Node node = ((Node) expression.evaluate(tt, XPathConstants.NODE));
                if (node != null) {
                    node.setNodeValue("Program Name");
                }

                // Save the transaction (initializing the container if this is the first one for
                // the time point)
                final String startDate = tt.getAttribute("StartDate");
                if (agentTransactions.get(startDate) == null) {
                    agentTransactions.put(startDate, new ArrayList<Element>());
                }
                agentTransactions.get(startDate).add(tt);
            }
        }
        assert agentNames.size() == 2;
        assert transactionTraces.size() == 2;

        CommonUtils.saveDocumentToFile(doc, new File("test-output/normalizedTraces.xml"));

        // Compile a list of all the transaction times and make sure that we received a minimum
        // amount to be sure the test is representative.
        final Set<String> allTimes = new HashSet<>();
        int totalTransactions = 0;
        for (String name : agentNames) {
            for (String time : transactionTraces.get(name).keySet()) {
                totalTransactions += transactionTraces.get(name).get(time).size();
                allTimes.add(time);
            }
        }
        assertTrue(allTimes.size() >= MIN_TIMEPOINTS,
            "Only captured '" + allTimes.size() + "' time points. Unable to verify.");
        logger.debug("Captured {} transactions within {} time points",
            totalTransactions, allTimes.size());

        // Finally we go through all the time points and transactions and verify their consistency
        for (String time : allTimes) {
            // Verify the presence/counts of transactions at each time point
            int otherCount = 0;
            for (String name : agentNames) {
                final Map<String, List<Element>> agentTransactions = transactionTraces.get(name);

                // Verify that there are some transactions at this time point
                assertTrue(agentTransactions.containsKey(time),
                    "Agent '" + name + "' has no transaction at time '" + time + "'");

                // Verify that the number of transactions at this time point are equal
                int thisCount = agentTransactions.get(time).size();
                if (otherCount == 0) {
                    otherCount = thisCount;
                } else {
                    assertEquals(thisCount, otherCount, "Number of transactions at time '" + time
                        + "' differ between the agents: " + thisCount + " <=> " + otherCount);
                }
            }

            // Verify the contents of the transactions
            final List<Element> firstElements = transactionTraces.get(agentNames.get(0)).get(time);
            final List<Element> secondElements = transactionTraces.get(agentNames.get(1)).get(time);
            for (Element first : firstElements) {
                final String expected = nodeToString(first, encoding).replaceAll("\\s+", " ");

                final List<String> candidates = new ArrayList<>();
                for (Element second : secondElements) {
                    candidates.add(nodeToString(second, encoding).replaceAll("\\s+", " "));
                }

                boolean match = candidates.contains(expected);
                if (!match) {
                    // If we didn't match dump the nodes for easier analysis
                    logger.debug("expected: {}", expected);
                    for (String candidate : candidates) {
                        logger.debug("candidate: {}", candidate);
                    }
                }
                assertTrue(match, "Difference detected for transaction at time '" + time + "'");
            }
        }
    }

    /**
     * Generates a text representation of an XML node.
     *
     * @param node XML node.
     * @return Text representation of the XML node.
     * @throws Exception If unable to obtain an XML transformer.
     */
    private static String nodeToString(Node node, String encoding) throws Exception {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();

        DOMSource source = new DOMSource(node);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        StreamResult stream = new StreamResult(byteStream);

        transformer.transform(source, stream);
        return new String(byteStream.toByteArray(), Charset.forName(encoding));
    }

    /**
     * Removes any nodes matching the provided XPath expressions from an XML document.
     *
     * @param doc XML document.
     * @param xpaths XPath expressions.
     */
    private static void removeXmlNodes(Document doc, Collection<String> xpaths) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            for (String xpath : xpaths) {
                final XPathExpression expression = factory.newXPath().compile(xpath);

                NodeList removed = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);

                for (int i = 0; i < removed.getLength(); ++i) {
                    final Node node = removed.item(i);

                    node.getParentNode().removeChild(node);
                }

                logger.debug("Removed {} node(s) matching: {}", removed.getLength(), xpath);
            }
        } catch (XPathExpressionException e) {
            logger.warn("Caught exception while removing nodes", e);
        }
    }

    protected void startAgents(CeapmJavaConfig java) throws Exception {
        String startAgents = "";
        for (CeapmConfig task : CEAPM_TASKS) {
            String taskName = task.getTaskName();

            // stop the task
            String stopCommand = new CommandBuilder(Command.STOP).add(taskName).build();
            runFlowByMachineId(CeapmSanityTestbed.MF_MACHINE_ID, MvsCommandFlow.class,
                new MvsCommandFlowContext.Builder(stopCommand).build());

            // prepare start command
            Map<String, String> startParms =
                CeapmRole.getAgentParameters(envProperties, task.getRole(),
                    CeapmRole.getAgentJavaParameters(java));
            String startCommand =
                new CommandBuilder(Command.START).add(taskName).add(startParms).build();
            startAgents += "/" + startCommand + ";";
        }

        // Start agents together to keep the reported metrics synchronized
        runFlowByMachineId(
            CeapmSanityTestbed.MF_MACHINE_ID,
            SysviewCommandFlow.class,
            new SysviewCommandFlowContext.Builder(startAgents).loadlib(
                CeapmSanityTestbed.SYSVIEW.getLoadlib()).build());

        for (CeapmConfig task : CEAPM_TASKS) {
            CeapmRole.waitUntilAgentRunning(envProperties, task.getRole());
        }
    }

    /**
     * Prepare environment for metric generation.
     */
    protected void setupMetrics() {
        // IMS group configuration, assure at least one empty group exists
        // (no group members are necessary)
        runFlowByMachineId(
            CeapmSanityTestbed.MF_MACHINE_ID,
            SysviewCommandFlow.class,
            new SysviewCommandFlowContext.Builder(
                "GROUPS ADDGRP Type IMSTRAN, Group CEAPMA, Desc Automation")
                .addAcceptableRcs(Rc.ERROR).loadlib(CeapmSanityTestbed.SYSVIEW.getLoadlib())
                .build());
    }

    /**
     * Metric generation.
     */
    protected void generateMetrics() throws InterruptedException {
        // To get metrics 'Resource Argument 2' and 'Task' under 'CICS Regions|*|Alerts|*|'
        // create queue of LOOP transactions running under XPFTLOOP command in MVS Term.
        // Only one LOOP transaction can run at the time.
        // Each transaction will ABEND after 5 seconds.
        String loops =
            StringUtils.repeat("/F " + CeapmSanityTestbed.CICS.getJobName() + ",LOOP;", 10);
        runFlowByMachineId(CeapmSanityTestbed.MF_MACHINE_ID, SysviewCommandFlow.class,
            new SysviewCommandFlowContext.Builder(loops)
                .loadlib(CeapmSanityTestbed.SYSVIEW.getLoadlib()).build());

        // Wait in order to allow time for LOOPs to be executed.
        // Then wait for enough additional metric intervals.
        final int metricGenerationDelay = 180_000;
        logger.info("Waiting for {}s to generate metrics...", metricGenerationDelay / 1000);
        Thread.sleep(metricGenerationDelay);
    }

    /**
     * Compares metrics with numeric values.
     * Only compared data point values, not their times.
     * The comparison is done by average difference over the whole data sets.
     *
     * @param first First data set.
     * @param second Second data set.
     * @return String describing an encountered difference or null if there is none.
     */
    @Nullable
    protected String compareNumberMetric(@NotNull List<Metric<Double>> first,
        @NotNull List<Metric<Double>> second) {
        assert first != null;
        assert second != null;

        if (first.size() != second.size()) {
            return "Number of data points is not equal (number): " + first.size() + " <=> "
                + second.size();
        }

        double difference = 0.0;
        double max = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < first.size(); ++i) {
            double v1 = first.get(i).value;
            double v2 = second.get(i).value;
            difference += v1 - v2;
            max = Math.max(max, Math.max(v1, v2));
        }

        double limit = TOLERANCE * max / 100;
        double avgDifference = Math.abs(difference) / first.size();
        if (avgDifference > limit) {
            return "Average difference (" + avgDifference + ") exceeds limit (" + limit + ").";
        }

        return null;
    }

    /**
     * Compares metrics with text values.
     * Validates that the sets contain the same data points (times) and same data point values.
     *
     * @param first First data set.
     * @param second Second data set.
     * @return String describing an encountered difference or null if there is none.
     */
    @Nullable
    protected String compareTextMetric(@NotNull List<Metric<String>> first,
        @NotNull List<Metric<String>> second) {
        assert first != null;
        assert second != null;

        if (first.size() != second.size()) {
            return "Number of data points is not equal (text): " + first.size() + " <=> "
                + second.size();
        }

        for (int i = 0; i < first.size(); ++i) {
            Date ts1 = first.get(i).timestamp;
            Date ts2 = second.get(i).timestamp;

            if (ts1.compareTo(ts2) != 0) {
                return "Inconsistent data points: " + formatter.format(ts1) + " <=> "
                    + formatter.format(ts2);
            }

            String firstValue = first.get(i).value;
            String secondValue = second.get(i).value;
            if (!firstValue.equals(secondValue)) {
                return "Different text value at " + formatter.format(ts1) + ": " + firstValue
                    + " <=> " + secondValue;
            }
        }

        return null;
    }

    /**
     * Compares metrics with date values.
     * Validates that the sets contain the same data points (times) and similar data point values.
     * Values are currently compared with a hard-coded 5 second tolerance.
     *
     * @param first First data set.
     * @param second Second data set.
     * @return String describing an encountered difference or null if there is none.
     */
    @Nullable
    protected String compareDateMetric(@NotNull List<Metric<Date>> first,
        @NotNull List<Metric<Date>> second) {
        assert first != null;
        assert second != null;

        if (first.size() != second.size()) {
            return "Number of data points is not equal (date): " + first.size() + " <=> "
                + second.size();
        }

        for (int i = 0; i < first.size(); ++i) {
            Date ts1 = first.get(i).timestamp;
            Date ts2 = second.get(i).timestamp;

            if (ts1.compareTo(ts2) != 0) {
                return "Inconsistent data points: " + formatter.format(ts1) + " <=> "
                    + formatter.format(ts2);
            }

            Date firstValue = first.get(i).value;
            Date secondValue = second.get(i).value;
            if (Math.abs(firstValue.getTime() - secondValue.getTime()) > 5_000) {
                return "Different date value at " + formatter.format(ts1) + ": "
                    + formatter.format(firstValue) + " <=> " + formatter.format(secondValue);
            }
        }

        return null;
    }

    /**
     * Generates a list of pattern/replacement pairs taken from a specific resource file.
     * Expects patterns and replacements alternating each on its own line - starting with a pattern.
     * The character sequence "%%%" in a pattern is interpreted as a wildcard that matches any
     * character sequence with exception of "|".
     * Ignores empty lines and those starting with a '#' character.
     *
     * @param resourceName Name of the resource.
     * @return Replacement pairs.
     * @throws IOException When unable to read the resource.
     */
    @NotNull
    protected static Map<Pattern, String> getReplacementsFromResource(@NotNull String resourceName)
        throws IOException {
        List<String> entries = getEntriesFromResource(resourceName);
        assert entries != null;

        Map<Pattern, String> replacements = new HashMap<Pattern, String>();
        Pattern key = null;
        for (String entry : entries) {
            if (key == null) {
                key = Pattern.compile("^\\Q" + entry.replace("%%%", "\\E[^\\|]*\\Q") + "\\E$");
            } else {
                replacements.put(key, entry);
                key = null;
            }
        }

        if (key != null) {
            throw new IOException("Malformed metric replacement data in '" + resourceName
                + "': number of patterns is odd");
        }

        return replacements;
    }

    /**
     * Generates a list of patterns taken from a specific resource file.
     * Expects one pattern per line. The character sequence "%%%" is interpreted as a wildcard that
     * matches any character sequence with the only exception of "|", "$$$" matches all.
     * Ignores empty lines and those starting with a '#' character.
     *
     * @param resourceName Name of the resource.
     * @return List of patterns.
     * @throws IOException When unable to read the resource.
     */
    @NotNull
    protected static List<Pattern> getPatternsFromResource(@NotNull String resourceName)
        throws IOException {
        List<String> entries = getEntriesFromResource(resourceName);
        List<Pattern> patterns = new ArrayList<Pattern>();
        for (String entry : entries) {
            // Escape meta characters and replace %%% and $$$ with a wild-card
            patterns.add(Pattern.compile("^\\Q"
                + entry.replace("%%%", "\\E[^\\|]*\\Q").replace("$$$", "\\E.*\\Q") + "\\E$"));
        }

        return patterns;
    }

    /**
     * Generates a list of strings taken from a specific resource file.
     * Ignores empty lines and those starting with a '#' character.
     *
     * @param resourceName Name of the resource.
     * @return List of entries.
     * @throws IOException When unable to read the resource.
     */
    @NotNull
    protected static List<String> getEntriesFromResource(@NotNull String resourceName)
        throws IOException {
        assert resourceName != null;

        try (BufferedReader r =
            new BufferedReader(new InputStreamReader(
                CeapmSanityTest.class.getResourceAsStream(resourceName)))) {

            List<String> entries = new ArrayList<String>();
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#') { // Skip comments and empty lines
                    continue;
                }

                entries.add(line);
            }

            return entries;
        } catch (IOException e) {
            throw new IOException("Failed to load list of entries from resource file", e);
        }
    }

    /**
     * Normalizes datapoint between two sets of data by removing any uncommon entries. Reports
     * when any differences are detected.
     *
     * @param path Metric path.
     * @param newSet Set of metrics from a new agent.
     * @param gaSet Set of metrics from a GA agent.
     * @param <T> Metric value type.
     */
    protected static <T> void normalizeTimePoints(@NotNull String path,
                                                  @NotNull List<Metric<T>> newSet,
                                                  @NotNull List<Metric<T>> gaSet) {
        // Collect the unique timepoints in each set
        Set<Date> newTimes = new LinkedHashSet<>();
        for (Metric<T> metric : newSet) {
            newTimes.add(metric.timestamp);
        }
        Set<Date> gaTimes = new LinkedHashSet<>();
        for (Metric<T> metric : gaSet) {
            gaTimes.add(metric.timestamp);
        }

        // Filter each set to only contain the common timepoints and keep track of whether we
        // removed anything
        final Set<Date> commonTimes = new HashSet<>(newTimes);
        commonTimes.retainAll(gaTimes);
        int differences = 0;

        Iterator<Metric<T>> iter = newSet.iterator();
        while (iter.hasNext()) {
            final Metric<T> metric = iter.next();
            if (!commonTimes.contains(metric.timestamp)) {
                differences++;
                iter.remove();
            }
        }

        iter = gaSet.iterator();
        while (iter.hasNext()) {
            final Metric<T> metric = iter.next();
            if (!commonTimes.contains(metric.timestamp)) {
                differences++;
                iter.remove();
            }
        }

        // Issue a warning if we detected (and filtered) unusual differences
        // Note missing first and/or last timepoint is rather common case
        // TODO Only warn if differences are in the middle of the metric timeline.
        if (differences > 2) {
            logger.warn("Detected differences in available timepoints for '{}'", path);
            logger.warn("* GA timepoints:  ({}) {}", gaTimes.size(),
                StringUtils.join(gaTimes, ","));
            logger.warn("* New timepoints: ({}) {}", newTimes.size(),
                StringUtils.join(newTimes, ","));
        }
    }

    /**
     * Checks specific metric for its value (release and build number)
     *
     * @param newMetrics metric list gathered from tested agent
     */
    private void validateBuildReleaseMetric(MetricSet newMetrics) {
        assertTrue(newMetrics.isText(BUILD_RELEASE_METRIC),
            "Metric doesn't appear: " + BUILD_RELEASE_METRIC);

        String value = newMetrics.getTextMetric(BUILD_RELEASE_METRIC).get(0).value;
        String pattern = "Release \\d+\\.\\d+.*\\(Build \\d+\\)";
        assertTrue(value.matches(pattern),
            "Metric value doesn't match expected pattern: " + BUILD_RELEASE_METRIC + " = " + value);
    }
}
