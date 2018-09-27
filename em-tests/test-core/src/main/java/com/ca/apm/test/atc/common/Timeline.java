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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

import com.ca.apm.test.atc.common.element.PageElement;

public class Timeline {

    public static final String DAYS = "days";
    public static final String HOURS = "hours";
    public static final String MINUTES = "minutes";
    public static final String CUSTOM = "custom";
    
    public static final String STATUS_CHANGE = "status";
    public static final String TOPOLOGICAL_CHANGE = "topological";
    public static final String ATTRIBUTE_CHANGE = "attribute";

    public static final Pattern START_TIME_REGEX = Pattern.compile("\\b([0-9]+)-[0-9]+\\b");
    public static final Pattern END_TIME_REGEX = Pattern.compile("\\b[0-9]+-([0-9]+)\\b");
 
    public static final String START_TIME_DATEPICKER_ID = "startTimeDtpDialog";
    public static final String END_TIME_DATEPICKER_ID = "endTimeDtpDialog";
    
    public static final String MODE_LIVE = "LIVE";
    public static final String MODE_HISTORIC = "HISTORIC";
    
    public static final int ANIMATION_DELAY = 500;

    private UI ui;

    public Timeline(UI ui) {
        this.ui = ui;
    }

    public PageElement getLiveRadioBtn() {
        return ui.getElementProxy(By.id("live-mode-true-label"));
    };

    public PageElement getHistoricRadioBtn() {
        return ui.getElementProxy(By.id("live-mode-false-label"));
    };

    public PageElement getLiveRadioBtnLabel() {
        return ui.getElementProxy(By.id("live-mode-true-label"));
    };

    public PageElement getHistoricRadioBtnLabel() {
        return ui.getElementProxy(By.id("live-mode-false-label"));
    };

    public String getStartTime() {
        return ui.getElementProxy(By.id("start-time-indication")).getAttribute("value");
    };
    
    public Long getStartTimeMilliseconds() throws ParseException {
        return Long.valueOf(ui.getElementProxy(By.id("start-time-indication")).getAttribute("millis"));
    }

    public String getEndTime() {
        return ui.getElementProxy(By.id("end-time-indication")).getAttribute("value");
    };
    
    public long getRange() throws ParseException {
        DateFormat df = new SimpleDateFormat("M/d/yy h:mm:ss a", Locale.US);
        String startTime = getStartTime();
        String endTime = getEndTime();
        return df.parse(endTime).getTime() - df.parse(startTime).getTime();
    }
    
    public Long getEndTimeMilliseconds() throws ParseException {
        return Long.valueOf(ui.getElementProxy(By.id("end-time-indication")).getAttribute("millis"));
    }

    public PageElement getEndTimeMinutesDecreaser() {
        return ui.getElementProxy(By.id("dtpTime")).findElement(
            By.cssSelector("[ng-click=\"decrementMinutes()\"]"));
    };

    public PageElement getEndTimePickerApplyBtn() {
        return ui.getElementProxy(By.cssSelector("#dtpDialog > div:last-child")).findElement(
            By.cssSelector("[ng-click=\"apply()\"]"));
    };

    public PageElement getAttributeChangesCheckbox() {
        return ui.getInvisibleElementProxy(By.id("gatheredAttributesChangeCheckbox"));
    };

    public PageElement getStatusChangesCheckbox() {
        return ui.getInvisibleElementProxy(By.id("statusChangeCheckbox"));
    };

    public PageElement getTopologicalChangesCheckbox() {
        return ui.getInvisibleElementProxy(By.id("topologicalChangeCheckbox"));
    };

    public PageElement getAttributeChangesCheckboxLabel() {
        return ui
            .getElementProxy(By.cssSelector("label[for=\"gatheredAttributesChangeCheckbox\"]"));
    };

    public PageElement getStatusChangesCheckboxLabel() {
        return ui.getElementProxy(By.cssSelector("label[for=\"statusChangeCheckbox\"]"));
    };

    public PageElement getTopologicalChangesCheckboxLabel() {
        return ui.getElementProxy(By.cssSelector("label[for=\"topologicalChangeCheckbox\"]"));
    };

    public PageElement getTimeline() {
        return ui.getElementProxy(By.id("timeline-timeline"));
    };
    
    public PageElement getTimelineWrapper() {
        return ui.getElementProxy(By.className("timeline-wrapper"));
    }
    
    public String getLastMinorLabel() {
        List<PageElement> dateLabels = getTimeline()
                .findPageElements(By.cssSelector("div[class~=\"vis-text\"][class~=\"vis-minor\"]"));
        
        String lastLbl = dateLabels.get(dateLabels.size() - 1).getText();
        // in some cases (e.g. after zooming) empty labels can still remain on the end of timeline
        if (lastLbl.isEmpty()) { 
            lastLbl = dateLabels.get(dateLabels.size() - 2).getText();
        }
        
        return lastLbl;
    }
    
    public PageElement getFirstMinorLabelElement() {
        List<PageElement> labels = getTimeline()
                .findPageElements(By.cssSelector("div[class~=\"vis-text\"][class~=\"vis-minor\"]"));
        
        // Find first element that is visible and clickable
        for (PageElement label : labels) {
            String leftValue = label.getCssValue("left");
            double leftPos = 0;
            
            try {
                leftValue = leftValue.substring(0, leftValue.indexOf("px"));
                leftPos = Math.floor(Float.parseFloat(leftValue));
            } catch (StringIndexOutOfBoundsException e) {
                // CSS "left" value can be null or "auto", skip
            }

            if (leftPos > 0) {
                return label;
            }
        }
        
        return labels.get(0);
    }
    
    public PageElement getStartTimeBar() {
        return getTimeline().findElement(
            By.cssSelector("div[class=\"vis-custom-time startTime\"]"));
    }
    
    public PageElement getEndTimeBar() {
        return getTimeline().findElement(
            By.cssSelector("div[class=\"vis-custom-time endTime\"]"));
    }

    public List<PageElement> getInRangeEventsByType(String typeClass) {
        try {
            String selector = "div[class~=\"vis-item\"][class~=\"vis-box\"][class~=\"" + typeClass + "\"][class~=\"in_range\"]";
            return ui.waitUntilElementsVisible(By.cssSelector(selector), 10);
        } catch (TimeoutException e) {
            return Collections.<PageElement>emptyList();
        }
    };

    public List<PageElement> getAllEventsByType(String typeClass) {
        return getTimeline().findPageElements(
            By.cssSelector("div[class~=\"vis-item\"][class~=\"vis-box\"][class~=\"" + typeClass
                + "\"]"));
    };

    public PageElement getFirstInRangeEventByType(String typeClass) {
        return getInRangeEventsByType(typeClass).get(0);
    };

    public PageElement getNthInRangeEventByType(String typeClass, int n) {
        return getInRangeEventsByType(typeClass).get(n);
    };

    public PageElement getLastInRangeEventByType(String typeClass) {
        List<PageElement> list = getInRangeEventsByType(typeClass);
        return list.get(list.size() - 1);
    };

    public boolean isEventInRange(WebElement event) {
        return event.getAttribute("class").indexOf("in_range") != -1;
    }

    public boolean isEventOutOfRange(WebElement event) {
        return event.getAttribute("class").indexOf("out_range") != -1;
    }

    public int getCountOfAllEventsByType(String typeClass) {
        return getAllEventsByType(typeClass).size();
    };

    public int getCountOfInRangeEventsByType(String typeClass) {
        return getInRangeEventsByType(typeClass).size();
    };

    public String getTimeStartOfEvent(WebElement timelineEvent) {
        String classValue = timelineEvent.getAttribute("class");
        Matcher m = START_TIME_REGEX.matcher(classValue);
        m.find();
        return m.group(1);
    };

    public String getTimeEndOfEvent(WebElement timelineEvent) {
        String classValue = timelineEvent.getAttribute("class");
        Matcher m = END_TIME_REGEX.matcher(classValue);
        m.find();
        return m.group(1);
    };

    public String getVertexIdsOfEvent(WebElement timelineEvent) {
        return timelineEvent.getAttribute("data-vertexids");
    };

    public boolean isLiveModeSelected() {
        return getTimelineWrapper()
                .findElement(By.className("mode-indicator"))
                .getText()
                .equals(MODE_LIVE);
    };

    public boolean isHistoricModeSelected() {
        return getTimelineWrapper()
                .findElement(By.className("mode-indicator"))
                .getText()
                .equals(MODE_HISTORIC);
    };

    public boolean isAttributeChangeSelected() {
        return getAttributeChangesCheckbox().isSelected();
    };

    public boolean isStatusChangeSelected() {
        return getStatusChangesCheckbox().isSelected();
    };

    public boolean isTopologicalChangeSelected() {
        return getTopologicalChangesCheckbox().isSelected();
    };
    
    private void expandModeSelector() {
        getTimelineWrapper().findElement(By.id("time-range-selection-combo")).click();
    }
    
    private PageElement getModeSelectionDropdown() {
        return ui.getElementProxy(By.id("time-range-selection-combo-menu"));
    }
    
    private List<WebElement> getModeSelectionDropdownOptions() {
        return getModeSelectionDropdown().findElements(By.tagName("li"));
    }
    
    private void selectModeByIndex(int index) {
        getModeSelectionDropdownOptions()
            .get(index)
            .findElement(By.tagName("a")).click();
    }

    /**
     * Turn off the Live mode/Turn on the Historic mode
     */
    public void turnOffLiveMode() {
        if (isLiveModeSelected()) {
            expandModeSelector();
            List<WebElement> options = getModeSelectionDropdownOptions();
            options.get(options.size() - 1).findElement(By.tagName("a")).click();
            ui.getCanvas().waitForUpdate();
        }
    };
    
    /**
     * Turn on the Live mode/Turn off the Historic mode
     */
    public void turnOnLiveMode() {
        if (!isLiveModeSelected()) {
            expandModeSelector();
            selectModeByIndex(0);
            ui.getCanvas().waitForUpdate();
        }
    };

    /**
     * Turn on/off live mode
     */
    public void toggleLiveMode() {
        if (isLiveModeSelected()) {
            getHistoricRadioBtn().click();
        } else {
            getLiveRadioBtn().click();
        }
        ui.getCanvas().waitForUpdate();
    };
    
    /**
     * Set Timeline range
     */
    public void setRange(String range) {
        PageElement select = ui.getElementProxy(By.id("timeline-time-units"));
        select.click();
        select.findElement(By.cssSelector("option[label=\"" + range + "\"]")).click();
    }
    
    public PageElement getStartTimeCalendarDialog() {
        return ui.getElementProxy(By.id(START_TIME_DATEPICKER_ID));
    }
    
    public PageElement getEndTimeCalendarDialog() {
        return ui.getElementProxy(By.id(END_TIME_DATEPICKER_ID));
    }
    
    public PageElement getAnyCalendarDialog() {
        for (PageElement dialog : ui.findElements(By.className("dtpDialog"))) {
            if (dialog.isDisplayed()) {
                return dialog;
            }
        }
        
        return null;
    }
    
    public void openStartTimeCalendar() {
        getTimelineWrapper().findElement(By.id("start-time-indication")).click();
    }
    
    public void openEndTimeCalendar() {
        getTimelineWrapper().findElement(By.id("end-time-indication")).click();
    }
    
    public PageElement getStartTimeCalendarMinuteDecreaseBtn() {
        return getStartTimeCalendarDialog().findElement(By.cssSelector("[ng-click=\"decrementMinutes()\"]"));
    }
    
    public PageElement getEndTimeCalendarMinuteDecreaseBtn() {
        return getEndTimeCalendarDialog().findElement(By.cssSelector("[ng-click=\"decrementMinutes()\"]"));
    }
    
    public PageElement getStartTimeCalendarMinuteIncreaseBtn() {
        return getStartTimeCalendarDialog().findElement(By.cssSelector("[ng-click=\"incrementMinutes()\"]"));
    }
    
    public PageElement getStartTimeCalendarHourDecreaseBtn() {
        return getStartTimeCalendarDialog().findElement(By.cssSelector("[ng-click=\"decrementHours()\"]"));
    }
    
    public PageElement getStartTimeCalendarHourIncreaseBtn() {
        return getStartTimeCalendarDialog().findElement(By.cssSelector("[ng-click=\"incrementHours()\"]"));
    }
    
    public void calendarApply() {
        getAnyCalendarDialog().findElement(By.cssSelector("[ng-click=\"apply()\"]")).click();
        ui.waitForWorkIndicator();
    }
    
    // TODO: doesn't work with vis.js 4.2.0
    public void dragEndTimeBarBeforeWindowStart() {
        PageElement endTBar = getEndTimeBar();
        PageElement frame = getTimeline();

        Actions builder = new Actions(ui.getDriver());
        Action dragEndTime = builder.clickAndHold(endTBar)
                .moveToElement(frame, 0, 0)
                .release(frame)
                .build();
        dragEndTime.perform();
    }

    public void selectEvent(PageElement event) {
        event.findElement(By.cssSelector("img")).click();
    };

    public void checkAttributeChangeCheckbox() {
        if (!isAttributeChangeSelected()) {
            getAttributeChangesCheckboxLabel().click();
            Utils.sleep(1500);
        }
    };

    public void uncheckAttributeChangeCheckbox() {
        if (isAttributeChangeSelected()) {
            getAttributeChangesCheckboxLabel().click();
            Utils.sleep(1500);
        }
    };

    public void checkStatusChangeCheckbox() {
        if (!isStatusChangeSelected()) {
            getStatusChangesCheckboxLabel().click();
            Utils.sleep(1500);
        }
    };

    public void uncheckStatusChangeCheckbox() {
        if (isStatusChangeSelected()) {
            getStatusChangesCheckboxLabel().click();
            Utils.sleep(1500);
        }
    };

    public void checkTopologicalChangeCheckbox() {
        if (!isTopologicalChangeSelected()) {
            getTopologicalChangesCheckboxLabel().click();
            Utils.sleep(1500);
            ui.getCanvas().waitForUpdate();
        }
    };

    public void uncheckTopologicalChangeCheckbox() {
        if (isTopologicalChangeSelected()) {
            getTopologicalChangesCheckboxLabel().click();
            Utils.sleep(1500);
            ui.getCanvas().waitForUpdate();
        }
    }
    
    /**
     * Depending on 'clicks' parameter sign zooms the timeline in or out.
     * Use negative number for zooming in and positive number for zooming the timeline out.
     * @param clicks umber of mouse wheel notches
     */
    private void timelineZoom(int clicks) {
        // current visjs version doesn't know wheel event (only mousewheel)
        // and expects the default scroll value to be 120 per notch for mousewheel
        int deltaY = clicks < 0 ? -120 : 120;
        final String zoomScript =
            "var timeline = document.getElementsByClassName('vis-timeline')," +
            "    clicks = " + Math.abs(clicks) + "," +
            "    deltaY = " + deltaY + "," +
            "    clientX = timeline[0].getBoundingClientRect().left + 100; " +
            "for (var e, i=0; i < clicks; i++) {" +
            "    e = new WheelEvent('wheel', { 'deltaY': deltaY, 'deltaMode': 1, 'clientX': clientX });" +
            "    timeline[0].dispatchEvent(e);" +
            "}";
        
        JavascriptExecutor jse = (JavascriptExecutor) ui.getDriver();
        jse.executeScript(zoomScript);
        
        Utils.sleep(ANIMATION_DELAY);
    }
    
    public void zoomIn(int clicks) {
        clicks *= -1;
        timelineZoom(clicks);
    }
    
    public void zoomOut(int clicks) {
        timelineZoom(clicks);
    }
    
    protected int getElementPosition(String position) {
        return Integer.parseInt(position.substring(0, position.indexOf(".")));
    }
    
    /**
     * Set calendar time. Negative values are rounded to 0
     * @param hours
     * @param minutes
     */
    public void setStartTimeCalendarTime(int hours, int minutes) {
        openStartTimeCalendar();
        
        if (hours < 0) {
            hours = 0;
        }
        
        if (minutes < 0) {
            minutes = 0;
        }
        
        PageElement inputHours = ui.getElementProxy(By.cssSelector("#startTimeDtpDialog .hours > input"));
        PageElement inputMinutes = ui.getElementProxy(By.cssSelector("#startTimeDtpDialog .minutes > input"));
        
        inputHours.clear();
        inputHours.sendKeys(String.valueOf(hours));
        inputMinutes.clear();
        inputMinutes.sendKeys(String.valueOf(minutes));
        
        calendarApply();
    }
    
    public PageElement getTimelineToggleControl() {
        return getTimelineWrapper().findElement(By.cssSelector("[ng-click=\"toggleTimeline()\"]"));
    }
    
    public boolean isExpanded() {
        return getTimelineWrapper().findElement(By.id("timeline-wrapper")).isDisplayed();
    }
    
    public void collapse() {
        if (isExpanded()) {
            getTimelineToggleControl().click();
            Utils.sleep(ANIMATION_DELAY);
        }
    }
    
    public void expand() {
        if (!isExpanded()) {
            getTimelineToggleControl().click();
            Utils.sleep(ANIMATION_DELAY);
        }
    }
}
