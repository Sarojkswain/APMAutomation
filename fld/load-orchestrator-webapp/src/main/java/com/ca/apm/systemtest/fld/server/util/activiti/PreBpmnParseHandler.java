/**
 * 
 */
package com.ca.apm.systemtest.fld.server.util.activiti;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;

/**
 * @author KEYJA01
 *
 */
public class PreBpmnParseHandler extends AbstractBpmnParseHandler<Process> {
    
    public PreBpmnParseHandler() {
        System.out.println("In the PreBpmnParseHandler:: constructor");
    }

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
        
        String listenerClass = DashboardIdExecutionListener.class.getName(); 
        
        ActivitiListener al = new ActivitiListener();
        al.setEvent(ExecutionListener.EVENTNAME_START);
        al.setImplementation(listenerClass);
        al.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        executionListeners.add(al);
    }
}
