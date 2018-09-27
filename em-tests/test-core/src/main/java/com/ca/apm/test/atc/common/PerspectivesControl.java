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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.ca.apm.test.atc.common.element.PageElement;

public class PerspectivesControl {
    
    private final UI ui;
    private final ModalDialog modalDialog;
    private static final String DROPDOWN_SELECTOR = ".perspective-dropdown";
    
    public static final String DEFAULT_PERSPECTIVE = "Default Overview";
    public static final String NO_GROUPS_PERSPECTIVE = "No Perspective";

    public PerspectivesControl(UI ui) {
        this.ui = ui;
        this.modalDialog = ui.getModalDialog();
    }

    /**
     * Return the element of PERSPECTIVES dropdown
     */
    public PageElement getDropdown() {
        Utils.waitForCondition(ui.getDriver(),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(DROPDOWN_SELECTOR
                        + " li.map-combo-filter-item")));
        return ui.getElementProxy(By.cssSelector(DROPDOWN_SELECTOR));
    }

    public boolean isExpanded() {
        return this.getDropdown()
                .findElement(By.cssSelector(".dropdown-menu")).isDisplayed();
    }

    public void expand() {
        if (!this.isExpanded()) {
            this.getDropdown()
                .findElement(By.cssSelector("button.dropdown-toggle"))
                .click();
        }
    }

    public PageElement getPerspective(String name) {
        return this.getDropdown().findElement(
                By.xpath(".//li/span[. = '" + name + "']"));
    }

    public void selectPerspectiveByName(String name) {
        this.expand();
        this.getPerspective(name).click();
        (new Canvas(ui)).waitForUpdate();
    }
    
    public void selectDefaultPerspective() {
        this.expand();
        this.getPerspective(DEFAULT_PERSPECTIVE).click();
    }

    /**
     * Return list of all existing perspectives.
     * If it makes sense, use rather {@link #getNamesOfAllPerspectives()}.
     */
    public List<PageElement> getListOfPerspectives() {
        return this.getDropdown().findPageElements(
            By.cssSelector(".dropdown-menu > div > li"));
    }

    /**
     * Return list of names of all existing perspectives
     */
    public List<String> getNamesOfAllPerspectives() {
        List<String> names = new ArrayList<String>();
        for (PageElement el : getListOfPerspectives()) {
            names.add(el.getText().trim());
        }
        return names;
    }
    
    /**
     * Return list of personal perspectives.
     * Use {@link #getNamesOfPersonalPerspectives()}.
     */
    private List<PageElement> getListOfPersonalPerspectives() {
        return this.getDropdown()
                .findPageElements(By.className("t-personal-perspective"));
    }

    public List<String> getNamesOfPersonalPerspectives() {
        List<String> names = new ArrayList<String>();
        for (PageElement el : getListOfPersonalPerspectives()) {
            names.add(el.getText().trim());
        }
        return names;
    }
    
    /**
     * Return list of public perspectives.
     * Use {@link #getNamesOfPublicPerspectives()}.
     */
    private List<PageElement> getListOfPublicPerspectives() {
        return this.getDropdown()
                .findPageElements(By.className("t-public-perspective"));
    }
    
    public List<String> getNamesOfPublicPerspectives() {
        List<String> names = new ArrayList<String>();
        for (PageElement el : getListOfPublicPerspectives()) {
            names.add(el.getText().trim());
        }
        return names;
    }

    /**
     * Check if perspective is currently active (selected)
     * 
     * @param {string} name - perspective name
     */
    public boolean isPerspectiveActive(String name) {
        try {
            return this.getDropdown().findElement(
                    By.xpath(".//button/span[starts-with(.,'" + name + "')]")).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isPerspectivePresent(String name) {
        try {
            return this.getPerspective(name).isPresent();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    public boolean isActivePerspectiveFaded() {
        try {
            return this.getDropdown().findElement(
                    By.xpath(".//button/span[contains(@class, \"faded\")]")).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    public String getActivePerspectiveTooltip() {
        try {
            PageElement el = this.getDropdown().findElement(By.xpath(".//button"));
            return el.getAttribute("title");
        } catch (NoSuchElementException e) {
            return "";
        }
    }
    
    public String getActivePerspectiveName() {
        PageElement perspectiveTextHolder = this.ui.getElementProxy(By.cssSelector(DROPDOWN_SELECTOR + " > button > span"));
        return perspectiveTextHolder.getAttribute("innerText");
    }
    
    public boolean isPersonalPerspectivePresent(String name) {
        for (PageElement el : this.getListOfPersonalPerspectives()) {
            if (el.getText().trim().contains(name)) {
                return true;
            }
        }
        return false;
    }

    public void clickAddPerspective() {
        this.expand();
        this.getDropdown().findElement(By.cssSelector(".perspective-add")).click();
    }

    public void clickEditPerspective() {
        this.expand();
        this.getDropdown().findElement(By.cssSelector(".t-edit-perspective")).click();
    }
    
    public void clickClonePerspective() {
        this.expand();
        this.getDropdown().findElement(By.cssSelector(".t-clone-perspective")).click();
    }
    
    public PageElement getEditIcon() {
        return this.getDropdown().findElement(By.cssSelector(".glyphicon-edit"));
    }

    public boolean isEditIconPresent() {
        try {
            return this.getEditIcon().isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public PageElement getCloneIcon() {
        return this.getDropdown().findElement(By.cssSelector(".glyphicon-clone"));

    }

    public boolean isCloneIconPresent() {
        try {
            return this.getCloneIcon().isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    public void addPerspective(String attribute, boolean isPersonal) throws Exception {
        clickAddPerspective();
        modalDialog.waitForModalDialogFadeIn();
        PerspectiveSettings perspectiveSettings = new PerspectiveSettings(ui);
        perspectiveSettings.addMultiLevelPerspective(new String[]{attribute}, attribute, isPersonal);
        ui.getCanvas().waitForUpdate();
    }
}
