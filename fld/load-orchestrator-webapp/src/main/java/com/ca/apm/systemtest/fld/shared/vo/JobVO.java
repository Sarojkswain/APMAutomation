package com.ca.apm.systemtest.fld.shared.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class JobVO {

    private String id;
    private String processInstanceId;
    private String executionId;
    private String processDefinitionId;
    private String tenantId;
    private String exceptionMessage;
    private Integer retries;
    private Date duedate;

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
     * @return the processInstanceId
     */
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    /**
     * @param processInstanceId the processInstanceId to set
     */
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    /**
     * @return the executionId
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * @param executionId the executionId to set
     */
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
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
     * @return the tenantId
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId the tenantId to set
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * @return the exceptionMessage
     */
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    /**
     * @param exceptionMessage the exceptionMessage to set
     */
    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    /**
     * @return the retries
     */
    public Integer getRetries() {
        return retries;
    }

    /**
     * @param retries the retries to set
     */
    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    /**
     * @return the duedate
     */
    public Date getDuedate() {
        return duedate;
    }

    /**
     * @param duedate the duedate to set
     */
    public void setDuedate(Date duedate) {
        this.duedate = duedate;
    }
    
    @Override
    public int hashCode() {
        int hc = 0;
        if (executionId != null) {
            hc = executionId.hashCode();
        }
        
        return hc;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JobVO)) {
            return false;
        }
        JobVO j2 = (JobVO) obj;
        if (executionId == null) {
            return false;
        }
        if (processInstanceId == null) {
            return false;
        }
        
        return executionId.equals(j2.executionId) && processInstanceId.equals(j2.processInstanceId);
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "JobVO [id=" + id + ", processInstanceId=" + processInstanceId + ", executionId="
            + executionId + ", processDefinitionId=" + processDefinitionId + ", tenantId="
            + tenantId + ", exceptionMessage=" + exceptionMessage + ", retries=" + retries
            + ", duedate=" + duedate + "]";
    }


}
