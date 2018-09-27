package com.ca.apm.idp.internal.pages;

import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginPage {
    private static final Logger log = LoggerFactory.getLogger(LoginPage.class);

	private final WebDriver driver;
	
    private WebDriverWait wait;

	private String pageUrl;

    @FindBy(id="username")
	@CacheLookup
	private WebElement userNameField;
	
	@FindBy(id="j_passWord")
	@CacheLookup
	private WebElement passwordField;
	
	@FindBy(id="loginerror")
	private WebElement errorMessage;
	
	
	By loginButtonLocator = By.className("tsButton");

	public LoginPage(WebDriver driver) {
		super();
        log.info(driver.getCurrentUrl());

        this.driver = driver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
	}

	public LoginPage typeUserName(String userName) {
        log.info("Enter username: " + userName);
		userNameField.sendKeys(userName);
		return this;
	}

	public LoginPage typePassword(String password) {
        log.info("Enter password");
		passwordField.sendKeys(password);
		return this;
	}

	public WebViewPage submitLogin() {
        log.info("Submit login form");
		driver.findElement(loginButtonLocator).submit();
		WebViewPage webViewPage = new WebViewPage(driver);		
		return webViewPage;
	}

	   public AccPage submitLoginToAcc() {
	        log.info("Submit login form");
	        driver.findElement(loginButtonLocator).submit();
	        AccPage accPage = new AccPage(driver);      
	        return accPage;
	    }

    public WebstartPage submitLoginToWebstart() {
        log.info("Submit webstart login form");
        driver.findElement(loginButtonLocator).submit();
        WebstartPage webstartPage = new WebstartPage(driver);
        return webstartPage;
    }

    public ErrorPage submitLoginShouldRedirectToErrPage() {
        log.info("Submit login form (should go to error page)");
        driver.findElement(loginButtonLocator).submit();
        return new ErrorPage(driver);
    }

	public LoginPage submitLoginShouldFailed() {
        log.info("Submit login form (should fail)");
        driver.findElement(loginButtonLocator).submit();
        return new LoginPage(driver);
    }
	
	public void verifyLoginFailed(){
        log.info("Verify error message is displayed");
	    assertTrue(errorMessage.isDisplayed(), "Error message not found");
	}
	
    public void checkLoginPageContent() {
        wait.until(ExpectedConditions.visibilityOf(userNameField));
        log.info("Checking login page content");
        assertTrue(userNameField.isDisplayed(), "Username field not found");
        assertTrue(passwordField.isDisplayed(), "Password field not found");
        assertTrue(driver.findElement(loginButtonLocator).isDisplayed(), "Login button not displayed");
        assertTrue(driver.findElement(loginButtonLocator).isEnabled(), "Login button not enabled");

    }

	public String getPageUrl() {
        return pageUrl;
    }

}
