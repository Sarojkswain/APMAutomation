/**
 * 
 */
package com.ca.apm.systemtest.fld.server.dao;

import org.hibernate.Query;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.server.model.FileCacheItem;

/**
 * @author KEYJA01
 *
 */
@Component
public class FileCacheDaoHibernate4Dao extends AbstractHibernate4GenericDao<FileCacheItem, Long> implements FileCacheDao {
    
    public FileCacheDaoHibernate4Dao() {
        super(FileCacheItem.class);
    }
    
    @Override
    public FileCacheItem findByUrl(String url) {
        Query query = getCurrentSession().createQuery("from FileCacheItem where url=:url");
        query.setParameter("url", url);
        return (FileCacheItem) query.uniqueResult();
    }
}
