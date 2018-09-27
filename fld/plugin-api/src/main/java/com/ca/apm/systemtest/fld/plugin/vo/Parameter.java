package com.ca.apm.systemtest.fld.plugin.vo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * @author keyja01
 *
 */
@JsonTypeInfo(use=Id.CLASS, property="@type")
public class Parameter {
	private String name;
	private String javaType;
	private boolean required;
	private String description;
	private String[] values;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String[] getValues() {
	    return values;
	}

	public void setValues(String[] values) {
	    this.values = values;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Parameter[name=")
			.append(name)
			.append(",javaType=")
			.append(javaType)
			.append(",required=")
			.append(required)
			.append(",description=")
			.append(description)
			.append(",values=")
			.append(values)
			.append("]");
		return sb.toString();
	}

}
