/**
 * 
 */
package com.ca.apm.systemtest.fld.server.dao;

import java.util.List;

import org.hibernate.SessionFactory;

import com.ca.apm.systemtest.fld.server.model.Dashboard;

/**
 * @author KEYJA01
 *
 */
public interface DashboardDao extends GenericDao<Dashboard, Long> {
    /**
     * Finds a dashboard by id of a process instance.
     * 
     * @param processInstanceid
     * @return
     */
    public Dashboard findByProcessInstanceid(String processInstanceid);
    
    /**
     * Finds all dashboards referencing the same process with the specified <code>processKey</code>. 
     * 
     * @param processKey
     * @return
     */
    public List<Dashboard> findAllByProcessKey(String processKey);
    
    /**
     * Sets session factory.
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory);
}
