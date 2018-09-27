
package com.ca.apm.systemtest.fld.server.dao;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.server.model.MemoryMonitorValue;

/**
 * Memory monitor DAO.
 * 
 * @author jirji01
 *
 */
@Component
public class MemoryMonitorHibernate4Dao
    extends AbstractHibernate4GenericDao<MemoryMonitorValue, String>
    implements GenericDao<MemoryMonitorValue, String> {

    public MemoryMonitorHibernate4Dao() {
        super(MemoryMonitorValue.class);
    }

    @Override
    public int deleteAll() {
        Session session = getCurrentSession();
        return session.createQuery("delete from " + getCurrentClass().getName()).executeUpdate();
    }
}
