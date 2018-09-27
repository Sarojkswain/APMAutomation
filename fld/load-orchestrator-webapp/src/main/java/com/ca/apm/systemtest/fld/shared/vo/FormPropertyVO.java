package com.ca.apm.systemtest.fld.shared.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class FormPropertyVO {

    private String id;
    private String name;
    private String value;
    private String type;
    private Boolean isReadable;
    private Boolean isWritable;
    private Boolean isRequired;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the isReadable
     */
    public Boolean isReadable() {
        return isReadable;
    }

    /**
     * @param isReadable the isReadable to set
     */
    public void setReadable(Boolean isReadable) {
        this.isReadable = isReadable;
    }

    /**
     * @return the isWritable
     */
    public Boolean isWritable() {
        return isWritable;
    }

    /**
     * @param isWritable the isWritable to set
     */
    public void setWritable(Boolean isWritable) {
        this.isWritable = isWritable;
    }

    /**
     * @return the isRequired
     */
    public Boolean isRequired() {
        return isRequired;
    }

    /**
     * @param isRequired the isRequired to set
     */
    public void setRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

}
