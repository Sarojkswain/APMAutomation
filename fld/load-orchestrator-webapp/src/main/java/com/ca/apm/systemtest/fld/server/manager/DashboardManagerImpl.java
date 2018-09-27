package com.ca.apm.systemtest.fld.server.manager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.dozer.Mapper;
import org.dozer.MappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.server.dao.DashboardDao;
import com.ca.apm.systemtest.fld.server.listener.EventListener;
import com.ca.apm.systemtest.fld.server.model.ConfigItem;
import com.ca.apm.systemtest.fld.server.model.Dashboard;
import com.ca.apm.systemtest.fld.server.model.DashboardConfig;
import com.ca.apm.systemtest.fld.server.model.MonitoredValue;
import com.ca.apm.systemtest.fld.server.rest.DashboardException;
import com.ca.apm.systemtest.fld.server.rest.DashboardException.ErrorCode;
import com.ca.apm.systemtest.fld.server.util.ConfigItemUtil;
import com.ca.apm.systemtest.fld.shared.vo.ConfigItemVO;
import com.ca.apm.systemtest.fld.shared.vo.DashboardVO;
import com.ca.apm.systemtest.fld.shared.vo.ExecutionVO;
import com.ca.apm.systemtest.fld.shared.vo.MonitoredValueVO;
import com.google.common.base.MoreObjects;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Default implementing class for common dashboard operations declared in {@link DashboardManager}. 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
public class DashboardManagerImpl implements DashboardManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardManagerImpl.class);

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private Mapper mapper;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FormService formService;

    public DashboardManagerImpl() {

    }

    @Override
    public Dashboard getDashboard(long dashboardId) throws DashboardException {
        Dashboard dashboard = dashboardDao.find(dashboardId);
        if (dashboard == null) {
            throw logError(ErrorCode.DashboardNotFound, 
                           "The dashboard for id {0} could not be found", dashboardId);
        }
        
        /*
         * Now we need to check the dashboard is not attached to some older process definition,
         * in other words, if there's a fresher process definition which might be different in 
         * process form parameters comparing to the older version.
         */
        ProcessDefinitionQuery getLatestProcDefQuery = repositoryService
            .createProcessDefinitionQuery().processDefinitionKey(dashboard.getProcessKey())
            .latestVersion();
        ProcessDefinition latestProcDef = getLatestProcDefQuery.singleResult();
        if (latestProcDef == null) {
            throw logError(ErrorCode.ProcessNotFound, 
                           "Dashboard with id {0} is not attached to any process definition!",
                           dashboardId);
        }

        updateConfigParams(latestProcDef, dashboard);
        return dashboard;
    }

    @Override
    public Dashboard convertImportVOToDashboard(DashboardVO dashboardVO, boolean forceConfigParamsUpdate) {
        if (dashboardVO == null) {
            throw logError(ErrorCode.InvalidParameter, "Dashboard is null!");
        }
        if (isBlank(dashboardVO.getProcessKey())) {
            throw logError(ErrorCode.ProcessNotFound, 
                           "Import dashboard named ''{0}'' has a blank process key=''{1}''!", 
                           dashboardVO.getProcessKey());
        }
        
        String processKey = dashboardVO.getProcessKey();
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(processKey).latestVersion().singleResult();
        if (procDef == null) {
            throw logError(ErrorCode.ProcessNotFound, 
                "No process definition with key ''{0}'' found!", processKey);
        }

        Dashboard dashboard = null;
        StartFormData formData = formService.getStartFormData(procDef.getId());
        List<FormProperty> formProps = formData.getFormProperties() != null ? formData.getFormProperties() : Collections.<FormProperty>emptyList();
        
        if (!forceConfigParamsUpdate) {
            //Validate dashboard configuration against the process form properties
            List<ConfigItemVO> configVOItems = dashboardVO.getConfig() != null ? dashboardVO.getConfig() : Collections.<ConfigItemVO>emptyList();

            if (configVOItems.isEmpty() && !formProps.isEmpty()) {
                throw logError(ErrorCode.InvalidParameter, 
                    "Dashboard ''{0}'' configuration is empty while the process ''{1}'' has {2} form properties!", 
                    dashboardVO.getName(), procDef.getKey(), formProps.size());
            } else if (!configVOItems.isEmpty() && formProps.isEmpty()) {
                throw logError(ErrorCode.InvalidParameter, 
                    "Dashboard ''{0}'' configuration has ''{1}'' parameters while the process ''{2}'' has no form properties!",
                    dashboardVO.getName(), configVOItems.size(), procDef.getKey());
            } else if (configVOItems.size() != formProps.size()) {
                HashSet<String> left = new HashSet<>();
                for (ConfigItemVO item: configVOItems) {
                    left.add(item.getFormId());
                }
                HashSet<String> right = new HashSet<>();
                for (FormProperty fp: formProps) {
                    right.add(fp.getId());
                }
                HashSet<String> left2 = new HashSet<>(left);
                HashSet<String> right2 = new HashSet<>(right);
                
                left.removeAll(right2);
                right.removeAll(left2);
                for (String name: left) {
                    logWarn("Extra config item only in JSON: {0}", name);
                }
                for (String name: right) {
                    logWarn("Missing new form parameter {0}", name);
                }
                
                throw logError(ErrorCode.InvalidParameter, 
                    "Number of dashboard ''{0}'' configuration parameters ({1}) does not match the number of process ''{2}'' form properties ({3})", 
                    dashboardVO.getName(), configVOItems.size(), procDef.getKey(), formProps.size());
            } else {
                Map<String, FormProperty> formPropsMap = new HashMap<>();
                for (FormProperty formProp : formProps) {
                    formPropsMap.put(formProp.getId(), formProp);
                }
                    
                for (ConfigItemVO configVO : configVOItems) {
                    FormProperty formProp = formPropsMap.get(configVO.getFormId());
                    if (formProp == null) {
                        throw logError(ErrorCode.InvalidParameter, 
                            "Process ''{0}'' has no form property with id=''{1}''",
                            procDef.getKey(), configVO.getFormId());
                    }
                    if (!configVO.getType().equals(formProp.getType().getName())) {
                        throw logError(ErrorCode.InvalidParameter, 
                            "Process ''{0}'' form property ''{1}'' has type ''{2}'' while dashboard ''{3}'' configuration parameter ''{4}'' has type of ''{5}''!",
                            procDef.getKey(), formProp.getId(), formProp.getType().getName(), 
                            dashboardVO.getName(), configVO.getFormId(), configVO.getType());
                    }
                }
            }
            dashboard = convertVOToDashboard(dashboardVO);
        } else {
            dashboard = convertVOToDashboard(dashboardVO);
            List<ConfigItem> updatedConfigItems = null;
            try {
                updatedConfigItems = ConfigItemUtil
                    .updateConfigItems(formProps, dashboard.getDashboardConfig().getConfigItems());
            } catch (Exception e) {
                throw logError(e, ErrorCode.UnknownError, "Failed to update dashboard config parameters!");
            }
            if (updatedConfigItems != null && !updatedConfigItems.isEmpty()) {
                dashboard.getDashboardConfig().setConfigItems(updatedConfigItems);
            }
        }

        dashboard.setProcessDefinitionVersion(procDef.getVersion());
        return dashboard;
    }

    public void updateConfigParams(ProcessDefinition procDef, Dashboard dashboard) {
        if (procDef == null) {
            String msg = "Process definition is null!";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(msg);
            }
            throw new DashboardException(ErrorCode.InvalidParameter, msg);
        }

        if (dashboard == null) {
            String msg = "Dashboard is null!";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(msg);
            }
            throw new DashboardException(ErrorCode.InvalidParameter, msg);
        }

        /*
         * We can upgrade dashboard's configuration only in case it's not running now.
         */
        if ((dashboard.getProcessDefinitionVersion() == null
            || dashboard.getProcessDefinitionVersion() != procDef.getVersion())
            && !isDashboardRunning(dashboard)) {
            StartFormData startFormData = formService.getStartFormData(procDef.getId());
            List<FormProperty> formProps = startFormData.getFormProperties();
            List<ConfigItem> updatedConfigItems = null;
            try {
                updatedConfigItems = ConfigItemUtil
                    .updateConfigItems(formProps, dashboard.getDashboardConfig().getConfigItems());
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(e.getMessage(), e.getCause());
                }
                throw new DashboardException(ErrorCode.UnknownError, e.getMessage());
            }
            if (updatedConfigItems != null && !updatedConfigItems.isEmpty()) {
                dashboard.getDashboardConfig().setConfigItems(updatedConfigItems);
            }
            dashboard.setProcessDefinitionVersion(procDef.getVersion());
            updateDashboard(dashboard);
        }

    }

    @Override
    public Dashboard createDashboard(Dashboard dashboard) throws DashboardException {
        dashboardDao.create(dashboard);
        return dashboard;
    }

    @Override
    public Dashboard createDashboardFromVO(DashboardVO dashboardVO) throws DashboardException {
        Dashboard dashboard = new Dashboard();
        DashboardConfig dashboardConfig = new DashboardConfig();

        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        ProcessDefinition processDef =
            query.processDefinitionKey(dashboardVO.getProcessKey()).latestVersion().singleResult();
        List<ConfigItem> configItems = new ArrayList<>(10);
        if (processDef != null) {
            //set process definition version
            dashboard.setProcessDefinitionVersion(processDef.getVersion());
            StartFormData formData = formService.getStartFormData(processDef.getId());
            List<FormProperty> formProps = formData.getFormProperties();
            for (FormProperty prop : formProps) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("form property: {}",
                        MoreObjects.toStringHelper(prop).add("name", prop.getName())
                            .add("id", prop.getId()).add("type", prop.getType())
                            .add("value", prop.getValue()));
                }
                if (prop.getType() == null) {
                    // It can happen when BPMN IDE saves empty <activiti:formProperty/> element
                    // in the file.
                    continue;
                }

                ConfigItem item = null;
                try {
                    item = ConfigItemUtil.convertFormProperty(prop);
                } catch (Exception e) {
                    throw new DashboardException(ErrorCode.InvalidParameter,
                        "Failed to convert form properties to dashboard config parameters!", e);
                }
                configItems.add(item);
            }
        } else {
            String errMessage = MessageFormat.format("No such process with key=''{0}'' found!",
                dashboardVO.getProcessKey());
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(errMessage);
            }
            throw new DashboardException(ErrorCode.ProcessNotFound, errMessage);
        }

        dashboardConfig.setConfigItems(configItems);

        if (dashboardVO.getMonitors() != null) {
            List<MonitoredValue> monitors = new ArrayList<>(dashboardVO.getMonitors().size());
            dashboardConfig.setMonitors(monitors);
            for (MonitoredValueVO vo : dashboardVO.getMonitors()) {
                MonitoredValue monitoredValue = convertMonitorFromVO(vo);
                monitors.add(monitoredValue);
            }
        }

        dashboardConfig.setVersion(1);
        dashboardConfig.setHideNonRequiredConfigParameters(false);
        dashboard.setDashboardConfig(dashboardConfig);
        dashboard.setId(null);
        dashboard.setName(dashboardVO.getName());
        dashboard.setProcessKey(dashboardVO.getProcessKey());
        dashboard.setIconName(dashboardVO.getIconName());

        // map our newly created dashboard back to the value object
        return createDashboard(dashboard);
    }

    @Override
    public void deleteDashboard(Dashboard dashboard) throws DashboardException {
        dashboardDao.delete(dashboard);
    }

    @Override
    public void deleteDashboardById(Long id) throws DashboardException {
        dashboardDao.deleteById(id);
    }

    @Override
    public void deleteAllDashboards() throws DashboardException {
        dashboardDao.deleteAll();
    }

    @Override
    public List<Dashboard> getAllDashboards() throws DashboardException {
        return dashboardDao.findAll();
    }

    @Override
    public List<ConfigItem> cloneConfigItems(List<ConfigItem> configItems)
        throws DashboardException {
        List<ConfigItem> clonedConfigItems = new ArrayList<>(
            configItems != null ? configItems.size() : 0);
        if (configItems != null) {
            for (ConfigItem configItem : configItems) {
                ConfigItem configItemClone = null;
                synchronized (mapper) {
                    configItemClone = mapper.map(configItem, configItem.getClass());
                }
                configItemClone.setId(null);
                LOGGER.info("Original config item: {}", configItem);
                LOGGER.info("Cloned config item: {}", configItemClone);
                clonedConfigItems.add(configItemClone);
            }
        }
        return clonedConfigItems;
    }

    @Override
    public Dashboard cloneDashboard(Dashboard dashboard,
        boolean persist) throws DashboardException {
        DashboardConfig dashConfig = new DashboardConfig();

        if (dashboard.getDashboardConfig().getConfigItems() != null) {
            dashConfig
                .setConfigItems(cloneConfigItems(dashboard.getDashboardConfig().getConfigItems()));
        }

        if (dashboard.getDashboardConfig().getMonitors() != null) {
            List<MonitoredValue> monitors
                = new ArrayList<>(dashboard.getDashboardConfig().getMonitors().size());
            dashConfig.setMonitors(monitors);
            for (MonitoredValue mv : dashboard.getDashboardConfig().getMonitors()) {
                monitors.add(new MonitoredValue(mv.getKey(), mv.getName(), mv.getGroup()));
            }
        }

        Dashboard dashboardClone = new Dashboard();
        dashboardClone.setName(dashboard.getName());
        dashboardClone.setIconName(dashboard.getIconName());
        dashboardClone.setProcessKey(dashboard.getProcessKey());
        dashboardClone.setId(null);
        dashboardClone.setDashboardConfig(dashConfig);
        dashboardClone.setProcessDefinitionVersion(dashboard.getProcessDefinitionVersion());
        if (persist) {
            return createDashboard(dashboardClone);
        }
        return dashboardClone;
    }

    @Override
    public Dashboard cloneDashboardById(long dashboardId,
        boolean persist) throws DashboardException {
        return cloneDashboard(getDashboard(dashboardId), persist);
    }

    @Override
    public Dashboard updateDashboard(Dashboard dashboard) throws DashboardException {
        dashboardDao.update(dashboard);
        return dashboard;
    }

    @Override
    public Dashboard updateDashboardFromVO(DashboardVO dashboardVO) throws DashboardException {
        if (dashboardVO == null) {
            throw new DashboardException(ErrorCode.InvalidParameter, "Dashboard is null!");
        }

        Dashboard dashboard = getDashboard(dashboardVO.getId());
        Dashboard fromVO = convertVOToDashboard(dashboardVO);

        // this should detach the existing dashboard config and replace it with a new one, updating
        // its version. The original one should remain in the database history.
        DashboardConfig oldDashboardConfig = dashboard.getDashboardConfig();
        DashboardConfig newDashboardConfig = fromVO.getDashboardConfig();

        newDashboardConfig.setVersion(oldDashboardConfig.getVersion() + 1L);
        dashboard.setDashboardConfig(newDashboardConfig);
        dashboard.setName(fromVO.getName());
        dashboard.setIconName(fromVO.getIconName());
        dashboard.getDashboardConfig()
            .setHideNonRequiredConfigParameters(dashboardVO.isHideNonRequiredConfigParameters());
        dashboard.setProcessKey(fromVO.getProcessKey());
        if (dashboard.getDashboardConfigHistory() == null) {
            dashboard.setDashboardConfigHistory(new ArrayList<DashboardConfig>(1));
        }
        dashboard.getDashboardConfigHistory().add(oldDashboardConfig);
        return updateDashboard(dashboard);
    }

    @Override
    public DashboardVO convertDashboardToVO(Dashboard dashboard) throws DashboardException {
        DashboardVO dashboardVO = null;
        synchronized (mapper) {
            dashboardVO = mapper.map(dashboard, DashboardVO.class);
        }

        /*
         * If the process is running, set the dashboard as active, and the start time. If it is not
         * running, set it as inactive and set both the start and end time.
         */
        dashboardVO.setActive(false);
        dashboardVO.setSuspended(false); // if the process is not running, is not suspended
        Boolean hideNonRequiredConfigFields = dashboard.getDashboardConfig() != null
            ? dashboard.getDashboardConfig().hideNonRequiredConfigParameters() : null;
        dashboardVO.setHideNonRequiredConfigParameters(hideNonRequiredConfigFields != null
            ? hideNonRequiredConfigFields
            : false);
        dashboardVO.setProcessStarted(dashboard.getLastProcessStartTime());
        dashboardVO.setProcessEnded(dashboard.getLastProcessEndTime());
        String runningProcessId = dashboard.getProcessInstanceId();
        if (runningProcessId != null) {
            ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
            ProcessInstance process = query.processInstanceId(runningProcessId).singleResult();
            if (process != null) {
                dashboardVO.setSuspended(process.isSuspended());
                boolean active = false;
                if (dashboard.getLastProcessStartTime() != null
                    && (dashboard.getLastProcessEndTime() == null
                        || dashboard.getLastProcessEndTime().before(
                            dashboard.getLastProcessStartTime()))) {
                    active = true;
                    dashboardVO.setProcessEnded(null);
                } else {
                    dashboardVO.setProcessInstanceId(null);
                }
                dashboardVO.setActive(active);
            }
        }

        dashboardVO.setExecutions(getExecutions(dashboard.getId()));
        return dashboardVO;
    }

    @Override
    public DashboardVO convertDashboardByIdToVO(long dashboardId) throws DashboardException {
        Dashboard dashboard = getDashboard(dashboardId);
        return convertDashboardToVO(dashboard);
    }

    @Override
    public Dashboard convertVOToDashboard(DashboardVO dashboardVO) throws DashboardException {
        Dashboard dashboard = null;
        try {
            synchronized (mapper) {
                dashboard = mapper.map(dashboardVO, Dashboard.class);
            }
        } catch (MappingException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Exception occurred while converting dashboard VO to entity: ", e);
            }
            throw new DashboardException(ErrorCode.InvalidParameter, e.getMessage());
        }

        if (dashboard.getDashboardConfig() != null) {
            // Initialize required parameter 'hideNonRequiredConfigParameters'
            dashboard.getDashboardConfig().setHideNonRequiredConfigParameters(
                dashboardVO.isHideNonRequiredConfigParameters());
        }

        return dashboard;
    }

    @Override
    public MonitoredValue convertMonitorFromVO(
        MonitoredValueVO monitoredValueVO) throws DashboardException {
        MonitoredValue monitoredValue = null;
        try {
            synchronized (mapper) {
                monitoredValue = mapper.map(monitoredValueVO, MonitoredValue.class);
            }
        } catch (MappingException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER
                    .error("Exception occurred while converting monitored value VO to entity: ", e);
            }
            throw new DashboardException(ErrorCode.InvalidParameter, e.getMessage());
        }
        return monitoredValue;
    }

    @Override
    public List<ExecutionVO> getExecutions(final Long dashboardId) throws DashboardException {
        ExecutionQuery query = runtimeService.createExecutionQuery();
        if (dashboardId != null) {
            query.processVariableValueEquals(DashboardIdStore.DASHBOARD_VARIABLE,
                Long.toString(dashboardId));
        }
        List<Execution> executions = query.list();
        List<ExecutionVO> vos = new ArrayList<ExecutionVO>();
        if (executions == null || executions.size() == 0) {
            return vos;
        }
        for (Execution e : executions) {
            ExecutionVO evo = new ExecutionVO();
            evo.setActivityId(e.getActivityId());
            evo.setId(e.getId());
            evo.setParentId(e.getParentId());
            evo.setProcessInstanceId(e.getProcessInstanceId());
            evo.setTenantId(e.getTenantId());
            vos.add(evo);
        }
        return vos;
    }

    @Override
    public List<Dashboard> getDashboardsByProcessKey(String processKey) throws DashboardException {
        return dashboardDao.findAllByProcessKey(processKey);
    }

    @Override
    public boolean isDashboardRunning(Long dashboardId) throws DashboardException {
        Dashboard dashboard = getDashboard(dashboardId);
        return isDashboardRunning(dashboard);
    }

    @Override
    public boolean isDashboardRunning(Dashboard dashboard) throws DashboardException {
        if (dashboard != null && dashboard.getProcessInstanceId() != null) {
            ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
            ProcessInstance process = query.processInstanceId(dashboard.getProcessInstanceId())
                .singleResult();
            if (process != null) {
                return dashboard.getLastProcessStartTime() != null
                    && (dashboard.getLastProcessEndTime() == null || dashboard
                    .getLastProcessEndTime().before(
                        dashboard.getLastProcessStartTime()));
            }
        }
        return false;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void setMonitoredValue(Execution execution, String monitoredValueKey, boolean value) {
        String businessKey = null;
        String processId = execution.getProcessInstanceId();
        ProcessInstance process = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        if (process != null) {
            businessKey = process.getBusinessKey();
            if (businessKey == null) {
                Object key = runtimeService.getVariable(execution.getId(), EventListener.VAR_BUSINESS_KEY);
                if (key != null) {
                    businessKey = key.toString();
                }
            }
        }
        
        if (businessKey != null) {
            process = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
            if (process != null) {
                runtimeService.setVariable(process.getProcessInstanceId(), monitoredValueKey, value);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.server.manager.DashboardManager#configureMonitoredValue(org.activiti.engine.runtime.Execution, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void configureMonitoredValue(Execution execution, String monitorKey, String monitorName) {
        String businessKey = null;
        ProcessInstance process = null;
        
        if (execution instanceof ProcessInstance && execution.getId().equals(execution.getProcessInstanceId())) {
            // if this is a process instance AND the same as the configured processInstanceId, just use it
            process = (ProcessInstance) execution;
        } else {
            // otherwise look it up from the runtime service to find the parent process
            String processId = execution.getProcessInstanceId();
            process = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        }
        boolean mainProcessFound = false;
        if (process != null) {
            businessKey = process.getBusinessKey();
            if (businessKey == null) {
                Object key = runtimeService.getVariable(execution.getId(), EventListener.VAR_BUSINESS_KEY);
                if (key != null) {
                    businessKey = key.toString();
                }
            } else {
                mainProcessFound = true;
            }
        }
        
        if (businessKey != null) {
            if (!mainProcessFound) {
                process = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
            }
            
            if (process != null) {
                Dashboard d = dashboardDao.findByProcessInstanceid(process.getProcessInstanceId());
                if (d != null) {
                    DashboardConfig config = d.getDashboardConfig();
                    boolean contains = false;
                    for (MonitoredValue mv: config.getMonitors()) {
                        if (mv.getKey().equals(monitorKey)) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        MonitoredValue mv = new MonitoredValue(monitorKey, monitorName);
                        config.getMonitors().add(mv);
                    }
                }
            }
        }
    }
    
    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    /**
     * @param mapper the mapper to set
     */
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param runtimeService the runtimeService to set
     */
    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    /**
     * @param repositoryService the repositoryService to set
     */
    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    /**
     * @param formService the formService to set
     */
    public void setFormService(FormService formService) {
        this.formService = formService;
    }

    private DashboardException logError(ErrorCode errCode, String pattern, Object...arguments) {
        return logError(null, errCode, pattern, arguments);
    }
    
    private DashboardException logError(Throwable ex, ErrorCode errCode, String pattern, Object...arguments) {
        String msg = null;
        if (LOGGER.isErrorEnabled()) {
            msg = arguments != null && arguments.length > 0 ? MessageFormat.format(pattern, arguments) : pattern;
            LOGGER.error(msg, ex);
        }
        if (errCode == null) {
            errCode = ErrorCode.UnknownError;
        }
        if (msg == null) {
            msg = arguments != null && arguments.length > 0 ? MessageFormat.format(pattern, arguments) : pattern;
        }
        return new DashboardException(errCode, msg, ex);
    }

    private void logWarn(String pattern, Object...arguments) {
        if (LOGGER.isWarnEnabled()) {
            String msg = arguments != null && arguments.length > 0 ? MessageFormat.format(pattern, arguments) : pattern;
            LOGGER.warn(msg);
        }
    }

    private void logInfo(String pattern, Object...arguments) {
        if (LOGGER.isInfoEnabled()) {
            String msg = arguments != null && arguments.length > 0 ? MessageFormat.format(pattern, arguments) : pattern;
            LOGGER.info(msg);
        }
    }

    private void logDebug(String pattern, Object...arguments) {
        if (LOGGER.isDebugEnabled()) {
            String msg = arguments != null && arguments.length > 0 ? MessageFormat.format(pattern, arguments) : pattern;
            LOGGER.debug(msg);
        }
    }

}
