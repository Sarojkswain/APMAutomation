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
import org.testng.collections.Sets;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.test.atc.common.Utils.sleep;

/**
 * @author jirji01
 */
public class UpgradeTest extends TasTestNgTest {

    private final Logger log = Logger.getLogger(getClass());

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_97_rh6.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_97_rh6() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_97_rh7.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_97_rh7() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_97_w10.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_97_w10() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_97_w8.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_97_w8() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_97_w12.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_97_w12() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_97_w10_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_97_w10_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_97_w8_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_97_w8_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_97_w12_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_97_w12_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_100_rh6.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_100_rh6() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_100_rh7.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_100_rh7() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_100_w10.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_100_w10() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_100_w8.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_100_w8() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_100_w12.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_100_w12() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_100_w10_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_100_w10_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_100_w8_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_100_w8_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_100_w12_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_100_w12_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_101_rh6.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_101_rh6() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_101_rh7.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_101_rh7() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_101_w10.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_101_w10() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_101_w8.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_101_w8() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_101_w12.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_101_w12() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_101_w10_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_101_w10_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_101_w8_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_101_w8_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_101_w12_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_101_w12_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_102_rh6.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_102_rh6() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_102_rh7.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_102_rh7() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_102_w10.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_102_w10() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_102_w8.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_102_w8() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_102_w12.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_102_w12() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_102_w10_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_102_w10_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_102_w8_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_102_w8_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_102_w12_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_102_w12_oracle() throws Exception {
        test();
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_103_rh6.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_103_rh6() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_103_rh7.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_103_rh7() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_103_w10.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_103_w10() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_103_w8.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_103_w8() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_103_w12.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_103_w12() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_103_w10_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_103_w10_oracle() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_103_w8_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_103_w8_oracle() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_103_w12_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_103_w12_oracle() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_105_rh6.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_105_rh6() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_105_rh7.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_105_rh7() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_105_w10.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_105_w10() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_105_w8.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_105_w8() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_105_w12.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_105_w12() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_105_w10_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_105_w10_oracle() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_105_w8_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_105_w8_oracle() throws Exception {
        test(true);
    }

    @Test(groups = {"upgrade_oracle"})
    @Tas(testBeds = @TestBed(name = UpgradeTestbed_105_w12_oracle.class, executeOn = UpgradeTestbed.MOM_MACHINE_ID), size = SizeType.BIG, owner = "jirji01")
    public void test_105_w12_oracle() throws Exception {
        test(true);
    }

    private void test() throws Exception {
        test(false);
    }

    private void test(boolean isAgc) throws Exception {

        log.info("sleep 10 minutes");
        sleep(1000*60*10);

        log.info("check configuration before upgrade");
        checkConfiguration();

        log.info("check metrics before upgrade");
        long numberOfMetricsOld = getNumberOfMetrics();

        log.info("stop testbed");

        log.info("stopping mom");
        runSerializedCommandFlowFromRole(UpgradeTestbed.MOM_ROLE_ID, EmRole.ENV_STOP_EM);
        log.info("stopping c1");
        runSerializedCommandFlowFromRole(UpgradeTestbed.C1_ROLE_ID, EmRole.ENV_STOP_EM);
        log.info("stopping c2");
        runSerializedCommandFlowFromRole(UpgradeTestbed.C2_ROLE_ID, EmRole.ENV_STOP_EM);
        log.info("stopping wv");
        runSerializedCommandFlowFromRole(UpgradeTestbed.DB_ROLE_ID, EmRole.ENV_STOP_WEBVIEW);
        log.info("stopping sql");
        runSerializedCommandFlowFromRole(UpgradeTestbed.DB_ROLE_ID, EmRole.ENV_STOP_APM_SQL_SERVER);
        if (isAgc) {
            log.info("stopping agc");
            runSerializedCommandFlowFromRole(UpgradeAgcTestbed.AGC_ROLE_ID, EmRole.ENV_STOP_EM);
            runSerializedCommandFlowFromRole(UpgradeAgcTestbed.AGC_ROLE_ID, EmRole.ENV_STOP_WEBVIEW);
        }

        log.info("start upgrade");

        log.info("starting db upgrade");
        runUpgrade(UpgradeTestbed.DB_MACHINE_ID);
        log.info("starting mom upgrade");
        runUpgrade(UpgradeTestbed.MOM_MACHINE_ID);
        log.info("starting c1 upgrade");
        runUpgrade(UpgradeTestbed.C1_MACHINE_ID);
        log.info("starting c2 upgrade");
        runUpgrade(UpgradeTestbed.C2_MACHINE_ID);
        if (isAgc) {
            log.info("starting agc upgrade");
            runUpgrade(UpgradeAgcTestbed.AGC_MACHINE_ID);
        }

        log.info("start testbed");

        log.info("starting sql");
        runSerializedCommandFlowFromRole(UpgradeTestbed.DB_ROLE_ID, EmRole.ENV_START_APM_SQL_SERVER);
        log.info("starting mom");
        runSerializedCommandFlowFromRole(UpgradeTestbed.MOM_ROLE_ID, EmRole.ENV_START_EM);
        log.info("starting c1");
        runSerializedCommandFlowFromRole(UpgradeTestbed.C1_ROLE_ID, EmRole.ENV_START_EM);
        log.info("starting c2");
        runSerializedCommandFlowFromRole(UpgradeTestbed.C2_ROLE_ID, EmRole.ENV_START_EM);
        log.info("starting wv");
        runSerializedCommandFlowFromRole(UpgradeTestbed.DB_ROLE_ID, EmRole.ENV_START_WEBVIEW);
        if (isAgc) {
            log.info("starting agc");
            runSerializedCommandFlowFromRole(UpgradeAgcTestbed.AGC_ROLE_ID, EmRole.ENV_START_EM);
            runSerializedCommandFlowFromRole(UpgradeAgcTestbed.AGC_ROLE_ID, EmRole.ENV_START_WEBVIEW);
        }

        log.info("sleep 10 minutes");
        sleep(1000*60*10);

        log.info("check configuration after upgrade");
        checkConfiguration();

        log.info("check metrics after upgrade");
        long numberOfMetricsNew = getNumberOfMetrics();
        Assert.assertTrue((numberOfMetricsOld - 100) <= numberOfMetricsNew, "new version should collect same or more metrics. ");
    }

    private List<String> laxOption(List<String> list) {
        List<String> result = new ArrayList<>(list);
        result.add("-Djava.awt.headless=false");

        return result;
    }

    private void checkConfiguration() {
        Map<String, List<String>> map = new HashMap<>();

        map.put("lax.nl.java.option.additional", laxOption(UpgradeTestbed.COLL_LAXNL_JAVA_OPTION));
        checkProperties(UpgradeTestbed.C1_MACHINE_ID, UpgradeTestbed.C1_ROLE_ID, "Introscope_Enterprise_Manager.lax", map);

        map.clear();
        map.put("lax.nl.java.option.additional", laxOption(UpgradeTestbed.MOM_LAXNL_JAVA_OPTION));
        checkProperties(UpgradeTestbed.MOM_MACHINE_ID, UpgradeTestbed.MOM_ROLE_ID, "Introscope_Enterprise_Manager.lax", map);

        map.clear();
        map.put("lax.nl.java.option.additional", laxOption(UpgradeTestbed.WV_LAXNL_JAVA_OPTION));
        checkProperties(UpgradeTestbed.DB_MACHINE_ID, UpgradeTestbed.DB_ROLE_ID, "Introscope_WebView.lax", map);

        map.clear();
        List<String> collectors = Arrays.asList(envProperties.getMachineHostnameByRoleId(UpgradeTestbed.C1_ROLE_ID), envProperties.getMachineHostnameByRoleId(UpgradeTestbed.C2_ROLE_ID));
        map.put("introscope.enterprisemanager.clustering.login.em1.host", collectors);
        map.put("introscope.enterprisemanager.clustering.login.em2.host", collectors);
        map.put("transport.buffer.input.maxNum", Collections.singletonList("2400"));
        map.put("transport.outgoingMessageQueueSize", Collections.singletonList("6000"));
        map.put("transport.override.isengard.high.concurrency.pool.min.size", Collections.singletonList("10"));
        map.put("transport.override.isengard.high.concurrency.pool.max.size", Collections.singletonList("10"));
        map.put("introscope.enterprisemanager.transactiontrace.arrivalbuffer.capacity", Collections.singletonList("5000"));
        checkProperties(UpgradeTestbed.MOM_MACHINE_ID, UpgradeTestbed.MOM_ROLE_ID, "config/IntroscopeEnterpriseManager.properties", map);

        map.clear();
        map.put("SaasAdmin", Collections.singletonList(""));
        map.put("Admin", Collections.singletonList(""));
        map.put("Guest", Arrays.asList("2a.1000.a9hZlUjIZUVV4vMjkv3BtA==.Yswf7wbWLN6rvbfb9jaXoQ==", "adb831a7fdd83dd1e2a39ce7591dff8", "5ed8944a85a9763fd315852f448cb7de36c5e928e13b3be427f98f7dc455f141"));
        map.put("cemadmin",  Arrays.asList("2a.1000.qPZK3Ql/Swn0IX/5u6zdbA==.ayqr0jCCFDgjd7w6Tj96fg==", "d66636b253cb346dbb6240e3def3618", "acef2c15bcd349db90dffece73e1256e881c4416fc1f2d3a4946418349d9a"));
        checkProperties(UpgradeTestbed.MOM_MACHINE_ID, UpgradeTestbed.MOM_ROLE_ID, "config/users.xml", map);

//        int agents = 0;
//        agents += getConnectedAgents(UpgradeTestbed.MOM_ROLE_ID);
//        agents += getConnectedAgents(UpgradeTestbed.C1_ROLE_ID);
//        agents += getConnectedAgents(UpgradeTestbed.C2_ROLE_ID);

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

    private int getConnectedAgents(String roleId) {
//        RunCommandFlowContext context = new RunCommandFlowContext.Builder("java -jar CLWorkstation.jar list agents matching '^((?!Virtual).)*$'").build();

        ClwUtils clw = new ClwUtils(utilities.createRemoteClwRunner(UpgradeTestbed.MOM_ROLE_ID, roleId));
        String collectorHostName = envProperties.getMachineHostnameByRoleId(roleId);
        return clw.getAgents(collectorHostName, "Active");
    }

    private long getNumberOfMetrics() throws Exception {
        ClwUtils clw = utilities.createClwUtils(UpgradeTestbed.MOM_ROLE_ID);
        String collectorHostName = envProperties.getMachineHostnameByRoleId(UpgradeTestbed.MOM_ROLE_ID);

        Calendar start = Calendar.getInstance();
        start.add(Calendar.MINUTE, -1);
        Calendar end = Calendar.getInstance();
        return clw.getMaxMetricsValueFromAgent(".*Custom Metric Agent.*","Enterprise Manager|MOM:Number of Collector Metrics", start, end);
    }

    private void runUpgrade(final String machineId) {
        UpgradeEMFlowContext context = deserializeFlowContextFromRole(machineId + "_upgrade", EmUpgradeRole.ENV_UPGRADE_START, UpgradeEMFlowContext.class);
        runFlowByMachineId(machineId,  UpgradeEMFlow.class, context, TimeUnit.HOURS, 2);
    }
}
