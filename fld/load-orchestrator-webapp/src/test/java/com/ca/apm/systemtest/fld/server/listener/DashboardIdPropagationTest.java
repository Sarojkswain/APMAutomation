/**
 * 
 */
package com.ca.apm.systemtest.fld.server.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.server.dao.DashboardDao;
import com.ca.apm.systemtest.fld.server.dao.LoggerMonitorDao;
import com.ca.apm.systemtest.fld.server.model.LoggerMonitorValue;

/**
 * @author KEYJA01
 *
 */
public class DashboardIdPropagationTest {
    private Logger log = LoggerFactory.getLogger(DashboardIdPropagationTest.class);
	private static final Long DBID = 12345L;
	private static Long dashboardId = null;
	private ClassPathXmlApplicationContext ctx;
	private ProcessEngine processEngine;

	@Before
	public void setup() {
		ctx = new ClassPathXmlApplicationContext("dashboard-test-context.xml");
		processEngine = ctx.getBean(ProcessEngine.class);
		EventListener eListener = ctx.getBean(EventListener.class);
		processEngine.getRuntimeService().addEventListener(eListener);
	}

	@After
	public void cleanup() {
		ctx.close();
		processEngine.close();
	}

	@Test
	public void testDashboardIdPropagation() throws InterruptedException {
	   log.info("Starting testDashboardIdPropagation()");
		// Setup mocks
		DashboardDao dashboardDao = ctx.getBean(DashboardDao.class);
		when(dashboardDao.findByProcessInstanceid(anyString())).thenReturn(null);
		LoggerMonitorDao logMonitor = ctx.getBean(LoggerMonitorDao.class);
		CreateMethodMock cMock = new CreateMethodMock();
		doAnswer(cMock).when(logMonitor).create(any(LoggerMonitorValue.class));

		// Deploy process
		DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment();
		builder.addInputStream("TestDashboardId.bpmn20.xml", DashboardIdPropagationTest.class
			.getResourceAsStream("/TestDashboardId.bpmn20.xml")).deploy();

		// Start process
		HashMap<String, String> properties = new HashMap<>();
		properties.put("AAA", "111");
		properties.put("BBB", "222");
		properties.put(DashboardIdStore.DASHBOARD_VARIABLE, DBID.toString());
		ProcessDefinition processDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery()
				.processDefinitionKey("testDashboardId").latestVersion().singleResult();
		ProcessInstance processInstance = processEngine.getFormService().submitStartFormData(processDefinition.getId(), properties);
		assertNotNull(processInstance);

		// Wait for process execution, max 15 seconds
		for (int j = 0; j < 15; j++) {
			Thread.sleep(1000);
			if (cMock.i == 2) {
				break;
			}
		}

		assertEquals("Wrong number of fired events", 2, cMock.i);
		assertEquals("Wrong dashboardId propagated", DBID, dashboardId);
       log.info("Finished testDashboardIdPropagation()");
	}

	public static void setDashboardId(Long dashboardId) {
		DashboardIdPropagationTest.dashboardId = dashboardId;
	}

	private static HashMap<Integer,String> checkHash = new HashMap<Integer, String>();
	static {
		checkHash.put(11, "TRACE COMPL_TAG  testDashboardId:1:4 completed with event ACTIVITY_COMPLETED");
		checkHash.put(19, "TRACE COMPL_TAG  testDashboardId:1:4 completed with event ACTIVITY_COMPLETED");
		checkHash.put(24, "TRACE COMPL_TAG  testDashboardId:1:4 completed with event ACTIVITY_COMPLETED");
		checkHash.put(27, "TRACE COMPL_TAG  testDashboardId:1:4 completed with event ACTIVITY_COMPLETED");
		checkHash.put(28, "TRACE COMPL_TAG  testDashboardId:1:4 completed with event PROCESS_COMPLETED");
		checkHash.put(32, "DEBUG SomeTag    Event JOB_EXECUTION_SUCCESS executed in testDashboardId:1:4 (inst: 5, exec: 13)");
	}

	private static class CreateMethodMock implements Answer<Void> {
		int i=0;

		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			LoggerMonitorValue val = (LoggerMonitorValue) invocation.getArguments()[0];
			System.out.format(">>> %3d %-5s %-16s %-10s %-40s\n", ++i, val.getLevel().name(), val.getCategory(), val.getTag(), val.getMessage());

			assertEquals("ACTIVITI_ENGINE", val.getCategory());
			String cmpTo = checkHash.get(i);
			if (cmpTo != null) {
				assertEquals(cmpTo, String.format("%-5s %-10s %-1s", val.getLevel().name(), val.getTag(), val.getMessage()));
			}
			return null;
		}
	};
}
