package com.ca.apm.saas.pagefactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HomePage
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    WebDriver driver;
    protected long delay = 10000;     
    List<String> browserTabs;
    
    @FindBy(xpath=("(//span[contains(@class,'ng-scope') and contains(text(),'Help')])[1]"))
    WebElement parentHelpLink; 

    @FindBy(xpath=("(//span[contains(@class,'ng-scope') and contains(text(),'Help')])[2]"))
    WebElement helpLink; 

    @FindBy(xpath=("//img[@title='Help']"))
    WebElement helpIcon; 

    @FindBy(id="homepage")
    WebElement experienceView;

    @FindBy(id="agentsview")
    WebElement agentsView; 

    @FindBy(id="mapview")
    WebElement mapView; 

    @FindBy(id="dashboard")
    WebElement dashboardView; 
    
    @FindBy(id="settings-perspectives-link")
    WebElement perspectiveLink;   
    
    @FindBy(id="settings-universe-link")
    WebElement universesLink;    

    @FindBy(id="settings-attributes-link")
    WebElement attributesLink;   
    
    @FindBy(id="settings-agents-link")
    WebElement agentsLink;    
    
    @FindBy(id="settings-security-link")
    WebElement securityLink;   
    
    @FindBy(id="settings-alerts-link")
    WebElement alertsLink;   

    @FindBy(id="settings-notifications-link")
    WebElement notificationsLink;   
  
    //@FindBy(linkText="ALL MY UNIVERSES")
    //@FindBy(xpath="//span[@title='Active Universe: ALL MY UNIVERSES']")
    @FindBy(xpath="//img[@src='res/dropdown-icon.svg']")
    WebElement experienceDropDown;
    
    @FindBy(css="a[title=\"Demo Applications\"]")
    WebElement demoApplications;
    
    @FindBy(css="a[title=\"Your Applications\"]")
    WebElement yourApplications;
    
    @FindBy(css="a[title=\"ALL MY UNIVERSES\"]")
    WebElement allMyUniverses;
    
    @FindBy(xpath="//span[@class='experience-card-heading-link truncate ng-binding']")
    WebElement allExperienceCards;  
    String xpath_allExperienceCards = "//span[@class='experience-card-heading-link truncate ng-binding']";
    
    @FindBy(linkText="Search")
    WebElement searchInput;
    

    public HomePage(WebDriver driver){
        this.driver = driver;
        // This initElements method will create all WebElements
        PageFactory.initElements(driver, this);  
    }
        
    public boolean isElementPresent(WebDriver webdriver, WebElement webelement) {    
        boolean exists = false;

        webdriver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);

        int attempts = 1;
        while (attempts < 3) {
            try {
                logger.info("Checking on the '{}'... Attempt #{}", webelement, attempts);
                webelement.getTagName();
                exists = true;
            } catch (NoSuchElementException e) {
                // nothing to do.
            } catch (Exception e2) {
                logger.warn("Error occurred checking on the '{}': {}; Attempting again...", webelement, e2.getMessage());
                e2.printStackTrace();
            }
                webdriver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
                attempts++;
        }
        return exists;
    }
    
    public boolean isHelpDocKeyPresent(String key, String value) {
        logger.info("APM Help: Checking Help Doc content == {}={} == exists...", key, value);
        String xpath = "//h1[contains(text()," + "'" + key + "'"+ ")]" ;
        //i.e. key: Experience View, value: Monitor Performance Using Experience View

        logger.info("APM Help: xpath = " + xpath );
        try {
            WebElement element = driver.findElement(By.xpath(xpath)); 
            String text = element.getText();
           
            logger.info("APM Help: Found Help Doc contains: {} ... next step: checking if this content is expected...", text);
            if(text.contains(value)) {
                logger.info("APM Help: content - {} - in the Help Doc is expected!!", text);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        logger.info("APM Help: Validation failed - Help doc doesn't contain required content - {}={}.", key, value);
        return false;
    }

    public boolean isHelpDocPresent(String value) {
        logger.info("APM Help: Checking Help Doc content == {} == exists...", value);
        //String id = "article-name"; // no longer available in the page on 7/27...
        //logger.info("APM Help: id =  " + id );
        String xpath = "//h1[@class='article-name']" ;
        logger.info("APM Help: xpath =  " + xpath );
        
        try {
            
            //WebElement element = driver.findElement(By.id(id));
            WebElement element = driver.findElement(By.xpath(xpath));
            String text = element.getText();
            
            logger.info("APM Help: Found Help Doc contains: {} ... next step: checking if this content is expected...", text);
            if(text.contains(value)) {
                logger.info("APM Help: content - {} - in the Help Doc is expected!!", text);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
  
        //logger.info("APM Help: Validation failed - Help doesn't contains required content - {}={}.", id, value);
        logger.info("APM Help: Validation failed - Help doesn't contains required content - {}={}.", xpath, value);
        return false;
    }

    public WebElement getWebElementHelpLink() {
        return helpLink;
    }
    
    public WebElement getWebElementSearchInput() {
        return searchInput;
    }

    public void clickLink(String linkName) throws InterruptedException {
        logger.info("Clicking on left menu - " + linkName);
        
        try {            
            switch(linkName.toLowerCase()) {
                case "experienceview":
                    experienceView.click();
                    logger.info("APM Help: Experience View - " + linkName + " is clicked");
                    break;
                case "agentsview":
                    agentsView.click();
                    logger.info("APM Help: Agents View - " + linkName + " is clicked");
                    break;
                case "mapview":
                    mapView.click();
                    logger.info("APM Help: Map View - " + linkName + " is clicked");
                    break;
                case "dashboardview":
                    dashboardView.click();
                    logger.info("APM Help: Dashboard View - " + linkName + " is clicked");
                    break;
                case "perspectivelink":
                    perspectiveLink.click();
                    logger.info("APM Help: Perspective Setting - " + linkName + " is clicked");
                    break;
                case "universeslink":
                    universesLink.click();
                    logger.info("APM Help: Universes Setting - " + linkName + " is clicked");
                    break;
                case "attributeslink":
                    attributesLink.click();
                    logger.info("APM Help: Attributes Setting - " + linkName + " is clicked");
                    break;
                case "agentslink":
                    agentsLink.click();
                    logger.info("APM Help: Agents Setting - " + linkName + " is clicked");
                    break;
                case "securitylink":
                    securityLink.click();
                    logger.info("APM Help: Security Setting - " + linkName + " is clicked");
                    break;
                case "alertslink":
                    alertsLink.click();
                    logger.info("APM Help: Alerts Setting - " + linkName + " is clicked");
                    break;
                case "notificationslink":
                    notificationsLink.click();
                    logger.info("APM Help: Notifications Setting - " + linkName + " is clicked");
                    break;
                default:
                    logger.warn("APM Help: No link name: " + linkName + " is provided!!");
                    break;
                  
            }               
            Thread.sleep(delay);
            
        } catch (NoSuchElementException e) {
            logger.warn(linkName + " is missing. Error: " + e.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }
    }    
    
    public void clickHomepage() throws InterruptedException {
        logger.info("Clicking on left menu - Experience View...");
        try {
            experienceView.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            logger.warn("Experience View missing. Error: " + e.getMessage());
        }   
    }
    
    public boolean isViewPresent(String expectedView) {

        logger.info("Checking View {} exists...", expectedView);

        try {
            driver.findElement(By.xpath("//div[contains(text(),'"
                + expectedView + "')]"));
            return true;
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        
        logger.info("Unable to find View/Button/Text with name {}.", expectedView);
        return false;
    }

    public boolean isSettingPresent(String expectedSetting) {

        logger.info("Checking Setting {} exists...", expectedSetting);

        try {
            driver.findElement(By.xpath("//*[contains(text(),'"
                + expectedSetting + "')]"));
            return true;
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        
        logger.info("Unable to find Setting/Button/Text with name {}.", expectedSetting);
        return false;
    }

    public boolean isInstructionPresent(String expectedInstruction) {

        logger.info("Checking if Instruction - {} - exists...", expectedInstruction);

        try {
            driver.findElement(By.xpath("//h1[contains(text(),'"
                    + expectedInstruction + "')]"));
            return true;
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        
        logger.info("Unable to find Setting/Button/Text with name {}.", expectedInstruction);
        return false;
    }
    
    public void clickAgentsView() throws InterruptedException {
        logger.info("Clicking on left menu - Agents View...");
        try {
            agentsView.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            logger.warn("Agent View missing. Error: " + e.getMessage());
        }
    }

    public void clickMapView() throws InterruptedException {
        logger.info("Clicking on left menu - Map View...");
        try {
            mapView.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            logger.warn("Map View missing. Error: " + e.getMessage());
        }    
    }

    public void clickDashboard() throws InterruptedException {
        logger.info("Clicking on left menu - Dashbaord ...");
        try {
            dashboardView.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            logger.warn("Dashboard View missing. Error: " + e.getMessage());
        }    
    }
    
    public void clickPerspectives() throws InterruptedException {
        logger.info("Clicking on left menu - Perspective ...");
        try {
            perspectiveLink.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            logger.warn("Perspective Link is missing. Error: " + e.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }
    }

    public void clickUniverses() throws InterruptedException {
        logger.info("Clicking on left menu - Universes ...");
        try {
            universesLink.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            logger.warn("Universes Link is missing. Error: " + e.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }    
    }

    public void clickAttributes() throws InterruptedException {
        logger.info("Clicking on left menu - Attributes ...");
        try {
            attributesLink.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            logger.warn("Attributes Link is missing. Error: " + e.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }    
    }
    
    public void clickAgents() throws InterruptedException {
        logger.info("Clicking on left menu - Agents ...");
        try {
            agentsLink.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            logger.warn("Agents Link is missing. Error: " + e.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }    
    }

    public void clickSecurity() throws InterruptedException {
        logger.info("Clicking on left menu - Security ...");
        try {
            securityLink.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            logger.warn("Security Link is missing. Error: " + e.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }    
    }
    
    public void clickAlerts() throws InterruptedException {
        logger.info("Clicking on left menu - Alerts ...");
        try {
            alertsLink.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            logger.warn("Alerts Link is missing. Error: " + e.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }    
    }
  
    public void clickNotifications() throws InterruptedException {
        logger.info("Clicking on left menu - Notifications ...");
        try {
            notificationsLink.click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e1) {
            logger.warn("Notifications Link is missing. Error: " + e1.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }    
    }
    
    public void clickExperienceDropDown() throws InterruptedException {
        logger.info("Clicking Drop-down image at top right ...");
        try {
            experienceDropDown.click();
            logger.info("Clicked Drop-down image!");
            Thread.sleep(delay);
        } catch (NoSuchElementException e1) {
            logger.warn("'Top right drop-down image' is missing. Error: " + e1.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("'Top right drop-down image' element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }    
    }
    
    public void clickDemoApplications () throws InterruptedException {
        logger.info("Clicking 'Demo Application' in top right drop-down ...");
        try {
            demoApplications.click();
            logger.info("Clicked 'Demo Application'!");
            Thread.sleep(delay);
        } catch (NoSuchElementException e1) {
            logger.warn("'Demo Application' in top right drop-down is missing. Error: " + e1.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("'Demo Application' element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        } catch (StaleElementReferenceException e3) {
            logger.warn("'Demo Application' element element is no longer appearing on the DOM page: " + e3.getMessage());
        }    
    
    }

    public void clickYourApplications () throws InterruptedException {
        logger.info("Clicking 'Your Application' in top right drop-down ...");
        try {
            yourApplications.click();
            logger.info("Clicked 'Your Application'!");
            Thread.sleep(delay);
        } catch (NoSuchElementException e1) {
            logger.warn("'Your Application' in top right drop-down is missing. Error: " + e1.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("'Your Application' element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }    
    }
    
    public void clickAllMyUniverses () throws InterruptedException {
        logger.info("Clicking 'All My Universes' in top right drop-down ...");
        try {
            allMyUniverses.click();
            logger.info("Clicked 'All My Universes'!");
            Thread.sleep(delay);
        } catch (NoSuchElementException e1) {
            logger.warn("'ALL MY UNIVERSES' in top right drop-down is missing. Error: " + e1.getMessage());
        } catch (ElementNotVisibleException e2) {
            logger.warn("'ALL MY UNIVERSES' element is present on the DOM, it is not visible, and so is not able to be interacted with: " + e2.getMessage());
        }    
    }

    public boolean isClicked_ExperienceView(long sleep) throws InterruptedException {
        return attemptToClick(experienceView, 1000, "Experience View");
    }
        
    public boolean isclicked_HelpLink() throws InterruptedException {
        return attemptToClick(helpLink, 1000, "Help Link");
    }
    
    private boolean attemptToClick(WebElement element, long sleep, 
                                   String type) throws InterruptedException {        
        int attempts = 1;
        
        while(attempts <= 5) {
            try {
                logger.info("Clicking on the '{}'... Attempt #{}", type, attempts);
                element.click();
                Thread.sleep(sleep);
                return true;
            }
            catch(Exception e) {
                logger.warn("Error occurred clicking on the '{}': {}; Attempting again...", type, e.getMessage());
                e.printStackTrace();
            }
            Thread.sleep(sleep);
            attempts++;
        }
        
        return false;
    }

    public void clickHelp_Link() throws Exception {
        try {
            int attempts = 0;
            while (attempts < 2) {
                try {
                    Actions builder = new Actions(driver);
                    Thread.sleep(delay);
                    logger.info("APM Help: About to click Help link in the left Menu tab");
                    //helpParentLink
                    builder.moveToElement(parentHelpLink).clickAndHold(parentHelpLink).moveToElement(parentHelpLink).click().build().perform();
                    Thread.sleep(3000);
                    builder.moveToElement(helpLink).clickAndHold(helpLink).moveToElement(helpLink).click().build().perform();
                    logger.info("APM Help: finished click Help link in the left Menu tab.");
                    Thread.sleep(delay);
                 
                    break;
                } catch (StaleElementReferenceException e) {
                    logger.warn("APM Help: 'Help Link' element is no longer appearing on the DOM page - " + e.getMessage());
                }
                attempts++;
            }
        }catch (Exception e) {
            logger.error("APM Help: help element not found.");
            throw(e);
        } 
    }  
    
    public void goToNewTab() throws InterruptedException {
        // get window handlers as list
        logger.info("APM NewTab: Getting all tabs in the browser");
        try {
            //List<String> browserTabs = new ArrayList<String> (driver.getWindowHandles());
            browserTabs = new ArrayList<String> (driver.getWindowHandles());
            // switch to new tab
            Thread.sleep(delay);
            logger.info("APM NewTab: Switching to new tab");
            driver.switchTo().window(browserTabs.get(1));
            logger.info("APM NewTab: Switched to new tab!!");
            Thread.sleep(delay);
        } catch (Exception e) {
            logger.error("APM NewTab: No new tab shows up");
        }
    }
    
    public void closeTabAndBackToMain() throws InterruptedException {
        // close tab and get back
        try {
            logger.info("APM NewTab: Closing new tab");
            driver.close();
            Thread.sleep(delay);
            logger.info("APM NewTab: Switching back to Main page");
            driver.switchTo().window(browserTabs.get(0));
            Thread.sleep(delay);
            logger.info("APM NewTab: Back to Main window");
        } catch (Exception e) {
            logger.error("APM NewTab: Cannot close new tab and back to Main page");
        }
    }

    public void clickHelp_Icon() throws Exception {
        try {
            int attempts = 0;
            while (attempts < 2) {
                try {
                    Actions builder = new Actions(driver);
                    Thread.sleep(delay);
                    logger.info("APM Help: About to click Help in the Experience View");
                    builder.moveToElement(helpIcon).clickAndHold(helpIcon).moveToElement(helpIcon).click().build().perform();
                    logger.info("APM Help: finished click Help link in the Experience View");
                    Thread.sleep(delay);
                    
                    break;
                } catch (StaleElementReferenceException e) {
                    logger.warn("'Help Link' element is no longer appearing on the DOM page - " + e.getMessage());
                }
                attempts++;
            }
        }catch (Exception e) {
            logger.error("help element not found.");
            throw(e);
        } 
    }  
    
    
    public List<String> getExperienceCards() throws InterruptedException {

        List<WebElement> cardElements = driver.findElements(By.xpath(xpath_allExperienceCards));
        
        // List of cards
        List<String> cards = new ArrayList<String>();
        
        int i = 0;
        // extract the card texts of each card element
        for (WebElement e : cardElements) {
            // add to cards list
            if (e.isDisplayed()){
                cards.add(e.getText());
                logger.info("Card #{} is - {} ", i , e.getText());   
            }
            i++;
        }
        return cards;
    }

    public boolean isCardExists(String cardName) throws InterruptedException {
        
        logger.info("Checking if experience card - {} - exists...", cardName);
        Thread.sleep(delay);
        try {
            // cardName = 'Your Application'
            driver.findElement(By.xpath("//span[@class='experience-card-heading-link truncate ng-binding'"
                    + " and @title='" + cardName + "']"));
            return true;
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        
        logger.info("Unable to find {} Card/Tile", cardName);
        return false;
    }
    
    public void clickCard(String cardName) throws InterruptedException {
        
        logger.info("Clicking card - {} - ...", cardName);
        try {
            // cardName = 'Your Application'
            driver.findElement(By.xpath("//span[@class='experience-card-heading-link truncate ng-binding'"
                    + " and @title='" + cardName + "']")).click();
            Thread.sleep(delay);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        logger.info("Clicked {} Card/Tile", cardName);
    }
    
    public boolean attemptToFindElement(String xpath, long sleep) {
        return attemptToFindElement(xpath, sleep, 10);
    }
    
    public boolean attemptToFindElement(String xpath, long sleep, int maxAttempts) {
        
        int attempts = 1;
        
        while(attempts <= maxAttempts) {
            try {
                logger.info("Trying to find element via xpath '{}'... Attempt #{}", xpath, attempts);
                driver.findElement(By.xpath(xpath));
                Thread.sleep(sleep);
                return true;
            }
            catch(Exception e) {
                logger.warn("Error occurred trying to find element via xpath '{}': {}; Attempting again...", xpath, e.getMessage());
                e.printStackTrace();
            }
            
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            attempts++;
        }
        
        return false;
    }
    
    public boolean attemptToClick(String xpath, long sleep) {   
        return attemptToClick(xpath, sleep, 10);
    }
    
    public boolean attemptToClick(String xpath, long sleep, int maxAttempts) {        
        int attempts = 1;
        
        while(attempts <= maxAttempts) {
            try {
                logger.info("Clicking on the element with xpath '{}'... Attempt #{}", xpath, attempts);
                WebElement element = driver.findElement(By.xpath(xpath));  
                element.click();
                Thread.sleep(sleep);
                return true;
            }
            catch(Exception e) {
                logger.warn("Error occurred clicking on element with xpath '{}': {}; Attempting again...", 
                    xpath, e.getMessage());
                e.printStackTrace();
            }
            
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            attempts++;
        }
        
        return false;
    }
}   