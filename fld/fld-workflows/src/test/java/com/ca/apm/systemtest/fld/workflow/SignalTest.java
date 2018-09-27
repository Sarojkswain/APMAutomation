/**
 * 
 */
package com.ca.apm.systemtest.fld.workflow;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author KEYJA01
 *
 */
public class SignalTest extends BaseActivitiTest {
    private ProcessEngine processEngine;
    
    @Before
    public void setup() throws Exception {
        processEngine = createProcessEngine();
    }
    

    @Test
    public void testProcessSignal() throws Exception {
        DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment();
        builder.addInputStream("TestSignal.bpmn20.xml", SignalTest.class.getResourceAsStream("/TestSignal.xml"));
        builder.deploy();
        
        ProcessDefinition pd = processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testSignal").singleResult();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("procName", "Process #1");
        ProcessInstance processInstance1 = processEngine.getFormService().submitStartFormData(pd.getId(), properties);
        
        properties.clear();
        properties.put("procName", "Process #2");
        ProcessInstance processInstance2 = processEngine.getFormService().submitStartFormData(pd.getId(), properties);
        
        waitFor(3000L);
        
        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance1.getId()).taskDefinitionKey("usertask1").singleResult();
        processEngine.getTaskService().complete(task.getId());
        
        // at this point the process should end
        waitFor(1000L);
        Assert.assertNull(processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult());
        Assert.assertNotNull(processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult());
    }
    
    
    private void waitFor(long ms) throws Exception {
        synchronized (processEngine) {
            processEngine.wait(ms);
        }
    }


    public void teardown() throws Exception {
        processEngine.close();
    }
}
