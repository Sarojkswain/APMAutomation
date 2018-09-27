/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 
 * Entity class for storing log monitor email recipients. 
 * 
 * @author TAVPA01
 * @author sinal04
 */
@Entity
@Table(name="log_monitor_user")
public class LogMonitorUser {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "log_monitor_user_id", nullable = false)
	private Long id;
	
	@Column(name = "timestamp", nullable = true)
	private Long timestamp;
	
	@Column(name = "name", nullable = true)
	private String name;
	
	@Column(name = "surname", nullable = true)
	private String surname;
	
	@Column(name = "telephone", nullable = true)
	private String telephone;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "log_monitor_user_id", referencedColumnName = "log_monitor_user_id")
	private Collection<LogMonitorUserEmail> emailEntries;

	@Column(name = "reason", nullable = true)
	private String reason;

	public LogMonitorUser() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Collection<LogMonitorUserEmail> getEmailEntries() {
		return emailEntries;
	}

	public Collection<String> getStringEmailAddresses() {
	    Collection<String> result = new ArrayList<String>(emailEntries != null ? emailEntries.size() : 0);
	    if (emailEntries != null) {
	        for (LogMonitorUserEmail emailInfo : emailEntries) {
	            result.add(emailInfo.getEmailAddress());
	        }
	    }
	    return result;
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

    public void setEmailEntries(Collection<LogMonitorUserEmail> emailEntries) {
		this.emailEntries = emailEntries;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LogMonitorUser [id=" + id + ", timestamp=" + timestamp + ", name=" + name
            + ", surname=" + surname + ", telephone=" + telephone + ", emailAddresses="
            + emailEntries + ", reason=" + reason + "]";
    }
	
	
}
