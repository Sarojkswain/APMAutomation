/**
 * 
 */
package com.ca.apm.systemtest.fld.server.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.server.model.Dashboard;

/**
 * @author KEYJA01
 *
 */
@Component
public class DashboardHibernate4Dao extends AbstractHibernate4GenericDao<Dashboard, Long> implements DashboardDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardHibernate4Dao.class);
    
	/**
	 * 
	 */
	public DashboardHibernate4Dao() {
		super(Dashboard.class);
	}

    @Override
    public Dashboard findByProcessInstanceid(String processInstanceId) {
        Query query = getCurrentSession().createQuery("from Dashboard where processInstanceId = :processInstanceId");
        query.setString("processInstanceId", processInstanceId);
        Dashboard d = (Dashboard) query.uniqueResult();
        
        return d;
    }
    
    @Override
    public void setSessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Dashboard> findAllByProcessKey(String processKey) {
        Criteria criteria = getCurrentSession().createCriteria(Dashboard.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("processKey", processKey));
        List<Dashboard> dashboardsFound = criteria.list();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Found the following dashboards for process key '{}': {}", processKey, dashboardsFound);
        }
        return dashboardsFound;
    }
}
