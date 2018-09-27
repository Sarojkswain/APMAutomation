package com.ca.apm.test.atc.common.landing;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.element.ElementConditionWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class Breadcrumb extends ElementConditionWrapper {
    
    public Breadcrumb(UI ui) {
        super(ui, By.id("home-breadcrumb"));
    }
    
    public void waitForLoad() {
        ui.waitForWorkIndicator(By.className("breadcrumb-work-indicator"));
    }
    
    public boolean isBreadcrumbVisible() {
        return ui.getElementProxy(getHomeIconSelector()).isDisplayed();
    }
    
    public void goHome() {
        waitForLoad();
        findElement(getHomeIconSelector()).click();
        waitForLoad();
    }
    
    public void goTo(int level) {
        waitForLoad();
        getNthLevel(level).go();
        waitForLoad();
    }
    
    public void goTo(int level, String label) {
        waitForLoad();
        getNthLevel(level).goTo(label);
        waitForLoad();
    }
    
    public List<BreadcrumbLevel> getLevels() {
        waitForLoad();
        List<BreadcrumbLevel> result = new ArrayList<BreadcrumbLevel>();
        
        List<PageElement> levelElems = findPageElements(By.className("t-breadcrumb-level"));
        for (int i = 0; i < levelElems.size(); i++) {
            result.add(new BreadcrumbLevel(levelElems.get(i), i));
        }
        
        return result;
    }
    
    public int getLevelsCount() {
        return getLevels().size();
    }
    
    public BreadcrumbLevel getNthLevel(int index) {
        List<BreadcrumbLevel> levels = getLevels();
        if (index >= levels.size()) {
            throw new IllegalArgumentException("Level " + index + " does not exist.");
        }
        return getLevels().get(index);
    }
    
    public BreadcrumbLevel getLastLevel() {
        List<BreadcrumbLevel> levels = getLevels();
        return levels.get(levels.size() - 1);
    }
    
    private By getHomeIconSelector() {
        return By.className("icon-home");
    }
}
