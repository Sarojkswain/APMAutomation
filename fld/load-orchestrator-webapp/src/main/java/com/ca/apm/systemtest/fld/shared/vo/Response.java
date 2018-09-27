/**
 * 
 */
package com.ca.apm.systemtest.fld.shared.vo;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author keyja01
 *
 */
@JsonInclude(Include.NON_NULL)
public class Response {
	private String status;
    private String returnValue;
    private String latestAgentDistributionVersion;

	private DashboardVO dashboard;

	private List<DashboardVO> dashboards;
	private List<MonitoredValueVO> monitors;
	private List<LogEntryVO> logEntries;
	private List<UserTaskVO> waitingUserTasks;
	private List<NodeVO> nodes;
	private List<PropertyValueVO> propertyList;
	private List<PluginVO> plugins;
	private List<ProcessInstanceVO> processInstances;
	private List<JobVO> jobs;
	private List<ProcessDefinitionVO> processDefinitions;
	private List<ResourceFileVO> resourceFiles;
	
	private Long logsCount;
	
	private Boolean hasAgentDistribution;
	
	/**
	 * Default constructor.
	 */
	public Response() {
	}

	/**
	 * Constructor.
	 * 
	 * @param dashboard   dashboard value object
	 */
	public Response(DashboardVO dashboard) {
		this.dashboard = dashboard;
		setStatus(HttpStatus.OK);
	}

	/**
     * @return the hasAgentDistribution
     */
    public Boolean getHasAgentDistribution() {
        return hasAgentDistribution;
    }

    /**
     * @param hasAgentDistribution the hasAgentDistribution to set
     */
    public void setHasAgentDistribution(Boolean hasAgentDistribution) {
        this.hasAgentDistribution = hasAgentDistribution;
    }

    public DashboardVO getDashboard() {
		return dashboard;
	}

	public void setDashboard(DashboardVO dashboard) {
		this.dashboard = dashboard;
	}

	public List<DashboardVO> getDashboards() {
		return dashboards;
	}

	public void setDashboards(List<DashboardVO> dashboards) {
		this.dashboards = dashboards;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setStatus(HttpStatus status) {
		this.status = status.toString();
	}

	public List<MonitoredValueVO> getMonitors() {
		return monitors;
	}

	public void setMonitors(List<MonitoredValueVO> monitors) {
		this.monitors = monitors;
	}

	public String getLatestAgentDistributionVersion() {
		return latestAgentDistributionVersion;
	}

	public void setLatestAgentDistributionVersion(String latestAgentDistributionVersion) {
		this.latestAgentDistributionVersion = latestAgentDistributionVersion;
	}

    public List<LogEntryVO> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntryVO> logEntries) {
        this.logEntries = logEntries;
    }

    public List<UserTaskVO> getWaitingUserTasks() {
        return waitingUserTasks;
    }

    public void setWaitingUserTasks(List<UserTaskVO> waitingUserTasks) {
        this.waitingUserTasks = waitingUserTasks;
    }

    public List<NodeVO> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeVO> nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the propertyList
     */
    public List<PropertyValueVO> getPropertyList() {
        return propertyList;
    }

    /**
     * @param propertyList the propertyList to set
     */
    public void setPropertyList(List<PropertyValueVO> propertyList) {
        this.propertyList = propertyList;
    }

    public List<PluginVO> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginVO> plugins) {
        this.plugins = plugins;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * @return Activiti process instances
     */
    public List<ProcessInstanceVO> getProcessInstances() {
        return processInstances;
    }

    /**
     * @param procInstances Activiti process instances
     */
    public void setProcessInstances(List<ProcessInstanceVO> procInstances) {
        this.processInstances = procInstances;
    }

    /**
     * @return the logsCount
     */
    public Long getLogsCount() {
        return logsCount;
    }

    /**
     * @param logsCount the logsCount to set
     */
    public void setLogsCount(Long logsCount) {
        this.logsCount = logsCount;
    }

    /**
     * @return the jobs
     */
    public List<JobVO> getJobs() {
        return jobs;
    }

    /**
     * @param jobs the jobs to set
     */
    public void setJobs(List<JobVO> jobs) {
        this.jobs = jobs;
    }

    /**
     * @return the processDefinitions
     */
    public List<ProcessDefinitionVO> getProcessDefinitions() {
        return processDefinitions;
    }

    /**
     * @param processDefinitions the processDefinitions to set
     */
    public void setProcessDefinitions(List<ProcessDefinitionVO> processDefinitions) {
        this.processDefinitions = processDefinitions;
    }

    /**
     * @return the resourceFiles
     */
    public List<ResourceFileVO> getResourceFiles() {
        return resourceFiles;
    }

    /**
     * @param resourceFiles the resourceFiles to set
     */
    public void setResourceFiles(List<ResourceFileVO> resourceFiles) {
        this.resourceFiles = resourceFiles;
    }
    
    
}
