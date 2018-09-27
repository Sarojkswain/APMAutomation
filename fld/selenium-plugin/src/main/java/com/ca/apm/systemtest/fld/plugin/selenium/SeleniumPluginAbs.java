/**
 * Selenium plugin common implementation bits base class.
 */

package com.ca.apm.systemtest.fld.plugin.selenium;

import io.github.bonigarcia.wdm.BrowserManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.selenium.Execution.ExecutionStatus;

/**
 * Simple plugin that can open a web browser and navigate to links. Does not yet
 * support tabs or opening in a new window
 *
 * @author keyja01
 */
public abstract class SeleniumPluginAbs extends AbstractPluginImpl implements SeleniumPlugin {

    private static final int IMPLICIT_WAIT_SECONDS = 10;
    private static final AtomicLong nextId = new AtomicLong(1000);
    private static Logger log = LoggerFactory.getLogger(SeleniumPluginAbs.class);
    private Map<String, WebDriver> driverMap = new HashMap<>();
    private Map<String, Execution> executionMap = new HashMap<>();
    private Map<String, SeleniumTest> testMap = new HashMap<>();
    private static long nextExecutionId = System.currentTimeMillis();
    
    protected SeleniumPluginAbs() {}

    protected final long getNextId() {
        return nextId.getAndIncrement();
    }

    private WebDriver getWebDriver(String sessionId) throws SeleniumPluginException {
        WebDriver driver = driverMap.get(sessionId);
        if (driver == null) {
            String msg =
                MessageFormat.format("Driver for session id {0} does not exist", sessionId);
            log.error(msg);
            throw new SeleniumPluginException(msg);
        }

        return driver;
    }

    @ExposeMethod(description = "Opens a URL in a browser session, returns the window ID")
    public String openUrl(String sessionId, String url) throws SeleniumPluginException {
        log.info("in session {}, opening URL {}", sessionId, url);
        WebDriver driver = getWebDriver(sessionId);
        driver.get(url);
        return driver.getWindowHandle();
    }

    @ExposeMethod(description = "Visits URL in given session and window.")
    @Override
    public void visitUrl(String sessionId, String windowId, String url)
        throws SeleniumPluginException {
        log.info("in session {} window {}, visiting URL {}", sessionId, windowId, url);
        WebDriver driver = getWebDriver(sessionId);
        if (windowId != null) {
            driver = driver.switchTo().window(windowId);
        }

        driver.get(url);
    }

    @ExposeMethod(description = "Finds links containing the keyword in the text or href in the specified "
        + "window")
    public List<String> findLinks(String sessionId, String windowId, String keyword)
        throws SeleniumPluginException {
        WebDriver driver = getWebDriver(sessionId);
        List<WebElement> links;
        if (keyword == null || keyword.isEmpty()) {
            links = driver.findElements(By.xpath("//a"));
        } else {
            links =
                driver.findElements(By.xpath("//a[contains(@href,'" + keyword
                    + "') or contains(text(), '" + keyword + "')]"));
        }
        List<String> retval = new ArrayList<>(10);
        for (WebElement e : links) {
            String href = e.getAttribute("href");
            if (href != null) {
                retval.add(href);
            }
        }
        return retval;
    }

    @ExposeMethod(
        description = "Opens a link on the current page.  Returns true if the link is found on "
            + "the page and can be clicked on, false otherwise")
    public boolean openLink(String sessionId, String windowId, String link)
        throws SeleniumPluginException {
        WebDriver driver = getWebDriver(sessionId);
        if (windowId != null) {
            driver.switchTo().window(windowId);
        }

        List<WebElement> links = driver.findElements(By.xpath("//a[@href='" + link + "']"));
        for (WebElement e : links) {
            String href = e.getAttribute("href");
            if (href != null && href.equals(link)) {
                log.info("Clicking on link \"{}\" ({})", e.getText(), href);
                new Actions(driver).moveToElement(e).click().perform();
                return true;
            }
        }

        log.warn("Failed to find link {} in window {} session {}", link, windowId, sessionId);
        return false;
    }

    /**
     * Convert SelectionBy and value to Selenium's By object.
     *
     * @param selectionBy selection criterion
     * @param id criterion value
     * @return new By instance
     * @throws SeleniumPluginException
     */
    public static By makeBy(SelectionBy selectionBy, String id) throws SeleniumPluginException {
        switch (selectionBy) {
            case ID:
                return By.id(id);

            case XPATH:
                return By.xpath(id);

            case NAME:
                return By.name(id);

            case TAG_NAME:
                return By.tagName(id);

            case LINK:
                return By.linkText(id);

            case PARTIAL_LINK:
                return By.partialLinkText(id);

            case CLASS:
                return By.className(id);

            default:
                String msg =
                    MessageFormat.format("Selection type not recognized: {0}", selectionBy);
                log.error(msg);
                throw new SeleniumPluginException(msg);
        }
    }

    /**
     * Fill text field designated by given coordinates with new text.
     *
     * @param sessionId session ID
     * @param windowId window ID
     * @param selectionBy selection criterion
     * @param id selection criterion value
     * @param newText new text
     * @return true on success, false otherwise
     * @throws SeleniumPluginException
     */
    @ExposeMethod(description = "Fills given text field with new text")
    @Override
    public boolean fillTextField(String sessionId, String windowId, SelectionBy selectionBy,
        String id, String newText) throws SeleniumPluginException {
        WebDriver driver = getWebDriver(sessionId);
        if (windowId != null) {
            driver.switchTo().window(windowId);
        }

        By by = makeBy(selectionBy, id);
        WebElement element = driver.findElement(by);
        if (element == null) {
            log.error("Failed to find element {} in window {} session {}", by, windowId, sessionId);
            return false;
        }

        element.clear();
        element.sendKeys(newText);

        return true;
    }

    /**
     * Submit form designated by given coordinates.
     *
     * @param sessionId session ID
     * @param windowId window ID
     * @param selectionBy selection criterion
     * @param id selection criterion value
     * @return true on success, false otherwise
     * @throws SeleniumPluginException
     */
    @ExposeMethod(description = "Fills given text field with new text")
    @Override
    public boolean submitForm(String sessionId, String windowId, SelectionBy selectionBy, String id)
        throws SeleniumPluginException {
        WebDriver driver = getWebDriver(sessionId);
        if (windowId != null) {
            driver.switchTo().window(windowId);
        }

        By by = makeBy(selectionBy, id);
        WebElement element = driver.findElement(by);
        if (element == null) {
            log.error("Failed to find element {} in window {} session {}", by, windowId, sessionId);
            return false;
        }
        element.submit();

        return true;
    }

    /**
     * Click on element designated by given coordinates.
     *
     * @param sessionId session ID
     * @param windowId window ID
     * @param selectionBy selection criterion
     * @param id selection criterion value
     * @return true on success, false otherwise
     * @throws SeleniumPluginException
     */
    @ExposeMethod(description = "Fills given text field with new text")
    @Override
    public boolean click(String sessionId, String windowId, SelectionBy selectionBy, String id)
        throws SeleniumPluginException {
        WebDriver driver = getWebDriver(sessionId);
        if (windowId != null) {
            driver.switchTo().window(windowId);
        }

        By by = makeBy(selectionBy, id);
        WebElement element = driver.findElement(by);
        if (element == null) {
            log.error("Failed to find element {} in window {} session {}", by, windowId, sessionId);
            return false;
        }

        log.info("clicking at {} in window {} session {}", by, windowId, sessionId);
        element.click();

        return true;
    }

    /**
     * Return content of a table.
     *
     * @param sessionId session ID
     * @param windowId window ID
     * @param selectionBy selection criterion
     * @param id selection criterion value
     * @return list of table cells, empty otherwise
     * @throws SeleniumPluginException
     */
    @ExposeMethod(description = "Return content of a table")
    @Override
    public List<String> tableContent(String sessionId, String windowId, SelectionBy selectionBy,
        String id) throws SeleniumPluginException {
        WebDriver driver = getWebDriver(sessionId);
        if (windowId != null) {
            driver.switchTo().window(windowId);
        }

        List<String> retval = new ArrayList<>(10);

        By by = makeBy(selectionBy, id);
        WebElement table = driver.findElement(by);
        if (table != null) {
            List<WebElement> tdlist = table.findElements(By.cssSelector("tr td"));
            for (WebElement el : tdlist) {
                retval.add(el.getText());
            }
        }

        return retval;
    }


    /**
     * Return text of selected elements.
     *
     * @param sessionId session ID
     * @param windowId window ID
     * @param selectionBy selection criterion
     * @param id selection criterion value
     * @return list of string, empty otherwise
     * @throws SeleniumPluginException
     */
    @ExposeMethod(description = "Return text of elements matching selector.")
    @Override
    public List<String> getText(String sessionId, String windowId, SelectionBy selectionBy,
        String id) throws SeleniumPluginException {
        WebDriver driver = getWebDriver(sessionId);
        if (windowId != null) {
            driver.switchTo().window(windowId);
        }

        List<String> retval = new ArrayList<>(10);

        By by = makeBy(selectionBy, id);
        List<WebElement> elements = driver.findElements(by);
        if (elements != null) {
            for (WebElement el : elements) {
                retval.add(el.getText());
            }
        }

        return retval;
    }


    /**
     * Select option from a drop down menu.
     *
     * @param sessionId session ID
     * @param windowId window ID
     * @param selectionBy selection criterion
     * @param id selection criterion value
     * @param option value to select
     * @return true on success, false otherwise
     * @throws SeleniumPluginException
     */
    @ExposeMethod(description = "Select option from a drop down menu")
    @Override
    public boolean selectOption(String sessionId, String windowId, SelectionBy selectionBy,
        String id, String option) throws SeleniumPluginException {
        WebDriver driver = getWebDriver(sessionId);
        if (windowId != null) {
            driver.switchTo().window(windowId);
        }

        By by = makeBy(selectionBy, id);
        WebElement select = driver.findElement(by);
        if (select == null) {
            log.error("Failed to find element {} in window {} session {}", by, windowId, sessionId);
            return false;
        }
        Select dropdown = new Select(select);
        dropdown.selectByValue(option);

        return true;
    }

    @ExposeMethod(description = "Wait for web element to show up.")
    @Override
    @SuppressWarnings("unused")
    public boolean waitForElement(String sessionId, String windowId, SelectionBy selectionBy,
        String id, int timeOutInSeconds) throws SeleniumPluginException {
        WebDriver driver = getWebDriver(sessionId);
        By by = makeBy(selectionBy, id);
        try {
            WebElement myDynamicElement =
                new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions
                    .presenceOfElementLocated(by));
        } catch (TimeoutException e) {
            log.warn("element {} has not appeared within {} seconds", by, timeOutInSeconds);
            return false;
        }
        log.info("element {} has appeared within less than {} seconds", by, timeOutInSeconds);
        return true;
    }

    @ExposeMethod(description = "Accept modal alert dialog.")
    @Override
    public boolean acceptAlert(String sessionId, String windowId) throws SeleniumPluginException {
        WebDriver driver = getWebDriver(sessionId);
        try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (NoAlertPresentException e) {
            log.warn("no alert is present");
            return false;
        }
        return true;
    }

    protected String startSession(WebDriver driver, String prefix) throws SeleniumPluginException {
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS);
        String id = prefix + getNextId();
        driverMap.put(id, driver);
        return id;
    }

    @ExposeMethod(description = "Closes the specified browser session")
    public void closeSession(String sessionid) {
        WebDriver driver = driverMap.remove(sessionid);
        if (driver != null) {
            Set<String> windows = driver.getWindowHandles();
            if (log.isDebugEnabled()) {
                log.debug("Open windows in session {}: {}", sessionid, windows);
            }
            for (String windowId: windows) {
                try {
                    driver.switchTo().window(windowId);
                    driver.close();
                } catch (Exception e) {
                    ErrorUtils.logExceptionFmt(log, e,
                        "Failed to close web driver for {1}, session {2}, window {3}."
                        + " Exception: {0}",
                        driver.getCurrentUrl(), sessionid, windowId);
                }
            }
            try {
                driver.quit();
            } catch (Throwable e) {
                ErrorUtils.logExceptionFmt(log, e,
                    "Failed to quit web driver for {1}, session {2}. Exception: {0}",
                    driver.getCurrentUrl(), sessionid);
            }
        }
    }

    @ExposeMethod(description = "Returns a list of the active browser sessions")
    public List<String> listSessions() {
        return new ArrayList<>(driverMap.keySet());
    }

    @ExposeMethod(
        description = "Execute a compiled Selenium test class from JAR located in tempDir")
    @Override
    public String executeSeleniumTest(final String tempDirName, final String className,
        final Map<String, String> params, boolean async)
        throws SeleniumPluginException {
        if (async) {
            final String executionId = "ex" + nextExecutionId++;
            final Execution execution = new Execution();
            executionMap.put(executionId, execution);
            
            execution.setStatus(ExecutionStatus.New);
            
            // execute in a new thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    execution.setStartTime(System.currentTimeMillis());
                    execution.setStatus(ExecutionStatus.Running);
                    try {
                        executeSeleniumTestInternal(tempDirName, className, params, executionId);
                        execution.setStatus(ExecutionStatus.Finished);
                    } catch (Exception e) {
                        execution.setStatus(ExecutionStatus.Exception);
                    } finally {
                        execution.setEndTime(System.currentTimeMillis());
                    }
                }
            }).start();
            
            return executionId;
        } else {
            executeSeleniumTestInternal(tempDirName, className, params, null);
            return null;
        }
    }


    @Override
    public void shouldStop(String executionId) {
        SeleniumTest tst = testMap.get(executionId);
        if (tst != null) {
            log.info("shouldStop() send to {}", executionId);
            tst.shouldStop();
        } else {
            log.error("ERROR: execution {} does NOT exist", executionId);
        }
    }

    private void executeSeleniumTestInternal(String tempDirName, String className,
        Map<String, String> params, String executionId)  throws SeleniumPluginException {
        File tempDir = getTempDirFile(tempDirName);
        List<URL> urlList = new ArrayList<URL>();
        File tempLibDir = new File(tempDir.getAbsolutePath() + "/lib");
        if (tempLibDir.isDirectory()) {
            createUrl(tempLibDir, urlList);
        } else {
            createUrl(tempDir, urlList);
        }
        try {
            urlList.add(tempDir.toURI().toURL());
        } catch (MalformedURLException e) {
            ErrorUtils.logExceptionFmt(log, e, "Exception: {0}");
        }
        URLClassLoader classLoader =
            new URLClassLoader(urlList.toArray(new URL[urlList.size()]), this.getClass()
                .getClassLoader());
        SeleniumTest test = null;
        try {
            Class<?> klass = Class.forName(className, true, classLoader);
            if (SeleniumTest.class.isAssignableFrom(klass)) {
                test = (SeleniumTest) klass.newInstance();
                if (executionId != null) {
                    testMap.put(executionId, test);
                }
                test.executeSeleniumScript(this, params);
            } else {
                throw new SeleniumPluginException(className
                    + " needs to implement SeleniumTest interface!");
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new SeleniumPluginException(ErrorUtils.logExceptionFmt(log, e, "Exception: {0}"),
                e);
        } finally {
            if (executionId != null) {
                testMap.remove(executionId);
            }
            try {
                classLoader.close();
            } catch (IOException e) {
                ErrorUtils.logExceptionFmt(log, e, "Exception: {0}");
            }
        }
    }

    private void createUrl(File tempDir, Collection<URL> urlList) {
        if (tempDir.isDirectory()) {
            for (File f : tempDir.listFiles()) {
                if (!f.isDirectory() && f.getName().toLowerCase().endsWith(".jar")) {
                    try {
                        urlList.add(f.toURI().toURL());
                    } catch (MalformedURLException e) {
                        final String msg = MessageFormat.format(
                            "Malformed URL {1}. Exception: {0}",
                            e.getMessage(), f.toURI().toString());
                        log.error(msg, e);
                    }
                }
            }
        }
    }
    
    
    
    @Override
    public Execution checkAsyncSeleniumtest(String testExecutionId) throws SeleniumPluginException {
        Execution execution = executionMap.get(testExecutionId);
        return execution;
    }

    protected String procureWebDriver(Class<? extends BrowserManager> driverManagerClass) {
        return procureWebDriver(driverManagerClass, null);
    }

    protected String procureWebDriver(Class<? extends BrowserManager> driverManagerClass,
        String forcedVersion) {
        final String driverName = driverManagerClass.getSimpleName();
        while (true) {
            try {
                log.info("Trying to procure {}.", driverName);
                final Method getInstanceMethod = driverManagerClass.getMethod("getInstance");
                final BrowserManager browserManager = (BrowserManager) getInstanceMethod
                    .invoke(null);
                if (forcedVersion != null) {
                    browserManager.setup(forcedVersion);
                } else {
                    browserManager.setup();
                }
                String version = browserManager.getDownloadedVersion();
                log.info("Downloaded {} version {}", driverName, version);
                return version;
            } catch (Throwable e) {
                String msg = ErrorUtils.logExceptionFmt(log, e,
                    "Got exception while trying to download {1}. Exception: {0}", driverName);
                error(msg, e);
                log.debug("Sleeping a bit before retrying download of {}.", driverName);
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e1) {
                    ErrorUtils.logExceptionFmt(log, e,
                        "Got interrupted while waiting to download {1}. Exception: {0}",
                        driverManagerClass.getSimpleName());
                }
            }
        }
    }

    protected void waitForWebDriverDownload(Future<String> webDriverVersion, Logger log,
        Class<? extends BrowserManager> driverManagerClass) {
        String driverName = driverManagerClass.getName();
        while (!webDriverVersion.isDone()) {
            try {
                webDriverVersion.get(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // Just loop again.
            } catch (ExecutionException e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Failed to download {1}. Exception: {0}", driverName);
            } catch (java.util.concurrent.TimeoutException e) {
                log.warn("Still waiting for {} to be downloaded.", driverName);
            }
        }
    }

    @Override
    public WebDriver webDriver(String sessionId) {
        WebDriver driver = driverMap.get(sessionId);
        return driver;
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        for (WebDriver driver : driverMap.values()) {
            try {
                driver.quit();
            } catch (Exception e) {
                // do nothing - we just want to at least try to clean up
            }
        }
    }
}
