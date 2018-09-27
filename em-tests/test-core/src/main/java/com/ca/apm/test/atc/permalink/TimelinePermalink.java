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
package com.ca.apm.test.atc.permalink;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.Timeline;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UrlUtils;

public class TimelinePermalink extends UITest {

    private UI ui = null;
    private WebDriver driver = null;
        
    private void init() throws Exception {
        ui = getUI();
        driver = ui.getDriver();
        ui.login();
        ui.getLeftNavigationPanel().goToMapViewPage();
    }
    
    private void waitForURLchange(int timeoutInSec, String url) {
        final String _url = url;
        
        ui.doWait(timeoutInSec).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return !d.getCurrentUrl().equals(_url);
            }
        });
    }
    
    @Test
    public void testTimeParams() throws Exception {
        init();
        
        Timeline timeline = ui.getTimeline();
        timeline.turnOnLiveMode();
        timeline.turnOffLiveMode();
        
        String url = driver.getCurrentUrl();
        String ts1 = UrlUtils.getQueryStringParam(url, "ts1");
        String ts2 = UrlUtils.getQueryStringParam(url, "ts2");
        Assert.assertNotNull(ts1);
        Assert.assertNotNull(ts2);
        
        timeline.openStartTimeCalendar();
        timeline.getStartTimeCalendarMinuteDecreaseBtn().click();
        timeline.calendarApply();
        waitForURLchange(5, url);
        
        timeline.openEndTimeCalendar();
        timeline.getEndTimeCalendarMinuteDecreaseBtn().click();
        timeline.calendarApply();
        waitForURLchange(5, url);
        
        url = driver.getCurrentUrl();
        Assert.assertNotEquals(UrlUtils.getQueryStringParam(url, "ts1"), ts1);
        Assert.assertNotEquals(UrlUtils.getQueryStringParam(url, "ts2"), ts2);
    }
    
    @Test
    public void testTimeBookmark() throws Exception {
        init();
        
        Timeline timeline = ui.getTimeline();
        timeline.turnOffLiveMode();
        
        final long now = (System.currentTimeMillis() / 1000) * 1000;  // remove milliseconds
        final long tenMinsAgo = now - 600000L;
        
        String url = ui.getCurrentUrl();
        url = url.substring(0, url.indexOf('?'));
        url += "?ts1=" + tenMinsAgo;
        url += "&ts2=" + now;
        url += "&m=H";
        driver.get(url);  // single get doesn't work here because of hash sign in URL
        driver.navigate().refresh();  // workaround is to use refresh
        
        DateFormat df = new SimpleDateFormat("M/d/yy h:mm:ss a", Locale.US);
        String endTime = timeline.getEndTime();
        Assert.assertEquals(df.parse(endTime).getTime(), now);
        
        String startTime = timeline.getStartTime();
        Assert.assertEquals(df.parse(startTime).getTime(), tenMinsAgo);
    }

}
