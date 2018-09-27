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
public class StatsDataCSVReport extends TessReportConfiguration {

    private static final String DEFAULT_AGG_TYPE = "aggregationBS";

    /**
     * @param reportName
     * @param subject
     */
    public StatsDataCSVReport(String reportName, String subject) {
        super(reportName, subject);
    }

    @Override
    public String reportType() {
        return "Statistics Data Report (CSV)";
    }
    
    @Override
    public void configureOnCEM(SeleniumAccess tessUI, WebDriver driver) {
        super.configureOnCEM(tessUI, driver);
        
        WebElement aggTypeSelect = driver.findElement(By.id("sAggregationType"));
        tessUI.selectOptionByValue(aggTypeSelect, DEFAULT_AGG_TYPE);
        
        WebElement toDateLink = driver.findElement(By.id("toDate_date_icon"));
        tessUI.clickWithDelay(toDateLink);
        WebElement rightArrow = driver.findElement(By.id("toDate_nextMonth"));
        tessUI.clickWithDelay(rightArrow);
        
        WebElement okButton = driver.findElement(By.id("toDate_OKButtonId"));
        tessUI.clickWithDelay(okButton);
    }
}
