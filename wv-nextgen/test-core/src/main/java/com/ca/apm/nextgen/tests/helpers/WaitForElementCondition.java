/**
 * 
 */
package com.ca.apm.nextgen.tests.helpers;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic wait for element condition. Element is located by the passed {@link By} instance. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class WaitForElementCondition implements ExpectedCondition<WebElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitForElementCondition.class);
    private static final int FIND_TRIES_COUNT = 10;
    
    private WebViewUi ui;
    private WebElement targetElement;
    private By byLocator;
    
    /**
     * Constructor.
     * 
     * @param ui
     * @param targetElement
     * @param byLocator
     */
    public WaitForElementCondition(WebViewUi ui, WebElement targetElement, By byLocator) {
        super();
        this.ui = ui;
        this.targetElement = targetElement;
        this.byLocator = byLocator;
    }


    @Override
    public WebElement apply(WebDriver input) {
        try {
            LOGGER.info("Trying to find element by locator: {}", byLocator);
            WebElement waitedElement = null;
            for (int i = 0; i < FIND_TRIES_COUNT; i++) {
                LOGGER.info("Try #: {}", (i + 1));
                ui.getActions()
                .moveToElement(targetElement)
                .perform();

                waitedElement = ui.getWebElementOrNull(byLocator);
                if (waitedElement != null) {
                    break;
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    LOGGER.warn("Wait interrupted: ", e);
                }
            }
            if (waitedElement == null) {
                LOGGER.info("Waited element not found!");
            }
            return waitedElement;
        } catch (NoSuchElementException ex) {
            LOGGER.error("Element not found yet!");
            return null;
        }
        
    }

}
