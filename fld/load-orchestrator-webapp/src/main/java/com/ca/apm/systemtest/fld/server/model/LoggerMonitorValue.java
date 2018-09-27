package com.ca.apm.systemtest.fld.server.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;

/**
 * Entity implementation class for Entity: LoggerMonitorValue
 *
 */
@Entity
@Table(name="logger_monitor")
public class LoggerMonitorValue implements Serializable {

	private static final long serialVersionUID = 8589130945421352721L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="logger_id", nullable=false)
	private Long id;
	

	@Column(name="level", nullable=true, length=80)
	private FldLevel level;
	
	@Column(name="category", nullable=true, length=256)
	private String category;

	@Column(name="tag", nullable=true, length=256)
	private String tag;

	@Column(name="message", nullable=true, length=65535)
	private String message;

	@Column(name="exception", nullable=true, length=65535)
	private String exception;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="timestamp", nullable=true, length=256)
	private Date timestamp;
	
	@Column(name="node", nullable=true, length=256)
    private String node;

    @Column(name="dashboard_id", nullable=true)
    private Long dashboardId;
    	
	@Column(name="process_instance_id", nullable=true, length=256)
	private String processInstanceId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="dashboard_id", insertable=false, updatable=false)
    private Dashboard dashboard;
	
	public LoggerMonitorValue() {
    }
	
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FldLevel getLevel() {
        return level;
    }

    public void setLevel(FldLevel level) {
        this.level = level;
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

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
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
        return "LoggerMonitorValue [id=" + id + ", level=" + level + ", category=" + category
            + ", tag=" + tag + ", message=" + message + ", exception=" + exception + ", timestamp="
            + timestamp + ", node=" + node + ", dashboardId=" + dashboardId
            + ", processInstanceId=" + processInstanceId + "]";
    }

	
}
