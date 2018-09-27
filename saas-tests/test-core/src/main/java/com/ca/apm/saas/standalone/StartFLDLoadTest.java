package com.ca.apm.saas.standalone;

import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * 
 * @author banra06
 */
public class StartFLDLoadTest extends TasTestNgTest {

	@Tas(testBeds = @TestBed(name = FLDStandAloneTestbed.class, executeOn = FLDStandAloneTestbed.EM_MACHINE_ID), size = SizeType.BIG, owner = "banra06")
	@Test
	public void startLoads() throws IOException {
		//Stress App Load
		System.out
				.println("-------- Starting stressapp load on ------ "
						+ envProperties
								.getMachineHostnameByRoleId(FLDStandAloneTestbed.WLSCLIENT_2));
		runSerializedCommandFlowFromRoleAsync(FLDStandAloneTestbed.WLSCLIENT_2,
				ClientDeployRole.STRESSAPP_START_LOAD);
		//WLS Cross Cluster Load
		System.out
				.println("Starting Weblogic Cross Cluster load on ------ "
						+ envProperties
								.getMachineHostnameByRoleId(FLDStandAloneTestbed.WLSCLIENT_1));
		runSerializedCommandFlowFromRoleAsync(FLDStandAloneTestbed.WLSCLIENT_1,
				ClientDeployRole.WLSCC_START_LOAD);
		//WLS 4000 Deep Components Load
		System.out
				.println("Starting Weblogic Deep TT load on ------ "
						+ envProperties
								.getMachineHostnameByRoleId(FLDStandAloneTestbed.WLSCLIENT_1));
		runSerializedCommandFlowFromRoleAsync(FLDStandAloneTestbed.WLSCLIENT_1,
				ClientDeployRole.DEEPTT_START_LOAD);
		//Wurtlitzer Load
		System.out
				.println("Starting Wurlitzer load on ------ "
						+ envProperties
								.getMachineHostnameByRoleId(FLDStandAloneTestbed.WURLITZER_ROLE_ID));
		runSerializedCommandFlowFromRoleAsync(
				FLDStandAloneTestbed.WURLITZER_ROLE_ID,
				WurlitzerLoadRole.START_WURLITZER_FLOW_KEY);
		// HVR Load
		System.out
				.println("Starting HVR load on ------ "
						+ envProperties
								.getMachineHostnameByRoleId(FLDStandAloneTestbed.HVR_ROLE_ID));
		runSerializedCommandFlowFromRoleAsync(FLDStandAloneTestbed.HVR_ROLE_ID,
				HVRAgentLoadRole.START_HVR_LOAD_KEY);
		// Tomcat6 and Tomcat7 Agent Load
		System.out
				.println("------- Starting Tomcat6 and Tomcat7 jmeter load on ------ "
						+ envProperties
								.getMachineHostnameByRoleId(FLDStandAloneTestbed.JMETER_LOAD1));
		runSerializedCommandFlowFromRoleAsync(
				FLDStandAloneTestbed.JMETER_LOAD1, JMeterRole.START_LOAD);
		runSerializedCommandFlowFromRoleAsync(
				FLDStandAloneTestbed.JMETER_LOAD2, JMeterRole.START_LOAD);
		// Tomcat9080 and Tomcat 9081 Agent Load 
		System.out
				.println("------- Starting Tomcat9080 and Tomcat9081 jmeter load on ------ "
						+ envProperties
								.getMachineHostnameByRoleId(FLDStandAloneTestbed.JMETER_LOAD3));
		runSerializedCommandFlowFromRoleAsync(
				FLDStandAloneTestbed.JMETER_LOAD3, JMeterRole.START_LOAD);
		runSerializedCommandFlowFromRoleAsync(
				FLDStandAloneTestbed.JMETER_LOAD4, JMeterRole.START_LOAD);
		// JBoss 6 and JBoss7 Agent Load
		System.out
				.println("------- Starting JBoss6 and JBoss7 jmeter load on ------ "
						+ envProperties
								.getMachineHostnameByRoleId(FLDStandAloneTestbed.JMETER_LOAD5));
		runSerializedCommandFlowFromRoleAsync(
				FLDStandAloneTestbed.JMETER_LOAD5, JMeterRole.START_LOAD);
		runSerializedCommandFlowFromRoleAsync(
				FLDStandAloneTestbed.JMETER_LOAD6, JMeterRole.START_LOAD);
		// WAS Agent Load
//		System.out
//				.println("------- Starting WebSphere jmeter load on ------ "
//						+ envProperties
//								.getMachineHostnameByRoleId(FLDStandAloneTestbed.JMETER_LOAD7));
//		runSerializedCommandFlowFromRoleAsync(
//				FLDStandAloneTestbed.JMETER_LOAD7, JMeterRole.START_LOAD);
		//WAS Cross CLuster Load
//		System.out
//		.println("------- Starting WebSphere Cross Cluster load on ------ "
//				+ envProperties
//						.getMachineHostnameByRoleId(FLDStandAloneTestbed.WAS_XCLUSTER_CLIENT_ROLE_ID));
//		runSerializedCommandFlowFromRoleAsync(FLDStandAloneTestbed.WAS_XCLUSTER_CLIENT_ROLE_ID,ClientDeployRole.WASCC_START_LOAD);
		//AT Rest Load
		System.out
		.println("------- Starting Assisted Triage load on ------ "
				+ envProperties
						.getMachineHostnameByRoleId(FLDStandAloneTestbed.JMETER_LOAD8));
		runSerializedCommandFlowFromRoleAsync(FLDStandAloneTestbed.JMETER_LOAD8,JMeterRole.START_LOAD);
		//TT Viewer Rest Load
		System.out
		.println("------- Starting TT Viewer load on ------ "
				+ envProperties
						.getMachineHostnameByRoleId(FLDStandAloneTestbed.JMETER_LOAD9));
		runSerializedCommandFlowFromRoleAsync(FLDStandAloneTestbed.JMETER_LOAD9,JMeterRole.START_LOAD);

		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		runSerializedCommandFlowFromRoleAsync(FLDStandAloneTestbed.CLW_ROLE_ID,CLWWorkStationLoadRole.CLW_START_LOAD);
		runSerializedCommandFlowFromRoleAsync(FLDStandAloneTestbed.CLW_ROLE_ID,CLWWorkStationLoadRole.HCLW_START_LOAD);
		assertTrue(true);
	}
}
