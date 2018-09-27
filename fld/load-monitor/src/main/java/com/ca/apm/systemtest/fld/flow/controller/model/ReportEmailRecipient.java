package com.ca.apm.systemtest.fld.flow.controller.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Email recipient configuration entity for FLD email reporting feature.
 *   
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Entity
@Table(name="report_email_recipients")
public class ReportEmailRecipient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = true)
    private String name;
    
    @Column(name = "surname", nullable = true)
    private String surname;
    
    @Column(name = "email", nullable = false)
    private String email;

    public ReportEmailRecipient() {
    }    
    
    public ReportEmailRecipient(String name, String surname, String email) {
        this.name = name;
        this.surname = surname;
        this.email = email;
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
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ReportEmailRecipient [id=" + id + ", name=" + name + ", surname=" + surname
            + ", email=" + email + "]";
    }
}
