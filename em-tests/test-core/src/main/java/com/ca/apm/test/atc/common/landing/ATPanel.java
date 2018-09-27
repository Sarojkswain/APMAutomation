package com.ca.apm.test.atc.common.landing;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.ChildElementSelectorWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class ATPanel extends ChildElementSelectorWrapper {

    private final int ANIMATION_DELAY = 700;

    public ATPanel(UI ui, PageElement parent) {
        super(ui, parent, By.className("at-panel-container"));
    }

    public void expand() {
        if (!isATPanelVisible()) {
            findElement(By.className("hide-pa-icon")).click();
            Utils.sleep(ANIMATION_DELAY);
        }
    }
    
    public void collapse() {
        if (isATPanelVisible()) {
            findElement(By.className("hide-pa-icon")).click();
            Utils.sleep(ANIMATION_DELAY);  // waiting for animation
        }
    }
    
    public boolean isATPanelVisible() {
        return getATPanelContent().isDisplayed();
    }
    
    public List<StoriesGroup> getProblemsGroups() {
        expand();
        List<StoriesGroup> groups = new ArrayList<StoriesGroup>();
        List<PageElement> grpElems = findElement(By.className("t-problems"))
                .findPageElements(By.className("stories-group"));
        for (PageElement g : grpElems) {
            groups.add(new StoriesGroup(g));
        }
        return groups;
    }
    
    public StoriesGroup getNthProblemGroup(int index) {
        return getProblemsGroups().get(index);
    }
    
    public List<StoriesGroup> getAnomaliesGroups() {
        List<StoriesGroup> groups = new ArrayList<StoriesGroup>();
        List<PageElement> grpElems = findElement(By.className("t-anomalies"))
                .findPageElements(By.className("stories-group"));
        for (PageElement g : grpElems) {
            groups.add(new StoriesGroup(g));
        }
        return groups;
    }
    
    public StoriesGroup getNthAnomalyGroup(int index) {
        return getAnomaliesGroups().get(index);
    }
    
    public List<String> getProblemsNames() {
        List<String> problems = new ArrayList<String>();
        List<PageElement> namesElems = getATPanelContent().findPageElements(
            By.cssSelector(".t-problems .story-desc"));
        for (PageElement storyNameElem : namesElems) {
            if (storyNameElem.isDisplayed()) {
                problems.add(storyNameElem.getText());
            }
        }
        return problems;
    }
    
    public List<String> getAnomaliesNames() {
        List<String> anomalies = new ArrayList<String>();
        List<PageElement> namesElems = getATPanelContent().findPageElements(
            By.cssSelector(".t-anomalies .story-desc"));
        for (PageElement storyNameElem : namesElems) {
            if (storyNameElem.isDisplayed()) {
                anomalies.add(storyNameElem.getText());
            }
        }
        return anomalies;
    }
    
    public void selectProblem(String storyName) {
        if (isProblemSelected(storyName)) {
            return;
        }
        List<PageElement> namesElems = getATPanelContent().findPageElements(By.className("story-desc"));
        for (PageElement storyNameElem : namesElems) {
            if (storyNameElem.getText().equals(storyName.trim())) {
                storyNameElem.click();
                ui.waitUntilVisible(By.cssSelector(
                    ".at-panel-container .story-detail-icon.icon-close:not(.ng-hide)"));
                break;
            }
        }
    }
    
    /**
     * @deprecated
     * @param storyName
     * @return
     */
    public boolean isProblemSelected(String storyName) {
        List<PageElement> namesElems = getATPanelContent().findPageElements(By.className("story-desc"));
        for (PageElement storyNameElem : namesElems) {
            if (storyNameElem.isDisplayed()
                    && storyNameElem.getText().equals(storyName.trim())) {
                PageElement parent = storyNameElem.findElement(By.xpath(".."));
                return parent.findElement(By.className("icon-close")).isDisplayed();
            }
        }
        return false;
    }
    
    public boolean isStorySelected(String storyName) {
        List<PageElement> namesElems = getATPanelContent().findPageElements(By.className("description"));
        for (PageElement storyNameElem : namesElems) {
            if (storyNameElem.isDisplayed()
                    && storyNameElem.getText().equals(storyName.trim())) {
                PageElement parent = storyNameElem.findElement(By.xpath(".."));
                return parent.findElement(By.className("icon-close")).isDisplayed();
            }
        }
        return false;
    }
    
    public StoriesGroup getFirstProblemStoryGroup() {
        List<StoriesGroup> storiesGroup = getProblemsGroups();
        if (storiesGroup.size() > 0) {
            return storiesGroup.get(0);
        }
        return null;
    }
    
    public StoriesGroup getFirstAnomalyStoryGroup() {
        List<StoriesGroup> storiesGroup = getAnomaliesGroups();
        if (storiesGroup.size() > 0) {
            return storiesGroup.get(0);
        }
        return null;
    }
    
    public Story getFirstProblemStory() {
        StoriesGroup firstStoryGroup = getFirstProblemStoryGroup();
        if (firstStoryGroup != null) {
            List<Story> stories = firstStoryGroup.getStories();
            if (stories.size() > 0) {
                Story story = stories.get(0);
                story.isProblem = true;
                return story;
            }
        }
        return null;
    }
    
    public Story getFirstAnomalyStory() {
        StoriesGroup firstStoryGroup = getFirstAnomalyStoryGroup();
        if (firstStoryGroup != null) {
            List<Story> stories = firstStoryGroup.getAnomalyStories();
            if (stories.size() > 0) {
                Story story = stories.get(0);
                story.isAnomaly = true;
                return story;
            }
        }
        return null;
    }
    
    public List<Story> getAllVisibleStories() {
        List<Story> stories = new ArrayList<>();
        List<StoriesGroup> problemsGroups = getProblemsGroups();
        List<StoriesGroup> anomaliesGroups = getAnomaliesGroups();
        
        for (StoriesGroup problemGroup : problemsGroups) {
            stories.addAll(problemGroup.getVisibleStories());
        }
        
        for (StoriesGroup anomalyGroup : anomaliesGroups) {
            stories.addAll(anomalyGroup.getAnomalyStories());
        }
        
        return stories;
    }
    
    public int getProblemsCount() {
        int count = 0;
        for (StoriesGroup storiesGroup : getProblemsGroups()) {
            count += storiesGroup.getVisibleStories().size();
        }
        return count;
    }
    
    public int getAnomaliesCount() {
        int count = 0;
        for (StoriesGroup storiesGroup : getAnomaliesGroups()) {
            count += storiesGroup.getVisibleStories().size();
        }
        return count;
    }
    
    public boolean isInDetailFiew() {
        return findElement(By.className("t-story-details")).isPresent();
    }
    
    public void cancelDetailView() {
        if (isInDetailFiew()) {
            findElement(By.className("story-detail")).click();
        }
    }
    
    public void scrollToAnomaliesPanel() {
        moveElementIntoView(By.className("t-anomalies"));
    }
    
    private PageElement getATPanelContent() {
        return findElement(By.className("problems-anomalies-content"));
    }
    
    private void moveElementIntoView(By locator) {
        WebElement element = findElement(locator);
        Actions actions = new Actions(ui.getDriver());
        actions.moveToElement(element);
        actions.perform();
    }
}
