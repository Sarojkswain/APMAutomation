/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.vo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Node hearbeat request.
 * 
 * @author keyja01
 *
 */
@JsonTypeInfo(use=Id.CLASS, property="@type")
public class HeartbeatRequest {
	private long timestamp;
	
	/**
	 * 
	 */
	public HeartbeatRequest() {
	}
	

	public HeartbeatRequest(long timestamp) {
		this.timestamp = timestamp;
	}



	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
