package com.ca.apm.systemtest.fld.server.model;

import java.io.Serializable;

public class NetworkTrafficMonitorValue
    implements
        Comparable<NetworkTrafficMonitorValue>,
        Serializable {

    private static final long serialVersionUID = -2519687499765611790L;

    private String host;
    private String remoteHost;
    private String type;
    private byte[] image;
    private String description;

    public NetworkTrafficMonitorValue(String host, String remoteHost, String type) {
        this.host = host;
        this.remoteHost = remoteHost;
        this.type = type;
    }

    public NetworkTrafficMonitorValue(String host, String remoteHost, String type, byte[] image,
        String description) {
        this(host, remoteHost, type);
        this.image = image;
        this.description = description;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuilder sb =
            (new StringBuilder("NetworkTrafficMonitorValue[,host=")).append(host)
                .append(",remoteHost=").append(remoteHost).append(",type=").append(type);
        if (image != null) {
            sb.append(",image.length=").append(image.length);
        }
        if (description != null) {
            sb.append(",description.length=").append(description.length());
        }
        return sb.append(']').toString();
    }

    @Override
    public int compareTo(NetworkTrafficMonitorValue o) {
        if (this.getHost().equals(o.getHost())) {
            if (this.getRemoteHost().equals(o.getRemoteHost())) {
                return this.getType().compareTo(o.getType());
            } else {
                return this.getRemoteHost().compareTo(o.getRemoteHost());
            }
        } else {
            return this.getHost().compareTo(o.getHost());
        }
    }

}
