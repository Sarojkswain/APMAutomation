package com.ca.apm.siteminder.pages;

import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sundeep (bhusu01)
 */
public class WebviewLoginPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebviewLoginPage.class);

    private final WebDriver driver;

    private WebDriverWait wait;

    String loginButtonXPath = "html/body/form/center/table/tbody/tr/td/table/tbody/tr[7]/td/input[6]";

    @FindBy(name="USER")
    private WebElement userNameField;

    @FindBy(name="PASSWORD")
    private WebElement passwordField;

    By loginButtonLocator = By.xpath(loginButtonXPath);

    public WebviewLoginPage(WebDriver driver) {
        super();
        this.driver = driver;
        wait = new WebDriverWait(driver,20);
        PageFactory.initElements(driver,this);
        LOGGER.info("Initialized login page");
    }

    public WebviewLoginPage typeUserName(String userName) {
        userNameField.sendKeys(userName);
        LOGGER.info("Entered username: " + userName);
        return this;
    }

    public WebviewLoginPage typePassword(String password) {
        passwordField.sendKeys(password);
        LOGGER.info("Entered password: " + password);
        return this;
    }

    public WebViewPage submitLogin() {
        LOGGER.info("Attempting login");
        driver.findElement(loginButtonLocator).submit();
        WebViewPage webviewPage = new WebViewPage(driver);
        return webviewPage;
    }

    public void checkLoginPageContent() {
        if (driver.findElements(By.id("LoginFrame")).size() > 0) {
            // we are on login page
            LOGGER.info("Switching to login frame");
            driver.switchTo().frame("LoginFrame");
        }
        String currentURL = driver.getCurrentUrl();
        LOGGER.info("Checking login page content");
        LOGGER.info("Current page: " + currentURL);
        wait.until(ExpectedConditions.visibilityOf(userNameField));
        assertTrue(userNameField.isDisplayed(), "Username field not found");
        assertTrue(passwordField.isDisplayed(), "Password field not found");
        assertTrue(driver.findElement(loginButtonLocator).isDisplayed(),
            "Login button not displayed");
        assertTrue(driver.findElement(loginButtonLocator).isEnabled(), "Login button not enabled");
        LOGGER.info("Login page content visible");
    }
}
