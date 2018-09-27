package com.ca.apm.systemtest.fld.flow.controller.vo;

import java.util.Date;

/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class LoadInfoVO {

    private String name;
    private String status;
    private Date timestamp;
    
    public LoadInfoVO() {
        
    }
    
    public LoadInfoVO(String name, String status, Date timestamp) {
        this.name = name;
        this.status = status;
        this.timestamp = timestamp;
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
     * @return the status
     */
    public String getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }
    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    
    
}
