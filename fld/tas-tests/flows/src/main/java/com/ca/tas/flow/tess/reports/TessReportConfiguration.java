/**
 * 
 */
package com.ca.tas.flow.tess.reports;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.util.selenium.SeleniumAccess;
import com.ca.tas.flow.tess.reports.TessReportSchedule.Period;

/**
 * @author keyja01
 *
 */
public class TessReportConfiguration {
    private static final Logger log = LoggerFactory.getLogger(TessReportConfiguration.class);
    public TessReportSchedule schedule;
    public String name;
    
    public String editLinkHref;
    public WebElement clickBox;
    
    public String reportType() {
        return "unknown";
    }
    
    public TessReportConfiguration(String reportName, String subject) {
    	this(reportName, subject, TessReportSchedule.DEFAULT_CEM_REPORT_FROM_EMAIL_ADDRESS, 
    			TessReportSchedule.DEFAULT_CEM_REPORT_TO_EMAIL_ADDRESS);
    }
    
    public TessReportConfiguration(String reportName, String subject, String fromAddress, String toAddress) {
        name = reportName;
        schedule = TessReportSchedule.daily(1, 0);
        schedule.subject = subject;
        schedule.fromAddress = fromAddress != null ? fromAddress : TessReportSchedule.DEFAULT_CEM_REPORT_FROM_EMAIL_ADDRESS;
        schedule.toAddress = toAddress != null ? toAddress : TessReportSchedule.DEFAULT_CEM_REPORT_TO_EMAIL_ADDRESS;
    }
    
    private TessReportConfiguration() {
    }

    public static TessReportConfiguration newConfig() {
        return new TessReportConfiguration();
    }
    
    @Override
	public String toString() {
		return "TessReportConfiguration [schedule=" + schedule + ", name="
				+ name + "]";
	}

	/**
     * Fills out the form to create a new instance of the report represented by this object.  Expects to 
     * be on the "CEM -> My Reports -> new" page (reportDefNew.html) 
     * @param driver
     */
    public void configureOnCEM(SeleniumAccess tessUI, WebDriver driver) {
        tessUI.setInputTextById("name", name);
        log.info("Set report name to " + name);
        String type = reportType();
        WebElement element = driver.findElement(By.id("reportType"));
        log.info("Will attempt to select option with text \"" + type + "\" on input " + element);
        tessUI.selectOptionByVisibleText(element, type);
    }
    
    
    public void scheduleOnCEM(SeleniumAccess tessUI, WebDriver driver) {
        if (schedule != null) {
            scheduleReport(tessUI, driver);
        }
    }
    
    
    private void scheduleReport(SeleniumAccess tessUI, WebDriver driver) {
    	log.info("Scheduling report named '{}' of type '{}'", name, reportType());

    	WebElement scheduledCheckbox = driver.findElement(By.id("scheduled"));
        tessUI.clickWithDelay(scheduledCheckbox);
        
        WebElement frequencyElement = driver.findElement(By.id("frequency"));
        tessUI.selectOptionByValue(frequencyElement, schedule.period.getCode());
        
        if (schedule.period == Period.Weekly) {
        	log.info("Setting schedule to dayOfWeek: {}", schedule.dayOfWeek.getCode());
            tessUI.selectOptionByVisibleText(driver.findElement(By.name("dayOfWeek")), schedule.dayOfWeek.getCode());
        } else if (schedule.period == Period.Monthly) {
        	log.info("Setting schedule to dayOfMonth: {}", Integer.toString(schedule.dayOfMonth));
            tessUI.selectOptionByVisibleText(driver.findElement(By.name("dayOfMonth")), Integer.toString(schedule.dayOfMonth));
        }
        
        // set the time
        log.info("Setting schedule time - hour: {}", Integer.toString(schedule.hour));
        tessUI.selectOptionByVisibleText(driver.findElement(By.name("hour")), Integer.toString(schedule.hour));
        log.info("Setting schedule time - minute: {}", Integer.toString(schedule.minute));
        tessUI.selectOptionByValue(driver.findElement(By.name("minute")), Integer.toString(schedule.minute));
        
        // set the to and from address
        log.info("Setting report fromAddress: {}", schedule.fromAddress);
        tessUI.setInputTextById("fromAddress", schedule.fromAddress);
        log.info("Setting report toAddress: {}", schedule.toAddress);
        tessUI.setInputTextById("to", schedule.toAddress);
        log.info("Setting report subject: {}", schedule.subject);
        tessUI.setInputTextById("subject", schedule.subject); 
    }
}
