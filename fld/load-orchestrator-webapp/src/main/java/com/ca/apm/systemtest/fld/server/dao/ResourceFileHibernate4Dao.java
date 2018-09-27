package com.ca.apm.systemtest.fld.server.dao;


import org.hibernate.Query;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.server.model.ResourceFile;

/**
 * 
 * 
 * @author ZUNPA01
 *
 */
@Component
public class ResourceFileHibernate4Dao
        extends AbstractHibernate4GenericDao<ResourceFile, Long> implements ResourceFileDao {

    public ResourceFileHibernate4Dao() {
        super(ResourceFile.class);
    }

    @Override
    public ResourceFile findByName(String name) {
        Query query = getCurrentSession().createQuery("from ResourceFile where name = :resourceName");
        query.setString("resourceName", name);
        return ((ResourceFile) query.uniqueResult());
    }

    @Override
    public void deleteByName(String name) {
        Query query = getCurrentSession().createQuery("delete from ResourceFile where name = :resourceName");
        query.setString("resourceName", name);
        query.executeUpdate();
    }

}
