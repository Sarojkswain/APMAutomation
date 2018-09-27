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

/**
 * Actions on map controls panel
 */
public class CanvasControl {
    UI ui;
    Canvas canvas;

    public CanvasControl(UI ui, Canvas canvas) {
        this.ui = ui;
        this.canvas = canvas;
    }

    /**
     * Return element of map controls panel
     */
    public PageElement getMapCtrlPanel() {
        canvas.waitForUpdate();
        return ui.getElementProxy(By.cssSelector("div[class~=\"map-controls-panel\"]"));
    }

    /**
     * Return element of "Fit All To View"
     */
    public PageElement getFitAllToView() {
        return this.getMapCtrlPanel().findElement(By.cssSelector("img[title=\"Fit All To View\"]"));
        // TODO: add and use class values instead of title
    }

    /**
     * Return element of "Fit Highlighted To View"
     */
    public PageElement getFitHightlightedToView() {
        return this.getMapCtrlPanel().findElement(
            By.cssSelector("img[title=\"Fit Highlighted To View\"]"));
    }

    /**
     * Return element of "Fit Selected To View"
     */
    public PageElement getFitSelectedToView() {
        return this.getMapCtrlPanel().findElement(
            By.cssSelector("img[title=\"Fit Selected To View\"]"));
    }

    /**
     * Return element of "Fold All Groups"
     */
    public PageElement getFoldAllGroups() {
        return this.getMapCtrlPanel().findElement(By.cssSelector("img[title=\"Fold All Groups\"]"));
    }

    /**
     * Return element of "Select Highlighted"
     */
    public PageElement getSelectHighlighted() {
        return this.getMapCtrlPanel().findElement(
            By.cssSelector("img[title=\"Select Highlighted\"]"));
    }

    /**
     * Return element of "Hide All Events"
     */
    public PageElement getHideAllEvents() {
        return this.getMapCtrlPanel().findElement(By.cssSelector("img[title=\"Hide All Events\"]"));
    }

    /**
     * Select element of "Fit All To View"
     */
    public void fitAllToView() {
        this.getFitAllToView().click();
        Utils.sleep(1300);
    }

    /**
     * Select element of "Fit Highlighted To View"
     */
    public void fitHightlightedToView() {
        this.getFitHightlightedToView().click();
        Utils.sleep(1000);
    }

    /**
     * Select element of "Fit Selected To View"
     */
    public void fitSelectedToView() {
        this.getFitSelectedToView().click();
        Utils.sleep(1000);
    }

    /**
     * Select element of "Fold All Groups"
     */
    public void foldAllGroups() {
        this.getFoldAllGroups().click();
        Utils.sleep(500);
    }

    public void unfoldAllGroups() throws Exception {
        fitAllToView();
        String[] allGroupNames = canvas.getArrayOfNodeNames();
        for (String groupName : allGroupNames) {
            canvas.expandGroup(canvas.getNodeByName(groupName));
            fitAllToView();
        }
    }

    /**
     * Select element of "Select Highlighted"
     */
    public void selectHighlighted() {
        this.getSelectHighlighted().click();
        Utils.sleep(1000);
    }

    /**
     * Select element of "Hide All Events"
     * 
     * @throws InterruptedException
     */
    public void hideAllEvents() {
        this.getHideAllEvents().click();
        Utils.sleep(1000);
    }
}
