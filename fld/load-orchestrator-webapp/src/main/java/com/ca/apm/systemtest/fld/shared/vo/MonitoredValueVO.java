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
public class MonitoredValueVO {
	private String key;
	private String group;
	private String name;
	private MonitoredValueStatus value;

	/**
	 * 
	 */
	public MonitoredValueVO() {
		// TODO Auto-generated constructor stub
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

	public MonitoredValueStatus getValue() {
		return value;
	}

	public void setValue(MonitoredValueStatus value) {
		this.value = value;
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
        return "MonitoredValueVO [key=" + key + ", group=" + group + ", name=" + name + ", value="
            + value + "]";
    }
	
}
