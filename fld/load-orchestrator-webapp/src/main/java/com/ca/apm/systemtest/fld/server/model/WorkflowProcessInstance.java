/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author KEYJA01
 *
 */
@Entity
@Table(name="workflow_process")
public class WorkflowProcessInstance {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="workflow_process_id", nullable=false)
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="dashboard_id")
	private Dashboard dashboard;

	/**
	 * 
	 */
	public WorkflowProcessInstance() {
	}

}
