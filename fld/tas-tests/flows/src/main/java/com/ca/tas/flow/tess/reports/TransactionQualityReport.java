/**
 * 
 */
package com.ca.tas.flow.tess.reports;

/**
 * @author keyja01
 *
 */
public class TransactionQualityReport extends ReportWithTimeFrame {

    /**
     * @param reportName
     * @param subject
     * @param timeFrame
     */
    public TransactionQualityReport(String reportName, String subject, TimeFrame timeFrame) {
        super(reportName, subject, timeFrame);
    }
    
    @Override
    public String reportType() {
        return "Transaction Quality";
    }

}
