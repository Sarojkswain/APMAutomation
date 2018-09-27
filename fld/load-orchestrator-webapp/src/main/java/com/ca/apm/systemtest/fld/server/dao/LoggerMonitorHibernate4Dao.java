package com.ca.apm.systemtest.fld.server.dao;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ca.apm.systemtest.fld.common.LoggerMonitorUtils;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;
import com.ca.apm.systemtest.fld.server.model.LoggerMonitorValue;
import com.ca.apm.systemtest.fld.shared.vo.RetrieveLogsRequestVO;


@Transactional(propagation = Propagation.REQUIRES_NEW)
public class LoggerMonitorHibernate4Dao
    extends AbstractHibernate4GenericDao<LoggerMonitorValue, Long> implements LoggerMonitorDao {

    private static final Logger logger = LoggerFactory.getLogger(LoggerMonitorHibernate4Dao.class);
    private static final int MAX_LOGS_DEAFULT = 30;

    public LoggerMonitorHibernate4Dao() {
        super(LoggerMonitorValue.class);

    }

    @Override
    public void create(LoggerMonitorValue logValue) {
        if (logValue == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("DAO: ignoring a null FLD log message");
            }
            return;
            
        }
        if (logValue.getDashboardId() == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("DAO: ignoring an FLD log message which has no dashboard id set, original: {}", 
                    logValue);
            }
            return;
        }
        
        logValue.setMessage(LoggerMonitorUtils.preparePersistentLogMessage(logValue.getMessage(), LoggerMonitorUtils.MAX_MESSAGE_LENGTH));
        logValue.setException(LoggerMonitorUtils.preparePersistentLogMessage(logValue.getException(), LoggerMonitorUtils.MAX_MESSAGE_LENGTH));
        logValue.setCategory(LoggerMonitorUtils.preparePersistentLogMessage(logValue.getCategory(), LoggerMonitorUtils.MAX_CATEGORY_LENGTH));
        logValue.setTag(LoggerMonitorUtils.preparePersistentLogMessage(logValue.getTag(), LoggerMonitorUtils.MAX_TAG_LENGTH));
        logValue.setNode(LoggerMonitorUtils.preparePersistentLogMessage(logValue.getNode(), LoggerMonitorUtils.MAX_NODE_NAME_LENGTH));
        logValue.setProcessInstanceId(LoggerMonitorUtils.preparePersistentLogMessage(logValue.getProcessInstanceId(), LoggerMonitorUtils.MAX_PROCESS_ID_LENGTH));
        
        super.create(logValue);
    }

    @Override
    public long countFilterLogs(RetrieveLogsRequestVO req) {
        Session session = getCurrentSession();
        Criteria criteria = basicCriteriaFilter(req, session.createCriteria(LoggerMonitorValue.class));
        Number count = (Number) criteria.setProjection(Projections.rowCount()).uniqueResult();
        return count != null ? count.longValue() : 0; 
    }

    /**
     * Find and filter logs from DB
     * 
     * @param req
     */
    @SuppressWarnings("unchecked")
    public List<LoggerMonitorValue> findFilterLogs(RetrieveLogsRequestVO req) {
        Session session = getCurrentSession();
        Integer reqMaxLogs = req.getMaxLogs();
        int maxLogs = reqMaxLogs != null && reqMaxLogs > 0? reqMaxLogs : MAX_LOGS_DEAFULT;
        Criteria criteria = basicCriteriaFilter(req, session.createCriteria(LoggerMonitorValue.class));
        
        if (req.getLogsAfterId() != null) {
            criteria.add(Restrictions.gt("id", new Long(req.getLogsAfterId())));
        } else if (req.getLogsBeforeId() != null) {
            long lastId = req.getLogsBeforeId();

            if (lastId == -1) {
                Criteria maxIdCriteria = session.createCriteria(LoggerMonitorValue.class).setProjection(Projections.max("id"));
                if (req.getDashboardId() != null) {
                    maxIdCriteria.add(Restrictions.eq("dashboardId", req.getDashboardId()));
                }
                if (req.getProcessInstanceId() != null) {
                    maxIdCriteria.add(Restrictions.eq("processInstanceId", req.getProcessInstanceId()));
                }
                
                if (logger.isDebugEnabled()) {
                    logger.debug("MAX Id Criteria: {}", maxIdCriteria);
                }
                
                Long maxIdVal = (Long) maxIdCriteria.uniqueResult();
                if (maxIdVal != null) {
                    lastId = maxIdVal.longValue();
                    if (logger.isDebugEnabled()) {
                        logger.debug("MAX Id: {}", lastId);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cannot find last log record index");
                    }
                    return Collections.EMPTY_LIST;
                }
            } 

            criteria.add(Restrictions.lt("id", lastId)).addOrder(Order.desc("id"));

        }

        criteria.setMaxResults(maxLogs);
        if (req.getOffset() != null) {
            criteria.setFirstResult(req.getOffset());    
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Criteria filter call - {}", criteria);
        }

        return criteria.list();
    }

    /**
     * Criteria fetch for level, category, tag, max logs, node name, dashboard id
     * 
     * @param req
     */
    public Criteria basicCriteriaFilter(RetrieveLogsRequestVO req, Criteria criteria) {
        if (req.getLevel() != null) {
            FldLevel lvl = FldLevel.valueOf(req.getLevel().toUpperCase());
            List<Criterion> levels = new ArrayList<Criterion>();
            switch (lvl) {
                case TRACE:
                    // include ALL logs
                    levels.add(Restrictions.eqOrIsNull("level", FldLevel.TRACE));
                case DEBUG:
                    levels.add(Restrictions.eqOrIsNull("level", FldLevel.DEBUG));
                case INFO:
                    levels.add(Restrictions.eqOrIsNull("level", FldLevel.INFO));
                case WARN:
                    levels.add(Restrictions.eqOrIsNull("level", FldLevel.WARN));
                case ERROR:
                    levels.add(Restrictions.eqOrIsNull("level", FldLevel.ERROR));
                default:
                    break;
            }
            criteria.add(Restrictions.or(levels.toArray(new Criterion[levels.size()])));
        }
        if (req.getCategoryFilter() != null) {
            criteria
                .add(Restrictions.like("category", req.getCategoryFilter(), MatchMode.ANYWHERE));
        }
        if (req.getTagFilter() != null) {
            criteria.add(Restrictions.like("tag", req.getTagFilter(), MatchMode.ANYWHERE));
        }
        if (req.getNodeName() != null) {
            criteria.add(Restrictions.like("node", req.getNodeName(), MatchMode.ANYWHERE));
        }
        if (req.getDashboardId() != null) {
            criteria.add(Restrictions.eq("dashboardId", req.getDashboardId()));
        }
        if (req.getProcessInstanceId() != null) {
            criteria.add(Restrictions.eq("processInstanceId", req.getProcessInstanceId()));
        }
        return criteria;
    }

    @Override
    public int purgeOldLogs(Date purgeBeforeDate) {
        Session session = getCurrentSession();
        int num = session.createQuery("delete LoggerMonitorValue lmv where lmv.timestamp < :purgeBeforeDate")
            .setDate("purgeBeforeDate", purgeBeforeDate)
            .executeUpdate();
        
        return num;
    }

}
