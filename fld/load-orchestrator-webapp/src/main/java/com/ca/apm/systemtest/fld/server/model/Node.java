package com.ca.apm.systemtest.fld.server.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents a remote node with running agent
 * 
 * @author ZUNPA01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Entity
@Table(name = "node")
public class Node implements Serializable {
    private static final long serialVersionUID = 4575821450343606875L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "node_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "hostname", nullable = true)
    private String hostName;

    @Column(name = "ip4", nullable = true)
    private String ip4;

    @Column(name = "last_heartbeat", nullable = true)
    private Long lastHeartbeat;

    @Column(name = "last_heartbeat_request", nullable = true)
    private Long lastHeartbeatRequest;

    @Column(name = "version", nullable = true)
    private Integer version = -1;

    @Column(name = "agent_updating", nullable = true)
    private Boolean isAgentUpdating;

    @Column(name = "ntp_time_offset", nullable = true)
    private Long ntpTimeOffset;

    public Node() {
    }

    public Long getId() {
        return (id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return (name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLastHeartbeat() {
        return (lastHeartbeat);
    }

    public void setLastHeartbeat(Long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Long getLastHeartbeatRequest() {
        return (lastHeartbeatRequest);
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
     * @param host  the host name to set
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

    @Override
    public String toString() {
        return name;
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
