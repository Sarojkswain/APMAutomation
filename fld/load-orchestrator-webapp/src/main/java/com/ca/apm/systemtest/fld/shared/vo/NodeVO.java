package com.ca.apm.systemtest.fld.shared.vo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * Load Orchestrator node value object.
 *
 * @author keyja01
 */
@JsonInclude(Include.NON_NULL)
public class NodeVO implements Serializable {
    private static final long serialVersionUID = -4669156284391749786L;

    private Long id;
    private String name;
    private String hostName;
    private String ip4;
    /**
     * Indicates the health of the node - when the node responds to a heartbeat request, this
     * timestamp is updated
     */
    private Long lastHeartbeat;
    /**
     * When a broadcast heartbeat request is sent, this is set
     */
    private Long lastHeartbeatRequest;
    /**
     * Indicated version of agent
     */
    private Integer version = -1;
    private Boolean isAvailable;
    private Boolean isAgentUpdating;
    private Long ntpTimeOffset;

    /**
     *
     */
    public NodeVO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Long getLastHeartbeatRequest() {
        return lastHeartbeatRequest;
    }

    public void setLastHeartbeatRequest(Long lastHeartbeatRequest) {
        this.lastHeartbeatRequest = lastHeartbeatRequest;
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
     * @return the isAvailable
     */
    public Boolean getIsAvailable() {
        return isAvailable;
    }

    /**
     * @param isAvailable the isAvailable to set
     */
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    /**
     * @return the isAgentUpdating
     */
    public Boolean getIsAgentUpdating() {
        return isAgentUpdating;
    }

    /**
     * @param isAgentUpdating the isAgentUpdating to set
     */
    public void setIsAgentUpdating(Boolean isAgentUpdating) {
        this.isAgentUpdating = isAgentUpdating;
    }

    public Long getNtpTimeOffset() {
        return ntpTimeOffset;
    }

    public void setNtpTimeOffset(Long ntpTimeOffset) {
        this.ntpTimeOffset = ntpTimeOffset;
    }
}
