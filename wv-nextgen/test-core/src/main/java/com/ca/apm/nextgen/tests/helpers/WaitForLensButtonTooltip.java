package com.ca.apm.nextgen.tests.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * @author haiva01
 */
public class WaitForLensButtonTooltip implements ExpectedCondition<WebElement> {
    private static final Logger log = LoggerFactory.getLogger(WaitForLensButtonTooltip.class);

    private WebViewUi ui;
    private WebElement lensButton;

    public WaitForLensButtonTooltip(WebViewUi ui, WebElement lensButton) {
        this.ui = ui;
        this.lensButton = lensButton;
    }

    @Override
    public WebElement apply(@Nullable WebDriver input) {
        try {
            ui.getActions()
                .moveToElement(lensButton)
                .perform();
            WebElement element = ui.getWebElementOrNull(
                By.xpath("//*[@id='webview-consolelens-dialog-launch-button-tooltip']"));
            if (element == null) {
                log.debug("Tooltip not fount yet...");
            }
            return element;
        } catch (NoSuchElementException ex) {
            log.debug("Tooltip not fount yet...");
            return null;
        }
    }
}
