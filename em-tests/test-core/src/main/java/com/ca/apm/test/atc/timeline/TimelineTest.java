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
package com.ca.apm.test.atc.timeline;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.Timeline;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.PageElement;

public class TimelineTest extends UITest {

    private UI ui = null;
    private Timeline timeline = null;

    private void init() throws Exception {
        ui = getUI();
        timeline = ui.getTimeline();
        
        ui.login(Role.ADMIN);
        ui.getLeftNavigationPanel().goToMapViewPage();

        timeline.expand();
    }

    private void testTimeRangeDiff(long expectedDiff) throws Exception {
        long startTime = 0;
        long endTime = 0;

        try {
            startTime = timeline.getStartTimeMilliseconds();
            endTime = timeline.getEndTimeMilliseconds();
            long timeDiff = endTime - startTime;
            
            Assert.assertEquals(timeDiff, expectedDiff, "actual: " + timeDiff + ", expected: "
                        + expectedDiff + ", startTime: " + startTime + ", endTime: " + endTime);
        } catch (ParseException pe) {
            throw new Exception(String.format("Could not parse timeline dates,  start time: '%s', end time: '%s'", startTime, endTime));
        }
    }

    /**
     * Should set the start and end times correctly
     * 
     * @throws Exception
     */
    @Test
    public void testTimeRange() throws Exception {
        init();

        timeline.turnOnLiveMode();
        timeline.turnOffLiveMode();
        
        // Timeline will maintain 8 minutes difference from Live,
        // set the start time a 2 hours back
        timeline.openStartTimeCalendar();
        PageElement decreaseHourHandler = timeline.getStartTimeCalendarHourDecreaseBtn();
        decreaseHourHandler.click();
        decreaseHourHandler.click();
        timeline.calendarApply();
        
        ui.getCanvas().waitForUpdate();

        // Range should be 2 hours 8 minutes now
        long rangeTime = (1000 * 60 * 60 * 2) + (1000 * 60 * 8);
        testTimeRangeDiff(rangeTime);
    }

    @Test
    public void testMinimalTimeRange() throws Exception {
        init();

        timeline.turnOnLiveMode();
        timeline.dragEndTimeBarBeforeWindowStart();
        ui.getCanvas().waitForUpdate();

        long eightMinutes = 1000 * 60 * 8;
        testTimeRangeDiff(eightMinutes);
    }

    /**
     * Should set the end time to the current time when switched to the Live mode
     * 
     * @throws Exception
     */
    @Test
    public void testSwitchingMode_endTimeOK() throws Exception {
        init();

        timeline.turnOnLiveMode();

        DateFormat df = new SimpleDateFormat("M/d/yy h:mm:ss a", Locale.US);
        String endTime = timeline.getEndTime();
        long timeDiff = df.parse(endTime).getTime() - System.currentTimeMillis();

        Assert.assertTrue(timeDiff < 1000);
    }

    /**
     * Test should check that dragging the end time bar will change timeline mode to historical
     * 
     * @throws Exception
     */
    @Test
    public void testChangeEndTime_ModeSwitched() throws Exception {
        init();
        timeline.turnOnLiveMode();
        getUI().getCanvas().waitForUpdate();
        timeline.openStartTimeCalendar();
        timeline.getStartTimeCalendarMinuteDecreaseBtn().click();
        timeline.calendarApply();
        getUI().getCanvas().waitForUpdate();
        Assert.assertTrue(timeline.isHistoricModeSelected());
    }

    private void checkInRangeEventsTime(String eventType) throws Exception {
        Utils.sleep(250); // wait for events to show up

        int eventsCount = timeline.getCountOfInRangeEventsByType(eventType);
        if (eventsCount == 0) {
            logger.warn("There are no {} events in the selected time range.", eventType);
            return;
        }

        DateFormat df = new SimpleDateFormat("M/d/yy h:mm:ss a", Locale.US);
        long startTime = df.parse(timeline.getStartTime()).getTime();
        long endTime = df.parse(timeline.getEndTime()).getTime();

        WebElement firstEvent = timeline.getFirstInRangeEventByType(eventType);
        long timeOfFirstEvent = Long.parseLong(timeline.getTimeStartOfEvent(firstEvent));
        Assert.assertTrue(timeOfFirstEvent >= startTime);
        Assert.assertTrue(timeOfFirstEvent <= endTime);

        WebElement lastEvent = timeline.getLastInRangeEventByType(eventType);
        long timeOfLastEvent = Long.parseLong(timeline.getTimeEndOfEvent(lastEvent));
        Assert.assertTrue(timeOfLastEvent >= startTime);
        Assert.assertTrue(timeOfLastEvent <= endTime);
    }

    /**
     * Test should check that the time values of in-range timeline events are within the time range.
     */
    @Test
    public void testInRangeEventsTime() throws Exception {
        init();
        timeline.turnOnLiveMode();
        timeline.turnOffLiveMode();

        timeline.checkStatusChangeCheckbox();
        checkInRangeEventsTime(Timeline.STATUS_CHANGE);

        timeline.checkTopologicalChangeCheckbox();
        checkInRangeEventsTime(Timeline.TOPOLOGICAL_CHANGE);

        timeline.checkAttributeChangeCheckbox();
        checkInRangeEventsTime(Timeline.ATTRIBUTE_CHANGE);
    }

    private void checkEventIcons(String eventType) throws Exception {

        DateFormat df = new SimpleDateFormat("M/d/yy h:mm:ss a", Locale.US);
        long startTime = df.parse(timeline.getStartTime()).getTime();
        long endTime = df.parse(timeline.getEndTime()).getTime();

        List<PageElement> allEvents = timeline.getAllEventsByType(eventType);
        long evtStart, evtEnd;

        for (WebElement event : allEvents) {
            evtStart = Long.parseLong(timeline.getTimeStartOfEvent(event));
            evtEnd = Long.parseLong(timeline.getTimeEndOfEvent(event));

            if (evtStart >= startTime && evtEnd < endTime) { // event in range
                Assert.assertTrue(timeline.isEventInRange(event));
            } else if (evtStart > endTime || evtEnd < startTime) { // event out of range
                Assert.assertTrue(timeline.isEventOutOfRange(event));
            }
        }
    }

    @Test
    public void testEventIconsVisuals() throws Exception {
        init();
        timeline.turnOnLiveMode();
        timeline.turnOffLiveMode();

        timeline.checkStatusChangeCheckbox();
        checkEventIcons(Timeline.STATUS_CHANGE);

        timeline.checkTopologicalChangeCheckbox();
        checkEventIcons(Timeline.TOPOLOGICAL_CHANGE);

        timeline.checkAttributeChangeCheckbox();
        checkEventIcons(Timeline.ATTRIBUTE_CHANGE);
    }
        
    /**
     * Test that the default time range keeps unchanged when switching between
     * map and trend view, between live and historic mode and between collapsed
     * and expanded state
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultRangeIsStable() throws Exception {
        init();
        
        timeline.turnOffLiveMode();
        timeline.turnOnLiveMode();
        
        logger.info("Test timeline range is default");
        testTimeRangeDiff(1000 * 60 * 8);
        
        ui.getLeftNavigationPanel().goToDashboardPage();
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        logger.info("Test timeline range did not change after view change");
        testTimeRangeDiff(1000 * 60 * 8);
        
        timeline.collapse();
        Utils.sleep(1000);
        timeline.expand();
        Utils.sleep(1000);
        
        testTimeRangeDiff(1000 * 60 * 8);
    }
    
    /**
     * Test the Timeline identity between Map View and Trend View:
     * 1. Start time and End time must be the same
     * 2. Mode must be the same
     * 3. Events status must be the same (checked/unchecked)
     * 4. Number of events on timeline must be the same (in Historic mode)
     * 
     * @throws Exception
     */
    @Test
    public void testMapViewTrendViewTimelineEquality() throws Exception {
        init();
        logger.info("Turn on Live Mode");
        timeline.turnOnLiveMode();
        Assert.assertFalse(ui.getBottomBar().isEventsActive());

        logger.info("Turn off Live Mode");
        timeline.turnOffLiveMode();
        
        logger.info("Set calendar back 30 minutes");
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE) - 30;
        
        // Calculate current time - 30 minutes
        if (minutes < 0) {
            hours = hours == 0 ? 23 : hours - 1;
            minutes += 60; 
        }

        // Move back in time to not see live events appearing on timeline
        timeline.setStartTimeCalendarTime(hours, minutes);
        logger.info("Enable all events");
        timeline.checkStatusChangeCheckbox();
        timeline.checkTopologicalChangeCheckbox();
        timeline.checkAttributeChangeCheckbox();
        Assert.assertTrue(ui.getBottomBar().isEventsActive());

        String mapStartTime = timeline.getStartTime();
        String mapEndTime = timeline.getEndTime();
        List<PageElement> allMapStatusEvents = timeline.getAllEventsByType(Timeline.STATUS_CHANGE);
        List<PageElement> allMapTopoEvents = timeline.getAllEventsByType(Timeline.TOPOLOGICAL_CHANGE);
        List<PageElement> allMapAttrEvents = timeline.getAllEventsByType(Timeline.ATTRIBUTE_CHANGE);
        
        logger.info("Go to Dashboard");
        ui.getLeftNavigationPanel().goToDashboardPage();
        
        String trendStartTime = timeline.getStartTime();
        String trendEndTime = timeline.getEndTime();
        List<PageElement> allTrendStatusEvents = timeline.getAllEventsByType(Timeline.STATUS_CHANGE);
        List<PageElement> allTrendTopoEvents = timeline.getAllEventsByType(Timeline.TOPOLOGICAL_CHANGE);
        List<PageElement> allTrendAttrEvents = timeline.getAllEventsByType(Timeline.ATTRIBUTE_CHANGE);

        Assert.assertEquals(mapStartTime, trendStartTime);
        Assert.assertEquals(mapEndTime, trendEndTime);
        Assert.assertEquals(allMapStatusEvents.size(), allTrendStatusEvents.size());
        Assert.assertEquals(allMapTopoEvents.size(), allTrendTopoEvents.size());
        Assert.assertEquals(allMapAttrEvents.size(), allTrendAttrEvents.size());
        
        // Check if events are selected
        Assert.assertTrue(timeline.isStatusChangeSelected());
        Assert.assertTrue(timeline.isTopologicalChangeSelected());
        Assert.assertTrue(timeline.isAttributeChangeSelected());
        Assert.assertTrue(ui.getBottomBar().isEventsActive());
        
        logger.info("Disable all events");
        timeline.uncheckStatusChangeCheckbox();
        timeline.uncheckTopologicalChangeCheckbox();
        timeline.uncheckAttributeChangeCheckbox();
        Assert.assertFalse(ui.getBottomBar().isEventsActive());
        
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        Assert.assertFalse(timeline.isStatusChangeSelected());
        Assert.assertFalse(timeline.isTopologicalChangeSelected());
        Assert.assertFalse(timeline.isAttributeChangeSelected());
        Assert.assertFalse(ui.getBottomBar().isEventsActive());
        
        // Test mode synchronization
        logger.info("Turn off Live mode");
        timeline.turnOffLiveMode();
        
        logger.info("Go to Trend view");
        ui.getLeftNavigationPanel().goToDashboardPage();
        
        Assert.assertFalse(timeline.isLiveModeSelected());
        
        logger.info("Turn on Live mode");
        timeline.turnOnLiveMode();
        
        logger.info("Go to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        Assert.assertTrue(timeline.isLiveModeSelected());
    }
}
