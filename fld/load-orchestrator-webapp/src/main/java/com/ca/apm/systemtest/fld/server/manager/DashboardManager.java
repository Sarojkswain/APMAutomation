package com.ca.apm.systemtest.fld.server.manager;

import java.util.List;

import org.activiti.engine.runtime.Execution;

import com.ca.apm.systemtest.fld.server.model.ConfigItem;
import com.ca.apm.systemtest.fld.server.model.Dashboard;
import com.ca.apm.systemtest.fld.server.model.MonitoredValue;
import com.ca.apm.systemtest.fld.server.rest.DashboardException;
import com.ca.apm.systemtest.fld.shared.vo.DashboardVO;
import com.ca.apm.systemtest.fld.shared.vo.ExecutionVO;
import com.ca.apm.systemtest.fld.shared.vo.MonitoredValueVO;

public interface DashboardManager {
    
    public List<Dashboard> getAllDashboards() throws DashboardException;
    
    public List<Dashboard> getDashboardsByProcessKey(String processKey) throws DashboardException;
    
    public Dashboard getDashboard(final long dashboardId) throws DashboardException;

    public Dashboard cloneDashboardById(final long dashboardId, boolean persist) throws DashboardException;
    
    public Dashboard cloneDashboard(Dashboard dashboard, boolean persist) throws DashboardException;
    
    public List<ConfigItem> cloneConfigItems(List<ConfigItem> configItems) throws DashboardException;

    public Dashboard createDashboard(Dashboard dashboard) throws DashboardException;
    
    public Dashboard createDashboardFromVO(DashboardVO dashboardVO) throws DashboardException;
    
    public Dashboard updateDashboard(Dashboard dashboard) throws DashboardException;
    
    public Dashboard updateDashboardFromVO(DashboardVO dashboardVO) throws DashboardException;
    
    public void deleteDashboard(Dashboard dashboard) throws DashboardException;
    
    public void deleteAllDashboards() throws DashboardException;
    
    public void deleteDashboardById(Long id) throws DashboardException;
    
    public DashboardVO convertDashboardToVO(Dashboard dashboard) throws DashboardException;
    
    public DashboardVO convertDashboardByIdToVO(final long dashboardId) throws DashboardException;

    public Dashboard convertVOToDashboard(DashboardVO dashboardVO) throws DashboardException;
    
    public MonitoredValue convertMonitorFromVO(MonitoredValueVO monitoredValueVO) throws DashboardException;
    
    public List<ExecutionVO> getExecutions(final Long dashboardId) throws DashboardException;

    public boolean isDashboardRunning(final Long dashboardId) throws DashboardException;
    
    public boolean isDashboardRunning(Dashboard dashboard) throws DashboardException;
    
    public void setMonitoredValue(Execution execution, String monitoredValueKey, boolean value);
    
    /**
     * Configures a new monitored value in the dashboard containing the specified execution.  If the 
     * monitored value already exists, no changes are performed, otherwise the configuration is updated 
     * with the new monitored value.
     * @param execution The execution (and yes, a ProcessInstance is also an execution)
     * @param key
     * @param name
     */
    public void configureMonitoredValue(Execution execution , String key, String name);
    
    /**
     * Converts a dashboard value object to a dashboard entity instance to be saved to the database as the 
     * imported dashboard. If there is any mismatch in dashboard configuration parameters and workflow startup 
     * form options and <code>forceConfigParamsUpdate</code> is <code>false</code> then this method will fail with a 
     * {@linke DashboardException}, otherwise if <code>forceConfigParamsUpdate</code> is <code>true</code> 
     * the dashboard config parameters get adopted to the startup form options automatically.   
     * 
     * @param  dashboardVO               dashboard VO representing the dashboard to be imported
     * @param  forceConfigParamsUpdate   whether any config parameters missmatch between the dashboard 
     *                                   and the respective process definition should be automatically resolved;
     *                                   if not, this method will fail with {@link DashboardException}
     * @return dashboard entity to be saved to accomplish the import
     * 
     */
    public Dashboard convertImportVOToDashboard(DashboardVO dashboardVO, boolean forceConfigParamsUpdate);
}
