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
package com.ca.apm.test.atc.timeline;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.Timeline;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;

public class ZoomingTest extends UITest {

    /** 
     * Enable as soon as the methods {@link Timeline#zoomIn(int)} and {@link Timeline#zoomOut(int)} work. 
     **/
    @Test(enabled=false)
    public void testTimelineZooming() throws Exception {
        UI ui = getUI();
        Timeline timeline = ui.getTimeline();
        
        ui.login();
        ui.getLeftNavigationPanel().goToMapViewPage();
        timeline.expand();
        Utils.sleep(Timeline.ANIMATION_DELAY);
        timeline.turnOffLiveMode();
        ui.waitForWorkIndicator();
        
        timeline.zoomOut(100);
        String dateLabel = timeline.getLastMinorLabel();
        // one slash or one hyphen sign in the middle?
        Assert.assertTrue(dateLabel.matches("^[^/\\s\\-]+(/|\\-){1}[^/\\s\\-]+$"));
        
        timeline.zoomIn(100);
        dateLabel = timeline.getLastMinorLabel();
        Assert.assertTrue(dateLabel.contains(":"));
        
        ui.cleanup();
    }
}
