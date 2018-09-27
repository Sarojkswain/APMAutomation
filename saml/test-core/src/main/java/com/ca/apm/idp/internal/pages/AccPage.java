/*
 * Copyright (c) 2016 CA.  All rights reserved.
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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccPage {
    private static final Logger log = LoggerFactory.getLogger(AccPage.class);

    private final WebDriver driver;

    private WebDriverWait wait;

    @FindBy(id = "accViewport")
    private WebElement accViewport;
    
    @FindBy(id="accLinkHome")
    private WebElement accLinkHome;

    @FindBy(id="accLinkAgents")
    private WebElement accLinkAgents;
    
    
    public AccPage(WebDriver driver) {
        super();
        this.driver = driver;
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, 30);
        log.info("AccPage URL: " + driver.getCurrentUrl());
    }

    public AccPage waitToLoad() {
        log.info("Wait for accViewport visible");
        wait.until(ExpectedConditions.elementToBeClickable(accLinkHome));
        return this;
    }
    
    public AccPage verifySomeWidgetsVisible() {
        log.info("Validate widgets visible");
        assertTrue(accLinkHome.isDisplayed(), "ACC home link not displayed");
        assertTrue(accLinkAgents.isDisplayed(), "ACC agents link not displayed");
        return this;
    }

    public WebDriver getDriver() {
        return driver;
    }

}
