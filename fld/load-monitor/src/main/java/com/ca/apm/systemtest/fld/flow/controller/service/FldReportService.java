package com.ca.apm.systemtest.fld.flow.controller.service;

import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;

/**
 * Service for sending email reports. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public interface FldReportService {

    /**
     * Sends a daily test report email to the persisted recipients populating a corresponding email template
     * with the provided <code>contextData</code>.
     * 
     * @param  contextData           email template context data
     * @throws MessagingException    post man does not want to send your email
     */
    public void sendDailyTestReportEmail(Map<String, String> contextData) throws MessagingException;
    
    /**
     * Sends a load launch report email to the persisted recipients populating a corresponding email template 
     * with the provided <code>contextData</code>.
     * 
     * @param contextData            email template context data
     * @param servletContext         servlet context to help resolve image resources
     * @throws MessageingException   post man does not want to send your email
     */
    public void sendLoadLaunchReportEmail(Map<String, String> contextData, ServletContext servletContext) throws MessagingException;
    
    public void notifyLoadInfo(String loadName, String status, Date timestamp);
    
}
