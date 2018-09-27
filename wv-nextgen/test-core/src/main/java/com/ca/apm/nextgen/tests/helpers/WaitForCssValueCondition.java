package com.ca.apm.nextgen.tests.helpers;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * This class implements condition for Selenium to wait for specific CSS attribute value on
 * specified element.
 *
 * @author haiva01
 */
public class WaitForCssValueCondition implements ExpectedCondition<WebElement> {
    private static final Logger log = LoggerFactory.getLogger(WaitForCssValueCondition.class);

    private final WebViewUi ui;
    private final SearchContext searchContext;
    private final By selector;
    private final String cssAttribute;
    private final String expectedValue;

    public WaitForCssValueCondition(WebViewUi ui, SearchContext searchContext,
        By selector, String cssAttribute, String expectedValue) {
        this.ui = ui;
        this.searchContext = searchContext;
        this.selector = selector;
        this.cssAttribute = cssAttribute;
        this.expectedValue = expectedValue;
    }

    public WaitForCssValueCondition(WebViewUi ui, By selector, String cssAttribute,
        String expectedValue) {
        this(ui, null, selector, cssAttribute, expectedValue);
    }

    @Nullable
    @Override
    public WebElement apply(@Nullable WebDriver input) {
        WebElement webElement = ui.getWebElement(
            searchContext == null ? ui.getWebDriver() : searchContext, selector);
        if (webElement.getCssValue(cssAttribute).equals(expectedValue)) {
            return webElement;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("searchContext", searchContext)
            .append("selector", selector)
            .append("cssAttribute", cssAttribute)
            .append("expectedValue", expectedValue)
            .toString();
    }
}
