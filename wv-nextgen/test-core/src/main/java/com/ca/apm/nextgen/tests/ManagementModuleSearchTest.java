package com.ca.apm.nextgen.tests;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static com.ca.apm.nextgen.tests.common.ManagementElementType.NEW_CONSOLE_NOTIFICATION_ACTION;
import static com.ca.apm.nextgen.tests.common.ManagementElementType.NEW_SUMMARY_ALERT;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.ACTION_NODE_TEMPLATE;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.ALERT_NODE_TEMPLATE;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.BOGEY1_ACTION_NAME;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.BOO1_ACTION_NAME;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_SEARCH_GRID_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.SUPER_DOMAIN_NODE;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.TEST1MM_MANAGEMENT_MODULE_NAME;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.TEST2MM_MANAGEMENT_MODULE_NAME;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.TEST_ASTERISK_MM_MODULE_NAME_PATTERN;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.common.WvConstants;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.ManagementTabUtils;
import com.ca.apm.nextgen.tests.helpers.TableUi;
import com.ca.apm.nextgen.tests.helpers.TableUi.TableRowRecord;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * Test suite implementing tests from ALM's Management Module Search testplan.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 * 
 */
public class ManagementModuleSearchTest extends BaseWebViewTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementModuleSearchTest.class);

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "sinal04",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "ManagementModuleSearch", "MANAGEMENT_TAB"})
    @AlmId(356595)
    public void testSearchModuleName() throws Exception {
        String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);

        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, webViewHost,
            8080, hubHost, WebViewUi.HUB_PATH, WebViewUi.HUB_PORT, prepareDesiredCapabilities());
        testRunner.testSearchModuleName();
    }

    public static void main(String[] args) throws Exception {
        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, "tas-cz-nc0", 
            8080, "localhost", "", 9515, DesiredCapabilities.chrome());
        testRunner.testSearchModuleName();
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
         * Implements ALM #356595.
         */
        public void testSearchModuleName() {
            String createdActionName = null;
            String createdAlertName = null;
            WebViewUi ui = null;
            try  {
                ui = createWebViewUi();
                login(ui);

                //Go to management tab.
                ui.clickManagementTab();

                //Create a test console action.
                createdActionName = ManagementTabUtils.createNewManagementElement(ui, 
                    BOO1_ACTION_NAME, 
                    TEST1MM_MANAGEMENT_MODULE_NAME,
                    NEW_CONSOLE_NOTIFICATION_ACTION, 
                    true, false);

                assertNotNull(createdActionName);

                LOGGER.info("Created a new console action named: {}", createdActionName);
                assertTrue(!createdActionName.isEmpty());

                //Select *SuperDomain* node in the tree.
                ManagementTabUtils.clickOnNavigationTreeNode(ui, SUPER_DOMAIN_NODE);

                //Create a test summary alert.
                createdAlertName = ManagementTabUtils.createNewManagementElement(ui, 
                    BOGEY1_ACTION_NAME, 
                    TEST2MM_MANAGEMENT_MODULE_NAME,
                    NEW_SUMMARY_ALERT, 
                    true, false);

                assertNotNull(createdAlertName);
                
                LOGGER.info("Created a new summary alert named: {}", createdAlertName);
                assertTrue(!createdAlertName.isEmpty());

                //Select *SuperDomain* node in the tree.
                ManagementTabUtils.clickOnNavigationTreeNode(ui, SUPER_DOMAIN_NODE);

                WebElement searchContentPanel = ManagementTabUtils.runSearch(ui, TEST_ASTERISK_MM_MODULE_NAME_PATTERN);
                
                WebElement tableContainer = ui.getWebElement(searchContentPanel, By.id(MM_SEARCH_GRID_ID));
                
                List<WebElement> tables = ui.getWebElements(tableContainer, By.xpath("./div/div/table"));
                
                assertNotNull(tables);
                assertTrue(tables.size() > 1);
                
                TableUi tableUi = new TableUi(ui, tables.get(0), tables.get(1));

                boolean foundTestAction = false;
                boolean foundTestAlert = false;
                for (TableRowRecord rowRec : tableUi) {
                    List<String> rowValues = rowRec.getValues();
                    String elementName = rowValues.get(1).trim();
                    String moduleName = rowValues.get(2).trim();
                    if (elementName.equals(createdAlertName) && TEST2MM_MANAGEMENT_MODULE_NAME.equals(moduleName)) {
                        foundTestAlert = true;
                        LOGGER.info("Found alert named '{}' in search results!", elementName);
                    } else if (elementName.equals(createdActionName) && TEST1MM_MANAGEMENT_MODULE_NAME.equals(moduleName)) {
                        foundTestAction = true;
                        LOGGER.info("Found action named '{}' in search results!", elementName);
                    }
                    if (foundTestAction && foundTestAlert) {
                        break;
                    }
                }

                assertTrue(foundTestAlert, String.format("Alert '%s' element not found!", createdAlertName));
                assertTrue(foundTestAction, String.format("Action '%s' element not found!", createdActionName));
                
                searchContentPanel = ManagementTabUtils.runSearch(ui, TEST1MM_MANAGEMENT_MODULE_NAME);
                tableContainer = ui.getWebElement(searchContentPanel, By.id(MM_SEARCH_GRID_ID));
                
                tables = ui.getWebElements(tableContainer, By.xpath("./div/div/table"));
                
                assertNotNull(tables);
                assertTrue(tables.size() > 1);
                
                tableUi = new TableUi(ui, tables.get(0), tables.get(1));

                foundTestAction = false;
                foundTestAlert = false;
                for (TableRowRecord rowRec : tableUi) {
                    List<String> rowValues = rowRec.getValues();
                    String elementName = rowValues.get(1).trim();
                    String moduleName = rowValues.get(2).trim();
                    if (elementName.equals(createdAlertName) && TEST2MM_MANAGEMENT_MODULE_NAME.equals(moduleName)) {
                        foundTestAlert = true;
                        LOGGER.info("Found alert named '{}' in search results!", elementName);
                    } else if (elementName.equals(createdActionName) && TEST1MM_MANAGEMENT_MODULE_NAME.equals(moduleName)) {
                        foundTestAction = true;
                        LOGGER.info("Found action named '{}' in search results!", elementName);
                    }
                    if (foundTestAction && foundTestAlert) {
                        //Though we do not expect the test alert to be shown up in the search results we need to keep on going through the list
                        //to make sure it's not found there. But if it was found, break and report an error.
                        break;
                    }
                }
                
                assertTrue(foundTestAction, String.format("Action '%s' element not found!", createdActionName));
                assertFalse(foundTestAlert, String.format("Alert '%s' element was found though it should have been not!", 
                    createdAlertName));
                
                ui.enablePolling(true);
            } catch (Throwable e) {
                throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, 
                    "'Search by module name' test failed. Exception: {0}");
            } finally {
                if (createdAlertName != null) {
                    LOGGER.info("CLEAN UP: deleting '{}' element..", createdAlertName);
                    String alertNodeId = String.format(ALERT_NODE_TEMPLATE, 
                        TEST2MM_MANAGEMENT_MODULE_NAME, createdAlertName);
                    try {
                        ManagementTabUtils.deleteManagementElement(ui, alertNodeId, true);
                    } catch (Exception e) {
                        LOGGER.error(String.format("Failed to remove alert named '%s'", createdAlertName), e);
                    }
                }

                if (createdActionName != null) {
                    LOGGER.info("CLEAN UP: deleting '{}' element..", createdActionName);
                    String actionNodeId = String.format(ACTION_NODE_TEMPLATE, 
                        TEST1MM_MANAGEMENT_MODULE_NAME, createdActionName);
                    try {
                        ManagementTabUtils.deleteManagementElement(ui, actionNodeId, true);
                    } catch (Exception e) {
                        LOGGER.error(String.format("Failed to remove action named '%s'", createdActionName), e);
                    }
                }
                
                if (ui != null) {
                    try {
                        ui.close();
                    } catch (Exception e) {
                        LOGGER.error("Failed to close WebViewUi driver: ", e);
                    }
                }
            }
        }
    }
}
