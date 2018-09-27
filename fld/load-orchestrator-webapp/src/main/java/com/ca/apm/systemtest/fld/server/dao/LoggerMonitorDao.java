package com.ca.apm.systemtest.fld.server.dao;

import java.util.Date;
import java.util.List;

import com.ca.apm.systemtest.fld.server.model.LoggerMonitorValue;
import com.ca.apm.systemtest.fld.shared.vo.RetrieveLogsRequestVO;

public interface LoggerMonitorDao extends GenericDao<LoggerMonitorValue, Long> {
    
    List<LoggerMonitorValue> findFilterLogs(RetrieveLogsRequestVO req);

    long countFilterLogs(RetrieveLogsRequestVO req);

    /**
     * Deletes all logs older than a given date
     * @param purgeBeforeDate
     * @return the number of deleted log entries
     */
    int purgeOldLogs(Date purgeBeforeDate);
}
