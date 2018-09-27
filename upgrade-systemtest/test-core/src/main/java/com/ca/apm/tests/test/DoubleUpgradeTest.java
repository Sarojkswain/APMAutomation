package com.ca.apm.tests.test;

import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.tests.flow.CheckEmConfigFlow;
import com.ca.apm.tests.flow.CheckEmConfigFlowContext;
import com.ca.apm.tests.flow.UpgradeEMFlow;
import com.ca.apm.tests.flow.UpgradeEMFlowContext;
import com.ca.apm.tests.role.EmUpgradeRole;
import com.ca.apm.tests.testbed.*;
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.Platform;
import com.ca.tas.type.SizeType;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.test.atc.common.Utils.sleep;

/**
 * @author jirji01
 */
public class DoubleUpgradeTest extends TasTestNgTest {

    private final Logger log = Logger.getLogger(getClass());

    @Test(groups = {"upgrade_double"})
    @Tas(testBeds = @TestBed(name = SimpleUpgradeTestbed.class, executeOn = SimpleUpgradeTestbed.EM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test() throws Exception {

        log.info("sleep 10 minutes");
//        sleep(1000*60*10);

        log.info("check configuration before upgrade");
        checkConfiguration();

        log.info("check metrics before upgrade");
        long numberOfMetricsOld = getNumberOfMetrics();

        log.info("stop testbed");
        runSerializedCommandFlowFromRole(SimpleUpgradeTestbed.EM_ROLE_ID, EmRole.ENV_STOP_EM);
        runSerializedCommandFlowFromRole(SimpleUpgradeTestbed.EM_ROLE_ID, EmRole.ENV_STOP_WEBVIEW);

        log.info("starting upgrade");
        runUpgrade(SimpleUpgradeTestbed.UPGRADE_1_ROLE_ID);

        log.info("start testbed");
        runSerializedCommandFlowFromRole(SimpleUpgradeTestbed.EM_ROLE_ID, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(SimpleUpgradeTestbed.EM_ROLE_ID, EmRole.ENV_START_WEBVIEW);

        log.info("sleep 10 minutes");
        sleep(1000*60*10);

        log.info("check configuration after upgrade");
        checkConfiguration();

        // check data store
        checkProperties(SimpleUpgradeTestbed.EM_MACHINE_ID, SimpleUpgradeTestbed.EM_ROLE_ID,
                "config/IntroscopeEnterpriseManager.properties",
                Collections.singletonMap("introscope.enterprisemanager.smartstor.directory.metadata", Collections.singletonList("data/metadata")));

        log.info("check metrics after upgrade");
        long numberOfMetricsNew = getNumberOfMetrics();
        Assert.assertTrue((numberOfMetricsOld - 100) <= numberOfMetricsNew, "new version should collect same or more metrics. ");

        numberOfMetricsOld = numberOfMetricsNew;

        log.info("stop testbed");
        runSerializedCommandFlowFromRole(SimpleUpgradeTestbed.EM_ROLE_ID, EmRole.ENV_STOP_EM);
        runSerializedCommandFlowFromRole(SimpleUpgradeTestbed.EM_ROLE_ID, EmRole.ENV_STOP_WEBVIEW);

        log.info("starting upgrade");
        runUpgrade(SimpleUpgradeTestbed.UPGRADE_2_ROLE_ID);

        log.info("start testbed");
        runSerializedCommandFlowFromRole(SimpleUpgradeTestbed.EM_ROLE_ID, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(SimpleUpgradeTestbed.EM_ROLE_ID, EmRole.ENV_START_WEBVIEW);

        log.info("sleep 10 minutes");
        sleep(1000*60*10);

        log.info("check configuration after upgrade");
        checkConfiguration();

        // check data store
        checkProperties(SimpleUpgradeTestbed.EM_MACHINE_ID, SimpleUpgradeTestbed.EM_ROLE_ID,
                "config/IntroscopeEnterpriseManager.properties",
                Collections.singletonMap("introscope.enterprisemanager.smartstor.directory.metadata", Collections.singletonList("data/metadata")));

        log.info("check metrics after upgrade");
        numberOfMetricsNew = getNumberOfMetrics();
        Assert.assertTrue((numberOfMetricsOld - 100) <= numberOfMetricsNew, "new version should collect same or more metrics. ");

    }

    private List<String> laxOption(List<String> list) {
        List<String> result = new ArrayList<>(list);
        result.add("-Djava.awt.headless=false");

        return result;
    }

    private void checkConfiguration() {
        Map<String, List<String>> map = new HashMap<>();

        map.clear();
        map.put("lax.nl.java.option.additional", laxOption(SimpleUpgradeTestbed.EM_LAXNL_JAVA_OPTION));
        checkProperties(SimpleUpgradeTestbed.EM_MACHINE_ID, SimpleUpgradeTestbed.EM_ROLE_ID, "Introscope_Enterprise_Manager.lax", map);

        map.clear();
        map.put("lax.nl.java.option.additional", laxOption(SimpleUpgradeTestbed.WV_LAXNL_JAVA_OPTION));
        checkProperties(SimpleUpgradeTestbed.EM_MACHINE_ID, SimpleUpgradeTestbed.EM_ROLE_ID, "Introscope_WebView.lax", map);

        map.clear();
        map.put("transport.buffer.input.maxNum", Collections.singletonList("2400"));
        map.put("transport.outgoingMessageQueueSize", Collections.singletonList("6000"));
        map.put("transport.override.isengard.high.concurrency.pool.min.size", Collections.singletonList("10"));
        map.put("transport.override.isengard.high.concurrency.pool.max.size", Collections.singletonList("10"));
        map.put("introscope.enterprisemanager.transactiontrace.arrivalbuffer.capacity", Collections.singletonList("5000"));
        checkProperties(SimpleUpgradeTestbed.EM_MACHINE_ID, SimpleUpgradeTestbed.EM_ROLE_ID, "config/IntroscopeEnterpriseManager.properties", map);

        map.clear();
        map.put("SaasAdmin", Collections.singletonList(""));
        map.put("Admin", Collections.singletonList(""));
        map.put("Guest", Arrays.asList("2a.1000.a9hZlUjIZUVV4vMjkv3BtA==.Yswf7wbWLN6rvbfb9jaXoQ==", "adb831a7fdd83dd1e2a39ce7591dff8", "5ed8944a85a9763fd315852f448cb7de36c5e928e13b3be427f98f7dc455f141"));
        map.put("cemadmin",  Arrays.asList("2a.1000.qPZK3Ql/Swn0IX/5u6zdbA==.ayqr0jCCFDgjd7w6Tj96fg==", "d66636b253cb346dbb6240e3def3618", "acef2c15bcd349db90dffece73e1256e881c4416fc1f2d3a4946418349d9a"));
        checkProperties(SimpleUpgradeTestbed.EM_MACHINE_ID, SimpleUpgradeTestbed.EM_ROLE_ID, "config/users.xml", map);

//        int agents = 0;
//        agents += getConnectedAgents(SimpleUpgradeTestbed.MOM_ROLE_ID);
//        agents += getConnectedAgents(SimpleUpgradeTestbed.C1_ROLE_ID);
//        agents += getConnectedAgents(SimpleUpgradeTestbed.C2_ROLE_ID);

//        Assert.assertEquals(agents, 12, "Expecting 12 connected agents.");
    }

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


    private long getNumberOfMetrics() throws Exception {
        ClwUtils clw = utilities.createClwUtils(SimpleUpgradeTestbed.EM_ROLE_ID);
        String collectorHostName = envProperties.getMachineHostnameByRoleId(SimpleUpgradeTestbed.EM_ROLE_ID);

        Calendar start = Calendar.getInstance();
        start.add(Calendar.MINUTE, -1);
        Calendar end = Calendar.getInstance();
        return clw.getMaxMetricsValueFromAgent(".*Custom Metric Agent.*","Enterprise Manager|MOM:Number of Collector Metrics", start, end);
    }

    private void runUpgrade(final String roleId) {
        UpgradeEMFlowContext context = deserializeFlowContextFromRole(roleId, EmUpgradeRole.ENV_UPGRADE_START, UpgradeEMFlowContext.class);
        runFlowByMachineId(SimpleUpgradeTestbed.EM_MACHINE_ID,  UpgradeEMFlow.class, context, TimeUnit.HOURS, 2);
    }
}
