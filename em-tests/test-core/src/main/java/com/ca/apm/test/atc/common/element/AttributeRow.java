package com.ca.apm.test.atc.common.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.DetailsPanel.AttributeType;

public class AttributeRow extends WebElementWrapper {

    private final AttributeType type;

    public AttributeRow(WebElement element, UI ui, AttributeType type) {
        super(element, ui);
        this.type = type;
    }

    public AttributeRow(PageElement element, AttributeType type) {
        super(element);
        this.type = type;
    }

    public PageElement getNameCell() {
        return findElement(By
                        .xpath(".//div[starts-with(@id,'name_view_table" + type.getTableId() + "_row')]"));
    }

    public PageElement getEndTimeValueCell() {
        return findElement(By
                        .xpath(".//div[starts-with(@id,'time0_view_table" + type.getTableId() + "_row')]"));
    }

    public PageElement getStartTimeValueCell() {
        // this cell unfortunately doesn't have ID
        return findElement(By
                    .xpath("./div[not(starts-with(@id,'time0_view_table" + type.getTableId() + "_row')) and contains(@class,'col1')]/div/div/div/div"));
    }

    public PageElement getExpandIcon() {
        return getNameCell()
                .findElement(By
                        .xpath("./img[@alt='Expand' and not(contains(@class,'ng-hide'))]"));
    }

    public PageElement getDeleteIcon() {
        return findElement(By.cssSelector("div.cell-delete img:not(.ng-hide)"));
    }

}
