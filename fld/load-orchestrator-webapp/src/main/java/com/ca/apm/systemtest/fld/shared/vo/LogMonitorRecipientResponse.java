package com.ca.apm.systemtest.fld.shared.vo;

import java.util.Collection;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class LogMonitorRecipientResponse {
    private String status;
    private int count;
    private LogMonitorRecipientVO recipient;
    private Collection<LogMonitorRecipientVO> recipients;

    /**
     * @return the recipient
     */
    public LogMonitorRecipientVO getRecipient() {
        return recipient;
    }

    /**
     * @param recipient the recipient to set
     */
    public void setRecipient(LogMonitorRecipientVO recipient) {
        this.recipient = recipient;
    }

    public Collection<LogMonitorRecipientVO> getRecipients() {
        return recipients;
    }

    public void setRecipients(Collection<LogMonitorRecipientVO> recipients) {
        this.recipients = recipients;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status.toString();
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

}
