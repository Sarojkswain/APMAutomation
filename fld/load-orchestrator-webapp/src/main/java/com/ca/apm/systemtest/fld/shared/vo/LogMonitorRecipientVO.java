package com.ca.apm.systemtest.fld.shared.vo;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 
 * VO class for transferring log monitor email recipients information.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@JsonInclude(Include.NON_NULL)
public class LogMonitorRecipientVO {
    private Long id;
    private Long timestamp;

    private String name;
    private String surname;
    private String telephone;
    private String reason;

    private Collection<String> emailAddresses;

    /**
     * Default constructor.
     */
    public LogMonitorRecipientVO() {}

    /**
     * Constructor with initializing values.
     * 
     * @param id
     * @param timestamp
     * @param name
     * @param surname
     * @param telephone
     * @param reason
     * @param emailAddresses
     */
    public LogMonitorRecipientVO(Long id, Long timestamp, String name, String surname,
        String telephone, String reason, Collection<String> emailAddresses) {
        this.id = id;
        this.timestamp = timestamp;
        this.name = name;
        this.surname = surname;
        this.telephone = telephone;
        this.reason = reason;
        this.emailAddresses = emailAddresses;
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
     * @return the timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Long timestamp) {
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
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return the telephone
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * @param telephone the telephone to set
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return the emailAddresses
     */
    public Collection<String> getEmailAddresses() {
        return emailAddresses;
    }

    /**
     * @param emailAddresses the emailAddresses to set
     */
    public void setEmailAddresses(Collection<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((emailAddresses == null) ? 0 : emailAddresses.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + ((surname == null) ? 0 : surname.hashCode());
        result = prime * result + ((telephone == null) ? 0 : telephone.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        LogMonitorRecipientVO other = (LogMonitorRecipientVO) obj;
        if (emailAddresses == null) {
            if (other.emailAddresses != null) return false;
        } else if (!emailAddresses.equals(other.emailAddresses)) return false;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (reason == null) {
            if (other.reason != null) return false;
        } else if (!reason.equals(other.reason)) return false;
        if (surname == null) {
            if (other.surname != null) return false;
        } else if (!surname.equals(other.surname)) return false;
        if (telephone == null) {
            if (other.telephone != null) return false;
        } else if (!telephone.equals(other.telephone)) return false;
        if (timestamp == null) {
            if (other.timestamp != null) return false;
        } else if (!timestamp.equals(other.timestamp)) return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LogMonitorRecipientVO [id=" + id + ", timestamp=" + timestamp + ", name=" + name
            + ", surname=" + surname + ", telephone=" + telephone + ", reason=" + reason
            + ", emailAddresses=" + emailAddresses + "]";
    }


}
