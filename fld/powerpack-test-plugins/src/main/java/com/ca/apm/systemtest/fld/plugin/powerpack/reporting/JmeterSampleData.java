package com.ca.apm.systemtest.fld.plugin.powerpack.reporting;

/**
 * Data object representing single Jmeter aggregated sample data.
 * Data is aggregated per unit of time.
 *  
 * The following metrics are aggregated:
 * <ul>
 * <li>Response Status - this will be the worst response code per unit of time</li>
 * <li>Maximal response time in milliseconds - response time of the request which was the slowliest</li>
 * <li>Minimal response time in milliseconds - response time of the request which was the quickest</li>
 * <li>Summary response time - overall response time per the same unit of time</li>
 * <li>Total request count</li>
 * </ul>  
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class JmeterSampleData {
   
    private static final String OK_STATUS = "200";
    
    private String timestamp;
    private String responseStatus;
    private Long minResponseTimeMillis;
    private Long maxResponseTimeMillis;
    private Long sumResponseTimeMillis;
    private Long requestCount;
    
    public JmeterSampleData() {
    }

    
    public JmeterSampleData(String timestamp, String responseStatus, Long minResponseTimeMillis,
        Long maxResponseTimeMillis, Long sumResponseTimeMillis, Long requestCount) {
        this.timestamp = timestamp;
        this.responseStatus = responseStatus;
        this.minResponseTimeMillis = minResponseTimeMillis;
        this.maxResponseTimeMillis = maxResponseTimeMillis;
        this.sumResponseTimeMillis = sumResponseTimeMillis;
        this.requestCount = requestCount;
    }


    /**
     * @return the responseStatus
     */
    public String getResponseStatus() {
        return responseStatus;
    }
    
    /**
     * @param responseStatus the responseStatus to set
     */
    public void setResponseStatus(String responseStatus) {
        this.responseStatus = OK_STATUS.equals(this.responseStatus) ? responseStatus : this.responseStatus;
    }
    
    /**
     * @return the minResponseTimeMillis
     */
    public Long getMinResponseTimeMillis() {
        return minResponseTimeMillis;
    }
    
    /**
     * @param minResponseTimeMillis the minResponseTimeMillis to set
     */
    public void setMinResponseTimeMillis(Long minResponseTimeMillis) {
        this.minResponseTimeMillis = Math.min(this.minResponseTimeMillis, minResponseTimeMillis);
    }
    
    /**
     * @return the maxResponseTimeMillis
     */
    public Long getMaxResponseTimeMillis() {
        return maxResponseTimeMillis;
    }
    
    /**
     * @param maxResponseTimeMillis the maxResponseTimeMillis to set
     */
    public void setMaxResponseTimeMillis(Long maxResponseTimeMillis) {
        this.maxResponseTimeMillis = Math.max(this.maxResponseTimeMillis, maxResponseTimeMillis);
    }
    
    /**
     * @return the sumResponseTimeMillis
     */
    public Long getSumResponseTimeMillis() {
        return sumResponseTimeMillis;
    }
    
    /**
     * 
     * @param responseTimeMillis
     */
    public void addResponseTimeMillis(Long responseTimeMillis) {
        this.sumResponseTimeMillis += responseTimeMillis;
    }
    
    /**
     * @return the requestCount
     */
    public Long getRequestCount() {
        return requestCount;
    }
    
    /**
     * @param requestCount the requestCount to set
     */
    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }

    public void incrementRequestCountBy1() {
        this.requestCount++;
    }

    /**
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
}