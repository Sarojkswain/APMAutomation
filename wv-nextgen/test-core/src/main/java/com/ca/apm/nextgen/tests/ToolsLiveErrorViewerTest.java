/**
 * 
 */
package com.ca.apm.nextgen.tests;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.TRADE_SERVICE_ROLE;
import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.TOMCAT_ROLE;
import static com.ca.apm.nextgen.tests.common.WvConstants.COMMON_COLUMN_CELL_CLASS_NAME;
import static com.ca.apm.nextgen.tests.common.WvToolsTabConstants.LIVE_ERROR_VIEWER_GRID_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.common.WvConstants;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.TableUi;
import com.ca.apm.nextgen.tests.helpers.ToolsTabUtils;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * Test suite implementing tests from ALM's Tools Live Error Viewer test plan. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ToolsLiveErrorViewerTest extends BaseWebViewTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsLiveErrorViewerTest.class);
    private static final String LIVE_ERROR_VIEWER_COLUMN_CELL_XPATH_TEMPLATE = "//div[@id='%s']//div[contains(@class, '%s')]";
    
    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "sinal04",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "LiveErrorViewer", "TOOLS_TAB"})
    @AlmId(351873)
    public void testCheckNumberOfErrorsInStatusBar() throws Exception {
        String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);

        String webAppUrl = envProperties.getRolePropertyById(TOMCAT_ROLE, TRADE_SERVICE_ROLE + "_url");
        
        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, webViewHost,
            8080, hubHost, WebViewUi.HUB_PATH, WebViewUi.HUB_PORT, prepareDesiredCapabilities(), webAppUrl);
        testRunner.testCheckNumberOfErrorsInStatusBar();
    }

    public static void main(String[] args) throws Exception {
        String hostname = "tas-cz-nbd";
        String webAppUrl = "http://" + hostname + ":7080/TradeService";
        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, hostname, 
            8080, "localhost", "", 9515, DesiredCapabilities.chrome(), webAppUrl);
        testRunner.testCheckNumberOfErrorsInStatusBar();
    }

    private static class TestRunner extends BaseTestRunner {

        private String webAppUrl;
        
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
         * @param webAppUrl                URL of the deployed TradeService web app 
         */
        public TestRunner(String webViewUserName, String webViewUserPassword, String webViewHost,
            int webViewPort, String hubHost, String hubPath, int hubPort,
            DesiredCapabilities desiredCapabilities, 
            String webAppUrl) {
            super(webViewUserName, webViewUserPassword, webViewHost, webViewPort, hubHost, hubPath, hubPort,
                desiredCapabilities);
            this.webAppUrl = webAppUrl;
        }
        
        /**
         * Implements ALM #351873 test case.
         */
        public void testCheckNumberOfErrorsInStatusBar() {
            try (WebViewUi ui = createWebViewUi()) {
                //Log into WebView.
                login(ui);

                //Go to Tools tab.
                ui.clickToolsTab();
                
                ToolsTabUtils toolsTabUtils = new ToolsTabUtils(ui);
                toolsTabUtils.clickLiveErrorViewerTab();
                
                HttpClient httpClient = HttpClientBuilder.create().build();
                
                //Non-existent resource URL.
                HttpGet getRequest = new HttpGet(webAppUrl + "/error.jpg");
                httpClient.execute(getRequest);
                
                //Wait for error data to appear in the table.
                String errorCellXpath = String.format(LIVE_ERROR_VIEWER_COLUMN_CELL_XPATH_TEMPLATE, 
                    LIVE_ERROR_VIEWER_GRID_ID, 
                    COMMON_COLUMN_CELL_CLASS_NAME);
                ui.waitForWebElement(By.xpath(errorCellXpath), 120, TimeUnit.SECONDS);

                TableUi tableEl = toolsTabUtils.getLiveErrorsTable();  

                int numOfRows = tableEl.rowCount();
                LOGGER.info("Number of live error rows found: {}", numOfRows);
                
                WebElement statusBarNumSpan = toolsTabUtils.getNumOfLiveErrorsFoundStatusBarElement();
                
                assertNotNull(statusBarNumSpan, "'Nummber of events found' element not found in the status bar!");
                
                String numOfEventsFound = statusBarNumSpan.getText();
                String[] numOfEventsSplitArray = StringUtils.split(numOfEventsFound);
                
                assertNotNull(numOfEventsSplitArray);
                assertEquals(numOfEventsSplitArray.length, 3);
                
                int statusBarNumOfEvents = Integer.parseInt(numOfEventsSplitArray[0]);
                
                LOGGER.info("Status bar: number of events found = {}", statusBarNumOfEvents);
                
                assertEquals(numOfRows, statusBarNumOfEvents, 
                    "Number of live errors found in the status bar does not match the number of live error events found in the table!");

                WebElement statusBarTimestampElem = toolsTabUtils.getLiveErrorsStatusBarTimestampElement();
                assertNotNull(statusBarTimestampElem);
                
                String timestampText = statusBarTimestampElem.getText();
                LOGGER.info("Status bar: timestamp = '{}'", timestampText);
                
                assertNotNull(timestampText);
                assertNotEquals(timestampText.trim(), "");
                
            } catch (Throwable e) {
                throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, 
                    "'Live errors number check' test failed. Exception: {0}");
            }
        }
    }
    
}
