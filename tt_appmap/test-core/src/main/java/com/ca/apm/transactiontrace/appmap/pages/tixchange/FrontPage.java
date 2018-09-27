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

package com.ca.apm.transactiontrace.appmap.pages.tixchange;

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
 * Front page of TixChange
 */
public class FrontPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String pageUrl;

    @FindBy(xpath = "//*[@data-reactid='.0.0.0.4.0.0.0.0.0']")
    private WebElement userIdField;
    @FindBy(xpath = "//*[@data-reactid='.0.0.0.4.0.0.0.1.0']")
    private WebElement loginButton;
    @FindBy(xpath = "//*[@data-reactid='.0.2.0.0.1.0.$undefinedA.0']")
    private WebElement concertsLink;

    public FrontPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
        LOGGER.info(pageUrl);
    }

    public FrontPage typeUserId(String userId) {
        LOGGER.info("Enter user: " + userId);
        userIdField.sendKeys(userId);
        return this;
    }

    public FrontPage submitLogin() {
        LOGGER.info("Submit login form");
        loginButton.submit();
        FrontPage webViewPage = new FrontPage(driver);
        return webViewPage;
    }

    public ConcertsPage clickConcertsLink() {
        LOGGER.info("Click Concerts");
        concertsLink.click();
        ConcertsPage webViewPage = new ConcertsPage(driver);
        return webViewPage;
    }

    public void checkFrontPageContent() {
        LOGGER.info("Checking front page content");
        wait.until(ExpectedConditions.visibilityOf(concertsLink));

        assertTrue(userIdField.isDisplayed(), "UserId field not found");
        assertTrue(loginButton.isDisplayed(), "Login button not displayed");
        assertTrue(loginButton.isEnabled(), "Login button not enabled");
        assertTrue(concertsLink.isDisplayed(), "Concerts link not found");
    }

    public String getPageUrl() {
        return pageUrl;
    }

}
