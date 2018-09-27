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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.test.atc.common.DetailsPanel.AttributeType;
import com.ca.apm.test.atc.common.element.AttributeRow;
import com.ca.apm.test.atc.common.element.PageElement;

public class Canvas {

    private final UI ui;

    private CanvasControl ctrl;

    private static final Logger logger = LoggerFactory.getLogger(Canvas.class);
    
    public static final String NODE_HIGHLIGHT = "#fffac2";
    public static final String NODE_SELECTION = "rgb(247,146,47)";
    // location of the Expand icon on the group element
    public static final String GROUP_EXPAND_LOC = "*[local-name()='g' and @id[starts-with(.,'GroupExpand')]]";
    // location of the Collapse icon on the group element
    public static final String GROUP_COLLAPSE_LOC = "*[local-name()='g' and @id[starts-with(.,'GroupCollapse')]]";

    public Canvas(UI ui) {
        this.ui = ui;
    }

    public CanvasControl getCtrl() {
        if (ctrl == null) {
            ctrl = new CanvasControl(ui, this);
        }
        
        return ctrl;
    }

    /**
     * Wait until Canvas is loaded
     */
    public void waitForUpdate() {
        ui.waitForWorkIndicator(By.id("appMapWorkIndicator"));
    }
    
    /**
     * Wait when switching between tabs without map re-layouting
     */
    public void waitForDisplay() {
        ui.waitUntilVisible(By.cssSelector("#graphContent svg g"));
        Utils.sleep(300);
    }

    /**
     * Return canvas element
     */
    public PageElement getCanvas() {
        this.waitForUpdate();
        this.waitForDisplay();
        return ui.getElementProxy(By.cssSelector("#graphCanvas"));
    }

    /**
     * Return list of nodes on the map
     */
    public List<PageElement> getListOfNodes() {
        return this.getCanvas()
        // notice the space after nodeSelector
            .findPageElements(
                By.xpath(".//*[local-name()=\"g\" and starts-with(@class,\"nodeSelector \")]"));
    }

    /**
     * Return list of single nodes (not groups) on the map
     */
    public List<PageElement> getListOfSingleNodes() {
        return this.getCanvas().findPageElements(
            By.xpath(".//*[local-name()=\"g\" and @class=\"nodeSelector graph-node\"]"));
    }

    public int getCountOfSelectedNodes() {
        return this.getCanvas().findPageElements(
            By.xpath(".//*[local-name()=\"g\"]/*[local-name()=\"rect\""
                + " and @fill=\"none\" and @stroke=\"" + NODE_SELECTION + "\"]")).size();
    }

    /**
     * Returns the entire name of the node - even if the full name is not visible.
     * @param node
     * @return String
     */
    public String getNodeName(WebElement node) {
        try {
            return node.findElement(By.xpath(".//*[local-name()='title']"))
                .getAttribute("textContent");
        } catch (Throwable ex) {
            final String msg = MessageFormat.format(
                "Failed to retrieve node title from node <{1}>. Exception: {0}",
                ex.getMessage(), node.getTagName());
            logger.error(msg, ex);
            throw new RuntimeException(msg, ex);
        }
    }

    /**
     * Return list of the highlighted nodes on the map
     */
    public List<PageElement> getListOfHighlightedNodes() {
        return this
            .getCanvas()
            .findPageElements(
                By.xpath(".//*[local-name()='g'"
                    + " and starts-with(@id,'Vertex_Border')]"
                    + "//*[local-name()='rect'"
                    + " and @class='t-hl-indicator'"
                    + " and @fill='" + NODE_HIGHLIGHT + "']"
                    + "/ancestor::*[local-name()='g'"
                    + " and starts-with(@class,'nodeSelector ')][1]"));
    }

    /**
     * Return array of names of nodes visible on the map
     */
    public String[] getArrayOfNodeNames() throws Exception {
        return Utils.callAgainOnStaleReferenceException(() -> {
            List<PageElement> titles = Canvas.this.getCanvas().findPageElements(
                By.xpath(".//*[local-name()='g' and starts-with(@class,'nodeSelector ')"
                    + " and (contains(@class,'graph-node') or contains(@class,'graph-group'))]"
                    //+ "//*[local-name()='g' and starts-with(@id,'Vertex_Border')]"
                    //+ "//*[local-name()='g' and @id='titleId']"
                    //+ "/*[local-name()='rect']"));
                    + "//*[local-name()='title']"
                    + "/.."));

            List<String> toReturn = new ArrayList<>(titles.size());
            for (PageElement el : titles) {
                toReturn.add(getNodeName(el));
            }
            logger.info("visible nodes on map: {}", toReturn);
            return toReturn.toArray(new String[toReturn.size()]);
        });
    }


    /**
     * Return list of nodes visible on the map that are groups for other nodes
     */
    public List<PageElement> getListOfGroups() {
        return this.getCanvas().findPageElements(
            By.xpath(".//*[local-name()='g' and @class='nodeSelector graph-group']"));
    }

    /**
     * Return visible node whose name contains given string
     * 
     * @param {string} name
     */
    public PageElement getNodeByNameSubstring(String name) {
        return this.getCanvas().findElement(
            By.xpath(".//*[local-name()='title' and text()[contains(.,'" + name + "')]]"
                + "/ancestor::*[local-name()='g' and starts-with(@class,'nodeSelector ')][1]"));
    }

    /**
     * Return visible node whose name equals given string
     * 
     * @param {string} name
     */
    public PageElement getNodeByName(String name) {
        return this.getCanvas().findElement(
            By.xpath(".//*[local-name()='title' and .='" + name + "']"
                + "/ancestor::*[local-name()='g' and starts-with(@class,'nodeSelector ')][1]"));
    }

    
    
    /**
     * Return current level of detail
     */
    public int getLevelOfDetail() {
        PageElement vrtx =
            this.getCanvas().findPageElements(By.cssSelector("g[id^=\"Vertex_Border\"]")).get(0);

        String attr = vrtx.getAttribute("id");

        return attr.contains("L1") ? 1 : attr.contains("L2")
            ? 2
            : attr.contains("L3") ? 3 : -1;
    }

    /**
     * Check if the node is highlighted
     * 
     * @param {object} node
     */
    public boolean isHighlighted(WebElement bTranNode) {
        String attr =
            bTranNode.findElement(By.cssSelector("g[id^=\"Vertex_Border\"] > rect:nth-child(1)"))
                .getAttribute("fill");
        return (attr.contains(NODE_HIGHLIGHT));
    }

    /**
     * Select node on the map
     * 
     * @param {object} node
     */
    public void selectNode(WebElement node) {
        node.click();
        Utils.sleep(500);
    }
    
    /**
     * Select map node by clicking on the right side of the node.
     * This avoids the event triggering the tooltips (node metrics data) 
     * @param nodeName
     */
    public void selectNodeByName(String nodeName) {
        getCtrl().fitAllToView();

        PageElement node = getNodeByNameSubstring(nodeName);
        Dimension dim = node.getSize();
        int width = dim.width;
        int height = dim.height;
        
        Actions builder = new Actions(ui.getDriver());
        builder.moveToElement(node, 2 * width / 3, height / 2);
        builder.click();
        builder.build().perform();
    }
    
    /**
     * Select map node(nodeName is an EXACT MATCH) by clicking on the right side of the node.
     * This avoids the event triggering the tooltips (node metrics data) 
     * @param nodeName
     */

     public void selectNodeByNameExactMatch(String nodeName) {
        getCtrl().fitAllToView();

        PageElement node = getNodeByName(nodeName);
        Dimension dim = node.getSize();
        int width = dim.width;
        int height = dim.height;
        
        Actions builder = new Actions(ui.getDriver());
        builder.moveToElement(node, 2 * width / 3, height / 2);
        builder.click();
        builder.build().perform();
    }
    

    /**
     * Fits all the elements of the map to the view and then clicks the most top-left corner
     * within canvas,
     * which is unused after that.
     * 
     * @throws Exception
     * @returns {*}
     */
    public void clickToUnusedPlaceInCanvas() {
        getCtrl().fitAllToView();
        
        Actions builder = new Actions(ui.getDriver());
        builder.moveToElement(getCanvas(), 2, 2);
        builder.click();
        builder.build().perform();
        
        Utils.sleep(500);
    }

    /**
     * Expand group
     * 
     * @param {object} node
     * @throws InterruptedException
     */
    public void expandGroup(WebElement bTranNode) {
        WebElement expandIcon = bTranNode.findElement(By.xpath("..//" + GROUP_EXPAND_LOC));
        new Actions(ui.getDriver()).moveToElement(expandIcon).click().build().perform();
        
        waitForUpdate();
        Utils.sleep(1500); // wait for layout animation - animation duration is 900ms
    }

    /**
     * Collapse group
     * 
     * @param {object} node
     * @throws InterruptedException
     */
    public void collapseGroup(PageElement node) {
    	WebElement collapseIcon = node.findElement(By.xpath("..//" + GROUP_COLLAPSE_LOC));
    	new Actions(ui.getDriver()).moveToElement(collapseIcon).click().build().perform();
    	
        waitForUpdate();
        Utils.sleep(1500); // wait for layout animation
    }
    
    public boolean isUvbPresentOnNode(WebElement el) {
        return el.findElements(By.xpath(".//*[local-name() = \"g\" and @id=\"uvbBar\"]/*[local-name() = \"use\"]")).size() == 1;
    }
    
    public boolean isStatusPresentOnNode(WebElement el) {
        return el.findElements(By.className("t-status-icon")).size() == 1;
    }

    public void deleteCustomAttributeFromNodesIfItExists(String[] nodeNames, String attributeName) throws Exception {
        logger.info("Called deleteCustomAttributeFromNodesIfItExists: attribute=" + attributeName + ", nodes=" + Arrays.asList(nodeNames).toString());
        
        final Canvas canvas = ui.getCanvas();
        final DetailsPanel details = ui.getDetailsPanel();
        ui.getFilterBy().hidePanel();
        
        for (final String nodeName : nodeNames) {
            canvas.selectNodeByName(nodeName);
            details.deleteAttribute(attributeName);
        }
        
        // Click out of the node so that the attributes with deleted value disappear from the attribute table
        canvas.clickToUnusedPlaceInCanvas();
    }

    public void addCustomAttributeToNode(String nodeName, String attributeName, String attributeValue) throws Exception {
        logger.info("Called addCustomAttributeToNode: attribute=" + attributeName + ", value=" + attributeValue + ", node=" + nodeName);
        
        logger.info("enabling live mode");
        ui.getTimeline().turnOnLiveMode();
        
        String[] nodeNames = new String[1];
        nodeNames[0] = nodeName;
    
        logger.info("make sure the attribute does not already exist");
        ui.getFilterBy().hidePanel();
        deleteCustomAttributeFromNodesIfItExists(nodeNames, attributeName);
    
        logger.info("select a group node");
        Canvas canvas = ui.getCanvas();
        canvas.getNodeByNameSubstring(nodeName).click();
        
        logger.info("expand the name attribute");
        DetailsPanel details = ui.getDetailsPanel();
        details.collapseSection(DetailsPanel.SECTION_PERFORMANCE_OVERVIEW);
        details.collapseSection(DetailsPanel.SECTION_ALERTS);
        details.waitUntilAttributeTableDataLoaded(AttributeType.BASIC_ATTRIBUTES);
        details.scrollToAttributesTable(AttributeType.BASIC_ATTRIBUTES);
        List<AttributeRow> nameAttrRows = details.getAttributeRowsByName(AttributeType.BASIC_ATTRIBUTES, "Name");
        Assert.assertEquals(nameAttrRows.size(), 1);
    
        nameAttrRows.get(0).getExpandIcon().click();
        nameAttrRows = details.getAttributeRowsByName(AttributeType.BASIC_ATTRIBUTES, "Name");
        Assert.assertTrue(nameAttrRows.size() > 1);
    
        logger.info("add a new attribute");
        details.addNewAttribute(attributeName, attributeValue, false);
    
        Utils.sleep(5000);
        
        Assert.assertTrue(details.isAttributeRowPresent(AttributeType.OTHER_ATTRIBUTES,
            attributeName, attributeValue, true, false));
    }
}
