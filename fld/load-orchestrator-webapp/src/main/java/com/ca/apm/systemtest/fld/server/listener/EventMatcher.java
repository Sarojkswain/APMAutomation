package com.ca.apm.systemtest.fld.server.listener;

import java.util.regex.Pattern;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;

public class EventMatcher {
	private ActivitiEventType[] eventType;
	private Pattern  processDefinitionId;
	private Pattern  processInstanceId;
	private Pattern  executionId;
	private FldLevel logLevel;
	private String   message;
	private String   tag;

	public EventMatcher() {
	}

	public EventMatcher(String types, String processDefinitionId, String processInstanceId
				, String executionId, String logLevel, String tag, String message) {
		this.logLevel = isEmpty(logLevel) ? FldLevel.DEBUG : FldLevel.valueOf(logLevel.toUpperCase());
		this.eventType = isEmpty(types) ? null : ActivitiEventType.getTypesFromString(types);
		this.processDefinitionId = getPattern(processDefinitionId);
		this.processInstanceId = getPattern(processInstanceId);
		this.executionId = getPattern(executionId);
		this.tag = isEmpty(tag) ? null : tag;
		this.message = isEmpty(message) ? null : message;
	}

	public boolean matches(ActivitiEvent event) {
		return eventTypeMatch(event)
			&& patMatch(processDefinitionId, event.getProcessDefinitionId())
			&& patMatch(processInstanceId, event.getProcessInstanceId())
			&& patMatch(executionId, event.getExecutionId());
	}

	private boolean patMatch(Pattern p, String val) {
		return p == null || (val != null && p.matcher(val).find());
	}

	private boolean eventTypeMatch(ActivitiEvent event) {
		if (eventType != null) {
			for (ActivitiEventType mType: eventType) {
				if (event.getType().equals(mType)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private Pattern getPattern(String patternDef) {
		return isEmpty(patternDef) ? null : Pattern.compile(patternDef);
	}

	private boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	// =========================================================================
	// Getters and setters
	// =========================================================================
	public Pattern getProcessDefinitionId() {
		return processDefinitionId;
	}
	public void setProcessDefinitionId(Pattern processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}
	public Pattern getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(Pattern processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public Pattern getExecutionId() {
		return executionId;
	}
	public void setExecutionId(Pattern executionId) {
		this.executionId = executionId;
	}
	public FldLevel getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(FldLevel logLevel) {
		this.logLevel = logLevel;
	}
	public ActivitiEventType[] getEventType() {
		return eventType;
	}
	public void setEventType(ActivitiEventType[] eventType) {
		this.eventType = eventType;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
}