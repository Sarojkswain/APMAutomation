package com.ca.apm.saas.ttviewer;

import com.ca.apm.systemtest.fld.role.CLWWorkStationLoadRole;
import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import org.testng.annotations.Test;
import com.ca.tas.type.SizeType;
import com.ca.tas.test.TasTestNgTest;
import java.io.IOException;
import static org.testng.Assert.assertTrue;

/**
 * 
 * @author banra06
 */
public class StartTTViewerSTLoad extends TasTestNgTest {

	@Tas(testBeds = @TestBed(name = TTViewerSTEMTestbed.class, executeOn = TTViewerSTEMTestbed.EM_MACHINE_ID), size = SizeType.BIG, owner = "banra06")
	@Test
	public void startLoads() throws IOException {
		runSerializedCommandFlowFromRoleAsync(TTViewerSTEMTestbed.WLSCLIENT_2,
				ClientDeployRole.STRESSAPP_START_LOAD);
		runSerializedCommandFlowFromRoleAsync(TTViewerSTEMTestbed.WLSCLIENT_1,
				ClientDeployRole.WLSCC_START_LOAD);
		runSerializedCommandFlowFromRoleAsync(TTViewerSTEMTestbed.WLSCLIENT_1,
				ClientDeployRole.DEEPTT_START_LOAD);
		runSerializedCommandFlowFromRoleAsync(
				TTViewerSTEMTestbed.WURLITZER_ROLE_ID,
				WurlitzerLoadRole.START_WURLITZER_FLOW_KEY);
		runSerializedCommandFlowFromRoleAsync(TTViewerSTEMTestbed.HVR_ROLE_ID,
				HVRAgentLoadRole.START_HVR_LOAD_KEY);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		runSerializedCommandFlowFromRoleAsync(TTViewerSTEMTestbed.CLW_ROLE_ID,
				CLWWorkStationLoadRole.CLW_START_LOAD);
		assertTrue(true);
	}
}
