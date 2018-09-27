package com.ca.apm.test.atc.landing;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.*;
import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.common.landing.LandingPage;
import com.ca.apm.test.atc.common.landing.SummaryPanel;
import com.ca.apm.test.atc.common.landing.SummaryPanel.TimeRange;
import com.ca.apm.test.atc.common.landing.Tile;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class TimePersistenceTest extends UITest {
    
    private final static Logger logger = Logger.getLogger(TimePersistenceTest.class);
    
    private UI ui;
    private LeftNavigationPanel leftNav;
    private TopNavigationPanel topNav;
    private TimeRange defaultTimeRange = TimeRange.LAST_24_HOURS;
    
    private enum TileSelectionStrategy { FIRST, RANDOM, LAST };
    
    private void init() throws Exception {
        ui = getUI();
        ui.login();
        leftNav = ui.getLeftNavigationPanel();
        topNav = ui.getTopNavigationPanel();
    }
    
    @Test
    public void testTimeRangesInHomePage() throws Exception {
        init();
        
        LandingPage landingPage = ui.getLandingPage();
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        
        TimeRange range = summaryPanel.getSelectedTimeRange();
        
        // Verify the default time range
        Assert.assertNotNull(range, "The default time range should be '" + defaultTimeRange.getLabel() + "'");
        Assert.assertEquals(range, defaultTimeRange, "The default time range should be '" + defaultTimeRange.getLabel() + "'");
        
        // Verify the available options in the time range dropdown are expected
        List<String> availableOptions = summaryPanel.getAvailableTimeRangeOptions();
        for (String option : availableOptions) {
            TimeRange r = TimeRange.of(option);
            Assert.assertNotNull(r, "The time range option '" + option + "' is not expected to be in the dropdown.");
        }
        
        // Verify all expected time range options are available
        for (TimeRange r : TimeRange.values()) {
            String option = r.getLabel();
            Assert.assertTrue(availableOptions.contains(option), "The time range option '" + option + "' is missing in the dropdown.");
        }
    }
    
    @Test
    public void testLiveModeAndTimeIndication() throws Exception {
        init();
        
        LandingPage landingPage = ui.getLandingPage();
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        
        for (TimeRange r : TimeRange.values()) {
            summaryPanel.setTimeRange(r);
            
            Assert.assertEquals(summaryPanel.isModeIndicatorDisplayingLive(), r.isLive(), 
                "Live mode indication is not correct for the time range " + r.getLabel());
            
            if (r.isLive()) {
                Long startTime = summaryPanel.getStartTime();
                Long endTime = summaryPanel.getEndTime();
            
                Assert.assertEquals(endTime - startTime, r.getSeconds() * 1000, 
                    "Start and end time indication does not match the selected time range");
            }
        }
    }
    
    @Test 
    public void testPreserveTimeFromHomeToMapAndBackLast24HoursLive() throws Exception {
        testPreserveTimeFromHomeToMapAndBackImpl(TimeRange.LAST_24_HOURS, true);
    }

    @Test 
    public void testPreserveTimeFromHomeToDashboardAndBackLast12HoursLive() throws Exception {
        testPreserveTimeFromHomeToDashboardAndBackImpl(TimeRange.LAST_12_HOURS, true);
    }

    @Test 
    public void testPreserveTimeFromHomeToMapAndBackLast6HoursLive() throws Exception {
        testPreserveTimeFromHomeToMapAndBackImpl(TimeRange.LAST_6_HOURS, true);
    }

    @Test 
    public void testPreserveTimeFromHomeToDashboardAndBackLast2HoursLive() throws Exception {
        testPreserveTimeFromHomeToDashboardAndBackImpl(TimeRange.LAST_2_HOURS, true);
    }

    @Test 
    public void testPreserveTimeFromHomeToMapAndBackLast30MinutesLive() throws Exception {
        testPreserveTimeFromHomeToMapAndBackImpl(TimeRange.LAST_30_MINS, true);
    }
    
    @Test 
    public void testPreserveTimeFromHomeToDashboardAndBackLast8MinutesLive() throws Exception {
        testPreserveTimeFromHomeToDashboardAndBackImpl(TimeRange.LAST_8_MINS, true);
    }
    
    @Test 
    public void testPreserveTimeFromHomeToDashboardAndBackLast24HoursHistoric() throws Exception {
        testPreserveTimeFromHomeToDashboardAndBackImpl(TimeRange.LAST_24_HOURS, false);
    }

    @Test 
    public void testPreserveTimeFromHomeToMapAndBackLast12HoursHistoric() throws Exception {
        testPreserveTimeFromHomeToMapAndBackImpl(TimeRange.LAST_12_HOURS, false);
    }

    @Test 
    public void testPreserveTimeFromHomeToDashboardAndBackLast6HoursHistoric() throws Exception {
        testPreserveTimeFromHomeToDashboardAndBackImpl(TimeRange.LAST_6_HOURS, false);
    }

    @Test 
    public void testPreserveTimeFromHomeToMapAndBackLast2HoursHistoric() throws Exception {
        testPreserveTimeFromHomeToMapAndBackImpl(TimeRange.LAST_2_HOURS, false);
    }

    @Test 
    public void testPreserveTimeFromHomeToDashboardAndBackLast30MinutesHistoric() throws Exception {
        testPreserveTimeFromHomeToDashboardAndBackImpl(TimeRange.LAST_30_MINS, false);
    }
    
    @Test 
    public void testPreserveTimeFromHomeToMapAndBackLast8MinutesHistoric() throws Exception {
        testPreserveTimeFromHomeToMapAndBackImpl(TimeRange.LAST_8_MINS, false);
    }
    
    @Test 
    public void testPreserveTimeFromHomeToWebviewLast8MinutesHistoric() throws Exception {
        testPreserveTimeFromHomeToWebviewImpl(TimeRange.LAST_8_MINS, false);
    }
    
    @Test 
    public void testPreserveTimeFromHomeToWebviewLast8MinutesLive() throws Exception {
        testPreserveTimeFromHomeToWebviewImpl(TimeRange.LAST_8_MINS, true);
    }
    
    @Test 
    public void testPreserveTimeFromHomeToWebviewLast30MinutesHistoric() throws Exception {
        testPreserveTimeFromHomeToWebviewImpl(TimeRange.LAST_30_MINS, false);
    }

    @Test 
    public void testPreserveTimeFromHomeToWebviewLast2HoursLive() throws Exception {
        testPreserveTimeFromHomeToWebviewImpl(TimeRange.LAST_2_HOURS, true);
    }
    
    @Test 
    public void testPreserveTimeFromHomeToWebviewLast6HoursHistoric() throws Exception {
        testPreserveTimeFromHomeToWebviewImpl(TimeRange.LAST_6_HOURS, false);
    }
    
    @Test 
    public void testPreserveTimeFromHomeToWebviewLast24HoursLive() throws Exception {
        testPreserveTimeFromHomeToWebviewImpl(TimeRange.LAST_24_HOURS, true);
    }

    @Test 
    public void testPreserveTimeDuringDrilldownLast24HoursLive() throws Exception {
        testPreserveTimeDuringDrilldownImpl(TimeRange.LAST_24_HOURS, true, TileSelectionStrategy.FIRST);
    }
    
    @Test 
    public void testPreserveTimeDuringDrilldownLast2HoursLive() throws Exception {
        testPreserveTimeDuringDrilldownImpl(TimeRange.LAST_2_HOURS, true, TileSelectionStrategy.RANDOM);
    }
    
    @Test 
    public void testPreserveTimeDuringDrilldownLast8MinutesLive() throws Exception {
        testPreserveTimeDuringDrilldownImpl(TimeRange.LAST_8_MINS, true, TileSelectionStrategy.LAST);
    }
    
    @Test (groups = "failing")
    public void testPreserveTimeDuringDrilldownLast12HoursHistoric() throws Exception {
        testPreserveTimeDuringDrilldownImpl(TimeRange.LAST_12_HOURS, false, TileSelectionStrategy.LAST);
    }
    
    @Test 
    public void testPreserveTimeDuringDrilldownLast30MinutesHistoric() throws Exception {
        testPreserveTimeDuringDrilldownImpl(TimeRange.LAST_30_MINS, false, TileSelectionStrategy.RANDOM);
    }
    
    @Test 
    public void testPreserveTimeDuringDrilldownLast8MinutesHistoric() throws Exception {
        testPreserveTimeDuringDrilldownImpl(TimeRange.LAST_8_MINS, false, TileSelectionStrategy.FIRST);
    }
    
    private void testPreserveTimeDuringDrilldownImpl(TimeRange r, boolean live, TileSelectionStrategy selectionStrategy) throws Exception {
        logger.info("Test that time is preserved during drilldown, range: " + r + ", live: " + live + ", tile selection strategy: " + selectionStrategy);
        
        init();
        
        LandingPage landingPage = ui.getLandingPage();
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        
        summaryPanel.setTimeRange(r);
        
        if (!live) {
            summaryPanel.setTimeRange(TimeRange.CUSTOM_RANGE);
        }
        
        Long origStartTime = summaryPanel.getStartTime();
        Long origEndTime = summaryPanel.getEndTime();
        boolean origLiveDisplayed = summaryPanel.isModeIndicatorDisplayingLive();
        
        Assert.assertEquals(origLiveDisplayed, live, "Live mode indicator displayed incorrectly");
        
        boolean topView = true;
        
        // Drill down level by level until we are in Notebook
        while (ui.getCurrentViewByUrl() == View.HOMEPAGE) {
            // Check the time in the time bar 
            if (topView) {
                topView = false;
            } else {
                Assert.assertEquals(summaryPanel.getStartTime(), origStartTime, "Start time not preserved when drilling down");
                Assert.assertEquals(summaryPanel.getEndTime(), origEndTime, "Start time not preserved when drilling down");
                Assert.assertEquals(summaryPanel.isModeIndicatorDisplayingLive(), live, "Live mode indicator displayed incorrectly");
            }

            landingPage.waitForTilesToLoad(true);
            List<Tile> tiles = landingPage.getActiveTiles();
            
            int index = 0;
            if (selectionStrategy == TileSelectionStrategy.LAST) {
                index = tiles.size() - 1;
            } else if (selectionStrategy == TileSelectionStrategy.RANDOM) {
                index = (int) Math.floor((Math.random() * tiles.size())); 
            }
            
            logger.info("Drilling down into the #" + index + " tile '" + tiles.get(index).getName() + "'");
            
            Tile tile = tiles.get(index);
            if (tile.hasNextDrilldownLevel()) {
                tile.drillDown();
            } else {
                tile.openNotebook();
            }
            
            Assert.assertTrue(landingPage.isDisplayed());
        }

        // We are in Notebook now
        ui.getNotebook().waitForLoad();
        
        Assert.assertEquals(summaryPanel.getStartTime(), origStartTime, "Start time not preserved when drilling down");
        Assert.assertEquals(summaryPanel.getEndTime(), origEndTime, "Start time not preserved when drilling down");
        Assert.assertEquals(summaryPanel.isModeIndicatorDisplayingLive(), false, "In Notebook the time mode should never be Live");
    }
    
    private void testPreserveTimeFromHomeToMapAndBackImpl(TimeRange origRange, boolean live) throws Exception {
        init();
        
        LandingPage landingPage = ui.getLandingPage();
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        
        summaryPanel.setTimeRange(origRange);
        
        if (!live) {
            summaryPanel.setTimeRange(TimeRange.CUSTOM_RANGE);
        }
        
        leftNav.goToMapViewPage();

        Timeline timeline = ui.getTimeline();
        timeline.expand();

        Assert.assertEquals(timeline.isLiveModeSelected(), live);
        
        Long startTimeMillis = timeline.getStartTimeMilliseconds();
        Long endTimeMillis = timeline.getEndTimeMilliseconds();
        
        if (live) {
            // live mode range is always 8 minutes in Mapview
            Assert.assertEquals(endTimeMillis - startTimeMillis, 8 * 60 * 1000);
        } else {
            // historic mode allows any time range
            Assert.assertEquals(endTimeMillis - startTimeMillis, origRange.getSeconds() * 1000);
        }
        
        // Go back
        leftNav.goToHomePage();
        
        landingPage = ui.getLandingPage();
        summaryPanel = landingPage.getSummaryPanel();
        
        if (live) {
        	TimeRange finalRange = summaryPanel.getSelectedTimeRange();
        	Assert.assertEquals(finalRange, origRange, "The selected time range should be the same after the user has returned back to Homepage");
        } else {
        	long range = (summaryPanel.getEndTime() - summaryPanel.getStartTime()) / 1000;
        	Assert.assertEquals(range, origRange.getSeconds(), "The selected time range should be the same after the user has returned back to Homepage");
        }
    }
    
    private void testPreserveTimeFromHomeToDashboardAndBackImpl(TimeRange origRange, boolean live) throws Exception {
        init();
        
        LandingPage landingPage = ui.getLandingPage();
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        
        summaryPanel.setTimeRange(origRange);
        
        if (!live) {
            summaryPanel.setTimeRange(TimeRange.CUSTOM_RANGE);
        }
        
        leftNav.goToDashboardPage();

        Timeline timeline = ui.getTimeline();
        timeline.expand();
                
        Assert.assertEquals(timeline.isLiveModeSelected(), live);
        
        Long startTimeMillis = timeline.getStartTimeMilliseconds();
        Long endTimeMillis = timeline.getEndTimeMilliseconds();
        
        if (live) {
            // live mode range is always 8 minutes in Mapview
            Assert.assertEquals(endTimeMillis - startTimeMillis, 8 * 60 * 1000);
        } else {
            // historic mode allows any time range
            Assert.assertEquals(endTimeMillis - startTimeMillis, origRange.getSeconds() * 1000);
        }
        
        // Go back
        leftNav.goToHomePage();
        
        landingPage = ui.getLandingPage();
        summaryPanel = landingPage.getSummaryPanel();
                
        if (live) {
        	TimeRange finalRange = summaryPanel.getSelectedTimeRange();
        	Assert.assertEquals(finalRange, origRange, "The selected time range should be the same after the user has returned back to Homepage");
        } else {
        	long range = (summaryPanel.getEndTime() - summaryPanel.getStartTime()) / 1000;
        	Assert.assertEquals(range, origRange.getSeconds(), "The selected time range should be the same after the user has returned back to Homepage");
        }
    }
    
    private void testPreserveTimeFromHomeToWebviewImpl(TimeRange r, boolean live) throws Exception {
        init();
        
        LandingPage landingPage = ui.getLandingPage();
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        
        summaryPanel.setTimeRange(r);
        
        if (!live) {
            summaryPanel.setTimeRange(TimeRange.CUSTOM_RANGE);
        }
        
        String wvUrl = topNav.getAnyWebviewLinkElement().getAttribute("href");
            
        logger.info("Webview URL: " + wvUrl);
        
        Map<String, Long> attr = UrlUtils.getMapOfParameterValuesFromWebViewUrl(wvUrl, true);
        Long endTimeMillis = attr.get("et");
        Long startTimeMillis = attr.get("st");
        Long timeRange = attr.get("tr");
        
        if (live) {
            Assert.assertNull(startTimeMillis, "The time range is live, there should be no value of the 'st' parameter (start time) in the WebView link URL.");
            Assert.assertNull(endTimeMillis, "The time range is live, there should be no value of the 'et' parameter (end time) in the WebView link URL.");
            Assert.assertEquals(timeRange, Long.valueOf(0L), "The time range is live, the value of the 'tr' parameter (time range) should be '0' in the WebView link URL.");
        } else {
            Assert.assertNotNull(startTimeMillis, "The time range is historic, there should be some value of the 'st' parameter (start time) in the WebView link URL.");
            Assert.assertNotNull(endTimeMillis, "The time range is historic, there should be some value of the 'et' parameter (end time) in the WebView link URL.");
            Assert.assertNotEquals(timeRange, Long.valueOf(0L), "The time range is historic, the value of the 'tr' parameter (time range) should not be '0' in the WebView link URL.");
            Assert.assertEquals(endTimeMillis - startTimeMillis, r.getSeconds() * 1000);
        }
    }

    @Test
    public void testPreserveLiveTimeFromMapToWebview() throws Exception {
        init();
        
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        Timeline timeline = ui.getTimeline();
        timeline.expand();
        timeline.turnOnLiveMode();
        
        String wvUrl = topNav.getAnyWebviewLinkElement().getAttribute("href");
        logger.info("Webview URL: " + wvUrl);
        
        Map<String, Long> attr = UrlUtils.getMapOfParameterValuesFromWebViewUrl(wvUrl, true);
        Long endTimeMillis = attr.get("et");
        Long startTimeMillis = attr.get("st");
        Long timeRange = attr.get("tr");
        
        Assert.assertNull(startTimeMillis, "The time range is live, there should be no value of the 'st' parameter (start time) in the WebView link URL.");
        Assert.assertNull(endTimeMillis, "The time range is live, there should be no value of the 'et' parameter (end time) in the WebView link URL.");
        Assert.assertEquals(timeRange, Long.valueOf(0L), "The time range is live, the value of the 'tr' parameter (time range) should be '0' in the WebView link URL.");
    }

    @Test 
    public void testPreserveHistoricTimeFromMapToWebview8Minutes() throws Exception {
    	testPreserveHistoricTimeFromMapToWebviewImpl(Timeline.MINUTES, "8");
    }
    
    @Test 
    public void testPreserveHistoricTimeFromMapToWebview40Minutes() throws Exception {
    	testPreserveHistoricTimeFromMapToWebviewImpl(Timeline.MINUTES, "40");
    }
    
    @Test 
    public void testPreserveHistoricTimeFromMapToWebview1Hour() throws Exception {
    	testPreserveHistoricTimeFromMapToWebviewImpl(Timeline.HOURS, "5");
    }

    @Test 
    public void testPreserveHistoricTimeFromMapToWebview1Day() throws Exception {
    	testPreserveHistoricTimeFromMapToWebviewImpl(Timeline.DAYS, "1");
    }
    
    private void testPreserveHistoricTimeFromMapToWebviewImpl(String timeUnit, String timeValue) throws Exception {
        init();
        
        ui.getLeftNavigationPanel().goToMapViewPage();
        Timeline timeline = ui.getTimeline();
        
        timeline.expand();
        timeline.turnOffLiveMode();
        Long startTimeTimeline = timeline.getStartTimeMilliseconds();
        Long endTimeTimeline = timeline.getEndTimeMilliseconds();
        
        String wvUrl = topNav.getAnyWebviewLinkElement().getAttribute("href");
        logger.info("Webview URL: " + wvUrl);
        
        Map<String, Long> attr = UrlUtils.getMapOfParameterValuesFromWebViewUrl(wvUrl, true);
        Long endTimeMillis = attr.get("et");
        Long startTimeMillis = attr.get("st");
        Long timeRange = attr.get("tr");
        
        Assert.assertNotNull(startTimeMillis, "The time range is historic, there should be some value of the 'st' parameter (start time) in the WebView link URL.");
        Assert.assertNotNull(endTimeMillis, "The time range is historic, there should be some value of the 'et' parameter (end time) in the WebView link URL.");
        Assert.assertNotEquals(timeRange, Long.valueOf(0L), "The time range is historic, the value of the 'tr' parameter (time range) should not be '0' in the WebView link URL.");
        Assert.assertEquals(startTimeMillis, startTimeTimeline, "The start time should be equal in MapView and in WebView");
        Assert.assertEquals(endTimeMillis, endTimeTimeline, "The end time should be equal in MapView and in WebView");
    }
}
