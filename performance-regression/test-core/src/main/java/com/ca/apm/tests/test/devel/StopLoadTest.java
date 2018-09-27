package com.ca.apm.tests.test.devel;

import java.util.HashSet;
import java.util.Map;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlow;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlowContext;
import com.ca.apm.tests.role.CautlRole;
import com.ca.apm.tests.role.JMeterRole;
import com.ca.apm.tests.role.WebViewLoadRole;
import com.ca.apm.tests.role.WurlitzerRole;
import com.ca.apm.tests.test.MyTasTestNgTest;
import com.ca.apm.tests.testbed.devel.StopLoadTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;


/**
 * @author jirji01
 */
public class StopLoadTest extends MyTasTestNgTest {
    @Test(groups = {"windows"})
    @Tas(testBeds = @TestBed(name = StopLoadTestbed.class, executeOn = StopLoadTestbed.EM_TEST_MACHINE_ID), owner = "jirji01")
    public void testFakeWorkstation() throws Exception {

        ClwUtils clw = utilities.createClwUtils(StopLoadTestbed.EM_ROLE_ID);
        String collectorHostName =
            envProperties.getMachineHostnameByRoleId(StopLoadTestbed.EM_ROLE_ID);

        // start load
        for (String id : getSerializedIds(StopLoadTestbed.FAKE_WORKSTATION_CAUTL_ROLE_ID,
            CautlRole.ENV_CAUTL_START)) {
            runSerializedCommandFlowFromRole(StopLoadTestbed.FAKE_WORKSTATION_CAUTL_ROLE_ID, id);
        }

        Thread.sleep(15000);
        Assert.assertEquals("Expecting more than one active agent with running Hammond.", 10,
            getConnectedWorkstations(clw, collectorHostName));

        // stop load
        runSerializedCommandFlowFromRole(StopLoadTestbed.FAKE_WORKSTATION_CAUTL_ROLE_ID,
            CautlRole.ENV_CAUTL_STOP);

        Thread.sleep(15000);
        Assert.assertEquals("No active agent when Hammond is shutdown.", 0,
            getConnectedWorkstations(clw, collectorHostName));
    }

    @Test(groups = {"windows"})
    @Tas(testBeds = @TestBed(name = StopLoadTestbed.class, executeOn = StopLoadTestbed.EM_TEST_MACHINE_ID), owner = "jirji01")
    public void testJMeter() throws Exception {

        ClwUtils clw = utilities.createClwUtils(StopLoadTestbed.EM_ROLE_ID);
        String collectorHostName =
            envProperties.getMachineHostnameByRoleId(StopLoadTestbed.EM_ROLE_ID);

        // start load
        
        JMeterRunFlowContext context = (JMeterRunFlowContext) deserializeFlowContextFromRole(StopLoadTestbed.JMETER_ROLE_ID,
            JMeterRole.RUN_JMETER, JMeterRunFlowContext.class);
        runFlowByMachineId(StopLoadTestbed.EM_TEST_MACHINE_ID, JMeterRunFlow.class, context);

        Thread.sleep(15000);
        Assert.assertEquals("Expecting more than one active agent with running Hammond.", 10,
            getConnectedWorkstations(clw, collectorHostName));

        // stop load
        runSerializedCommandFlowFromRole(StopLoadTestbed.JMETER_ROLE_ID, JMeterRole.STOP_JMETER);

        Thread.sleep(15000);
        Assert.assertEquals("No active agent when Hammond is shutdown.", 0,
            getConnectedWorkstations(clw, collectorHostName));
    }

    @Test(groups = {"windows"})
    @Tas(testBeds = @TestBed(name = StopLoadTestbed.class, executeOn = StopLoadTestbed.EM_TEST_MACHINE_ID), owner = "jirji01")
    public void testWurlitzer() throws Exception {

        ClwUtils clw = utilities.createClwUtils(StopLoadTestbed.EM_ROLE_ID);
        String collectorHostName =
            envProperties.getMachineHostnameByRoleId(StopLoadTestbed.EM_ROLE_ID);

        // start load

        runSerializedCommandFlowFromRole(StopLoadTestbed.WURLITZER_ROLE_ID,
            WurlitzerRole.ENV_RUN_WURLITZER);

        Thread.sleep(15000);
        Assert.assertTrue("Expecting more than twenty active agent with running Wurlitzer.",
            clw.getAgents(collectorHostName, "Active") >= 20);

        // stop load
        runSerializedCommandFlowFromRole(StopLoadTestbed.WURLITZER_ROLE_ID,
            WurlitzerRole.ENV_STOP_WURLITZER);

        Thread.sleep(15000);
        Assert.assertTrue("No active agent when Wurlitzer is down.", clw.getAgents(collectorHostName, "Active") < 20);
    }
    
    @Test(groups = {"windows"})
    @Tas(testBeds = @TestBed(name = StopLoadTestbed.class, executeOn = StopLoadTestbed.EM_TEST_MACHINE_ID), size = SizeType.MEDIUM, owner = "jirji01")
    public void testWebView() throws Exception {

        ClwUtils clw = utilities.createClwUtils(StopLoadTestbed.EM_ROLE_ID);
        String collectorHostName =
            envProperties.getMachineHostnameByRoleId(StopLoadTestbed.EM_ROLE_ID);

        // start load

        runSerializedCommandFlowFromRole(StopLoadTestbed.WEB_VIEW_LOAD_ROLE,
            WebViewLoadRole.ENV_WEBVIEW_LOAD_START);

        Thread.sleep(15000);
        Assert.assertTrue("Expecting more than one active browser with running WebViewLoad.",
            getConnectedBrowsers(clw, collectorHostName) > 0);

        // stop load
        runSerializedCommandFlowFromRole(StopLoadTestbed.WEB_VIEW_LOAD_ROLE,
            WebViewLoadRole.ENV_WEBVIEW_LOAD_STOP);

        Thread.sleep(600000);
        Assert.assertEquals("No active browsers when WebViewLoad is off.", 0,
            getConnectedBrowsers(clw, collectorHostName));
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

    private int getConnectedWorkstations(ClwUtils clw, String collectorHost) {

        final String hostAndPort;
        if (collectorHost == null) {
            hostAndPort = "";
        } else {
            hostAndPort = " (" + collectorHost + "@.*)";
        }

        final String agentRegularExpression =
            "Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)"
                + hostAndPort;
        final String metricRegularExpression =
            "Enterprise Manager|Connections:Number of Workstations";

        // invoke CLW and get metrics
        String ret =
            clw.getMetricUtils().getLastHistoricalDataFromAgents(agentRegularExpression,
                metricRegularExpression);
        if (ret == null) {
            ret = "0";
        }

        return Integer.parseInt(ret);
    }
    private int getConnectedBrowsers(ClwUtils clw, String collectorHost) {

        if (collectorHost == null) {
            collectorHost = ".*";
        } 
        
        final String agentRegularExpression =
            collectorHost + "|APM Introscope WebView|APM.WebView";
        final String metricRegularExpression =
            "WebView:Active Browsers Count";

        // invoke CLW and get metrics
        String ret =
            clw.getMetricUtils().getLastHistoricalDataFromAgents(agentRegularExpression,
                metricRegularExpression);
        if (ret == null) {
            ret = "0";
        }

        return Integer.parseInt(ret);
    }
}
