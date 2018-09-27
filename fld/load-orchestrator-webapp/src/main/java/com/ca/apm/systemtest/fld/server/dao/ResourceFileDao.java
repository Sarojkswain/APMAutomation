package com.ca.apm.systemtest.fld.server.dao;


import com.ca.apm.systemtest.fld.server.model.ResourceFile;

/**
 * 
 * 
 * @author ZUNPA01
 *
 */
public interface ResourceFileDao extends GenericDao<ResourceFile, Long> {
    
    /**
     * Returns a resource by its name.
     * 
     * @param name
     * @return
     */
    public ResourceFile findByName(String name);
    
    /**
     * Deletes a resource by its name.
     * 
     * @param name
     */
    public void deleteByName(String name);

}
