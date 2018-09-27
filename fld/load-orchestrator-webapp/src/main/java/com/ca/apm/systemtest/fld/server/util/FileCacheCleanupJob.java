/**
 * 
 */
package com.ca.apm.systemtest.fld.server.util;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ca.apm.systemtest.fld.filecache.FileCache;
import com.ca.apm.systemtest.fld.server.dao.AgentDistributionDao;
import com.ca.apm.systemtest.fld.server.model.AgentDistribution;

/**
 * @author keyja01
 *
 */
@Component("fileCacheCleanupJob")
public class FileCacheCleanupJob implements InitializingBean, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(FileCacheCleanupJob.class);

    private static final long ONE_MONTH = 30*24*60*60*1000;
    
    @Autowired
    private FileCache fileCache;
    
    @Autowired
    private AgentDistributionDao agentDistroDao;

    private ProcessEngine processEngine;
    
    private HistoryService historyService;

    private ApplicationContext applicationContext;
    
    public FileCacheCleanupJob() {
        log.trace("In FileCacheCleanupJob()::");
    }
    
    @Scheduled(initialDelay=60000L, fixedDelay=86400000L)
    public void runCleanup() {
        try {
            log.info("About to delete old file cache entries");
            fileCache.pruneCache();
            log.info("Deleted old file cache entries");
        } catch (Exception ex) {
            log.warn("Unable to prune file cache", ex);
        }
    }
    
    
    @Scheduled(initialDelay=30000L, fixedDelay=3600000L)
    @Transactional(propagation=Propagation.REQUIRED)
    public void runCleanupAgentDistro() {
        try {
            AgentDistribution ad = agentDistroDao.findNewest();
            if (ad != null) {
                int num = agentDistroDao.deleteOlder(ad.getTimestamp());
                log.info("Deleted " + num + " older agent distributions");
            }
        } catch (Exception ex) {
            log.warn("Unable to clean up agent distributions", ex);
        }
    }
    
    
    @Scheduled(initialDelay=45000L, fixedDelay=86400000L)
    public void runCleanupActivitiHistory() {
        processEngine = applicationContext.getBean(ProcessEngine.class);
        historyService = processEngine.getHistoryService();
        int deletedCount = 0;
        try {
            Date d = new Date(System.currentTimeMillis() - ONE_MONTH);
            boolean done = false;
            while (!done) {
                List<HistoricProcessInstance> page = historyService.createHistoricProcessInstanceQuery().finishedBefore(d).listPage(0, 50);
                if (page == null || page.size() == 0) {
                    break;
                }
                for (HistoricProcessInstance hpi: page) {
                    try {
                        historyService.deleteHistoricProcessInstance(hpi.getId());
                        deletedCount++;
                    } catch (ActivitiObjectNotFoundException aonfe) {
                        log.debug("Caught ActivitiObjectNotFoundException - was this a subprocess already deleted?");
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Exception while purging old process instances from activiti", ex);
        }
        log.info("Deleted " + deletedCount + " historic process instances");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        
    }
    
}
