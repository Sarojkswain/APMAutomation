/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author KEYJA01
 *
 */
@Entity
@Table(name="monitors")
public class MonitoredValue {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="monitor_id", nullable=false)
	private Long id;
	
	@Column(name="key", nullable=false, length=256)
	private String key;
	
	@Column(name="name", nullable=false, length=80)
	private String name;
	
	@Column(name="monitor_group", nullable=true, length=80)
	private String group;

	/**
	 * 
	 */
	public MonitoredValue() {
	}
	
	public MonitoredValue(String key, String name) {
	    this(key, name, null);
	}

	public MonitoredValue(String key, String name, String group) {
	    this.key = key;
	    this.name = name;
	    this.group = group;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MonitoredValue [id=" + id + ", key=" + key + ", name=" + name + ", group=" + group
            + "]";
    }
	
}
