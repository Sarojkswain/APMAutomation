package com.ca.apm.test.atc.common.landing;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.ChildElementSelectorWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class StoryPanel extends ChildElementSelectorWrapper {

    private final int ANIMATION_DELAY = 600;
    
    public StoryPanel(UI ui, PageElement parent) {
        super(ui, parent, By.cssSelector("story-panel"));
    }

    public void expand() {
        PageElement toggleBtn = findElement(By.className("hide-pa-icon"));
        if (!toggleBtn.getAttribute("class").contains("open")) {
            toggleBtn.click();
            Utils.sleep(ANIMATION_DELAY);
        }
    }
    
    public List<WebElement> getAffectedComponentsProblemsElements() {
        return findElements(By.cssSelector(".t-problems .story-column-number"));
    }
    
    public List<WebElement> getAffectedComponentsAnomaliesElements() {
        return findElements(By.cssSelector(".t-anomalies .story-column-number"));
    }

    public int getProblemsCount() {
        this.expand();
        return this.getStoryCount(this.getAffectedComponentsProblemsElements());
    }
    
    public int getAnomaliesCount() {
        this.expand();
        return this.getStoryCount(this.getAffectedComponentsAnomaliesElements());
    }
    
    public int getStoryCount(List<WebElement> stories) {
        int count = 0;
        for (WebElement story : stories) {
            if (story.isDisplayed()) {
                count += Integer.valueOf(story.getText().trim());
            }
        }
        return count;
    }
    
    public List<WebElement> getProblemStories() {
        return findElements(By.cssSelector(".t-stories .story-detail"));
    }
    
    public List<WebElement> getAnomalyStories() {
        return findElements(By.cssSelector(".t-anomalies .story-detail"));
    }
    
    public void moveElementIntoView(By locator) {
        WebElement element = findElement(locator);
        Actions actions = new Actions(ui.getDriver());
        actions.moveToElement(element);
        actions.perform();
    }
}
