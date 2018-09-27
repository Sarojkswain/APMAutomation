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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.common.FilterBy;
import com.ca.apm.test.atc.common.Ribbon;
import com.ca.apm.test.atc.common.Timeline;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;

/**
 * This test exercises the expand/collapse views of Filters, highlight, timeline and highlight
 * search widget
 * This functionality exists in both map and trend/card view.
 * This test also confirms default expanded/collapsed settings.
 */

public class ExpandCollapseViewsTest extends UITest {

    /**
     * This test will verify the search widget is not shown by default, only exposed on click of the
     * search button.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchWidgetExpand() throws Exception {
        UI ui = getUI();
        ui.login();

        ui.getLeftNavigationPanel().goToMapViewPage();

        logger.info("search input should not be visible at first");
        By searchInputID = By.id("searchInputID");
        WebElement searchInput = ui.getDriver().findElement(searchInputID);

        boolean isClickable = true;
        // it has to be checked like this, because it is visible, but with zero width
        try {
            searchInput.click();
        } catch (WebDriverException e) {
            isClickable = false;
        }
        Assert.assertFalse(isClickable);

        logger.info("search input should be visible/clickable when expanded");
        ui.getDriver().findElement(By.className("search-image-button")).click();
        ui.getDriver().findElement(searchInputID).click();

        ui.cleanup();
    }

    /**
     * This test confirms the defaults set by ng-init="initMapViews defined in views/map.html
     * 
     * @throws Exception
     */
    @Test
    public void testConfirmDefaultExpandedForMapView() throws Exception {
        UI ui = getUI();
        ui.login();

        ui.doWait(10).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return !d.getTitle().isEmpty();
            }
        });

        Ribbon ribbon = ui.getRibbon();
        ui.getLeftNavigationPanel().goToMapViewPage();

        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.TIMELINE));
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.HIGHLIGHTING));
        
        ui.cleanup();
    }

    /**
     * This confirms the defaults set in ng-init="initMapViews of ../views/trendView.html
     * 
     * @throws Exception
     */
    @Test
    public void testConfirmDefaultExpandedForTrendView() throws Exception {
        UI ui = getUI();
        ui.login();

        ui.doWait(10).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return !d.getTitle().isEmpty();
            }
        });

        Ribbon ribbon = ui.getRibbon();
        ui.getLeftNavigationPanel().goToMapViewPage();

        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.TIMELINE));
        
        ui.cleanup();
    }

    /**
     * This test will exercise the expand/collapse buttons for the map view (map.html)
     * 
     * @throws Exception
     */
    @Test
    public void testExpandCollapseViewsForMap() throws Exception {
        UI ui = getUI();
        ui.login();

        ui.doWait(10).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return !d.getTitle().isEmpty();
            }
        });

        Ribbon ribbon = ui.getRibbon();
        Timeline timeline = ui.getTimeline();
        FilterBy filter = ui.getFilterBy();

        ui.getLeftNavigationPanel().goToMapViewPage();
        filter.add("Type");
        timeline.expand();
        Utils.sleep(Timeline.ANIMATION_DELAY);
        
        Assert.assertTrue(timeline.isExpanded());
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
                
        ribbon.expandToolbar(Ribbon.CollapsibleToolbar.HIGHLIGHTING);
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.HIGHLIGHTING));
        Assert.assertTrue(timeline.isExpanded());
        
        filter.hidePanel();
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.HIGHLIGHTING));
        Assert.assertTrue(timeline.isExpanded());
        
        timeline.collapse();
        Utils.sleep(Timeline.ANIMATION_DELAY);
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.HIGHLIGHTING));
        Assert.assertFalse(timeline.isExpanded());
        
        ribbon.collapseToolbar(Ribbon.CollapsibleToolbar.HIGHLIGHTING);
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.HIGHLIGHTING));
        Assert.assertFalse(timeline.isExpanded());

        filter.showPanel();
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.HIGHLIGHTING));
        Assert.assertFalse(timeline.isExpanded());

        timeline.expand();
        Utils.sleep(Timeline.ANIMATION_DELAY);

        Assert.assertTrue(timeline.isExpanded());
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.HIGHLIGHTING));
        
        ribbon.expandToolbar(Ribbon.CollapsibleToolbar.HIGHLIGHTING);
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.HIGHLIGHTING));
        Assert.assertTrue(timeline.isExpanded());
        
        ui.cleanup();
    }

    /**
     * This test will exercise the expand/collapse buttons for the trendView (trendView.html)
     * 
     * @throws Exception
     */
    @Test
    public void testExpandCollapseViewsForCard() throws Exception {
        UI ui = getUI();
        ui.login();

        Ribbon ribbon = ui.getRibbon();
        FilterBy filter = ui.getFilterBy();
        Timeline timeline = ui.getTimeline();

        ui.getLeftNavigationPanel().goToDashboardPage();
        filter.add("Type");

        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));

        timeline.expand();
        Utils.sleep(Timeline.ANIMATION_DELAY);

        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.TIMELINE));

        filter.hidePanel();
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.TIMELINE));

        timeline.collapse();
        Utils.sleep(Timeline.ANIMATION_DELAY);

        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.TIMELINE));

        filter.showPanel();
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.FILTER_BY));
        Assert.assertFalse(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.TIMELINE));

        timeline.expand();
        Utils.sleep(Timeline.ANIMATION_DELAY);

        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.TIMELINE));
        Assert.assertTrue(ribbon.isToolbarExpanded(Ribbon.CollapsibleToolbar.TIMELINE));
        
        ui.cleanup();
    }
}
