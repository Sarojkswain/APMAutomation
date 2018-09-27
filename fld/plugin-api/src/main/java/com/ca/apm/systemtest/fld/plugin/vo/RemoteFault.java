/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.vo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * 
 * 
 * @author keyja01
 * @deprecated use {@link RemoteCallResult} and its factory method to create fault result objects
 *
 */
@JsonTypeInfo(use=Id.CLASS, property="@type")
public class RemoteFault {
	private String code;
	private String message;
	
	/**
	 * 
	 */
	public RemoteFault() {
	}

	public RemoteFault(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
