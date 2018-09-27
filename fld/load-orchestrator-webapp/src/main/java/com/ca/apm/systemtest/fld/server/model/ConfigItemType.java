/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

/**
 * @author KEYJA01
 *
 */
public enum ConfigItemType {
	StringType("s"), LongType("l"), DoubleType("d"), BooleanType("b"), ComplexType("cx"), CustomType("ct");
	private String val;
	
	private ConfigItemType(String val) {
		this.val = val;
	}

	public String getVal() {
		return val;
	}
	
	public static ConfigItemType convert(String s) {
		for (ConfigItemType t: ConfigItemType.values()) {
			if (t.getVal().equals(s)) {
				return t;
			}
		}
		
		return null;
	}
	
}
