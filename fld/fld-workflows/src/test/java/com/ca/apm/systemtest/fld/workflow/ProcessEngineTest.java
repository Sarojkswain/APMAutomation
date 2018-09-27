/**
 * 
 */
package com.ca.apm.systemtest.fld.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 * @author KEYJA01
 *
 */
public class ProcessEngineTest extends BaseActivitiTest {
    private static final String TEST_STATUS_PROCESS = "testStatusProcess";
    private static final Object lock = new Object();
    private String processInstanceId = null;
    private boolean completed = false;
    public static long start;


    @Before
    public void setup() {
        processEngine = createProcessEngine();
    }
    
    /**
     * Creates a basic configuration for the process engine. Subclasses should override if they need more flexibility
     * @return
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
        asyncExecutor.setDefaultAsyncJobAcquireWaitTimeInMillis(97);
        asyncExecutor.setDefaultTimerJobAcquireWaitTimeInMillis(150);
/*
        <property name="corePoolSize" value="3" />
        <property name="maxPoolSize" value="30" />
        <property name="keepAliveTime" value="20000" />
        <property name="queueSize" value="200" />
        <property name="maxTimerJobsPerAcquisition" value="3" />
        <property name="maxAsyncJobsDuePerAcquisition" value="3" />
        <property name="defaultAsyncJobAcquireWaitTimeInMillis" value="5000" />
        <property name="defaultTimerJobAcquireWaitTimeInMillis" value="5000" />
        <property name="timerLockTimeInMillis" value="1200000" />
        <property name="asyncJobLockTimeInMillis" value="1200000" />
 */
        
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP)
                .setJobExecutorActivate(false)
                .setAsyncExecutorEnabled(true)
                .setAsyncExecutorActivate(true)
                .setAsyncExecutor(asyncExecutor);
        
        List<ActivitiEventListener> eventListeners = new ArrayList<ActivitiEventListener>();
        eventListeners.add(new ActivitiEventListener() {
            @Override
            public void onEvent(ActivitiEvent event) {
                if (event instanceof ActivitiEntityEvent && event.getType().equals(ActivitiEventType.PROCESS_COMPLETED)) {
                    String id = event.getProcessInstanceId();
                    if (id != null && id.equals(processInstanceId)) {
                        // processId matches for the PROCESS_COMPLETED event, mark it as completed
                        completed = true;
                        Timer t = new Timer();
                        t.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                synchronized(lock) {
                                    lock.notify();
                                }
                            }
                        }, 1000L);
                    }
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
    
    /**
     * Test that the PROCESS_COMPLETE event is being properly fired so that the dashboards can be notified when the embedded process has ended.
     */
    @Test
    public void testProcessStatus() {
        DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment();
        builder.addInputStream("TestActiveState.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/TestActiveState.xml"));
        Deployment deployment = builder.deploy();
        Assert.notNull(deployment);
        
        start = System.currentTimeMillis();
        ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey(TEST_STATUS_PROCESS);
        Assert.notNull(instance);
        processInstanceId = instance.getId();
        
        // at this point the instance should be waiting for the user to take an action, but active
        instance = processEngine.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(TEST_STATUS_PROCESS).singleResult();
        // should not be ended or suspended
        Assert.isTrue(!instance.isEnded());
        Assert.isTrue(!instance.isSuspended());
        System.out.println(elapsed() + " ---- should be waiting in user task");
        
        // find the user task and complete it, which will advance to the next step in the workflow
        TaskQuery taskQuery = processEngine.getTaskService().createTaskQuery();
        Task task = taskQuery.active().singleResult();
        Assert.notNull(task);
        processEngine.getTaskService().complete(task.getId());
        System.out.println(elapsed() + " ---- completed user task");

        // now we are waiting in a timer event for 5 seconds, check that the process is still active
        instance = processEngine.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(TEST_STATUS_PROCESS).singleResult();
        Assert.isTrue(!instance.isEnded());
        Assert.isTrue(!instance.isSuspended());
        
        synchronized (lock) {
            try {
                // wait for up to 20 seconds for the workflow to finish.  If it finishes earlier,
                // it will call lock.notify() and allow the test to proceed
                lock.wait(20000L);
            } catch (InterruptedException e) {
                // really not interested in this exception
            }
        }
        
        // after the process instance has completed, the query should return null
        instance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(instance.getId()).singleResult();
        Assert.isNull(instance);
        Assert.isTrue(completed);
    }
    
    
    public static String elapsed() {
        Long ms = System.currentTimeMillis() - start;
        Float f = ms.floatValue() / 1000.0f;
        return f.toString(); 
    }
    
    
    @After
    public void teardown() {
        try {
            processEngine.close();
        } catch (Exception e) {
            // do nothing
        }
        processEngine = null;
    }
}
