/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Represents a user-defined dashboard configuration
 * @author KEYJA01
 *
 */
@Entity
@Table(name="dashboard")
public class Dashboard {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="dashboard_id", nullable=false)
	private Long id;
	
	/** 
	 * The processKey identifies the process associated with the dashboard in the BPM engine.  The processKey is not unique among 
	 * dashboards, ie. multiple dashboards may share the same processKey.
	 */
	@Column(name="process_key", nullable=false)
	private String processKey;
	
	@Column(name="name", nullable=false)
	private String name;
	
	@Column(name="process_instance_id", length=64, nullable=true)
	private String processInstanceId;
	
    @Column(name="process_business_key", length=64, nullable=true)
	private String processBusinessKey;
	
	@Column(name="icon", nullable=true)
	private String iconName;
	
	@Column(name="last_start_time", nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastProcessStartTime;

	@Column(name="process_definition_version", nullable=true)
	private Integer processDefinitionVersion; 
	
    @Column(name="last_end_time", nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
	private Date lastProcessEndTime;
	
	/**
	 * The dashboard's currently valid configuration.  
	 */
	@OneToOne(cascade=CascadeType.ALL)
	@JoinTable(name="dashboard_dashboard_config", 
		joinColumns=@JoinColumn(name="dashboard_id", referencedColumnName="dashboard_id"), 
		inverseJoinColumns=@JoinColumn(name="dashboard_config_id", referencedColumnName="dashboard_config_id"))
	private DashboardConfig dashboardConfig;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="dashboard_config_history",
		joinColumns=@JoinColumn(name="dashboard_id", referencedColumnName="dashboard_id"),
		inverseJoinColumns=@JoinColumn(name="dashboard_config_id", referencedColumnName="dashboard_config_id"))
	@OrderColumn(name="idx")
	private List<DashboardConfig> dashboardConfigHistory;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="dashboard")
	@OrderColumn(name="workflow_index")
	private List<WorkflowProcessInstance> workflowProcesses;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="dashboard")
    private List<LoggerMonitorValue> logs;
	
	/**
	 * 
	 */
	public Dashboard() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProcessKey() {
		return processKey;
	}

	public void setProcessKey(String processKey) {
		this.processKey = processKey;
	}

	public DashboardConfig getDashboardConfig() {
		return dashboardConfig;
	}

	public void setDashboardConfig(DashboardConfig dashboardConfig) {
		this.dashboardConfig = dashboardConfig;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DashboardConfig> getDashboardConfigHistory() {
		return dashboardConfigHistory;
	}

	public void setDashboardConfigHistory(
			List<DashboardConfig> dashboardConfigHistory) {
		this.dashboardConfigHistory = dashboardConfigHistory;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

    public Date getLastProcessStartTime() {
        return lastProcessStartTime;
    }

    public void setLastProcessStartTime(Date lastProcessStartTime) {
        this.lastProcessStartTime = lastProcessStartTime;
    }

    public Date getLastProcessEndTime() {
        return lastProcessEndTime;
    }

    public void setLastProcessEndTime(Date lastProcessEndTime) {
        this.lastProcessEndTime = lastProcessEndTime;
    }
    
    /**
     * @return the processDefinitionVersion
     */
    public Integer getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    /**
     * @param processDefinitionVersion the processDefinitionVersion to set
     */
    public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }
    
    public String getProcessBusinessKey() {
        return processBusinessKey;
    }

    public void setProcessBusinessKey(String processBusinessKey) {
        this.processBusinessKey = processBusinessKey;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Dashboard [id=" + id + ", processKey=" + processKey + ", name=" + name
            + ", processInstanceId=" + processInstanceId + ", iconName=" + iconName
            + ", lastProcessStartTime=" + lastProcessStartTime + ", processDefinitionVersion="
            + processDefinitionVersion + ", lastProcessEndTime=" + lastProcessEndTime 
            + ", businessKey=" + processBusinessKey + "]";
    }



}
