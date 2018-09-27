package com.ca.apm.systemtest.fld.server.logmonitor;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.LoggerMonitorUtils;
import com.ca.apm.systemtest.fld.common.logmonitor.LoggerMessage;
import com.ca.apm.systemtest.fld.server.dao.LoggerMonitorDao;
import com.ca.apm.systemtest.fld.server.model.LoggerMonitorValue;

public class LoggerMessageListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggerMessageListener.class);

    private Mapper mapper;
    private LoggerMonitorDao loggerMonitorDao;


    public LoggerMessageListener() {
        if (logger.isDebugEnabled()) {
            logger.debug("In LoggerMessageListener()");
        }
    }

    @Override
    public void onMessage(Message message) {

        LoggerMonitorValue loggerPersistMessage = null;
        
        if (message instanceof TextMessage) {
            final TextMessage txtMessage = (TextMessage) message;
            try {
                LoggerMessage loggerMessage = LoggerMonitorUtils
                    .convertJSONtoLog(txtMessage.getText());
                
                if (loggerMessage == null) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Ignoring a null FLD log message");
                    }
                    return;
                    
                }
                if (loggerMessage.getDashboardId() == null) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Ignoring an FLD log message which has no dashboard id set, original: {}", 
                            loggerMessage);
                    }
                    return;
                }
                
                loggerMessage.setExcept(LoggerMonitorUtils.preparePersistentLogMessage(loggerMessage.getExcept(), LoggerMonitorUtils.MAX_MESSAGE_LENGTH));
                loggerMessage.setMessage(LoggerMonitorUtils.preparePersistentLogMessage(loggerMessage.getMessage(), LoggerMonitorUtils.MAX_MESSAGE_LENGTH));
                loggerMessage.setCategory(LoggerMonitorUtils.preparePersistentLogMessage(loggerMessage.getCategory(), LoggerMonitorUtils.MAX_CATEGORY_LENGTH));
                loggerMessage.setTag(LoggerMonitorUtils.preparePersistentLogMessage(loggerMessage.getTag(), LoggerMonitorUtils.MAX_TAG_LENGTH));
                loggerMessage.setNode(LoggerMonitorUtils.preparePersistentLogMessage(loggerMessage.getNode(), LoggerMonitorUtils.MAX_NODE_NAME_LENGTH));
                loggerMessage.setProcessInstanceId(LoggerMonitorUtils.preparePersistentLogMessage(loggerMessage.getProcessInstanceId(), LoggerMonitorUtils.MAX_PROCESS_ID_LENGTH));

                loggerPersistMessage = new LoggerMonitorValue();
                synchronized (mapper) {
                    mapper.map(loggerMessage, loggerPersistMessage);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Log message converted from MQ");
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Original LoggerMessage: {}", loggerMessage);
                    logger.debug("Converted LoggerMonitorValue: {}", loggerPersistMessage);
                }
                loggerMonitorDao.create(loggerPersistMessage);

                if (logger.isDebugEnabled()) {
                    logger.debug("Log message persisted");
                }
            } catch (Exception e) {
                logger.warn("Logger message: {}", loggerPersistMessage);
                logger.warn("Unexpected exception receiving log message", e);
            }

        }

    }

    public Mapper getMapper() {
        return mapper;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public LoggerMonitorDao getLoggerMonitorDao() {
        return loggerMonitorDao;
    }

    public void setLoggerMonitorDao(LoggerMonitorDao loggerMonitorDao) {
        this.loggerMonitorDao = loggerMonitorDao;
    }
}
