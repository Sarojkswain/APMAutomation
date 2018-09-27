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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.ca.apm.automation.action.flow.mainframe.ConfigureEncodedFlow;
import com.ca.apm.automation.action.flow.mainframe.ConfigureEncodedFlowContext;
import com.ca.apm.automation.utils.ApmJdbc;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.apm.automation.utils.mainframe.Transactions;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole.CicsConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsTestDriverRole;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole.MqZosConfig;
import com.ca.apm.powerpack.sysview.tests.role.WasAppRole;
import com.ca.apm.powerpack.sysview.tests.testbed.TransactionImplementationTestbed;
import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.ConfigGenerator;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.powerpack.sysview.config.TransformerConfig;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * This is a semi-automatic ("assisted") test that is designed to validate the two implementations
 * of CICS TT generation code in the CE-APM agent.
 *
 * <p>While the test has it's own support for generating CICS transactions it is only optional
 * (controlled using the {@link #shouldGenerate} member). The design of the test is to long for a
 * prolonged period of time with the CE-APM agent catching as many (types) of CICS transactions as
 * possible and then comparing those.
 *
 * <p>Once deployed, the lock on the mainframe physical machine can be released as the test (launch)
 * phase is designed to run on the distributed machine. Keep in mind that you might need to reserve
 * it if running for longer than the default lock time.
 *
 * <p>The test runs until the file defined by {@link #STOP_FILE} appears on the system where test is
 * running.
 */
@Tas(testBeds = @TestBed(name = TransactionImplementationTestbed.class,
    executeOn = TransactionImplementationTestbed.WINDOWS_ID), size = SizeType.HUMONGOUS)
@Test(groups = TestClassification.ASSISTED)
public class TransactionImplementationTest extends TasTestNgTest {
    private static final Logger logger =
        LoggerFactory.getLogger(TransactionImplementationTest.class);

    private static final File STOP_FILE = new File("C:\\stop.txt");

    private Map<String, Runnable> generators = new HashMap<>();
    private boolean shouldGenerate = false;

    @BeforeTest
    protected void initialize() throws Exception {
        final String cptjApp = envProperties.getRolePropertyById(
            TransactionImplementationTestbed.MQ_APP_ID, WasAppRole.APP_URL_ROOT_PROP);
        final String wsApp = envProperties.getRolePropertyById(
            TransactionImplementationTestbed.WS_APP_ID, WasAppRole.APP_URL_ROOT_PROP);
        final String ctdDir = envProperties.getRolePropertyById(
            TransactionImplementationTestbed.CTD_ID, CicsTestDriverRole.INSTALL_DIR_PROPERTY);
        final MqZosConfig mq = TransactionImplementationTestbed.MQ;
        final CicsConfig cics = TransactionImplementationTestbed.CICS;

        final String generatedDefinition = "generated.xml";
        final String ctgHost = "tcp://"
            + envProperties.getMachineHostnameByRoleId(TransactionImplementationTestbed.CTG_ID);
        new ConfigGenerator( "tcp://" + ctgHost, 2006, cics.getIpicId(), 0, 0, 5, "[]")
            .addProgramCall("CALLPROG", null)
            .generate(ctdDir + "\\xml\\mapping.xml", ctdDir + "\\xml\\" + generatedDefinition);

        generators.put("CTG -> CICS", () -> {
            try {
                Transactions.generateCtgCics(ctdDir, generatedDefinition, true);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while generating CTG -> CICS transaction");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.warn("Failed to generate CTG -> CICS transaction", e);
            }
        });

        generators.put("MQ -> CICS", () -> {
            try {
                Transactions.postCptjcaapp(cptjApp, mq.getHost(), mq.getPort(),
                    mq.getQueueManagerName(), cics.getInputQueue(), cics.getReplyQueue(), 2);
            } catch (Exception e) {
                logger.warn("Failed to generate MQ -> CICS transaction", e);
            }
        });

        generators.put("WS -> CICS", () -> {
            try {
                Transactions.postWsExampleApp(wsApp, cics.getHost(), cics.getWsPort(), 2);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while generating WS -> CICS transaction");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.warn("Failed to generate WS -> CICS transaction", e);
            }
        });
    }

    @Test
    protected void test() throws Exception {
        BooleanSupplier shouldStop = () -> (STOP_FILE.exists());

        // Make sure the agent has both implementations enabled.
        Map<String, String> properties = new HashMap<>();
        properties.put(TransformerConfig.GENERATE_CICS_LIFETIME_TRACE, "yes");
        properties.put(TransformerConfig.GENERATE_CICS_OLD_LIFETIME_TRACE, "yes");
        String ceapmHome = envProperties.getRolePropertyById(TransactionImplementationTestbed.CEAPM.getRole(),
            CeapmRole.CEAPM_HOME_PROPERTY);
        // TODO: This requires the explicit cfgBuilder variable and specific order of calls because
        // inheritance is broken for ConfigureFlowContext.Builder. This can be simplified once pull
        // request: https://github-isl-01.ca.com/APM/tas/pull/63 is accepted and we use a version
        // that contains it.
        ConfigureEncodedFlowContext.Builder cfgBuilder = new ConfigureEncodedFlowContext.Builder();
        cfgBuilder
            .encoding(CeapmRole.CEAPM_PROPERTIES_ENCODING)
            .configurationMap(ceapmHome + CeapmRole.CEAPM_PROPERTIES_FILE, properties);
        runFlowByMachineId(TransactionImplementationTestbed.MAINFRAME_ID, ConfigureEncodedFlow.class,
            cfgBuilder.build());

        // Start the agent
        CeapmRole.startAgent(aaClient, envProperties,
            TransactionImplementationTestbed.CEAPM.getRole(), null, true);

        Date start = new Date();

        // (optional) Generation
        if (shouldGenerate) {
            generate(shouldStop, 120);
        }

        // Wait for stop condition if it hasn't happened yet (e.g. if generation was disabled).
        if (!shouldStop.getAsBoolean()) {
            logger.info("Waiting on stop condition ...");
            while (!shouldStop.getAsBoolean()) {
                Thread.sleep(1_000);
            }
            logger.info("Stop condition detected, continuing");
        }

        Date end = new Date();

        try (ApmJdbc apm = new ApmJdbc("localhost", 5001, "Admin", "")) {
            // Collection
            Collection<Document> traces =
                apm.getTraces(start, end, "type:normal and process:Cross-Enterprise*", 0);

            // Verification
            verify(traces);
        }
    }

    /**
     * Generates CICS transactions using the initialized {@link #generators} until the provided
     * stopCondition is met.
     *
     * @param stopCondition Condition for stopping the generator.
     * @param gracePeriod Grace period in seconds used to allow traces to be delivered.
     */
    private void generate(BooleanSupplier stopCondition, int gracePeriod) {
        Collection<Thread> threads = new ArrayList<>(generators.size());
        for (Map.Entry<String, Runnable> generatorEntry : generators.entrySet()) {
            final String description = generatorEntry.getKey();
            final Runnable generator = generatorEntry.getValue();

            threads.add(new Thread(() -> {
                Thread.currentThread().setName(description + " transaction generator");

                logger.debug("Starting {} transaction generation", description);
                while (!stopCondition.getAsBoolean()) {
                    generator.run();
                }
                logger.debug("Ended {} transaction generation", description);
            }));
        }

        threads.parallelStream().forEach(Thread::start);
        threads.parallelStream().forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for generator threads to finish");
                // In this case we don't care and keep going with joining the generator threads
            }
        });

        try {
            logger.info("Waiting {} seconds for trace delivery", gracePeriod);
            Thread.sleep(gracePeriod * 1_000);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for trace delivery");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Verifies test assertions (details documented inline).
     *
     * @param traces Collection of traces to use for verification.
     * @throws AssertionError
     */
    private void verify(Collection<Document> traces) throws AssertionError {
        logger.info("Verifying {} traces", traces.size());
        assertTrue(traces.size() > 0);

        Map<String, Document> unmatched = new HashMap<>();
        for (Document doc : traces) {
            doc.getDocumentElement().normalize();
            String path = getTraceMetricPath(doc);
            assertNotNull(path);

            if (unmatched.containsKey(path)) {
                compareTraces(doc, unmatched.get(path));
                unmatched.remove(path);
            } else {
                unmatched.put(path, doc);
            }
        }

        if (!unmatched.isEmpty()) {
            unmatched.keySet().stream().forEach(s -> logger.debug("Unmatched: '{}'", s));
            fail("Found unmatched traces");
        }
    }

    /**
     * Compares two TransactionTrace XML documents recursively.
     *
     * @param first First document.
     * @param second Second document.
     */
    private void compareTraces(Document first, Document second) {
        CommonUtils.saveDocumentToFile(first, new File("test-output/last-compared-first.xml"));
        CommonUtils.saveDocumentToFile(second, new File("test-output/last-compared-second.xml"));

        assertEquals(getTraceMetricPath(first), getTraceMetricPath(second));

        List<Node> topComponents = Arrays.asList(first, second).stream()
            .map(d -> getFirstNode(d, "//TransactionTrace/CalledComponent"))
            .collect(Collectors.toList());

        compareComponentsDeep(topComponents.get(0), topComponents.get(1));
    }

    /**
     * Compares two CalledComponent XML elements recursively.
     *
     * @param first First element.
     * @param second Second element.
     */
    private void compareComponentsDeep(Node first, Node second) {
        List<Node> nodes = Arrays.asList(first, second);
        nodes.forEach(node -> assertEquals(node.getNodeType(), Node.ELEMENT_NODE));
        List<Element> components = nodes.stream()
            .map(node -> (Element)node).collect(Collectors.toList());

        // 1. Verifying Called Component attributes
        // Collect all the attributes (name -> value)
        List<Map<String, String>> allAttributes = components.stream()
            .map(this::getElementAttributes)
            .collect(Collectors.toList());
        // Remove the RelativeTimestamp attribute as we don't care about the absolute order
        allAttributes.forEach(attrs -> attrs.remove("RelativeTimestamp"));
        // The set of Attributes should not be empty
        assertTrue(allAttributes.stream().noneMatch(Map::isEmpty));
        // Compare attributes (both ways)
        allAttributes.stream().forEach(allExpected -> allExpected.entrySet().forEach(
            expected -> allAttributes.forEach(candidates -> {
                if (allExpected == candidates) { // Don't compare with itself
                    return;
                }

                final String expectedName = expected.getKey();
                final String expectedValue = expected.getValue();

                logger.debug("ATTRIB [{}] = '{}'", expectedName, expectedValue);

                assertTrue(candidates.containsKey(expectedName));
                assertEquals(candidates.get(expectedName), expectedValue);
            })));

        // 2. Verifying Called Component Parameters
        // Collect the parameters (Name -> Value)
        List<Map<String, String>> allParameters = components.stream()
            .map(this::getComponentParameters)
            .collect(Collectors.toList());
        // The set of parameters should not be empty
        assertTrue(allParameters.stream().noneMatch(Map::isEmpty));
        // Compare parameters (both ways)
        allParameters.stream().forEach(allExpected -> allExpected.entrySet().forEach(
            expected -> allParameters.forEach(candidates -> {
                if (allExpected == candidates) { // Don't compare with itself
                   return;
                }

                final String expectedName = expected.getKey();
                final String expectedValue = expected.getValue();

                logger.info("PARAM [{}] = '{}'", expectedName, expected.getValue());

                assertTrue(candidates.containsKey(expectedName));
                assertEquals(candidates.get(expectedName), expectedValue);
            })));

        // 3. Verifying same sub-Components and recursion
        // Collect the sub-Components (ComponentName -> node)
        List<Map<String, Node>> subComponents = components.stream()
            .map(this::getSubComponents)
            .collect(Collectors.toList());
        // Compare sub-Components (both ways)
        subComponents.stream().forEach(expected -> subComponents.stream().forEach(actual -> {
            if (expected == actual) { // Don't compare with itself
                return;
            }

            logger.info("SUBS ({}) [{}]", expected.size(),
                expected.keySet().stream().collect(Collectors.joining(",")));

            assertTrue(actual.keySet().containsAll(expected.keySet())); // Same set
        }));
        // Recurse into sub-Components
        subComponents.get(0).keySet().stream().forEach(componentName -> {
            logger.info("RECURSE INTO [{}]", componentName);
            compareComponentsDeep(subComponents.get(0).get(componentName),
                subComponents.get(1).get(componentName));
        });
    }

    /**
     * Retrieves the metric path represented by a TransactionTrace XML document.
     *
     * @param doc Document.
     * @return Metric path or {@code null} if one isn't found.
     */
    @Nullable
    private String getTraceMetricPath(Document doc) {
        return getFirstNodeValue(doc, "//TransactionTrace/CalledComponent/@MetricPath");
    }

    /**
     * Retrieves the value of the first node matching an XPath specification.
     *
     * @param root Root node for the search.
     * @param xpath XPath specification.
     * @return Node value or {@code null} if no node matched the XPath specification.
     */
    @Nullable
    private String getFirstNodeValue(Node root, String xpath) {
        Node node = getFirstNode(root, xpath);

        if (node == null) {
            return null;
        } else {
            return node.getNodeValue();
        }
    }

    /**
     * Retrieves the first node matching an XPath specification.
     *
     * @param root Root node for the search.
     * @param xpath XPath specification.
     * @return Node or {@code null} if no node matched the XPath specification.
     */
    @Nullable
    private Node getFirstNode(Node root, String xpath) {
        List<Node> nodes = getNodes(root, xpath);
        if (nodes.isEmpty()) {
            return null;
        } else {
            return nodes.get(0);
        }
    }

    /**
     * Retrieves all the nodes matching an XPath specification.
     *
     * @param root Root node for the search.
     * @param xpath XPath specification.
     * @return List of nodes matching the XPath specification.
     */
    private List<Node> getNodes(Node root, String xpath) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPathExpression expression = factory.newXPath().compile(xpath);
            NodeList raw = (NodeList)expression.evaluate(root, XPathConstants.NODESET);

            return IntStream.range(0, raw.getLength())
                .<Node>mapToObj(raw::item)
                .collect(Collectors.toList());
        } catch (XPathExpressionException e) {
            logger.error("Caught exception while querying nodes", e);
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves map of the attributes of an element.
     *
     * @param element Element to search.
     * @return Map of attribute key-value pairs.
     */
    private Map<String, String> getElementAttributes(Element element) {
        NamedNodeMap raw = element.getAttributes();
        return IntStream.range(0, raw.getLength())
            .<Attr>mapToObj(i -> (Attr)raw.item(i))
            .collect(Collectors.toMap(Attr::getName, Attr::getValue));
    }

    /**
     * Retrieves map of the Parameters of an CalledComponent element.
     *
     * @param element CalledComponent element to search.
     * @return Map of parameter key-value pairs.
     */
    private Map<String, String> getComponentParameters(Element element) {
        List<Node> raw = getNodes(element, "Parameters/Parameter");
        return raw.stream().collect(Collectors.toMap(
            p -> ((Element)p).getAttribute("Name"),
            p -> ((Element)p).getAttribute("Value")
        ));
    }

    /**
     * Retrieves a collection of all the sub-CalledComponents of a CalledComponent element.
     *
     * @param element CalledComponent element to search.
     * @return Collection mapping the ComponentName attribute value to the corresponding
     * CalledComponent element.
     */
    private Map<String, Node> getSubComponents(Element element) {
        List<Node> raw = getNodes(element, "CalledComponents/CalledComponent");

        // Make sure there are no duplicates (supported in general but not expected in our case)
        long uniqueNames = raw.stream()
            .map(n -> ((Element)n).getAttribute("ComponentName"))
            .distinct().count();
        assertEquals(uniqueNames, raw.size(), "Duplicate Called Component(s) detected");

        return raw.stream().collect(Collectors.toMap(
            p -> ((Element)p).getAttribute("ComponentName"),
            p -> p));
    }
}
