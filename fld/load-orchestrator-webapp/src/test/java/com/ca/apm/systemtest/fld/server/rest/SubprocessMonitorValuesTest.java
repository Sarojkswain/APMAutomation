package com.ca.apm.systemtest.fld.server.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ca.apm.systemtest.fld.server.model.Dashboard;
import com.ca.apm.systemtest.fld.shared.vo.DashboardVO;
import com.ca.apm.systemtest.fld.shared.vo.MonitoredValueStatus;
import com.ca.apm.systemtest.fld.shared.vo.MonitoredValueVO;
import com.ca.apm.systemtest.fld.shared.vo.ProcessInstanceVO;
import com.ca.apm.systemtest.fld.shared.vo.Response;

/**
 * Unit test to verify that {@linke DashboardRestController#readProcessMonitors(Long, String)} returns not only the main process's monitors 
 * but also monitors of all its sub-processes (see RTC 368611 for more details). 
 * 
 * @author SINAL04
 *
 */
public class SubprocessMonitorValuesTest extends BasicTest {

    @Before
    public void setup() throws Exception {
        init();
    }

    @After
    public void teardown() throws Exception {
        finish();
    }

    @Test
    public void testSubprocessMonitorsAreVisible() throws InterruptedException {
        txManager.getSessionFactory().getCurrentSession().beginTransaction();

        Map<String, String> resourceMap = new HashMap<>();
        resourceMap.put("ProcessWithMonitorsInsideOtherProcess.bpmn",
            "/ProcessWithMonitorsInsideOtherProcess.bpmn");
        resourceMap.put("ProcessWithMonitorsInsideOtherProcess1.bpmn",
            "/ProcessWithMonitorsInsideOtherProcess1.bpmn");
        resourceMap.put("ProcessWithMonitorsInsideOtherProcess2.bpmn",
            "/ProcessWithMonitorsInsideOtherProcess2.bpmn");
        
        deployWorkflows(resourceMap);

        DashboardVO dVO = new DashboardVO();
        dVO.setId(1L);
        dVO.setName("Nested Monitors Test");
        dVO.setProcessKey("monitor.inside.subprocess.main");
        
        MonitoredValueVO procMonVO = new MonitoredValueVO();
        procMonVO.setKey("proc");
        procMonVO.setName("proc");
        
        MonitoredValueVO subProcMonVO = new MonitoredValueVO();
        subProcMonVO.setKey("sub-proc");
        subProcMonVO.setName("sub-proc");
        
        MonitoredValueVO subSubProcMonVO = new MonitoredValueVO();
        subSubProcMonVO.setKey("sub-sub-proc");
        subSubProcMonVO.setName("sub-sub-proc");
        
        MonitoredValueVO nonExistingMonVO = new MonitoredValueVO();
        nonExistingMonVO.setKey("non-existing");
        nonExistingMonVO.setName("non-existing");

        dVO.setMonitors(Arrays.asList(procMonVO, subProcMonVO, subSubProcMonVO, nonExistingMonVO));

        ResponseEntity<Response> response = null;

        response = dashboardRestController.createDashboard(dVO);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Dashboard d = null;
        if (response != null) {
            d = mapper.map(response.getBody().getDashboard(), Dashboard.class);
        }

        response = dashboardRestController.launchProcess(d.getId(), null);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        
        long startTime = System.currentTimeMillis();
        while (true) {
            response = dashboardRestController.getSubprocesses(d.getId());
            Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
            
            Response respBody = response.getBody();
            Assert.assertNotNull(respBody);
            List<ProcessInstanceVO> processes = respBody.getProcessInstances();
            Assert.assertNotNull(processes);
            System.out.println("Real number of running processes: " + processes.size());
            if (processes.size() == 2) {
                System.out.println("Process number expectation is met, quitting!");
                break;
            }
            //If we got no expected number of processes for a long time, 
            //then break anyway
            if ((System.currentTimeMillis() - startTime) > 10000) {
                System.out.println("Didn't get expected number of processes :(, quitting..");
                break;
            }
            Thread.sleep(1000);    
        }
        
        response = dashboardRestController.readProcessMonitors(d.getId(), null);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        List<MonitoredValueVO> monitors = response.getBody().getMonitors();
        Assert.assertNotNull(monitors);
        Assert.assertTrue(!monitors.isEmpty());
        Assert.assertEquals(4, monitors.size());
        
        Map<String, MonitoredValueVO> monitorsMap = new HashMap<>();
        for (MonitoredValueVO monitor : monitors) {
            monitorsMap.put(monitor.getKey(), monitor);
        }
        
        System.out.println("Getting monitors: ");

        MonitoredValueVO procMonVal = monitorsMap.get("proc");
        Assert.assertNotNull(procMonVal);
        System.out.println("  " + procMonVal.toString());
        Assert.assertEquals(MonitoredValueStatus.OK, procMonVal.getValue());
        
        MonitoredValueVO subProcMonVal = monitorsMap.get("sub-proc");
        Assert.assertNotNull(subProcMonVal);
        System.out.println("  " + subProcMonVal.toString());
        Assert.assertEquals(MonitoredValueStatus.OK, subProcMonVal.getValue());

        MonitoredValueVO subSubProcMonVal = monitorsMap.get("sub-sub-proc");
        Assert.assertNotNull(subSubProcMonVal);
        System.out.println("  " + subSubProcMonVal.toString());
        Assert.assertEquals(MonitoredValueStatus.OK, subSubProcMonVal.getValue());
        
        MonitoredValueVO nonExistingMonVal = monitorsMap.get("non-existing");
        Assert.assertNotNull(nonExistingMonVal);
        System.out.println("  " + nonExistingMonVal.toString());
        Assert.assertEquals(MonitoredValueStatus.Unknown, nonExistingMonVal.getValue());
        
        System.out.println("All Ok!");
    }

    private void deployWorkflows(Map<String, String> resourceMap) {
        for (Entry<String, String> resourceEntry : resourceMap.entrySet()) {
            System.out.println("Deploying process '" + resourceEntry.getKey() + "'");
            DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment();
            builder.addInputStream(resourceEntry.getKey(),
                getClass().getResourceAsStream(resourceEntry.getValue()));
            
            Deployment deployment = builder.deploy();
            Assert.assertNotNull(deployment);
        }
    }

    @Override
    protected void childInit() throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void childFinish() throws Exception {
        // TODO Auto-generated method stub
        
    }


}
