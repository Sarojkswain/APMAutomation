/**
 * 
 */
package com.ca.tas.flow.tess.reports;


/**
 * @author keyja01
 *
 */
public class ImpactLeadersReport extends ReportWithTimeFrame {
    public ImpactLeadersReport(String reportName, String subject, TimeFrame timeFrame) {
        super(reportName, subject, timeFrame);
    }

    @Override
    public String reportType() {
        return "Impact Leaders";
    }
}
