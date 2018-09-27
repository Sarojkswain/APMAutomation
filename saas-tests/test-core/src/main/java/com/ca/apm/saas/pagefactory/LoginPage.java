package com.ca.apm.saas.pagefactory;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author Liddy Hsieh, Abhishek Sinha
 */
public class LoginPage {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);
    protected long delay = 10;
    protected long maxWait = 15000;
    
    WebDriver driver;
    
    @FindBy(id="username")
    WebElement userName; 
    
    @FindBy(id="password")
    WebElement passWord; 
    
    @FindBy(id="tenant_id")
    WebElement tenantId; 

    @FindBy(linkText = "Sign in with another Tenant")
    WebElement loginWithAnotherTenant;
    
    @FindBy(id="sppSignInBtn")
    WebElement signIn; 
    
    @FindBy(id="signin")
    WebElement titleText; 

    @FindBy(id="settings-view-link")
    WebElement settingsIcon; 
    
    @FindBy(xpath="//div[@class='panel sign-in-panel ng-scope']")
    WebElement signInText;
    
    // pop-up windows    
    @FindBy(xpath="//div[@class='wm-close-button walkme-x-button']")
    WebElement apmLoginPopupPage1;
    
    @FindBy(xpath="//div[@class='walkme-click-and-hover walkme-custom-balloon-close-button walkme-action-close']")
    WebElement apmLoginPopupPage2;
    
    public boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }    
    
    public boolean isElementPresent(WebDriver webdriver, WebElement webelement) {    
        boolean exists = false;

        webdriver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);

        try {
            webelement.getTagName();
            exists = true;
        } catch (NoSuchElementException e) {
            logger.warn("Error occurred: " + e.getMessage());
        }

        webdriver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);

        return exists;
    }

    public LoginPage(WebDriver driver){
        this.driver = driver;
        // This initElements method will create all WebElements
        PageFactory.initElements(driver, this);  
    }
 
    /**
     * Set user name & click Sign In Button
     * 
     * @param strUserName
     * @throws InterruptedException
     */
    public void setUserName(String strUserName) throws InterruptedException {
       
        userName.sendKeys(strUserName);
    }
    
    /**
     * Set password & click Sign In Button
     * 
     * @param strUserName
     * @throws InterruptedException
     */
    public void setPassword(String strPassword) throws InterruptedException {
        
        passWord.sendKeys(strPassword);
    }
    
    public void setTenantId(String strTenantId) throws InterruptedException {
       
        tenantId.sendKeys(strTenantId);
    }

    public void clickSignIn() throws InterruptedException {
     
        signIn.click();
    }
    
    public void clickAnotherTenant() throws InterruptedException {
     
        loginWithAnotherTenant.click();
    }
    
    /**
     * Get the title of Login Page
     * 
     * @return
     */
    public String getLoginTitle() {
        return titleText.getText();
    }
    
    public String getSettingsIcon() throws InterruptedException {
        
        Thread.sleep(delay);
        return settingsIcon.getText();
    }
    
    public void closePopUp() throws InterruptedException{
    	
        Thread.sleep(maxWait);
        
        if(isElementPresent(driver, apmLoginPopupPage1)){
            apmLoginPopupPage1.click();
            logger.info("1st pop-up window closed");
            Thread.sleep(maxWait);
        } 
        
        if(isElementPresent(driver, apmLoginPopupPage2)){
            apmLoginPopupPage2.click();
            logger.info("2nd pop-up window closed");
            Thread.sleep(maxWait);
        }    
    }
    
    public void dismissAlert() throws NoAlertPresentException {
        // Switching to Alert        
        Alert alert=driver.switchTo().alert();      
                
        // Capturing alert message.    
        String alertMessage=driver.switchTo().alert().getText();        
                
        // Displaying alert message     
        logger.info(alertMessage);           
                
        // Accepting alert      
        alert.dismiss();             
    }
    
    /** 
     * This POM method will be exposed in test case to login
     * @param strUserName user name
     * @param strPassword password
     * @param strTenantId tenant id
     * @return 
     * @throws InterruptedException 
     */   
    public void signInToDemo(String strUserName, String strPassword, 
                             String strTenantId) throws InterruptedException {
        
       // Add a condition where "Tenant ID" field is disabled and need to click on
       // sign-in-with different Tenant ID
       if (this.isElementPresent(By.className("field-title signin-another-tenant ng-binding") )){
            this.clickAnotherTenant();
            Thread.sleep(maxWait);            
        }
        this.setUserName(strUserName);
        this.setPassword(strPassword);
        this.setTenantId(strTenantId);
        this.clickSignIn();
        Thread.sleep(maxWait);
    }
}