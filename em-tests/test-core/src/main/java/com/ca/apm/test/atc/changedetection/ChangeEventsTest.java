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
package com.ca.apm.test.atc.changedetection;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.*;
import com.ca.apm.test.atc.common.DetailsPanel.AttributeType;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.element.AttributeRow;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static org.testng.Assert.*;

public class ChangeEventsTest extends UITest {

    private static final String DIR_ASCENDING = "asc";
    private static final String DIR_DESCENDING = "desc";

    private UI ui = null;
    private Timeline timeline = null;
    private DetailsPanel details = null;
    private Canvas canvas = null;

    private void init(boolean createAttributeChange) throws Exception {
        ui = getUI();
        timeline = ui.getTimeline();
        details = ui.getDetailsPanel();
        canvas = ui.getCanvas();

        ui.login(Role.ADMIN);
        ui.getLeftNavigationPanel().goToMapViewPage();
        timeline.expand();

        ui.getPerspectivesControl().selectPerspectiveByName("Type");
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive("Type"));

        // make sure there is an attribute change
        if (createAttributeChange) {
            addAndDeleteAttribute();
        }

        // make sure to see as many events as possible
        timeline.turnOnLiveMode(); // there could be no events in the past
        timeline.turnOffLiveMode();
    }

    private void addAndDeleteAttribute() throws Exception {
        final String NEW_ATTR_NAME = "e2e-test-cd-attr-name";
        final String NEW_ATTR_VALUE = "e2e-test-cd-attr-value";
        final String NODE_NAME = "BUSINESSTRANSACTION";
        final String[] nodeNames = { NODE_NAME }; 

        boolean wasHistoricModeSelected = timeline.isHistoricModeSelected();
        if (wasHistoricModeSelected) {
            timeline.turnOnLiveMode();
        }

        assertFalse(canvas.getListOfNodes().size() < 1,
                "Unable to create an attribute. There are no nodes in the map.");

        // First delete the attribute if it already exists
        canvas.deleteCustomAttributeFromNodesIfItExists(nodeNames, NEW_ATTR_NAME);

        canvas.waitForUpdate();
        canvas.selectNode(canvas.getNodeByNameSubstring(NODE_NAME));

        details = ui.getDetailsPanel();
        
        // Collapse Basic Attribute, Alerts and Performance Overview sections to minimize 
        //  the whole panel contents skipping up and down while the contents of those sections loads
        details.collapseSection(DetailsPanel.SECTION_ALERTS);
        details.collapseSection(DetailsPanel.SECTION_PERFORMANCE_OVERVIEW);
        details.collapseSection(DetailsPanel.SECTION_BASIC_ATTRIBUTES);
        
        details.addNewAttribute(NEW_ATTR_NAME, NEW_ATTR_VALUE, false);

        // Load the attribute table rows after the attribute was created
        List<AttributeRow> attributeRows = details.getAttributeRowsByNameAsync(AttributeType.OTHER_ATTRIBUTES, NEW_ATTR_NAME);
        assertTrue(attributeRows.size() > 0);

        // Delete the created attribute
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        attributeRows.get(0).getDeleteIcon().click();
        details.waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);

        Utils.sleep(500);
        
        // Load the attribute table rows after the attribute was deleted
        List<AttributeRow> rows = details.getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, NEW_ATTR_NAME);
        
        // There may be 1 row with the attribute name and the value <empty> 
        //  or there may be no row with the attribute name if a refresh occurred meanwhile 
        if (rows.size() == 1) {
            assertEquals(rows.get(0).getEndTimeValueCell().getText(), "<empty>");
        } else {
            assertEquals(rows.size(), 0);
        }

        // Tidy up
        canvas.clickToUnusedPlaceInCanvas();
        if (wasHistoricModeSelected) {
            timeline.turnOffLiveMode();
        }
    }

    @Test
    public void testTogglingEvents() throws Exception {
        init(true);

        // ATTRIBUTE EVENTS
        timeline.uncheckAttributeChangeCheckbox();
        assertEquals(timeline.getCountOfAllEventsByType(Timeline.ATTRIBUTE_CHANGE), 0);
        assertFalse(details.isSectionEnabled(DetailsPanel.SECTION_EVENTS_ATTRIBUTES));

        timeline.checkAttributeChangeCheckbox();
        assertTrue(timeline.getCountOfAllEventsByType(Timeline.ATTRIBUTE_CHANGE) > 0);

        if (timeline.getCountOfInRangeEventsByType(Timeline.ATTRIBUTE_CHANGE) > 0) {
            assertTrue(details.getEventsCount(EventType.ATTRIBUTE_CHANGE) > 0);
        }

        // STATUS EVENTS
        timeline.uncheckStatusChangeCheckbox();
        assertEquals(timeline.getCountOfAllEventsByType(Timeline.STATUS_CHANGE), 0);
        assertFalse(details.isSectionEnabled(DetailsPanel.SECTION_EVENTS_STATUS));

        timeline.checkStatusChangeCheckbox();
        assertTrue(timeline.getCountOfAllEventsByType(Timeline.STATUS_CHANGE) >= 0);

        if (timeline.getCountOfInRangeEventsByType(Timeline.STATUS_CHANGE) > 0) {
            assertTrue(details.getEventsCount(EventType.STATUS_CHANGE) > 0);
        }

        // TOPOLOGICAL EVENTS
        timeline.uncheckTopologicalChangeCheckbox();
        assertEquals(timeline.getCountOfAllEventsByType(Timeline.TOPOLOGICAL_CHANGE), 0);
        assertFalse(details.isSectionEnabled(EventType.TOPOLOGICAL_CHANGE.asText()));

        timeline.checkTopologicalChangeCheckbox();
        assertTrue(timeline.getCountOfAllEventsByType(Timeline.TOPOLOGICAL_CHANGE) >= 0);

        if (timeline.getCountOfInRangeEventsByType(Timeline.TOPOLOGICAL_CHANGE) > 0) {
            assertTrue(details.getEventsCount(EventType.TOPOLOGICAL_CHANGE) > 0);
        }
        
        ui.cleanup();
    }

    private void verifySorting(List<String> values, String direction, EventType eventType) {
        int size = values.size();
        assertTrue(size > 0);

        String msg, cur, next;
        DateFormat df = new SimpleDateFormat("M/d/yy h:mm:ss a", Locale.US);
        for (int i = 0; i < size - 1; i++) {
            cur = values.get(i);
            next = values.get(i + 1);

            if (direction == DIR_ASCENDING) {
                msg = "Bad sorting of " + eventType + ": The array element #" + i
                        + " (" + cur + ") should be less or equal than the element #" + (i + 1)
                        + " (" + next + ")";

                try {
                    assertTrue(df.parse(cur).compareTo(df.parse(next)) <= 0, msg);
                } catch (ParseException e) {
                    assertTrue(cur.compareToIgnoreCase(next) <= 0, msg);
                }
            } else {
                msg = "Bad sorting of " + eventType + ": The array element #" + i
                        + " (" + cur + ") should be greater or equal than the element #" + (i + 1)
                        + " (" + next + ")";

                try {
                    assertTrue(df.parse(cur).compareTo(df.parse(next)) >= 0, msg);
                } catch (ParseException e) {
                    assertTrue(cur.compareToIgnoreCase(next) >= 0, msg);
                }
            }
        }
    }

    private void testEventTable(EventType eventType) throws InterruptedException {
        assertEquals(details.getEventTableColumnCount(eventType),
                details.getColumnCount(eventType));

        String eventTimeColName = DetailsPanel.EVENT_TABLE_COLUMN_EVENT_TIME;
        int eventTimeColumnNdx = details.getColumnOrder(eventType, eventTimeColName);
        int nodeNameColumnNdx =
                details.getColumnOrder(eventType, DetailsPanel.EVENT_TABLE_COLUMN_NODE_NAME);
        WebElement eventTimeCol = details.getEventTableColumnByIndex(eventType, eventTimeColumnNdx);

        assertTrue(eventTimeCol.isDisplayed(),
                "Column " + eventTimeColName + " not found in the " + eventType + " table");

        assertTrue(
                details.isEventTableColumnSortedInDescendingWay(eventType, eventTimeColumnNdx),
                "The default sorting of the table " + eventType + " is not correct");

        List<String> values = details.getValuesFromColumn(
                details.getEventTableRows(eventType), eventTimeColumnNdx);
        verifySorting(values, DIR_DESCENDING, eventType);

        details.sortEventTableColumnInAscendingWay(eventType, eventTimeColumnNdx);
        values = details.getValuesFromColumn(
                details.getEventTableRows(eventType), eventTimeColumnNdx);
        verifySorting(values, DIR_ASCENDING, eventType);

        details.sortEventTableColumnInAscendingWay(eventType, nodeNameColumnNdx);
        values = details.getValuesFromColumn(
                details.getEventTableRows(eventType), nodeNameColumnNdx);
        verifySorting(values, DIR_ASCENDING, eventType);

        details.sortEventTableColumnInDescendingWay(eventType, nodeNameColumnNdx);
        values = details.getValuesFromColumn(
                details.getEventTableRows(eventType), nodeNameColumnNdx);
        verifySorting(values, DIR_DESCENDING, eventType);

        details.sortEventTableColumnInDescendingWay(eventType, eventTimeColumnNdx);
    }

    @Test
    public void testEventTables() throws Exception {
        init(true);


        timeline.checkStatusChangeCheckbox();
        canvas.waitForUpdate();
        if (timeline.getCountOfInRangeEventsByType(Timeline.STATUS_CHANGE) > 0) {
            testEventTable(EventType.STATUS_CHANGE);
        }

        timeline.checkTopologicalChangeCheckbox();
        canvas.waitForUpdate();
        if (timeline.getCountOfInRangeEventsByType(Timeline.TOPOLOGICAL_CHANGE) > 0) {
            testEventTable(EventType.TOPOLOGICAL_CHANGE);
        }

        timeline.checkAttributeChangeCheckbox();
        canvas.waitForUpdate();
        if (timeline.getCountOfInRangeEventsByType(Timeline.ATTRIBUTE_CHANGE) > 0) {
            testEventTable(EventType.ATTRIBUTE_CHANGE);
        }
        ui.cleanup();
    }

    @Test(groups = "failing")
    public void testHighlightingFromEventsTable() throws Exception {
        init(false);
        timeline.checkTopologicalChangeCheckbox();

        logger.info("should create \"Type\" highlight");
        ui.getRibbon().expandHighlightToolbar();
        FilterMenu nameBrusher = ui.getDataBrushing().add("Type");

        logger.info("should highlight \"SOCKET\" node");
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkMenuOption("SOCKET");
        assertEquals(nameBrusher.getListOfSelectedItems().size(), 1);
        nameBrusher.confirmMenu();
        assertTrue(canvas.isHighlighted(canvas.getNodeByNameSubstring("SOCKET")));

        logger.info("should highlight \"SERVLET\" node by clicking in events table");
        canvas.getNodeByNameSubstring("SERVLET").click();
        
        if (timeline.getCountOfAllEventsByType(Timeline.STATUS_CHANGE) > 0) {
            assertEquals(details.getHighlightedEventTableRows(EventType.STATUS_CHANGE).size(), 0);
            details.getFirstEventTableRow(EventType.STATUS_CHANGE).click();
            assertEquals(details.getHighlightedEventTableRows(EventType.STATUS_CHANGE).size(), 1);
            assertTrue(canvas.isHighlighted(canvas.getNodeByNameSubstring("SERVLET")));

            logger.info("previous highlighting is canceled");
            assertFalse(canvas.isHighlighted(canvas.getNodeByNameSubstring("SOCKET")));
    
            logger.info("previous highlighting is restored by clicking outside event table");
            details.getAlertSummaryPanel().click();
            assertEquals(details.getHighlightedEventTableRows(EventType.STATUS_CHANGE).size(), 0);
            assertTrue(canvas.isHighlighted(canvas.getNodeByNameSubstring("SOCKET")));
            assertFalse(canvas.isHighlighted(canvas.getNodeByNameSubstring("SERVLET")));
        } else {
            logger.warn("Status events not found, skipping test.");
        }

        ui.cleanup();
    }

}
