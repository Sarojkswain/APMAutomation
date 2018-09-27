package com.ca.apm.test.atc.common;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import com.ca.apm.test.atc.common.element.ElementConditionWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class AboutDialog extends ElementConditionWrapper {

    public AboutDialog(UI ui) {
        super(ui, By.className("about-dialog"));
    }

    public PageElement getEMInfoElement() {
        try {
            return findElement(By.xpath("//span[contains(text(), \"Enterprise Manager\")]"));
        } catch (NoSuchElementException nse) {
            return null;
        }
    }

    public PageElement getWebviewInfoElement() {
        try {
            return findElement(By.xpath("//span[contains(text(), \"Webview Release\")]"));
        } catch (NoSuchElementException nse) {
            return null;
        }
    }

    public PageElement getSupportLinkElement() {
        try {
            return findElement(By.partialLinkText("support.ca.com"));
        } catch (NoSuchElementException nse) {
            return null;
        }
    }

    public PageElement getCopyrightInfoElement() {
        try {
            return findElement(By.xpath("//p[contains(text(), \"Copyright\")]"));
        } catch (NoSuchElementException nse) {
            return null;
        }
    }

    public void close() {
        findElement(By.id("btn-close")).click();
        ui.waitWhileVisible(By.className("about-dialog"), 10);
    }
}
