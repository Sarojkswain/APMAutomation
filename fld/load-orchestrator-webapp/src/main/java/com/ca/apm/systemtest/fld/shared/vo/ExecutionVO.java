package com.ca.apm.systemtest.fld.shared.vo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * JSON wrapper Activiti executions
 * @author ZUNPA01
 *
 */
@JsonInclude(Include.NON_NULL)
public class ExecutionVO implements Serializable {
    private static final long serialVersionUID = 1581766063660651285L;

    private String id;
    private String processInstanceId;
    private String parentId;
    private String activityId;
    private String tenantId;

    public ExecutionVO() {
    }

    public String getId() {
        return (id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessInstanceId() {
        return (processInstanceId);
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getParentId() {
        return (parentId);
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getActivityId() {
        return (activityId);
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getTenantId() {
        return (tenantId);
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

}
