/**
 * 
 */
package com.ca.apm.systemtest.fld.workflow;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author KEYJA01
 *
 */
public class CancelProcessTest extends BaseActivitiTest {
    
    @Before
    public void setup() {
        processEngine = createProcessEngine();
    }
    
    
    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.workflow.BaseActivitiTest#createProcessEngineConfig()
     */
    @Override
    protected ProcessEngineConfiguration createProcessEngineConfig() {
        StandaloneInMemProcessEngineConfiguration config = (StandaloneInMemProcessEngineConfiguration) ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        String url = config.getJdbcUrl();
        url = url + this.getClass().getSimpleName().substring(0, 5).toUpperCase();
        config.setJdbcUrl(url);
        
        DefaultAsyncJobExecutor asyncExecutor = new DefaultAsyncJobExecutor();
        asyncExecutor.setCorePoolSize(10);
        asyncExecutor.setMaxPoolSize(100);
        asyncExecutor.setMaxAsyncJobsDuePerAcquisition(10);
        asyncExecutor.setMaxTimerJobsPerAcquisition(10);
        asyncExecutor.setTimerLockTimeInMillis(1200000);
        asyncExecutor.setAsyncJobLockTimeInMillis(1200000);
        asyncExecutor.setDefaultAsyncJobAcquireWaitTimeInMillis(78);
        asyncExecutor.setDefaultTimerJobAcquireWaitTimeInMillis(111);
        
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP)
        .setJobExecutorActivate(false)
        .setAsyncExecutorEnabled(true)
        .setAsyncExecutorActivate(true)
        .setAsyncExecutor(asyncExecutor);

        List<ActivitiEventListener> eventListeners = new ArrayList<ActivitiEventListener>();
        eventListeners.add(new ActivitiEventListener() {
            @Override
            public void onEvent(ActivitiEvent event) {
                ActivitiEventType eventType = event.getType();
                String id = event.getProcessInstanceId();
                switch (eventType) {
                    case ENTITY_SUSPENDED:
                        ActivitiEntityEvent aee = (ActivitiEntityEvent) event;
                        System.out.println("Entity suspended: " + aee.getEntity());
                        break;
                    case PROCESS_CANCELLED:
                        System.out.println("Process cancelled: " + id);
                        break;
                    case PROCESS_COMPLETED:
                        System.out.println("Process completed: " + id);
                        break;
                    case JOB_CANCELED:
                        System.out.println("Job cancelled");
                    default:
                        break;
                }
                if (event instanceof ActivitiEntityEvent && event.getType().equals(ActivitiEventType.PROCESS_COMPLETED)) {
                }
            }
            
            @Override
            public boolean isFailOnException() {
                return false;
            }
        });
        
        config.setEventListeners(eventListeners);
        
        return config;
    }
    
    
    @Test
    public void testCancelProcess() throws Exception {
        DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment();
        builder.addInputStream("TestCancel.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/TestCancel.xml"));
        Deployment deployment = builder.deploy();
        Assert.assertNotNull(deployment);
        
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("testCancelProcess");
        System.out.println("Going to wait 20 secs and then delete process instance");
        synchronized (processInstance) {
            processInstance.wait(2000L);
        }
        
        System.out.println("Before suspending");
        processEngine.getRuntimeService().suspendProcessInstanceById(processInstance.getId());
        synchronized (processInstance) {
            processInstance.wait(750L);
        }

        System.out.println("Before cancelling");
        processEngine.getRuntimeService().deleteProcessInstance(processInstance.getId(), "Because I can");
        
        System.out.println("after cancelling");
        synchronized (processInstance) {
            processInstance.wait(100L);
        }
        
        ProcessInstance pi2 = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        Assert.assertNull(pi2);
    }
    
    
    @After
    public void teardown() {
        System.out.println("Closing down processEngine");
        if (processEngine != null) {
            processEngine.close();
        }
    }
}
