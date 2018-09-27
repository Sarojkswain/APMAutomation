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
package com.ca.apm.test.atc.status;

import static org.testng.Assert.assertTrue;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.CommonMapViewUITest;
import com.ca.apm.test.atc.common.Canvas;
import com.ca.apm.test.atc.common.DetailsPanel.Metric;
import com.ca.apm.test.atc.common.PerspectivesControl;
import com.ca.apm.test.atc.common.Status;

public class StatusTest extends CommonMapViewUITest {
    
    private static final String GROUP_IN_PERSP1 = "SERVLET - type";
    
    private static final String NODE_IN_NO_GROUP_PERSPECTIVE = "ServletA6|service";

    @Override
    protected void init() throws Exception {
        ui = getUI();
        
        ui.login();
        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.getTimeline().turnOnLiveMode();
        
        super.init();
    }
    
    @Test
    public void testStatus() throws Exception {
        init();

        if (!ui.getPerspectivesControl().isPerspectivePresent(PERSP1)) {
            ui.getPerspectivesControl().addPerspective(PERSP1, false);
            ui.getPerspectivesControl().selectPerspectiveByName(PERSP1);
        } else if (!ui.getPerspectivesControl().isPerspectiveActive(PERSP1)) {
            ui.getPerspectivesControl().selectPerspectiveByName(PERSP1);
        }

        WebElement typeServletGroup = ui.getCanvas().getNodeByNameSubstring(GROUP_IN_PERSP1);
        assertTrue(typeServletGroup.findElement(By.cssSelector("#statusBar")).isDisplayed(),
                "Status was not found on group " + GROUP_IN_PERSP1);

        ui.getPerspectivesControl().selectPerspectiveByName(PerspectivesControl.NO_GROUPS_PERSPECTIVE);

        WebElement node =
                ui.getCanvas().getNodeByNameSubstring(NODE_IN_NO_GROUP_PERSPECTIVE);

        if (!ui.getCanvas().isStatusPresentOnNode(node)) {
            logger.warn("Status not found on node " + NODE_IN_NO_GROUP_PERSPECTIVE);
        }

        node.click();

        ui.waitUntilVisible(
                        By.xpath("//*[@id=\"alert-summary-container\"]//table[@ng-if=\"cStatus !== null\"]"));
        Thread.sleep(500);

        if (ui.getDetailsPanel().getAlertsList().size() > 0) {
            String statusText = ui.getDetailsPanel().getAlertStatus();
            assertTrue(Status.contains(statusText),
                "Status text " + statusText + " is not OK or Danger or Caution.");
        } else {
            logger.warn("Alert statuses not found, skipping tests.");
        }

        ui.getPerspectivesControl().selectDefaultPerspective();
        ui.getLeftNavigationPanel().goToDashboardPage();

        if (!ui.getPerspectivesControl().isPerspectivePresent(PERSP1)) {
            ui.getPerspectivesControl().addPerspective(PERSP1, false);
        }

        ui.getPerspectivesControl().selectPerspectiveByName(PERSP1);
    }

    @Test
    public void testApplicationTransactionStatus() throws Exception {
        init();
        
        ui.getTimeline().turnOffLiveMode();

        logger.info("Select the group node '{}'", PERSP2_GROUP1_NAME);
        Canvas canvas = ui.getCanvas();
        WebElement appGroup = canvas.getNodeByNameSubstring(PERSP2_GROUP1_NAME);
        appGroup.click();
        
        List<String> appGroupAlerts = ui.getDetailsPanel().getAlertsList();
        logger.info("Alerts in right panel '{}' ", appGroupAlerts);
        if (appGroupAlerts.size() > 0) {
            String appGroupStatus = ui.getDetailsPanel().getAlertStatus();
            
            assertTrue(Status.contains(appGroupStatus),
                "Status text " + appGroupStatus + " is not OK or Danger or Caution.");
            
            assertTrue(ui.getDetailsPanel().getMetricElement(Metric.AVG_RESPONSE_TIME).isDisplayed());

            logger.info("Expand the group '{}'", appGroup);
            canvas.expandGroup(appGroup);
            
            logger.info("Select the node '{}'", PERSP2_GROUP1_TEST_SUBNODE_NAME);
            WebElement appNode = canvas.getNodeByName(PERSP2_GROUP1_TEST_SUBNODE_NAME);
            appNode.click();
            
            String appNodeStatus = ui.getDetailsPanel().getAlertStatus();
            
            assertTrue(Status.contains(appNodeStatus),
                "Status text " + appNodeStatus + " is not OK or Danger or Caution.");
            
            List<String> appNodeAlerts = ui.getDetailsPanel().getAlertsList();
            logger.info("Alerts in right panel: '{}'", appNodeAlerts);
            
            assertTrue(appNodeAlerts.size() > 0);
            assertTrue(ui.getDetailsPanel().getMetricElement(Metric.AVG_RESPONSE_TIME).isDisplayed());

            logger.info("Verify alerts for node are a subset of alerts for group");
            
            assertTrue(statusToInt(appNodeStatus) <= statusToInt(appGroupStatus));
            assertTrue(appNodeAlerts.size() <= appGroupAlerts.size());
            
            for (String nodeAlert : appNodeAlerts) {
                assertTrue(appGroupAlerts.contains(nodeAlert), "Alert not contained: " + nodeAlert);
            }
        } else {
            logger.warn("Alert statuses not found, skipping this part of test case.");
        }
    }

    private int statusToInt(String status) {
        return Status.getByStatusName(status).ordinal();
    }
}
