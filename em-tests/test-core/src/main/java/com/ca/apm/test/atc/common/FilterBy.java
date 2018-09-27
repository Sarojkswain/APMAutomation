/*
 * Copyright (c) 2015 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.test.atc.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;

import com.ca.apm.test.atc.common.element.ATCCheckBox;
import com.ca.apm.test.atc.common.element.PageElement;

public class FilterBy implements IFilter {

    private static final Logger logger = Logger.getLogger(FilterBy.class);
    
    private static final String FILTER_BY_OPEN_DROPDOWN_CSS = "ul[class~=\"dropdown-menu\"][class~=\"filter-add\"][style*=\"display: block\"]";
        
    private final UI ui;

    public FilterBy(UI ui) throws Exception {
        this.ui = ui;
    }
    
    public int getAddedFilterItemsCount() {
        return getAddedFilterItemsNames().size();
    }
    
    public int getAddedActiveFilterItemsCount() {
        return getAddedActiveFilterItemsNames().size();
    }
    
    public List<String> getAddedFilterItemsNames() {
        List<String> result = new ArrayList<String>();
        for (final PageElement item : getPanel().findPageElements(By.className("combo-filter-shell"))) {
            String itemName = item.findElement(By.className("combo-filter-btn-text")).getText(); 
            itemName = itemName.substring(0, itemName.indexOf(" ("));    
            result.add(itemName);
        }
        return result;
    }
    
    public List<String> getAddedActiveFilterItemsNames() {
        List<String> result = new ArrayList<String>();
        for (final PageElement item : getPanel().findPageElements(By.className("combo-filter-shell"))) {
            if (item.findElement(By.className("combo-filter-switcher-active")).isPresent()) {
                String itemName = item.findElement(By.className("combo-filter-btn-text")).getText(); 
                itemName = itemName.substring(0, itemName.indexOf(" ("));    
                result.add(itemName);
            }
        }
        return result;
    }

    /**
     * Get a list of all filter items within a filter 
     */
    public List<FilterMenu> getFilterItems() throws Exception {
        return getFilterItems(0);
    }

    /**
     * Get a list of filter items within the clause of the given index 
     */
    public List<FilterMenu> getFilterItems(int clauseIndex) throws Exception {
        List<FilterMenu> filterItems = new ArrayList<FilterMenu>();
        for (String filterItemName : getFilterItemNames(clauseIndex)) {
            filterItems.add(new FilterMenu(filterItemName, this, ui));
        }
        return filterItems;
    }
    
    public int getFilterItemCount() throws Exception {
        return isPanelOpen() ? getFilterItems().size() : 0;
    }

    public int getFilterItemCount(int clauseIndex) throws Exception {
        return isPanelOpen() ? getFilterItems(clauseIndex).size() : 0;
    }
    
    /**
     * Get filter item with the index that is starting from the beginning of the filter with no respect to individual clauses.
     * 
     * @param index
     * @return
     * @throws Exception
     */
    public FilterMenu getFilterItem(int index) throws Exception {
        showPanel();
        List<FilterMenu> items = getFilterItems();
        Assert.assertTrue(items.size() > index, "There is no filter item with the index " + index);
        return items.get(index);
    }
   
    @Override
    public void waitForUpdate() {
        ui.waitForWorkIndicator();
    }

    public FilterMenu add(String name) {
        expandFilterByMenu(0);
        
        return selectFilter(name);
    }
    
    public FilterMenu add(String name, int clauseIndex) {
        expandFilterByMenu(clauseIndex);
        
        return selectFilter(name);
    }
    
    public FilterMenu add(String name, int clauseIndex, boolean isBtCoverage, boolean isNewClause) {
        expandFilterByMenu(clauseIndex);
        
        if (isBtCoverage) {
            checkAddAsANewBTCoverage();
        }
        
        if (isNewClause) {
            checkAddAsANewClause();
        }
        
        return selectFilter(name);
    }
    
    public FilterMenu addToBtGroup(String name, int btGroupId) {
        expandFilterMenuInBtGroup(btGroupId);
        
        return selectFilterIntoBtGroup(name);
    }

    /**
     * Remove filter
     * 
     * @param {object} filter
     */
    public void remove(FilterMenu filter) {
        filter.getRemove().click();
        this.waitForUpdate();
    }

    public void removeCompleteFilter() throws Exception {
        while((getFilterClausesCount() > 1) || (getFilterItemCount(0) > 0)) {
            removeFilterClause(0);
        }
    }

    public void removeFilterClause(int clauseIndex) throws Exception {
        if (getFilterItemCount(clauseIndex) > 0) {
            getRemoveClauseCheckBox(clauseIndex).click();
            Utils.sleep(500);
            this.waitForUpdate();
        }
    }
    
    /**
     * Return element of Filters panel
     */
    @Override
    public PageElement getPanel() {
        return ui.getElementProxy(By.className("filter-directive-panel"));
    }
    
    public boolean isPanelOpen() {
        return ui.elementExists(By.className("filter-directive-panel"));
    }

    /**
     * @return list of filter clauses - means the groups of filter items separated by an OR line in the UI
     */
    public List<PageElement> getFilterClauses() {
        return this.getPanel().findPageElements(By.className("filter-clause"));
    }

    public int getFilterClausesCount() {
        return isPanelOpen() ? getFilterClauses().size() : 0;
    }
    
    /**
     * @return list of filter clauses - means the groups of filter items separated by an OR line in the UI
     */
    public PageElement getFilterClause(int clauseIndex) {
        List<PageElement> clauses = getFilterClauses();
        Assert.assertTrue(clauses.size() > clauseIndex, "There is not any clause with the index " + clauseIndex);
        return clauses.get(clauseIndex);
    }
        
    /**
     * @return list of filters added as Transaction filters - divs starting with a bt-in prefix
     */
    public List<PageElement> getFilterItemsInAnyBTCoverage() {
        return this.getPanel().findPageElements(By.cssSelector("div[class*='bt-in']"));
    }
    
    public int getCountOfFilterItemsInAnyBTCoverage() {
        return getFilterItemsInAnyBTCoverage().size();
    }

    /**
     * Return list of all existing filter item names
     */
    public List<String> getFilterItemNames() {
        List<String> result = new ArrayList<String>();
        for (PageElement el : getPanel().findPageElements(By.className("combo-filter-shell"))) {
            String itemName = el.findElement(By.className("combo-filter-btn-text")).getText(); 
            itemName = itemName.substring(0, itemName.indexOf(" ("));    
            result.add(itemName);
        }
        return result;
    }
    
    public List<String> getAddFilterItemNames() {
        expandFilterByMenu(0);
        
        List<String> result = new ArrayList<>();
        List<PageElement> listItems = this.ui.findElements(By.cssSelector(".filter-add.dropdown-menu .vertical-scroll > li > span"));
        
        for (PageElement listItem : listItems) {
            result.add(listItem.getAttribute("innerText"));
        }
        
        collapseFilterByMenu(0);
        return result;
    }

    /**
     * Return list of all filter item names from within the specified clause
     */
    public List<String> getFilterItemNames(int clauseIndex) {
        PageElement clause = getFilterClause(clauseIndex);
        List<String> result = new ArrayList<String>();
        for (final PageElement el : clause.findPageElements(By.className("combo-filter-shell"))) {
            String itemName = el.findElement(By.className("combo-filter-btn-text")).getText(); 
            itemName = itemName.substring(0, itemName.indexOf(" ("));    
            result.add(itemName);
        }
        return result;
    }

    /**
     * Return list of all filter item from within the specified clause as instances of {@link FilterMenu}.
     */
    public List<FilterMenu> getFilterItemObjects(int clauseIndex) {
        PageElement clause = getFilterClause(clauseIndex);
        List<FilterMenu> result = new ArrayList<FilterMenu>();
        for (final PageElement el : clause.findPageElements(By.className("combo-filter-shell"))) {
            String itemName = el.findElement(By.className("combo-filter-btn-text")).getText(); 
            itemName = itemName.substring(0, itemName.indexOf(" ("));

            String btGroupIdString = el.getAttribute("data-bt");
            int btGroupId = FilterMenu.NOT_IN_BT_COVERAGE;
            if ((btGroupIdString != null) && (btGroupIdString.trim().length() > 0)) {
                try {
                    btGroupId = Integer.valueOf(btGroupIdString);
                } catch (NumberFormatException e) {
                    logger.error("Could not parse the value of the data-bt attribute of the filter item '" + itemName + "' into an integer value: '" + btGroupIdString + "'");
                }
            }
            
            FilterMenu obj = new FilterMenu(itemName, this, ui, btGroupId);
            result.add(obj);
        }
        return result;
    }
    
    /**
     * Return the [+] button within the given BT Coverage to add a new filter item into it 
     */
    private PageElement getAddFilterItemToBtGroupButton(int btGroupId) {
        return getPanel().findElement(By.cssSelector("div[class~=\"combo-filter-shell\"][data-bt=\"" + btGroupId + "\"] .combo-filter-add-to-group"));
    }
    
    /**
     * Return the specified filter item element from the specified clause
     */
    public PageElement getFilterItemElement(int clauseIndex, int indexWithinClause) {
        PageElement clause = getFilterClause(clauseIndex);
        List<PageElement> items = clause.findPageElements(By.cssSelector(".combo-filter-shell"));
        Assert.assertTrue(items.size() > indexWithinClause, "There is no filter item with the index " + indexWithinClause + " in the clause " + clauseIndex);
        return items.get(indexWithinClause);
    }
    
    /**
     * Determine whether the passed filter item is a part of any business transaction coverage.
     * 
     * @param el an element representing a filter item in the filter toolbar
     */
    public boolean isFilterItemInBtClause(PageElement el) {
        String bt = el.getAttribute("data-bt");
        return (bt != null) && !bt.isEmpty();
    }
    
    /**
     * Return list of tabs
     */
    public List<PageElement> getListOfTabs() {
        return this.getPanel().findPageElements(
            By.xpath("//div[contains(@ng-repeat, \"tab in tabs\")]"));
    }

    /**
     * Return element of the [+] button (for adding a new filter item) at the end of the filter clause with the given index 
     */
    public PageElement getFilterByBtn(int clauseIndex) {
        if (isPanelOpen()) {
            List<PageElement> buttons = getPanel().findPageElements(By.cssSelector(".filter-clause-add-item > a"));
            Assert.assertTrue(buttons.size() > clauseIndex, "There is not a [+] button with index " + clauseIndex);
            return buttons.get(clauseIndex);
        } else {
            return ui.findElement(By.className("filter-list-toggle"));
        }
    }

    /**
     * Expand the drop-down menu of the button for adding a new filter item to the end of the filter clause with the given index 
     */
    public void expandFilterByMenu(int clauseIndex) {
        PageElement el = this.getFilterByBtn(clauseIndex);
        String isExpanded = el.getAttribute("aria-expanded");
        if (!"true".equals(isExpanded)) {
            el.click();
            Utils.sleep(100);    
        }
    }
    
    /**
     * Expand the drop-down menu of the button for adding a new filter item into a BT Coverage with the given ID 
     */
    public void expandFilterMenuInBtGroup(int btGroupId) {
        PageElement el = this.getAddFilterItemToBtGroupButton(btGroupId);
        String isExpanded = el.getAttribute("aria-expanded");
        if (!"true".equals(isExpanded)) {
            el.click();
            Utils.sleep(100);    
        }
    }

    /**
     * Collapse the menu for adding a new filter with a list of available filters
     */
    public void collapseFilterByMenu(int clauseIndex) {
        PageElement el = this.getFilterByBtn(clauseIndex);
        String isExpanded = el.getAttribute("aria-expanded");
        if ("true".equals(isExpanded)) {
            el.click();
            Utils.sleep(100);    
        }
    }

    /**
     * Collapse the drop-down menu for adding a new filter item into a BT Coverage with the given ID 
     */
    public void collapseFilterMenuInBtGroup(int btGroupId) {
        PageElement el = this.getAddFilterItemToBtGroupButton(btGroupId);
        String isExpanded = el.getAttribute("aria-expanded");
        if ("true".equals(isExpanded)) {
            el.click();
            Utils.sleep(100);    
        }
    }
    
    /**
     * Return all "Filter by" menu entries
     * 
     * @returns {*}
     */
    public List<PageElement> getFilterByMenuList(int clauseIndex) {
        return this.getFilterByBtn(clauseIndex).findPageElements(By.xpath("../ul[@class='dropdown-menu']/li"));
    }

    /**
     * Return the opened/active drop-down menu with a list of filter items to be added to the current filter 
     * 
     * @return
     */
    private PageElement getOpenedAddNewDropdownMenu() {
        By selector = By.cssSelector(FILTER_BY_OPEN_DROPDOWN_CSS);
        return ui.getElementProxy(selector);
    }

    /**
     * Return the scrollable area with just the list of filter items inside the opened/active drop-down menu   
     * 
     * @return
     */
    private PageElement getOpenedAddNewDropdownMenuScrollableFilterItemArea() {
        return getOpenedAddNewDropdownMenu().findElement(By.cssSelector(".vertical-scroll"));
    }
    
    /**
     * Select the desired filter from the expanded list of filters available
     * @param name name of the filter
     * @return the FilterMenu element
     */
    private FilterMenu selectFilter(String name) {
        PageElement el = getElementInFilterBy(name);
        if (getOpenedAddNewDropdownMenuScrollableFilterItemArea().hasVerticalScrollbar()) {
            el.scrollIntoView();
        }
        
        el.click();
        waitForUpdate();

        FilterMenu menu = new FilterMenu(name, this, ui);
        return menu;
    }
    
    /**
     * Select the desired filter from the expanded list of filters available from within the [+] button in the BTgroup
     * @param name name of the filter
     * @return the FilterMenu element
     */
    private FilterMenu selectFilterIntoBtGroup(String name) {
        PageElement el = getElementInFilterBy(name);
        if (getOpenedAddNewDropdownMenu().hasVerticalScrollbar()) {
            el.scrollIntoView();
        }
        
        el.click();
        waitForUpdate();

        FilterMenu menu = new FilterMenu(name, this, ui);
        return menu;
    }

    public List<PageElement> getElementsInFilterBy() {
        return getOpenedAddNewDropdownMenu().findPageElements(By.cssSelector(".filter-add .vertical-scroll li.map-combo-filter-item span"));
    }
    
    /**
     * Return filter element from the pop-up menu that opens after clicking on the [+] button
     * 
     * @param {string} name
     */
    private PageElement getElementInFilterBy(String name) {
        return getOpenedAddNewDropdownMenu().findElement(By.xpath(".//span[text()='" + name + "']/.."));
    }

    /**
     * Return the check-box element that makes the consecutively selected filter item be added as a new BT Coverage element
     */
    private ATCCheckBox getAddAsANewBTCoverageCheckBox() {
        ui.waitUntilVisible(By.cssSelector(FILTER_BY_OPEN_DROPDOWN_CSS + " .t-as-a-new-bt .custom-checkbox"));
        return new ATCCheckBox(
            getOpenedAddNewDropdownMenu().findElement(By.cssSelector(".t-as-a-new-bt .custom-checkbox")));
    }

    /**
     * Return the check-box element that makes the consecutively selected filter item be added as a new disjunctive clause
     */
    private ATCCheckBox getAddAsANewClauseCheckBox() {
        ui.waitUntilVisible(By.cssSelector(FILTER_BY_OPEN_DROPDOWN_CSS + " .t-as-new-clause .custom-checkbox"));
        return new ATCCheckBox(
            getOpenedAddNewDropdownMenu().findElement(By.cssSelector(".t-as-new-clause .custom-checkbox")));
    }
    
    public void checkAddAsANewBTCoverage() {
        ATCCheckBox cb = getAddAsANewBTCoverageCheckBox();
        if (!cb.isCheckboxChecked()) {
            cb.click();
        }
    }

    public boolean isCheckedAddAsANewBTCoverage() {
        return getAddAsANewBTCoverageCheckBox().isCheckboxChecked();
    }

    public void checkAddAsANewClause() {
        ATCCheckBox cb = getAddAsANewClauseCheckBox();
        if (!cb.isCheckboxChecked()) {
            cb.click();
        }
    }

    public boolean isCheckedAddAsANewClause() {
        ATCCheckBox cb = getAddAsANewClauseCheckBox();
        return cb.isCheckboxChecked();
    }
    
    /**
     * Return "Remove all filters of the clause" checkbox element
     */
    public ATCCheckBox getRemoveClauseCheckBox(int clauseIndex) {
        PageElement clause = getFilterClause(clauseIndex); 
        return new ATCCheckBox(clause.findElement(By.cssSelector(".filter-clause-control"))
            .findElement(By.cssSelector(".filter-clause-control-remove")));
    }

    public enum UniverseButton {
        SAVE("buttonSave"), SAVE_AS("buttonSaveAs"), CANCEL("buttonCancel");
        
        private String name;
        
        private UniverseButton(String name) {
            this.name = name;
        }
        
        public By getSelector() {
            return By.cssSelector("button[name=\"" + name + "\"]"); 
        }
    }
    
    private PageElement getUniverseButton(UniverseButton button) {
        return ui.getElementProxy(button.getSelector());
    }

    public PageElement getSaveUniverseButton() {
        return getUniverseButton(UniverseButton.SAVE);
    }

    public PageElement getSaveAsUniverseButton() {
        return getUniverseButton(UniverseButton.SAVE_AS);
    }

    public PageElement getCancelUniverseEditingButton() {
        return getUniverseButton(UniverseButton.CANCEL);
    }

    public void cancelUniverseEditing() {
        getCancelUniverseEditingButton().click();
        ui.waitForWorkIndicator(By.cssSelector(".work-indicator"));
    }

    private boolean isUniverseButtonVisible(UniverseButton button) {
        return ui.getElementProxy(button.getSelector(), 2).isDisplayed();
    }

    public boolean isSaveUniverseButtonVisible() {
        return isUniverseButtonVisible(UniverseButton.SAVE);
    }

    public boolean isSaveAsUniverseButtonVisible() {
        return isUniverseButtonVisible(UniverseButton.SAVE_AS);
    }

    public boolean isCancelUniverseEditingVisible() {
        return isUniverseButtonVisible(UniverseButton.CANCEL);
    }

    private PageElement getShowEntryElement() {
        By selector = By.className("global-filter-show-entry");
        return ui.getElementProxy(selector);
    }

    public boolean isCheckedShowEntryElement() {
        PageElement el = getShowEntryElement();
        String classString = el.getAttribute("class");
        return classString.indexOf("inactive") == -1;
    }

    public void checkShowEntryElement() {
        if (!isCheckedShowEntryElement()) {
            getShowEntryElement().click();
        }
    }

    public void uncheckShowEntryElement() {
        if (isCheckedShowEntryElement()) {
            getShowEntryElement().click();
        } 
    }

    /**
     * If the specified filter item is an item that starts a BT Coverage, this drop zone is beyond 
     * the BT Coverage border. Dropping another item means that such an item is moved before this
     * element, but not inside the BT Coverage.
     *      
     * @param clauseIndex
     * @param indexWithinClause
     * @return
     */
    public PageElement getDropZoneBeforeFilterItem(int clauseIndex, int indexWithinClause) {
        PageElement filterItem = getFilterItemElement(clauseIndex, indexWithinClause);
        return filterItem.findElement(By.cssSelector(".combo-filter-drop.before"));
    }
    
    /**
     * If the specified filter item is an item that starts a BT Coverage, this drop zone is inside 
     * the BT Coverage border. Dropping another item means that such an item is moved before this
     * element inside the BT Coverage to which this item belongs.
     * 
     * If the specified filter item is not an item that starts a BT Coverage, this zone does not exist.
     * In such a case use the method {@link #getDropZoneBeforeFilterItem(int, int)}.
     * 
     * @param clauseIndex
     * @param indexWithinClause
     * @return
     */
    public PageElement getDropZoneAtStartOfFilterItem(int clauseIndex, int indexWithinClause) {
        PageElement filterItem = getFilterItemElement(clauseIndex, indexWithinClause);
        return filterItem.findElement(By.cssSelector(".combo-filter-drop.start"));
    }
    
    /**
     * If the specified filter item is an item that finishes a BT Coverage, this drop zone is beyond 
     * the BT Coverage border. Dropping another item means that such an item is moved after this
     * element, but not inside the BT Coverage.
     * 
     * The after drop zone is displayed only when the filter item is at the end of a clause of filter items.
     * Otherwise use the zone before the next element.
     *      
     * @param clauseIndex
     * @param indexWithinClause
     * @return
     */
    public PageElement getDropZoneAfterFilterItem(int clauseIndex, int indexWithinClause) {
        PageElement filterItem = getFilterItemElement(clauseIndex, indexWithinClause);
        return filterItem.findElement(By.cssSelector(".combo-filter-drop.after"));
    }
    
    /**
     * If the specified filter item is an item that finishes a BT Coverage, this drop zone is inside 
     * the BT Coverage border. Dropping another item means that such an item is moved before this
     * element inside the BT Coverage to which this item belongs.
     * 
     * If the specified filter item is not an item that finishes a BT Coverage, this zone does not exist.
     * In such a case use the method {@link #getDropZoneAfterFilterItem(int, int)}.
     * 
     * @param clauseIndex
     * @param indexWithinClause
     * @return
     */
    public PageElement getDropZoneAtEndOfFilterItem(int clauseIndex, int indexWithinClause) {
        PageElement filterItem = getFilterItemElement(clauseIndex, indexWithinClause);
        return filterItem.findElement(By.cssSelector(".combo-filter-drop.end"));
    }
    
    public void performDragAndDrop(String dragElementSelector, String dropElementSelector) throws Exception {
        Html5DragAndDropSupport.performDragAndDrop(ui.getDriver(), dragElementSelector, dropElementSelector);
    }
    
    public String getDraggableItemSelector(int id) {
        return "div[data-filter-id=\"" + id + "\"]";
    }
    
    /**
     * If the specified filter item is an item that starts a BT Coverage, this drop zone is beyond 
     * the BT Coverage border. Dropping another item here means that such an item is moved before this
     * element, but not inside the BT Coverage.
     *      
     * @param id id of the filter item  
     */
    public String getDropZoneBeforeItemSelector(int id) {
        return "div[data-filter-id=\"" + id + "\"] .combo-filter-drop.before";
    }
    
    /** 
     * If the specified filter item is an item that finishes a BT Coverage, this drop zone is beyond 
     * the BT Coverage border. Dropping another item here means that such an item is moved after this
     * element, but not inside the BT Coverage.
     * 
     * The after drop zone is displayed only when the filter item is at the end of a clause of filter items.
     * Otherwise use the zone before the next element.
     *      
     * @param id id of the filter item  
     */
    public String getDropZoneAfterItemSelector(int id) {
        return "div[data-filter-id=\"" + id + "\"] .combo-filter-drop.after";
    }
    
    /**
     * If the specified filter item is an item that starts a BT Coverage, this drop zone is inside 
     * the BT Coverage border. Dropping another item here means that such an item is moved before this
     * element inside the BT Coverage to which this item belongs.
     * 
     * If the specified filter item is not an item that starts a BT Coverage, this zone does not exist.
     * In such a case use the method {@link #getDropZoneBeforeItemSelector(int)}.
     *      
     * @param id id of the filter item  
     */
    public String getDropZoneAtStartOfItemSelector(int id) {
        return "div[data-filter-id=\"" + id + "\"] .combo-filter-drop.start";
    }
    
    /** 
     * If the specified filter item is an item that finishes a BT Coverage, this drop zone is inside 
     * the BT Coverage border. Dropping another item here means that such an item is moved before this
     * element inside the BT Coverage to which this item belongs.
     * 
     * If the specified filter item is not an item that finishes a BT Coverage, this zone does not exist.
     * In such a case use the method {@link #getDropZoneAfterItemSelector(int)}.
     *      
     * @param id id of the filter item  
     */
    public String getDropZoneAtEndOfItemSelector(int id) {
        return "div[data-filter-id=\"" + id + "\"] .combo-filter-drop.end";
    }
    
    public void hidePanel() {
        if (isPanelOpen()) {
            ui.getElementProxy(By.cssSelector("div[ng-click=\"toggleFilterPanel()\"]")).click();
        }
    }
    
    public void showPanel() {
        if (!isPanelOpen()) {
            ui.getElementProxy(By.cssSelector("div[ng-click=\"toggleFilterPanel()\"]")).click();
        }
    }
}
