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

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.element.PageElement;

public class Search {

    private UI ui;
    private static final String SEARCH_BOX_SELECTOR = "div.search-box";

    /**
     * 
     * @param driver
     */
    public Search(UI ui) {
        this.ui = ui;
    }

    /**
     * Return input field element
     */
    public PageElement getInput() {
        clickSearchDropDown();
        return getElement().findElement(By.cssSelector("input[class~=\"search-box-input\"]"));
    }

    /**
     * 
     * @return
     */
    public PageElement getElement() {
        clickSearchDropDown(); 
        return ui.getElementProxy(By.cssSelector(SEARCH_BOX_SELECTOR));
    }

    /**
     * Return close button element
     */
    public PageElement getClose() {
        return getElement().findElement(By.cssSelector("div[class~=\"search-box-close\"] > span"));
    }

    /**
     * Return the "Info" element
     */
    public PageElement getInfo() {
        By locator = By.cssSelector(SEARCH_BOX_SELECTOR + " div[class~=\"search-box-info\"] > span");
        return ui.getElementProxy(locator);
    }

    /**
     * Return the count of search results
     */
    public int getResultsCount() {
        return Integer.valueOf(getInfo().getText().split("/")[1]);
    }

    /**
     * Return the order of the current search result item
     */
    public String getResultOrder() {
        return getInfo().getText().split(" ")[0];
    }

    /**
     * Return the "Previous" button element
     */
    public PageElement getBtnPrev() {
        return getElement().findElement(By.cssSelector("div[class~=\"t-search-prev\"]"));
    }

    /**
     * Return the "Next" button element
     */
    public PageElement getBtnNext() {
        return getElement().findElement(By.cssSelector("div[class~=\"t-search-next\"]"));
    }

    /**
     * Input text to Search dialog
     * 
     * @param {string} text - search string
     */
    public void inputSearch(String text) {
        getInput().clear();
        getInput().sendKeys(text);
        Utils.sleep(500);
    }

    /**
     * Close the Search dialog
     */
    public void close() {
        getClose().click();
        Utils.sleep(100);
    }

    /**
     * Click on the "Previous" button element
     */
    public void clickPrev() {
        getBtnPrev().click();
        Utils.sleep(100);
    }

    /**
     * Click on the "Next" button element
     */
    public void clickNext() {
        getBtnNext().click();
        Utils.sleep(100);
    }

    /**
     * This method exposes the search widget within its drop down button.
     * If the drop down is already in exposed state, then this method is a no-op
     */

    public void clickSearchDropDown( )
    {
        // First try to find the search input div
        By searchInputBox = By.cssSelector("div.search-box");
        PageElement searchInput = ui.getElementProxy(searchInputBox);
        
        // If the widget is not already displayed, then go ahead and expand/display it
        if (!searchInput.isDisplayed()) {            
            By bySearchClass = By.className("search-image-button");
            PageElement divButton = ui.getElementProxy(bySearchClass);
            divButton.click();
        }
    }
}
