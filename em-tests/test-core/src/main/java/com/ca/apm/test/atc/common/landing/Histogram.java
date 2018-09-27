package com.ca.apm.test.atc.common.landing;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.element.ChildElementSelectorWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class Histogram extends ChildElementSelectorWrapper {
    
    public Histogram(UI ui, PageElement parent) {
        super(ui, parent, By.cssSelector("histogram-directive"));
    }

    public PageElement getSlowTransactionsVolumeElement() {
        return findElement(By.className("t-slow-trx-cnt"));
    }
    
    public int getSlowTransactionsVolume() {
        return LandingUtils.getValueFromFormattedString(getSlowTransactionsVolumeElement().getText());
    }

    public PageElement getFailedTransactionsVolumeElement() {
        return findElement(By.className("t-failed-trx-cnt"));
    }
    
    public int getFailedTransactionsVolume() {
        return LandingUtils.getValueFromFormattedString(getFailedTransactionsVolumeElement().getText());
    }
}
