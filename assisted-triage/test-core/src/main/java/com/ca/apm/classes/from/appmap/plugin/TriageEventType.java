/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.classes.from.appmap.plugin;

import java.util.HashMap;
import java.util.Map;

public enum TriageEventType {
	UVB(TriageEventConstants.UVB_NAME), 
	ERROR(TriageEventConstants.ERROR_NAME), 
	STALL(TriageEventConstants.STALL_NAME), 
	ALERT(TriageEventConstants.ALERT_NAME);

	public static class TriageEventConstants {
		public static final String UVB_NAME = "uvb";
		public static final String ERROR_NAME = "error";
		public static final String STALL_NAME = "stall";
		public static final String ALERT_NAME = "alert";
	}

	private String value = null;

	TriageEventType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return value();
	}

	public static final TriageEventType fromString(String value) {
		return stringToEnum.get(value);
	}

	private static final Map<String, TriageEventType> stringToEnum = new HashMap<String, TriageEventType>();

	static {
		for (TriageEventType type : values()) {
			stringToEnum.put(type.value(), type);
		}
	}

}