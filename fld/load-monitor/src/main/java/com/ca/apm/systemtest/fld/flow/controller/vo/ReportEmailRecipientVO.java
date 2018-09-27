package com.ca.apm.systemtest.fld.flow.controller.vo;

/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ReportEmailRecipientVO {

    private Long id;
    private String name;
    private String surname;
    private String email;
    
    public ReportEmailRecipientVO() {
    
    }
    
    public ReportEmailRecipientVO(Long id, String name, String surname, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    /**
     * 
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     * 
     * @param id
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
}
