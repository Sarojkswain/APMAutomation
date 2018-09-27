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

public class WebViewPage {
    private static final Logger log = LoggerFactory.getLogger(WebViewPage.class);

	private final WebDriver driver;

	private WebDriverWait wait;

	@FindBy(id = "x-auto-7-label")
	private WebElement applicationComponentsWidget;
	
	@FindBy(id="x-auto-4-label")
	private WebElement businessTransactionWidget;

	@FindBy(id="webview-HomePage-risks-portlet-header")
	private WebElement riskFromOtherTiersWidget;
	
	@FindBy(id="webview-HomePage-business-transactions-rt-header")
	private WebElement slowest25BTsResponseTimeWidget;
	
	@FindBy(id="webview-logout-link")
	private WebElement logoutLink;
	
	
	public WebViewPage(WebDriver driver) {
		super();
		this.driver = driver;
		PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, 30);
        log.info("WebViewPage URL: {}", driver.getCurrentUrl());
	}

	public WebViewPage waitToLoad() {
        log.info("Wait for logout link visible");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink));
		return this;
	}
	
	public LogoutPage logout(){
        log.info("Logout");
	    logoutLink.click();
	    return new LogoutPage(driver);
	}

	public WebViewPage verifySomeWidgetsVisible() {
        log.info("Validate widgets visible");
		assertTrue(applicationComponentsWidget.isDisplayed(), "Application components widget not displayed");
		assertTrue(businessTransactionWidget.isDisplayed(), "Business transactions widget not displayed");
		assertTrue(riskFromOtherTiersWidget.isDisplayed(), "Risk from other tiers widget not displayed");
		assertTrue(slowest25BTsResponseTimeWidget.isDisplayed(), "Slowest 25BTs widget not displayed");
		return this;
	}

    public WebViewPage verifyInvestigatorVisible() {
        log.info("Validate investigator visible");
        WebElement investigatorOverallCapacity = driver.findElement(By.id("webview-investigator-breadcrumb-node-Enterprise Manager"));
//        WebElement investigatorContainer = driver.findElement(By.id("webview-investigator-linechart-container"));
        assertTrue(investigatorOverallCapacity.isDisplayed(), "Investigator overall capacity widget not displayed");
//        assertTrue(investigatorContainer.isDisplayed());
        return this;
    }
    
	public WebDriver getDriver() {
		return driver;
	}

}
