/**
 * 
 */
package com.ca.tas.flow.tess.reports;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ca.apm.systemtest.fld.util.selenium.SeleniumAccess;

/**
 * @author keyja01
 *
 */
public abstract class ReportWithTimeFrame extends TessReportConfiguration {
    private TimeFrame timeFrame;

    /**
     * 
     * @param reportName
     * @param subject
     * @param fromAddress
     * @param toAddress
     * @param timeFrame
     */
    public ReportWithTimeFrame(String reportName, String subject, String fromAddress, String toAddress, TimeFrame timeFrame) {
        super(reportName, subject, fromAddress, toAddress);
        this.timeFrame = timeFrame;
    }

    /**
     * @param reportName
     * @param subject
     */
    public ReportWithTimeFrame(String reportName, String subject, TimeFrame timeFrame) {
    	this(reportName, subject, TessReportSchedule.DEFAULT_CEM_REPORT_FROM_EMAIL_ADDRESS, 
    			TessReportSchedule.DEFAULT_CEM_REPORT_TO_EMAIL_ADDRESS, timeFrame);
    }

    @Override
    public void configureOnCEM(SeleniumAccess tessUI, WebDriver driver) {
        super.configureOnCEM(tessUI, driver);
        
        WebElement timeFrameSelect = driver.findElement(By.id("timeFrame"));
        tessUI.selectOptionByValue(timeFrameSelect, timeFrame.toString());
    }
}
