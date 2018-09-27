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
@DiscriminatorValue("dbl")
public class DoubleConfigItem extends ConfigItem {
	
	@Column(name="double_value", nullable=true)
	private Double doubleValue;

	/**
	 * 
	 */
	public DoubleConfigItem() {
	}

	public DoubleConfigItem(String name, String formId, Double doubleValue) {
		super(name, formId);
		this.doubleValue = doubleValue;
	}

	/* (non-Javadoc)
	 * @see com.ca.apm.systemtest.fld.server.model.ConfigItem#getItemType()
	 */
	@Override
	public ConfigItemType getItemType() {
		return ConfigItemType.DoubleType;
	}

	public Double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}

	@Override
	public Object getValue() {
		return doubleValue;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DoubleConfigItem [doubleValue=" + doubleValue + ", super=" + super.toString()
            + "]";
    }

}
