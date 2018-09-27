package com.ca.apm.saas.pagefactory;

import java.util.List;
import java.util.regex.*;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kurma05
 *
 */
public class MapPage {
    private static final Logger logger = LoggerFactory.getLogger(MapPage.class);

    WebDriver driver;
    int waitSeconds = 3000;
    int delay = 10;

    @FindBy(id = "mapview")
    WebElement mapView;

    @FindBy(id = "performance-overview-container")
    WebElement metricsOverview;
    
    @FindBy(xpath = ("//*[contains(@class,'perspective-dropdown dropdown')] "))
    WebElement perspectivesDropDown;
    
    @FindBy(xpath = ("//*[contains(@class,'layer-dropdown dropdown')] "))
    WebElement layerDropDown;
    
    @FindBy(id="time-range-selection-combo" )
    WebElement timeRangeDropDown;

    @FindBy(xpath=("//*[@class='ng-binding'][contains(text(),'Last 8 Mins')]"))
    WebElement liveTimeRange;
  
    @FindBy(id="bottomDrawerMinimizeIcon")
    WebElement collapseBusinessTxns;
    
    @FindBy(xpath=("//div[@class='map-control map-control-reload']"))
    WebElement reloadMap;
    
    @FindBy(xpath=("//*[ @class='global-filter-show-entry active')]"))
    WebElement showEntryPoint;
    
    @FindBy(xpath=("//span[@class='ng-scope'][contains(text(),'Show filters')]"))
    WebElement showFilters;
    
    @FindBy(xpath=("  //div[@title ='Remove this filter']"))
    WebElement removeThisFilter;

    @FindBy(xpath=("//*[ @class='global-filter-show-entry inactive')]"))
    WebElement hideEntryPoint;
    
    @FindBy(xpath=("//*[name()='details-panel']//*[name()='attribute-grid']//*[name()='performance-overview']//*[name()='collapsible-attribute-container']//*[name()='ng-include']//button[contains(.,'Load Charts')]"))    
    WebElement loadCharts;

    // Added 2 elements below since with current functionality "generic Front-end"
    // button is not visible by default, need to expand it
    @FindBy(xpath = ("//*[@heading='Generic Front-end']"))
    WebElement genericFrontEnd;

    @FindBy(xpath = ("//*[@class='side-panel-title']"))
    WebElement nodeSidePanel;
    
    public MapPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    public boolean clickLayer(String layer) throws InterruptedException {
        
        int attempts = 1;
        
        while(attempts <= 5) {
            try {
                logger.info("Clicking on layer '{}'... Attempt #{}", layer, attempts);
                layerDropDown.click();                
                WebElement element = driver.findElement(
                    By.xpath("//span[@class='ng-binding'][starts-with(text(),'" + layer + "')]")); 
                element.click();
                Thread.sleep(waitSeconds);
                return true;
            }
            catch(Exception e) {
                logger.warn("Error occurred while selecting '{}' layer: {}", layer, e.getMessage());
            }
            Thread.sleep(10000);
            attempts++;
        }
        
        return false;
    }

    public boolean clickInfrastructureLayer() throws InterruptedException {

        logger.info("Selecting 'Infrastructure layer'...");
        return clickLayer("Infrastructure Layer");
    }
    
    public boolean clickApplicationLayer() throws InterruptedException {
        
        logger.info("Selecting 'Application Layer'...");
        return clickLayer("Application Layer");
    }
    
    public boolean clickNoPerspective() throws InterruptedException {
        return clickPerspective("No Perspective");
    }
        
    public void clickPerspectiveList() throws InterruptedException {

        logger.info("Clicking on Perspectives list...");
        perspectivesDropDown.click();
        Thread.sleep(waitSeconds);
    }
    
    public void createDockerHostPerspective() throws InterruptedException {
        
        logger.info("Creating new perspective...");
        perspectivesDropDown.click();
        
        //click 'Create a Perspective' button
        WebElement createPerspectivesButton = driver.findElement(
            By.xpath("//*[@class='ng-scope'][contains(text(),'Create a Perspective')]")); 
        createPerspectivesButton.click();
        Thread.sleep(waitSeconds);
        
        //select entries
        new Select(driver.findElement(
            By.xpath("//div[@class='ng-isolate-scope']/div[1]/select")))
                .selectByVisibleText("Docker object");
        new Select(driver.findElement(
            By.xpath("//div[@class='ng-isolate-scope']/div[2]/select")))
                .selectByVisibleText("Host object");
        Thread.sleep(waitSeconds);
        
        //save        
        WebElement saveButton = driver.findElement(
            By.xpath("//span[@class='ng-scope'][contains(text(),'Save')]")); 
        saveButton.click(); 
        Thread.sleep(waitSeconds);
    }
    
    public boolean clickPerspective(String perspective) throws InterruptedException {
        
        int attempts = 1;
        
        while(attempts <= 5) {
            try {
                logger.info("Clicking on perspective '{}'... Attempt #{}", perspective, attempts);
                perspectivesDropDown.click();                
                WebElement element = driver.findElement(
                    By.xpath("//span[@class='ng-binding'][contains(text(),'" + perspective + "')]")); 
                element.click();
                Thread.sleep(waitSeconds);
                return true;
            }
            catch(Exception e) {
                logger.warn("Error occurred while selecting '{}' perspective: {}", perspective, e.getMessage());
            }
            Thread.sleep(10000);
            attempts++;
        }
        
        return false;
    }
    
    public boolean clickFitAll() throws InterruptedException {
        
        WebElement element = driver.findElement(
            By.xpath("//img[@class='map-control'][@title='Fit All To View']"));        
        return attemptToClick(element, 5000, "Fit All To View");
    }
    
    public boolean clickMapView(long sleep) throws InterruptedException {
        return attemptToClick(mapView, 10000, "Map View");
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
    
    public boolean attemptToFindElement(String xpath, long sleep) {
        return attemptToFindElement(xpath, sleep, 5);
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

    public void clickLiveTimeRange() throws InterruptedException {

        logger.info("Selecting Live time range ...");
        timeRangeDropDown.click();
        liveTimeRange.click();
        Thread.sleep(waitSeconds);
    }
   
    public void clickReloadMap() {
        
        logger.info("Trying to refresh map..");
        try {
            reloadMap.click();
            Thread.sleep(waitSeconds);
        } catch (Exception e) {
            logger.info("Reload map button isn't available. Error: " + e.getMessage());
        }
    }
    
    public boolean clickComponentViewTab(String nodeName, String tabName) throws InterruptedException {
        
        long startTime = System.currentTimeMillis();
        long maxWaiting = 300000; 
        
        while((System.currentTimeMillis() - startTime) < maxWaiting) {
            logger.info("Waiting for '{}' tab to appear for node {}... Max {} ms.", tabName, nodeName, maxWaiting);
            if(!clickNode(nodeName)) {
                continue;
            }
            
            // Below click is added since "Generic Front-end" tab is not visible by default, 
            // we need to expand it to view further details
            nodeSidePanel.click();
            
            if(clickComponentViewTab(tabName)) {
                return true;
            }
            Thread.sleep(20000);
        }
        
        return false;
    }
    
    public boolean clickComponentViewTab(String tabName) throws InterruptedException {

        logger.info("Clicking on '{}' tab...", tabName);
        try {            
            WebElement element = driver.findElement(
                    By.xpath("//*[@class='nav-link ng-binding'][contains(text(),'" + tabName + "')]"));            
            JavascriptExecutor js = (JavascriptExecutor)driver;
            js.executeScript("arguments[0].click();", element);
            
            Thread.sleep(waitSeconds);
            return true;
        } catch (NoSuchElementException e) {
            logger.warn("'{}' tab missing. Error: {}", tabName, e.getMessage());
        }
        return false;
    }
    
    public boolean waitToClickNode(String title) throws InterruptedException {
        
        long startTime = System.currentTimeMillis();
        long maxWaiting = 180000;//3 min
        
        while((System.currentTimeMillis() - startTime) < maxWaiting) {
            logger.info("Waiting for '{}' node to appear... Max {} ms.", title, maxWaiting);
            if(clickNode(title)) {
               return true;
            }
            clickReloadMap();
            Thread.sleep(20000);
        }
        
        return false;
    }

    public boolean clickNode(String title) throws InterruptedException {

        logger.info("Clicking node element with title {}...", title);
        
        
        String subStringNode, subStringTitle = title.toLowerCase();
        Matcher nodeRegex;

        List<WebElement> nodes =
            driver.findElements(By.xpath("//*[@class='nodeSelector graph-node']"));

        // Create a regex for searching a specific node with "title"
        subStringTitle = "(.*)(" + subStringTitle + ")(.*)";

        Pattern appNamePattern = Pattern.compile(subStringTitle);

        for (WebElement w : nodes) {
            subStringNode = w.getAttribute("textContent").toLowerCase();

            nodeRegex = appNamePattern.matcher(subStringNode);

            try {
                // If pattern matches and it does not contain "view",
                // considering "node" with view does not have "Agent" tab
                if (nodeRegex.matches() && !subStringNode.contains("view")) {
                    logger.info("Found node with text: " + subStringTitle);
                    w.click();
                    Thread.sleep(waitSeconds);
                    return true;
                }
            } catch (Exception e) {
                logger.warn("Error occurred: " + e.getMessage());
            }
        }
        
        logger.warn("Unable to find any nodes with title {}.", title);
        return false;
    }
    
    public boolean isMetricPresent(String metricName) {

        logger.info("Checking metric {} exists...", metricName);
        return attemptToFindElement("//*[text()[contains(.,'" + metricName + "')]]", 5000);       
    }
    
    public boolean isAlertPresent(String name) {

        logger.info("Checking alert '{}' exists...", name);        
        WebElement alerts =
            driver.findElement(By.xpath("//*[@id='alert-list-container']/div/div[2]"));

        try {
            if(alerts.getText().contains(name)) {
                logger.info("Found alerts '{}'", alerts.getText());
                Thread.sleep(waitSeconds);
                return true;
            }
        }
        catch(Exception e) {
            logger.warn("Error occurred: " + e.getMessage());
        }
        
        logger.warn("Unable to find alert with name '{}'", name);
        return false;
    }
    
    public boolean addNewMapFilter(String filterType, String[] options) throws InterruptedException {

        logger.info("Adding '{}' filter on the map...", filterType);

        WebElement filterButton =
            driver.findElement(By
                .xpath("//*[contains(@class,'filter-list-toggle envelope dropdown-toggle')]/a"));
        filterButton.click();
        Thread.sleep(waitSeconds * 2);

        return addMapFilter(filterType, options);
    }
    
    public boolean addAdditionalMapFilter(String filterType, String[] options) throws InterruptedException {
        
        logger.info("Adding '{}' filter on the map...", filterType);
        
        WebElement filterButton = driver.findElement(
            By.xpath("//a[@class='dropdown-toggle btn-add']"));
        filterButton.click();
        Thread.sleep(waitSeconds*2);
        
        return addMapFilter(filterType, options);
    }
    
    private boolean addMapFilter(String filterType, String[] options) throws InterruptedException {
        
        WebElement typeElement = driver.findElement(
            By.xpath("//span[@class='truncate ng-binding'][contains(text(),'" + filterType + "')]"));
        typeElement.click();
        Thread.sleep(waitSeconds);
        
        WebElement typeDropDownElement = driver.findElement(
            By.xpath("//*[@class='combo-filter-btn-text ng-binding'][contains(text(),'" + filterType + "')]"));
        typeDropDownElement.click();
        Thread.sleep(waitSeconds);
        
        WebElement selectAllElement = driver.findElement(
            By.xpath("//span[@class='combo-filter-item-text truncate ng-binding'][contains(text(),'Select All')]"));
        selectAllElement.click();
        Thread.sleep(waitSeconds);
        
        for(String option: options) {
            logger.info("Filtering out by {} '{}' on the map...", filterType, option);
            WebElement customOption = driver.findElement(
                By.xpath("//span[@title='" + option + "']"));
            customOption.click();
        }
        
        logger.info("Saving filters...");        
        return clickOK();
    }
    
    public boolean setMapFilter(String filterType, String[] options) throws InterruptedException{
    	WebElement typeDropDownElement = driver.findElement(
                By.xpath("//*[@class='combo-filter-btn-text ng-binding'][contains(text(),'" + filterType + "')]"));
            typeDropDownElement.click();
            Thread.sleep(waitSeconds);
            
            WebElement selectAllCheckbox = driver.findElement(
                    By.xpath("//div[contains(@class,'t-select-all-cb custom-checkbox ng-isolate-scope')]"));
            
            if(!selectAllCheckbox.isSelected()) selectAllCheckbox.click();
            selectAllCheckbox.click();
            Thread.sleep(waitSeconds);
            
            for(String option: options) {
                logger.info("Filtering out by {} '{}' on the map...", filterType, option);
                
                WebElement filterForm = driver.findElement(By.xpath("//input[@placeholder='Filter']"));
                filterForm.sendKeys(option);
                Thread.sleep(waitSeconds);
                
                WebElement customOption = driver.findElement(
                    By.xpath("//span[@title='" + option + "']"));
                customOption.click();
                filterForm.clear();
            }
            
            logger.info("Saving filters...");        
            return clickOK();
    }
    
    private boolean clickOK() {

        logger.info("Trying to find 'OK' button in drop-down...");        
        List<WebElement> elements =
            driver.findElements(By.xpath("//span[@class='ng-binding ng-scope'][contains(text(),'OK')]"));

        for (WebElement element : elements) {
            try {
                element.click();
                logger.info("Clicked 'OK' button.");
                return true;
            }
            catch (ElementNotVisibleException e) {
                logger.warn("Found invisible 'OK' button; error: " + e.getMessage());
                logger.info("Trying to find another visible 'OK' button...");
            }
        }
        
        logger.warn("Unable to find OK button.");
        return false;
    }
    
    public boolean clickAttribute(String attribute) throws Exception {

        logger.info("Clicking on {} ...", attribute);
        
        long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) < 300000) {
            try {
                WebElement element = driver.findElement(
                    By.xpath("//span[@class='ng-binding'][contains(text(),'"
                    + attribute + "')]"));
                element.click(); 
                return true;
            }
            catch (Exception e) {
                if(e.getMessage().contains("is not clickable at point")) {
                    logger.info("Waiting for attribute {} to become clickable...", attribute) ;
                    Thread.sleep(30000);
                }
                else {
                    throw new Exception(e);
                }
            }
        }
        
        logger.warn("Timeout while waiting for attribute {} to become clickable.", attribute);
        return false;
    }
        
    public boolean expandMapNode() throws InterruptedException {

        logger.info("Expanding node on the map...");
      
        int attempts = 1;
        int maxAttempts = 60;
        
        while(attempts <= maxAttempts) {
            try {
                logger.info("Trying to expand node group... Attempt #{}", attempts);
                WebElement element = driver.findElement(
                    By.xpath("//*[@id='GroupExpand']"));                
                element.click();
                Thread.sleep(10000);
                return true;
            } catch (Exception e) {
                logger.error("Error while expanding a map node: " + e.getMessage());
                e.printStackTrace();
            }
            Thread.sleep(10000);
            attempts++;
        }
        
        return false;
    }
    
    /*
     * Transaction Traces
     */
    
    public boolean openBusinessTxns() throws InterruptedException {
        
        int attempts = 1;
        
        while(attempts <= 5) {
            try {
                logger.info("Opening Business Transactions...Attempt #{}", attempts);
                
                String xpath = "//div[@id='bottomDrawerMaximizeButton']/div/span[contains(text(),'Business Transactions')]";
                WebElement element = driver.findElement(By.xpath(xpath));
                
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].click();", element);
                Thread.sleep(waitSeconds*2);
                return true;
            }
            catch(Exception e) {
                logger.warn("Error occurred opening Business Transaction: {}; Attempting again...", e.getMessage());
                e.printStackTrace();
            }
            Thread.sleep(10000);
            attempts++;
        }
        
        return false;
    }
    
    public void collapseBusinessTxns() throws InterruptedException {

        logger.info("Closing Business Transactions...");
        collapseBusinessTxns.click();
        Thread.sleep(waitSeconds);
    }
    
    public void openTraceView(String tabName) throws InterruptedException {
        
        logger.info("Opening trace '{}' view...", tabName);
        WebElement element = driver.findElement(
            By.xpath("//span[@class='ng-scope'][contains(text(),'" + tabName + "')]"));
        element.click();
        Thread.sleep(waitSeconds);
    }

    public boolean expandTreeView() {
        
        logger.info("Expanding tree view ...");
        
        try {
            WebElement element = driver.findElement(
                By.xpath("//div[@class='ui-grid-tree-base-row-header-buttons ng-scope ui-grid-icon-plus-squared']"));
            element.click();
            Thread.sleep(waitSeconds);
            return true;
        }
        catch (Exception e) {
            logger.warn("Error occurred trying to expand tt tree view: " + e.getMessage());            
        }
        
        return false;
    }
    
    public boolean colapseTreeView() {
        
        logger.info("Colapsing tree view ...");
        
        try {            
            String xpath = "//div[@class='ui-grid-tree-base-row-header-buttons ng-scope ui-grid-icon-minus-squared']";
            WebElement element = driver.findElement(By.xpath(xpath));
            element.click();
            Thread.sleep(waitSeconds);
            return true;
        }
        catch (Exception e) {
            logger.warn("Error occurred trying to colapse tt tree view: " + e.getMessage());           
        }
        
        return false;
    }
 
    public boolean isTracePropertyPresent(String key, String value) {

        logger.info("Checking property {}={} exists...", key, value);
        String xpath = "//li[span/text()='" + key + ":'][span[contains(text(),'" + value + "')]]";
        
        try {
            driver.findElement(By.xpath(xpath));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        logger.info("Validation failed for property {}={}.", key, value);
        return false;
    }
    
    public boolean isTraceSummaryPresent(String value) {
        
        logger.info("Checking summary exists for '{}'...", value);        
        String xpath = "//*[@class='ng-binding ng-scope'][contains(text(),'" + value + "')]";
        
        try {
            driver.findElement(By.xpath(xpath));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        logger.info("Trace summary validation failed for '{}'", value);
        return false;
    }
    
    public boolean openLastTrace(String traceType) {
        
        try {
            //filter
            String dropDownValue = "Other";
            if(traceType.equalsIgnoreCase("Error")) {
                dropDownValue = "Error";
            }
            else if(traceType.equalsIgnoreCase("Stall")) {
                dropDownValue = "Stall";
            }
            
            new Select(driver.findElement(By.xpath("//select[@placeholder='Filter for column']"))).selectByVisibleText(dropDownValue);
            Thread.sleep(waitSeconds); 
             
            //open latest trace        
            logger.info("Opening {} trace...", traceType);
       
            //open latest trace
            WebElement element = driver.findElement(
                By.xpath("//div[@class='ui-grid-cell-contents ng-binding ng-scope']"));
            
            new WebDriverWait(driver, 20).until(ExpectedConditions.elementToBeClickable(element));         
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();",element);
            Thread.sleep(waitSeconds); 
            return true;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
        
    public void expandTraceComponents(String segment) throws InterruptedException {
      
        logger.info("Expanding trace components for {}", segment);
        
        WebElement element = driver.findElement(By.xpath
            ("//div[contains(@class, 'tt-segment-components-header') and contains(@title, '" + segment + "')]/div/img"));
        element.click();
        Thread.sleep(waitSeconds);
    }
    
    public void selectTraceComponent(String componentPath) throws InterruptedException {
        
        logger.info("Selecting trace for '{}'...", componentPath);
        WebElement element = driver.findElement(
            By.xpath("//div[contains(@title,'" + componentPath + "')]/div[@class='tt-segment-name']"));
        element.click();
        Thread.sleep(waitSeconds);
    }
    
    public void clickShowFilters() {
        try {
            logger.info("Checking if 'Show filters' tab exists, if it's, click it otherwise wait for 10 seconds and then skip the step ...");
            new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(showFilters)).click();
            logger.info("Successfully clicked 'Show filter' tab");
        } catch (Exception e) {
            logger.info("'Show filters' tab doesn't appear. ... ignoring" );
        }        
    }

    public void removeThisFilter() {
        try {
            logger.info("Checking if 'remove this filter' icon exists, if it's, click it otherwise wait for 10 seconds and then skip the step ...");
            new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(removeThisFilter)).click();
            logger.info("Successfully clicked 'Remove this filter' icon");
        } catch (Exception e) {
            logger.info("'Remove this filter' icon doesn't appear. ... ignoring" );
        }       
    }

    public boolean clickLoadCharts() {
        
        logger.info("Clicking Load Charts button ...");
        int attempts = 0;
        while (attempts < 3) {
            logger.info("Attempt # " + (attempts + 1) + "/3. Retrying every " + delay + " seconds");
            try {      
                new WebDriverWait(driver, delay).until(ExpectedConditions.visibilityOf(loadCharts));                
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].click();",loadCharts);
                Thread.sleep(10000);

                logger.info("Successfully clicked 'Load Charts' button");
                return true;
            } catch (Exception e) {
                logger.warn("Load Charts button doesn't appear ERROR: - {} ", e.getMessage() );
            }
            attempts++;
        }
        return false;
    }

}
    