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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.element.AttributeRow;
import com.ca.apm.test.atc.common.element.PageElement;

import static com.ca.apm.test.atc.common.element.WebElementWrapper.wrapElement;

public class DetailsPanel {

    private final static Logger logger = Logger.getLogger(DetailsPanel.class);
    private final String DETAILS_PANEL_CSS_CLASS = "side-panel";
    
    private final UI ui;

    /* Sections of the details panel */

    public static final String SECTION_ALERTS_SUMMARY = "alert-summary-container";
    public static final String SECTION_ALERTS = "alert-list-container";
    public static final String SECTION_EVENTS_STATUS = "event-table-STATUS_CHANGE";
    public static final String SECTION_EVENTS_ATTRIBUTES = "event-table-GATHERED_ATTRIBUTES_CHANGE";
    public static final String SECTION_EVENTS_TOPOLOGICAL = "event-table-TOPOLOGICAL_CHANGE";
    public static final String SECTION_PERFORMANCE_OVERVIEW = "performance-overview-container";
    public static final String SECTION_IDENT_ATTRIBUTES = "ident-attributes";
    public static final String SECTION_BASIC_ATTRIBUTES = "basic-attributes";
    public static final String SECTION_CUSTOM_ATTRIBUTES = "other-attributes";

    public PageElement getCollapsibleSection(String id) {
        return this.getDetailsPanel().findElement(
                By.cssSelector("#" + id + " div.attribute-table-header"));
    }
    
    /* Metrics */

    public enum Metric {
        AVG_RESPONSE_TIME("Average Response Time (ms)"),
        ERRORS_PER_INTERVAL("Errors Per Interval"),
        STALL_COUNT("Stall Count"),
        CONCURRENT_INVOCATIONS("Concurrent Invocations");
        
        private final String label;
        
        private Metric(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getMetricNameOnNode(String nodeName) {
            return nodeName + ":" + label;
        }
    }
    
    public DetailsPanel(UI ui) {
        this.ui = ui;
    }

    
    public PageElement getDetailsPanel() {
        return ui.getElementProxy(By.className(DETAILS_PANEL_CSS_CLASS));
    }
    
    public void waitUntilVisible() {
        ui.waitUntilVisible(By.className(DETAILS_PANEL_CSS_CLASS));
    }

    public boolean isNoComponentSelected() {
        String text =
                this.getDetailsPanel()
                        .findElement(By.cssSelector("div.attribute-panel-help span"))
                        .getText();
        return text.equals("No map component selected");
    }
    
    public boolean isDetailsPanelDisplayed() {
        return getDetailsPanel().isDisplayed();
    }
    
    public PageElement getMinimizeButton() {
        return getDetailsPanel().findElement(By.id("sidePanelMinimizeIcon"));
    }
 
    public PageElement getMaximizeButton() {
        return getDetailsPanel().findElement(By.id("sidePanelMaximizeButton"));
    }

    public boolean isDetailsPanelMinimized() {
        return getMaximizeButton().isDisplayed();
    }

    /**
     * Return a performance link to WebView.
     * NOTE: The method waits until the element is available.
     * 
     * @return
     */
    public PageElement getPerformanceOverviewLinkToWebView() {
        return ui.getElementProxy(
                By.cssSelector("#" + SECTION_PERFORMANCE_OVERVIEW
                        + " div.performanceOverviewContainer a"));
    }

    public void scrollToSection(String id) {
        this.getCollapsibleSection(id).scrollIntoView();
    }

    public boolean isSectionCollapsed(String id) {
        String src =
                this.getCollapsibleSection(id)
                        .findElement(By.cssSelector("img.collapse-icon"))
                        .getAttribute("src");
        return src.indexOf("Expand") != -1;
    }

    public boolean isSectionExpanded(String id) {
        String src =
                this.getCollapsibleSection(id)
                        .findElement(By.cssSelector("img.collapse-icon"))
                        .getAttribute("src");
        return src.indexOf("Collapse") != -1;
    }

    public boolean isSectionEnabled(String id) {
        try {
            String clazz = this.getCollapsibleSection(id).getAttribute("class");
            
            String[] splitted = clazz.split(" ");
            for (String s : splitted) {
                if (s.indexOf("enabled") != -1) {
                    return true;
                }
            }
        } catch (NoSuchElementException e) {
            return false;
        }
        return false;
    }

    public boolean isSectionDisabled(String id) {
        String clazz = this.getCollapsibleSection(id).getAttribute("class");

        String[] splitted = clazz.split(" ");
        for (String s : splitted) {
            if (s.indexOf("disabled") != -1) {
                return true;
            }
        }
        return false;
    }

    public void expandSection(String id) throws Exception {
        if (this.isSectionCollapsed(id)) {
            this.getCollapsibleSection(id).click();
            Thread.sleep(500);
        }

    }

    public void collapseSection(String id) throws InterruptedException {
        if (this.isSectionExpanded(id)) {
            this.getCollapsibleSection(id).click();
            Thread.sleep(500);
        }
    }

    /* Events */

    public static final int MAX_EVENTS_LIMIT = 1000;

    public static final String EVENT_TABLE_COLUMN_NODE_NAME = "nodeName";
    public static final String EVENT_TABLE_COLUMN_EVENT_TIME = "eventTime";

    public int getColumnOrder(EventType evType, String column) {
        if (column.equals(DetailsPanel.EVENT_TABLE_COLUMN_EVENT_TIME)) {
            if ((evType == EventType.TOPOLOGICAL_CHANGE) || (evType == EventType.STATUS_CHANGE)) {
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    public int getColumnCount(EventType evType) {
        if (evType == EventType.ATTRIBUTE_CHANGE) {
            return 5;
        } else {
            return 3;
        }
    }

    public PageElement getEventsPanel(EventType evType) {
        return ui.getElementProxy(By.id("event-table-" + evType.asText()));
    }

    // Number of elements can be a number or a string such as "1000+". We have to make a number of
    // that in all cases.
    public int getEventsCount(EventType evType) {
        String countAsString = this.getEventsPanel(evType).getAttribute("number-of-elements");
        if (countAsString.endsWith("+")) {
            countAsString = countAsString.replace("+", "");
            return Integer.parseInt(countAsString) + 1;
        } else {
            return Integer.parseInt(countAsString);
        }
    }

    public List<PageElement> getEventTableColumns(EventType evType) {
        return this.getEventsPanel(evType).findPageElements(
                By.cssSelector("div.ngHeaderContainer div.ngHeaderCell"));
    }

    public int getEventTableColumnCount(EventType evType) {
        return this.getEventTableColumns(evType).size();
    }

    public PageElement getEventTableColumnByIndex(EventType evType, int colIndex) {
        return this.getEventTableColumns(evType).get(colIndex)
                .findElement(By.cssSelector("div.ngHeaderSortColumn"));
    }

    public boolean isEventTableColumnSortedInAscendingWay(EventType evType, int colIndex) {
        return this.getEventTableColumnByIndex(evType, colIndex)
                .findElement(By.cssSelector("div.ngSortButtonDown")).isDisplayed();
    }

    public boolean isEventTableColumnSortedInDescendingWay(EventType evType, int colIndex) {
        return this.getEventTableColumnByIndex(evType, colIndex)
                .findElement(By.cssSelector("div.ngSortButtonUp")).isDisplayed();
    }

    public List<PageElement> getEventTableRows(EventType evType) {
        PageElement eventsPanel = this.getEventsPanel(evType);
        try {
            return eventsPanel.findPageElements(By
                .cssSelector("div.ngViewport div.ngRow"));
        } catch (Exception e) {
            logger.error("Event table " + evType + " not found.");
            return Collections.<PageElement>emptyList();
        }
    }

    public PageElement getFirstEventTableRow(EventType evType) {
        return this.getEventTableRows(evType).get(0);
    }

    public PageElement getNthEventTableRow(EventType evType, int n) {
        return this.getEventTableRows(evType).get(n);
    }

    public PageElement getLastEventTableRow(EventType evType) {
        List<PageElement> els = this.getEventTableRows(evType);
        return els.get(els.size() - 1);
    }

    public int getEventTableRowCount(EventType evType) {
        return this.getEventTableRows(evType).size();
    }

    public List<PageElement> getHighlightedEventTableRows(EventType evType) {
        List<PageElement> els = this.getEventTableRows(evType);
        List<PageElement> toReturn = new ArrayList<PageElement>();
        for (PageElement el : els) {
            toReturn.addAll(el.findPageElements(By
                    .xpath("//div[contains(@class,\"highlightedRow\")]/parent::div")));
            if (el.getAttribute("class").contains("selected")) {
                toReturn.add(el);
            }
        }
        return toReturn;
    }

    public List<PageElement> getRowsAboveHighlightedSection(EventType evType) {
        List<PageElement> highlightedRows = this.getHighlightedEventTableRows(evType);
        if (highlightedRows.size() > 0) {
            return highlightedRows.get(0).findPageElements(
                By.xpath("./preceding-sibling::div[contains(@class,\"ngRow\")]"));
        } else {
            return getEventTableRows(evType);
        }
    }

    public List<PageElement> getRowsBelowHighlightedSection(EventType evType) {
        List<PageElement> highlightedRows = this.getHighlightedEventTableRows(evType);
        if (highlightedRows.size() > 0) {
            return highlightedRows.get(highlightedRows.size() - 1).findPageElements(
                By.xpath("./following-sibling::div[contains(@class,\"ngRow\")]"));
        } else {
            return Collections.<PageElement>emptyList();
        }
    }

    public int getHighlightedEventTableRowCount(EventType evType) {
        return this.getHighlightedEventTableRows(evType).size();
    }

    public List<String> getValuesFromColumn(List<PageElement> rows, int columnNum) {
        List<String> els = new ArrayList<String>();
        // List<WebElement> rows =
        // column.findElements(By.cssSelector("div[class~='ngCellText'][class~='col'] span"));
        for (PageElement row : rows) {
            String selector = "div.ngCellText.col" + columnNum + " span";
            PageElement value = row.findElement(By.cssSelector(selector));
            els.add(value.getText());
        }
        return els;

    }

    public void sortEventTableColumnInAscendingWay(EventType evType, int colIndex)
            throws InterruptedException {

        if (!this.isEventTableColumnSortedInAscendingWay(evType, colIndex)) {
            this.getEventTableColumnByIndex(evType, colIndex).click();
            Thread.sleep(500);
            // expect(me.isEventTableColumnSortedInAscendingWay(evType, colIndex)).toBeTruthy(
            // "The column #" + colIndex +
            // " should have been marked as sorted in the ascending way.");
        }
    }

    public void sortEventTableColumnInDescendingWay(EventType evType, int colIndex)
            throws InterruptedException {
        if (!this.isEventTableColumnSortedInDescendingWay(evType, colIndex)) {
            this.getEventTableColumnByIndex(evType, colIndex).click();
            Thread.sleep(500);
            // expect(me.isEventTableColumnSortedInDescendingWay()).toBeTruthy(
            // "The column #" + colIndex +
            // " should have been marked as sorted in the descending way.");
        }

    }

    /* Attributes */
    
    public static final String EMPTY_ATTR_STRING = "<empty>";

    public static enum AttributeType {
        IDENT_ATTRIBUTES(2, SECTION_IDENT_ATTRIBUTES),
        BASIC_ATTRIBUTES(1, SECTION_BASIC_ATTRIBUTES), 
        OTHER_ATTRIBUTES(0, SECTION_CUSTOM_ATTRIBUTES);

        private int tableId;
        private String containerId;

        AttributeType(int tableId, String containerId) {
            this.tableId = tableId;
            this.containerId = containerId;
        }

        public int getTableId() {
            return tableId;
        }

        public String getContainerId() {
            return containerId;
        }
    }

    private List<AttributeRow> wrapRows(List<PageElement> rows, AttributeType type) {
        List<AttributeRow> list = new ArrayList<AttributeRow>(rows.size());
        for (PageElement row : rows) {
            list.add(new AttributeRow(row, type));
        }
        return list;
    }

    public PageElement getAttributesPanel(AttributeType attrType) {
        try {
            final String selector =
                attrType == AttributeType.IDENT_ATTRIBUTES
                    ? "div[ng-grid=\"gridOptionsIdent\"]"
                    : "collapsible-attribute-container#" + attrType.getContainerId();
            return ui.getElementProxy(By.cssSelector(selector));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void waitUntilAttributeTableDataLoaded(AttributeType attrType) {
        ui.waitForWorkIndicator(By.cssSelector(
            "collapsible-attribute-container#" + attrType.getContainerId() + " work-indicator"));
        final By nameXpath = By.xpath("//*[@id=\"other-attributes\"]//div[. = \"Name\"]");
        PageElement nameElement = wrapElement(ui.doWait(20).until(
            ExpectedConditions.presenceOfElementLocated(nameXpath)), ui);
        nameElement.scrollIntoView();
        ui.waitUntilVisible(nameXpath);
    }
    
    public String getAttributesCount(AttributeType attrType) {
        return this.getAttributesPanel(attrType).getAttribute("number-of-elements");
    }

    public List<AttributeRow> getAttributeRows(AttributeType attrType) {
        return wrapRows(getAttributesPanel(attrType).findPageElements(By.cssSelector(".ngRow.even, .ngRow.odd")), attrType);
    }
    
    public List<PageElement> getEventsRows(String section) {
        return ui.waitUntilElementsVisible(By.cssSelector("#" + section + " .ngRow"), 10);
    }

    public List<AttributeRow> getAttributeRowsByName(AttributeType attrType, String name) {
        final String xpath = ".//div[starts-with(@id,'name_view_table" + attrType.getTableId()
            + "_row')]/span[.='" + name + "']/../../../../../..";
        return wrapRows(getAttributesPanel(attrType).findPageElements(By.xpath(xpath)), attrType);
    }
    
    public List<AttributeRow> getAttributeRowsByNameAsync(AttributeType attrType, String name) {
        final String xpath = ".//div[starts-with(@id,'name_view_table" + attrType.getTableId()
                + "_row')]/span[.='" + name + "']/../../../../../..";
        ui.waitUntilVisible(By.xpath(xpath), 10);
        return wrapRows(getAttributesPanel(attrType).findPageElements(By.xpath(xpath)), attrType);
    }

    public AttributeRow getAttributeRowByNameAndValue(AttributeType attrType, String name, String value) {
        final String xpath = ".//div[starts-with(@id,'name_view_table" + attrType.getTableId()
            + "_row')]/span[.='" + name + "']/../../../../../.."
            + "//div[starts-with(@id,'time0_view_table" + attrType.getTableId() + "_row')]"
            + "/div/span[.='" + value + "']/../../../../../../..";
        return new AttributeRow(getAttributesPanel(attrType).findElement(By.xpath(xpath)).getWrappedElement(), this.ui, attrType);
    }
    
    public AttributeRow getAttributeRowByNameAndValueAsync(AttributeType attrType, String name, String value) {
        final String xpath = ".//div[starts-with(@id,'name_view_table" + attrType.getTableId()
            + "_row')]/span[.='" + name + "']/../../../../../.."
            + "//div[starts-with(@id,'time0_view_table" + attrType.getTableId() + "_row')]"
            + "/div/span[.='" + value + "']/../../../../../../..";
        ui.waitUntilVisible(By.xpath(xpath), 10);
        return new AttributeRow(getAttributesPanel(attrType).findElement(By.xpath(xpath)).getWrappedElement(), this.ui, attrType);
    }

    public AttributeRow getLastAttributeRow(AttributeType attrType) {
        List<AttributeRow> rows = getAttributeRows(attrType);
        if (rows.size() > 0) {
            return rows.get(rows.size() - 1);
        } else {
            return null;
        }
    }

    public boolean isAttributeRowPresent(final AttributeType attrType, final String attributeName,
        final String attributeValue, final Boolean editable, final Boolean async) throws Exception {
        return Utils.<Boolean>callAgainOnStaleReferenceException(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                scrollToAttributesTable(attrType);
                List<AttributeRow> rows;
                if (async) {
                    rows = getAttributeRowsByNameAsync(attrType, attributeName);
                } else {
                    rows = getAttributeRowsByName(attrType, attributeName);
                }

                if (rows.size() == 0) {
                    return false;
                }
                if (attributeValue == null && editable == null) {
                    return true;
                }
                for (AttributeRow row : rows) {
                    String valueCellText = row.getEndTimeValueCell().getText();
                    boolean deleteButtonPresent = row.getDeleteIcon().isDisplayed();
                    if (attributeValue != null && !valueCellText.equals(attributeValue)) {
                        continue;
                    }
                    if (editable != null && deleteButtonPresent != editable) {
                        continue;
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public boolean isAttributeRowPresent(AttributeType attrType, String attributeName,
        String attributeValue) throws Exception {
        return isAttributeRowPresent(attrType, attributeName, attributeValue, null, false);
    }

    public boolean isAttributeRowPresent(AttributeType attrType, String attributeName,
        boolean editable) throws Exception {
        return isAttributeRowPresent(attrType, attributeName, null, editable, false);
    }

    public boolean isAttributeRowPresent(AttributeType attrType, String attributeName)
        throws Exception {
        return isAttributeRowPresent(attrType, attributeName, null, null, false);
    }
    
    public PageElement getAttributeTableValueHeader(AttributeType attrType) {
        By locator = By.cssSelector("#" + attrType.getContainerId() + " .ngHeaderContainer .ngHeaderCell.col2");
        return ui.getElementProxy(locator, 4);
    }

    public List<String> getAttributeEndTimeValues(final AttributeType attrType, final String name)
        throws Exception {
        return Utils.<List<String>>callAgainOnStaleReferenceException(new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                List<String> toReturn = new ArrayList<String>();
                List<AttributeRow> rows = DetailsPanel.this.getAttributeRowsByName(attrType, name);
                for (AttributeRow row : rows) {
                    toReturn.add(row.getEndTimeValueCell().getText());
                }
                return toReturn;
            }
        });
    }

    public PageElement getAttributeInputField(AttributeType attrType) {
        By locator = By.xpath(".//input[contains(@id,'_edit_table')]");
        return ui.getElementProxy(locator, 4);
    }

    public void scrollToAttributesTable(AttributeType attrType) {
        scrollToSection(attrType.getContainerId());
    }
    
    /**
     * @param name
     * @param value
     * @param isAttributeRule - null if dialog with radio options is not expected
     * @throws Exception
     */
    public void addNewAttribute(final String name, final String value, final Boolean isAttributeRule) throws Exception {
        waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        scrollToAttributesTable(AttributeType.OTHER_ATTRIBUTES);
        
        Utils.runAgainOnStaleReferenceException(new Runnable() {
            @Override
            public void run() {
                getAttributeTableValueHeader(AttributeType.OTHER_ATTRIBUTES).click();

                AttributeRow lastRow = getLastAttributeRow(AttributeType.OTHER_ATTRIBUTES);
                lastRow.getNameCell().click();
                getAttributeInputField(AttributeType.OTHER_ATTRIBUTES).sendKeys(name);

                lastRow.getEndTimeValueCell().click();
                getAttributeInputField(AttributeType.OTHER_ATTRIBUTES).sendKeys(value + "\n");
            }
        });
        
        if (isAttributeRule != null) {
            int radioOption = isAttributeRule ? 1 : 0;
            ModalDialog dialog = ui.getModalDialog();
            dialog.getRadioOption(radioOption).click();
            dialog.clickButton(DialogButton.CONTINUE);
        }
        
        waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
    }
    
    public void deleteAttribute(final String attrName) {
        waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        Utils.runAgainOnStaleReferenceException(new Runnable() {
            @Override
            public void run() {
                for (final AttributeRow r : getAttributeRowsByName(AttributeType.OTHER_ATTRIBUTES, attrName)) {
                    r.getDeleteIcon().click(); 
                }
            }
        });
        
        waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
    }
    
    public void deleteAttribute(final String attrName, final String attrValue) {
        waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
        Utils.runAgainOnStaleReferenceException(new Runnable() {
            @Override
            public void run() {
                getAttributeRowByNameAndValue(AttributeType.OTHER_ATTRIBUTES, attrName, attrValue)
                    .getDeleteIcon().click();
            }
        });
        
        waitUntilAttributeTableDataLoaded(AttributeType.OTHER_ATTRIBUTES);
    }
            
    public List<PageElement> getPerformanceHeaderLinks() {
        return ui.findElements(By.cssSelector("p.performanceOverviewMetricName > a:not(.ng-hide), p.performanceOverviewMetricName > span:not(.ng-hide)"));
    }

    public By getMetricElementSelector(Metric metric) {
        return By
            .xpath("//p[@class=\"performanceOverviewMetricName\"]/a[. = \"" + metric.getLabel() + "\" and not(contains(@class, \"ng-hide\"))]"
                + "|//p[@class=\"performanceOverviewMetricName\"]/span[. = \"" + metric.getLabel() + "\" and not(contains(@class, \"ng-hide\"))]");
    }
    
    public PageElement getMetricElement(Metric metric) {
        return ui.getElementProxy(getMetricElementSelector(metric));
    }

    public PageElement getAlertSummaryPanel() {
        By locator = By.id(SECTION_ALERTS_SUMMARY);
        return ui.getElementProxy(locator);
    }
    
    public PageElement getAlertsPanel() {
        By locator = By.id(SECTION_ALERTS);
        return ui.getElementProxy(locator);
    }

    public String getAlertStatus() {
        By locator = By.xpath("id(\"" + SECTION_ALERTS_SUMMARY + "\")//table[@ng-if=\"cStatus !== null\"]//tr[1]/td[2]/span");
        return ui.getElementProxy(locator).getText();
    }
    
    public List<String> getAlertsList() {
        List<PageElement> elements = getAlertsPanel().findPageElements(By.cssSelector(".alert-list-item a"));
        List<String> result = new ArrayList<>(elements.size());
        for (PageElement el : elements) {
            String alertText = el.getText();
            if (!alertText.isEmpty()) {
                int parenthPos = alertText.lastIndexOf(" (");
                if (parenthPos >= 0) {
                    alertText = alertText.substring(0, parenthPos);
                }
                result.add(alertText);
            }
        }
        return result;
    }
   
    public PageElement getAlertByName(String name) {
        final String xpath = "//*[@id=\"alert-list-container\"]/div/div[2]/div[@class=\"alert-list-item ng-scope\"]/a[contains(text(), \"" + name + "\")]";
        final By locator = By.xpath(xpath);
        return ui.getElementProxy(locator);
    }
}
