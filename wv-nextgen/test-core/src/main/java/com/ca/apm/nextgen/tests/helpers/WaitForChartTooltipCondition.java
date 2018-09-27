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
public class WaitForChartTooltipCondition implements ExpectedCondition<WebElement> {
    private static final Logger log = LoggerFactory.getLogger(WaitForChartTooltipCondition.class);

    private WebViewUi ui;
    private WebElement innerChart;
    private int x;
    private int y;

    public WaitForChartTooltipCondition(WebViewUi ui, WebElement innerChart, int x, int y) {
        this.ui = ui;
        this.innerChart = innerChart;
        this.x = x;
        this.y = y;
    }

    @Override
    public WebElement apply(@Nullable WebDriver input) {
        try {
            ui.getActions()
                .moveToElement(innerChart, x, y)
                .perform();
            WebElement element = ui.getWebElementOrNull(
                By.xpath("//a[@id='webview-investigator-linechart-tooltip']"));
            if (element == null) {
                log.debug("Tooltip not fount yet...");
            }
            return element;
        } catch (NoSuchElementException ex) {
            log.debug("Element not fount yet...");
            return null;
        }
    }
}
