/**
 * 
 */
package com.ca.apm.systemtest.fld.server.logmonitor;

import java.io.PrintWriter;
import java.util.Date;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.plugin.vo.ProcessInstanceIdStore;
import com.ca.apm.systemtest.fld.server.dao.LoggerMonitorDao;
import com.ca.apm.systemtest.fld.server.model.LoggerMonitorValue;

/**
 * Local implementation of the {@link FldLogger} which simply directly persists to the database
 * @author KEYJA01
 *
 */
public class FldLoggerLocalImpl implements FldLogger {
    private static final Logger logger = LoggerFactory.getLogger(FldLoggerLocalImpl.class);
    
    @Autowired
    private LoggerMonitorDao loggerMonitorDao;
    
    public FldLoggerLocalImpl() {
        System.out.println("In fldLoggerLocalImpl()::");
    }
    
    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#log(com.ca.apm.systemtest.fld.common.logmonitor.FldLevel, java.lang.String, java.lang.String, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void log(FldLevel level, String category, String tag, String message, Throwable cause) {
        Long dashboardId = DashboardIdStore.getDashboardId();
        if (dashboardId != null) {
            String exception = null;
            if (cause != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                cause.printStackTrace(new PrintWriter(out));
                exception = cause.toString();
            }
            LoggerMonitorValue lmv = new LoggerMonitorValue();
            lmv.setDashboardId(dashboardId);
            lmv.setLevel(level);
            lmv.setCategory(category);
            lmv.setTag(tag);
            lmv.setTimestamp(new Date());
            lmv.setMessage(message);
            lmv.setException(exception);
            lmv.setProcessInstanceId(ProcessInstanceIdStore.getProcessInstanceId());
            loggerMonitorDao.create(lmv);
        } else if (logger.isDebugEnabled()) {
            logger.debug("FldLoggerLocalImpl: ignoring received message since dashboard id is unknown: level={}, category={}, tag={}, message={}, cause={}", 
                level, category, tag, message, cause);
        }
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#trace(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void trace(String category, String tag, String message) {
        log(FldLevel.TRACE, category, tag, message, null);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#trace(java.lang.String, java.lang.String, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void trace(String category, String tag, String message, Throwable cause) {
        log(FldLevel.TRACE, category, tag, message, cause);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#debug(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void debug(String category, String tag, String message) {
        log(FldLevel.DEBUG, category, tag, message, null);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#debug(java.lang.String, java.lang.String, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void debug(String category, String tag, String message, Throwable cause) {
        log(FldLevel.DEBUG, category, tag, message, cause);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#info(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void info(String category, String tag, String message) {
        log(FldLevel.INFO, category, tag, message, null);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#info(java.lang.String, java.lang.String, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void info(String category, String tag, String message, Throwable cause) {
        log(FldLevel.INFO, category, tag, message, cause);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#warn(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void warn(String category, String tag, String message) {
        log(FldLevel.WARN, category, tag, message, null);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#warn(java.lang.String, java.lang.String, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void warn(String category, String tag, String message, Throwable cause) {
        log(FldLevel.WARN, category, tag, message, cause);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#error(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void error(String category, String tag, String message) {
        log(FldLevel.ERROR, category, tag, message, null);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.common.logmonitor.FldLogger#error(java.lang.String, java.lang.String, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void error(String category, String tag, String message, Throwable cause) {
        log(FldLevel.ERROR, category, tag, message, cause);
    }

}
