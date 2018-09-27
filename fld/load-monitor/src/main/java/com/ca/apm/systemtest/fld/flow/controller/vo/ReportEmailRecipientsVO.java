package com.ca.apm.systemtest.fld.flow.controller.vo;

import java.util.List;

/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ReportEmailRecipientsVO extends BaseVO {

    private List<ReportEmailRecipientVO> emailRecipients;

    public ReportEmailRecipientsVO(List<ReportEmailRecipientVO> emailRecipients) {
        this.emailRecipients = emailRecipients;
    }

    /**
     * @return the emailRecipients
     */
    public List<ReportEmailRecipientVO> getEmailRecipients() {
        return emailRecipients;
    }

    /**
     * @param emailRecipients the emailRecipients to set
     */
    public void setEmailRecipients(List<ReportEmailRecipientVO> emailRecipients) {
        this.emailRecipients = emailRecipients;
    }
    
    
}
