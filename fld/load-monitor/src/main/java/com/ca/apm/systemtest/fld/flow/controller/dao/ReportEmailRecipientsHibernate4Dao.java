package com.ca.apm.systemtest.fld.flow.controller.dao;

import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.flow.controller.model.ReportEmailRecipient;

/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Component
public class ReportEmailRecipientsHibernate4Dao extends AbstractHibernate4GenericDao<ReportEmailRecipient, Long> 
                                                implements ReportEmailRecipientsDao {

    public ReportEmailRecipientsHibernate4Dao() {
        super(ReportEmailRecipient.class);
    }

}
