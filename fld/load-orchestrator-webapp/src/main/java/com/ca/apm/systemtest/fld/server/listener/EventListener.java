/**
 *
 */
package com.ca.apm.systemtest.fld.server.listener;

import java.util.Date;
import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiProcessStartedEvent;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.plugin.vo.ProcessInstanceIdStore;
import com.ca.apm.systemtest.fld.server.dao.DashboardDao;
import com.ca.apm.systemtest.fld.server.model.Dashboard;

/**
 * <pre>
 * 	Patterns are evaluated from first to last, first which match is used. You can
 * 	use \ as escape character when you need : as value for pattern.
 *
 * VALUE
 * 	ListOfEventTypes:ProcDefIdPattern:ProcInstIdPattern:ExecIdPattern:Level:Tag:Message
 *
 * MATCHING PARAMETERS
 * 	ListOfEventTypes - is comma separated list of event types (look to ActivitiEventType enum)
 * 	ProcessDefIdPattern - Standard Java Pattern to match against event.ProcessDefinitionId
 * 	ProcessInstIdPattern - Standard Java Pattern to match against event.ProcessInstanceId
 * 	ExecIdPattern - Standard Java Pattern to match against event.ExecIdPattern
 *
 * OUTPUT PARAMETERS
 * 	Level - Should be one of TRACE, DEBUG, INFO, WARN, ERROR (default to DEBUG)
 * 	Tag - Tag used for logging into FldLogger
 * 	Message - Format for output message in java 'Format', parameters are EventType, ProcessDefId,
 * 		ProcessInstId, ExecId (default is "Event %s executed in %s (inst: %s, exec: %s)")
 *
 * SPRING CONFIGURATION EXAMPLE
 * 	&lt;bean id="eventListener" class="com.ca.apm.systemtest.fld.server.listener.EventListener">
 * 		&lt;property name="eventMatches">
 * 			&lt;list>
 * 				&lt;value>ACTIVITY_COMPLETED,PROCESS_COMPLETED:web.*\:4:::WARN:COMPL_TAG:%2$s
 * 				completed with event %1$s&lt;/value>
 * 				&lt;value>:::::CATCH_ALL:| %-20s| %-20s| %-5s| %-5s|&lt;/value>
 * 			&lt;/list>
 * 		&lt;/property>
 * 	&lt;/bean>
 * </pre>
 *
 * @author keyja01, tavpa01
 */
public class EventListener implements ActivitiEventListener {

    public static final String VAR_BUSINESS_KEY = "VAR_BUSINESS_KEY";
    private static final String LOG_CATEGORY = "ACTIVITI_ENGINE";
    private static final Logger log = LoggerFactory.getLogger(EventListener.class);
    private static final String MSG_FORMAT = "Event %s executed in %s (inst: %s, exec: %s)";


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private FldLogger fldLogger;
    
    private List<EventMatcher> eventMatches;

    /**
     *
     */
    public EventListener() {
    }

    /* (non-Javadoc)
     * @see org.activiti.engine.delegate.event.ActivitiEventListener#onEvent(org.activiti.engine
     * .delegate.event.ActivitiEvent)
     */
    @Override
    public void onEvent(ActivitiEvent event) {
        logEventIntoLocalLogs(event);
        updateDashboard(event);
        updateBusinessKey(event);
        logEvent(event);

    }

    private void logEventIntoLocalLogs(ActivitiEvent event) {
        if (log.isDebugEnabled()) {
            Class<? extends ActivitiEvent> eventClass = event.getClass();
            if (TypeUtils.isAssignable(eventClass, ActivitiActivityEvent.class)) {
                ActivitiActivityEvent ev = (ActivitiActivityEvent) event;
                log.debug("Got event {} for activity {}/{}/{} in execution {} of process {}[{}]",
                    ev.getType(), ev.getActivityName(), ev.getActivityId(), ev.getActivityType(),
                    ev.getExecutionId(), ev.getProcessDefinitionId(), ev.getProcessInstanceId());
            }
        }
    }

    private void logEvent(ActivitiEvent event) {
        if (eventMatches != null) {
            for (EventMatcher match : eventMatches) {
                if (match.matches(event)) {
                    // Log event
                    String msgFormat = match.getMessage() != null ? match.getMessage() : MSG_FORMAT;
                    String msg = String
                        .format(msgFormat, event.getType(), event.getProcessDefinitionId(),
                            event.getProcessInstanceId(), event.getExecutionId());
                    FldLevel fldLevel = match.getLogLevel();
                    if (FldLevel.WARN.equals(match.getLogLevel())) {
                        log.warn(msg);
                    } else if (FldLevel.ERROR.equals(match.getLogLevel())) {
                        log.error(msg);
                    } else if (FldLevel.INFO.equals(match.getLogLevel())) {
                        log.info(msg);
                    } else if (FldLevel.DEBUG.equals(match.getLogLevel())) {
                        log.debug(msg);
                    } else if (FldLevel.TRACE.equals(match.getLogLevel())) {
                        log.trace(msg);
                    }
                    String tag = match.getTag() == null ? event.getType().name() : match.getTag();

                    ProcessInstanceIdStore.setProcessInstanceId(event.getProcessInstanceId());
                    fldLogger.log(fldLevel, LOG_CATEGORY, tag, msg, null);
                    break;
                }
            }
        }
    }

    
    /**
     * Used to automatically propagate the businessKey to sub processes (both CallActivity and embedded)
     * @param event
     */
    private void updateBusinessKey(ActivitiEvent event) {
        String key = null;
        
        
        boolean doit = ActivitiEventType.PROCESS_STARTED.equals(event.getType()) 
            && (event instanceof ActivitiProcessStartedEvent);
        
        if (doit) {
            ActivitiProcessStartedEvent apsei = (ActivitiProcessStartedEvent) event;
            String nestedID = apsei.getNestedProcessInstanceId();
            if (nestedID == null) {
                return;
            }
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(nestedID).singleResult();
            if (pi != null) {
                key = pi.getBusinessKey();
                if (key == null) {
                    Object obj = runtimeService.getVariable(pi.getProcessInstanceId(), VAR_BUSINESS_KEY);
                    if (obj != null) {
                        key = obj.toString();
                    }
                }
            }
        }
            
        if (key != null) {
            runtimeService.setVariable(event.getProcessInstanceId(), VAR_BUSINESS_KEY, key);
//            runtimeService.updateBusinessKey(event.getProcessInstanceId(), key);
        }
    }
    
    private void updateDashboard(ActivitiEvent event) {
        switch (event.getType()) {
            case PROCESS_STARTED: 
                log.info("ACTIVITI PROCESS_STARTED: threadId = {}, DashboardIdStore's dashboardId = {}", 
                    Thread.currentThread().getId(), DashboardIdStore.getDashboardId());

                ProcessInstanceIdStore.setProcessInstanceId(event.getProcessInstanceId());    
                String dshbrd = initDashboardId(event);
                log.info("ACTIVITI PROCESS_STARTED: foundDashboardId = {}", dshbrd);

                break;
            case PROCESS_COMPLETED:
            case PROCESS_CANCELLED:
                String processId = event.getProcessInstanceId();
                // update the dashboard with info that the process has completed
                Dashboard d = dashboardDao.findByProcessInstanceid(processId);
                if (d != null) {
                    d.setLastProcessEndTime(new Date());
                    dashboardDao.update(d);
                }
                ProcessInstanceIdStore.clearProcessInstanceId();

                
                log.info("ACTIVITI PROCESS_CANCELLED: threadId = {}, DashboardIdStore's dashboardId = {}, clearing dashboard id", 
                    Thread.currentThread().getId(), DashboardIdStore.getDashboardId());

                DashboardIdStore.clearDashboardId();
                break;
            case ACTIVITY_COMPLETED:
                log.info("ACTIVITI ACTIVITY_COMPLETED: threadId = {}, DashboardIdStore's dashboardId = {}, clearing dashboard id", 
                    Thread.currentThread().getId(), DashboardIdStore.getDashboardId());
                DashboardIdStore.clearDashboardId();
                break;
            case ACTIVITY_STARTED:
                log.info("ACTIVITI ACTIVITY_STARTED: threadId = {}, DashboardIdStore's dashboardId = {}", 
                    Thread.currentThread().getId(), DashboardIdStore.getDashboardId());
                ProcessInstanceIdStore.setProcessInstanceId(event.getProcessInstanceId());    
                String foundDshbrd = initDashboardId(event);
                log.info("ACTIVITI ACTIVITY_STARTED: foundDashboardId = {}", foundDshbrd);
                break;
            default:
                break;
        }
    }

    private String initDashboardId(ActivitiEvent event) {
        String dashStringId = getDashboardId(event.getProcessInstanceId());
        if (dashStringId != null) {
            try {
                Long dashId = Long.parseLong(dashStringId);
                log.info("initDashboardId: eventType={}, threadId = {}, dashId = {}, DashboardIdStore's dashboardId = {}", 
                    event.getType(), Thread.currentThread().getId(), dashId, DashboardIdStore.getDashboardId());
                DashboardIdStore.setDashboardId(dashId);
            } catch (NumberFormatException nfe) {
                ErrorUtils.logExceptionFmt(log, nfe,
                    "### ERROR: SET THREADLOCAL execId: {1} dashboard id : {2}",
                    event.getExecutionId(), dashStringId);
            }
        }
        return dashStringId;
    }

    private String getDashboardId(String processInstanceId) {
        ExecutionQuery exQuery = runtimeService.createExecutionQuery();
        List<Execution> executions = exQuery.processInstanceId(processInstanceId).list();
        for (Execution execution : executions) {
            String dashboardId = runtimeService
                .getVariable(execution.getId(), DashboardIdStore.DASHBOARD_VARIABLE, String.class);
            if (dashboardId != null) {
                return dashboardId;
            }
            
        }
        ProcessInstance superProcessInstance = runtimeService.createProcessInstanceQuery().subProcessInstanceId(processInstanceId).singleResult();
        if (superProcessInstance != null) {
            return getDashboardId(superProcessInstance.getId());
        }
        return null;
    }
    
    @Override
    public boolean isFailOnException() {
        // signal that we should not fail when an exception in this listener occurs
        return false;
    }

    public RuntimeService getRuntimeService() {
        return runtimeService;
    }

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public DashboardDao getDashboardDao() {
        return dashboardDao;
    }

    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public List<EventMatcher> getEventMatches() {
        return eventMatches;
    }

    public void setEventMatches(List<EventMatcher> eventMatches) {
        this.eventMatches = eventMatches;
    }
}
