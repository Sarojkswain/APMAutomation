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

import java.util.List;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.element.PageElement;

public class Ribbon {
    
    public enum CollapsibleToolbar {
        FILTER_BY("filter-by-title", "global-filter-toolbar", "collapse-filter-toolbar"),
        TIMELINE("timeline-title", "timeline-wrapper", "collapse-timeline-toolbar"),
        HIGHLIGHTING("highlight-title", "global-highlighting-toolbar", "collapse-highlight-toolbar");
        
        private final String collapsedCssClass;
        private final String expandedToolbarId;
        private final String collapseIconOfExpandedToolbarId;
        
        private CollapsibleToolbar(String collapsedCssClass, String expandedToolbarId, String collapseIconOfExpandedToolbarId) {
            this.collapsedCssClass = collapsedCssClass;
            this.expandedToolbarId = expandedToolbarId;
            this.collapseIconOfExpandedToolbarId = collapseIconOfExpandedToolbarId;
        }
        
        public By getCollapsedToolbarSelector() {
            return By.cssSelector("div.menuBar ." + collapsedCssClass);
        }
        
        public By getExpandedToolbarSelector() {
            return By.id(expandedToolbarId);
        }
        
        public By getCollapseIconOfExpandedToolbarSelector() {
            return By.id(collapseIconOfExpandedToolbarId);
        }
    }
    
    private final LeftNavigationPanel navigation;
    private final UI ui;

    public Ribbon(LeftNavigationPanel navigation, UI ui) {
        this.navigation = navigation;
        this.ui = ui;
    }

    /**
     * @deprecated Use {@link LeftNavigationPanel#goToMapViewPage()}
     */  
    public void goToMapTab() {
        navigation.goToMapViewPage();
    }

    /**
     * @deprecated Use {@link LeftNavigationPanel#goToDashboardPage()}
     */ 
    public void goToCardTab() {
        navigation.goToDashboardPage();
    }

    /**
     * @deprecated Use {@link LeftNavigationPanel#goToSettingsPage()}
     */ 
    /*public void goToSettingsTab() {
        navigation.goToSettingsPage();
    }*/
    
    public void waitUntilMenuBarVisible() {
        ui.waitUntilVisible(By.className("menuBar"));
    }
    
    public boolean isToolbarExpanded(CollapsibleToolbar toolbar) {
        waitUntilMenuBarVisible();
        List<PageElement> elems = ui.findElements(toolbar.getExpandedToolbarSelector());
        return elems.size() == 1 && elems.get(0).isDisplayed();
    }

    public PageElement getCollapsedToolbarElement(CollapsibleToolbar toolbar) {
        collapseToolbar(toolbar);
        
        return ui.getElementProxy(toolbar.getCollapsedToolbarSelector());
    }
    
    public void collapseToolbar(CollapsibleToolbar toolbar) {
        if (isToolbarExpanded(toolbar)) {
            ui.waitUntilVisible(toolbar.getExpandedToolbarSelector());
            ui.getElementProxy(toolbar.getCollapseIconOfExpandedToolbarSelector()).click();
            ui.waitUntilVisible(toolbar.getCollapsedToolbarSelector());
        }
    }
    
    public void expandToolbar(CollapsibleToolbar toolbar) {
        if (!isToolbarExpanded(toolbar)) {
            ui.getElementProxy(toolbar.getCollapsedToolbarSelector()).click();
            ui.waitUntilVisible(toolbar.getExpandedToolbarSelector());
        }
        
        ui.waitForWorkIndicator();
    }
    
    public int getCounterValueFromCollapsedToolbar(CollapsibleToolbar toolbar) {
        if (isToolbarExpanded(toolbar)) {
            throw new UnsupportedOperationException("There is no count value if the toolbar is expanded.");
        }
        
        if (toolbar == CollapsibleToolbar.TIMELINE) {
            throw new UnsupportedOperationException("There is no count value in the collapsed Timeline toolbar.");
        }
        
        PageElement ct = ui.getElementProxy(toolbar.getCollapsedToolbarSelector());
        
        // If the counter value were zero, it is not displayed
        List<PageElement> el = ct.findPageElements(By.className("options-counter"));
        if (el.size() == 0) {
            return 0;
        } else {
            String value = el.get(0).getText();
            // For some reason web-driver sometimes returns '' or "" if there is 0 in the element that is transparent 
            if ((value == null) || value.isEmpty() || value.equals("\"\"") || value.equals("''")) {
                return 0;
            } else {
                return Integer.valueOf(value);
            }
        }
    }
    
    public void expandTimelineToolbar() throws Exception {
        expandToolbar(CollapsibleToolbar.TIMELINE);
    }

    public void expandFilterToolbar() throws Exception {
        expandToolbar(CollapsibleToolbar.FILTER_BY);
    }

    public void expandHighlightToolbar() throws Exception {
        expandToolbar(CollapsibleToolbar.HIGHLIGHTING);
    }

    public void collapseTimelineToolbar() throws Exception {
        collapseToolbar(CollapsibleToolbar.TIMELINE);
    }

    public void collapseFilterToolbar() throws Exception {
        collapseToolbar(CollapsibleToolbar.FILTER_BY);
    }

    public void collapseHighlightToolbar() throws Exception {
        collapseToolbar(CollapsibleToolbar.HIGHLIGHTING);
    }
    
    /**
     * Log out
     * 
     * @deprecated Use {@link UI#logout()}
     */
    public void logout() {
        ui.logout();
    }
 }
