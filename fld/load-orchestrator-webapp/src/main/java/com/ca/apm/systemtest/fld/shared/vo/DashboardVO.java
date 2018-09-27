/**
 * 
 */
package com.ca.apm.systemtest.fld.shared.vo;

import java.util.Date;
import java.util.List;

/**
 * @author keyja01
 *
 */
public class DashboardVO {
	private Long id;
	
	private String processKey;
	
	private String name;
	
	private List<ConfigItemVO> config;
	
	private List<MonitoredValueVO> monitors;
	
	private boolean hideNonRequiredConfigParameters;
	private boolean active;
	private boolean suspended;
    private boolean hasWaitingUserTasks;
    private boolean hasStuckJobs;
	
	private Date processStarted;
	private Date processEnded;
	private String processInstanceId;
	
	private String iconName;

	
	private List<ExecutionVO> executions;
	
	/**
	 * 
	 */
	public DashboardVO() {
		// TODO Auto-generated constructor stub
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ConfigItemVO> getConfig() {
		return config;
	}

	public void setConfig(List<ConfigItemVO> config) {
		this.config = config;
	}

	public List<MonitoredValueVO> getMonitors() {
		return monitors;
	}

	public void setMonitors(List<MonitoredValueVO> monitors) {
		this.monitors = monitors;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

    public Date getProcessStarted() {
        return processStarted;
    }

    public void setProcessStarted(Date processStarted) {
        this.processStarted = processStarted;
    }

    public Date getProcessEnded() {
        return processEnded;
    }

    public void setProcessEnded(Date processEnded) {
        this.processEnded = processEnded;
    }

    public List<ExecutionVO> getExecutions() {
        return executions;
    }

    public void setExecutions(List<ExecutionVO> executions) {
        this.executions = executions;
    }

    /**
     * @return the hideNonRequiredConfigParameters
     */
    public boolean isHideNonRequiredConfigParameters() {
        return hideNonRequiredConfigParameters;
    }

    /**
     * @param hideNonRequiredConfigParameters the hideNonRequiredConfigParameters to set
     */
    public void setHideNonRequiredConfigParameters(boolean hideNonRequiredConfigParameters) {
        this.hideNonRequiredConfigParameters = hideNonRequiredConfigParameters;
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
     * @return the hasWaitingUserTasks
     */
    public boolean isHasWaitingUserTasks() {
        return hasWaitingUserTasks;
    }

    /**
     * @param hasWaitingUserTasks the hasWaitingUserTasks to set
     */
    public void setHasWaitingUserTasks(boolean hasWaitingUserTasks) {
        this.hasWaitingUserTasks = hasWaitingUserTasks;
    }

    /**
     * @return the hasStuckJobs
     */
    public boolean isHasStuckJobs() {
        return hasStuckJobs;
    }

    /**
     * @param hasStuckJobs the hasStuckJobs to set
     */
    public void setHasStuckJobs(boolean hasStuckJobs) {
        this.hasStuckJobs = hasStuckJobs;
    }
    
}
