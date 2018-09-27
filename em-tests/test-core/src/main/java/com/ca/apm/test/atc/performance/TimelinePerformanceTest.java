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
package com.ca.apm.test.atc.performance;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.common.Timeline;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;

public class TimelinePerformanceTest extends PerformanceTest implements RepeatableTest {
    
    private int counter = 0;
    
    @Test
    @Parameters({ "testIterations" })
    public void testDecreasingEndTime(int iterations) {
        try {
            UI ui = getUI();
            ui.login(Role.ADMIN);
            
            ui.getLeftNavigationPanel().goToMapViewPage();
            ui.getRibbon().expandTimelineToolbar();
            ui.getTimeline().turnOnLiveMode();
            ui.getCanvas().waitForUpdate();
            
            runTest(this, iterations);
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
    }

    @Override
    public void oneLoop() {
        UI ui = getUI();
        Timeline timeline = ui.getTimeline();

        timeline.openStartTimeCalendar();
        if ((counter / 10) % 2 == 0) {
            timeline.getStartTimeCalendarMinuteDecreaseBtn().click();
        }
        else {
            timeline.getStartTimeCalendarMinuteIncreaseBtn().click();
        }
        timeline.calendarApply();
        ui.getCanvas().waitForUpdate();
        
        counter++;
    }

}
