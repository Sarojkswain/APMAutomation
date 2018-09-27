package com.ca.apm.tests.pagefactory;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.test.utils.Utils;

/**
 * @author kurma05
 *
 */
public class LoginPage {
    
    protected long maxWait = 15000;
    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);
    
    WebDriver driver;
    
    @FindBy(id="username")
    WebElement userName; 
    
    @FindBy(id="j_passWord")
    WebElement passWord; 
    
    @FindBy(id="webview-loginPage-login-button")
    WebElement signIn; 
 
    public LoginPage(WebDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);  
    }
    
    public void setUserName(String strUserName) throws InterruptedException {
        userName.sendKeys(strUserName);
    }
    
    public void setPassword(String strPassword) throws InterruptedException {
        passWord.sendKeys(strPassword);
    }
    
    public void clickSignIn() throws InterruptedException {        
        signIn.click();
    }
     
    public void loginToApmServer(String strUserName, String strPassword) throws Exception {
        
        logger.info("Switching to default page content...");
        driver.switchTo().defaultContent(); 
        switchToFrame("LoginFrame");
        
        logger.info("Setting user name to: " + strUserName);
        this.setUserName(strUserName);
        logger.info("Setting user password to: " + strPassword);
        this.setPassword(strPassword);
        
        logger.info("Clicking sign in...");
        this.clickSignIn();
        Thread.sleep(maxWait);
    }
    
    private void switchToFrame(String frame) throws Exception {
        
        int attempts = 1;
        
        while(attempts <= 5) {
            try {
                logger.info("Switching to frame '{}'... Attempt #{}", frame, attempts);
                WebElement fr = driver.findElement(By.id(frame));
                driver.switchTo().frame(fr);
                return;
            }
            catch(Exception e) {
                logger.warn("Error occurred switching to frame '{}': {}", frame, e.getMessage());
                e.printStackTrace();
                Utils.logAllUIElements(driver);
                if(attempts == 5) {
                    throw e;
                }
            }
            Thread.sleep(5000);
            attempts++;
        }
    }
}