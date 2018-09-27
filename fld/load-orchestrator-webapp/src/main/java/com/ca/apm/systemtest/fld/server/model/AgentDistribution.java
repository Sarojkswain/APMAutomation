/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author KEYJA01
 *
 */
@Entity
@Table(name="agent_distributions")
public class AgentDistribution {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="agent_dist_id", nullable=false)
	private Long id;
	
	@Column(name="timestamp")
	private Long timestamp; 
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="distro_data")
	private AgentDistributionData data;
	
	/**
	 * 
	 */
	public AgentDistribution() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

    public AgentDistributionData getData() {
        return data;
    }

    public void setData(AgentDistributionData data) {
        this.data = data;
    }
}
