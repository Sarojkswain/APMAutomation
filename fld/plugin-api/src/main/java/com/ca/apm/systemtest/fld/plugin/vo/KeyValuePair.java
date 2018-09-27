/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.vo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Simple VO class for transferring a key-value pair
 * @author keyja01
 *
 */
@JsonTypeInfo(use = Id.CLASS, property = "@type")
public class KeyValuePair {
    private String key;
    private Object value;

    

    public KeyValuePair(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public KeyValuePair() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }


}
