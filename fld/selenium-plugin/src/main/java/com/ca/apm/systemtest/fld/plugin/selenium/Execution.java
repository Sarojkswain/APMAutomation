/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.selenium;

/**
 * @author KEYJA01
 *
 */
public class Execution {
    private long startTime;
    private long endTime;
    private ExecutionStatus status;
    
    public enum ExecutionStatus {
        New, Running, StopRequested, Exception, Finished; 
    }

    /**
     * 
     */
    public Execution() {
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }
}
