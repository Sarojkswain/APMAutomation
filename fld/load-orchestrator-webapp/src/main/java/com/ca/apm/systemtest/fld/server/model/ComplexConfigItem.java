/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

/**
 * @author KEYJA01
 *
 */
@Entity
@DiscriminatorValue("cpx")
public class ComplexConfigItem extends ConfigItem {
	
	@Column(name="complex_type_name", nullable=true)
	private String complexTypeName;
	
	@Basic
	@Lob
	@Column(name="complex_type_value", nullable=true)
	private String complexTypeValue;

	@Override
	public ConfigItemType getItemType() {
		return ConfigItemType.ComplexType;
	}

	public String getComplexTypeName() {
		return complexTypeName;
	}

	public void setComplexTypeName(String complexTypeName) {
		this.complexTypeName = complexTypeName;
	}

	public String getComplexTypeValue() {
		return complexTypeValue;
	}

	public void setComplexTypeValue(String complexTypeValue) {
		this.complexTypeValue = complexTypeValue;
	}
	
	@Override
	public Object getValue() {
		return complexTypeValue;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ComplexConfigItem [complexTypeName=" + complexTypeName + ", complexTypeValue="
            + complexTypeValue + ", super=" + super.toString() + "]";
    }
	
}
