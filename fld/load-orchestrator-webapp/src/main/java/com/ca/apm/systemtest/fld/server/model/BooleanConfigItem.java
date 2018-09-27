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
@DiscriminatorValue("bln")
public class BooleanConfigItem extends ConfigItem {
	
	@Column(name="boolean_value", nullable=true)
	private Boolean booleanValue;

	/**
	 * 
	 */
	public BooleanConfigItem() {
		// TODO Auto-generated constructor stub
	}
	
	public BooleanConfigItem(String name, String formId, Boolean val) {
		super(name, formId);
		this.booleanValue = val;
	}

	/* (non-Javadoc)
	 * @see com.ca.apm.systemtest.fld.server.model.ConfigItem#getItemType()
	 */
	@Override
	public ConfigItemType getItemType() {
		return ConfigItemType.BooleanType;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	@Override
	public Object getValue() {
		return booleanValue;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "BooleanConfigItem [booleanValue=" + booleanValue + ", super="
            + super.toString() + "]";
    }
	
	
}
