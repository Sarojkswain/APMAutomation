/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.transactiontrace.appmap.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.testng.Assert.assertTrue;

/**
 * Main login page of webview
 *
 */
public class LoginPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginPage.class);

    private final WebDriver driver;
    By loginButtonLocator = By.className("tsButton");
    private WebDriverWait wait;
    private String pageUrl;
    @FindBy(name = "j_username")
    private WebElement userNameField;
    @FindBy(id = "j_passWord")
    private WebElement passwordField;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
        LOGGER.info(pageUrl);
    }

    public LoginPage typeUserName(String userName) {
        LOGGER.info("Enter username: " + userName);
        userNameField.sendKeys(userName);
        return this;
    }

    public LoginPage typePassword(String password) {
        LOGGER.info("Enter password");
        passwordField.sendKeys(password);
        return this;
    }

    public TeamCenterPage submitLogin() {
        LOGGER.info("Submit login form");
        driver.findElement(loginButtonLocator).submit();
        TeamCenterPage webViewPage = new TeamCenterPage(driver);
        return webViewPage;
    }

    public void checkLoginPageContent() {
        if (driver.findElements(By.id("LoginFrame")).size() > 0) {
            // we are on login page
            LOGGER.info("Switching to login frame");
            driver.switchTo().frame("LoginFrame");
        }
        LOGGER.info("Checking login page content");
        wait.until(ExpectedConditions.visibilityOf(userNameField));

        assertTrue(userNameField.isDisplayed(), "Username field not found");
        assertTrue(passwordField.isDisplayed(), "Password field not found");
        assertTrue(driver.findElement(loginButtonLocator).isDisplayed(), "Login button not displayed");
        assertTrue(driver.findElement(loginButtonLocator).isEnabled(), "Login button not enabled");

    }

    public String getPageUrl() {
        return pageUrl;
    }

}
