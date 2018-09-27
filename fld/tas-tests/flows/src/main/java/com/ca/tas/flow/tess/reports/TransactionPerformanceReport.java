/**
 * 
 */
package com.ca.tas.flow.tess.reports;


/**
 * @author keyja01
 *
 */
public class TransactionPerformanceReport extends ReportWithTimeFrame {

    /**
     * @param reportName
     * @param subject
     */
    public TransactionPerformanceReport(String reportName, String subject, TimeFrame timeFrame) {
        super(reportName, subject, timeFrame);
    }

    
    @Override
    public String reportType() {
        return "Transaction Performance";
    }
}
