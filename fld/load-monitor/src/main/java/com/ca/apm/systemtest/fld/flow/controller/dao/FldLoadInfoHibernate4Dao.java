/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller.dao;

import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.flow.controller.model.FldLoadInfo;

/**
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Component
public class FldLoadInfoHibernate4Dao
    extends AbstractHibernate4GenericDao<FldLoadInfo, FldLoadInfo.FldLoadInfoId>
    implements FldLoadInfoDao {

    public FldLoadInfoHibernate4Dao() {
        super(FldLoadInfo.class);
    }
    
    

}
