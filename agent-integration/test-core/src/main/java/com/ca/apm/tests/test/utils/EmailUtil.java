/*
#  ~ Copyright (c) 2017. CA Technologies.  All rights reserved.
#  ~
#  ~
#  ~ Author:  kurma05
#
 */
package com.ca.apm.tests.test.utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides util methods for sending email. 
 *
 */
public class EmailUtil {
    
	private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);
	private static final String MESSAGE_END_TEXT = "<br/><br/> -- This email is an automatic notification sent by saas automation";
	private static final String SMTP_SERVER = "mail.ca.com";
	private static final String EMAIL_SENDER = "Team-APM-Automation@ca.com";
	
	public static void sendEmail(String subject, String msgText, String recipients,
                                 ArrayList<String> fileNames, String msgPriority) {
      
        sendEmail(SMTP_SERVER, EMAIL_SENDER, recipients, subject, msgText, fileNames, msgPriority);
    }
	
    public static void sendEmail(String smtpServer, 
                                 String sender, 
                                 String recipients, 
                                 String subject, 
                                 String msgText,
                                 ArrayList<String> fileNames,
                                 String msgPriority) {
        
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", smtpServer);
        Session session = Session.getDefaultInstance(properties);

        try {
            if(recipients == null) throw new MessagingException("List of email recipients wasn't provided!");
            
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            for(String recipient: recipients.trim().split(";|,")) {
                
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }

            // set the subject
            subject = subject + " - " + InetAddress.getLocalHost().getHostName();             
            message.setSubject(subject);   
            message.setHeader("X-Priority", msgPriority);
            Multipart multipart = new MimeMultipart();
            MimeBodyPart messageText = new MimeBodyPart();  
            messageText.setContent(msgText + MESSAGE_END_TEXT, "text/html; charset=utf-8");
            multipart.addBodyPart(messageText);

            //attach file
            if(fileNames != null) { 
                for (String fileName: fileNames) {
                    logger.info("Attaching file: " + fileName);
                    MimeBodyPart attachment = new MimeBodyPart();
                    attachment.attachFile(fileName);  
                    multipart.addBodyPart(attachment);
                }
            }
            
            message.setContent(multipart);   
            Transport.send(message);
            
            logger.debug("Email meta data:- smtp server: " + smtpServer + ", sender: " + sender + ", recipients: "
                    + recipients);
            logger.info("Email message sent");
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}