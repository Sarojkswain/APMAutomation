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
import static org.testng.AssertJUnit.fail;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.test.Clw;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.apm.automation.utils.appmap.Graph;
import com.ca.apm.automation.utils.appmap.Vertex;
import com.ca.apm.automation.utils.mainframe.Transactions;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CicsTestDriverRole;
import com.ca.apm.powerpack.sysview.tests.testbed.CrossLparTransactionTestbed;
import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.ConfigGenerator;
import com.ca.apm.transactiontrace.appmap.pages.LoginPage;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/** Test CICS-CICS cross-LPAR correlation in TTs. */
@Test(groups = TestClassification.FULL)
@Tas(testBeds = @TestBed(name = CrossLparTransactionTestbed.class,
    executeOn = CrossLparTransactionTestbed.DIST_WIN_ID), size = SizeType.MEDIUM)
public class CrossLparTransactionTest extends TasTestNgTest {
    private static final Logger logger = LoggerFactory.getLogger(CrossLparTransactionTest.class);

    private static final String CICS_PROGRAM_NAME = "CALLPROG";
    private static final String CA11_CICS_SYSID = "WB53";
    private static final String CICS_PROGRAM_DEFINITION =
        "[{program:CALLPRO1,sysId:" + CA11_CICS_SYSID + ",subActions:[{delay:1000}]}]";
    private static final String GENERATED_DEFINITON = "generated.xml";
    private static final int GENERATION_DURATION = 1; // [minutes]
    private static final int GENERATION_DELAY = 5_000; // [ms]

    protected WebDriver driver;
    protected String webviewUri;

    protected String callerCicsRegion;
    protected String callerCicsHost;
    protected String calleeCicsRegion;
    protected String calleeCicsHost;

    @BeforeClass
    public void startup() throws IOException, TimeoutException {
        for (String roleId : new String[] {CrossLparTransactionTestbed.CEAPM_11.getRole(),
                CrossLparTransactionTestbed.CEAPM_31.getRole()}) {
            CeapmRole.startAgent(aaClient, envProperties, roleId, null, true);
        }
    }

    @BeforeTest
    public void initialize() throws Exception {
        Properties emProperties =
            envProperties.getRolePropertiesById(CrossLparTransactionTestbed.EM_ID);
        webviewUri = String.format("http://%s:%s", emProperties.getProperty("em_hostname"),
            emProperties.getProperty("wvPort"));

        // Generate the necessary CTD test configuration files
        final String ctdDir = envProperties.getRolePropertyById(CrossLparTransactionTestbed.CTD_ID,
            CicsTestDriverRole.INSTALL_DIR_PROPERTY);

        new ConfigGenerator(
            "tcp://" + envProperties.getMachineHostnameByRoleId(CrossLparTransactionTestbed.CTG_ID),
            2006, CrossLparTransactionTestbed.CICS_31.getIpicId(), GENERATION_DURATION,
            GENERATION_DELAY * 1_000, 1, CICS_PROGRAM_DEFINITION)
            .addProgramCall(CICS_PROGRAM_NAME, null)
            .generate(ctdDir + "\\xml\\mapping.xml", ctdDir + "\\xml\\" + GENERATED_DEFINITON);

        callerCicsRegion = CrossLparTransactionTestbed.CICS_31.getJobName();
        callerCicsHost = CrossLparTransactionTestbed.CICS_31.getHost();
        calleeCicsRegion = CrossLparTransactionTestbed.CICS_11.getJobName();
        calleeCicsHost = CrossLparTransactionTestbed.CICS_11.getHost();
    }

    /**
     * Verifies that CTG correlation ID is correctly reported when executing cross-LPAR CICS
     * transactions initiated through a CTG server.
     */
    @Test(priority = 10)
    public void investigatorTest() throws Exception {
        // Generation duration + Two minutes to ensure delivery [s]
        final int captureDuration = GENERATION_DURATION * 60 + 150;
        final String ctdDir = envProperties.getRolePropertyById(CrossLparTransactionTestbed.CTD_ID,
            CicsTestDriverRole.INSTALL_DIR_PROPERTY);
        final String emLibDir = envProperties.getRolePropertyById(CrossLparTransactionTestbed.EM_ID,
            DeployEMFlowContext.ENV_EM_LIB_DIR);

        // Capture thread
        final List<Document> transactionData = new ArrayList<>(1);
        final Thread capture = new Thread(new Runnable() {
            @Override
            public void run() {
                Clw clw = new Clw.Builder().clwWorkStationDir(emLibDir).build();

                logger.info("Started TT capture");
                Document doc = clw.getTransactions(".*", captureDuration);
                logger.info("Finished TT capture");

                transactionData.add(doc);
            }
        });

        // Start capture
        capture.start();

        Transactions.generateCtgCics(ctdDir, GENERATED_DEFINITON, false);

        // Wait for capture to finish
        logger.info("Waiting for TT capture to finish");
        capture.join();

        assertEquals(transactionData.size(), 1);
        Document doc = transactionData.get(0);

        // Write the whole unmodified XML to a file for potential analysis.
        CommonUtils.saveDocumentToFile(doc, new File("test-output/capturedTraces.xml"));

        doc.getDocumentElement().normalize();

        final Pattern cicsPath =
            Pattern.compile("CICS Regions\\|[^|]*\\|[^|]*\\|[^|]*\\|Transaction Lifetime");
        final Pattern ctgPath =
            Pattern.compile("Backends\\|CTG ECI server .* program " + CICS_PROGRAM_NAME);

        // Go over all Transaction Traces and their Called Components and collect correlated sets
        // that will be later used during validation.
        Map<String, List<Element[]>> traces = new HashMap<>();
        NodeList ttNodes = doc.getElementsByTagName("TransactionTrace");
        logger.debug("Captured a total of {} transaction traces", ttNodes.getLength());

        for (int ttIdx = 0; ttIdx < ttNodes.getLength(); ++ttIdx) {
            assertEquals(ttNodes.item(ttIdx).getNodeType(), Node.ELEMENT_NODE);
            final Element tt = (Element) ttNodes.item(ttIdx);

            final NodeList ccNodes = tt.getElementsByTagName("CalledComponent");
            for (int j = 0; j < ccNodes.getLength(); ++j) {
                assertEquals(ccNodes.item(j).getNodeType(), Node.ELEMENT_NODE);
                final Element cc = (Element) ccNodes.item(j);
                final String metricPath = assertAndGetAttribute(cc, "MetricPath");

                if (!cicsPath.matcher(metricPath).matches()
                    && !ctgPath.matcher(metricPath).matches()) {
                    continue;
                }

                NodeList parameters = cc.getElementsByTagName("Parameter");
                for (int k = 0; k < parameters.getLength(); ++k) {
                    assertEquals(parameters.item(k).getNodeType(), Node.ELEMENT_NODE);
                    final Element parameter = (Element) parameters.item(k);

                    final String name = assertAndGetAttribute(parameter, "Name");
                    final String value = assertAndGetAttribute(parameter, "Value");

                    if (value.equalsIgnoreCase("IScopeCTGID")) {
                        if (!traces.containsKey(name)) {
                            traces.put(name, new ArrayList<Element[]>(3));
                        }
                        traces.get(name).add(new Element[]{tt, cc});
                    }
                }
            }
        }

        logger.debug("Identified a total of {} unique CTG correlation IDs" , traces.size());

        // Validate
        int setsMatched = 0;
        for (String corrId : traces.keySet()) {
            int ctgFound = 0;
            int callerCicsFound = 0;
            int calleeCicsFound = 0;

            for (Element[] data : traces.get(corrId)) {
                assertEquals(data.length, 2);
                final Element tt = data[0];
                final Element cc = data[1];

                final String metricPath = assertAndGetAttribute(cc, "MetricPath");

                if (ctgPath.matcher(metricPath).matches()) {
                    ++ctgFound;
                } else if (cicsPath.matcher(metricPath).matches()) {
                    final String host = assertAndGetAttribute(tt, "Host");

                    String region = null;
                    NodeList parameters = cc.getElementsByTagName("Parameter");
                    for (int k = 0; k < parameters.getLength(); ++k) {
                        assertEquals(parameters.item(k).getNodeType(), Node.ELEMENT_NODE);
                        final Element parameter = (Element) parameters.item(k);

                        final String name = assertAndGetAttribute(parameter, "Name");
                        final String value = assertAndGetAttribute(parameter, "Value");
                        if (name.equalsIgnoreCase("Job Name (Server Name)")) {
                            region = value;
                        }
                    }
                    assertNotNull(region);

                    if (host.equalsIgnoreCase(callerCicsHost)
                        && region.equalsIgnoreCase(callerCicsRegion)) {
                        ++callerCicsFound;
                    } else if (host.equalsIgnoreCase(calleeCicsHost)
                        && region.equalsIgnoreCase(calleeCicsRegion)) {
                        ++calleeCicsFound;
                    }
                }
            }

            // A correlation Id that didn't include a ctg backend isn't interesting
            if (ctgFound <= 0) {
                logger.debug("Correlation ID '{}' was not found in any CTG backends, ignoring",
                    corrId);
                continue;
            }

            logger.debug("Correlation ID '{}' was found in {} CTG backend(s), {} caller frontend(s)"
                + ", {} callee frontend(s)", corrId, ctgFound, callerCicsFound, calleeCicsFound);
            assertEquals(ctgFound, 1);
            assertEquals(callerCicsFound, 1);
            assertEquals(calleeCicsFound, 1);
            ++setsMatched;
        }
        logger.info("Matched a total of {} correlated sets", setsMatched);
        assertTrue(setsMatched >= 1);
    }

    /**
     * Verifies that the EM mapping logic for CICS vertices correctly connects CICS regions that are
     * part of a cross-LPAR transaction.
     * <p>
     * Note that this test is designed to be executed after {@link #investigatorTest()} and will
     * not work if executed standalone.
     */
    @Test(priority = 20)
    public void appmapTest() throws Exception {
        try {
            // We have to wait for the TT incubator to process the incoming traces.
            logger.info("Waiting for incubation period to elapse before verifying AppMap data");
            Thread.sleep(5 * 60 * 1_000);

            login();

            driver.get(webviewUri + Graph.WEBVIEW_PATH);

            final Graph appMap = Graph.fromWebviewHtmlSource(driver.getPageSource());
            assertNotNull(appMap);

            final Collection<Vertex> ctgBackends =
                appMap.getVerticesMatching(Vertex.CTG_BACKEND_FILTER);
            assertFalse(ctgBackends.isEmpty(), "Found no CTG backends");
            final Collection<Vertex> cicsFrontends = appMap.getVerticesMatching(Vertex.CICS_FILTER);
            assertFalse(cicsFrontends.isEmpty(), "Found no CICS frontends");

            // Overall the expected AppMap state looks like this:
            //     CTD frontend -> CTG backend ==> CICS_CALLER ==> CICS_CALLEE

            // Validate: CTG backend ==> CICS_CALLER (only)
            int foundCtgCics = 0;
            for (Vertex ctg : ctgBackends) {
                for (Vertex cics : ctg.getCalleesMatching(Vertex.CICS_FILTER)) {
                    logger.info("{} ==> {}", ctg, cics);
                    assertTrue(checkCicsVertex(cics, callerCicsRegion, callerCicsHost));
                    ++foundCtgCics;
                }
            }
            assertTrue(foundCtgCics > 0,
                "CTG ==> CICS(" + callerCicsRegion + "@" + callerCicsHost + ") not found");

            // Validate: CICS_CALLER ==> CICS_CALLEE (only)
            int foundCallerCallee = 0;
            for (Vertex caller : cicsFrontends) {
                if (!checkCicsVertex(caller, callerCicsRegion, callerCicsHost)) {
                    continue;
                }

                // We limit this to CICS callees to minimize the possibility of the test being
                // influenced by unrelated transactions executed against the involved regions.
                for (Vertex callee : caller.getCalleesMatching(Vertex.CICS_FILTER)) {
                    logger.info("{} ==> {}", caller, callee);
                    assertTrue(checkCicsVertex(callee, calleeCicsRegion, calleeCicsHost));
                    ++foundCallerCallee;
                }
            }
            assertTrue(foundCallerCallee > 0, "CICS(" + callerCicsRegion + "@" + callerCicsHost
                + ") ==> CICS(" + calleeCicsRegion + "@" + calleeCicsHost + ") not found");

            // Validate: CICS_CALLEE ==> *no-callees*
            for (Vertex cics : cicsFrontends) {
                if (!checkCicsVertex(cics, calleeCicsRegion, calleeCicsHost)) {
                    continue;
                }

                for (Vertex callee : cics.getAllCallees()) {
                    logger.error("{} ==> {}", cics, callee);
                }
                assertTrue(cics.getAllCallees().isEmpty());
            }
        } finally {
            closeWebDriver();
        }
    }

    /**
     * Checks that an XML Element contains an attribute and returns its value.
     *
     * @param element XML Element.
     * @param attribute Attribute name.
     * @return Attribute value.
     */
    private String assertAndGetAttribute(Element element, String attribute) {
        assertTrue(element.hasAttribute(attribute));
        return element.getAttribute(attribute);
    }

    /**
     * Checks whether a vertex is a CICS vertex representing a specific region on a specific
     * host (LPAR).
     *
     * @param vertex Vertex to check.
     * @param region CICS region name.
     * @param host CICS region hostname (LPAR).
     * @return {@code true} if the vertex represents a CICS region matching the passed in values.
     */
    private boolean checkCicsVertex(Vertex vertex, String region, String host) {
        assertNotNull(vertex);
        assertNotNull(region);
        assertNotNull(host);
        assertTrue(vertex.matches(Vertex.CICS_FILTER));

        final String actualRegion = vertex.getFirstAttributeValue("applicationName");
        final String actualHost = vertex.getFirstAttributeValue("hostname");

        return actualRegion.equalsIgnoreCase(region) && actualHost.equalsIgnoreCase(host);
    }

    // TODO: Refactor out to a common location, used by at least two tests now
    private void login() {
        try {
            driver = null;
            System.setProperty("webdriver.chrome.driver",
                "c:/automation/deployed/driver/chromedriver.exe");
            driver = ChromeDriver.class.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            fail("Cannot create driver instance");
        }

        // Login to EM
        WebDriverWait wait = new WebDriverWait(driver, 10);
        driver.get(webviewUri);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("Admin");
        loginPage.submitLogin();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn-close")));
    }

    // TODO: Refactor out to a common location, used by at least two tests now
    private void closeWebDriver() {
        driver.close();
        driver.quit();
    }
}
