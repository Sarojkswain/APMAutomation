package com.ca.apm.systemtest.fld.server.model;

import java.io.Serializable;

public class MemoryMonitorValue implements Serializable {

    private static final long serialVersionUID = 7528365329377493035L;

    private String id;
    private String description;
    private byte[] image;

    public MemoryMonitorValue() {}

    public MemoryMonitorValue(String id) {
        this.id = id;
    }

    public MemoryMonitorValue(String id, String description, byte[] image) {
        this.id = id;
        this.description = description;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return (new StringBuilder("MemoryMonitorValue:id=")).append(id).toString();
    }

}
