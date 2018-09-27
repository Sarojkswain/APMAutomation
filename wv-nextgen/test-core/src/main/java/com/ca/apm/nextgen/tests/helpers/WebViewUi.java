package com.ca.apm.nextgen.tests.helpers;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleDeclaration;

import com.ca.tas.role.seleniumgrid.BrowserType;

import static com.ca.apm.nextgen.tests.helpers.SeleniumHelpers.browserType;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.openqa.selenium.logging.LogType.BROWSER;
import static org.testng.Assert.assertEquals;

/**
 * WebView UI handling class.
 */
public class WebViewUi implements AutoCloseable {
    public static final int HUB_PORT = 4444;
    public static final String HUB_PROTOCOL = "http";
    public static final String HUB_PATH = "/wd/hub";

    /**
     * Required browser width for running selenium test
     */
    public static final int MIN_BROWSER_WIDTH = 1280;
    /**
     * Required browser height for running selenium test
     */
    public static final int MIN_BROWSER_HEIGHT = 1024;

    public static final String TIME_CONTROLLER_TIME_WINDOW_COMBOBOX_INPUT_ID
        = "webview-timecontroller-timewindow-combobox-input";
    public static final String TIME_CONTROLLER_RESOLUTION_COMBOBOX_INPUT_ID
        = "webview-timecontroller-resolution-combobox-input";


    private static final Logger log = LoggerFactory.getLogger(WebViewUi.class);
    private static final AtomicLong screenshotId = new AtomicLong(0);

    private WebDriver wd;
    private Actions actions;
    private Navigation navigation;

    public WebViewUi(WebDriver wd) {
        this.wd = wd;
        this.actions = new Actions(wd);
        this.navigation = wd.navigate();
    }

    /**
     * Create {@link WebViewUi} instance using given host as Selenium Hub host and given
     * capabilities browser.
     *
     * @param hubHost      Selenium Hub host
     * @param capabilities desired browser capabilities
     * @return instance of {@link WebViewUi}
     */
    public static WebViewUi create(String hubHost, DesiredCapabilities capabilities) {
        return create(HUB_PROTOCOL, hubHost, HUB_PORT, HUB_PATH, capabilities);
    }

    public static WebViewUi create(String protocol, String hubHost, int hubPort, String path,
        DesiredCapabilities capabilities) {
        try {
            RemoteWebDriver driver = new RemoteWebDriver(new URL(protocol, hubHost, hubPort, path),
                capabilities);
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            driver.manage().window().maximize();
            WebViewUi ui = new WebViewUi(driver);
            //ui.adjustBrowserToMinimalSize();
            return ui;
        } catch (MalformedURLException ex) {
            log.error("Malformed HUB URL", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * This method parses HTML style attribute contents.
     *
     * @param styleAttributeContents string with style attribute's contents
     * @return {@link CSSStyleDeclaration} instance
     * @throws IOException
     */
    public static CSSStyleDeclaration parseCss3StyleAttribute(String styleAttributeContents)
        throws IOException {
        InputSource source = new InputSource(new StringReader(styleAttributeContents));
        CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
        CSSStyleDeclaration decl = parser.parseStyleDeclaration(source);
        return decl;
    }

    public WebDriver getWebDriver() {
        return wd;
    }

    public Actions getActions() {
        return actions;
    }

    public Navigation getNavigation() {
        return navigation;
    }

    public JavascriptExecutor getJavaScriptExecutor() {
        return (JavascriptExecutor) getWebDriver();
    }

    public void back() {
        getNavigation().back();
    }

    /**
     * This method adjusts the size of browser window to be at least
     * {@link WebViewUi#MIN_BROWSER_WIDTH}*{@link WebViewUi#MIN_BROWSER_HEIGHT}.
     */
    public void adjustBrowserToMinimalSize() {
        final WebDriver.Window win = getWebDriver().manage().window();
        final Dimension dim = win.getSize();
        setBrowserSize(Math.max(dim.getWidth(), MIN_BROWSER_WIDTH),
            Math.max(dim.getHeight(), MIN_BROWSER_HEIGHT));
    }

    /**
     * Set Browser size
     *
     * @param width
     * @param height
     */
    public boolean setBrowserSize(int width, int height) {
        WebDriver.Window win = getWebDriver().manage().window();
        Dimension dim = new Dimension(width, height);
        // Set browser size
        win.setSize(dim);
        // Check browser size
        dim = win.getSize();
        log.info("Browser size after adjustment width: {} height: {}", dim.getWidth(),
            dim.getHeight());
        return dim.getWidth() >= MIN_BROWSER_WIDTH && dim.getHeight() >= MIN_BROWSER_HEIGHT;
    }

    /**
     * Takes a screenshot (png) of the browser's current page. The screen shot is
     * saved as a file with prefix <code>"screenshot-"</code> in the directory specified by the
     * property
     * <code>screenshot.directory</code>, in the launch.properties file.
     */
    public void takeScreenShot() {
        takeScreenShot("screenshot-");
    }

    /**
     * Takes a screenshot (png) of the browser's current page. The screen shot is
     * saved as a file with prefix <code>filePrefix</code> in the directory specified by the
     * property
     * <code>screenshot.directory</code>, in the launch.properties file.
     *
     * @param filePrefix
     */
    public void takeScreenShot(String filePrefix) {
        for (; ; ) {
            String fileName = filePrefix + screenshotId.incrementAndGet();
            File destFile = new File(fileName + ".png");
            if (destFile.exists()) {
                // Try again with higher number.
                continue;
            }
            File scrFile = ((TakesScreenshot) getWebDriver()).getScreenshotAs(OutputType.FILE)
                .getAbsoluteFile();
            try {
                FileUtils.copyFile(scrFile, destFile);
                log.info("Screenshot: {}", destFile.getAbsolutePath());
            } catch (IOException e) {
                log.error("Unable to create snapshot! [file] \"{}.png\"", fileName, e);
                throw new RuntimeException(e);
            }
            break;
        }
    }

    /**
     * Wait until WebElement is present given selector
     *
     * @param selector By selector used to locate WebElement
     * @param waitTime wait time amount
     * @param unit     wait time amount unit
     * @return located WebElement
     */
    public WebElement waitForWebElement(By selector, long waitTime, TimeUnit unit) {
        try {
            WebDriverWait wait = new WebDriverWait(getWebDriver(), unit.toSeconds(waitTime));
            return wait.until(ExpectedConditions.presenceOfElementLocated(selector));
        } catch (Throwable e) {
            log.error("waitForWebElement() Failed: Waiting failed to locate [{}]", selector, e);
            takeScreenShot();
            throw new RuntimeException(e);
        }
    }

    public WebElement waitForWebElement(By selector) {
        return waitForWebElement(selector, 1, TimeUnit.MINUTES);
    }

    public WebElement waitForWebElementOrNull(By selector, long waitTime, TimeUnit unit) {
        try {
            WebDriverWait wait = new WebDriverWait(getWebDriver(), unit.toSeconds(waitTime));
            log.debug("Element found fine using [{}]", selector.toString());
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(selector));
            return element;
        } catch (Throwable e) {
            log.debug("waitForWebElement() Failed: Waiting failed to locate [{}]: {}", selector, e);
            return null;
        }
    }

    public WebElement waitForWebElementOrNull(By selector) {
        return waitForWebElementOrNull(selector, 1, TimeUnit.MINUTES);
    }

    /**
     * Wait until WebElement is present given selector
     *
     * @param expectedCondition expected condition
     * @param waitTime          wait time amount
     * @param unit              wait time amount unit
     * @return located WebElement
     */
    public <ConditionResultT> ConditionResultT waitFor(
        ExpectedCondition<ConditionResultT> expectedCondition, long waitTime, TimeUnit unit) {
        try {
            WebDriverWait wait = new WebDriverWait(getWebDriver(), unit.toSeconds(waitTime));
            log.info("Waiting for [{}]", expectedCondition);
            return wait.until(expectedCondition);
        } catch (Throwable e) {
            log.error("waitFor() Failed: Waiting failed to locate [{}]", expectedCondition, e);
            takeScreenShot();
            throw new RuntimeException(e);
        }
    }

    public <ConditionResultT> ConditionResultT waitFor(
        ExpectedCondition<ConditionResultT> expectedCondition) {
        return waitFor(expectedCondition, 1, TimeUnit.MINUTES);
    }

    /**
     * Click webview tab by tabName.
     */
    private void clickWvTab(String tabName) {
        log.debug("Start clickWvTab(\"{}\")", tabName);
        try {
            WebElement tp = getWebElement(By.cssSelector("div[id=\"webview-TabPanel\"]"));
            getWebElement(tp, By.xpath("//*[.='" + tabName + "']")).click();
            log.debug("End method: success. Tab clicked okay. [name] {}", tabName);
            // Try to avoid rendering issues by using delay here so that the page will manage to
            // render itself before anything else happens.
            delay(TimeUnit.SECONDS, 5);
        } catch (Throwable e) {
            takeScreenShot();
            log.error("End method: failure. Tab may not have been selected! [{}]", tabName);
            throw new RuntimeException(e);
        }
    }

    /**
     * Click the HOME tab and validate that a top-level element is
     * present on the tab view.
     */
    public void clickHomeTab() {
        clickWvTab("Home");
    }

    /**
     * Click the CONSOLE tab and validate that a top-level element is
     * present on the tab view.
     */
    public void clickConsoleTab() {
        clickWvTab("Console");
    }

    /**
     * Click the CONSOLE tab and validate that a top-level element is
     * present on the tab view.
     */
    public void clickManagementTab() {
        clickWvTab("Management");
    }

    /**
     * Clicks the INVESTIGATOR tab.
     */
    public void clickInvestigatorTab() {
        clickWvTab("Investigator");
    }

    /**
     * Clicks the TOOLS tab.
     */
    public void clickToolsTab() {
        clickWvTab("Tools");
    }

    public WebElement getWebElement(By selector) {
        return getWebElement(getWebDriver(), selector);
    }

    /**
     * Get page element by search context and selector.
     *
     * @param rootElement search context
     * @param selector    selector
     * @return WebElement
     * @throws RuntimeException wrapping {@link org.openqa.selenium.NoSuchElementException} if
     *                          the element is not found
     */
    public WebElement getWebElement(SearchContext rootElement, By selector) {
        try {
            log.debug("Looking for element using [{}]", selector.toString());
            WebElement element = rootElement.findElement(selector);
            log.debug("Element found fine using [{}]", selector.toString());
            return element;
        } catch (NoSuchElementException e) {
            throw ErrorReport.elementNotFound(log, e, this,
                "Element not found using selector [{1}]", selector.toString());
        }
    }

    public List<WebElement> getWebElements(By selector) {
        return getWebElements(getWebDriver(), selector);
    }

    public List<WebElement> getWebElements(SearchContext rootElement, By selector) {
        return rootElement.findElements(selector);
    }

    public WebElement getWebElementOrNull(By selector) {
        return getWebElementOrNull(getWebDriver(), selector);
    }

    /**
     * This method is similar to
     * {@link WebViewUi#getWebElements(org.openqa.selenium.SearchContext, org.openqa.selenium.By)}
     * but it does not throw an exception if the sought element is not found. Instead, it returns
     * <code>null</code>.
     *
     * @param rootElement search context
     * @param selector    selector
     * @return WebElement or <code>null</code>
     */
    public WebElement getWebElementOrNull(SearchContext rootElement, By selector) {
        try {
            WebElement element = rootElement.findElement(selector);
            log.debug("Element found fine using [{}]", selector.toString());
            return element;
        } catch (NoSuchElementException e) {
            log.debug("Element not found using selector [{}]", selector.toString());
            return null;
        }
    }

    public WebElement getHomeTabChart() {
        WebElement frontEndChart = getWebElement(getWebDriver(), By.id("webview-HomePage-APP-ART"));
        return getWebElement(frontEndChart, By.id("webview-investigator-linechart-container"));
    }

    public WebElement getExportMenuButton(WebElement chart) {
        // hover over chart
        getActions().moveToElement(chart).perform();
        // find menu button within chart container
        WebElement menuButton = getWebElement(chart,
            By.id("webview-investigator-linechart-chart-chartmenu"));
        getActions().moveToElement(menuButton).perform();
        return menuButton;
    }

    public void clickMinMaxMenuButton(WebElement chart) {
        try {
            WebElement menuButton = getExportMenuButton(chart);
            WebElement minMaxButton = getMinMaxButton(menuButton);
            getActions()
                .moveToElement(minMaxButton)
                .click()
                .perform();
            waitFor(ExpectedConditions.stalenessOf(minMaxButton));
        } catch (Throwable e) {
            takeScreenShot();
            String msg = ErrorReport.logExceptionFmt(log, e, "Exception: {0}");
            throw new RuntimeException(msg, e);
        }
    }

    public void clickExportMenuButton(WebElement button) {
        // click button a single time to try and bring up chart menu
        getActions().moveToElement(button).click().perform();
    }

    public WebElement getExportButton(WebElement menuButton) {
        clickExportMenuButton(menuButton);
        // This query is intentionally without context because the menu is not nested in the
        // menuButton WebElement.
        return getWebElement(By.id("webview-investigator-linechart-chart-export-csv"));
    }

    public WebElement getMinMaxButton(WebElement menuButton) {
        clickExportMenuButton(menuButton);
        // This query is intentionally without context because the menu is not nested in the
        // menuButton WebElement.
        return getWebElement(By.id("MinMaxDisplay"));
    }

    public void clickExportButton(WebElement button) {
        clickExportMenuButton(button);
    }

    /**
     * This method finds a button by selector, validates its not disabled and clicks it.
     *
     * @param buttonSelector selector
     */
    public void clickButton(By buttonSelector) {
        clickButton(getWebDriver(), buttonSelector);
    }

    /**
     * This method finds a button by selector, validates it is not disabled and clicks it.
     *
     * @param searchContext  search context
     * @param buttonSelector selector
     */
    public void clickButton(SearchContext searchContext, By buttonSelector) {
        try {
            log.info("Trying to click button specified by {}", buttonSelector.toString());
            WebElement button = getWebElement(searchContext, buttonSelector);
            // Verify button is enabled.
            assertEquals(button.getCssValue("cursor"), "pointer",
                "Cursor style is expected to be 'pointer' for enabled button.");
            // And click it.
            getActions()
                .moveToElement(button)
                .click()
                .perform();
            log.info("Click to button specified by {} performed.", buttonSelector.toString());
        } catch (Throwable e) {
            takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Error clicking button specified by {1}. Exception: {0}",
                buttonSelector.toString());
        }
    }

    /**
     * This method finds a drop-down menu button by selector, validates it is not disabled and
     * clicks it.
     *
     * @param searchContext  search context
     * @param buttonSelector button selector
     */
    public void clickMenuButton(SearchContext searchContext, By buttonSelector) {
        try {
            WebElement button = getWebElement(searchContext, buttonSelector);
            clickButton(button, By.xpath(".//span[text()]"));
        } catch (Throwable e) {
            takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Failed to click on menu button specified by {1}. Exception: {0}",
                buttonSelector.toString());
        }
    }

    public void moveToMenuButton(SearchContext searchContext, By buttonSelector) {
        try {
            WebElement button = getWebElement(searchContext, buttonSelector);
            WebElement buttonText = getWebElement(button, By.xpath(".//span[text()]"));
            getActions()
                .moveToElement(buttonText)
                .perform();
        } catch (Throwable e) {
            takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Failed to move to menu button specified by {1}. Exception: {0}",
                buttonSelector.toString());
        }
    }

    public void clickDialogButton(SearchContext searchContext, By buttonSelector) {
        try {
            WebElement button = getWebElement(searchContext, buttonSelector);
            clickButton(button, By.xpath(".//div[text()]"));
        } catch (Throwable e) {
            takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Failed to click on dialog button specified by {1}. Exception: {0}",
                buttonSelector.toString());
        }
    }

    /**
     * Get current URL of the viewed web page.
     *
     * @return URL as String
     */
    public String getCurrentUrl() {
        return getWebDriver().getCurrentUrl();
    }

    /**
     * Enable or disable the event processing in the 15 second polling loop in WebView.
     * Initially, the event processing is active, of course. Set enable to false to disable the
     * event processing. Set enable to true to re-enable it. Use this after the automation has
     * logged in to WebView. Any browser refresh or logout/login will reset the event processing.
     *
     * @param enable true or false
     */
    public void enablePolling(boolean enable) {
        log.debug("{} event polling", enable ? "Enable" : "Disable");
        getJavaScriptExecutor().executeScript("window.enablePolling(arguments[0]);", enable);
    }

    /**
     * Logs in to WebView located at <code>webViewHost</code> and running at <code>port</code>.
     *
     * @param webViewHost WebView host
     * @param port        WebView port
     * @param login       WebView user login
     * @param password    WebView user password
     * @return <code>true</code> if logged in successfully, otherwise <code>false</code>
     */
    public boolean login(String webViewHost, int port, String login, String password) {
        String url = "http://" + webViewHost + ":" + port + "/#home;tr=0";
        return login(url, login, password);
    }

    /**
     * Webview UI Specific Login code
     * Login using all user-specified values: Url, name, and password.
     */
    public boolean login(String url, String name, String password) {
        return login(url, name, password, true);
    }

    /**
     * Webview UI Specific Login code
     * Login using all user-specified values: Url, name, and password.
     */
    public boolean login(String url, String name, String password, boolean validatePostLogin) {
        return login(url, name, password, true, true, validatePostLogin);
    }

    /**
     * Webview UI Specific Login code
     * Login using all user-specified values: Url, name, and password.
     */
    public boolean login(String url, String name, String password, boolean addParamsToUrl,
        boolean switchToLoginFrame, boolean validatePostLogin) {
        log.debug("login({}, {}, {}) method entry.", url, name, password);

        String urlToLogin;
        if (addParamsToUrl) {
            if (StringUtils.startsWith(StringUtils.substringAfterLast(url, "/"), "#")) {
                urlToLogin = url;
            } else {
                urlToLogin = StringUtils.endsWith(url, "/")
                    ? url + "#home;tr=0" : url + "/#home;tr=0";
            }
        } else {
            urlToLogin = url;
        }

        log.info("Login: \"{}\" [user] \"{}\" [password] \"{}\"", urlToLogin, name, password);
        try {
            final WebDriver webDriver = getWebDriver();
            webDriver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
            webDriver.get(urlToLogin);
            //Login page has iFrame

            if (switchToLoginFrame) {
                webDriver.switchTo().frame("LoginFrame");
            } else {
                webDriver.switchTo().parentFrame();
            }
            WebElement usernameInput = waitForWebElement(By.cssSelector("input[id=\"username\"]"));
            log.info("Login page validation success - username field exists.");

            //Input credentials and submit.

            usernameInput.click();
            usernameInput.clear();
            usernameInput.sendKeys(name);

            WebElement passwordInput = webDriver
                .findElement(By.cssSelector("input[id=\"j_passWord\"]"));
            passwordInput.click();
            passwordInput.clear();
            passwordInput.sendKeys(password);

            webDriver.findElement(By.cssSelector("input[id=\"webview-loginPage-login-button\"]"))
                .click();
            log.info("Submitted credentials and clicked Login button...");

            log.info(
                "switching back to defaultContent because otherwise IE11 will reply with access "
                    + "denied");
            webDriver.switchTo().defaultContent();

            if (validatePostLogin) {
                // validate post-login
                waitForWebElement(By.cssSelector("div[id=\"webview-TabPanel\"]"));
                log.info("Credentials accepted! Validated post-Login page element.");
            }
            log.debug("login(url-name-pwd) method exit.");
            return true;
        } catch (Throwable e) {
            takeScreenShot();
            String msg = ErrorReport.logExceptionFmt(log, e, "Login exception: {0}");
            throw new RuntimeException(msg, e);
        }
    }

    public boolean logout() {
        return logout(true);
    }

    /**
     * Log out of WebView.
     *
     * @param validatePostLogout If this parameter is true then this method will validate that
     *                           the page visited after logout is a login page.
     */
    public boolean logout(boolean validatePostLogout) {
        try {
            WebElement webviewLogoutLink = getWebElement(By.id("webview-logout-link"));
            webviewLogoutLink.click();
            if (validatePostLogout) {
                waitForWebElement(By.cssSelector("input[id=\"username\"]"));
                getWebElement(By.cssSelector("input[id=\"j_passWord\"]"));
                getWebElement(By.cssSelector("input[id=\"webview-loginPage-login-button\"]"));
            }
            return true;
        } catch (Throwable e) {
            takeScreenShot();
            String msg = ErrorReport.logExceptionFmt(log, e, "Logout exception: {0}");
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Select Console tab dashboard by its name.
     *
     * @param dashboardName dasboard name
     */
    public void selectConsoleDashboard(String dashboardName) {
        try {
            WebElement dashBoardCBButton = getWebElement(By.xpath(
                "//input[@id='dashboard-selection-combobox-input']/../following-sibling::td/div"));
            getActions()
                .moveToElement(dashBoardCBButton)
                .click()
                .perform();

            WebElement dashboardItem = getWebElement(
                By.xpath(
                    String.format(Locale.US,
                        "//div/span[@class='webview-Common-ListItem'"
                            + " and starts-with(text(), '%s')]",
                        dashboardName)));
            getJavaScriptExecutor()
                .executeScript("arguments[0].scrollIntoView(true);", dashboardItem);
            getActions()
                .moveToElement(dashboardItem)
                .click()
                .perform();
            delay(TimeUnit.SECONDS, 5);
            log.info("Dashboard '{}' selected.", dashboardName);
        } catch (Throwable e) {
            takeScreenShot();
            String msg = ErrorReport.logExceptionFmt(log, e,
                "Failed to select console dashboard {1}. Exception: {0}", dashboardName);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * This method traverses tree of nodes in Management tab and expands all nodes until the last.
     * The last node is single-clicked.
     *
     * @param nodeIds array of node IDs to expand to get to the final node
     * @return WebElement of the last node
     */
    public WebElement selectTreeNode(String[] nodeIds) {
        return selectTreeNode(nodeIds, 0);
    }

    /**
     * This method traverses tree of nodes in Management tab and expands all nodes until the last.
     * The last node is single-clicked.
     *
     * @param nodeId string representing target node ID
     * @return WebElement of the last node
     */
    public WebElement selectTreeNode(String nodeId) {
        final String[] nodeNames = StringUtils.split(nodeId, '|');
        final String[] nodeIds = new String[nodeNames.length];
        nodeIds[0] = nodeNames[0];
        for (int i = 1; i != nodeNames.length; ++i) {
            nodeIds[i] = nodeIds[i - 1] + "|" + nodeNames[i];
        }
        return selectTreeNode(nodeIds, 0);
    }

    /**
     * Expand each node in array until the
     * last node;  then single click this node
     * to display viewer.
     *
     * @param nodeIds
     * @param curIndex
     * @return web element
     */
    private WebElement selectTreeNodeWorker(String[] nodeIds, int curIndex) {
        if (nodeIds.length == 0) {
            return null;
        }

        WebElement parentNode = waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(String.format(Locale.US, "div[ftid=\"%s\"]", nodeIds[curIndex]))));

        WebElement treeNodeButton = getWebElement(parentNode, By.cssSelector("img:nth-child(2)"));
        scrollIntoView(treeNodeButton);

        List<WebElement> allSpans = getWebElements(parentNode, By.cssSelector("span"));
        WebElement treeNode = allSpans.get(1);

        // determine if parent node has open children

        boolean hasOpenChildren = false;
        try {
            WebElement openChildren = getWebElementOrNull(parentNode,
                By.cssSelector("div [role=group]"));
            if (openChildren != null) {
                String displayAttribute = defaultString(openChildren.getAttribute("style"), "");
                CSSStyleDeclaration decl = parseCss3StyleAttribute(displayAttribute);
                String display = decl.getPropertyValue("display");
                if (display != null && display.equals("block")) {
                    hasOpenChildren = true;
                }
            } else {
                log.debug("No open children on node {}. ", parentNode);
            }
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Failure while trying to determine if node has open children. Exception: {0}");
        }

        // skip click on tree node expand/collapse button if children are already open
        if (hasOpenChildren && curIndex < nodeIds.length - 1) {
            return selectTreeNodeWorker(nodeIds, ++curIndex);

        } else if (curIndex < nodeIds.length - 1) {
            // open tree node if more nodes
            getActions().moveToElement(treeNodeButton).click().build().perform();
            return selectTreeNodeWorker(nodeIds, ++curIndex);
        } else {
            // otherwise click on tree node
            getActions().moveToElement(treeNode).click().build().perform();
            return treeNode;
        }
    }

    /**
     * This method traverses tree of nodes in Management tab and expands all nodes until the last.
     * The last node is single-clicked.
     *
     * @param nodeIds  array of node IDs to expand to get to the final node
     * @param curIndex current index into nodeIds array
     * @return WebElement of the last node
     */
    public WebElement selectTreeNode(String[] nodeIds, int curIndex) {
        try {
            return selectTreeNodeWorker(nodeIds, curIndex);
        } catch (Throwable e) {
            takeScreenShot();
            String msg = ErrorReport.logExceptionFmt(log, e, "Exception: {0}");
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * This method opens Console agent filter lens dialog. This method assumes the current tab is
     * the Console tab.
     *
     * @return WebElement of console filter lens dialog
     */
    public WebElement openConsoleLensFilterDialog() {
        try {
            // Click Console Agent Filter Lens button.

            WebElement lensFilterButton
                = getWebElement(By.id("webview-consolelens-dialog-launch-button"));
            getActions()
                .moveToElement(lensFilterButton)
                .click()
                .perform();

            // Wait for dialog to appear.

            return waitForWebElement(By.id("webview-consolelens-dialog-dialog"));
        } catch (Throwable e) {
            takeScreenShot();
            String msg = ErrorReport.logExceptionFmt(log, e, "Exception: {0}");
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Scroll given element into view using JavaScript executed in the browser.
     *
     * @param element element to be scrolled into view
     */
    public void scrollIntoView(WebElement element) {
        try {
            getJavaScriptExecutor().executeScript("arguments[0].scrollIntoView(true);", element);
        } catch (Throwable e) {
            takeScreenShot();
            String msg = ErrorReport.logExceptionFmt(log, e, "Exception: {0}");
            throw new RuntimeException(msg, e);
        }
    }

    public WebElement waitForLogoutLink(String logoutLinkText) {
        return waitForWebElement(
            By.xpath("//*[@id='webview-logout-link' and text()='" + logoutLinkText + "']"));
    }

    public String getWebElementAttribute(WebElement element, String attribute) {
        return element.getAttribute(attribute);
    }

    public List<LogEntry> getBrowserLogs() {
        try {
            BrowserType browserType = browserType(getWebDriver());
            if (!(browserType.equals(BrowserType.INTERNET_EXPLORER)
                  || browserType.equals(BrowserType.EDGE))
                && getWebDriver().manage().logs().getAvailableLogTypes().contains(BROWSER)) {
                final LogEntries logEntries = getWebDriver().manage().logs().get(BROWSER);
                return IterableUtils.toList(logEntries);
            }
        } catch (Throwable ex) {
            String exMessage = ex.getMessage();
            if (exMessage.matches("^Command not found: POST /session/[^/]+/log(?:/types)?$")) {
                log.warn("This web driver does not support retrieving browser logs.");
            } else {
                ErrorReport
                    .logExceptionFmt(log, ex, "Exception while printing browser console logs: {0}");
            }
        }

        return new ArrayList<>(0);
    }

    @Override
    public void close() throws Exception {
        log.info("Browser console messages:");
        for (LogEntry entry : getBrowserLogs()) {
            log.info("BROWSER: {} {} {}", new Date(entry.getTimestamp()), entry.getLevel(),
                entry.getMessage());
        }

        getWebDriver().quit();
    }

    /**
     * Change time window in WebView from/to Live to/from historical mode.
     *
     * @param tw {@link TimeWindow} enum value
     */
    public void changeTimeWindow(TimeWindow tw) {
        log.info("Setting time window to: {}", tw);

        try {
            WebElement timeWindowCB = getWebElement(
                By.id(TIME_CONTROLLER_TIME_WINDOW_COMBOBOX_INPUT_ID));
            timeWindowCB.click();

            WebElement timeWindow = getWebElement(By.xpath(String
                .format(Locale.ENGLISH,
                    "//div/span[@class='webview-Common-ListItem' and text()='%s']",
                    tw.getText())));
            getActions().moveToElement(timeWindow).click().perform();
        } catch (Throwable e) {
            takeScreenShot();
            String msg = ErrorReport
                .logExceptionFmt(log, e, "Exception while setting time window: {0}");
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Sets time resolution value for the previously selected time window.
     *
     * @param timeResolution time resolution enum parameter
     */
    public void changeTimeResolution(TimeResolution timeResolution) {
        log.info("Setting time resolution to: {}", timeResolution);

        try {
            WebElement timeResolutionInput = getWebElement(
                By.id(TIME_CONTROLLER_RESOLUTION_COMBOBOX_INPUT_ID));
            timeResolutionInput.click();

            WebElement timeResolutionElem = getWebElement(By.xpath(String
                .format(Locale.ENGLISH,
                    "//div/span[@class='webview-Common-ListItem' and text()='%s']",
                    timeResolution.getText())));
            getActions().moveToElement(timeResolutionElem).click().perform();
        } catch (Throwable e) {
            takeScreenShot();
            String msg = ErrorReport
                .logExceptionFmt(log, e, "Exception while setting time resolution: {0}");
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Delay execution for given amount of time.
     *
     * @param timeUnit time unit
     * @param amount   count of time units
     */
    public void delay(TimeUnit timeUnit, long amount) {
        try {
            timeUnit.sleep(amount);
        } catch (InterruptedException e) {
            throw ErrorReport
                .logExceptionAndWrapFmt(log, e, "Sleep got interrupted. Exception: {0}");
        }
    }

    /**
     * Make a checkbox selected/checked.
     *
     * @param webElement checkbox element
     */
    public void checkCheckBox(WebElement webElement) {
        if (!webElement.isSelected()) {
            getActions()
                .moveToElement(webElement)
                .click()
                .perform();
        }
    }

    /**
     * Make a checkbox unselected/unchecked.
     *
     * @param webElement checkbox element
     */
    public void uncheckCheckBox(WebElement webElement) {
        if (webElement.isSelected()) {
            moveAndClick(webElement);
        }
    }

    public void moveAndClick(WebElement webElement) {
        getActions()
            .moveToElement(webElement)
            .click()
            .perform();
    }

    public void clickByJs(WebElement webElement) {
        try {
            getJavaScriptExecutor().executeScript("arguments[0].click();", webElement);
        } catch (Throwable e) {
            takeScreenShot();
            String msg = ErrorReport
                .logExceptionFmt(log, e, "Failed to click onto {1} with JavaScript: Exception: {0}",
                    webElement.toString());
            throw new RuntimeException(msg, e);
        }
    }

    public void clearAndSetInputField(WebElement inputElement, String text) {
        inputElement.clear();
        getActions()
            .moveToElement(inputElement)
            .click()
            .sendKeys(text)
            .perform();
    }

    /**
     * Time resolution value for time window selection.
     *
     * @author Alexander Sinyushkin (sinal04@ca.com)
     *
     */
    /**
     * @author Alexander Sinyushkin (sinal04@ca.com)
     */
    public enum TimeResolution {
        SECONDS_15("15 seconds"),
        SECONDS_30("30 seconds"),
        MINUTE_1("1 minute"),
        MINUTES_2("2 minutes"),
        MINUTES_6("6 minutes"),
        MINUTES_12("12 minutes"),
        MINUTES_24("24 minutes"),
        MINUTES_48("48 minutes"),
        MINUTES_168("168 minutes"),
        HOUR_1("1 hour"),
        HOURS_12("12 hours");

        String text;

        TimeResolution(String text) {
            this.text = text;
        }

        /**
         * @return the text
         */
        public String getText() {
            return text;
        }

    }


    /**
     * @see WebViewUi#changeTimeWindow(com.ca.apm.nextgen.tests.helpers.WebViewUi.TimeWindow).
     */
    public enum TimeWindow {
        LIVE(0, "Live", null),
        EIGHT_MINUTES(1, "8 Minutes", new TimeResolution[]{
            TimeResolution.SECONDS_15,
            TimeResolution.SECONDS_30,
            TimeResolution.MINUTE_1,
            TimeResolution.MINUTES_2}),
        TWENTY_MINUTES(2, "20 Minutes", new TimeResolution[]{
            TimeResolution.SECONDS_15,
            TimeResolution.SECONDS_30,
            TimeResolution.MINUTE_1,
            TimeResolution.MINUTES_2,
            TimeResolution.MINUTES_6}),
        ONE_HOUR(3, "1 Hour", new TimeResolution[]{
            TimeResolution.SECONDS_15,
            TimeResolution.SECONDS_30,
            TimeResolution.MINUTE_1,
            TimeResolution.MINUTES_2,
            TimeResolution.MINUTES_6,
            TimeResolution.MINUTES_12,
            TimeResolution.MINUTES_24}),
        TWO_HOURS(4, "2 Hours", new TimeResolution[]{
            TimeResolution.SECONDS_15,
            TimeResolution.SECONDS_30,
            TimeResolution.MINUTE_1,
            TimeResolution.MINUTES_2,
            TimeResolution.MINUTES_6,
            TimeResolution.MINUTES_12,
            TimeResolution.MINUTES_24,
            TimeResolution.MINUTES_48,
            TimeResolution.HOUR_1}),
        SIX_HOURS(5, "6 Hours", new TimeResolution[]{
            TimeResolution.SECONDS_30,
            TimeResolution.MINUTE_1,
            TimeResolution.MINUTES_2,
            TimeResolution.MINUTES_6,
            TimeResolution.MINUTES_12,
            TimeResolution.MINUTES_24,
            TimeResolution.MINUTES_48,
            TimeResolution.HOUR_1,
            TimeResolution.MINUTES_168}),
        TWELVE_HOURS(6, "12 Hours", new TimeResolution[]{
            TimeResolution.MINUTE_1,
            TimeResolution.MINUTES_2,
            TimeResolution.MINUTES_6,
            TimeResolution.MINUTES_12,
            TimeResolution.MINUTES_24,
            TimeResolution.MINUTES_48,
            TimeResolution.HOUR_1,
            TimeResolution.MINUTES_168}),
        TWENTYFOUR_HOURS(7, "24 Hours", new TimeResolution[]{
            TimeResolution.MINUTES_2,
            TimeResolution.MINUTES_6,
            TimeResolution.MINUTES_12,
            TimeResolution.MINUTES_24,
            TimeResolution.MINUTES_48,
            TimeResolution.HOUR_1,
            TimeResolution.MINUTES_168,
            TimeResolution.HOURS_12}),
        FOURTYEIGHT_HOURS(8, "48 Hours", new TimeResolution[]{
            TimeResolution.MINUTES_2,
            TimeResolution.MINUTES_6,
            TimeResolution.MINUTES_12,
            TimeResolution.MINUTES_24,
            TimeResolution.MINUTES_48,
            TimeResolution.HOUR_1,
            TimeResolution.MINUTES_168,
            TimeResolution.HOURS_12}),
        SIXTY_HOURS(9, "60 Hours", new TimeResolution[]{
            TimeResolution.MINUTES_6,
            TimeResolution.MINUTES_12,
            TimeResolution.MINUTES_24,
            TimeResolution.MINUTES_48,
            TimeResolution.HOUR_1,
            TimeResolution.MINUTES_168,
            TimeResolution.HOURS_12}),
        SEVEN_DAYS(10, "7 Days", new TimeResolution[]{
            TimeResolution.MINUTES_12,
            TimeResolution.MINUTES_24,
            TimeResolution.MINUTES_48,
            TimeResolution.HOUR_1,
            TimeResolution.MINUTES_168,
            TimeResolution.HOURS_12}),
        THIRTY_DAY(11, "30 Days", new TimeResolution[]{
            TimeResolution.HOUR_1,
            TimeResolution.MINUTES_168,
            TimeResolution.HOURS_12}),
        CUSTOM_RANGE(12, "Custom Range", null);

        int index;
        String text;
        TimeResolution[] timeResolutions;

        TimeWindow(int index, String text, TimeResolution[] timeResolutions) {
            this.index = index;
            this.text = text;
            this.timeResolutions = timeResolutions;
        }

        public int getIndex() {
            return index;
        }

        public String getText() {
            return text;
        }

        /**
         * @return the timeResolutions
         */
        public TimeResolution[] getTimeResolutions() {
            return timeResolutions;
        }

    }

}
