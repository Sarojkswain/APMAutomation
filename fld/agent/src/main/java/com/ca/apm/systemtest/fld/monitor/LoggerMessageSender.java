package com.ca.apm.systemtest.fld.monitor;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.ca.apm.systemtest.fld.common.LoggerMonitorUtils;
import com.ca.apm.systemtest.fld.common.logmonitor.LoggerMessage;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.plugin.vo.ProcessInstanceIdStore;
import com.fasterxml.jackson.core.JsonProcessingException;


public class LoggerMessageSender {

    private static final Logger logger = LoggerFactory.getLogger(LoggerMessageSender.class);
	
	private JmsTemplate jmsTemplate;
	
	private Queue queue;
	
	private String nodeName;
	
	
	/** Send JSON message to MQ 
	 * 
	 * @param loggerMessage
	 */
	public void sendLogMessage(final LoggerMessage loggerMessage) {
		
		this.jmsTemplate.send(queue, new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = null;
                try {
                    loggerMessage.setNode(nodeName);
                    loggerMessage.setDashboardId(DashboardIdStore.getDashboardId());
                    loggerMessage.setProcessInstanceId(ProcessInstanceIdStore.getProcessInstanceId());
					String jsonMessage = LoggerMonitorUtils.convertLogtoJSON(loggerMessage, true);
					message = session.createTextMessage(jsonMessage);
					logger.trace("sendLogMessage() done");					
				} catch (JsonProcessingException e) {
				    throw new JMSException("Unable to convert message to JSON");
				}
				return message;

			}
		});
	
	}


    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }


    public void setQueue(Queue queue) {
        this.queue = queue;
    }
    
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}
