/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * @author KEYJA01
 *
 */
@Entity
@Table(name="dashboard_config")
public class DashboardConfig {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="dashboard_config_id", nullable=false)
	private Long id;
	
	@Column(name="version", nullable=false)
	private long version;
	
    @Column(name="hide_non_required_config_parameters", nullable=true)
    private Boolean hideNonRequiredConfigParameters = false;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="dashboard_config_id", referencedColumnName="dashboard_config_id")
	@OrderColumn(name="index")
	private List<ConfigItem> configItems;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="dashboard_config_id", referencedColumnName="dashboard_config_id")
	@OrderColumn(name="index")
	private List<MonitoredValue> monitors;

	
	/**
	 * 
	 */
	public DashboardConfig() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public List<ConfigItem> getConfigItems() {
		return configItems;
	}

	public void setConfigItems(List<ConfigItem> configItems) {
		this.configItems = configItems;
	}

	public List<MonitoredValue> getMonitors() {
		return monitors;
	}

	public void setMonitors(List<MonitoredValue> monitors) {
		this.monitors = monitors;
	}

    /**
     * @return <code>true</code> if non-required configuration parameters should be hidden; 
     *         <code>false</code> otherwise
     */
    public Boolean hideNonRequiredConfigParameters() {
        return hideNonRequiredConfigParameters;
    }

    /**
     * @param hideNonRequiredConfigParameters the hideNonRequiredConfigParameters to set
     */
    public void setHideNonRequiredConfigParameters(Boolean hideNonRequiredConfigParameters) {
        if (hideNonRequiredConfigParameters == null) {
            hideNonRequiredConfigParameters = false;
        }
        this.hideNonRequiredConfigParameters = hideNonRequiredConfigParameters;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DashboardConfig [id=" + id + ", version=" + version
            + ", hideNonRequiredConfigParameters=" + hideNonRequiredConfigParameters + "]";
    }

}
