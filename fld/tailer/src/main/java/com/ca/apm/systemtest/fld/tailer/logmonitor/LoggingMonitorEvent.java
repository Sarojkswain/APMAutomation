package com.ca.apm.systemtest.fld.tailer.logmonitor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggingMonitorEvent {
    private static final SimpleDateFormat formatDate = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss.SSS");
    private Date timestamp;
    private String serverId;
    private String logFileLocation;
    private String hostName;
    private String log;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getLogFileLocation() {
        return logFileLocation;
    }

    public void setLogFileLocation(String logFileLocation) {
        this.logFileLocation = logFileLocation;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public String toString() {
        String fileName = "";
        if (logFileLocation != null) {
            int l1 = logFileLocation.lastIndexOf('/');
            int l2 = logFileLocation.lastIndexOf('\\');
            int l = l1 == -1 && l2 == -1 ? 0 : l1 > l2 ? l1 : l2;
            fileName = logFileLocation.substring(l + 1);
        }
        String logHead = "";
        if (log != null) {
            int nlPos = log.indexOf('\n');
            if (nlPos == -1 || (log.length() - nlPos) <= 1) {
                logHead = nlPos != -1 ? log.substring(0, nlPos) : log;
            } else {
                logHead =
                    log.substring(0, nlPos) + " ... another " + (log.length() - nlPos)
                        + " chars";
            }
        }
        return "[" + hostName + "::" + serverId + "::" + fileName + "] [" + formatDate
            .format(timestamp) + "] " + logHead;
    }
}
