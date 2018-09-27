/**
 * 
 */
package com.ca.apm.systemtest.fld.shared.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author KEYJA01
 *
 */
@JsonInclude(Include.NON_NULL)
public class RetrieveLogsRequestVO {
    private Integer maxLogs;
    private Integer offset;
    private String level;
    private String categoryFilter;
    private String tagFilter;
    private String nodeName;
    private String processInstanceId;
    private Integer logsBeforeId;
    private Integer logsAfterId;
    private Long dashboardId;
    
    public RetrieveLogsRequestVO() {
    }

    public Integer getMaxLogs() {
        return maxLogs;
    }

    public void setMaxLogs(Integer maxLogs) {
        this.maxLogs = maxLogs;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCategoryFilter() {
        return categoryFilter;
    }

    public void setCategoryFilter(String categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    public String getTagFilter() {
        return tagFilter;
    }

    public void setTagFilter(String tagFilter) {
        this.tagFilter = tagFilter;
    }

    public Integer getLogsBeforeId() {
        return logsBeforeId;
    }

    public void setLogsBeforeId(Integer logsBeforeId) {
        this.logsBeforeId = logsBeforeId;
    }

    public Integer getLogsAfterId() {
        return logsAfterId;
    }

    public void setLogsAfterId(Integer logsAfterId) {
        this.logsAfterId = logsAfterId;
    }
    
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public Long getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(Long dashboardId) {
        this.dashboardId = dashboardId;
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
     * @return the offset
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(Integer offset) {
        this.offset = offset;
    }
    
    
    
    
}
