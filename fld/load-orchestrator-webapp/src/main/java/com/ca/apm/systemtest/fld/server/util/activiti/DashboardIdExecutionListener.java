/**
 * 
 */
package com.ca.apm.systemtest.fld.server.util.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;

/**
 * This execution listener implementation is used to automatically assign the dashboardId to 
 * newly launched sub-processes.  It should be added as an execution listener to processes, 
 * either automatically or by explicit configuration.
 * @author KEYJA01
 *
 */
@SuppressWarnings("serial")
public class DashboardIdExecutionListener implements ExecutionListener {

    /* (non-Javadoc)
     * @see org.activiti.engine.delegate.ExecutionListener#notify(org.activiti.engine.delegate.DelegateExecution)
     */
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        ExecutionEntity ex = (ExecutionEntity) execution;
        ExecutionEntity superEx = ex.getSuperExecution();
        
        if (superEx != null) {
            String key = (String) superEx.getVariable(DashboardIdStore.DASHBOARD_VARIABLE);
            ex.setVariable(DashboardIdStore.DASHBOARD_VARIABLE, key);
        }
        
    }

}
