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

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.element.ATCCheckBox;
import com.ca.apm.test.atc.common.element.PageElement;

public class FilterMenu {
    
    public final static int NOT_IN_BT_COVERAGE = -1;
    
    private final static String OPEN_FILTER_COMBO_SELECTOR = "div[class~=\"dropdown-menu\"][class~=\"combo-filter-dropdown\"][style*=\"display: block\"]";
        
    private String name;
    private IFilter filter;
    private UI ui;
    private boolean isAppendToBody;
    private int btGroupId;

    public FilterMenu(String name, IFilter filter, UI ui) {
        this(name, filter, ui, NOT_IN_BT_COVERAGE);
    }

    public FilterMenu(String name, IFilter filter, UI ui, int btGroupId) {
        this.filter = filter;
        this.name = name;
        this.ui = ui;
        this.isAppendToBody = true;
        this.btGroupId = btGroupId;
    }
    
    public boolean isAppendToBody() {
        return isAppendToBody;
    }

    public void setAppendToBody(boolean isAppendToBody) {
        this.isAppendToBody = isAppendToBody;
    }

    public String getName() {
        return name;
    }

    public int getBtGroupId() {
        return btGroupId;
    }
    
    /**
     * Return element of filter items panel
     */
    public PageElement getPanel() {
        return filter.getPanel();
    }

    /**
     * Return element of filter menu
     */
    public PageElement getMenuContainer() {
        // notice the space and opening bracket after filter name
        return this.getPanel().findElement(
            By.xpath(".//div[text()[starts-with(.,'" + name + " (')]]/../../.."));
    }

    public PageElement getMenu() {
        if (isAppendToBody) {
            return ui.getElementProxy(By.cssSelector(OPEN_FILTER_COMBO_SELECTOR));
        } else {
            // notice the space and opening bracket after filter name
            return this.getMenuContainer().findElement(By.cssSelector(".combo-filter-dropdown"));
        }
    }

    /**
     * Return element for "dropdown-toggle"
     * (used for determining if filter menu is disabled)
     */
    public PageElement getDropdownToggle() {
        return this.getMenuContainer();
    }

    /**
     * Return list of items in filter menu
     */
    public List<PageElement> getListOfMenuItems() {
        // "No results" matches here on purpose
        ui.waitUntilElementsVisible(By.cssSelector(OPEN_FILTER_COMBO_SELECTOR + " .combo-filter-item-text"));
        
        return ui.findElements(By.cssSelector(OPEN_FILTER_COMBO_SELECTOR + " .combo-filter-item"));
    }

    /**
     * Return list of item name elements in filter menu
     */
    public List<PageElement> getListOfMenuItemNameElements() {
        By anyItemTextSelector =
            By.cssSelector(OPEN_FILTER_COMBO_SELECTOR
                + " .combo-filter-item .combo-filter-item-text");
        return ui.waitUntilElementsVisible(anyItemTextSelector);
    }

    /**
     * Return array of menu element names in filter
     */
    public String[] getArrayOfMenuItemsNames() {
        List<PageElement> l = this.getListOfMenuItemNameElements();
        List<String> toReturn = new ArrayList<String>();
        for (PageElement w : l) {
            toReturn.add(w.getText());
        }

        return toReturn.toArray(new String[toReturn.size()]);
    };

    /**
     * Return element for removing the filter
     */
    public PageElement getRemove() {
        return this.getMenuContainer().findElement(
            By.cssSelector(".combo-filter-remove"));
    }

    /**
     * Return element for enabling/disabling the filter menu
     */
    private PageElement getMenuOnOff() {
        return this.getMenuContainer().findElement(
            By.cssSelector(".combo-filter-switcher"));
    }
    
    public boolean isAllOnAllOffMenuChecked() {
        return getMenuOnOff().getAttribute("class").contains("combo-filter-switcher-active");
    }

    /**
     * Return element of filter menu "Select All" checkbox
     */
    public ATCCheckBox getMenuSelectAll() {
        By selectAllSelector = By.cssSelector(OPEN_FILTER_COMBO_SELECTOR + " .combo-filter-items .t-select-all-cb");
        return new ATCCheckBox(ui.getElementProxy(selectAllSelector));
    }

    /**
     * Return element of filter menu "Add current selection to filter" checkbox
     */
    public PageElement getMenuOptionAddToSelection() {
        By addToSelectSelector = By.cssSelector(OPEN_FILTER_COMBO_SELECTOR + " .combo-filter-items div[cc-checked=\"addToSelection\"]");
        return ui.getElementProxy(addToSelectSelector);
    }

    /**
     * Return element of filter menu item checkbox
     * 
     * @param {string} name
     */
    public ATCCheckBox getMenuItem(String name) {
        // Wait until any item visible
        ui.waitUntilElementsVisible(By.cssSelector(OPEN_FILTER_COMBO_SELECTOR + " .combo-filter-item-text"));
        
        // Get the item
        PageElement menu = getMenu();
        PageElement items = menu.findElement(By.cssSelector("div.combo-filter-items"));
        PageElement item = items.findElement(By.xpath(".//span[contains(text(), '" + name + "')]/.."));
        PageElement checkBox = item.findElement(By.xpath("./div[contains(concat(' ', normalize-space(@class), ' '), ' custom-checkbox ')]"));
        return new ATCCheckBox(checkBox);
    }

    private By getMenuOkSelector() {
        return By.cssSelector("div.combo-filter-footer button.combo-filter-button-ok");
    }

    /**
     * Return element of filter menu OK button
     */
    public PageElement getMenuOK() {
        return this.getMenu().findElement(getMenuOkSelector());
    }

    /**
     * Return element of filter menu Add button
     */
    public PageElement getMenuAdd() {
        return this.getMenu().findElement(
            By.cssSelector("div.combo-filter-footer button.combo-filter-button-add"));
    }

    /**
     * Return element of filter menu Cancel button
     */
    public PageElement getMenuCancel() {
        return this.getMenu().findElement(
            By.cssSelector("div.combo-filter-footer button.combo-filter-button-cancel"));
    }
    
    /**
     * Return element for sorting in the filter menu
     * 
     * @param {string} sort
     */
    public PageElement getMenuSorting(String sort) {
        return this.getMenu().findElement(By.cssSelector("div.combo-filter-header"))
            .findElement(By.xpath("div[text()[contains(.,'" + sort + "')]]"));
    }

    /**
     * Return element for filtering in the filter menu
     */
    public PageElement getMenuFilter() {
        return this.getMenu().findElement(By.cssSelector("div.combo-filter-header"))
            .findElement(By.cssSelector("input[placeholder=\"Filter\"]"));
    }

    /**
     * Return element for clear filtering in the filter menu
     */
    public PageElement getMenuClearFilter() {
        return this.getMenu().findElement(By.cssSelector("div.combo-filter-header"))
            .findElement(By.cssSelector("span.clear-input"));
    }

    /**
     * Return list of selected items in the filter menu
     */
    public List<PageElement> getListOfSelectedItems() {
        List<PageElement> visItems = getListOfMenuItems();
        List<PageElement> toReturn = new ArrayList<PageElement>();
        for (PageElement el : visItems) {
            if (el.findPageElements(By.cssSelector(".custom-checkbox-checked")).size() == 1) {
                toReturn.add(el);
            }
        }
        return toReturn;
    };
    
    /**
     * Return list of menu element names in filter that are selected
     */
    public List<String> getListOfSelectedMenuItemsNames() {
        List<PageElement> l = this.getListOfSelectedItems();
        List<String> toReturn = new ArrayList<String>();
        for (PageElement w : l) {
            toReturn.add(w.getText().trim());
        }

        return toReturn;
    };
    
    /**
     * Return array of menu element names in filter that are selected
     */
    public String[] getArrayOfSelectedMenuItemsNames() {
        List<PageElement> l = this.getListOfSelectedItems();
        List<String> toReturn = new ArrayList<String>();
        for (PageElement w : l) {
            toReturn.add(w.getText());
        }

        return toReturn.toArray(new String[toReturn.size()]);
    };
    
    private boolean isMenuExpanded() {
        boolean comboMarkedAsOpen = getMenuContainer().findPageElements(By.cssSelector(".combo-filter-marker-active")).size() > 0;
        boolean menuIsOpen = ui.findElements(By.cssSelector(OPEN_FILTER_COMBO_SELECTOR)).size() > 0;
        return comboMarkedAsOpen && menuIsOpen;
    }
    
    /**
     * Expand filter menu
     */
    public void expandDropDownMenu() {
        if (!isMenuExpanded()) {
            getMenuContainer().click();
            ui.waitUntilVisible(By.cssSelector(OPEN_FILTER_COMBO_SELECTOR));
            Utils.sleep(250);
        }
    }

    /**
     * Collapse filter menu
     */
    public void collapseDropDownMenu() {
        if (isMenuExpanded()) {
            getMenuContainer().click();
            ui.waitWhileVisible(By.cssSelector(OPEN_FILTER_COMBO_SELECTOR));
            Utils.sleep(250);
        }
    }
    
    public void enableFilterItem() {
        PageElement checkBox =  this.getMenuOnOff();
        String classString = checkBox.getAttribute("class");
        if (classString.indexOf("combo-filter-switcher-active") == -1) {
            checkBox.click();
            filter.waitForUpdate();
        }
    }
    
    public void disableFilterItem() {
        PageElement checkBox =  this.getMenuOnOff();
        String classString = checkBox.getAttribute("class");
        if (classString.indexOf("combo-filter-switcher-active") != -1) {
            checkBox.click();
            filter.waitForUpdate();
        }
    }
        
    /**
     * Confirm filter menu
     * 
     * @throws Exception
     */
    public void confirmMenu() {
        getMenuOK().click();
        filter.waitForUpdate();
        Utils.sleep(250);
    }

    /**
     * Cancel filter menu
     * 
     * @throws Exception
     */
    public void cancelMenu() {
        getMenuCancel().click();
        filter.waitForUpdate();
        Utils.sleep(250);
    }

    /**
     * Add to selection
     */
    public void addToSelection() {
        getMenuAdd().click();
        ui.waitUntilVisible(getMenuOkSelector());
        Utils.sleep(250);
    }

    /**
     * Select sorting for given filter
     * 
     * @param {string} sort
     */
    public void sort(String sort) {
        getMenuSorting(sort).click();
    }

    /**
     * Click on "Select All"
     * 
     * @deprecated use {{@link #checkSelectAll() or @link #uncheckSelectAll()}
     */
    public void clickOnSelectAll() {
        PageElement el = this.getMenuSelectAll();
        el.click();
    }
    
    /**
     * Check the option "Select All"
     */
    public void checkSelectAll() {
        PageElement el = this.getMenuSelectAll();
        String classString = el.getAttribute("class");
        if (!classString.contains("custom-checkbox-checked")) {
            el.click();
        }
    }    

    /**
     * Uncheck the option "Select All"
     */
    public void uncheckSelectAll() {
        PageElement el = this.getMenuSelectAll();
        String classString = el.getAttribute("class");
        if (classString.contains("custom-checkbox-3state")) {
            el.click();
        }
        el = this.getMenuSelectAll();
        classString = el.getAttribute("class");
        if (classString.contains("custom-checkbox-checked")) {
            el.click();
        }
    }  
    
    /**
     * Click on "Add to filter"
     * 
     * @deprecated use {@link #checkAddToSelection() or #uncheckAddToSelection()}
     */
    public void clickOnAddToSelection() {
        PageElement el = this.getMenuOptionAddToSelection();
        el.click();
    }
    
    public void checkAddToSelection() {
        PageElement el = this.getMenuOptionAddToSelection();
        String classString = el.getAttribute("class");
        if (!classString.contains("custom-checkbox-checked")) {
            el.click();
        }
    }

    public void uncheckAddToSelection() {
        PageElement el = this.getMenuOptionAddToSelection();
        String classString = el.getAttribute("class");
        if (classString.contains("custom-checkbox-checked")) {
            el.click();
        }
    }

    public void checkMenuOption(String name) {
        PageElement el = this.getMenuItem(name);
        String classString = el.getAttribute("class");
        if (!classString.contains("custom-checkbox-checked")) {
            el.click();
        }
    }
    
    public void uncheckMenuOption(String name) {
        PageElement el = this.getMenuItem(name);
        String classString = el.getAttribute("class");
        if (classString.contains("custom-checkbox-checked")) {
            el.click();
        }
    }

    public int getMenuSelectedItemsCountPerFilterTitle() {
        String input = getMenuContainer().findElement(By.className("combo-filter-btn-text")).getText();
        String pattern = ".+\\((\\d)\\)";
        String numberText = input.replaceAll(pattern, "$1");
        
        try {
            return Integer.valueOf(numberText);
        } catch (NumberFormatException ne) {
            return 0;
        }
    }
}
