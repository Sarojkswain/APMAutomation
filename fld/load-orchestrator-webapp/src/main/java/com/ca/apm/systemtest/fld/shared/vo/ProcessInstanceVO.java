package com.ca.apm.systemtest.fld.shared.vo;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ProcessInstanceVO {

    private String id;
    private String businessKey;
    private String processDefinitionId;
    private String name;
    private Date startTime;
    private Date endTime;
    private Long durationInMillis;
    private Map<String, Object> processVariables;
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * @return the businessKey
     */
    public String getBusinessKey() {
        return businessKey;
    }
    
    /**
     * @param businessKey the businessKey to set
     */
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }
    
    /**
     * @return the processDefinitionId
     */
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }
    
    /**
     * @param processDefinitionId the processDefinitionId to set
     */
    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }
    
    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }
    
    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    /**
     * @return the durationInMillis
     */
    public Long getDurationInMillis() {
        return durationInMillis;
    }
    
    /**
     * @param durationInMillis the durationInMillis to set
     */
    public void setDurationInMillis(Long durationInMillis) {
        this.durationInMillis = durationInMillis;
    }
    
    /**
     * @return the processVariables
     */
    public Map<String, Object> getProcessVariables() {
        return processVariables;
    }
    
    /**
     * @param processVariables the processVariables to set
     */
    public void setProcessVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
    }
    
}
