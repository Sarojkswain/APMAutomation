
package com.ca.apm.systemtest.fld.common.logmonitor;

import java.util.Date;


public class LoggerMessage {

    private FldLevel fldLevel;
    private String category;
    private String tag;
    private String message;
    private String except;
    private Date timestamp;
    private String node;
    private Long dashboardId;
    private String processInstanceId;


    public LoggerMessage(FldLevel fldLevel, String processInstanceId, String category, String tag, String message,
        String except, Date timestamp) {
        super();
        this.processInstanceId = processInstanceId;
        this.fldLevel = fldLevel;
        this.category = category;
        this.tag = tag;
        this.message = message;
        this.except = except;
        this.timestamp = timestamp;
        this.node = null;
        this.dashboardId = null;
    }

    public LoggerMessage() {
        super();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((dashboardId == null) ? 0 : dashboardId.hashCode());
        result = prime * result + ((except == null) ? 0 : except.hashCode());
        result = prime * result + ((fldLevel == null) ? 0 : fldLevel.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        result = prime * result + ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        LoggerMessage other = (LoggerMessage) obj;
        if (category == null) {
            if (other.category != null) return false;
        } else if (!category.equals(other.category)) return false;
        if (dashboardId == null) {
            if (other.dashboardId != null) return false;
        } else if (!dashboardId.equals(other.dashboardId)) return false;
        if (except == null) {
            if (other.except != null) return false;
        } else if (!except.equals(other.except)) return false;
        if (fldLevel != other.fldLevel) return false;
        if (message == null) {
            if (other.message != null) return false;
        } else if (!message.equals(other.message)) return false;
        if (node == null) {
            if (other.node != null) return false;
        } else if (!node.equals(other.node)) return false;
        if (processInstanceId == null) {
            if (other.processInstanceId != null) return false;
        } else if (!processInstanceId.equals(other.processInstanceId)) return false;
        if (tag == null) {
            if (other.tag != null) return false;
        } else if (!tag.equals(other.tag)) return false;
        if (timestamp == null) {
            if (other.timestamp != null) return false;
        } else if (!timestamp.equals(other.timestamp)) return false;
        return true;
    }

    public FldLevel getFldLevel() {
        return fldLevel;
    }

    public void setFldLevel(FldLevel fldLevel) {
        this.fldLevel = fldLevel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExcept() {
        return except;
    }

    public void setExcept(String except) {
        this.except = except;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LoggerMessage [fldLevel=" + fldLevel + ", category=" + category + ", tag=" + tag
            + ", message=" + message + ", except=" + except + ", timestamp=" + timestamp
            + ", node=" + node + ", dashboardId=" + dashboardId + ", processInstanceId="
            + processInstanceId + "]";
    }

}
