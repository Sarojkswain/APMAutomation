/**
 * 
 */
package com.ca.apm.systemtest.fld.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author KEYJA01
 *
 */
public class SubProcessTest extends BaseActivitiTest {
    public static final String BUSINESS_KEY = "BusinessKey";

    public static class MyParseHandler extends AbstractBpmnParseHandler<Process>{

        @Override
        protected Class<? extends BaseElement> getHandledType() {
            return Process.class;
        }

        @Override
        protected void executeParse(BpmnParse bpmnParse, Process p) {
            List<ActivitiListener> executionListeners = p.getExecutionListeners();
            if (executionListeners == null) {
                executionListeners = new ArrayList<>();
                p.setExecutionListeners(executionListeners);
            }
            if (executionListeners.size() > 0) {
                executionListeners.clear();
            }
            
            String listenerClass = SubProcessExecutionListener.class.getName(); 
            
            ActivitiListener al = new ActivitiListener();
            al.setEvent(ExecutionListener.EVENTNAME_START);
            al.setImplementation(listenerClass);
            al.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
            executionListeners.add(0, al);
            
            System.out.println(p);
        }
        
    }
    
    private RuntimeService runtimeService;


    @Before
    public void setup() {
        processEngine = createProcessEngine();
        runtimeService = processEngine.getRuntimeService();
    }
    
    @After
    public void teardown() {
        if (processEngine != null) {
            processEngine.close();
            processEngine = null;
        }
    }
    
    @Test
    public void testSubProcessExecutionQuery() {
        DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment();
        builder.addInputStream("TestMainProcess.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/subprocess/TestMainProcess.xml"));
        builder.addInputStream("TestSubProcess1.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/subprocess/TestSubProcess1.xml"));
        builder.addInputStream("TestSubProcess2.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/subprocess/TestSubProcess2.xml"));
        builder.addInputStream("TestSubProcess3.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/subprocess/TestSubProcess3.xml"));
        Deployment deployment = builder.deploy();
        Assert.assertNotNull(deployment);
        
        String businessKey = "bk" + System.currentTimeMillis();
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("test.main.process", businessKey);
        System.out.println("Started process with id " + processInstance.getId());
        
        List<Execution> list = runtimeService.createExecutionQuery().variableValueEquals(BUSINESS_KEY, businessKey).list();
        assertEquals("There should be exactly four open executions", list.size(), 4);
    }
    
    @Test 
    public void testSubProcessesRecursiveApproach() {
        DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment();
        builder.addInputStream("TestMainProcess.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/subprocess/TestMainProcess.xml"));
        builder.addInputStream("TestSubProcess1.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/subprocess/TestSubProcess1.xml"));
        builder.addInputStream("TestSubProcess2.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/subprocess/TestSubProcess2.xml"));
        builder.addInputStream("TestSubProcess3.bpmn20.xml", ProcessEngineTest.class.getResourceAsStream("/subprocess/TestSubProcess3.xml"));
        Deployment deployment = builder.deploy();
        Assert.assertNotNull(deployment);
        
        String businessKey = "bk" + System.currentTimeMillis();
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("test.main.process", businessKey);
        System.out.println("Started process with id " + processInstance.getId());

        Map<String, ProcessInstance> subProcesses = collectAllSubprocess(processInstance.getId(), null);
        System.out.println("All subprocesses: " + subProcesses);
        assertNotNull(subProcesses);
        assertEquals("There should be exactly 3 running subprocesses", subProcesses.size(), 3);
    }
    
    @Override
    protected ProcessEngineConfiguration createProcessEngineConfig() {
        StandaloneInMemProcessEngineConfiguration config = (StandaloneInMemProcessEngineConfiguration) super.createProcessEngineConfig();
        
        List<BpmnParseHandler> parseHandlers = config.getPreBpmnParseHandlers();
        if (parseHandlers == null) {
            parseHandlers = new ArrayList<>();
            config.setPreBpmnParseHandlers(parseHandlers);
        }
        parseHandlers.add(new MyParseHandler());
        
        return config;
    }

    /**
     * Recursively gets all the subprocesses for the process with <code>processInstanceId</code>.
     * 
     * @param    processInstanceId  process id of the process for which we would like to have all our subprocesses
     * @param    subprocessesMap    map containing pairs "processInstanceId" to <code>ProcessInstance</code>
     * @return
     */
    private Map<String, ProcessInstance> collectAllSubprocess(String processInstanceId, Map<String, ProcessInstance> subprocessesMap) {
        //Make sure we don't get an NPE.
        subprocessesMap = subprocessesMap == null ? new HashMap<String, ProcessInstance>() : subprocessesMap;
        List<ProcessInstance> subProcessInstances = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstanceId).list();
        for (ProcessInstance subprocess : subProcessInstances) {
            if (!subprocessesMap.containsKey(subprocess.getId())) {
                subprocessesMap.put(subprocess.getId(), subprocess);

                //Go see if we don't have subprocesses which may have their own subprocesses.
                collectAllSubprocess(subprocess.getId(), subprocessesMap);
            }
        }
        return subprocessesMap;
    }

}
