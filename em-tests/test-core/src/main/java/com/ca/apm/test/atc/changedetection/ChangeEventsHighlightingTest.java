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
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.testbed.atc.TeamCenterRegressionTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChangeEventsHighlightingTest extends UITest {

    private UI ui = null;
    private Timeline timeline = null;
    private DetailsPanel details = null;

    private enum EventPosition {
        FIRST, BEGINNING, MIDDLE, END, LAST
    }

    private void init() throws Exception {
        ui = getUI();
        timeline = ui.getTimeline();
        details = ui.getDetailsPanel();

        ui.login(Role.ADMIN);
        ui.getLeftNavigationPanel().goToMapViewPage();

        timeline.turnOnLiveMode();
        timeline.turnOffLiveMode();
    }

    /**
     * Find the in-range event of given event type and click it
     * 
     * @param eventType the type of event to look for (timeline row)
     * @param eventPosition position in timeline
     * @return
     */
    private WebElement clickOnTimelineEvent(String eventType, EventPosition eventPosition) {

        int eventsCount = timeline.getCountOfInRangeEventsByType(eventType);
        if (eventsCount == 0) {
            logger.warn("There are no {} events in the selected time range.", eventType);
            return null;
        }

        int n = 0;
        if (eventPosition == EventPosition.BEGINNING && eventsCount > 3) {
            n = 1;
        } else if (eventPosition == EventPosition.MIDDLE) {
            n = (int) Math.floor(eventsCount / 2);
        } else if (eventPosition == EventPosition.END) {
            n = eventsCount - 1;
            if (eventsCount > 3) {
                n--;
            }
        } else if (eventPosition == EventPosition.LAST) {
            n = eventsCount - 1;
        }

        WebElement event;
        try {
            event = timeline.getNthInRangeEventByType(eventType, n);
            event.click();
        } catch (WebDriverException e) {
            // cannot click on the event icon because it is too near to the either start or end bar
            logger.warn("{} timeline event (#{}) not clickable.", eventType, eventPosition, e);
            return null;
        }

        return event;
    }

    private Date parseTime(String timeInMillis) {
        long time = Long.parseLong(timeInMillis);

        // truncate the milliseconds (the times in the events table have also the millis truncated,
        // and we want to compare them)
        time = 1000 * (time / 1000L);
        return new Date(time);
    }

    private void checkEventsHighlighting(String timelineEvtType, EventType tableEvtType,
        EventPosition pos) throws ParseException {

        WebElement event = clickOnTimelineEvent(timelineEvtType, pos);
        if (event == null) {
            return;
        }

        // find out if the timeline icon represents a merged event or a single event
        boolean isMerged = event.getAttribute("class").contains("merged");

        Date eventStartTime = parseTime(timeline.getTimeStartOfEvent(event));
        Date eventEndTime = parseTime(timeline.getTimeEndOfEvent(event));
        int eventTimeColumnNo =
            details.getColumnOrder(tableEvtType, DetailsPanel.EVENT_TABLE_COLUMN_EVENT_TIME);
        SimpleDateFormat eventTimeColFmt = new SimpleDateFormat("M/d/yy h:mm:ss a", Locale.US);
        List<String> eventTimeColValues;

        // check whether there are any rows above the first highlighted record in the event table
        List<PageElement> precedingRows = details.getRowsAboveHighlightedSection(tableEvtType);
        if (precedingRows.size() > 0) {
            // Select the record just a row above the first highlighted record and check that its
            // time is after the selected timeline event time (the table is sorted in event time
            // descending order) and thus it should really have not be highlighted

            eventTimeColValues = details.getValuesFromColumn(precedingRows, eventTimeColumnNo);
            Date precedingRecEventTime =
                eventTimeColFmt.parse(eventTimeColValues.get(eventTimeColValues.size() - 1));
            String msg =
                "On selecting the " + tableEvtType + " " + (isMerged ? "merged" : "single")
                    + " event [" + eventStartTime + "," + eventEndTime
                    + "], the event time of the record just a row above the first"
                    + "highlighted record is " + precedingRecEventTime
                    + " while it should be after the selected event start time";
            Assert.assertTrue(eventEndTime.before(precedingRecEventTime), msg);
        }

        // check whether there are any rows below the last highlighted record in the event table
        List<PageElement> followingRows = details.getRowsBelowHighlightedSection(tableEvtType);
        if (followingRows.size() > 0) {
            // Select the record just a row below the last highlighted record and check that its
            // time is
            // before the selected timeline event time (the table is sorted in event time descending
            // order)
            // and thus it should really have not be highlighted

            eventTimeColValues = details.getValuesFromColumn(followingRows, eventTimeColumnNo);
            Date nextRecEventTime = eventTimeColFmt.parse(eventTimeColValues.get(0));
            String msg =
                "On selecting the " + tableEvtType + " " + (isMerged ? "merged" : "single")
                    + " event [" + eventStartTime + "," + eventEndTime
                    + "], the event time of the "
                    + " record just a row below the last highlighted record is " + nextRecEventTime
                    + " while it should be after the selected event end time";
            Assert.assertTrue(eventStartTime.after(nextRecEventTime), msg);
        }

        // get the event times of the highlighted rows
        List<PageElement> highlightedRows = details.getHighlightedEventTableRows(tableEvtType);
        eventTimeColValues = details.getValuesFromColumn(highlightedRows, eventTimeColumnNo);
        int arrLen = eventTimeColValues.size();

        // If the number of events in the timeline exceeds the event table row limit
        // the table doesn't contain all events
        int eventsCount = details.getEventsCount(tableEvtType);
        boolean tableLimitOverrun = eventsCount > DetailsPanel.MAX_EVENTS_LIMIT;
        if (tableLimitOverrun && (arrLen <= (isMerged ? 1 : 0))) {
            logger.warn("There are {} {} events in the timeline"
                    + " which overruns the table row limit of the event table",
                eventsCount, timelineEvtType);
        } else {
            // If the event icon represented a merged event
            // multiple records have to be highlighted in the event table
            String msg =
                "Selecting the " + tableEvtType + " " + (isMerged ? "merged" : "single")
                    + " event [" + eventStartTime + "," + eventEndTime + "] caused only " + arrLen
                    + " rows to be selected in the event table";
            Assert.assertTrue(arrLen > (isMerged ? 1 : 0), msg);

            // Check that event time of each highlighted event record fits to the time range
            // represented by the event element selected in the timeline
            for (int i = 0; i < arrLen; i++) {
                Date eventTime = eventTimeColFmt.parse(eventTimeColValues.get(i));
                msg =
                    "On selecting the " + tableEvtType + " " + (isMerged ? "merged" : "single")
                        + " event [" + eventStartTime + "," + eventEndTime
                        + "], the event time of the " + i + "-th highlighted record is "
                        + eventTime;
                Assert.assertTrue(
                    eventTime.after(eventStartTime) || eventTime.equals(eventStartTime), msg);
                Assert.assertTrue(eventTime.before(eventEndTime) || eventTime.equals(eventEndTime),
                    msg);
            }
        }

        // At last click to an unused place in the canvas to clear the highlighting
        ui.getCanvas().clickToUnusedPlaceInCanvas();

        Assert.assertTrue(
            details.getHighlightedEventTableRowCount(EventType.ATTRIBUTE_CHANGE) == 0,
            "After clicking to an unused place in the map,"
                + " there should be no highlighted record in the attribute event table");
        Assert.assertTrue(
            details.getHighlightedEventTableRowCount(EventType.STATUS_CHANGE) == 0,
            "After clicking to an unused place in the map,"
                + " there should be no highlighted record in the status event table");
        Assert.assertTrue(
            details.getHighlightedEventTableRowCount(EventType.TOPOLOGICAL_CHANGE) == 0,
            "After clicking to an unused place in the map,"
                + " there should be no highlighted record in the topological event table");
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testEventTablesHighlighting() throws Exception {
        init();

        timeline.expand();
        timeline.checkStatusChangeCheckbox();
        timeline.checkTopologicalChangeCheckbox();
        timeline.checkAttributeChangeCheckbox();

        EventPosition[] evtPositions =
            {EventPosition.FIRST, EventPosition.MIDDLE, EventPosition.LAST};

        for (EventPosition pos : evtPositions) {
            logger.info(
                "checking highlighting of status events in the events table when the {} position "
                    + "status event is clicked in timeline", pos);
            checkEventsHighlighting(Timeline.STATUS_CHANGE, EventType.STATUS_CHANGE, pos);
            logger.info(
                "checking highlighting of topological events in the events table when the {} "
                    + "position topological event is clicked in timeline", pos);
            checkEventsHighlighting(Timeline.TOPOLOGICAL_CHANGE, EventType.TOPOLOGICAL_CHANGE,
                pos);
            logger.info(
                "checking highlighting of attribute events in the events table when the {} "
                    + "position attribute event is clicked in timeline", pos);
            checkEventsHighlighting(Timeline.ATTRIBUTE_CHANGE, EventType.ATTRIBUTE_CHANGE, pos);
        }

        timeline.uncheckStatusChangeCheckbox();
        Assert
            .assertEquals(details.getHighlightedEventTableRowCount(EventType.STATUS_CHANGE), 0);

        timeline.uncheckTopologicalChangeCheckbox();
        Assert
            .assertEquals(details.getHighlightedEventTableRowCount(EventType.TOPOLOGICAL_CHANGE), 0);

        timeline.uncheckAttributeChangeCheckbox();
        Assert
            .assertEquals(details.getHighlightedEventTableRowCount(EventType.ATTRIBUTE_CHANGE), 0);
    }

    private void checkMapNodesHighlighting(String timelineEvtType, EventType tableEvtType,
        EventPosition pos) throws ParseException {

        WebElement event = clickOnTimelineEvent(timelineEvtType, pos);
        if (event == null) {
            return;
        }

        Utils.sleep(100); // wait for nodes to highlight
        Canvas canvas = ui.getCanvas();
        List<PageElement> highlightedNodes = canvas.getListOfHighlightedNodes();

        Assert.assertTrue(highlightedNodes.size() > 0,
            "At least one map node has to be highlighted.");

        String[] ids = timeline.getVertexIdsOfEvent(event).split(",");
        Set<String> eventVertIDs = new HashSet<>(Arrays.asList(ids));

        for (WebElement node : highlightedNodes) {
            String[] nodeVertIDs = node.getAttribute("id").split(",");
            Assert.assertNotNull(nodeVertIDs, "Highlighted node doesn't contain 'id' attribute.");

            boolean found = false;
            for (String id : nodeVertIDs) {
                if (eventVertIDs.contains(id)) {
                    found = true;
                    eventVertIDs.remove(id);
                }
            }

            Assert.assertTrue(found, "This map node should not be highlighted."
                + " Vertex ids of highlighted node: [" + StringUtils.join(nodeVertIDs, ',')
                + "], Vertex ids of the selected event: " + eventVertIDs);
        }

        String msg =
            timelineEvtType + " timeline event selected. Following vertex IDs are NOT"
                + " highlighted in the map: " + eventVertIDs;
        Assert.assertTrue(eventVertIDs.isEmpty(), msg);
    }

    private void checkMapNodesHighlighting(EventPosition[] evtPositions) throws Exception {
        for (EventPosition evtPosition : evtPositions) {
            logger.info("checking highlighting of the nodes on the map"
                    + " when the {} position attribute event is clicked in timeline",
                evtPosition);
            checkMapNodesHighlighting(Timeline.ATTRIBUTE_CHANGE, EventType.ATTRIBUTE_CHANGE,
                evtPosition);
            logger.info("checking highlighting of the nodes on the map"
                    + " when the {} position status event is clicked in timeline",
                evtPosition);
            checkMapNodesHighlighting(Timeline.STATUS_CHANGE, EventType.STATUS_CHANGE,
                evtPosition);
            logger.info("checking highlighting of the nodes on the map"
                    + " when the {} position topological event is clicked in timeline",
                evtPosition);
            checkMapNodesHighlighting(Timeline.TOPOLOGICAL_CHANGE, EventType.TOPOLOGICAL_CHANGE,
                evtPosition);
        }
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test(groups = "failing")
    public void testMapNodesHighlighting() throws Exception {
        init();

        timeline.expand();
        timeline.checkAttributeChangeCheckbox();
        timeline.checkStatusChangeCheckbox();
        timeline.checkTopologicalChangeCheckbox();

        // filter data only to Trading Service
        getUI().getFilterBy().removeCompleteFilter();
        Utils.sleep(250);
        FilterMenu bt = getUI().getFilterBy().add("Business Service");

        bt.expandDropDownMenu();
        bt.uncheckSelectAll();
        bt.checkMenuOption("Trading Service");
        bt.confirmMenu();

        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.getRibbon().expandTimelineToolbar();

        EventPosition[] evtPositions =
            {EventPosition.BEGINNING, EventPosition.MIDDLE, EventPosition.END};

        // GROUPS EXPANDED
        logger.info("expanding all groups");
        ui.getCanvas().getCtrl().unfoldAllGroups();
        checkMapNodesHighlighting(evtPositions);

        // GROUPS COLLAPSED
        logger.info("collapsing all groups");
        ui.getCanvas().getCtrl().foldAllGroups();
        checkMapNodesHighlighting(evtPositions);

        timeline.uncheckStatusChangeCheckbox();
        timeline.uncheckAttributeChangeCheckbox();
        timeline.uncheckTopologicalChangeCheckbox();
        Assert.assertTrue(ui.getCanvas().getListOfHighlightedNodes().isEmpty());
    }

}
