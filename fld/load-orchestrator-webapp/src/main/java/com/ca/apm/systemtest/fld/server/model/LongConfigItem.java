/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author KEYJA01
 *
 */
@Entity
@DiscriminatorValue("lng")
public class LongConfigItem extends ConfigItem {
	
	@Column(name="long_value", nullable=true)
	private Long longValue;
	

	/**
	 * 
	 */
	public LongConfigItem() {
	}
	
	
	public LongConfigItem(String name, String formId, Long longValue) {
		super(name, formId);
		this.longValue = longValue;
	}



	/* (non-Javadoc)
	 * @see com.ca.apm.systemtest.fld.server.model.ConfigItem#getItemType()
	 */
	@Override
	public ConfigItemType getItemType() {
		return ConfigItemType.LongType;
	}

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}
	
	@Override
	public Object getValue() {
		return longValue;
	}


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LongConfigItem [longValue=" + longValue + ", super=" + super.toString() + "]";
    }
	
	
}
