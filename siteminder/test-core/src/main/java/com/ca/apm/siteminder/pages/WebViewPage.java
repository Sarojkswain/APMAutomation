package com.ca.apm.siteminder.pages;

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
        wait = new WebDriverWait(driver, 60);
        log.info("WebViewPage URL: " + driver.getCurrentUrl());
	}

	public WebViewPage waitToLoad() {
        wait.until(ExpectedConditions.visibilityOf(logoutLink));
        log.info("Logout link visible in home page");
		return this;
	}
	
	public LogoutPage logout(){
	    logoutLink.click();
	    return new LogoutPage(driver);
	}

	public WebViewPage verifySomeWidgetsVisible() {
        wait.until(ExpectedConditions.visibilityOf(applicationComponentsWidget));
		assertTrue(applicationComponentsWidget.isDisplayed());
		assertTrue(businessTransactionWidget.isDisplayed());
		assertTrue(riskFromOtherTiersWidget.isDisplayed());
		assertTrue(slowest25BTsResponseTimeWidget.isDisplayed());
        log.info("Found elements on home page");
		return this;
	}

    public WebViewPage verifyInvestigatorVisible() {
        WebElement investigatorOverallCapacity = driver.findElement(By.id("webview-investigator-breadcrumb-node-Enterprise Manager"));
//        WebElement investigatorContainer = driver.findElement(By.id("webview-investigator-linechart-container"));
        assertTrue(investigatorOverallCapacity.isDisplayed());
//        assertTrue(investigatorContainer.isDisplayed());
        return this;
    }
    
	public WebDriver getDriver() {
		return driver;
	}


    public void tryDirectURL(String webViewUrl) {
        driver.get(webViewUrl);
        PageFactory.initElements(driver, this);
        log.info("Accessing webview at " + webViewUrl);
    }
}
