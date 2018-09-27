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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ISelect;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.element.ATCDropDownOpener;
import com.ca.apm.test.atc.common.element.AttributeRulesTableRow;
import com.ca.apm.test.atc.common.element.PageElement;

/**
 * @author nemda02
 */
public class AttributeRulesTable {

    public enum Operator {
        EQUALS("Equals"),
        STARTS_WITH("Starts with"),
        CONTAINS("Contains"),
        DOESNT_EQUAL("Doesn't equal"),
        IS_EMPTY("Is empty"),
        IS_NOT_EMPTY("Is not empty");
        
        private final String text;
        
        private Operator(String text) {
            this.text = text;
        }
        
        public String getText() {
            return text;
        }
    }
    
    public static final String NEW_ATTR_RULE_CELL_TEXT = "<new attribute rule>";
    
    private final UI ui;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AttributeRulesTable(UI ui) {
        this.ui = ui;
    }
    
    public PageElement getAttributeRulesPanel() {
        return ui.getElementProxy(By.cssSelector("attribute-grid"));
    }

    public PageElement getTable() {
        return ui.getElementProxy(By
            .cssSelector("#decoration-policies-grid .ui-grid-render-container-body"));
    }
    
    public PageElement getTableContent() {
        return getTable().findElement(
                By.cssSelector(".ui-grid-render-container-body .ui-grid-viewport"));
    }
    
    public PageElement getSearchInputBox() {
        return ui.getElementProxy(By.id("search-input-box"));
    }

    /**
     * This method returns only rendered rows if the table has many rows, because of table virtualization.
     * @return
     */
    public List<AttributeRulesTableRow> getRows() {
        return wrapRows(getTable().findPageElements(
                By.cssSelector(".ui-grid-row")));
    }

    public PageElement getTableHeader() {
        return getTable().findElement(By.cssSelector(".ui-grid-header-cell-row"));
    }

    private String getHeaderCellSelector(int index) {
        return "div.ui-grid-header-cell:nth-of-type(" + index + ")";
    }

    public PageElement getHeaderCell(int index) {
        return getTableHeader().findElement(
                By.cssSelector(getHeaderCellSelector(index)));
    }

    public PageElement getNewAttributeNameHeaderCell() {
        return getHeaderCell(AttributeRulesTableRow.getNewAttributeNameCellIndex());
    }

    public By getNewAttributeNameSortArrowLocator(boolean ascending) {
        String arrowSelector = ascending ? " i.ui-grid-icon-up-dir" : " i.ui-grid-icon-down-dir";
        return By.cssSelector(
                getHeaderCellSelector(AttributeRulesTableRow.getNewAttributeNameCellIndex()) + arrowSelector);
    }

    /**
     * 
     * @param ascending - true if ascending order is expected
     */
    public void sortByNewAttributeName(boolean ascending) {
        getNewAttributeNameHeaderCell().click();
        ui.waitUntilVisible(getNewAttributeNameSortArrowLocator(ascending));
    }

    public PageElement getInputField() {
        PageElement input = null;
        try {
            input = getTable().findElement(By.cssSelector("input.grid-edited-cell"));
        } catch (NoSuchElementException e) {
            logger.info("Input field not found");
        }
        return input;
    }

    public PageElement getDropDownList() {
        try {
            return ui
                .getElementProxy(
                    By.cssSelector("#decoration-policies-grid .ui-grid-render-container-body select[ui-grid-edit-dropdown]"),
                    3);
        } catch (TimeoutException e) {
            logger.info("Dropdown not found");
            return null;
        }
    }

    public void selectItemInDropDownList(String label) {
        ISelect comboSelection = new Select(getDropDownList().getWrappedElement());
        comboSelection.selectByVisibleText(label);
    }

    private By getIntellisensePopupSelector() {
        return By.cssSelector("ul.dropdown-menu.typeahead-popup");
    }

    public void waitForIntellisensePopup() {
        ui.waitUntilVisible(getIntellisensePopupSelector());
    }

    public PageElement getIntellisensePopup() {
        return ui.getElementProxy(getIntellisensePopupSelector());
    }

    public List<String> getIntellisensePopupOptions() {
        List<String> result = new ArrayList<String>();
        List<PageElement> foundOptions;
        try {
             foundOptions = ui.getElementProxy(getIntellisensePopupSelector())
                    .findPageElements(By.cssSelector("li > a"));
        } catch (TimeoutException ex) {
            return result;
        }
        for (PageElement webElement : foundOptions) {
            result.add(webElement.getText());
        }
        return result;
    }

    private List<AttributeRulesTableRow> wrapRows(List<PageElement> rows) {
        List<AttributeRulesTableRow> list = new ArrayList<AttributeRulesTableRow>(rows.size());
        for (PageElement row : rows) {
            list.add(new AttributeRulesTableRow(row, this));
        }
        return list;
    }

    public List<AttributeRulesTableRow> getRowsByParams(String newAttrName, String newAttrValue,
            String existingAttrName, Operator operator, String conditionValue) {

        List<AttributeRulesTableRow> allRows = getRows();
        for (int i = allRows.size() - 1; i >= 0; i--) {
            AttributeRulesTableRow row = allRows.get(i);
            String newAttrNameRow = row.getNewAttributeNameCell().getText();
            String newAttrValueRow = row.getNewAttributeValueCell().getText();
            String existingAttrNameRow = row.getExistingAttributeNameCell().getText();
            String operatorRow = row.getOperatorCell().getText();
            String conditionValueRow = row.getConditionValueCell().getText();
            if (!newAttrNameRow.equalsIgnoreCase(newAttrName) || !newAttrValueRow.equals(newAttrValue)
                    || !existingAttrNameRow.equalsIgnoreCase(existingAttrName) || !operatorRow.equalsIgnoreCase(operator.getText())
                    || ((conditionValue != null) && !conditionValueRow.equals(conditionValue))) {
                allRows.remove(i);
            }
        }

        return allRows;
    }

    public AttributeRulesTableRow getRowByNewAttrName(String newAttrName) throws Exception {
        List<AttributeRulesTableRow> allRows = getRows();
        AttributeRulesTableRow row = null;
        for (AttributeRulesTableRow tableRow : allRows) {
            if (tableRow.getNewAttributeNameCell().getText().equals(newAttrName)) {
                if (row != null) {
                    throw new Exception("Multiple rows with same new attribute names exists.");
                }
                row = tableRow;
            }
        }
        return row;
    }

    /**
     * 
     * @return Returns last (editable) table row
     */
    public AttributeRulesTableRow getLastRow(boolean editable) {
        getTableContent().scrollBottomIntoView();

        List<AttributeRulesTableRow> allRows = getRows();
        if (allRows.isEmpty()) {
            return null;
        } else {
            int index = editable ? 2 : 1;
            return allRows.get(allRows.size() - index);
        }
    }
    
    public AttributeRulesTableRow getLastRow() {
        return getLastRow(false);
    }

    public AttributeRulesTableRow createRow(String newAttrName, String newAttrValue, String existingAttrName, Operator operator) {
        AttributeRulesTableRow lastRow = getLastRow();

        lastRow.getNewAttributeNameCell().click();
        Utils.sleep(100);
        getInputField().sendKeys(newAttrName);

        lastRow.getNewAttributeValueCell().click();
        Utils.sleep(100);
        getInputField().sendKeys(newAttrValue);

        lastRow.getExistingAttributeNameCell().click();
        Utils.sleep(100);
        getInputField().sendKeys(existingAttrName);

        lastRow.getOperatorCell().click();
        Utils.sleep(100);
        getDropDownList().click();
        selectItemInDropDownList(operator.getText());

        lastRow.getAffectedComponentsCell().click();
        waitForReload();

        return lastRow;
    }
    
    public AttributeRulesTableRow createRow(String newAttrName, String newAttrValue, String existingAttrName,
                              Operator operator, String conditionValue) {
        getSearchInputBox().clear();
        
        AttributeRulesTableRow lastRow = getLastRow();

        lastRow.getNewAttributeNameCell().click();
        Utils.sleep(100);
        getInputField().sendKeys(newAttrName);

        lastRow.getNewAttributeValueCell().click();
        Utils.sleep(100);
        getInputField().sendKeys(newAttrValue);

        lastRow.getExistingAttributeNameCell().click();
        Utils.sleep(100);
        getInputField().sendKeys(existingAttrName);

        lastRow.getOperatorCell().click();
        Utils.sleep(100);
        getDropDownList().click();
        selectItemInDropDownList(operator.getText());

        lastRow.getConditionValueCell().click();
        Utils.sleep(100);
        getInputField().sendKeys(conditionValue);

        lastRow.getAffectedComponentsCell().click();
        waitForReload();

        return lastRow;
    }

    private void removeRows(String newAttrName, String newAttrValue, String existingAttrName,
            Operator operator, String conditionValue, boolean rowsExpected,
            Integer expectedRowsCount)
            throws Exception {

        // Limit the table rows to those that contain the required value of 'Custom Attribute Name'  
        logger.info("Setting search phrase: " + newAttrValue);
        getSearchInputBox().clear();
        getSearchInputBox().sendKeys(newAttrValue);
        Utils.sleep(250);
        
        List<AttributeRulesTableRow> rows =
                getRowsByParams(newAttrName, newAttrValue, existingAttrName, operator,
                        conditionValue);
        
        logger.debug("RemoveRows A - rows: " + rows);
        
        if (rowsExpected) {
            Assert.assertFalse(rows.isEmpty());
        }
        
        if (expectedRowsCount != null) {
            Assert.assertEquals(rows.size(), expectedRowsCount.intValue());
        }

        while(!rows.isEmpty()) {
            rows.get(0).delete();

            waitForReload();
            
            rows = getRowsByParams(
                newAttrName, newAttrValue, existingAttrName, operator, conditionValue);
        }

        logger.debug("RemoveRows B - rows: " + rows);
        
        Assert.assertTrue(rows.isEmpty());
    }

    public void removeRows(String newAttrName, String newAttrValue, String existingAttrName,
        Operator operator) throws Exception {
        removeRows(newAttrName, newAttrValue, existingAttrName, operator, null, true,
            null);
    }

    public void removeRows(String newAttrName, String newAttrValue, String existingAttrName,
        Operator operator, String conditionValue) throws Exception {
        removeRows(newAttrName, newAttrValue, existingAttrName, operator, conditionValue, true,
            null);
    }

    public void removeRows(String newAttrName, String newAttrValue, String existingAttrName,
        Operator operator, String conditionValue, int expectedRowsCount) throws Exception {
        removeRows(newAttrName, newAttrValue, existingAttrName, operator, conditionValue, true,
            expectedRowsCount);
    }

    public void removeRowsIfExist(String newAttrName, String newAttrValue, String existingAttrName,
        Operator operator, String conditionValue) throws Exception {
        removeRows(newAttrName, newAttrValue, existingAttrName, operator, conditionValue, false,
            null);
    }

    public PageElement getBatchDeleteButton() {
        return ui.getElementProxy(By.cssSelector(".batch-delete"));
    }

    public PageElement getCopyToUniverseButton() {
        return ui.getElementProxy(By.cssSelector(".copy-to-universe"));
    }

    public void selectAll() {
        if (!getTableHeader()
                .findElement(By.cssSelector(".select-all-rows input[type='checkbox']"))
                .isSelected()) {
            getTableHeader().findElement(By.cssSelector(".select-all-rows")).click();
        }
    }

    private boolean isLastRowForAddingNewRule() {
        if (getRows().size() >= 1) {
            if (NEW_ATTR_RULE_CELL_TEXT.equals(getLastRow().getNewAttributeNameCell().getText())) {
                logger.debug("Last row of attribute table is ready for adding new rule");
                return true;
            } else {
                logger.debug("Last row of attribute table is not ready for adding new rule");
                return false;
            }
        } else {
            logger.debug("Attribute rule table is empty");
            return false;    
        }
    }
    
    public void deleteAll() throws Exception {
        int rowCount = getRows().size();
        boolean isLastRowForAdding = isLastRowForAddingNewRule(); 
        if ((isLastRowForAdding && rowCount > 1) || (!isLastRowForAdding && rowCount > 0)) {
            logger.info("Removing all attribute rules - start; row count = " + rowCount);
            
            // If there is a single rule, there is no checkbox for marking the rule and no batch delete button 
            if ((isLastRowForAdding && rowCount == 2) || (!isLastRowForAdding && rowCount == 1)) {
                logger.debug("There is a single attribute rule row in the table, deleting individually");
                List<AttributeRulesTableRow> rows = getRows();
                rows.get(0).delete();
            } else {
                logger.debug("There are multiple attribute rule rows in the table, marking all and deleting with batch delete");
                selectAll();
                getBatchDeleteButton().click();
                
                ModalDialog dialog = ui.getModalDialog();
                dialog.clickButton(DialogButton.YES);
            }
                   
            waitForReload();
            
            rowCount = getRows().size();
            logger.info("Removing all attribute rules - end; row count = " + rowCount);

            // If filtering is applied, no 'new attribute rule' row is displayed
            
            if (isLastRowForAddingNewRule()) {
                Assert.assertEquals(rowCount, 1);
            } else {
                Assert.assertEquals(rowCount, 0);
            }
        }
    }

    public ATCDropDownOpener getUniverseDropdown() {
        return new ATCDropDownOpener(ui.getElementProxy(getUniverseDropdownSelector()));
    }
    
    public By getUniverseDropdownSelector() {
        return By.id("attr-rules-universe-selection-combo");
    }
    
    public By getUniverseDropdownMenuSelector() {
        return By.id("attr-rules-universe-selection-combo-menu");
    }

    /** 
     * Return the name of the universe selected in the dropdown which determines the universe from which rules should be displayed
     * @return
     */
    public String getSelectedUniverseName() {
        return getUniverseDropdown().getText();
    }
    
    public void selectUniverse(String name) {
        getUniverseDropdown().selectFromDropdown(getUniverseDropdownMenuSelector(), name);
    }

    public void copySelectedToUniverses(List<String> universeNames) throws Exception {
        getCopyToUniverseButton().click();
        ModalDialog dialog = ui.getModalDialog();
        for (String name : universeNames) {
            String xpath = "//label[text() = '" + name + "']";
            dialog.findBySelector(By.xpath(xpath)).click();
        }
        dialog.clickButton(DialogButton.SAVE);
        waitForReload();
    }

    public void waitForReload() {
        ui.getLeftNavigationPanel().waitForWorkIndicator(LeftNavigationPanel.SettingsItems.ATTRIBUTE_RULES);
    }
}
