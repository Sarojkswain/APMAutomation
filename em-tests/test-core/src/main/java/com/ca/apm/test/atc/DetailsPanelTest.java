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

import java.util.List;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.common.Canvas;
import com.ca.apm.test.atc.common.DetailsPanel;
import com.ca.apm.test.atc.common.PerspectivesControl;
import com.ca.apm.test.atc.common.Timeline;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.element.PageElement;

public class DetailsPanelTest extends UITest {

    private UI ui = null;
           
    private void checkCollapsibleSection(String id) throws Exception {
        DetailsPanel details = ui.getDetailsPanel();
        
        if (details.isSectionEnabled(id)) {
            details.collapseSection(id);
            Assert.assertTrue(details.isSectionCollapsed(id));
            ui.getRibbon().expandTimelineToolbar();
            Assert.assertTrue(details.isSectionCollapsed(id));
            
            details.expandSection(id);
            Assert.assertTrue(details.isSectionExpanded(id));
            ui.getRibbon().expandTimelineToolbar();
            Assert.assertTrue(details.isSectionExpanded(id));
            
        } else {
            logger.warn("Unable to test collapsing and expanding - {} section is disabled", id);
        }
    }
    
    @Test
    public void testContainersCollapsing() throws Exception {
        ui = getUI();
        ui.login();
        
        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.getTimeline().expand();
        
        Timeline timeline = ui.getTimeline();
        timeline.turnOffLiveMode();
        
        // map empty?
        Canvas canvas = ui.getCanvas();
        List<PageElement> allNodes = canvas.getListOfNodes();
        Assert.assertTrue(allNodes.size() > 0);
        
        ui.getPerspectivesControl().selectPerspectiveByName(
            PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        canvas.getCtrl().fitAllToView();
        
        final String TEST_NODE = "Login";
        
        // ALERTS
        WebElement node = canvas.getNodeByName(TEST_NODE);
        canvas.selectNode(node);
        checkCollapsibleSection(DetailsPanel.SECTION_ALERTS);
        
        // BASIC ATTRIBUTES
        node = canvas.getNodeByName(TEST_NODE);
        canvas.selectNode(node);
        checkCollapsibleSection(DetailsPanel.SECTION_BASIC_ATTRIBUTES);
        
        // CUSTOM ATTRIBUTES + PERFORMANCE OVERVIEW
        node = canvas.getNodeByName(TEST_NODE);
        canvas.selectNode(node);
        checkCollapsibleSection(DetailsPanel.SECTION_CUSTOM_ATTRIBUTES);
        checkCollapsibleSection(DetailsPanel.SECTION_PERFORMANCE_OVERVIEW);
        
        // CHANGE EVENTS
        timeline.checkAttributeChangeCheckbox();
        timeline.checkStatusChangeCheckbox();
        timeline.checkTopologicalChangeCheckbox();
        
        node = canvas.getNodeByName(TEST_NODE);
        canvas.selectNode(node);
        checkCollapsibleSection(DetailsPanel.SECTION_EVENTS_ATTRIBUTES);
        checkCollapsibleSection(DetailsPanel.SECTION_EVENTS_STATUS);
        checkCollapsibleSection(DetailsPanel.SECTION_EVENTS_TOPOLOGICAL);
    }

    
    @Test
    public void testPanelHiding() throws Exception {
        ui = getUI();

        logger.info("logging in as Admin");
        ui.login(Role.ADMIN);

        logger.info("switching to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();
        DetailsPanel panel = ui.getDetailsPanel();

        logger.info("checking that the panel is not displayed in Live Mode");
        ui.getTimeline().turnOnLiveMode();
        Assert.assertFalse(panel.isDetailsPanelDisplayed());

        logger.info("checking that the panel is displayed in History Mode");
        ui.getTimeline().turnOffLiveMode();
        Assert.assertTrue(panel.isDetailsPanelDisplayed());

        logger.info("checking that the panel is displayed in Live Mode after a node is selected");
        Canvas canvas = ui.getCanvas();
        ui.getTimeline().turnOnLiveMode();
        Assert.assertFalse(panel.isDetailsPanelDisplayed());
        canvas.selectNode(canvas.getListOfNodes().get(0));
        Assert.assertTrue(panel.isDetailsPanelDisplayed());

        logger.info("checking that the panel is maximized");
        if (panel.isDetailsPanelMinimized()) {
            panel.getMaximizeButton().click();
        }
        Assert.assertFalse(panel.isDetailsPanelMinimized());

        logger.info("checking that the panel can be minimized");
        panel.getMinimizeButton().click();
        Assert.assertTrue(panel.isDetailsPanelDisplayed());
        Assert.assertTrue(panel.isDetailsPanelMinimized());

        logger.info("checking the panel disappears after canceling the selection");
        canvas.clickToUnusedPlaceInCanvas();
        Assert.assertFalse(panel.isDetailsPanelDisplayed());

        logger
            .info("checking the panel reappears in minimized state after a node is selected again");
        canvas.selectNode(canvas.getListOfNodes().get(1));
        Assert.assertTrue(panel.isDetailsPanelDisplayed());
        Assert.assertTrue(panel.isDetailsPanelMinimized());

        logger.info("checking the panel can be maximized again");
        panel.getMaximizeButton().click();
        Assert.assertFalse(panel.isDetailsPanelMinimized());

        logger.info("checking that the panel stays visible after switching to History Mode");
        ui.getTimeline().turnOffLiveMode();
        Assert.assertTrue(panel.isDetailsPanelDisplayed());
        // in history mode, the panel should be visible even with no selection
        canvas.clickToUnusedPlaceInCanvas();
        Assert.assertTrue(panel.isDetailsPanelDisplayed());
    }
}
