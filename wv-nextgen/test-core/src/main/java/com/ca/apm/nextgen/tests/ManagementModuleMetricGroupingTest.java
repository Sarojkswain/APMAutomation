package com.ca.apm.nextgen.tests;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static org.testng.Assert.assertNotNull;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.common.WvConstants;
import com.ca.apm.nextgen.tests.common.WvManagementTabConstants;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.ManagementTabUtils;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * Test suite implementing tests from ALM's Management Module Metric Grouping testplan.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ManagementModuleMetricGroupingTest extends BaseWebViewTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementModuleMetricGroupingTest.class);

    public static final String[] DEFAULT_METRIC_GROUP_NAMES = { 
                                                                WvManagementTabConstants.AGENT_CONNECTION_STATUS_ALERT_NAME, 
                                                                "Agent Summary Info", 

                                                                "Backend Average Response Times",
                                                                "Backend Heuristics", 
                                                                "Backend Responses per Interval", 
                                                                "Backend Stalls",
                                                                "Bytes In Use", 

                                                                "Component Stall Count", 
                                                                "CPU Heuristics",
                                                                "CPU Utilization % (aggregate)", 
                                                                "CPU Utilization % (combined)", 
                                                                "CPU Utilization (Process)",
                               
                                                                "EJB Individual Response Time (ms)", 
                                                                "Entity EJB Response Time (ms)", 
                                                                "Errors Per Interval",
                               
                                                                "File I/O Rate (Bytes Per Second)", 
                                                                "File Input Rate", 
                                                                "File Output Rate",
                                                                "Frontend Average Response Times", 
                                                                "Frontend Errors Heuristic", 
                                                                "Frontend Heuristics",
                                                                "Frontend Response Time Heuristic", 
                                                                "Frontend Responses per Interval", 
                                                                "Frontend Stalls",
                                                                "Frontend Stalls Heuristic",
                                                                "Frontend URLs Average Response Times",

                                                                "GC Heap", 
                                                                
                                                                "Idle Servlet Thread Count", 
                               
                                                                "JDBC Available Connections", 
                                                                "JDBC Heuristic", 
                                                                "JSP Average Response Time (ms)", 
                                                                "JVM Heuristics", 

                                                                "Operating System", 
                                                                
                                                                "Servlet Average Response Time (ms)", 
                                                                "Servlet Individual Average Response Time (ms)", 
                                                                "Session EJB Response Time (ms)", 
                                                                "Socket Concurrency", 
                                                                "Socket Concurrent Readers", 
                                                                "Socket Concurrent Writers", 
                                                                "SQL Average Processing Time (ms)", 
                                                                "SQL Query Processing Time (ms)", 
                                                                "SQL Query vs. Result Processing Time (ms)", 
                                                                "SQL Update Processing Time (ms)", 
                                                                "Stalls", 

                                                                "Thread Heuristic", 
                                                                
                                                                "URL Average Response Time (ms)", 
                                                                "URL Errors Per Interval", 

                                                                "Wait for JDBC Connection Count", 
                                                                "Wait for JDBC Connection Response Time (ms)" 
    };

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "sinal04",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "ManagementModuleMetricGrouping", "MANAGEMENT_TAB"})
    @AlmId(356734)
    public void testNamesAndExistence() throws Exception {
        String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);

        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, webViewHost,
            8080, hubHost, WebViewUi.HUB_PATH, WebViewUi.HUB_PORT, prepareDesiredCapabilities());
        testRunner.testNamesAndExistence();
    }

    public static void main(String[] args) throws Exception {
        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, "tas-cz-nc0", 
            8080, "localhost", "", 9515, DesiredCapabilities.chrome());
        testRunner.testNamesAndExistence();
    }

    private static class TestRunner extends BaseTestRunner {

        /**
         * Constructor.
         *  
         * @param webViewUserName
         * @param webViewUserPassword
         * @param webViewHost
         * @param webViewPort
         * @param hubHost
         * @param hubPath
         * @param hubPort
         * @param desiredCapabilities
         */
        public TestRunner(String webViewUserName, String webViewUserPassword, String webViewHost,
            int webViewPort, String hubHost, String hubPath, int hubPort,
            DesiredCapabilities desiredCapabilities) {
            super(webViewUserName, webViewUserPassword, webViewHost, webViewPort, hubHost, hubPath, hubPort,
                desiredCapabilities);
        }
    
        /**
         * Implements ALM #356734.
         */
        public void testNamesAndExistence() {
            try (WebViewUi ui = createWebViewUi()) {
                login(ui);

                //Go to management tab.
                ui.clickManagementTab();

                //Select the TestSample management module's Metric Groupings node in the navigation tree.
                //Use the first node under the Metric Groupings node to expand it.
                ui.selectTreeNode(WvManagementTabConstants.TEST_SAMPLE_METRIC_GROUPINGS_NODE + "|" + DEFAULT_METRIC_GROUP_NAMES[0]);
                
                //Go through default metric groups and check they are found.
                for (int i = 0; i < DEFAULT_METRIC_GROUP_NAMES.length; i++) {
                    String ftid = WvManagementTabConstants.TEST_SAMPLE_METRIC_GROUPINGS_NODE + "|" + DEFAULT_METRIC_GROUP_NAMES[i];
                    WebElement node = ManagementTabUtils.clickOnNavigationTreeNode(ui, ftid);
                    assertNotNull(node);
                }
            } catch (Throwable e) {
                throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, 
                    "'Check Metric Group names and existence' test failed. Exception: {0}");
            }
        }
    }
}
