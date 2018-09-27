package com.ca.apm.tests.util.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/*
 * code based on
 * selenium-plugin/src/main/java/com/ca/apm/systemtest/fld/plugin/selenium/SeleniumPluginAbs.java
 */
public abstract class SeleniumHelperBase implements SeleniumHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumHelperBase.class);

    private static final int IMPLICIT_WAIT_SECONDS = 5;
    private static final AtomicLong nextId = new AtomicLong(1000);

    private Map<String, WebDriver> driverMap = new HashMap<>();

    protected SeleniumHelperBase() {}

    public abstract void initialize();

    @Override
    public String openUrl(String sessionId, String url) {
        LOGGER.info("Session {}, opening URL {}", sessionId, url);
        WebDriver driver = getWebDriver(sessionId);
        driver.get(url);
        return driver.getWindowHandle();
    }

    @Override
    public boolean waitForElement(String sessionId, String windowId, SelectionBy selectionBy,
        String id, int timeOutInSeconds) {
        WebDriver driver = getWebDriver(sessionId);
        By by = makeBy(selectionBy, id);
        try {
            WebElement dynamicElement =
                new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions
                    .presenceOfElementLocated(by));
            LOGGER.trace("dynamicElement = {}", dynamicElement);
        } catch (org.openqa.selenium.TimeoutException e) {
            LOGGER.warn("element {} has not appeared within {} seconds", by, timeOutInSeconds);
            return false;
        }
        LOGGER.info("element {} has appeared within less than {} seconds", by, timeOutInSeconds);
        return true;
    }

    @Override
    public boolean fillTextField(String sessionId, String windowId, SelectionBy selectionBy,
        String id, String newText) {
        WebDriver driver = getWebDriver(sessionId);
        if (windowId != null) {
            driver.switchTo().window(windowId);
        }
        By by = makeBy(selectionBy, id);
        WebElement element = driver.findElement(by);
        if (element == null) {
            LOGGER.error("Failed to find element {} in window {} session {}", by, windowId,
                sessionId);
            return false;
        }
        element.clear();
        element.sendKeys(newText);
        return true;
    }

    @Override
    public boolean click(String sessionId, String windowId, SelectionBy selectionBy, String id) {
        WebDriver driver = getWebDriver(sessionId);
        if (windowId != null) {
            driver.switchTo().window(windowId);
        }
        By by = makeBy(selectionBy, id);
        WebElement element = driver.findElement(by);
        if (element == null) {
            LOGGER.error("Failed to find element {} in window {} session {}", by, windowId,
                sessionId);
            return false;
        }
        LOGGER.info("clicking at {} in window {} session {}", by, windowId, sessionId);
        element.click();
        return true;
    }

    public void closeSession(String sessionid) {
        WebDriver driver = driverMap.remove(sessionid);
        if (driver != null) {
            Set<String> windows = driver.getWindowHandles();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Open windows in session {}: {}", sessionid, windows);
            }
            for (String windowId : windows) {
                try {
                    driver.switchTo().window(windowId);
                    driver.close();
                } catch (Exception e) {
                    LOGGER.error("Failed to close web driver for {1}, session {2}, window {3}."
                            + " Exception: {0}", e, driver.getCurrentUrl(), sessionid, windowId);
                }
            }
            try {
                driver.quit();
            } catch (Throwable e) {
                LOGGER.error("Failed to quit web driver for {1}, session {2}. Exception: {0}", e,
                    driver.getCurrentUrl(), sessionid);
            }
        }
    }

    protected String startSession(WebDriver driver, String prefix) {
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS);
        String id = prefix + getNextId();
        driverMap.put(id, driver);
        return id;
    }

    protected final long getNextId() {
        return nextId.getAndIncrement();
    }

    private WebDriver getWebDriver(String sessionId) {
        WebDriver driver = driverMap.get(sessionId);
        if (driver == null) {
            String msg =
                MessageFormat.format("Driver for session id {0} does not exist", sessionId);
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
        return driver;
    }



    protected static boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") != -1;
    }

    /**
     * Convert SelectionBy and value to Selenium's By object.
     *
     * @param selectionBy selection criterion
     * @param id criterion value
     * @return new By instance
     */
    protected static By makeBy(SelectionBy selectionBy, String id) {
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
                LOGGER.error(msg);
                throw new RuntimeException(msg);
        }
    }

}
