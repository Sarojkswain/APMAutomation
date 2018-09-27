/**
 * 
 */
package com.ca.apm.systemtest.fld.server.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.server.model.LogMonitorUser;

/**
 * DAO for operations on log monitor recipients.
 * 
 * @author TAVPA01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
@Component
public class LogMonitorUserHibernate4Dao extends AbstractHibernate4GenericDao<LogMonitorUser, Long>
    implements
        LogMonitorUserDao {

    /**
     * Constructor.
	 */
    public LogMonitorUserHibernate4Dao() {
        super(LogMonitorUser.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public LogMonitorUser findByEmail(String email) {
        Criteria criteria = getCurrentSession().createCriteria(LogMonitorUser.class).createAlias("emailEntries", "emails").add(Restrictions.eq("emails.emailAddress", email));
        List<LogMonitorUser> result = criteria.list();
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }


}
