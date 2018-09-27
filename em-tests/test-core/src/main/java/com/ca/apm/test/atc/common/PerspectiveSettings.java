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
import org.openqa.selenium.support.ui.Select;

import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.element.PageElement;

public class PerspectiveSettings {

    private final UI ui;
    private final ModalDialog modalDialog;

    public PerspectiveSettings(UI ui) {
        this.ui = ui;
        modalDialog = ui.getModalDialog();
    }

    private By getWorkIndicatorSelector() {
        return By.cssSelector("#perspectivesWorkIndicator");
    }

    private void waitForWorkIndicator() {
        ui.waitForWorkIndicator(getWorkIndicatorSelector());
    }

    /**
     * Return element of Perspectives table
     */
    public PageElement getTable() {
        return ui.getElementProxy(By.cssSelector(".grouping-grid-container"))
                .findElement(By.cssSelector(".ui-grid-canvas"));
    }

    /**
     * Return element of modal dialog
     * (dialog for perspective adding/editing/confirming, etc)
     */
    public PageElement getModalDialog() {
        return ui.getElementProxy(By.cssSelector("div.modal-dialog"));
    }

    public PageElement getModalDialogSurround() throws Exception {
        return modalDialog.getModalDialog().findElement(By.xpath(".."));
    }

    /**
     * Return perspective row element
     * 
     * @param {string} name - perspective name
     */
    public PageElement getPerspectiveRow(String name) {
        return getTable().findElement(
            By.xpath(".//div[contains(@class,\"t-perspective-name\")"
                + " and .=\"" + name + "\"]/../../.."));
    }

    /**
     * Return remove button element of specified perspective
     * 
     * @param {string} name - perspective name
     */
    public PageElement getDeleteButton(String name) {
        PageElement row = getPerspectiveRow(name);
        return row.findElement(By.cssSelector(".t-delete-group"));
    }

    public boolean isDeleteButtonPresent(String name) {
        try {
            return getDeleteButton(name).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Return list of defined perspective levels
     * in dialog for adding/editing new perspective
     * @throws Exception 
     */
    public List<PageElement> getListOfLevelsInEditDialog() throws Exception {
        return modalDialog.getModalDialog().findPageElements(By.cssSelector("div[ng-repeat].grouping-dialog-level-item"));
    }

    /**
     * Return list of defined perspective levels
     * in dialog for adding/editing new perspective as names of attributes
     * @throws Exception 
     */
    public List<String> getListOfLevelsInEditDialogAsStrings() throws Exception {
        List<PageElement> elements = getListOfLevelsInEditDialog();
        List<String> res = new ArrayList<String>(elements.size());
        
        for (PageElement element : elements) {
            String attrName = element.findElement(By.cssSelector("select[ng-model=\"level.selected\"] option[selected=\"selected\"]")).getText();
            res.add(attrName);
        }
        
        return res;
    }
    
    /**
     * Return element of level menu
     * in dialog for adding/editing new perspective
     * 
     * @param {number} level
     * @throws Exception 
     */
    private PageElement getLevelMenu(int level) throws Exception {
        int index = level - 1;
        return getListOfLevelsInEditDialog().get(index);
    }

    /**
     * Select the attribute in the specified level menu
     * in dialog for adding/editing new perspective
     *
     * @param {object} el
     * @param {string} attribute
     */
    private void selectItemInLevelMenu(PageElement el, String attribute) {
        Select sel = new Select(el.findElement(By.cssSelector("select")).getWrappedElement());
        sel.selectByVisibleText(attribute);
    }
    
    /**
     * Select the attribute in the specified level men
     * in dialog for adding/editing new perspective
     *
     * @param {object} el
     * @param {string} attribute
     * @throws Exception 
     */
    public void selectItemInLevelMenu(int level, String attribute) throws Exception {
        PageElement menu = getLevelMenu(level);
        menu.click();
        selectItemInLevelMenu(menu, attribute);
    }

    /**
     * Return element of button for adding new level of perspective
     * in dialog for adding/editing new perspective
     * @throws Exception 
     */
    public PageElement getAddLevelBtn() throws Exception {
        return modalDialog.getModalDialog().findElement(
                By.cssSelector(".grouping-dialog-level-item > .icon-plus"));
    }

    /**
     * Return element of button for removing last level of perspective
     * in dialog for adding/editing new perspective
     * @throws Exception 
     */
    public PageElement getRemoveLevelBtn() throws Exception {
        return modalDialog.getModalDialog().findElement(
                By.cssSelector(".grouping-dialog-item-remove"));
    }

    public boolean isRemoveLevelBtnPresent() throws Exception {
        return modalDialog.getModalDialog()
                .findPageElements(By.cssSelector(".grouping-dialog-item-remove"))
                .size() > 0;
    }

    /**
     * Return element of input field for entering perspective name
     * in dialog for adding/editing new perspective
     * @throws Exception 
     */
    public PageElement getPerspectiveNameInput() throws Exception {
        return modalDialog.getModalDialog().findElement(By.cssSelector("input#new-group-name"));
    }

    /**
     * Return text entered into the input field for entering perspective name
     * in dialog for adding/editing new perspective
     * @throws Exception 
     */
    public String getPerspectiveNameInputValue() throws Exception {
        return getPerspectiveNameInput().getAttribute("value");
    }

    /**
     * Display dialog for adding new perspective
     * 
     * @throws Exception
     */
    public void displayAddPerspectiveDialog() throws Exception {
        getAddPerspectiveBtn().click();
        modalDialog.waitForModalDialogFadeIn();
        
        // wait for level menus to load
        Utils.sleep(350);
    }

    /**
     * Click on button for saving changes
     * in dialog for adding/editing new perspective
     * 
     * @throws Exception
     */
    public void saveEdit() throws Exception {
        modalDialog.clickButton(DialogButton.SAVE);
        modalDialog.waitForModalDialogFadeOut();
        
        // wait for ui-grid to refresh
        waitForWorkIndicator();
    }

    /**
     * Add new perspective
     * 
     * @param {string} attribute - attribute used for new main perspective
     * @throws Exception
     */
    public void addPerspective(String attribute, boolean isPersonal) throws Exception {
        displayAddPerspectiveDialog();
        addMultiLevelPerspective(new String[]{attribute}, attribute, isPersonal);
    }

    /**
     * Add new multi-level perspective
     * 
     * @param {string[]} attributes - subgroups to be created
     * @param {string} [name] - name of new perspective, set to null for default perspective name
     * @throws Exception
     */
    public void addMultiLevelPerspective(String[] attributes, String perspectiveName, boolean isPersonal) throws Exception {
        int num = 1;
        for (String attrName : attributes) {
            if (num > 3) {
                getAddLevelBtn().click();
            }
            Utils.sleep(300);
            PageElement menu = getLevelMenu(num);
            selectItemInLevelMenu(menu, attrName);
            num++;
        }
        if (perspectiveName != null) {
            getPerspectiveNameInput().clear();
            getPerspectiveNameInput().sendKeys(perspectiveName);
        }

        if (isPersonal) {
            uncheckPublicCheckbox();
        } else {
            checkPublicCheckbox();
        }
        saveEdit();
    }

    public boolean isPerspectivePresent(String name) {
        try {
            return getPerspectiveRow(name).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    public void deletePerspectiveIfExists(String name) throws Exception {
        while (isPerspectivePresent(name)) {
            deletePerspective(name);
        }
    }
    
    public boolean isPerspectivePersonal(String name) {
        PageElement row = getPerspectiveRow(name);
        return "Personal".equals(row.findElement(By.cssSelector(".ui-grid-cell:nth-child(3)")).getText().trim());
    }

    /**
     * Close the Add new perspective dialog
     * 
     * @throws Exception
     */
    public void closeModalDialog() throws Exception {
        modalDialog.clickButton(DialogButton.CLOSE);
    }

    /**
     * Return element for adding perspectives in the settings view
     */
    public PageElement getAddPerspectiveBtn() {
        return ui.getElementProxy(By.cssSelector(".t-add-perspective"), 2);
    }

    public boolean isAddPerspectiveBtnPresent() {
        try {
            return getAddPerspectiveBtn().isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Return list of names of perspectives in the settings view
     */
    public List<String> getListOfPerspectiveNames() {
        List<PageElement> els =
                getTable().findPageElements(By.cssSelector(".ui-grid-row"));
        List<String> toReturn = new ArrayList<String>();
        for (PageElement el : els) {
            toReturn.add(el
                    .findElement(
                            By.cssSelector("div.ui-grid-cell:nth-child(1) .ui-grid-cell-contents"))
                    .getText());
        }
        return toReturn;
    }

    /**
     * Return element of "Set as default" link for a perspective in the settings view
     * 
     * @param name
     */
    public PageElement getSetAsDefaultLink(String name) {
        return getPerspectiveRow(name).findElement(
                By.cssSelector(".t-set-default-group"));
    }

    public boolean isSetAsDefaultLinkPresent(String name) {
        try {
            return getSetAsDefaultLink(name).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Return element of "Edit" link for a perspective in the settings view
     * 
     * @param name
     */
    public PageElement getEditLink(String name) {
        return getPerspectiveRow(name).findElement(
                By.cssSelector(".t-edit-group"));
    }

    public boolean isEditLinkPresent(String name) {
        try {
            return getEditLink(name).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Return element of "Duplicate" link for a perspective in the settings view
     * 
     * @param name
     */
    public PageElement getDuplicateLink(String name) {
        return getPerspectiveRow(name).findElement(
                By.cssSelector(".t-clone-group"));
    }

    public boolean isDuplicateLinkPresent(String name) {
        try {
            return getEditLink(name).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Delete existing perspective using the "Delete" link in settings view
     * 
     * @param name
     * @throws Exception
     */
    public void deletePerspective(String name) throws Exception {
        PageElement deleteButton = getDeleteButton(name);
        deleteButton.click();
        
        modalDialog.waitForModalDialogFadeIn();
        modalDialog.clickButton(DialogButton.YES);
        modalDialog.waitForModalDialogFadeOut();
        
        waitForWorkIndicator();
    }

    /**
     * Display edit dialog for existing perspective using the "Edit" link in settings view
     * 
     * @param name
     * @throws Exception
     */
    public void selectEditLink(String name) throws Exception {
        getEditLink(name).click();
        modalDialog.waitForModalDialogFadeIn();
    }
    
    /**
     * Display edit dialog for existing perspective using the "Duplicate" link in settings view
     * 
     * @param name
     * @throws Exception
     */
    public void selectDuplicateLink(String name) throws Exception {
        getDuplicateLink(name).click();
        modalDialog.waitForModalDialogFadeIn();
    }

    /**
     * Set perspective as default using the "Set as default" link in settings view
     * 
     * @param name
     * @throws InterruptedException
     */
    public void setAsDefault(String name) throws InterruptedException {
        if (isSetAsDefaultLinkPresent(name)) {
            getSetAsDefaultLink(name).click();
            waitForWorkIndicator();
        }
    }

    /**
     * Check is perspective is set as default
     * 
     * @param name
     */
    public boolean isSetAsDefault(String name) {
        try {
            return getPerspectiveRow(name).findElement(By.cssSelector(".grouping-settings-table-default-item")).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Finds default perspective
     * @return String name or null
     */
    public String getDefault() {
        List<String> names = getListOfPerspectiveNames();
        
        for (String name: names) {
            if (isSetAsDefault(name)) {
                return name;
            }
        }
        
        return null;
    }

    private PageElement getPublicCheckbox() throws Exception {
        return modalDialog.getModalDialog().findElement(By.id("isPublicCheckbox"));
    }
    
    private PageElement getPublicCheckboxLabel() throws Exception {
        return modalDialog.getModalDialog().findElement(By.cssSelector(".public-checkbox label"));
    }
    
    public void checkPublicCheckbox() throws Exception {
        PageElement el = getPublicCheckbox();
        if (!el.isSelected()) {
            getPublicCheckboxLabel().click();
        }
    }
    
    public void uncheckPublicCheckbox() throws Exception {
        PageElement el = getPublicCheckbox();
        if (el.isSelected()) {
            getPublicCheckboxLabel().click();
        }
    }
    
    public boolean isPublicCheckboxChecked() throws Exception {
        return getPublicCheckbox().isSelected();
    }
    
    public boolean isPublicCheckboxEnabled() throws Exception {
        return getPublicCheckbox().isEnabled();
    }
}
