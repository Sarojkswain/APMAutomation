/**
 * 
 */
package com.ca.apm.systemtest.fld.shared.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * JSON wrapper for config items - 
 * @author KEYJA01
 *
 */
@JsonInclude(Include.NON_NULL)
public class ConfigItemVO {
	private String formId;
	private String type;
	private String name;
	private String typeInformation;
	private Object value;
	private Boolean required;

	/**
	 * 
	 */
	public ConfigItemVO() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * @return the typeInformation
     */
    public String getTypeInformation() {
        return typeInformation;
    }

    /**
     * @param typeInformation the typeInformation to set
     */
    public void setTypeInformation(String typeInformation) {
        this.typeInformation = typeInformation;
    }
    
    
}
