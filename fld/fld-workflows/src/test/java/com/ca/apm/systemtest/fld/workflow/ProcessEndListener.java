/**
 * 
 */
package com.ca.apm.systemtest.fld.workflow;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * @author KEYJA01
 *
 */
@SuppressWarnings("serial")
public class ProcessEndListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        System.out.println(execution.getEventName());
    }
}
