package com.ca.apm.test.atc.common.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;

public class ElementConditionWrapper extends PageElement {

    protected static final int DEFAULT_WAIT_DURATION = 5;

    protected ExpectedCondition<WebElement> condition;
    protected final int waitDuration;

    public ElementConditionWrapper(UI ui, ExpectedCondition<WebElement> condition, int waitDuration) {
        super(ui);
        this.condition = condition;
        this.waitDuration = waitDuration;
    }

    public ElementConditionWrapper(UI ui, ExpectedCondition<WebElement> condition) {
        this(ui, condition, DEFAULT_WAIT_DURATION);
    }

    public ElementConditionWrapper(UI ui, By selector) {
        this(ui, ExpectedConditions.visibilityOfElementLocated(selector), DEFAULT_WAIT_DURATION);
    }

    @Override
    public WebElement getWrappedElement() {
        return Utils.waitForCondition(ui.getDriver(), condition, waitDuration);
    }
}
