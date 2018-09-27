package com.ca.apm.test.atc.common.landing;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.ChildElementSelectorWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class ShareUrlBox extends ChildElementSelectorWrapper {

    public ShareUrlBox(UI ui, PageElement parent) {
        super(ui, parent, By.className("copy-url-dialog"));
    }
    
    public String getText() {
        return getUrlInputElement().getAttribute("value");
    }
    
    public void clearText() {
        PageElement input = getUrlInputElement();
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
    }
    
    public void copyUrlToClipboard() {
        PageElement copyBtn = findElement(By.className("copy-btn"));
        copyBtn.click();
        Utils.sleep(100);
    }
    
    public PageElement getUrlInputElement() {
        return findElement(By.className("url-to-copy"));
    }
}
