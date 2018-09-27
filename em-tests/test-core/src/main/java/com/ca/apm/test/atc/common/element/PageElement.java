package com.ca.apm.test.atc.common.element;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;

public abstract class PageElement implements WebElement, WrapsElement, Locatable {
    
    protected final UI ui;
    
    protected PageElement(UI ui) {
        if (ui == null) {
            throw new IllegalArgumentException("The UI cannot be null");
        }
        this.ui = ui;
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        return getWrappedElement().getScreenshotAs(target);
    }

    @Override
    public void click() {
        WebElement element = getWrappedElement();
        ui.actions()
            .moveToElement(element)
            .click()
            .perform();
    }

    @Override
    public void submit() {
        getWrappedElement().submit();
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        getWrappedElement().sendKeys(keysToSend);
    }

    @Override
    public void clear() {
        getWrappedElement().clear();
    }

    @Override
    public String getTagName() {
        return getWrappedElement().getTagName();
    }

    @Override
    public String getAttribute(String name) {
        return getWrappedElement().getAttribute(name);
    }

    @Override
    public boolean isSelected() {
        return getWrappedElement().isSelected();
    }

    @Override
    public boolean isEnabled() {
        if (!getWrappedElement().isEnabled()) {
            return false;
        }
        return getWrappedElement().getAttribute("disabled") == null;
    }

    @Override
    public String getText() {
        return getWrappedElement().getText();
    }

    /**
     * Use findPageElements() instead
     */
    @Override
    public List<WebElement> findElements(By by) {
        return getWrappedElement().findElements(by);
    }

    public List<PageElement> findPageElements(By by) {
        return WebElementWrapper.wrapElements(findElements(by), ui);
    }

    @Override
    public PageElement findElement(final By by) {
        return new ChildElementSelectorWrapper(ui, this, by);
    }

    public boolean isPresent() {
        try {
            getWrappedElement();
            return true;
        } catch (NoSuchElementException | TimeoutException ex) {
            return false;
        }

    }

    @Override
    public boolean isDisplayed() {
        try {
            return getWrappedElement().isDisplayed();
        } catch (NoSuchElementException | TimeoutException ex) {
            return false;
        }
    }

    @Override
    public Point getLocation() {
        return getWrappedElement().getLocation();
    }

    @Override
    public Dimension getSize() {
        return getWrappedElement().getSize();
    }

    @Override
    public Rectangle getRect() {
        return getWrappedElement().getRect();
    }

    @Override
    public String getCssValue(String propertyName) {
        return getWrappedElement().getCssValue(propertyName);
    }

    public void scrollIntoView() {
        JavascriptExecutor jse = (JavascriptExecutor) ui.getDriver();
        jse.executeScript("arguments[0].scrollIntoView(true);", getWrappedElement());
    }
    
    public void scrollToTop() {
        JavascriptExecutor jse = (JavascriptExecutor) ui.getDriver();
        jse.executeScript("window.scrollTo(0, 0)");
    }

    public void scrollBottomIntoView() {
        JavascriptExecutor jse = (JavascriptExecutor) ui.getDriver();
        jse.executeScript("arguments[0].scrollTop += arguments[0].scrollHeight;", getWrappedElement());
    }

    public boolean hasVerticalScrollbar() {
        JavascriptExecutor jse = (JavascriptExecutor) ui.getDriver();
        return (Boolean) jse.executeScript(
            "return arguments[0].scrollHeight > arguments[0].clientHeight;", getWrappedElement());
    }

    public void waitUntilVisible() {
        Utils.waitUntilVisible(ui.getDriver(), getWrappedElement());
    }

    public void waitForTextChange() {
        waitForTextChange(Utils.DEFAULT_WAIT_DURATION);
    }

    public void waitForTextChange(int seconds) {
        waitForTextChange(getText(), seconds);
    }

    public void waitForTextChange(final String initialText, int seconds) {
        Utils.waitForCondition(ui.getDriver(), new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver wd) {
                return !getText().equals(initialText);
            }
        }, seconds);
    }

    public void waitForTextChange(final String initialText) {
        waitForTextChange(initialText, Utils.DEFAULT_WAIT_DURATION);
    }

    public void waitWhileEmptyText() {
        Utils.waitForCondition(ui.getDriver(), new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver wd) {
                return !getText().trim().isEmpty();
            }
        }, Utils.DEFAULT_WAIT_DURATION);
    }

    public void waitForClassChange(String originalClassValue) {
        Utils.waitForClassValueChange(ui.getDriver(), this, originalClassValue);
    }

    public void waitForAttributeChange(String attributeName) {
        Utils.waitForAttributeChange(ui.getDriver(), this, attributeName, getAttribute(attributeName));
    }

    @Override
    public Coordinates getCoordinates() {
        return ((Locatable) getWrappedElement()).getCoordinates();
    }

}
