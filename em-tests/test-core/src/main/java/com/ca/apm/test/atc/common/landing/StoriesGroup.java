package com.ca.apm.test.atc.common.landing;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.test.atc.common.element.WebElementWrapper;

public class StoriesGroup extends WebElementWrapper {

    protected StoriesGroup(PageElement element) {
        super(element);
    }
    
    public String getHeader() {
        return findElement(By.className("stories-header")).getText().trim();
    }
    
    public List<Story> getStories() {
        List<Story> stories = new ArrayList<Story>();
        List<PageElement> pElems = findPageElements(By.className("at-story")); 
        for (PageElement e : pElems) {
            stories.add(new Story(e));
        }
        return stories;
    }
    
    public List<Story> getAnomalyStories() {
        List<Story> stories = new ArrayList<Story>();
        List<PageElement> anomalies = findPageElements(By.className("at-story")); 
        for (PageElement anomaly : anomalies) {
            if (anomaly.isDisplayed()) {
                stories.add(new Story(anomaly));
            }
        }
        return stories;
    }
    
    public List<Story> getVisibleStories() {
        List<Story> stories = new ArrayList<Story>();
        for (Story story : getStories()) {
            if (story.isDisplayed()) {
                stories.add(story);
            }
        }
        return stories;
    }
    
    public Story getNthStory(int index) {
        return getStories().get(index);
    }

}
