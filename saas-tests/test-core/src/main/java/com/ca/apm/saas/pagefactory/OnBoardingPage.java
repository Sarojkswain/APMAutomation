/*
 * Author: Abhishek Sinha(sinab10@ca.com)
 * 
 * Copyright (c) 2017 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.saas.pagefactory;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;


public class OnBoardingPage {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
	protected long delay = 15000;    
    WebDriver driver;
    
    // 'Application Monitoring' Open Button on Onboarding page
    @FindBy(id="APM_openbtn")
    WebElement apmOpenBtn;
    
    // 'End User Monitoring' Open Button on Onboarding page
    @FindBy(id="AXA_openbtn")
    WebElement axaOpenBtn;
    
    // 'Infrastructure Monitoring' Open Button on Onboarding page
    /*@FindBy(id="")
    WebElement IMOpenBtn;*/
    
    public OnBoardingPage(WebDriver driver){
        this.driver = driver;
        // This initElements method will create all WebElements
        PageFactory.initElements(driver, this);  
    }
    
    public void clickApmButton() throws InterruptedException {
        //new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(apmOpenBtn)).click();
        Thread.sleep(delay/3);
        apmOpenBtn.click();
        Thread.sleep(delay/3);
    }
    
    public void clickEumButton() throws InterruptedException {
        try {
            new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(axaOpenBtn));
            Assert.assertTrue(isElementPresent(driver,axaOpenBtn), "AXA button in Onboarding page didn't load properly");
            axaOpenBtn.click();
        }catch(Exception e) {
            logger.info("AXA button NOT shows up - error throw {}");
            e.printStackTrace();      
        }
    }
    
    /*public void clickImButton() throws InterruptedException {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(IMOpenBtn)).click();
        Thread.sleep(delay);
    }*/
    
    public WebElement getWebElementApmBtn() {
        return apmOpenBtn;
    }
    
    public WebElement getWebElementEumBtn() {
        return axaOpenBtn;
    }
    
    /*public WebElement getWebElementImBtn() {
        return IMOpenBtn;
    }*/
    
    public boolean isElementPresent(WebDriver webdriver, WebElement webelement) {    
        boolean exists = false;

        webdriver.manage().timeouts().implicitlyWait(10, TimeUnit.MILLISECONDS);

        try {
            webelement.getTagName();
            exists = true;
        } catch (NoSuchElementException e) {
            // nothing to do.
        }

        webdriver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);

        return exists;
    }
}
