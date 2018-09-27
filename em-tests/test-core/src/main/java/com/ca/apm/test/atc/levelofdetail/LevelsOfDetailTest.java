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
package com.ca.apm.test.atc.levelofdetail;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.PerspectivesControl;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;

public class LevelsOfDetailTest extends UITest {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    @Test
    public void switchingZoomLevels_test() throws Exception {
        UI ui = getUI();

        logger.info("should log into Team Center");
        ui.login(Role.ADMIN);
        ui.doWait(10).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return !d.getTitle().isEmpty();
            }
        });

        logger.info("should switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();

        logger.info("should turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        logger.info("should zoom to level 1");
        ui.getPerspectivesControl().selectPerspectiveByName(PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        String firstNodeName = ui.getCanvas().getArrayOfNodeNames()[0];
        ui.getCanvas().selectNodeByName(firstNodeName);
        ui.getZoom().zoomIn(2);
        Assert.assertEquals(ui.getCanvas().getLevelOfDetail(), 1);

        logger.info("should zoom to level 2");
        ui.getZoom().zoomOut(1);
        Assert.assertEquals(ui.getCanvas().getLevelOfDetail(), 2);
        
        logger.info("should zoom to level 3");
        ui.getZoom().zoomOut(1);
        Assert.assertEquals(ui.getCanvas().getLevelOfDetail(), 3);
        
        logger.info("should zoom back to level 1");
        ui.getZoom().zoomIn(2);
        Assert.assertEquals(ui.getCanvas().getLevelOfDetail(), 1);
        
        ui.cleanup();
    }
}
