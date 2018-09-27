/**
 * 
 */
package com.ca.apm.systemtest.fld.server.rest;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.server.dao.LoggerMonitorDao;
import com.ca.apm.systemtest.fld.server.dao.PropertyValueDao;
import com.ca.apm.systemtest.fld.server.manager.DashboardManager;
import com.ca.apm.systemtest.fld.server.model.BooleanConfigItem;
import com.ca.apm.systemtest.fld.server.model.ConfigItem;
import com.ca.apm.systemtest.fld.server.model.Dashboard;
import com.ca.apm.systemtest.fld.server.model.DashboardConfig;
import com.ca.apm.systemtest.fld.server.model.LoggerMonitorValue;
import com.ca.apm.systemtest.fld.server.model.LongConfigItem;
import com.ca.apm.systemtest.fld.server.model.MonitoredValue;
import com.ca.apm.systemtest.fld.server.model.PropertyValue;
import com.ca.apm.systemtest.fld.server.model.StringConfigItem;
import com.ca.apm.systemtest.fld.server.rest.DashboardException.ErrorCode;
import com.ca.apm.systemtest.fld.shared.vo.ConfigItemVO;
import com.ca.apm.systemtest.fld.shared.vo.DashboardVO;
import com.ca.apm.systemtest.fld.shared.vo.ErrorMessage;
import com.ca.apm.systemtest.fld.shared.vo.JobVO;
import com.ca.apm.systemtest.fld.shared.vo.LogEntryVO;
import com.ca.apm.systemtest.fld.shared.vo.MonitoredValueStatus;
import com.ca.apm.systemtest.fld.shared.vo.MonitoredValueVO;
import com.ca.apm.systemtest.fld.shared.vo.ProcessInstanceVO;
import com.ca.apm.systemtest.fld.shared.vo.PropertyListVO;
import com.ca.apm.systemtest.fld.shared.vo.PropertyValueVO;
import com.ca.apm.systemtest.fld.shared.vo.Response;
import com.ca.apm.systemtest.fld.shared.vo.RetrieveLogsRequestVO;
import com.ca.apm.systemtest.fld.shared.vo.UserTaskFormPropertyVO;
import com.ca.apm.systemtest.fld.shared.vo.UserTaskVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * To use this REST API, explicitly set the Accept header to "Accept:
 * GET /api/dashboards - retrieve a list of available dashboards <br>
 * GET /api/dashboards/1 - retrieve a specific dashboards <br>
 * POST /api/dashboards - create a new dashboard <br>
 * PUT /api/dashboards/1 - update dashboard #1 (not yet implemented) <br>
 * DELETE /api/dashboards/1 - delete dashboard #1 (not yet supported) <br>
 * <br>
 * POST /api/dashboards/1/launch - launch a new workflow instance for the dashboard <br>
 * POST /api/dashboards/1/cancel - cancels the running workflow instance for the dashboard<br>
 * GET /api/dashboards/1/monitors - retrieve the current status of the associated workflow instance
 * (if any)<br>
 * GET /api/dashboards/1/statusImage - gets a status image for the current workflow instance<br>
 * GET /api/dashboards/1/userTasks - retrieve a list of waiting userTask of the associated workflow
 * instance (if any)<br>
 * POST /api/dashboards/1/completeTask - completes a waiting userTask, also submits associated form
 * data (if any) <br>
 * POST /api/dashboards/1/logs - retrieve log entries
 * 
 * @author keyja01
 *
 */
@RestController("dashboardRestController")
public class DashboardRestController {
    
    @Autowired
    private DashboardManager dashboardManager;

    @Autowired
    private LoggerMonitorDao loggerMonitorDao;

    @Autowired
    private Mapper mapper;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FormService formService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private PropertyValueDao propertyDao;

    @Autowired
    private HistoryService historyService;
    
    @Autowired
    private ManagementService managementService;

    private Logger logger = LoggerFactory.getLogger(DashboardRestController.class);

    /**
     * Returns a specific dashboard.
     * 
     * @return
     */
    @RequestMapping(value = "/dashboards/{dashboardId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> getDashboard(@PathVariable("dashboardId") Long dashboardId) {
        Dashboard dashboard = dashboardManager.getDashboard(dashboardId);
        DashboardVO vo = dashboardManager.convertDashboardToVO(dashboard);
        initDashboardVO(vo, dashboard);
        
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setDashboard(vo);

        ResponseEntity<Response> resp = new ResponseEntity<Response>(response, HttpStatus.OK);
        return resp;
    }

    @RequestMapping(value = "/dashboards/export/{dashboardId}", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRED)
    @ResponseBody
    public ResponseEntity<DashboardVO> exportDashboard(@PathVariable("dashboardId") Long dashboardId) {
        DashboardVO vo = dashboardManager.convertDashboardByIdToVO(dashboardId);
        vo.setId(null);
        vo.setExecutions(null);
        vo.setProcessEnded(null);
        vo.setProcessStarted(null);
        vo.setProcessInstanceId(null);
        vo.setSuspended(false);
        vo.setActive(false);
        String dashboardName =
            vo.getName() != null ? vo.getName().trim() : ("Dashboard" + dashboardId);
        if (isBlank(dashboardName)) {
            dashboardName = "Dashboard" + dashboardId;
        }
        dashboardName = dashboardName.replaceAll("\\s+", "_");
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Disposition", "attachment; filename=\"" + dashboardName + ".json\"");
        headers.add("Content-Type", "text/html");
        ResponseEntity<DashboardVO> resp =
            new ResponseEntity<DashboardVO>(vo, headers, HttpStatus.OK);
        return resp;
    }

    @ResponseBody
    @RequestMapping(value = "/dashboards/import", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> importDashboard(@RequestParam(required = false, value = "forceUpdateConfigParameters", 
        defaultValue = "false") Boolean forceUpdateConfigParams, 
        @RequestParam("dashboard") MultipartFile file) {

        if (forceUpdateConfigParams == null) {
            forceUpdateConfigParams = false;
        }
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        DashboardVO dashboardVO = null;
        try {
            dashboardVO = mapper.readValue(file.getInputStream(), DashboardVO.class);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to parse request body: ", e);    
            }
            throw new DashboardException(ErrorCode.InvalidParameter, e.getMessage());
        }

        //Dashboard to process match validation
        String processKey = dashboardVO.getProcessKey();
        if (processKey == null) {
            throw new DashboardException(ErrorCode.InvalidParameter, 
                "Process key can not be null!");
        }
        if (isBlank(processKey)) {
            throw new DashboardException(ErrorCode.InvalidParameter, 
                "Process key can not be empty!");
        }
        
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(processKey).latestVersion().singleResult();
        if (procDef == null) {
            throw new DashboardException(ErrorCode.ProcessNotFound, "No process definition with key '" + processKey + "' found!");
        }
        
        Dashboard dashboard = dashboardManager.convertImportVOToDashboard(dashboardVO, forceUpdateConfigParams);

        //This should be already set but just make sure
        dashboard.setProcessDefinitionVersion(procDef.getVersion());
        // Make sure dashboard.id is null
        dashboard.setId(null);
        
        DashboardVO dashboardResult = dashboardManager.convertDashboardToVO(dashboardManager.createDashboard(dashboard));
        dashboardResult.setActive(false);
        
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setDashboard(dashboardResult);

        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/dashboards/{dashboardId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> deleteDashboard(@PathVariable("dashboardId") Long dashboardId) {
        dashboardManager.deleteDashboardById(dashboardId);
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        ResponseEntity<Response> resp = new ResponseEntity<Response>(response, HttpStatus.OK);
        return resp;
    }

    @RequestMapping(value = "/deleteAllDashboards", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> deleteAllDashboards() {
        dashboardManager.deleteAllDashboards();
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        ResponseEntity<Response> resp = new ResponseEntity<Response>(response, HttpStatus.OK);
        return resp;
    }

    @RequestMapping(value = "/listProperties2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> listPropertyValues(
        @RequestParam(required = false, value = "propertyName") String propertyName,
        @RequestParam(required = false, value = "propertiesFiles") String propertiesFile,
        @RequestBody(required = false) PropertyListVO propListDefinition) {
        
        List<PropertyValue> properties = null;
        if (propertyName == null && propertiesFile == null && propListDefinition == null) {
            properties = propertyDao.findAll();
        } else if (propertyName != null && propertiesFile != null) {
            properties = new ArrayList<>(1);
            PropertyValue pv = propertyDao.findByNameAndFile(propertyName, propertiesFile);
            properties.add(pv);
        } else if (propertyName != null) {
            properties = propertyDao.findByName(propertyName);
        } else if (propListDefinition != null) {
            properties = propertyDao.findByNames(propListDefinition.getPropertyNames());
        }

        List<PropertyValueVO> propList =
            new ArrayList<>(properties != null ? properties.size() : 0);

        if (properties != null) {
            for (PropertyValue prop : properties) {
                propList.add(convertPropertyValue(prop));
            }
        }
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setPropertyList(propList);

        ResponseEntity<Response> resp = new ResponseEntity<Response>(response, HttpStatus.OK);
        return resp;
    }

    @RequestMapping(value = "/dashboardRunHistory/{dashboardId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> getDashboardHistory(@PathVariable("dashboardId") Long dashboardId) {
        Dashboard dashboard = dashboardManager.getDashboard(dashboardId);
        
        List<HistoricProcessInstance> processHistory = historyService.createHistoricProcessInstanceQuery().
            finished().
            processDefinitionKey(dashboard.getProcessKey()).
            variableValueEquals(DashboardIdStore.DASHBOARD_VARIABLE, Long.toString(dashboardId)).
            includeProcessVariables().
            list();
        List<ProcessInstanceVO> resultList = new ArrayList<ProcessInstanceVO>(processHistory.size()); 
        for (HistoricProcessInstance process : processHistory) {
            Map<String, Object> procVariables = process.getProcessVariables();
            if (procVariables != null) {
                logger.debug("Printing out original historical process variables ({} items): ", procVariables.size());
                for (Entry<String, Object> procVarEntry : procVariables.entrySet()) {
                    logger.debug("Key: {}  --->  Value: {} ; Value class: {}", 
                        procVarEntry.getKey(), 
                        procVarEntry.getValue(), 
                        procVarEntry.getValue() != null ? procVarEntry.getValue().getClass().getCanonicalName() : null);
                }
            }
            
            //Should not use dozer here as it fails to convert File fields
            ProcessInstanceVO historicProcInstanceVO = new ProcessInstanceVO();
            historicProcInstanceVO.setBusinessKey(process.getBusinessKey());
            historicProcInstanceVO.setDurationInMillis(process.getDurationInMillis());
            historicProcInstanceVO.setStartTime(process.getStartTime());
            historicProcInstanceVO.setEndTime(process.getEndTime());
            historicProcInstanceVO.setId(process.getId());
            historicProcInstanceVO.setName(process.getName());
            historicProcInstanceVO.setProcessDefinitionId(process.getProcessDefinitionId());
            historicProcInstanceVO.setProcessVariables(process.getProcessVariables());

            resultList.add(historicProcInstanceVO);
        }

        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setProcessInstances(resultList);

        ResponseEntity<Response> resp = new ResponseEntity<Response>(response, HttpStatus.OK);
        return resp;
    }
    
    /**
     * Returns a list of all dashboards available.
     * 
     * @return
     */
    @RequestMapping(value = "/dashboards", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> getDashboards() {
        List<Dashboard> list = dashboardManager.getAllDashboards();
        List<DashboardVO> dashboards = new ArrayList<>(list.size());
        for (Dashboard d : list) {
            DashboardVO vo = dashboardManager.convertDashboardToVO(d);
            initDashboardVO(vo, d);
            dashboards.add(vo);
        }

        Response apiResponse = new Response();
        apiResponse.setStatus(HttpStatus.OK);
        apiResponse.setDashboards(dashboards);
        ResponseEntity<Response> resp = new ResponseEntity<Response>(apiResponse, HttpStatus.OK);

        return resp;
    }

    @RequestMapping(value = "/dashboards/{dashboardId}/logsCount", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> getLogsCount(@PathVariable Long dashboardId,
        @RequestBody RetrieveLogsRequestVO request) {
        request.setDashboardId(dashboardId);
        long count = loggerMonitorDao.countFilterLogs(request);

        Response resp = new Response();
        resp.setStatus(HttpStatus.OK);
        resp.setLogsCount(count);
        return new ResponseEntity<Response>(resp, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/dashboards/{dashboardId}/logs", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> retrieveLogs(@PathVariable Long dashboardId,
        @RequestBody RetrieveLogsRequestVO request) {

        if (request.getLogsAfterId() != null && request.getLogsBeforeId() != null) {
            throw new DashboardException(ErrorCode.InvalidParameter,
                "Both getLogsAfterId and getLogsBeforeId cannot be non-null");
        }
        
        request.setDashboardId(dashboardId);
        long count = loggerMonitorDao.countFilterLogs(request);

        List<LoggerMonitorValue> logsDB = loggerMonitorDao.findFilterLogs(request);
        List<LogEntryVO> logEntries = new ArrayList<>(logsDB.size());

        for (LoggerMonitorValue logdb : logsDB) {
            LogEntryVO logRest = new LogEntryVO();

            logRest.setId(logdb.getId());
            logRest.setLevel(logdb.getLevel().name());
            logRest.setCategory(logdb.getCategory());
            logRest.setTag(logdb.getTag());
            logRest.setMessage(logdb.getMessage());
            logRest.setException(logdb.getException());
            logRest.setTimestamp(logdb.getTimestamp());
            logRest.setNodeName(logdb.getNode());
            logRest.setDashboardId(logdb.getDashboardId());
            logRest.setProcessInstanceId(logdb.getProcessInstanceId());
            logEntries.add(logRest);
        }

        Response resp = new Response();
        resp.setStatus(HttpStatus.OK);
        resp.setLogsCount(count);
        resp.setLogEntries(logEntries);

        return new ResponseEntity<Response>(resp, HttpStatus.OK);
    }


    @RequestMapping(value = "/dashboards", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> createDashboard(@RequestBody DashboardVO dashboardVO) {
        Dashboard dashboard = dashboardManager.createDashboardFromVO(dashboardVO);
        // map our newly created dashboard back to the value object
        dashboardVO = dashboardManager.convertDashboardToVO(dashboard);
        dashboardVO.setActive(false);
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setDashboard(dashboardVO);

        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    /**
     * Clones a specific dashboard. You can specify name of new dashboard in request body.
     * 
     * @return new object
     */
    @RequestMapping(value = "/dashboards/{dashboardId}/clone", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> cloneDashboard(@PathVariable Long dashboardId,
        @RequestParam(required = false, value = "name") String name) {
        if (dashboardId == null) {
            throw new DashboardException(ErrorCode.InvalidParameter, 
                "Dashboard id can not be null!");
        }
        
        Dashboard dashboardClone = dashboardManager.cloneDashboardById(dashboardId, false);
        //Just make sure we have a clean id
        dashboardClone.setId(null);
        if (name != null && !name.isEmpty()) {
            dashboardClone.setName(name);
        } else {
            dashboardClone.setName(dashboardClone.getName() + " (copy)");
        }

        // map our newly created dashboard back to the value object
        DashboardVO dashboardCloneVO = dashboardManager.convertDashboardToVO(
            dashboardManager.createDashboard(dashboardClone));
        dashboardCloneVO.setActive(false);
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setDashboard(dashboardCloneVO);

        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/dashboards/{dashboardId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> updateDashboard(@PathVariable Long dashboardId,
        @RequestBody DashboardVO dashboardVO) {
        if (dashboardVO == null) {
            throw new DashboardException(ErrorCode.InvalidParameter, "Dashboard is null!");
        }
        if (dashboardVO.getId() == null && dashboardId != null) {
            dashboardVO.setId(dashboardId);
        }
        
        DashboardVO vo = dashboardManager.convertDashboardToVO(dashboardManager.updateDashboardFromVO(dashboardVO));
        return new ResponseEntity<Response>(new Response(vo), HttpStatus.OK);
    }

    @RequestMapping(value = "/dashboards/{dashboardId}/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> cancelProcess(@PathVariable Long dashboardId) {
        Dashboard d = dashboardManager.getDashboard(dashboardId);
        ProcessInstance pi = null;
        String procInstId = d.getProcessInstanceId();
        
        try {
            ProcessInstanceQuery piQuery = runtimeService.createProcessInstanceQuery();
            pi = piQuery.processInstanceId(procInstId).singleResult();
        } catch (ActivitiException e) {
            throw new DashboardException(ErrorCode.UnknownError, 
                "Could not get process instance for process instance id = " + procInstId);
        }
        
        if (pi != null && !pi.isSuspended()) {
            try {
                runtimeService.suspendProcessInstanceById(procInstId);
            } catch (ActivitiException ex) {
                throw new DashboardException(ErrorCode.InvalidProcessState,
                    "Could not suspend the process: " + ex.getMessage());
            }
        }

        runtimeService.deleteProcessInstance(d.getProcessInstanceId(),
            "Deleted in Load Orchestrator REST API");

        Response resp = new Response();
        resp.setStatus(HttpStatus.OK);
        return new ResponseEntity<Response>(resp, HttpStatus.OK);
    }


    @RequestMapping(value = "/dashboards/{dashboardId}/launch", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> launchProcess(@PathVariable Long dashboardId, 
        @RequestBody(required = false) DashboardVO dashboardVO) {
        
        Dashboard d = dashboardManager.getDashboard(dashboardId);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        String runningProcessId = d.getProcessInstanceId();
        if (runningProcessId != null) {
            ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
            ProcessInstance process = query.processInstanceId(runningProcessId).singleResult();
            if (process != null) {
                if (!process.isEnded() && !process.isSuspended()) {
                    throw new DashboardException(ErrorCode.InvalidProcessState,
                        "The dashboard's process is currently in state: ended:" + process.isEnded()
                            + ", suspended:" + process.isSuspended());
                }
            }
        }

        Map<String, String> variables = null;
        if (dashboardVO != null && dashboardVO.getConfig() != null && !dashboardVO.getConfig().isEmpty()) {
            List<ConfigItemVO> configItems = dashboardVO.getConfig();
            variables = new HashMap<>(configItems.size() + 1);
            for (ConfigItemVO configItemVO : configItems) {
                variables.put(configItemVO.getFormId(), configItemVO.getValue() == null ? null : configItemVO.getValue().toString());
            }
        } else {
            variables = new HashMap<>(d.getDashboardConfig().getConfigItems().size() + 1);
            for (ConfigItem ci : d.getDashboardConfig().getConfigItems()) {
                variables.put(ci.getFormId(), ci.getValue() == null ? null : ci.getValue().toString());
            }
        }
        
        variables.put(DashboardIdStore.DASHBOARD_VARIABLE, Long.toString(dashboardId));

        ProcessDefinition processDefinition =
            repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(d.getProcessKey()).latestVersion().singleResult();
        if (processDefinition == null) {
            final String msg = MessageFormat.format(
                "The process with key {0} for dashboard \"{1}\" (ID {2}) could not be found",
                d.getProcessKey(), d.getName(), dashboardId);
            throw new DashboardException(ErrorCode.ProcessNotFound, msg);
        }
        String pid = processDefinition.getId();
        String businessKey = dashboardId + "-" + System.currentTimeMillis();
        ProcessInstance processInstance = formService.submitStartFormData(pid, businessKey, variables);
        
        logger.debug("Assigning business key " + businessKey + " to new process " + processInstance.getId());

        d.setProcessInstanceId(processInstance.getId());
        d.setProcessBusinessKey(businessKey);
        d.setLastProcessStartTime(new Date());
        
        DashboardVO dvo = dashboardManager.convertDashboardToVO(dashboardManager.updateDashboard(d));

        Response resp = new Response();
        resp.setDashboard(dvo);
        resp.setStatus(HttpStatus.OK);

        return new ResponseEntity<>(resp, headers, HttpStatus.OK);
    }


    @RequestMapping(value = "/dashboards/{dashboardId}/pause", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> pauseProcess(@PathVariable Long dashboardId) {
        Dashboard d = dashboardManager.getDashboard(dashboardId);

        try {
            runtimeService.suspendProcessInstanceById(d.getProcessInstanceId());
        } catch (ActivitiObjectNotFoundException ex) {
            throw new DashboardException(ErrorCode.InvalidProcessState,
                "The process could not be found (either not yet started, cancelled or completed): "
                    + ex.getMessage());
        } catch (ActivitiException ex) {
            logger.info("Cannot suspend process for dashboard " + dashboardId + ": is it already suspended?");
        }

        Response resp = new Response();
        resp.setDashboard(dashboardManager.convertDashboardToVO(d));
        resp.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }


    @RequestMapping(value = "/dashboards/{dashboardId}/resume", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> resumeProcess(@PathVariable Long dashboardId) {
        Dashboard d = dashboardManager.getDashboard(dashboardId);

        try {
            runtimeService.activateProcessInstanceById(d.getProcessInstanceId());
        } catch (ActivitiObjectNotFoundException ex) {
            throw new DashboardException(ErrorCode.InvalidProcessState,
                "The process could not be found (either not yet started, cancelled or completed): "
                    + ex.getMessage());
        } catch (ActivitiException ex) {
            logger.info("Cannot resume process for dashboard " + dashboardId + ": is it already running?");
        }

        Response resp = new Response();
        resp.setDashboard(dashboardManager.convertDashboardToVO(d));
        resp.setStatus(HttpStatus.OK);
        return new ResponseEntity<Response>(resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/diagramImage", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public @ResponseBody byte[] getDiagramImage(@RequestParam(required = true) String deploymentId,
                                                @RequestParam(required = true) String diagramResourceName,
                                                @RequestParam(required = false) Double scale) {
        if (scale == null || scale <= 0.0) {
            scale = 1.0;
        }

        byte[] imageData = new byte[0];

        if (!isBlank(deploymentId) && !isBlank(diagramResourceName)) {
            try (InputStream imageStream = repositoryService.getResourceAsStream(deploymentId, diagramResourceName)) {
                imageData = FileCopyUtils.copyToByteArray(imageStream);
            } catch (IOException e) {
                logger.error("An exception occurred copying the image data", e);
                throw new DashboardException(ErrorCode.UnknownError,
                    "An exception occurred copying the image data");
            }
        }

        return imageData;
    }

    @RequestMapping(value = "/processImage/{processId}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public @ResponseBody byte[] generateProcessDiagramImage(@PathVariable String processId,
        @RequestParam(required = false) Double scale) {

        if (scale == null || scale <= 0.0) {
            scale = 1.0;
        }

        
        byte[] imageData = new byte[0];

        ProcessInstance processInstance =
            runtimeService.createProcessInstanceQuery().processInstanceId(processId)
                .singleResult();

        // if the process is running, try to generate an image with the current status
        if (processInstance != null) {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
            List<String> activityIds = runtimeService.getActiveActivityIds(processInstance.getId());
            ProcessDiagramGenerator processDiagramGenerator =
                processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();

            try (InputStream generateDiagram =
                processDiagramGenerator.generateDiagram(bpmnModel, "png", activityIds, scale)) {
                imageData = FileCopyUtils.copyToByteArray(generateDiagram);
            } catch (IOException e) {
                logger.error("An exception occurred copying the image data", e);
                throw new DashboardException(ErrorCode.UnknownError,
                    "An exception occurred copying the image data");
            }
        }

        // if the process is ended, generate a static image
        return imageData;
    }

    @RequestMapping(value = "/dashboards/subprocesses/{dashboardId}", method = RequestMethod.GET, 
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> getSubprocesses(@PathVariable Long dashboardId) {
        Dashboard d = dashboardManager.getDashboard(dashboardId);
        List<ProcessInstanceVO> resultList = Collections.<ProcessInstanceVO>emptyList();
        if (d != null && d.getProcessInstanceId() != null) {
            Map<String, ProcessInstance> subProcesses = collectAllSubprocess(d.getProcessInstanceId(), null);
            if (subProcesses != null && !subProcesses.isEmpty()) {
                Collection<ProcessInstance> subProcessInstances = subProcesses.values();
                resultList = new ArrayList<ProcessInstanceVO>(subProcessInstances.size()); 
                for (ProcessInstance subProcInst : subProcessInstances) {
                    ProcessInstanceVO procInstanceVO = null;
                    synchronized (mapper) {
                         procInstanceVO = mapper.map(subProcInst, ProcessInstanceVO.class);
                    }
                    resultList.add(procInstanceVO);
                }
            }
        }
        
        Response resp = new Response();
        resp.setProcessInstances(resultList);
        resp.setStatus(HttpStatus.OK);
        return new ResponseEntity<Response>(resp, HttpStatus.OK);

    }
    
    /**
     * Re-launches whether all of the provided jobs or none if there are validation errors.
     * Re-launching is made by resetting retries counter of each job to some value > 0.
     * 
     * @param   jobs    jobs to re-launch
     * @return          HTTP status
     */
    @RequestMapping(value = "/dashboards/reLaunchJobs", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> reLaunchJobs(@RequestBody JobVO[] jobs) {

        if (jobs != null) {
            //First, validate all
            for (JobVO job : jobs) {
                if (isBlank(job.getId())) {
                    throw new DashboardException(ErrorCode.InvalidParameter, "Invalid job id: " + job.getId());
                }
                if (job.getRetries() == null || job.getRetries() < 0) {
                    throw new DashboardException(ErrorCode.InvalidParameter, "Invalid retries count for job(id=" + job.getId() + "): " + job.getRetries());
                }
            }
            //If we got here, we had passed validation for all job retry counts
            for (JobVO job : jobs) {
                managementService.setJobRetries(job.getId(), job.getRetries());
            }
        }

        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }
    
    
    @RequestMapping(value = "/dashboards/{dashboardId}/stuckJobs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> getStuckJobs(@PathVariable Long dashboardId) {
        Dashboard d = dashboardManager.getDashboard(dashboardId);
        
        Set<JobVO> result = getStuckJobs(dashboardId.toString(), 
            d.getProcessInstanceId());
        
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setJobs(new ArrayList<JobVO>(result));
        ResponseEntity<Response> resp = new ResponseEntity<Response>(response, HttpStatus.OK);
        return resp;
    }
    
    @RequestMapping(value = "/dashboards/{dashboardId}/completeTask", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> completeWaitingUserTask(@PathVariable Long dashboardId,
        @RequestBody UserTaskVO userTask) {

        Task t = taskService.createTaskQuery().taskId(userTask.getId()).singleResult();
        if (t == null) {
            throw new DashboardException(ErrorCode.InvalidParameter, "The user task for id "
                + userTask.getId() + " could not be found");
        }

        Map<String, String> properties = new HashMap<String, String>();
        for (UserTaskFormPropertyVO fp : userTask.getFormData()) {
            if (fp.getWritable() && fp.getValue() != null && !fp.getValue().isEmpty()) {
                properties.put(fp.getId(), fp.getValue());
            }
        }
        formService.submitTaskFormData(userTask.getId(), properties);

        Response response = new Response();
        response.setStatus(HttpStatus.OK);

        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/dashboards/{dashboardId}/userTasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> retrieveWaitingUserTask(@PathVariable Long dashboardId) {
        Dashboard d = dashboardManager.getDashboard(dashboardId);

        DashboardVO dvo = dashboardManager.convertDashboardToVO(d);

        List<UserTaskVO> list = new ArrayList<UserTaskVO>();
        // check task list only if not suspended
        if (!dvo.isSuspended()) {
            String processInstanceId = d.getProcessInstanceId();
            //Result map to accumulate all user tasks including those of subprocesses. Why map? 
            //It's faster to search for duplicates.
            Map<String, Task> tasksMap = new HashMap<String, Task>();
            collectProcessUserTasks(processInstanceId, tasksMap);
            
            for (Task t : tasksMap.values()) {
                UserTaskVO userTask = new UserTaskVO();
                userTask.setId(t.getId());
                userTask.setName(t.getName());
                userTask.setDescription(t.getDescription());
                userTask.setProcessInstanceId(t.getProcessInstanceId());
                userTask.setProcessDefinitionId(t.getProcessDefinitionId());
                List<UserTaskFormPropertyVO> properties = new ArrayList<UserTaskFormPropertyVO>();
                TaskFormData formData = formService.getTaskFormData(t.getId());
                for (FormProperty fp : formData.getFormProperties()) {
                    UserTaskFormPropertyVO property = new UserTaskFormPropertyVO();
                    property.setId(fp.getId());
                    property.setName(fp.getName());
                    property.setType(fp.getType().getName());
                    if (fp.getType().getInformation("values") != null) {
                        property.setTypeInformation(fp.getType().getInformation("values")
                            .toString());
                    }
                    property.setValue(fp.getValue());
                    property.setReadable(fp.isReadable());
                    property.setRequired(fp.isRequired());
                    property.setWritable(fp.isWritable());
                    properties.add(property);
                }
                userTask.setFormData(properties);
                list.add(userTask);
            }
        }

        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setWaitingUserTasks(list);

        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/dashboards/{dashboardId}/monitorGroups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<List<String>> getMonitorGroupsList(@PathVariable Long dashboardId) {
        Dashboard d = dashboardManager.getDashboard(dashboardId);

        Set<String> set = new HashSet<>();
        for (MonitoredValue monitor : d.getDashboardConfig().getMonitors()) {
            set.add(monitor.getGroup());
        }
        List<String> list = new ArrayList<String>(set);
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);

        return (new ResponseEntity<List<String>>(list, HttpStatus.OK));
    }


    @RequestMapping(value = "/dashboards/{dashboardId}/monitors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> readProcessMonitors(@PathVariable Long dashboardId,
        @RequestParam(required = false) String group) {
        Dashboard d = dashboardManager.getDashboard(dashboardId);

        List<MonitoredValueVO> list = getMonitorValuesForDashboard(d, group);

        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setMonitors(list);

        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorMessage> handleException(Throwable ex) {
        logger.warn("DashboardRestController: unexpected exception", ex);

        ErrorMessage em = new ErrorMessage();

        em.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        List<String> errors = new ArrayList<>(2);
        errors.add("A completely and totally unexpected exception has occurred.");
        errors.add(ex.getMessage());
        em.setErrors(errors);

        ResponseEntity<ErrorMessage> retval =
            new ResponseEntity<ErrorMessage>(em, HttpStatus.INTERNAL_SERVER_ERROR);

        return retval;
    }


    @ExceptionHandler(DashboardException.class)
    public ResponseEntity<ErrorMessage> handleApplicationException(DashboardException ex) {
        logger.warn("An application exception has occurred", ex);

        ErrorMessage em = new ErrorMessage();

        em.setStatus(HttpStatus.BAD_REQUEST);
        List<String> errors = new ArrayList<>(2);
        errors.add(ex.getMessage());
        if (ex.getCause() != null) {
            errors.add(ex.getCause().getMessage());
        }
        em.setErrors(errors);

        ResponseEntity<ErrorMessage> retval =
            new ResponseEntity<ErrorMessage>(em, HttpStatus.BAD_REQUEST);

        return retval;
    }

    public void setDashboardManager(DashboardManager dashboardManager) {
        this.dashboardManager = dashboardManager;
    }
    
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void setFormService(FormService formService) {
        this.formService = formService;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    private List<MonitoredValueVO> getMonitorValuesForDashboard(Dashboard d, String group) {
        if (d == null || d.getDashboardConfig() == null || 
            d.getDashboardConfig().getMonitors() == null ||
            d.getDashboardConfig().getMonitors().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<MonitoredValue> monitors = d.getDashboardConfig().getMonitors();
        List<MonitoredValueVO> list = new ArrayList<>();

        List<String> variableKeys = new ArrayList<>();
        
        for (Iterator<MonitoredValue> it = monitors.iterator(); it.hasNext();) {
            MonitoredValue mv = it.next();
            if (group != null && !group.equals(mv.getGroup())) {
                it.remove();
                continue;
            }
            variableKeys.add(mv.getKey());
        }
        Map<String, Object> variableMap = null;

        if (d != null) {
            variableMap = collectProcessVariables(d.getProcessInstanceId(), variableKeys, null);
            
            List<Execution> executions = runtimeService.createExecutionQuery()
                .variableValueEquals(DashboardIdStore.DASHBOARD_VARIABLE, d.getId().toString())
                .list();
            for (Execution execution: executions) {
                String procInstId = execution.getProcessInstanceId();
                if (!procInstId.equals(d.getProcessInstanceId())) {
                    variableMap = collectProcessVariables(procInstId, variableKeys, variableMap);
                }
            }
        }

        if (variableMap == null) {
            variableMap = Collections.emptyMap();
        }
        
        synchronized (mapper) {
            for (MonitoredValue mv : monitors) {
                MonitoredValueVO vo = mapper.map(mv, MonitoredValueVO.class);
                Object obj = variableMap.get(mv.getKey());
                if (obj == null) {
                    vo.setValue(MonitoredValueStatus.Unknown);
                } else {
                    Boolean b = null;
                    if (obj instanceof Boolean) {
                        b = (Boolean) obj;
                    } else {
                        if (obj instanceof Number) {
                            Number n = (Number) obj;
                            b = n.intValue() != 0;
                        } else {
                            b = Boolean.valueOf(obj.toString());
                        }
                    }
                    if (b) {
                        vo.setValue(MonitoredValueStatus.OK);
                    } else {
                        vo.setValue(MonitoredValueStatus.Error);
                    }
                }
                list.add(vo);
            }
        }

        Collections.sort(list, new Comparator<MonitoredValueVO>() {
            public int compare(MonitoredValueVO monitorVO1, MonitoredValueVO monitorVO2) {
                return monitorVO1.getKey().compareTo(monitorVO2.getKey());
            }
        });

        return list; 
    }
    
    private Set<JobVO> getStuckJobs(String dashboardId, String processInstanceId) {
        Set<JobVO> result = new HashSet<>();
        if (processInstanceId != null) {
            List<Job> stuckJobs = managementService.createJobQuery()
                .processInstanceId(processInstanceId)
                .noRetriesLeft()
                .list();
            if (stuckJobs != null && !stuckJobs.isEmpty()) {
                for (Job job : stuckJobs) {
                    JobVO jobVO = null;
                    synchronized (mapper) {
                         jobVO = mapper.map(job, JobVO.class);
                    }
                    result.add(jobVO);
                }
            }
        }
        
        // also check for sub-process executions that may be stuck
        List<Execution> executions = runtimeService.createExecutionQuery()
            .variableValueEquals(DashboardIdStore.DASHBOARD_VARIABLE, dashboardId)
            .list();
        for (Execution e: executions) {
            List<Job> stuckJobs = managementService.createJobQuery()
                    .processInstanceId(e.getProcessInstanceId())
                    .noRetriesLeft()
                    .list();
            if (stuckJobs != null && stuckJobs.size() > 0) {
                for (Job job : stuckJobs) {
                    JobVO jobVO = null;
                    synchronized (mapper) {
                         jobVO = mapper.map(job, JobVO.class);
                    }
                    result.add(jobVO);
                }
            }
        }
        return result;
    }
    
    private void initDashboardVO(DashboardVO dashboardVO, Dashboard dashboard) {
        if (dashboardVO.isActive() && !dashboardVO.isSuspended()) {
            String processInstanceId = dashboardVO.getProcessInstanceId();
            //Result map to accumulate all user tasks including those of subprocesses. Why map? 
            //It's faster to search for duplicates.
            Map<String, Task> tasksMap = new HashMap<String, Task>();
            collectProcessUserTasks(processInstanceId, tasksMap);
            dashboardVO.setHasWaitingUserTasks(!tasksMap.isEmpty());
            Set<JobVO> stuckJobs = getStuckJobs(dashboardVO.getId().toString(), 
                dashboardVO.getProcessInstanceId());
            dashboardVO.setHasStuckJobs(!stuckJobs.isEmpty());
            dashboardVO.setMonitors(getMonitorValuesForDashboard(dashboard, null));
        }
    }
    
    /**
     * Collects variables with the specified <code>variableKeys</code> for the process with the specified <code>processInstanceId</code>.
     * 
     * @param  processInstanceId  process instance id
     * @param  variableKeys       variable keys
     * @param  variableMap        result container; maybe <code>null</code>
     * @return                    variables map
     */
    private Map<String, Object> collectProcessVariables(String processInstanceId, List<String> variableKeys, Map<String, Object> variableMap) {
        ProcessInstance process =
            runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        if (process != null && !process.isEnded() && !process.isSuspended()) {
            variableMap = variableMap == null ? new HashMap<String, Object>() : variableMap;
            Map<String, Object> nextVariableMap = runtimeService.getVariables(processInstanceId, variableKeys);
            if (nextVariableMap != null) {
                variableMap.putAll(nextVariableMap);    
            }
            
        }
        return variableMap;
    }

    /**
     * Recursively gets user tasks for the process and all its possible child subprocesses.
     * 
     * @param processInstanceId  process instance id
     * @param tasksMap           result map where user tasks are collected (procInstanceId -> Task); may be <code>null</code>
     * @return                   <code>tasksMap</code> itself if it wasn't <code>null</code>, otherwise a newly created map
     */
    private Map<String, Task> collectProcessUserTasks(String processInstanceId, Map<String, Task> tasksMap) {
        //Make sure we don't get an NPE.
        tasksMap = tasksMap == null ? new HashMap<String, Task>() : tasksMap;
        
        List<Task> userTasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        
        for (Task userTask : userTasks) {
            if (!tasksMap.containsKey(userTask.getId())) {
                tasksMap.put(userTask.getId(), userTask);    
            }
        }
        
        //Go see if we don't have subprocesses which may have their own user tasks.
        List<ProcessInstance> subProcessInstances = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstanceId).list();
        if (subProcessInstances != null) {
            for (ProcessInstance subProcess : subProcessInstances) {
                collectProcessUserTasks(subProcess.getId(), tasksMap);
            }
        }
        return tasksMap;
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

    private PropertyValueVO convertPropertyValue(PropertyValue propertyValue) {
        PropertyValueVO propertyValueVO = null;
        synchronized (mapper) {
            propertyValueVO = mapper.map(propertyValue, PropertyValueVO.class);
        }
        return propertyValueVO;
    }

    public static void main(String[] args) throws Exception {
        DozerBeanMapper mapper = new DozerBeanMapper(Collections.singletonList("dozer-config.xml"));

        Dashboard d = new Dashboard();
        d.setId(99999L);
        d.setName("dashboard name");
        d.setProcessKey("process key");
        DashboardConfig dc = new DashboardConfig();
        dc.setId(1001L);
        dc.setVersion(100);
        dc.setConfigItems(new ArrayList<ConfigItem>());
        dc.getConfigItems().add(
            new StringConfigItem("string item #1", "form id 1", "string value #1"));
        dc.getConfigItems().add(new LongConfigItem("long item #1", "form id 2", 100L));
        dc.getConfigItems().add(new BooleanConfigItem("boolean #1", "form id 3", true));
        dc.setMonitors(new ArrayList<MonitoredValue>());
        dc.getMonitors().add(new MonitoredValue("mv #1", "mv #1 name"));
        d.setDashboardConfig(dc);

        DashboardVO vo = mapper.map(d, DashboardVO.class);
        // mapper.map(d.getDashboardConfig(), vo);
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(jsonMapper.writeValueAsString(vo));

        ConfigItemVO civo = new ConfigItemVO();
        civo.setName("extra #1");
        civo.setType("string");
        civo.setValue("extra value #1");
        vo.getConfig().add(civo);

        d = mapper.map(vo, Dashboard.class);
        System.out.println(d);
    }
}
