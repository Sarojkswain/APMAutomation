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
package com.ca.apm.test.atc.attributes;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.*;
import com.ca.apm.test.atc.common.AttributeRulesTable.Operator;
import com.ca.apm.test.atc.common.DetailsPanel.AttributeType;
import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.element.AttributeRow;
import com.ca.apm.test.atc.common.element.AttributeRulesTableRow;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.testbed.atc.TeamCenterRegressionTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class AttributeRulesTest extends UITest {

    private static final String NEW_ATTRIBUTE_NAME = "e2e_attr_rules_test";
    private static final String NEW_ATTRIBUTE_NAME_RENAMED = "e2e_attr_rules_test_renamed";
    private static final String NEW_ATTRIBUTE_VALUE = "value";
    private static final String EXISTING_ATTRIBUTE_NAME = "type";
    private static final String EXISTING_ATTRIBUTE_NAME_AG = "agent";
    private static final String EXISTING_ATTRIBUTE_NAME_BT = "Business Transaction";
    private static final String EXISTING_ATTRIBUTE_NAME_AN = "Application";
    private static final Operator OPERATOR = Operator.DOESNT_EQUAL;
    private static final String CONDITION_VALUE = "EXTERNAL";
    private static final String UNIVERSE_FOR_GUEST = "e2e_universe_for_guest";

    private UI ui;
    private Timeline timeline;

    private void initialize() throws Exception {
        ui = getUI();
        timeline = ui.getTimeline();
        logger.info("logging in as Admin");
        ui.login();
        
        logger.info("go to MapView as in ExperienceView there is no ENTERPRISE universe");
        ui.waitForWorkIndicator();
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        logger.info("select the default universe");
        if (!ui.getTopNavigationPanel().getActiveUniverse().equals(Universe.DEFAULT_UNIVERSE)) {
            ui.getTopNavigationPanel().selectUniverse(Universe.DEFAULT_UNIVERSE);
        }
        
        logger.info("switching to attribute rules");
        ui.waitForWorkIndicator();
        ui.getLeftNavigationPanel().goToDecorationPolicies();
    }
    
    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test(groups = "failing")
    
    /**
     * Test that guest cannot see rules created in the default universe AKA "ENTERPRISE" or Global universe
     * @throws Exception
     */
    public void testAuthorizationInDefaultUniverse() throws Exception {
        initialize();
        
        testAuthorization(Universe.DEFAULT_UNIVERSE, 0);
        
        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    /**
     * Test that guest can see rules created in the universe for which he has read permissions but he cannot edit rules or add new rules
     * @throws Exception
     */
    public void testAuthorizationInOrdinaryUniverse() throws Exception {
        initialize();
        
        ui.getLeftNavigationPanel().goToUniverses();
        UniverseSettings univSettings = ui.getUniverseSettings();
        if (!univSettings.isUniversePresent(UNIVERSE_FOR_GUEST)) {
            univSettings.createUniverse(UNIVERSE_FOR_GUEST);
            univSettings.addUser(UNIVERSE_FOR_GUEST, UI.Role.GUEST.getUser(), UI.Permission.read);
        }
                
        ui.getTopNavigationPanel().selectUniverse(UNIVERSE_FOR_GUEST);
        
        testAuthorization(UNIVERSE_FOR_GUEST, 1);
        
        ui.getLeftNavigationPanel().goToUniverses();
        if (univSettings.isUniversePresent(UNIVERSE_FOR_GUEST)) {
            univSettings.deleteUniverse(UNIVERSE_FOR_GUEST);
        }
        
        ui.cleanup();
    }
    
    private void testAuthorization(String universeName, int expectedRulesVisibleByGuest) throws Exception {
        ui.getLeftNavigationPanel().goToDecorationPolicies();
        
        AttributeRulesTable attrRulesTable = ui.getAttributeRulesTable();
       
        Assert.assertEquals(attrRulesTable.getUniverseDropdown().getSelectedOption("Universe:"), universeName, 
            "The universe selection at the attribute rule table should match that in the top bar.");

        attrRulesTable.deleteAll();
        attrRulesTable.createRow(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);

        logger.info("check that the attribute rule table is editable for admin");
        Assert.assertEquals(attrRulesTable.getRows().size(), 2);
        Assert.assertEquals(attrRulesTable.getLastRow().getNewAttributeNameCell().getText(), AttributeRulesTable.NEW_ATTR_RULE_CELL_TEXT);

        logger.info("logging out admin");
        ui.logout();

        logger.info("logging in as Guest");
        ui.login(Role.GUEST);

        ui.getLeftNavigationPanel().goToDecorationPolicies();

        logger.info("checking that the attribute rules table is not editable");
        Assert.assertEquals(attrRulesTable.getRows().size(), expectedRulesVisibleByGuest, "It is expected that guest can see " + expectedRulesVisibleByGuest 
            + " attribute rules created in the " + universeName + " universe");
        
        if (expectedRulesVisibleByGuest > 0) {
            AttributeRulesTableRow lastRow = attrRulesTable.getLastRow();
            Assert.assertFalse(lastRow.getDeleteButton().isDisplayed());
            Assert.assertFalse(lastRow.getDuplicateButton().isDisplayed());
    
            lastRow.getNewAttributeNameCell().click();
            Assert.assertFalse(attrRulesTable.getInputField().isDisplayed());
            lastRow.getNewAttributeValueCell().click();
            Assert.assertFalse(attrRulesTable.getInputField().isDisplayed());
            lastRow.getExistingAttributeNameCell().click();
            Assert.assertFalse(attrRulesTable.getInputField().isDisplayed());
            lastRow.getConditionValueCell().click();
            Assert.assertFalse(attrRulesTable.getInputField().isDisplayed());
            lastRow.getOperatorCell().click();
            Assert.assertFalse(attrRulesTable.getDropDownList().isDisplayed());
            
            Assert.assertFalse(lastRow.getCaseSensitiveCell().findElement(By.cssSelector("input[type='checkbox']")).isEnabled());
            
            boolean isException = false;
            try {
                lastRow.select();
            } catch (NoSuchElementException e) {
                isException = true;
            }
            Assert.assertTrue(isException);
        }

        logger.info("cleaning up");
        ui.logout();
        
        initialize();
        
        attrRulesTable.deleteAll();
    }
    
    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testAttributeDecoratingInDefaultUniverse() throws Exception {
        initialize();
        
        testAttributesDecorating(Universe.DEFAULT_UNIVERSE);
    }
    
    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testAttributeDecoratingInOrdinaryUniverse() throws Exception {
        initialize();
        
        final String activeUniverse = ui.getTopNavigationPanel().getActiveUniverse();
        logger.info("Active universe is " + activeUniverse);
        
        if (Universe.DEFAULT_UNIVERSE.equals(activeUniverse)) {
            List<String> universeNames = ui.getTopNavigationPanel().getUniverseNames();
            logger.info("Available universes:  " + activeUniverse);
            
            for (final String univName : universeNames) {
                if (!Universe.DEFAULT_UNIVERSE.equals(univName)) {
                    testAttributesDecorating(univName);
                    break;
                }
            }
        }
    }
    
    private void testAttributesDecorating(String universeName) throws Exception {
        if (!ui.getTopNavigationPanel().getActiveUniverse().equals(universeName)) {
            ui.getTopNavigationPanel().selectUniverse(universeName);
        }
        
        logger.info("turn on live mode");
        ui.getLeftNavigationPanel().goToMapViewPage();
        timeline.turnOnLiveMode();
        
        ui.getLeftNavigationPanel().goToDecorationPolicies();
        AttributeRulesTable attrRulesTable = ui.getAttributeRulesTable();
        
        List<AttributeRulesTableRow> rows =
                attrRulesTable.getRowsByParams(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE,
                        EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);
        
        if (rows.isEmpty()) {
            logger.info("creating an attribute rule");
            attrRulesTable.createRow(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME,
                    OPERATOR, CONDITION_VALUE);
        }

        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        logger.info("turn off live mode to be sure the cache on backend is not applied");
        timeline.turnOffLiveMode();
        
        ui.getCanvas().waitForUpdate();
        ui.getCanvas().getCtrl().fitAllToView();
        
        logger.info("checking that the attribute rule was applied");
        List<PageElement> nodes = ui.getCanvas().getListOfNodes();
        Assert.assertFalse(nodes.isEmpty());
        for (WebElement node : nodes) {
            node.click();
            ui.getDetailsPanel().waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
            ui.getDetailsPanel().scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
            List<AttributeRow> attrRows = ui.getDetailsPanel().getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME);
            Assert.assertEquals(attrRows.size(), 1);
            Assert.assertEquals(attrRows.get(0).getEndTimeValueCell().getText(), NEW_ATTRIBUTE_VALUE);
        }

        ui.getLeftNavigationPanel().goToDecorationPolicies();

        logger.info("removing the previously created attribute rule");
        attrRulesTable.removeRows(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);

        logger.info("switching to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        logger.info("turn live mode on and off to be sure to watch the current moment while the cache on backend is not applied");
        timeline.turnOnLiveMode();
        timeline.turnOffLiveMode();
        
        ui.getCanvas().waitForUpdate();
        ui.getCanvas().getCtrl().fitAllToView();
        
        logger.info("checking that the attribute rule is no longer applied");
        nodes = ui.getCanvas().getListOfNodes();
        Assert.assertFalse(nodes.isEmpty());
        for (WebElement node : nodes) {
            node.click();
            ui.getDetailsPanel().waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
            ui.getDetailsPanel().scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
            List<AttributeRow> attrRows = ui.getDetailsPanel().getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME);
            Assert.assertTrue(attrRows.isEmpty());
        }

        logger.info("cleaning up");
        ui.logout();
        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testCRUDOperations() throws Exception {
        doTestCRUDOperations(EXISTING_ATTRIBUTE_NAME_AG, true);
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testCRUDOperationsWithFriendlyNameOfExistingAttribute1() throws Exception {
        doTestCRUDOperations(EXISTING_ATTRIBUTE_NAME_BT, false);
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testCRUDOperationsWithFriendlyNameOfExistingAttribute2() throws Exception {
        doTestCRUDOperations(EXISTING_ATTRIBUTE_NAME_AN, false);
    }
    
    private void doTestCRUDOperations(String existingAttributeName, boolean testCaseSensitivity) throws Exception {
        initialize();

        logger.info("making sure the rule does not already exist");
        AttributeRulesTable attrRulesTable = ui.getAttributeRulesTable();
        attrRulesTable.removeRowsIfExist(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE, existingAttributeName, OPERATOR, CONDITION_VALUE);
        attrRulesTable.removeRowsIfExist(NEW_ATTRIBUTE_NAME_RENAMED, NEW_ATTRIBUTE_VALUE, existingAttributeName, OPERATOR, CONDITION_VALUE);

        logger.info("creating an attribute rule");
        attrRulesTable.createRow(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE, existingAttributeName,
                OPERATOR, CONDITION_VALUE);

        logger.info("checking the rule was created");
        ui.getLeftNavigationPanel().goToPerspectives();
        ui.waitForWorkIndicator();
        ui.getLeftNavigationPanel().goToDecorationPolicies();
        List<AttributeRulesTableRow> rows = attrRulesTable.getRowsByParams(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE,
                existingAttributeName, OPERATOR, CONDITION_VALUE);
        Assert.assertEquals(rows.size(), 1);

        Assert.assertEquals(
            attrRulesTable.getRowsByParams(NEW_ATTRIBUTE_NAME_RENAMED, NEW_ATTRIBUTE_VALUE,
                existingAttributeName, OPERATOR, CONDITION_VALUE).size(), 0);

        logger.info("updating the rule");
        rows.get(0).editNewAttributeNameCell(NEW_ATTRIBUTE_NAME_RENAMED);

        logger.info("checking the rule was updated");
        ui.getLeftNavigationPanel().goToPerspectives();
        ui.getLeftNavigationPanel().goToDecorationPolicies();
        rows =
            attrRulesTable.getRowsByParams(NEW_ATTRIBUTE_NAME_RENAMED, NEW_ATTRIBUTE_VALUE,
                existingAttributeName, OPERATOR, CONDITION_VALUE);
        Assert.assertEquals(rows.size(), 1);
        
        logger.info("system attribute is not allowed as custom attribute name");
        rows.get(0).getNewAttributeNameCell().click();
        attrRulesTable.getInputField().sendKeys("type");
        rows.get(0).getAffectedComponentsCell().click();
        ModalDialog dialog = ui.getModalDialog();
        dialog.clickButton(DialogButton.YES);
        Assert.assertEquals(rows.get(0).getNewAttributeNameCell().getText(), NEW_ATTRIBUTE_NAME_RENAMED);
        
        if (testCaseSensitivity) {
            logger.info("case sensitivity check");
            attrRulesTable.createRow("location", NEW_ATTRIBUTE_VALUE, existingAttributeName, Operator.IS_EMPTY);
            attrRulesTable.createRow("Location", NEW_ATTRIBUTE_VALUE, existingAttributeName, Operator.IS_NOT_EMPTY);
            
            Utils.sleep(5000);
            
            ui.getLeftNavigationPanel().goToMapViewPage();
            FilterBy f = ui.getFilterBy();
            f.add("location");
            f.add("Location");
        }
      
        logger.info("removing the attribute rules");
        ui.getLeftNavigationPanel().goToDecorationPolicies();
        attrRulesTable.removeRows(NEW_ATTRIBUTE_NAME_RENAMED, NEW_ATTRIBUTE_VALUE, existingAttributeName, OPERATOR, CONDITION_VALUE);
        
        if (testCaseSensitivity) {
            attrRulesTable.removeRows("location", NEW_ATTRIBUTE_VALUE, existingAttributeName, Operator.IS_EMPTY);
            attrRulesTable.removeRows("Location", NEW_ATTRIBUTE_VALUE, existingAttributeName, Operator.IS_NOT_EMPTY);
        }
       
        logger.info("cleaning up");
        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testDuplicationAndScrolling() throws Exception {
        initialize();

        logger.info("turning off live mode");
        ui.getLeftNavigationPanel().goToMapViewPage();
        timeline.turnOffLiveMode();
        ui.getLeftNavigationPanel().goToDecorationPolicies();

        logger.info("making sure the rule does not already exist");
        AttributeRulesTable attrRulesTable = ui.getAttributeRulesTable();
        attrRulesTable.removeRowsIfExist(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);

        logger.info("creating an attribute rule");
        AttributeRulesTableRow lastRow =
                attrRulesTable.createRow(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);

        final int rulesToDuplicate = 30;
        logger.info("duplicating the rule {} times", rulesToDuplicate);
        List<AttributeRulesTableRow> rows;
        for (int i = 0; i < rulesToDuplicate; i++) {
            ui.waitForWorkIndicator();
            rows = attrRulesTable.getRowsByParams(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);
            Assert.assertFalse(rows.isEmpty());
            lastRow = rows.get(rows.size() - 1);
            if (!lastRow.isDisplayed()) {
                lastRow.getActionsCell().scrollIntoView();
            }

            logger.info("iteration {}", i + 1);
            lastRow.duplicate();
        }

        int prevRenamed =
                attrRulesTable.getRowsByParams(NEW_ATTRIBUTE_NAME_RENAMED, NEW_ATTRIBUTE_VALUE,
                        EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE).size();

        logger.info("updating the last rule");
        lastRow = attrRulesTable.getLastRow(true);
        lastRow.editCell(2, NEW_ATTRIBUTE_NAME_RENAMED);

        logger.info("checking the rule was updated");
        Assert.assertEquals(
                attrRulesTable.getRowsByParams(NEW_ATTRIBUTE_NAME_RENAMED, NEW_ATTRIBUTE_VALUE,
                        EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE).size(), prevRenamed + 1);

        logger.info("deleting the rules");
        attrRulesTable.deleteAll();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testIntellisense() throws Exception {
        initialize();

        AttributeRulesTable attrRulesTable = ui.getAttributeRulesTable();

        logger.info("intellisense popup should open");
        AttributeRulesTableRow lastRow = attrRulesTable.getLastRow();
        lastRow.getExistingAttributeNameCell().click();
        attrRulesTable.waitForIntellisensePopup();
        int initialCount = attrRulesTable.getIntellisensePopupOptions().size();
        Assert.assertTrue(initialCount > 0);

        logger.info("intellisense popup should have scrollbar when there are many values");
        Assert.assertTrue(attrRulesTable.getIntellisensePopup().hasVerticalScrollbar());

        logger.info("intellisense popup should display only matching values");
        String inputValue = "sname"; // matches: wsNamespace, servletClassname
        attrRulesTable.getInputField().sendKeys(inputValue.toLowerCase());
        List<String> options = attrRulesTable.getIntellisensePopupOptions();
        Assert.assertTrue(initialCount > options.size());
        Assert.assertTrue(options.size() > 1);
        for (String option : options) {
            Assert.assertTrue(option.toLowerCase().contains(inputValue)); // case is ignored, therefore toLowerCase
        }

        logger.info("intellisense popup should NOT have scrollbar when the number of values is low");
        Assert.assertFalse(attrRulesTable.getIntellisensePopup().hasVerticalScrollbar());

        logger.info("intellisense popup should hide if no match found");
        attrRulesTable.getInputField().sendKeys("8s89df78ghd");
        Assert.assertEquals(attrRulesTable.getIntellisensePopupOptions().size(), 0);
        Assert.assertFalse(attrRulesTable.getIntellisensePopup().isDisplayed());
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testIntellisenseWithFriendlyName() throws Exception {
        initialize();
        
        AttributeRulesTable attrRulesTable = ui.getAttributeRulesTable();

        logger.info("intellisense popup should open");
        AttributeRulesTableRow lastRow = attrRulesTable.getLastRow();
        lastRow.getExistingAttributeNameCell().click();
        attrRulesTable.waitForIntellisensePopup();
        int initialCount = attrRulesTable.getIntellisensePopupOptions().size();
        Assert.assertTrue(initialCount > 0);

        logger.info("intellisense popup should display matching values");
        String inputValue = "servlet";
        attrRulesTable.getInputField().sendKeys(inputValue);
        List<String> options = attrRulesTable.getIntellisensePopupOptions();
        Assert.assertTrue(initialCount > options.size());
        Assert.assertTrue(options.size() > 1);
        for (String option : options) {
            Assert.assertTrue(option.contains(inputValue));
        }
        
        options.contains("servletClassname");
        options.contains("servletMethod");

        logger.info("intellisense popup should hide if no match found - e.g. when entering system name of the attribute Business Transaction");
        attrRulesTable.getInputField().clear();
        attrRulesTable.getInputField().sendKeys("transactionId");
        Assert.assertEquals(attrRulesTable.getIntellisensePopupOptions().size(), 0);
        Assert.assertFalse(attrRulesTable.getIntellisensePopup().isDisplayed());
        
        logger.info("intellisense popup should hide if no match found - e.g. when entering system name of the attribute Business Service");
        attrRulesTable.getInputField().clear();
        attrRulesTable.getInputField().sendKeys("serviceId");
        Assert.assertEquals(attrRulesTable.getIntellisensePopupOptions().size(), 0);
        Assert.assertFalse(attrRulesTable.getIntellisensePopup().isDisplayed());
        
        logger.info("intellisense popup should hide if no match found - e.g. when entering system name of the attribute Application");
        attrRulesTable.getInputField().clear();
        attrRulesTable.getInputField().sendKeys("applicationName");
        Assert.assertEquals(attrRulesTable.getIntellisensePopupOptions().size(), 0);
        Assert.assertFalse(attrRulesTable.getIntellisensePopup().isDisplayed());
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testSorting() throws Exception {
        initialize();

        AttributeRulesTable attrRulesTable = ui.getAttributeRulesTable();
        attrRulesTable.deleteAll();

        logger.info("creating attribute rules");
        attrRulesTable.createRow("C", NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);
        attrRulesTable.createRow("A", NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);
        attrRulesTable.createRow("B", NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);

        logger.info("checking the rules are in the order they were added");
        checkThatSortingIsOff(attrRulesTable);
        List<AttributeRulesTableRow> rows = attrRulesTable.getRows();
        Assert.assertEquals(rows.get(0).getNewAttributeNameCell().getText(), "C");
        Assert.assertEquals(rows.get(1).getNewAttributeNameCell().getText(), "A");
        Assert.assertEquals(rows.get(2).getNewAttributeNameCell().getText(), "B");

        logger.info("testing that the attribute rules can be sorted again (asc)");
        attrRulesTable.sortByNewAttributeName(true);
        rows = attrRulesTable.getRows();
        Assert.assertEquals(rows.get(0).getNewAttributeNameCell().getText(), "A");
        Assert.assertEquals(rows.get(1).getNewAttributeNameCell().getText(), "B");
        Assert.assertEquals(rows.get(2).getNewAttributeNameCell().getText(), "C");

        logger.info("testing that the attribute rules can be sorted again (desc)");
        attrRulesTable.sortByNewAttributeName(false);
        rows = attrRulesTable.getRows();
        Assert.assertEquals(rows.get(0).getNewAttributeNameCell().getText(), "C");
        Assert.assertEquals(rows.get(1).getNewAttributeNameCell().getText(), "B");
        Assert.assertEquals(rows.get(2).getNewAttributeNameCell().getText(), "A");

        logger.info("editing a rule");
        rows.get(1).editNewAttributeNameCell("Z");

        logger.info("checking the table was not re-sorted");
        checkThatSortingIsOff(attrRulesTable);
        rows = attrRulesTable.getRows();
        Assert.assertEquals(rows.get(0).getNewAttributeNameCell().getText(), "C");
        Assert.assertEquals(rows.get(1).getNewAttributeNameCell().getText(), "Z");
        Assert.assertEquals(rows.get(2).getNewAttributeNameCell().getText(), "A");

        logger.info("testing that the attribute rules can be sorted again (asc)");
        attrRulesTable.sortByNewAttributeName(true);
        rows = attrRulesTable.getRows();
        Assert.assertEquals(rows.get(0).getNewAttributeNameCell().getText(), "A");
        Assert.assertEquals(rows.get(1).getNewAttributeNameCell().getText(), "C");
        Assert.assertEquals(rows.get(2).getNewAttributeNameCell().getText(), "Z");

        logger.info("testing that the attribute rules can be sorted again (desc)");
        attrRulesTable.sortByNewAttributeName(false);
        rows = attrRulesTable.getRows();
        Assert.assertEquals(rows.get(0).getNewAttributeNameCell().getText(), "Z");
        Assert.assertEquals(rows.get(1).getNewAttributeNameCell().getText(), "C");
        Assert.assertEquals(rows.get(2).getNewAttributeNameCell().getText(), "A");

        attrRulesTable.deleteAll();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testCopyToUniverse() throws Exception {
        final String universe1 = "test universe 1";
        final String universe2 = "test universe 2";
        final String universe3 = "test universe 3";
        
        initialize();
        
        ui.getLeftNavigationPanel().goToUniverses();

        logger.info("delete the universes if they happen to exist from a previous test run");
        UniverseSettings universeSettings = ui.getUniverseSettings();
        if (universeSettings.isUniversePresent(universe1)) {
            universeSettings.deleteUniverse(universe1);
        }
        if (universeSettings.isUniversePresent(universe2)) {
            universeSettings.deleteUniverse(universe2);
        }
        if (universeSettings.isUniversePresent(universe3)) {
            universeSettings.deleteUniverse(universe3);
        }
        
        logger.info("create the test universes");
        universeSettings.createUniverse(universe1);
        universeSettings.createUniverse(universe2);
        universeSettings.createUniverse(universe3);

        logger.info("make sure the default universe is active");
        ui.getTopNavigationPanel().selectUniverse(Universe.DEFAULT_UNIVERSE);
        
        ui.getLeftNavigationPanel().goToDecorationPolicies();
        
        AttributeRulesTable attrRulesTable = ui.getAttributeRulesTable();
        
        logger.info("delete attribute rules that may happen to exist from previous runs");
        attrRulesTable.deleteAll();

        logger.info("create attribute rules for this test case");
        attrRulesTable.createRow("global_1", NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);
        attrRulesTable.createRow("global_2", NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);
        attrRulesTable.createRow("global_3", NEW_ATTRIBUTE_VALUE, EXISTING_ATTRIBUTE_NAME, OPERATOR, CONDITION_VALUE);
        Assert.assertEquals(attrRulesTable.getRows().size(), 4); // the 4th row is the row for adding a new rule

        logger.info("it should not be possible to select 'new row'");
        boolean isException = false;
        try {
            attrRulesTable.getLastRow().select();
        } catch (NoSuchElementException e) {
            isException = true;
        }
        Assert.assertTrue(isException);

        logger.info("select 2 rules for copying");
        AttributeRulesTableRow global1 = attrRulesTable.getRowByNewAttrName("global_1");
        Assert.assertNotNull(global1);
        global1.select();
        AttributeRulesTableRow global3 = attrRulesTable.getRowByNewAttrName("global_3");
        Assert.assertNotNull(global3);
        global3.select();

        logger.info("it should not be possible to copy from the current universe to the current universe");
        isException = false;
        try {
            attrRulesTable.copySelectedToUniverses(Arrays.asList(Universe.DEFAULT_UNIVERSE));
        } catch (NoSuchElementException e) {
            isException = true;
        }
        Assert.assertTrue(isException);
        ui.getModalDialog().clickButton(DialogButton.CLOSE);

        logger.info("copy 2 rules to 2 universes");
        attrRulesTable.copySelectedToUniverses(Arrays.asList(universe1, universe2));
        attrRulesTable.deleteAll();

        attrRulesTable.selectUniverse(universe1);
        Assert.assertEquals(attrRulesTable.getRows().size(), 3);
        global1 = attrRulesTable.getRowByNewAttrName("global_1");
        Assert.assertNotNull(global1);
        global1.duplicate();
                
        global3 = attrRulesTable.getRowByNewAttrName("global_3");
        Assert.assertNotNull(global3);
        global3.duplicate();

        attrRulesTable.selectUniverse(universe3);
        Assert.assertEquals(attrRulesTable.getRows().size(), 1);

        attrRulesTable.selectUniverse(universe2);
        Assert.assertEquals(attrRulesTable.getRows().size(), 3);

        logger.info("copy from an ordinaty universe to the default universe i.e. ENTERPRISE");
        attrRulesTable.selectAll();
        attrRulesTable.copySelectedToUniverses(Arrays.asList(Universe.DEFAULT_UNIVERSE));
        attrRulesTable.selectUniverse(Universe.DEFAULT_UNIVERSE);
        Assert.assertEquals(attrRulesTable.getRows().size(), 3);

        logger.info("clean up");
        attrRulesTable.deleteAll();
        
        ui.getLeftNavigationPanel().goToUniverses();
        universeSettings.deleteUniverse(universe1);
        universeSettings.deleteUniverse(universe2);
        universeSettings.deleteUniverse(universe3);
        
        ui.logout();
    }

    private void checkThatSortingIsOff(AttributeRulesTable attrRulesTable) {
        ui.waitWhileVisible(attrRulesTable.getNewAttributeNameSortArrowLocator(true), 2);
        ui.waitWhileVisible(attrRulesTable.getNewAttributeNameSortArrowLocator(false), 2);
    }
}
