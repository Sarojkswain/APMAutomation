/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.transactiontrace.appmap.pages.qatestapp;

import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auto Tracing page of QATestApp
 */
public class AutoTracingPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoTracingPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String pageUrl;

    @FindBy(xpath = "//input[@name='param']")
    private WebElement testNumberField;
    @FindBy(xpath = "//input[@type='submit']")
    private WebElement submitButton;

    public AutoTracingPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
        LOGGER.info(pageUrl);
    }

    public AutoTracingPage typeTestNumber(String testNumber) {
        LOGGER.info("Enter test number: " + testNumber);
        testNumberField.clear();
        testNumberField.sendKeys(testNumber);
        return this;
    }

    public AutoTracingPage submitTest() {
        LOGGER.info("Submit test form");
        submitButton.submit();
        AutoTracingPage webViewPage = new AutoTracingPage(driver);
        return webViewPage;
    }

    public void checkAutoTracingPageContent() {
        LOGGER.info("Checking Auto Tracing page content");
        wait.until(ExpectedConditions.visibilityOf(submitButton));

        assertTrue(testNumberField.isDisplayed(), "Test Number field not found");
        assertTrue(submitButton.isDisplayed(), "Submit button not displayed");
        assertTrue(submitButton.isEnabled(), "Submit button not enabled");
    }

    public String getPageUrl() {
        return pageUrl;
    }

}
