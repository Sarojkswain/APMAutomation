package com.ca.apm.test.atc.common;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.ca.apm.test.atc.common.element.ATCDropDownOpener;
import com.ca.apm.test.atc.common.element.ElementConditionWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class TopNavigationPanel extends ElementConditionWrapper {

    public enum ViewOptionType {
        VIEW_WEBVIEW_LINK_CLASS("t-vw-link"), 
        VIEW_ACC_LINK_CLASS("t-acc-link"), 
        VIEW_UI_LINK_CLASS("t-view-link");

        private final String cssClass;

        private ViewOptionType(String cssClass) {
            this.cssClass = cssClass;
        }

        public String getCssClass() {
            return cssClass;
        }
    }

    private Universe universe = null;

    public TopNavigationPanel(UI ui) {
        super(ui, By.id("ca-navbar-top"));
    }

    public By getSelector() {
        return By.id("ca-navbar-top");
    }

    public Universe getUniverse() {
        if (universe == null) {
            universe = new Universe(ui);
        }

        return universe;
    }

    public PageElement getMessageCenterControlInClosedState() {
        return findElement(getMessageCenterControlInClosedStateSelector());
    }
    
    public PageElement getMessageCenterControlInOpenedState() {
        return findElement(getMessageCenterControlInOpenedStateSelector());
    }

    public PageElement getMessageCenterMessagesContainer() {
        return findElement(getMessageCenterPopoverContainerSelector());
    }
    
    public By getMessageCenterPopoverContainerSelector() {
        return By.id("message-popover-content");
    }
    
    public By getMessageCenterControlInClosedStateSelector() {
        return By.id("message-popover-open-control");
    }
    
    public By getMessageCenterControlInOpenedStateSelector() {
        return By.id("messages-icon-container");
    }

    public By getUniverseDropdownSelector() {
        return By.id("universe-selection-combo");
    }

    private By getUniverseDropdownMenuSelector() {
        return By.id("universe-selection-combo-menu");
    }
    
    public ATCDropDownOpener getUniverseDropdown() {
        return new ATCDropDownOpener(ui.getElementProxy(getUniverseDropdownSelector()));
    }

    public String getActiveUniverse() {
        return ui.getElementProxy(getUniverseDropdownSelector()).getText().trim();
    }

    public boolean isUniverseDropdownPresent() {
        return ui.findElements(getUniverseDropdownSelector()).size() > 0;
    }

    public void selectUniverse(String name) {
        getUniverseDropdown().selectFromDropdown(getUniverseDropdownMenuSelector(), name);
        
        ui.waitForWorkIndicator();
    }

    public List<String> getUniverseNames() {
        ATCDropDownOpener dropDown = getUniverseDropdown();
        if (dropDown.isEnabled() == false) {
            return Collections.singletonList(dropDown.getText());
        } else {
            return dropDown.getDropdownOptions(getUniverseDropdownMenuSelector(), "t-universe-selection-item");
        }
    }

    public By getUserFieldSelector() {
        return By.id("username-field");
    }

    public boolean isUserFieldPresent() {
        return ui.findElements(getUserFieldSelector()).size() > 0;
    }

    /**
     * Return either the available Webview link element if there is a single one or any Webview link
     * element from the list if there are multiple.
     */
    public PageElement getAnyWebviewLinkElement() {
        return getUniverseDropdown().getDropdownLinkElements(getUniverseDropdownMenuSelector(),         
            ViewOptionType.VIEW_WEBVIEW_LINK_CLASS.getCssClass()).get(0);
    }

    public PageElement getLogoElement() {
        try {
            return this.findElement(By.cssSelector(".header-container-left a.logo-link-block"));
        } catch (NoSuchElementException nse) {
            return null;
        }
    }

    public PageElement getLogoImageElement() {
        try {
            return this.getLogoElement().findElement(By.tagName("img"));
        } catch (NoSuchElementException nse) {
            return null;
        }
    }

    public String getBrandingText() {
        try {
            return this.findElement(By.className("product-name-medium")).getAttribute("innerText");
        } catch (NoSuchElementException nse) {
            return null;
        }
    }

    public PageElement getUserSelectionDropdownControl() {
        return this.findElement(By.id("user-selector"));
    }

    public PageElement getLogoutLinkElement() {
        return findElement(By.id("user-logout"));
    }

    public boolean isInfoCenterPanelOpen() {
        return ui.findElement(By.id("message-popover-content")).isDisplayed();
    }

    public void expandInfoCenterPanel() {
        if (!isInfoCenterPanelOpen()) {
            getMessageCenterControlInClosedState().click();
        }
    }

    public void collapseInfoCenterPanel() {
        if (isInfoCenterPanelOpen()) {
            getMessageCenterControlInClosedState().click();
        }
    }

    public boolean infoCenterMessageExists(final String alertName) {
        expandInfoCenterPanel();

        Utils.waitForCondition(ui.getDriver(), new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                List<WebElement> messages =
                    getMessageCenterMessagesContainer().findElements(
                        By.className("error-message-text"));
                for (WebElement message : messages) {
                    if (message.getText().contains(alertName)) {
                        return true;
                    }
                }
                return false;
            }
        });

        return true;
    }

    public void removeInfoCenterMessages() {
        expandInfoCenterPanel();
        try {
            getMessageCenterMessagesContainer().findElement(By.className("remove-all")).click();
        } catch (Exception e) {
            // Probably no messages, pass
        }

        collapseInfoCenterPanel();
    }
}
