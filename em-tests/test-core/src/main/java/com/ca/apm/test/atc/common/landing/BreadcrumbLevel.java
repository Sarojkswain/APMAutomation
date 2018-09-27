package com.ca.apm.test.atc.common.landing;

import java.util.List;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.element.ATCDropDownOpener;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.test.atc.common.element.WebElementWrapper;

public class BreadcrumbLevel extends WebElementWrapper {

    private int index;
    
    protected BreadcrumbLevel(PageElement element, int index) {
        super(element);
        this.index = index;
    }
    
    public void go() {
        findElement(By.className("attributes-container")).click();
    }
    
    public void goTo(String dropdownOption) {
        ATCDropDownOpener dropdownLink = new ATCDropDownOpener(
            findElement(getDropdownLinkSelector()));
        dropdownLink.selectFromDropdown(getDropdownMenuSelector(), dropdownOption);
    }
    
    public List<String> getDropdownOptions() {
        ATCDropDownOpener dropdownLink = new ATCDropDownOpener(
            findElement(getDropdownLinkSelector()));         
        return dropdownLink.getDropdownOptions(getDropdownMenuSelector());
    }
    
    public String getLabel() {
        return findElement(By.className("attribute-name")).getText();
    }
    
    public String getValue() {
        return findElement(getAttibuteValueSelector()).getText();
    }
    
    public boolean isDropdownPresent() {
        return findElement(getDropdownLinkSelector()).isDisplayed();
    }
    
    public boolean isDisabled() {
        return findElement(getAttibuteValueSelector()).getAttribute("class").contains("no-hover");
    }
    
    private By getAttibuteValueSelector() {
        return By.className("attribute-value");
    }
    
    private By getDropdownLinkSelector() {
        return By.className("caret-container");
    }
    
    private By getDropdownMenuSelector() {
        return By.className("t-breadcrumb-level-" + index + "-dropdown");
    }
}
