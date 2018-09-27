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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;

import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.element.PageElement;

public class UniverseSettings {
    private static final String TABLE_CLASS = "universe-grid";
    
    private final UI ui;
    private final ModalDialog modalDialog;
    private final Logger logger = Logger.getLogger(getClass());
    
    public UniverseSettings(UI ui) {
        this.modalDialog = ui.getModalDialog();
        this.ui = ui;
    }

    private String universeRowXpath(String name) {
        String selector = "//div[contains(@class,'" + TABLE_CLASS + "')]";
        selector += "//span[text() = '" + name + "']/../../../..";
        return selector;
    }

    private PageElement getDeleteLink(String name) {
        return getUniverseRow(name).findElement(By.cssSelector(".universe-delete-action"));
    }
    
    public PageElement getDeleteLink(PageElement row) {
        return row.findElement(By.cssSelector(".universe-delete-action"));
    }

    public boolean isDeleteLinkPresent(PageElement row) {
        return !row.findPageElements(By.cssSelector(".universe-delete-action")).isEmpty();
    }

    public PageElement getRenameLink(String name) {
        return getUniverseRow(name).findElement(By.cssSelector(".universe-rename-action"));
    }

    public PageElement getRenameLink(PageElement row) {
        return row.findElement(By.cssSelector(".universe-rename-action"));
    }

    public boolean isRenameLinkPresent(PageElement row) {
        return !row.findPageElements(By.cssSelector(".universe-rename-action")).isEmpty();
    }

    private PageElement getEditLink(String name) {
        return getUniverseRow(name).findElement(By.cssSelector(".universe-edit-action"));
    }

    public PageElement getEditLink(PageElement row) {
        return row.findElement(By.cssSelector(".universe-edit-action"));
    }

    public boolean isEditLinkPresent(PageElement row) {
        return !row.findPageElements(By.cssSelector(".universe-edit-action")).isEmpty();
    }

    public String getClusterUniverseName() throws MalformedURLException {
        URL currentUrl = new URL(ui.getDriver().getCurrentUrl());
        return currentUrl.getHost().split("\\.")[0] + " components";
    }
    
    /**
     * Create basic universe with Type filter only
     * @param name 
     * @throws Exception
     */
    public void createUniverse(String name) throws Exception {
        getCreateUniverseButton().click();
        ui.getCanvas().waitForDisplay();
        ui.getCanvas().waitForUpdate();
        FilterBy filterBy = new FilterBy(ui);
        filterBy.add("Type");
        filterBy.getSaveUniverseButton().click();
        createUniverseName(name);
        getUniverseRow(name);
    }
    
    public boolean canDeleteUniverse(String name) {
        PageElement deleteLink = getDeleteLink(name);
        return deleteLink.isPresent();
    }

    public void deleteUniverse(String name) throws Exception {
        PageElement deleteLink = getDeleteLink(name);
        
        if (deleteLink.isPresent()) {
            deleteLink.click();
            modalDialog.waitForModalDialogFadeIn();
            modalDialog.clickButton(DialogButton.YES);
            ui.waitForWorkIndicator();
            logger.info("universe deleted: " + name);
        }
    }
    
    public void renameUniverse(String oldName, String newName) throws Exception {
        getRenameLink(oldName).click();
        modalDialog.waitForModalDialogFadeIn();
        PageElement input = modalDialog.getModalDialog().findElement(By.cssSelector("input"));
        Assert.assertEquals(input.getAttribute("value"), oldName);
        PageElement title = modalDialog.getModalDialog().findElement(By.cssSelector(".modal-title"));
        Assert.assertEquals(title.getText(), "Rename universe");
        input.clear();
        input.sendKeys(newName);
        modalDialog.clickButton(DialogButton.CONTINUE);
    }

    public String editUniverse(String name) throws Exception {
        getEditLink(name).click();
        return ui.getCurrentUrl();
    }

    public void createUniverseName(String name) throws Exception {
        modalDialog.waitForModalDialogFadeIn();
        modalDialog.getModalDialog().findElement(By.cssSelector("input")).sendKeys(name);
        PageElement title = modalDialog.getModalDialog().findElement(By.cssSelector(".modal-title"));
        Assert.assertEquals(title.getText(), "Create new universe");
        Utils.sleep(1000);
        if (!modalDialog.getButton(DialogButton.CONTINUE).isEnabled()) {
            String error = modalDialog.findBySelector(By.cssSelector(".form-input-error")).getText();
            modalDialog.clickButton(DialogButton.CLOSE);
            throw new Exception(error);
        } else {
            modalDialog.clickButton(DialogButton.CONTINUE);
            ui.waitForWorkIndicator();
        }
    }

    public PageElement getCreateUniverseButton() {
        return ui.getElementProxy(By.cssSelector(".universe-create button"));
    }

    private PageElement getNameCell(String name) {
        return getUniverseRow(name).findElement(By.cssSelector(".universe-name-row"));
    }

    public PageElement getUsersCell(String name) {
        return getUniverseRow(name).findElement(By.cssSelector(".universe-users-row"));
    }
    
    public PageElement getUsersCellClickableContainer(String name) {
        return getUniverseRow(name).findElement(By.cssSelector(".universe-users-row-container"));
    }

    public PageElement getUsersCell(PageElement row) {
        return row.findElement(By.cssSelector(".universe-users-row"));
    }

    public PageElement getUniverseRow(String name) {
        return ui.getElementProxy(By.xpath(universeRowXpath(name)));
    }
    
    public boolean isUniversePresent(String name) {
        return ui.getElementProxy(By.xpath(universeRowXpath(name)), 2).isDisplayed();
    }
    
    public List<PageElement> getAllRows() {
        return ui.findElements(By.cssSelector(".ui-grid-render-container-body .ui-grid-row"));
    }

    public String getNoDataMessage() {
        return ui.getElementProxy(By.cssSelector(".no-data-message"))
            .findElement(By.tagName("span")).getText();
    }

    public PageElement expandRow(String name) {
        getNameCell(name).click();
        return ui.getElementProxy(By.xpath(universeRowXpath(name)
                + "//div[contains(@class, 'universe-row')]"));
    }
    
    public void expandAllFilters(String universeName) {
        getUniverseRow(universeName).findElement(By.cssSelector(".universe-row-expand-actions a:nth-child(1)")).click();
    }

    public boolean isShowEntry(String universeName) throws Exception {
        By selector = By.cssSelector(".universe-row-entry-points strong");
        String text = getUniverseRow(universeName).findElement(selector).getText().trim();
        if ("No".equals(text)) {
            return false;
        } else if ("Yes".equals(text)) {
            return true;
        } else {
            throw new Exception("Invalid text in 'Include request entry points'");
        }
    }
    
    public List<PageElement> getFilterContainers(String universeName) {
        return getUniverseRow(universeName).findPageElements(By.cssSelector(".universe-row-filter-values"));
    }
    
    public List<String> getFilterOperators(String universeName) {
        List<String> operators = new ArrayList<String>();
        for(PageElement el : getUniverseRow(universeName).findPageElements(By.cssSelector(".universe-row-operator"))) {
            operators.add(el.getText().trim());
        }
        return operators;
    }

    public String getFilterName(String universeName, int index) {
        PageElement f = getFilterContainers(universeName).get(index);
        return f.findElement(By.cssSelector(".combo-filter-btn-text")).getText().trim();
    }
    

    public boolean isFilterBtCoverage(String universeName, int index) {
        PageElement f = getFilterContainers(universeName).get(index);
        return f.getAttribute("class").contains("universe-row-filter-values-bt");
    }

    public List<String> getFilterValues(String universeName, int index) {
        List<String> values = new ArrayList<String>();
        PageElement f = getFilterContainers(universeName).get(index);
        for(PageElement el : f.findPageElements(By.cssSelector(".combo-filter-item"))){ 
            values.add(el.getText().trim());
        }
        return values;
    }
    
    public void selectRow(String universeName) {
        PageElement row = getUniverseRow(universeName);
        if (!row.findElement(By.cssSelector("input[type='checkbox']")).isSelected()) {
            row.findElement(By.className("nice-checkbox")).click();
        }
    }

    public void selectAllRows() {
        if (!ui.getElementProxy(By.cssSelector(".select-all-rows input[type='checkbox']"))
                .isSelected()) {
            ui.getElementProxy(By.cssSelector(".select-all-rows")).click();
        }
    }

    public PageElement getEditUsersButton() {
        return ui.getElementProxy(By.cssSelector("button.edit-users"));
    }

    public void openUsersDialog(String universeName) throws Exception {
        getUsersCellClickableContainer(universeName).click();
        
        modalDialog.waitForModalDialogFadeIn();
    }
    
    public void saveUsersDialog() throws Exception {
        modalDialog.clickButton(DialogButton.SAVE);
        ui.waitForWorkIndicator();
    }

    public void closeUsersDialog() throws Exception {
        modalDialog.clickButton(DialogButton.CLOSE);
    }

    public List<PageElement> getTypeAheadOptions() {
        PageElement popup = ui.getElementProxy(By.cssSelector(".dropdown-menu.typeahead-popup"));
        return popup.findPageElements(By.tagName("li"));
    }

    public List<String> getTypeAheadValues() {
        List<String> result = new ArrayList<String>();
        List<PageElement> elements = getTypeAheadOptions();
        for (PageElement webElement : elements) {
            result.add(webElement.getText());
        }
        return result;
    }

    public PageElement getValidateUserButton() throws Exception {
        return modalDialog.getModalDialog().findElement(
            By.cssSelector(".clear-input.universe-users-add-dialog-input-search"));
    }

    public PageElement getUserValidatedIcon() throws Exception {
        return ui.getElementProxy(
            By.cssSelector(".clear-input.ng-scope.universe-users-add-dialog-input-ok"));
    }

    public PageElement getUserNotFoundIcon() throws Exception {
        return ui.getElementProxy(
            By.cssSelector(".clear-input.ng-scope.universe-users-add-dialog-input-not-found"));
    }

    public PageElement getAccessRadioButton(UI.Permission permission) throws Exception {
        String access = "access-" + permission.toString() + "-label";
        return modalDialog.getModalDialog().findElement(
            By.cssSelector(".universe-users-dialog-add"))
            .findElement(By.id(access));
    }

    public PageElement getAddUserInput() throws Exception {
        return modalDialog.getModalDialog().findElement(
                By.cssSelector(".universe-users-add-dialog-input")).findElement(By.tagName("input"));
    }

    public PageElement getAddUserInput10_2() throws Exception {
        return modalDialog.getModalDialog().findElement(
                By.cssSelector(".universe-users-dialog-add")).findElement(By.tagName("input"));
    }

    public PageElement getAddUserButton() throws Exception {
        return modalDialog.getModalDialog().findElement(
                By.cssSelector(".universe-users-dialog-add button"));
    }

    /**
     * Confirmation button when adding an unknown entity to add it as a user entity
     * @return User button from the confirmation dialog
     * @throws Exception
     */
    public PageElement getAddAsUserButton() throws Exception {
        return modalDialog.getModalDialog().findElement(
            By.cssSelector(".universe-users-dialog-button-user"));
    }

    /**
     * Confirmation button when adding an unknown entity to add it as a group entity
     * @return Group button from the confirmation dialog
     * @throws Exception
     */
    public PageElement getAddAsGroupButton() throws Exception {
        return modalDialog.getModalDialog().findElement(
            By.cssSelector(".universe-users-dialog-button-group"));
    }

    /**
     * Confirmation button when adding an entity with already defined access rights
     * @return Override existing access level button from the confirmation dialog
     * @throws Exception
     */
    public PageElement getChangeAccessButton() throws Exception {
        return modalDialog.getModalDialog().findElement(
            By.id("button-access-change"));
    }

    /**
     * Confirmation button when adding an entity with already defined access rights
     * @return Keep existing access level button from the confirmation dialog
     * @throws Exception
     */
    public PageElement getKeepExistingAccessButton() throws Exception {
        return modalDialog.getModalDialog().findElement(
            By.id("button-access-keep"));
    }

    public PageElement getRemoveAllUsersButton() throws Exception {
        return modalDialog.getModalDialog().findElement(
                By.cssSelector(".combo-filter-remove.universe-users-dialog-remove-all"));
    }
    
    public void removeAllUsers(String universeName) throws Exception {
        openUsersDialog(universeName);
        getRemoveAllUsersButton().click();
        saveUsersDialog();
    }

    public void removeUser(String username) throws Exception {
        List<PageElement> dialogUserRows = getDialogUserRows();
        for (PageElement userRow : dialogUserRows) {
            String user = userRow.findElement(By.tagName("span")).getText();
            if(user.equalsIgnoreCase(username)) {
                userRow.findElement(By.cssSelector(".universe-users-dialog-remove")).click();
                break;
            }
        }
    }

    public Map<String, String> getUsersWithPermissions() throws Exception {
        Map<String, String> result = new HashMap<>();
        List<PageElement> dialogUserRows = getDialogUserRows();
        for (PageElement userRow : dialogUserRows) {
            String user = userRow.findElement(By.tagName("span")).getText();
            String access = getUserAccessToggle(userRow).getText();
            result.put(user, access);
        }
        return result;
    }

    public PageElement getUserAccessToggle(PageElement userRow) throws Exception {
        return userRow.findElement(By.cssSelector(".universe-users-dialog-access"));
    }

    public List<PageElement> getDialogUserRows() throws Exception {
        return modalDialog.getModalDialog().findPageElements(By.cssSelector(".universe-users-dialog-item"));
    }

    /**
     * Adds a user to the universe with the specified access level
     * @param universeName
     * @param user
     * @param permission
     * @throws Exception
     */
    public void addUser(String universeName, String user, UI.Permission permission) throws Exception {
        openUsersDialog(universeName);
        getAddUserInput().sendKeys(user);
        getAccessRadioButton(permission).click();
        getAddUserButton().click();
        getAddAsUserButton().click();
        saveUsersDialog();
    }

    /**
     * Adds a user to the universe with the specified access level
     * @param universeName
     * @param group
     * @param permission
     * @throws Exception
     */
    public void addGroup(String universeName, String group, UI.Permission permission) throws Exception {
        openUsersDialog(universeName);
        getAddUserInput().sendKeys(group);
        getAccessRadioButton(permission).click();
        getAddUserButton().click();
        getAddAsGroupButton().click();
        saveUsersDialog();
    }

    /**
     * Adds all entries as users with the default (selected) access level 
     * @param universeName
     * @param users
     * @throws Exception
     */
    public void addUsersFor10_2Universe(String universeName, List<String> users) throws Exception {
        openUsersDialog(universeName);
        getAddUserInput10_2().sendKeys(StringUtils.join(users, ","));
        getAddUserButton().click();
        saveUsersDialog();
    }
}
