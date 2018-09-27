/*
 * Copyright (c) 2015 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.test.atc.common;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.base.CharMatcher;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.tas.role.seleniumgrid.BrowserType;

import static com.ca.tas.role.seleniumgrid.BrowserType.EDGE;
import static com.ca.tas.role.seleniumgrid.BrowserType.INTERNET_EXPLORER;
import static org.testng.AssertJUnit.fail;

public class Utils {
    public static final int DEFAULT_WAIT_DURATION = 20;
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static FluentWait<WebDriver> getFluentWait(WebDriver driver, long duration) {
        FluentWait<WebDriver> wait =
            new FluentWait<WebDriver>(driver).withTimeout(duration, TimeUnit.SECONDS)
                .pollingEvery(500, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
        return wait;
    }

    public static <V> V waitForCondition(WebDriver driver, ExpectedCondition<V> condition,
        long duration) {
        FluentWait<WebDriver> wait = getFluentWait(driver, duration);
        return wait.until(condition);
    }

    public static <V> V waitForCondition(WebDriver driver, ExpectedCondition<V> condition) {
        return waitForCondition(driver, condition, DEFAULT_WAIT_DURATION);
    }

    /**
     * @param driver
     * @param tabsCount expected tabs count
     */
    public static void waitForTabsCount(WebDriver driver, final int tabsCount) {
        waitForCondition(driver, new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver wd) {
                return wd.getWindowHandles().size() == tabsCount;
            }
        }, DEFAULT_WAIT_DURATION);
    }

    /**
     * Use other approach for waiting e.g.: waitIfVisible or waitIfNotVisible
     *
     * @param millis
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method helps to run a code that can throw a StaleElementReferenceException if there is
     * an auto-refresh during the code's execution.
     * If that is the case the call method is called again.
     *
     * @param callable
     * @return
     * @throws Exception
     */
    public static <T> T callAgainOnStaleReferenceException(Callable<T> callable) throws Exception {
        try {
            return callable.call();
        } catch (StaleElementReferenceException e) {
            return callable.call();
        }
    }

    /**
     * This method helps to run a code that can throw a StaleElementReferenceException if there is
     * an auto-refresh during the code's execution.
     * If that is the case the run method is called again.
     *
     * @param runnable
     * @return
     */
    public static void runAgainOnStaleReferenceException(Runnable runnable) {
        try {
            runnable.run();
        } catch (StaleElementReferenceException e) {
            runnable.run();
        }
    }

    public static String switchToAnotherTab(WebDriver driver, int expectedTabsCount) {
        try {
            Utils.waitForTabsCount(driver, expectedTabsCount);
        } catch (TimeoutException e) {
            fail("Number of opened browser tabs/windows is not equal to " + expectedTabsCount);
        }

        Set<String> allWinHandles = driver.getWindowHandles();
        if (allWinHandles.size() > 1) {
            allWinHandles.remove(driver.getWindowHandle());
        }
        String nextTabHandle = allWinHandles.iterator().next();
        driver.switchTo().window(nextTabHandle);

        return nextTabHandle;
    }

    public static String closeTab(WebDriver driver, String windowHandle) {
        for (String wh : driver.getWindowHandles()) {
            if (wh.equals(windowHandle)) {
                driver.switchTo().window(wh);
                break;
            }
        }

        driver.close();

        String nextTabHandle = null;
        Set<String> allWinHandles = driver.getWindowHandles();
        if (allWinHandles.size() > 0) {
            nextTabHandle = allWinHandles.iterator().next();
            driver.switchTo().window(nextTabHandle);
        }
        return nextTabHandle;
    }

    public static void waitWhileVisible(WebDriver driver, By locator) {
        waitWhileVisible(driver, locator, Utils.DEFAULT_WAIT_DURATION);
    }

    public static void waitWhileVisible(WebDriver driver, By locator, long duration) {
        waitForCondition(driver, ExpectedConditions.invisibilityOfElementLocated(locator),
            duration);
    }

    public static WebElement waitUntilVisible(WebDriver driver, By locator, long duration) {
        return waitForCondition(driver, ExpectedConditions.visibilityOfElementLocated(locator),
            duration);
    }

    public static WebElement waitUntilVisible(WebDriver driver, WebElement element, long duration) {
        return waitForCondition(driver, ExpectedConditions.visibilityOf(element), duration);
    }

    public static WebElement waitUntilVisible(WebDriver driver, WebElement element) {
        return waitUntilVisible(driver, element, DEFAULT_WAIT_DURATION);
    }

    /**
     * Wait until a group of elements becomes available
     */
    public static List<WebElement> waitUntilElementsVisible(WebDriver driver, By locator,
        long duration) {
        return waitForCondition(driver,
            ExpectedConditions.visibilityOfAllElementsLocatedBy(locator), duration);
    }

    public static WebElement waitUntilVisible(WebDriver driver, By locator) {
        return waitUntilVisible(driver, locator, Utils.DEFAULT_WAIT_DURATION);
    }

    public static void waitForAttributeChange(WebDriver driver, final WebElement element,
        final String attribute, final String initialValue) {
        waitForCondition(driver, new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver wd) {
                return element.getAttribute(attribute).equals(initialValue);
            }
        }, 10);
    }

    public static void waitForClassValueChange(WebDriver driver, final WebElement element,
        final String currentClassValue) {
        waitForAttributeChange(driver, element, "class", currentClassValue);
    }

    public static void waitForAngularConfiguration(WebDriver driver,
        final String configurationString, long duration) {
        FluentWait<WebDriver> wait = getFluentWait(driver, duration);
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                String script =
                    "try {return angular.element(document.body).injector().get"
                        + "('ConfigurationService')."
                        + configurationString + ";} catch(e) {return 'ERROR';};";
                Object scriptResult = ((JavascriptExecutor) input).executeScript(script);
                if (scriptResult == null) {
                    return true;
                }
                return !scriptResult.toString().equals("ERROR");
            }
        });
    }

    public static boolean isRemoteDriver(WebDriver driver) {
        return ClassUtils.isAssignable(driver.getClass(), RemoteWebDriver.class);
    }

    public static BrowserType toBrowserType(String browserName) {
        switch (browserName) {
            case "chrome":
                return BrowserType.CHROME;

            case "firefox":
                return BrowserType.FIREFOX;

            case "htmlunit":
                return BrowserType.HTMLUNIT;

            case "internet explorer":
                return INTERNET_EXPLORER;

            case "MicrosoftEdge":
                return EDGE;

            default:
                throw new RuntimeException("Unsupported browser: " + browserName);
        }
    }

    public static BrowserType toBrowserType(Class<?> klass) {
        if (ClassUtils.isAssignable(klass, ChromeDriver.class)) {
            return BrowserType.CHROME;
        } else if (ClassUtils.isAssignable(klass, FirefoxDriver.class)) {
            return BrowserType.FIREFOX;
        } else if (ClassUtils.isAssignable(klass, InternetExplorerDriver.class)) {
            return INTERNET_EXPLORER;
        } else if (ClassUtils.isAssignable(klass, EdgeDriver.class)) {
            return EDGE;
        } else if (ClassUtils.isAssignable(klass, HtmlUnitDriver.class)) {
            return BrowserType.HTMLUNIT;
        } else {
            throw new RuntimeException("Unsupported browser: " + klass.getName());
        }
    }

    public static BrowserType browserType(WebDriver driver) {
        if (isRemoteDriver(driver)) {
            RemoteWebDriver rd = (RemoteWebDriver) driver;
            String browserName = rd.getCapabilities().getBrowserName();
            return toBrowserType(browserName);
        } else {
            return toBrowserType(driver.getClass());
        }
    }

    /**
     * This method sets value to input field. It tries to handle incompatibilities between
     * different browser.
     *
     * @param driver web driver
     * @param inputElement input element
     * @param text text to be set to given input field
     */
    public static void clearAndSetInputField(WebDriver driver, WebElement inputElement,
        String text) {
        inputElement.clear();
        BrowserType browserType = browserType(driver);

        boolean useSetAttribute = false;
        if (// Edge driver does not support sendKeys as of 3.14393 for Edge 14.14393.
            browserType.equals(EDGE)) {
            log.info("Edge driver does not support sendKeys(text).");
            useSetAttribute = true;
        } else if (
            // IE has problems with sending keys of non-ASCII characters.
            (browserType.equals(INTERNET_EXPLORER)
                && !CharMatcher.ascii().matchesAllOf(text))) {
            log.info("Internet Explorer has problems with sending keys of non-ASCII characters.");
            useSetAttribute = true;
        }

        if (useSetAttribute) {
            log.warn(
                "Using JavaScript input.setAttribute('value', text) instead of sendKeys(text).");
            new Actions(driver)
                .moveToElement(inputElement)
                .click()
                .perform();
            ((JavascriptExecutor) driver)
                .executeScript("arguments[0].setAttribute('value', arguments[1]);", inputElement,
                    text);
        } else {
            new Actions(driver)
                .moveToElement(inputElement)
                .click()
                .sendKeys(text)
                .perform();
        }
    }

    /**
     * This method helps with testing whether an item is present in a collection.
     *
     * @param collection  collection to inspect
     * @param item        item to look for
     * @param <ItemTypeT> type of item
     */
    public static <ItemTypeT> void assertContains(Collection<? extends ItemTypeT> collection,
        ItemTypeT item) {
        if (!collection.contains(item)) {
            final String failMsg = String.format(Locale.US, "'%s' not found in collection [%s]",
                item.toString(), StringUtils.join(collection, ", "));
            Assert.fail(failMsg);
        }
    }

    /**
     * This method handles moving to given element and clicking on it. It decides to use one of
     * two approaches depending on the browser that is being used for the testing.
     *
     * @param pageElement page element to move to and click on
     * @param ui          UI instance
     */
    public static void moveToAndClick(WrapsElement pageElement, UI ui) {
        if (browserType(ui.getDriver()).equals(BrowserType.CHROME)
            || browserType(ui.getDriver()).equals(BrowserType.FIREFOX)
            || browserType(ui.getDriver()).equals(BrowserType.INTERNET_EXPLORER)) {
            ui.actions()
                .moveToElement(pageElement.getWrappedElement(), 0, 0)
                .click()
                .perform();
        } else {
            ui.actions()
                .moveToElement(pageElement.getWrappedElement())
                .click()
                .perform();
        }
    }
}
