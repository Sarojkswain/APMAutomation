/**
 * 
 */
package com.ca.tas.flow.tess.reports;

/**
 * @author keyja01
 *
 */
public class UserSlaPerformanceReport extends ReportWithTimeFrame {

    /**
     * @param reportName
     * @param subject
     * @param timeFrame
     */
    public UserSlaPerformanceReport(String reportName, String subject, TimeFrame timeFrame) {
        super(reportName, subject, timeFrame);
    }
    
    @Override
    public String reportType() {
        return "User SLA Performance";
    }

}
