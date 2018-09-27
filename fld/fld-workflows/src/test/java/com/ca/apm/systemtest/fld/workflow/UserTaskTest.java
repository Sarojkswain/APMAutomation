/**
 * 
 */
package com.ca.apm.systemtest.fld.workflow;

import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author keyja01
 *
 */
public class UserTaskTest extends BaseActivitiTest {
    
    
    @Before
    public void setup() {
        processEngine = createProcessEngine();
    }
    
    
    @After
    public void teardown() {
        if (processEngine != null) {
            processEngine.close();
        }
    }
    
    @Test
    public void testUserTask() throws Exception {
        DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment();
        builder.addInputStream("UserTaskTest.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/UserTaskTest.xml"));
        builder.addInputStream("UserTaskTestSubProcess.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/UserTaskTestSubProcess.xml"));
        Deployment deployment = builder.deploy();
        Assert.assertNotNull(deployment);
        
        RuntimeService rs = processEngine.getRuntimeService();
        ProcessInstance process = rs.startProcessInstanceByKey("usertask.main");
        System.out.println(process.getId() + " " + process.getProcessInstanceId());
        
        
        List<ProcessInstance> pl = rs.createProcessInstanceQuery().list();
        for (ProcessInstance p: pl) {
            System.out.println(p + ", " + p.getId() + ", " + p.getParentId());
        }
        
        TaskService ts = processEngine.getTaskService();
        NativeTaskQuery ntq = ts.createNativeTaskQuery();
        ntq.sql("select * from ACT_RU_TASK order by ID_ asc");
        
        List<Execution> el = rs.createExecutionQuery().list();
        for (Execution e: el) {
            System.out.println(e + ", " + e.getId() + ", parent: " + e.getParentId() + ", processInstanceId: " + e.getProcessInstanceId());
        }
        
        System.out.println("-----------------------------------");
        List<Task> tl = ntq.list();
        for (Task t: tl) {
            System.out.println(t + ", procId: " + t.getProcessInstanceId() + ", executionId: " + t.getExecutionId());
        }
        
        
        TaskQuery tq = ts.createTaskQuery();
        List<Task> list = tq.processInstanceId(process.getId()).list();
        for (Task t: list) {
            System.out.println(t);
        }
        System.out.println("Now querying by execution");
        List<Execution> executions = rs.createExecutionQuery().parentId(process.getProcessInstanceId()).list();
        for (Execution e: executions) {
            tq = ts.createTaskQuery();
            list = tq.executionId(e.getId())
                .list();
            for (Task t: list) {
                System.out.println(t);
            }
        }
        
        
        System.out.println("Now listing all active");
        list = ts.createTaskQuery().active().processInstanceId(process.getProcessInstanceId()).list();
        for (Task t: list) {
            System.out.println(t + " --> processInstanceId" + t.getProcessInstanceId());
        }
    }
}
