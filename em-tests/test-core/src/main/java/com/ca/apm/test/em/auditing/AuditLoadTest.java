package com.ca.apm.test.em.auditing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import com.ca.apm.testbed.auditing.SimpleAuditEmTestbed;
import com.ca.apm.tests.flow.CsvToXlsFlow;
import com.ca.apm.tests.flow.CsvToXlsFlowContext;
import com.ca.apm.tests.role.CsvToXlsRole;
import com.ca.apm.tests.role.PerfMonitorRole;
import com.ca.apm.tests.role.WurlitzerRole;
import com.ca.apm.tests.testbed.ClusterRegressionTestBed;
import com.ca.tas.role.HammondRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import static com.ca.apm.nextgen.tests.common.ManagementElementType.NEW_SUMMARY_ALERT;
import static com.ca.apm.nextgen.tests.common.ManagementElementType.NEW_CONSOLE_NOTIFICATION_ACTION;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.AQUARIUS_SUMMARY_ALERT_NAME;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.SUPER_DOMAIN_NODE;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.ALERT_NODE_TEMPLATE;
import static com.ca.apm.testbed.auditing.SimpleAuditEmTestbed.SELENIUM_HUB_ROLE_ID;
import static com.ca.apm.testbed.auditing.SimpleAuditEmTestbed.SELENIUM_HUB_MACHINE_ID;
import static com.ca.apm.testbed.auditing.SimpleAuditEmTestbed.EM_ROLE_ID;

import com.ca.apm.nextgen.tests.common.ManagementElementType;
import com.ca.apm.nextgen.tests.helpers.ManagementTabUtils;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
//import com.ca.apm.test.atc.common.*;
//import com.ca.apm.test.atc.common.AttributeRulesTable.Operator;
import com.google.common.collect.Maps;

/**
 * @author filja01
 */
public class AuditLoadTest extends TasTestNgTest {
    private final Logger log = LoggerFactory.getLogger(AuditLoadTest.class);
    private volatile boolean stop = false;
    
    public static final Integer WEBVIEW_PORT = 8082;
    
    public static final long TEST_DURATION = 200*60*1000L;//200m
    public static final int NUMBER_OF_BROWSERS = 100;
    
    public static final String MANAGEMENT_MODULE_NAME = "Default";
    public static final String AQUARIUS_METRIC_GROUPING_NAME = "Aquarius Metric Grouping";
    public static final String AQUARIUS_CONSOLE_NOTIFICATION_ACTION_NAME = "Aquarius Console Notification Action";
    public static final String ACTIONS_NODE_TEMPLATE = "management-tree_*SuperDomain*|Management Modules|%s|Actions|%s";
    /*
    private static final String UNIVERSE_GUEST = "universe_guest";
    
    private static final String NEW_ATTRIBUTE_NAME = "e2e_attr_rules_test";
    private static final String NEW_ATTRIBUTE_VALUE = "value";
    private static final String EXISTING_ATTRIBUTE_NAME = "type";
    private static final Operator OPERATOR = Operator.DOESNT_EQUAL;
    private static final String CONDITION_VALUE = "EXTERNAL";
    */
    @Test(groups = {"windows"})
    @Tas(testBeds = @TestBed(name = SimpleAuditEmTestbed.class, executeOn = SELENIUM_HUB_MACHINE_ID),
        owner = "filja01", size = SizeType.HUMONGOUS)
    public void test() {
        
        startPerfMon();
        startHammondLoad();
        startWurlitzerLoad();
        
        sleep(90000L); //90 sec wait
        
        final String hubHost = envProperties.getMachineHostnameByRoleId(SELENIUM_HUB_ROLE_ID);
        final String webviewUrl = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
        
        ArrayList<Thread> threads = new ArrayList<>();
        
        //open browser and run repeat scripts
        for (int i = 0; i < NUMBER_OF_BROWSERS; i++) {
            final int c = i;
            final String threadName = "CH_"+c;
            Thread thCH = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Run {} thread", threadName);
                    WebViewUi ui = null;
                    int i = 0;
                    while (true) {
                        ui = null;
                        try {
                            ui = WebViewUi.create(WebViewUi.HUB_PROTOCOL, hubHost, 4444, "/wd/hub", DesiredCapabilities.chrome());
                            //driver = new RemoteWebDriver(new URL(hubHostUrl), DesiredCapabilities.chrome());
                        } catch (Exception ex) {
                            log.error("Malformed HUB URL: {}", ex);
                        }
                        if (ui != null) {
                            try {
                                if (i%2==0) {
                                    log.info("Run {} thread with SUMMARY ALERT", threadName);
                                    runTestWebviewAudit(ui, webviewUrl, WEBVIEW_PORT, threadName, AQUARIUS_SUMMARY_ALERT_NAME,
                                        NEW_SUMMARY_ALERT, ALERT_NODE_TEMPLATE);
                                }
                                else {
                                    log.info("Run {} thread with NOTIFICATION ACTION", threadName);
                                    runTestWebviewAudit(ui, webviewUrl, WEBVIEW_PORT, threadName, AQUARIUS_CONSOLE_NOTIFICATION_ACTION_NAME,
                                        NEW_CONSOLE_NOTIFICATION_ACTION, ACTIONS_NODE_TEMPLATE);
                                }
                            } catch (Exception e) {
                                log.error("TIME OUT error: {}", e);
                            }
                            try {
                                ui.close();
                            } catch (Exception e) {
                                log.error("Close connection ERROR: {}", e);
                            }
                        }
                        i++;
                        if (stop) {
                            break;
                        }
                        sleep(3000L);//run script every 3 seconds
                        if (stop) {
                            break;
                        }
                    }
                }
            });
            thCH.start();
            threads.add(thCH);
            
            sleep(7000L);//start threads every 3s
        }
        
        sleep(TEST_DURATION);
        stop = true;
        log.info("Wait for test end");
        sleep(30000L); //wait 30sec for closing of threads
        
        getPerfLogFiles();
        
        log.info("Test end");
    }
    
    @AfterTest
    public void stopProcesses() throws Exception {
        stopPerfMon();
        stopHammondLoad();
        stopWurlitzerLoad();
        
    }
    
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private void runTestWebviewAudit(WebViewUi ui, String host, int port, String threadName, String name, ManagementElementType type, String NodeTemplate) {
        log.info("{}: Run Audit test", threadName);
        //driver.manage().window().maximize(); // Always maximize on windows
        //sleep(1000);
        ui.login(host, port, "cemadmin", "quality");
        sleep(1000);
        /*ui.clickManagementTab();
        
        //CREATE SUMMARY ALERT
        String createdName = ManagementTabUtils.createNewManagementElement(ui, 
            name+threadName, 
            MANAGEMENT_MODULE_NAME,
            type, true, false);
        if (createdName != null && !createdName.isEmpty()) {
            log.info("{}: Created", threadName);
        }
        sleep(1000);
        //Select *SuperDomain* node in the tree.
        ui.selectTreeNode(SUPER_DOMAIN_NODE);
        
        //Navigate back to the created summary alert node.
        final String newNodeId = String.format(NodeTemplate, 
            MANAGEMENT_MODULE_NAME, createdName);

        //First click Delete the node but choose No. 
        //ManagementTabUtils.deleteManagementElement(ui, newSummaryAlertNodeId, false);

        //This time choose Yes to remove it.
        ManagementTabUtils.deleteManagementElement(ui, newNodeId, true);
        
        String nodeIdXpath = ManagementTabUtils.getTreeNodeIdXpath(newNodeId);
        By byNodeIdXpath = By.xpath(nodeIdXpath);
        WebElement nodeElement = ui.getWebElementOrNull(byNodeIdXpath);
        if (nodeElement != null) {
            ui.waitFor(ExpectedConditions.stalenessOf(nodeElement));    
        }
        
        nodeElement = ui.getWebElementOrNull(byNodeIdXpath);
        //Check that it has disappeared.
        if (nodeElement == null) {
            log.info("{}: Deleted", threadName);
        }
        */
        ui.logout();   
    }
    /*
    private void runTestATCUniverseAudit(UI ui, String host, int port, String threadName) {
        log.info("{}: Run ATC Audit Universe test", threadName);
        
        sleep(2000);
        ui.setStartUrl(host+":"+port+"//ApmServer");
        try {
            ui.login("cemadmin", "quality");
            ui.getLeftNavigationPanel().goToUniverses();
            UniverseSettings univSettings = ui.getUniverseSettings();
            univSettings.createUniverse(UNIVERSE_GUEST);
            univSettings.deleteUniverse(UNIVERSE_GUEST);
            
            ui.logout();   
        } catch (Exception e) {
            log.error("ERROR: {}", e);
        }
    }
    
    private void runTestATCAttributeAudit(UI ui, String host, int port, String threadName) {
        log.info("{}: Run ATC Audit Attribute test", threadName);
        
        sleep(2000);
        ui.setStartUrl(host+":"+port+"//ApmServer");
        try {
            ui.login("cemadmin", "quality");
            ui.getLeftNavigationPanel().goToDecorationPolicies();
            AttributeRulesTable attrRulesTable = ui.getAttributeRulesTable();
            attrRulesTable.createRow(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);
            attrRulesTable.removeRowsIfExist(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);
            
            ui.logout();   
        } catch (Exception e) {
            log.error("ERROR: {}", e);
        }
    }
    */
    protected void startPerfMon() {
        try {
            runSerializedCommandFlowFromRole(SimpleAuditEmTestbed.PERFMON_MOM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_START);
        } catch (Exception e) {
            runSerializedCommandFlowFromRole(SimpleAuditEmTestbed.PERFMON_MOM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_STOP);
            runSerializedCommandFlowFromRole(SimpleAuditEmTestbed.PERFMON_MOM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_START);
        }
    }
    
    protected void stopPerfMon() {
        runSerializedCommandFlowFromRole(SimpleAuditEmTestbed.PERFMON_MOM_ROLE_ID,
            PerfMonitorRole.ENV_PERF_MONITOR_STOP);
    }
    
    protected void startWurlitzerLoad() {
        for (final String id : getSerializedIds(SimpleAuditEmTestbed.WURLITZER_ROLE_ID,
            WurlitzerRole.ENV_RUN_WURLITZER)) {
            new Thread() {
                public void run() {
                    runSerializedCommandFlowFromRole(SimpleAuditEmTestbed.WURLITZER_ROLE_ID, id);
                }
            }.start();
        }
    }
    
    protected void stopWurlitzerLoad() {
        for (String id : getSerializedIds(SimpleAuditEmTestbed.WURLITZER_ROLE_ID,
            WurlitzerRole.ENV_STOP_WURLITZER)) {
            try {
                runSerializedCommandFlowFromRole(SimpleAuditEmTestbed.WURLITZER_ROLE_ID, id);
            } catch (Exception e) {
            }
        }
    }
    
    protected void startHammondLoad() {
        for (final String id : getSerializedIds(SimpleAuditEmTestbed.HAMMOND_ROLE_ID,
            HammondRole.ENV_HAMMOND_START)) {
            new Thread() {
                public void run() {
                    runSerializedCommandFlowFromRole(SimpleAuditEmTestbed.HAMMOND_ROLE_ID, id);
                }
            }.start();
        }
    }

    protected void stopHammondLoad() {
        for (String id : getSerializedIds(SimpleAuditEmTestbed.HAMMOND_ROLE_ID,
                HammondRole.ENV_HAMMOND_STOP)) {
            try {
                runSerializedCommandFlowFromRole(SimpleAuditEmTestbed.HAMMOND_ROLE_ID, id);
            } catch (Exception e) {
            }
        }
    }
    
    private Iterable<String> getSerializedIds(String roleId, String prefix) {
        Map<String, String> roleProperties =
            Maps.fromProperties(envProperties.getRolePropertiesById(roleId));

        HashSet<String> startIds = new HashSet<>();
        for (String key : roleProperties.keySet()) {
            if (key.startsWith(prefix)) {
                startIds.add(key.split("::")[0]);
            }
        }
        return startIds;
    }
    
    protected void getPerfLogFiles() {
        try {
            runSerializedCommandFlowFromRole(SimpleAuditEmTestbed.PERFMON_MOM_ROLE_ID,
                PerfMonitorRole.ENV_GET_PERF_LOG);
        } catch (Exception e) {}

        CsvToXlsFlowContext context =
            (CsvToXlsFlowContext) deserializeFlowContextFromRole(
                SimpleAuditEmTestbed.CSV_TO_XLS_ROLE_ID, CsvToXlsRole.RUN_CSV_TO_XLS,
                CsvToXlsFlowContext.class);
        runFlowByMachineId(SimpleAuditEmTestbed.MACHINE_ID, CsvToXlsFlow.class, context);
    }
}
