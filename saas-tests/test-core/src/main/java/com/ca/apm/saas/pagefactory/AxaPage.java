package com.ca.apm.saas.pagefactory;

import java.util.ArrayList;
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
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.slf4j.*;

import com.ca.apm.saas.test.SaaSBaseTest;

/*
 * @author Liddy Hsieh, Jyotsna Akula 
 * 
 */

public class AxaPage
{
    protected long delay = 10;
    protected long maxWait = 15000;
    
    WebDriver driver;
    protected static final Logger logger = LoggerFactory.getLogger(AxaPage.class);
    
  //private String getFilePath(String downloadPath, String... nameTags){

    @FindBy(xpath=("//span[contains(text(),'Done')]"))
    WebElement doneButton; 

    // pop-up window 
    @FindBy(xpath="//div[@class='walkme-click-and-hover walkme-custom-balloon-close-button walkme-action-close']")
    WebElement axaPopUp;

    @FindBy(xpath=("//div[contains(text(),'AXA Application')]"))
    WebElement axaAppTxt;

    @FindBy(id="appexperienceanalytics") 
    WebElement appExperienceAnalytics;
    
    //@FindBy(xpath=("//span[@class='section-title ng-binding'][contains(text(),'Analytics')]"))
    @FindBy(xpath=("//span[contains(@class,'section-title ng-binding') and contains(text(),'Analytics')]"))
    WebElement analyticsLink; 

    // Sub links of Analytics
    @FindBy(xpath=("//ul[contains(@class,'sub-section-nav ng-scope')]//span[contains(text(),'Overview')]"))
    WebElement overviewLink;
    
    @FindBy(xpath=("//h2[contains(text(),'App Ranking')]"))
    WebElement appRankingTxt;
        
    @FindBy(xpath=("//div[contains(text(),'App Experience Analytics')]"))
    WebElement appExperienceAnalyticsTxt; 

    @FindBy(xpath=("//ul[contains(@class,'sub-section-nav ng-scope')]//span[contains(text(),'Performance')]"))
    WebElement performanceLink; 

    @FindBy(xpath=("//a[contains(text(),'App Performance')]"))
    WebElement appPerformanceLink;
    
    @FindBy(xpath=("//ul[contains(@class,'sub-section-nav ng-scope')]//span[contains(text(),'Crashes')]"))
    WebElement crashesLink; 

    @FindBy(xpath=("//a[contains(text(),'App Crashes')]"))
    WebElement appCrashesLink;

    @FindBy(xpath=("//ul[contains(@class,'sub-section-nav ng-scope')]//span[contains(text(),'Usage')]"))
    WebElement usageLink; 
    
    
    @FindBy(xpath=("//div[contains(@class,'box-header ng-scope box-header-tight')]//h2[contains(text()[1],'Users')]"))
    WebElement usersTxt;


    //@FindBy(xpath=("//ul[contains(@class,'sub-section-nav ng-scope')]//span[contains(text(),'Sessions')]"))
    @FindBy(xpath=("//ul[contains(@class,'sub-section-nav ng-scope')]//span[contains(text(),'Sessions')]"))
    WebElement sessionsLink; 

    @FindBy(xpath=("//h2[contains(text(),'Sessions')]"))
    WebElement sessionsTxt;


    @FindBy(xpath=("//span[contains(@class,'section-title ng-binding') and contains(text(),'Compare')]"))
    WebElement compareLink; 
    
    @FindBy(xpath=("//span[contains(@class,'section-title ng-binding') and contains(text(),'Manage Apps')]"))
    WebElement manageAppsLink; 

    @FindBy(xpath=("//span[contains(@class,'section-title ng-binding') and contains(text(),'Alerts')]"))
    WebElement alertsLink; 

    @FindBy(xpath=("//span[contains(@class,'section-title ng-binding') and contains(text(),'Help')]"))
    WebElement helpLink; 

    @FindBy(xpath=("//span[contains(@class,'section-title ng-binding') and contains(text(),'Community')]"))
    WebElement communityLink; 

    @FindBy(xpath=("//a[contains(@class,'ng-binding') and contains(text(),'Logout')]"))
    WebElement logoutLink; 

    // //div[contains(@class,'navbar-option-content-container')] // list of items
    @FindBy(xpath=("//img[contains(@src,'grid-icon.svg')]"))
    WebElement productGridIcon; 

    @FindBy(xpath=("//span[contains(text(),'Data Studio')]"))
    WebElement dataStudioLink;

    @FindBy(id="i-frame") ///html/body/div[3]/div[1]/div/div/div/iframe
    WebElement kibanaFrame;
    
    @FindBy(linkText="#/dashboard/01)-Browser-Overview")
    //@FindBy(xpath=("/html/body/div[2]/div/div/config/div/div[1]/saved-object-finder/paginate/ul/li[1]/a"))
    WebElement browserOverviewLink;
    
    //@FindBy(xpath=("/html/body/div[2]/div/div/navbar/span"))
    @FindBy(xpath=("//span[contains(text(),'Browser Overview')]"))
    WebElement browserOverviewTxt;
    
    //@FindBy(xpath=("//strong[contains(text(), 'dashboards')]"))
    @FindBy(xpath=("//span[contains(text(), 'dashboards')]"))
    WebElement dashboardsTxt;
    
    // ToDo: find element in iframe
    @FindBy(xpath=("//a[contains(text(),'dashboard')]/@href"))
    WebElement dashboardLink;
   
    @FindBy(xpath=("//span[contains(text(), 'OK')]"))
    WebElement walkThroughOK;
    
    @FindBy(xpath=("//*[@class='kuiLocalMenuItem navbar-timepicker-time-desc']"))
    WebElement timelineButton;
        
    @FindBy(xpath=("//*[@class='kbn-timepicker-section']//*[contains(text(),'Last 24 hours')]"))
    WebElement last24HoursTimelineButton;
    
    @FindBy(xpath=("//*[@class='panel-heading']//*[contains(text(),' Session Overview')]"))
    WebElement sessionOverviewPanelText;
    
     
    public WebElement getKibanaFrame() {
        return driver.findElement(By.id("kibanaFrame"));
    }
    
    public String getSelectedNavigationLink() {
        return driver.findElement(new ByChained(By.className("nav-item-selected"), By.xpath("./a/span"))).getText();
        
    }
    
    public String getBrowserOverviewLinkTxt() {
        return driver.findElement(By.xpath("//li[@class='list-group-item list-group-menu-item ng-scope']/a")).getAttribute("href");    
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

    public AxaPage(WebDriver driver){
        this.driver = driver;
        //wait for maximum of 10 seconds until the elements specified by annotations is loaded. 
        //If the element is not found in the given time interval, it will throw NoSuchElementException' exception.
        AjaxElementLocatorFactory factory = new AjaxElementLocatorFactory(driver, 10);
        // This initElements method will create all WebElements
        PageFactory.initElements(driver, this); 
    }
    
    public void clickAnalyticsLink() throws InterruptedException {
        //new WebDriverWait(driver, maxWait).until(ExpectedConditions.elementToBeClickable(analyticsLink)).click();
        Thread.sleep(maxWait);
        analyticsLink.click();       
        logger.info("AXA: Clicked Analytics Link");

    }

    public void hoverOverMenu() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(analyticsLink));
        Actions builder = new Actions(driver);
        //Thread.sleep(maxWait);
        logger.info("AXA: Hold overview page");
        builder.moveToElement(analyticsLink).clickAndHold(overviewLink).build().perform();
        logger.info("AXA: *****" + getSelectedNavigationLink() + "*******");
        logger.info("AXA: After hold function");
    }
    
    public boolean isNavigationLinkSelected(String linkName) {
        WebElement fun = driver.findElement(By.xpath(String.format("//li[./a/span[@text='%s']]", linkName)));
        String text = fun.getAttribute("class");
        logger.info("AXA: ***** text of isNavigationLinkSelected " + text + "*****");
        return text.contains("nav-item-selected");
    }
    
    public void clickOverviewLink () throws InterruptedException {
        //new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(analyticsLink));
        int attempts = 0;
        while (attempts < 2) {
            try {
                Actions builder = new Actions(driver);
                Thread.sleep(maxWait);
                logger.info("AXA: Accessing overview page ");
                builder.moveToElement(analyticsLink).clickAndHold(overviewLink).moveToElement(overviewLink).click().build().perform();
                logger.info("AXA: Accessing overview page");
                Thread.sleep(maxWait);
                
                break;
            } catch (StaleElementReferenceException e) {
                logger.warn("'OverView Link' element is no longer appearing on the DOM page - " + e.getMessage());
            }
            attempts++;
        }
    }

    public void clickPerformanceLink () throws InterruptedException {
        //new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(analyticsLink));
        int attempts = 0;
        while (attempts < 2) {
            try {
                Actions builder = new Actions(driver);
                Thread.sleep(maxWait);
                logger.info("AXA: Accessing performance page");
                builder.moveToElement(analyticsLink).clickAndHold(performanceLink).moveToElement(performanceLink).click().build().perform();
                logger.info("AXA: Clicked performance page");
                Thread.sleep(maxWait);
                
                break;
            } catch (StaleElementReferenceException e) {
                logger.warn("'Performance Link' element is no longer appearing on the DOM page - " + e.getMessage());
            }
            attempts++;
        }
    }

    public void clickCrachesLink () throws InterruptedException {
        //new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(analyticsLink));
        int attempts = 0;
        while (attempts < 2) {
            try {
                Actions builder = new Actions(driver);
                Thread.sleep(maxWait);
                logger.info("AXA: Accessing Crashes & Errors page");
                builder.moveToElement(analyticsLink).clickAndHold(crashesLink).moveToElement(crashesLink).click().build().perform();
                logger.info("AXA: Clicked Crashes & Errors page");
                Thread.sleep(maxWait);
                
                break; 
                
            } catch (StaleElementReferenceException e) {
                logger.warn("'Craches Link' element is no longer appearing on the DOM page - " + e.getMessage());
            }
                attempts++;
        }
    }

    public void clickUsageLink () throws InterruptedException {
        //new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(analyticsLink));
        int attempts = 0;
        while (attempts < 2) {
            try {
                Actions builder = new Actions(driver);
                Thread.sleep(maxWait);
                logger.info("AXA: Accessing Usage page");
                builder.moveToElement(analyticsLink).clickAndHold(usageLink).moveToElement(usageLink).click().build().perform();
                logger.info("AXA: Clicked Usage page");
                Thread.sleep(maxWait);
                
                break;
            } catch (StaleElementReferenceException e) {
                logger.warn("'Usage Link' element is no longer appearing on the DOM page - " + e.getMessage());
            }
            attempts++;
        }
    }
    
    public void clickSessionsLink () throws InterruptedException {
        //new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(analyticsLink)); 
        int attempts = 0;
        while (attempts < 2) {
            try {
                Actions builder = new Actions(driver);
                Thread.sleep(maxWait);
                logger.info("AXA: Accessing Sessions page");
                builder.moveToElement(analyticsLink).clickAndHold(sessionsLink).moveToElement(sessionsLink).click().build().perform();
                logger.info("AXA: Clicked Sessions page");
                Thread.sleep(maxWait);
                break;
            } catch (StaleElementReferenceException e) {
                logger.warn("'Sessions Link' element is no longer appearing on the DOM page - " + e.getMessage());
            }
            attempts++;
        }
    }
        
    public void clickDataStudioLink() throws InterruptedException {
        /*
        Thread.sleep(maxWait);
        axaGridIcon.click();
        Thread.sleep(maxWait);
        dataStudioLink.click();
        */
        int attempts = 0;
        while (attempts < 2) {
            try {
                new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(productGridIcon)).click();
                logger.info("AXA: Clicked Axa Grid Icon");
                new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(dataStudioLink)).click();
                logger.info("AXA: Clicked Data Studio link");
                
                break;
            }catch (StaleElementReferenceException e) {
                logger.warn("'AXA Data Studio' element is no longer appearing on the DOM page - " + e.getMessage());
            }
            attempts++;
        } 
    }
    
    public void clickKibanaFrame() throws InterruptedException { 
        /*
        Thread.sleep(maxWait);
        kibanaFrame.click();
        Thread.sleep(maxWait);
        */
        new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(kibanaFrame)).click();
        logger.info("AXA: Clicked Kibana Frame");
    }
    
        
    public void checkDefaultDashboardInDataStudio() throws InterruptedException{
    	new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(kibanaFrame));
        logger.info("AXA: Kibana Frame shows up");
        driver.switchTo().frame(kibanaFrame);
        logger.info("AXA: Switched to iFrame");
    	
        logger.info("AXA: About to click the Timeline Button");
    	//new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(timelineButton));
        Thread.sleep(maxWait);
        clickButton(timelineButton);
        
        logger.info("AXA: About to select the 'Last 24 Hours' option");
        //new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(last24HoursTimelineButton));
        Thread.sleep(maxWait);
        clickButton(last24HoursTimelineButton);
        
    }
    
    public void clickButton(WebElement webElement) throws InterruptedException{
    	int attempts = 0;
        while (attempts < 2) {
            try {
                Actions builder = new Actions(driver);
                Assert.assertEquals(true, webElement.isDisplayed(), "The Button is NOT displayed");
                
                builder.moveToElement(webElement).click().build().perform();
                Thread.sleep(5000);
                
                break;
            }catch (StaleElementReferenceException e) {
                logger.warn("The element is no longer appearing on the Data Studio page - " + e.getMessage());
            }
            attempts++;
            logger.info("AXA: Attemp - " + attempts );
        }
    }
    
    /*
     * //get window handlers as list
List<String> browserTabs = new ArrayList<String> (driver.getWindowHandles());
//switch to new tab
driver.switchTo().window(browserTabs .get(1));
//check is it correct page opened or not (e.g. check page's title)
//...
//then close tab and get back
driver.close();
driver.switchTo().window(browserTabs.get(0))
     */
    
    public void logout() {
        try {
            logger.info("AXA: Logging out of AXA");
            new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
            logger.info("AXA: Successfully logged out of AXA");
        } catch (Exception e) {
            logger.info("AXA: Logout link doesn't appear. ... ignoring" );
        }
    }

    public WebElement getLogoutText() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(logoutLink));
        logger.info("Logout webElement shows up");
        return logoutLink;
    }


    public WebElement getAxaAppTxt() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(axaAppTxt));
        logger.info("AXA: 'Explore the AXA Application!' shows after login to AXA");
        return axaAppTxt;
    }

    public WebElement getAnalyticsLink() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(analyticsLink));
        logger.info("AXA: analyticsLink WebElement shows text as: [" + analyticsLink.getText() + "]");
        return analyticsLink;
    }
    
    public WebElement getAppExperienceAnalyticsTxt() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(appExperienceAnalyticsTxt));
        logger.info("AXA: App Experience Analytics WebElement shows text as: [" + appExperienceAnalyticsTxt.getText() + "]");
        return appExperienceAnalyticsTxt;
    }
    
    public WebElement getAppRankingTxt() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(appRankingTxt));
        logger.info("AXA: Application Ranking WebElement shows text as: [" + appRankingTxt.getText() + "]");
        return appRankingTxt;
    }
    
    public WebElement getAppPerformanceLink() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(appPerformanceLink));
        logger.info("AXA: PerformanceLink WebElement shows text as: [" + appPerformanceLink.getText() + "]");
        return appPerformanceLink;
    }
    
    public WebElement getAppCrashesLink() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(appCrashesLink));
        logger.info("AXA: Crashes WebElement shows text as: [" + appCrashesLink.getText() + "]");
        return appCrashesLink;
    }
    
    public WebElement getUsersTxt() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(usersTxt));
        logger.info("AXA: users WebElement shows tet as : [" + usersTxt.getText() + "]");
        return usersTxt;
    }
    
    public WebElement getSessionsTxt() {
        new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(sessionsTxt));
        logger.info("AXA: sessions WebElement shows text as : [" + sessionsTxt.getText() + "]");
        return sessionsTxt;
    }
    
    public WebElement getDashboardsTxt () {
        logger.info("AXA: There are [" + dashboardsTxt.getText() + "] under 'Load Saved Dashboard'");
        return dashboardsTxt;
    }
    
    public WebElement getSessionOverviewPanelHeader() {
        logger.info("AXA: There is [" + sessionOverviewPanelText.getText() + "] text in the Session Overview panel");
        return sessionOverviewPanelText;
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
