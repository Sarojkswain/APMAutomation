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
public class TransactionDefectReport extends ReportWithTimeFrame {

    /**
     * @param reportName
     * @param subject
     */
    public TransactionDefectReport(String reportName, String subject, TimeFrame timeFrame) {
        super(reportName, subject, timeFrame);
    }

    @Override
    public String reportType() {
        return "Transaction Defect";
    }
    
    @Override
    public void configureOnCEM(SeleniumAccess tessUI, WebDriver driver) {
        super.configureOnCEM(tessUI, driver);
        
        WebElement reportDimensionSelect = driver.findElement(By.id("reportDimension"));
        tessUI.selectOptionByValue(reportDimensionSelect, "timeSeries");
    }
}
