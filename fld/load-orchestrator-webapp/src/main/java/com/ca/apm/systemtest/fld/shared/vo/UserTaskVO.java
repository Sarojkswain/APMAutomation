package com.ca.apm.systemtest.fld.shared.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * JSON wrapper for waiting user tasks - 
 * @author ZUNPA01
 *
 */
@JsonInclude(Include.NON_NULL)
public class UserTaskVO {
    private String id;
    private String name;
    private String processInstanceId;
    private String description;
    private String processDefinitionId;
    private List<UserTaskFormPropertyVO> formData;

    public UserTaskVO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<UserTaskFormPropertyVO> getFormData() {
        return formData;
    }

    public void setFormData(List<UserTaskFormPropertyVO> formData) {
        this.formData = formData;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UserTaskVO [id=" + id + ", name=" + name + ", processInstanceId="
            + processInstanceId + ", description=" + description + ", processDefinitionId="
            + processDefinitionId + ", formData=" + formData + "]";
    }

    
}
