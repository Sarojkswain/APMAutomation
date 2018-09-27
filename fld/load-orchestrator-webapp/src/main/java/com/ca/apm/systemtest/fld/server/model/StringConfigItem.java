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
@DiscriminatorValue("str")
public class StringConfigItem extends ConfigItem {
	
	@Column(name="string_value", nullable=true, length=4096)
	private String stringValue;

	/**
	 * 
	 */
	public StringConfigItem() {
	}
	
	public StringConfigItem(String name, String formId, String stringValue) {
		super(name, formId);
		this.stringValue = stringValue;
	}


	/* (non-Javadoc)
	 * @see com.ca.apm.systemtest.fld.server.model.ConfigItem#getItemType()
	 */
	@Override
	public ConfigItemType getItemType() {
		return ConfigItemType.StringType;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
	@Override
	public Object getValue() {
		return stringValue;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "StringConfigItem [stringValue=" + stringValue + ", super=" + super.toString()
            + "]";
    }
	
	
}
