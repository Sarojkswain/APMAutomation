package com.ca.apm.test.atc.common.element;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.ca.apm.test.atc.common.AttributeRulesTable;
import com.ca.apm.test.atc.common.ModalDialog;
import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;

public class AttributeRulesTableRow extends WebElementWrapper {

    private static final int NEW_ATTRIBUTE_NAME_INDEX = 2;
    private static final int NEW_ATTRIBUTE_VALUE_INDEX = 3;
    private static final int EXISTING_ATTRIBUTE_NAME_INDEX = 4;
    private static final int OPERATOR_INDEX = 5;
    private static final int CASE_SENSITIVE_INDEX = 6;
    private static final int CONDITION_VALUE_INDEX = 7;
    private static final int AFFECTED_COMPONENTS_INDEX = 8;
    private static final int ACTIONS_INDEX = 9;
    
    protected AttributeRulesTable motherTable;

    public AttributeRulesTableRow(WebElement element, UI ui, AttributeRulesTable table) {
        super(element, ui);
        this.motherTable = table;
    }

    public AttributeRulesTableRow(PageElement element, AttributeRulesTable table) {
        super(element);
        this.motherTable = table;
    }

    public static int getNewAttributeNameCellIndex() {
        return NEW_ATTRIBUTE_NAME_INDEX;
    }
    
    public PageElement getCell(int index) {
        return findElement(By.cssSelector("div.ui-grid-cell:nth-of-type(" + index + ")"));
    }

    public PageElement getNewAttributeNameCell() {
        return getCell(NEW_ATTRIBUTE_NAME_INDEX);
    }

    public PageElement getNewAttributeValueCell() {
        return getCell(NEW_ATTRIBUTE_VALUE_INDEX);
    }

    public PageElement getExistingAttributeNameCell() {
        return getCell(EXISTING_ATTRIBUTE_NAME_INDEX);
    }

    public PageElement getOperatorCell() {
        return getCell(OPERATOR_INDEX);
    }

    public PageElement getCaseSensitiveCell() {
        return getCell(CASE_SENSITIVE_INDEX);
    }

    public PageElement getConditionValueCell() {
        return getCell(CONDITION_VALUE_INDEX);
    }

    public PageElement getAffectedComponentsCell() {
        return getCell(AFFECTED_COMPONENTS_INDEX);
    }

    public PageElement getActionsCell() {
        return getCell(ACTIONS_INDEX);
    }

    public PageElement getDuplicateButton() {
        try {
            return getActionsCell().findElement(By.cssSelector(".duplicate-button"));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public PageElement getDeleteButton() {
        try {
            return getActionsCell().findElement(By.cssSelector(".delete-button"));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void duplicate() {
        getDuplicateButton().click();
        motherTable.waitForReload();
    }

    public void delete() throws Exception {
        getDeleteButton().click();

        ModalDialog dialog = ui.getModalDialog();
        dialog.clickButton(DialogButton.YES);

        motherTable.waitForReload();
    }

    public void editCell(int index, String newValue) {
        getCell(index).click();
        Utils.sleep(100);
        motherTable.getInputField().sendKeys(newValue);
        getAffectedComponentsCell().click();
        motherTable.waitForReload();
    }

    public void editNewAttributeNameCell(String newAttributeName) {
        editCell(NEW_ATTRIBUTE_NAME_INDEX, newAttributeName);
    }

    public void select() {
        if (!getCell(1).findElement(By.cssSelector(".nice-checkbox input[type='checkbox']"))
                .isSelected()) {
            getCell(1).findElement(By.cssSelector(".nice-checkbox label")).click();
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[row|");
        s.append("CustomAttrName='").append(getNewAttributeNameCell().getText()).append("',");
        s.append("NewlyAssignedValue='").append(getNewAttributeValueCell().getText()).append("',");
        s.append("ExistingAttrName='").append(getExistingAttributeNameCell().getText()).append("',");
        s.append("MatchingOperator='").append(getOperatorCell().getText()).append("',");
        s.append("Condition='").append(getConditionValueCell().getText()).append("'");
        s.append("]");
        return s.toString();
    }
}
