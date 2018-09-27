package com.ca.apm.saas.pagefactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.slf4j.*;

/*
 * @author Jyotsna Akula 
 * 
 */

public class AxaApmPage
{
    protected long delay = 10;
    protected int wait = 3000;
    protected long maxWait = 15000;
  
    WebDriver driver;
    protected static final Logger logger = LoggerFactory.getLogger(AxaPage.class);

    // pop-up window 
    @FindBy(xpath="//div[@class='walkme-click-and-hover walkme-custom-balloon-close-button walkme-action-close']")
    WebElement axaPopUp;
    
    @FindBy(xpath=("//span[contains(@class,'section-title ng-binding') and contains(text(),'Analytics')]"))
    WebElement analyticsLink; 

    // doesnt work for axa-apm case since the clicks are different
    @FindBy(xpath=("//ul[contains(@class,'sub-section-nav ng-scope')]//span[contains(text(),'Sessions')]"))
    WebElement sessionsLink; 

    @FindBy(xpath=("//h2[contains(text(),'Sessions')]"))
    WebElement sessionsTxt;
    
    @FindBy(xpath=("//img[contains(@src,'grid-icon.svg')]"))
    WebElement productGridIcon; 

    @FindBy(xpath=("//span[contains(text(),'Data Studio')]"))
    WebElement dataStudioLink;

    @FindBy(xpath=("//span[contains(text(), 'OK')]"))
    WebElement walkThroughOK;
   
    //@FindBy(xpath=("//input[@class='big ng-pristine ng-valid error']"))
    @FindBy(xpath=("//input[contains(@class,'big ng-pristine ng-valid error')]"))
    WebElement apmHostUrl;
   
    @FindBy(name="versionss")
    WebElement apmVersion;
    
    @FindBy(xpath=("//span[@class='button mxm-button-button']/span/span[contains(text(),'Generate URL')]"))
    WebElement generateUrl;
    
    @FindBy(xpath=("//span[@class='button mxm-button-button']/span/span[contains(text(),'Save')]"))
    WebElement saveUrl;
    
    @FindBy(linkText=("Sessions"))
    WebElement sessionDetailsLink;
    
    @FindBy(xpath=("//a[contains(text(),'App Performance')]"))
    WebElement appPerformanceLink;

    @FindBy(id="appexperienceanalytics") 
    WebElement appExperienceAnalytics;
    
    @FindBy(xpath=("//span[contains(@class,'section-title ng-binding') and contains(text(),'Manage Apps')]"))
    WebElement manageAppsLink; 
    
    @FindBy(xpath=("//a[contains(@class,'ng-binding') and contains(text(),'Logout')]"))
    WebElement logoutLink; 

    @FindBy(xpath=("//div[@class='banner-modal-close']"))
    WebElement closeWindow; 
   
 
    public boolean generateURL() throws InterruptedException{
        int attempts = 0;
        while (attempts < 2) {
        	try{
            	logger.info("generate URL is displayed? {}", generateUrl.isDisplayed());
        		new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(generateUrl)).click();
            	logger.info("Clicked generate URL");
				return true;
	     	}catch(NoSuchElementException nse){
		    	logger.info("No such element in the page " + nse.getMessage());
	        	Thread.sleep(wait);
	        }
        	attempts++;
        }
		return false;
    }     	

    
    public boolean saveURL() throws InterruptedException{
        int attempts = 0;
        while (attempts < 2) {
        	try{
        	    logger.info("save URL is displayed? {}", saveUrl.isDisplayed());
        		new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(saveUrl)).click();	
            	logger.info("Clicked save URL");
            	Thread.sleep(wait);
				return true;
	     	}catch(NoSuchElementException nse){
		    	logger.info("No such element in the page " + nse.getMessage());
	        	Thread.sleep(wait);
	        }
        	attempts++;
        }
		return false;
    }     	

    public boolean closeApmSetup() throws InterruptedException{
        int attempts = 0;
        while (attempts < 2) {
        	try{
				//WebElement close = driver.findElement(By.xpath("//div[@class='banner-modal-close']"));
				logger.info("close APM Setup:getText-{} getTagName-{} , isDisplayed-{}", closeWindow.getText(), closeWindow.getTagName(), closeWindow.isDisplayed());
				closeWindow.click(); 
				logger.info("Closed APM Setup");
				Thread.sleep(wait);
				return true;
	     	}catch(NoSuchElementException nse){
		    	logger.info("No such element in the page " + nse.getMessage());
	        	Thread.sleep(wait);
	        }
        	attempts++;
        }
		return false;
    }   	
  
	public boolean setApmConfig(String version) throws InterruptedException {
        int attempts = 0;
        while (attempts < 2) {
        	try{
			    logger.info("apm version is displayed? {}", apmVersion.isDisplayed());
				Select apmVersionDropDown = new Select(apmVersion);
			    apmVersionDropDown.selectByValue(version);
			    return true;
	     	}catch(NoSuchElementException nse){
		    	logger.info("No such element in the page " + nse.getMessage());
	        	Thread.sleep(wait);
	        }
        	attempts++;
        }
		return false;		
	}
   public boolean setApmHostURL(String instance) throws InterruptedException {
        int attempts = 0;
        int maxAttempts = 2;
        while  (attempts < 2) {
            try {
            	switch(instance.toLowerCase()){
            		case "staging":
            			logger.info("APM Host Url input field for {} is displayed? {}", instance, apmHostUrl.isDisplayed());
            			apmHostUrl.sendKeys("https://869873.staging.apm.cloud.ca.com:443");
	            		return true;
            		case "production":
	        			logger.info("APM Host Url input field for {} is displayed? {}", instance, apmHostUrl.isDisplayed());
	        			apmHostUrl.sendKeys("https://543718.apm.cloud.ca.com:443");
	            		return true;
            		default:
            			Assert.fail("Unsupported instance type to setup APM host URL for testing BA webapp AXA APM integration" + instance);
             	}
        	} catch(Exception e){
        		logger.info("Setting up APM host URL for " + instance + " Home page link. Attempt #{}/{}", attempts + 1, maxAttempts);
        	}
            Thread.sleep(wait);
            attempts++;
        } 
        return false;
    }
   
    public boolean clickProductGridIcon(String product) throws InterruptedException {
        int attempts = 0;
        while (attempts < 2) {
            try {
            	switch(product.toUpperCase()){
            		case "AXA":
            			logger.info("Clicking product icon grid to navigate to {}", product);
	            		productGridIcon.click();
	            		appExperienceAnalytics.click();
	            		logger.info("Waiting {} seconds to close pop up window incase it appears before proceeding further.", wait);
	            		Thread.sleep(wait);
	            		closeAxaPopUp();
	            		logger.info("Waiting {} seconds to close walk through windows incase they appear before proceeding further.", maxWait);
	            		Thread.sleep(maxWait);
	            		closeWalkThroughOK();
	            		return true;
            		case "APM":
            			logger.info("Clicking product icon grid to navigate to {}.", product);
            			productGridIcon.click();
            			appPerformanceLink.click();
            			return true;
            		default:
            			Assert.fail("Unsupported product type " + product);
            			return false;
             	}
        	} catch(Exception e){
        		logger.info("Wating for navigation to product {} Home page link. Attempt #{}/2.", product, attempts + 1);
        		Thread.sleep(wait);
        	}
           attempts++;
        }
		return false;       
    }
    
    public boolean clickManageAppsLink () throws InterruptedException {
        int attempts = 0;
        while (attempts < 2) {
            try {
                Actions builder = new Actions(driver);
                logger.info("Clicking manage apps link. attempt {}/2", (attempts+1));
                builder.moveToElement(analyticsLink).moveToElement(manageAppsLink).click().build().perform();
                logger.info("Clicked manage apps link");
               	return true; 
            } catch (StaleElementReferenceException e) {
                logger.warn("'Manage Apps' element is no longer appearing on the DOM page - " + e.getMessage());
                Thread.sleep(wait);
           }catch(NoSuchElementException nse){
                logger.info("Unable to click Manage apps link" + nse.getMessage());
                Thread.sleep(wait);
            }
            attempts++;
        }
		return false;
    }
    
    public boolean clickAxaAppLink(String axaApp) throws InterruptedException {
        int attempts = 0;
        String xpathExpression = "//h4[contains(text(),'" + axaApp + "')]";
        logger.info("xpath for {} axa app link is {} ", axaApp, xpathExpression);
        while (attempts < 2) {
        	try{
        		Thread.sleep(wait);
		    	WebElement we = driver.findElement(By.xpath(xpathExpression));
		    	logger.info("App {} displayed? {}", axaApp,we.isDisplayed());
		    	we.click();
		    	logger.info("Clicked axa app link for [{}]", axaApp);
		    	return true;
	     	}catch(NoSuchElementException nse){
		    	 logger.info("No such element in the page " + nse.getMessage());
	             Thread.sleep(wait);
	     	}
        	attempts++;
        }
		return false;
    } 
    
    public boolean clickApmSetupLink() throws InterruptedException {
        int attempts = 0;
        while (attempts < 2) {
        	try{
	        	WebElement we = driver.findElement(By.xpath("//a/span/span[contains(text(),'APM Setup')]"));
	        	logger.info("Clicking APM Setup link");
	        	we.click();
	        	logger.info("Clicked APM Setup link");
	            Thread.sleep(wait);
	        	return true;
	        }catch(NoSuchElementException nse){
	            logger.info("No such element in the page " + nse.getMessage());
	            Thread.sleep(wait);
	        }
        	attempts++;
        }
        return false;
    }
   
    public boolean filterSessions(String axaApp) throws InterruptedException{
  	  WebElement findSessions = driver.findElement(By.xpath("//input[@placeholder='Find Sessions']"));
      
  	  int attempts = 0;
        while (attempts < 2) {
        	try{
            	logger.info("Find Sessions input field is displayed ? {}", findSessions.isDisplayed());
              //move to element find session or any other webelement to collapse left menu
            	Actions builder = new Actions(driver);
            	builder.moveToElement(findSessions).build().perform();
            	findSessions.sendKeys(axaApp);
            	logger.info("Filter set to find sessions for {}",axaApp);
            	break;
        	}catch(NoSuchElementException nse){
  		    	logger.info("No such element in the page " + nse.getMessage());
  	        	Thread.sleep(3000);
  	        }
        	attempts++;
        }
    	attempts = 0;
      while (attempts < 2) {
        	try{
      	    WebElement sessionDetailsLink = driver.findElement(By.xpath("//span[contains(@class,'session-message-column')]/div[@id='t_ss_row_problem_0']"));
            	logger.info("Session Details link for {} is displayed ? {}", axaApp, sessionDetailsLink.isDisplayed());
            	sessionDetailsLink.click();
            	logger.info("Session Details link for {} is clicked, waiting for deeplink", axaApp);
            	Thread.sleep(30000);
            	return true;
  	     	}catch(NoSuchElementException nse){
  		    	logger.info("No such element in the page " + nse.getMessage());
  	        	Thread.sleep(10000);
  	        }
        	attempts++;
        }
  		return false;
    }  
    
    public boolean clickDeepLink() throws InterruptedException {
  	    int attempts = 0;
  	    while (attempts < 5) {
  	    try {
  	          logger.info("Accessing deep link to TT");
  	          WebElement we = driver.findElement(By.xpath("//a/span[contains(@class,'open-in')]"));
  	          logger.info("Deep link to TT is displayed?{}",we.isDisplayed());
  	          Thread.sleep(15000);
  	          we.click();
  	          logger.info("Clicked deep link to TT");
  	          return true;       
  	      }catch (Exception e) {
  	          logger.info("Error while clicking deep link - {}, attempt {}/5" + e.getMessage());
  	          Thread.sleep(15000);
  	      }
  	      attempts++;
  	  }
  	  return false;
  }
   
  

    
    public boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            logger.warn("No such element in the page " + e.getMessage());
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
            logger.warn("No such element in the page " + e.getMessage());
            return false;
        }

        return exists;
    }

    public AxaApmPage(WebDriver driver){
        this.driver = driver;
        //wait for maximum of 10 seconds until the elements specified by annotations is loaded. 
        //If the element is not found in the given time interval, it will throw NoSuchElementException' exception.
        AjaxElementLocatorFactory factory = new AjaxElementLocatorFactory(driver, 10);
        // This initElements method will create all WebElements
        PageFactory.initElements(driver, this); 
    }
    
    public void clickAnalyticsLink() throws InterruptedException {
        Thread.sleep(wait);
        analyticsLink.click();       
        logger.info("AXA: Clicked Analytics Link");

    }

    
    public boolean clickSessionsLink (RemoteWebDriver driver, String instance) throws InterruptedException {

	    int attempts = 0;
    	String url = new String();
    	instance.toLowerCase();
	    while (attempts < 2) {
	        try {
	        	switch(instance){
	        		case ("staging"):
		        		url = "https://staging.cloud.ca.com/admin/maa/#/analytics/sessions/summary";
		        		break;
	        		case ("production"):
		        		url = "https://cloud.ca.com/admin/maa/#/analytics/sessions/summary";
		        		break;	       
	        		default:
	        			Assert.fail("Instance type " + instance + " not supported to navigate to sessions summary page (to test deep link to TT)");	        			
	        	}
	        	logger.info("Sessions summary page for instance[{}]  is [{}]", instance, url);
	        	driver.get(url);
	            return true;
	        } catch (Exception e) {
	            logger.warn("Unableto load sessions summary page - " + e.getMessage());
	        	Thread.sleep(wait);
	        }
	        attempts++;
	    }
	  return false;
  }
   public WebElement getAnalyticsLink() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(analyticsLink));
        logger.info("AXA: analyticsLink WebElement shows text as: [" + analyticsLink.getText() + "]");
        return analyticsLink;
    }
    
    public void closeAxaPopUp(){    
        try {
            logger.info("AXA: Clicking 'Welcome to AXA' pop-up if it shows up within 10 seconds, otherwise  skip the step ...");
            new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(axaPopUp)).click();
            logger.info("AXA: Successfully closed AXA pop-up window");
        } catch (Exception e) {
            logger.info("AXA: Welcome to AXA Pop-Up doesn't appear. ... ignoring" );
        }
    }
 
    public void closeWalkThroughOK(){
        try {
            logger.info("AXA: Clicking Walk through pop-up if it shows upwithin 10 seconds, otherwise  skip the step ...");
            new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(walkThroughOK)).click();
            logger.info("AXA: Successfully closed walk through pop-up window");
        } catch (Exception e) {
            logger.info("AXA: walk through window doesn't pop up. ... ignoring" );
        }
        
    }
        
}
