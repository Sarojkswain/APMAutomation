package com.ca.apm.nextgen.tests.helpers;

import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.CHOOSE_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.CONFIRMATION_DIALOG_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.CONFIRMATION_DIALOG_NO_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.CONFIRMATION_DIALOG_YES_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.DANGER_ACTION_LIST_GRID_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.ELEMENTS_DROP_DOWN_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.ELEMENTS_MENU_DROP_DOWN_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_ACTIONS_LIST_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_ACTIVE_CECK_BOX_NAME;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_ADD_ACTION_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_APPLY_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_DELETE_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_NAME_FIELD_INPUT_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_SEARCH_CONTENT_PANEL_GRID_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_SEARCH_GO_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_SEARCH_INPUT_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.NEW_ELEMENT_CREATION_DIALOG_OK_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.NEW_ELEMENT_CREATION_DIALOG_PANEL_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.NEW_ELEMENT_FORCE_UNIQUENESS_CHECK_BOX_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.NEW_ELEMENT_MANAGEMENT_MODULE_COMBO_BOX_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.NEW_ELEMENT_NAME_FIELD_ID;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.nextgen.tests.common.ManagementElementType;
import com.ca.apm.nextgen.tests.common.WvManagementTabConstants;
import com.ca.apm.nextgen.tests.helpers.TableUi.TableRowRecord;

/**
 * Utilities class providing helper functions to work with WebView's Management tab UI.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ManagementTabUtils {
    
    public static final String NODE_ID_XPATH = "//div[@ftid='%s']";
    public static final String CLICKABLE_NODE_ID_XPATH = "//div[@ftid='%s']/div[1]";
    public static final String CHOOSE_ACTION_GRID_XPATH = ".//div[@id='" + WvManagementTabConstants.CHOOSE_ACTION_DIALOG_GRID_ID + "']";
    public static final String CHOOSE_DIALOG_XPATH = "//div[text()='Choose Action']/../../../../..";
    public static final String MANAGEMENT_MODULE_COLUMN_NAME = "Management Module";
    public static final String NAME_COLUMN_NAME = "Name";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementTabUtils.class);

    /**
     * 
     * @param ui
     * @param searchToken
     * @return
     */
    public static WebElement runSearch(WebViewUi ui, String searchToken) {
        //Wait for the Search field to load.
        WebElement searchInputField = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(MM_SEARCH_INPUT_ID)));
        //Clear it.
        searchInputField.clear();
        //Fill it with the provided search token.
        ui.getActions().moveToElement(searchInputField).click().sendKeys(searchToken).perform();
        
        WebElement goButton = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(MM_SEARCH_GO_BUTTON_ID)));
        ui.getActions().moveToElement(goButton).click().build().perform();

        return ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(MM_SEARCH_CONTENT_PANEL_GRID_ID)));
    }
    
    /**
     * Clicks on a navigation tree node in the management tab. The tree node's parent node must 
     * be expanded, otherwise the node will not be found.
     *  
     * @param ui        WebViewUi driver
     * @param nodeId    node id (ftid attribute value)
     * @return          node element
     */
    public static WebElement clickOnNavigationTreeNode(WebViewUi ui, String nodeId) {
        WebElement node = ui.getWebElement(By.xpath(String.format(Locale.US, CLICKABLE_NODE_ID_XPATH, nodeId)));
        ui.getActions().moveToElement(node).click().build().perform();
        return node;
    }
    
    /**
     * Formats and returns XPATH for locating nodes in the Management tab navigation tree component.
     * 
     * @param   nodeId  node id
     * @return          formatted XPATH 
     */
    public static String getTreeNodeIdXpath(String nodeId) {
        return String.format(Locale.US, NODE_ID_XPATH, nodeId);
    }
    
    /**
     * Presses on remove management element button and optionally performs the element removal process.
     *  
     * @param ui                        WebViewUi driver
     * @param managementElemNodeId
     * @param clickYes
     */
    public static void deleteManagementElement(WebViewUi ui, String managementElemNodeId, boolean clickYes) {
        //Navigate to the management element node.
        clickOnNavigationTreeNode(ui, managementElemNodeId);

        WebElement managementElementNameTextField = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(MM_EDITOR_NAME_FIELD_INPUT_ID)));
        
        assertNotNull(managementElementNameTextField);
        String managementElementName = managementElementNameTextField.getAttribute("value");
        
        assertNotNull(managementElementName);
        assertTrue(!managementElementName.isEmpty());

        //Click the Delete button.
        By deleteButtonSelector = By.id(MM_EDITOR_DELETE_BUTTON_ID);
        ui.clickDialogButton(ui.getWebDriver(), deleteButtonSelector);

        //Wait for confirmation dialog to render.
        WebElement confirmDialog = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(CONFIRMATION_DIALOG_ID)));

        WebElement confirmationMessageElement = ui.getWebElementOrNull(By.id(CONFIRMATION_DIALOG_ID));
        
        assertNotNull(confirmationMessageElement);
        String msg = confirmationMessageElement.getText();
        assertNotNull(msg);
        
        assertTrue(msg.contains(managementElementName));
        
        if (clickYes) {
            //Click 'Yes' button.
            ui.clickDialogButton(confirmDialog,
                By.id(CONFIRMATION_DIALOG_YES_BUTTON_ID));
        } else {
            //Click 'No' button.
            ui.clickDialogButton(confirmDialog,
                By.id(CONFIRMATION_DIALOG_NO_BUTTON_ID));
        }

        ui.waitFor(ExpectedConditions.stalenessOf(confirmDialog));
    }
    
    /**
     * Finds action named <code>actionName</code> 
     * 
     * @param ui                       WebViewUi driver
     * @param actionsContainer
     * @param actionName
     * @return
     */
    public static WebElement findActionElementInActionsList(WebViewUi ui,
                                                            String actionName) {
        try {
            WebElement actionsContainer = ui.getWebElement(
                By.id(MM_EDITOR_ACTIONS_LIST_ID));

            TableUi tableUi = new TableUi(ui, actionsContainer,
                By.id(DANGER_ACTION_LIST_GRID_ID));
                
            WebElement actionNameElement = null;
            for (TableRowRecord rowRec : tableUi) {
                List<String> rowValues = rowRec.getValues();
                String actName = rowValues!= null && rowValues.size() > 1 ? rowValues.get(1) : null; 
                if (actionName.equals(actName)) {
                    LOGGER.info("Found row with '{}' action.", actName);
                    actionNameElement = rowRec.getValuesElements().get(1);
                    break;
                }
            }
            return actionNameElement;
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e,
                "Failed to find action ''{1}'' in actions list. Exception: {0}", actionName);
        }
    }
    
    /**
     * Opens a danger action choose dialog.
     * 
     * @param   ui     WebViewUi
     * @return         dialog element
     */
    public static WebElement openChooseActionDialog(WebViewUi ui) {
        //Click 'Add' button to open up a Danger Action selection dialog.
        ui.clickDialogButton(ui.getWebDriver(),
            By.id(MM_EDITOR_ADD_ACTION_BUTTON_ID));

        // Wait for Choose Action dialogue to appear and pick an action.
        WebElement chooseActionDialog = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(CHOOSE_DIALOG_XPATH)));
        return chooseActionDialog;
    }
    
    /**
     * Chooses desired danger action from the dialog.
     * 
     * @param ui                     WebViewUi driver
     * @param chooseActionDialog     choose action dialog context
     * @param actionName             action to pick
     * @param managementModuleName   management module to match
     * @return                       <code>true</code> if a corresponding row was found and chosen, otherwise
     *                               <code>false</code> 
     */
    public static boolean chooseAction(WebViewUi ui, WebElement chooseActionDialog, String actionName,
                                       String managementModuleName) {
        try {
            TableUi tableUi = new TableUi(ui, chooseActionDialog,
                By.xpath(CHOOSE_ACTION_GRID_XPATH));
                
            Map<String, Integer> headersMap = tableUi.getHeaderTextToIndexMap();
            final int mmIndex = headersMap.get(MANAGEMENT_MODULE_COLUMN_NAME);
            final int nameIndex = headersMap.get(NAME_COLUMN_NAME);
            
            WebElement alertCellElement = null;
            for (int i = 0, rowCount = tableUi.rowCount(); i != rowCount; ++i) {
                List<String> rowValues = tableUi.getRowValues(i);
                String name = StringUtils.trim(rowValues.get(nameIndex));
                String mmName = rowValues.get(mmIndex);
                if (name.equals(actionName) && mmName.equals(managementModuleName)) {
                    alertCellElement = tableUi.getCellElement(i, nameIndex);
                    break;
                }
            }
            
            if (alertCellElement == null) {
                LOGGER.info("No action named '{}' for management module '{}' found.", 
                    actionName, managementModuleName);
                return false;
            }
            
            // Choose the row.
            WebElement cellTextElement = ui.getWebElement(alertCellElement, By.xpath(".//div[text()]"));
                
            ui.getActions()
              .moveToElement(cellTextElement)
              .click()
              .perform();

            // Click Choose dialog button.
            ui.clickDialogButton(chooseActionDialog,
                By.id(CHOOSE_BUTTON_ID));

            // Wait for it to disappear.
            ui.waitFor(ExpectedConditions.stalenessOf(chooseActionDialog));
            return true;
        } catch (Throwable e) {
            ui.takeScreenShot("choose-danger-action-");
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e,
                "Failed to pick action ''{1}'' from Choose Action table. Exception: {0}",
                actionName);
        }
    }

    /**
     * On the 'Management' tab of a WebView creates a new element of the specified <code>elementType</code> 
     * with the given <code>name</code> and connects it to the management module identified by <code>managementModuleName</code>.
     * 
     * @param ui                       WebViewUi driver
     * @param name                     element name
     * @param managementModuleName     management module name
     * @param elementType              element type
     * @param forceUniqueness          force unique check?
     * @param active                   should the element be set active? (applies only to alerts and actions)
     */
    public static String createNewManagementElement(WebViewUi ui, String name, String managementModuleName, 
                                                  ManagementElementType elementType, boolean forceUniqueness, boolean active) {
        //Wait until 'Elements' drop down button is visible.
        By elements = By.id(ELEMENTS_DROP_DOWN_BUTTON_ID);
        ui.waitFor(ExpectedConditions.visibilityOfElementLocated(elements));

        //Expand the drop down. 
        ui.clickDialogButton(ui.getWebDriver(), elements);

        //Wait until elements menu is visible.
        WebElement elementsMenu = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(ELEMENTS_MENU_DROP_DOWN_ID)));
        
        //Select item from the elements menu.
        ui.moveToMenuButton(elementsMenu, By.id(elementType.getMenuItemId()));

        if (elementType.getSubMenuId() != null) {
            //Wait until submenu is visible.
            WebElement subMenu = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(elementType.getSubMenuId())));
            
            //Click on submenu item.
            ui.clickMenuButton(subMenu, By.id(elementType.getSubMenuItemId()));
        }

        //Wait for dialog to appear.
        WebElement newElementDialog = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(NEW_ELEMENT_CREATION_DIALOG_PANEL_ID)));

        fillInAndSaveNewManagementElement(ui, newElementDialog, name, managementModuleName, forceUniqueness);
        
        if (active) {
            WebElement activeCheckBoxElement = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.name(MM_EDITOR_ACTIVE_CECK_BOX_NAME)));
            ui.checkCheckBox(activeCheckBoxElement);
            
            //Save changes by pressing Apply button.
            ui.clickDialogButton(ui.getWebDriver(),
                By.id(MM_EDITOR_APPLY_BUTTON_ID));

        }

        WebElement createdElementTextField = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(MM_EDITOR_NAME_FIELD_INPUT_ID)));
        return createdElementTextField.getAttribute("value");
    }
    
    /**
     * Fills in management module element data into the <code>newElementDialog</code> and 
     * clicks save.
     * 
     * @param ui                          WebViewUi driver
     * @param newElementDialog            dialog context
     * @param name                        element name
     * @param managementModulemName       management module name
     * @param forceUniqueness             force unique check?
     */
    public static void fillInAndSaveNewManagementElement(WebViewUi ui, WebElement newElementDialog,
                                                         String name, String managementModulemName, 
                                                         boolean forceUniqueness) {
        //Fill the dialog in.
        fillNewElementDialog(ui, newElementDialog, name, managementModulemName, forceUniqueness);

        //Click OK button.
        ui.clickDialogButton(ui.getWebDriver(), By.id(NEW_ELEMENT_CREATION_DIALOG_OK_BUTTON_ID));

        //Wait for the dialog to disappear.
        ui.waitFor(ExpectedConditions.stalenessOf(newElementDialog));
    }
    
    /**
     * Fills in data into a new element creation dialog on Management tab of the WebView UI.
     * These can be actions, alerts, etc. - whatever is provided in the <code>'elements'</code> combo box. 
     * 
     * @param ui                        WebViewUi driver
     * @param newElementDialog          new element dialog context
     * @param name                      new element name
     * @param managementModuleName      name of the management module to connect with 
     * @param forceUniqueness           check uniqueness
     */
    public static void fillNewElementDialog(WebViewUi ui, SearchContext newElementDialog, String name, String managementModuleName,
                                            Boolean forceUniqueness) {
        try {
            WebElement nameElement = ui.getWebElement(newElementDialog,
                By.id(NEW_ELEMENT_NAME_FIELD_ID));
                
            ui.clearAndSetInputField(nameElement, name);
            
            WebElement mmNameElement = ui.getWebElement(newElementDialog,
                By.id(NEW_ELEMENT_MANAGEMENT_MODULE_COMBO_BOX_ID));
                
            ui.clearAndSetInputField(mmNameElement, managementModuleName);
            
            WebElement forceUniquenessCheckbox = ui.getWebElement(newElementDialog,
                By.name(NEW_ELEMENT_FORCE_UNIQUENESS_CHECK_BOX_ID));
                
            if (forceUniqueness != null) {
                if (forceUniqueness) {
                    ui.checkCheckBox(forceUniquenessCheckbox);
                } else {
                    ui.uncheckCheckBox(forceUniquenessCheckbox);
                }
            }
        } catch (Throwable e) {
            ui.takeScreenShot("fill-" + name + "-element-dialog-");
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e,
                "Failed to fill new element dialog. Exception: {0}");
        }
    }

}
