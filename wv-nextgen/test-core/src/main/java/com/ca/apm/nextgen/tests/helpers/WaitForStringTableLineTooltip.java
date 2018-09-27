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
public class WaitForStringTableLineTooltip implements ExpectedCondition<WebElement> {
    private static final Logger log = LoggerFactory.getLogger(WaitForStringTableLineTooltip.class);

    private WebViewUi ui;
    private WebElement tableLine;

    public WaitForStringTableLineTooltip(WebViewUi ui, WebElement tableLine) {
        this.ui = ui;
        this.tableLine = tableLine;
    }

    @Override
    public WebElement apply(@Nullable WebDriver input) {
        try {
            ui.getActions()
                .moveToElement(tableLine)
                .perform();
            WebElement element = ui.getWebElementOrNull(
                By.id("webview-investigator-livestringfigure-tooltip"));
            if (element == null) {
                log.debug("Tooltip not fount yet...");
            }
            return element;
        } catch (NoSuchElementException ex) {
            log.debug("Tooltip not fount yet...");
            return null;
        } catch (Throwable ex) {
            ui.takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(log, ex,
                "Error while waiting for tooltip. Exception: {0}");
        }
    }
}
