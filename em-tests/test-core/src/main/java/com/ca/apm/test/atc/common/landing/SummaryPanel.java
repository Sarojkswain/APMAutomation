package com.ca.apm.test.atc.common.landing;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.ATCDropDownOpener;
import com.ca.apm.test.atc.common.element.ChildElementSelectorWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class SummaryPanel extends ChildElementSelectorWrapper {

    public enum TimeRange {
        LAST_24_HOURS("Last 24 Hours", true, 24 * 60 * 60),
        LAST_12_HOURS("Last 12 Hours", true, 12 * 60 * 60),
        LAST_6_HOURS("Last 6 Hours", true, 6 * 60 * 60),
        LAST_2_HOURS("Last 2 Hours", true, 2 * 60 * 60),
        LAST_30_MINS("Last 30 Mins", true, 30 * 60),
        LAST_8_MINS("Last 8 Mins", true, 8 * 60),
        CUSTOM_RANGE("Custom Range", false, 0L);
        
        private final String label;
        private final boolean isLive;
        private final long seconds;
        
        private TimeRange(String label, boolean isLive, long seconds) {
            this.label = label;
            this.isLive = isLive;
            this.seconds = seconds;
        }

        public String getLabel() {
            return label;
        }

        public boolean isLive() {
            return isLive;
        }

        public long getSeconds() {
            return seconds;
        }
        
        public static TimeRange of(String value) {
            for (TimeRange t : values()) {
                if (t.getLabel().equals(value)) {
                    return t;
                }
            }
            
            return null;
        }
    }
    
    private final int ANIMATION_DELAY = 600;
    private final LandingPage landingPage;
    private Histogram histogram;
    
    public SummaryPanel(UI ui, LandingPage landingPage) {
        super(ui, landingPage, By.cssSelector("summary-panel-directive"));
        this.landingPage = landingPage;
    }

    public void waitForPanelToLoad() {
        By locator = By.cssSelector(".summary-container > .summary-content .work-indicator");
        ui.waitForWorkIndicator(locator);
    }
    
    public PageElement getContainer() {
        return findElement(By.id("summary-container"));
    }
    
    public PageElement getToggleElement() {
        return findElement(By.id("summary-toggle"));
    }
    
    public void collapsePanel() {
        // In case the animation is in progress on invoke
        Utils.sleep(ANIMATION_DELAY);
        PageElement foldIcon = getSummaryFoldIcon();
        boolean isExpanded = foldIcon.getAttribute("class").contains("open"); 
        if (isExpanded) {
            foldIcon.click();
            Utils.sleep(ANIMATION_DELAY);
        }
        waitForPanelToLoad();
    }
    
    public void expandPanel() {
        // In case the animation is in progress on invoke
        Utils.sleep(ANIMATION_DELAY);
        PageElement foldIcon = getSummaryFoldIcon();
        boolean isCollapsed = foldIcon.getAttribute("class").contains("close"); 
        if (isCollapsed) {
            foldIcon.click();
            Utils.sleep(ANIMATION_DELAY);
        }
        waitForPanelToLoad();
    }
    
    public boolean isPanelExpanded() {
        return getContainer().isDisplayed();
    }
    
    public Histogram getHistogram() {
        if (histogram == null) {
            histogram = new Histogram(ui, this);
        }
        
        return histogram;
    }
    
    public void setTimeRange(TimeRange range) {
        getTimeRangeDropdown().selectFromDropdown(getTimeRangeDropdownMenuSelector(), range.getLabel());
        
        waitForPanelToLoad();
        landingPage.waitForTilesToLoad();
    }
    
    public List<String> getAvailableTimeRangeOptions() {
        return getTimeRangeDropdown().getDropdownOptions(getTimeRangeDropdownMenuSelector());
    }
    
    public TimeRange getSelectedTimeRange() {
        String text = getTimeRangeDropdown().findElement(By.className("control-text")).getText();
        return TimeRange.of(text);
    }
    
    public boolean isModeIndicatorDisplayingLive() {
        return getTimeRangeDropdown().findPageElements(By.cssSelector(".mode-indicator.live")).size() > 0;
    }

    public boolean isModeIndicatorDisplayingHistoric() {
        return getTimeRangeDropdown().findPageElements(By.cssSelector(".mode-indicator.historic")).size() > 0;
    }
    
    public void filterPoorTransactions() {
        filterTransactions(By.className("t-poor-transactions-filter-btn"));
    }
    
    public void filterSlowTransactions() {
        filterTransactions(By.className("t-slow-transactions-filter-btn"));
    }
    
    public void filterFailedTransactions() {
        filterTransactions(By.className("t-failed-transactions-filter-btn"));
    }
    
    private PageElement getSummaryFoldIcon() {
        return findElement(By.className("summary-fold-icon"));
    }
    
    private void filterTransactions(By filterBtnLocator) {
        expandPanel();
        for (PageElement btn : findPageElements(filterBtnLocator)) {
            if (btn.isDisplayed()) {
                btn.click();
                break;
            }
        }
        
        waitForPanelToLoad();
        landingPage.waitForTilesToLoad(true);
    }
    
    private ATCDropDownOpener getTimeRangeDropdown() {
        return new ATCDropDownOpener(findElement(getTimeRangeDropdownSelector()));
    }
    
    private By getTimeRangeDropdownSelector() {
        return By.id("summary-range-selector");
    }
    
    private By getTimeRangeDropdownMenuSelector() {
        return By.id("time-range-selection-combo-menu");
    }
    
    public List<PageElement> getBreadcrumbLevelElements() {
        return findPageElements(By.className("t-breadcrumb-level"));
    }
    
    public WebElement getPoorTransactionsFilterHandle() {
        return Utils.waitUntilVisible(ui.getDriver(), By.cssSelector(".summary-container .apdex-dots-container"));
    }
    
    private By getStartTimeIndicationSelector() {
        return By.id("start-time-indication");
    }
    
    private By getEndTimeIndicationSelector() {
        return By.id("end-time-indication");
    }
    
    public Long getStartTime() {
        return Long.valueOf(findElement(getStartTimeIndicationSelector()).getAttribute("millis"));
    }
    
    public Long getEndTime() {
        return Long.valueOf(findElement(getEndTimeIndicationSelector()).getAttribute("millis"));
    }
    
    public int getSlowTransactionsCount() {
        expandPanel();
        String count = findElement(By.cssSelector(".filter-slow-transactions > tspan")).getText();
        return Integer.valueOf(count);
    }
    
    public void turnOffLiveMode() {
        findElement(By.id("summary-range-selector")).click();
        WebElement rangeCollection = ui.getDriver().findElement(By.id("time-range-selection-combo-menu"));
        rangeCollection.findElement(By.linkText(TimeRange.CUSTOM_RANGE.getLabel())).click();
    }
}
