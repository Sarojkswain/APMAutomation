/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.vo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Encapsulates information on an available attribute of the plugin.
 * @author keyja01
 *
 */
@JsonTypeInfo(use=Id.CLASS, property="@type")
public class Attribute {
	private String name;
	private String javaType;
	private boolean readable;
	private boolean writable;
	

	public Attribute(String name, String javaType, boolean readable, boolean writable) {
		this.name = name;
		this.javaType = javaType;
		this.readable = readable;
		this.writable = writable;
	}

	public Attribute() {
		super();
	}

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

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public boolean isWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	@Override
	public int hashCode() {
		int hc = 383832;
		if (readable) {
			hc ^= 55110;
		}
		if (writable) {
			hc ^= -1993292;
		}
		if (name != null) {
			hc ^= name.hashCode();
		}
		if (javaType != null) {
			hc ^= javaType.hashCode();
		}
		return hc;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Attribute)) {
			return false;
		}
		Attribute a2 = (Attribute) obj;
		if (readable != a2.readable || writable != a2.writable) {
			return false;
		}
		if ((name == null || a2.name == null) && (name != a2.name)) {
			return false;
		}
		if (!name.equals(a2.name)) {
			return false;
		}
		if ((javaType == null || a2.javaType == null) && (javaType != a2.javaType)) {
			return false;
		}
		if (!javaType.equals(a2.javaType)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Attribute[name=")
			.append(name)
			.append(",javaType=")
			.append(javaType)
			.append(",readable=")
			.append(readable)
			.append(",writable=")
			.append(writable)
			.append("]");
		return sb.toString();
	}
}
