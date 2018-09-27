package com.ca.apm.test.atc.common.landing;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.test.atc.common.element.WebElementWrapper;

public class Story extends WebElementWrapper {
    
    public boolean isProblem = false;
    public boolean isAnomaly = false;

    protected Story(PageElement element) {
        super(element);
    }
    
    public boolean isExpanded() {
        return findElement(By.className("t-story-details")).isDisplayed();
    }
    
    public void expand() {
        if (!isExpanded()) {            
            findElement(By.cssSelector("div[ng-click='toggleStory()']")).click();
            Utils.sleep(100);  // wait for story to expand
        }
    }
    
    public void collapse() {
        if (isExpanded()) {            
            findElement(By.className("story-desc")).click();
            Utils.sleep(100);  // wait for story to collapse
        }
    }
    
    public String getFirstAppeared() {
        return findElement(By.className("t-first-appeared")).getText().trim();
    }
    
    public String getLastAppeared() {
        return findElement(By.className("t-last-appeared")).getText().trim();
    }
    
    public boolean areOwnersPresent() {
        return findElement(By.className("t-owners-list")).isDisplayed();
    }
    
    public List<String> getOwners() {
        String[] owners =  findElement(By.className("t-owners-list")).getText().split("[\\s,\\,]");
        return Arrays.asList(owners);
    }
    
    public String getAffectedComponentsString() {
        return findElement(By.cssSelector(".t-affected-components.story-detail")).getText().trim();
    }
    
    public String getAffectedComponentsNumber() {
        return findElement(
            By.cssSelector(".story-column-number")).getText().trim();
    }
    
    public ShareUrlBox openShareUrlBox() {
        findElement(By.className("share-url")).click();
        Utils.sleep(150);
        return new ShareUrlBox(ui, this);
    }
    
    public void openNotebook() {
        expand();
        findElement(By.className("icon-file-text-o")).click();
        ui.getCanvas().waitForUpdate();
    }
    
    public String getName() {
        return findElement(By.className("description")).getText();
    }
}
