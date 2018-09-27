package com.ca.apm.test.atc.common.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.ca.apm.test.atc.common.UI;

public class ChildElementSelectorWrapper extends PageElement {

    protected final PageElement parent;
    protected final By childSelector;

    protected ChildElementSelectorWrapper(UI ui, PageElement parent, By childSelector) {
        super(ui);
        this.parent = parent;
        this.childSelector = childSelector;
    }

    protected final By getChildSelector() {
        return childSelector;
    }
    
    @Override
    public final WebElement getWrappedElement() {
        return parent.getWrappedElement().findElement(getChildSelector());
    }
}
