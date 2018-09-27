/**
 * 
 */
package com.ca.tas.flow.tess.reports;


/**
 * Configures the Correlational SLA Report with default options set
 * @author keyja01
 *
 */
public class CorrelationalSlaReport extends ReportWithTimeFrame {

    /**
     * @param reportName
     * @param subject
     */
    public CorrelationalSlaReport(String reportName, String subject, TimeFrame timeFrame) {
        super(reportName, subject, timeFrame);
    }

    @Override
    public String reportType() {
        return "Correlational SLA Report";
    }
}
