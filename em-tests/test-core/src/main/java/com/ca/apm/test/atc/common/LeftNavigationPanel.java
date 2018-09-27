/*
 * Copyright (c) 2017 CA. All rights reserved.
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
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.common.element.ElementConditionWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class LeftNavigationPanel extends ElementConditionWrapper {
    
    public enum SettingsItems {
        PERSPECTIVES("settings-perspectives-link", null, By.cssSelector(".ui-grid-canvas .ui-grid-row")),
        UNIVERSES("settings-universe-link", By.className("work-indicator"), By.cssSelector(".universe-container .ui-grid-render-container-body .ui-grid-canvas")),
        ATTRIBUTE_RULES("settings-attributes-link", By.id("attrRulesIndicator"), By.id("attr-rules-universe-selection-combo")),
        ENTERPRISE("settings-enterprise-link", null, By.className("settings-header")),
        PROVIDERS("settings-providers-link", null, By.cssSelector(".followers-table .ui-grid-header-cell:first-child")),
        AGENTS("settings-agents-link", null, By.cssSelector(".agents-grid-container")),
        SECURITY("settings-security-link", null, By.cssSelector(SecurityPage.CSS_SELECTOR_GENERATE_NEW_TOKEN)),
        ALERTS("settings-alerts-link", By.id("alertsWorkIndicator"), By.cssSelector(".alerts-grid-container"));

        private final String menuId;
        private final By workIndicatorSelector;
        private final By waitUntilVisibleSelector;

        private SettingsItems(String menuId, By workIndicatorSelector, By waitUntilVisibleSelector) {
            this.menuId = menuId;
            this.workIndicatorSelector = workIndicatorSelector;
            this.waitUntilVisibleSelector = waitUntilVisibleSelector;
        }

        public By getLinkSelector() {
            return By.cssSelector("#" + menuId + " a");
        }

        public By getWorkIndicatorSelector() {
            return workIndicatorSelector;
        }

        public By getWaitUntilSelector() {
            return waitUntilVisibleSelector;
        }
    }
    
    public enum SecondaryNavItems {
        COMMUNITIES("support-link"),
        ABOUT_DIALOG("about-link"),
        HELP("help-link");
            
        private final String menuId;

        private SecondaryNavItems(String menuId) {
            this.menuId = menuId;
        }

        public By getLinkSelector() {
            return By.id(menuId);
        }
    }
    
    public LeftNavigationPanel(UI ui) {
        super(ui, By.id("left-menu"));
    }

    public By getSelector() {
        return By.id("left-menu");
    }
 
    private void goTo(View view) {
        if (!isViewActive(view)) {
            selectView(view);
        }
    }

    public void goToMapViewPage() {
        goTo(View.MAPVIEW);
        ui.getMapviewPage().waitForReload();
    }

    public void goToDashboardPage() {
        goTo(View.DASHBOARD);
        ui.getDashboardPage().waitForReload();
    }

    public void goToHomePage() {
        goTo(View.HOMEPAGE);
        ui.getLandingPage().waitForTilesToLoad();
    }

    public void clickMenuItem(PageElement pe) {
        Utils.moveToAndClick(pe, ui);
        
        // Move cursor away
        ui.actions()
            .moveToElement(ui.findElement(By.id("header-logo")))
            .perform();

        waitWhileLeftMenuExpanded();
    }

    public void selectView(View view) {
        PageElement pe = null;
        switch (view) {
            case MAPVIEW:
                pe = getMapViewLink();
                break;
            case DASHBOARD:
                pe = getDashboardLink();
                break;
            case HOMEPAGE:
                pe = getHomepage();
                break;
            default:
                throw new RuntimeException("Unhandled view: " + view);
        }

        clickMenuItem(pe);

        ui.waitUntilVisible(view.getCharacteristicSelector());
    }

    /**
     * This method implements waiting on <code>class</code> attribute contents, until
     * <code>ca-menu-fixed-left-expanded</code> is not present in it.
     */
    public void waitWhileLeftMenuExpanded() {
        ui.waitForCondition(
            ExpectedConditions.not(
                ExpectedConditions.attributeContains(getSelector(), "class",
                    "ca-menu-fixed-left-expanded")), 20);
    }
    
    private By getSecondaryHelpMenuSelector() {
        return By.className("help-menu-nav-items");
    }
    
    /**
     * This method implements waiting on <code>class</code> attribute contents, until
     * <code>help-menu-open</code> is not present in it.
     */
    public void waitWhileSecondaryHelpMenuExpanded() {
        ui.waitForCondition(
            ExpectedConditions.not(
                ExpectedConditions.attributeContains(getSecondaryHelpMenuSelector(), "class",
                    "help-menu-open")), 20);
    }
    
    /**
     * This method implements waiting on <code>class</code> attribute contents, until
     * <code>help-menu-open</code> is not present in it.
     */
    public void waitUntilSecondaryHelpMenuExpanded() {
        ui.waitForCondition(
            ExpectedConditions.attributeContains(getSecondaryHelpMenuSelector(), "class",
                "help-menu-open"), 20);
    }

    public boolean isViewActive(View view) {
        return ui.findElements(view.getCharacteristicSelector()).size() > 0;
    }

    // MapView
    private By getMapViewLinkSelector() {
        return By.id("mapview");
    }

    public PageElement getMapViewLink() {
        return ui.getElementProxy(getMapViewLinkSelector());
    }

    public boolean isMapViewLinkPresent() {
        return ui.findElements(getMapViewLinkSelector()).size() > 0;
    }

    // HomePage
    private By getMapHomepageSelector() {
        return By.id("homepage");
    }

    public PageElement getHomepage() {
        return ui.getElementProxy(getMapHomepageSelector());
    }

    public boolean isHomepagePresent() {
        return ui.findElements(getMapHomepageSelector()).size() > 0;
    }

    // Dashboard
    private By getDashboardSelector() {
        return By.id("dashboard");
    }

    public PageElement getDashboardLink() {
        return ui.getElementProxy(getDashboardSelector());
    }

    public boolean isDashboardPresent() {
        return ui.findElements(getDashboardSelector()).size() > 0;
    }
    
    /**
     * Click on PERSPECTIVES menu item
     */
    public void goToPerspectives() {
        goToSettingsPage(SettingsItems.PERSPECTIVES);
    }

    public boolean isSettingsPerspectivesLinkPresent() {
        return ui.findElements(SettingsItems.PERSPECTIVES.getLinkSelector()).size() > 0;
    }
    
    /**
     * Click on ATTRIBUTE RULES menu item
     */
    public void goToDecorationPolicies() {
        goToSettingsPage(SettingsItems.ATTRIBUTE_RULES);
    }

    /**
     * Click on ENTERPRISE menu item
     */
    public void goToEnterprise() {
        goToSettingsPage(SettingsItems.ENTERPRISE);
    }

    /**
     * Click on SECURITY menu item
     */
    public void goToSecurity() {
        goToSettingsPage(SettingsItems.SECURITY);
    }

    /**
     * Click on FOLLOWERS menu item
     */
    public void goToFollowers() {
        goToSettingsPage(SettingsItems.PROVIDERS);
    }

    /**
     * Click on UNIVERSES menu item
     */
    public void goToUniverses() {
        goToSettingsPage(SettingsItems.UNIVERSES);
    }

    /**
     * Click on ALERTS menu item
     */
    public void goToAlerts() {
        ui.waitForWorkIndicator();
        goToSettingsPage(SettingsItems.ALERTS);
    }

    /**
     * Click on AGENTS menu item
     */
    public void goToAgents() {
        ui.waitForWorkIndicator();
        goToSettingsPage(SettingsItems.AGENTS);
    }

    /** 
     * Click on ABOUT menu item 
     */
    public AboutDialog openAboutDialog() {
        ui.waitForWorkIndicator();
        goToSecondaryPage(SecondaryNavItems.ABOUT_DIALOG);
        
        return new AboutDialog(ui);
    }
    
    /** 
     * Click on COMMUNITIES menu item 
     */
    public void goToCommunities() {
        ui.waitForWorkIndicator();
        goToSecondaryPage(SecondaryNavItems.COMMUNITIES);
    }
    
    /** 
     * Click on HELP menu item 
     */
    public void goToHelpPage() {
        ui.waitForWorkIndicator();
        goToSecondaryPage(SecondaryNavItems.HELP);
    }
    
    private void goToSettingsPage(SettingsItems tab) {
        PageElement menuElement = getConfigurationMenuElement(tab);

        clickMenuItem(menuElement);
        
        waitForWorkIndicator(tab);
        
        if (tab.getWaitUntilSelector() != null) {
            ui.waitUntilVisible(tab.getWaitUntilSelector());
        }
    }

    private void expandSecondaryHelpMenu() {
        PageElement menuElement = ui.getElementProxy(By.id("help-root-link"));
        
        Utils.moveToAndClick(menuElement, ui);
        
        waitUntilSecondaryHelpMenuExpanded();
    }
    
    private void goToSecondaryPage(SecondaryNavItems tab) {
        expandSecondaryHelpMenu();

    }
    
    private PageElement getConfigurationMenuElement(SettingsItems tab) {
        return ui.getElementProxy(tab.getLinkSelector());
    }
    
    private PageElement getSecondaryMenuElement(SecondaryNavItems tab) {
        return ui.getElementProxy(tab.getLinkSelector());
    }
    
    public void waitForWorkIndicator(SettingsItems tab) {
        this.ui.waitForWorkIndicator(tab.getWorkIndicatorSelector());
    }
}
