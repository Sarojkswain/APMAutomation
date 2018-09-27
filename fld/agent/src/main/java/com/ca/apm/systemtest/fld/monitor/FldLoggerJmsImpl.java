package com.ca.apm.systemtest.fld.monitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.common.logmonitor.LoggerMessage;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.plugin.vo.ProcessInstanceIdStore;

/**
 * Implementation of FldLogger that sends logs to a centralized server via JMS.
 */
@Component("fldLogger")
public class FldLoggerJmsImpl implements FldLogger {
	private static final Logger logger = LoggerFactory.getLogger(FldLoggerJmsImpl.class);

	@Autowired
	private LoggerMessageSender sender;

	public FldLoggerJmsImpl() {
		if (logger.isInfoEnabled()) {
		    logger.info("Instantiating {}", FldLoggerJmsImpl.class.getName());    
		}
	}
	
	@Override
	public void log(FldLevel level, String category, String tag, String message, Throwable throwable) {
	    if (DashboardIdStore.getDashboardId() == null) {
	        if (logger.isDebugEnabled()) {
	            logger.debug("JMS fldLogger: skipping fld log message as dashboardId is null. level={}, category={}, tag={}, message={}, throwable={}", 
	                level, category, tag, message, throwable);
	        }
	        return;
	    }
	    String exception = null;
	    if (throwable != null) {
	        StringWriter stackTraceWriter = new StringWriter();
	        throwable.printStackTrace(new PrintWriter(stackTraceWriter, true));
	        exception = stackTraceWriter.toString();
	    }
	    Date timestamp = new Date();
	    LoggerMessage msg = new LoggerMessage(level, ProcessInstanceIdStore.getProcessInstanceId(), 
	        category, tag, message, exception, timestamp);
        try {
            sender.sendLogMessage(msg);
        } catch (Exception e) {
            logger.warn("Unable to send log message", e);
        }
	}

    @Override
    public void trace(String category, String tag, String message) {
        log(FldLevel.TRACE, category, tag, message, null);
    }

    @Override
    public void trace(String category, String tag, String message, Throwable cause) {
        log(FldLevel.TRACE, category, tag, message, cause);
    }

    @Override
    public void debug(String category, String tag, String message) {
        log(FldLevel.DEBUG, category, tag, message, null);
    }

    @Override
    public void debug(String category, String tag, String message, Throwable cause) {
        log(FldLevel.DEBUG, category, tag, message, cause);
    }

    @Override
    public void info(String category, String tag, String message) {
        log(FldLevel.INFO, category, tag, message, null);
    }

    @Override
    public void info(String category, String tag, String message, Throwable cause) {
        log(FldLevel.INFO, category, tag, message, cause);
    }

    @Override
    public void warn(String category, String tag, String message) {
        log(FldLevel.WARN, category, tag, message, null);
    }

    @Override
    public void warn(String category, String tag, String message, Throwable cause) {
        log(FldLevel.WARN, category, tag, message, cause);
    }

    @Override
    public void error(String category, String tag, String message) {
        log(FldLevel.ERROR, category, tag, message, null);
    }

    @Override
    public void error(String category, String tag, String message, Throwable cause) {
        log(FldLevel.ERROR, category, tag, message, cause);
    }
    
}
