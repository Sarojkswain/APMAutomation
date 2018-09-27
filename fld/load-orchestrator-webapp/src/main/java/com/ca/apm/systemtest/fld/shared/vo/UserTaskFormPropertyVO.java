package com.ca.apm.systemtest.fld.shared.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * JSON wrapper for waiting user task from data 
 * @author ZUNPA01
 *
 */
@JsonInclude(Include.NON_NULL)
public class UserTaskFormPropertyVO {
    private String id;
    private String name;
    private String type;
    private String typeInformation;
    private String value;
    private Boolean readable;
    private Boolean required;
    private Boolean writable;

    public UserTaskFormPropertyVO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getReadable() {
        return readable;
    }

    public void setReadable(Boolean readable) {
        this.readable = readable;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getWritable() {
        return writable;
    }

    public void setWritable(Boolean writable) {
        this.writable = writable;
    }

    public String getTypeInformation() {
        return typeInformation;
    }

    public void setTypeInformation(String typeInformation) {
        this.typeInformation = typeInformation;
    }

}
