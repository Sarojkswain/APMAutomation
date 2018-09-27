package com.ca.apm.systemtest.fld.server;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.files.FileUtils;
import com.ca.apm.systemtest.fld.plugin.logmonitor.LoggingMonitorEvent;
import com.ca.apm.systemtest.fld.server.dao.LogMonitorUserHibernate4Dao;
import com.ca.apm.systemtest.fld.server.model.LogMonitorUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class SendEmailListener implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(SendEmailListener.class);

	@Autowired
	private LogMonitorUserHibernate4Dao logMonitorDao;

	private JavaMailSender mailSender;
	private SimpleMailMessage messageTemplate;
	private String mailTemplate;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void onMessage(Message jmsMessage) {
		if (jmsMessage instanceof TextMessage) {
			try {
				TextMessage txtMessage = (TextMessage) jmsMessage;
				LoggingMonitorEvent event = mapper.readValue(txtMessage.getText(), LoggingMonitorEvent.class);

				String result = formatMessageBody(event.getHostName(), event.getTimestamp().toString(),
				    event.getLogFileLocation(), event.getLog()); //mailTemplate;
//				result = result.replaceAll("\\$hostname", event.getHostName());
//				result = result.replaceAll("\\$file-location", event.getLogFileLocation());
//				result = result.replaceAll("\\$server-id", event.getServerId());
//				result = result.replaceAll("\\$timestamp", event.getTimestamp().toString());
//				result = result.replaceAll("\\$log", event.getLog());

				List<LogMonitorUser> allUsers = logMonitorDao.findAll();
				String[] templateToEmailAddresses = messageTemplate.getTo();
				int recipientsAmount = 0;
				if (allUsers != null) {
				    recipientsAmount += allUsers.size();
				}
				if (templateToEmailAddresses != null) {
				    recipientsAmount += templateToEmailAddresses.length;
				}
				
				Collection<String> toEmailAddresses = new ArrayList<String>(recipientsAmount);
				
				if (allUsers.size() == 0) {
				    toEmailAddresses.addAll(Arrays.asList(messageTemplate.getTo()));
				} else {
					for (int i = 0; i < allUsers.size(); i++) {
					    LogMonitorUser logMonitorUser = allUsers.get(i);
					    toEmailAddresses.addAll(logMonitorUser.getStringEmailAddresses());
					}
				}
				
				String[] to = toEmailAddresses.toArray(new String[toEmailAddresses.size()]);
				MimeMessage mailMessage = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true);
				helper.setFrom(messageTemplate.getFrom());
				helper.setTo(to);
				helper.setSubject(messageTemplate.getSubject());
				helper.setText(result, true);
				mailSender.send(mailMessage);
				jmsMessage.acknowledge();
			} catch (MailException ex) {
				log.error("Error sending email", ex);
			} catch (Exception ex) {
				log.error("Error deserializing message", ex);
			}
		} else {
			log.warn("Received message is not of type TextMessage");
		}
	}
	
	
	private String formatMessageBody(String hostName, String timestamp, String logFileLocation, String log) throws IOException, TemplateException {
	    Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
	    Template temp = new Template("mailbody", new StringReader(mailTemplate), cfg);
	    HashMap<String, String> map = new HashMap<>();
	    map.put("hostName", hostName);
	    map.put("serverId", hostName);
	    map.put("timestamp", timestamp);
	    map.put("logFileLocation", logFileLocation);
	    map.put("log", log);
	    StringWriter out = new StringWriter();
	    temp.process(map, out);
	    
	    return out.toString();
	}
	

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setMessageTemplate(SimpleMailMessage messageTemplate) {
		this.messageTemplate = messageTemplate;
	}

	public void setMailTemplateFile(String mailTemplateFile) {
        try {
            mailTemplate = FileUtils.readFileFromResourcePath(mailTemplateFile, this.getClass().getClassLoader());
		} catch (IOException e) {
			ErrorUtils.logExceptionFmt(log, e, "Exception: {0}");
		}
	}
}
