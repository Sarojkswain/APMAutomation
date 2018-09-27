package com.ca.apm.test.atc.common.element;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;

import com.ca.apm.test.atc.common.UI;

public class WebElementWrapper extends PageElement {

    public static List<PageElement> wrapElements(List<WebElement> elements, UI ui) {
        List<PageElement> swappedElements = new ArrayList<PageElement>(elements.size());
        for (WebElement element : elements) {
            swappedElements.add(new WebElementWrapper(element, ui));
        }
        return swappedElements;
    }
    
    public static PageElement wrapElement(WebElement element, UI ui) {
        return new WebElementWrapper(element, ui);
    }

    private final WebElement el;

    public WebElementWrapper(WebElement element, UI ui) {
        super(ui);
        if (element == null) {
            throw new IllegalArgumentException("The element to be wrapped cannot be null");
        }
        this.el = element;
    }

    protected WebElementWrapper(PageElement element) {
        this(element, element.ui);
    }

    @Override
    public final WebElement getWrappedElement() {
        return el;
    }
}
