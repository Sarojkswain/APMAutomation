/**
 *
 */
package com.ca.apm.systemtest.fld.plugin.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Heartbeat response.
 *
 * @author keyja01
 */
@JsonTypeInfo(use = Id.CLASS, property = "@type")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HeartbeatResponse {
    private String hostName;
    private String ip4;
    private String nodeName;
    private Integer version;
    private long heartbeatRequestTimestamp;
    private long ntpTimeOffset;

    /**
     *
     */
    public HeartbeatResponse() {
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param host the host name to set
     */
    public void setHostName(String host) {
        this.hostName = host;
    }

    /**
     * @return the ip4
     */
    public String getIp4() {
        return ip4;
    }

    /**
     * @param ip4 the ip4 to set
     */
    public void setIp4(String ip4) {
        this.ip4 = ip4;
    }

    /**
     * @return the heartbeatRequestTimestamp
     */
    public long getHeartbeatRequestTimestamp() {
        return heartbeatRequestTimestamp;
    }

    /**
     * @param heartbeatRequestTimestamp the heartbeatRequestTimestamp to set
     */
    public void setHeartbeatRequestTimestamp(long heartbeatRequestTimestamp) {
        this.heartbeatRequestTimestamp = heartbeatRequestTimestamp;
    }

    /**
     * @return time offset from time acquired from NTP server
     */
    public long getNtpTimeOffset() {
        return ntpTimeOffset;
    }

    /**
     * @param ntpTimeOffset time offset from time acquired from NTP server
     */
    public void setNtpTimeOffset(long ntpTimeOffset) {
        this.ntpTimeOffset = ntpTimeOffset;
    }

}
