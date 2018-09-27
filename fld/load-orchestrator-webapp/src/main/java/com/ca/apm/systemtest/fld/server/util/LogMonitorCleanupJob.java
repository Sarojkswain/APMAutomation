package com.ca.apm.systemtest.fld.server.util;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.server.dao.LoggerMonitorDao;


/**
 * Periodically deletes older log entries from LogMonitor
 * @author KEYJA01
 *
 */
@Component("logMonitorCleanupJob")
public class LogMonitorCleanupJob {
    private static final Logger log = LoggerFactory.getLogger(LogMonitorCleanupJob.class);
    private static final long PURGE_PERIOD = 30L * 24L * 60L * 60L * 1000L;
    @Autowired
    private LoggerMonitorDao dao;

    @Scheduled(initialDelay=60000L, fixedDelay=86400000L)
    public void purgeOldEntries() {
        Date d = new Date(System.currentTimeMillis() - PURGE_PERIOD);
        log.debug("Purging log entries older than " + d);
        int num = dao.purgeOldLogs(d);
        log.debug("Deleted " + num + " older log entries");
    }
}
