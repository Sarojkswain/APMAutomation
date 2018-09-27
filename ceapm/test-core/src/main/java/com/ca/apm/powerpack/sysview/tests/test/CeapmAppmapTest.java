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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.utils.appmap.Alert;
import com.ca.apm.automation.utils.appmap.Graph;
import com.ca.apm.automation.utils.appmap.Vertex;
import com.ca.apm.automation.utils.mainframe.Transactions;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole.CeapmJavaConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsRole.CicsConfig;
import com.ca.apm.powerpack.sysview.tests.role.CicsTestDriverRole;
import com.ca.apm.powerpack.sysview.tests.role.ImsRole.ImsConfig;
import com.ca.apm.powerpack.sysview.tests.role.MqZosRole.MqZosConfig;
import com.ca.apm.powerpack.sysview.tests.role.WasAppRole;
import com.ca.apm.powerpack.sysview.tests.testbed.CeapmAppmapTestbed;
import com.ca.apm.transactiontrace.appmap.pages.LoginPage;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Test integration with APM Team Center.
 */
public class CeapmAppmapTest extends TasTestNgTest {
    private static final Logger logger = LoggerFactory.getLogger(CeapmAppmapTest.class);
    private static final String APPMAP_PATH = "/ApmServer";
    private static final long RUN_TIME_LIMIT = 40 * 60 * 1000;

    protected WebDriver driver;
    protected String webviewUri;

    protected Map<String, Pattern> ctgBackendFilter = Vertex.CTG_BACKEND_FILTER;
    protected Map<String, Pattern> mqBackendFilter;
    protected Map<String, Pattern> wsBackendFilter = Vertex.WS_BACKEND_FILTER;
    protected Map<String, Pattern> btBackendFilter = Vertex.BT_BACKEND_FILTER;
    protected Map<String, Pattern> cicsFilter = Vertex.CICS_FILTER;
    protected Map<String, Pattern> imsFilter = Vertex.IMS_FILTER;
    protected Map<String, Pattern> db2Filter = Vertex.DB2_FILTER;

    protected static String SYSVIEW_MGMT_MODULE = "Cross-Enterprise APM: SYSVIEW";
    protected static String DB2_MGMT_MODULE = "Cross-Enterprise APM: DB2 z/OS";

    // ATC escapes special characters in MM names
    private static String escape(String what) {
        return what.replace(":", "\\:").replace("|", "\\|");
    }

    protected static String DB2_ALERT_PREFIX =
    "SuperDomain:" + escape(DB2_MGMT_MODULE) + ":" + escape("Cross-Enterprise APM: DB2 z/OS ");
    protected static String SYSVIEW_ALERT_PREFIX =
    "SuperDomain:" + escape(SYSVIEW_MGMT_MODULE) + ":";
    protected static Set<String> db2Alerts = new HashSet<String>(
    Arrays.asList(DB2_ALERT_PREFIX + "EDM Pool DBD Pool Full Failures Alert",
        DB2_ALERT_PREFIX + "EDM Pool Full Failures Alert",
        DB2_ALERT_PREFIX + "EDM Pool Statement Pool Full Failures Alert",
        DB2_ALERT_PREFIX + "Log Active Log Space Available (%) Alert",
        DB2_ALERT_PREFIX + "Subsystem Critical Exceptions Alert",
        DB2_ALERT_PREFIX + "Subsystem Warning Exceptions Alert"));
    // TODO last two are optional, if none of the SYSVDB2-defined alerts fired
    
    protected static Set<String> imsAlerts = new HashSet<String>(
        Arrays.asList(SYSVIEW_ALERT_PREFIX + "IMS Average Lifetime Per Transaction Alert"));
    protected static Set<String> cicsAlerts = new HashSet<String>(
    Arrays.asList(SYSVIEW_ALERT_PREFIX + "CICS Average Lifetime Per Transaction Alert",
        SYSVIEW_ALERT_PREFIX + "CICS Transactions Per Second Alert",
        SYSVIEW_ALERT_PREFIX + "CICS Dynamic Storage Area (DSA) Free Alert",
        SYSVIEW_ALERT_PREFIX + "CICS Extended Dynamic Storage Area (EDSA) Free Alert",
        SYSVIEW_ALERT_PREFIX + "CICS Alerts Summary"));

    // TODO last one is optional, if none of the SYSVIEW-defined CICS alerts fired
    
    // Expected differential alert metrics
    // with helpers that generate patterns matching differential alerts
    
    private static final Pattern db2DiffAlert(String metric) {
        return Pattern.compile(Alert.getDiffAlertPattern(null, null, null, DB2_MGMT_MODULE,
            "Cross-Enterprise APM: DB2 z/OS Differential Control",
            "Differential Control\\|DB2 z/OS Subsystems\\|[^|]+\\|\\Q" + metric + "\\E"));
    }

    protected static Set<Pattern> db2DiffAlerts =
    new HashSet<Pattern>(Arrays.asList(db2DiffAlert("Workload:Queued Create Thread Requests"),
        db2DiffAlert("Workload:Maximum Remote Users (%)"),
        db2DiffAlert("Workload:Maximum Users (%)")));

    private static final Pattern imsDiffAlert(String metric) {
        return Pattern.compile(Alert.getDiffAlertPattern(null, null, null, SYSVIEW_MGMT_MODULE,
            "IMS Differential Control",
            "Differential Control\\|IMS Subsystems\\|[^|]+:\\Q" + metric + "\\E"));
    }

    private static final Pattern imsDiffNonDuplicateAlert(String metric) {
        return Pattern.compile(Alert.getDiffAlertPattern(null, null, null, SYSVIEW_MGMT_MODULE,
            "IMS Differential Control", "IMS Subsystems\\|[^|]+:\\Q" + metric + "\\E"));
    }

    protected static Set<Pattern> imsDiffAlerts =
    new HashSet<Pattern>(Arrays.asList(imsDiffAlert("Average CPU Time Per Transaction (\u00B5s)"),
        imsDiffAlert("Average Lifetime Per Transaction (\u00B5s)"),
        imsDiffAlert("Transaction Rate Per Interval"), imsDiffAlert("Transaction Queue Depth"),
        imsDiffNonDuplicateAlert("Executed I/O Operations Count Per Interval")));

    private static final Pattern cicsDiffAlert(String metric) {
        return Pattern.compile(Alert.getDiffAlertPattern(null, null, null, SYSVIEW_MGMT_MODULE,
            "CICS Differential Control",
            "Differential Control\\|CICS Regions\\|[^|]+:\\Q" + metric + "\\E"));
    }

    protected static Set<Pattern> cicsDiffAlerts =
    new HashSet<Pattern>(Arrays.asList(cicsDiffAlert("Average CPU Time Per Transaction (\u00B5s)"),
        cicsDiffAlert("Average Lifetime Per Transaction (\u00B5s)"),
        cicsDiffAlert("Transactions Per Second")));
    protected static Map<String, Set<String>> cicsMetrics = new HashMap<String, Set<String>>();
    protected static Map<String, Set<String>> imsMetrics = new HashMap<String, Set<String>>();
    protected static Map<String, Set<String>> db2Metrics = new HashMap<String, Set<String>>();

    static {
        cicsMetrics.put("CICS Region Metrics",
            new HashSet<String>(Arrays.asList(
                "Average CPU Time Per Transaction (\u00B5s)",
                "Average Lifetime Per Transaction (\u00B5s)",
                "Transactions Per Second")));
        imsMetrics.put("IMS Subsystem Metrics",
            new HashSet<String>(Arrays.asList(
                "Average CPU Time Per Transaction (\u00B5s)",
                "Transaction Rate Per Interval",
                "Transaction Queue Depth",
                "Average Lifetime Per Transaction (\u00B5s)")));
        db2Metrics.put("DB2 z/OS Subsystem Metrics",
            new HashSet<String>(Arrays.asList(
                "Active Log Space Available (%)",
                "EDM Pool Full Failures", "DBD Pool Full Failures",
                "Statement Pool Full Failures",
                "Queued Create Thread Requests",
                "Maximum Users (%)",
                "Maximum Remote Users (%)")));
    }
    
    private static Collection<Vertex> cicsFrontends;
    private static Collection<Vertex> db2Frontends;
    private static Collection<Vertex> imsFrontends;
    private static Collection<Vertex> ctgBackends;
    private static Collection<Vertex> wsBackends;
    private static Collection<Vertex> mqBackends;

    @BeforeTest
    public void initialize() throws IOException {
        Properties emProperties =
            envProperties.getRolePropertiesById(CeapmAppmapTestbed.EM_ROLE_ID);
        webviewUri = String.format("http://%s:%s", emProperties.getProperty("em_hostname"),
            emProperties.getProperty("wvPort"));
    
        mqBackendFilter = new HashMap<>();
        mqBackendFilter.put("type", Pattern.compile("GENERICBACKEND"));
        mqBackendFilter.put(
            "name",
            Pattern.compile("Backends\\|WebSphereMQ.*\\|"
                + CeapmAppmapTestbed.MQ.getQueueManagerName()
                + "\\|Connector\\|Queues\\|.*\\|Put\\|Queue Put"));
    }

    @BeforeMethod
    public void login() {
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
        driver.manage().window().maximize();
        loginPage.submitLogin();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn-close")));
    }

    @AfterMethod
    public void closeWebDriver() {
        driver.close();
        driver.quit();
    }

    @Tas(testBeds = @TestBed(name = CeapmAppmapTestbed.class,
        executeOn = CeapmAppmapTestbed.WIN_MACHINE_ID), size = SizeType.MEDIUM)
    @Test(groups = TestClassification.SMOKE)
    public void testJava6() throws Exception {
        mainTest(CeapmJavaConfig.JVM6);
    }

    @Tas(testBeds = @TestBed(name = CeapmAppmapTestbed.class,
        executeOn = CeapmAppmapTestbed.WIN_MACHINE_ID), size = SizeType.MEDIUM)
    @Test(groups = TestClassification.SMOKE)
    public void testJava7() throws Exception {
        mainTest(CeapmJavaConfig.JVM7);
    }

    @Tas(testBeds = @TestBed(name = CeapmAppmapTestbed.class,
        executeOn = CeapmAppmapTestbed.WIN_MACHINE_ID), size = SizeType.MEDIUM)
    @Test(groups = TestClassification.SMOKE)
    public void testJava8() throws Exception {
        mainTest(CeapmJavaConfig.JVM8);
    }

    /**
     * Start CEAPM agent using specific Java version and execute the test steps.
     * @param java Java version.
     */
    protected void mainTest(CeapmJavaConfig java) throws Exception {
        CeapmRole.startAgent(aaClient, envProperties, CeapmAppmapTestbed.CEAPM.getRole(),
            CeapmRole.getAgentJavaParameters(java), true);

        populateAppMap();

        final long startTime = System.currentTimeMillis();
        String lastVerticesNotFound = "";
        String verticesNotFound = "";
        String pageSource = "";
        Graph appMap = null;

        do {
            Thread.sleep(10_000);

            // Gather appmap data for verification
            driver.get(webviewUri + Graph.WEBVIEW_PATH);
            if (driver.getPageSource().length() != pageSource.length()) {
                pageSource = driver.getPageSource();
                appMap = Graph.fromWebviewHtmlSource(pageSource);

                cicsFrontends = appMap.getVerticesMatching(cicsFilter);
                db2Frontends = appMap.getVerticesMatching(db2Filter);
                imsFrontends = appMap.getVerticesMatching(imsFilter);
                ctgBackends = appMap.getVerticesMatching(ctgBackendFilter);
                wsBackends = appMap.getVerticesMatching(wsBackendFilter);
                mqBackends = appMap.getVerticesMatching(mqBackendFilter);

                // Loop exit condition - all expected vertices exist
                lastVerticesNotFound = verticesNotFound;
                verticesNotFound = "";
                verticesNotFound += (cicsFrontends.isEmpty() ? "CICS frontends, " : "");
                verticesNotFound += (db2Frontends.isEmpty() ? "DB2 frontends, " : "");
                verticesNotFound += (imsFrontends.isEmpty() ? "IMS frontends, " : "");
                verticesNotFound += (ctgBackends.isEmpty() ? "CTG backends, " : "");
                verticesNotFound += (mqBackends.isEmpty() ? "MQ backends, " : "");
                verticesNotFound += (wsBackends.isEmpty() ? "WS backends, " : "");

                // Log vertices still missing in appmap
                if (!verticesNotFound.equals(lastVerticesNotFound)) {
                    long seconds = (System.currentTimeMillis() - startTime) / 1000;
                    if (!verticesNotFound.isEmpty()) {
                        logger.debug("Vertices not found in Appmap (" + seconds + "s): "
                            + verticesNotFound.substring(0, verticesNotFound.length() - 2));
                    } else {
                        logger.debug("All vertices found in Appmap (" + seconds + "s).");
                    }
                }
            }
        } while (!verticesNotFound.isEmpty()
            && System.currentTimeMillis() - startTime < RUN_TIME_LIMIT);

        logger.debug("Testing vertices and edges...");
        ctgCicsStructureTest(appMap);
        mqCicsStructureTest(appMap);
        wsCicsStructureTest(appMap);

        logger.debug("Testing vertex metrics..."); 
        metricsInit();
        metricsTest(cicsFrontends, cicsMetrics);
        metricsTest(db2Frontends, db2Metrics);

        logger.debug("Testing vertex alerts..."); 
        alertsTest(cicsFrontends, cicsAlerts, cicsDiffAlerts);
        alertsTest(db2Frontends, db2Alerts, db2DiffAlerts);

        logger.debug("Testing IMS all at once..."); 
        mqImsStructureTest(appMap);
        metricsTest(imsFrontends, imsMetrics);
        alertsTest(imsFrontends, imsAlerts, imsDiffAlerts);
    }

    /**
     * Generates all the transactions
     */
    protected void populateAppMap() {
        // Generate CTG->CICS transactions by CicsTestDriver 
        final Thread ctdRunner = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        generateCtgCics();
                    }
                } catch (Exception e) {
                    logger.error("CicsTestDriver runner exits with: " + e.getMessage());
                }
            }
        });
        ctdRunner.start();

        // Generate WAS originated transactions (MQ-CICS, MQ-IMS and WS-CICS)
        final Thread wasRunner = new Thread(new Runnable() {
            @Override
            public void run() {
                String appUrl = envProperties.getRolePropertyById(CeapmAppmapTestbed.MQ_APP_ROLE_ID,
                    WasAppRole.APP_URL_ROOT_PROP);
                String appWsUrl = envProperties.getRolePropertyById(
                    CeapmAppmapTestbed.WS_APP_ROLE_ID, WasAppRole.APP_URL_ROOT_PROP);
                try {
                    while (true) {
                        generateMqIms(appUrl, 1);
                        Thread.sleep(1_000);
                        generateMqCics(appUrl, 1);
                        Thread.sleep(1_000);
                        postWsExampleAppOrder(appWsUrl, 1);
                        Thread.sleep(1_000);
                    }
                } catch (Exception e) {
                    logger.error("WAS application runner exits with: " + e.getMessage());
                }
            }
        });
        wasRunner.start();
    }

    /**
     * CTG -> CICS vertices and edges
     * @param appMap appmap data
     */
    protected void ctgCicsStructureTest(Graph appMap) throws Exception {
        Collection<Vertex> ctgBackends = appMap.getVerticesMatching(ctgBackendFilter);
        assertFalse(ctgBackends.isEmpty(), "Found no CTG backends");
        assertFalse(cicsFrontends.isEmpty(), "Found no CICS frontends");
        assertFalse(db2Frontends.isEmpty(), "Found no DB2 frontends");

        // Specific CTG -> CICS
        int found = 0;
        for (Vertex ctg : ctgBackends) {
            for (Vertex cics : ctg.getCalleesMatching(cicsFilter)) {
                if (cics.getFirstAttributeValue("applicationName")
                    .equals(CeapmAppmapTestbed.CICS.getJobName())) {
                    validateCicsVertex(cics, CeapmAppmapTestbed.CICS.getJobName());
                    ++found;
                }
            }
        }
        assertTrue(found > 0, "No CTG -> CICS edges found");
    
        // Specific CICS -> DB2
        found = 0;
        for (Vertex cics : cicsFrontends) {
            for (Vertex db2 : cics.getCalleesMatching(db2Filter)) {
                if (db2.getFirstAttributeValue("applicationName")
                    .equals(CeapmAppmapTestbed.SYSVDB2.getSubsystem())) {
                    validateDb2Vertex(db2, CeapmAppmapTestbed.SYSVDB2.getSubsystem());
                    ++found;
                }
            }
        }
        assertTrue(found > 0, "No CICS -> DB2 edges found");

        // * -> CICS -> *
        // Only expected callers
        for (Vertex cics : cicsFrontends) {
            validateCicsVertex(cics, cics.getFirstAttributeValue("applicationName"));
            // Callers
            for (Vertex caller : cics.getAllCallers()) {
                assertTrue(caller.matches(mqBackendFilter) || caller.matches(ctgBackendFilter)
                    || caller.matches(wsBackendFilter) || caller.matches(btBackendFilter));
            }
        }
    
        // * -> DB2 -> *
        // Only expected callers
        for (Vertex db2 : db2Frontends) {
            validateDb2Vertex(db2, db2.getFirstAttributeValue("applicationName"));
            // Callers
            for (Vertex caller : db2.getAllCallers()) {
                assertTrue(caller.matches(cicsFilter));
            }
        }
    }

    /**
     * MQ -> CICS vertices and edges
     * @param appMap appmap data
     */
    protected void mqCicsStructureTest(Graph appMap) throws Exception {
        Collection<Vertex> mqBackends = appMap.getVerticesMatching(mqBackendFilter);
        assertFalse(mqBackends.isEmpty(), "Found no MQ backends");
        assertFalse(cicsFrontends.isEmpty(), "Found no CICS frontends");
    
        // Specific MQ -> CICS
        int found = 0;
        for (Vertex mq : mqBackends) {
            for (Vertex cics : mq.getCalleesMatching(cicsFilter)) {
                if (cics.getFirstAttributeValue("applicationName")
                    .equals(CeapmAppmapTestbed.CICS.getJobName())) {
                    validateCicsVertex(cics, CeapmAppmapTestbed.CICS.getJobName());
                    ++found;
                }
            }
        }
        assertTrue(found > 0, "No MQ -> CICS edges found");
    }

    /**
     * MQ -> IMS vertices and edges
     * @param appMap appmap data
     */
    protected void mqImsStructureTest(Graph appMap) throws Exception {
        Collection<Vertex> mqBackends = appMap.getVerticesMatching(mqBackendFilter);
        assertFalse(mqBackends.isEmpty(), "Found no MQ backends");
        assertFalse(imsFrontends.isEmpty(), "Found no IMS frontends");

        // Specific MQ -> IMS
        int found = 0;
        for (Vertex mq : mqBackends) {
            for (Vertex ims : mq.getCalleesMatching(imsFilter)) {
                if (ims.getFirstAttributeValue("applicationName")
                    .equals(CeapmAppmapTestbed.IMS.getRegion())) {
                    validateImsVertex(ims, CeapmAppmapTestbed.IMS.getRegion());
                    ++found;
                }
            }
        }
        assertTrue(found > 0, "No MQ -> IMS edges found");
    
        // * -> IMS -> *
        // Only expected callers
        for (Vertex ims : imsFrontends) {
            validateImsVertex(ims, ims.getFirstAttributeValue("applicationName"));
            // Callers
            for (Vertex caller : ims.getAllCallers()) {
                assertTrue(caller.matches(mqBackendFilter) || caller.matches(btBackendFilter));
            }
        }
    }

    /**
     * 
     * WS -> CICS vertices and edges
     * @param appMap appmap data
     */
    protected void wsCicsStructureTest(Graph appMap) throws Exception {
        Collection<Vertex> wsBackends = appMap.getVerticesMatching(wsBackendFilter);
        assertFalse(wsBackends.isEmpty(), "Found no WS backends");
        assertFalse(cicsFrontends.isEmpty(), "Found no CICS frontends");

        // Specific WS -> CICS
        int found = 0;
        for (Vertex ws : wsBackends) {
            for (Vertex cics : ws.getCalleesMatching(cicsFilter)) {
                if (cics.getFirstAttributeValue("applicationName")
                    .equals(CeapmAppmapTestbed.CICS.getJobName())) {
                    validateCicsVertex(cics, CeapmAppmapTestbed.CICS.getJobName());
                    ++found;
                }
            }
        }
        assertTrue(found > 0, "No WS -> CICS edges found");
    }

    /**
     * Appmap UI preparation for metricsTest
     * 
     */
    protected void metricsInit() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        // Navigate to the AppMap
        driver.get(webviewUri + APPMAP_PATH);

        // Close the Welcome page if it comes up
        try {
            WebElement closeButton =
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn-close")));
            if (closeButton != null) {
                logger.debug("Close welcome page");
                closeButton.click();
            }
        } catch (WebDriverException e) {
            logger.warn("Failed to close welcome page");
            Thread.sleep(2000);
        }

        // Turn off grouping
        logger.debug("Open Perspective dropdown");
        WebElement groupDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(@title, 'Selected Perspective:')]")));
        assertNotNull(groupDropdown);
        groupDropdown.click();

        logger.debug("Select 'No Group' perspective");
        WebElement noGroupButton = wait.until(ExpectedConditions
            .visibilityOfElementLocated(By.xpath("//*[starts-with(text(), 'No Group')]")));
        assertNotNull(noGroupButton);
        noGroupButton.click();

        logger.debug("Select Map view");
        WebElement mapButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[@class='viewMenuItem' and @title='Map']")));
        assertNotNull(mapButton);
        mapButton.click();

        // Wait for map rendering.
        long time = System.currentTimeMillis();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='appMapWorkIndicator']")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@id='appMapWorkIndicator']")));
        logger.debug("Progress bar waiting: " + (System.currentTimeMillis() - time)/1000 + "s");
    }

    /**
     * Tests metrics bound to vertex
     * @param vertices vertices to be tested
     * @param expectedMetrics expected metrics list 
     */
    protected void metricsTest(Collection<Vertex> vertices,
        Map<String, Set<String>> expectedMetrics) throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, 10);

        final Vertex vertex = vertices.iterator().next();
        final String vertexId = vertex.getId();
        final String vertexName =
            vertex.hasAttribute("name") ? vertex.getFirstAttributeValue("name") : "?";

        // Select the vertex
        logger.debug("Verifying vertex {} ({})", vertexName, vertexId);
        WebElement testVertex = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='" + vertexId + "']")));
        assertNotNull(testVertex);
        testVertex.click();

        // Waiting for side panel refresh 
        Thread.sleep(2000); //  as 'Loading data...' appears sometimes only 
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@class='work-indicator']")));

        for (String sectionName : expectedMetrics.keySet()) {
            // Verify expected section name
            WebElement section = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//attribute-grid[@selected-ids='" + vertexId
                    + "']//div[@class='performanceOverviewSectionName']//a[text() = '"
                    + sectionName + "']")));
            assertNotNull(section);
            logger.debug("  * {}", sectionName);

            // Verify expected metric names
            for (String metricName : expectedMetrics.get(sectionName)) {
                WebElement metric = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.xpath("//attribute-grid[@selected-ids='"
                        + vertexId + "']//p[@class='performanceOverviewMetricName']/a[text() = '"
                        + metricName + "']")));
                assertNotNull(metric);
                logger.debug("    * {}", metricName);
            }
        }
    }

    /**
     * Tests alerts bound to vertex
     * @param vertices vertices to be tested
     * @param expectedAlerts expected alerts list
     * @param expectedDiffAlerts expected differential alerts list
     */
    protected void alertsTest(Collection<Vertex> vertices, Set<String> expectedAlerts,
        Set<Pattern> expectedDiffAlerts) throws Exception {

        for (Vertex vertex : vertices) {
            validateAlerts(vertex, expectedAlerts);
            validateDifferentialAlerts(vertex, expectedDiffAlerts);
        }
    }

    /**
     * CicsTestDriver transactions
     */
    protected void generateCtgCics() {
        String ctdDir = envProperties.getRolePropertyById(CeapmAppmapTestbed.CTD_ROLE_ID,
            CicsTestDriverRole.INSTALL_DIR_PROPERTY);
        try {
            Transactions.generateCtgCics(ctdDir, "DB2_localhost2006_660IPIC.xml", true);
        } catch (Exception e) {
            logger.error("CicsTestDriver runner exits with: " + e.getMessage());
        }
    }

    /**
     * Generates MQ -> CICS transactions
     * @param wasContext WAS context root
     * @param count number of messages / transactions to generate
     */
    protected void generateMqCics(String wasContext, int count) throws ClientProtocolException,
        IOException {
        logger.debug("Generating MQ-CICS transactions");
        final MqZosConfig mq = CeapmAppmapTestbed.MQ;
        final CicsConfig cics = CeapmAppmapTestbed.CICS;
        assertEquals(cics.getMq(), mq);
        postAndValidateCptjcaapp(wasContext, mq, cics.getInputQueue(), cics.getReplyQueue(), count);
    }

    /**
     * Generates MQ -> IMS transactions
     * @param wasContext WAS context root
     * @param count number of messages / transactions to generate
     */
    protected void generateMqIms(String wasContext, int count) throws ClientProtocolException,
        IOException {
        logger.debug("Generating MQ-IMS transactions");
        final MqZosConfig mq = CeapmAppmapTestbed.MQ;
        final ImsConfig ims = CeapmAppmapTestbed.IMS;
        assertEquals(ims.getMq(), mq);
        postAndValidateCptjcaapp(wasContext, mq, ims.getInputQueue(), ims.getReplyQueue(), count);
    }

    /**
     * Post MQ transactions and validate replies.
     * see {@link Transactions#postCptjcaapp} for parameters
     */
    static void postAndValidateCptjcaapp(String wasContext, MqZosConfig mq, String sendQueue,
        String replyQueue, int count) throws ClientProtocolException, IOException {
        postAndValidateCptjcaapp(wasContext, mq.getHost(), mq.getPort(), mq.getQueueManagerName(),
            sendQueue, replyQueue, count);
    }

    /**
     * Post MQ transactions and validate replies.
     * see {@link Transactions#postCptjcaapp} for parameters
     */
    static void postAndValidateCptjcaapp(String wasContext, String mqHost, int mqPort,
        String mqManager, String mqQueue, String mqReplyQueue, int count)
        throws ClientProtocolException, IOException {
    
        String reply = "";
        int attempt = 1;
        do {
            reply = Transactions.postCptjcaapp(wasContext, mqHost, mqPort, mqManager, mqQueue,
                mqReplyQueue, count);
            logger.debug(reply);

        } while (reply.contains("MQException: MQJE001: Completion Code '2', Reason '2009'")
            && attempt++ < 2);
    
        // validate that the application sent and received the expected number of messages
        String infix =
            "Queue Manager Instance: " + mqManager + "|" + mqHost + "|" + mqPort + " Queue: ";
        assertFalse(reply.contains("MQException"), "exception detected");
    
        for (int i = 1; i <= count; i++) {
            String expected = "Sent message " + i + " to " + infix + mqQueue;
            assertTrue(reply.contains(expected), "sent " + i);
        }
        assertFalse(reply.contains("Sent message " + (count + 1) + " to " + infix + mqQueue),
            "sent extra");
    }

    /**
     * WS -> CICS transactions
     * @param appWsUrl WAS context root (URL)
     * @param count number of transactions to generate
     */
    protected void postWsExampleAppOrder(String appWsUrl, int count)
        throws ClientProtocolException, IOException, InterruptedException {
        Transactions.postWsExampleApp(appWsUrl, CeapmAppmapTestbed.CICS.getHost(),
            CeapmAppmapTestbed.CICS.getWsPort(), count);
    }

    /**
     * Validate CICS vertex attributes 
     * @param cics cics vertex
     * @param region
     */
    protected void validateCicsVertex(Vertex cics, String region) {
        logger.debug("validating CICS " + region);
        // Attributes
        assertTrue(cics.hasAttribute("type"), "Missing type attribute");
        assertEquals(cics.getFirstAttributeValue("type"), "TRANSACTION_PROCESSOR");
        assertTrue(cics.hasAttribute("transactionProcessor"),
            "Missing transactionProcessor attribute");
        assertEquals(cics.getFirstAttributeValue("transactionProcessor"), "CICS");
        assertTrue(cics.hasAttribute("applicationName"), "Missing applicationName attribute");
        assertEquals(cics.getFirstAttributeValue("applicationName"), region);
        assertTrue(cics.hasAttribute("name"), "Missing name attribute");
        assertEquals(cics.getFirstAttributeValue("name"), "CICS Region " + region);
        assertTrue(cics.hasAttribute("jobName"), "Missing jobName attribute");
        assertEquals(cics.getFirstAttributeValue("jobName"), region);
    }

    /**
     * Validate IMS vertex attributes
     * @param ims ims vertex
     * @param subsystem
     */
    protected void validateImsVertex(Vertex ims, String subsystem) {
        logger.debug("validating IMS " + subsystem);

        // Attributes
        assertTrue(ims.hasAttribute("type"), "Missing type attribute");
        assertEquals(ims.getFirstAttributeValue("type"), "TRANSACTION_PROCESSOR");
        assertTrue(ims.hasAttribute("transactionProcessor"),
            "Missing transactionProcessor attribute");
        assertEquals(ims.getFirstAttributeValue("transactionProcessor"), "IMS");
        assertTrue(ims.hasAttribute("applicationName"), "Missing applicationName attribute");
        assertEquals(ims.getFirstAttributeValue("applicationName"), subsystem);
        assertTrue(ims.hasAttribute("name"), "Missing name attribute");
        assertEquals(ims.getFirstAttributeValue("name"), "IMS Subsystem " + subsystem);
    }

    /**
     * Validate DB2 vertex attributes 
     * @param db2 db2 vertex
     * @param subsystem
     */
    protected void validateDb2Vertex(Vertex db2, String subsystem) {
        logger.debug("validating DB2 " + subsystem);

        // Attributes
        assertTrue(db2.hasAttribute("type"), "Missing type attribute");
        assertEquals(db2.getFirstAttributeValue("type"), "DATABASE");
        assertTrue(db2.hasAttribute("databaseType"), "Missing databaseType attribute");
        assertEquals(db2.getFirstAttributeValue("databaseType"), "DB2");
        assertTrue(db2.hasAttribute("SSID"), "Missing SSID attribute");
        assertEquals(db2.getFirstAttributeValue("SSID"), subsystem);
        assertTrue(db2.hasAttribute("applicationName"), "Missing applicationName attribute");
        assertEquals(db2.getFirstAttributeValue("applicationName"), subsystem);
        assertTrue(db2.hasAttribute("name"), "Missing name attribute");
        assertEquals(db2.getFirstAttributeValue("name"), "DB2 Subsystem " + subsystem);
    }

    /**
     * Differential alert metrics test
     * 
     * @param vertex tested vertex
     * @param expectedAlerts expected alerts list
     */
    protected void validateDifferentialAlerts(Vertex vertex, Set<Pattern> expectedAlerts) {
        // list of unexpected alerts
        Set<String> unexpected = new HashSet<String>();

        // set of found alerts for each pattern
        HashMap<Pattern, Set<String>> expectedMap = new HashMap<>();
        for (Pattern expected : expectedAlerts) {
            expectedMap.put(expected, new HashSet<String>());
        }

        outerloop: for (Alert alert : vertex.getAlerts()) { // Check for expected
            if (!alert.isDifferential()) {
                continue;
            }

            if (logger.isDebugEnabled()) {
                logger.debug(alert.toString());
            }

            String name = alert.getName();

            for (Entry<Pattern, Set<String>> expected : expectedMap.entrySet()) {
                Pattern pattern = expected.getKey();
                if (pattern.matcher(name).matches()) {
                    expected.getValue().add(name);
                    continue outerloop;
                }
            }
            unexpected.add(name);
            logger.error("Unexpected alert attached: {}", name);
        }

        // Verify all patterns have been matched once
        int correctlyMatched = 0;
        for (Entry<Pattern, Set<String>> expected : expectedMap.entrySet()) {
            Pattern pattern = expected.getKey();
            Set<String> matched = expected.getValue();
            if (matched.isEmpty()) {
                logger.error("Expected alert missing for pattern: {}", pattern);
            } else if (matched.size() != 1) {
                logger.error("Matched multiple alerts for pattern: {} => {}", pattern, matched);
            } else {
                correctlyMatched++;
            }
        }

        assertEquals(correctlyMatched, expectedAlerts.size(), "expected alerts");
        assertTrue(unexpected.isEmpty());
    }

    /**
     * Non-differential alerts test
     * @param vertex tested vertex
     * @param expectedAlerts expected alerts list
     */
    protected void validateAlerts(Vertex vertex, Set<String> expectedAlerts) {
        // Gather attached non-differential alerts
        Set<String> attached = new HashSet<String>();
        for (Alert alert : vertex.getAlerts()) { // Check for expected
            if (alert.isDifferential()) {
                continue;
            }

            String name = alert.getName();
            attached.add(name);

            if (logger.isDebugEnabled()) {
                logger.debug(name);
            }
        }
        // Verify that expected alerts are present
        Set<String> remaining = new HashSet<String>(expectedAlerts);
        remaining.removeAll(attached);
        if (!remaining.isEmpty()) {
            for (String a : remaining) {
                logger.error("Expected alert not attached: {}", a);
            }
        }
        assertTrue(remaining.isEmpty());
        // Verify that no unexpected alerts are present
        remaining = new HashSet<String>(attached);
        remaining.removeAll(expectedAlerts);
        if (!remaining.isEmpty()) {
            for (String a : remaining) {
                logger.error("Unexpected alert attached: {}", a);
            }
        }
        assertTrue(remaining.isEmpty());
    }
}
