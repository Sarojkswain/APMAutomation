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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.element.PageElement;

public class DataBrushing implements IFilter {

    private Map<String, FilterMenu> filters = new HashMap<String, FilterMenu>();

    private final UI ui;

    public DataBrushing(UI ui) {
        this.ui = ui;
    }

    /**
     * Add new filter - returns new instance of Filter object
     * 
     * @param String name
     * @throws Exception 
     *            
     */
    public FilterMenu add(String name) {
        if (!filters.containsKey(name)) {
            expandShowMeMenu();
            selectElementInShowMe(name);
            filters.put(name, new FilterMenu(name, this, ui));
        }

        return filters.get(name);
    }

    /**
     * Return filter element in "Show me" menu
     * 
     * @param {String} name
     */
    public PageElement getElementInShowMe(String name) {
        return getShowMeMenuOptions().findElement(By.xpath(".//li/span[text()='" + name + "']/.."));
    }

    /**
     * Select filter element in "Show me" menu
     * 
     * @param {String} name
     * @throws Exception 
     */
    public void selectElementInShowMe(String name) {
        getElementInShowMe(name).click();
        waitForUpdate();
    }

    /**
     * Expand/Collapse "Show me" menu with list of available filters
     * 
     * @throws Exception
     */
    public void expandShowMeMenu() {
        if (!getShowMeMenuOptions().isDisplayed()) {
            getShowMeBtn().click();
            Utils.sleep(100);
        }
    }

    /**
     * Return element of "Show me" menu button
     */
    public PageElement getShowMeBtn() {
        By btnSelector = By.cssSelector(".t-highlight-panel .filter-clause-add-item > a");
        return ui.getElementProxy(btnSelector);
    }
    
    public PageElement getShowMeMenuOptions() {
        return ui.getElementProxy(By.cssSelector("ul.dropdown-menu.highlight"));
    }

    public List<PageElement> getShowMeItems() {
        return this.getShowMeMenuOptions().findPageElements(By.cssSelector(".map-combo-filter-item"));
    }
    
    /**
     * Return element of Highlighting panel
     */
    @Override
    public PageElement getPanel() {
        return ui.getElementProxy(By.cssSelector("div[class~=\"t-highlight-panel\"]"));
    }

    /**
     * 
     */
    @Override
    public void waitForUpdate() {
        Utils.sleep(500);
    }

    /**
     * 
     * @return
     */
    public Map<String, FilterMenu> getFilters() {
        return filters;
    }

    /**
     * 
     * @param nameBrusher
     */
    public void remove(FilterMenu brusher) {
       brusher.getRemove().click();
       Utils.sleep(400);
       filters.remove(brusher.getName());
    }
    
    public void removeAll() {
        ui.getElementProxy(By.cssSelector(".t-highlight-panel .filter-clause-control-remove")).click();
        filters.clear();
        Utils.sleep(400);
    }
}
