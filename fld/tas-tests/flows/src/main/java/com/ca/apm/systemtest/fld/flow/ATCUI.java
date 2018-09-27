package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.systemtest.fld.util.selenium.SeleniumClientBase;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.ScreenshotException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * This is utility class that handles ATC web application UI through Selenium and allows
 * create UI queries aimed to Assisted Triage.
 *
 * @author filja01
 */
public class ATCUI extends SeleniumClientBase {
    private static final Logger log = LoggerFactory.getLogger(ATCUI.class);
    
    public static final String INTERFACES_LINK_XPATH = "//A[starts-with(@HREF,'interfaces')]";
    public static final String IF_CHECKBOX_XPATH_FMT = "//td/input[@name='if' and @value='%s']";
    public static final String TIM_WEBAPP_INDEX_URL = "http://%s:%s@%s:%d/cgi-bin/ca/apm/tim/index";
    public static final String SET_BUTTON_XPATH = "//input[@value='Set']";
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    
    public ATCUI(String hostname, int port, String username, String password, WebDriver driver) {
        super(driver);
        this.hostname = hostname;
        this.port = port;
        this.username = StringUtils.defaultString(username, "cemadmin");
        this.password = StringUtils.defaultString(password, "quality");
    }

    /**
     * Login to Homepage
     */
    public void login() {
        getUrl("http://" + hostname + ":" + port + "/ApmServer/#/home");
        doLogin();
    }

    /**
     * Do login in login-screen
     * 
     */
    private void doLogin() {
        // disable welcome screen
        ((RemoteWebDriver) driver).executeScript("window.localStorage.setItem('welcomeObj', JSON.stringify({ skip: true }));");
        
        if (driver.findElements(By.id("LoginFrame")).size() > 0) {
            // we are on login page
            driver.switchTo().frame("LoginFrame");

            WebElement username = driver.findElement(By.id("username"));
            WebElement password = driver.findElement(By.id("j_passWord"));

            username.sendKeys(this.username);
            password.sendKeys(this.password);

            username.submit();
        }
    }
    
    private void waitForPanelToLoad() {
        By locator = By.cssSelector(
                ".summary-container > .summary-content .work-indicator");
        waitForWorkIndicator(locator);
    }
    
    private void waitForTilesToLoad() {
        By locator =
            By.cssSelector(".home-main-section > .home-main > .experiences-container .work-indicator");
        waitForWorkIndicator(locator);
    }
    
    /**
     * Set some Live range from dropdown menu
     * 
     */
    public void setSomeLiveRange(String threadName) {
        
        for (int i = 3; i < 3; i++) {
            try {
                WebElement timeRangeDropdown = waitUntilVisible(By.id("time-range-selection-combo"),10);
                    //driver.findElement(By.id("time-range-selection-combo"));
                WebElement timeRangeDropdownSelector = driver.findElement(By.id("time-range-selection-combo-menu"));
                
                timeRangeDropdown.click();
                sleep(100);
                
                List<WebElement> allMenuItems = timeRangeDropdownSelector.findElements(By.tagName("li"));
                
                // filter not displayed
                List<WebElement> menuItems = new ArrayList<WebElement>();
                for (WebElement mI : allMenuItems) {
                    if (mI.isDisplayed()) {
                        menuItems.add(mI);
                    }
                }
                int timeRangeSize = menuItems.size();
                if (timeRangeSize <= 0) {
                    log.error("{}: No TimeRange to select.", threadName);
                    return; 
                }
                Random r = new Random();
                int j = r.nextInt(timeRangeSize);
                //log.info("{}: Number of TimeRanges: {}. Selected number: {}", threadName, timeRangeSize, j);
                
                menuItems.get(j).click();
            } catch (Exception e) {
                log.error("{}: Not able to select TimeRange! TRY {}", threadName, i);
                continue;
            }
            break;
        }
        
        waitForPanelToLoad();
        waitForTilesToLoad();
    }
    
    /**
     * Click on the home experience button in the breadcrumb element
     * 
     */
    public void clickOnHomeExperience(String threadName) {
        // scroll up
        JavascriptExecutor js = (JavascriptExecutor)driver;
        js.executeScript("scroll(250, 0)");
        try {
            WebElement levelItem = waitUntilVisible(By.id("home-breadcrumb"), 10);
            WebElement clickBtn = levelItem.findElement(
                By.className("icon-home"));
            //log.info("{}: Click on home button.", threadName);
            clickBtn.click();
        } catch (Exception e) {
            WebElement clickSuperBtn = driver.findElement(By.className("logo-link-block"));
            clickSuperBtn.click();
            log.warn("{}: Click on SUPER home button.", threadName);
        }
        sleep(1000); //wait for refresh
    }
    
    /**
     * Select randomly some Experience from drop-down menu
     * 
     */
    public void clickOnExperienceFromDropdown(String threadName) {
        sleep(1000);  // Wait for start
        WebElement levelItem;

        levelItem = waitUntilVisible(By.className("home-breadcrumb"), 10);
        // open menu
        for (int i = 0; i < 5; i++) {
            try {
                WebElement toggleBtn = levelItem.findElement(
                    By.cssSelector("a[uib-dropdown-toggle]"));
                //log.info("{}: Open experience menu.", threadName);
                toggleBtn.click();
                sleep(2000);  // Wait for menu to open
                // select label
                List<WebElement> allMenuItems = driver.findElements(
                    By.cssSelector("body.uib-dropdown-open > ul[uib-dropdown-menu] > li > a"));
                // filter not displayed
                List<WebElement> menuItems = new ArrayList<WebElement>();
                for (WebElement mI : allMenuItems) {
                    if (mI.isDisplayed()) {
                        menuItems.add(mI);
                    }
                }
                int experienceSize = menuItems.size()-1; //don't count default
                if (experienceSize <= 0) {
                    log.error("{}: No Experience in breadcrumb menu to select.", threadName);
                    return;
                    //throw new IllegalArgumentException(
                    //    "No Experience in breadcrumb menu to select"); 
                }
                Random r = new Random();
                int j = r.nextInt(experienceSize);
                //log.info("{}: Number of experiences: {}. Selected number: {}", threadName, experienceSize, j);
                
                menuItems.get(j+1).click(); //go behind default
            } catch (StaleElementReferenceException e) {
                log.error("{}: Not able to select Experience in breadcrumb menu! TRY {}", threadName, i);
                continue;
            }
            break;
        }
        sleep(2000); // wait after click
    }
    
    /**
     * Wait on element to became visible
     * 
     * @return 
     */
    private WebElement waitUntilVisible(By locator, long duration) {
      FluentWait<WebDriver> wait = getFluentWait(driver, duration);
      WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
      
      return element;
    }
    
    /**
     * 
     * @return 
     */
    private static FluentWait<WebDriver> getFluentWait(WebDriver driver, long duration) {
        FluentWait<WebDriver> wait =
            new FluentWait<WebDriver>(driver).withTimeout(duration, TimeUnit.SECONDS)
                .pollingEvery(500, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
        return wait;
    }
    
    
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Expand AT Panel
     *  
     */
    public void expandATPanel(String threadName) {
        if (isATPanelCollapsed()) {
            log.info("{}: Expanding AT panel...", threadName);
            driver.findElement(By.cssSelector(".at-panel-container .hide-pa-icon")).click();
            sleep(1500);  // waiting for animation
        }
    }
    
    /**
     * Collapse AT Panel
     *  
     */
    public void collapseATPanel() {
        if (isATPanelExpanded()) {
            driver.findElement(By.cssSelector(".at-panel-container .hide-pa-icon")).click();
            sleep(1500);  // waiting for animation
        }
    }
    
    /**
     * Check if AT Panel is expanded
     * 
     * @return
     */
    public boolean isATPanelExpanded() {
        return driver.findElement(By.cssSelector(".at-panel-container .hide-pa-container")).getAttribute("class").contains("open");
    }
    
    /**
     * Check if AT Panel is collapsed
     * 
     * @return
     */
    public boolean isATPanelCollapsed() {
        boolean isCollapsed = true;
        try {
            driver.findElement(By.cssSelector(".at-panel-container .hide-pa-container .close"));
        } catch(NoSuchElementException e) {
            isCollapsed = false;
        }
        
        return isCollapsed;
    }
    
    /**
     * Get AT Panel content
     * 
     * @return
     */
    private WebElement getATPanelContent() {
        WebElement wel = null;
        try {
            wel = waitUntilVisible(
                By.cssSelector(".at-panel-container .problems-anomalies-content"),10);
        } catch (Exception e) {
            
        }
        return wel;
    }
    
    /**
     * Try to select randomly some Problem from AT Panel. It is possible that nothing will be selected. 
     * 
     */
    public void selectSomeProblem(String threadName) {
        //log.info("{}: Select problem.", threadName);
        expandATPanel(threadName);
        
        for (int i = 0; i < 5; i++) {
            try {
                List<WebElement> namesElems = getATPanelContent().findElements(By.className("story-desc"));
                int storySize = namesElems.size();
                if (storySize <= 0) {
                    log.error("{}: No Story in AT Panel to select.", threadName);
                    return;
                    //throw new IllegalArgumentException(
                    //    "No Story in AT Panel to select"); 
                }
                Random r = new Random();
                int j = r.nextInt(storySize);
                
                //log.info("{}: Number of problems: {}. Selected number: {}", threadName, storySize, j);
                
                namesElems.get(j).click();
            } catch (Exception e) {
                log.error("{}: Not able to select problem! TRY{}", threadName, i);
                continue;
            }
            break;
        }
        log.info("{}: Click on problem.", threadName);
        try {
            waitUntilVisible(By.cssSelector(
                ".at-panel-container .story-detail-icon.icon-times:not(.ng-hide)"),10);
        } catch (Exception e) {
            return;
        }
        sleep(1000); // wait after problem
        return;
    }
    
    /**
     * Try to open notebook in expanded problem
     * 
     */
    public void openNotebookInExpandedProblem(String threadName) {
        //log.info("{}: Open notebook.", threadName);
        expandATPanel(threadName);
        
        WebElement extedendedProblem = null;
        for (int i = 0; i < 5; i++) {
            try {
                extedendedProblem = getATPanelContent().findElement(By.cssSelector(".story-detail-icon.icon-times:not(.ng-hide)"));
            } catch(NoSuchElementException e) {
                log.warn("{}: No problem is opened!", threadName);
                return;
            } catch (Exception e) {
                log.error("{}: Error happend during opening of Notebook! TRY {}", threadName, i);
            }
            try {
                WebElement parent = extedendedProblem.findElement(By.xpath("../.."));
            
                WebElement notebookBtn = parent.findElement(By.cssSelector("div:nth-child(2) > div:nth-child(6) > a"));
                notebookBtn.click();
            } catch (Exception e) {
                log.error("{}: Error happend during opening of Notebook! TRY {}", threadName, i);
                continue;
            }
            break;
        }
        
        waitForNotebookLoad();
    }
    
    /**
     * Wait for notebook to load
     * 
     */
    private void waitForNotebookLoad() {
        waitForWorkIndicator(By.cssSelector(".home-detail-map .work-indicator"));
    }
    
    /**
     * Wait for Work indicator
     * 
     */
    private void waitForWorkIndicator(By locator) {
        if (locator == null) {
            return;
        }
        
        try {
            // Wait 1 sec whether the work indicator pops up
            // (it appears with a delay, not immediately)
            waitUntilVisible(locator, 1);
        } catch (TimeoutException e) {
        }

        // Wait while the work indicator is displayed
        try {
            waitWhileVisible(locator, 10);
        } catch (TimeoutException e) {
            
        }
    }
    
    /**
     * Wait while indicator visible
     * 
     */
    private void waitWhileVisible(By locator, long duration) {
//      logger.debug("Start waiting while " + locator + " is visible for max " + duration + " sec");
      FluentWait<WebDriver> wait = getFluentWait(driver, duration);
      wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
//      logger.debug("End waiting while " + locator + " is visible");
  }


    /**
     * Try to select some evidence. When needed, selects Problem and opens Notebook.
     * It is possible that nothing will be selected. 
     * 
     */
    public void clickOnSomeEvidenceInAT(String threadName) {
        //log.info("{}: Try select evidence.", threadName);
        expandATPanel(threadName);
        
        /*if (!isProblemSelected()) {
            log.warn("{}: No problem is selected!", threadName);
            if (!selectSomeProblem(threadName)) {
                return;
            }
            openNotebookInExpandedProblem(threadName);
        }*/
        List<WebElement> evidencesElems = new ArrayList<WebElement>();
        for (int i = 0; i < 5; i++) {
            try {
                evidencesElems = getATPanelContent().findElements(By.className("story-evidence"));
            } catch (Exception e) {
                log.error("{}: Not able to select evidence! TRY {}", threadName, i);
                continue;
            }
        
            int evidencesSize = evidencesElems.size();
            if (evidencesSize <= 0) {
                log.error("{}: No Evidence in AT Panel to select.", threadName);
                //throw new IllegalArgumentException(
                //    "No Evidence in AT Panel to select"); 
                return;
            }
            Random r = new Random();
            int j = r.nextInt(evidencesSize);
            
            //log.info("{}: Number of evidences: {}. Selected number: {}", threadName, evidencesSize, j);
            
            try {
                evidencesElems.get(j).click();
            } catch (StaleElementReferenceException e) {
                log.error("{}: Not able to select evidence! TRY {}", threadName, i);
                continue;
            }
            break;
        }
        sleep(2000); // wait after problem
        
    }
    
    
    private WebElement getHomeMainSectionDiv() {
        return driver.findElement(By
            .cssSelector(".home-main-section .home-main"));
    }
    
    
    public void clickOnSomeExperienceTile(String threadName) {
        waitForTilesToLoad();
        
        //log.info("{}: Try select Experience.", threadName);
        
        for (int i = 0; i < 5; i++) {
            try {
                List<WebElement> tileElements =
                    getHomeMainSectionDiv().findElements(
                        By.cssSelector("div.experience-card-base.experience-card"));
        
                int experSize = tileElements.size();
                if (experSize <= 0) {
                    log.error("{}: No Experience to select.", threadName);
                    return;
                }
                Random r = new Random();
                int j = r.nextInt(experSize);
                
                //log.info("{}: Try select Experience {} from {}.", threadName, j, experSize);
            
                WebElement cardHeadingLinkElement = tileElements.get(j).findElement(By.className("experience-card-heading-link"));
                JavascriptExecutor js = (JavascriptExecutor)driver;
                js.executeScript("arguments[0].scrollIntoView();", cardHeadingLinkElement);
                cardHeadingLinkElement.click();
                //tileElements.get(j).findElement(By.cssSelector("span.exerience-card-heading-link.truncate")).click();
            } catch (NoSuchElementException | ScreenshotException e) {
                log.error("{}: Not able to select experience! TRY {}", threadName, i);
                continue;
            }
            break;
        }
        sleep(2000); // wait after click
    }
}
