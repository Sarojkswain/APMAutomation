package com.ca.apm.systemtest.fld.flow.controller.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextResource;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.ca.apm.systemtest.fld.flow.controller.dao.FldLoadInfoDao;
import com.ca.apm.systemtest.fld.flow.controller.dao.ReportEmailRecipientsDao;
import com.ca.apm.systemtest.fld.flow.controller.model.FldLoadInfo;
import com.ca.apm.systemtest.fld.flow.controller.model.ReportEmailRecipient;
import com.ca.apm.systemtest.fld.flow.controller.vo.LoadInfoVO;

/**
 * Email report service implementation.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Component("fldReportServiceBean")
public class FldReportServiceImpl implements FldReportService, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(FldReportServiceImpl.class);

    private static final String BULK_LOAD_REPORT_TEMPLATE_NAME = "bulk-load-report";
    private static final String FLD_LOAD_STATUS_REPORT_NAME = "FLD Load status report";
    
    public static final String UNKNOWN_NAME = "<UNKNOWN>";
    
    @Autowired
    JavaMailSender mailSender;
    
    @Autowired
    private SpringTemplateEngine templateEngine;
    
    @Autowired
    private ReportEmailRecipientsDao reportEmailRecipientsDao;

    @Autowired
    private FldLoadInfoDao loadInfoDao;
    
    @Value("#{'${default.recipients:}'.split(',')}") 
    private List<String> defaultRecipients;
    
    @Value("${bulk.report.delay.in.seconds:3600}")
    private Long bulkReportDelayInSeconds; 
    
    @Override
    public void afterPropertiesSet() throws Exception {
        //Email recipients post-configuration
        List<ReportEmailRecipient> allRecipients = reportEmailRecipientsDao.findAll();
        if (allRecipients == null || allRecipients.isEmpty()) {
            LOGGER.info("No email recipients found in the db.");
            if (defaultRecipients != null && !defaultRecipients.isEmpty()) {
                LOGGER.info("Found configured default email recipients from external config: {}", 
                    defaultRecipients);

                List<String> validatedDefaultRecipients = new ArrayList<>(defaultRecipients.size());
                EmailValidator emailValidator = EmailValidator.getInstance();
                for (String email : defaultRecipients) {
                    if (!emailValidator.isValid(email)) {
                        LOGGER.warn("Skipping a default recipient email '{}': not a valid email format!", email);
                        continue;
                    }
                    validatedDefaultRecipients.add(email.trim());
                }

                if (validatedDefaultRecipients.isEmpty()) {
                    LOGGER.info("No valid default recipient emails found!");
                } else {
                    LOGGER.info("Persisting default email recipients. Starting...");
                    for (String email : validatedDefaultRecipients) {
                        LOGGER.info("Persisting email: '{}'", email);
                        reportEmailRecipientsDao.create(new ReportEmailRecipient(UNKNOWN_NAME, UNKNOWN_NAME, email));    
                    }
                    LOGGER.info("...Done.");
                }
            }
        } else {
            LOGGER.info("Found following email recipients persisted in the db: {}", allRecipients);
        }
        
        //Log the bulk report delay value.
        LOGGER.info("Bulk report delay in seconds: {}", bulkReportDelayInSeconds);
        
        //Accumulative load and test report post-configuration.
        
        LOGGER.info("Cleaning any left intermediate report information.");
        LOGGER.info("Deleting load info entries:");
        //Clean up load info's.
        int deletionsNum = loadInfoDao.deleteAll();
        LOGGER.info("... deleted {} rows.", deletionsNum);
    }

    @Override
    public void sendDailyTestReportEmail(Map<String, String> contextData) throws MessagingException {
        List<String> emailRecipients = getEmailRecipients();

        String almId = contextData.get("almId");
        Date now = new Date();
        String description = contextData.get("description");
        String passedStr = contextData.get("passed");
        Boolean passed = Boolean.parseBoolean(passedStr);
        
        LOGGER.info("Generating & sending a test report: almId = {}, description = '{}', passed = {}, date = {}", 
            almId, description, passed, now);
        
        final Context ctx = new Context(Locale.US);
        ctx.setVariable("almId", almId);
        ctx.setVariable("reportTime", now);
        ctx.setVariable("description", description);
        ctx.setVariable("passed", passed);

        sendEmail(emailRecipients, "test-report", contextData.get("subject"), ctx, null);
    }

    @Override
    public void sendLoadLaunchReportEmail(Map<String, String> contextData, ServletContext servletContext) throws MessagingException {
        List<String> emailRecipients = getEmailRecipients();
        
        String loadName = contextData.get("loadName");
        String description = contextData.get("description");
        String startedAt = contextData.get("startedAt");
        String finishesAt = contextData.get("finishesAt");
        
        LOGGER.info("Generating & sending a load launch information report: loadName = {}, description = '{}', startedAt = {}, finishesAt = {}", 
            loadName, description, startedAt, finishesAt);
        
        Map<String, Resource> imgResources = new HashMap<>();
        imgResources.put("logoImg", new ServletContextResource(servletContext, "/img/fld_new_load_logo.png"));
        imgResources.put("rocketImg", new ServletContextResource(servletContext, "/img/load_launch_rocket.png"));
        
        final Context ctx = new Context(Locale.US);
        ctx.setVariable("loadName", loadName);
        ctx.setVariable("loadDescription", description);
        ctx.setVariable("startedAt", startedAt);
        ctx.setVariable("finishesAt", finishesAt);
        ctx.setVariable("logoImage", "logoImg");
        ctx.setVariable("rocketImage", "rocketImg");

        sendEmail(emailRecipients, "load-report", contextData.get("subject"), ctx, imgResources);
    }

    @Override
    public synchronized void notifyLoadInfo(final String loadName, final String status, final Date timestamp) {
        List<FldLoadInfo> loadInfos = loadInfoDao.findAll();
        if (loadInfos == null || loadInfos.isEmpty()) {
            //First event. Start counting time and send a report email when the delay runs out.
            launchDelayedBulkLoadReportDispatcher();
        }
        
        loadInfoDao.create(new FldLoadInfo(loadName, status, timestamp));
    }

    private void sendBulkLoadReport(String templateName, String subject) throws MessagingException {
        List<String> emailRecipients = getEmailRecipients();

        List<FldLoadInfo> loadInfos = loadInfoDao.findAll();
        List<LoadInfoVO> loadInfoVOs = new ArrayList<>(loadInfos.size());
        for (FldLoadInfo info : loadInfos) {
            loadInfoVOs.add(new LoadInfoVO(info.getId().getLoadName(), info.getId().getStatus(), info.getTimestamp()));
        }
        
        final Context ctx = new Context(Locale.US);
        Date now = new Date();
        ctx.setVariable("reportTime", now);
        ctx.setVariable("loadInfos", loadInfoVOs);

        sendEmail(emailRecipients, templateName, subject, ctx, null);
        
        //Finally, make sure we delete all intermediate load info reports.
        for (FldLoadInfo info : loadInfos) {
            //Deleting one by one just those we recently fetched from db. 
            //Just in case there are some newer load reports arrived after we queried the db.
            //We'd like to dispatch a second email rather than just removing them. 
            loadInfoDao.delete(info);
        }
        
        loadInfos = loadInfoDao.findAll();
        if (loadInfos != null && !loadInfos.isEmpty()) {
            LOGGER.info("Newer load (count={}) info records found, launching another delayed bulk report dispatcher.", 
                loadInfos.size());
            launchDelayedBulkLoadReportDispatcher();
        }
    }
    
    private void launchDelayedBulkLoadReportDispatcher() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    LOGGER.info("Load info bulk reporter: sleeping for {} seconds before sending a bulk report email.", 
                        bulkReportDelayInSeconds);
                    Thread.sleep(bulkReportDelayInSeconds * 1000L);
                    LOGGER.info("Load info bulk reporter: waking up and sending a bulk load info report email.");
                    sendBulkLoadReport(BULK_LOAD_REPORT_TEMPLATE_NAME, FLD_LOAD_STATUS_REPORT_NAME);
                    LOGGER.info("Load info bulk reporter: report sent.");
                } catch (Exception e) {
                    LOGGER.error("Failed to send a bulk load report: ", e);
                }
            }
        }).start();
    }
    
    private void sendEmail(List<String> recipientEmails, String templateName, String subject, Context ctx, Map<String, Resource> imgResources) throws MessagingException {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); //true = multipart
        messageHelper.setSubject(subject);
        messageHelper.setFrom("FLD_master@ca.com");
        
        messageHelper.setTo(recipientEmails.toArray(new String[recipientEmails.size()]));
        
        final String htmlContent = templateEngine.process(templateName, ctx);
        messageHelper.setText(htmlContent, true); //true = isHtml

        if (imgResources != null) {
            for (Entry<String, Resource> imgResourceEntry : imgResources.entrySet()) {
                messageHelper.addInline(imgResourceEntry.getKey(), imgResourceEntry.getValue());
            }
        }

        // Send mail
        mailSender.send(mimeMessage);        
    }
    
    private List<String> getEmailRecipients() throws MessagingException {
        //Figure out email recipients
        List<ReportEmailRecipient> recipients = reportEmailRecipientsDao.findAll();
        ArrayList<String> recipientEmails = new ArrayList<>(recipients != null ? recipients.size() : 0);
        if (recipients != null) {
            for (ReportEmailRecipient recipient : recipients) {
                recipientEmails.add(recipient.getEmail());
            }
        }

        if (recipientEmails.isEmpty()) {
            String errMsg = "No email recipients configured!";
            LOGGER.error(errMsg);
            throw new MessagingException(errMsg);
        }
        return recipientEmails;
    }

}
