package com.ca.apm.systemtest.fld.workflow;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

@SuppressWarnings("serial")
public class SubProcessExecutionListener implements ExecutionListener {
    
    public SubProcessExecutionListener() {
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        ExecutionEntity ex = (ExecutionEntity) execution;
        ExecutionEntity superEx = ex.getSuperExecution();
        
        String key = "";
        if (superEx == null) {
            key = ex.getBusinessKey();
        } else {
            key = (String) superEx.getVariable(SubProcessTest.BUSINESS_KEY);
        }
        
        ex.setVariable(SubProcessTest.BUSINESS_KEY, key);
    }
    
}