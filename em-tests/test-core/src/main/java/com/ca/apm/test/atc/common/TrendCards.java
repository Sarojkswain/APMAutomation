package com.ca.apm.test.atc.common;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.ca.apm.test.atc.common.element.PageElement;

public class TrendCards {
    
    private final UI ui;
    
    public TrendCards(UI ui) {
        this.ui = ui;
    }
    
    /**
     * Wait until filters are refreshed
     */
    public void waitForUpdate() {
        ui.waitWhileVisible(By.cssSelector("div[show=\"FilterService.refreshTrendDataIndicator\"]"));
        ui.waitUntilVisible(By.cssSelector(".trend-tab"));
    }
    
    public PageElement getTrendCardsContainer() {
        return ui.getElementProxy(By.id("trend-card-container"));
    }
    
    /**
     * Get the outermost trend view element
     */
    public PageElement getTrendCardsPanel() {
        return ui.getElementProxy(By.id("trend-card-panel"));
    }
    
    /**
     * Return Tab element by index
     * 
     * @param index starting from 0
     * @return
     */
    public PageElement getTab(int index) {
        this.waitForUpdate();
        return ui.getElementProxy(By.cssSelector(".trend-tab:nth-of-type(" + (index + 1) + ")"));
    }

    /**
     * Return Tab elements
     * 
     * @return
     */
    public List<PageElement> getTabs() {
        this.waitForUpdate();
        return ui.waitUntilElementsVisible(By.className("trend-tab"), 10);
    }

    /**
     * Return Tab header text (group name)
     * 
     * @param element representing a tab
     * @return
     */
    public String getTabHeaderText(WebElement tabElement) {
        return tabElement.findElement(By.className("t-group-name")).getText();
    }
    
    /**
     * Return Tab header text (group name)
     * 
     * @param index starting from 0
     * @return
     */
    public String getTabHeaderText(int index) {
        PageElement el = getTab(index);
        return el.findElement(By.className("t-group-name")).getText();
    }

    /**
     * Return array of tab header texts
     */
    public String[] getArrayOfTabHeaderTexts() {
        List<PageElement> tabs = getTabs();
        String[] toReturn = new String[tabs.size()];
        for (int i = 0; i < tabs.size(); i++) {
            toReturn[i] = getTabHeaderText(tabs.get(i));
        }
        return toReturn;
    }

    /**
     * Return list of cards 
     */
    public List<PageElement> getListOfCards() {
        return this.getTrendCardsContainer().findPageElements(By.className("db-list-main-entry-container"));
    }
    
    /**
     * Return list of trend card elements
     */
    public List<PageElement> getListOfCardWrappers() {
        return ui.waitUntilElementsVisible(By.className("trend-card-wrapper"), 10);
    }

    /**
     * Return list of card name elements
     */
    public List<PageElement> getListOfCardNameElementsWithinElement(PageElement container) {
        return container.findPageElements(By.className("type-main-list-name"));
    }
    
    /**
     * Return list of card name elements
     */
    public List<PageElement> getListOfCardNameElements() {
        return getListOfCardNameElementsWithinElement(getTrendCardsContainer());
    }

    /**
     * Return list of card name elements within the given tab
     */
    public List<PageElement> getListOfCardNameElementsWithinTab(int index) {
        return getListOfCardNameElementsWithinElement(getTab(index));
    }

    /**
     * Return array of card element names 
     */
    public List<String> getListOfCardNamesWithinElement(PageElement container) {
        List<PageElement> els = getListOfCardNameElementsWithinElement(container);
        List<String> toReturn = new ArrayList<String>();
        for (PageElement el : els) {
            toReturn.add(el.getText());
        }
        return toReturn;
    }
    
    /**
     * Return array of card element names 
     */
    public List<String> getListOfAllCardNames() {
        return getListOfCardNamesWithinElement(getTrendCardsContainer());
    }
    
    /**
     * Return array of card element names within the given tab
     */
    public List<String> getArrayOfCardNamesWithinTab(int index) {
        return getListOfCardNamesWithinElement(getTab(index));
    }
    
    private By getCardByNameLocator(String name) {
        return By.xpath(
            ".//div[contains(concat(' ', normalize-space(@class), ' '), ' db-list-main-entry-container ')]" +
            "/div/*[text()='" + name + "']/../..");
    }

    /**
     * Return app element in filter based on name
     * 
     * @param {string} name
     */
    public PageElement getCardByName(String name) {
        return this.getTrendCardsContainer().findElement(getCardByNameLocator(name));
    }

    public boolean isCardByNamePresent(String name) {
        return this.getTrendCardsContainer().findPageElements(getCardByNameLocator(name)).size() > 0;
    }
    
    public PageElement getLinkToMapByNodeName(String name) {
        PageElement element = this.getCardByName(name).findElement(By.xpath("./div/a"));
        element.scrollIntoView();
        return element;
    }
    
    public PageElement getCardHeaderByNodeName(String name) {
        return this.getCardByName(name).findElement(By.cssSelector(".t-trend-card-header"));
    }
    
    /**
     * Return app element in filter on given index
     * 
     * @param {number} index
     */
    public PageElement getCardByIndex(int index) {
        return this.getListOfCards().get(index)
            .findElement(By.cssSelector("div[cardinfo=\"app\"]"));
    }

    /**
     * Return name of app element in filter on index
     * 
     * @param {number} index
     */
    public String getCardNameByIndex(int index) {
        return this.getListOfCards().get(index)
            .findElement(By.cssSelector(".type-main-list-name")).getText();
    }

}
