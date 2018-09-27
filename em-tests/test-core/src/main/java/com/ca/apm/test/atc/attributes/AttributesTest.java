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

import static com.ca.apm.test.atc.common.Utils.assertContains;

import java.util.List;
import java.util.Random;

import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.CommonMapViewUITest;
import com.ca.apm.test.atc.common.AttributeRulesTable.Operator;
import com.ca.apm.test.atc.common.Canvas;
import com.ca.apm.test.atc.common.DetailsPanel;
import com.ca.apm.test.atc.common.DetailsPanel.AttributeType;
import com.ca.apm.test.atc.common.LeftNavigationPanel;
import com.ca.apm.test.atc.common.ModalDialog;
import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.TopNavigationPanel;
import com.ca.apm.test.atc.common.Universe;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.AttributeRow;
import com.ca.apm.test.atc.common.element.AttributeRulesTableRow;
import com.ca.apm.testbed.atc.TeamCenterRegressionTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class AttributesTest extends CommonMapViewUITest {

    private static final Logger logger = LoggerFactory.getLogger(AttributesTest.class);
    
    public static final String EMPTY_ATTR_VALUE_PLACEHOLDER = "<empty>";
    
    private static final String BASIC_ATTRIBUTE_NAME = "Name";
    private static final String IDENT_ATTRIBUTE_NODES = "Nodes";
        
    private static final String GROUP_1_COMMON_BASIC_ATTR_NAME = "agentDomain";
    private static final String GROUP_1_COMMON_BASIC_ATTR_VALUE = "SuperDomain/TomcatDomain";

    private static final String NEW_ATTRIBUTE_NAME_MULTISELECT = "e2e_multiselect_test";
    private static final String NEW_ATTRIBUTE_NAME_RULE = "e2e_rule_test";
    private static final String NEW_ATTRIBUTE_NAME_CRUD = "e2e_CRUD_test";
    private static final String NEW_ATTRIBUTE_NAME_LIVEMODE = "e2e_liveMode_test";
    private static final String NEW_ATTRIBUTE_NAME_INTELLIGENCE = "location";
    private static final String NEW_ATTRIBUTE_NAME_DIFF = "e2e_diff";

    private static final String NEW_ATTRIBUTE_VALUE = "value";
    private static final String NEW_ATTRIBUTE_VALUE_2 = "value_2";
    
    private static final String TEST_UNIVERSE_NAME = "u4AttrRuleCreation";
    private static final String TEST_UNIVERSE_RULE_NAME = "e2e_rule_test_u";

    private LeftNavigationPanel leftNav;
    private TopNavigationPanel topNav;
    
    private String[] nodesToDeleteE2eAttributes = {PERSP1_GROUP1_NAME, PERSP1_GROUP2_NAME, PERSP1_GROUP3_NAME};

    @Override
    protected void init() throws Exception {
        ui = getUI();
        
        leftNav = ui.getLeftNavigationPanel();
        topNav = ui.getTopNavigationPanel();

        logger.info("Log in");
        ui.login();

        logger.info("Switch to Map view");
        leftNav.goToMapViewPage();
        
        logger.info("Select default universe");
        topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);

        super.init();
        
        logger.info("Select the desired perspective");
        if (!ui.getPerspectivesControl().isPerspectiveActive(PERSP1)) {
            logger.info("Switching perspective to '{}'", PERSP1);
            ui.getPerspectivesControl().selectPerspectiveByName(PERSP1);
        }
        
        Assert.assertTrue(ui.getPerspectivesControl().isPerspectiveActive(PERSP1));
    }
   
    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test(groups = "failing")
    public void testAttributesMultiselect() throws Exception {
        init();

        logger.info("Enable live mode");
        ui.getTimeline().turnOnLiveMode();

        Canvas canvas = ui.getCanvas();
        
        logger.info("Delete the attribute '{}' if it already exists", NEW_ATTRIBUTE_NAME_MULTISELECT);
        canvas.deleteCustomAttributeFromNodesIfItExists(nodesToDeleteE2eAttributes, NEW_ATTRIBUTE_NAME_MULTISELECT);
        
        logger.info("Select the group node '{}'", PERSP1_GROUP2_NAME);
        
        canvas.expandGroup(canvas.getNodeByNameSubstring(PERSP1_GROUP2_NAME));
        canvas.getCtrl().fitAllToView();
        canvas.selectNodeByName(PERSP1_GROUP3_NAME);

        ui.getDriver().getKeyboard().pressKey(Keys.CONTROL);
        canvas.selectNodeByName(PERSP1_GROUP2_TEST_SUBNODE_NAME);
        
        DetailsPanel details = ui.getDetailsPanel();
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        ui.getDriver().getKeyboard().releaseKey(Keys.CONTROL);
        Assert.assertNotNull(details.getAttributesPanel(AttributeType.BASIC_ATTRIBUTES));
        Assert.assertNotNull(details.getAttributesPanel(AttributeType.OTHER_ATTRIBUTES));

        logger.info("Add the new attribute '{}' with the value '{}'", NEW_ATTRIBUTE_NAME_MULTISELECT, NEW_ATTRIBUTE_VALUE);
        details.addNewAttribute(NEW_ATTRIBUTE_NAME_MULTISELECT, NEW_ATTRIBUTE_VALUE, null);

        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
        
        Assert.assertNotNull(details.getAttributeRowByNameAndValueAsync(AttributeType.OTHER_ATTRIBUTES,
            NEW_ATTRIBUTE_NAME_MULTISELECT, NEW_ATTRIBUTE_VALUE));

        logger.info("Checking the nodes have the new attribute set");
        canvas.expandGroup(canvas.getNodeByNameSubstring(PERSP1_GROUP3_NAME));
        canvas.selectNodeByName(PERSP1_GROUP3_TEST_SUBNODE_NAME);
        
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.OTHER_ATTRIBUTES,
            NEW_ATTRIBUTE_NAME_MULTISELECT, NEW_ATTRIBUTE_VALUE, true, true));

        canvas.selectNodeByName(PERSP1_GROUP2_TEST_SUBNODE_NAME);        
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.OTHER_ATTRIBUTES,
            NEW_ATTRIBUTE_NAME_MULTISELECT, NEW_ATTRIBUTE_VALUE, true, true));

        logger.info("multi-select three nodes");
        canvas.selectNodeByName(PERSP1_GROUP3_TEST_SUBNODE_NAME);
        ui.getDriver().getKeyboard().pressKey(Keys.CONTROL);
        canvas.selectNodeByName(PERSP1_GROUP2_TEST_SUBNODE_NAME);

        // this node should not have the attribute assigned
        canvas.selectNodeByName(PERSP1_GROUP2_TEST_SUBNODE2_NAME);        
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        ui.getDriver().getKeyboard().releaseKey(Keys.CONTROL); 

        logger.info("expand the attribute");
        details
            .getAttributeRowByNameAndValue(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_MULTISELECT, "")
            .getExpandIcon().click();

        List<String> attributeValues =
            details.getAttributeEndTimeValues(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_MULTISELECT);

        assertContains(attributeValues, "");
        assertContains(attributeValues, DetailsPanel.EMPTY_ATTR_STRING + " (1)");
        assertContains(attributeValues, NEW_ATTRIBUTE_VALUE + " (2)");

        logger.info("Add a value to the node that does not have the attribute yet");
        details
            .getAttributeRowByNameAndValue(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_MULTISELECT,
                DetailsPanel.EMPTY_ATTR_STRING + " (1)").getEndTimeValueCell().click();
        details.getAttributeInputField(AttributeType.OTHER_ATTRIBUTES).sendKeys(
            NEW_ATTRIBUTE_VALUE_2);

        logger.info("remove the attribute from one node");
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        canvas.selectNodeByName(PERSP1_GROUP3_TEST_SUBNODE_NAME);
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
        details.deleteAttribute(NEW_ATTRIBUTE_NAME_MULTISELECT, NEW_ATTRIBUTE_VALUE);
        
        List<AttributeRow> rows = details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_MULTISELECT); 
        Assert.assertEquals(rows.size(), 1);
        Assert.assertEquals(rows.get(0).getEndTimeValueCell().getText(), DetailsPanel.EMPTY_ATTR_STRING);

        logger.info("collapse group 1, checking attribute values and deleting them");
        canvas.collapseGroup(canvas.getNodeByNameSubstring(PERSP1_GROUP3_NAME));
        canvas.selectNodeByName(PERSP1_GROUP3_NAME);
        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
        
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        
        details
            .getAttributeRowByNameAndValue(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_MULTISELECT, "")
            .getExpandIcon().click();
        attributeValues =
            details.getAttributeEndTimeValues(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_MULTISELECT);

        assertContains(attributeValues, "");
        assertContains(attributeValues, DetailsPanel.EMPTY_ATTR_STRING + " (1)");

        int numberOfComponents = Integer.parseInt(details.getAttributeEndTimeValues(AttributeType.IDENT_ATTRIBUTES, IDENT_ATTRIBUTE_NODES).get(0));
        String attrValueCellContent = NEW_ATTRIBUTE_VALUE + " (" + (numberOfComponents - 1) + ")";
        assertContains(attributeValues, attrValueCellContent);
        
        details.deleteAttribute(NEW_ATTRIBUTE_NAME_MULTISELECT, attrValueCellContent);
        rows = details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_MULTISELECT);
        Assert.assertEquals(rows.size(), 1);
        Assert.assertEquals(rows.get(0).getEndTimeValueCell().getText(), DetailsPanel.EMPTY_ATTR_STRING);
        
        logger.info("multi-select nodes outside of group 1, which still have the attribute set");
        canvas.selectNodeByName(PERSP1_GROUP2_TEST_SUBNODE_NAME);
        ui.getDriver().getKeyboard().pressKey(Keys.CONTROL);
        canvas.selectNodeByName(PERSP1_GROUP2_TEST_SUBNODE2_NAME);
        
        
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        ui.getDriver().getKeyboard().releaseKey(Keys.CONTROL);
        
        logger.info("Check the attribute values and remove them");
        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);

        details
            .getAttributeRowByNameAndValue(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_MULTISELECT, "")
            .getExpandIcon().click();
        attributeValues =
            details.getAttributeEndTimeValues(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_MULTISELECT);

        assertContains(attributeValues, "");
        assertContains(attributeValues, NEW_ATTRIBUTE_VALUE + " (1)");
        assertContains(attributeValues, NEW_ATTRIBUTE_VALUE_2 + " (1)");

        details.deleteAttribute(NEW_ATTRIBUTE_NAME_MULTISELECT, "");
        rows = details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_MULTISELECT); 
        Assert.assertEquals(rows.size(), 1);
        Assert.assertEquals(rows.get(0).getEndTimeValueCell().getText(), EMPTY_ATTR_VALUE_PLACEHOLDER);

        logger.info("Clean up");
        ui.logout();
        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test(groups = "failing")
    public void testAttributeRuleCreation() throws Exception {
        init();

        logger.info("Make sure the universe does not exist aready");
        List<String> universeNames = topNav.getUniverseNames();

        logger.info("existing universes: {}", universeNames);
        
        if (universeNames.contains(TEST_UNIVERSE_NAME)) {
            logger.info("the universe '{}' already exists, it has to be deleted now", TEST_UNIVERSE_NAME);
            
            leftNav.goToUniverses();
            ui.getUniverseSettings().deleteUniverse(TEST_UNIVERSE_NAME);
        }
        
        logger.info("Make sure the attribute rule does not exist already");
        leftNav.goToDecorationPolicies();
        ui.getAttributeRulesTable().removeRowsIfExist(NEW_ATTRIBUTE_NAME_RULE, NEW_ATTRIBUTE_VALUE, null, null, null);

        logger.info("create universe");
        leftNav.goToUniverses();
        ui.getUniverseSettings().createUniverse(TEST_UNIVERSE_NAME);
        
        logger.info("Switch to Map view");
        leftNav.goToMapViewPage();
        
        Assert.assertEquals(topNav.getActiveUniverse(), TEST_UNIVERSE_NAME);

        logger.info("Select a group node");
        Canvas canvas = ui.getCanvas();
        canvas.getNodeByNameSubstring(PERSP1_GROUP2_NAME).click();

        DetailsPanel details = ui.getDetailsPanel();
        Assert.assertNotNull(details.getAttributesPanel(AttributeType.BASIC_ATTRIBUTES));
        Assert.assertNotNull(details.getAttributesPanel(AttributeType.OTHER_ATTRIBUTES));

        logger.info("Add a new attribute as attribute rule for Universe {}", TEST_UNIVERSE_NAME);
        topNav.selectUniverse(TEST_UNIVERSE_NAME);
        details.addNewAttribute(TEST_UNIVERSE_RULE_NAME, NEW_ATTRIBUTE_VALUE_2, true);
        // wait for decoration
        Utils.sleep(5000);
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.OTHER_ATTRIBUTES,
            TEST_UNIVERSE_RULE_NAME, NEW_ATTRIBUTE_VALUE_2, false, false));
        
        logger.info("verify rule is visible for universe " + Universe.DEFAULT_UNIVERSE);
        topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);
        canvas.waitForUpdate();
        canvas.getCtrl().fitAllToView();
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.OTHER_ATTRIBUTES,
            TEST_UNIVERSE_RULE_NAME, NEW_ATTRIBUTE_VALUE_2, false, false));

        logger.info("Add a new attribute as attribute rule (Global)");
        details.addNewAttribute(NEW_ATTRIBUTE_NAME_RULE, NEW_ATTRIBUTE_VALUE, true);        
        // wait for decoration
        Utils.sleep(5000);
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.OTHER_ATTRIBUTES,
            NEW_ATTRIBUTE_NAME_RULE, NEW_ATTRIBUTE_VALUE, false, true));

        logger.info("expand the group");
        canvas.expandGroup(canvas.getNodeByNameSubstring(PERSP1_GROUP2_NAME));
        canvas.getCtrl().fitAllToView();

        logger.info("Check that a subnode has the attribute");
        canvas.getNodeByName(PERSP1_GROUP2_TEST_SUBNODE_NAME).click();
        
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.OTHER_ATTRIBUTES,
            NEW_ATTRIBUTE_NAME_RULE, NEW_ATTRIBUTE_VALUE, false, false));

        logger.info("Switch to attribute rules");
        leftNav.goToDecorationPolicies();

        logger.info("search for rules and delete them");
        ui.getAttributeRulesTable().removeRowsIfExist(NEW_ATTRIBUTE_NAME_RULE, NEW_ATTRIBUTE_VALUE,
                PERSP1.toLowerCase(), Operator.EQUALS, PERSP1_GROUP2_NAME);
        
        ui.getAttributeRulesTable().selectUniverse(TEST_UNIVERSE_NAME);
        ui.getAttributeRulesTable().removeRowsIfExist(TEST_UNIVERSE_RULE_NAME, NEW_ATTRIBUTE_VALUE_2,
            PERSP1.toLowerCase(), Operator.EQUALS, PERSP1_GROUP2_NAME);

        logger.info("Clean up");
        ui.getLeftNavigationPanel().goToUniverses();
        ui.getUniverseSettings().deleteUniverse(TEST_UNIVERSE_NAME);
        ui.logout();
        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test(groups = "failing")
    public void testAttributeCRUDOperations() throws Exception {
        init();

        logger.info("Enabling live mode");
        ui.getTimeline().turnOnLiveMode();

        Canvas canvas = ui.getCanvas();
        
        logger.info("Make sure the attribute does not already exist");
        canvas.deleteCustomAttributeFromNodesIfItExists(nodesToDeleteE2eAttributes, NEW_ATTRIBUTE_NAME_CRUD);

        logger.info("Select a group node");
        canvas.selectNodeByName(PERSP1_GROUP2_NAME);
        
        logger.info("expand the name attribute");
        DetailsPanel details = ui.getDetailsPanel();
        details.waitUntilAttributeTableDataLoaded(AttributeType.BASIC_ATTRIBUTES);
        List<AttributeRow> nameAttrRows = details.getAttributeRowsByName(AttributeType.BASIC_ATTRIBUTES, BASIC_ATTRIBUTE_NAME);
        Assert.assertEquals(nameAttrRows.size(), 1);

        nameAttrRows.get(0).getExpandIcon().click();
        nameAttrRows = details.getAttributeRowsByName(AttributeType.BASIC_ATTRIBUTES, BASIC_ATTRIBUTE_NAME);
        Assert.assertTrue(nameAttrRows.size() > 1);

        logger.info("Add a new attribute");
        details.addNewAttribute(NEW_ATTRIBUTE_NAME_CRUD, NEW_ATTRIBUTE_VALUE, false);

        Utils.sleep(5000);
        
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.OTHER_ATTRIBUTES,
            NEW_ATTRIBUTE_NAME_CRUD, NEW_ATTRIBUTE_VALUE, true, true));

        logger.info("expand the group");
        canvas.expandGroup(canvas.getNodeByNameSubstring(PERSP1_GROUP2_NAME));
        canvas.getCtrl().fitAllToView();

        logger.info("edit the attribute value in one of the group's subnodes");
        canvas.selectNodeByName(PERSP1_GROUP2_TEST_SUBNODE_NAME);

        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        AttributeRow attrRow =
            details.getAttributeRowByNameAndValue(AttributeType.OTHER_ATTRIBUTES,
                NEW_ATTRIBUTE_NAME_CRUD, NEW_ATTRIBUTE_VALUE);
        Assert.assertNotNull(attrRow);

        attrRow.getEndTimeValueCell().click();
        details.getAttributeInputField(AttributeType.OTHER_ATTRIBUTES).sendKeys(NEW_ATTRIBUTE_VALUE_2);
        attrRow.getNameCell().click();
        Utils.sleep(250);
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.OTHER_ATTRIBUTES,
            NEW_ATTRIBUTE_NAME_CRUD, NEW_ATTRIBUTE_VALUE_2, true, false));

        logger.info("remove the attribute from some of the group's subnodes");
        canvas.getCtrl().fitAllToView();
        canvas.selectNodeByName(PERSP1_GROUP2_TEST_SUBNODE2_NAME);
        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        attrRow =
            details.getAttributeRowByNameAndValue(AttributeType.OTHER_ATTRIBUTES,
                NEW_ATTRIBUTE_NAME_CRUD, NEW_ATTRIBUTE_VALUE);
        Assert.assertNotNull(attrRow);
        
        canvas.selectNodeByName(PERSP1_GROUP2_TEST_SUBNODE2_NAME);
        attrRow.getDeleteIcon().click();
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        
        Utils.sleep(2000);
        
        List<AttributeRow> rows = details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_CRUD); 
        Assert.assertEquals(rows.size(), 1);
        Assert.assertEquals(rows.get(0).getEndTimeValueCell().getText(), EMPTY_ATTR_VALUE_PLACEHOLDER);
                
        canvas.getCtrl().fitAllToView();
        logger.info("collapse the group and check the attribute");
        canvas.collapseGroup(canvas.getNodeByNameSubstring(PERSP1_GROUP2_NAME));
        Utils.sleep(2000); // wait for collapse animation
        canvas.selectNodeByName(PERSP1_GROUP2_NAME);
        Utils.sleep(2000);
        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        attrRow =
            details.getAttributeRowByNameAndValue(AttributeType.OTHER_ATTRIBUTES,
                NEW_ATTRIBUTE_NAME_CRUD, "");

        attrRow.getExpandIcon().click();

        List<String> attributeValues =
            details
                .getAttributeEndTimeValues(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_CRUD);

        assertContains(attributeValues, "");
        assertContains(attributeValues, DetailsPanel.EMPTY_ATTR_STRING + " (1)");
        assertContains(attributeValues, NEW_ATTRIBUTE_VALUE_2 + " (1)");
        int numberOfComponents = Integer.parseInt(details.getAttributeEndTimeValues(AttributeType.IDENT_ATTRIBUTES, IDENT_ATTRIBUTE_NODES).get(0));
        assertContains(attributeValues, NEW_ATTRIBUTE_VALUE + " (" + (numberOfComponents - 2) + ")");

        logger.info("delete the attribute");
        details.deleteAttribute(NEW_ATTRIBUTE_NAME_CRUD, "");
        rows = details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_CRUD); 
        Assert.assertEquals(rows.size(), 1);
        Assert.assertEquals(rows.get(0).getEndTimeValueCell().getText(), DetailsPanel.EMPTY_ATTR_STRING);

        canvas.expandGroup(canvas.getNodeByNameSubstring(PERSP1_GROUP2_NAME));

        canvas.selectNodeByName(PERSP1_GROUP2_TEST_SUBNODE_NAME);
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        Assert.assertTrue(details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES,
            NEW_ATTRIBUTE_NAME_CRUD).isEmpty());

        canvas.getNodeByName(PERSP1_GROUP2_TEST_SUBNODE2_NAME).click();
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        Assert.assertTrue(details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES,
            NEW_ATTRIBUTE_NAME_CRUD).isEmpty());

        logger.info("Clean up");
        ui.logout();
        ui.cleanup();
    }

    /** Test that the attribute name and value that are just being edited neither disappear nor are corrupted 
     * during live mode auto-refresh events */
    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testAttributeEditingInLiveMode() throws Exception {
        init();

        logger.info("Enable live mode");
        ui.getTimeline().turnOnLiveMode();
        ui.getTimeline().expand();

        Canvas canvas = ui.getCanvas();
        
        logger.info("Delete the attribute '{}' if it already exists", NEW_ATTRIBUTE_NAME_LIVEMODE);
        canvas.deleteCustomAttributeFromNodesIfItExists(nodesToDeleteE2eAttributes, NEW_ATTRIBUTE_NAME_LIVEMODE);

        logger.info("Select the group node '{}'", PERSP1_GROUP1_NAME);
        canvas.getNodeByNameSubstring(PERSP1_GROUP1_NAME).click();

        logger.info("Type the new attribute name '{}' and wait for auto refresh", NEW_ATTRIBUTE_NAME_LIVEMODE);
        DetailsPanel details = ui.getDetailsPanel();
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);

        AttributeRow lastRow = details.getLastAttributeRow(AttributeType.OTHER_ATTRIBUTES);
        lastRow.getNameCell().click();
        details.getAttributeInputField(AttributeType.OTHER_ATTRIBUTES).sendKeys(NEW_ATTRIBUTE_NAME_LIVEMODE);
        
        ui.waitForAutorefresh();

        logger.info("Type the new attribute value '{}' and wait for auto-refresh", NEW_ATTRIBUTE_VALUE);
        lastRow.getEndTimeValueCell().click();
        details.getAttributeInputField(AttributeType.OTHER_ATTRIBUTES).sendKeys(NEW_ATTRIBUTE_VALUE);
        
        ui.waitForAutorefresh();
        ui.waitForWorkIndicator();

        ModalDialog dialog = ui.getModalDialog();
        dialog.getRadioOption(0).click();
        dialog.clickButton(DialogButton.CONTINUE);
        
        details.getLastAttributeRow(AttributeType.OTHER_ATTRIBUTES).getNameCell().click();

        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.OTHER_ATTRIBUTES,
            NEW_ATTRIBUTE_NAME_LIVEMODE, NEW_ATTRIBUTE_VALUE, true, true));

        logger.info("Delete the attribute '{}'", NEW_ATTRIBUTE_NAME_LIVEMODE);
        
        details.deleteAttribute(NEW_ATTRIBUTE_NAME_LIVEMODE, NEW_ATTRIBUTE_VALUE);
        List<AttributeRow> rows = details.getAttributeRowsByName(
                AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_LIVEMODE);
        Assert.assertEquals(rows.size(), 1);
        Assert.assertEquals(rows.get(0).getEndTimeValueCell().getText(), EMPTY_ATTR_VALUE_PLACEHOLDER);
        
        logger.info("Clean up");
        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testAttributesVisualization() throws Exception {
        init();

        logger.info("Enabling live mode");
        ui.getTimeline().turnOnLiveMode();

        logger.info("Selecting the group node '{}'", PERSP1_GROUP1_NAME);
        Canvas canvas = ui.getCanvas();
        canvas.expandGroup(canvas.getNodeByNameSubstring(PERSP1_GROUP1_NAME));
        canvas.getCtrl().fitAllToView();
        
        logger.info("Selecting the subnode '{}'", PERSP1_GROUP1_TEST_SUBNODE_NAME);
        canvas.getNodeByName(PERSP1_GROUP1_TEST_SUBNODE_NAME).click();
                
        logger.info("Checking that the basic attributes are not editable");
        DetailsPanel details = ui.getDetailsPanel();
        details.waitUntilAttributeTableDataLoaded(AttributeType.BASIC_ATTRIBUTES);
        details.scrollToAttributesTable(AttributeType.BASIC_ATTRIBUTES);
        Assert.assertFalse(details.getAttributeRows(AttributeType.BASIC_ATTRIBUTES).isEmpty());
        
        Random rn = new Random();
        int index = rn.nextInt(details.getAttributeRows(AttributeType.BASIC_ATTRIBUTES).size());
        details.getAttributeRows(AttributeType.BASIC_ATTRIBUTES).get(index).getNameCell().click();
        Assert.assertFalse(details.getAttributeInputField(AttributeType.BASIC_ATTRIBUTES).isDisplayed());
        details.getAttributeRows(AttributeType.BASIC_ATTRIBUTES).get(index).getEndTimeValueCell().click();
        Assert.assertFalse(details.getAttributeInputField(AttributeType.BASIC_ATTRIBUTES).isDisplayed());
        Assert.assertFalse(details.getAttributeRows(AttributeType.BASIC_ATTRIBUTES).get(index).getDeleteIcon().isDisplayed());
        
        logger.info("Verify that the attribute '{}' exists as has the value '{}'", GROUP_1_COMMON_BASIC_ATTR_NAME, GROUP_1_COMMON_BASIC_ATTR_VALUE);
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.BASIC_ATTRIBUTES,
            GROUP_1_COMMON_BASIC_ATTR_NAME, GROUP_1_COMMON_BASIC_ATTR_VALUE, false, false));

        logger.info("Checking the decorated and custom attributes' names are not editable");
        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
        Assert.assertFalse(details.getAttributeRows(AttributeType.OTHER_ATTRIBUTES).isEmpty());
        
        index = rn.nextInt(details.getAttributeRows(AttributeType.OTHER_ATTRIBUTES).size() - 1);
        details.getAttributeRows(AttributeType.OTHER_ATTRIBUTES).get(index).getNameCell().click();
        Assert.assertFalse(details.getAttributeInputField(AttributeType.OTHER_ATTRIBUTES).isDisplayed());

        logger.info("Clean up");
        ui.cleanup();
    }
    
    private enum EarlyAttributeIntelligencePivot {
        HOSTNAME("Hostname", Operator.EQUALS, 1), AGENT("agent", Operator.CONTAINS, 2);
        private final String attributeName;
        private final Operator attributeRuleOperator;
        private final int radioOptionIndex;
        
        EarlyAttributeIntelligencePivot(String attributeName, Operator attributeRuleOperator,
            int radioOptionIndex) {
            this.attributeName = attributeName;
            this.attributeRuleOperator = attributeRuleOperator;
            this.radioOptionIndex = radioOptionIndex;
        }

        public String getAttributeName() {
            return attributeName;
        }
        
        public Operator getAttributeRuleOperator() {
            return attributeRuleOperator;
        }

        public int getRadioOptionIndex() {
            return radioOptionIndex;
        }
    }
    
    private void testEarlyAttributeIntelligence(EarlyAttributeIntelligencePivot pivot) throws Exception {
        init();

        logger.info("Selecting universe");
        topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);

        logger.info("Making sure the attribute rule does not already exist");
        leftNav.goToDecorationPolicies();
        ui.getAttributeRulesTable().removeRowsIfExist(NEW_ATTRIBUTE_NAME_INTELLIGENCE,
                NEW_ATTRIBUTE_VALUE, pivot.getAttributeName(), pivot.getAttributeRuleOperator(),
                null);

        logger.info("Switching to Map view");
        leftNav.goToMapViewPage();

        logger.info("Enabling live mode");
        ui.getTimeline().turnOnLiveMode();

        logger.info("Selecting the group node '{}'", PERSP1_GROUP1_NAME);
        Canvas canvas = ui.getCanvas();
        canvas.waitForUpdate();
        canvas.getCtrl().fitAllToView();
        
        canvas.expandGroup(canvas.getNodeByNameSubstring(PERSP1_GROUP1_NAME));
        canvas.getCtrl().fitAllToView();
        
        canvas.getNodeByName(PERSP1_GROUP1_TEST_SUBNODE_NAME).click();
        
        logger.info("Reading the attribute '{}'", pivot.getAttributeName());
        DetailsPanel details = ui.getDetailsPanel();
        final String pivotValue = details.getAttributeEndTimeValues(AttributeType.BASIC_ATTRIBUTES, pivot.getAttributeName()).get(0);
                
        logger.info("Entering value of the attribute '{}': '{}'", NEW_ATTRIBUTE_NAME_INTELLIGENCE, NEW_ATTRIBUTE_VALUE);
        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
        AttributeRow row = details.getAttributeRowByNameAndValue(AttributeType.OTHER_ATTRIBUTES, NEW_ATTRIBUTE_NAME_INTELLIGENCE, DetailsPanel.EMPTY_ATTR_STRING);
        row.getEndTimeValueCell().click();
        details.getAttributeInputField(AttributeType.OTHER_ATTRIBUTES).sendKeys(NEW_ATTRIBUTE_VALUE);
        row.getNameCell().click();
        
        ModalDialog dialog = ui.getModalDialog();
        dialog.getRadioOption(pivot.getRadioOptionIndex()).click();
        dialog.clickButton(DialogButton.CONTINUE);
        
        logger.info("Switching to attribute rules");
        leftNav.goToDecorationPolicies();
        
        logger.info("Check that attribute rule was really created");
        List<AttributeRulesTableRow> attrRules = ui.getAttributeRulesTable().getRowsByParams(
                NEW_ATTRIBUTE_NAME_INTELLIGENCE, NEW_ATTRIBUTE_VALUE,
                pivot.getAttributeName(), pivot.getAttributeRuleOperator(), pivotValue);
        Assert.assertEquals(attrRules.size(), 1);
        
        attrRules.get(0).delete();
        logger.info("Clean up");
        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testEarlyAttributeIntelligenceBasedOnAgentValue() throws Exception {
        testEarlyAttributeIntelligence(EarlyAttributeIntelligencePivot.AGENT);
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testEarlyAttributeIntelligenceBasedOnHostnameValue() throws Exception {
        testEarlyAttributeIntelligence(EarlyAttributeIntelligencePivot.HOSTNAME);
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testAttributeDiffInTwoPointsInTime() throws Exception {
        init();

        logger.info("Enable live mode");
        ui.getTimeline().turnOnLiveMode();

        logger.info("Select the group node '{}'", PERSP1_GROUP1_NAME);
        Canvas canvas = ui.getCanvas();
        canvas.expandGroup(canvas.getNodeByNameSubstring(PERSP1_GROUP1_NAME));
        canvas.getCtrl().fitAllToView();
        canvas.getNodeByName(PERSP1_GROUP1_TEST_SUBNODE_NAME).click();
        canvas.waitForUpdate();
        
        final String attributeName = NEW_ATTRIBUTE_NAME_DIFF + "_" + System.currentTimeMillis();
        logger.info("Add a new attribute '{}' with the value '{}'", attributeName, NEW_ATTRIBUTE_VALUE);
        DetailsPanel details = ui.getDetailsPanel();
        details.addNewAttribute(attributeName, NEW_ATTRIBUTE_VALUE, null);

        canvas.waitForUpdate();
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        
        Assert.assertNotNull(details.getAttributeRowByNameAndValue(AttributeType.OTHER_ATTRIBUTES,
            attributeName, NEW_ATTRIBUTE_VALUE));

        logger.info("Enable historic mode and attribute events");
        ui.getTimeline().expand();
        
        // Wait so that the previous value modification becomes an attribute event in history
        Utils.sleep(5000);
        
        ui.getTimeline().turnOffLiveMode();
        canvas.waitForUpdate();
        
        ui.getTimeline().checkAttributeChangeCheckbox();
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        
        logger.info("Check that the attribute '{}' has the right start time and end time values", attributeName);
        details.scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
        List<AttributeRow> rows = details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, attributeName);
        Assert.assertEquals(rows.size(), 1);
        AttributeRow row = rows.get(0);
        Assert.assertEquals(row.getStartTimeValueCell().getText().trim(), EMPTY_ATTR_VALUE_PLACEHOLDER);
        Assert.assertEquals(row.getEndTimeValueCell().getText(), NEW_ATTRIBUTE_VALUE);

        logger.info("Switch to live mode and delete the attribute '{}'", attributeName);
        ui.getTimeline().turnOnLiveMode();
        details.deleteAttribute(attributeName, NEW_ATTRIBUTE_VALUE);
        rows = details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, attributeName); 
        Assert.assertEquals(rows.size(), 1);
        Assert.assertEquals(rows.get(0).getEndTimeValueCell().getText(), EMPTY_ATTR_VALUE_PLACEHOLDER);
        
        canvas.clickToUnusedPlaceInCanvas();
        canvas.getNodeByName(PERSP1_GROUP1_TEST_SUBNODE_NAME).click();
        canvas.waitForUpdate();
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        
        Assert.assertTrue(details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES,
            attributeName).isEmpty());
    }
}
