package com.ca.apm.systemtest.fld.shared.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ResourceFileVO {

    private Long id;
    private Long sizeInBytes;
    private String name;
    
    public ResourceFileVO() {
        
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(Long id) {
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
     * @return the sizeInBytes
     */
    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    /**
     * @param sizeInBytes the sizeInBytes to set
     */
    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    
}
