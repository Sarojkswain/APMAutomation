package com.ca.apm.systemtest.fld.test.smoke;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.systemtest.fld.flow.RunWebViewLoadFlow;
import com.ca.apm.systemtest.fld.flow.RunWebViewLoadFlowContext;
import com.ca.apm.systemtest.fld.role.loads.WebViewLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.smoke.WebViewSmokeTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.google.common.collect.Maps;

@Tas(testBeds = @TestBed(name=WebViewSmokeTestbed.class, executeOn="emMachine"))
@Test
public class WebViewLoadSmokeTest extends TasTestNgTest {

    public void testWebView() {
        for (String machineAlias: FLDLoadConstants.WEBVIEW_LOAD_MACHINE_IDS) {
            String roleId = "webViewLoadRole_" + machineAlias;
            String machineId = envProperties.getMachineIdByRoleId(roleId);
            Map<String, String> roleProps = Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
            
//            String serializer = AbstractEnvPropertySerializer.getSerializer(WebViewLoadRole.ENV_WEBVIEW_LOAD_START, roleProps);
            
            IFlowContext startFlowContext = deserializeFromProperties(roleId,
                WebViewLoadRole.ENV_WEBVIEW_LOAD_START, roleProps,
                RunWebViewLoadFlowContext.class);
            runFlowByMachineIdAsync(machineId, RunWebViewLoadFlow.class, startFlowContext, TimeUnit.DAYS, 28);
        }
    }
}
