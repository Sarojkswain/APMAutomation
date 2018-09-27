package com.ca.apm.systemtest.fld.server.rest;

import java.util.List;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import com.ca.apm.systemtest.fld.server.model.Dashboard;
import com.ca.apm.systemtest.fld.shared.vo.DashboardVO;
import com.ca.apm.systemtest.fld.shared.vo.Response;
import com.ca.apm.systemtest.fld.shared.vo.UserTaskVO;

/**
 * Unit test to verify Pause/Resume functionality for {@link DashboardRestController} which is implemented in 
 * {@link DashboardRestController#pauseProcess(Long)} and {@linke DashboardRestController#resumeProcess(Long)}.
 * 
 * @author filja01
 *
 */
public class DashboardRCPauseResumeTest extends BasicTest {
    private Logger log = LoggerFactory.getLogger(DashboardRCPauseResumeTest.class);

    @Before
    public void setup() throws Exception {
        init();
    }

    @After
    public void teardown() throws Exception {
        finish();
    }

    @Test
    public void testPauseResume() throws InterruptedException {
        
        DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment();
        builder.addInputStream("UserTaskTest.bpmn20.xml",
            DashboardRCPauseResumeTest.class.getResourceAsStream("/UserTaskTest.bpmn20.xml"));
        Deployment deployment = builder.deploy();
        Assert.notNull(deployment);

        Dashboard d = new Dashboard();
        d.setId(99991L);
        d.setName("dashboard test");
        d.setProcessKey("fld.user.task.test");

        DashboardVO dvo = mapper.map(d, DashboardVO.class);

        ResponseEntity<Response> response = null;

        txManager.getSessionFactory().getCurrentSession().beginTransaction();

        response = dashboardRestController.createDashboard(dvo);
        if (response != null) {
            d = mapper.map(response.getBody().getDashboard(), Dashboard.class);
        }

        response = dashboardRestController.launchProcess(d.getId(), null);

        // get the starting numbers of processes (running and paused)
        ProcessInstanceQuery query = processEngine.getRuntimeService()
            .createProcessInstanceQuery()
            .processDefinitionKey(d.getProcessKey());
        int runProc = query.active().list().size();
        int pausProc = query.suspended().list().size();
        boolean catchException = false;
        try {
            dashboardRestController.pauseProcess(d.getId());
            Thread.sleep(100);
            dashboardRestController.pauseProcess(d.getId());
        } catch (DashboardException e) {
            catchException = true;
        }
        Assert.state(!catchException, "Pause called multiple times should to throw no error.");

        // check number of running and paused (running should be -1, and paused +1)
        int runProcP = query.active().list().size();
        int pausProcP = query.suspended().list().size();

        Assert.state(runProc + pausProc == runProcP + pausProcP,
            "Total number of processes (paused + running) should be same");
        Assert.state(runProc == runProcP + 1, "Should be one less active process!");
        Assert.state(pausProc + 1 == pausProcP, "Should be one more suspended process!");

        catchException = false;
        try {
            dashboardRestController.resumeProcess(d.getId());
            Thread.sleep(100);
            dashboardRestController.resumeProcess(d.getId());
            System.out.println("foo foo foo");
        } catch (DashboardException e) {
            catchException = true;
        }
        Assert.state(!catchException, "Resume called multiple times should to throw no error.");

        int runProcR = query.active().list().size();
        int pausProcR = query.suspended().list().size();

        Assert.state(runProcR + pausProcR == runProcP + pausProcP,
            "Number of processes should be same");
        Assert.state(runProcR == runProcP + 1, "Should be addedd back one active process!");
        Assert.state(pausProcR + 1 == pausProcP, "Should be less one suspended process!");

        Thread.sleep(100);
        response = dashboardRestController.retrieveWaitingUserTask(d.getId());
        List<UserTaskVO> tasks = response.getBody().getWaitingUserTasks();
        System.out.println("Running usertasks:" + tasks.size());
        int tSize = tasks.size();

        dashboardRestController.pauseProcess(d.getId());
        Thread.sleep(500);
        response = dashboardRestController.retrieveWaitingUserTask(d.getId());
        tasks = response.getBody().getWaitingUserTasks();

        Assert.state(tasks.size() == 0, "Process is suspended, no task should be waiting!");
        System.out.println("Running usertasks:" + tasks.size());

        dashboardRestController.resumeProcess(d.getId());
        Thread.sleep(500);
        response = dashboardRestController.retrieveWaitingUserTask(d.getId());
        tasks = response.getBody().getWaitingUserTasks();

        Assert.state(tasks.size() == tSize,
            "Process is resumed, same number of tasks should be waiting!");
        System.out.println("Running usertasks:" + tasks.size());

        for (int j = 0; j < tasks.size(); j++) {
            dashboardRestController.completeWaitingUserTask(d.getId(), tasks.get(j));
            int sks =
                dashboardRestController.retrieveWaitingUserTask(d.getId()).getBody()
                    .getWaitingUserTasks().size();
            log.info("Completed user task: remaining usertasks: " + sks);
        }

        int active = query.active().list().size();
        int stopped = query.suspended().list().size();
        int processes = active + stopped;
        System.out.println("Running processeses:" + processes + " active " + active
            + ", suspended " + stopped);

        catchException = false;
        Thread.sleep(500);
        try {
            dashboardRestController.pauseProcess(d.getId());
        } catch (DashboardException e) {
            catchException = true;
        }
        Assert.state(catchException, "Pause of the ended process should to throw an error.");

        catchException = false;
        Thread.sleep(500);
        try {
            dashboardRestController.resumeProcess(d.getId());
        } catch (DashboardException e) {
            catchException = true;
        }
        Assert.state(catchException, "Resume of the ended process should to throw an error.");
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
