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
package com.ca.apm.test.atc;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import com.ca.apm.test.atc.common.DetailsPanel;
import com.ca.apm.test.atc.common.DetailsPanel.Metric;
import com.ca.apm.test.atc.common.PerspectivesControl;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.WebView;

import static org.testng.Assert.*;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

public class DeepLinkingTest extends UITest {

    private static final String[] POTENTIAL_SERVICE_NODE_NAMES = { "PlaceOrder", "JspServlet", "DefaultServlet", "ServletA6" };
    private static final String SERVICE_NODE_NAME_SUFFIX = "|service";
    
    private String NODE_NAME;
    private String NODE_NAME_FULL;
    
    private static final String ALERT_NAME = "Servlet Errors";
    
    private UI prepare() throws Exception {
        UI ui = getUI();
        ui.login();
        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.getTimeline().turnOnLiveMode();
        ui.getCanvas().waitForUpdate();
        ui.getPerspectivesControl().selectPerspectiveByName(PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        
        List<String> existingNodes = Arrays.asList(ui.getCanvas().getArrayOfNodeNames());
        for (final String node : POTENTIAL_SERVICE_NODE_NAMES) {
            if (existingNodes.contains(node + SERVICE_NODE_NAME_SUFFIX)) {
                NODE_NAME = node;
                NODE_NAME_FULL = node + SERVICE_NODE_NAME_SUFFIX;
                
                logger.info("Using the existing node '{}' for this test", node);
                break;
            }
        }
        
        assertTrue(NODE_NAME != null, "There are no available nodes in the map to perform this test");
        
        return ui;
    }
    
    @Test
    public void testAlerts() throws Exception {
        UI ui = prepare();
        ui.getCanvas().getNodeByName(NODE_NAME_FULL).click();
        ui.getDetailsPanel().getAlertByName(ALERT_NAME).click();
        Thread.sleep(2000);

        ui.switchToWebView();
        
        ui.waitUntilVisible(By.id("webview-mmEditor-mmeditorContent-name-field-input"));

        String url = ui.getDriver().getCurrentUrl();
        String alertNameEncoded = URLEncoder.encode(ALERT_NAME, "UTF-8");
        assertTrue(url.contains(alertNameEncoded), "URL of WebView does not contain URL-encoded alert name '" + alertNameEncoded + "'");
        assertEquals(
            ui.getDriver().findElement(By.id("webview-mmEditor-mmeditorContent-name-field-input")).getAttribute("value"),
            ALERT_NAME,
            "Alert name in WebView should be " + ALERT_NAME);

        ui.switchToATC();
        
        Thread.sleep(1000);

        ui.getDetailsPanel().expandSection(DetailsPanel.SECTION_PERFORMANCE_OVERVIEW);
        ui.getDetailsPanel().getPerformanceOverviewLinkToWebView().click();
        Thread.sleep(2000);

        ui.switchToWebView();

        logger.info("Check if breadcrumb for " + NODE_NAME + " exists.");
        ui.waitUntilVisible(By.id("webview-investigator-breadcrumb-node-" + NODE_NAME));

        assertEquals(ui.getWebView().getTimeControllerSelectedValue(), WebView.TimeRange.LIVE.getLabel(), "WebView should be in Live mode");

        ui.switchToATC();

        ui.getTimeline().turnOffLiveMode();
        
        ui.getCanvas().getNodeByName(NODE_NAME_FULL).click();
        ui.getDetailsPanel().getAlertByName(ALERT_NAME).click();
        Thread.sleep(2000);

        ui.switchToWebView();

        assertEquals(ui.getWebView().getTimeControllerSelectedValue(), WebView.TimeRange.CUSTOM_RANGE.getLabel(), "WebView should be in Custom Range mode");
        
        ui.switchToATCandCloseWebViewTab();
        ui.cleanup();
    }

    @Test
    public void testBlameMetrics() throws Exception {
        String metricNameOnNode;
        
        UI ui = prepare();

        ui.getTimeline().turnOffLiveMode();
        ui.getCanvas().getNodeByName(NODE_NAME_FULL).click();

        ui.waitUntilVisible(ui.getDetailsPanel().getMetricElementSelector(Metric.AVG_RESPONSE_TIME));

        ui.getDetailsPanel().getMetricElement(Metric.AVG_RESPONSE_TIME).click();

        Thread.sleep(2000);

        ui.switchToWebView();

        ui.getWebView().waitForUpdateLegendGrid();

        metricNameOnNode = Metric.AVG_RESPONSE_TIME.getMetricNameOnNode(NODE_NAME); 
        assertTrue(
            ui.getWebView().getMetricNameInMetricBrowserUnderGraph().contains(metricNameOnNode),
            metricNameOnNode + " is not visible in WebView.");

        assertEquals(ui.getWebView().getTimeControllerSelectedValue(), WebView.TimeRange.CUSTOM_RANGE.getLabel(), "WebView is not in Custom Range mode.");

        ui.switchToATC();

        ui.getDetailsPanel().getMetricElement(Metric.CONCURRENT_INVOCATIONS).click();

        ui.switchToWebView();

        Thread.sleep(2000);

        ui.getWebView().waitForUpdateLegendGrid();

        metricNameOnNode = Metric.CONCURRENT_INVOCATIONS.getMetricNameOnNode(NODE_NAME); 
        assertTrue(
            ui.getWebView().getMetricNameInMetricBrowserUnderGraph().contains(metricNameOnNode),
            metricNameOnNode + " metric is not visible in WebView.");

        assertEquals(ui.getWebView()
            .getTimeControllerSelectedValue(), WebView.TimeRange.CUSTOM_RANGE.getLabel(), "WebView is not in Custom Range mode.");

        ui.switchToATC();

        ui.getDetailsPanel().getMetricElement(Metric.ERRORS_PER_INTERVAL).click();

        ui.switchToWebView();

        Thread.sleep(2000);

        ui.getWebView().waitForUpdateLegendGrid();

        metricNameOnNode = Metric.ERRORS_PER_INTERVAL.getMetricNameOnNode(NODE_NAME); 
        assertTrue(
            ui.getWebView().getMetricNameInMetricBrowserUnderGraph().contains(metricNameOnNode),
            metricNameOnNode + " metric is not visible in WebView.");

        assertEquals(ui.getWebView().getTimeControllerSelectedValue(), WebView.TimeRange.CUSTOM_RANGE.getLabel(), "WebView is not in Custom Range mode.");

        ui.switchToATC();

        ui.getDetailsPanel().getMetricElement(Metric.STALL_COUNT).click();

        ui.switchToWebView();

        Thread.sleep(2000);

        ui.getWebView().waitForUpdateLegendGrid();

        metricNameOnNode = Metric.STALL_COUNT.getMetricNameOnNode(NODE_NAME); 
        assertTrue(
            ui.getWebView().getMetricNameInMetricBrowserUnderGraph().contains(metricNameOnNode),
            metricNameOnNode + " metric is not visible in WebView.");

        assertEquals(ui.getWebView().getTimeControllerSelectedValue(), WebView.TimeRange.CUSTOM_RANGE.getLabel(), "WebView is not in Custom Range mode.");

        ui.cleanup();
    }
}
