/**
 * 
 */
package com.ca.apm.systemtest.fld.server.dao;

import com.ca.apm.systemtest.fld.server.model.FileCacheItem;

/**
 * @author KEYJA01
 *
 */
public interface FileCacheDao extends GenericDao<FileCacheItem, Long> {
    public FileCacheItem findByUrl(String url);
}
