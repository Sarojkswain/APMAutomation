/**
 * 
 */
package com.ca.tas.flow.tess.reports;

/**
 * @author keyja01
 *
 */
public class UserSlaQualityReport extends ReportWithTimeFrame {

    /**
     * @param reportName
     * @param subject
     * @param timeFrame
     */
    public UserSlaQualityReport(String reportName, String subject, TimeFrame timeFrame) {
        super(reportName, subject, timeFrame);
    }

    @Override
    public String reportType() {
        return "User SLA Quality";
    }
}
