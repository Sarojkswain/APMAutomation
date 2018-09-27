package com.ca.apm.saas.pagefactory;

import java.util.ArrayList;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.openqa.selenium.support.ui.WebDriverWait;

/*
 * @author kurma05, hsiwa01
 */
public class AgentsPage {

    private static final Logger logger = LoggerFactory.getLogger(AgentsPage.class);
    private int waitSeconds = 3000;
    private int delay = 10;
    private int maxWait = 15000;
    private WebDriver driver;
    
    @FindBy(id = "settings-agents-link")
    WebElement agentsTab;
    
    @FindBy(id="agentsview")
    WebElement agentsView; 

    @FindBy(xpath="//span[@class='ng-scope'][contains(text(),'Trace All Agents')]")
    WebElement traceAllAgentsButton;

    @FindBy(xpath="//span[@class='ng-scope'][contains(text(),'Start')]")
    WebElement startButton;

    @FindBy(xpath="//span[@class='ng-scope'][contains(text(),'Close')]")
    WebElement closeButton;

    @FindBy(id="timeline-header")
    WebElement headerText; 


    public AgentsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    public void clickAgentsTab() throws InterruptedException {

        logger.info("Clicking on the agent tab...");
        agentsTab.click();
        Thread.sleep(waitSeconds);
    }
    
    public boolean sortAgentsByHost() throws InterruptedException {
    	//String xpathExpression = "//div[contains(@class,'ui-grid-coluiGrid-00F1')]/div/div/span[contains(@class,'ui-grid-header-cell-label ng-binding')]";
    	String xpathExpression = "//span[contains(@class,'ui-grid-header-cell-label ng-binding') and contains(text(),'Host')]";
        for (int k = 0;    k < 2 ;k++ ){
        	try{   
	        	logger.info("Clicking Host to sort agents by Host - sleeping for {} sec. k[{}] ",(waitSeconds *5)/1000,k);
        		Thread.sleep(waitSeconds*2);
	        	driver.findElement(By.xpath(xpathExpression)).click();
	        	logger.info("Clicked Host to sort agents by Host. k[{}] ",k);
	        	return true;
	        }catch(NoSuchElementException nse){
	            logger.error("Unable to find Host in the Agents page . k[{}] ",k);
	            nse.getMessage();
	        }
        }
		return false;
    }
    
    public void startTransactionTrace(String agentName, String agentHost) throws InterruptedException {
        startTransactionTrace(agentName, agentHost, "1000", "1");
    }
    
    public void startTransactionTrace(String agentName, String agentHost, 
                                      String minTraceDuration, String traceSessionDuration) throws InterruptedException {

        logger.info("Starting TT session for agent '{}' on host '{}'...", agentName, agentHost);
        
        String xpath = "//div[@id='agents-overview-table']/div/div/table/tbody/tr[(td/div[contains(text(),'" + 
            agentName + "')]) and (td/div[contains(text(),'" + agentHost + "')])]/*/div/button/span[contains(text(),'Trace Agent')]";
        
        WebElement traceAgentButton = driver.findElement(By.xpath(xpath));
        traceAgentButton.click();
        Thread.sleep(waitSeconds);
        
        //set duration & min 
        driver.findElement(By.xpath("//input[@name='minTraceDuration']")).clear();
        driver.findElement(By.xpath("//input[@name='minTraceDuration']")).sendKeys(minTraceDuration);
        driver.findElement(By.xpath("//input[@name='traceSessionDuration']")).clear();
        driver.findElement(By.xpath("//input[@name='traceSessionDuration']")).sendKeys(traceSessionDuration);
        Thread.sleep(waitSeconds);
        
        //start trace
        WebElement startButton = driver.findElement(
            By.xpath("//span[@class='ng-scope'][contains(text(),'Start')]"));
        startButton.click();
        Thread.sleep(waitSeconds);
        
        WebElement closeButton = driver.findElement(
            By.xpath("//span[@class='ng-scope'][contains(text(),'Close')]"));
        closeButton.click();
        Thread.sleep(waitSeconds);
    }
    
    public void startTraceAllAgents() throws InterruptedException {
        try {
            logger.info("Clicking'Trace All Agents' button ...");
            new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(traceAllAgentsButton)).click();
            
            logger.info("Starting 'Trace All Agents' ...");
            new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(startButton)).click();
            
            logger.info("Closing 'Trace All Agents' ...");
            new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(closeButton)).click();
        } catch (NoSuchElementException e) {
            logger.warn("'Trace All Agent/Start/Stop buttons is missing. Error: " + e.getMessage());
        }
    }   
      

    public boolean clickAgentsView() throws InterruptedException {
        logger.info("Clicking on left menu - Agents View");
        try {
            agentsView.click();
            Thread.sleep(waitSeconds);
            return true; 
        } catch (Exception e) {
            logger.warn("Agent View missing. Error: " + e.getMessage());
        }
        return false;
    }
    
    public boolean openAnIsolationViewForAgent(String agentCardName) throws InterruptedException {
        int attempts = 0;
        while (attempts < 4) {
            logger.info("Opening an isolation view for Agent card name:  - {} ...", agentCardName);
            logger.info("Attempt # " + (attempts + 1) + "/3. Retrying every " + maxWait+ " seconds");
            try {
                //div[(div/div/span[contains(@title,'All Agents')]) and (div/div/span/a[contains(@title,'Open an Isolation view')])]/div/div/span/a
                String xpath = "//div[(div/div/span[contains(@title,'"+ agentCardName  +"')]) and (div/div/span/a[contains(@title,'Open an Isolation view')])]/div/div/span/a";
                WebElement element = driver.findElement(By.xpath(xpath));
                logger.info("Sleeping " + maxWait + " seconds for element to show ...");
                Thread.sleep(maxWait);

                // element is wrapped in a div or a span. The page is fully loaded and visible, but chromedriver refused to click it. Here is the solution. 
                new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(element));
                element.sendKeys(Keys.RETURN); 

                logger.info("Clicked an isolation view for Agent card  - {} !", agentCardName);
                return true;
            } catch (Exception e) {
                logger.warn("Cannot Open an Isolated View for Agent card  - {}  missing. Error: {} ", agentCardName, e.getMessage());
            }
            attempts++;
        }
        return false;
    }
    
    public boolean gotoNewOpenedTab() throws InterruptedException {
        try {
            //Get Current Page
            logger.info("Getting parent window handle.");
            String parentWindow = driver.getWindowHandle();  
            
            //Add Logic to Wait till Page Load
            Thread.sleep(maxWait);

            // Get all Open Tabs
            logger.info("Getting all Open tabs");
            ArrayList<String> tabHandles = new ArrayList<String>(driver.getWindowHandles());
            
            for(String eachHandle : tabHandles)
            {
                if (!eachHandle.equals(parentWindow))
                {
                    driver.switchTo().window(eachHandle);
                    logger.info("Switched to a child new tab!");
                    return true;                    
                }
            }
            logger.info("At the end of 'gotoNewOpenedTab' method");
            
        } catch (Exception e) {
            logger.warn("New Opened tab missing. Error: " + e.getMessage());
        }
        return false;
    }

    private boolean isElementPresent(By by) {
        try {
          driver.findElement(by);
          return true;
        } catch (NoSuchElementException e) {
            logger.warn("Element is NOT presenting. Error: " + e.getMessage());
            return false;
        }
      }

}
