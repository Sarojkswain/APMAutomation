package com.ca.apm.nextgen.tests.helpers;

import static com.ca.apm.nextgen.tests.common.WvToolsTabConstants.HISTORICAL_EVENT_VIEWER_CONTAINER_ID;
import static com.ca.apm.nextgen.tests.common.WvToolsTabConstants.HISTORICAL_EVENT_VIEWER_TAB_NAME;
import static com.ca.apm.nextgen.tests.common.WvToolsTabConstants.LIVE_ERROR_VIEWER_CONTAINER_GRID_TREE_ID;
import static com.ca.apm.nextgen.tests.common.WvToolsTabConstants.LIVE_ERROR_VIEWER_GRID_ID;
import static com.ca.apm.nextgen.tests.common.WvToolsTabConstants.LIVE_ERROR_VIEWER_STATUS_BAR_ID;
import static com.ca.apm.nextgen.tests.common.WvToolsTabConstants.LIVE_ERROR_VIEWER_TAB_NAME;
import static com.ca.apm.nextgen.tests.common.WvToolsTabConstants.TRANSACTION_TRACER_TAB_GRID_ID;
import static com.ca.apm.nextgen.tests.common.WvToolsTabConstants.TRANSACTION_TRACER_TAB_NAME;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Utilities class providing helper functions for UI manipulation on the TOOLS tab of the WebView. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ToolsTabUtils {

    private static final String TAB_SPAN_XPATH_TEMPLATE = "//span[.='%s']";
    private static final String LIVE_ERROR_VIEWER_STATUS_BAR_EVENTS_FOUND_XPATH_TEMPLATE = "//div[@id='%s']/div/div[1]/span[2]";
    private static final String LIVE_ERROR_VIEWER_STATUS_BAR_TIMESTAMP_XPATH_TEMPLATE = "//div[@id='%s']/div/div[2]/span[2]";

    private WebViewUi ui;

    /**
     * Constructor.
     * 
     * @param ui  common UI helper 
     */
    public ToolsTabUtils(WebViewUi ui) {
        this.ui = ui;
    }

    /**
     * Returns a {@link WebElement} belonging to the status bar of the 'Live Error Viewer' tab and 
     * containing the number of events found information. 
     * 
     * @return web element
     */
    public WebElement getNumOfLiveErrorsFoundStatusBarElement() {
        String numOfEventsFoundXpath = String.format(LIVE_ERROR_VIEWER_STATUS_BAR_EVENTS_FOUND_XPATH_TEMPLATE, 
            LIVE_ERROR_VIEWER_STATUS_BAR_ID);
        return ui.getWebElement(By.xpath(numOfEventsFoundXpath));
    }

    /**
     * Returns a {@link WebElement} belonging to the status bar of the 'Live Error Viewer' tab and 
     * containing the timestamp information. 
     * 
     * @return web element
     */
    public WebElement getLiveErrorsStatusBarTimestampElement() {
        String timestampXpath = String.format(LIVE_ERROR_VIEWER_STATUS_BAR_TIMESTAMP_XPATH_TEMPLATE, 
            LIVE_ERROR_VIEWER_STATUS_BAR_ID);
        return ui.getWebElement(By.xpath(timestampXpath));
    }

    /**
     * Returns a {@link TableUi} wrapper for the live error event table 
     * on the 'Live Error Viewer' tab.  
     * 
     * @return
     */
    public TableUi getLiveErrorsTable() {
        return new TableUi(ui, By.xpath(String.format("//div[@id='%s']", LIVE_ERROR_VIEWER_GRID_ID)));  
    }
    
    /**
     * Clicks on 'Live Error Viewer' tab.
     * 
     */
    public void clickLiveErrorViewerTab() {
        clickSubTab(LIVE_ERROR_VIEWER_TAB_NAME);
        ui.waitForWebElement(By.id(LIVE_ERROR_VIEWER_CONTAINER_GRID_TREE_ID));
    }
    
    /**
     * Clicks on 'Historical Event Viewer' tab.
     * 
     */
    public void clickHistoricalEventViewerTab() {
        clickSubTab(HISTORICAL_EVENT_VIEWER_TAB_NAME);
        ui.waitForWebElement(By.id(HISTORICAL_EVENT_VIEWER_CONTAINER_ID));
    }
    
    /**
     * Clicks on 'Transaction Tracer' tab.
     * 
     */
    public void clickTransactionTracerTab() {
        clickSubTab(TRANSACTION_TRACER_TAB_NAME);
    }
    
    private void clickSubTab(String tabName) {
        WebElement toolsTableGridElem = ui.waitForWebElement(By.id(TRANSACTION_TRACER_TAB_GRID_ID));
        ui.getWebElement(toolsTableGridElem, By.xpath(String.format(TAB_SPAN_XPATH_TEMPLATE, tabName))).click();
    }

    
}
