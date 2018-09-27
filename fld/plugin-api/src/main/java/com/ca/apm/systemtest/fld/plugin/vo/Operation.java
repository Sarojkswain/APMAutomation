/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.vo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * @author keyja01
 *
 */
@JsonTypeInfo(use=Id.CLASS, property="@type")
public class Operation {
	private String name;
	private String javaReturnType;
	private String description;
	private Parameter[] parameters;
	
	public Parameter createParameter() {
		return new Parameter();
	}
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getJavaReturnType() {
		return javaReturnType;
	}


	public void setJavaReturnType(String javaReturnType) {
		this.javaReturnType = javaReturnType;
	}


	public Parameter[] getParameters() {
		return parameters;
	}


	public void setParameters(Parameter[] parameters) {
		this.parameters = parameters;
	}
	

	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Operation[name=")
			.append(name)
			.append(",javaReturnType=")
			.append(javaReturnType)
			.append(",description=")
			.append(description)
			.append(",parameters={");
		String comma = "";
		for (Parameter p: parameters) {
			sb.append(comma).append(p);
			comma = ",";
		}
		sb.append("}]");
		return sb.toString();
	}
}
