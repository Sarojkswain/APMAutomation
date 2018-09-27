package com.ca.apm.tests.util.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author haiva01 
 */
public abstract class SeleniumClientBase implements SeleniumAccess, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(SeleniumClientBase.class);

    protected WebDriver driver;

    private long delay = 0;

    public SeleniumClientBase(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Sets delay in ms.
     *
     * @param ms
     */
    public void setDelay(long ms) {
        this.delay = ms;
    }

    protected void getUrl(String url) {
        log.info("Opening URL = '{}'", url);
        driver.get(url);
        delay();
    }

    protected void delay() {
        if (this.delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    protected void delay(long ms) {
        if (this.delay > 0) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    @Override
    public void clickWithDelay(WebElement element) {
        element.click();
        delay();
    }


    @Override
    public void selectOptionByIndex(WebElement element, int index) {
        new Select(element).selectByIndex(index);
        delay();
    }


    @Override
    public void selectOptionByVisibleText(WebElement element, String text) {
        new Select(element).selectByVisibleText(text);
        delay();
    }


    @Override
    public void selectOptionByValue(WebElement element, String value) {
        new Select(element).selectByValue(value);
        delay();
    }


    @Override
    public void setInputTextByName(String name, String text) {
        WebElement e = driver.findElement(By.name(name));
        e.sendKeys(text);
        delay();
    }


    @Override
    public void setInputTextById(String id, String text) {
        WebElement e = driver.findElement(By.id(id));
        e.clear();
        e.sendKeys(text);
        delay();
    }

    protected void submit(WebElement element) {
        element.submit();
        delay();
    }

    @Override
    public void close() throws Exception {
        if (driver == null) {
            return;
        }

        String base = driver.getWindowHandle();
        Collection<String> windows = driver.getWindowHandles();
        windows.remove(base);
        windows = new ArrayList<>(windows);
        windows.add(base);

        if (log.isDebugEnabled()) {
            log.debug("Open windows: {}", windows);
        }
        for (String windowId: windows) {
            try {
                driver.switchTo().window(windowId);
                driver.close();
            } catch (Exception e) {
                log.error("Failed to close web driver for {1}, window {2}. Exception: {0}",
                    e, driver.getCurrentUrl(), windowId);
            }
        }

        try {
            driver.quit();
        } catch (Throwable e) {
            log.error("Failed to quit web driver for {1}. Exception: {0}",
                e, driver.getCurrentUrl());
        }
    }
}
