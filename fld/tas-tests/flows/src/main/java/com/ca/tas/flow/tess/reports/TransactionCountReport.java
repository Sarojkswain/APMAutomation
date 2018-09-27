/**
 * 
 */
package com.ca.tas.flow.tess.reports;


/**
 * Configures a Transaction Count Report
 * @author keyja01
 *
 */
public class TransactionCountReport extends ReportWithTimeFrame {
    /**
     * @param reportName
     * @param subject
     */
    public TransactionCountReport(String reportName, String subject, TimeFrame timeFrame) {
        super(reportName, subject, timeFrame);
    }

    
    @Override
    public String reportType() {
        return "Transaction Count";
    }
}
