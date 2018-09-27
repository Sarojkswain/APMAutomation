/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

package com.ca.apm.siteminder.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AdminUILoginPage {
    private static final Logger log = LoggerFactory.getLogger(AdminUILoginPage.class);

    private WebDriverWait wait;

    private String pageUrl;

    @FindBy(id = "username")
    @CacheLookup
    private WebElement userNameField;

    @FindBy(id = "password")
    @CacheLookup
    private WebElement passwordField;

    @FindBy(id = "signinButton")
    private WebElement signInButton;

    public AdminUILoginPage(WebDriver driver) {
        super();
        log.info(driver.getCurrentUrl());

        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
    }

    public AdminUILoginPage typeUserName(String userName) {
        userNameField.sendKeys(userName);
        log.info("Typing username: " + userNameField.getText());
        return this;
    }

    public AdminUILoginPage typePassword(String password) {
        passwordField.sendKeys(password);
        log.info("Typing password: " + passwordField.getText());
        return this;
    }


    public SMHomePage clickLoginButton(WebDriver driver) {
        signInButton.submit();
        log.info("Performed submit");
        return new SMHomePage(driver);
    }

    /**
     * Works on IE when you get certificate error
     *
     * @param driver
     */
    public void overrideCertificateError(WebDriver driver) {
        driver.get("javascript:document.getElementById('overridelink').click();");
    }


    public void checkLoginPageContent() {

        wait.until(ExpectedConditions.visibilityOf(userNameField));
    }

    public String getPageUrl() {
        return pageUrl;
    }



}
