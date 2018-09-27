package com.ca.apm.systemtest.fld.server.dao;

import com.ca.apm.systemtest.fld.server.model.LogMonitorUser;

/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public interface LogMonitorUserDao extends GenericDao<LogMonitorUser, Long> {

    /**
     * Searches for a log monitor recipient by the provided email.
     * 
     * @param   email   recipient's email address
     * @return  log monitor recipient if found, otherwise <code>null</code>
     */
    public LogMonitorUser findByEmail(String email);
    
}
