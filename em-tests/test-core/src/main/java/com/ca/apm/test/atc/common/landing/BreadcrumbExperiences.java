package com.ca.apm.test.atc.common.landing;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.ATCDropDownOpener;
import com.ca.apm.test.atc.common.element.ElementConditionWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class BreadcrumbExperiences extends ElementConditionWrapper {

    public enum GroupedBy {
        GROUPED_BY_NAME("Name");

        private final String text;

        private GroupedBy(String text) {
            this.text = text;
        }

        public static GroupedBy of(String value) {
            for (GroupedBy groupedBy : values()) {
                if (groupedBy.text.equals(value)) {
                    return groupedBy;
                }
            }

            return null;
        }

        public String getText() {
            return text;
        }
    }
    
    public enum SortedBy {
        SORTED_BY_NAME("Name", true, true),
        SORTED_BY_MY_ORDER("My Order", true, false), 
        SORTED_BY_TRANSACTIONS_VOLUME("Transactions Volume", false, true), 
        SORTED_BY_TRANSACTIONS_HEALTH("Transactions Health", false, true), 
        SORTED_BY_EXPERIENCE_STATUS("Experience Status", false, true),
        SORTED_BY_FAILED_COUNT("Failed count", false, true), 
        SORTED_BY_SLOW_COUNT("Slow count", false, true);

        private final String text;
        private final boolean isOnTop;
        private final boolean isIn2ndAndDeeperLevelsOfBV;
        

        private SortedBy(String text, boolean isOnTop, boolean isIn2ndAndDeeperLevelsOfBV) {
            this.text = text;
            this.isOnTop = isOnTop;
            this.isIn2ndAndDeeperLevelsOfBV = isIn2ndAndDeeperLevelsOfBV;
        }

        public static SortedBy of(String value) {
            for (SortedBy st : values()) {
                if (st.text.equals(value)) {
                    return st;
                }
            }

            return null;
        }

        public boolean isOnTop() {
            return isOnTop;
        }

        public boolean isIn2ndAndDeeperLevelsOfBV() {
            return isIn2ndAndDeeperLevelsOfBV;
        }

        public String getText() {
            return text;
        }
    }

    public enum TileSpecialFiltering {
        SLOW_TRANSACTIONS("Filtered view: Slow Transactions"), 
        POOR_TRANSACTIONS("Filtered view: Poor Transactions"), 
        FAILED_TRANSACTIONS("Filtered view: Failed Transactions"),
        FILTERED_EXPERIENCES("Filtered Experiences");

        private final String text;

        private TileSpecialFiltering(String text) {
            this.text = text;
        }

        public static TileSpecialFiltering of(String value) {
            for (TileSpecialFiltering st : values()) {
                if (st.text.equals(value)) {
                    return st;
                }
            }

            return null;
        }
    }

    private final LandingPage landingPage;
    
    private TilesInDangerIndicator tilesInDangerIndicator = null;

    public BreadcrumbExperiences(UI ui, LandingPage landingPage) {
        super(ui, By.id("home-breadcrumb-experiences"));
        this.landingPage = landingPage;
    }
    
    public TilesInDangerIndicator getTilesInDangerIndicator() {
        if (tilesInDangerIndicator == null) {
            tilesInDangerIndicator = new TilesInDangerIndicator(ui, this);
        }
        return tilesInDangerIndicator;
    }
    
    private PageElement getSortedByContainer() {
        return findElement(By.className("t-sorted-by-combo"));
    }
    
    public boolean isGoBackButtonPresent() {
        return findElement(By.className("go-back-btn")).isDisplayed();
    }
    
    public boolean isSortedByDropdownPresent() {
        return !findPageElements(By.className("t-sorted-by-combo")).isEmpty();
    }
    
    private PageElement getSortedByDropdownToggle() {
        return getSortedByContainer().findElement(By.className("dropdown-toggle"));
    }
    
    private boolean isSortedByDropdownExpanded() {
        String val = getSortedByDropdownToggle().getAttribute("aria-expanded");
        return Boolean.valueOf(val);
    }

    private PageElement getSortedByDropdownMenu() {
        return ui.getElementProxy(By.className("t-sorted-by-dropdown"));
    }
    
    public List<String> getSortedByDropdownMenuOptions() {
        if (!isSortedByDropdownExpanded()) {
            getSortedByDropdownToggle().click();
        }
        
        List<PageElement> itemElements = getSortedByDropdownMenu().findPageElements(By.xpath("//li/a"));
        List<String> itemLabels = new ArrayList<String>();
        for (PageElement el : itemElements) {
            itemLabels.add(el.getText());
        }
        
        return itemLabels;
    }

    public SortedBy getSortedBy() {
        String sortedByText = getSortedByContainer().findElement(By.cssSelector(".dropdown-toggle span")).getText();
        return SortedBy.of(sortedByText);
    }
    
    public GroupedBy getGroupedBy() {
        String groupedByText = getCustomAttributeName();
        return GroupedBy.of(groupedByText);
    }

    public PageElement getTilesTitleElement() {
        for (PageElement titleElement : findPageElements(By.className("t-tiles-title"))) {
            if (titleElement.isDisplayed()) {
                return titleElement;
            }
        }
        return null;
    }

    public PageElement getCancelSpecialViewElement() {
        return findElement(By.className("t-special-view"));
    }
    
    public PageElement getCancelTilesFilterElement() {
        return findElement(By.className("t-filtered-view"));
    }
    
    public PageElement getGoBackBtnElement() {
        return findElement(By.className("go-back-btn"));
    }

    public boolean isTileSpecialView() {
        return getTilesTitleElement().getAttribute("class").contains("t-special-view");
    }

    public boolean isTileFilteredView() {
        return getTilesTitleElement().getAttribute("class").contains("t-filtered-view");
    }

    public void cancelSpecialView() {
        getCancelSpecialViewElement().click();
        landingPage.waitForTilesToLoad();
    }
    
    public void cancelTilesFilter() {
        getCancelTilesFilterElement().click();
        landingPage.waitForTilesToLoad();
    }
    
    public void goOneLevelBack() {
        getGoBackBtnElement().click();
        landingPage.waitForTilesToLoad();
    }

    public TileSpecialFiltering getTileSpecialFiltering() {
        if (isTileSpecialView()) {
            return TileSpecialFiltering.of(getTilesTitleElement().getText());
        } else {
            return null;
        }
    }
    
    public TileSpecialFiltering getTileSelectionFiltering() {
        if (isTileFilteredView()) {
            return TileSpecialFiltering.of(getTilesTitleElement().getText());
        }
        return null;
    }
    
    public void setSortedBy(SortedBy sortedBy) {
        if (!isSortedByDropdownExpanded()) {
            getSortedByDropdownToggle().click();
        }
        
        PageElement menuItem = getSortedByDropdownMenu().findElement(By.xpath("//li/a[text() = \"" + sortedBy.getText() + "\"]"));
        menuItem.click();
        
        Utils.sleep(250);
    }
    
    public boolean isCustomAttributeDropdownPresent() {
        return findPageElements(By.className("t-custom-attribute")).size() > 0;
    }
    
    public void setCustomAttribute(String attributeName) {
        ATCDropDownOpener dropdownLink = new ATCDropDownOpener(findElement(By.className("t-custom-attribute")));
        dropdownLink.selectFromDropdown(By.className("t-custom-attribute-dropdown"), attributeName);
        landingPage.waitForTilesToLoad();
    }
    
    public String getCustomAttributeName() {
        return findElement(By.className("t-custom-attribute")).getText().trim();
    }
    
    public void cancelAllFilters() {
        for (WebElement cancelBtn : findElements(By.className("filter-enabled"))) {
            if (cancelBtn.isDisplayed()) {
                cancelBtn.click();
            }
        }
    }
}
