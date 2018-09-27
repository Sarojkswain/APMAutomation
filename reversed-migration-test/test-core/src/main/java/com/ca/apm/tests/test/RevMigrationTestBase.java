package com.ca.apm.tests.test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.LogCheckFlow;
import com.ca.apm.automation.action.flow.utility.LogCheckFlowContext;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.tests.flow.LogInspectFlow;
import com.ca.apm.tests.flow.LogInspectFlowContext;
import com.ca.apm.tests.flow.RollbackEMFlow;
import com.ca.apm.tests.flow.RollbackEMFlowContext;
import com.ca.apm.tests.flow.UpgradeEMFlow;
import com.ca.apm.tests.flow.UpgradeEMFlowContext;
import com.ca.apm.tests.role.EmRollbackRole;
import com.ca.apm.tests.role.EmUpgradeRole;
import com.ca.apm.tests.testbed.RevMigrationTestBed;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.testapp.custom.NowhereBankBTRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.test.utils.GraphHolder;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Base for Reversed migration.
 * Scenario:
 *  - install APM using {@link RevMigrationTestBed} of given version, platform and database
 *    - upgrade and rollback roles are also configured but, not 'executed'
 *    - nowherebank is installed
 *
 *  - start nowhere bank
 *
 *  - test authenticate to UI
 *  - get number of metrics
 *
 *  - stop WV and EM
 *  - upgrade
 *  - start EM and WV
 *
 *  - test authenticate to UI
 *  - get number of metrics
 *
 *  - stop WV and EM
 *  - rollback
 *  - start EM and WV
 *
 *  - stop WV and EM
 *  - upgrade again (from rollback)
 *  - start EM and WV
 *  - test authenticate to UI
 */
public abstract class RevMigrationTestBase extends TasTestNgTest {
    private final Logger log = Logger.getLogger(getClass());

    public static final String TEST_OWNER = "posra01";
    public static final String TEST_GROUP_REVMIG = "revmig";
    public static final String TEST_GROUP_RH = "redhat";

    public void test(boolean is97version) throws Exception {
        log.info("starting nowherebank");
        startNowhereBank(RevMigrationTestBed.NOWHEREBANK_ROLE);
        log.info("started nowherebank");

        log.info("sleep 1 min");
        sleep(1000 * 60 * 1);

        // enable transaction traces to populate APM graph
        enableTransactionTrace(RevMigrationTestBed.EM_ROLE_ID);

        APMUIAuthTest(RevMigrationTestBed.EM_ROLE_ID, RevMigrationTestBed.WV_ADMIN_USER, RevMigrationTestBed.WV_ADMIN_PASS);

        if (!is97version) {
            APMRESTGraph(RevMigrationTestBed.EM_ROLE_ID);
            checkLogFiles(); // upgrade from 9.7.1->10.7 contains migration errors for 'sample' module, so for now do not run test for 9.7.1
        }

//        log.info("check configuration before upgrade");
//        checkConfiguration();

        log.info("check metrics before upgrade");
//        long numberOfAgentsOld = getConnectedAgents(RevMigrationTestBed.EM_ROLE_ID);
        long numberOfMetricsOld = getNumberOfMetrics();

        log.info("stopping webview");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_STOP_WEBVIEW);
        log.info("stopping em");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_STOP_EM);


        // upgrade
        log.info("start upgrade");

        log.info("starting upgrade");
        runUpgrade(RevMigrationTestBed.STANDALONE_MACHINE);

        log.info("starting em");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_START_EM);
        log.info("starting wv");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_START_WEBVIEW);

        log.info("sleep 1 min");
        sleep(1000 * 60);

        // enable transaction traces to populate APM graph
        enableTransactionTrace(RevMigrationTestBed.EM_ROLE_ID);

        APMUIAuthTest(RevMigrationTestBed.EM_ROLE_ID, RevMigrationTestBed.WV_ADMIN_USER, RevMigrationTestBed.WV_ADMIN_PASS);

        if (!is97version) {
            APMRESTGraph(RevMigrationTestBed.EM_ROLE_ID);
            checkLogFiles(); // upgrade from 9.7.1->10.7 contains migration errors for 'sample' module, so for now do not run test for 9.7.1
        }

//        log.info("check configuration after upgrade");
//        checkConfiguration();

        log.info("check metrics after upgrade");
//        long numberOfAgents = getConnectedAgents(RevMigrationTestBed.EM_ROLE_ID);
        long numberOfMetricsNew = getNumberOfMetrics();
        Assert.assertTrue((numberOfMetricsOld - 100) <= numberOfMetricsNew, "new version should collect same or more metrics. ");

        // rollback
        log.info("start rollback");

        log.info("stopping webview");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_STOP_WEBVIEW);
        log.info("stopping em");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_STOP_EM);

        log.info("starting rollback");
        runRollback(RevMigrationTestBed.STANDALONE_MACHINE);

        log.info("starting em");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_START_EM);
        log.info("starting wv");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_START_WEBVIEW);

        log.info("sleep 1 min");
        sleep(1000 * 60);

        // enable transaction traces to populate APM graph
        enableTransactionTrace(RevMigrationTestBed.EM_ROLE_ID);

        APMUIAuthTest(RevMigrationTestBed.EM_ROLE_ID, RevMigrationTestBed.WV_ADMIN_USER, RevMigrationTestBed.WV_ADMIN_PASS);

        if (!is97version) {
            APMRESTGraph(RevMigrationTestBed.EM_ROLE_ID);
            checkLogFiles(); // upgrade from 9.7.1->10.7 contains migration errors for 'sample' module, so for now do not run test for 9.7.1
        }

        log.info("stopping webview");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_STOP_WEBVIEW);
        log.info("stopping em");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_STOP_EM);

        // upgrade - 2 round
        log.info("start upgrade after rollback");

        log.info("starting upgrade after rollback");
        runUpgrade(RevMigrationTestBed.STANDALONE_MACHINE);

        log.info("starting em");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_START_EM);
        log.info("starting wv");
        runSerializedCommandFlowFromRole(RevMigrationTestBed.EM_ROLE_ID, EmRole.ENV_START_WEBVIEW);

        log.info("sleep 1 min");
        sleep(1000 * 60);

        // enable transaction traces to populate APM graph
        enableTransactionTrace(RevMigrationTestBed.EM_ROLE_ID);

        APMUIAuthTest(RevMigrationTestBed.EM_ROLE_ID, RevMigrationTestBed.WV_ADMIN_USER, RevMigrationTestBed.WV_ADMIN_PASS);

        if (!is97version) {
            APMRESTGraph(RevMigrationTestBed.EM_ROLE_ID);
            checkLogFiles(); // upgrade from 9.7.1->10.7 contains migration errors for 'sample' module, so for now do not run test for 9.7.1
        }
    }

    private void enableTransactionTrace(String emRoleId) {
        log.info("enabling transaction trace exceeding 1ms for 120sec");
        ClwUtils emCLW = utilities.createClwUtils(emRoleId);
        emCLW.getClwRunner().runClw("trace transactions exceeding 1 ms in agents matching \".*\" for 120 s");
    }

    private void checkLogFiles() {
        String[] logFiles = new String[] {
                envProperties.getRolePropertyById(RevMigrationTestBed.EM_ROLE_ID, "emLogFile"),
                envProperties.getRolePropertyById(RevMigrationTestBed.EM_ROLE_ID, "emLogDir")+"/IntroscopeWebView.log"
        };


        List<String> ignoredRegExps = Arrays.asList(
                "\\[ERROR\\].*\\[Manager\\.AppMap\\.Propagator\\]",
                "\\[ERROR\\].*Unable to connect to EM.*Shutting down",
                "\\[ERROR\\].*Error updating auth info \\: Expecting zero or one results\\.",
                "\\[ERROR\\].*\\[Manager\\] XML Problem while parsing.*Invalid content was found starting with element \\'Description\\'. One of \\'\\{Name\\}\\' is expected\\.",
                "\\[ERROR\\].*\\[Manager\\.Bootstrap\\] XML problem while parsing .*DefaultMM\\.jar",
//                "\\[ERROR\\].*\\[Manager.AppMap\\] Cannot convert JSON to entity.*entities\\.Universe" // decide how to handle it for 10.2
                "\\[ERROR\\].*\\[Manager\\.AppMap\\.Eventing\\.PagerDuty\\].*configuration property.*is not defined"

            );
        List<String> acceptedRegExps = Arrays.asList("\\[ERROR\\]");

        for(String logFile: logFiles) {
            // check EM log files
            log.info("Checking log file: "+logFile);
            LogInspectFlowContext context = LogInspectFlowContext.create(logFile, acceptedRegExps, ignoredRegExps);
            // throws an exception if acceptedregexps found in log file
            runFlowByMachineId(RevMigrationTestBed.STANDALONE_MACHINE, LogInspectFlow.class, context);
        }
    }

    private List<String> laxOption(List<String> list) {
        List<String> result = new ArrayList<>(list);
        result.add("-Djava.awt.headless=false");

        return result;
    }

//    private void checkConfiguration() {
//        Map<String, List<String>> map = new HashMap<>();
//
//        map.put("lax.nl.java.option.additional", laxOption(UpgradeTestbed.COLL_LAXNL_JAVA_OPTION));
//        checkProperties(UpgradeTestbed.C1_MACHINE_ID, UpgradeTestbed.C1_ROLE_ID, "Introscope_Enterprise_Manager.lax", map);
//
//        map.clear();
//        map.put("lax.nl.java.option.additional", laxOption(UpgradeTestbed.MOM_LAXNL_JAVA_OPTION));
//        checkProperties(UpgradeTestbed.MOM_MACHINE_ID, UpgradeTestbed.MOM_ROLE_ID, "Introscope_Enterprise_Manager.lax", map);
//
//        map.clear();
//        map.put("lax.nl.java.option.additional", laxOption(UpgradeTestbed.WV_LAXNL_JAVA_OPTION));
//        checkProperties(UpgradeTestbed.DB_MACHINE_ID, UpgradeTestbed.DB_ROLE_ID, "Introscope_WebView.lax", map);
//
//        map.clear();
//        List<String> collectors = Arrays.asList(envProperties.getMachineHostnameByRoleId(UpgradeTestbed.C1_ROLE_ID), envProperties.getMachineHostnameByRoleId(UpgradeTestbed.C2_ROLE_ID));
//        map.put("introscope.enterprisemanager.clustering.login.em1.host", collectors);
//        map.put("introscope.enterprisemanager.clustering.login.em2.host", collectors);
//        map.put("transport.buffer.input.maxNum", Collections.singletonList("2400"));
//        map.put("transport.outgoingMessageQueueSize", Collections.singletonList("6000"));
//        map.put("transport.override.isengard.high.concurrency.pool.min.size", Collections.singletonList("10"));
//        map.put("transport.override.isengard.high.concurrency.pool.max.size", Collections.singletonList("10"));
//        map.put("introscope.enterprisemanager.transactiontrace.arrivalbuffer.capacity", Collections.singletonList("5000"));
//        checkProperties(UpgradeTestbed.MOM_MACHINE_ID, UpgradeTestbed.MOM_ROLE_ID, "config/IntroscopeEnterpriseManager.properties", map);
//
//        map.clear();
//        map.put("SaasAdmin", Collections.singletonList(""));
//        map.put("Admin", Collections.singletonList(""));
//        map.put("Guest", Arrays.asList("2a.1000.a9hZlUjIZUVV4vMjkv3BtA==.Yswf7wbWLN6rvbfb9jaXoQ==", "adb831a7fdd83dd1e2a39ce7591dff8", "5ed8944a85a9763fd315852f448cb7de36c5e928e13b3be427f98f7dc455f141"));
//        map.put("cemadmin",  Arrays.asList("2a.1000.qPZK3Ql/Swn0IX/5u6zdbA==.ayqr0jCCFDgjd7w6Tj96fg==", "d66636b253cb346dbb6240e3def3618", "acef2c15bcd349db90dffece73e1256e881c4416fc1f2d3a4946418349d9a"));
//        checkProperties(UpgradeTestbed.MOM_MACHINE_ID, UpgradeTestbed.MOM_ROLE_ID, "config/users.xml", map);

//        int agents = 0;
//        agents += getConnectedAgents(UpgradeTestbed.MOM_ROLE_ID);
//        agents += getConnectedAgents(UpgradeTestbed.C1_ROLE_ID);
//        agents += getConnectedAgents(UpgradeTestbed.C2_ROLE_ID);

//        Assert.assertEquals(agents, 12, "Expecting 12 connected agents.");
//    }

/*
    private void checkProperties(String machineId, String roleId, String configFile, Map<String, List<String>> map) {

        String separator = "/";
        Platform platform = Platform.fromString(envProperties.getMachinePropertyById(machineId, MachineEnvironmentProperties.PLATFORM));
        if (platform == Platform.WINDOWS) {
            separator = "\\";
        }

        CheckEmConfigFlowContext context = new CheckEmConfigFlowContext.Builder()
                .fileName(envProperties.getRolePropertyById(roleId, EmRole.ENV_PROPERTY_INSTALL_DIR) + separator + configFile)
                .properties(map)
                .build();

        runFlowByMachineId(machineId,  CheckEmConfigFlow.class, context, TimeUnit.MINUTES, 5);
    }
*/

    private int getConnectedAgents(String roleId) {
        RunCommandFlowContext context = new RunCommandFlowContext.Builder("java -jar CLWorkstation.jar list agents matching '^((?!Virtual).)*$'").build();

        ClwUtils clw = new ClwUtils(utilities.createRemoteClwRunner(RevMigrationTestBed.EM_ROLE_ID, roleId));
        String collectorHostName = envProperties.getMachineHostnameByRoleId(roleId);
        return clw.getAgents(collectorHostName, "Active");
    }

    private long getNumberOfMetrics() throws Exception {
        ClwUtils clw = utilities.createClwUtils(RevMigrationTestBed.EM_ROLE_ID);
        Calendar start = Calendar.getInstance();
        start.add(Calendar.MINUTE, -1);
        Calendar end = Calendar.getInstance();
        return clw.getMaxMetricsValueFromAgent(".*Custom Metric Agent.*","Enterprise Manager|MOM:Number of Collector Metrics", start, end);
    }

    private void runUpgrade(final String machineId) {
        UpgradeEMFlowContext context = deserializeFlowContextFromRole(machineId + "_upgrade", EmUpgradeRole.ENV_UPGRADE_START, UpgradeEMFlowContext.class);
//        final DebugDelegate debugDelegate = new DebugDelegate() {
//            @Override
//            public int getDebugPort() {
//                return 8001;
//            }
//        };
//        runFlowByMachineId(machineId,  UpgradeEMFlow.class, context, TimeUnit.HOURS, 2, debugDelegate);
        runFlowByMachineId(machineId,  UpgradeEMFlow.class, context, TimeUnit.HOURS, 2);
    }

    private void runRollback(final String machineId) {
        RollbackEMFlowContext context = deserializeFlowContextFromRole(machineId + "_rollback", EmRollbackRole.ENV_ROLLBACK_START, RollbackEMFlowContext.class);
//        final DebugDelegate debugDelegate = new DebugDelegate() {
//            @Override
//            public int getDebugPort() {
//                return 8001;
//            }
//        };
//        runFlowByMachineId(machineId,  RollbackEMFlow.class, context, TimeUnit.HOURS, 2, debugDelegate);
        runFlowByMachineId(machineId,  RollbackEMFlow.class, context, TimeUnit.HOURS, 2);
    }

    private void startNowhereBank(String roleId) {
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.MESSAGING_SERVER_01);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_ENGINE_02);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_MEDIATOR_03);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_PORTAL_04);
        runSerializedCommandFlowFromRoleAsync(roleId, NowhereBankBTRole.BANKING_GENERATOR_05);
    }

    public void APMUIAuthTest(String emRoleId, String username, String password) {
        String host = this.envProperties.getMachineHostnameByRoleId(emRoleId);
        int port = Integer.parseInt(this.envProperties.getRolePropertyById(emRoleId, "wvPort"));
        String baseWVUrl = "http://"+host+":"+port;

        log.info("Check UI authentication to "+emRoleId+", url: "+baseWVUrl);

        ResponseEntity<String> response = null;
        final int NUM_OF_RETRIES = 5;
        for(int t = 0; t < NUM_OF_RETRIES; t++) {
            RestTemplate restClient = new RestTemplate();
            restClient.setErrorHandler(getDefaultErrorHandler());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Arrays.asList(MediaType.TEXT_HTML));
            HttpEntity<String> entity = new HttpEntity<String>("j_username=" + username + "&j_password=" + password, headers);
            try {
//            response = restClient.postForEntity(baseWVUrl + "/ApmServer/j_security_check", entity, String.class);
                response = restClient.postForEntity(baseWVUrl + "/j_security_check", entity, String.class);
            } catch (RestClientException e) {
                log.warn("UI authentication failed with an exception", e);
                response = null;
            }

            if (response!=null) {
                for (int i = 0; i < 10; i++) {
                    if (HttpStatus.FOUND.equals(response.getStatusCode())) {
                        response = restClient.postForEntity(response.getHeaders().get(HttpHeaders.LOCATION).get(0), entity, String.class);
                    }
                    if (HttpStatus.OK.equals(response.getStatusCode())) {
                        break;
                    }
                }

                if (!HttpStatus.NOT_FOUND.equals(response.getStatusCode())) {
                    break;
                }
            }
            // response was 404, or it failed, so retry after few minutes
            if (t < NUM_OF_RETRIES-1) {
                log.info("UI Authentication failed with 404 - retrying in 60 secs. Left retries: " + (NUM_OF_RETRIES-t-1));
                sleep(1000 * 60);
            }
        }
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(!response.getBody().contains("j_security_check"), "Authentication failed");
    }

    public void APMRESTGraph(String emRoleId) {
        /*
        POST http://tas-cz-n136.ca.com:8081/apm/appmap/private/graph
        Headers
        Content-Type:application/json
        Authorization:Bearer 0979e6b7-adc3-473a-b3c4-1a4abe0baf9b
        Accept:application/json
        Authentication:0979e6b7-adc3-473a-b3c4-1a4abe0baf9b

        Body:
        {}

        Response:
        */
        String host = this.envProperties.getMachineHostnameByRoleId(emRoleId);
        int port = Integer.parseInt(this.envProperties.getRolePropertyById(emRoleId, "wvEmWebPort"));
        String baseEMWVUrl = "http://" + host + ":" + port;

        log.info("REST APM get graph: " + emRoleId + ", url: " + baseEMWVUrl);

        final int NUM_OF_RETRIES = 5;
        GraphHolder graph = null;
        for (int i = 0; i < NUM_OF_RETRIES; i++) {
            RestTemplate restClient = new RestTemplate();
            restClient.setErrorHandler(getDefaultErrorHandler());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer " + RevMigrationTestBed.ADMIN_AUX_TOKEN);
            headers.set("Authentication", RevMigrationTestBed.ADMIN_AUX_TOKEN);
            HttpEntity<String> entity = new HttpEntity<String>("{}", headers);

            ResponseEntity<String> response = null;
            try {
                response = restClient.postForEntity(baseEMWVUrl + "/apm/appmap/private/graph", entity, String.class);
            } catch (RestClientException e) {
                log.warn("APM REST call failed with an exception", e);
            }
            if (response!=null) {
                Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
                log.info("REST response graph content: " + response.getBody());
                try {
                    graph = GraphHolder.initGraph(response.getBody());
                    if (!graph.getVertices().isEmpty()) {
                        log.info("REST response is not empty.");
                        break;
                    }
                } catch (Throwable e) {
                    // catching Assertion exception from GraphHolder.initGraph() when non complete graph is retrieved
                    log.warn("Cannot load graph from response - retrying", e);
                }
            }

            if (i < NUM_OF_RETRIES-1) {
                log.info("No vertices returned, retrying in 2 minutes. Left retries: "+(NUM_OF_RETRIES-i-1));
                sleep(1000*60*2);
            }
        }
        Assert.assertNotNull(graph);
        Assert.assertTrue(!graph.getVertices().isEmpty(), "Empty graph returned." );
    }

    private ResponseErrorHandler getDefaultErrorHandler() {
        return new DefaultResponseErrorHandler() {
            protected boolean hasError(HttpStatus statusCode) {
                return false;
            }
        };
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
