package com.ca.apm.test.atc.common;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.element.PageElement;

public class DetailView {

    private UI ui;

    public DetailView(UI ui) {
        this.ui = ui;
    }

    public PageElement getDetailViewBar() {
        return ui.getElementProxy(By.cssSelector(".detailViewBar"));
    }

    private PageElement getCloseButton() {
        return ui.getElementProxy(By.id("detail-view-close-button"));
    }

    public void closeDetailViewTab() throws Exception {
        int tabsCount = ui.getDriver().getWindowHandles().size();
        getCloseButton().click();
        Utils.switchToAnotherTab(ui.getDriver(), tabsCount - 1);
    }

    public void waitForUpdate() {
        ui.getCanvas().waitForUpdate();
    }
}
