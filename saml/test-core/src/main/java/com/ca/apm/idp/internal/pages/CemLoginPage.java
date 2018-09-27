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
package com.ca.apm.idp.internal.pages;

import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CemLoginPage {
    private static final Logger log = LoggerFactory.getLogger(LoginPage.class);

    private final WebDriver driver;
    
    private String pageUrl;

    @FindBy(id="loginForm:loginId_userName")
    @CacheLookup
    private WebElement userNameField;
    
    @FindBy(id="loginForm:loginId_passWord")
    @CacheLookup
    private WebElement passwordField;
    
    By loginButtonLocator = By.className("tsButton");

    public CemLoginPage(WebDriver driver) {
        super();

        log.info("URL: " + driver.getCurrentUrl());

        this.driver = driver;
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
    }

    public CemLoginPage typeUserName(String userName) {
        log.info("Enter username: " + userName);
        userNameField.sendKeys(userName);
        return this;
    }

    public CemLoginPage typePassword(String password) {
        log.info("Enter password");
        passwordField.sendKeys(password);
        return this;
    }

    public CemPage submitLogin() {
        log.info("Submit login form");
        driver.findElement(loginButtonLocator).submit();
        CemPage cemPage = new CemPage(driver);      
        return cemPage;
    }
    
    public void checkLoginPageContent() {
        log.info("Validate page content");
        assertTrue(userNameField.isDisplayed(), "Username field not found");
        assertTrue(passwordField.isDisplayed(), "Password field not found");
        assertTrue(driver.findElement(loginButtonLocator).isDisplayed(), "Login button not displayed");
        assertTrue(driver.findElement(loginButtonLocator).isEnabled(), "Login button not enabled");
    }

    public String getPageUrl() {
        return pageUrl;
    }

}
